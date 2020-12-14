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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 23 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	
	@Test
	public void createMeeting() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 1";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(meeting);
		Assert.assertNotNull(meeting.getKey());
		Assert.assertNotNull(meeting.getCreationDate());
		Assert.assertNotNull(meeting.getLastModified());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertNotNull(meeting.getEndDate());
		Assert.assertEquals(0l, meeting.getLeadTime());
		Assert.assertEquals(0l, meeting.getFollowupTime());
		
		Assert.assertEquals(name, meeting.getSubject());
		Assert.assertEquals(creator, meeting.getCreator());
		Assert.assertEquals(entry, meeting.getEntry());
		Assert.assertEquals(subIdent, meeting.getSubIdent());
		Assert.assertNull(meeting.getBusinessGroup());
		
		Assert.assertEquals(AccessLevel.SAME_ENTERPRISE_AND_FEDERATED.name(), meeting.getAccessLevel());
		Assert.assertEquals(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name(), meeting.getAllowedPresenters());
		Assert.assertTrue(meeting.isEntryExitAnnouncement());
		Assert.assertEquals(LobbyBypassScope.ORGANIZATION_AND_FEDERATED.name(), meeting.getLobbyBypassScope());
	}
	
	@Test
	public void loadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 2";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-2");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting);
		
		TeamsMeeting reloadedMeeting = teamsMeetingDao.loadByKey(meeting.getKey());

		Assert.assertNotNull(reloadedMeeting);
		Assert.assertEquals(meeting, reloadedMeeting);
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertNotNull(meeting.getEndDate());
		
		Assert.assertEquals(name, meeting.getSubject());
		Assert.assertEquals(creator, meeting.getCreator());
		Assert.assertEquals(entry, meeting.getEntry());
		Assert.assertEquals(subIdent, meeting.getSubIdent());
		Assert.assertNull(meeting.getBusinessGroup());
	}
	
	@Test
	public void getMeetingByIdentifier() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 6";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-3");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting);
		
		TeamsMeeting reloadedMeeting = teamsMeetingDao.loadByIdentifier(meeting.getIdentifier());

		Assert.assertNotNull(reloadedMeeting);
		Assert.assertEquals(meeting, reloadedMeeting);
	}
	
	@Test
	public void isIdentifierInUse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 6";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-4");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting);
		
		boolean inUse = teamsMeetingDao.isIdentifierInUse(meeting.getIdentifier(), null);
		Assert.assertTrue(inUse);
		boolean iUseIt = teamsMeetingDao.isIdentifierInUse(meeting.getIdentifier(), meeting);
		Assert.assertFalse(iUseIt);
		String randomIdentifier = UUID.randomUUID().toString();
		boolean neverUsed = teamsMeetingDao.isIdentifierInUse(randomIdentifier, meeting);
		Assert.assertFalse(neverUsed);
	}
	
	@Test
	public void getAllMeetings() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 7";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-4");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		
		List<TeamsMeeting> allMeetings = teamsMeetingDao.getAllMeetings();
		Assert.assertNotNull(allMeetings);
		Assert.assertTrue(allMeetings.contains(meeting));
	}
	
	@Test
	public void getMeetingsbyRepositoryEntryAndSubIdent() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 8";
		String subIdent = UUID.randomUUID().toString();
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting);
		
		List<TeamsMeeting> meetings = teamsMeetingDao.getMeetings(entry, subIdent, null);
		Assert.assertNotNull(meetings);
		Assert.assertEquals(1, meetings.size());
		Assert.assertEquals(meeting, meetings.get(0));
	}
	
	@Test
	public void getUpcomingMeetings() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 9";
		String subIdent = UUID.randomUUID().toString();
		Date start = DateUtils.addDays(new Date(), 2);
		Date end = DateUtils.addHours(start, 2);
		TeamsMeeting upcomingMeeting = teamsMeetingDao.createMeeting(name, start, end,
				entry, subIdent, null, null);
		
		Date pastStart = DateUtils.addDays(new Date(), -2);
		Date pastEnd = DateUtils.addHours(pastStart, 2);
		TeamsMeeting pastMeeting = teamsMeetingDao.createMeeting(name, pastStart, pastEnd,
				entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		
		List<TeamsMeeting> meetings = teamsMeetingDao.getUpcomingMeetings(entry, subIdent, 5);
		Assert.assertNotNull(meetings);
		Assert.assertTrue(meetings.contains(upcomingMeeting));
		Assert.assertFalse(meetings.contains(pastMeeting));
	}

}
