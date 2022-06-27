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
package org.olat.modules.quality.generator.provider.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseProviderTest  extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDAO;
	
	@Autowired
	private CourseProvider sut;
	
	@Test
	public void shouldNotGenerateDataCollectionIfBeginDateIsNotReachedYet() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 2);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 11).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
	}
	
	@Test
	public void shouldGenerateDataCollectionIfBeginDateIsReached() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 11).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		assertThat(dataCollection.getStart()).isCloseTo(new GregorianCalendar(2010, 6, 11).getTime(), 1000);
		assertThat(dataCollection.getDeadline()).isCloseTo(new GregorianCalendar(2010, 6, 21).getTime(), 1000);
		assertThat(dataCollection.getTopicRepositoryEntry().getKey()).isEqualTo(courseEntry.getKey());
	}
	
	@Test
	public void shouldGenerateDataCollectionIfBeginDateIsReachedWithDelay() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 13).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		assertThat(dataCollection.getStart()).isCloseTo(new GregorianCalendar(2010, 6, 11).getTime(), 1000);
		assertThat(dataCollection.getDeadline()).isCloseTo(new GregorianCalendar(2010, 6, 21).getTime(), 1000);
		assertThat(dataCollection.getTopicRepositoryEntry().getKey()).isEqualTo(courseEntry.getKey());
	}
	
	@Test
	public void shouldNotGenerateDataCollectionIfBeginDatePlusDueDaysPlusDurationIsOver() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 30).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
	}
	
	@Test
	public void shouldNotGenerateDataCollectionIfEndDateIsNotReachedYet() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 6, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "-10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseEndConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 11).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
	}
	
	@Test
	public void shouldGenerateDataCollectionIfEndDateIsReached() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 6, 21);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "-10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseEndConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 11).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		assertThat(dataCollection.getStart()).isCloseTo(new GregorianCalendar(2010, 6, 11).getTime(), 1000);
		assertThat(dataCollection.getDeadline()).isCloseTo(new GregorianCalendar(2010, 6, 21).getTime(), 1000);
		assertThat(dataCollection.getTopicRepositoryEntry().getKey()).isEqualTo(courseEntry.getKey());
	}
	
	@Test
	public void shouldGenerateDataCollectionIfEndDateIsReachedWithDelay() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 6, 18);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "-10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseEndConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 1).getTime();
		Date now = new GregorianCalendar(2010, 6, 13).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		assertThat(dataCollection.getStart()).isCloseTo(new GregorianCalendar(2010, 6, 8).getTime(), 1000);
		assertThat(dataCollection.getDeadline()).isCloseTo(new GregorianCalendar(2010, 6, 18).getTime(), 1000);
		assertThat(dataCollection.getTopicRepositoryEntry().getKey()).isEqualTo(courseEntry.getKey());
	}
	
	@Test
	public void shouldNotGenerateDataCollectionIfEndDatePlusDueDaysPlusDurationIsOver() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 6, 25);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "-10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseEndConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 11).getTime();
		Date now = new GregorianCalendar(2010, 6, 13).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
	}
	
	@Test
	public void shouldCopyOrganisationsOfCourseToDataCollection() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 6, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 6, 18);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		repositoryService.removeOrganisation(courseEntry, defaultOrganisation);
		Organisation courseOrganisation1 = createOrganisation(defaultOrganisation);
		repositoryService.addOrganisation(courseEntry, courseOrganisation1);
		Organisation courseOrganisation2 = createOrganisation(defaultOrganisation);
		repositoryService.addOrganisation(courseEntry, courseOrganisation2);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		String dueDateDays = "-10";
		String durationHours = "240";
		QualityGeneratorConfigs configs = createCourseEndConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 1).getTime();
		Date now = new GregorianCalendar(2010, 6, 13).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		List<Organisation> organisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		assertThat(organisations)
				.containsExactlyInAnyOrder(courseOrganisation1, courseOrganisation2)
				.doesNotContain(defaultOrganisation);
	}

	private RepositoryEntry createCourse(GregorianCalendar lifecycleStart, GregorianCalendar lifecycleEnd) {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(JunitTestHelper.random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		RepositoryEntryLifecycle lifecycle = lifecycleDAO.create(null, null, false, lifecycleStart.getTime(), lifecycleEnd.getTime());
		courseEntry = repositoryManager.setDescriptionAndName(courseEntry, null, null, null, null, null, null, null, null, lifecycle);
		dbInstance.commitAndCloseSession();
		return courseEntry;
	}

	private QualityGeneratorConfigs createCourseBeginConfigs(QualityGenerator generator, String dueDateDays,
			String durationHours, RepositoryEntryRef courseEntry) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CourseProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CourseProvider.CONFIG_KEY_ROLES, "coach");
		configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_BEGIN);
		configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
		configs.setValue(CourseProvider.CONFIG_KEY_DURATION_HOURS, durationHours);
		// Restrict to a single course, because a lot of courses with the same life cycle are generated.
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, Collections.singletonList(courseEntry));
		dbInstance.commitAndCloseSession();
		return configs;
	}

	private QualityGeneratorConfigs createCourseEndConfigs(QualityGenerator generator, String dueDateDays,
			String durationHours, RepositoryEntryRef courseEntry) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CourseProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CourseProvider.CONFIG_KEY_ROLES, "coach");
		configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_END);
		configs.setValue(CourseProvider.CONFIG_KEY_DUE_DATE_DAYS, dueDateDays);
		configs.setValue(CourseProvider.CONFIG_KEY_DURATION_HOURS, durationHours);
		// Restrict to a single course, because a lot of courses with the same life cycle are generated.
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
