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
package org.olat.modules.quality.generator.provider.curriculumelement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.quality.generator.QualityGeneratorOverrides.NO_OVERRIDES;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorOverrides;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverrideImpl;
import org.olat.modules.quality.generator.model.QualityGeneratorOverridesImpl;
import org.olat.modules.quality.generator.ui.CurriculumElementBlackListController;
import org.olat.modules.quality.generator.ui.CurriculumElementWhiteListController;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementProviderTest extends OlatTestCase {

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
	private CurriculumService curriculumService;
	
	@Autowired
	private CurriculumElementProvider sut;
	
	@Test
	public void shouldCopyOrganisationsOfCurriculumElementToDataCollection() {
		Date startDate = new GregorianCalendar(2010, 6, 3).getTime();
		
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation curriculumOrganisation = createOrganisation(defaultOrganisation);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), false, curriculumOrganisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(), null, null,
				null, null, null, null, null, null, curriculum);
		curriculumElement.setBeginDate(startDate);
		curriculumService.updateCurriculumElement(curriculumElement);
		
		QualityGenerator generator = createGenerator();
		String durationDays = "10";
		QualityGeneratorConfigs configs = createConfigs(generator, durationDays, curriculumElement);
		
		Date lastRun = new GregorianCalendar(2010, 6, 1).getTime();
		Date now = new GregorianCalendar(2010, 6, 13).getTime();
		
		List<QualityDataCollection> generated = sut.generate(generator, configs, NO_OVERRIDES, lastRun, now);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dataCollection = generated.get(0);
		List<Organisation> organisations = qualityService.loadDataCollectionOrganisations(dataCollection);
		assertThat(organisations)
				.containsExactlyInAnyOrder(curriculumOrganisation)
				.doesNotContain(defaultOrganisation);
	}
	
	@Test
	public void shouldCreatePreview() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		QualityPreview preview = previews.get(0);
		assertThat(preview.getGenerator().getKey()).isEqualTo(generator.getKey());
		assertThat(preview.getGeneratorProviderKey()).isEqualTo(curriculumElement.getKey());
		assertThat(preview.getFormEntry().getKey()).isEqualTo(generator.getFormEntry().getKey());
		assertThat(preview.getTopicType()).isEqualTo(QualityDataCollectionTopicType.CURRICULUM_ELEMENT);
		assertThat(preview.getTopicCurriculumElement()).isEqualTo(curriculumElement);
		assertThat(preview.getNumParticipants()).isEqualTo(2);
		assertThat(preview.getStatus()).isEqualTo(QualityPreviewStatus.regular);
	}
	
	@Test
	public void shouldCreatePreview_includeBlacklisted() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		CurriculumElement curriculumElementBlackList = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		CurriculumElementWhiteListController.setCurriculumElementRefs(configs, List.of(curriculumElement, curriculumElementBlackList));
		CurriculumElementBlackListController.setCurriculumElementRefs(configs, List.of(curriculumElementBlackList));
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
	public void shouldCreatePreview_excludeAlreadyGenerated() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		sut.generateDataCollection(generator, configs, null, DateUtils.addDays(new Date(), 3), curriculumElement);
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_excludeOutsideDateRange() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 20), DateUtils.addDays(new Date(), 30)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_override() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(curriculumElement.getKey());
		override.setIdentifier(sut.getIdentifier(generator, curriculumElement));
		override.setStart(DateUtils.addDays(new Date(), 4));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).hasSize(1);
		assertThat(previews.get(0).getStatus()).isEqualTo(QualityPreviewStatus.changed);
		assertThat(previews.get(0).getStart()).isCloseTo(override.getStart(), 1000);
	}
	
	@Test
	public void shouldCreatePreview_overrideMovedIntoRangeFromFuture() {
		Date startDate = DateUtils.addDays(new Date(), 20);
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
		
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(curriculumElement.getKey());
		override.setIdentifier(sut.getIdentifier(generator, curriculumElement));
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
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).isEmpty();
		
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(curriculumElement.getKey());
		override.setIdentifier(sut.getIdentifier(generator, curriculumElement));
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
		CurriculumElement curriculumElement = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement);
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		
		assertThat(previews).hasSize(1);
		
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setGenerator(generator);
		override.setGeneratorProviderKey(curriculumElement.getKey());
		override.setIdentifier(sut.getIdentifier(generator, curriculumElement));
		override.setStart(DateUtils.addDays(new Date(), 30));
		QualityGeneratorOverridesImpl overrides = new QualityGeneratorOverridesImpl(List.of(override));
		previews = sut.getPreviews(generator, configs, overrides, searchParams);
		
		assertThat(previews).isEmpty();
	}
	
	@Test
	public void shouldCreatePreview_restricCurriculumElement() {
		Date startDate = DateUtils.addDays(new Date(), 3);
		CurriculumElement curriculumElement1 = createCurriculumElement(startDate);
		CurriculumElement curriculumElement2 = createCurriculumElement(startDate);
		
		QualityGenerator generator = createGenerator();
		QualityGeneratorConfigs configs = createConfigs(generator, "10", curriculumElement1);
		CurriculumElementWhiteListController.setCurriculumElementRefs(configs, List.of(curriculumElement1, curriculumElement2));
		dbInstance.commitAndCloseSession();
		
		GeneratorPreviewSearchParams searchParams = new GeneratorPreviewSearchParams();
		searchParams.setDateRange(new DateRange(DateUtils.addDays(new Date(), 2), DateUtils.addDays(new Date(), 5)));
		List<QualityPreview> previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).hasSize(2);
		
		searchParams.setCurriculumElements(List.of(curriculumElement1));
		previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).hasSize(1);
		
		searchParams.setCurriculumElements(List.of());
		previews = sut.getPreviews(generator, configs, QualityGeneratorOverrides.NO_OVERRIDES, searchParams);
		assertThat(previews).isEmpty();
	}

	private QualityGeneratorConfigs createConfigs(QualityGenerator generator, String durationDays, CurriculumElement curriculumElement) {
		QualityGeneratorConfigs configs = new QualityGeneratorConfigsImpl(generator);
		configs.setValue(CurriculumElementProvider.CONFIG_KEY_TITLE, "DATA_COLLECTION_TITLE");
		configs.setValue(CurriculumElementProvider.CONFIG_KEY_ROLES, "coach");
		configs.setValue(CurriculumElementProvider.CONFIG_KEY_DUE_DATE_TYPE, CurriculumElementProvider.CONFIG_KEY_DUE_DATE_BEGIN);
		configs.setValue(CurriculumElementProvider.CONFIG_KEY_DUE_DATE_DAYS, "0");
		configs.setValue(CurriculumElementProvider.CONFIG_KEY_DURATION_DAYS, durationDays);
		// Restrict to a single curriculum element
		CurriculumElementWhiteListController.setCurriculumElementRefs(configs, Collections.singletonList(curriculumElement));
		
		curriculumService.addMember(curriculumElement, JunitTestHelper.createAndPersistIdentityAsUser(random()), CurriculumRoles.coach);
		curriculumService.addMember(curriculumElement, JunitTestHelper.createAndPersistIdentityAsUser(random()), CurriculumRoles.coach);
		
		dbInstance.commitAndCloseSession();
		return configs;
	}

	private QualityGenerator createGenerator() {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Collection<Organisation> organisations = Collections.singletonList(organisation);
		QualityGenerator generator = generatorService.createGenerator(sut.getType(), organisations);
		RepositoryEntry formEntry = qualityTestHelper.createFormEntry();
		generator.setFormEntry(formEntry);
		generatorService.updateGenerator(generator);
		dbInstance.commitAndCloseSession();
		return generator;
	}
	
	private CurriculumElement createCurriculumElement(Date startDate) {
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation curriculumOrganisation = createOrganisation(defaultOrganisation);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), false, curriculumOrganisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(), null, null,
				null, null, null, null, null, null, curriculum);
		curriculumElement.setBeginDate(startDate);
		curriculumService.updateCurriculumElement(curriculumElement);
		dbInstance.commitAndCloseSession();
		return curriculumElement;
	}

	private Organisation createOrganisation(Organisation parent) {
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), parent, null);
		dbInstance.commitAndCloseSession();
		return organisation;
	}

}
