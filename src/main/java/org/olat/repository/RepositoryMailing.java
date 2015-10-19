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
package org.olat.repository;

import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.group.ui.main.MemberPermissionChangeEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryMailing {
	
	public static Type getDefaultTemplateType(MemberPermissionChangeEvent event) {
		if(event != null && event.size() == 1) {
			if(event.getRepoTutor() != null && event.getRepoTutor().booleanValue()) {
				return Type.addTutor;
			} else if(event.getRepoParticipant() != null && event.getRepoParticipant().booleanValue()) {
				return Type.addParticipant;
			}
		}
		return null;
	}
	
	/**
	 * The mail template when adding users to a group.
	 * 
	 * @param re
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate createAddParticipantMailTemplate(RepositoryEntry re, Identity actor) {
		String subjectKey = "notification.mail.added.subject";
		String bodyKey = "notification.mail.added.body";
		return createMailTemplate(re, actor, subjectKey, bodyKey);
	}
	
	/**
	 * The mail template when adding tutors to a group.
	 * 
	 * @param re
	 * @param actor
	 * @return the generated MailTemplate
	 */
	private static MailTemplate createAddTutorMailTemplate(RepositoryEntry re, Identity actor) {
		String subjectKey = "notification.mail.added.subject";
		String bodyKey = "notification.mail.added.body";
		return createMailTemplate(re, actor, subjectKey, bodyKey);
	}
	
	/**
	 * The mail template when removing users from a repository entry.
	 * 
	 * @param re
	 * @param actor
	 * @return the generated MailTemplate
	 */
	private static MailTemplate createRemoveParticipantMailTemplate(RepositoryEntry re, Identity actor) {
		String subjectKey = "notification.mail.removed.subject";
		String bodyKey = "notification.mail.removed.body";
		return createMailTemplate(re, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getDefaultTemplate(Type type, RepositoryEntry re, Identity ureqIdentity) {
		if(type == null) return null;
		
		switch(type) {
			case addParticipant:
				return createAddParticipantMailTemplate(re, ureqIdentity);
			case removeParticipant:
				return createRemoveParticipantMailTemplate(re, ureqIdentity);
			case addTutor:
				return createAddTutorMailTemplate(re, ureqIdentity);
		}
		return null;
	}

	protected static void sendEmail(Identity ureqIdentity, Identity identity, RepositoryEntry re,
			Type type, MailPackage mailing) {
		
		if(mailing != null && !mailing.isSendEmail()) {
			return;
		}
		
		if(mailing == null) {
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			RepositoryModule repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
			Roles ureqRoles = securityManager.getRoles(ureqIdentity);
			if(!repositoryModule.isMandatoryEnrolmentEmail(ureqRoles)) {
				return;
			}
		}

		MailTemplate template = mailing == null ? null : mailing.getTemplate();
		if(mailing == null || mailing.getTemplate() == null) {
			template = getDefaultTemplate(type, re, ureqIdentity);
		}
		
		MailContext context = mailing == null ? null : mailing.getContext();
		if(context == null) {
			context = new MailContextImpl(null, null, "[RepositoryEntry:" + re.getKey() + "]");
		}

		String metaId = mailing == null ? null : mailing.getUuid();
		MailerResult result = new MailerResult();
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailManager.makeMailBundle(context, identity, template, ureqIdentity, metaId, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		if(mailing != null) {
			mailing.appendResult(result);
		}
	}

	public enum Type {
		addParticipant,
		removeParticipant,
		addTutor,
	}
	
	private static MailTemplate createMailTemplate(RepositoryEntry re, Identity actor, String subjectKey, String bodyKey) {
		// build learning resources as list of url as string
		final String reName = re.getDisplayname();
		final String redescription = (StringHelper.containsNonWhitespace(re.getDescription()) ? FilterFactory.getHtmlTagAndDescapingFilter().filter(re.getDescription()) : ""); 

		// get some data about the actor and fetch the translated subject / body via i18n module
		String[] bodyArgs = new String[] {
				actor.getUser().getProperty(UserConstants.FIRSTNAME, null),
				actor.getUser().getProperty(UserConstants.LASTNAME, null),
				actor.getUser().getProperty(UserConstants.EMAIL, null),
				actor.getUser().getProperty(UserConstants.EMAIL, null)// 2x for compatibility with old i18m properties
			};
		
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
		Translator trans = Util.createPackageTranslator(RepositoryManager.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey, bodyArgs);
		
		// create a mail template which all these data
		MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put user variables into velocity context
				User user = identity.getUser();
				context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				context.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
				context.put("login",  user.getProperty(UserConstants.EMAIL, null));
				// Put variables from greater context
				context.put("coursename", reName);
				context.put("coursedescription", redescription);
			}
		};
		return mailTempl;
	}
}