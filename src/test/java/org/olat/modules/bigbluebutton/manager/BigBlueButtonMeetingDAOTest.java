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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
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
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BigBlueButtonServerDAO bigBlueButtonServerDao;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	
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
	public void loadForUpdate() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "BigBlueButton - 2";
		String subIdent = UUID.randomUUID().toString();
		
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, null);
		dbInstance.commit();

		
		BigBlueButtonMeeting reloadedMeeting = bigBlueButtonMeetingDao.loadForUpdate(meeting);
		dbInstance.commit();
		Assert.assertNotNull(reloadedMeeting);
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
	
	@Test
	public void getAllMeetings() {
		String name = "BigBlueButton - 3";
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB group", "bbb-desc", -1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, null, null, group);
		dbInstance.commit();
		
		List<BigBlueButtonMeeting> meetings = bigBlueButtonMeetingDao.getAllMeetings();
		Assert.assertNotNull(meetings);
		Assert.assertTrue(!meetings.isEmpty());
		Assert.assertTrue(meetings.contains(meeting));
	}
	
	@Test
	public void getMeetingsByServer() {
		String url = "https://bbb.frentix.com/bigbluebutton";
		String sharedSecret = UUID.randomUUID().toString();
		BigBlueButtonServer server = bigBlueButtonServerDao.createServer(url, null, sharedSecret);
		
		String name = "BigBlueButton - 7";
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB server", "bbb-server", -1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, null, null, group);
		dbInstance.commit();
		
		((BigBlueButtonMeetingImpl)meeting).setServer(server);
		meeting = bigBlueButtonMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonMeeting> serversMeetings = bigBlueButtonMeetingDao.getMeetings(server);
		Assert.assertNotNull(serversMeetings);
		Assert.assertEquals(1, serversMeetings.size());
		Assert.assertTrue(serversMeetings.contains(meeting));
	}
	
	@Test
	public void getConcurrentMeetings() {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template", externalId, false);
		template.setMaxConcurrentMeetings(2);
		template = bigBlueButtonMeetingTemplateDao.updateTemplate(template);
		dbInstance.commit();
		
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB group", "bbb-desc", -1, -1, false, false, false, false, false);
		createMeeting("BigBlueButton - 4", date(1, 12), 15, date(1, 14), 15, template, group);
		createMeeting("BigBlueButton - 5", date(1, 10), 120, date(1, 18), 120, template, group);
		createMeeting("BigBlueButton - 6", date(1, 14), 0, date(1, 19), 0, template, group);
		createMeeting("BigBlueButton - 7", date(2, 12), 15, date(2, 15), 15, template, group);
		dbInstance.commit();

		List<Long> concurrent = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(1, 15), date(1, 19));
		Assert.assertEquals(2, concurrent.size());
		
		List<Long> concurrentFollowup = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(1, 17), date(1, 21));
		Assert.assertEquals(2, concurrentFollowup.size());
		
		List<Long> concurrentWidePeriod = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(0, 10), date(3, 21));
		Assert.assertEquals(4, concurrentWidePeriod.size());
		
		List<Long> concurrentWithin = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(2, 13), date(2, 14));
		Assert.assertEquals(1, concurrentWithin.size());
		
		List<Long> concurrentOverlapAfter = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(1, 19), date(1, 21));
		Assert.assertTrue(concurrentOverlapAfter.isEmpty());
		
		List<Long> concurrentOverlapBefore = bigBlueButtonMeetingDao.getConcurrentMeetings(template, date(1, 7), date(1, 10));
		Assert.assertTrue(concurrentOverlapBefore.isEmpty());
	}
	
	private BigBlueButtonMeeting createMeeting(String name, Date start, int leadTime, Date end, int followupTime,
			BigBlueButtonMeetingTemplate template, BusinessGroup group) {
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting(name, null, null, group);
		meeting.setStartDate(start);
		meeting.setLeadTime(leadTime);
		meeting.setEndDate(end);
		meeting.setFollowupTime(followupTime);
		meeting.setTemplate(template);
		return bigBlueButtonMeetingDao.updateMeeting(meeting);
	}
	
	private Date date(int addDays, int hour) {
		Calendar cal = Calendar.getInstance();
		cal = CalendarUtils.getStartOfDay(cal);
		cal.add(Calendar.DATE, addDays);
		cal.set(Calendar.HOUR, hour);
		return cal.getTime();
	}
}
