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
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionProfessorship;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * This is a standard template used across the recruiting tool
 * to standardize the name of the variables.
 * 
 * 
 * Initial date: 19.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingMailTemplate extends ApplicationMailTemplate {

	public static final String POSITION_TITLE = "positionTitle";
	public static final String POSITION_SHORT_TITLE = "positionShortTitle";
	public static final String POSITION_TITLE_DE = "positionTitleDe";
	public static final String POSITION_TITLE_EN = "positionTitleEn";
	public static final String POSITION_TITLE_FR = "positionTitleFr";
	public static final String POSITION_PROFESSORSHIP = "professorship";
	public static final String POSITION_MAIL_SIGNATURE = "mailSignature";
	public static final String POSITION_APPLICATION_DEADLINE = "applicationDeadline";
	public static final String POSITION_APPLICATION_DEADLINE_DE = "applicationDeadlineDe";
	public static final String POSITION_APPLICATION_DEADLINE_FR = "applicationDeadlineFr";
	public static final String POSITION_MAIL = "positionMail";
	
	public static final String RATING_DEADLINE = "ratingDeadline";
	public static final String RATING_DEADLINE_DE = "ratingDeadlineDe";
	public static final String RATING_DEADLINE_DAYS = "ratingDeadlineDays";

	public static final String ORG_UNIT_TITLE = "orgUnit";
	public static final String ORG_UNIT_MAIL = "orgUnitMail";
	public static final String ORG_UNIT_SIGNATURE = "orgUnitMailSignature";
	public static final String ORG_UNIT_URL = "orgUnitURL";
	
	public static final String HEAD_FIRST_NAME = "headFirstName";
	public static final String HEAD_LAST_NAME = "headLastName";
	public static final String HEAD_TITLE = "headTitle";
	public static final String HEAD_TITLE_EN = "headTitleEn";
	public static final String HEAD_TITLE_DE = "headTitleDe";
	public static final String HEAD_PHONE_OFFICE = "headPhoneOffice";
	public static final String HEAD_PHONE_PRIVATE = "headPhonePrivate";
	public static final String HEAD_PHONE_MOBILE = "headPhoneMobile";
	public static final String HEAD_MAIL = "headMail";
	
	public static final String SECRETARY_FIRST_NAME = "secretaryFirstName";
	public static final String SECRETARY_LAST_NAME = "secretaryLastName";
	public static final String SECRETARY_TITLE = "secretaryTitle";
	public static final String SECRETARY_PHONE_OFFICE = "secretaryPhoneOffice";
	public static final String SECRETARY_PHONE_PRIVATE = "secretaryPhonePrivate";
	public static final String SECRETARY_PHONE_MOBILE = "secretaryPhoneMobile";
	public static final String SECRETARY_MAIL = "secretaryMail";
	
	public static final String REFEREE_FIRST_NAME = "refereeFirstName";
	public static final String REFEREE_LAST_NAME = "refereeLastName";
	public static final String REFEREE_TITLE = "refereeTitle";
	public static final String REFEREE_EMAIL = "refereeEmail";
	public static final String REFEREE_INSTITUTION = "refereeInstitution";
	public static final String REFEREE_TITLE_LAST_NAME = "refereeTitleLastName";
	public static final String REFEREE_TITLE_FIRST_LAST_NAME = "refereeTitleFirstLastName";
	public static final String REFEREE_DEAR_TITLE_NAME = "refereeDearTitleName";
	public static final String REFERENCE_DEADLINE = "referenceDeadline";
	public static final String REFERENCE_URL = "referenceURL";
	public static final String REFEREE_MIN_REQUIRED = "refereeMinRequired";
	
	public static final String FACULTY_MEMBER_FIRST_NAME = "facultyFirstName";
	public static final String FACULTY_MEMBER_LAST_NAME = "facultyLastName";
	public static final String FACULTY_MEMBER_TITLE = "facultyTitle";
	public static final String FACULTY_MEMBER_TITLE_FIRST_LAST_NAME = "facultyTitleFirstLastName";
	public static final String FACULTY_MEMBER_DEAR_TITLE_NAME = "facultyDearTitleName";
	
	public static final String COMMITTEE_MEMBER_FIRST_NAME = "committeeFirstName";
	public static final String COMMITTEE_MEMBER_LAST_NAME = "committeeLastName";
	public static final String COMMITTEE_MEMBER_TITLE = "committeeTitle";
	public static final String COMMITTEE_MEMBER_TITLE_FIRST_LAST_NAME = "committeeTitleFirstLastName";
	public static final String COMMITTEE_MEMBER_DEAR_TITLE_NAME = "committeeDearTitleName";
	
	public static final String FEEDBACK_DEADLINE = "feedbackDeadline";
	
	public static final String APPLICATION_FIRST_NAME = "applicantFirstName";
	public static final String APPLICATION_LAST_NAME = "applicantLastName";
	public static final String APPLICATION_TITLE = "applicantTitle";
	public static final String APPLICATION_ORGANISATION = "applicantOrganisation";
	public static final String APPLICATION_TITLE_FIRST_LAST_NAME = "applicantTitleFirstLastName";
	public static final String APPLICATION_TITLE_FIRST_LAST_NAMES = "applicantTitleFirstLastNames";
	public static final String APPLICATION_DEAR_TITLE_NAME = "applicantDearTitleName";
	
	public static final String APPLICANT_URL = "applicantURL";
	
	public static final String APPLICATION_LIST = "applicantList";
	
	public static final String PROJECT_TITLE = "projectTitle";
	
	public static final String PROJECT_FINANCIAL_IMPACT_1 = "projectFinancialImpact1";
	public static final String PROJECT_FINANCIAL_IMPACT_2 = "projectFinancialImpact2";
	public static final String PROJECT_FINANCIAL_IMPACT_3 = "projectFinancialImpact3";
	public static final String PROJECT_FINANCIAL_IMPACT_4 = "projectFinancialImpact4";
	public static final String PROJECT_FINANCIAL_IMPACT_5 = "projectFinancialImpact5";
	public static final String PROJECT_FINANCIAL_IMPACT_SUM = "projectFinancialImpactSum";
	
	
	private final Translator translator;
	private final Identity secretary;
	private final Identity headOfCommittee;
	private final SubjectAndBody subjectAndBody;

	private final RecruitingModule recruitingModule;
	private final RecruitingService recruitingService;
	private final SalutationGenerator salutationGenerator;

	public RecruitingMailTemplate(Long key, String name, String label,
			String subjectTemplate, String bodyTemplate, MailAttachment letterTemplate,
			Identity headOfCommittee, Identity secretary, SubjectAndBody subjectAndBody,
			SalutationGenerator salutationGenerator, Translator translator) {
		super(key, name, label, subjectTemplate, bodyTemplate, letterTemplate, translator.getLocale());
		this.secretary = secretary;
		this.translator = translator;
		this.subjectAndBody = subjectAndBody;
		this.headOfCommittee = headOfCommittee;
		this.salutationGenerator = salutationGenerator;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
	}

	@Override
	public void putVariablesInMailContext(VelocityContext context, ApplicationShort app, List<? extends ApplicationShort> appList,
			Reference reference, Identity member, List<ApplicationFeedback> feedbacks, ApplicationsFeedbackConfiguration feedbackConfiguration, Position position) {
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		
		putVariables(context);
		putPositionVariables(context, position, organisationSettings);
		putOrganisation(context, organisationSettings);
		if(app != null) {
			putApplicationVariables(context, app);
			putProjectVariables(context, app.getProject());
		} else if(appList != null && appList.size() == 1) {
			ApplicationShort singleApp = appList.get(0);
			putApplicationVariables(context, singleApp);
			putProjectVariables(context, singleApp.getProject());
		}
		if(appList != null) {
			putApplicationListVariables(context, appList);
		} else if(app != null) {
			putApplicationListVariables(context, Collections.singletonList(app));
		}

		if(headOfCommittee != null) {
			putHeadOfCommitteeVariables(context);
		}
		if(secretary != null) {
			putSecretaryVariables(context);
		}
		
		if(reference != null) {
			putReferenceVariables(context, reference, position);	
		}
		if(feedbackConfiguration != null) {
			putApplicationsFeedbackConfiguration(context, feedbackConfiguration);
		}
		if(feedbacks != null ) {
			putApplicationFeedbacks(context, feedbacks);
		}
		if(member != null) {
			putApplicationFeedbackMember(context, member);
		}
	}
	
	private void putVariables(VelocityContext context) {
		String todayStr;
		if(getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
			Formatter formatterDe = Formatter.getInstance(Locale.GERMAN);
			todayStr = formatterDe.formatDateLong(new Date());
		} else {
			todayStr = DateCellRenderer.format(new Date());
		}
		context.put("today", todayStr);
	}
	
	private void putOrganisation(VelocityContext context, OrganisationUnit organisationSettings) {
		String mail = recruitingModule.getStaffMail(organisationSettings);
		context.put(ORG_UNIT_MAIL, nullToEmpty(mail));
		
		if(organisationSettings == null) {
			context.put(ORG_UNIT_TITLE, "");
			context.put(ORG_UNIT_SIGNATURE, "");
			context.put(ORG_UNIT_URL, "");
		} else {
			context.put(ORG_UNIT_TITLE, organisationSettings.getMLName(getLocale()));
			String mailSignature = "";
			if(StringHelper.containsNonWhitespace(organisationSettings.getMailSignature())) {
				mailSignature = organisationSettings.getMailSignature();
			}
			if(StringHelper.containsNonWhitespace(mailSignature) && subjectAndBody.isHtml()) {
				mailSignature = Formatter.escWithBR(mailSignature).toString();
			}
			context.put(ORG_UNIT_SIGNATURE, mailSignature);
			context.put(ORG_UNIT_URL, nullToEmpty(organisationSettings.getUrl()));
		}
	}
	
	private void putPositionVariables(VelocityContext context, Position position, OrganisationUnit organisationSettings) {
		context.put("position", position);
		context.put(POSITION_TITLE, position.getMLTitle(getLocale()));
		context.put(POSITION_SHORT_TITLE, position.getShortTitle(getLocale()));
		
		context.put(POSITION_TITLE_DE, position.getMLTitle(Locale.GERMAN));
		context.put(POSITION_TITLE_EN, position.getMLTitle(Locale.ENGLISH));
		context.put(POSITION_TITLE_FR, position.getMLTitle(Locale.FRENCH));
		
		PositionProfessorship professorship;
		if(StringHelper.containsNonWhitespace(position.getProfessorship())) {
			professorship = PositionProfessorship.valueOf(position.getProfessorship());
		} else {
			professorship = PositionProfessorship.any;
		}
		
		String translatedProfessorship;
		switch(professorship) {
			case any: translatedProfessorship = translator.translate("email.professorship.any"); break;
			case full: translatedProfessorship = translator.translate("email.professorship.full"); break;
			case assistant: translatedProfessorship = translator.translate("email.professorship.assistant"); break;
			default: translatedProfessorship = translator.translate("email.professorship.any");
		}
		context.put(POSITION_PROFESSORSHIP, translatedProfessorship);
		
		if(position.getOrganisation() != null && organisationSettings != null) {
			String mailSignature = "";
			if(StringHelper.containsNonWhitespace(organisationSettings.getMailSignature())) {
				mailSignature = organisationSettings.getMailSignature();
			} else {
				mailSignature = translator.translate("email.signature");
			}
			
			if(StringHelper.containsNonWhitespace(mailSignature) && subjectAndBody.isHtml()) {
				mailSignature = Formatter.escWithBR(mailSignature).toString();
			}
			context.put(POSITION_MAIL_SIGNATURE, mailSignature);
		} else {
			context.put(POSITION_MAIL_SIGNATURE, "");
		}
		
		if(position.getApplicationDeadline() != null) {
			Formatter formatterDe = Formatter.getInstance(Locale.GERMAN);
			Formatter formatterFr = Formatter.getInstance(Locale.FRENCH);
			Date deadline = position.getApplicationDeadline();
			String deadlineStr;
			String deadlineDeStr = formatterDe.formatDateLong(deadline);
			String deadlineFrStr = formatterFr.formatDateLong(deadline);
			if(getLocale().getLanguage().equals(Locale.GERMAN.getLanguage())) {
				deadlineStr = formatterDe.formatDateLong(deadline);
			} else if(getLocale().getLanguage().equals(Locale.FRENCH.getLanguage())) {
				deadlineStr = formatterFr.formatDateLong(deadline);
			} else {
				deadlineStr = DateCellRenderer.format(deadline);
			}
			context.put(POSITION_APPLICATION_DEADLINE, deadlineStr);
			context.put(POSITION_APPLICATION_DEADLINE_DE, deadlineDeStr);
			context.put(POSITION_APPLICATION_DEADLINE_FR, deadlineFrStr);
		} else {
			context.put(POSITION_APPLICATION_DEADLINE, "");
			context.put(POSITION_APPLICATION_DEADLINE_DE, "");
			context.put(POSITION_APPLICATION_DEADLINE_FR, "");
		}

		String mail = recruitingModule.getStaffMail(position, organisationSettings);
		context.put(POSITION_MAIL, nullToEmpty(mail));
		
		long days;
		String ratingDeadline = "";
		String ratingDeadlineDe = "";
		if(position.getRatingDeadline() != null) {
			long diffInMillies = position.getRatingDeadline().getTime() - new Date().getTime();
			days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			ratingDeadline = DateCellRenderer.format(position.getRatingDeadline());
			ratingDeadlineDe = DateCellRenderer.format(position.getRatingDeadline(), Locale.GERMAN);
		} else {
			days = 2l;
		}
		context.put(RATING_DEADLINE, ratingDeadline);
		context.put(RATING_DEADLINE_DE, ratingDeadlineDe);
		context.put(RATING_DEADLINE_DAYS, Long.toString(days));
		
		if(position.isRefereeRecommendationEnabled() && position.getMinReferees() != null) {
			context.put(REFEREE_MIN_REQUIRED, position.getMinReferees().toString());
		}
	}
	
	private void putApplicationVariables(VelocityContext context, ApplicationShort app) {
		context.put("application", app);
		context.put("firstname", app.getPerson().getFirstName());
		context.put("lastname", app.getPerson().getLastName());

		context.put(APPLICATION_FIRST_NAME, app.getPerson().getFirstName());
		context.put("applicantFirstname", app.getPerson().getFirstName());
		context.put(APPLICATION_LAST_NAME, app.getPerson().getLastName());
		context.put("applicantLastname", app.getPerson().getLastName());
		context.put(APPLICATION_ORGANISATION, app.getBusinessInformations().getOrganization());
		
		String mail = app.getPerson().getMail();
		if(StringHelper.containsNonWhitespace(mail)) {
			context.put("applicantMail", mail);
			context.put("applicantEmail", mail);
		}
		
		//title and co.
		if (recruitingModule.isApplicationPersonTitleEnabled()) {
			String title = StringHelper.containsNonWhitespace(app.getPerson().getTitle())
					? translator.translate(app.getPerson().getTitle()) : "";
			context.put("title", title);
			context.put(APPLICATION_TITLE, title);
		}
	
		String titleAndName = salutationGenerator.getTitleFullname(app, translator.getLocale());
		context.put("titleAndName", titleAndName);
		context.put("titleName", titleAndName);
		context.put("nameAndTitle", titleAndName);
		context.put("nameTitle", titleAndName);
		context.put("applicantFullname", titleAndName);
		context.put("applicantFullName", titleAndName);
		context.put("applicantTitleFullName", titleAndName);
		
		String dearTitleAndName = salutationGenerator.getSalutation(app, translator.getLocale());
		context.put("dearTitleAndName", dearTitleAndName);
		context.put("dearTitleName", dearTitleAndName);
		context.put("applicantDearTitleFullName", dearTitleAndName);
		context.put(APPLICATION_DEAR_TITLE_NAME, dearTitleAndName);

		String titleLastName = salutationGenerator.getTitleLastName(app, translator.getLocale());
		context.put("titleLastName", titleLastName);
		context.put("titleLastname", titleLastName);
		context.put("applicantTitleLastname", titleLastName);
		context.put("applicantTitleLastName", titleLastName);
		
		String titleFirstLastName = salutationGenerator.getTitleFirstLastName(app, translator.getLocale());
		context.put("titleFirstLastName", titleFirstLastName);
		context.put("titleFirstLastname", titleFirstLastName);
		context.put("applicantTitleFirstLastname", titleFirstLastName);
		context.put("applicantTitleFirstLastName", titleFirstLastName);
		context.put(APPLICATION_TITLE_FIRST_LAST_NAME, titleFirstLastName);
		// same as above but with a trailing s
		String titleFirstLastNames = StringHelper.containsNonWhitespace(titleFirstLastName) ? titleFirstLastName + "s" : "";
		context.put("applicantTitleFirstLastnames", titleFirstLastNames);
		context.put("applicantTitleFirstLastNames", titleFirstLastNames);
		context.put(APPLICATION_TITLE_FIRST_LAST_NAMES, titleFirstLastNames);
		
		String url = app.getApplicantUrl();
		if(StringHelper.containsNonWhitespace(url)) {	
			String wUrl =  RecruitingHelper.getLinkToRefereeDashboard(app);
			String htmlLink = "<a href='" + wUrl + "'>" + wUrl + "</a>";
			context.put(APPLICANT_URL, htmlLink);
			context.put("applicantUrl", htmlLink);
			context.put("applicanturl", htmlLink);
		}
	}
	
	private void putApplicationListVariables(VelocityContext context, List<? extends ApplicationShort> appList) {
		if(appList.isEmpty()) {
			context.put(APPLICATION_LIST, "-");
			context.put("applicationList", "-");
			context.put("applicationsList", "-");
			context.put("applicationlist", "-");
			context.put("applicationslist", "-");
		} else {
			StringBuilder ulList = new StringBuilder();
			StringBuilder flatList = new StringBuilder();
			ulList.append("<ul>");
			
			if(appList.size() > 1) {
				appList = new ArrayList<>(appList);
				Collections.sort(appList, new LastnameComparator());
			}
			
			for(ApplicationShort app:appList) {
				String titleFirstLastName = salutationGenerator.getTitleFirstLastName(app, translator.getLocale());
				ulList.append("<li>").append(titleFirstLastName).append("</li>");
				
				if(flatList.length() > 0) {
					flatList.append(", ");
				}
				flatList.append(titleFirstLastName);
			}
			ulList.append("</ul>");
			context.put(APPLICATION_LIST, ulList.toString());
			context.put("applicationList", flatList.toString());
			context.put("applicationsList", flatList.toString());
			context.put("applicationlist", flatList.toString());
			context.put("applicationslist", flatList.toString());
		}
	}
	
	private void putProjectVariables(VelocityContext context, Project project) {
		if(project == null) return;
		
		context.put(PROJECT_TITLE, project.getTitle());
		context.put("projecttitle", project.getTitle());
		
		context.put(PROJECT_FINANCIAL_IMPACT_1, project.getFinancialImpact1());
		context.put(PROJECT_FINANCIAL_IMPACT_2, project.getFinancialImpact2());
		context.put(PROJECT_FINANCIAL_IMPACT_3, project.getFinancialImpact3());
		context.put(PROJECT_FINANCIAL_IMPACT_4, project.getFinancialImpact4());
		context.put(PROJECT_FINANCIAL_IMPACT_5, project.getFinancialImpact5());
		
		if(recruitingModule.getApplicationProjectFinancialImpact1Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact1Type().getType() == ApplicationFieldType.Type.sum) {
			context.put(PROJECT_FINANCIAL_IMPACT_SUM, project.getFinancialImpact1());
		} else if(recruitingModule.getApplicationProjectFinancialImpact2Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact2Type().getType() == ApplicationFieldType.Type.sum) {
			context.put(PROJECT_FINANCIAL_IMPACT_SUM, project.getFinancialImpact2());
		} else if(recruitingModule.getApplicationProjectFinancialImpact3Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact3Type().getType() == ApplicationFieldType.Type.sum) {
			context.put(PROJECT_FINANCIAL_IMPACT_SUM, project.getFinancialImpact3());
		} else if(recruitingModule.getApplicationProjectFinancialImpact4Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact4Type().getType() == ApplicationFieldType.Type.sum) {
			context.put(PROJECT_FINANCIAL_IMPACT_SUM, project.getFinancialImpact4());
		} else if(recruitingModule.getApplicationProjectFinancialImpact5Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact5Type().getType() == ApplicationFieldType.Type.sum) {
			context.put(PROJECT_FINANCIAL_IMPACT_SUM, project.getFinancialImpact5());
		}
	}

	private void putHeadOfCommitteeVariables(VelocityContext context) {
		context.put("headFirstname", nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, getLocale())));
		context.put("headLastname", nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, getLocale())));
		
		context.put(HEAD_FIRST_NAME, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, getLocale())));
		context.put(HEAD_LAST_NAME, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, getLocale())));
		
		String title = headOfCommittee.getUser().getProperty(UserConstants.TITLE, getLocale());
		putTitle(HEAD_TITLE, title, context);
		String titleEn = headOfCommittee.getUser().getProperty(UserConstants.TITLE, Locale.ENGLISH);
		putTitle(HEAD_TITLE_EN, titleEn, context);
		String titleDe = headOfCommittee.getUser().getProperty(UserConstants.TITLE, Locale.GERMAN);
		putTitle(HEAD_TITLE_DE, titleDe, context);
		
		context.put(HEAD_PHONE_OFFICE, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.TELOFFICE, getLocale())));
		context.put(HEAD_PHONE_PRIVATE, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.TELPRIVATE, getLocale())));
		context.put(HEAD_PHONE_MOBILE, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.TELMOBILE, getLocale())));
		context.put(HEAD_MAIL, nullToEmpty(headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale())));
		
		context.put("headFullname", nullToEmpty(RecruitingHelper.formatFullName(headOfCommittee)));
		context.put("headFullName", nullToEmpty(RecruitingHelper.formatFullName(headOfCommittee)));
		context.put("headTitleFullname", nullToEmpty(RecruitingHelper.formatFullNameWithTitle(headOfCommittee, getLocale())));
		context.put("headTitleFullName", nullToEmpty(RecruitingHelper.formatFullNameWithTitle(headOfCommittee, getLocale())));
	}
	
	private void putTitle(String variableName, String title, VelocityContext context) {
		if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
			context.put(variableName, title);
		} else {
			context.put(variableName, "");
		}
	}
	
	private void putSecretaryVariables(VelocityContext context) {
		context.put("secretaryFirstname", nullToEmpty(secretary.getUser().getProperty(UserConstants.FIRSTNAME, getLocale())));
		context.put("secretaryLastname", nullToEmpty(secretary.getUser().getProperty(UserConstants.LASTNAME, getLocale())));
		
		context.put(SECRETARY_FIRST_NAME, nullToEmpty(secretary.getUser().getProperty(UserConstants.FIRSTNAME, getLocale())));
		context.put(SECRETARY_LAST_NAME, nullToEmpty(secretary.getUser().getProperty(UserConstants.LASTNAME, getLocale())));
		
		String title = secretary.getUser().getProperty(UserConstants.TITLE, getLocale());
		if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
			context.put(SECRETARY_TITLE, title);
		} else {
			context.put(SECRETARY_TITLE, "");
		}
		
		context.put(SECRETARY_PHONE_OFFICE, nullToEmpty(secretary.getUser().getProperty(UserConstants.TELOFFICE, getLocale())));
		context.put(SECRETARY_PHONE_PRIVATE, nullToEmpty(secretary.getUser().getProperty(UserConstants.TELPRIVATE, getLocale())));
		context.put(SECRETARY_PHONE_MOBILE, nullToEmpty(secretary.getUser().getProperty(UserConstants.TELMOBILE, getLocale())));
		context.put(SECRETARY_MAIL, nullToEmpty(secretary.getUser().getProperty(UserConstants.EMAIL, getLocale())));
		
		context.put("secretaryFullname", nullToEmpty(RecruitingHelper.formatFullName(secretary)));
		context.put("secretaryFullName", nullToEmpty(RecruitingHelper.formatFullName(secretary)));
		context.put("secretaryTitleFullname", nullToEmpty(RecruitingHelper.formatFullNameWithTitle(secretary, getLocale())));
		context.put("secretaryTitleFullName", nullToEmpty(RecruitingHelper.formatFullNameWithTitle(secretary, getLocale())));
	}
	
	private void putReferenceVariables(VelocityContext context, Reference reference, Position position) {
		//referee
		context.put(REFEREE_FIRST_NAME, nullToEmpty(reference.getFirstName()));
		context.put(REFEREE_LAST_NAME, nullToEmpty(reference.getLastName()));
		context.put(REFEREE_TITLE, nullToEmpty(reference.getTitle()));
		context.put(REFEREE_EMAIL, nullToEmpty(reference.getEmail()));
		context.put(REFEREE_INSTITUTION, nullToEmpty(reference.getInstitution()));
		
		context.put("refereeTitleFullName", salutationGenerator.getTitleFullname(reference, getLocale()));
		context.put("refereeTitleFullname", salutationGenerator.getTitleFullname(reference, getLocale()));
		context.put("dearRefereeTitleFullname", salutationGenerator.getSalutation(reference, getLocale()));
		context.put("dearRefereeTitleFullname", salutationGenerator.getSalutation(reference, getLocale()));
		context.put(REFEREE_DEAR_TITLE_NAME, salutationGenerator.getSalutation(reference, getLocale()));
		context.put(REFEREE_TITLE_LAST_NAME, salutationGenerator.getTitleLastName(reference, getLocale()));
		context.put("refereeTitleLastname", salutationGenerator.getTitleLastName(reference, getLocale()));
		context.put("refereeTitleFirstLastname", salutationGenerator.getTitleFirstLastName(reference, getLocale()));
		context.put(REFEREE_TITLE_FIRST_LAST_NAME, salutationGenerator.getTitleFirstLastName(reference, getLocale()));
		
		context.put("refereeFullName", salutationGenerator.getFullname(reference, getLocale()));
		context.put("refereeFullname", salutationGenerator.getFullname(reference, getLocale()));
		context.put("refereeLastName", nullToEmpty(reference.getLastName()));

		//alias for expert
		context.put("expertTitleFullName", salutationGenerator.getTitleFullname(reference, getLocale()));
		context.put("expertTitleFullname", salutationGenerator.getTitleFullname(reference, getLocale()));
		context.put("expertDearTitleName", salutationGenerator.getSalutation(reference, getLocale()));
		context.put("dearExpertTitleFullName", salutationGenerator.getSalutation(reference, getLocale()));
		context.put("dearExpertTitleFullname", salutationGenerator.getSalutation(reference, getLocale()));
		
		context.put("expertFullName", salutationGenerator.getFullname(reference, getLocale()));
		context.put("expertFullname", salutationGenerator.getFullname(reference, getLocale()));
		context.put("expertEmail", nullToEmpty(reference.getEmail()));
		context.put("expertInstitution", nullToEmpty(reference.getInstitution()));
		
		
		Date deadline = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			deadline = position.getExpertRecommandationDeadline();
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			deadline = position.getRefereeRecommandationDeadline();
		}
		if(reference.getSubmissionDeadline() != null) {
			deadline = reference.getSubmissionDeadline();
		}
		String deadlineStr = "";
		String deadlineDeStr = "";
		if(deadline != null) {
			deadlineStr = DateCellRenderer.format(deadline);
			Formatter formatter = Formatter.getInstance(Locale.GERMAN);
			deadlineDeStr = formatter.formatDateLong(deadline);
		}
		context.put(REFERENCE_DEADLINE, deadlineStr);
		context.put("refereeDeadline", deadlineStr);
		context.put("expertDeadline", deadlineStr);
		
		context.put("referenceDeadlineDe", deadlineDeStr);
		context.put("refereeDeadlineDe", deadlineDeStr);
		context.put("expertDeadlineDe", deadlineDeStr);
		
		String link = RecruitingHelper.getLinkToReference(reference);
		String htmlLink = "<a href='" + link + "'>" + link + "</a>";
		context.put("refereeURL", htmlLink);
		context.put("refereeUrl", htmlLink);
		context.put("expertURL", htmlLink);
		context.put("expertUrl", htmlLink);
		context.put("referenceURL", htmlLink);//compatibility only
		context.put("referenceUrl", htmlLink);//compatibility only
	}
	
	private void putApplicationsFeedbackConfiguration(VelocityContext context, ApplicationsFeedbackConfiguration feedbackConfiguration) {
		if(feedbackConfiguration.getDeadline() != null) {
			putApplicationFeedbackDeadline(context, feedbackConfiguration.getDeadline());
		}
	}
	
	private void putApplicationFeedbacks(VelocityContext context, List<ApplicationFeedback> feedbacks) {
		if(feedbacks != null && feedbacks.size() == 1) {
			putApplicationFeedbackDeadline(context, feedbacks.get(0).getDeadline());
		} else {
			Date feedbackDeadline = null;
			for(ApplicationFeedback feedback:feedbacks) {
				if(feedbackDeadline == null || (feedback.getDeadline() != null && feedback.getDeadline().before(feedbackDeadline))) {
					feedbackDeadline = feedback.getDeadline();
				}
			}
			putApplicationFeedbackDeadline(context, feedbackDeadline);
		}
	}
	
	private void putApplicationFeedbackMember(VelocityContext context, Identity member) {
		User user = member.getUser();
		
		context.put(FACULTY_MEMBER_FIRST_NAME, user.getProperty(UserConstants.FIRSTNAME, getLocale()));
		context.put(FACULTY_MEMBER_LAST_NAME, user.getProperty(UserConstants.LASTNAME, getLocale()));
		
		String title = user.getProperty(UserConstants.TITLE, getLocale());
		if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
			context.put(FACULTY_MEMBER_TITLE, title);
		} else {
			context.put(FACULTY_MEMBER_TITLE, "");
		}

		context.put(FACULTY_MEMBER_TITLE_FIRST_LAST_NAME, RecruitingHelper.formatFullNameWithTitle(member, getLocale()));
		context.put(FACULTY_MEMBER_DEAR_TITLE_NAME, this.salutationGenerator.getSalutation(member, getLocale()));
	}
	
	private void putApplicationFeedbackDeadline(VelocityContext context, Date deadline) {
		String deadlineStr = "";
		String deadlineDeStr = "";

		if(deadline != null) {
			deadlineStr = DateCellRenderer.format(deadline);
			Formatter formatter = Formatter.getInstance(Locale.GERMAN);
			deadlineDeStr = formatter.formatDateLong(deadline);
		}
		
		context.put("feedbackDeadline", deadlineStr);
		context.put("feedbackDeadlineDe", deadlineDeStr);
	}
	
	private static String nullToEmpty(String txt) {
		return txt == null ? "" : txt;
	}
}