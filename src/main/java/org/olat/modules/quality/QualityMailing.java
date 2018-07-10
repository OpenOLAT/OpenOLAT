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
package org.olat.modules.quality;

import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityMailing {
	
	private static MailTemplate createMailTemplate(QualityReminder reminder, Identity executor) {
		
		QualityService qualityService = CoreSpringFactory.getImpl(QualityService.class);
		
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(executor.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
		Formatter formatter = Formatter.getInstance(locale);
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(executor);
		searchParams.setDataCollectionRef(reminder.getDataCollection());
		List<QualityExecutorParticipation> participations = qualityService.loadExecutorParticipations(translator, searchParams , 0, -1);
		QualityExecutorParticipation participation = null;
		if (!participations.isEmpty()) {
			participation = participations.get(0);
		}
		
		User user = executor.getUser();
		String firstname = user.getProperty(UserConstants.FIRSTNAME, null);
		String lastname = user.getProperty(UserConstants.LASTNAME, null);
		
		String start = participation != null && participation.getStart() != null
				? formatter.formatDateAndTime(participation.getStart())
				: "";
		String deadline = participation != null && participation.getDeadline() != null
				? formatter.formatDateAndTime(participation.getDeadline())
				: "";
		String topictype = participation != null && participation.getTranslatedTopicType() != null
				? participation.getTranslatedTopicType()
				: "";
		String topic = participation != null && participation.getTopic() != null
				? participation.getTopic()
				: "";
		String title = participation != null && participation.getTitle() != null
				? participation.getTitle()
				: "";
				
		String subject = translator.translate(reminder.getType().getSubjectI18nKey());
		String body = translator.translate(reminder.getType().getBodyI18nKey());

		StringBuilder url = new StringBuilder();
		url.append(Settings.getServerContextPathURI());
		url.append("/QualitySite/0/my/0/");
		if (participation != null) {
			url.append("execution/");
			url.append(participation.getParticipationRef().getKey());
		}
		
		MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				context.put("firstname", firstname);
				context.put("lastname", lastname);
				context.put("start", start);
				context.put("deadline", deadline);
				context.put("topictype", topictype);
				context.put("topic", topic);
				context.put("title", title);
				context.put("url", url.toString());
			}
		};
		return mailTempl;
	}

}
