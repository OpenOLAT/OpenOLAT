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
package org.olat.modules.todo.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.todo.ToDoAssignedMailBatch;
import org.olat.modules.todo.ToDoAssignedMailBatch.Entry;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToDoMailing {
	
	private static final Logger log = Tracing.createLoggerFor(ToDoMailing.class);
	private static final int MAX_DIGEST_ROWS = 20;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private BaseSecurity securityManager;

	public void sendAssignedEmail(Identity doer, Identity assignee, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		if (assignee.getStatus() > Identity.STATUS_VISIBLE_LIMIT) {
			return;
		}
		
		Locale locale = i18nManager.getLocaleOrDefault(assignee.getUser().getPreferences().getLanguage());
		MailBundle bundle = createBundle(assignee, locale, List.of(new Entry(doer, toDoTask, toDoProvider)));
		if (bundle != null) {
			MailerResult result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.debug("To-do {} (key::{}) assigned email sent to {}.", toDoTask.getTitle(), toDoTask.getKey(),
						assignee);
			} else {
				log.warn("Sending to-do {} (key::{}) assigned email to {} failed!", toDoTask.getTitle(),
						toDoTask.getKey(), assignee);
			}
		}
	}

	/**
	 * Sends one digest mail per recipient. A recipient with exactly one entry gets the
	 * existing single mail instead of the digest. Sends asynchronously, the caller has
	 * already committed the to-dos of the batch.
	 *
	 * @param batch The to-do assignments of one operation, grouped by recipient.
	 */
	public void sendAssignedEmails(ToDoAssignedMailBatch batch) {
		Map<Long, List<Entry>> recipientKeyToEntries = batch.getAssignments();
		if (recipientKeyToEntries.isEmpty()) {
			return;
		}
		
		List<Identity> recipients = securityManager.loadIdentityByKeys(recipientKeyToEntries.keySet());
		List<MailBundle> bundles = new ArrayList<>(recipients.size());
		for (Identity recipient : recipients) {
			if (recipient.getStatus() > Identity.STATUS_VISIBLE_LIMIT) {
				continue;
			}
			List<Entry> entries = recipientKeyToEntries.get(recipient.getKey());
			if (entries == null || entries.isEmpty()) {
				continue;
			}
			
			Locale locale = i18nManager.getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
			MailBundle bundle = createBundle(recipient, locale, entries);
			if (bundle != null) {
				bundles.add(bundle);
				log.debug("To-do assigned digest email ({} to-dos) queued for {}.", entries.size(), recipient);
			}
		}
		if (!bundles.isEmpty()) {
			mailManager.sendMessageAsync(bundles.toArray(new MailBundle[0]));
		}
	}
	
	public void sendDoneEmail(Identity doer, List<Identity> members, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		members.forEach(member -> this.sendDoneEmail(doer, member, toDoTask, toDoProvider));
	}
	
	public void sendDoneEmail(Identity doer, Identity member, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		if (member.getStatus() > Identity.STATUS_VISIBLE_LIMIT) {
			return;
		}
		
		Locale locale = i18nManager.getLocaleOrDefault(member.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		String subject = translator.translate("email.done.subject");
		String body = translator.translate("email.done.body.styled");
		Entry entry = new Entry(doer, toDoTask, toDoProvider);
		SingleToDoTemplate template = new SingleToDoTemplate(subject, body, translator,
				getDoerDisplayName(entry, locale), getTitle(translator, toDoTask), getUrl(entry));
		
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(null, member, template, doer, null, result);
		if (bundle != null) {
			result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.debug("To-do {} (key::{}) done email sent to {}.", toDoTask.getTitle(), toDoTask.getKey(), member);
			} else {
				log.warn("Sending to-do {} (key::{}) done email to {} failed!", toDoTask.getTitle(), toDoTask.getKey(), member);
			}
		}
	}
	
	/**
	 * @param entries One entry: the existing single assignment mail. Several entries: the digest.
	 */
	private MailBundle createBundle(Identity recipient, Locale locale, List<Entry> entries) {
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		MailerResult result = new MailerResult();
		Entry first = entries.get(0);
		String doerDisplayName = getDoerDisplayName(first, locale);
		
		if (entries.size() == 1) {
			String subject = translator.translate("email.assigned.subject");
			String body = translator.translate("email.assigned.body.styled");
			SingleToDoTemplate template = new SingleToDoTemplate(subject, body, translator, doerDisplayName,
					getTitle(translator, first.toDoTask()), getUrl(first));
			return mailManager.makeMailBundle(null, recipient, template, first.doer(), null, result);
		}
		
		String subject = translator.translate("email.assigned.digest.subject");
		String body = translator.translate("email.assigned.digest.body.styled");
		String rows = renderRows(translator, entries);
		AssignedDigestTemplate template = new AssignedDigestTemplate(subject, body, translator, doerDisplayName,
				entries.size(), rows);
		return mailManager.makeMailBundle(null, recipient, template, first.doer(), null, result);
	}
	
	private String renderRows(Translator translator, List<Entry> entries) {
		StringBuilder rows = new StringBuilder();
		int shown = Math.min(entries.size(), MAX_DIGEST_ROWS);
		for (Entry entry : entries.subList(0, shown)) {
			rows.append(translator.translate("email.assigned.digest.row", getTitle(translator, entry.toDoTask()), getUrl(entry)));
		}
		if (entries.size() > shown) {
			rows.append(translator.translate("email.assigned.digest.more", String.valueOf(entries.size() - shown)));
		}
		return rows.toString();
	}
	
	private String getDoerDisplayName(Entry entry, Locale locale) {
		return entry.doer() != null
				? userManager.getUserDisplayName(entry.doer())
				: entry.provider().getModifiedBy(locale, entry.toDoTask());
	}
	
	private static String getTitle(Translator translator, ToDoTask toDoTask) {
		return StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(translator, toDoTask));
	}
	
	private static String getUrl(Entry entry) {
		String businessPath = entry.provider().getBusinessPath(entry.toDoTask());
		List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		return BusinessControlFactory.getInstance().getAsURIString(ces, true);
	}
	
	private abstract static class ToDoTaskTemplate extends MailTemplate {
		
		private final Translator translator;
		private final String doerDisplayName;

		public ToDoTaskTemplate(String subjectTemplate, String bodyTemplate, Translator translator, String doerDisplayName) {
			super(subjectTemplate, bodyTemplate, null);
			this.translator = translator;
			this.doerDisplayName = doerDisplayName;
		}

		@Override
		public void putVariablesInMailContext(Identity recipient) {
			fillContextWithStandardIdentityValues(recipient, translator.getLocale());
			putVariablesInMailContext("doerDisplayName", StringHelper.escapeHtml(doerDisplayName));
		}
	}

	private static final class SingleToDoTemplate extends ToDoTaskTemplate {
		
		private final String toDoTitle;
		private final String contextUrl;
		
		public SingleToDoTemplate(String subjectTemplate, String bodyTemplate, Translator translator,
				String doerDisplayName, String toDoTitle, String contextUrl) {
			super(subjectTemplate, bodyTemplate, translator, doerDisplayName);
			this.toDoTitle = toDoTitle;
			this.contextUrl = contextUrl;
		}
		
		@Override
		public void putVariablesInMailContext(Identity recipient) {
			super.putVariablesInMailContext(recipient);
			putVariablesInMailContext("toDoTitle", toDoTitle);
			putVariablesInMailContext("contextUrl", contextUrl);
		}
	}
	
	private static final class AssignedDigestTemplate extends ToDoTaskTemplate {
		
		private final int toDoCount;
		private final String toDoRows;
		
		public AssignedDigestTemplate(String subjectTemplate, String bodyTemplate, Translator translator,
				String doerDisplayName, int toDoCount, String toDoRows) {
			super(subjectTemplate, bodyTemplate, translator, doerDisplayName);
			this.toDoCount = toDoCount;
			this.toDoRows = toDoRows;
		}
		
		@Override
		public void putVariablesInMailContext(Identity recipient) {
			super.putVariablesInMailContext(recipient);
			putVariablesInMailContext("toDoCount", String.valueOf(toDoCount));
			putVariablesInMailContext("toDoRows", toDoRows);
		}
	}
	
}
