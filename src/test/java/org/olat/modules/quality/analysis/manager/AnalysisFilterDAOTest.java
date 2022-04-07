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
package org.olat.modules.quality.analysis.manager;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.curriculum.CurriculumCalendars.disabled;
import static org.olat.modules.quality.analysis.GroupBy.CONTEXT_CURRICULUM;
import static org.olat.modules.quality.analysis.GroupBy.CONTEXT_ORGANISATION;
import static org.olat.modules.quality.analysis.GroupBy.CONTEXT_TAXONOMY_LEVEL;
import static org.olat.modules.quality.analysis.GroupBy.TOPIC_ORGANISATION;
import static org.olat.modules.quality.analysis.MultiKey.of;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 05.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisFilterDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private EvaluationFormManager evaManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private AnalysisFilterDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldLoadFiguresDataCollectionCount() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Identity executor3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = organisationService.createOrganisation("Org-3", "", null, null, null);
		// Data collection with three participations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dc1, asList(executor1, executor2, executor3));
		// Another data collection with three participations
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dc2, asList(executor1, executor2, executor3));
		// Data collection without participation
		QualityDataCollection dcWithout = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dcWithout));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(3);
	}
	
	@Test
	public void shouldLoadFiguresParticipationCount() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("e1");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("e2");
		Organisation dcOrganisation = organisationService.createOrganisation("Org-4", "", null, null, null);
		// Data collection with two participations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, asList(executor1, executor2));
		qualityService.createContextBuilder(dc1, participations1.get(0)).build();
		qualityService.createContextBuilder(dc1, participations1.get(1)).build();
		// Another data collection with one participations
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, asList(executor1));
		qualityService.createContextBuilder(dc2, participations2.get(0)).build();
		// Another data collection with a done participation. Only a session exists
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations3 = qualityService.addParticipations(dc3, asList(executor1));
		qualityService.createContextBuilder(dc3, participations3.get(0)).build();
		EvaluationFormSession session = evaManager.createSession(participations3.get(0));
		evaManager.finishSession(session);
		// Data collection without participation
		QualityDataCollection dcWithout = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcWithout));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Long count = sut.loadAnalyticFigures(searchParams).getParticipationCount();
		
		assertThat(count).isEqualTo(4);
	}
	
	@Test
	public void shouldLoadDistinctSessions() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		// Data collection with two participations and finished sessions
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc, asList(executor1, executor2));
		// First participation
		EvaluationFormParticipation participation1 = participations1.get(0);
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc, participation1);
		contextBuilder1.addCurriculumElement(element1).addCurriculumElement(element2).build();
		EvaluationFormSession session1 = evaManager.createSession(participation1);
		session1 = evaManager.finishSession(session1);
		// Second participation
		EvaluationFormParticipation participation2 = participations1.get(0);
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc, participation2);
		contextBuilder2.addCurriculumElement(element2).build();
		EvaluationFormSession session2 = evaManager.createSession(participation2);
		evaManager.finishSession(session2);
		// Participation with two curriculums and no session
		QualityDataCollection dcNoSession = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNoSession = qualityService.addParticipations(dcNoSession, Collections.singletonList(executor1));
		EvaluationFormParticipation participationNoSession = participationsNoSession.get(0);
		QualityContextBuilder contextBuilderNoSession = qualityService.createContextBuilder(dcNoSession, participationNoSession);
		contextBuilderNoSession.addCurriculumElement(element1).addCurriculumElement(element2).build();
		finish(asList(dc));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadSessionKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(session1.getKey(), session2.getKey());
	}
	
	@Test
	public void shouldGetAvailableAttributes() {
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicIdentity()).isFalse();
		assertThat(attributes.isTopicRepository()).isFalse();
		assertThat(attributes.isTopicOrganisation()).isFalse();
		assertThat(attributes.isTopicCurriculum()).isFalse();
		assertThat(attributes.isTopicCurriculumElement()).isFalse();
		assertThat(attributes.isContextLocation()).isFalse();
		assertThat(attributes.isContextExecutorOrganisation()).isFalse();
		assertThat(attributes.isContextCurriculum()).isFalse();
		assertThat(attributes.isContextCurriculumElement()).isFalse();
		assertThat(attributes.isContextCurriculumElementType()).isFalse();
		assertThat(attributes.isContextCurriculumOrganisation()).isFalse();
		assertThat(attributes.isContextTaxonomyLevel()).isFalse();
		assertThat(attributes.isSeriesIndex()).isFalse();
		assertThat(attributes.isDataCollection()).isFalse();
	}
	
	@Test
	public void shouldGetAvailableAttributesOfViewerOrganisations() {
		Organisation organisationLevel1 = organisationService.getDefaultOrganisation();
		Organisation organisationLevel2 = qualityTestHelper.createOrganisation(organisationLevel1);
		Organisation organisationLevel3 = qualityTestHelper.createOrganisation(organisationLevel2);
		Organisation organisationOther = qualityTestHelper.createOrganisation();
		
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dataCollection = qualityService.createDataCollection(asList(organisationLevel2), formEntry);
		finish(asList(dataCollection));
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		dataCollection.setTopicIdentity(identity);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		assertThat(attributes.isTopicIdentity()).isTrue();
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(Collections.singletonList(organisationLevel1));
		attributes = sut.getAvailableAttributes(searchParams);
		assertThat(attributes.isTopicIdentity()).isTrue();
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(Collections.singletonList(organisationLevel2));
		attributes = sut.getAvailableAttributes(searchParams);
		assertThat(attributes.isTopicIdentity()).isTrue();
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(Collections.singletonList(organisationLevel3));
		attributes = sut.getAvailableAttributes(searchParams);
		assertThat(attributes.isTopicIdentity()).isFalse();
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionOrganisationRefs(Collections.singletonList(organisationOther));
		attributes = sut.getAvailableAttributes(searchParams);
		assertThat(attributes.isTopicIdentity()).isFalse();
	}
	
	@Test
	public void shouldGetAvailableAttributeForTopicIdentity() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		dataCollection.setTopicIdentity(identity);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicIdentity()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForTopicRepository() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		dataCollection.setTopicRepositoryEntry(entry);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicRepository()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForTopicOrganisation() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Organisation organisation = qualityTestHelper.createOrganisation();
		dataCollection.setTopicOrganisation(organisation);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicOrganisation()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForTopicCurriculum() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		dataCollection.setTopicCurriculum(curriculum);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicCurriculum()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForTopicCurriculumElement() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		CurriculumElement element = qualityTestHelper.createCurriculumElement();
		dataCollection.setTopicCurriculumElement(element);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isTopicCurriculumElement()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextLocation() {
		String location = "loc";
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.withLocation(location)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextLocation()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextOrganisation() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = qualityTestHelper.createOrganisation();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addExecutorOrganisation(organisation)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextExecutorOrganisation()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextCurriculum() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element = qualityTestHelper.createCurriculumElement(curriculum);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addCurriculumElement(element)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextCurriculum()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextCurriculumElement() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		CurriculumElement element = qualityTestHelper.createCurriculumElement();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addCurriculumElement(element)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextCurriculumElement()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextCurriculumElementType() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		CurriculumElement element = qualityTestHelper.createCurriculumElement();
		CurriculumElementType type = curriculumService.createCurriculumElementType("t", "s", null, null);
		element.setType(type);
		element = curriculumService.updateCurriculumElement(element);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addCurriculumElement(element)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextCurriculumElementType()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextCurriculumOrganisation() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		Organisation organisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum(organisation);
		CurriculumElement element = qualityTestHelper.createCurriculumElement(curriculum);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addCurriculumElement(element)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextCurriculumElement()).isTrue();
	}

	@Test
	public void shouldGetAvailableAttributeForContextTaxonpmyLevel() {
		QualityDataCollection dataCollection = createFinishedDataCollection();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("");
		TaxonomyLevel level = qualityTestHelper.createTaxonomyLevel();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				singletonList(executor));
		qualityService.createContextBuilder(dataCollection, participations.get(0))
				.addTaxonomyLevel(level)
				.build();
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isContextTaxonomyLevel()).isTrue();
	}
	
	@Test
	public void shouldGetAvailableAttributeForSeriesIndex() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), dc1, null, null);
		finish(asList(dc1, dc2));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isSeriesIndex()).isTrue();
	}
	
	@Test
	public void shouldGetAvailableAttributeForDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		AvailableAttributes attributes = sut.getAvailableAttributes(searchParams);
		
		assertThat(attributes.isDataCollection()).isTrue();
	}

	private QualityDataCollection createFinishedDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc));
		return dc;
	}
	
	@Test
	public void shouldLoadDistinctTopicOrganisationPaths() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicOrganisation(organisation1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicOrganisation(organisation1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicOrganisation(organisation2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcNoOrganisation = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoOrganisation));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadTopicOrganisationPaths(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(organisation1.getMaterializedPathKeys(), organisation2.getMaterializedPathKeys())
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctTopicCurriculumKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicCurriculum(curriculum1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicCurriculum(curriculum1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicCurriculum(curriculum2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcNoCurriculum = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoCurriculum));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicCurriculumKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(curriculum1.getKey(), curriculum2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctTopicCurriculumElementKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement curriculumElement2 = qualityTestHelper.createCurriculumElement();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicCurriculumElement(curriculumElement1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicCurriculumElement(curriculumElement1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicCurriculumElement(curriculumElement2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcNoCurriculumElement = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoCurriculumElement));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicCurriculumElementKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(curriculumElement1.getKey(), curriculumElement2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctTopicIdentityKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser("i1");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("i2");
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicIdentity(identity1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicIdentity(identity1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicIdentity(identity2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcNoIdentity = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoIdentity));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicIdentityKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(identity1.getKey(), identity2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctTopicRepositoryKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicRepositoryEntry(entry1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicRepositoryEntry(entry1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicRepositoryEntry(entry2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcNoRepository = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoRepository));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicRepositoryKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(entry1.getKey(), entry2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctContextRoles() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("1");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("2");
		Identity executor3 = JunitTestHelper.createAndPersistIdentityAsUser("3");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, asList(executor1, executor2, executor3));
		qualityService.createContextBuilder(dc1, participations1.get(0), course, GroupRoles.coach).build();
		qualityService.createContextBuilder(dc1, participations1.get(1), course, GroupRoles.coach).build();
		qualityService.createContextBuilder(dc1, participations1.get(2)).build();
		finish(asList(dc1));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<QualityContextRole> roles = sut.loadContextRoles(searchParams);
		
		assertThat(roles)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(QualityContextRole.coach, QualityContextRole.none)
				.doesNotContain(QualityContextRole.participant, QualityContextRole.owner);
	}
	
	@Test
	public void shouldLoadDistinctContextLocation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		String location1 = "loc1";
		String location2 = "loc2";
		// Participation with two locations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.withLocation(location1).build();
		// Participation with the same location
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.withLocation(location2).build();
		// Participation without location
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadContextLocations(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(location1, location2)
				.doesNotContainNull();
	}

	
	@Test
	public void shouldLoadDistinctContextOrganisationPathes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		organisationService.removeMember(defaultOrganisation, executor);
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addExecutorOrganisation(organisation1).addExecutorOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addExecutorOrganisation(organisation2).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadContextOrganisationPathes(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(organisation1.getMaterializedPathKeys(), organisation2.getMaterializedPathKeys())
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctContextCurriculum() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Curriculum> filtered = sut.loadContextCurriculums(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(curriculum1, curriculum2)
			.doesNotContainNull();
	}

	@Test
	public void shouldLoadDistinctContextCurriculumElementPathes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum element (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadContextCurriculumElementPathes(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(element1.getMaterializedPathKeys(), element2.getMaterializedPathKeys())
			.doesNotContainNull();
	}

	@Test
	public void shouldLoadDistinctContextCurriculumElementsCurriculumKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum element (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> filtered = sut.loadContextCurriculumElementsCurriculumKey(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(element1.getCurriculum().getKey(), element2.getCurriculum().getKey())
			.doesNotContainNull();
	}

	@Test
	public void shouldLoadDistinctContextCurriculumElementTypes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElementType type1 = curriculumService.createCurriculumElementType("a", "b", null, null);
		CurriculumElementType type2 = curriculumService.createCurriculumElementType("a", "b", null, null);
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, type1, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, type2, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		// Participation with curriculum element of type1
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with curriculum element of type2
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum element of type1 (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<CurriculumElementType> filtered = sut.loadContextCurriculumElementsTypes(searchParams);
		
		assertThat(filtered)
			.containsExactlyInAnyOrder(type1, type2)
			.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctContextOrganisationPath() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum(organisation1);
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum(organisation2);
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Second participation with curriculum (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculumElement(element1).build();
		// Participation without curriculum (to test no nulls)
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcDistinct, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadContextCurriculumOrganisationPathes(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(organisation1.getMaterializedPathKeys(), organisation2.getMaterializedPathKeys())
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctContextTaxonomyLevelPathes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		TaxonomyLevel taxonomyLevel1 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel taxonomyLevel2 = qualityTestHelper.createTaxonomyLevel();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addTaxonomyLevel(taxonomyLevel1).addTaxonomyLevel(taxonomyLevel2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addTaxonomyLevel(taxonomyLevel2).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<String> filtered = sut.loadContextTaxonomyLevelPathes(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(taxonomyLevel1.getMaterializedPathKeys(), taxonomyLevel2.getMaterializedPathKeys())
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctDataCollection() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, asList(executor1, executor2));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		qualityService.createContextBuilder(dc, participations.get(1)).build();
		finish(asList(dc));
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<QualityDataCollection> filtered = sut.loadDataCollection(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(dc)
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadMaxSerieIndex() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = organisationService.createOrganisation("Org-5", "", null, null, null);
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), dc1, null, null);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), dc2, null, null);
		QualityDataCollection dc4 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc5 = qualityService.createDataCollection(asList(dcOrganisation), dc4, null, null);
		QualityDataCollection dc6 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dc4, dc5, dc6));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Integer maxLevels = sut.loadMaxSeriesIndex(searchParams);
		
		assertThat(maxLevels).isEqualTo(3);
	}
	
	@Test
	public void shouldLoadGroupedStatistics() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		String identifier1 = UUID.randomUUID().toString();
		String identifier2 = UUID.randomUUID().toString();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("e1");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("e2");
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicOrganisation(organisation1);
		dc2.setTopicOrganisation(organisation2);
		qualityService.updateDataCollection(dc1);
		qualityService.updateDataCollection(dc2);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, asList(executor1, executor2));
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, asList(executor1, executor2));
		EvaluationFormParticipation participationOrg1Ex1 = participations1.get(0);
		EvaluationFormParticipation participationOrg1Ex2 = participations1.get(1);
		EvaluationFormParticipation participationOrg2Ex1 = participations2.get(0);
		EvaluationFormParticipation participationOrg2Ex2 = participations2.get(1);
		qualityService.createContextBuilder(dc1, participationOrg1Ex1).build();
		qualityService.createContextBuilder(dc1, participationOrg1Ex2).build();
		qualityService.createContextBuilder(dc2, participationOrg2Ex1).build();
		qualityService.createContextBuilder(dc2, participationOrg2Ex2).build();
		EvaluationFormSession sessionOrg1Ex1 = evaManager.createSession(participationOrg1Ex1);
		EvaluationFormSession sessionOrg1Ex2 = evaManager.createSession(participationOrg1Ex2);
		EvaluationFormSession sessionOrg2Ex1 = evaManager.createSession(participationOrg2Ex1);
		EvaluationFormSession sessionOrg2Ex2 = evaManager.createSession(participationOrg2Ex2);
		evaManager.createNumericalResponse(identifier1 , sessionOrg1Ex1, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg1Ex2, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg2Ex1, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg2Ex2, BigDecimal.ZERO);
		evaManager.createNumericalResponse(identifier2 , sessionOrg1Ex1, BigDecimal.ONE);
		evaManager.createNoResponse(identifier2, sessionOrg1Ex2);
		evaManager.finishSession(sessionOrg1Ex1);
		evaManager.finishSession(sessionOrg1Ex2);
		evaManager.finishSession(sessionOrg2Ex1);
		evaManager.finishSession(sessionOrg2Ex2);
		finish(asList(dc1, dc2));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		MultiGroupBy multiGroupBy = MultiGroupBy.of(TOPIC_ORGANISATION);
		List<RawGroupedStatistic> statisticList = sut.loadGroupedStatistic(searchParams,
				asList(identifier1.toString(), identifier2.toString()), true, multiGroupBy, null);
		GroupedStatistics<RawGroupedStatistic> statistics = new GroupedStatistics<>(statisticList);
		
		RawGroupedStatistic statistic11 = statistics.getStatistic(identifier1, of(organisation1.getKey().toString()));
		assertThat(statistic11.getCount()).isEqualTo(2);
		assertThat(statistic11.getRawAvg()).isEqualTo(10);
		RawGroupedStatistic statistic12 = statistics.getStatistic(identifier1, of(organisation2.getKey().toString()));
		assertThat(statistic12.getCount()).isEqualTo(2);
		assertThat(statistic12.getRawAvg()).isEqualTo(5);
		RawGroupedStatistic statistic21 = statistics.getStatistic(identifier2, of(organisation1.getKey().toString()));
		assertThat(statistic21.getCount()).isEqualTo(1);
		assertThat(statistic21.getRawAvg()).isEqualTo(1);
		RawGroupedStatistic statistic22 = statistics.getStatistic(identifier2, of(organisation2.getKey().toString()));
		assertThat(statistic22).isNull();
	}
	
	@Test
	public void shouldLoadGroupedStatisticsWithoutIdentifier() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		String identifier1 = UUID.randomUUID().toString();
		String identifier2 = UUID.randomUUID().toString();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("e1");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("e2");
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicOrganisation(organisation1);
		dc2.setTopicOrganisation(organisation2);
		qualityService.updateDataCollection(dc1);
		qualityService.updateDataCollection(dc2);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, asList(executor1, executor2));
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, asList(executor1, executor2));
		EvaluationFormParticipation participationOrg1Ex1 = participations1.get(0);
		EvaluationFormParticipation participationOrg1Ex2 = participations1.get(1);
		EvaluationFormParticipation participationOrg2Ex1 = participations2.get(0);
		EvaluationFormParticipation participationOrg2Ex2 = participations2.get(1);
		qualityService.createContextBuilder(dc1, participationOrg1Ex1).build();
		qualityService.createContextBuilder(dc1, participationOrg1Ex2).build();
		qualityService.createContextBuilder(dc2, participationOrg2Ex1).build();
		qualityService.createContextBuilder(dc2, participationOrg2Ex2).build();
		EvaluationFormSession sessionOrg1Ex1 = evaManager.createSession(participationOrg1Ex1);
		EvaluationFormSession sessionOrg1Ex2 = evaManager.createSession(participationOrg1Ex2);
		EvaluationFormSession sessionOrg2Ex1 = evaManager.createSession(participationOrg2Ex1);
		EvaluationFormSession sessionOrg2Ex2 = evaManager.createSession(participationOrg2Ex2);
		evaManager.createNumericalResponse(identifier1 , sessionOrg1Ex1, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg1Ex2, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg2Ex1, BigDecimal.TEN);
		evaManager.createNumericalResponse(identifier1 , sessionOrg2Ex2, BigDecimal.ZERO);
		evaManager.createNumericalResponse(identifier2 , sessionOrg1Ex1, BigDecimal.ONE);
		evaManager.createNoResponse(identifier2, sessionOrg1Ex2);
		evaManager.finishSession(sessionOrg1Ex1);
		evaManager.finishSession(sessionOrg1Ex2);
		evaManager.finishSession(sessionOrg2Ex1);
		evaManager.finishSession(sessionOrg2Ex2);
		finish(asList(dc1, dc2));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		MultiGroupBy multiGroupBy = MultiGroupBy.of(TOPIC_ORGANISATION);
		List<RawGroupedStatistic> statisticList = sut.loadGroupedStatistic(searchParams,
				asList(identifier1.toString(), identifier2.toString()), false, multiGroupBy, null);
		GroupedStatistics<RawGroupedStatistic> statistics = new GroupedStatistics<>(statisticList);
		
		RawGroupedStatistic statistic11 = statistics.getStatistic(null, of(organisation1.getKey().toString()));
		assertThat(statistic11.getCount()).isEqualTo(3);
		assertThat(statistic11.getRawAvg()).isEqualTo(21/3);
		RawGroupedStatistic statistic12 = statistics.getStatistic(null, of(organisation2.getKey().toString()));
		assertThat(statistic12.getCount()).isEqualTo(2);
		assertThat(statistic12.getRawAvg()).isEqualTo(5);
		
//		RawGroupedStatistic statistic11 = statistics.getStatistic(identifier1, of(organisation1.getKey().toString()));
//		assertThat(statistic11.getCount()).isEqualTo(2);
//		assertThat(statistic11.getRawAvg()).isEqualTo(10);
//		RawGroupedStatistic statistic12 = statistics.getStatistic(identifier1, of(organisation2.getKey().toString()));
//		assertThat(statistic12.getCount()).isEqualTo(2);
//		assertThat(statistic12.getRawAvg()).isEqualTo(5);
//		RawGroupedStatistic statistic21 = statistics.getStatistic(identifier2, of(organisation1.getKey().toString()));
//		assertThat(statistic21.getCount()).isEqualTo(1);
//		assertThat(statistic21.getRawAvg()).isEqualTo(1);
//		RawGroupedStatistic statistic22 = statistics.getStatistic(identifier2, of(organisation2.getKey().toString()));
//		assertThat(statistic22).isNull();
	}
	
	@Test
	public void shouldLoadStatisticsGroupedByAllKeys() {
		BigDecimal expected = BigDecimal.TEN;
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		String identifier = UUID.randomUUID().toString();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Collection<Organisation> organisations = asList(organisation1, organisation2);
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		Collection<Curriculum> curriculums = asList(curriculum1, curriculum2);
		TaxonomyLevel level1 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel level2 = qualityTestHelper.createTaxonomyLevel();
		Collection<TaxonomyLevel> levels = asList(level1, level2);
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, asList(executor));
		EvaluationFormParticipation participation = participations.get(0);
		QualityContextBuilder contextBuilder = qualityService.createContextBuilder(dc, participation);
		contextBuilder.addExecutorOrganisation(organisation1).addExecutorOrganisation(organisation2).addCurriculumElement(element1)
				.addCurriculumElement(element2).addTaxonomyLevel(level1).addTaxonomyLevel(level2).build();
		EvaluationFormSession session = evaManager.createSession(participation);
		evaManager.createNumericalResponse(identifier , session, expected);
		evaManager.finishSession(session);
		finish(asList(dc));
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		MultiGroupBy multiGroupBy = MultiGroupBy.of(CONTEXT_ORGANISATION, CONTEXT_CURRICULUM, CONTEXT_TAXONOMY_LEVEL);
		List<RawGroupedStatistic> statisticList = sut.loadGroupedStatistic(searchParams,
				asList(identifier), true, multiGroupBy, null);
		GroupedStatistics<RawGroupedStatistic> statistics = new GroupedStatistics<>(statisticList);
		
		for (Organisation organisation: organisations) {
			for (Curriculum curriculum: curriculums) {
				for (TaxonomyLevel level: levels) {
					MultiKey multiKey = of(organisation.getKey().toString(), curriculum.getKey().toString(), level.getKey().toString());
					assertThat(statistics.getStatistic(identifier, multiKey).getRawAvg()).isEqualTo(expected.doubleValue());
				}
			}
		}
	}
	
	@Test
	public void shouldLoadGroupedStatisticForEveryGroupBy() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = organisationService.createOrganisation("Org-6", "", null, null, null);
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc1, asList(executor));
		EvaluationFormParticipation participation = participations.get(0);
		EvaluationFormSession session = evaManager.createSession(participation);
		String identifier = UUID.randomUUID().toString();
		evaManager.createNumericalResponse(identifier , session, BigDecimal.TEN);
		evaManager.finishSession(session);
		finish(asList(dc1));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		for (GroupBy groupBy : GroupBy.values()) {
			MultiGroupBy multiGroupBy = MultiGroupBy.of(groupBy);
			sut.loadGroupedStatistic(searchParams, singletonList(identifier), true, multiGroupBy, null);
		}
		
		// Assert that no exception is thrown.
	}
	
	@Test
	public void shouldLoadGroupedStatisticForEveryTemporalGroupBy() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = organisationService.createOrganisation("Org-7", "", null, null, null);
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc1, asList(executor));
		EvaluationFormParticipation participation = participations.get(0);
		EvaluationFormSession session = evaManager.createSession(participation);
		String identifier = UUID.randomUUID().toString();
		evaManager.createNumericalResponse(identifier , session, BigDecimal.TEN);
		evaManager.finishSession(session);
		finish(asList(dc1));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		for (TemporalGroupBy temporalGroupBy : TemporalGroupBy.values()) {
			MultiGroupBy multiGroupBy = MultiGroupBy.noGroupBy();
			sut.loadGroupedStatistic(searchParams, singletonList(identifier), true,
					multiGroupBy, temporalGroupBy);
		}
		
		// Assert that no exception is thrown.
	}

	@Test
	public void shouldFilterByFinishedDataCollections() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dcFinished1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcFinished1, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dcFinishe2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcFinishe2, QualityDataCollectionStatus.FINISHED);
		QualityDataCollection dcRunning = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityTestHelper.updateStatus(dcRunning, QualityDataCollectionStatus.RUNNING);
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByFormEntry() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherFormEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), otherFormEntry);
		finish(asList(dc1, dc2, dc3));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setFormEntryRef(formEntry);
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByDateRangeFrom() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Date now = new Date();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setDeadline(addDays(now, 1));
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setDeadline(addDays(now, 20));
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dcToEarly = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcToEarly.setDeadline(addDays(now, -2));
		qualityService.updateDataCollection(dcToEarly);
		finish(asList(dc1, dc2, dcToEarly));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDateRangeFrom(now);
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByDateRangeTo() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Date now = new Date();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setDeadline(addDays(now, -1));
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setDeadline(addDays(now, -20));
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dcToLate = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcToLate.setDeadline(addDays(now, 2));
		qualityService.updateDataCollection(dcToLate);
		finish(asList(dc1, dc2, dcToLate));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDateRangeTo(now);
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByDataCollections() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Data collection 1
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		qualityService.createContextBuilder(dc1, participations1.get(0)).build();
		// Data collection 2
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		qualityService.createContextBuilder(dc2, participations2.get(0)).build();
		// Other data collection
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		qualityService.createContextBuilder(dcOther, participationsOther.get(0)).build();
		finish(asList(dc1, dc2, dcOther));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDataCollectionRefs(asList(dc1, dc2));
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections)
				.containsExactlyInAnyOrder(dc1, dc2)
				.doesNotContain(dcOther);
	}
	
	@Test
	public void shouldFilterByTopicIdentity() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser("i1");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("i2");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsUser("io");
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicIdentity(identity1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc2);
		dc2.setTopicIdentity(identity1);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc3);
		dc3.setTopicIdentity(identity2);
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dcOther);
		dcOther.setTopicIdentity(otherIdentity);
		QualityDataCollection dcNoIdentity = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcOther, dcNoIdentity));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicIdentityRefs(asList(identity1, identity2));
		List<Long> keys = sut.loadTopicIdentityKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(identity1.getKey(), identity2.getKey())
				.doesNotContain(otherIdentity.getKey());
	}
	
	@Test
	public void shouldFilterByTopicRepository() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry otherEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicRepositoryEntry(entry1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicRepositoryEntry(entry1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicRepositoryEntry(entry2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcOther.setTopicRepositoryEntry(otherEntry);
		qualityService.updateDataCollection(dcOther);
		QualityDataCollection dcNoRepository = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcOther, dcNoRepository));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicRepositoryRefs(asList(entry1, entry2));
		List<Long> keys = sut.loadTopicRepositoryKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(entry1.getKey(), entry2.getKey())
				.doesNotContain(otherEntry.getKey());
	}
	
	@Test
	public void shouldFilterByTopicOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		Organisation childOrganisation = organisationService.createOrganisation("Org-20", "", null, organisation1, null);
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicOrganisation(organisation1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicOrganisation(organisation1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicOrganisation(organisation2);
		qualityService.updateDataCollection(dc3);
		// Do not load other organisations
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcOther.setTopicOrganisation(otherOrganisation);
		qualityService.updateDataCollection(dcOther);
		// Do not include children in filtered organisations
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcChild.setTopicOrganisation(childOrganisation);
		qualityService.updateDataCollection(dcChild);
		QualityDataCollection dcNoOrganisation = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcOther, dcChild, dcNoOrganisation));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicOrganisationRefs(asList(organisation1, organisation2));
		List<String> paths = sut.loadTopicOrganisationPaths(searchParams);
		
		assertThat(paths)
				.containsExactlyInAnyOrder(organisation1.getMaterializedPathKeys(), organisation2.getMaterializedPathKeys())
				.doesNotContain(otherOrganisation.getMaterializedPathKeys(), childOrganisation.getMaterializedPathKeys());
	}
	
	@Test
	public void shouldFilterByTopicCurriculums() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		Curriculum otherCurriculum = qualityTestHelper.createCurriculum();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicCurriculum(curriculum1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicCurriculum(curriculum1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicCurriculum(curriculum2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcOther.setTopicCurriculum(otherCurriculum);
		qualityService.updateDataCollection(dcOther);
		QualityDataCollection dcNoCurriculum = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcOther, dcNoCurriculum));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicCurriculumRefs(asList(curriculum1, curriculum2));
		List<Long> keys = sut.loadTopicCurriculumKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(curriculum1.getKey(), curriculum2.getKey())
				.doesNotContain(otherCurriculum.getKey());
	}
	
	@Test
	public void shouldFilterByTopicCurriculumElements() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement curriculumElement2 = qualityTestHelper.createCurriculumElement();
		CurriculumElement otherCurriculumElement = qualityTestHelper.createCurriculumElement();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicCurriculumElement(curriculumElement1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc2.setTopicCurriculumElement(curriculumElement1);
		qualityService.updateDataCollection(dc2);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc3.setTopicCurriculumElement(curriculumElement2);
		qualityService.updateDataCollection(dc3);
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcOther.setTopicCurriculumElement(otherCurriculumElement);
		qualityService.updateDataCollection(dcOther);
		QualityDataCollection dcNoCurriculumElement = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcOther, dcNoCurriculumElement));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicCurriculumElementRefs(asList(curriculumElement1, curriculumElement2));
		List<Long> keys = sut.loadTopicCurriculumElementKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(curriculumElement1.getKey(), curriculumElement2.getKey())
				.doesNotContain(otherCurriculumElement.getKey());
	}
	
	@Test
	public void shouldFilterByContextLocation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		String location1 = "loc1";
		String location2 = "loc2";
		// Participation with a location
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.withLocation(location1).build();
		// Participation with the same location
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.withLocation(location1).build();
		// Participation with an other location
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.withLocation(location2).build();
		// Participation without location
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextLocations(asList(location1, location2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(dc1, dc2, dcOther).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByContextRole() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry course = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("1");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Participation as coach
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		qualityService.createContextBuilder(dc1, participations1.get(0), course, GroupRoles.coach).build();
		// Participation as coach again
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		qualityService.createContextBuilder(dc2, participations2.get(0), course, GroupRoles.coach).build();
		// Participation as owner
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		qualityService.createContextBuilder(dcOther, participationsOther.get(0), course, GroupRoles.owner).build();
		// Data collection without participation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextRoles(asList(QualityContextRole.coach));
		List<QualityDataCollection> collections = sut.loadDataCollection(searchParams);
		
		assertThat(collections)
				.containsExactlyInAnyOrder(dc1, dc2)
				.doesNotContain(dcOther);
	}
	
	@Test
	public void shouldFilterByContextOrganisation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation subOrganisation = organisationService.createOrganisation("Org-21", "", null, organisation1, null);
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addExecutorOrganisation(organisation1).addExecutorOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addExecutorOrganisation(organisation2).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addExecutorOrganisation(subOrganisation).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addExecutorOrganisation(otherOrganisation).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextOrganisationRef(organisation1);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dc1);
	}
	
	@Test
	public void shouldFilterByContextOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation subOrganisation = organisationService.createOrganisation("Org-23", "", null, organisation1, null);
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addExecutorOrganisation(organisation1).addExecutorOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addExecutorOrganisation(organisation2).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addExecutorOrganisation(subOrganisation).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addExecutorOrganisation(otherOrganisation).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextOrganisationRefs(asList(organisation1, organisation2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(organisation1, organisation2, subOrganisation).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByContextCurriculum() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		Curriculum curriculumOther = qualityTestHelper.createCurriculum();
		CurriculumElement elementOther = qualityTestHelper.createCurriculumElement(curriculumOther);
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with other curriculum
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(elementOther).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumRefs(asList(curriculum1, curriculum2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByContextCurriculums() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		Curriculum curriculumOther = qualityTestHelper.createCurriculum();
		CurriculumElement elementOther = qualityTestHelper.createCurriculumElement(curriculumOther);
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with other curriculum
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(elementOther).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumRefs(asList(curriculum1, curriculum2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByContextCurriculumElement() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		CurriculumElement subElement = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherElement = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with a child element
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsChild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationsChild.get(0));
		contextBuilderChild.addCurriculumElement(subElement).build();
		// Participation with other curriculum element
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(otherElement).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcChild, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumElementRef(element1);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dc1);
	}
	
	@Test
	public void shouldFilterByContextCurriculumElements() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		CurriculumElement subElement = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherElement = qualityTestHelper.createCurriculumElement();
		// Participation with curriculum element
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with a child element
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsChild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationsChild.get(0));
		contextBuilderChild.addCurriculumElement(subElement).build();
		// Participation with other curriculum element
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(otherElement).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcChild, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumElementRefs(asList(element1, element2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(element1, element2, subElement).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByContextCurriculumElementTypes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElementType type = curriculumService.createCurriculumElementType("a", "b", null, null);
		CurriculumElementType typeOther = curriculumService.createCurriculumElementType("y", "z", null, null);
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, type, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, type, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		
		CurriculumElement elementNull = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, null, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement elementOther = curriculumService.createCurriculumElement("el", "Element",
				CurriculumElementStatus.active, null, null, null, typeOther, disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
		// Participation with curriculum element of type
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).build();
		// Participation with another curriculum element of type
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element2).build();
		// Participation with other curriculum element  of other type
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(elementOther).build();
		// Participation with curriculum element without type
		QualityDataCollection dcTypeNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsTypeNull = qualityService.addParticipations(dcTypeNull, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderTypeNull = qualityService.createContextBuilder(dcTypeNull, participationsTypeNull.get(0));
		contextBuilderTypeNull.addCurriculumElement(elementNull).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcTypeNull, dcNull, dcOther));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumElementTypeRefs(asList(type));
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections)
				.containsExactlyInAnyOrder(dc1, dc2)
				.doesNotContain(dcTypeNull, dcNull, dcOther);
	}
	
	@Test
	public void shouldFilterByContextCurriculumOrganisation() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum(organisation1);
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum(organisation2);
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		Organisation organisationSub = organisationService.createOrganisation("Org-9", "", null, organisation1, null);
		Curriculum curriculumSub = qualityTestHelper.createCurriculum(organisationSub);
		CurriculumElement elementSub = qualityTestHelper.createCurriculumElement(curriculumSub);
		Organisation organisationOther = qualityTestHelper.createOrganisation();
		Curriculum curriculumOther = qualityTestHelper.createCurriculum(organisationOther);
		CurriculumElement elementOther = qualityTestHelper.createCurriculumElement(curriculumOther);
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).addCurriculumElement(element2).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addCurriculumElement(elementSub).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(elementOther).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumOrganisationRef(organisation1);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dc1);
	}
	
	@Test
	public void shouldFilterByContextCurriculumOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum(organisation1);
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement(curriculum1);
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum(organisation2);
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement(curriculum2);
		Organisation organisationSub = organisationService.createOrganisation("Org-24", "", null, organisation1, null);
		Curriculum curriculumSub = qualityTestHelper.createCurriculum(organisationSub);
		CurriculumElement elementSub = qualityTestHelper.createCurriculumElement(curriculumSub);
		Organisation organisationOther = qualityTestHelper.createOrganisation();
		Curriculum curriculumOther = qualityTestHelper.createCurriculum(organisationOther);
		CurriculumElement elementOther = qualityTestHelper.createCurriculumElement(curriculumOther);
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculumElement(element1).addCurriculumElement(element2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculumElement(element1).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addCurriculumElement(elementSub).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculumElement(elementOther).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumOrganisationRefs(asList(organisation1, organisation2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(organisation1, organisation2, organisationSub).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByContextTaxonomyLevel() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Taxonomy taxonomy = qualityTestHelper.createTaxonomy();
		TaxonomyLevel taxonomyLevel1 = qualityTestHelper.createTaxonomyLevel(taxonomy);
		TaxonomyLevel taxonomyLevel2 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel subTaxonomyLevel = qualityTestHelper.createTaxonomyLevel(taxonomyLevel1);
		TaxonomyLevel otherTaxonomyLevel = qualityTestHelper.createTaxonomyLevel();
		// Participation with two taxonomyLevels
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addTaxonomyLevel(taxonomyLevel1).addTaxonomyLevel(taxonomyLevel2).build();
		// Participation with the same taxonomyLevel
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addTaxonomyLevel(taxonomyLevel2).build();
		// Participation in a child taxonomyLevel (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addTaxonomyLevel(subTaxonomyLevel).build();
		// Participation with an other taxonomyLevel
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addTaxonomyLevel(otherTaxonomyLevel).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextTaxonomyLevelRef(taxonomyLevel1);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dc1);
	}
	
	@Test
	public void shouldFilterByContextTaxonomyLevels() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Taxonomy taxonomy = qualityTestHelper.createTaxonomy();
		TaxonomyLevel taxonomyLevel1 = qualityTestHelper.createTaxonomyLevel(taxonomy);
		TaxonomyLevel taxonomyLevel2 = qualityTestHelper.createTaxonomyLevel();
		TaxonomyLevel subTaxonomyLevel = qualityTestHelper.createTaxonomyLevel(taxonomyLevel1);
		TaxonomyLevel otherTaxonomyLevel = qualityTestHelper.createTaxonomyLevel();
		// Participation with two taxonomyLevels
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addTaxonomyLevel(taxonomyLevel1).addTaxonomyLevel(taxonomyLevel2).build();
		// Participation with the same taxonomyLevel
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addTaxonomyLevel(taxonomyLevel2).build();
		// Participation in a child taxonomyLevel (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addTaxonomyLevel(subTaxonomyLevel).build();
		// Participation with an other taxonomyLevel
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addTaxonomyLevel(otherTaxonomyLevel).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextTaxonomyLevelRefs(asList(taxonomyLevel1, taxonomyLevel2));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(taxonomyLevel1, taxonomyLevel2, subTaxonomyLevel).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByWithUserInfosOnly() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst1");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst2");
		Identity executorOther = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst3");
		// First session with informations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor1));
		EvaluationFormParticipation participation1 = participations1.get(0);
		qualityService.createContextBuilder(dc1, participation1).build();
		EvaluationFormSession session1 = evaManager.createSession(participation1);
		evaManager.updateSession(session1, null, "firstname", "lastname", "age", null, null, null);
		evaManager.finishSession(session1);
		// Second session with informations
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor2));
		EvaluationFormParticipation participation2 = participations2.get(0);
		qualityService.createContextBuilder(dc2, participation2).build();
		EvaluationFormSession session2 = evaManager.createSession(participation2);
		evaManager.updateSession(session2, null, null, null, null, null, null, "math");
		evaManager.finishSession(session2);
		// Session without informations
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executorOther));
		EvaluationFormParticipation participationOther = participationsOther.get(0);
		qualityService.createContextBuilder(dcOther, participationOther).build();
		EvaluationFormSession sessionOther = evaManager.createSession(participationOther);
		evaManager.finishSession(sessionOther);
		finish(asList(dc1, dc2, dcOther));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setWithUserInfosOnly(true);
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(executor1, executor2).size();
		assertThat(count).isEqualTo(expected);
		
		searchParams.setWithUserInfosOnly(false);
		Long countUnfiltered = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expectedUnfiltered = asList(executor1, executor2, executorOther).size();
		assertThat(countUnfiltered).isEqualTo(expectedUnfiltered);
	}
	
	@Test
	public void shouldFilterBySerieNumber() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = organisationService.createOrganisation("Org-8", "", null, null, null);
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), dc1, null, null);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), dc2, null, null);
		QualityDataCollection dc4 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		QualityDataCollection dc5 = qualityService.createDataCollection(asList(dcOrganisation), dc4, null, null);
		QualityDataCollection dc6 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dc4, dc5, dc6));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setSeriesIndexes(asList(1, 3));
		Long count = sut.loadAnalyticFigures(searchParams).getDataCollectionCount();
		
		long expected = asList(dc1, dc3, dc4, dc6).size();
		assertThat(count).isEqualTo(expected);
	}

	@Test
	public void shouldFilterByTopicIdentityNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc.setTopicIdentity(executor);
		dc = qualityService.updateDataCollection(dc);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicIdentityNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByTopicOrganisationNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc.setTopicOrganisation(dcOrganisation);
		dc = qualityService.updateDataCollection(dc);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicOrganisationNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByTopicCurriculumNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc.setTopicCurriculum(curriculum);
		dc = qualityService.updateDataCollection(dc);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicCurriculumNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByTopicCurriculumElementNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc.setTopicCurriculumElement(curriculumElement);
		dc = qualityService.updateDataCollection(dc);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicCurriculumElementNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByTopicRepositoryEntryNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		RepositoryEntry re = qualityTestHelper.createRepositoryEntry();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc.setTopicRepositoryEntry(re);
		dc = qualityService.updateDataCollection(dc);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setTopicRepositoryNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByContextOrganisationNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		// Remove user from all organisations. This test / filter is somewhat
		// superfluous because a user is always in an organisation
		organisationService.removeMember(executor, OrganisationRoles.user);
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).addExecutorOrganisation(dcOrganisation).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextOrganisationNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByContextCurriculumNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).addCurriculumElement(curriculumElement).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByContextCurriculumElementNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).addCurriculumElement(curriculumElement).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumElementNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByContextCurriculumOraganisationNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		CurriculumElement curriculumElement = qualityTestHelper.createCurriculumElement();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).addCurriculumElement(curriculumElement).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextCurriculumOrganisationNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	@Test
	public void shouldFilterByContextTaxonomyLevelNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		TaxonomyLevel taxonomyLevel = qualityTestHelper.createTaxonomyLevel();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).addTaxonomyLevel(taxonomyLevel).build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextTaxonomyLevelNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	
	@Test
	public void shouldFilterByContextLocationNull() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("Analyst");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		// Data collection ok
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0)).withLocation("somewhere").build();
		// Data collection with null value
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNull = qualityService.addParticipations(dcNull, singletonList(executor));
		qualityService.createContextBuilder(dcNull, participationsNull.get(0)).build();
		finish(asList(dc, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setContextLocationNull(true);
		List<QualityDataCollection> dataCollections = sut.loadDataCollection(searchParams);
		
		assertThat(dataCollections).hasSize(1);
		assertThat(dataCollections.get(0)).isEqualTo(dcNull);
	}
	
	private void finish(Collection<QualityDataCollection> dataCollections) {
		for (QualityDataCollection dataCollection: dataCollections) {
			qualityTestHelper.updateStatus(dataCollection, QualityDataCollectionStatus.FINISHED);
		}
	}
	
	private static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

}
