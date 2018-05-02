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

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormParticipationDAOTest extends OlatTestCase {
	
	private static final String IDENTIFIER_TYPE = "type";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	
	@Autowired
	private EvaluationFormParticipationDAO sut;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateParticipation() {
		boolean anonymous = true;
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		dbInstance.commit();
		
		EvaluationFormParticipation participation = sut.createParticipation(survey, identifier, anonymous, executor);
		
		assertThat(participation).isNotNull();
		assertThat(participation.getCreationDate()).isNotNull();
		assertThat(participation.getLastModified()).isNotNull();
		assertThat(participation.getIdentifier().getType()).isEqualTo(IDENTIFIER_TYPE);
		assertThat(participation.getIdentifier().getKey()).isEqualTo(identifierKey);
		assertThat(participation.isAnonymous()).isEqualTo(anonymous);
		assertThat(participation.getExecutor()).isEqualTo(executor);
		assertThat(participation.getStatus()).isEqualTo(EvaluationFormParticipationStatus.prepared);
	}
	
	@Test
	public void shouldUpdateStatus() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormParticipation participation = sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, null);
		dbInstance.commit();
		
		EvaluationFormParticipationStatus newStatus = EvaluationFormParticipationStatus.done;
		sut.changeStatus(participation, newStatus);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation.getStatus()).isEqualTo(newStatus);
	}

	
	@Test
	public void shouldLoadBySurveyAndExecutor() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipation participation = sut.createParticipation(survey, identifier, false, executor);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByExecutor(survey, executor);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadBySurveyAndIdentifier() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipation participation = sut.createParticipation(survey, identifier, false, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(survey, identifier);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadByIdentifier() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormParticipation participation = sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadByIdentifierAndReturnNuttIfNotUnique() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, null);
		sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation).isNull();
	}

}
