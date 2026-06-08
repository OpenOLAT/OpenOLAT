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
package org.olat.modules.selectus.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitEditController extends FormBasicController {
	
	private static final String[] settingsKeys = new String[]{ "system", "specific" };
	
	private SingleSelection settingsEl;
	private TextElement urlEl;
	private List<TextElement> nameEls = new ArrayList<>();
	private TextElement descriptionEl;
	private TextElement staffMailEl;
	private TextElement staffBccEl;
	private TextElement mailSignatureEl;
	
	private OrganisationUnit organisationUnit;
	private final Locale[] positionLanguages;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	public OrganisationUnitEditController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}
	
	public OrganisationUnitEditController(UserRequest ureq, WindowControl wControl, OrganisationUnit organisationUnit) {
		super(ureq, wControl, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		this.organisationUnit = organisationUnit;
		positionLanguages = recruitingModule.getPositionLocales();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_org_unit_form");
		
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			String name = organisationUnit == null ? null : organisationUnit.getName(locale);
			TextElement nameEl = uifactory.addTextElement("organisation.name." + lang, "organisation.name." + lang, 255, name, formLayout);
			nameEl.setElementCssClass("o_sel_org_unit_name_".concat(lang));
			nameEl.setUserObject(locale);
			nameEl.setMandatory(true);
			nameEls.add(nameEl);
		}

		String url = organisationUnit == null ? null : organisationUnit.getUrl();
		urlEl = uifactory.addTextElement("organisation.url", "organisation.url", 255, url, formLayout);
		urlEl.setExampleKey("organisation.url.example", null);
		
		String description = organisationUnit == null ? null : organisationUnit.getDescription();
		descriptionEl = uifactory.addTextAreaElement("organisation.description", 6, 60, description, formLayout);
		descriptionEl.setElementCssClass("o_sel_org_unit_descr");

		String[] settingsValues = new String[] {
				translate("system.settings"), translate("specific.settings")
		};
		settingsEl = uifactory.addRadiosHorizontal("settings", "email.settings", formLayout, settingsKeys, settingsValues);
		settingsEl.addActionListener(FormEvent.ONCHANGE);
		if(organisationUnit == null || organisationUnit.isSystemConfiguration()) {
			settingsEl.select(settingsKeys[0], true);
		} else {
			settingsEl.select(settingsKeys[1], true);
		}
		
		String helpDomain;
		if(StringHelper.containsNonWhitespace(WebappHelper.getMailConfig("mailFromDomain"))) {
			helpDomain = translate("mail.organisation.domain.help", new String[] { WebappHelper.getMailConfig("mailFromDomain") });
		} else {
			helpDomain = "";
		}
		String help = translate("mail.organisation.help", new String[] { " " + helpDomain });
		settingsEl.setHelpText(help);
		
		String staffMail = organisationUnit == null ? null : organisationUnit.getStaffMail();
		staffMailEl = uifactory.addTextElement("organisation.staff.mail", "organisation.staff.mail", 255, staffMail, formLayout);
		
		String staffBcc = organisationUnit == null ? null : organisationUnit.getStaffBcc();
		staffBccEl = uifactory.addTextElement("organisation.staff.bcc", "organisation.staff.bcc", 255, staffBcc, formLayout);
		staffBccEl.setVisible(recruitingModule.isSendBccForConfirmation());
		if(organisationUnit == null || organisationUnit.getKey() == null) {
			staffBccEl.setPlaceholderKey("organisation.staff.bcc.hint.nobbc.new", null);
		} else {
			staffBccEl.setPlaceholderKey("organisation.staff.bcc.hint.nobbc", null);
		}
		
		String mailSignature = organisationUnit == null ? null : organisationUnit.getMailSignature();
		mailSignatureEl = uifactory.addTextAreaElement("organisation.mail.signature", "organisation.mail.signature", 2000, 8, 60, true, false, false, mailSignature, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		updateSettings();
	}
	
	private void updateSettings() {
		boolean unitSettingsEnabled = settingsEl.isSelected(1);
		staffMailEl.setEnabled(unitSettingsEnabled);
		staffBccEl.setEnabled(unitSettingsEnabled);
		if(!staffMailEl.isEnabled()) {
			staffMailEl.setValue(recruitingModule.getStaffMail());
		}
		if(!staffBccEl.isEnabled()) {
			staffBccEl.setValue(recruitingModule.getBccStaffMail());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(TextElement nameEl:nameEls) {
			nameEl.clearError();
			if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
				boolean inUse = false; //TODO load mail settings recruitingFrontendManager.isOrganisationUnitNamesInUse(nameEl.getValue(), organisationUnit);
				if(inUse) {
					nameEl.setErrorKey("error.name.in.use");
					allOk &= false;
				}
			} else {
				nameEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		urlEl.clearError();
		UrlValidator urlValidator = UrlValidator.getInstance();
		if(StringHelper.containsNonWhitespace(urlEl.getValue())
				&& !urlValidator.isValid(urlEl.getValue())) {
			urlEl.setErrorKey("error.url");
			allOk &= false;
		}
		
		staffBccEl.clearError();
		settingsEl.clearError();
		staffMailEl.clearError();
		if(!settingsEl.isOneSelected()) {
			settingsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(settingsEl.isSelected(1)) {
			EmailValidator emailValidator = EmailValidator.getInstance();
			if(StringHelper.containsNonWhitespace(staffMailEl.getValue())
					&& !emailValidator.isValid(staffMailEl.getValue())) {
				staffMailEl.setErrorKey("error.staff.mail");
				allOk &= false;
			}
			if(StringHelper.containsNonWhitespace(staffBccEl.getValue())
					&& !emailValidator.isValid(staffBccEl.getValue())) {
				staffBccEl.setErrorKey("error.staff.mail");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(settingsEl == source) {
			updateSettings();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {	
		for(TextElement nameEl:nameEls) {
			Locale locale = (Locale)nameEl.getUserObject();
			organisationUnit.setName(nameEl.getValue(), locale);
		}

		organisationUnit.setUrl(urlEl.getValue());
		organisationUnit.setDescription(descriptionEl.getValue());
		organisationUnit.setSystemConfiguration(settingsEl.isSelected(0));
		if(settingsEl.isSelected(1)) {
			organisationUnit.setStaffMail(staffMailEl.getValue());
			organisationUnit.setStaffBcc(staffBccEl.getValue());
		} else {
			organisationUnit.setStaffMail(null);
			organisationUnit.setStaffBcc(null);
		}
		organisationUnit.setMailSignature(mailSignatureEl.getValue());

		organisationUnit = recruitingFrontendManager.updateOrganisationUnit(organisationUnit);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
