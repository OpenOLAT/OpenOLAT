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
package org.olat.modules.adobeconnect.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private AdobeConnectMeetingDAO adobeConnectMeetingDao;
	
	@Test
	public void createMeeting() {
		AdobeConnectMeeting meeting = adobeConnectMeetingDao.createMeeting("New meeting", "Very interessant", false, new Date(), 15, new Date(), 15, null, "sco-id", "folder-id", "DFN", null, null, null);
		dbInstance.commit();
		Assert.assertNotNull(meeting);
		Assert.assertNotNull(meeting.getKey());
		Assert.assertNotNull(meeting.getCreationDate());
		Assert.assertNotNull(meeting.getLastModified());
		Assert.assertEquals("sco-id", meeting.getScoId());
		Assert.assertEquals("DFN", meeting.getEnvName());
		Assert.assertEquals("New meeting", meeting.getName());
		Assert.assertEquals("Very interessant", meeting.getDescription());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertNotNull(meeting.getEndDate());
	}
	
	@Test
	public void createMeeting_courseLike() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		AdobeConnectMeeting meeting = adobeConnectMeetingDao.createMeeting("Course meeting", "Annoying", false, new Date(), 10, new Date(), 10, null, "sco-id", "folder-id", "DFN", entry, subIdent, null);
		dbInstance.commit();
		Assert.assertNotNull(meeting);
		Assert.assertNotNull(meeting.getKey());
		Assert.assertNotNull(meeting.getCreationDate());
		Assert.assertNotNull(meeting.getLastModified());
		Assert.assertEquals("sco-id", meeting.getScoId());
		Assert.assertEquals("DFN", meeting.getEnvName());
		Assert.assertEquals("Course meeting", meeting.getName());
		Assert.assertEquals("Annoying", meeting.getDescription());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertNotNull(meeting.getEndDate());
		Assert.assertEquals(entry, meeting.getEntry());
		Assert.assertEquals(subIdent, meeting.getSubIdent());
		Assert.assertNull(meeting.getBusinessGroup());
	}

	@Test
	public void loadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		AdobeConnectMeeting meeting = adobeConnectMeetingDao.createMeeting("Key meeting", "Primary", false, new Date(), 10, new Date(), 10, null, "sco-pid", "folder-id", null, entry, subIdent, null);
		dbInstance.commitAndCloseSession();
		
		// load the meeting
		AdobeConnectMeeting reloadedMeeting = adobeConnectMeetingDao.loadByKey(meeting.getKey());
		// check
		Assert.assertNotNull(reloadedMeeting);
		Assert.assertNotNull(reloadedMeeting.getKey());
		Assert.assertNotNull(reloadedMeeting.getCreationDate());
		Assert.assertNotNull(reloadedMeeting.getLastModified());
		Assert.assertEquals("sco-pid", reloadedMeeting.getScoId());
		Assert.assertNull(meeting.getEnvName());
		Assert.assertEquals("Key meeting", meeting.getName());
		Assert.assertEquals("Primary", meeting.getDescription());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertNotNull(meeting.getEndDate());
		Assert.assertEquals(entry, meeting.getEntry());
		Assert.assertEquals(subIdent, meeting.getSubIdent());
		Assert.assertNull(meeting.getBusinessGroup());
	}
	
	@Test
	public void getAllMeetings() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		AdobeConnectMeeting meeting1 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", false, new Date(), 5, new Date(), 5, null, "sco-cid-1", "folder-id", null, entry, subIdent, null);
		AdobeConnectMeeting meeting2 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", false, new Date(), 5, new Date(), 5, "tmp-id-1", "sco-cid-2", "folder-id", null, entry, subIdent, null);
		dbInstance.commitAndCloseSession();
		
		List<AdobeConnectMeeting> allMeetings = adobeConnectMeetingDao.getAllMeetings();
		Assert.assertTrue(allMeetings.size() >= 2);
		Assert.assertTrue(allMeetings.contains(meeting1));
		Assert.assertTrue(allMeetings.contains(meeting2));
	}

	@Test
	public void getMeetings_repositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		AdobeConnectMeeting meeting1 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", true, new Date(), 5, new Date(), 5, null, "sco-cid-1", "folder-id", null, entry, subIdent, null);
		AdobeConnectMeeting meeting2 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", true, new Date(), 5, new Date(), 5, "tmp-id-1", "sco-cid-2", "folder-id", null, entry, subIdent, null);
		dbInstance.commitAndCloseSession();
		
		// load meetings
		List<AdobeConnectMeeting> meetings = adobeConnectMeetingDao.getMeetings(entry, subIdent);
		Assert.assertEquals(2, meetings.size());
		Assert.assertTrue(meetings.contains(meeting1));
		Assert.assertTrue(meetings.contains(meeting2));
	}
	
	@Test
	public void getMeetings_businessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "Connected group", "Adobe connected group", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commit();
		
		AdobeConnectMeeting meeting1 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", false, new Date(), 5, new Date(), 5, null, "sco-cid-1", "folder-id", null, null, null, group);
		AdobeConnectMeeting meeting2 = adobeConnectMeetingDao.createMeeting("Course A", "Primary", false, new Date(), 5, new Date(), 5, null, "sco-cid-2", "folder-id", null, null, null, group);
		dbInstance.commitAndCloseSession();
		
		// load meetings
		List<AdobeConnectMeeting> meetings = adobeConnectMeetingDao.getMeetings(group);
		Assert.assertEquals(2, meetings.size());
		Assert.assertTrue(meetings.contains(meeting1));
		Assert.assertTrue(meetings.contains(meeting2));
	}
	
	@Test
	public void getMeetingsBefore() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		Date oldDate = cal.getTime();
		AdobeConnectMeeting oldMeeting = adobeConnectMeetingDao.createMeeting("Course old", "Primary", false, oldDate, 5, oldDate, 5, null, "sco-cid-1", "folder-id", null, entry, subIdent, null);
		AdobeConnectMeeting newMeeting = adobeConnectMeetingDao.createMeeting("Course new", "Primary", false, new Date(), 5, new Date(), 5, null, "sco-cid-1", "folder-id", null, entry, subIdent, null);
		dbInstance.commitAndCloseSession();
		
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date beforeDate = cal.getTime();
		List<AdobeConnectMeeting> allMeetings = adobeConnectMeetingDao.getMeetingsBefore(beforeDate);
		Assert.assertTrue(allMeetings.size() >= 1);
		Assert.assertTrue(allMeetings.contains(oldMeeting));
		Assert.assertFalse(allMeetings.contains(newMeeting));
	}
}
