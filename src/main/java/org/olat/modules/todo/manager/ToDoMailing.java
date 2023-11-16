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

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
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
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private I18nManager i18nManager;

	public void sendAssignedEmail(Identity doer, Identity assignee, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		Locale locale = i18nManager.getLocaleOrDefault(assignee.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		String subject = translator.translate("email.assigned.subject");
		String body = translator.translate("email.assigned.body.styled");
		ToDoTaskTemplate template = new ToDoTaskTemplate(subject, body, translator, doer, toDoTask, toDoProvider);
		
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(null, assignee, template, doer, null, result);
		if (bundle != null) {
			result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.debug("To-do {} (key::{}) assigned email sent to {}.", toDoTask.getTitle(), toDoTask.getKey(),
						assignee);
			} else {
				log.warn("Sending to-do {} (key::{}) assigned email to {} failed!", toDoTask.getTitle(),
						toDoTask.getKey(), assignee);
			}
		}
	}

	public void sendDoneEmail(Identity doer, List<Identity> members, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		members.forEach(member -> this.sendDoneEmail(doer, member, toDoTask, toDoProvider));
	}
	
	public void sendDoneEmail(Identity doer, Identity member, ToDoTask toDoTask, ToDoProvider toDoProvider) {
		Locale locale = i18nManager.getLocaleOrDefault(member.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		String subject = translator.translate("email.done.subject");
		String body = translator.translate("email.done.body.styled");
		ToDoTaskTemplate template = new ToDoTaskTemplate(subject, body, translator, doer, toDoTask, toDoProvider);
		
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
	
	private class ToDoTaskTemplate extends MailTemplate {
		
		private final Translator translator;
		private final Identity doer;
		private final ToDoTask toDoTask;
		private final ToDoProvider toDoProvider;

		public ToDoTaskTemplate(String subjectTemplate, String bodyTemplate, Translator translator, Identity doer,
				ToDoTask toDoTask, ToDoProvider toDoProvider) {
			super(subjectTemplate, bodyTemplate, null);
			this.translator = translator;
			this.doer = doer;
			this.toDoTask = toDoTask;
			this.toDoProvider = toDoProvider;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
			fillContextWithStandardIdentityValues(context, recipient, translator.getLocale());
			
			String userDisplayName = doer != null
					? userManager.getUserDisplayName(doer)
					: toDoProvider.getModifiedBy(translator.getLocale(), toDoTask);
			putVariablesInMailContext(context, "doerDisplayName", userDisplayName);
			
			putVariablesInMailContext(context, "toDoTitle", ToDoUIFactory.getDisplayName(translator, toDoTask));
			
			String url = null;
			// May happen in unit tests
			if (toDoProvider != null) {
				String businessPath = toDoProvider.getBusinessPath(toDoTask);
				List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
				url = BusinessControlFactory.getInstance().getAsURIString(ces, true);
			}
			putVariablesInMailContext(context, "contextUrl", url);
		}
	}

}
