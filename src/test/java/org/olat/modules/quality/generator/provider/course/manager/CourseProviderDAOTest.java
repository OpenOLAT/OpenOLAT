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
package org.olat.modules.quality.generator.provider.course.manager;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.course.CourseModule;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseProviderDAOTest extends OlatTestCase {
	
	private Date hour = new Date();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CourseProviderDAO sut;
	
	@Test
	public void shouldFilterByCourse() {
		OLATResourceable ores = FeedManager.getInstance().createBlogResource();
		OLATResource blogOres = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry blog = createEntry(blogOres, null, null);
		RepositoryEntry course1 = createEntry(null, null, null);
		RepositoryEntry course2 = createEntry(null, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course1, course2)
				.doesNotContain(blog);
	}

	@Test
	public void shouldFilterByPublishedCourses() {
		RepositoryEntry preparation = createEntry(null, RepositoryEntryStatusEnum.preparation, null);
		RepositoryEntry coachpublished = createEntry(null, RepositoryEntryStatusEnum.coachpublished, null);
		RepositoryEntry published = createEntry(null, RepositoryEntryStatusEnum.published, null);
		RepositoryEntry review = createEntry(null, RepositoryEntryStatusEnum.review, null);
		RepositoryEntry trash = createEntry(null, RepositoryEntryStatusEnum.trash, null);
		RepositoryEntry deleted = createEntry(null, RepositoryEntryStatusEnum.deleted, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(published)
				.doesNotContain(preparation, coachpublished, review, trash, deleted);
	}
	
	@Test
	public void shouldFilterByNotAlreadyGenerated() {
		Organisation organisation = organisationService.createOrganisation("Org-32", "o1", null, null, null);
		RepositoryEntry courseWithoutDataCollection = createEntry(null, null, organisation);
		RepositoryEntry courseWithDataCollection = createEntry(null, null, organisation);
		QualityGenerator generator = generatorService.createGenerator("Gen", singletonList(organisation));
		createDataCollection(organisation, courseWithDataCollection, generator);
		RepositoryEntry courseWithOtherDataCollection = createEntry(null, null, organisation);
		QualityGenerator generatorOther = generatorService.createGenerator("Gen", singletonList(organisation));
		createDataCollection(organisation, courseWithOtherDataCollection, generatorOther);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setGeneratorRef(generator);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(courseWithoutDataCollection, courseWithOtherDataCollection)
				.doesNotContain(courseWithDataCollection);
	}
	
	@Test
	public void shouldFilterByOrganisation() {
		Organisation superOrganisation = organisationService.createOrganisation("Org-33", "o1", null, null, null);
		Organisation organisation = organisationService.createOrganisation("Org-34", "o1", null, superOrganisation, null);
		Organisation subOrganisation = organisationService.createOrganisation("Org-35", "o1s", null, organisation, null);
		Organisation organisationOther = organisationService.createOrganisation("Org.36", "o2", null, null, null);
		RepositoryEntry courseSuper = createEntry(null, null, superOrganisation);
		RepositoryEntry course1 = createEntry(null, null, organisation);
		RepositoryEntry course2 = createEntry(null, null, organisation);
		RepositoryEntry courseSub = createEntry(null, null, subOrganisation);
		RepositoryEntry other = createEntry(null, null, organisationOther);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setOrganisationRefs(asList(organisation));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.containsExactlyInAnyOrder(course1, course2, courseSub)
				.doesNotContain(courseSuper, other);
	}
	
	@Test
	public void shouldFilterByBeginFrom() {
		RepositoryEntry courseWithoutBegin = createEntry(null, null, null);
		RepositoryEntry courseBeginToEarly = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(courseBeginToEarly, before, null);
		Date beginFrom = nextHour();
		RepositoryEntry course = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(course, after, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setBeginFrom(beginFrom);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutBegin, courseBeginToEarly);
	}
	
	@Test
	public void shouldFilterByBeginTo() {
		RepositoryEntry courseWithoutBegin = createEntry(null, null, null);
		RepositoryEntry course = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(course, before, null);
		Date beginTo = nextHour();
		RepositoryEntry courseBeginToLate = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(courseBeginToLate, after, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setBeginTo(beginTo);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutBegin, courseBeginToLate);
	}
	
	@Test
	public void shouldFilterByEndFrom() {
		RepositoryEntry courseWithoutEnd = createEntry(null, null, null);
		RepositoryEntry courseEndToEarly = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(courseEndToEarly, null, before);
		Date endFrom = nextHour();
		RepositoryEntry course = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(course, null, after);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setEndFrom(endFrom);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutEnd, courseEndToEarly);
	}
	
	@Test
	public void shouldFilterByEndTo() {
		RepositoryEntry courseWithoutEnd = createEntry(null, null, null);
		RepositoryEntry course = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(course, null, before);
		Date endTo = nextHour();
		RepositoryEntry courseEndToLate = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(courseEndToLate, null, after);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setEndTo(endTo);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutEnd, courseEndToLate);
	}
	
	@Test
	public void shouldFilterByWhiteListRefs() {
		RepositoryEntry course1 = createEntry(null, null, null);
		RepositoryEntry course2 = createEntry(null, null, null);
		RepositoryEntry other = createEntry(null, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setWhiteListRefs(asList(course1, course2));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course1, course2)
				.doesNotContain(other);
	}
	
	@Test
	public void shouldFilterByBlackListRefs() {
		RepositoryEntry course1 = createEntry(null, null, null);
		RepositoryEntry course2 = createEntry(null, null, null);
		RepositoryEntry blackList = createEntry(null, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setBlackListRefs(asList(blackList));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course1, course2)
				.doesNotContain(blackList);
	}
	
	@Test
	public void shouldFilterByGeneratorDataCollectionStart() {
		Date start = nextHour();
		Date other = nextYear();
		Organisation organisation = organisationService.createOrganisation("Org-37", "o1", null, null, null);
		QualityGenerator generator = generatorService.createGenerator("Gen", singletonList(organisation));
		QualityGenerator generatorOther = generatorService.createGenerator("Gen", singletonList(organisation));
		//Without data collection
		RepositoryEntry courseNoDataCollection = createEntry(null, null, organisation);
		//Data collection with generator, without start date
		RepositoryEntry courseSameGeneratorNoStart = createEntry(null, null, organisation);
		createDataCollection(organisation, courseSameGeneratorNoStart, generator);
		//Data collection with generator, with other start date
		RepositoryEntry courseSameGeneratorOtherStart = createEntry(null, null, organisation);
		QualityDataCollection dcSameGeneratorOtherStart = createDataCollection(organisation, courseSameGeneratorOtherStart, generator);
		dcSameGeneratorOtherStart.setStart(other);
		qualityService.updateDataCollection(dcSameGeneratorOtherStart);
		//Data collection with generator, with same start date
		RepositoryEntry courseSameGeneratorSameStart = createEntry(null, null, organisation);
		QualityDataCollection dcSameGeneratorSameStart = createDataCollection(organisation, courseSameGeneratorSameStart, generator);
		dcSameGeneratorSameStart.setStart(start);
		qualityService.updateDataCollection(dcSameGeneratorSameStart);
		//Data collection with other generator, with same start date
		RepositoryEntry courseOtherGeneratorSameStart = createEntry(null, null, organisation);
		QualityDataCollection dcOtherGeneratorSameStart = createDataCollection(organisation, courseOtherGeneratorSameStart, generatorOther);
		dcOtherGeneratorSameStart.setStart(other);
		qualityService.updateDataCollection(dcOtherGeneratorSameStart);
		//Data collection without generator, with same start date
		RepositoryEntry courseNoGeneratorSameStart = createEntry(null, null, organisation);
		QualityDataCollection dcNoGeneratorSameStart = createDataCollection(organisation, courseNoGeneratorSameStart, null);
		dcNoGeneratorSameStart.setStart(start);
		qualityService.updateDataCollection(dcNoGeneratorSameStart);
		//Data collection without generator, without start date
		RepositoryEntry courseNoGeneratorNoStart = createEntry(null, null, organisation);
		createDataCollection(organisation, courseNoGeneratorNoStart, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setGeneratorRef(generator);
		seachParameters.setGeneratorDataCollectionStart(start);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(
						courseNoDataCollection,
						courseSameGeneratorNoStart,
						courseSameGeneratorOtherStart,
						courseOtherGeneratorSameStart,
						courseNoGeneratorSameStart,
						courseNoGeneratorNoStart)
				.doesNotContain(courseSameGeneratorSameStart);
	}
	
	@Test
	public void shouldFilterLifecycleValidAt() {
		Date beforeInside = nextHour();
		Date inside = nextYear();
		Date afterInside = nextYear();
		RepositoryEntry courseStartNoneEndNone = createEntry(null, null, null);
		RepositoryEntry courseStartNoneEndAfterInside = createEntry(null, null, null);
		setLifecycle(courseStartNoneEndAfterInside, null, afterInside);
		RepositoryEntry courseStartBeforeInsideEndNone = createEntry(null, null, null);
		setLifecycle(courseStartBeforeInsideEndNone, beforeInside, null);
		RepositoryEntry courseStartBeforeInsideEndAfterInside = createEntry(null, null, null);
		setLifecycle(courseStartBeforeInsideEndAfterInside, beforeInside, afterInside);
		RepositoryEntry courseStartInFutrue = createEntry(null, null, null);
		setLifecycle(courseStartInFutrue, afterInside, afterInside);
		RepositoryEntry courseEndInPast = createEntry(null, null, null);
		setLifecycle(courseEndInPast, beforeInside, beforeInside);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setLifecycleValidAt(inside);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);
		
		assertThat(courses)
				.contains(
						courseStartNoneEndNone,
						courseStartNoneEndAfterInside, courseStartBeforeInsideEndNone, courseStartBeforeInsideEndAfterInside)
				.doesNotContain(courseStartInFutrue, courseEndInPast);
	}
	
	@Test
	public void shouldFilterByEducationalTypeExclusion() {
		RepositoryEntry courseWithoutType = createEntry(null, null, null);
		RepositoryEntry courseWithType = createEntry(null, null, null);
		updateEducationalType(courseWithType, "ok");
		RepositoryEntry courseExcludedType1 = createEntry(null, null, null);
		updateEducationalType(courseExcludedType1, "nok1");
		RepositoryEntry courseExcludedType2 = createEntry(null, null, null);
		updateEducationalType(courseExcludedType2, "nok2");
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setExcludedEducationalTypeKeys(asList(
				courseExcludedType1.getEducationalType().getKey(),
				courseExcludedType2.getEducationalType().getKey()));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(courseWithoutType, courseWithType)
				.doesNotContain(courseExcludedType1, courseExcludedType2);
	}
	
	private RepositoryEntry createEntry(OLATResource resource, RepositoryEntryStatusEnum status, Organisation organisation) {
		String initialAuthorAlt = UUID.randomUUID().toString();
		String displayname = UUID.randomUUID().toString();
		resource = resource != null? resource: OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
		status = status != null? status: RepositoryEntryStatusEnum.published;
		organisation = organisation != null? organisation: organisationService.getDefaultOrganisation();
		return repositoryService.create(null, initialAuthorAlt , null, displayname, null, resource, status, organisation);
	}
	
	private RepositoryEntry setLifecycle(RepositoryEntry entry, Date begin, Date end) {
		String softKey = "lf_" + entry.getSoftkey();
		RepositoryEntryLifecycle cycle = lifecycleDao.create(entry.getDisplayname(), softKey, true, begin, end);
		entry.setLifecycle(cycle);
		dbInstance.getCurrentEntityManager().merge(entry);
		return entry;
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

	private QualityDataCollection createDataCollection(Organisation organisation, RepositoryEntry entry, QualityGenerator generator) {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		return qualityService.createDataCollection(singletonList(organisation), formEntry, generator, entry.getKey());
	}
	
	private Date nextHour() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(hour);
		calendar.add(Calendar.HOUR, 1);
		hour = calendar.getTime();
		return hour;
	}
	
	private Date nextYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(hour);
		calendar.add(Calendar.YEAR, 1);
		hour = calendar.getTime();
		return hour;
	}
	
}
