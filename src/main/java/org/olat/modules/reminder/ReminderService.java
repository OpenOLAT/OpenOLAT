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
package org.olat.modules.reminder;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReminderService {

	public static final String REMINDERS_XML = "Reminders.xml";
	
	/**
	 * Create a non-persisted reminder.
	 * @param entry
	 * @return
	 */
	public Reminder createReminder(RepositoryEntry entry, Identity creator);
	
	public Reminder save(Reminder reminder);
	
	public Reminder loadByKey(Long key);
	
	public List<Reminder> getReminders(RepositoryEntryRef entry);
	
	public List<ReminderInfos> getReminderInfos(RepositoryEntryRef entry);
	
	public Reminder duplicate(Reminder toCopy, Identity creator);
	
	public void delete(Reminder reminder);
	
	/**
	 * Get all identities matching the reminder rules.
	 * The method returns an identity even if it was already reminded.
	 *
	 * @param reminder
	 * @return
	 */
	public List<Identity> getIdentities(Reminder reminder);
	
	/**
	 * This send the reminders to the target identities within the rules.
	 * 
	 * @param reminder
	 * @param resend 
	 */
	public MailerResult sendReminder(Reminder reminder, boolean resend);
	
	/**
	 * This send reminder, without any check.
	 * 
	 * @param reminder
	 * @param identitiesToRemind
	 */
	public MailerResult sendReminder(Reminder reminder, List<Identity> identitiesToRemind);
	
	
	public List<SentReminder> getSentReminders(Reminder reminder);
	
	public List<SentReminder> getSentReminders(RepositoryEntryRef entry);
	
	/**
	 * The list of reminders to send at a specific date.
	 * 
	 * @param date The date
	 * @return A list of reminders
	 */
	public List<Reminder> getReminders(Date date);
	
	public String toXML(ReminderRules rules);
	
	public ReminderRules toRules(String rulesXml);
	
	public void exportReminders(RepositoryEntryRef entry, OutputStream fExportedDataDir);
	
	/**
	 * The reminders are not persisted and not converted to any new course, group...
	 * 
	 * @param fExportedDataDir
	 * @return
	 */
	public List<Reminder> importRawReminders(Identity creator, RepositoryEntry newEntry, File fExportedDataDir);

}
