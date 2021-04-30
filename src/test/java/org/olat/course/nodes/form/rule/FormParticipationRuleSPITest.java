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
package org.olat.course.nodes.form.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationRuleSPITest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FormManager formManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private FormParticipationRuleSPI sut;
	
	@Test
	public void shoudGetIndividualsToRemind() {
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Identity participantDone = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participantDone, courseEntry, GroupRoles.participant.name());
		Identity participantInProgress = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participantInProgress, courseEntry, GroupRoles.participant.name());
		Identity participantNoParticipation = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participantNoParticipation, courseEntry, GroupRoles.participant.name());
		Identity participantOtherNode = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participantNoParticipation, courseEntry, GroupRoles.participant.name());
		Identity participantOtherEntry = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participantOtherEntry, courseEntryOther, GroupRoles.participant.name());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(coach, courseEntry, GroupRoles.coach.name());
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(owner, courseEntry, GroupRoles.owner.name());
		
		FormCourseNode courseNode = new FormCourseNode();
		EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.createSurvey(surveyIdentifier, formEntry);
		participate(survey, participantDone);
		formManager.loadOrCreateParticipation(survey, participantInProgress);
		participate(survey, coach);
		participate(survey, owner);
		EvaluationFormSurveyIdentifier surveyIdentifierOtherNode = formManager.getSurveyIdentifier(new FormCourseNode(), courseEntry);
		EvaluationFormSurvey surveyOtherNode = formManager.createSurvey(surveyIdentifierOtherNode, formEntry);
		participate(surveyOtherNode, participantOtherNode);
		EvaluationFormSurveyIdentifier surveyIdentifierOtherEntry = formManager.getSurveyIdentifier(courseNode, courseEntryOther);
		EvaluationFormSurvey surveyOtherEntry = formManager.createSurvey(surveyIdentifierOtherEntry, formEntry);
		participate(surveyOtherEntry, participantOtherEntry);
		dbInstance.commitAndCloseSession();
		
		
		List<Identity> individualsToRemind = sut.getIndividualsToRemind(courseEntry, courseNode);
		
		assertThat(individualsToRemind).containsExactlyInAnyOrder(
					participantInProgress,
					participantNoParticipation
				).doesNotContain(
					participantDone, 
					coach,
					owner,
					participantOtherEntry
				);
	}

	private void participate(EvaluationFormSurvey survey, Identity identity) {
		EvaluationFormParticipation participation = formManager.loadOrCreateParticipation(survey, identity);
		EvaluationFormSession sesssion = formManager.loadOrCreateSession(participation);
		evaluationFormManager.finishSession(sesssion);
	}

}
