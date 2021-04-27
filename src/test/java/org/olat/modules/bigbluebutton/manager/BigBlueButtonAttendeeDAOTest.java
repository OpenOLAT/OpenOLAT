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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAttendeeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonAttendeeDAO bigBlueButtonAttendeeDao;
	
	
	@Test
	public void createAttendee() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-attendee-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Attendees 1", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Attend - 1", null, null, group, id);
		dbInstance.commit();
		
		BigBlueButtonAttendee attendee = bigBlueButtonAttendeeDao
				.createAttendee(id, null, BigBlueButtonAttendeeRoles.moderator, new Date(), meeting);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(attendee);
		Assert.assertNotNull(attendee.getCreationDate());
		Assert.assertNotNull(attendee.getLastModified());
		Assert.assertNotNull(attendee.getJoinDate());
		Assert.assertEquals(meeting, attendee.getMeeting());
		Assert.assertEquals(id, attendee.getIdentity());
		Assert.assertEquals(BigBlueButtonAttendeeRoles.moderator, attendee.getRolesEnum());
	}
	
	@Test
	public void createAttendee_guest() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Attendees 1", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Attend - 1", null, null, group, null);
		dbInstance.commit();
		
		BigBlueButtonAttendee attendee = bigBlueButtonAttendeeDao
				.createAttendee(null, "Ruby", BigBlueButtonAttendeeRoles.guest, new Date(), meeting);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(attendee);
		Assert.assertNotNull(attendee.getCreationDate());
		Assert.assertNotNull(attendee.getLastModified());
		Assert.assertNotNull(attendee.getJoinDate());
		Assert.assertEquals("Ruby", attendee.getPseudo());
		Assert.assertNull(attendee.getIdentity());
		Assert.assertEquals(BigBlueButtonAttendeeRoles.guest, attendee.getRolesEnum());
	}
	
	@Test
	public void hasAttendee_identified() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-attendee-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-attendee-3");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Attendees 2", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Attend - 1", null, null, group, id2);
		BigBlueButtonAttendee attendee1 = bigBlueButtonAttendeeDao
				.createAttendee(id1, null, BigBlueButtonAttendeeRoles.moderator, new Date(), meeting);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(attendee1);
		
		boolean present1 = bigBlueButtonAttendeeDao.hasAttendee(id1, meeting);
		Assert.assertTrue(present1);
		boolean present2 = bigBlueButtonAttendeeDao.hasAttendee(id2, meeting);
		Assert.assertFalse(present2);
	}
	
	@Test
	public void hasAttendee_guest() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Attendees 2", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Attend - 1", null, null, group, null);
		BigBlueButtonAttendee attendee1 = bigBlueButtonAttendeeDao
				.createAttendee(null, "Jeremey", BigBlueButtonAttendeeRoles.guest, new Date(), meeting);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(attendee1);
		
		boolean present1 = bigBlueButtonAttendeeDao.hasAttendee("Jeremey", meeting);
		Assert.assertTrue(present1);
		boolean present2 = bigBlueButtonAttendeeDao.hasAttendee("Albert", meeting);
		Assert.assertFalse(present2);
	}
	
	@Test
	public void getAttendee() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-attendee-4");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Attendees 2", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Attend - 1", null, null, group, null);
		BigBlueButtonAttendee attendee = bigBlueButtonAttendeeDao
				.createAttendee(id, null, BigBlueButtonAttendeeRoles.moderator, new Date(), meeting);
		dbInstance.commitAndCloseSession();
			
		BigBlueButtonAttendee reloadedAttendee = bigBlueButtonAttendeeDao.getAttendee(id, meeting);
		Assert.assertNotNull(reloadedAttendee);
		Assert.assertEquals(attendee, reloadedAttendee);
		Assert.assertEquals(id, reloadedAttendee.getIdentity());
		Assert.assertEquals(meeting, reloadedAttendee.getMeeting());
	}

}
