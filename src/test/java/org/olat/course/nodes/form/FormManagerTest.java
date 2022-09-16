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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.modules.assessment.ParticipantType;
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
	public void shouldGetFormParticipationsFilterByParticipants() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.createRandomRepositoryEntry(owner);
		Identity memberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity memberNotParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity notMemberParticipated = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity fakeParticipant = JunitTestHelper.createAndPersistIdentityAsUser(random());
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
		EvaluationFormParticipation participation2 = evaluationFormManager.createParticipation(survey, notMemberParticipated);
		EvaluationFormSession session2 = evaluationFormManager.createSession(participation2);
		evaluationFormManager.finishSession(session2);
		dbInstance.commitAndCloseSession();
		
		// Fake participant has participated
		EvaluationFormParticipation participation3 = evaluationFormManager.createParticipation(survey, fakeParticipant);
		EvaluationFormSession session3 = evaluationFormManager.createSession(participation3);
		evaluationFormManager.finishSession(session3);
		dbInstance.commitAndCloseSession();
		
		FormParticipationSearchParams searchParams = new FormParticipationSearchParams();
		searchParams.setCourseEntry(courseEntry);
		searchParams.setFakeParticipants(Set.of(fakeParticipant));
		
		// Owner all
		searchParams.setIdentity(owner);
		searchParams.setAdmin(true);
		searchParams.setCoach(false);
		List<FormParticipation> formParticipations = sut.getFormParticipations(survey, searchParams);
		
		assertThat(formParticipations).extracting(FormParticipation::getIdentity)
				.containsExactlyInAnyOrder(
						memberParticipated,
						memberNotParticipated,
						notMemberParticipated,
						fakeParticipant);
		
		FormParticipation memberParticipation = getFormParticipation(formParticipations, memberParticipated);
			assertThat(memberParticipation.getEvaluationFormParticipationRef())
					.as("Participating member has EvaluationFormParticipationRef").isNotNull();
			assertThat(memberParticipation.getParticipationStatus())
					.as("Participating member has status done").isEqualTo(EvaluationFormParticipationStatus.done);
			assertThat(memberParticipation.getSubmissionDate())
					.as("Participating member has submission date").isNotNull();
		
		FormParticipation memberNotParticipatedParticipation = getFormParticipation(formParticipations, memberNotParticipated);
			assertThat(memberNotParticipatedParticipation.getEvaluationFormParticipationRef())
					.as("Not participating member has no EvaluationFormParticipationRef").isNull();
			assertThat(memberNotParticipatedParticipation.getParticipationStatus())
					.as("Not participating has no status").isNull();
			assertThat(memberNotParticipatedParticipation.getSubmissionDate())
					.as("Not participating has no submission date").isNull();
		
		FormParticipation notMemberParticipation = getFormParticipation(formParticipations, notMemberParticipated);
			assertThat(notMemberParticipation.getEvaluationFormParticipationRef())
					.as("Participating not member has EvaluationFormParticipationRef").isNotNull();
			assertThat(notMemberParticipation.getParticipationStatus())
					.as("Participating not member has status done").isEqualTo(EvaluationFormParticipationStatus.done);
			assertThat(notMemberParticipation.getSubmissionDate())
					.as("Participating not member has submission date").isNotNull();
		
		// Owner, filtered by members
		searchParams.setIdentity(owner);
		searchParams.setAdmin(true);
		searchParams.setCoach(false);
		searchParams.setParticipants(Set.of(ParticipantType.member));
		formParticipations = sut.getFormParticipations(survey, searchParams);
		
		assertThat(formParticipations).extracting(FormParticipation::getIdentity)
				.containsExactlyInAnyOrder(
						memberParticipated,
						memberNotParticipated);
		
		// Owner, filtered by non members
		searchParams.setIdentity(owner);
		searchParams.setAdmin(true);
		searchParams.setCoach(false);
		searchParams.setParticipants(Set.of(ParticipantType.nonMember));
		formParticipations = sut.getFormParticipations(survey, searchParams);
		
		assertThat(formParticipations).extracting(FormParticipation::getIdentity)
				.containsExactlyInAnyOrder(
						notMemberParticipated);
						
		// Owner, filtered by fake participant
		searchParams.setIdentity(owner);
		searchParams.setAdmin(true);
		searchParams.setCoach(false);
		searchParams.setParticipants(Set.of(ParticipantType.fakeParticipant));
		formParticipations = sut.getFormParticipations(survey, searchParams);
		
		assertThat(formParticipations).extracting(FormParticipation::getIdentity)
				.containsExactlyInAnyOrder(
						fakeParticipant);
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
	
}
