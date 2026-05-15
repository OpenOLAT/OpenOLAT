/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.PersonName;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RefereesStepController extends StepFormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private int counter = 0;
	private TextElement expertsBlackListEl;
	private MultipleSelectionElement consentEl;
	private List<RefereeWrapper> referees = new ArrayList<>();
	
	private Position position;
	private RefereeList reviewers;
	private Application application;
	private final TabConfiguration tabConfiguration;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public RefereesStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		application = (Application)getFromRunContext(WizardConstants.APPLICATION);
		reviewers = (RefereeList)getFromRunContext(WizardConstants.REVIEWERS);
		position = application.getPosition();
		tabConfiguration = position.getTabConfiguration(Tab.referees);
		initForm(ureq);
	}
	
	public RefereesStepController(UserRequest ureq, WindowControl wControl, Position position, Application application, TabConfiguration tabConfiguration) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.position = position;
		this.application = application;
		this.tabConfiguration = tabConfiguration;
		reviewers = new RefereeList();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.referees.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		
		if(reviewers == null) {
			reviewers = new RefereeList();
			addToRunContext(WizardConstants.REVIEWERS, reviewers);
		}
		
		if(position != null && position.isRefereeRecommendationEnabled()) {
			initFormDescription();

			PersonTitle[] personTitles = recruitingModule.getReferencePersonTitles();
			String[] titleKeys = new String[personTitles.length + 1];
			String[] titleValues = new String[personTitles.length + 1];
			titleKeys[0] = "";
			titleValues[0] = "-";
			for(int i=personTitles.length; i-->0; ) {
				titleKeys[i+1] = personTitles[i].title();
				titleValues[i+1] = translate(personTitles[i].i18nKey());
			}
			
			long min = position.getMinRefereesAsLong();
			long max = position.getMaxRefereesAsLong();
			for(int i=0; i<Math.max(min, max); i++) {
				boolean mandatory = i < min;
				
				Referee reviewer = null;
				if(reviewers.getReferees().size() > i) {
					reviewer = reviewers.getReferees().get(i);
				}

				if(i != 0) {
					uifactory.addSpacerElement("spacer" + i, formLayout, false);
				}
				StaticTextElement nameEl = uifactory.addStaticTextElement("title." + (++counter), "reviewer.title", "", formLayout);
				nameEl.setLabel("reviewer.title", new String[] { Integer.toString(i + 1)});

				String fullNameCount = Integer.toString(++counter);
				SingleSelection titleEl = uifactory.addDropdownSingleselect("referee.title." + fullNameCount, "reviewer.fullname", formLayout, titleKeys, titleValues, null);
				titleEl.setDomReplacementWrapperRequired(false);
				titleEl.setMandatory(mandatory);
				String title = reviewer == null ? null : reviewer.getTitle();
				if(StringHelper.containsNonWhitespace(title)) {
					for(String key: titleKeys) {
						if(key.equals(title)) {
							titleEl.select(key , true);
						}
					}
				}
	
				String firstName = reviewer == null ? "" : reviewer.getFirstName();
				TextElement firstNameEl = uifactory.addTextElement("referee.firstname." + fullNameCount, "edit.application.firstName", 255, firstName, formLayout);
				firstNameEl.setDomReplacementWrapperRequired(false);
				firstNameEl.setMandatory(mandatory);
				String lastName = reviewer == null ? "" : reviewer.getLastName();
				TextElement lastNameEl = uifactory.addTextElement("referee.lastname." + fullNameCount, "edit.application.lastName", 255, lastName, formLayout);
				lastNameEl.setDomReplacementWrapperRequired(false);
				lastNameEl.setMandatory(mandatory);
				
				String institution = reviewer == null ? "" : reviewer.getInstitution();
				TextElement institutionEl = uifactory.addTextElement("referee.institution" + (++counter), "reviewer.institution", 255, institution, formLayout);
				institutionEl.setMandatory(mandatory);
				String email = reviewer == null ? "" : reviewer.getEmail();
				TextElement emailEl = uifactory.addTextElement("referee.email" + (++counter), "reviewer.email", 255, email, formLayout);
				emailEl.setMandatory(mandatory);
				referees.add(new RefereeWrapper(mandatory, titleEl, firstNameEl, lastNameEl, institutionEl, emailEl));
			}
		} else {
			reviewers.getReferees().clear();
		}
		
		if(position != null && position.isExpertRecommendationEnabled() && recruitingModule.isReferenceExpertsBlackListEnabled()) {
			String explanation = translate("expert.blacklist.explanation");
			uifactory.addStaticTextElement("blacklist", "expert.blacklist", explanation, formLayout);
			expertsBlackListEl = uifactory.addTextAreaElement("blacklist.field", null, 3600, 4, 60, false, false, false, null, formLayout);
		}
		
		if(recruitingModule.isReferenceConsentEnabled()) {
			String i18nKey;
			if(position.isExpertRecommendationEnabled() && position.isRefereeRecommendationEnabled()) {
				i18nKey = "consent.reference.contact.on.referees.experts";
			} else if(position.isExpertRecommendationEnabled()) {
				i18nKey = "consent.reference.contact.on.experts";
			} else if(position.isRefereeRecommendationEnabled()) {
				i18nKey = "consent.reference.contact.on.referees";
			} else {
				i18nKey = "consent.reference.contact.on.experts";
			}
			String[] onValues = new String[] { translate(i18nKey) };
			consentEl = uifactory.addCheckboxesHorizontal("consent.reference.contact", "consent.reference.contact", formLayout, onKeys, onValues);
		}
	}
	
	private void initFormDescription() {
		String explanation = null;
		String help = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(help)) {
			explanation = help;
		} else {
			if(position.getRefereeRecommandationSendMailType() == ReferenceSendMailType.auto) {
				explanation = translate("wizard.referees.explain.auto");
			} else {
				explanation = translate("wizard.referees.explain.staff");
			}
		}
		
		if(recruitingModule.isReferenceApplicantManagement() && position.isApplicantRefereeManagementEnabled()) {
			String additionalHelp = tabConfiguration.getAdditionalHelp(getLocale());
			if(StringHelper.containsNonWhitespace(additionalHelp)) {
				explanation = "<p>" + explanation + "</p><p>" + additionalHelp + "</p>";
			} else {
				explanation = "<p>" + explanation + "</p><p>" + translate("wizard.referees.explain.additional") + "</p>";
			}
		}
		setFormTranslatedDescription(StringHelper.xssScan(RecruitingHelper.escWithBR(explanation)));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(RefereeWrapper referee:referees) {
			boolean mandatory = referee.isMandatory();
			if(!mandatory) {
				if(StringHelper.containsNonWhitespace(referee.getFirstNameEl().getValue())
						|| StringHelper.containsNonWhitespace(referee.getLastNameEl().getValue())
						|| StringHelper.containsNonWhitespace(referee.getEmailEl().getValue())
						|| StringHelper.containsNonWhitespace(referee.getInstitutionEl().getValue())) {
					mandatory = true;//the three fields need to be filled if one is filled
				}
			}
			allOk &= RecruitingHelper.validateTextElement(referee.getLastNameEl(), 255, mandatory, new OWASPAntiSamyXSSFilter());
			allOk &= RecruitingHelper.validateTextElement(referee.getFirstNameEl(), 255, mandatory, new OWASPAntiSamyXSSFilter());
			allOk &= RecruitingHelper.validateEmailElement(referee.getEmailEl(), 255, mandatory, new OWASPAntiSamyXSSFilter());
			allOk &= RecruitingHelper.validateTextElement(referee.getInstitutionEl(), 255, mandatory, new OWASPAntiSamyXSSFilter());
		}
		
		if(consentEl != null) {
			consentEl.clearError();
			if(!consentEl.isAtLeastSelected(1)) {
				consentEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		application = (Application)getFromRunContext(WizardConstants.APPLICATION);
		application = commitChanges(application);
		addToRunContext(WizardConstants.APPLICATION, application);
		logAudit("Apply reviewers: " + application.toString(), null);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private Application commitChanges(Application app) {
		if(consentEl != null) {
			app.setExpertConsent(consentEl.isAtLeastSelected(1));
		}
		if(expertsBlackListEl != null) {
			app.setExpertBlackList(expertsBlackListEl.getValue());
		}
		
		RefereeList reviewers = (RefereeList)getFromRunContext(WizardConstants.REVIEWERS);
		if(reviewers == null) {
			reviewers = new RefereeList();
			addToRunContext(WizardConstants.REVIEWERS, reviewers);
		}
		
		reviewers.getReferees().clear();
		for(RefereeWrapper referee:referees) {
			String title = referee.getTitle();
			String lastName = referee.getLastName();
			String firstName = referee.getFirstName();
			String institution = referee.getInstitutionEl().getValue();
			String email = referee.getEmailEl().getValue();
			String fullname = salutationGenerator.getTitleFullname(referee, getLocale());
			reviewers.getReferees().add(new Referee(title, firstName, lastName, fullname, institution, email));
		}
		return app;
	}
	
	public static class RefereeWrapper implements PersonName {
		
		private final boolean mandatory;

		private SingleSelection titleEl;
		private final TextElement firstNameEl, lastNameEl;
		private final TextElement institutionEl;
		private final TextElement emailEl;
		
		public RefereeWrapper(boolean mandatory, SingleSelection titleEl, TextElement firstNameEl, TextElement lastNameEl, TextElement institutionEl, TextElement emailEl) {
			this.mandatory = mandatory;
			this.titleEl = titleEl;
			this.lastNameEl = lastNameEl;
			this.firstNameEl = firstNameEl;
			this.institutionEl = institutionEl;
			this.emailEl = emailEl;
		}
		
		public boolean isMandatory() {
			return mandatory;
		}

		@Override
		public String getTitle() {
			return titleEl.isOneSelected() ? titleEl.getSelectedKey() : null;
		}

		@Override
		public String getFirstName() {
			return firstNameEl.getValue();
		}

		@Override
		public String getLastName() {
			return lastNameEl.getValue();
		}

		public SingleSelection getTitleEl() {
			return titleEl;
		}

		public void setTitleEl(SingleSelection titleEl) {
			this.titleEl = titleEl;
		}

		public TextElement getFirstNameEl() {
			return firstNameEl;
		}


		public TextElement getLastNameEl() {
			return lastNameEl;
		}

		public TextElement getInstitutionEl() {
			return institutionEl;
		}

		public TextElement getEmailEl() {
			return emailEl;
		}
	}
}
