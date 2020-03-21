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
package org.olat.modules.bigbluebutton.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	
	@Test
	public void createMeetingForRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "BigBlueButton - 1";
		String subIdent = UUID.randomUUID().toString();
		
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, null);
		dbInstance.commit();
		Assert.assertNotNull(meeting.getKey());
		Assert.assertNotNull(meeting.getCreationDate());
		Assert.assertNotNull(meeting.getLastModified());
		Assert.assertNotNull(meeting.getMeetingId());
		Assert.assertNotNull(meeting.getAttendeePassword());
		Assert.assertNotNull(meeting.getModeratorPassword());
		Assert.assertEquals(entry, meeting.getEntry());
		Assert.assertEquals(subIdent, meeting.getSubIdent());
		Assert.assertNull(meeting.getBusinessGroup());	
	}
	

	@Test
	public void createUpdateMeetingForRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "BigBlueButton - 2";
		String subIdent = UUID.randomUUID().toString();
		
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, null);
		dbInstance.commit();
		meeting.setName("A brand new name");
		meeting.setDescription("A little description");
		meeting.setWelcome("Welcome you");
		meeting.setPermanent(false);
		meeting.setStartDate(new Date());
		meeting.setLeadTime(15);
		meeting.setEndDate(new Date());
		meeting.setFollowupTime(7);
		
		meeting = bigBlueButtonMeetingDao.updateMeeting(meeting);
		dbInstance.commit();
		
		BigBlueButtonMeeting reloadedMeeting = bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
		
		Assert.assertEquals(meeting, reloadedMeeting);
		Assert.assertNotNull(meeting.getCreationDate());
		Assert.assertNotNull(meeting.getLastModified());
		Assert.assertEquals(meeting.getMeetingId(), reloadedMeeting.getMeetingId());
		Assert.assertEquals(meeting.getAttendeePassword(), reloadedMeeting.getAttendeePassword());
		Assert.assertEquals(meeting.getModeratorPassword(), reloadedMeeting.getModeratorPassword());
		
		Assert.assertEquals("A brand new name", reloadedMeeting.getName());
		Assert.assertEquals("A little description", reloadedMeeting.getDescription());
		Assert.assertEquals("Welcome you", reloadedMeeting.getWelcome());

		Assert.assertFalse(reloadedMeeting.isPermanent());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertEquals(15l, reloadedMeeting.getLeadTime());
		Assert.assertNotNull(meeting.getStartWithLeadTime());
		Assert.assertNotNull(meeting.getEndDate());
		Assert.assertEquals(7l, reloadedMeeting.getFollowupTime());
		Assert.assertNotNull(meeting.getEndWithFollowupTime());
		
		Assert.assertNull(reloadedMeeting.getBusinessGroup());
	}
	
	@Test
	public void getMeetingsByRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "BigBlueButton - 2";
		String subIdent = UUID.randomUUID().toString();
		
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, null);
		dbInstance.commit();
		
		List<BigBlueButtonMeeting> meetings = bigBlueButtonMeetingDao.getMeetings(entry, subIdent, null);
		Assert.assertNotNull(meetings);
		Assert.assertEquals(1, meetings.size());
		Assert.assertTrue(meetings.contains(meeting));
	}

}
