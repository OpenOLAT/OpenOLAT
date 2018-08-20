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
package org.olat.modules.quality.manager;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.ui.QualityMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityMailing {

	private static final OLog log = Tracing.createLoggerFor(QualityMailing.class);
	
	private static final Collection<QualityDataCollectionStatus> STATUS_FILTER = Arrays.asList(
			QualityDataCollectionStatus.READY,
			QualityDataCollectionStatus.RUNNING,
			QualityDataCollectionStatus.FINISHED);
	
	@Autowired
	private QualityParticipationDAO participationDao;

	void sendMail(QualityReminder reminder, QualityReminder invitation, EvaluationFormParticipation participation) {
		if (participation.getExecutor() != null) {
			Identity executor = participation.getExecutor();
			MailTemplate template = createMailTemplate(reminder, invitation, executor);
			
			MailerResult result = new MailerResult();
			MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
			MailBundle bundle = mailManager.makeMailBundle(null, executor, template, null, null, result);
			if(bundle != null) {
				result = mailManager.sendMessage(bundle);
				if (result.isSuccessful()) {
					log.info(MessageFormat.format("{0} for quality data collection [key={1}] sent to {2}",
							reminder.getType().name(), reminder.getDataCollection().getKey(), executor));
				} else {
					log.warn(MessageFormat.format("Sending {0} for quality data collection [key={1}] to {2} failed: {3}",
							reminder.getType().name(), reminder.getDataCollection().getKey(), executor, result.getErrorMessage()));
				}
			}
		}
	}

	MailTemplate createMailTemplate(QualityReminder reminder, QualityReminder invitationReminder, Identity executor) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(executor.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);
		Formatter formatter = Formatter.getInstance(locale);
		
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(executor);
		searchParams.setDataCollectionRef(reminder.getDataCollection());
		searchParams.setDataCollectionStatus(STATUS_FILTER);
		List<QualityExecutorParticipation> participations = participationDao.loadExecutorParticipations(translator, searchParams , 0, -1);
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
		String invitation = invitationReminder != null && invitationReminder.getSendDone() != null
				? formatter.formatDateAndTime(invitationReminder.getSendDone())
				: "";
				
		String subject = translator.translate(reminder.getType().getSubjectI18nKey());
		String body = translator.translate(reminder.getType().getBodyI18nKey());

		Long participationKey = participation != null? participation.getParticipationRef().getKey(): null;
		String url = getUrl(participationKey);
		
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
				context.put("url", url);
				context.put("invitation", invitation);
			}
		};
		return mailTempl;
	}

	private String getUrl(Long participationKey) {
		StringBuilder url = new StringBuilder();
		url.append(Settings.getServerContextPathURI());
		url.append("/url/QualitySite/0/quality/0/my/0/");
		if (participationKey != null) {
			url.append("execution/");
			url.append(participationKey);
		}
		return url.toString();
	}
	
}
