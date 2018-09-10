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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.modules.forms.model.jpa.SurveyFilter;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyFilterTest extends OlatTestCase {
	
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
		EvaluationFormSession unfinishedSession = evaTestHelper.createSession(survey);
		EvaluationFormSession otherSession = evaTestHelper.createSession(otherSurvey);
		evaManager.finishSession(otherSession);
		dbInstance.commitAndCloseSession();
		
		SurveyFilter filter = new SurveyFilter(survey);
		List<EvaluationFormSession> filtered = evaManager.loadSessionsFiltered(filter, 0, -1);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(session1, session2)
				.doesNotContain(unfinishedSession, otherSession);
	}

}
