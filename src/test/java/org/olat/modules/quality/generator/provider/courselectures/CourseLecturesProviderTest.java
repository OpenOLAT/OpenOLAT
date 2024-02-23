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
import static org.olat.modules.quality.generator.QualityGeneratorOverrides.NO_OVERRIDES;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorOverrideSearchParams;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.QualityGeneratorOverrides;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverrideImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverridesImpl;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;
import org.olat.modules.quality.generator.ui.RepositoryEntryBlackListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
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
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		assertThat(generated).isEmpty();
	}
	
	@Test
	public void shouldNotCreateDataCollectionIfOverrideInOtherRepository() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntryPast = createCourseWithLecture(DateUtils.addDays(new Date(), -12), DateUtils.addDays(new Date(), -11), List.of(teacher));
		RepositoryEntry courseEntryOverride = createCourseWithLecture(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 3), List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntryPast);
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, List.of(courseEntryPast, courseEntryOverride));
		dbInstance.commitAndCloseSession();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, DateUtils.addDays(new Date(), -20), new Date());
		dbInstance.commitAndCloseSession();
		assertThat(generated).isEmpty();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), -20), DateUtils.addDays(new Date(), 10)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, NO_OVERRIDES, searchParams);
		QualityPreview privewOverride = previews.stream()
				.filter(preview -> preview.getIdentifier().indexOf(courseEntryOverride.getKey().toString()) > -1)
				.findFirst().get();
		
		QualityGeneratorOverride override = generatorService.createOverride(teacher, privewOverride.getIdentifier(), privewOverride.getGenerator(), privewOverride.getGeneratorProviderKey());
		override.setStart(DateUtils.addDays(new Date(), -2));
		generatorService.updateOverride(teacher, override);
		dbInstance.commitAndCloseSession();
		
		GeneratorOverrideSearchParams overrideSearchParams = new GeneratorOverrideSearchParams();
		overrideSearchParams.setGenerators(List.of(generator));
		QualityGeneratorOverrides overrides = generatorService.getOverrides(overrideSearchParams);
		
		// Start was moved, but not in this range
		generated = sut.generate(generator, configs, overrides, DateUtils.addDays(new Date(), -20), DateUtils.addDays(new Date(), -3));
		dbInstance.commitAndCloseSession();
		assertThat(generated).isEmpty();
		
		// Start was moved in this range
		generated = sut.generate(generator, configs, overrides, DateUtils.addDays(new Date(), -20), new Date());
		dbInstance.commitAndCloseSession();
		assertThat(generated).hasSize(1);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, to);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, to);
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
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
		QualityDataCollection dataCollection = generated.get(0);
		dbInstance.commitAndCloseSession();
		
		Date beforeEndAsConfigured = DateUtils.addMinutes(lectureEnd, -startDataCollectionBeforeEnd);
		long withinAMinute = 60*1000;
		Date deadline = DateUtils.addDays(beforeEndAsConfigured, durationDays);
		assertThat(dataCollection.getStart()).isCloseTo(beforeEndAsConfigured, withinAMinute);
		assertThat(dataCollection.getDeadline()).isCloseTo(deadline, withinAMinute);
	}
	
	@Test
	public void shouldCreatePreview() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		QualityPreview preview = previews.get(0);
		assertThat(preview.getGenerator().getKey()).isEqualTo(generator.getKey());
		assertThat(preview.getGeneratorProviderKey()).isEqualTo(courseEntry.getKey());
		assertThat(preview.getFormEntry().getKey()).isEqualTo(generator.getFormEntry().getKey());
		assertThat(preview.getTopicType()).isEqualTo(QualityDataCollectionTopicType.IDENTIY);
		assertThat(preview.getTopicIdentity()).isEqualTo(teacher);
		assertThat(preview.getNumParticipants()).isEqualTo(3);
		assertThat(preview.getStatus()).isEqualTo(QualityPreviewStatus.regular);
	}
	
	@Test
	public void shouldCreatePreview_includeBlacklisted() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		RepositoryEntry courseEntryBlacklist = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();

		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, List.of(courseEntry, courseEntryBlacklist));
		RepositoryEntryBlackListController.setRepositoryEntryRefs(configs, List.of(courseEntryBlacklist));
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews)
				.hasSize(2)
				.extracting(QualityPreview::getStatus)
				.containsExactlyInAnyOrder(QualityPreviewStatus.regular, QualityPreviewStatus.blacklist);
	}
	
	@Test
	public void shouldCreatePreview_restrictRepositoryEntry() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		RepositoryEntry courseEntry1 = createCourseWithLecture(startDate, startEnd);
		RepositoryEntry courseEntry2 = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();

		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry1);
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, List.of(courseEntry1, courseEntry2));
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).hasSize(2);
		
		searchParams.setRepositoryEntry(courseEntry1);
		previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).hasSize(1);
		
		searchParams.setRepositoryEntryKeys(List.of());
		previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_excludeAlreadyGenerated() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd);
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		sut.generate(generator, configs, NO_OVERRIDES, DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5));
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_excludeOutsideDateRange() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 5), DateUtils.addDays(new Date(), 6)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_override() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 7)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		
		LectureBlockInfo lectureBlockInfo = new LectureBlockInfo(null, teacher.getKey(), courseEntry.getKey(), null, null, null, null);
		QualityDataCollectionTopicType topicType = sut.getGeneratedTopicType(configs);
		Long generatorProviderKey = sut.getGeneratorProviderKey(lectureBlockInfo, topicType);
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(generatorProviderKey);
		override.setIdentifier(sut.getIdentifier(generator, courseEntry, teacher));
		override.setStart(DateUtils.addDays(new Date(), 5));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}

	@Test
	public void shouldCreatePreview_overrideMovedIntoRangeFromFuture() {
		Date startDate = DateUtils.addDays(new Date(), 20);
		Date startEnd = DateUtils.addDays(new Date(), 21);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 7)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
		
		LectureBlockInfo lectureBlockInfo = new LectureBlockInfo(null, teacher.getKey(), courseEntry.getKey(), null, null, null, null);
		QualityDataCollectionTopicType topicType = sut.getGeneratedTopicType(configs);
		Long generatorProviderKey = sut.getGeneratorProviderKey(lectureBlockInfo, topicType);
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(generatorProviderKey);
		override.setIdentifier(sut.getIdentifier(generator, courseEntry, teacher));
		override.setStart(DateUtils.addDays(new Date(), 4));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}

	@Test
	public void shouldCreatePreview_overrideMovedIntoRangeFromPast() {
		Date startDate = DateUtils.addDays(new Date(), -20);
		Date startEnd = DateUtils.addDays(new Date(), -19);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 7)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
		
		LectureBlockInfo lectureBlockInfo = new LectureBlockInfo(null, teacher.getKey(), courseEntry.getKey(), null, null, null, null);
		QualityDataCollectionTopicType topicType = sut.getGeneratedTopicType(configs);
		Long generatorProviderKey = sut.getGeneratorProviderKey(lectureBlockInfo, topicType);
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(generatorProviderKey);
		override.setIdentifier(sut.getIdentifier(generator, courseEntry, teacher));
		override.setStart(DateUtils.addDays(new Date(), 4));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}

	@Test
	public void shouldCreatePreview_overrideMovedOutOfRange() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		Date startEnd = DateUtils.addDays(new Date(), 4);
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = createCourseWithLecture(startDate, startEnd, List.of(teacher));
		
		QualityGenerator generator = createGeneratorInDefaultOrganisation();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", courseEntry);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		
		LectureBlockInfo lectureBlockInfo = new LectureBlockInfo(null, teacher.getKey(), courseEntry.getKey(), null, null, null, null);
		QualityDataCollectionTopicType topicType = sut.getGeneratedTopicType(configs);
		Long generatorProviderKey = sut.getGeneratorProviderKey(lectureBlockInfo, topicType);
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(generatorProviderKey);
		override.setIdentifier(sut.getIdentifier(generator, courseEntry, teacher));
		override.setStart(DateUtils.addDays(new Date(), 20));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).isEmpty();
	}

	private RepositoryEntry createCourseWithLecture(Date lectureStartDate, Date lectureEndDate) {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createCourseWithLecture(lectureStartDate, lectureEndDate, List.of(teacher));
	}
	
	private RepositoryEntry createCourseWithLecture(Date lectureStartDate, Date lectureEndDate, List<Identity> teachers) {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		
		repositoryEntryRelationDao.addRole(JunitTestHelper.createAndPersistIdentityAsUser(random()), courseEntry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(JunitTestHelper.createAndPersistIdentityAsUser(random()), courseEntry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(JunitTestHelper.createAndPersistIdentityAsUser(random()), courseEntry, GroupRoles.participant.name());
		
		for (Identity teacher : teachers) {
			LectureBlock lectureBlock = lectureService.createLectureBlock(courseEntry);
			lectureBlock.setPlannedLecturesNumber(3);
			lectureBlock.setStartDate(lectureStartDate);
			lectureBlock.setEndDate(lectureEndDate);
			lectureService.save(lectureBlock, null);
			lectureService.addTeacher(lectureBlock, teacher);
		}
		
		dbInstance.commitAndCloseSession();
		return courseEntry;
	}

	private QualityGeneratorConfigs createConfigs(QualityGenerator generator, String durationDays, RepositoryEntry courseEntry) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_TOPIC, CourseLecturesProvider.CONFIG_KEY_TOPIC_COACH);
		configs.setValue(CourseLecturesProvider.CONFIG_KEY_ROLES, "participant");
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
