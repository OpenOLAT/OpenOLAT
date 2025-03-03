/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.ui.organisation.OrganisationEmailDomainAdminController;
import org.olat.user.ui.organisation.OrganisationEmailDomainDataModel;
import org.olat.user.ui.organisation.OrganisationEmailDomainRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Feb 25, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationConfigAdminController extends FormBasicController {

	private static final String[] enableRegistrationKeys = new String[]{"on"};
	private final boolean orgEmailDomainEnabled;

	private FormSubmit submitBtn;
	private FormToggle registrationEl;
	private FormToggle domainRestrictionEl;
	private MultipleSelectionElement registrationLoginElement;
	private MultipleSelectionElement emailValidationEl;
	private MultipleSelectionElement allowRecurringUserEl;
	private TextElement validUntilGuiEl;
	private TextElement validUntilRestEl;
	private TextElement domainListElement;
	private FormLayoutContainer domainsContainer;
	private FormLayoutContainer settingsContainer;
	private FormLayoutContainer validityCont;
	private OrganisationEmailDomainDataModel domainDataModel;
	private FlexiTableElement tableEl;


	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public RegistrationConfigAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_config");
		orgEmailDomainEnabled = organisationModule.isEnabled() && organisationModule.isEmailDomainEnabled();

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//settings
		settingsContainer = FormLayoutContainer.createDefaultFormLayout("settingsCont", getTranslator());
		settingsContainer.setRootForm(mainForm);
		settingsContainer.setFormTitle(translate("admin.registration.title"));
		formLayout.add(settingsContainer);

		FormLayoutContainer enableCont = FormLayoutContainer.createDefaultFormLayout("enableCont", getTranslator());
		enableCont.setRootForm(mainForm);
		formLayout.add(enableCont);
		registrationEl = uifactory.addToggleButton("enable.self.registration", "admin.enableRegistration", translate("on"), translate("off"), enableCont);
		registrationEl.addActionListener(FormEvent.ONCHANGE);
		registrationEl.toggle(registrationModule.isSelfRegistrationEnabled());

		registrationLoginElement = uifactory.addCheckboxesHorizontal("enable.registration.login", "admin.enableRegistrationLogin", settingsContainer, enableRegistrationKeys, new String[]{ translate("admin.enableRegistration.on") });
		registrationLoginElement.addActionListener(FormEvent.ONCHANGE);
		registrationLoginElement.select("on", registrationModule.isSelfRegistrationLoginEnabled());

		uifactory.addSpacerElement("spacer", settingsContainer, false);

		allowRecurringUserEl = uifactory.addCheckboxesHorizontal("enable.recurring.user", "admin.enable.recurring.user", settingsContainer, enableRegistrationKeys, new String[]{translate("admin.enable.recurring.user.label")});
		allowRecurringUserEl.select("on", registrationModule.isAllowRecurringUserEnabled());

		// Only update the email validation setting if orgEmailDomainEnabled is true.
		// Otherwise, retain the existing value to avoid unnecessary changes. (OO-8342)
		registrationModule.setEmailValidationEnabled(
				orgEmailDomainEnabled || registrationModule.isEmailValidationEnabled()
		);

		String emailValidationStepValue = !orgEmailDomainEnabled ? translate("admin.enableRegistration.on") : translate("admin.enable.email.validation.disabled");
		emailValidationEl = uifactory.addCheckboxesHorizontal("email.validation", "admin.enable.email.validation", settingsContainer, enableRegistrationKeys, new String[]{emailValidationStepValue});
		emailValidationEl.select("on", registrationModule.isEmailValidationEnabled());
		emailValidationEl.setEnabled(!orgEmailDomainEnabled);

		//domain configuration
		initDomainForm(formLayout);

		// validity area
		validityCont = FormLayoutContainer.createDefaultFormLayout("validityCont", getTranslator());
		validityCont.setRootForm(mainForm);
		validityCont.setFormTitle(translate("admin.registration.config.validity"));
		formLayout.add(validityCont);

		validUntilGuiEl = uifactory.addTextElement("admin.registration.valid.until.gui", 20, registrationModule.getValidUntilMinutesGui().toString(), validityCont);
		validUntilGuiEl.setMandatory(true);
		validUntilRestEl = uifactory.addTextElement("admin.registration.valid.until.rest", 20, registrationModule.getValidUntilHoursRest().toString(), validityCont);
		validUntilRestEl.setMandatory(true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		submitBtn = uifactory.addFormSubmitButton("save", buttonsCont);
		formLayout.add(buttonsCont);
	}

	private void initDomainForm(FormItemContainer formLayout) {
		domainsContainer = FormLayoutContainer.createDefaultFormLayout("domainsCont", getTranslator());
		domainsContainer.setRootForm(mainForm);
		domainsContainer.setFormTitle(translate("admin.registration.domains.title"));
		formLayout.add(domainsContainer);

		if (!orgEmailDomainEnabled) {
			domainsContainer.setFormInfo(translate("admin.registration.domains.desc"));
			domainRestrictionEl = uifactory.addToggleButton("enable.domain.restriction", "admin.enable.domain.restriction", translate("on"), translate("off"), domainsContainer);
			domainRestrictionEl.addActionListener(FormEvent.ONCHANGE);
			domainRestrictionEl.toggle(registrationModule.isDomainRestrictionEnabled());

			String domainsList = registrationModule.getDomainListRaw();
			domainListElement = uifactory.addTextAreaElement("registration.domain.list", "admin.registration.domains", 64000, 10, 65, true, false, domainsList, domainsContainer);
		} else {
			domainsContainer.setFormDescription(translate("admin.registration.organisation.email.domain",
					BusinessControlFactory.getInstance().getURLFromBusinessPathString("[AdminSite:0][organisations:0]")));
			initOrgDomainTable(flc);
		}
	}

	private void initOrgDomainTable(FormLayoutContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.organisation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.domain));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.enabled));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.subdomainsAllowed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainDataModel.OrganisationEmailDomainCols.numIdentitiesWithDomain));

		Translator domainTranslator = Util.createPackageTranslator(OrganisationEmailDomainAdminController.class, getLocale());
		domainDataModel = new OrganisationEmailDomainDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", domainDataModel, 5, false, domainTranslator, formLayout);
		loadModel();
	}

	private void loadModel() {
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		List<OrganisationEmailDomain> emailDomains = organisationService.getEmailDomains(searchParams);
		Map<Long, Integer> emailDomainKeyToUsersCount = organisationService.getEmailDomainKeyToUsersCount(emailDomains);

		List<OrganisationEmailDomainRow> rows = new ArrayList<>(emailDomains.size());
		for (OrganisationEmailDomain emailDomain : emailDomains) {
			OrganisationEmailDomainRow row = new OrganisationEmailDomainRow(emailDomain);
			row.setNumIdentitieswithDomain(emailDomainKeyToUsersCount.getOrDefault(emailDomain.getKey(), 0));
			rows.add(row);
		}

		domainDataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateInteger(validUntilGuiEl, 1);
		allOk &= validateInteger(validUntilRestEl, 1);

		if (!orgEmailDomainEnabled) {
			String whiteList = domainListElement.getValue();
			domainListElement.clearError();
			if (registrationModule.isDomainRestrictionEnabled()
					&& StringHelper.containsNonWhitespace(whiteList)) {
				List<String> normalizedList = registrationModule.getDomainList(whiteList);
				List<String> errors = registrationManager.validateWhiteList(normalizedList);
				if (!errors.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (String error : errors) {
						if (!sb.isEmpty()) sb.append(" ,");
						sb.append(error);
					}
					domainListElement.setErrorKey("admin.registration.domains.error", sb.toString());
					allOk = false;
				}
			}
		}
		return allOk;
	}

	private boolean validateInteger(TextElement el, int min) {
		boolean allOk = true;
		el.clearError();
		String val = el.getValue();
		if (StringHelper.containsNonWhitespace(val)) {
			try {
				int value = Integer.parseInt(val);
				if (min > value) {
					el.setErrorKey("error.wrong.int");
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("error.wrong.int");
				allOk = false;
			}
		} else if (el.isMandatory()) {
			el.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		return allOk;
	}

	private void updateUI() {
		boolean enableMain = registrationEl.isOn();
		settingsContainer.setVisible(enableMain);
		domainsContainer.setVisible(enableMain);
		validityCont.setVisible(enableMain);
		registrationLoginElement.setEnabled(enableMain);
		submitBtn.setVisible(enableMain);
		if (!orgEmailDomainEnabled) {
			domainListElement.setVisible(domainRestrictionEl.isOn());
		}

		boolean enableDomains = enableMain && registrationLoginElement.isSelected(0);
		domainsContainer.setVisible(enableDomains);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == registrationEl) {
			boolean enable = registrationEl.isOn();
			registrationModule.setSelfRegistrationEnabled(enable);
			updateUI();
		} else if (source == domainRestrictionEl) {
			updateUI();
		} else if (source == registrationLoginElement) {
			updateUI();
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (settingsContainer.isVisible()) {
			registrationModule.setSelfRegistrationLoginEnabled(registrationLoginElement.isSelected(0));
			registrationModule.setEmailValidationEnabled(emailValidationEl.isSelected(0));
			registrationModule.setAllowRecurringUserEnabled(allowRecurringUserEl.isSelected(0));
		}
		if (domainsContainer.isVisible() && !orgEmailDomainEnabled) {
			boolean enable = domainRestrictionEl.isOn();
			registrationModule.setDomainRestrictionEnabled(enable);
		}

		Integer validUntilHoursGui = Integer.parseInt(validUntilGuiEl.getValue());
		registrationModule.setValidUntilMinutesGui(validUntilHoursGui);
		Integer validUntilHoursRest = Integer.parseInt(validUntilRestEl.getValue());
		registrationModule.setValidUntilHoursRest(validUntilHoursRest);

		String domains = domainListElement == null ? null : domainListElement.getValue();
		registrationModule.setDomainListRaw(domains);
	}
}
