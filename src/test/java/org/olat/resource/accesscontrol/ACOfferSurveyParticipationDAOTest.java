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
package org.olat.resource.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.createRandomResource;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACOfferSurveyDAO;
import org.olat.resource.accesscontrol.manager.ACOfferSurveyParticipationDAO;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ACOfferSurveyParticipationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACFrontendManager acFrontendManager;
	@Autowired
	private ACOrderDAO acOrderManager;
	@Autowired
	private ACOfferSurveyDAO offerSurveyDao;
	@Autowired
	private EvaluationFormTestsHelper evaluationFormTestsHelper;

	@Autowired
	private ACOfferSurveyParticipationDAO sut;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	private Order createOrder(Identity delivery) {
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		Order order = acOrderManager.createOrder(delivery);
		OrderPart part = acOrderManager.addOrderPart(order);
		acOrderManager.addOrderLine(part, offer);
		return order;
	}

	private EvaluationFormSurvey createSurvey() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		return offerSurveyDao.createSurvey(resource, formEntry, "Step 1");
	}

	@Test
	public void shouldCreateParticipation() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();

		EvaluationFormParticipation participation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		assertThat(participation).isNotNull();
		assertThat(participation.getExecutor()).isEqualTo(executor);
		assertThat(participation.getStatus()).isEqualTo(EvaluationFormParticipationStatus.prepared);
	}

	@Test
	public void shouldReuseActiveParticipationForSameSurveyAndOrder() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation firstParticipation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		EvaluationFormParticipation secondParticipation = sut.createParticipation(survey, order, executor);

		assertThat(secondParticipation).isEqualTo(firstParticipation);
	}

	@Test
	public void shouldAllowNewParticipationAfterCancelling() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation firstParticipation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		sut.cancelParticipation(firstParticipation);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation secondParticipation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		assertThat(secondParticipation).isNotNull();
		assertThat(secondParticipation.getRun()).isEqualTo(2);
		assertThat(secondParticipation.isLastRun()).isTrue();
		List<EvaluationFormParticipation> participations = sut.loadParticipations(survey, order);
		assertThat(participations).hasSize(2);
		assertThat(participations).extracting(EvaluationFormParticipation::getStatus)
				.containsExactlyInAnyOrder(EvaluationFormParticipationStatus.canceled, EvaluationFormParticipationStatus.prepared);
		assertThat(participations).filteredOn(p -> p.getKey().equals(firstParticipation.getKey())).first()
				.extracting(EvaluationFormParticipation::isLastRun).isEqualTo(false);
	}

	@Test
	public void shouldAllowAllParticipationsOfAPairToBeCanceled() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation participation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		sut.cancelParticipation(participation);
		dbInstance.commitAndCloseSession();

		assertThat(sut.hasActiveParticipation(survey, order)).isFalse();
	}

	@Test
	public void shouldOnlyLoadParticipationsOfTheGivenOrder() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order1 = createOrder(executor);
		Order order2 = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation participation1 = sut.createParticipation(survey, order1, executor);
		sut.createParticipation(survey, order2, executor);
		dbInstance.commitAndCloseSession();

		List<EvaluationFormParticipation> participations = sut.loadParticipations(survey, order1);

		assertThat(participations).containsExactly(participation1);
	}

	@Test
	public void shouldCancelSessionWhenParticipationIsCanceled() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation participation = sut.createParticipation(survey, order, executor);
		EvaluationFormSession session = evaluationFormManager.createSession(participation);
		evaluationFormManager.finishSession(session);
		dbInstance.commitAndCloseSession();

		sut.cancelParticipation(participation);
		dbInstance.commitAndCloseSession();

		EvaluationFormSession canceledSession = evaluationFormManager.loadSessionByParticipation(participation);
		assertThat(canceledSession.getEvaluationFormSessionStatus()).isEqualTo(EvaluationFormSessionStatus.canceled);
	}

	@Test
	public void shouldPersistLastRunFalseAfterCancelParticipation() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation participation = sut.createParticipation(survey, order, executor);
		dbInstance.commitAndCloseSession();

		sut.cancelParticipation(participation);
		dbInstance.commitAndCloseSession();

		EvaluationFormParticipation reloadedParticipation = evaluationFormManager.loadParticipationByKey(participation);
		assertThat(reloadedParticipation.getStatus()).isEqualTo(EvaluationFormParticipationStatus.canceled);
		assertThat(reloadedParticipation.isLastRun()).isFalse();
	}

	@Test
	public void shouldExcludeCanceledSessionFromExport() {
		EvaluationFormSurvey survey = createSurvey();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Order order = createOrder(executor);
		dbInstance.commitAndCloseSession();
		EvaluationFormParticipation participation = sut.createParticipation(survey, order, executor);
		EvaluationFormSession session = evaluationFormManager.createSession(participation);
		evaluationFormManager.finishSession(session);
		dbInstance.commitAndCloseSession();

		sut.cancelParticipation(participation);
		dbInstance.commitAndCloseSession();

		List<EvaluationFormSession> doneSessions = evaluationFormManager
				.loadSessionsFiltered(SessionFilterFactory.createSelectDone(survey), 0, -1);
		assertThat(doneSessions).isEmpty();
	}

}
