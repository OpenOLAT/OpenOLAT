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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormResponseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@Autowired
	private EvaluationFormResponseDAO sut;

	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldUpdateResponse() {
		EvaluationFormSession session = evaTestHelper.createSession();
		EvaluationFormResponse response = createResponse(session);
		dbInstance.commitAndCloseSession();
		
		BigDecimal numericalValue = new BigDecimal("3.3");
		String stringuifiedResponse = numericalValue.toPlainString();
		Path fileResponse = Paths.get("a", "b", "a", "c");
		EvaluationFormResponse updatedResponse = sut.updateResponse(numericalValue, stringuifiedResponse, fileResponse, response);
		
		assertThat(updatedResponse.getNumericalResponse()).isEqualTo(numericalValue);
		assertThat(updatedResponse.getStringuifiedResponse()).isEqualTo(stringuifiedResponse);
		assertThat(updatedResponse.getFileResponse()).isEqualTo(fileResponse);
		assertThat(updatedResponse.isNoResponse()).isFalse();
	}

	@Test
	public void shouldCreateNoResponse() {
		EvaluationFormSession session = evaTestHelper.createSession();
		String responseIdentifier = UUID.randomUUID().toString();
		EvaluationFormResponse response = sut.createNoResponse(responseIdentifier, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(response.getCreationDate()).isNotNull();
		assertThat(response.getLastModified()).isNotNull();
		assertThat(response.getResponseIdentifier()).isEqualTo(responseIdentifier);
		assertThat(response.getSession()).isEqualTo(session);
		assertThat(response.isNoResponse()).isTrue();
		assertThat(response.getNumericalResponse()).isNull();
		assertThat(response.getStringuifiedResponse()).isNull();
		assertThat(response.getFileResponse()).isNull();
	}
	
	@Test
	public void shouldUpdateNoResponse() {
		EvaluationFormSession session = evaTestHelper.createSession();
		EvaluationFormResponse initialResponse = createResponse(session);
		dbInstance.commitAndCloseSession();

		EvaluationFormResponse response = sut.updateNoResponse(initialResponse);

		assertThat(response.isNoResponse()).isTrue();
		assertThat(response.getNumericalResponse()).isNull();
		assertThat(response.getStringuifiedResponse()).isNull();
		assertThat(response.getFileResponse()).isNull();
	}

	@Test
	public void shouldLoadResponsesBySurvey() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession surveySession1 = evaTestHelper.createSession(survey);
		EvaluationFormResponse response11 = evaTestHelper.createResponse(surveySession1);
		EvaluationFormResponse response12 = evaTestHelper.createResponse(surveySession1);
		EvaluationFormSession surveySession2 = evaTestHelper.createSession(survey);
		EvaluationFormResponse response21 = evaTestHelper.createResponse(surveySession2);
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		EvaluationFormResponse otherResponse = evaTestHelper.createResponse(otherSession);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormResponse> loadedResponses = sut.loadResponsesBySurvey(survey);
		
		assertThat(loadedResponses)
				.contains(response11, response12, response21)
				.doesNotContain(otherResponse);
	}
	
	@Test
	public void shouldLoadResponsesByParticipations() { 
		EvaluationFormParticipation participation1 = evaTestHelper.createParticipation();
		EvaluationFormSession session1 = evaTestHelper.createSession(participation1);
		EvaluationFormResponse response11 = evaTestHelper.createResponse(session1);
		EvaluationFormResponse response12 = evaTestHelper.createResponse(session1);
		EvaluationFormParticipation participation2 = evaTestHelper.createParticipation();
		EvaluationFormSession session2 = evaTestHelper.createSession(participation2);
		EvaluationFormResponse response21 = evaTestHelper.createResponse(session2);
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		EvaluationFormResponse otherResponse = evaTestHelper.createResponse(otherSession);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormParticipation> participations = Arrays.asList(participation1, participation2);
		List<EvaluationFormResponse> loadedResponses = sut.loadResponsesByParticipations(participations);
		
		assertThat(loadedResponses)
				.contains(response11, response12, response21)
				.doesNotContain(otherResponse);
	}
	
	@Test
	public void shouldLoadResponsesBySessions() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session1 = evaTestHelper.createSession(survey);
		EvaluationFormResponse response11 = evaTestHelper.createResponse(session1);
		EvaluationFormResponse response12 = evaTestHelper.createResponse(session1);
		EvaluationFormSession session2 = evaTestHelper.createSession(survey);
		EvaluationFormResponse response21 = evaTestHelper.createResponse(session2);
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		EvaluationFormResponse otherResponse = evaTestHelper.createResponse(otherSession);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<EvaluationFormResponse> loadedResponses = sut.loadResponsesBySessions(filter);
		
		assertThat(loadedResponses)
				.contains(response11, response12, response21)
				.doesNotContain(otherResponse);
	}
	
	@Test
	public void shouldDeleteResponses() {
		EvaluationFormSession session = evaTestHelper.createSession();
		String responseIdentifier = UUID.randomUUID().toString();
		createResponse(session, responseIdentifier);
		createResponse(session, responseIdentifier);
		createResponse(session, responseIdentifier);
		dbInstance.commitAndCloseSession();

		SessionFilter filter = SessionFilterFactory.create(session);
		List<EvaluationFormResponse> responses = sut.loadResponsesBySessions(filter);
		assertThat(responses).hasSize(3);
		
		List<Long> keys = responses.stream().map(EvaluationFormResponse::getKey).collect(Collectors.toList());
		sut.deleteResponses(keys);
		dbInstance.commitAndCloseSession();
		
		responses = sut.loadResponsesBySessions(filter);
		assertThat(responses).hasSize(0);
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_ok() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		evaluationFormManager.finishSession(session);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, "text", null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(survey, List.of(responseIdentifier))).isTrue();
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_no_string() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		evaluationFormManager.finishSession(session);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, null, null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(survey, List.of(responseIdentifier))).isFalse();
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_empty_string() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		evaluationFormManager.finishSession(session);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, "", null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(survey, List.of(responseIdentifier))).isFalse();
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_other_idenitifer() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		evaluationFormManager.finishSession(session);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, "text", null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(survey, List.of(random()))).isFalse();
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_session_not_finished() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, "text", null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(survey, List.of(responseIdentifier))).isFalse();
	}
	
	@Test
	public void shouldCheckIsStringuifiedAvailable_other_survey() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSurvey otherSurvey = evaTestHelper.createSurvey();
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		evaluationFormManager.finishSession(session);
		String responseIdentifier = random();
		sut.createResponse(responseIdentifier, null, "text", null, session);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.isStringuifiedAvailable(otherSurvey, List.of(responseIdentifier))).isFalse();
	}

	private EvaluationFormResponse createResponse(EvaluationFormSession session) {
		String responseIdentifier = UUID.randomUUID().toString();
		return createResponse(session, responseIdentifier);
	}

	private EvaluationFormResponse createResponse(EvaluationFormSession session, String responseIdentifier) {
		BigDecimal numericalValue = new BigDecimal("2.2");
		String stringuifiedResponse = numericalValue.toPlainString();
		Path fileResponse = Paths.get("this", "is", "a", "path");
		return sut.createResponse(responseIdentifier, numericalValue, stringuifiedResponse,
				fileResponse, session);
	}

}
