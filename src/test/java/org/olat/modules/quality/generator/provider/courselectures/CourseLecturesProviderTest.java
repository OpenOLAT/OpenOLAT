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
package org.olat.modules.quality.generator.provider.courselectures;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseLecturesProviderTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private CourseLecturesProvider sut;
	
	@Test
	public void shouldCopyOrganisationsOfCourseToDataCollection() {
		Date startDate = DateUtils.addDays(new Date(), -2);
		Date startEnd = DateUtils.addDays(new Date(), -1);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		repositoryService.removeOrganisation(courseEntry, defaultOrganisation);
		Organisation courseOrganisation1 = createOrganisation(defaultOrganisation);
		repositoryService.addOrganisation(courseEntry, courseOrganisation1);
		Organisation courseOrganisation2 = createOrganisation(defaultOrganisation);
		repositoryService.addOrganisation(courseEntry, courseOrganisation2);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, courseEntry);
		
		Date lastRun = DateUtils.addDays(new Date(), -2);
		Date now = new Date();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		List<Organisation> organisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		assertThat(organisations)
				.containsExactlyInAnyOrder(courseOrganisation1, courseOrganisation2)
				.doesNotContain(defaultOrganisation);
	}
	
	@Test
	public void shouldCreateDataCollectionIfDeadlineIsInFuture() {
		Date startDate = DateUtils.addDays(new Date(), -2);
		Date startEnd = DateUtils.addDays(new Date(), -1);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, courseEntry);
		
		Date lastRun = DateUtils.addDays(new Date(), -2);
		Date now = new Date();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).hasSize(1);
	}
	
	@Test
	public void shouldNotCreateDataCollectionIfDeadlineIsInPast() {
		Date startDate = DateUtils.addDays(new Date(), -12);
		Date startEnd = DateUtils.addDays(new Date(), -11);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, courseEntry);
		
		Date lastRun = DateUtils.addDays(new Date(), -20);
		Date now = new Date();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
		
	}
	
	@Test
	public void shouldGenerateWithAnnouncementForwards() {
		Date now = new Date();
		Date startDate = DateUtils.addDays(now, 3);
		Date startEnd = DateUtils.addDays(now, 4);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, courseEntry);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS, "5");
		
		Date lastRun = DateUtils.addDays(now, -9);
		Date to = DateUtils.addDays(now, 0);
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, to);
		assertThat(generated).hasSize(1);
		QualityDataCollection dataCollection = generated.get(0);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(dataCollection.getStart()).isCloseTo(startEnd, 1000);
		
		QualityReminder reminder = qualityService.loadReminder(dataCollection, QualityReminderType.ANNOUNCEMENT_COACH_TOPIC);
		softly.assertThat(reminder).isNotNull();
		softly.assertThat(reminder.getSendPlaned()).isCloseTo(DateUtils.addDays(now, -1), 1000);
		softly.assertAll();
	}
	
	@Test
	public void shouldNotGenerateAnnouncementIfStarted() {
		Date now = new Date();
		Date startDate = DateUtils.addDays(now,-2);
		Date startEnd = DateUtils.addDays(now, -1);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, courseEntry);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ANNOUNCEMENT_COACH_DAYS, "2");
		
		Date lastRun = DateUtils.addDays(now, -9);
		Date to = DateUtils.addDays(now, 0);
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, to);
		QualityDataCollection dataCollection = generated.get(0);
		dbInstance.commitAndCloseSession();
		
		QualityReminder reminder = qualityService.loadReminder(dataCollection, QualityReminderType.ANNOUNCEMENT_COACH_TOPIC);
		assertThat(reminder).isNull();
	}
	
	@Test
	public void shouldStartDataCollectionAsConfigured() {
		Date now = new Date();
		Date lectureStart = DateUtils.addDays(now, -2);
		Date lectureEnd = DateUtils.addDays(now, -1);
		RepositoryEntry courseEntry = createCourseWithLecture(lectureStart, lectureEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		int durationDays = 10;
		QualityGeneratorConfigs configs = createConfigs(generator, valueOf(durationDays), courseEntry);
		int startDataCollectionBeforeEnd = 45;
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_MINUTES_BEFORE_END, valueOf(startDataCollectionBeforeEnd));
		
		Date lastRun = DateUtils.addDays(now, -2);
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		QualityDataCollection dataCollection = generated.get(0);
		dbInstance.commitAndCloseSession();
		
		Date beforeEndAsConfigured = DateUtils.addMinutes(lectureEnd, -startDataCollectionBeforeEnd);
		long withinAMinute = 60*1000;
		Date deadline = DateUtils.addDays(beforeEndAsConfigured, durationDays);
		assertThat(dataCollection.getStart()).isCloseTo(beforeEndAsConfigured, withinAMinute);
		assertThat(dataCollection.getDeadline()).isCloseTo(deadline, withinAMinute);
	}

	private RepositoryEntry createCourseWithLecture(Date lectureStartDate, Date lectureEndDate) {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(JunitTestHelper.random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		LectureBlock lectureBlock = lectureService.createLectureBlock(courseEntry);
		lectureBlock.setPlannedLecturesNumber(3);
		lectureBlock.setStartDate(lectureStartDate);
		lectureBlock.setEndDate(lectureEndDate);
		lectureService.save(lectureBlock, null);
		lectureService.addTeacher(lectureBlock, initialAuthor);
		
		dbInstance.commitAndCloseSession();
		return courseEntry;
	}

	private QualityGeneratorConfigs createConfigs(QualityGenerator generator, String durationDays, RepositoryEntry courseEntry) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ROLES, "coach");
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MIN, "0");
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOTAL_LECTURES_MAX, "10000");
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE, CourseLecturesProvider.CONFIG_KEY_SURVEY_LECTURE_LAST);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_DURATION_DAYS, durationDays);
		// Restrict to a single course
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, Collections.singletonList(courseEntry));
		dbInstance.commitAndCloseSession();
		return configs;
	}

	private QualityGenerator createGeneratorInDefaultOrganisation() {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Collection<Organisation> organisations = Collections.singletonList(organisation);
		QualityGenerator generator = generatorService.createGenerator(sut.getType(), organisations);
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		generator.setFormEntry(formEntry);
		generatorService.updateGenerator(generator);
		dbInstance.commitAndCloseSession();
		return generator;
	}

	private Organisation createOrganisation(Organisation parent) {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), parent, null);
		dbInstance.commitAndCloseSession();
		return organisation;
	}

}
