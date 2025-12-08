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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.forms.EvaluationFormDispatcher.PUBLIC_PARTICIPATION_TYPE;
import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;
import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;
import static org.olat.test.JunitTestHelper.random;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormEmailExecutor;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.model.QualityParticipationStats;
import org.olat.modules.quality.ui.ExecutorParticipationDataModel.ExecutorParticipationCols;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityParticipationDAOTest extends OlatTestCase {
	
	private static final TranslatorMock TRANSLATOR = new TranslatorMock();

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private EvaluationFormManager evaManager;
	
	@Autowired
	private QualityParticipationDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldLoadParticipations() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		int numberOfParticipations = 3;
		for (int i = 0; i < numberOfParticipations; i++) {
			Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
			EvaluationFormParticipation participation = qualityTestHelper.createParticipation(survey, identity);
			qualityTestHelper.createContext(dataCollection, participation);
		}
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection, null, null);
		
		assertThat(participations).hasSize(numberOfParticipations);
		QualityParticipation participation = participations.get(0);
		assertThat(participation.getParticipationRef()).isNotNull();
		assertThat(participation.getFirstname()).isNotNull();
		assertThat(participation.getLastname()).isNotNull();
		assertThat(participation.getEmail()).isNotNull();
		assertThat(participation.getContextRef()).isNotNull();
		assertThat(participation.getRole()).isNotNull();
		assertThat(participation.getAudienceRepositoryEntryName()).isNotNull();
		assertThat(participation.getAudienceCurriculumElementName()).isNotNull();
	}
	
	@Test
	public void shouldLoadParticipations_Email() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		EvaluationFormEmailExecutor emailExecutor = new EvaluationFormEmailExecutor(random(), random(), random());
		EvaluationFormParticipation evaParticipation = evaManager.createParticipation(survey, emailExecutor);
		qualityTestHelper.createContext(dataCollection, evaParticipation);
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection, null, null);
		
		QualityParticipation participation = participations.get(0);
		assertThat(participation.getParticipationRef()).isNotNull();
		assertThat(participation.getFirstname()).isEqualTo(emailExecutor.firstName());
		assertThat(participation.getLastname()).isEqualTo(emailExecutor.lastName());
		assertThat(participation.getEmail()).isEqualTo(emailExecutor.email());
		assertThat(participation.getContextRef()).isNotNull();
		assertThat(participation.getRole()).isNotNull();
		assertThat(participation.getAudienceRepositoryEntryName()).isNotNull();
		assertThat(participation.getAudienceCurriculumElementName()).isNotNull();
	}
	
	@Test
	public void shouldLoadParticipations_filterIdentifierTypes() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		EvaluationFormEmailExecutor emailExecutor = new EvaluationFormEmailExecutor(random(), random(), random());
		EvaluationFormParticipation participationEmail = evaManager.createParticipation(survey, emailExecutor);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		EvaluationFormParticipation participation = evaManager.createParticipation(survey, executor);
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(random(), random());
		EvaluationFormParticipation participation2 = evaManager.createParticipation(survey, identifier);
		
		qualityTestHelper.createContext(dataCollection, participationEmail);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadParticipations(dataCollection, null, null))
				.extracting(QualityParticipation::getParticipationRef)
				.extracting(EvaluationFormParticipationRef::getKey)
				.containsExactlyInAnyOrder(participationEmail.getKey(), participation.getKey(), participation2.getKey());
		
		assertThat(sut.loadParticipations(dataCollection, List.of(identifier.getType()), null))
				.extracting(QualityParticipation::getParticipationRef)
				.extracting(EvaluationFormParticipationRef::getKey)
				.containsExactlyInAnyOrder(participation2.getKey());
		
		assertThat(sut.loadParticipations(dataCollection, null, List.of(identifier.getType())))
				.extracting(QualityParticipation::getParticipationRef)
				.extracting(EvaluationFormParticipationRef::getKey)
				.containsExactlyInAnyOrder(participationEmail.getKey(), participation.getKey());
	}
	
	@Test
	public void shouldGetParticipationStats() {
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection1);
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey2 = qualityTestHelper.createSurvey(dataCollection2);
		QualityDataCollection dataCollectionOther = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey surveyOther = qualityTestHelper.createSurvey(dataCollectionOther);
		
		// Executor
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection1, Arrays.asList(identity, otherIdentity));
		qualityTestHelper.addParticipations(dataCollection2, Arrays.asList(identity));
		qualityTestHelper.addParticipations(dataCollectionOther, Arrays.asList(identity));
		
		// E-Mail
		evaManager.createParticipation(survey, new EvaluationFormEmailExecutor(random(), random(), random()));
		evaManager.createParticipation(survey, new EvaluationFormEmailExecutor(random(), random(), random()));
		evaManager.createParticipation(survey, new EvaluationFormEmailExecutor(random(), random(), random()));
		evaManager.createParticipation(survey2, new EvaluationFormEmailExecutor(random(), random(), random()));
		evaManager.createParticipation(surveyOther, new EvaluationFormEmailExecutor(random(), random(), random()));
		
		// Public (count only done)
		EvaluationFormParticipation participation1 = evaManager.createParticipation(survey, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession session1 = evaManager.createSession(participation1);
		evaManager.finishSession(session1);
		EvaluationFormParticipation participation2 = evaManager.createParticipation(survey, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession session2 = evaManager.createSession(participation2);
		evaManager.finishSession(session2);
		EvaluationFormParticipation participation3 = evaManager.createParticipation(survey, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession session3 = evaManager.createSession(participation3);
		evaManager.finishSession(session3);
		EvaluationFormParticipation participation4 = evaManager.createParticipation(survey, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession session4 = evaManager.createSession(participation4);
		evaManager.finishSession(session4);
		EvaluationFormParticipation participation5 = evaManager.createParticipation(survey2, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession session5 = evaManager.createSession(participation5);
		evaManager.finishSession(session5);
		EvaluationFormParticipation participation6 = evaManager.createParticipation(survey, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		evaManager.createSession(participation6); // not finished
		EvaluationFormParticipation participationOther = evaManager.createParticipation(surveyOther, new EvaluationFormParticipationIdentifier(PUBLIC_PARTICIPATION_TYPE, random()));
		EvaluationFormSession sessionOther = evaManager.createSession(participationOther);
		evaManager.finishSession(sessionOther);
		dbInstance.commitAndCloseSession();
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setDataCollections(List.of(dataCollection1, dataCollection2));
		searchParams.setDataCollectionStatus(asList(FINISHED, PREPARATION, READY, RUNNING));
		QualityParticipationStats stats = sut.getExecutorParticipationStats(searchParams);
		
		assertThat(stats.numExecutor()).isEqualTo(3);
		assertThat(stats.numEmail()).isEqualTo(4);
		assertThat(stats.numPublic()).isEqualTo(5);
		assertThat(stats.total()).isEqualTo(12);
		
		stats = sut.getExecutorParticipationStats(null);
		assertThat(stats.numExecutor()).isEqualTo(0);
		assertThat(stats.numEmail()).isEqualTo(0);
		assertThat(stats.numPublic()).isEqualTo(0);
		assertThat(stats.total()).isEqualTo(0);
		
		searchParams.setDataCollectionKeys(List.of(-1l));
		stats = sut.getExecutorParticipationStats(searchParams);
		assertThat(stats.numExecutor()).isEqualTo(0);
		assertThat(stats.numEmail()).isEqualTo(0);
		assertThat(stats.numPublic()).isEqualTo(0);
		assertThat(stats.total()).isEqualTo(0);
	}
	
	@Test
	public void shouldLoadExecutorParticipations() {
		QualityDataCollection previous = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection(previous);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0, -1);
		
		QualityExecutorParticipation participation = participations.get(0);
		assertThat(participation.getParticipationRef()).isNotNull();
		assertThat(participation.getParticipationIdentifier().getType()).isNotNull();
		assertThat(participation.getParticipationIdentifier().getKey()).isNotNull();
		assertThat(participation.getExecutionStatus()).isNotNull();
		assertThat(participation.getStart()).isNotNull();
		assertThat(participation.getDeadline()).isNotNull();
		assertThat(participation.getTitle()).isNotNull();
		assertThat(participation.getTopicType()).isNotNull();
		assertThat(participation.getTranslatedTopicType()).isNotNull();
		assertThat(participation.getTopic()).isNotNull();
		assertThat(participation.getPreviousTitle()).isNotNull();
	}
	
	@Test
	public void shouldLoadExecutorParticipationsPaged() {
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection("Z");
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection("A");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection1, Arrays.asList(identity));
		qualityTestHelper.addParticipations(dataCollection2, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(identity);
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 1, 1);
		
		assertThat(participations).hasSize(1);
	}
	
	@Test
	public void shouldLoadExecutorParticipationsOrdered() {
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection("Z");
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection("A");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection1, Arrays.asList(identity));
		qualityTestHelper.addParticipations(dataCollection2, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();
		
		SortKey sortKey = new SortKey(ExecutorParticipationCols.title.name(), true);
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(identity);
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0, -1, sortKey);
		
		assertThat(participations.get(0).getTitle()).isEqualTo("A");
		assertThat(participations.get(1).getTitle()).isEqualTo("Z");
	}
	
	@Test
	public void shouldLoadExecutorParticipationsOrderedByAllColumns() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();
		
		List<ExecutorParticipationCols> excludedCols = Arrays.asList(ExecutorParticipationCols.execute);
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		for (ExecutorParticipationCols col: ExecutorParticipationCols.values()) {
			if (!excludedCols.contains(col)) {
				SortKey sortKey = new SortKey(col.name(), true);
				sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0, -1, sortKey);
			}
		}
		
		// Only check that no Exception is thrown to be sure that hql syntax is ok.
	}
	
	@Test
	public void shouldFilterExecutorParticipationsByExecutor() {
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection1,
				Arrays.asList(identity, otherIdentity));
		qualityTestHelper.addParticipations(dataCollection2, Arrays.asList(identity));
		EvaluationFormSurvey otherSurvey = qualityTestHelper.createRandomSurvey();
		qualityTestHelper.createParticipation(otherSurvey, identity);
		dbInstance.commitAndCloseSession();

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(identity);
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0,
				-1);

		assertThat(participations).hasSize(2);
	}
	
	@Test
	public void shouldFilterExecutorParticipationsByDataCollection() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		QualityDataCollection otherDataCollection = qualityTestHelper.createDataCollection();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		List<EvaluationFormParticipation> createdParticipations = qualityTestHelper.addParticipations(dataCollection,
				Arrays.asList(identity, otherIdentity));
		qualityTestHelper.addParticipations(otherDataCollection, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setDataCollections(List.of(dataCollection));
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0,
				-1);

		assertThat(participations).hasSize(2);
		List<Long> loadedKeys = participations.stream().map(QualityExecutorParticipation::getParticipationRef)
				.map(EvaluationFormParticipationRef::getKey).collect(toList());
		Long[] expectedKeys = createdParticipations.stream().map(EvaluationFormParticipation::getKey)
				.toArray(Long[]::new);
		assertThat(loadedKeys).containsExactlyInAnyOrder(expectedKeys);
	}
	
	@Test
	public void shouldFilterExecutorParticipationsByEvaluationFormParticipation() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurveyRef survey = evaManager.loadSurvey(of(dataCollection));
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection, Arrays.asList(identity1, identity2, identity3));
		EvaluationFormParticipation participation = evaManager.loadParticipationByExecutor(survey, identity1);
		dbInstance.commitAndCloseSession();

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setParticipationRef(participation);
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0,
				-1);

		assertThat(participations.get(0).getParticipationRef().getKey()).isEqualTo(participation.getKey());
	}
	
	@Test
	public void shouldFilterExecutorParticipationsByParticipationStatus() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurveyRef survey = evaManager.loadSurvey(of(dataCollection));
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection, Arrays.asList(identity1, identity2, identity3));
		EvaluationFormParticipation participation = evaManager.loadParticipationByExecutor(survey, identity1);
		EvaluationFormSession session = evaManager.createSession(participation);
		evaManager.finishSession(session);
		dbInstance.commitAndCloseSession();

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setParticipationStatus(EvaluationFormParticipationStatus.prepared);
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0,
				-1);

		assertThat(participations).hasSize(2);
	}
	
	@Test
	public void shouldFilterExecutorParticipationsByDataCollectionStatus() {
		QualityDataCollection ready = qualityTestHelper.createDataCollection();
		ready = qualityTestHelper.updateStatus(ready, READY);
		QualityDataCollection running = qualityTestHelper.createDataCollection();
		running = qualityTestHelper.updateStatus(running, RUNNING);
		QualityDataCollection finnished = qualityTestHelper.createDataCollection();
		finnished = qualityTestHelper.updateStatus(finnished, FINISHED);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(ready, Arrays.asList(identity));
		qualityTestHelper.addParticipations(running, Arrays.asList(identity));
		qualityTestHelper.addParticipations(finnished, Arrays.asList(identity));
		dbInstance.commitAndCloseSession();

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setDataCollectionStatus(Arrays.asList(READY, RUNNING));
		List<QualityExecutorParticipation> participations = sut.loadExecutorParticipations(TRANSLATOR, searchParams, 0,
				-1);

		assertThat(participations).hasSize(2);
	}
}
