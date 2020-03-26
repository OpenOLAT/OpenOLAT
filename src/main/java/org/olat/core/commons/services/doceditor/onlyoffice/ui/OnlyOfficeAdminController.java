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
package org.olat.core.commons.services.doceditor.onlyoffice.ui;

import static org.olat.core.commons.services.doceditor.onlyoffice.ui.OnlyOfficeUIFactory.validateIsMandatory;
import static org.olat.core.commons.services.doceditor.onlyoffice.ui.OnlyOfficeUIFactory.validatePositiveInteger;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;

import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OnlyOfficeAdminController extends FormBasicController {

	private static final String[] ENABLED_KEYS = new String[]{"on"};
	private static final String USAGE_AUTHOR = "author";
	private static final String USAGE_COACH = "coach";
	private static final String USAGE_MANAGERS = "managers";
	
	private MultipleSelectionElement enabledEl;
	private TextElement baseUrlEl;
	private TextElement jwtSecretEl;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;
	private TextElement licenseEditEl;
	private MultipleSelectionElement usageRolesEl;

	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;
	@Autowired
	private OnlyOfficeSecurityService onlyOfficeSecurityService;

	public OnlyOfficeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");
		
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
		enabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isEnabled());
		
		String url = onlyOfficeModule.getBaseUrl();
		baseUrlEl = uifactory.addTextElement("admin.base.url", 128, url, formLayout);
		baseUrlEl.setMandatory(true);
		
		String secret = onlyOfficeModule.getJwtSecret();
		jwtSecretEl = uifactory.addTextElement("admin.jwt.secret", 128, secret, formLayout);
		jwtSecretEl.setMandatory(true);
		
		dataTransferConfirmationEnabledEl = uifactory.addCheckboxesHorizontal(
				"admin.data.transfer.confirmation.enabled", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isDataTransferConfirmationEnabled());
		
		String licenseEdit = onlyOfficeModule.getLicenseEdit() != null
				? onlyOfficeModule.getLicenseEdit().toString()
				: null;
		licenseEditEl = uifactory.addTextElement("admin.license.edit", 10, licenseEdit, formLayout);
		
		Long editLicensesInUse = onlyOfficeService.getEditLicensesInUse();
		editLicensesInUse = editLicensesInUse != null? editLicensesInUse: 0;
		uifactory.addStaticTextElement("admin.license.edit.in.use", editLicensesInUse.toString(), formLayout);
		
		KeyValues usageRolesKV = new KeyValues();
		usageRolesKV.add(entry(USAGE_AUTHOR, translate("admin.usage.roles.author")));
		usageRolesKV.add(entry(USAGE_COACH, translate("admin.usage.roles.coach")));
		usageRolesKV.add(entry(USAGE_MANAGERS, translate("admin.usage.roles.managers")));
		usageRolesEl = uifactory.addCheckboxesVertical("admin.usage.roles", formLayout, usageRolesKV.keys(), usageRolesKV.values(), 1);
		usageRolesEl.setHelpTextKey("admin.usage.roles.help", null);
		usageRolesEl.select(USAGE_AUTHOR, onlyOfficeModule.isUsageRestrictedToAuthors());
		usageRolesEl.select(USAGE_COACH, onlyOfficeModule.isUsageRestrictedToCoaches());
		usageRolesEl.select(USAGE_MANAGERS, onlyOfficeModule.isUsageRestrictedToManagers());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if (enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(baseUrlEl);
			
			boolean jwtSecretOk = validateIsMandatory(jwtSecretEl);
			if (jwtSecretOk && !onlyOfficeSecurityService.isValidSecret(jwtSecretEl.getValue())) {
				jwtSecretEl.setErrorKey("admin.jwt.secret.invalid", null);
				jwtSecretOk = false;
			}
			allOk &= jwtSecretOk;
			
			allOk &= validatePositiveInteger(licenseEditEl);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		onlyOfficeModule.setEnabled(enabled);
		
		String url = baseUrlEl.getValue();
		url = url.endsWith("/")? url: url + "/";
		onlyOfficeModule.setBaseUrl(url);
		
		String jwtSecret = jwtSecretEl.getValue();
		onlyOfficeModule.setJwtSecret(jwtSecret);
		
		boolean dataTransferConfirmationEnabled = dataTransferConfirmationEnabledEl.isAtLeastSelected(1);
		onlyOfficeModule.setDataTransferConfirmationEnabled(dataTransferConfirmationEnabled);
		
		String licenseEditValue = licenseEditEl.getValue();
		Integer licenseEdit = StringHelper.containsNonWhitespace(licenseEditValue)
				? Integer.valueOf(licenseEditValue)
				: null;
		onlyOfficeModule.setLicenseEdit(licenseEdit);
		
		Collection<String> restrictionKeys = usageRolesEl.getSelectedKeys();
		onlyOfficeModule.setUsageRestrictedToAuthors(restrictionKeys.contains(USAGE_AUTHOR));
		onlyOfficeModule.setUsageRestrictedToCoaches(restrictionKeys.contains(USAGE_COACH));
		onlyOfficeModule.setUsageRestrictedToManagers(restrictionKeys.contains(USAGE_MANAGERS));
	}

	@Override
	protected void doDispose() {
		//
	}

}
