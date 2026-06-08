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
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.ApplicationFieldType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailVariablesController extends BasicController {
	
	private final VelocityContainer mainVC;

	@Autowired
	private RecruitingService selectusService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private OrganisationModule organisationModule;
	
	public MailVariablesController(UserRequest ureq, WindowControl wControl, Position position,
			boolean application, boolean applicationList, boolean applicantDashboardUrl,
			Reference reference, ApplicationsFeedbackConfiguration feedbackConfiguration, 
			boolean headOfCommittee, boolean secretary, boolean committeeMember) {
		super(ureq, wControl);
		OrganisationUnit organisationSettings = selectusService.getOrganisationUnit(position);
		
		mainVC = createVelocityContainer("mail_variables");
		List<VariablesCollection> variablesCollections = new ArrayList<>();
		initPositionAttributes(variablesCollections, position, organisationSettings);
		if(organisationModule.isEnabled() && position.getOrganisation() != null) {
			OrganisationUnit organisationUnit = selectusService.getOrganisationUnit(position);
			initOrganisationUnit(variablesCollections, position.getOrganisation(), organisationUnit);
		}
		
		applicantDashboardUrl = applicantDashboardUrl && recruitingModule.isReferenceApplicantManagement()
				&& position != null && position.isApplicantRefereeManagementEnabled();
		
		if(application || applicationList || applicantDashboardUrl) {
			initApplication(variablesCollections, position, application, applicationList, applicantDashboardUrl);
		}
		
		if(application && recruitingModule.isApplicationProjectEnabled() && position.isApplicationProject()) {
			initProject(variablesCollections);
		}

		initReferences(variablesCollections, (reference != null), position.isRefereeRecommendationEnabled());
		
		if(feedbackConfiguration != null) {
			initFeedbackConfiguration(variablesCollections);
		}
		initCommittee(variablesCollections, headOfCommittee, secretary, committeeMember);
		mainVC.contextPut("variablesCollections", variablesCollections);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void initPositionAttributes(List<VariablesCollection> variablesCollections, Position position, OrganisationUnit organisationSettings) {
		VariablesCollection positionVariables = new VariablesCollection("explain.position.variables");
		positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_TITLE, translate("explain.position.title")));
		positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_SHORT_TITLE, translate("explain.position.short.title")));
		
		Locale[] positionLanguages = recruitingModule.getPositionLocales();
		if(positionLanguages.length > 1) {
			String availableLanguages = position.getAvailableLanguages();
			if(StringHelper.containsNonWhitespace(availableLanguages) && availableLanguages.split(",").length > 1) {
				positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_TITLE_DE, translate("explain.position.title.de")));
				positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_TITLE_EN, translate("explain.position.title.en")));
			}
		}
		
		if(recruitingModule.isProfessorshipTypeEnabled()) {
			positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_PROFESSORSHIP, translate("explain.position.professorship")));
		}
		
		positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_APPLICATION_DEADLINE, translate("explain.position.application.deadline")));
		positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_MAIL_SIGNATURE, translate("explain.position.mail.signature")));
		if(recruitingModule.isMailProPositionEnabled() && StringHelper.containsNonWhitespace(recruitingModule.getStaffMail(position, organisationSettings))) {
			positionVariables.add(new Variable(RecruitingMailTemplate.POSITION_MAIL, translate("explain.position.mail")));
		}
		
		if(position.getRatingDeadline() != null) {
			positionVariables.add(new Variable(RecruitingMailTemplate.RATING_DEADLINE, translate("explain.position.rating.deadline")));
			positionVariables.add(new Variable(RecruitingMailTemplate.RATING_DEADLINE_DE, translate("explain.position.rating.deadline.de")));
			positionVariables.add(new Variable(RecruitingMailTemplate.RATING_DEADLINE_DAYS, translate("explain.position.rating.deadline.days")));
		}

		variablesCollections.add(positionVariables);
	}
	
	private void initOrganisationUnit(List<VariablesCollection> variablesCollections, Organisation organisation, OrganisationUnit settings) {
		VariablesCollection organisationVariables = new VariablesCollection("explain.organisation.variables");
		organisationVariables.add(new Variable(RecruitingMailTemplate.ORG_UNIT_TITLE, translate("explain.organisation.title")));
		
		if(settings != null && StringHelper.containsNonWhitespace(settings.getMailSignature())) {
			organisationVariables.add(new Variable(RecruitingMailTemplate.ORG_UNIT_SIGNATURE, translate("explain.organisation.signature")));
		}
		if(StringHelper.containsNonWhitespace(recruitingModule.getStaffMail(settings))) {
			organisationVariables.add(new Variable(RecruitingMailTemplate.ORG_UNIT_MAIL, translate("explain.organisation.mail")));
		}
		if(settings != null && StringHelper.containsNonWhitespace(settings.getUrl())) {
			organisationVariables.add(new Variable(RecruitingMailTemplate.ORG_UNIT_URL, translate("explain.organisation.url")));
		}
		variablesCollections.add(organisationVariables);
	}
	
	private void initCommittee(List<VariablesCollection> variablesCollections, boolean withHead, boolean withSecretary, boolean withMember) {
		// head
		VariablesCollection committeeVariables = new VariablesCollection("explain.committee.variables");
		if(withHead) {
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_FIRST_NAME, translate("explain.committee.head.firstName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_LAST_NAME, translate("explain.committee.head.lastName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_TITLE, translate("explain.committee.head.title")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_PHONE_OFFICE, translate("explain.committee.head.phone.office")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_PHONE_PRIVATE, translate("explain.committee.head.phone.private")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_PHONE_MOBILE, translate("explain.committee.head.phone.mobile")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.HEAD_MAIL, translate("explain.committee.head.mail")));
		}
		if(withSecretary) {
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_FIRST_NAME, translate("explain.committee.secretary.firstName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_LAST_NAME, translate("explain.committee.secretary.lastName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_TITLE, translate("explain.committee.secretary.title")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_PHONE_OFFICE, translate("explain.committee.secretary.phone.office")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_PHONE_PRIVATE, translate("explain.committee.secretary.phone.private")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_PHONE_MOBILE, translate("explain.committee.secretary.phone.mobile")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.SECRETARY_MAIL, translate("explain.committee.secretary.mail")));
		}
		
		if(withMember) {
			committeeVariables.add(new Variable(RecruitingMailTemplate.COMMITTEE_MEMBER_FIRST_NAME, translate("explain.committee.firstName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.COMMITTEE_MEMBER_LAST_NAME, translate("explain.committee.lastName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.COMMITTEE_MEMBER_TITLE, translate("explain.committee.title")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.COMMITTEE_MEMBER_TITLE_FIRST_LAST_NAME, translate("explain.committee.title.firstName.lastName")));
			committeeVariables.add(new Variable(RecruitingMailTemplate.COMMITTEE_MEMBER_DEAR_TITLE_NAME, translate("explain.committee.title.lastName")));
		}
		
		if(withHead || withSecretary || withMember) {
			variablesCollections.add(committeeVariables);
		}
	}
	
	private void initReferences(List<VariablesCollection> variablesCollections, boolean reference, boolean referenceEnabled) {
		VariablesCollection refereeVariables = new VariablesCollection("explain.referee.variables");
		if(reference) {
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_FIRST_NAME, translate("explain.referee.firstName")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_LAST_NAME, translate("explain.referee.lastName")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_TITLE, translate("explain.referee.title")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_EMAIL, translate("explain.referee.mail")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_INSTITUTION, translate("explain.referee.institution")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_TITLE_LAST_NAME, translate("explain.referee.title.lastName")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_TITLE_FIRST_LAST_NAME, translate("explain.refere.title.firstName.latName")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_DEAR_TITLE_NAME, translate("explain.referee.dear.title.name")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFERENCE_DEADLINE, translate("explain.referee.deadline")));
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFERENCE_URL, translate("explain.referee.url")));
		}
		
		if(referenceEnabled) {
			refereeVariables.add(new Variable(RecruitingMailTemplate.REFEREE_MIN_REQUIRED, translate("explain.referee.min.required")));
		}

		variablesCollections.add(refereeVariables);
	}
	
	private void initApplication(List<VariablesCollection> variablesCollections, Position position,
			boolean application, boolean applicationList, boolean applicantDashboardUrl) {
		VariablesCollection applicationVariables = new VariablesCollection("explain.application.variables");
		
		if(application) {
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_FIRST_NAME, translate("explain.application.firstName")));
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_LAST_NAME, translate("explain.application.lastName")));
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_TITLE, translate("explain.application.title")));
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_ORGANISATION, translate("explain.application.organisation")));
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_TITLE_FIRST_LAST_NAME, translate("explain.application.title.firstName.lastName")));
			
			List<Locale> locales = recruitingModule.getPositionLocales(position);
			if(locales.size() == 1 && locales.contains(Locale.ENGLISH)) {
				applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_DEAR_TITLE_NAME, translate("explain.application.dear.title.name.en")));
			} else if(locales.size() == 1 && locales.contains(Locale.GERMAN)) {
				applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_DEAR_TITLE_NAME, translate("explain.application.dear.title.name.de")));
			} else {
				applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_DEAR_TITLE_NAME, translate("explain.application.dear.title.name")));
			}
		}
		
		if(applicationList) {
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICATION_LIST, translate("explain.application.list")));
		}
		
		if(applicantDashboardUrl) {
			applicationVariables.add(new Variable(RecruitingMailTemplate.APPLICANT_URL, translate("explain.application.dashboard.url")));
		}

		variablesCollections.add(applicationVariables);
	}
	
	private void initProject(List<VariablesCollection> variablesCollections) {
		VariablesCollection projectVariables = new VariablesCollection("explain.project.variables");
		
		if(recruitingModule.isApplicationProjectTitleEnabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_TITLE, translate("explain.project.title")));
		}
		
		if(recruitingModule.isApplicationProjectFinancialImpact1Enabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_1, translate("explain.project.financial.impact.1")));
		}
		if(recruitingModule.isApplicationProjectFinancialImpact2Enabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_2, translate("explain.project.financial.impact.2")));
		}
		if(recruitingModule.isApplicationProjectFinancialImpact3Enabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_3, translate("explain.project.financial.impact.3")));
		}
		if(recruitingModule.isApplicationProjectFinancialImpact4Enabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_4, translate("explain.project.financial.impact.4")));
		}
		if(recruitingModule.isApplicationProjectFinancialImpact5Enabled()) {
			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_5, translate("explain.project.financial.impact.5")));
		}

		if((recruitingModule.getApplicationProjectFinancialImpact1Type().isEnabled()
				&& recruitingModule.getApplicationProjectFinancialImpact1Type().getType() == ApplicationFieldType.Type.sum)
				|| (recruitingModule.getApplicationProjectFinancialImpact2Type().isEnabled()
						&& recruitingModule.getApplicationProjectFinancialImpact2Type().getType() == ApplicationFieldType.Type.sum)
				|| (recruitingModule.getApplicationProjectFinancialImpact3Type().isEnabled()
						&& recruitingModule.getApplicationProjectFinancialImpact3Type().getType() == ApplicationFieldType.Type.sum)
				|| (recruitingModule.getApplicationProjectFinancialImpact4Type().isEnabled()
						&& recruitingModule.getApplicationProjectFinancialImpact4Type().getType() == ApplicationFieldType.Type.sum)
				|| (recruitingModule.getApplicationProjectFinancialImpact5Type().isEnabled()
						&& recruitingModule.getApplicationProjectFinancialImpact5Type().getType() == ApplicationFieldType.Type.sum)) {

			projectVariables.add(new Variable(RecruitingMailTemplate.PROJECT_FINANCIAL_IMPACT_SUM, translate("explain.project.financial.impact.sum")));
		}
		
		variablesCollections.add(projectVariables);
	}
	
	private void initFeedbackConfiguration(List<VariablesCollection> variablesCollections) {
		VariablesCollection feedbackVariables = new VariablesCollection("explain.applications.feedback.variables");

		feedbackVariables.add(new Variable(RecruitingMailTemplate.FACULTY_MEMBER_FIRST_NAME, translate("explain.applications.feedback.firstName")));
		feedbackVariables.add(new Variable(RecruitingMailTemplate.FACULTY_MEMBER_LAST_NAME, translate("explain.applications.feedback.lastName")));
		feedbackVariables.add(new Variable(RecruitingMailTemplate.FACULTY_MEMBER_TITLE, translate("explain.applications.feedback.title")));
		feedbackVariables.add(new Variable(RecruitingMailTemplate.FACULTY_MEMBER_TITLE_FIRST_LAST_NAME, translate("explain.applications.feedback.title.firstName.lastName")));
		feedbackVariables.add(new Variable(RecruitingMailTemplate.FACULTY_MEMBER_DEAR_TITLE_NAME, translate("explain.applications.feedback..dear.title.name")));
		
		feedbackVariables.add(new Variable(RecruitingMailTemplate.FEEDBACK_DEADLINE, translate("explain.applications.feedback.deadline")));
		
		variablesCollections.add(feedbackVariables);
	}
	
	public class Variable {
		
		private final String name;
		private final String explanation;
		
		public Variable(String name, String explanation) {
			this.name = name;
			this.explanation = explanation;
		}

		public String getName() {
			return name;
		}

		public String getExplanation() {
			return explanation;
		}
	}
	
	public class VariablesCollection {
		
		private final String i18nKey;
		private List<Variable> variables = new ArrayList<>();
		
		public VariablesCollection(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String getI18nKey() {
			return i18nKey;
		}
		
		public List<Variable> getVariables() {
			return variables;
		}
		
		public void add(Variable variable) {
			variables.add(variable);
		}
		
	}
}
