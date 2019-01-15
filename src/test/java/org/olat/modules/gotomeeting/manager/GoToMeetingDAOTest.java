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
package org.olat.modules.gotomeeting.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.CodeHelper;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GoToMeetingDAO meetingDao;
	@Autowired
	private GoToOrganizerDAO organizerDao;
	
	@Test
	public void createMeeting_without() {
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer("My account", username, accessToken, refreshToken, organizerKey, "Lucas", "de Leyde", null, null, 10l, null);
		Assert.assertNotNull(organizer);
		
		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());

		GoToMeeting training = meetingDao.createTraining("New training", null, "Very interessant", trainingKey, start, end, organizer, null, null, null);
		dbInstance.commit();
		Assert.assertNotNull(training);
		Assert.assertNotNull(training.getKey());
		Assert.assertNotNull(training.getCreationDate());
		Assert.assertNotNull(training.getLastModified());
		Assert.assertEquals("New training", training.getName());
		Assert.assertEquals("Very interessant", training.getDescription());
		Assert.assertNotNull(training.getStartDate());
		Assert.assertNotNull(training.getEndDate());
	}
	
	@Test
	public void getMeetings_withRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Michael", "Wolgemut", null, null, 10l, null);
		Assert.assertNotNull(organizer);

		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao.createTraining("New training", null, "Very interessant", trainingKey, start, end,
				organizer, entry, "d9912", null);
		dbInstance.commit();
		Assert.assertNotNull(training);
		
		List<GoToMeeting> meetings = meetingDao.getMeetings(GoToType.training, entry, "d9912", null);
		Assert.assertNotNull(meetings);
		Assert.assertEquals(1, meetings.size());
		Assert.assertTrue(meetings.contains(training));
	}
	
	@Test
	public void loadMeetingByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Levinus", "Memminger", null, null, 10l, null);
		Assert.assertNotNull(organizer);

		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao.createTraining("Training by key", null, "Load training by key", trainingKey, start, end,
				organizer, entry, "d9915", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(training);
		
		GoToMeeting reloadedTraining = meetingDao.loadMeetingByKey(training.getKey());
		Assert.assertNotNull(reloadedTraining);
		Assert.assertEquals(training, reloadedTraining);
	}
	
	@Test
	public void loadMeetingByExternalId() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		String externalId = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Levinus", "Memminger", null, null, 10l, null);
		Assert.assertNotNull(organizer);

		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao.createTraining("Training by key", externalId, "Load training by external key", trainingKey, start, end,
				organizer, entry, "d9916", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(training);
		
		GoToMeeting reloadedTraining = meetingDao.loadMeetingByExternalId(externalId);
		Assert.assertNotNull(reloadedTraining);
		Assert.assertEquals(training, reloadedTraining);
	}
	
	@Test
	public void countMeetingsOrganizedBy() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		String externalId = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Hans", "Pleydenwurff", null, null, 10l, null);
		Assert.assertNotNull(organizer);

		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao.createTraining("Training by key", externalId, "Count the meetings organized by this organizer",
				trainingKey, start, end, organizer, entry, "d9916", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(training);
		
		int countOrganizersMeetings = meetingDao.countMeetingsOrganizedBy(organizer);
		Assert.assertEquals(1, countOrganizersMeetings);
	}
	
	/**
	 * Check different overlap scenario
	 */
	@Test
	public void getMeetingsOverlap() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Michael", "Wolgemut", null, null, 10l, null);
		Assert.assertNotNull(organizer);

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 8, 12, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		cal.set(2016, 8, 12, 18, 0, 0);
		Date end = cal.getTime();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao.createTraining("New training", null, "Very interessant", trainingKey, start, end,
				organizer, entry, "d9914", null);
		dbInstance.commit();
		Assert.assertNotNull(training);

		//check organizer availability (same date)
		List<GoToMeeting> overlaps = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start, end);
		Assert.assertNotNull(overlaps);
		Assert.assertEquals(1, overlaps.size());

		//check organizer availability (end overlap)
		cal.set(2016, 8, 12, 10, 0, 0);
		Date start_1 = cal.getTime();
		cal.set(2016, 8, 12, 14, 0, 0);
		Date end_1 = cal.getTime();
		List<GoToMeeting> overlaps_1 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_1, end_1);
		Assert.assertEquals(1, overlaps_1.size());
		
		//check organizer availability (start overlap)
		cal.set(2016, 8, 12, 14, 0, 0);
		Date start_2 = cal.getTime();
		cal.set(2016, 8, 12, 20, 0, 0);
		Date end_2 = cal.getTime();
		List<GoToMeeting> overlaps_2 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_2, end_2);
		Assert.assertEquals(1, overlaps_2.size());
		
		//check organizer availability (within)
		cal.set(2016, 8, 12, 14, 0, 0);
		Date start_3 = cal.getTime();
		cal.set(2016, 8, 12, 15, 0, 0);
		Date end_3 = cal.getTime();
		List<GoToMeeting> overlaps_3 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_3, end_3);
		Assert.assertNotNull(overlaps_3);
		Assert.assertEquals(1, overlaps_3.size());
		
		//check organizer availability (start before, end after)
		cal.set(2016, 8, 12, 10, 0, 0);
		Date start_4 = cal.getTime();
		cal.set(2016, 8, 12, 22, 0, 0);
		Date end_4 = cal.getTime();
		List<GoToMeeting> overlaps_4 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_4, end_4);
		Assert.assertNotNull(overlaps_4);
		Assert.assertEquals(1, overlaps_4.size());
		
		//check organizer availability (in past)
		cal.set(2016, 8, 12, 9, 0, 0);
		Date start_5 = cal.getTime();
		cal.set(2016, 8, 12, 11, 0, 0);
		Date end_5 = cal.getTime();
		List<GoToMeeting> overlaps_5 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_5, end_5);
		Assert.assertEquals(0, overlaps_5.size());
		
		//check organizer availability (in future)
		cal.set(2016, 8, 12, 20, 0, 0);
		Date start_6= cal.getTime();
		cal.set(2016, 8, 12, 21, 0, 0);
		Date end_6 = cal.getTime();
		List<GoToMeeting> overlaps_6 = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start_6, end_6);
		Assert.assertEquals(0, overlaps_6.size());
	}
}
