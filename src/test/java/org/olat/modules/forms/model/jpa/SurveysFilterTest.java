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
package org.olat.modules.forms.model.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveysFilterTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private EvaluationFormManager evaManager;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldFilterBySurvey() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSurvey otherSurvey = evaTestHelper.createSurvey();
		EvaluationFormSession session1 = evaTestHelper.createSession(survey);
		evaManager.finishSession(session1);
		EvaluationFormSession session2 = evaTestHelper.createSession(survey);
		evaManager.finishSession(session2);
		EvaluationFormSession sessionOtherSurvey = evaTestHelper.createSession(otherSurvey);
		evaManager.finishSession(sessionOtherSurvey);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(Collections.singletonList(survey));
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(session1, session2)
				.doesNotContain(sessionOtherSurvey);
	}
	
	@Test
	public void shouldFilterBySurveys() {
		EvaluationFormSurvey survey1 = evaTestHelper.createSurvey();
		EvaluationFormSurvey survey2 = evaTestHelper.createSurvey();
		EvaluationFormSurvey otherSurvey = evaTestHelper.createSurvey();
		EvaluationFormSession session11 = evaTestHelper.createSession(survey1);
		evaManager.finishSession(session11);
		EvaluationFormSession session22 = evaTestHelper.createSession(survey1);
		evaManager.finishSession(session22);
		EvaluationFormSession session21 = evaTestHelper.createSession(survey2);
		evaManager.finishSession(session21);
		EvaluationFormSession otherSession = evaTestHelper.createSession(otherSurvey);
		evaManager.finishSession(otherSession);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(Arrays.asList(survey1, survey2));
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(session11, session22, session21)
				.doesNotContain(otherSession);
	}
	
	@Test
	public void shouldFilterByIdentitfierOres() {
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		OLATResourceable oresOther = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		String subIdent2a = UUID.randomUUID().toString();
		
		EvaluationFormSurvey survey = evaManager.createSurvey(of(ores), formEntry);
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		EvaluationFormSurvey surveySubident = evaManager.createSurvey(of(ores, subIdent), formEntry);
		EvaluationFormSession sessionSubident = evaTestHelper.createSession(surveySubident);
		EvaluationFormSurvey surveySubident2 = evaManager.createSurvey(of(ores, subIdent, subIdent2), formEntry);
		EvaluationFormSession sessionSubident2 = evaTestHelper.createSession(surveySubident2);
		EvaluationFormSurvey surveySubident2a = evaManager.createSurvey(of(ores, subIdent, subIdent2a), formEntry);
		EvaluationFormSession sessionSubident2a = evaTestHelper.createSession(surveySubident2a);
		EvaluationFormSurvey otherSurveySubident2 = evaManager.createSurvey(of(oresOther, subIdent, subIdent2), formEntry);
		EvaluationFormSession otherSessionSubident2 = evaTestHelper.createSession(otherSurveySubident2);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(of(ores));
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(
						session,
						sessionSubident,
						sessionSubident2,
						sessionSubident2a)
				.doesNotContain(
						otherSessionSubident2);
	}
	
	@Test
	public void shouldFilterByIdentitfierSubident() {
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		OLATResourceable oresOther = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		String subIdent2a = UUID.randomUUID().toString();
		
		EvaluationFormSurvey survey = evaManager.createSurvey(of(ores), formEntry);
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		EvaluationFormSurvey surveySubident = evaManager.createSurvey(of(ores, subIdent), formEntry);
		EvaluationFormSession sessionSubident = evaTestHelper.createSession(surveySubident);
		EvaluationFormSurvey surveySubident2 = evaManager.createSurvey(of(ores, subIdent, subIdent2), formEntry);
		EvaluationFormSession sessionSubident2 = evaTestHelper.createSession(surveySubident2);
		EvaluationFormSurvey surveySubident2a = evaManager.createSurvey(of(ores, subIdent, subIdent2a), formEntry);
		EvaluationFormSession sessionSubident2a = evaTestHelper.createSession(surveySubident2a);
		EvaluationFormSurvey otherSurveySubident2 = evaManager.createSurvey(of(oresOther, subIdent, subIdent2), formEntry);
		EvaluationFormSession otherSessionSubident2 = evaTestHelper.createSession(otherSurveySubident2);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(of(ores, subIdent));
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(
						sessionSubident,
						sessionSubident2,
						sessionSubident2a)
				.doesNotContain(
						session,
						otherSessionSubident2);
	}
	
	@Test
	public void shouldFilterByIdentitfierSubident2() {
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		OLATResourceable oresOther = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		String subIdent2a = UUID.randomUUID().toString();
		
		EvaluationFormSurvey survey = evaManager.createSurvey(of(ores), formEntry);
		EvaluationFormSession session = evaTestHelper.createSession(survey);
		EvaluationFormSurvey surveySubident = evaManager.createSurvey(of(ores, subIdent), formEntry);
		EvaluationFormSession sessionSubident = evaTestHelper.createSession(surveySubident);
		EvaluationFormSurvey surveySubident2 = evaManager.createSurvey(of(ores, subIdent, subIdent2), formEntry);
		EvaluationFormSession sessionSubident2 = evaTestHelper.createSession(surveySubident2);
		EvaluationFormSurvey surveySubident2a = evaManager.createSurvey(of(ores, subIdent, subIdent2a), formEntry);
		EvaluationFormSession sessionSubident2a = evaTestHelper.createSession(surveySubident2a);
		EvaluationFormSurvey otherSurveySubident2 = evaManager.createSurvey(of(oresOther, subIdent, subIdent2), formEntry);
		EvaluationFormSession otherSessionSubident2 = evaTestHelper.createSession(otherSurveySubident2);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(of(ores, subIdent, subIdent2));
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(
						sessionSubident2)
				.doesNotContain(
						session,
						sessionSubident,
						sessionSubident2a,
						otherSessionSubident2);
	}
	
	@Test
	public void shouldFilterByStatus() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSession sessionFinished1 = evaTestHelper.createSession(survey);
		evaManager.finishSession(sessionFinished1);
		EvaluationFormSession sessionFinished2 = evaTestHelper.createSession(survey);
		evaManager.finishSession(sessionFinished2);
		EvaluationFormSession sessionUnfinished = evaTestHelper.createSession(survey);
		dbInstance.commitAndCloseSession();
		
		SurveysFilter filter = new SurveysFilter(Collections.singletonList(survey), EvaluationFormSessionStatus.done, true);
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(sessionFinished1, sessionFinished2)
				.doesNotContain(sessionUnfinished);
	}
}
