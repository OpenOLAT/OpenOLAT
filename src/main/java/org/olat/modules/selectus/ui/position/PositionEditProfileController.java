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
package org.olat.modules.selectus.ui.position;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingDuplicateApplicationAlgorithm;
import org.olat.modules.selectus.RecruitingDuplicateApplicationOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.MailSettingEnum;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.PolicyLink;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionImpl;
import org.olat.modules.selectus.model.PositionProfessorship;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.comparator.OrganisationComparator;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionEditProfileController extends FormBasicController implements PositionEditableController {

	private static final String ORG_UNIT_EMPTY_KEY = "no-org-unit";
	private static final String[] enabledKeys = new String[] { "on" };
	private static final int DESC_MAX_LENGTH = 15000;
	
	private MultipleSelectionElement availableLanguageEls;
	private TextElement idElement;
	private SingleSelection organisationEl;
	private List<TextElement> posTitleLanguagesEl = new ArrayList<>(2);
	private List<TextElement> shortTitleLanguagesEl = new ArrayList<>(2);
	private List<RichTextElement> descLanguagesEl = new ArrayList<>(2);
	
	private List<TextElement> departmentLanguagesEl = new ArrayList<>(2);
	private TextElement homepageElement;
	private MultipleSelectionElement professorshipElement;

	private FileElement doc1Element;
	private FileElement doc2Element;
	private FileElement doc3Element;
	private List<PolicyLinkWrapper> policyLinkWrappers = new ArrayList<>();
	
	private TextElement bccMailEl;
	private TextElement senderMailEl;
	private SingleSelection mailSettingEl;
	private RichTextElement messageToCommitteeEl;
	private StaticTextElement messageToCommitteeExplanationEl;
	private MultipleSelectionElement messageToCommitteeEnableEl;
	
	private SingleSelection duplicateApplicationEl;
	
	private Position position;
	private OrganisationUnit organisationSettings;
	private final boolean readOnly;
	private final boolean newPosition;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	private String[] statusKeys = new String[]{
			PositionStatus.preparation.name(),
			PositionStatus.published.name(),
			PositionStatus.publishedAndInScreening.name(),
			PositionStatus.closedAndInScreening.name(),
			PositionStatus.closedAndNoRating.name(),
			PositionStatus.closed.name()
	};
	private String[] statusValues = new String[statusKeys.length];
	
	private String[] professorshipKeys = new String[]{
			PositionProfessorship.assistant.name(),
			PositionProfessorship.full.name()
	};
	private String[] professorshipValues = new String[professorshipKeys.length];

	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private final Locale[] positionLanguages;
	private final String[] positionLanguagesKeys;
	private final String[] positionLanguagesValues;
	
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	public PositionEditProfileController(UserRequest ureq, WindowControl wControl, Position position, boolean newPosition,
			boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.newPosition = newPosition;
		this.readOnly = readOnly;
		organisationSettings = erFrontendManager.getOrganisationUnit(position);
		
		positionLanguages = recruitingModule.getPositionLocales();
		positionLanguagesKeys = new String[positionLanguages.length];
		for(int i=positionLanguages.length; i-->0; ) {
			positionLanguagesKeys[i] = positionLanguages[i].getLanguage();
			positionLanguageToLocale.put(positionLanguages[i].getLanguage(), positionLanguages[i]);
		}
		positionLanguagesValues = new String[positionLanguages.length];
		for(int i=positionLanguages.length; i-->0; ) {
			positionLanguagesValues[i] = positionLanguages[i].getDisplayLanguage(getLocale());
		}
		
		for(int i=statusValues.length; i-->0; ) {
			statusValues[i] = translate("status." + statusKeys[i]);
		}
		professorshipValues[0] = translate("professorship.assistant");
		professorshipValues[1] = translate("professorship.full");
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
		initForm(ureq);
		initMailSetting();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description");
		formLayout.setElementCssClass("o_sel_edit_position_form");
		
		UserSession usess = ureq.getUserSession();
		String availableLanguages = position.getAvailableLanguages();
		availableLanguageEls = uifactory.addCheckboxesHorizontal("position.languages", formLayout, positionLanguagesKeys, positionLanguagesValues);
		availableLanguageEls.setElementCssClass("o_sel_position_languages");
		availableLanguageEls.setVisible(positionLanguages.length > 1);
		availableLanguageEls.setEnabled(!readOnly);
		availableLanguageEls.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(availableLanguages)) {
			String[] availableLanguageArr = position.getAvailableLanguagesArray();
			for(int i=0; i<positionLanguagesKeys.length; i++) {
				for(int j=0; j<availableLanguageArr.length; j++) {
					if(positionLanguagesKeys[i].equals(availableLanguageArr[j])) {
						availableLanguageEls.select(positionLanguagesKeys[i], true);
					}
				}
			}
		} else {
			for(int i=0; i<positionLanguagesKeys.length; i++) {
				availableLanguageEls.select(positionLanguagesKeys[i], true);
			}
		}
		
		boolean focus = false;
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String posTitle = position.getPositionTitle(locale);
			TextElement posTitleEl = uifactory.addTextElement("position_title_" + lang, "edit.position_title", 256, posTitle, formLayout);
			posTitleEl.setMandatory(true);
			posTitleEl.setUserObject(locale);
			posTitleEl.setEnabled(!readOnly);
			if(!focus && !StringHelper.containsNonWhitespace(posTitle)) {
				posTitleEl.setFocus(focus);
				focus = true;
			}
			if(positionLanguages.length > 1) {
				posTitleEl.setLabel("edit.position_title_ml", new String[]{ lang });
				posTitleEl.setElementCssClass("o_sel_position_title_" + lang);
			} else {
				posTitleEl.setElementCssClass("o_sel_position_title");
			}
			posTitleLanguagesEl.add(posTitleEl);
		}

		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String shortTitle = position.getShortTitle(locale);
			TextElement shortTitleEl = uifactory.addTextElement("short_title_" + lang, "edit.short_title", 256, shortTitle, formLayout);
			shortTitleEl.setMandatory(true);
			shortTitleEl.setUserObject(locale);
			shortTitleEl.setEnabled(!readOnly);
			if(positionLanguages.length > 1) {
				shortTitleEl.setLabel("edit.short_title_ml", new String[]{ lang });
				shortTitleEl.setElementCssClass("o_sel_position_shorttitle_" + lang);
			} else {
				shortTitleEl.setElementCssClass("o_sel_position_shorttitle");
			}
			shortTitleLanguagesEl.add(shortTitleEl);
		}
		
		String planingsNumber = position.getPlaningsNumber();
		idElement = uifactory.addTextElement("position_id", "edit.position_id", 32, planingsNumber, formLayout);
		idElement.setMandatory(!recruitingModule.isPositionPlannigIdOptional());
		idElement.setVisible(recruitingModule.isPositionPlannigIdEnabled());
		idElement.setEnabled(!readOnly);
		idElement.setElementCssClass("o_sel_position_id");
		
		initOrganisation(usess, formLayout);

		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String department = position.getDepartment(locale);
			TextElement departmentEl = uifactory.addTextElement("department_" + lang, "edit.department", 256, department, formLayout);
			departmentEl.setMandatory(!recruitingModule.isPositionDepartmentOptional());
			departmentEl.setVisible(recruitingModule.isPositionDepartmentEnabled());
			departmentEl.setEnabled(!readOnly);
			departmentEl.setUserObject(locale);
			if(positionLanguages.length > 1) {
				departmentEl.setLabel("edit.department_ml", new String[]{ lang });
				departmentEl.setElementCssClass("o_sel_position_departement_" + lang);
			} else {
				departmentEl.setElementCssClass("o_sel_position_departement");
			}
			departmentLanguagesEl.add(departmentEl);
		}
		
		prefillDepartment();
		
		String homepage = position.getHomepage();
		homepageElement = uifactory.addTextElement("homepage", "edit.homepage", 256, homepage, formLayout);
		homepageElement.setMandatory(!recruitingModule.isPositionHomepageOptional());
		homepageElement.setVisible(recruitingModule.isPositionHomepageEnabled());
		homepageElement.setEnabled(!readOnly);
		homepageElement.setElementCssClass("o_sel_position_homepage");
		
		String link = RecruitingHelper.getLinkToPosition(position);
		StaticTextElement urlEl = uifactory.addStaticTextElement("ext_link", "edit.extern_link", link, formLayout);
		urlEl.setElementCssClass("o_sel_position_url");
		urlEl.setVisible(position.getKey() != null);

		uifactory.addSpacerElement("sep2", formLayout, false);
		
		int descRows = recruitingModule.getPositionDescriptionRows();
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String description = position.getDescription(locale);
			RichTextElement descriptionElement = uifactory.addRichTextElementForStringData("description_" + lang, "edit.description", description, descRows, 60,
					false, null, null, formLayout, usess, getWindowControl());
			descriptionElement.getEditorConfiguration().setRelativeUrls(false);
			descriptionElement.getEditorConfiguration().setRemoveScriptHost(false);
			descriptionElement.getEditorConfiguration().setPathInStatusBar(false);
			descriptionElement.setMandatory(true);
			descriptionElement.setEnabled(!readOnly);
			descriptionElement.setMaxLength(DESC_MAX_LENGTH);
			descriptionElement.setUserObject(locale);
			if(positionLanguages.length > 1) {
				descriptionElement.setLabel("edit.description_ml", new String[]{ lang });
				descriptionElement.setElementCssClass("o_sel_position_descritpion_" + lang);
			} else {
				descriptionElement.setElementCssClass("o_sel_position_descritpion");
			}
			descLanguagesEl.add(descriptionElement);
		}
		
		uifactory.addSpacerElement("sep3", formLayout, false);
		
		professorshipElement = uifactory.addCheckboxesVertical("professorship", "professorship.type", formLayout, professorshipKeys, professorshipValues, 1);
		professorshipElement.setVisible(recruitingModule.isProfessorshipTypeEnabled());
		professorshipElement.setEnabled(!readOnly);
		String professorship = position.getProfessorship();
		if(PositionProfessorship.assistant.name().equals(professorship)) {
			professorshipElement.select(professorshipKeys[0], true);
		} else if(PositionProfessorship.full.name().equals(professorship)) {
			professorshipElement.select(professorshipKeys[1], true);
		} else {
			professorshipElement.select(professorshipKeys[0], true);
			professorshipElement.select(professorshipKeys[1], true);
		}

		if(recruitingModule.isProfessorshipTypeEnabled()) {
			uifactory.addSpacerElement("sep4", formLayout, false);
		}
		
		uifactory.addStaticExampleText("docs.explanation", null, translate("edit.docs.committee.explanation"), formLayout);

		doc1Element = uifactory.addFileElement(getWindowControl(), getIdentity(), "document1", "edit.docs.committee", formLayout);
		doc1Element.setMaxUploadSizeKB(20480, "error.upload.maxsize", new String[] {"20"});
		doc1Element.addActionListener(FormEvent.ONCHANGE);
		doc1Element.setDeleteEnabled(true);
		doc1Element.setUserObject(new DocumentElement());
		doc1Element.setEnabled(!readOnly);
		Attachment doc1 = position.getDocument1();
		if(doc1 != null) {
			File file = new File(doc1.getName());
			doc1Element.setInitialFile(file);
		}
		doc2Element = uifactory.addFileElement(getWindowControl(), getIdentity(), "document2", "", formLayout);
		doc2Element.setMaxUploadSizeKB(20480, "error.upload.maxsize", new String[] {"20"});
		doc2Element.addActionListener(FormEvent.ONCHANGE);
		doc2Element.setDeleteEnabled(true);
		doc2Element.setUserObject(new DocumentElement());
		doc2Element.setEnabled(!readOnly);
		Attachment doc2 = position.getDocument2();
		if(doc2 != null) {
			File file = new File(doc2.getName());
			doc2Element.setInitialFile(file);
		}
		doc3Element = uifactory.addFileElement(getWindowControl(), getIdentity(), "document3", "", formLayout);
		doc3Element.setMaxUploadSizeKB(20480, "error.upload.maxsize", new String[] {"20"});
		doc3Element.addActionListener(FormEvent.ONCHANGE);
		doc3Element.setDeleteEnabled(true);
		doc3Element.setUserObject(new DocumentElement());
		doc3Element.setEnabled(!readOnly);
		Attachment doc3 = position.getDocument3();
		if(doc3 != null) {
			File file = new File(doc3.getName());
			doc3Element.setInitialFile(file);
		}

		uifactory.addSpacerElement("sep6", formLayout, false);
		
		policyLinkWrappers.clear();
		policyLinkWrappers.add(initPolicyLink(position.getPolicyLink1(), 0, formLayout));
		policyLinkWrappers.add(initPolicyLink(position.getPolicyLink2(), 1, formLayout));
		policyLinkWrappers.add(initPolicyLink(position.getPolicyLink3(), 2, formLayout));
		policyLinkWrappers.add(initPolicyLink(position.getPolicyLink4(), 3, formLayout));
		
		if(recruitingModule.isMailProPositionEnabled()) {
			uifactory.addSpacerElement("sep7", formLayout, false);
			mailSettingEl = uifactory.addDropdownSingleselect("mail.setting", "mail.setting", formLayout, new String[0], new String[0], null);
			mailSettingEl.addActionListener(FormEvent.ONCHANGE);
			mailSettingEl.setEnabled(!readOnly);
			
			String senderMail = recruitingModule.getStaffMail(position, organisationSettings);
			senderMailEl = uifactory.addTextElement("mail.sender", "mail.sender", 255, senderMail, formLayout);
			senderMailEl.setEnabled(!readOnly);
			String bccMail = recruitingModule.getBccStaffMail(position, organisationSettings);
			bccMailEl = uifactory.addTextElement("mail.bcc", "mail.bcc", 255, bccMail, formLayout);
			bccMailEl.setVisible(recruitingModule.isSendBccForConfirmation());
			bccMailEl.setEnabled(!readOnly);
			if(newPosition) {
				bccMailEl.setPlaceholderKey("mail.no.bcc.new", null);
			} else {
				bccMailEl.setPlaceholderKey("mail.no.bcc", null);
			}
		}
		
		uifactory.addSpacerElement("sep8", formLayout, false);
		
		RecruitingDuplicateApplicationOption allowDuplicateApplications = recruitingModule.getApplicationDuplicateEmailsAllowed();
		if(allowDuplicateApplications == RecruitingDuplicateApplicationOption.AT_POSITION) {
			String duplicateHintI18n = recruitingModule.getApplicationDuplicateAlgorithm() == RecruitingDuplicateApplicationAlgorithm.EMAIL_FIRST_LAST_NAME
					? "duplicate.setting.hint.names"
					: "duplicate.setting.hint.email";
			uifactory.addStaticTextElement("duplicate.hint", null, translate(duplicateHintI18n), formLayout);
			
			SelectionValues duplicatePK = new SelectionValues();
			duplicatePK.add(SelectionValues.entry(RecruitingDuplicateApplicationOption.ALLOWED.name(), translate("duplicate.allowed")));
			duplicatePK.add(SelectionValues.entry(RecruitingDuplicateApplicationOption.NOT_ALLOWED.name(), translate("duplicate.not.allowed")));
			duplicateApplicationEl = uifactory.addRadiosHorizontal("duplicate.setting", "duplicate.setting", formLayout, duplicatePK.keys(), duplicatePK.values());
			if(position.getDuplicateApplicationAllowedEnum() != null) {
				duplicateApplicationEl.select(position.getDuplicateApplicationAllowedEnum().name(), true);
			} else {
				duplicateApplicationEl.select(RecruitingDuplicateApplicationOption.NOT_ALLOWED.name(), true);
			}
			uifactory.addSpacerElement("sep8b", formLayout, false);
		}

		String[] enabledValues = new String[]{ translate("enable") };
		String message = position.getMessageToCommitte();
		messageToCommitteeEnableEl = uifactory.addCheckboxesHorizontal("message.committee.enable", "message.committee.enable", formLayout,
				enabledKeys, enabledValues);
		messageToCommitteeEnableEl.setElementCssClass("o_sel_position_enable_message_to_committee");
		messageToCommitteeEnableEl.addActionListener(FormEvent.ONCLICK);
		messageToCommitteeEnableEl.setEnabled(!readOnly);
		String	 explanation = translate("message.committee.exp");
		messageToCommitteeExplanationEl = uifactory.addStaticTextElement("message.committee.exp", null, explanation, formLayout);
		messageToCommitteeEl = uifactory.addRichTextElementForStringData("message.committee", null, message, 8, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		messageToCommitteeEl.getEditorConfiguration().setRelativeUrls(false);
		messageToCommitteeEl.getEditorConfiguration().setRemoveScriptHost(false);
		messageToCommitteeEl.getEditorConfiguration().setPathInStatusBar(false);
		messageToCommitteeEl.setEnabled(!readOnly);
		if(StringHelper.containsNonWhitespace(message)) {
			messageToCommitteeEnableEl.select(enabledKeys[0], true);
		} else {
			messageToCommitteeEl.setVisible(false);
			messageToCommitteeExplanationEl.setVisible(false);
		}

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateAvailableMultiLanguagesFields();
	}
	
	private void initMailSetting() {
		if(!recruitingModule.isMailProPositionEnabled()) return;

		// select mail settings
		setMailSetting();
		MailSettingEnum setting = recruitingModule.getMailSetting(position, organisationSettings);
		mailSettingEl.select(setting.name(), true);
		if(setting == MailSettingEnum.position) {
			senderMailEl.setValue(position.getSenderMail());
			bccMailEl.setValue(position.getBccMail());
		}
		updateMailSettingUI();
		
		String helpDomain;
		if(StringHelper.containsNonWhitespace(WebappHelper.getMailConfig("mailFromDomain"))) {
			helpDomain = translate("mail.position.domain.help", new String[] { WebappHelper.getMailConfig("mailFromDomain") } );
		} else {
			helpDomain = "";
		}
		mailSettingEl.setHelpText(translate("mail.position.help", new String[] { " " + helpDomain }));
	}
	
	public void setMailSetting() {
		if(!recruitingModule.isMailProPositionEnabled()) return;
		
		SelectionValues mailSettings = new SelectionValues();
		mailSettings.add(SelectionValues.entry(MailSettingEnum.system.name(), translate("mail.setting.system")));

		if(organisationModule.isEnabled()
				&& (position.getOrganisation() != null || (organisationEl != null && organisationEl.isOneSelected()))
				&& organisationEl.isOneSelected() && !ORG_UNIT_EMPTY_KEY.equals(organisationEl.getSelectedKey())) {
			mailSettings.add(SelectionValues.entry(MailSettingEnum.organisationUnit.name(), translate("mail.setting.organisationUnit")));
		}
		
		mailSettings.add(SelectionValues.entry(MailSettingEnum.position.name(), translate("mail.setting.position")));

		String selectedKey = mailSettingEl.isOneSelected() ? mailSettingEl.getSelectedKey() : null;
		mailSettingEl.setKeysAndValues(mailSettings.keys(), mailSettings.values(), null);
		if(selectedKey != null && mailSettings.containsKey(selectedKey)) {
			mailSettingEl.select(selectedKey, true);
		} else {
			mailSettingEl.select(MailSettingEnum.system.name(), true);
		}
	}
	
	private void updateMailSettingUI() {
		if(mailSettingEl != null && mailSettingEl.isOneSelected()) {
			String selectedSetting = mailSettingEl.getSelectedKey();
			boolean positionMail = MailSettingEnum.position.name().equals(selectedSetting);
			senderMailEl.setEnabled(positionMail);
			bccMailEl.setEnabled(positionMail);
			
			Position mockedPosition = new PositionImpl();
			mockedPosition.setSenderMail(position.getSenderMail());
			mockedPosition.setBccMail(position.getBccMail());
			mockedPosition.setMailSetting(MailSettingEnum.valueOf(selectedSetting));
			
			Organisation organisation = getSelectedOrganisation();
			mockedPosition.setOrganisation(organisation);
			organisationSettings = erFrontendManager.getOrganisationUnit(organisation);

			String sender = recruitingModule.getStaffMail(mockedPosition, organisationSettings);
			String bcc = recruitingModule.getBccStaffMail(mockedPosition, organisationSettings);
			senderMailEl.setValue(sender);
			bccMailEl.setValue(bcc);
		}
	}
	
	private void initOrganisation(UserSession usess, FormItemContainer formLayout) {
		if(organisationModule.isEnabled()) {
			Roles roles = usess.getRoles();
			List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles, OrganisationRoles.selectusmanager, OrganisationRoles.administrator);
			if(organisations.size() > 1) {
				Collections.sort(organisations, new OrganisationComparator());
			}
			SelectionValues organisationPK = new SelectionValues();
			boolean optional = (roles.isSelectusManager() || roles.isAdministrator()) && !recruitingModule.isPositionOrgUnitEnabled();
			if(optional) {
				organisationPK.add(SelectionValues.entry(ORG_UNIT_EMPTY_KEY, "-"));
			}
			for(Organisation organisation:organisations) {
				organisationPK.add(SelectionValues.entry(organisation.getKey().toString(), organisation.getDisplayName()));
			}
			organisationEl = uifactory.addDropdownSingleselect("position_org_unit", "edit.position.orgUnit", formLayout,
					organisationPK.keys(), organisationPK.values(), null);
			organisationEl.addActionListener(FormEvent.ONCHANGE);
			organisationEl.setMandatory(!optional);
			organisationEl.setEnabled(!readOnly);

			if(position.getOrganisation() != null && organisationPK.containsKey(position.getOrganisation().getKey().toString())) {
				String selectedUnitKey = position.getOrganisation().getKey().toString();
				organisationEl.select(selectedUnitKey, true);
			} else if(!organisationPK.isEmpty()) {
				organisationEl.select(organisationPK.keys()[0], true);
			}
		}
	}
	
	private PolicyLinkWrapper initPolicyLink(PolicyLink policyLink, int count, FormItemContainer formLayout) {
		String policyPage = velocity_root + "/edit_policy_links.html";
		FormLayoutContainer policyLinkContainer = FormLayoutContainer.createCustomFormLayout("policy_link_" + count, getTranslator(), policyPage);
		policyLinkContainer.setRootForm(mainForm);
		if(count == 0) {
			policyLinkContainer.setLabel("edit.position.rating.policy.links", null);
		}
		formLayout.add(policyLinkContainer);

		PolicyLinkWrapper wrapper = new PolicyLinkWrapper();
		wrapper.policyLink = policyLink;
		String label = policyLink == null ? null : policyLink.getLabel();
		wrapper.label = uifactory.addTextElement("policy.label", null, 255, label, policyLinkContainer);
		wrapper.label.setDomReplacementWrapperRequired(false);
		wrapper.label.setDisplaySize(32);
		wrapper.label.setEnabled(!readOnly);
		String url = policyLink == null ? null : policyLink.getUrl();
		wrapper.url = uifactory.addTextElement("policy.url", null, 1024, url, policyLinkContainer);
		wrapper.url.setDomReplacementWrapperRequired(false);
		wrapper.url.setDisplaySize(32);
		wrapper.url.setEnabled(!readOnly);
		
		policyLinkContainer.add(wrapper.label);
		policyLinkContainer.add(wrapper.url);
		wrapper.container = policyLinkContainer;
		return wrapper;
	}
	
	private void updateAvailableMultiLanguagesFields() {
		Set<Locale> availableLocales = getSelectedLocale();
		if(availableLocales.isEmpty() || positionLanguages.length == 1) {
			Locale defaultLocale = recruitingModule.getPositionDefaultLocale();
			
			for(TextElement mlEl:posTitleLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				if(defaultLocale.equals(locale)) {
					mlEl.setVisible(true);
					mlEl.setMandatory(true);
					mlEl.setElementCssClass("o_sel_position_title");
					mlEl.setLabel("edit.position_title", null);
				} else {
					mlEl.setVisible(false);
					mlEl.setMandatory(false);
				}
			}
			for(TextElement mlEl:shortTitleLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				if(defaultLocale.equals(locale)) {
					mlEl.setVisible(true);
					mlEl.setMandatory(true);
					mlEl.setLabel("edit.short_title", null);
					mlEl.setElementCssClass("o_sel_position_shorttitle");
				} else {
					mlEl.setVisible(false);
					mlEl.setMandatory(false);
				}
			}
			for(TextElement mlEl:descLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				if(defaultLocale.equals(locale)) {
					mlEl.setVisible(true);
					mlEl.setMandatory(true);
					mlEl.setLabel("edit.description", null);
					mlEl.setElementCssClass("o_sel_position_descritpion");
				} else {
					mlEl.setVisible(false);
					mlEl.setMandatory(false);
				}
			}
			if(recruitingModule.isPositionDepartmentEnabled()) {
				for(TextElement mlEl:departmentLanguagesEl) {
					Locale locale = (Locale)mlEl.getUserObject();
					if(defaultLocale.equals(locale)) {
						mlEl.setVisible(true);
						mlEl.setMandatory(!recruitingModule.isPositionDepartmentOptional());
						mlEl.setLabel("edit.department", null);
						mlEl.setElementCssClass("o_sel_position_departement");
					} else {
						mlEl.setVisible(false);
						mlEl.setMandatory(false);
					}
				}
			}
		} else {
			for(TextElement mlEl:posTitleLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				String lang = locale.getLanguage();
				
				mlEl.setVisible(availableLocales.contains(locale));
				mlEl.setMandatory(availableLocales.contains(locale));
				mlEl.setElementCssClass("o_sel_position_title_" + lang);
				mlEl.setLabel("edit.position_title_ml", new String[]{ lang });
			}
			for(TextElement mlEl:shortTitleLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				String lang = locale.getLanguage();

				mlEl.setVisible(availableLocales.contains(locale));
				mlEl.setMandatory(availableLocales.contains(locale));
				mlEl.setLabel("edit.short_title_ml", new String[]{ lang });
				mlEl.setElementCssClass("o_sel_position_shorttitle_" + lang);
			}
			for(TextElement mlEl:descLanguagesEl) {
				Locale locale = (Locale)mlEl.getUserObject();
				String lang = locale.getLanguage();
				
				mlEl.setVisible(availableLocales.contains(locale));
				mlEl.setMandatory(availableLocales.contains(locale));
				mlEl.setLabel("edit.description_ml", new String[]{ lang });
				mlEl.setElementCssClass("o_sel_position_descritpion_" + lang);
			}
			if(recruitingModule.isPositionDepartmentEnabled()) {
				for(TextElement mlEl:departmentLanguagesEl) {
					Locale locale = (Locale)mlEl.getUserObject();
					String lang = locale.getLanguage();
					
					mlEl.setVisible(availableLocales.contains(locale));
					mlEl.setMandatory(availableLocales.contains(locale) && !recruitingModule.isPositionDepartmentOptional());
					mlEl.setLabel("edit.department_ml", new String[]{ lang });
					mlEl.setElementCssClass("o_sel_position_departement_" + lang);
				}
			}
		}
	}
	
	private Set<Locale> getSelectedLocale() {
		Collection<String> availableLanguages = availableLanguageEls.getSelectedKeys();
		Set<Locale> availableLocales = new HashSet<>();
		for(String availableLanguage:availableLanguages) {
			availableLocales.add(positionLanguageToLocale.get(availableLanguage));
		}
		return availableLocales;
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		setMailSetting();
		updateMailSettingUI();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		Set<Locale> availableLocales = getSelectedLocale();
		Locale defaultLocale = recruitingModule.getPositionDefaultLocale();
		allOk &= validateTextElement(idElement, 32, !recruitingModule.isPositionPlannigIdOptional());
		for(TextElement posTitleEl:posTitleLanguagesEl) {
			if(availableLocales.isEmpty()) {
				Locale locale = (Locale)posTitleEl.getUserObject();
				if(locale.equals(defaultLocale)) {
					allOk &= validateTextElement(posTitleEl, 255, true);
				}
			} else if(availableLocales.contains(posTitleEl.getUserObject())) {
				allOk &= validateTextElement(posTitleEl, 255, true);
			} else {
				posTitleEl.clearError();
			}
		}
		for(TextElement shortTitleEl:shortTitleLanguagesEl) {
			if(availableLocales.isEmpty()) {
				Locale locale = (Locale)shortTitleEl.getUserObject();
				if(locale.equals(defaultLocale)) {
					allOk &= validateTextElement(shortTitleEl, 255, true);
				}
			} else if(availableLocales.contains(shortTitleEl.getUserObject())) {
				allOk &= validateTextElement(shortTitleEl, 255, true);
			} else {
				shortTitleEl.clearError();
			}
		}

		for(TextElement departmentEl:departmentLanguagesEl) {
			if(availableLocales.isEmpty()) {
				Locale locale = (Locale)departmentEl.getUserObject();
				if(locale.equals(defaultLocale)) {
					allOk &= validateTextElement(departmentEl, 255, !recruitingModule.isPositionDepartmentOptional());
				}
			} else if(availableLocales.contains(departmentEl.getUserObject())) {
				allOk &= validateTextElement(departmentEl, 255, !recruitingModule.isPositionDepartmentOptional());
			} else {
				departmentEl.clearError();
			}
		}
		
		if(organisationEl != null) {
			organisationEl.clearError();
			if(organisationEl.isVisible() && !organisationEl.isOneSelected()) {
				organisationEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		allOk &= validateTextElement(homepageElement, 255, !recruitingModule.isPositionHomepageOptional());
		
		for(RichTextElement descEl:descLanguagesEl) {
			if(availableLocales.isEmpty()) {
				Locale locale = (Locale)descEl.getUserObject();
				if(locale.equals(defaultLocale)) {
					allOk &= validateTextElement(descEl, DESC_MAX_LENGTH, true);
				}
			} else if(availableLocales.contains(descEl.getUserObject())) {
				allOk &= validateTextElement(descEl, DESC_MAX_LENGTH, true);
				
			} else {
				descEl.clearError();
			}
		}

		professorshipElement.clearError();
		if(professorshipElement.isVisible() &&!professorshipElement.isAtLeastSelected(1)) {
			professorshipElement.setErrorKey("professorship.error");
			allOk &= false;
		}
		
		allOk &= validateMailSettings();
		allOk &= validatePolicyLink(0);
		allOk &= validatePolicyLink(1);
		allOk &= validatePolicyLink(2);
		allOk &= validatePolicyLink(3);
		
		return allOk;
	}
	
	private boolean validateMailSettings() {
		boolean allOk = true;
		
		if(mailSettingEl != null) {
			mailSettingEl.clearError();
			senderMailEl.clearError();
			bccMailEl.clearError();
			
			if(!mailSettingEl.isOneSelected()) {
				mailSettingEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(MailSettingEnum.position.name().equals(mailSettingEl.getSelectedKey())) {
				if(!StringHelper.containsNonWhitespace(senderMailEl.getValue())) {
					senderMailEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			}
			
			if(senderMailEl.isEnabled() && senderMailEl.isVisible()) {
				try {
					InternetAddress add = new InternetAddress(senderMailEl.getValue());
					if(!MailHelper.isValidEmailAddress(senderMailEl.getValue()) && add.getAddress() != null) {
						senderMailEl.setErrorKey("error.mail.invalid.syntax");
						allOk &= false;
					}
				} catch (AddressException e) {
					senderMailEl.setErrorKey("error.mail.invalid.syntax");
					allOk &= false;
				}
			}
			
			if(bccMailEl.isEnabled() && bccMailEl.isVisible()) {
				try {
					MailerSender.parseAddress(bccMailEl.getValue());
					String[] addresseArr = bccMailEl.getValue().split("[;,]");
					for(String address:addresseArr) {
						if(StringHelper.containsNonWhitespace(address) && !MailHelper.isValidEmailAddress(address.trim())) {
							bccMailEl.setErrorKey("error.mail.invalid.syntax");
							allOk &= false;
						}
					}
				} catch (AddressException e) {
					bccMailEl.setErrorKey("error.mails.invalid.syntax");
					allOk &= false;
				}
			}
		}
		return allOk;
	}
	
	private boolean validatePolicyLink(int count) {
		boolean allOk = true;
		
		PolicyLinkWrapper wrapper = policyLinkWrappers.get(count);
		FormLayoutContainer container = wrapper.container;
		container.clearError();
		
		//label
		TextElement labelEl = wrapper.label;
		String label = labelEl.getValue();
		if(StringHelper.containsNonWhitespace(label) && label.length() > 255) {
			container.setErrorKey("input.toolong", new String[]{ Integer.toString(255) });
			allOk = false;
		}
		
		//url
		TextElement urlEl = wrapper.url;
		String url = urlEl.getValue();
		if(StringHelper.containsNonWhitespace(url) && url.length() > 1024) {
			container.setErrorKey("input.toolong", new String[]{ Integer.toString(1024) });
			allOk = false;
		} else if(!isValidUrl(url)) {
			container.setErrorKey("error.url.invalid");
			allOk = false;
		}

		return allOk;
	}
	
	private boolean isValidUrl(String value) {
		boolean allOk = true;
		if (StringHelper.containsNonWhitespace(value)) {			
			// check url address syntax
			try {
				URL url = new URL(value);
				url.toURI();

				if(!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) {
					allOk &= false;
				}
				if(!StringHelper.containsNonWhitespace(url.getAuthority())) {
					allOk &= false;
				}
				if(!StringHelper.containsNonWhitespace(url.getHost())) {
					allOk &= false;
				}
			} catch (MalformedURLException | URISyntaxException e) {
				allOk &= false;
			}
			
			if(!value.startsWith("http://") &&!value.startsWith("https://")) {
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateTextElement(TextElement textEl, int length, boolean mandatory) {
		boolean ok = true;
		textEl.clearError();
		
		Filter filter = original -> original;
		String value = textEl.getValue(filter);
		if(mandatory) {
			if(!StringHelper.containsNonWhitespace(value)) {
				textEl.setErrorKey("form.legende.mandatory");
				ok = false;
			} else if (value.length() > length) {
				textEl.setErrorKey("input.toolong", new String[]{ Integer.toString(length) });
				ok = false;
			}
		} else if(StringHelper.containsNonWhitespace(value)) {
			if(value.length() > length) {
				textEl.setErrorKey("input.toolong", new String[]{ Integer.toString(length) });
				ok = false;
			}
		}
		return ok;
	}
	
	private void prefillDepartment() {
		if(organisationEl == null || departmentLanguagesEl == null ||
				departmentLanguagesEl.isEmpty() || !recruitingModule.isPositionPrefillDepartment()) return;
		if(!organisationEl.isOneSelected() || ORG_UNIT_EMPTY_KEY.equals(organisationEl.getSelectedKey())) return;
		
		Organisation selectOrganisation = getSelectedOrganisation();
		if(selectOrganisation != null) {
			OrganisationUnit selectedUnit = null;
			for(TextElement departmentEl:departmentLanguagesEl) {
				if(!StringHelper.containsNonWhitespace(departmentEl.getValue())) {
					if(selectedUnit == null && selectOrganisation != null) {
						selectedUnit = erFrontendManager.getOrganisationUnit(selectOrganisation);
						if(selectedUnit == null) {
							break;
						}
					}
					Locale locale = (Locale)departmentEl.getUserObject();
					String unitName = selectedUnit.getMLName(locale);
					departmentEl.setValue(unitName);
				}
			}
		}
	}
	
	private void removeDocument(Attachment attachment) {
		erFrontendManager.deleteAttachment(position, attachment);
	}
	
	private Attachment commitDocument(FileElement fileEl, Attachment attachment) {
		File file = fileEl.getUploadFile();
		String filename = fileEl.getUploadFileName();
		if(file != null && file.exists()) {
			try(FileInputStream fis = new FileInputStream(file)) {
				byte[] datas = IOUtils.toByteArray(fis);
				if(!StringHelper.containsNonWhitespace(filename)) {
					filename = file.getName();
				}
				attachment = erFrontendManager.setAttachmentDatas(attachment, filename, DocumentType.pdf, datas);
				return attachment;
			} catch (Exception e) {
				logError("", e);
			}
		}
		return null;
	}
	
	public Organisation getSelectedOrganisation() {
		Organisation organisation = null;
		if(organisationEl != null && organisationEl.isVisible() && organisationEl.isOneSelected()) {
			if(ORG_UNIT_EMPTY_KEY.equals(organisationEl.getSelectedKey())) {
				organisation = null;
			} else {
				organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(organisationEl.getSelectedKey())));

			}
		}
		return organisation;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = erFrontendManager.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);

		Collection<String> availableLanguages = availableLanguageEls.getSelectedKeys();
		Set<Locale> availableLocales = new HashSet<>();
		StringBuilder availableLanguagesSb = new StringBuilder();
		for(String availableLanguage:availableLanguages) {
			if(availableLanguagesSb.length() > 0) availableLanguagesSb.append(",");
			availableLanguagesSb.append(availableLanguage);
			availableLocales.add(positionLanguageToLocale.get(availableLanguage));
		}
		if(availableLanguages.isEmpty()) {
			position.setAvailableLanguages("-");
		} else {
			position.setAvailableLanguages(availableLanguagesSb.toString());
		}

		for(TextElement titleEl:posTitleLanguagesEl) {
			Locale locale = (Locale)titleEl.getUserObject();
			if(availableLocales.isEmpty() || availableLocales.contains(locale)) {
				position.setPositionTitle(titleEl.getValue(), locale);
			} else {
				position.setPositionTitle(null, locale);
			}
		}
		
		for(TextElement titleEl:shortTitleLanguagesEl) {
			Locale locale = (Locale)titleEl.getUserObject();
			if(availableLocales.isEmpty() || availableLocales.contains(locale)) {
				position.setShortTitle(titleEl.getValue(), locale);
			} else {
				position.setShortTitle(null, locale);
			}
		}
		
		for(TextElement descEl:descLanguagesEl) {
			Locale locale = (Locale)descEl.getUserObject();
			if(availableLocales.isEmpty() || availableLocales.contains(locale)) {
				position.setDescription(descEl.getValue(), locale);
			} else {
				position.setDescription(null, locale);
			}
		}
		
		position.setPlaningsNumber(idElement.getValue());
		
		Organisation organisation = getSelectedOrganisation();
		position.setOrganisation(organisation);

		for(TextElement departmentEl:departmentLanguagesEl) {
			Locale locale = (Locale)departmentEl.getUserObject();
			if(availableLocales.isEmpty() || availableLocales.contains(locale)) {
				position.setDepartment(departmentEl.getValue(), locale);
			} else {
				position.setDepartment(null, locale);
			}
		}

		position.setHomepage(homepageElement.getValue());

		if(professorshipElement.isVisible() && professorshipElement.isAtLeastSelected(1)) {
			if(professorshipElement.isSelected(0) && professorshipElement.isSelected(1)) {
				position.setProfessorship(PositionProfessorship.any.name());
			} else if(professorshipElement.isSelected(0)) {
				position.setProfessorship(PositionProfessorship.assistant.name());
			} else {
				position.setProfessorship(PositionProfessorship.full.name());
			}
		}
		
		DocumentElement d1 = (DocumentElement)doc1Element.getUserObject();
		if(d1.isDelete()) {
			removeDocument(position.getDocument1());
		} else {
			Attachment doc1 = commitDocument(doc1Element, position.getDocument1());
			if(doc1 != null) {
				position.setDocument1(doc1);
			}
		}
		
		DocumentElement d2 = (DocumentElement)doc2Element.getUserObject();
		if(d2.isDelete()) {
			removeDocument(position.getDocument2());
		} else {
			Attachment doc2 = commitDocument(doc2Element, position.getDocument2());
			if(doc2 != null) {
				position.setDocument2(doc2);
			}
		}
		
		DocumentElement d3 = (DocumentElement)doc3Element.getUserObject();
		if(d3.isDelete()) {
			removeDocument(position.getDocument3());
		} else {
			Attachment doc3 = commitDocument(doc3Element, position.getDocument3());
			if(doc3 != null) {
				position.setDocument3(doc3);
			}
		}
		
		if(duplicateApplicationEl != null && duplicateApplicationEl.isOneSelected()) {
			position.setDuplicateApplicationAllowedEnum(RecruitingDuplicateApplicationOption.valueOf(duplicateApplicationEl.getSelectedKey()));
		}
		
		if(messageToCommitteeEnableEl.isAtLeastSelected(1)) {
			String message = messageToCommitteeEl.getValue();
			position.setMessageToCommitte(message);
		} else {
			position.setMessageToCommitte(null);
		}
		
		if(mailSettingEl != null && mailSettingEl.isVisible()) {
			String selectedSetting = mailSettingEl.getSelectedKey();
			position.setMailSetting(MailSettingEnum.valueOf(selectedSetting));
			if(MailSettingEnum.position.name().equals(selectedSetting)) {
				position.setSenderMail(senderMailEl.getValue());
				position.setBccMail(bccMailEl.getValue());
			} else {
				position.setSenderMail(null);
				position.setBccMail(null);
			}
		}
		
		position.setPolicyLink1(commitPolicyLink(0));
		position.setPolicyLink2(commitPolicyLink(1));
		position.setPolicyLink3(commitPolicyLink(2));
		position.setPolicyLink4(commitPolicyLink(3));
		
		position = erFrontendManager.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update position: " + position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	private PolicyLink commitPolicyLink(int count) {
		PolicyLinkWrapper wrapper = policyLinkWrappers.get(count);
		String label = wrapper.label.getValue();
		String url = wrapper.url.getValue();
		
		if(StringHelper.containsNonWhitespace(url)) {
			if(wrapper.policyLink == null) {
				wrapper.policyLink = new PolicyLink();
			}
		} else {
			wrapper.policyLink = null;
		}
		
		if(StringHelper.containsNonWhitespace(label)) {
			wrapper.policyLink.setLabel(label);
		} else if(wrapper.policyLink != null) {
			wrapper.policyLink.setLabel(null);
		}
		
		if(StringHelper.containsNonWhitespace(url)) {
			wrapper.policyLink.setUrl(url);
		} else if(wrapper.policyLink != null) {
			wrapper.policyLink.setUrl(null);
		}
		return wrapper.policyLink;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (availableLanguageEls == source) {
			updateAvailableMultiLanguagesFields();
		} else if(organisationEl == source) {
			prefillDepartment();
			setMailSetting();
			updateMailSettingUI();
		} else if (messageToCommitteeEnableEl == source) {
			boolean enabled = messageToCommitteeEnableEl.isAtLeastSelected(1);
			messageToCommitteeEl.setVisible(enabled);
			messageToCommitteeExplanationEl.setVisible(enabled);
		} else if(mailSettingEl == source) {
			updateMailSettingUI();
		} else if(source instanceof FileElement) {
			FileElement fileEl = (FileElement)source;
			if(fileEl.getUserObject() instanceof DocumentElement) {
				DocumentElement docEl = (DocumentElement)fileEl.getUserObject();
				if(event instanceof DeleteFileElementEvent) {
					if(fileEl.getInitialFile() != null) {
						if(fileEl.getUploadFile() != null) {
							fileEl.reset();
						} else {
							docEl.setDelete(true);
							fileEl.setInitialFile(null);
						}	
					} else if (fileEl.getUploadFile() != null) {
						fileEl.reset();
					}
				} else {
					docEl.setDelete(false);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private class PolicyLinkWrapper {
		private TextElement url;
		private TextElement label;
		private FormLayoutContainer container;
		private PolicyLink policyLink;
	}
	
	public static class DocumentElement {
		
		private boolean delete;

		public boolean isDelete() {
			return delete;
		}

		public void setDelete(boolean delete) {
			this.delete = delete;
		}
	}
}