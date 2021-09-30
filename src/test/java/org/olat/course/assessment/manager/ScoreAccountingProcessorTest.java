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
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationMembershipEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreAccountingProcessorTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private ScoreAccountingTriggerDAO scoreAccountingTriggerDAO;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ScoreAccountingProcessor sut;

	@Test
	public void shouldCreateAssessmentEntriesForNewCourseMemebers() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser(random());
		dbInstance.commitAndCloseSession();
		
		Event event = RepositoryEntryMembershipModifiedEvent.roleParticipantAdded(participant, courseEntry);
		sut.event(event);
		dbInstance.commitAndCloseSession();
	
		assertThat(assessmentService.hasAssessmentEntry(participant, courseEntry)).isTrue();
	}
	
	@Test
	public void shouldNotCreateNewAssessmentEntriesForExceptionalObligations() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser(random());
		// Add participant without sending events (and not creating assessment entries)
		repositoryEntryRelationDao.addRole(participant, courseEntry, GroupRoles.participant.name());
		OrganisationRef organisationRef = organisationService.getDefaultOrganisation();
		ScoreAccountingTriggerData data = new ScoreAccountingTriggerData();
		data.setIdentifier(random());
		data.setOrganisationRef(organisationRef);
		scoreAccountingTriggerDAO.create(courseEntry, random(), data);
		dbInstance.commitAndCloseSession();
		
		Event event = OrganisationMembershipEvent.identityAdded(organisationRef, participant);
		sut.event(event);
		dbInstance.commitAndCloseSession();
		
		assertThat(assessmentService.hasAssessmentEntry(participant, courseEntry)).isFalse();
	}

}
