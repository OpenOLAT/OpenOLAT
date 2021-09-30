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
import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;
import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.ui.ExecutorParticipationDataModel.ExecutorParticipationCols;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
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
	public void shouldGetParticipationCount() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		int numberOfParticipations = 3;
		for (int i = 0; i < numberOfParticipations; i++) {
			EvaluationFormParticipation participation = qualityTestHelper.createParticipation(survey);
			qualityTestHelper.createContext(dataCollection, participation);
		}
		dbInstance.commitAndCloseSession();
		
		int count = sut.getParticipationCount(dataCollection);
		
		assertThat(count).isEqualTo(numberOfParticipations);
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
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection, 0, -1);
		
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
	public void shouldLoadParticipationsPaged() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		int numberOfParticipations = 3;
		for (int i = 0; i < numberOfParticipations; i++) {
			EvaluationFormParticipation participation = qualityTestHelper.createParticipation(survey);
			qualityTestHelper.createContext(dataCollection, participation);
		}
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection, 1, 1);
		
		assertThat(participations).hasSize(1);
	}
	
	@Test
	public void shouldLoadParticipationsOrdered() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		Identity identityZ = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		User userZ = identityZ.getUser();
		userZ.setProperty(UserConstants.LASTNAME, "Z");
		UserManager.getInstance().updateUser(identityZ, userZ);
		EvaluationFormParticipation participationZ = qualityTestHelper.createParticipation(survey, identityZ);
		qualityTestHelper.createContext(dataCollection, participationZ);
		Identity identityA = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		User userA = identityA.getUser();
		userA.setProperty(UserConstants.LASTNAME, "A");
		UserManager.getInstance().updateUser(identityA, userA);
		EvaluationFormParticipation participationA = qualityTestHelper.createParticipation(survey, identityA);
		qualityTestHelper.createContext(dataCollection, participationA);
		dbInstance.commitAndCloseSession();
		
		SortKey sortKey = new SortKey(ParticipationCols.lastname.name(), true);
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection, 0, -1, sortKey);
		
		assertThat(participations.get(0).getLastname()).isEqualTo("A");
		assertThat(participations.get(1).getLastname()).isEqualTo("Z");
	}
	
	@Test
	public void shouldLoadParticipationsOrderedByAllColumns() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormSurvey survey = qualityTestHelper.createSurvey(dataCollection);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.createParticipation(survey, identity);
		dbInstance.commitAndCloseSession();
		
		List<ParticipationCols> excludedCols = Arrays.asList();
		for (ParticipationCols col: ParticipationCols.values()) {
			if (!excludedCols.contains(col)) {
				SortKey sortKey = new SortKey(col.name(), true);
				sut.loadParticipations(dataCollection, 0, -1, sortKey);
			}
		}
		
		// Only check that no Exception is thrown to be sure that hql syntax is ok.
	}
	
	@Test
	public void shouldGetExecutorParticipationCount() {
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("quality-");
		qualityTestHelper.addParticipations(dataCollection1, Arrays.asList(identity, otherIdentity));
		qualityTestHelper.addParticipations(dataCollection2, Arrays.asList(identity));
		EvaluationFormSurvey otherSurvey = qualityTestHelper.createRandomSurvey();
		qualityTestHelper.createParticipation(otherSurvey, identity);
		dbInstance.commitAndCloseSession();
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(identity);
		searchParams.setDataCollectionStatus(asList(FINISHED, PREPARATION, READY, RUNNING));
		Long count = sut.getExecutorParticipationCount(searchParams);
		
		assertThat(count).isEqualTo(2);
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
		searchParams.setDataCollectionRef(dataCollection);
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
