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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.06.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryQualityContextBuilderTest extends OlatTestCase {

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
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryTaxonomyDao;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}

	@Test
	public void shouldInitParticipantContextWithAllRelations() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String location = "Kaiserslautern";
		repositoryManager.setDescriptionAndName(entry, "", null, null, location, null, null, null, null, null);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation curriculumOrganisation1 = organisationService.createOrganisation("Org-10", UUID.randomUUID().toString(),
				"", null, null);
		Organisation curriculumOrganisation2Parent = organisationService.createOrganisation("Org-11",
				UUID.randomUUID().toString(), "", null, null);
		Organisation curriculumOrganisation2 = organisationService.createOrganisation("Org-12", UUID.randomUUID().toString(),
				"", curriculumOrganisation2Parent, null);
		Organisation curriculumOrganisation3 = organisationService.createOrganisation("Org-13", UUID.randomUUID().toString(),
				"", null, null);
		Curriculum curriculum1 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 1", "",
				curriculumOrganisation1);
		Curriculum curriculum2 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 2", "",
				curriculumOrganisation2);
		Curriculum curriculum3 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 3", "",
				curriculumOrganisation3);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 1", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		CurriculumElement curriculumElement2Parent = curriculumService.createCurriculumElement(
				UUID.randomUUID().toString(), "Element 2 parent", CurriculumElementStatus.active, null, null, null,
				null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum2);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 2", CurriculumElementStatus.active, null, null, curriculumElement2Parent, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum2);
		CurriculumElement curriculumElement3 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 3", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum3);
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement3, entry, true);
		Taxonomy taxonomy = taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "Taxonomy", "", null);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevel2Parent = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null,
				null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				taxonomyLevel2Parent, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevelOfCurriculumElement = taxonomyService
				.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null, null, taxonomy);
		repositoryTaxonomyDao.createRelation(entry, taxonomyLevel1);
		repositoryTaxonomyDao.createRelation(entry, taxonomyLevel2);
		curriculumElementToTaxonomyLevelDao.createRelation(curriculumElement1, taxonomyLevelOfCurriculumElement);
		curriculumService.addMember(curriculumElement1, executor, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElement2, executor, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElement3, executor, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();

		entry = repositoryService.loadByKey(entry.getKey());
		QualityContext context = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.participant).build();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		softly.assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		softly.assertThat(context.getRole()).isEqualTo(QualityContextRole.participant);
		softly.assertThat(context.getLocation()).isEqualTo(location);
		softly.assertThat(context.getAudienceRepositoryEntry()).isEqualTo(entry);
		
		List<Curriculum> curriculms = context
				.getContextToCurriculum().stream()
				.map(QualityContextToCurriculum::getCurriculum)
				.collect(Collectors.toList());
		softly.assertThat(curriculms)
				.containsExactlyInAnyOrder(curriculum1, curriculum2)
				.doesNotContain(curriculum3);
		
		List<CurriculumElement> curriculumElements = context
				.getContextToCurriculumElement().stream()
				.map(QualityContextToCurriculumElement::getCurriculumElement)
				.collect(Collectors.toList());
		softly.assertThat(curriculumElements)
				.containsExactlyInAnyOrder(curriculumElement1, curriculumElement2)
				.doesNotContain(curriculumElement3);
		
		List<TaxonomyLevel> taxonomyLevels = context
				.getContextToTaxonomyLevel().stream()
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel)
				.collect(Collectors.toList());
		softly.assertThat(taxonomyLevels)
				.containsExactlyInAnyOrder(taxonomyLevel1, taxonomyLevel2)
				.doesNotContain(taxonomyLevel3, taxonomyLevelOfCurriculumElement);
		softly.assertAll();
	}
	
	@Test
	public void shouldInitCoachContextWithAllRelations() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String location = "Frankfurt";
		repositoryManager.setDescriptionAndName(entry, "", null, null, location, null, null, null, null, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		Identity participantOther = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(coach));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);

		Organisation curriculumOrganisation1 = organisationService.createOrganisation("Org-1", UUID.randomUUID().toString(), "", null,
				null);
		Organisation curriculumOrganisation2Parent = organisationService.createOrganisation("Org-26", UUID.randomUUID().toString(), "",
				null, null);
		Organisation organisation2 = organisationService.createOrganisation("Org-27", UUID.randomUUID().toString(), "",
				curriculumOrganisation2Parent, null);
		Organisation curriculumOrganisationOther = organisationService.createOrganisation("Org-28", UUID.randomUUID().toString(), "", null,
				null);
		Curriculum curriculum1 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 1", "",
				curriculumOrganisation1);
		Curriculum curriculum2 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 2", "",
				organisation2);
		Curriculum curriculumOther = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Other curriculum", "",
				curriculumOrganisationOther);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 1", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		CurriculumElement curriculumElement2Parent = curriculumService.createCurriculumElement(
				UUID.randomUUID().toString(), "Element 2 parent", CurriculumElementStatus.active, null, null, null,
				null, null, CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum2);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 2", CurriculumElementStatus.active, null, null, curriculumElement2Parent, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum2);
		CurriculumElement curriculumElementOther = curriculumService.createCurriculumElement(
				UUID.randomUUID().toString(), "Other element", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculumOther);
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		Taxonomy taxonomy = taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "Taxonomy", "", null);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevel2Parent = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null,
				null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				taxonomyLevel2Parent, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null,
				null, taxonomy);
		TaxonomyLevel taxonomyLevelOfCurriculumElement = taxonomyService
				.createTaxonomyLevel(UUID.randomUUID().toString(), random(), null, null, null, taxonomy);
		repositoryTaxonomyDao.createRelation(entry, taxonomyLevel1);
		repositoryTaxonomyDao.createRelation(entry, taxonomyLevel2);
		curriculumElementToTaxonomyLevelDao.createRelation(curriculumElement1, taxonomyLevelOfCurriculumElement);
		curriculumService.addMember(curriculumElement1, coach, CurriculumRoles.coach);
		curriculumService.addMember(curriculumElement1, participant1, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElement2, participant2, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElement2, participant3, CurriculumRoles.participant);
		curriculumService.addMember(curriculumElementOther, participantOther, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		entry = repositoryService.loadByKey(entry.getKey());
		QualityContext context = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.coach).build();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		softly.assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		softly.assertThat(context.getRole()).isEqualTo(QualityContextRole.coach);
		softly.assertThat(context.getLocation()).isEqualTo(location);
		softly.assertThat(context.getAudienceRepositoryEntry()).isEqualTo(entry);
		
		List<Curriculum> curriculms = context
				.getContextToCurriculum().stream()
				.map(QualityContextToCurriculum::getCurriculum)
				.collect(Collectors.toList());
		softly.assertThat(curriculms)
				.containsExactlyInAnyOrder(curriculum1, curriculum2)
				.doesNotContain(curriculumOther);
		
		List<CurriculumElement> curriculumElements = context
				.getContextToCurriculumElement().stream()
				.map(QualityContextToCurriculumElement::getCurriculumElement)
				.collect(Collectors.toList());
		softly.assertThat(curriculumElements)
				.containsExactlyInAnyOrder(curriculumElement1, curriculumElement2)
				.doesNotContain(curriculumElementOther);
		
		List<TaxonomyLevel> taxonomyLevels = context
				.getContextToTaxonomyLevel().stream()
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel)
				.collect(Collectors.toList());
		softly.assertThat(taxonomyLevels)
				.containsExactlyInAnyOrder(taxonomyLevel1, taxonomyLevel2)
				.doesNotContain(taxonomyLevel3, taxonomyLevelOfCurriculumElement);
		softly.assertAll();
	}
	
	@Test
	public void shouldAddMatchingUserAndCurriculumOrganisation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation organisationExecutorAndCurriculum1 = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculum1, executor, OrganisationRoles.user);
		Organisation organisationExecutorAndCurriculum2 = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculum2, executor, OrganisationRoles.user);
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		Organisation organisationExecutorAndCurriculumButManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculumButManager, executor, OrganisationRoles.usermanager);
		Organisation organisationRepositoreyOnly = qualityTestHelper.createOrganisation();
		
		Curriculum curriculum1 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 1", "",
				organisationExecutorAndCurriculum1);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 1", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		curriculumService.addMember(curriculumElement1, executor, CurriculumRoles.participant);
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);

		Curriculum curriculum2 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 2", "",
				organisationExecutorAndCurriculum2);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 2", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum2);
		curriculumService.addMember(curriculumElement2, executor, CurriculumRoles.participant);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		
		Curriculum curriculum3 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum 3", "",
				organisationExecutorAndCurriculumButManager);
		CurriculumElement curriculumElement3 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element 3", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum3);
		curriculumService.addMember(curriculumElement3, executor, CurriculumRoles.participant);
		curriculumService.addRepositoryEntry(curriculumElement3, entry, true);
		
		entry = repositoryManager.setDescriptionAndName(entry, "Repo. entry", null, null, null, null, null, null, null, null, null, null,
				null, Collections.singletonList(organisationRepositoreyOnly), null, null);
		dbInstance.commitAndCloseSession();
		
		QualityContext context = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.participant).build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(
						organisationExecutorAndCurriculum1,
						organisationExecutorAndCurriculum2)
				.doesNotContain(
						organisationExecutorOnly,
						organisationExecutorAndCurriculumButManager,
						organisationRepositoreyOnly);
	}
	
	@Test
	public void shouldAddMatchingUserAndRepositoryOrganisation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		Organisation organisationExecutorAndCurriculumButManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorAndCurriculumButManager, executor, OrganisationRoles.curriculummanager);
		Organisation organisationUserAndRepository = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationUserAndRepository, executor, OrganisationRoles.user);
		Organisation organisationUserAndRepositoryButManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationUserAndRepositoryButManager, executor, OrganisationRoles.linemanager);
		Organisation organisationRepositoreyOnly = qualityTestHelper.createOrganisation();

		Curriculum curriculum3 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "Curriculum", "",
				organisationExecutorAndCurriculumButManager);
		CurriculumElement curriculumElement3 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"Element", CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum3);
		curriculumService.addMember(curriculumElement3, executor, CurriculumRoles.participant);
		curriculumService.addRepositoryEntry(curriculumElement3, entry, true);
		
		entry = repositoryManager.setDescriptionAndName(entry, "Repo. entry", null, null, null, null, null, null, null, null, null, null, null,
				Arrays.asList(organisationUserAndRepository, organisationUserAndRepositoryButManager, organisationRepositoreyOnly), null, null);
		dbInstance.commitAndCloseSession();
		
		QualityContext context = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.participant).build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(
						organisationUserAndRepository)
				.doesNotContain(
						organisationExecutorOnly,
						organisationExecutorAndCurriculumButManager,
						organisationRepositoreyOnly);
	}
	
	@Test
	public void shouldAddAllUserOrganisationsIfNoMatch() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation executorDefaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisationExecutorOnly = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationExecutorOnly, executor, OrganisationRoles.user);
		Organisation organisationUserAndRepositoryButManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(organisationUserAndRepositoryButManager, executor, OrganisationRoles.linemanager);
		Organisation organisationRepositoreyOnly = qualityTestHelper.createOrganisation();

		entry = repositoryManager.setDescriptionAndName(entry, "Repo. entry", null, null, null, null, null, null, null, null, null, null, null,
				Arrays.asList(organisationUserAndRepositoryButManager, organisationRepositoreyOnly), null, null);
		dbInstance.commitAndCloseSession();
		
		QualityContext context = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.participant).build();

		List<Organisation> organisations = context
				.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(
						executorDefaultOrganisation,
						organisationExecutorOnly)
				.doesNotContain(
						organisationUserAndRepositoryButManager,
						organisationRepositoreyOnly);
	}
	
	@Test
	public void shouldNotDeleteContextOfOtherRoles() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		QualityContext participantContext = RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.participant).build();
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation, entry, GroupRoles.coach).build();
		dbInstance.commitAndCloseSession();
		
		QualityContext reloadedParticipantContext = contextDao.loadByKey(participantContext);
		assertThat(reloadedParticipantContext).isNotNull();
	}

}
