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
package org.olat.modules.teams.manager;

import java.util.Date;

import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsAttendee;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsUser;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsAttendeeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsUserDAO teamsUserDao;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsAttendeeDAO teamsAttendeeDao;
	
	@Test
	public void createAttendee() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-1");
		String identifier = UUID.randomUUID().toString();
		String displayName = "Teams attendee 1";
		TeamsUser user = teamsUserDao.createUser(id, identifier, displayName);

		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - attendee - 1";
		String subIdent = UUID.randomUUID().toString();
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, id);
		dbInstance.commitAndCloseSession();
		
		TeamsAttendee attendee = teamsAttendeeDao.createAttendee(id, user, "Role", new Date(), meeting);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(attendee);
		Assert.assertNotNull(attendee.getKey());
		Assert.assertNotNull(attendee.getCreationDate());
		Assert.assertNotNull(attendee.getLastModified());
		Assert.assertNotNull(attendee.getJoinDate());
		Assert.assertEquals(id, attendee.getIdentity());
		Assert.assertEquals(user, attendee.getTeamsUser());
		Assert.assertEquals(meeting, attendee.getMeeting());
	}
	
	@Test
	public void hasAttendee() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-3");
		String identifier = UUID.randomUUID().toString();
		String displayName = "Teams attendee 2";
		TeamsUser user = teamsUserDao.createUser(id, identifier, displayName);

		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - attendee - 2";
		String subIdent = UUID.randomUUID().toString();
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, id);
		dbInstance.commitAndCloseSession();
		
		TeamsAttendee attendee = teamsAttendeeDao.createAttendee(id, user, "Role", new Date(), meeting);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(attendee);
		
		boolean hasAttended = teamsAttendeeDao.hasAttendee(id, meeting);
		Assert.assertTrue(hasAttended);
		boolean hasNotAttended = teamsAttendeeDao.hasAttendee(id2, meeting);
		Assert.assertFalse(hasNotAttended);
	}
	
	@Test
	public void deleteAttendee() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-4");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-5");
		Identity idRef = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-attendee-5");
		TeamsUser user1 = teamsUserDao.createUser(id1, UUID.randomUUID().toString(), "Teams attendee 3");
		TeamsUser user2 = teamsUserDao.createUser(id2, UUID.randomUUID().toString(), "Teams attendee 4");
		TeamsUser userRef = teamsUserDao.createUser(idRef, UUID.randomUUID().toString(), "Teams attendee 5");

		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		TeamsMeeting meeting = teamsMeetingDao.createMeeting("Online-Meeting - attendee - 3", new Date(), new Date(),
				entry, subIdent, null, id1);
		TeamsMeeting meetingRef = teamsMeetingDao.createMeeting("Online-Meeting - attendee - 4", new Date(), new Date(),
				entry, subIdent, null, idRef);

		dbInstance.commitAndCloseSession();
		
		TeamsAttendee attendee1 = teamsAttendeeDao.createAttendee(id1, user1, "Role", new Date(), meeting);
		TeamsAttendee attendee2 = teamsAttendeeDao.createAttendee(id2, user2, "Role", new Date(), meeting);
		TeamsAttendee attendeeRef = teamsAttendeeDao.createAttendee(idRef, userRef, "Role", new Date(), meetingRef);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(attendee1);
		Assert.assertNotNull(attendee2);
		Assert.assertNotNull(attendeeRef);

		boolean hasAttended = teamsAttendeeDao.hasAttendee(id1, meeting);
		Assert.assertTrue(hasAttended);
		
		teamsAttendeeDao.deleteMeetingsAttendees(meeting);
		dbInstance.commitAndCloseSession();
		
		
		boolean stillAttended = teamsAttendeeDao.hasAttendee(idRef, meetingRef);
		Assert.assertTrue(stillAttended);
	}

}
