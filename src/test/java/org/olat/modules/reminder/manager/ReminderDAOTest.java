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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.SentReminder;
import org.olat.repository.RepositoryEntry;
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
	
	@Test
	public void createAndPersistReminder() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry);
		Assert.assertNotNull(reminder);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 1");
		reminder.setEmailBody("Hello world");

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
		Assert.assertEquals("Hello world", savedReminder.getEmailBody());
		
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
		Assert.assertEquals("Hello world", reloadedReminder.getEmailBody());
	}
	
	@Test
	public void createAndPersistSendReminder() {
		//create and reminder and an identity
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mind-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder reminder = reminderDao.createReminder(entry);
		reminder.setConfiguration("<rules></rules>");
		reminder.setDescription("Reminder - 2");
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

}
