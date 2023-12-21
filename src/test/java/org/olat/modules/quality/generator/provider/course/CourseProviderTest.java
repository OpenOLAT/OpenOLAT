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
import static org.olat.modules.quality.generator.QualityGeneratorOverrides.NO_OVERRIDES;
import static org.olat.test.JunitTestHelper.random;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateRange;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.ProviderHelper;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverrideImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverridesImpl;
import org.olat.modules.quality.generator.ui.RepositoryEntryBlackListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
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
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDAO;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		String dueDateDays = "10"; // Data collection starts 10 days after course start.
		String durationHours = "240"; // Data collection duration is 10 days. So ends 20 days after course start.
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, dueDateDays, durationHours, courseEntry);
		
		Date lastRun = new GregorianCalendar(2010, 6, 22).getTime();
		Date now = new GregorianCalendar(2010, 6, 30).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		List<Organisation> organisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		assertThat(organisations)
				.containsExactlyInAnyOrder(courseOrganisation1, courseOrganisation2)
				.doesNotContain(defaultOrganisation);
	}
	
	@Test
	public void shouldCreatePreviewDaily() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		QualityPreview preview = previews.get(0);
		assertThat(preview.getGenerator().getKey()).isEqualTo(generator.getKey());
		assertThat(preview.getGeneratorProviderKey()).isEqualTo(courseEntry.getKey());
		assertThat(preview.getFormEntry().getKey()).isEqualTo(generator.getFormEntry().getKey());
		assertThat(preview.getTopicType()).isEqualTo(QualityDataCollectionTopicType.REPOSITORY);
		assertThat(preview.getTopicRepositoryEntry().getKey()).isEqualTo(courseEntry.getKey());
		assertThat(preview.getParticipants().size()).isEqualTo(2);
		assertThat(preview.getStatus()).isEqualTo(QualityPreviewStatus.regular);
	}
	
	@Test
	public void shouldCreatePreviewDaily_includeBlacklisted() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		RepositoryEntryBlackListController.setRepositoryEntryRefs(configs, List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		QualityPreview preview = previews.get(0);
		assertThat(preview.getStatus()).isEqualTo(QualityPreviewStatus.blacklist);
	}
	
	@Test
	public void shouldCreatePreviewDaily_restrictRepositoryEntry() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry1 = createCourse(lifecycleStart, lifecycleEnd);
		RepositoryEntry courseEntry2 = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry1, courseEntry2));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(24);
		
		previewSearchParams.setRepositoryEntry(courseEntry1);
		previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		assertThat(previews).hasSize(12);
	}
	
	@Test
	public void shouldCreatePreviewDaily_excludeAlreadyGenerated() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		sut.generateDataCollection(generator, configs, null, courseEntry, new GregorianCalendar(2045, 11, 4, 14, 2, 0).getTime());
		sut.generateDataCollection(generator, configs, null, courseEntry, new GregorianCalendar(2045, 11, 5, 14, 2, 0).getTime());
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(10);
	}
	
	@Test
	public void shouldCreatePreviewDaily_excludeOutsideDateRange() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 11, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 11, 31);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 7).getTime(), new GregorianCalendar(2045, 11, 24).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(7);
	}
	
	@Test
	public void shouldCreatePreviewDaily_excludeDatesOutsideLifeCycle() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 11, 7);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 11, 31);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator,  "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(10);
	}
	
	@Test
	public void shouldCreatePreviewDaily_excludeCoursesOutsideLifeCycle() {
		GregorianCalendar beforeInside =  new GregorianCalendar(2045, 11, 7);
		GregorianCalendar afterInside = new GregorianCalendar(2047, 11, 7);
		RepositoryEntry courseStartNoneEndNone = createCourse(null, null);
		RepositoryEntry courseStartNoneEndAfterInside = createCourse(null, afterInside);
		RepositoryEntry courseStartBeforeInsideEndNone = createCourse(beforeInside, null);
		RepositoryEntry courseStartBeforeInsideEndAfterInside = createCourse(beforeInside, afterInside);
		RepositoryEntry courseStartInFutrue = createCourse(afterInside, afterInside);
		RepositoryEntry courseEndInPast = createCourse(beforeInside, beforeInside);
		dbInstance.commitAndCloseSession();
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240",
				List.of(courseStartNoneEndNone, courseStartNoneEndAfterInside, courseStartBeforeInsideEndNone,
						courseStartBeforeInsideEndAfterInside, courseStartInFutrue, courseEndInPast));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 18).getTime(), new GregorianCalendar(2045, 11, 18).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		List<Long> generatorProviderKeys = previews.stream().map(QualityPreview::getGeneratorProviderKey).toList();
		assertThat(generatorProviderKeys)
				.contains(
						courseStartNoneEndNone.getKey(),
						courseStartNoneEndAfterInside.getKey(),
						courseStartBeforeInsideEndNone.getKey(),
						courseStartBeforeInsideEndAfterInside.getKey())
				.doesNotContain(
						courseStartInFutrue.getKey(),
						courseEndInPast.getKey());
	}
	
	@Test
	public void shouldCreatePreviewDaily_override() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		
		// Override start (moved to first day of range)
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(courseEntry.getKey());
		override.setIdentifier(sut.getDailyIdentifier(generator, courseEntry, new GregorianCalendar(2045, 11, 4).getTime()));
		override.setStart(new GregorianCalendar(2045, 11, 1).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		previews.sort((p1, p2) -> p1.getStart().compareTo(p2.getStart()));
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}
	
	@Test
	public void shouldCreatePreviewDaily_overrideMovedIntoRange() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		
		// Override start (moved to first day of range)
		QualityGeneratorOverrideImpl override1 = new QualityGeneratorOverrideImpl();
		override1.setGenerator(generator);
		override1.setGeneratorProviderKey(courseEntry.getKey());
		override1.setIdentifier(sut.getDailyIdentifier(generator, courseEntry, new GregorianCalendar(2045, 10, 27).getTime()));
		override1.setStart(new GregorianCalendar(2045, 11, 1, 0, 0, 20).getTime());
		// Second moved from outside to outside
		QualityGeneratorOverrideImpl override2 = new QualityGeneratorOverrideImpl();
		override2.setGenerator(generator);
		override2.setGeneratorProviderKey(courseEntry.getKey());
		override2.setIdentifier(sut.getDailyIdentifier(generator, courseEntry, new GregorianCalendar(2045, 10, 25).getTime()));
		override2.setStart(new GregorianCalendar(2045, 10, 26).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override1, override2));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(13);
		previews.sort((p1, p2) -> p1.getStart().compareTo(p2.getStart()));
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override1.getStart(), 1000);
	}
	
	@Test
	public void shouldCreatePreviewDaily_overrideMovedOutOfRanged() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);
		
		// Override start outside range
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(courseEntry.getKey());
		override.setIdentifier(sut.getDailyIdentifier(generator, courseEntry, new GregorianCalendar(2045, 11, 4).getTime()));
		override.setStart(new GregorianCalendar(2045, 10, 2).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(11);
	}
	
	@Test
	public void shouldCreatePreviewDaily_overrideMovedToDayWithDataCollection() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2045, 1, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2046, 12, 30);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createDailyConfigs(generator, "240", List.of(courseEntry));
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		DateRange dateRange = new DateRange(new GregorianCalendar(2045, 11, 1).getTime(), new GregorianCalendar(2045, 11, 31).getTime());
		previewSearchParams.setDateRange(dateRange);
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(12);

		String identifier = sut.getDailyIdentifier(generator, courseEntry, new GregorianCalendar(2045, 10, 4).getTime());
		QualityGeneratorOverride override = generatorService.createOverride(identifier, generator, courseEntry.getKey());
		override.setStart(new GregorianCalendar(2045, 11, 4).getTime());
		override = generatorService.updateOverride(override);
		sut.generateDataCollection(generator, configs, override, courseEntry, new GregorianCalendar(2045, 10, 4).getTime());
		dbInstance.commitAndCloseSession();
		
		previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		assertThat(previews).hasSize(12);
	}

	@Test
	public void shouldCreatePreviewDueDate_override() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 8, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 8, 10);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, "1", "48", courseEntry);
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		previewSearchParams.setGeneratorKeys(List.of(generator.getKey()));
		previewSearchParams.setDateRange(new DateRange(new GregorianCalendar(2010, 8, 1).getTime(), new GregorianCalendar(2010, 8, 10).getTime()));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.regular);
		
		// Override start
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(courseEntry.getKey());
		override.setIdentifier(sut.getDueDateIdentifier(generator, courseEntry));
		override.setStart(new GregorianCalendar(2010, 8, 2).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}

	@Test
	public void shouldCreatePreviewDueDate_overrideMovedInToRange() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 8, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 8, 10);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, "1", "48", courseEntry);
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		previewSearchParams.setGeneratorKeys(List.of(generator.getKey()));
		previewSearchParams.setDateRange(new DateRange(new GregorianCalendar(2010, 9, 1).getTime(), new GregorianCalendar(2010, 9, 10).getTime()));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(0);
		
		// Override start into the range
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(courseEntry.getKey());
		override.setIdentifier(sut.getDueDateIdentifier(generator, courseEntry));
		override.setStart(new GregorianCalendar(2010, 9, 2).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}

	@Test
	public void shouldNotCreatePreviewDueDate_overrideMovedOutOfRanged() {
		GregorianCalendar lifecycleStart = new GregorianCalendar(2010, 8, 1);
		GregorianCalendar lifecycleEnd = new GregorianCalendar(2010, 8, 10);
		RepositoryEntry courseEntry = createCourse(lifecycleStart, lifecycleEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createCourseBeginConfigs(generator, "1", "48", courseEntry);
		
		GeneratorPreviewSearchParams previewSearchParams = new GeneratorPreviewSearchParams();
		previewSearchParams.setGeneratorKeys(List.of(generator.getKey()));
		previewSearchParams.setDateRange(new DateRange(new GregorianCalendar(2010, 8, 1).getTime(), new GregorianCalendar(2010, 8, 10).getTime()));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, previewSearchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.regular);
		
		// Override start outside range
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(courseEntry.getKey());
		override.setIdentifier(sut.getDueDateIdentifier(generator, courseEntry));
		override.setStart(new GregorianCalendar(2010, 7, 1).getTime());
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, previewSearchParams);
		
		assertThat(previews).hasSize(0);
	}

	private RepositoryEntry createCourse(GregorianCalendar lifecycleStart, GregorianCalendar lifecycleEnd) {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		RepositoryEntryLifecycle lifecycle = lifecycleDAO.create(null, null, false,
				lifecycleStart != null ? lifecycleStart.getTime() : null,
				lifecycleEnd != null ? lifecycleEnd.getTime() : null);
		courseEntry = repositoryManager.setDescriptionAndName(courseEntry, null, null, null, null, null, null, null, null, lifecycle);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		repositoryEntryRelationDao.addRole(coach1, courseEntry, GroupRoles.coach.name());
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		repositoryEntryRelationDao.addRole(coach2, courseEntry, GroupRoles.coach.name());
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

	private QualityGeneratorConfigs createDailyConfigs(QualityGenerator generator, String durationHours, List<? extends RepositoryEntryRef> courseEntries) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CourseProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CourseProvider.CONFIG_KEY_ROLES, "coach");
		configs.setValue(CourseProvider.CONFIG_KEY_TRIGGER, CourseProvider.CONFIG_KEY_TRIGGER_DAILY);
		configs.setValue(CourseProvider.CONFIG_KEY_DURATION_HOURS, durationHours);
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_HOUR, "14");
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_MINUTE, "02");
		List<DayOfWeek> dayOfWeeks = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
		configs.setValue(CourseProvider.CONFIG_KEY_DAILY_WEEKDAYS, ProviderHelper.concatDaysOfWeek(dayOfWeeks));
		// Restrict to a single course, because a lot of courses with the same life cycle are generated.
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, courseEntries);
		dbInstance.commitAndCloseSession();
		return configs;
	}

	private QualityGenerator createGeneratorInDefaultOrganisation() {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Collection<Organisation> organisations = Collections.singletonList(organisation);
		QualityGenerator generator = generatorService.createGenerator(sut.getType(), organisations);
		RepositoryEntry formEntry = qualityTestHelper.createFormEntry();
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
