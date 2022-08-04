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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
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
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToTaxonomyLevelDAO;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityContextToCurriculum;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.QualityContextToOrganisation;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementQualityContextBuilderTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityContextDAO contextDao;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryTaxonomyDao;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}

	@Test
	public void shouldInitWithAllRelations() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation curriculumOrganisation1 = organisationService.createOrganisation("Org-29", UUID.randomUUID().toString(), "", null,
				null);
		Organisation curriculumOrganisation2Parent = organisationService.createOrganisation("Org-30", UUID.randomUUID().toString(), "",
				null, null);
		Organisation curriculumOrganisation2 = organisationService.createOrganisation("Org-31", UUID.randomUUID().toString(), "",
				curriculumOrganisation2Parent, null);
		
		Curriculum curriculum1 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 1", "", false,
				curriculumOrganisation1);
		Curriculum curriculum2 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 2", "", false,
				curriculumOrganisation2);

		CurriculumElement curriculumElementParent = curriculumService.createCurriculumElement(
				UUID.randomUUID().toString(), "Element", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum2);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 1", CurriculumElementStatus.active, null, null, curriculumElementParent, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum1);
		CurriculumElement otherCurriculumElement = curriculumService.createCurriculumElement(
				UUID.randomUUID().toString(), "Element 2", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum2);

		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		curriculumService.addRepositoryEntry(curriculumElement, entry, true);

		Taxonomy taxonomy = taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "Taxonomy", "", null);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevel2Parent = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null,
				null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				taxonomyLevel2Parent, taxonomy);
		TaxonomyLevel taxonomyLevelRepo = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevelOfCurriculumElement = taxonomyService
				.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(curriculumElement, taxonomyLevel1);
		curriculumElementToTaxonomyLevelDao.createRelation(curriculumElement, taxonomyLevel2);
		repositoryTaxonomyDao.createRelation(entry, taxonomyLevelRepo);
		
		curriculumService.addMember(curriculumElement, executor, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElement, executor, CurriculumRoles.coach);
		curriculumService.addMember(otherCurriculumElement, executor, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		QualityContext context = CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.participant)
				.build();

		assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		assertThat(context.getRole()).isEqualTo(QualityContextRole.participant);
		assertThat(context.getAudienceCurriculumElement()).isEqualTo(curriculumElement);
		assertThat(context.getAudienceRepositoryEntry()).isNull();
		
		List<Curriculum> curriculms = context
				.getContextToCurriculum().stream()
				.map(QualityContextToCurriculum::getCurriculum)
				.collect(Collectors.toList());
		assertThat(curriculms)
				.containsExactlyInAnyOrder(curriculum1)
				.doesNotContain(curriculum2);
		
		List<CurriculumElement> curriculumElements = context
				.getContextToCurriculumElement().stream()
				.map(QualityContextToCurriculumElement::getCurriculumElement)
				.collect(Collectors.toList());
		assertThat(curriculumElements)
				.containsExactlyInAnyOrder(curriculumElement)
				.doesNotContain(otherCurriculumElement);
		
		List<TaxonomyLevel> taxonomyLevels = context
				.getContextToTaxonomyLevel().stream()
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel)
				.collect(Collectors.toList());
		assertThat(taxonomyLevels)
				.containsExactlyInAnyOrder(taxonomyLevel1, taxonomyLevel2)
				.doesNotContain(taxonomyLevel2Parent, taxonomyLevelOfCurriculumElement);
	}
	
	@Test
	public void shouldAddMatchingUserAndCurriculumOrganisations() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation organisationExecutorAndCurriculum = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculum, executor, OrganisationRoles.user);
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		
		Curriculum curriculum = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum", "", false,
				organisationExecutorAndCurriculum);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		QualityContext context = CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.participant)
				.build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(organisationExecutorAndCurriculum)
				.doesNotContain(organisationExecutorOnly);
	}
	
	@Test
	public void shouldAddAllUserOrganisationsIfNoMatch() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		Organisation organisationCurriculumOnly = qualityTestHelper.createOrganisation();
		
		Curriculum curriculum = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum", "", false,
				organisationCurriculumOnly);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		QualityContext context = CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.participant)
				.build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(defaultOrganisation, organisationExecutorOnly)
				.doesNotContain(organisationCurriculumOnly);
	}
	
	@Test
	public void shouldAddAllUserOrganisationsIfOnlyManagerMatch() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		Organisation organisationExecutorAndCurriculumButManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculumButManager, executor, OrganisationRoles.usermanager);
		
		Curriculum curriculum = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum", "", false,
				organisationExecutorAndCurriculumButManager);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		QualityContext context = CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.participant)
				.build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(defaultOrganisation, organisationExecutorOnly)
				.doesNotContain(organisationExecutorAndCurriculumButManager);
	}
	
	@Test
	public void shouldNotDeleteContextOfOtherRoles() {
		Curriculum curriculum = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum", "", false, null);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Curriculum", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		QualityContext participantContext = CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.participant).build();
		dbInstance.commitAndCloseSession();
		
		CurriculumElementQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, curriculumElement, CurriculumRoles.coach).build();
		dbInstance.commitAndCloseSession();
		
		QualityContext reloadedParticipantContext = contextDao.loadByKey(participantContext);
		assertThat(reloadedParticipantContext).isNotNull();
	}

}
