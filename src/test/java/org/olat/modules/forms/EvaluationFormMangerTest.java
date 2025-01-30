/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.forms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;
import static org.olat.test.JunitTestHelper.random;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EvaluationFormMangerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager sut;
	
	@Test
	public void shouldCreate_updateLastRuns() {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		EvaluationFormSurvey survey = sut.createSurvey(of(formEntry, JunitTestHelper.random()), formEntry);
		EvaluationFormParticipation participation1 = sut.createParticipation(survey, executor, false, 1);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadParticipationByKey(participation1).isLastRun()).isTrue();
		
		EvaluationFormParticipation participation2 = sut.createParticipation(survey, executor, false, 2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadParticipationByKey(participation1).isLastRun()).isFalse();
		assertThat(sut.loadParticipationByKey(participation2).isLastRun()).isTrue();
	}


}
