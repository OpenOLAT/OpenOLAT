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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.Paging;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.jpa.CalculatedDouble;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormReportDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@Autowired
	private EvaluationFormReportDAO sut;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldReturnResponses() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		String responseIdentifier2 = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		String stringResponse11 = UUID.randomUUID().toString();
		String stringResponse12 = UUID.randomUUID().toString();
		String stringResponse21 = UUID.randomUUID().toString();
		String otherIdentifierResponse = UUID.randomUUID().toString();
		String otherSessionResponse = UUID.randomUUID().toString();
		
		EvaluationFormResponse response111 = evaluationFormManager.createStringResponse(responseIdentifier1, session1, stringResponse11);
		EvaluationFormResponse response112 = evaluationFormManager.createStringResponse(responseIdentifier1, session1, stringResponse12);
		EvaluationFormResponse response121 = evaluationFormManager.createStringResponse(responseIdentifier2, session1, stringResponse11);
		EvaluationFormResponse response221 = evaluationFormManager.createStringResponse(responseIdentifier1, session2, stringResponse21);
		EvaluationFormResponse responseOtherIdentifier = evaluationFormManager.createStringResponse(otherIdentifier, session1, otherIdentifierResponse);
		EvaluationFormResponse responseOtherSession = evaluationFormManager.createStringResponse(responseIdentifier1, otherSession, otherSessionResponse);
		EvaluationFormResponse noResponse = evaluationFormManager.createNoResponse(responseIdentifier1, session1);
		dbInstance.commit();
		
		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1, responseIdentifier2);
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<EvaluationFormResponse> responses = sut.getResponses(responseIdentifiers, filter, Paging.all());
		
		assertThat(responses)
				.containsExactlyInAnyOrder(response111, response112, response121, response221)
				.doesNotContain(responseOtherIdentifier, responseOtherSession, noResponse);
	}
	
	@Test
	public void shouldReturnResponsesCount() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		String responseIdentifier2 = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		String stringResponse11 = UUID.randomUUID().toString();
		String stringResponse12 = UUID.randomUUID().toString();
		String stringResponse21 = UUID.randomUUID().toString();
		String otherIdentifierResponse = UUID.randomUUID().toString();
		String otherSessionResponse = UUID.randomUUID().toString();
		
		EvaluationFormResponse response111 = evaluationFormManager.createStringResponse(responseIdentifier1, session1, stringResponse11);
		EvaluationFormResponse response112 = evaluationFormManager.createStringResponse(responseIdentifier1, session1, stringResponse12);
		EvaluationFormResponse response121 = evaluationFormManager.createStringResponse(responseIdentifier2, session1, stringResponse11);
		EvaluationFormResponse response221 = evaluationFormManager.createStringResponse(responseIdentifier1, session2, stringResponse21);
		evaluationFormManager.createStringResponse(otherIdentifier, session1, otherIdentifierResponse);
		evaluationFormManager.createStringResponse(responseIdentifier1, otherSession, otherSessionResponse);
		evaluationFormManager.createNoResponse(responseIdentifier1, session1);
		dbInstance.commit();
		
		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1, responseIdentifier2);
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		Long count = sut.getResponsesCount(responseIdentifiers, filter, Paging.all());
		
		long expected = Arrays.asList(response111, response112, response121, response221).size();
		assertThat(count).isEqualTo(expected);
	}
	
	@Test
	public void shouldReturnResponsesPaged() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession session3 = evaTestHelper.createSession();
		String stringResponse11 = UUID.randomUUID().toString();
		String stringResponse21 = UUID.randomUUID().toString();
		String otherSessionResponse = UUID.randomUUID().toString();
		
		evaluationFormManager.createStringResponse(responseIdentifier1, session1, stringResponse11);
		evaluationFormManager.createStringResponse(responseIdentifier1, session2, stringResponse21);
		evaluationFormManager.createStringResponse(responseIdentifier1, session3, otherSessionResponse);
		evaluationFormManager.createNoResponse(responseIdentifier1, session1);
		dbInstance.commit();
		
		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1);
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		int max = 1;
		List<EvaluationFormResponse> responses = sut.getResponses(responseIdentifiers, filter, Paging.max(max));
		
		assertThat(responses).hasSize(max);
	}

	@Test
	public void shouldGetCountBySinguifiedResponse() {
		String responseIdentifier = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession session3 = evaTestHelper.createSession();
		EvaluationFormSession session4 = evaTestHelper.createSession();
		EvaluationFormSession session5 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		String choice1 = UUID.randomUUID().toString();
		String choice2 = UUID.randomUUID().toString();
		
		evaluationFormManager.createNoResponse(responseIdentifier, session1);
		evaluationFormManager.createStringResponse(responseIdentifier, session1, choice1);
		evaluationFormManager.finishSession(session1);
		evaluationFormManager.createStringResponse(responseIdentifier, session2, choice1);
		evaluationFormManager.finishSession(session2);
		evaluationFormManager.createStringResponse(responseIdentifier, session3, choice1);
		evaluationFormManager.finishSession(session3);
		evaluationFormManager.createStringResponse(responseIdentifier, session4, choice2);
		evaluationFormManager.finishSession(session4);
		// unfinished session counts as well
		evaluationFormManager.createStringResponse(responseIdentifier, session5, choice2);
		evaluationFormManager.createStringResponse(responseIdentifier, otherSession, choice2);
		evaluationFormManager.createStringResponse(otherIdentifier, session5, choice2);
		dbInstance.commit();
		
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2, session3, session4, session5);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<CalculatedLong> counts = sut.getCountByStringuifideResponse(responseIdentifier, filter);
		
		assertThat(counts).hasSize(2);
		Map<String, Long> identToValue = counts.stream()
				.collect(Collectors.toMap(CalculatedLong::getIdentifier, CalculatedLong::getValue));
		assertThat(identToValue.get(choice1)).isEqualTo(3);
		assertThat(identToValue.get(choice2)).isEqualTo(2);
	}
	
	@Test
	public void shouldGetCountByIdentifiersAndNumResponse() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		String responseIdentifier2 = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession session3 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		BigDecimal numberThreeTimes = BigDecimal.valueOf(1);
		BigDecimal numberOnce = BigDecimal.valueOf(2);
		
		evaluationFormManager.createNumericalResponse(responseIdentifier1, otherSession, numberThreeTimes);
		evaluationFormManager.createNoResponse(responseIdentifier1, session1);
		evaluationFormManager.createNumericalResponse(otherIdentifier, session1, numberOnce);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session1, numberThreeTimes);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session1, numberOnce);
		evaluationFormManager.createNumericalResponse(responseIdentifier2, session1, numberOnce);
		evaluationFormManager.finishSession(session1);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session2, numberThreeTimes);
		evaluationFormManager.finishSession(session2);
		// unfinished session counts as well
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session3, numberThreeTimes);
		dbInstance.commit();
		
		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1, responseIdentifier2);
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2, session3);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<CalculatedLong> counts = sut.getCountByIdentifiersAndNumerical(responseIdentifiers, filter);
		
		assertThat(counts).hasSize(3);
		assertThat(getValue(counts, responseIdentifier1, numberThreeTimes.toPlainString())).isEqualTo(3);
		assertThat(getValue(counts, responseIdentifier1, numberOnce.toPlainString())).isEqualTo(1);
		assertThat(getValue(counts, responseIdentifier2, numberOnce.toPlainString())).isEqualTo(1);
	}
	
	private Long getValue(List<CalculatedLong> calculatedLongs, String identifier, String subidentifier) {
		for (CalculatedLong calculatedLong: calculatedLongs) {
			if (calculatedLong.getIdentifier().equals(identifier) && calculatedLong.getSubIdentifier().equals(subidentifier)) {
				return calculatedLong.getValue();
			}
		}
		return null;
	}
	
	@Test
	public void shouldGetCountNoResponsesByIdentifier() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		String responseIdentifier2 = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession session3 = evaTestHelper.createSession();
		EvaluationFormSession session4 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		String choice1 = UUID.randomUUID().toString();
		
		evaluationFormManager.createNoResponse(responseIdentifier1, session1);
		evaluationFormManager.createNoResponse(responseIdentifier2, session1);
		evaluationFormManager.finishSession(session1);
		evaluationFormManager.createNoResponse(responseIdentifier1, session2);
		evaluationFormManager.finishSession(session2);
		evaluationFormManager.createNoResponse(responseIdentifier1, session3);
		evaluationFormManager.createStringResponse(responseIdentifier2, session3, choice1);
		evaluationFormManager.finishSession(session3);
		// unfinished session counts as well
		evaluationFormManager.createStringResponse(responseIdentifier1, session4, choice1);
		evaluationFormManager.createNoResponse(responseIdentifier1, otherSession);
		evaluationFormManager.createNoResponse(otherIdentifier, session1);
		dbInstance.commit();

		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1, responseIdentifier2);
		List<EvaluationFormSession> sessions = Arrays.asList(session1, session2, session3, session4);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<CalculatedLong> counts = sut.getCountNoResponsesByIdentifiers(responseIdentifiers, filter);
		
		assertThat(counts).hasSize(2);
		Map<String, Long> identToValue = counts.stream()
				.collect(Collectors.toMap(CalculatedLong::getIdentifier, CalculatedLong::getValue));
		assertThat(identToValue.get(responseIdentifier1)).isEqualTo(3);
		assertThat(identToValue.get(responseIdentifier2)).isEqualTo(1);
	}

	
	@Test
	public void shouldGetAvgByResponseIdentifiery() {
		String responseIdentifier1 = UUID.randomUUID().toString();
		String responseIdentifier2 = UUID.randomUUID().toString();
		String otherIdentifier = UUID.randomUUID().toString();
		String identifierNoResponse = UUID.randomUUID().toString();
		EvaluationFormSession session1 = evaTestHelper.createSession();
		EvaluationFormSession session2 = evaTestHelper.createSession();
		EvaluationFormSession session3 = evaTestHelper.createSession();
		EvaluationFormSession session4 = evaTestHelper.createSession();
		EvaluationFormSession session5 = evaTestHelper.createSession();
		EvaluationFormSession otherSession = evaTestHelper.createSession();
		BigDecimal ten = BigDecimal.TEN;
		BigDecimal zero = BigDecimal.ZERO;
		
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session1, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier2, session1, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session2, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session3, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session4, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, session5, zero);
		evaluationFormManager.createNumericalResponse(otherIdentifier, session1, ten);
		evaluationFormManager.createNumericalResponse(responseIdentifier1, otherSession, ten);
		evaluationFormManager.createNoResponse(identifierNoResponse, session1);
		dbInstance.commit();
		
		List<String> responseIdentifiers = Arrays.asList(responseIdentifier1, responseIdentifier2, identifierNoResponse);
		List<? extends EvaluationFormSessionRef> sessions = Arrays.asList(session1, session2, session3, session4, session5);
		SessionFilter filter = SessionFilterFactory.create(sessions);
		List<CalculatedDouble> avg = sut.getAvgByResponseIdentifiers(responseIdentifiers, filter);

		assertThat(avg).hasSize(2);
		Map<String, Double> identToValue = avg.stream()
				.collect(Collectors.toMap(CalculatedDouble::getIdentifier, CalculatedDouble::getValue));
		assertThat(identToValue.get(responseIdentifier1)).isEqualTo(Double.valueOf(8));
		assertThat(identToValue.get(responseIdentifier2)).isEqualTo(Double.valueOf(10));
	}
}
