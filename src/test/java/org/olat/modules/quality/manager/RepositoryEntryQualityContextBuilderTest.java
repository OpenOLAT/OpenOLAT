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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
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
	private RepositoryEntryToTaxonomyLevelDAO repositoryTaxonomyDao;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}

	@Test
	public void shouldInitWithAllRelations() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);

		Organisation organisation1 = organisationService.createOrganisation("", UUID.randomUUID().toString(), "", null,
				null);
		Organisation organisation2Parent = organisationService.createOrganisation("", UUID.randomUUID().toString(), "",
				null, null);
		Organisation organisation2 = organisationService.createOrganisation("", UUID.randomUUID().toString(), "",
				organisation2Parent, null);
		Organisation organisation3 = organisationService.createOrganisation("", UUID.randomUUID().toString(), "", null,
				null);
		Curriculum curriculum1 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "", "",
				organisation1);
		Curriculum curriculum2 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "", "",
				organisation2);
		Curriculum curriculum3 = curriculumService.createCurriculum(UUID.randomUUID().toString(), "", "",
				organisation3);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"", null, null, null, null, curriculum1);
		CurriculumElement curriculumElement2Parent = curriculumService
				.createCurriculumElement(UUID.randomUUID().toString(), "", null, null, null, null, curriculum2);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"", null, null, curriculumElement2Parent, null, curriculum2);
		CurriculumElement curriculumElement3 = curriculumService.createCurriculumElement(UUID.randomUUID().toString(),
				"", null, null, null, null, curriculum3);
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement3, entry, true);
		Taxonomy taxonomy = taxonomyService.createTaxonomy(UUID.randomUUID().toString(), "", "", null);
		TaxonomyLevel taxonomyLevel1 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), "", "", null,
				null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2Parent = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), "", "",
				null, null, null, taxonomy);
		TaxonomyLevel taxonomyLevel2 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), "", "", null,
				null, taxonomyLevel2Parent, taxonomy);
		TaxonomyLevel taxonomyLevel3 = taxonomyService.createTaxonomyLevel(UUID.randomUUID().toString(), "", "", null,
				null, null, taxonomy);
		TaxonomyLevel taxonomyLevelOfCurriculumElement = taxonomyService
				.createTaxonomyLevel(UUID.randomUUID().toString(), "", "", null, null, null, taxonomy);
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

		assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		assertThat(context.getRole()).isEqualTo(QualityContextRole.participant);
		assertThat(context.getAudienceRepositoryEntry()).isEqualTo(entry);
		List<Curriculum> curriculms = context.getContextToCurriculum().stream()
				.map(QualityContextToCurriculum::getCurriculum).collect(Collectors.toList());
		assertThat(curriculms).containsExactlyInAnyOrder(curriculum1, curriculum2).doesNotContain(curriculum3);
		List<CurriculumElement> curriculumElements = context.getContextToCurriculumElement().stream()
				.map(QualityContextToCurriculumElement::getCurriculumElement).collect(Collectors.toList());
		assertThat(curriculumElements).containsExactlyInAnyOrder(curriculumElement1, curriculumElement2)
				.doesNotContain(curriculumElement3);
		List<Organisation> organisations = context.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation).collect(Collectors.toList());
		assertThat(organisations).containsExactlyInAnyOrder(organisation1, organisation2).doesNotContain(organisation3);
		List<TaxonomyLevel> taxonomyLevels = context.getContextToTaxonomyLevel().stream()
				.map(QualityContextToTaxonomyLevel::getTaxonomyLevel).collect(Collectors.toList());
		assertThat(taxonomyLevels).containsExactlyInAnyOrder(taxonomyLevel1, taxonomyLevel2)
				.doesNotContain(taxonomyLevel3, taxonomyLevelOfCurriculumElement);
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
