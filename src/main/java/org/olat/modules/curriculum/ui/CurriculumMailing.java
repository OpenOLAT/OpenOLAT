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
package org.olat.modules.curriculum.ui;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
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
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 7 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMailing {

	/**
	 * The mail template when adding owner to a course.
	 * 
	 * @param re
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate getDefaultMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.added.subject";
		String bodyKey = "notification.mail.added.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getRemoveMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.removed.subject";
		String bodyKey = "notification.mail.removed.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	private static MailTemplate createMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement,
			Identity actor, String subjectKey, String bodyKey) {
		// build learning resources as list of url as string
		final String curriculumName = curriculum.getDisplayName();
		final String curriculumDescription = (StringHelper.containsNonWhitespace(curriculum.getDescription())
				? FilterFactory.getHtmlTagAndDescapingFilter().filter(curriculum.getDescription()) : ""); 
		final String curriculumUrl = Settings.getServerContextPathURI() + "/url/MyCoursesSite/0/Curriculum/0/Curriculum/" + curriculum.getKey();
		
		final String curriculumElementName;
		final String curriculumElementDescription;
		final String curriculumElementIdentifier;
		if(curriculumElement == null) {
			curriculumElementName = "";
			curriculumElementDescription = "";
			curriculumElementIdentifier = "";
		} else {
			curriculumElementName = curriculumElement.getDisplayName();
			curriculumElementDescription = (StringHelper.containsNonWhitespace(curriculumElement.getDescription())
					? FilterFactory.getHtmlTagAndDescapingFilter().filter(curriculumElement.getDescription()) : ""); 
			curriculumElementIdentifier = curriculumElement.getIdentifier();
		}
		
		// get some data about the actor and fetch the translated subject / body via i18n module
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
		String[] bodyArgs = new String[] {
				actor.getUser().getProperty(UserConstants.FIRSTNAME, null),		// 0
				actor.getUser().getProperty(UserConstants.LASTNAME, null),		// 1
				UserManager.getInstance().getUserDisplayEmail(actor, locale),	// 2
				UserManager.getInstance().getUserDisplayEmail(actor, locale),	// 3 (2x for compatibility with old i18m properties)
				Formatter.getInstance(locale).formatDate(new Date())			// 4
			};
		
		Translator trans = Util.createPackageTranslator(CurriculumMailing.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey, bodyArgs);
		
		// create a mail template which all these data
		return new MailTemplate(subject, body, null) {
			
			private static final String LOGIN = "login";
			private static final String CURRICULUM_NAME = "curriculumName";
			private static final String CURRICULUM_DESCRIPTION = "curriculumDescription";
			private static final String CURRICULUM_URL = "curriculumUrl";
			private static final String CURRICULUM_ELEMENT_NAME = "curriculumElementName";
			private static final String CURRICULUM_ELEMENT_DESCRIPTION = "curriculumElementDescription";
			private static final String CURRICULUM_ELEMENT_IDENTIFIER = "curriculumElementIdentifier";
			
			@Override
			public Collection<String> getVariableNames() {
				Set<String> variableNames = new HashSet<>();
				variableNames.addAll(getStandardIdentityVariableNames());
				variableNames.add(LOGIN);
				variableNames.add(CURRICULUM_NAME);
				variableNames.add(CURRICULUM_DESCRIPTION);
				variableNames.add(CURRICULUM_URL);
				variableNames.add(CURRICULUM_ELEMENT_NAME);
				variableNames.add(CURRICULUM_ELEMENT_DESCRIPTION);
				variableNames.add(CURRICULUM_ELEMENT_IDENTIFIER);
				return variableNames;
			}

			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put user variables into velocity context
				fillContextWithStandardIdentityValues(context, identity, locale);
				User user = identity.getUser();
				context.put(LOGIN, UserManager.getInstance().getUserDisplayEmail(user, locale));
				// Put variables from greater context
				context.put(CURRICULUM_NAME, curriculumName);
				context.put("curriculumname", curriculumName);
				context.put(CURRICULUM_DESCRIPTION, curriculumDescription);
				context.put("curriculumdescription", curriculumDescription);
				context.put(CURRICULUM_URL, curriculumUrl);
				context.put("curriculumurl", curriculumUrl);
				
				context.put(CURRICULUM_ELEMENT_NAME, curriculumElementName);
				context.put("curriculumelementname", curriculumElementName);
				context.put(CURRICULUM_ELEMENT_DESCRIPTION, curriculumElementDescription);
				context.put("curriculumelementdescription", curriculumElementDescription);
				context.put(CURRICULUM_ELEMENT_IDENTIFIER, curriculumElementIdentifier);
				context.put("curriculumelementidentifier", curriculumElementIdentifier);
			}
		};
	}
	
	public static void sendEmail(Identity ureqIdentity, Identity identity, Curriculum curriculum, CurriculumElement curriculumElement, MailPackage mailing) {
		if(mailing == null || !mailing.isSendEmail()) {
			return;
		}

		MailTemplate template = mailing.getTemplate();
		if(template == null) {
			template = getDefaultMailTemplate(curriculum, curriculumElement, ureqIdentity);
		}
		
		MailContext context = mailing.getContext();
		if(context == null) {
			context = new MailContextImpl(null, null, "[MyCoursesSite:0][Curriculum:0][Curriculum:" + curriculum.getKey() + "]");
		}
		
		MailerResult result = new MailerResult();
		String metaId = mailing.getUuid();
		MailManager mailService = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailService.makeMailBundle(context, identity, template, ureqIdentity, metaId, result);
		if(bundle != null) {
			mailService.sendMessage(bundle);
		}
		mailing.appendResult(result);
	}
}
