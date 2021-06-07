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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.SentReminder;
import org.olat.modules.reminder.model.ImportExportReminder;
import org.olat.modules.reminder.model.ImportExportReminders;
import org.olat.modules.reminder.model.ReminderImpl;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderServiceImpl implements ReminderService {
	
	private static final Logger log = Tracing.createLoggerFor(ReminderServiceImpl.class);
	
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private ReminderRuleEngine ruleEngine;
	
	@Override
	public Reminder createReminder(RepositoryEntry entry, Identity creator) {
		return reminderDao.createReminder(entry, creator);
	}
	
	@Override
	public Reminder save(Reminder reminder) {
		//start optimization
		optimizeStartDate(reminder);
		return reminderDao.save(reminder);
	}
	
	private void optimizeStartDate(Reminder reminder) {
		Date startDate = null;
		String configuration = reminder.getConfiguration();
		if(StringHelper.containsNonWhitespace(configuration)) {
			ReminderRules rules = toRules(configuration);
			for(ReminderRule rule:rules.getRules()) {
				if(rule instanceof ReminderRuleImpl && ReminderRuleEngine.DATE_RULE_TYPE.equals(rule.getType())) {
					ReminderRuleImpl r = (ReminderRuleImpl)rule;
					if(DateRuleSPI.AFTER.equals(r.getOperator()) && StringHelper.containsNonWhitespace(r.getRightOperand())) {
						try {
							Date date = Formatter.parseDatetime(r.getRightOperand());
							if(startDate == null) {
								startDate = date;
							} else if(startDate.compareTo(date) > 0) {
								startDate = date;
							}
						} catch (ParseException e) {
							log.error("", e);
						}
					}
				}
			}
		}		
		((ReminderImpl)reminder).setStartDate(startDate);
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
	public List<ReminderInfos> getReminderInfos(RepositoryEntryRef entry) {
		return reminderDao.getReminderInfos(entry);
	}
	
	@Override
	public Reminder duplicate(Reminder toCopy, Identity creator) {
		return reminderDao.duplicate(toCopy, creator);
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
		return ReminderRulesXStream.toXML(rules);
	}
	
	@Override
	public ReminderRules toRules(String rulesXml) {
		return ReminderRulesXStream.toRules(rulesXml);
	}
	
	@Override
	public void exportReminders(RepositoryEntryRef entry, OutputStream fOut) {
		List<Reminder> reminders = reminderDao.getReminders(entry);
		try {
			ImportExportReminders exportReminders = new ImportExportReminders();
			for(Reminder reminder:reminders) {
				ImportExportReminder exportReminder = new ImportExportReminder(reminder);
				exportReminders.getReminders().add(exportReminder);
			}
			ReminderRulesXStream.toXML(exportReminders, fOut);
		} catch(Exception e) {
			log.error("", e);
		}
	}

	@Override
	public List<Reminder> importRawReminders(Identity creator, RepositoryEntry newEntry, File fExportedDataDir) {
		File reminderFile = new File(fExportedDataDir, REMINDERS_XML);
		List<Reminder> reminders = new ArrayList<>();
		if(reminderFile.exists()) {
			try(InputStream in = new FileInputStream(reminderFile)) {
				ImportExportReminders importReminders = ReminderRulesXStream.fromXML(in);
				List<ImportExportReminder> importReminderList = importReminders.getReminders();
				for(ImportExportReminder importReminder:importReminderList) {
					Reminder reminder = reminderDao.createReminder(newEntry, creator);
					reminder.setDescription(importReminder.getDescription());
					reminder.setEmailBody(importReminder.getEmailBody());	
					reminder.setEmailSubject(importReminder.getEmailSubject() == null ? importReminder.getDescription() : importReminder.getEmailSubject());
					reminder.setConfiguration(importReminder.getConfiguration());
					reminders.add(reminder);
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return reminders;
	}

	@Override
	public void remindAll() {
		Date now = new Date();
		List<Reminder> reminders = reminderDao.getReminders(now);
		for(Reminder reminder:reminders) {
			sendReminder(reminder, false);
		}
	}
	
	@Override
	public List<Identity> getIdentities(Reminder reminder) {
		return ruleEngine.evaluate(reminder, true);
	}

	@Override
	public MailerResult sendReminder(Reminder reminder, boolean resend) {
		List<Identity> identitiesToRemind = ruleEngine.evaluate(reminder, resend);
		return sendReminder(reminder, identitiesToRemind);
	}
	
	@Override
	public MailerResult sendReminder(Reminder reminder, List<Identity> identitiesToRemind) {
		RepositoryEntry entry = reminder.getEntry();
		ContactList contactList = new ContactList("Infos");
		contactList.addAllIdentites(identitiesToRemind);
		
		MailContext context = new MailContextImpl("[RepositoryEntry:" + entry.getKey() + "]");
		Locale locale = I18nModule.getDefaultLocale();
		Translator trans = Util.createPackageTranslator(ReminderAdminController.class, locale);
		String subject = reminder.getEmailSubject();
		String body = reminder.getEmailBody();
		if (body.contains("$courseurl")) {
			body = body.replace("$courseurl", "<a href=\"$courseurl\">$courseurl</a>");
		} else {			
			body = body + "<p>---<br />" + trans.translate("reminder.from.course", new String[] {"<a href=\"$courseurl\">$coursename</a>"}) + "</p>";
		}
		String metaId = UUID.randomUUID().toString();
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();

		MailerResult overviewResult = new MailerResult();
		CourseReminderTemplate template = new CourseReminderTemplate(subject, body, url, entry, locale);

		for(Identity identityToRemind:identitiesToRemind) {
			String status;
			MailBundle bundle = mailManager.makeMailBundle(context, identityToRemind, template, null, metaId, overviewResult);
			if(bundle == null) {
				status = "error";
			} else {
				MailerResult result = mailManager.sendMessage(bundle);
				overviewResult.append(result);
				
				List<Identity> failedIdentities = result.getFailedIdentites();
				if(failedIdentities != null && failedIdentities.contains(identityToRemind)) {
					status = "error";
				} else {
					status = "ok";
				}
			}
			reminderDao.markAsSend(reminder, identityToRemind, status);
		}
		
		return overviewResult;
	}
}
