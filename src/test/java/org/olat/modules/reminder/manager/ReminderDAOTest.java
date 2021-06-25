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
package org.olat.modules.reminder.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.reminder.EmailCopy;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.SentReminder;
import org.olat.modules.reminder.model.ReminderImpl;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.SentReminderImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createAndPersistReminder() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry, creator);
		Assert.assertNotNull(reminder);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 1");
		reminder.setEmailSubject("This is a subject");
		reminder.setEmailBody("Hello world");
		reminder.setEmailCopy(Set.of(EmailCopy.owner, EmailCopy.custom));
		reminder.setCustomEmailCopy("email@adress");

		//save and check
		Reminder savedReminder = reminderDao.save(reminder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedReminder);
		Assert.assertNotNull(savedReminder.getKey());
		Assert.assertNotNull(savedReminder.getCreationDate());
		Assert.assertNotNull(savedReminder.getLastModified());
		Assert.assertNotNull(savedReminder.getEntry());
		Assert.assertEquals(entry, savedReminder.getEntry());
		Assert.assertEquals("Reminder - 1", savedReminder.getDescription());
		Assert.assertEquals("<rules></rules>", savedReminder.getConfiguration());
		Assert.assertEquals("This is a subject", savedReminder.getEmailSubject());
		Assert.assertEquals("Hello world", savedReminder.getEmailBody());
		Assert.assertTrue(savedReminder.getEmailCopy().contains(EmailCopy.owner));
		Assert.assertTrue(savedReminder.getEmailCopy().contains(EmailCopy.custom));
		Assert.assertEquals("email@adress", savedReminder.getCustomEmailCopy());
		
		//reload and double check
		Reminder reloadedReminder = reminderDao.loadByKey(savedReminder.getKey());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reloadedReminder);
		Assert.assertEquals(savedReminder.getKey(), reloadedReminder.getKey());
		Assert.assertNotNull(reloadedReminder.getCreationDate());
		Assert.assertNotNull(reloadedReminder.getLastModified());
		Assert.assertEquals(entry, reloadedReminder.getEntry());
		Assert.assertEquals(savedReminder, reloadedReminder);
		Assert.assertEquals("Reminder - 1", reloadedReminder.getDescription());
		Assert.assertEquals("<rules></rules>", reloadedReminder.getConfiguration());
		Assert.assertEquals("This is a subject", reloadedReminder.getEmailSubject());
		Assert.assertEquals("Hello world", reloadedReminder.getEmailBody());
	}
	
	@Test
	public void createAndPersistSendReminder() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-2");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mind-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry, creator);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 2");
		reminder.setEmailSubject("This is a subject");
		reminder.setEmailBody("Hello world");
		Reminder savedReminder = reminderDao.save(reminder);
		Assert.assertNotNull(savedReminder);
		dbInstance.commitAndCloseSession();

		//mark as sent
		reminderDao.markAsSend(reminder, id, "ok");
		dbInstance.commitAndCloseSession();
		
		//reload
		List<SentReminder> sentReminders = reminderDao.getSendReminders(savedReminder);
		Assert.assertNotNull(sentReminders);
		Assert.assertEquals(1, sentReminders.size());
		SentReminder sentReminder = sentReminders.get(0);
		Assert.assertNotNull(sentReminder);
		Assert.assertNotNull(sentReminder.getCreationDate());
		Assert.assertEquals(id, sentReminder.getIdentity());
		Assert.assertEquals(savedReminder, sentReminder.getReminder());
	}
	
	@Test
	public void getReminders_startDate() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 3);
		Assert.assertNotNull(savedReminder);
		dbInstance.commitAndCloseSession();
		
		Date now = new Date();
		boolean found = false;
		List<Reminder> loadedReminders = reminderDao.getReminders(now);
		for(Reminder loadedReminder:loadedReminders) {
			if(loadedReminder.equals(savedReminder)) {
				found = true;
			}
		}
		Assert.assertTrue(found);	
	}

	@Test
	public void getReminders_repositoryEntry_softDeleted() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-12");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry, creator);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 12");
		reminder.setEmailSubject("This is a deleted subject");
		reminder.setEmailBody("Hello, I'm deleted");
		Reminder savedReminder = reminderDao.save(reminder);
		Assert.assertNotNull(savedReminder);
		dbInstance.commitAndCloseSession();
		
		//check that we found the reminder
		List<Reminder> loadedReminders = reminderDao.getReminders(new Date());
		Assert.assertNotNull(loadedReminders);
		Assert.assertTrue(loadedReminders.contains(savedReminder));
		
		// delete the resource
		repositoryService.deleteSoftly(entry, creator, false, false);
		dbInstance.commitAndCloseSession();
		
		// check we don't found the reminder
		List<Reminder> reloadedReminders = reminderDao.getReminders(new Date());
		Assert.assertNotNull(reloadedReminders);
		Assert.assertFalse(reloadedReminders.contains(savedReminder));
	}
	
	@Test
	public void getReminders_repositoryEntry() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry, creator);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 4");
		reminder.setEmailSubject("This is a subject");
		reminder.setEmailBody("Hello world");
		reminder.setEmailCopy(Set.of(EmailCopy.owner, EmailCopy.custom));
		reminder.setCustomEmailCopy("email@adress");
		Reminder savedReminder = reminderDao.save(reminder);
		Assert.assertNotNull(savedReminder);
		dbInstance.commitAndCloseSession();
		
		List<Reminder> loadedReminders = reminderDao.getReminders(entry);
		Assert.assertNotNull(loadedReminders);
		Assert.assertEquals(1, loadedReminders.size());
		
		Reminder loadedReminder = loadedReminders.get(0);
		Assert.assertNotNull(loadedReminder);
		Assert.assertEquals(savedReminder, loadedReminder);
		Assert.assertEquals(entry, loadedReminder.getEntry());
		Assert.assertEquals("Reminder - 4", loadedReminder.getDescription());
		Assert.assertEquals("This is a subject", loadedReminder.getEmailSubject());
		Assert.assertEquals("Hello world", loadedReminder.getEmailBody());
		Assert.assertTrue(savedReminder.getEmailCopy().contains(EmailCopy.owner));
		Assert.assertTrue(savedReminder.getEmailCopy().contains(EmailCopy.custom));
		Assert.assertEquals("email@adress", savedReminder.getCustomEmailCopy());
	}
	
	@Test
	public void markAsSend() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-5");
		Identity recepient = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 5);
		Assert.assertNotNull(savedReminder);
		dbInstance.commitAndCloseSession();
		
		SentReminderImpl sentReminder = reminderDao.markAsSend(savedReminder, recepient, "ok");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(sentReminder);
		Assert.assertNotNull(sentReminder.getKey());
		Assert.assertEquals("ok", sentReminder.getStatus());
		Assert.assertEquals(savedReminder, sentReminder.getReminder());
		Assert.assertEquals(recepient, sentReminder.getIdentity());
	}
	
	@Test
	public void getReminderInfos() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-6");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-6a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-6b");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 6);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedReminder);
		
		//send 2 reminders
		SentReminderImpl sentReminder1 = reminderDao.markAsSend(savedReminder, recepient1, "ok");
		SentReminderImpl sentReminder2 = reminderDao.markAsSend(savedReminder, recepient2, "error");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(sentReminder1);
		Assert.assertNotNull(sentReminder2);
		
		//check reminder infos
		List<ReminderInfos> reminderInfos = reminderDao.getReminderInfos(entry);
		Assert.assertNotNull(reminderInfos);
		Assert.assertEquals(1, reminderInfos.size());
		ReminderInfos reminderInfo = reminderInfos.get(0);
		Assert.assertNotNull(reminderInfo);
		
		Assert.assertEquals(savedReminder.getKey(), reminderInfo.getKey());
		Assert.assertEquals(2, reminderInfo.getNumOfRemindersSent());
		Assert.assertEquals(creator.getKey(), reminderInfo.getCreatorKey());
		Assert.assertEquals("Reminder - 6", reminderInfo.getDescription());
	}
	
	@Test
	public void getSendReminders_byReminder() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-7");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-7a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-7b");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 7);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedReminder);
		
		//send 3 reminders
		SentReminderImpl sentReminder1 = reminderDao.markAsSend(savedReminder, recepient1, "ok");
		SentReminderImpl sentReminder2 = reminderDao.markAsSend(savedReminder, recepient2, "error");
		SentReminderImpl sentReminder3 = reminderDao.markAsSend(savedReminder, recepient1, "error");
		dbInstance.commitAndCloseSession();
		
		//load the sent reminder log
		List<SentReminder> sentReminders = reminderDao.getSendReminders(savedReminder);
		Assert.assertNotNull(sentReminders);
		Assert.assertEquals(3, sentReminders.size());
		Assert.assertTrue(sentReminders.contains(sentReminder1));
		Assert.assertTrue(sentReminders.contains(sentReminder2));
		Assert.assertTrue(sentReminders.contains(sentReminder3));
	}
	
	@Test
	public void getSendReminders_byRepositoryEntry() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-8");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-8a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-8b");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 8);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedReminder);
		
		//send 3 reminders
		SentReminderImpl sentReminder1 = reminderDao.markAsSend(savedReminder, recepient1, "ok");
		SentReminderImpl sentReminder2 = reminderDao.markAsSend(savedReminder, recepient2, "error");
		SentReminderImpl sentReminder3 = reminderDao.markAsSend(savedReminder, recepient2, "error");
		dbInstance.commitAndCloseSession();
		
		//load the sent reminder log
		List<SentReminder> sentReminders = reminderDao.getSendReminders(entry);
		Assert.assertNotNull(sentReminders);
		Assert.assertEquals(3, sentReminders.size());
		Assert.assertTrue(sentReminders.contains(sentReminder1));
		Assert.assertTrue(sentReminders.contains(sentReminder2));
		Assert.assertTrue(sentReminders.contains(sentReminder3));
	}
	
	@Test
	public void getReminderRecipientKeys() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-9");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9b");
		Identity recepient3 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9c");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder savedReminder = createAndSaveReminder(entry, creator, 8);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedReminder);
		
		//send 3 reminders
		reminderDao.markAsSend(savedReminder, recepient1, "ok");
		reminderDao.markAsSend(savedReminder, recepient2, "error");
		reminderDao.markAsSend(savedReminder, recepient3, "error");
		dbInstance.commitAndCloseSession();
		
		//load the sent reminder log
		List<Long> recipientKeys = reminderDao.getReminderRecipientKeys(savedReminder);
		Assert.assertNotNull(recipientKeys);
		Assert.assertEquals(3, recipientKeys.size());
		Assert.assertTrue(recipientKeys.contains(recepient1.getKey()));
		Assert.assertTrue(recipientKeys.contains(recepient2.getKey()));
		Assert.assertTrue(recipientKeys.contains(recepient3.getKey()));
	}
	
	@Test
	public void duplicateReminder() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-12");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminderToCopy = createAndSaveReminder(entry, creator, 12);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reminderToCopy);
		
		Reminder duplicate = reminderDao.duplicate(reminderToCopy, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(duplicate);
		Assert.assertNotNull(duplicate.getKey());
		
		ReminderImpl reloadedDuplicate = (ReminderImpl)reminderDao.loadByKey(duplicate.getKey());
		Assert.assertNotNull(reloadedDuplicate);
		Assert.assertEquals(creator, reloadedDuplicate.getCreator());
		Assert.assertEquals(entry, reloadedDuplicate.getEntry());
		Assert.assertEquals(reminderToCopy.getEmailBody(), reloadedDuplicate.getEmailBody());
		Assert.assertEquals(reminderToCopy.getEmailSubject(), reloadedDuplicate.getEmailSubject());
		Assert.assertTrue(reloadedDuplicate.getDescription().startsWith(reminderToCopy.getDescription()));
		Assert.assertEquals(reminderToCopy.getConfiguration(), reloadedDuplicate.getConfiguration());
		Assert.assertEquals(reminderToCopy.getEmailCopy(), reloadedDuplicate.getEmailCopy());
		Assert.assertEquals(reminderToCopy.getCustomEmailCopy(), reloadedDuplicate.getCustomEmailCopy());
	}
	
	@Test
	public void deleteReminder() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-9");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9b");
		Identity recepient3 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-9c");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminderToDelete = createAndSaveReminder(entry, creator, 8);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reminderToDelete);
		
		//send 4 reminders
		reminderDao.markAsSend(reminderToDelete, recepient1, "ok");
		reminderDao.markAsSend(reminderToDelete, recepient2, "error");
		reminderDao.markAsSend(reminderToDelete, recepient3, "error");
		reminderDao.markAsSend(reminderToDelete, recepient2, "error");
		dbInstance.commitAndCloseSession();
		
		//check
		List<SentReminder> sentReminders = reminderDao.getSendReminders(reminderToDelete);
		Assert.assertNotNull(sentReminders);
		Assert.assertEquals(4, sentReminders.size());
		
		reminderDao.delete(reminderToDelete);
		dbInstance.commit();
		
		//check that the reminder is missing
		List<Reminder> deletedReminders = reminderDao.getReminders(entry);
		Assert.assertNotNull(deletedReminders);
		Assert.assertEquals(0, deletedReminders.size());
	}
	
	/**
	 * Check that not all reminders are deleted from the database.
	 */
	@Test
	public void deleteReminder_paranoia() {
		//create and reminder and an identity
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("creator-rem-10");
		Identity recepient1 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-10a");
		Identity recepient2 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-10b");
		Identity recepient3 = JunitTestHelper.createAndPersistIdentityAsRndUser("recepient-rem-10c");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminderToDelete = createAndSaveReminder(entry, creator, 10);
		Reminder survivingReminder = createAndSaveReminder(entry, creator, 10);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reminderToDelete);
		
		//send 4 reminders
		reminderDao.markAsSend(reminderToDelete, recepient1, "ok");
		reminderDao.markAsSend(reminderToDelete, recepient2, "error");
		SentReminder sentReminder1 = reminderDao.markAsSend(survivingReminder, recepient3, "error");
		SentReminder sentReminder2 = reminderDao.markAsSend(survivingReminder, recepient2, "error");
		dbInstance.commitAndCloseSession();
		
		//check
		List<SentReminder> sentRemindersToDelete = reminderDao.getSendReminders(reminderToDelete);
		Assert.assertNotNull(sentRemindersToDelete);
		Assert.assertEquals(2, sentRemindersToDelete.size());
		List<SentReminder> survivingSentReminders = reminderDao.getSendReminders(survivingReminder);
		Assert.assertNotNull(survivingSentReminders);
		Assert.assertEquals(2, survivingSentReminders.size());
		
		reminderDao.delete(reminderToDelete);
		dbInstance.commit();
		
		//check that the reminder is missing
		List<Reminder> deletedReminders = reminderDao.getReminders(entry);
		Assert.assertNotNull(deletedReminders);
		Assert.assertEquals(1, deletedReminders.size());
		Assert.assertEquals(survivingReminder, deletedReminders.get(0));
		//check that the send reminders are deleted but not all
		List<SentReminder> reloadedSurvivingSentReminders = reminderDao.getSendReminders(survivingReminder);
		Assert.assertNotNull(reloadedSurvivingSentReminders);
		Assert.assertEquals(2, reloadedSurvivingSentReminders.size());
		List<SentReminder> allSurvivingSentReminders = reminderDao.getSendReminders(entry);
		Assert.assertNotNull(allSurvivingSentReminders);
		Assert.assertEquals(2, allSurvivingSentReminders.size());
		Assert.assertTrue(allSurvivingSentReminders.contains(sentReminder1));
		Assert.assertTrue(allSurvivingSentReminders.contains(sentReminder2));
	}
	
	private Reminder createAndSaveReminder(RepositoryEntry entry, Identity creator, int num) {
		Reminder reminder = reminderDao.createReminder(entry, creator);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - " + num);
		reminder.setEmailSubject("This is a subject - " + num);
		reminder.setEmailBody("Hello world - " + num);
		reminder.setEmailCopy(Set.of(EmailCopy.owner, EmailCopy.custom));
		reminder.setCustomEmailCopy("email@adress");
		return reminderDao.save(reminder);
	}
	
	@Test
	public void getCourseEnrollmentDates() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-rem-11");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-rem-11a");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-rem-11b");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-rem-11c");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant3, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		List<Identity> identities = new ArrayList<>();
		identities.add(coach);
		identities.add(participant1);
		identities.add(participant2);
		identities.add(participant3);
		
		//get the dates
		Map<Long,Date> enrollmentDates = reminderDao.getCourseEnrollmentDates(entry, identities);
		Assert.assertNotNull(enrollmentDates);
		Assert.assertEquals(4, enrollmentDates.size());
		Assert.assertTrue(enrollmentDates.containsKey(coach.getKey()));
		Assert.assertTrue(enrollmentDates.containsKey(participant1.getKey()));
		Assert.assertTrue(enrollmentDates.containsKey(participant2.getKey()));
		Assert.assertTrue(enrollmentDates.containsKey(participant3.getKey()));
		Assert.assertNotNull(enrollmentDates.get(coach.getKey()));
		Assert.assertNotNull(enrollmentDates.get(participant1.getKey()));
		Assert.assertNotNull(enrollmentDates.get(participant2.getKey()));
		Assert.assertNotNull(enrollmentDates.get(participant3.getKey()));
	}
}
