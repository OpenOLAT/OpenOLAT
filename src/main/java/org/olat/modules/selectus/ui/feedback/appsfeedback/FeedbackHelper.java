/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import static org.olat.modules.selectus.manager.ApplicationMailTemplate.DEFAULT_TEMPLATE;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.feedback.ApplicationFeedbackImpl;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Initial date: 5 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackHelper {
	
	public static Identity generateDummyMember() {
		TransientIdentity id = new TransientIdentity();
		id.setProperty(UserConstants.FIRSTNAME, "Antoine");
		id.setProperty(UserConstants.LASTNAME, "Smith");
		id.setProperty(UserConstants.TITLE, "Dr.");
		id.setProperty(UserConstants.TELMOBILE, "0797654321");
		id.setProperty(UserConstants.TELOFFICE, "0067654321");
		id.setProperty(UserConstants.TELPRIVATE, "0077654321");
		id.setProperty(UserConstants.EMAIL, "antoine.smith@selectus.com");
		return id;
	}
	
	public static ApplicationFeedback generateDummyFeedback(ApplicationsFeedbackConfiguration feedbackConfig, Date deadline) {
		ApplicationFeedbackImpl ref = new ApplicationFeedbackImpl();
		
		TransientIdentity id = new TransientIdentity();
		id.setProperty(UserConstants.FIRSTNAME, "Louis");
		id.setProperty(UserConstants.LASTNAME, "de Broglie");
		id.setProperty(UserConstants.TITLE, "Dr.");
		id.setProperty(UserConstants.TELMOBILE, "0787654321");
		id.setProperty(UserConstants.TELOFFICE, "0047654321");
		id.setProperty(UserConstants.TELPRIVATE, "0057654321");
		id.setProperty(UserConstants.EMAIL, "louis.de.broglie@frentix.com");
		ref.setIdentity(id);
		
		ref.setConfiguration(feedbackConfig);
		
		if(deadline != null) {
			ref.setDeadline(deadline);
		} else if(feedbackConfig != null && feedbackConfig.getDeadline() != null) {
			ref.setDeadline(feedbackConfig.getDeadline());
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);
			ref.setDeadline(cal.getTime());
		}
		return ref;
	}
	
	public static String[] generateMailArguments(Identity headOfCommittee, Position position, Application application,
			ApplicationsFeedbackConfiguration feedbackConfig, Identity member,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		OrganisationUnit organisationSettings = CoreSpringFactory.getImpl(RecruitingService.class).getOrganisationUnit(position);
		String staffMail = CoreSpringFactory.getImpl(RecruitingModule.class).getStaffMail(position, organisationSettings);
		String serverUrl = Settings.getServerContextPathURI();
		Locale locale = translator.getLocale();

		String headLastname = "";
		String headFirstname = "";
		if(headOfCommittee != null ) {
			headLastname = headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, locale);
			headFirstname = headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, locale);
		}
		
		String fullName = null;
		String titleLastName = null;
		if(member != null) {
			fullName = RecruitingHelper.formatFullName(member);
			titleLastName = RecruitingHelper.formatFullNameWithTitle(member, locale);
		}
		
		String dearApplicatantTitleAndName = "";
		String applicantTitleLastName = "";
		String applicationTitleFullname = "";
		if(application != null) {
			dearApplicatantTitleAndName = salutationGenerator.getSalutation(application, locale);
			applicantTitleLastName = salutationGenerator.getTitleLastName(application, locale);
			applicationTitleFullname = salutationGenerator.getTitleFullname(application, locale);
		}
		
		String feedbackDeadline = "";
		if(feedbackConfig != null && feedbackConfig.getDeadline() != null) {
			feedbackDeadline = DateCellRenderer.format(feedbackConfig.getDeadline());
		}

		return new String[]{
				position.getMLTitle(locale), 	// 0
				staffMail,						// 1
				headLastname,					// 2
				headFirstname,					// 3
				serverUrl,						// 4
				fullName,						// 5
				titleLastName,					// 6
				dearApplicatantTitleAndName,	// 7
				applicantTitleLastName,			// 8
				applicationTitleFullname,		// 9
				feedbackDeadline				// 10
		};
	}
	
	public static String getEmail(Identity member) {
		String email = member.getUser().getProperty(UserConstants.EMAIL, null);
		if(!StringHelper.containsNonWhitespace(email)) {
			email = member.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		}
		return email;
	}
	
	public static String getDefaultTemplateSubject(Position position, SalutationGenerator salutationGenerator, Locale locale) {
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		String[] args = generateMailArguments(null, position, null, null, null, salutationGenerator, translator);
		return translator.translate("apps.feedback.mail.subject", args);
	}
	
	public static String getDefaultTemplateBody(Position position, SalutationGenerator salutationGenerator, Locale locale) {
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		String[] args = generateMailArguments(null, position, null, null, null, salutationGenerator, translator);
		return translator.translate("apps.feedback.mail.body", args);
	}
	
	public static String getDefaultTemplateBodyHtml(Position position, SalutationGenerator salutationGenerator, Locale locale) {
		String body = getDefaultTemplateBody(position, salutationGenerator, locale);
		if(!StringHelper.isHtml(body)) {
			body = Formatter.escWithBR(body).toString();
		}
		return body;
	}
	
	private static SubjectAndBody feedbackTemplateBase(Identity headOfCommittee, Position position, List<? extends ApplicationShort> applicationList,
			Identity member, ApplicationsFeedbackConfiguration feedbackConfig,
			SalutationGenerator salutationGenerator, Translator translator) {
		
		Application application = applicationList != null && applicationList.size() == 1 && applicationList.get(0) instanceof Application
				? (Application)applicationList.get(0) : null;
		String[] args = generateMailArguments(headOfCommittee, position, application, feedbackConfig, member, salutationGenerator, translator);

		String subject = feedbackConfig.getMailSubject();
		if(!RecruitingHelper.containsTemplate(subject)) {
			subject = translator.translate("apps.feedback.mail.subject", args); 
		}
		String body = feedbackConfig.getMailTemplate();
		if(!RecruitingHelper.containsTemplate(body)) {
			body = translator.translate("apps.feedback.mail.body", args);
		}
		MailAttachment letter = CoreSpringFactory.getImpl(MailService.class)
				.toAttachment(feedbackConfig.getMailLetter(), application, translator.getLocale());
		return new SubjectAndBody(subject, body, letter);
	}
	
	public static RecruitingMailTemplate feedbackTemplate(Identity headOfCommittee, Identity secretary,
			Position position, List<? extends ApplicationShort> appList, Identity member, List<ApplicationFeedback> feedbacks,
			ApplicationsFeedbackConfiguration feedbackConfig, SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService erFrontendManager = CoreSpringFactory.getImpl(RecruitingService.class);
		SubjectAndBody subjectAndBody = feedbackTemplateBase(headOfCommittee, position, appList,
				member, feedbackConfig, salutationGenerator, translator);
		RecruitingMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, translator);
		
		ApplicationShort app = appList != null && appList.size() == 1 ? appList.get(0) : null;
		
		MailerResult mailerResult = new MailerResult(); 
		SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
				.createWithContext(app, appList, null, member, feedbacks, null, position, template, mailerResult);
		if(mailerResult.isSuccessful()) {
			template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
					subjectAndBody2.getSubject(), subjectAndBody2.getBody(), subjectAndBody2.getLetter(),
					headOfCommittee, secretary, subjectAndBody2, salutationGenerator, translator);
		}
		return template;
	}
}
