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

import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.SentReminder;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderServiceImpl implements ReminderService {
	
	private static final XStream ruleXStream = XStreamHelper.createXStreamInstance();
	static {
		ruleXStream.alias("rule", org.olat.modules.reminder.model.ReminderRuleImpl.class);
		ruleXStream.alias("rules", org.olat.modules.reminder.model.ReminderRules.class);
	}
	
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private ReminderRuleEngine ruleEngine;
	@Autowired
	private MailManager mailManager;
	
	@Override
	public Reminder createReminder(RepositoryEntry entry) {
		return reminderDao.createReminder(entry);
	}
	
	@Override
	public Reminder save(Reminder reminder) {
		return reminderDao.save(reminder);
	}
	
	@Override
	public Reminder loadByKey(Long key) {
		return reminderDao.loadByKey(key);
	}
	
	@Override
	public List<Reminder> getReminders(RepositoryEntryRef entry) {
		return reminderDao.getReminders(entry);
	}
	
	@Override
	public Reminder duplicate(Reminder toCopy) {
		return reminderDao.duplicate(toCopy);
	}
	
	@Override
	public void delete(Reminder reminder) {
		reminderDao.delete(reminder);
	}

	@Override
	public List<SentReminder> getSentReminders(Reminder reminder) {
		return reminderDao.getSendReminders(reminder);
	}

	@Override
	public List<SentReminder> getSentReminders(RepositoryEntryRef entry) {
		return reminderDao.getSendReminders(entry);
	}

	@Override
	public String toXML(ReminderRules rules) {
		return ruleXStream.toXML(rules);
	}
	
	@Override
	public ReminderRules toRules(String rulesXml) {
		return (ReminderRules)ruleXStream.fromXML(rulesXml);
	}
	
	@Override
	public void remindAll() {
		List<Reminder> reminders = reminderDao.getReminders();
		for(Reminder reminder:reminders) {
			sendReminder(reminder);
		}
	}

	@Override
	public void sendReminder(Reminder reminder) {
		
		RepositoryEntry entry = reminder.getEntry();
		List<Identity> identitiesToRemind = ruleEngine.evaluate(reminder, false);

		ContactList contactList = new ContactList("Infos");
		contactList.addAllIdentites(identitiesToRemind);
		
		MailContext context = new MailContextImpl("[RepositoryEntry:" + entry.getKey() + "]");
		String subject = "Reminder";
		String body = reminder.getEmailBody();
		
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		bundle.setContactList(contactList);
		bundle.setContent(subject, body);
		
		MailerResult result = mailManager.sendMessage(bundle);
		List<Identity> failedIdentities = result.getFailedIdentites();
		for(Identity identityToRemind:identitiesToRemind) {
			String status;
			if(failedIdentities != null && failedIdentities.contains(identityToRemind)) {
				status = "error";
			} else {
				status = "ok";
			}
			reminderDao.markAsSend(reminder, identityToRemind, status);
		}
	}
}
