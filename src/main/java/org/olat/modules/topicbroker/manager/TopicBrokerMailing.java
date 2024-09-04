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
package org.olat.modules.topicbroker.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerMailing {
	
	private static final Logger log = Tracing.createLoggerFor(TopicBrokerMailing.class);
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private I18nManager i18nManager;

	public void sendEnrollmentEmail(Identity recipient, TBBroker broker, TBParticipant participant,
			List<TBSelection> enrolledSelections, RepositoryEntry courseEntry, CourseNode courseNode) {
		Locale locale = i18nManager.getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(TBUIFactory.class, locale);
		
		String subject;
		String body;
		if (enrolledSelections == null || enrolledSelections.isEmpty()) {
			subject = translator.translate("email.enrollment.unsuccessfull.subject");
			body = translator.translate("email.enrollment.unsuccessfull.body");
		} else if (TBUIFactory.getRequiredEnrollments(broker, participant) == enrolledSelections.size()) {
			subject = translator.translate("email.enrollment.successfull.subject");
			if (broker.isParticipantCanWithdraw() && broker.getWithdrawEndDate() != null) {
				body = translator.translate("email.enrollment.successfull.body.withdraw");
			} else {
				body = translator.translate("email.enrollment.successfull.body");
			}
		} else {
			subject = translator.translate("email.enrollment.partially.subject");
			if (broker.isParticipantCanWithdraw() && broker.getWithdrawEndDate() != null) {
				body = translator.translate("email.enrollment.partially.body.withdraw");
			} else {
				body = translator.translate("email.enrollment.partially.body");
			}
		}
		
		TBEnrollmentTemplate template = new TBEnrollmentTemplate(subject, body, translator, broker, enrolledSelections, courseEntry, courseNode);
		
		MailContext context = new MailContextImpl("[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]");
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, recipient, template, null, null, result);
		if (bundle != null) {
			result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.debug("Topic broker (key::{}) enrollment email sent to {}.", broker.getKey(), recipient);
			} else {
				log.warn("Sending topic broker (key::{}) enrollment email to {} failed!", broker.getKey(), recipient);
			}
		}
	}
	
	private class TBEnrollmentTemplate extends MailTemplate {
		
		private final Translator translator;
		private final TBBroker broker;
		private List<TBSelection> enrolledSelections;
		private final RepositoryEntry courseEntry;
		private final CourseNode courseNode;
		
		public TBEnrollmentTemplate(String subjectTemplate, String bodyTemplate, Translator translator, TBBroker broker,
				List<TBSelection> enrolledSelections, RepositoryEntry courseEntry, CourseNode courseNode) {
			super(subjectTemplate, bodyTemplate, null);
			this.translator = translator;
			this.broker = broker;
			this.enrolledSelections = enrolledSelections;
			this.courseEntry = courseEntry;
			this.courseNode = courseNode;
		}
		
		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
			fillContextWithStandardIdentityValues(context, recipient, translator.getLocale());
			putVariablesInMailContext(context, "recipientDisplayName", userManager.getUserDisplayName(recipient));
			
			String withdrawDeadline = Formatter.getInstance(translator.getLocale()).formatDate(broker.getWithdrawEndDate());
			putVariablesInMailContext(context, "withdrawDeadline", withdrawDeadline);
			
			String enrollmentList = "";
			if (enrolledSelections != null && !enrolledSelections.isEmpty()) {
				enrolledSelections = new ArrayList<>(enrolledSelections);
				enrolledSelections.sort((s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
				for (TBSelection selection: enrolledSelections) {
					enrollmentList += "- " +  StringHelper.escapeHtml(selection.getTopic().getTitle()) + "<br>";
				}
			}
			putVariablesInMailContext(context, "enrollmentList", enrollmentList);
			
			putVariablesInMailContext(context, "courseTitle", StringHelper.escapeHtml(courseEntry.getDisplayname()));
			putVariablesInMailContext(context, "courseTitleSubject", courseEntry.getDisplayname());
			putVariablesInMailContext(context, "courseElementTitle", StringHelper.escapeHtml(courseNode.getLongTitle()));
			putVariablesInMailContext(context, "courseElementTitleSubject", courseNode.getLongTitle());
			
			String businessPath = "[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			putVariablesInMailContext(context, "url", url);
		}
	}

}
