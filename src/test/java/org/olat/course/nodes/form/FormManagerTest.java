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
package org.olat.course.nodes.form;

import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
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
 * Initial date: 17 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private FormManager sut;
	
	@Test
	public void shouldGetFormParticipationsOfOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner);
		Identity memberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity memberNotParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity notMemeberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity notMemeberNotParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		dbInstance.commitAndCloseSession();
		
		repositoryService.addRole(owner, courseEntry, GroupRoles.owner.name());
		repositoryService.addRole(memberParticipated, courseEntry, GroupRoles.participant.name());
		repositoryService.addRole(memberNotParticipated, courseEntry, GroupRoles.participant.name());
		
		CourseNode courseNode = new FormCourseNode();
		EvaluationFormSurveyIdentifier surveyIdent = sut.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = sut.createSurvey(surveyIdent, courseEntry);
		dbInstance.commitAndCloseSession();
		
		// Member has participated
		EvaluationFormParticipation participation1 = evaluationFormManager.createParticipation(survey, memberParticipated);
		EvaluationFormSession session1 = evaluationFormManager.createSession(participation1);
		evaluationFormManager.finishSession(session1);
		
		// Non member has participated
		EvaluationFormParticipation participation2 = evaluationFormManager.createParticipation(survey, notMemeberParticipated);
		EvaluationFormSession session2 = evaluationFormManager.createSession(participation2);
		evaluationFormManager.finishSession(session2);
		dbInstance.commitAndCloseSession();
		
		
		UserCourseEnvironment ownerCourseEnv = createUserCourseEnvironment(courseEntry, owner);
		List<FormParticipation> formParticipations = sut.getFormParticipations(survey, ownerCourseEnv);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(formParticipations).as("Owner should see members and not members").hasSize(3);
		
		FormParticipation memberParticipation = getFormParticipation(formParticipations, memberParticipated);
		softly.assertThat(memberParticipation)
				.as("Participating member has to be present").isNotNull();
		if (memberParticipation != null) {
			softly.assertThat(memberParticipation.getEvaluationFormParticipationRef())
					.as("Participating member has EvaluationFormParticipationRef").isNotNull();
			softly.assertThat(memberParticipation.getParticipationStatus())
					.as("Participating member has status done").isEqualTo(EvaluationFormParticipationStatus.done);
			softly.assertThat(memberParticipation.getSubmissionDate())
					.as("Participating member has submission date").isNotNull();
		}
		
		FormParticipation memberNotParticipatedParticipation = getFormParticipation(formParticipations, memberNotParticipated);
		softly.assertThat(memberNotParticipatedParticipation)
				.as("Participating not member has to be present").isNotNull();
		if (memberNotParticipatedParticipation != null) {
			softly.assertThat(memberNotParticipatedParticipation.getEvaluationFormParticipationRef())
					.as("Not participating member has no EvaluationFormParticipationRef").isNull();
			softly.assertThat(memberNotParticipatedParticipation.getParticipationStatus())
					.as("Not participating has no status").isNull();
			softly.assertThat(memberNotParticipatedParticipation.getSubmissionDate())
					.as("Not participating has no submission date").isNull();
		}
		
		FormParticipation notMemberParticipation = getFormParticipation(formParticipations, notMemeberParticipated);
		softly.assertThat(notMemberParticipation)
				.as("Participating not member has to be present").isNotNull();
		if (notMemberParticipation != null) {
			softly.assertThat(notMemberParticipation.getEvaluationFormParticipationRef())
					.as("Participating not member has EvaluationFormParticipationRef").isNotNull();
			softly.assertThat(notMemberParticipation.getParticipationStatus())
					.as("Participating not member has status done").isEqualTo(EvaluationFormParticipationStatus.done);
			softly.assertThat(notMemberParticipation.getSubmissionDate())
					.as("Participating not member has submission date").isNotNull();
		}
		
		FormParticipation notMemberNotParticipatedParticipation = getFormParticipation(formParticipations, notMemeberNotParticipated);
		softly.assertThat(notMemberNotParticipatedParticipation)
				.as("Not participating not member must not be present").isNull();
		
		softly.assertAll();
	}
	
	@Test
	public void shouldGetFormParticipationsOfCoach() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		Identity memberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity memberNotParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity notMemeberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity notMemeberNotParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		dbInstance.commitAndCloseSession();
		
		repositoryService.addRole(owner, courseEntry, GroupRoles.owner.name());
		repositoryService.addRole(coach, courseEntry, GroupRoles.coach.name());
		repositoryService.addRole(memberParticipated, courseEntry, GroupRoles.participant.name());
		repositoryService.addRole(memberNotParticipated, courseEntry, GroupRoles.participant.name());
		
		CourseNode courseNode = new FormCourseNode();
		EvaluationFormSurveyIdentifier surveyIdent = sut.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = sut.createSurvey(surveyIdent, courseEntry);
		dbInstance.commitAndCloseSession();
		
		// Member has participated
		EvaluationFormParticipation participation1 = evaluationFormManager.createParticipation(survey, memberParticipated);
		EvaluationFormSession session1 = evaluationFormManager.createSession(participation1);
		evaluationFormManager.finishSession(session1);
		
		// Non member has participated
		EvaluationFormParticipation participation2 = evaluationFormManager.createParticipation(survey, notMemeberParticipated);
		EvaluationFormSession session2 = evaluationFormManager.createSession(participation2);
		evaluationFormManager.finishSession(session2);
		dbInstance.commitAndCloseSession();
		
		
		UserCourseEnvironment coachCourseEnv = createUserCourseEnvironment(courseEntry, coach);
		List<FormParticipation> formParticipations = sut.getFormParticipations(survey, coachCourseEnv);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(formParticipations).as("Coach should see members").hasSize(2);
		softly.assertThat(getFormParticipation(formParticipations, memberParticipated))
				.as("Participating member has to be present").isNotNull();
		softly.assertThat(getFormParticipation(formParticipations, memberNotParticipated))
				.as("Participating not member has to be present").isNotNull();
		softly.assertThat(getFormParticipation(formParticipations, notMemeberParticipated))
				.as("Participating not member must not be present").isNull();
		softly.assertThat(getFormParticipation(formParticipations, notMemeberNotParticipated))
				.as("Not participating not member must not be present").isNull();
		softly.assertAll();
	}
	
	private FormParticipation getFormParticipation(List<FormParticipation> formParticipations,
			Identity memberParticipated) {
		for (FormParticipation formParticipation : formParticipations) {
			if (formParticipation.getIdentity().getKey().equals(memberParticipated.getKey())) {
				return formParticipation;
			}
		}
		return null;
	}
	
	private UserCourseEnvironment createUserCourseEnvironment(RepositoryEntry courseEntry, Identity identity) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(identity);
		return new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
	}
	
}
