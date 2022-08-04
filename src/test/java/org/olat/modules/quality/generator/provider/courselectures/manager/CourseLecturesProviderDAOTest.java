/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.provider.courselectures.manager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseLecturesProviderDAOTest extends OlatTestCase {
	
	private Date hour = new Date();

	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private CourseLecturesProviderDAO sut;
	
	@Test
	public void shouldLoadLectureBlockEndDate() {
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Date start = nextHour();
		Date end = nextHour();
		createLectureBlock(course, teacher, 1, start, end);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		LectureBlockInfo info = infos.get(0);
		assertThat(info.getLectureEndDate()).isCloseTo(end, 1000);
	}

	@Test
	public void shouldLoadLecturesTotal() {
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		int lecturesPlaned1 = 3;
		createLectureBlock(course, teacher, lecturesPlaned1);
		int lecturesPlaned2 = 4;
		createLectureBlock(course, teacher, lecturesPlaned2);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		LectureBlockInfo info = infos.get(0);
		assertThat(info.getLecturesTotal()).isEqualTo(lecturesPlaned1 + lecturesPlaned2);
	}
	
	@Test
	public void shouldLoadLecturesFrom() {
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		int lecturesPlaned1 = 3;
		LectureBlock lectureBlock1 = createLectureBlock(course, teacher, lecturesPlaned1);
		int lecturesPlaned2 = 4;
		LectureBlock lectureBlock2 = createLectureBlock(course, teacher, lecturesPlaned2);
		int lecturesPlaned3 = 5;
		LectureBlock lectureBlock3 = createLectureBlock(course, teacher, lecturesPlaned3);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		Map<Long, Long> keysToFrom = infos.stream().collect(Collectors.toMap(LectureBlockInfo::getLectureBlockKey, LectureBlockInfo::getFirstLecture));
		assertThat(keysToFrom.get(lectureBlock1.getKey())).isEqualTo(1);
		assertThat(keysToFrom.get(lectureBlock2.getKey())).isEqualTo(1 + lecturesPlaned1);
		assertThat(keysToFrom.get(lectureBlock3.getKey())).isEqualTo(1 + lecturesPlaned1 + lecturesPlaned2);
	}
	
	@Test
	public void shouldLoadLecturesTo() {
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		int lecturesPlaned1 = 3;
		LectureBlock lectureBlock1 = createLectureBlock(course, teacher, lecturesPlaned1);
		int lecturesPlaned2 = 4;
		LectureBlock lectureBlock2 = createLectureBlock(course, teacher, lecturesPlaned2);
		int lecturesPlaned3 = 5;
		LectureBlock lectureBlock3 = createLectureBlock(course, teacher, lecturesPlaned3);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		Map<Long, Long> keysToTo = infos.stream().collect(Collectors.toMap(LectureBlockInfo::getLectureBlockKey, LectureBlockInfo::getLastLecture));
		assertThat(keysToTo.get(lectureBlock1.getKey())).isEqualTo(lecturesPlaned1);
		assertThat(keysToTo.get(lectureBlock2.getKey())).isEqualTo(lecturesPlaned1 + lecturesPlaned2);
		assertThat(keysToTo.get(lectureBlock3.getKey())).isEqualTo(lecturesPlaned1 + lecturesPlaned2 + lecturesPlaned3);
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByTeacher() {
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		createLectureBlock(course, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		LectureBlockInfo info = infos.get(0);
		assertThat(info.getTeacherKey()).isEqualTo(teacher.getKey());
		assertThat(info.getCourseRepoKey()).isEqualTo(course.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByCourses() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setWhiteListRefs(Arrays.asList(course1, course2));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey())
				.doesNotContain(otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByWhiteList() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setWhiteListRefs(Arrays.asList(course1, course2));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey())
				.doesNotContain(otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByBlackList() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setBlackListRefs(Arrays.asList(otherCourse));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey())
				.doesNotContain(otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByEducationalTypeExclusion() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = organisationService.createOrganisation("org", "Org", null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum("Curriculum", "Curriculum", null, false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element", "Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherElement = curriculumService.createCurriculumElement("Element", "Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry courseWithoutType = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseWithType = JunitTestHelper.createAndPersistRepositoryEntry();
		updateEducationalType(courseWithType, "ok");
		RepositoryEntry courseNok1 = JunitTestHelper.createAndPersistRepositoryEntry();
		updateEducationalType(courseNok1, "nok1");
		RepositoryEntry courseNok2 = JunitTestHelper.createAndPersistRepositoryEntry();
		updateEducationalType(courseNok2, "nok2");
		createLectureBlock(courseWithoutType, teacher, 1);
		createLectureBlock(courseWithType, teacher, 1);
		createLectureBlock(courseNok1, teacher, 1);
		createLectureBlock(courseNok2, teacher, 1);
		curriculumService.addRepositoryEntry(element, courseWithoutType, false);
		curriculumService.addRepositoryEntry(element, courseWithType, false);
		curriculumService.addRepositoryEntry(otherElement, courseNok1, false);
		curriculumService.addRepositoryEntry(otherElement, courseNok2, false);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setExcludedEducationalTypeKeys(asList(
				courseNok1.getEducationalType().getKey(),
				courseNok2.getEducationalType().getKey()));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(courseWithoutType.getKey(), courseWithType.getKey())
				.doesNotContain(courseNok1.getKey(), courseNok2.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByEndDate() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseToLessLectures = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseAfterToDate = JunitTestHelper.createAndPersistRepositoryEntry();
		Date start11 = nextHour();
		Date from = nextHour();
		Date end11 = nextHour();
		createLectureBlock(course, teacher, 1, start11, end11);
		Date start21 = nextHour();
		Date end21 = nextHour();
		createLectureBlock(courseToLessLectures, teacher, 1, start21, end21);
		Date start12 = nextHour();
		Date end12 = nextHour();
		Date to = nextHour();
		createLectureBlock(course, teacher, 1, start12, end12);
		Date otherStart = nextHour();
		Date otherEnd = nextHour();
		createLectureBlock(courseAfterToDate, teacher, 3, otherStart, otherEnd);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setFrom(from);
		searchParams.setTo(to);
		searchParams.setSelectingLecture(2);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course.getKey())
				.doesNotContain(
						courseToLessLectures.getKey(),
						courseAfterToDate.getKey());
	}

	@Test
	public void shouldFilterLectureBlockInfosByFinishedDataCollectionForTopicIdentity() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = organisationService.createOrganisation("Org-39", "", null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		RepositoryEntry courseNoDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseNoDC, organisation);
		RepositoryEntry courseRunningDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseRunningDC, organisation);
		RepositoryEntry courseFinishedDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseFinishedDC, organisation);
		createLectureBlock(courseNoDC, teacher, 1);
		createLectureBlock(courseRunningDC, teacher, 1);
		createLectureBlock(courseFinishedDC, teacher, 1);
		QualityGenerator generator = generatorService.createGenerator("Generator", organisations);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection runningDataCollection = qualityService.createDataCollection(organisations, formEntry, generator, courseRunningDC.getKey());
		runningDataCollection.setTopicIdentity(teacher);
		runningDataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
		runningDataCollection = qualityService.updateDataCollectionStatus(runningDataCollection, QualityDataCollectionStatus.RUNNING);
		qualityService.updateDataCollection(runningDataCollection);
		QualityDataCollection finishedDataCollection = qualityService.createDataCollection(organisations, formEntry, generator, courseFinishedDC.getKey());
		finishedDataCollection.setTopicIdentity(teacher);
		finishedDataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
		finishedDataCollection = qualityService.updateDataCollectionStatus(finishedDataCollection, QualityDataCollectionStatus.FINISHED);
		qualityService.updateDataCollection(finishedDataCollection);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setFinishedDataCollectionForGeneratorAndTopicIdentityRef(generator);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(courseFinishedDC.getKey())
				.doesNotContain(courseNoDC.getKey(), courseRunningDC.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByFinishedDataCollectionForTopicRepository() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = organisationService.createOrganisation("org", "Org", null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		RepositoryEntry courseNoDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseNoDC, organisation);
		RepositoryEntry courseRunningDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseRunningDC, organisation);
		RepositoryEntry courseFinishedDC = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseFinishedDC, organisation);
		createLectureBlock(courseNoDC, teacher, 1);
		createLectureBlock(courseRunningDC, teacher, 1);
		createLectureBlock(courseFinishedDC, teacher, 1);
		QualityGenerator generator = generatorService.createGenerator("Gen", organisations);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection runningDataCollection = qualityService.createDataCollection(organisations, formEntry, generator, teacher.getKey());
		runningDataCollection.setTopicRepositoryEntry(courseRunningDC);
		runningDataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
		runningDataCollection = qualityService.updateDataCollectionStatus(runningDataCollection, QualityDataCollectionStatus.RUNNING);
		qualityService.updateDataCollection(runningDataCollection);
		QualityDataCollection finishedDataCollection = qualityService.createDataCollection(organisations, formEntry, generator, teacher.getKey());
		finishedDataCollection.setTopicRepositoryEntry(courseFinishedDC);
		finishedDataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
		finishedDataCollection = qualityService.updateDataCollectionStatus(finishedDataCollection, QualityDataCollectionStatus.FINISHED);
		qualityService.updateDataCollection(finishedDataCollection);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setFinishedDataCollectionForGeneratorAndTopicRepositoryRef(generator);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(courseFinishedDC.getKey())
				.doesNotContain(courseNoDC.getKey(), courseRunningDC.getKey());
	}

	@Test
	public void shouldFilterLectureBlockInfosByExcludeForTopicIdentity() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = organisationService.createOrganisation("org", "Org", null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course1, organisation);
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course2, organisation);
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(otherCourse, organisation);
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		QualityGenerator generator = generatorService.createGenerator("Gen", organisations);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry, generator, otherCourse.getKey());
		dataCollection.setTopicIdentity(teacher);
		dataCollection.setTopicType(QualityDataCollectionTopicType.IDENTIY);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setExcludeGeneratorAndTopicIdentityRef(generator);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey())
				.doesNotContain(otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByExcludeForTopicRepository() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = organisationService.createOrganisation("org", "Org", null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course1, organisation);
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course2, organisation);
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(otherCourse, organisation);
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		QualityGenerator generator = generatorService.createGenerator("Gen", organisations);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry, generator, teacher.getKey());
		dataCollection.setTopicRepositoryEntry(otherCourse);
		dataCollection.setTopicType(QualityDataCollectionTopicType.REPOSITORY);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setExcludeGeneratorAndTopicRepositoryRef(generator);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey())
				.doesNotContain(otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByOrganisation() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation superOrganisation = organisationService.createOrganisation("org", "Org", null, null, null);
		Organisation organisation = organisationService.createOrganisation("org", "Org", null, superOrganisation, null);
		Organisation subOrganisation = organisationService.createOrganisation("org", "Org", null, organisation, null);
		RepositoryEntry courseSuperOrg = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseSuperOrg, superOrganisation);
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course1, organisation);
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(course2, organisation);
		RepositoryEntry courseSubOrg = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(courseSubOrg, subOrganisation);
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		createLectureBlock(courseSuperOrg, teacher, 1);
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(course2, teacher, 1);
		createLectureBlock(courseSubOrg, teacher, 1);
		createLectureBlock(otherCourse, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setOrganisationRefs(Arrays.asList(organisation));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey(), course2.getKey(), courseSubOrg.getKey())
				.doesNotContain(courseSuperOrg.getKey(), otherCourse.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByMinTotalLectures() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock1 = createLectureBlock(course1, teacher, 10);
		LectureBlock lectureBlock2 = createLectureBlock(course2, teacher, 5);
		LectureBlock lectureBlock3 = createLectureBlock(course2, teacher, 5);
		LectureBlock lectureBlock4 = createLectureBlock(otherCourse, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setMinTotalLectures(6);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getLectureBlockKey)
				.containsExactlyInAnyOrder(lectureBlock1.getKey(), lectureBlock2.getKey(), lectureBlock3.getKey())
				.doesNotContain(lectureBlock4.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByMaxTotalLectures() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherCourse = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock1 = createLectureBlock(course1, teacher, 5);
		LectureBlock lectureBlock2 = createLectureBlock(course2, teacher, 1);
		LectureBlock lectureBlock3 = createLectureBlock(course2, teacher, 1);
		LectureBlock lectureBlock4 = createLectureBlock(otherCourse, teacher, 10);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setMaxTotalLectures(6);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getLectureBlockKey)
				.containsExactlyInAnyOrder(lectureBlock1.getKey(), lectureBlock2.getKey(), lectureBlock3.getKey())
				.doesNotContain(lectureBlock4.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosBySelectingLecture() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock1 = createLectureBlock(course, teacher, 5);
		LectureBlock lectureBlock2 = createLectureBlock(course, teacher, 5);
		LectureBlock lectureBlock3 = createLectureBlock(course, teacher, 5);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setSelectingLecture(6);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		List<Long> lectureBlocksKeys = infos.stream().map(LectureBlockInfo::getLectureBlockKey).collect(Collectors.toList());
		assertThat(lectureBlocksKeys)
				.containsExactlyInAnyOrder(lectureBlock2.getKey())
				.doesNotContain(lectureBlock1.getKey(), lectureBlock3.getKey());
		
		searchParams.setSelectingLecture(10);
		infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getLectureBlockKey)
				.containsExactlyInAnyOrder(lectureBlock2.getKey())
				.doesNotContain(lectureBlock1.getKey(), lectureBlock3.getKey());
	}
	
	@Test
	public void shouldFilterLectureBlockInfosByLastLectureBlock() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock1 = createLectureBlock(course, teacher, 5);
		LectureBlock lectureBlock2 = createLectureBlock(course, teacher, 5);
		LectureBlock lectureBlock3 = createLectureBlock(course, teacher, 5);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setLastLectureBlock(true);
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getLectureBlockKey)
				.containsExactlyInAnyOrder(lectureBlock3.getKey())
				.doesNotContain(lectureBlock1.getKey(), lectureBlock2.getKey());
	}
	
	@Test
	public void shouldNotLoadLecturesOfDeletedCourses() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		RepositoryEntry course1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry trashed = JunitTestHelper.createAndPersistRepositoryEntry();
		trashed.setEntryStatus(RepositoryEntryStatusEnum.trash);
		repositoryService.update(trashed);
		RepositoryEntry deleted = JunitTestHelper.createAndPersistRepositoryEntry();
		deleted.setEntryStatus(RepositoryEntryStatusEnum.deleted);
		repositoryService.update(deleted);
		createLectureBlock(course1, teacher, 1);
		createLectureBlock(trashed, teacher, 1);
		createLectureBlock(deleted, teacher, 1);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setTeacherRef(teacher);
		searchParams.setWhiteListRefs(Arrays.asList(course1, trashed));
		List<LectureBlockInfo> infos = sut.loadLectureBlockInfo(searchParams);

		assertThat(infos).extracting(LectureBlockInfo::getCourseRepoKey)
				.containsExactlyInAnyOrder(course1.getKey())
				.doesNotContain(deleted.getKey(), trashed.getKey());
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry course, Identity teacher, int numLectures) {
		return createLectureBlock(course, teacher, numLectures, nextHour(), nextHour());
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry course, Identity teacher, int numLectures, Date start, Date end) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(course);
		lectureBlock.setStartDate(start);
		lectureBlock.setEndDate(end);
		lectureBlock.setStatus(LectureBlockStatus.active);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.open);
		lectureBlock.setPlannedLecturesNumber(numLectures);
		lectureBlock = lectureService.save(lectureBlock, null);
		lectureService.addTeacher(lectureBlock, teacher);
		return lectureBlock;
	}
	
	private Date nextHour() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(hour);
		calendar.add(Calendar.HOUR, 1);
		hour = calendar.getTime();
		return hour;
	}
	
	private RepositoryEntry updateEducationalType(RepositoryEntry entry, String identifier) {
		RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(identifier);
		if (educationalType == null) {
			educationalType = repositoryManager.createEducationalType(identifier);
			
		}
		entry.setEducationalType(educationalType);
		dbInstance.getCurrentEntityManager().merge(entry);
		return entry;
	}

}
