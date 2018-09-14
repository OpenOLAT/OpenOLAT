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
import static org.olat.modules.quality.analysis.GroupBy.TOPIC_ORGANISATION;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.manager.QualityTestHelper;
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
	public void shouldLoadDataCollectionCount() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Identity executor3 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = organisationService.createOrganisation("", "", null, null, null);
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
		Long count = sut.loadDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(3);
	}
	
	@Test
	public void shouldLoadDistinctSessions() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor1 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Identity executor2 = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		// Data collection with two participations and finished sessions
		QualityDataCollection dc = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc, asList(executor1, executor2));
		// First participation
		EvaluationFormParticipation participation1 = participations1.get(0);
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc, participation1);
		contextBuilder1.addCurriculum(curriculum1).addCurriculum(curriculum2).build();
		EvaluationFormSession session1 = evaManager.createSession(participation1);
		session1 = evaManager.finishSession(session1);
		// Second participation
		EvaluationFormParticipation participation2 = participations1.get(0);
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc, participation2);
		contextBuilder2.addCurriculum(curriculum2).addCurriculum(curriculum2).build();
		EvaluationFormSession session2 = evaManager.createSession(participation2);
		evaManager.finishSession(session2);
		// Participation with two curriculums and no session
		QualityDataCollection dcNoSession = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsNoSession = qualityService.addParticipations(dcNoSession, Collections.singletonList(executor1));
		EvaluationFormParticipation participationNoSession = participationsNoSession.get(0);
		QualityContextBuilder contextBuilderNoSession = qualityService.createContextBuilder(dcNoSession, participationNoSession);
		contextBuilderNoSession.addCurriculum(curriculum1).addCurriculum(curriculum2).build();
		finish(asList(dc));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadSessionKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(session1.getKey(), session2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctTopicOrganisationKeys() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicOrganisation(organisation1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc2);
		dc2.setTopicOrganisation(organisation1);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc3);
		dc2.setTopicOrganisation(organisation2);
		QualityDataCollection dcNoOrganisation = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoOrganisation));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicOrganisationKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(organisation1.getKey(), organisation2.getKey());
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
		qualityService.updateDataCollection(dc2);
		dc2.setTopicCurriculum(curriculum1);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc3);
		dc2.setTopicCurriculum(curriculum2);
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
		qualityService.updateDataCollection(dc2);
		dc2.setTopicCurriculumElement(curriculumElement1);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc3);
		dc2.setTopicCurriculumElement(curriculumElement2);
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
		Identity itentity1 = JunitTestHelper.createAndPersistIdentityAsUser("i1");;
		Identity itentity2 = JunitTestHelper.createAndPersistIdentityAsUser("i2");
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dc1.setTopicIdentity(itentity1);
		qualityService.updateDataCollection(dc1);
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc2);
		dc2.setTopicIdentity(itentity1);
		QualityDataCollection dc3 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.updateDataCollection(dc3);
		dc2.setTopicIdentity(itentity2);
		QualityDataCollection dcNoIdentity = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		finish(asList(dc1, dc2, dc3, dcNoIdentity));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Long> keys = sut.loadTopicIdentityKeys(searchParams);
		
		assertThat(keys)
				.doesNotContainNull()
				.containsExactlyInAnyOrder(itentity1.getKey(), itentity2.getKey());
	}
	
	@Test
	public void shouldLoadDistinctContextOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addOrganisation(organisation1).addOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addOrganisation(organisation2).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		List<Organisation> filtered = sut.loadContextOrganisations(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(organisation1, organisation2)
				.doesNotContainNull();
	}
	
	@Test
	public void shouldLoadDistinctContextOrganisationPathes() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addOrganisation(organisation1).addOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addOrganisation(organisation2).build();
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
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculum(curriculum1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculum(curriculum2).build();
		// Second participation with curriculum (to test distinct)
		QualityDataCollection dcDistinct = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsDistinct = qualityService.addParticipations(dcDistinct, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderDistinct = qualityService.createContextBuilder(dcDistinct, participationsDistinct.get(0));
		contextBuilderDistinct.addCurriculum(curriculum1).build();
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
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
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
		List<GroupedStatistic> statisticList = sut.loadGroupedStatisticByResponseIdentifiers(searchParams,
				asList(identifier1, identifier2), TOPIC_ORGANISATION);
		GroupedStatistics statistics = new GroupedStatistics(statisticList);
		
		GroupedStatistic statistic11 = statistics.getStatistic(identifier1, organisation1.getKey());
		assertThat(statistic11.getCount()).isEqualTo(2);
		assertThat(statistic11.getAvg()).isEqualTo(10);
		GroupedStatistic statistic12 = statistics.getStatistic(identifier1, organisation2.getKey());
		assertThat(statistic12.getCount()).isEqualTo(2);
		assertThat(statistic12.getAvg()).isEqualTo(5);
		GroupedStatistic statistic21 = statistics.getStatistic(identifier2, organisation1.getKey());
		assertThat(statistic21.getCount()).isEqualTo(1);
		assertThat(statistic21.getAvg()).isEqualTo(1);
		GroupedStatistic statistic22 = statistics.getStatistic(identifier2, organisation2.getKey());
		assertThat(statistic22).isNull();
	}
	
	@Test
	public void shouldLoadGroupedStatisticForEveryGroupBy() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = organisationService.createOrganisation("", "", null, null, null);
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
			sut.loadGroupedStatisticByResponseIdentifiers(searchParams, singletonList(identifier), groupBy);
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
		Long count = sut.loadDataCollectionCount(searchParams);
		
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
		Long count = sut.loadDataCollectionCount(searchParams);
		
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
		Long count = sut.loadDataCollectionCount(searchParams);
		
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
		QualityDataCollection dcToEarly = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		dcToEarly.setDeadline(addDays(now, 2));
		qualityService.updateDataCollection(dcToEarly);
		finish(asList(dc1, dc2, dcToEarly));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setDateRangeTo(now);
		Long count = sut.loadDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByCurriculums() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum1 = qualityTestHelper.createCurriculum();
		Curriculum curriculum2 = qualityTestHelper.createCurriculum();
		Curriculum otherCurriculum = qualityTestHelper.createCurriculum();
		// Participation with curriculum
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addCurriculum(curriculum1).build();
		// Participation with another curriculum
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addCurriculum(curriculum2).build();
		// Participation with other curriculum
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addCurriculum(otherCurriculum).build();
		// Participation without curriculum
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setCurriculumRefs(asList(curriculum1, curriculum2));
		Long count = sut.loadDataCollectionCount(searchParams);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldFilterByCurriculumElements() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Curriculum curriculum = qualityTestHelper.createCurriculum();
		CurriculumElement element1 = qualityTestHelper.createCurriculumElement();
		CurriculumElement element2 = qualityTestHelper.createCurriculumElement();
		CurriculumElement subElement = curriculumService.createCurriculumElement("", "", null, null, element1, null, curriculum);
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
		searchParams.setCurriculumElementRefs(asList(element1, element2));
		Long count = sut.loadDataCollectionCount(searchParams);
		
		long expected = asList(element1, element2, subElement).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldFilterByOrganisations() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsUser("");
		Organisation dcOrganisation = qualityTestHelper.createOrganisation();
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation subOrganisation = organisationService.createOrganisation("", "", null, organisation1, null);
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		// Participation with two organisations
		QualityDataCollection dc1 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations1 = qualityService.addParticipations(dc1, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder1 = qualityService.createContextBuilder(dc1, participations1.get(0));
		contextBuilder1.addOrganisation(organisation1).addOrganisation(organisation2).build();
		// Participation with the same organisation
		QualityDataCollection dc2 = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc2, Collections.singletonList(executor));
		QualityContextBuilder contextBuilder2 = qualityService.createContextBuilder(dc2, participations2.get(0));
		contextBuilder2.addOrganisation(organisation2).build();
		// Participation in a child organisation (include them)
		QualityDataCollection dcChild = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationscild = qualityService.addParticipations(dcChild, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderChild = qualityService.createContextBuilder(dcChild, participationscild.get(0));
		contextBuilderChild.addOrganisation(subOrganisation).build();
		// Participation with an other organisation
		QualityDataCollection dcOther = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		List<EvaluationFormParticipation> participationsOther = qualityService.addParticipations(dcOther, Collections.singletonList(executor));
		QualityContextBuilder contextBuilderOther = qualityService.createContextBuilder(dcOther, participationsOther.get(0));
		contextBuilderOther.addOrganisation(otherOrganisation).build();
		// Participation without organisation
		QualityDataCollection dcNull = qualityService.createDataCollection(asList(dcOrganisation), formEntry);
		qualityService.addParticipations(dcNull, Collections.singletonList(executor));
		finish(asList(dc1, dc2, dcOther, dcChild, dcNull));
		dbInstance.commitAndCloseSession();
		
		AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
		searchParams.setOrganisationRefs(asList(organisation1, organisation2));
		Long count = sut.loadDataCollectionCount(searchParams);
		
		long expected = asList(organisation1, organisation2, subOrganisation).size();
		assertThat(count).isEqualTo(expected);
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
