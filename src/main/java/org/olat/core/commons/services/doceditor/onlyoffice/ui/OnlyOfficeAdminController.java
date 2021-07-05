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
import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
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
	private MultipleSelectionElement editorEnabledEl;
	private MultipleSelectionElement mobileModeEl;
	private TextElement mobileQueryEl;
	private MultipleSelectionElement editEnabledEl;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;
	private TextElement licenseEditEl;
	private StaticTextElement licenseInUseEl;
	private MultipleSelectionElement usageRolesEl;
	private MultipleSelectionElement thumbnailsEnabledEl;

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
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");
		
		// General
		String[] enabledValues = translateAll(getTranslator(), ENABLED_KEYS);
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, enabledValues);
		enabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isEnabled());
		
		String url = onlyOfficeModule.getBaseUrl();
		baseUrlEl = uifactory.addTextElement("admin.base.url", 128, url, formLayout);
		baseUrlEl.setMandatory(true);
		
		String secret = onlyOfficeModule.getJwtSecret();
		jwtSecretEl = uifactory.addTextElement("admin.jwt.secret", 128, secret, formLayout);
		jwtSecretEl.setMandatory(true);
		
		// Editor
		uifactory.addSpacerElement("spacer.editor", formLayout, false);
		
		editorEnabledEl = uifactory.addCheckboxesHorizontal("admin.editor.enabled", formLayout, ENABLED_KEYS, enabledValues);
		editorEnabledEl.addActionListener(FormEvent.ONCHANGE);
		editorEnabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isEditorEnabled());
		
		SelectionValues mobileModesKV = new SelectionValues();
		mobileModesKV.add(SelectionValues.entry(Mode.VIEW.name(), translate("admin.mode.view")));
		mobileModesKV.add(SelectionValues.entry(Mode.EMBEDDED .name(), translate("admin.mode.embedded")));
		mobileModesKV.add(SelectionValues.entry(Mode.EDIT.name(), translate("admin.mode.edit")));
		mobileModeEl = uifactory.addCheckboxesHorizontal("admin.mobile.modes", formLayout, mobileModesKV.keys(), mobileModesKV.values());
		mobileModeEl.addActionListener(FormEvent.ONCHANGE);

		mobileQueryEl = uifactory.addTextElement("admin.mobile.query", 1000, "", formLayout);
		mobileQueryEl.setHelpTextKey("admin.mobile.query.help", null);

		dataTransferConfirmationEnabledEl = uifactory.addCheckboxesHorizontal(
				"admin.data.transfer.confirmation.enabled", formLayout, ENABLED_KEYS, enabledValues);
		
		editEnabledEl = uifactory.addCheckboxesHorizontal("admin.edit.enabled", formLayout, ENABLED_KEYS, enabledValues);
		editEnabledEl.setHelpTextKey("admin.edit.enabled.help", null);
		editEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		licenseEditEl = uifactory.addTextElement("admin.license.edit", 10, "", formLayout);
		
		licenseInUseEl = uifactory.addStaticTextElement("admin.license.edit.in.use", "", formLayout);
		
		SelectionValues usageRolesKV = new SelectionValues();
		usageRolesKV.add(entry(USAGE_AUTHOR, translate("admin.usage.roles.author")));
		usageRolesKV.add(entry(USAGE_COACH, translate("admin.usage.roles.coach")));
		usageRolesKV.add(entry(USAGE_MANAGERS, translate("admin.usage.roles.managers")));
		usageRolesEl = uifactory.addCheckboxesVertical("admin.usage.roles", formLayout, usageRolesKV.keys(), usageRolesKV.values(), 1);
		usageRolesEl.setHelpTextKey("admin.usage.roles.help", null);
		
		// Thumbnails
		uifactory.addSpacerElement("spacer.thumbnails", formLayout, false);
		
		thumbnailsEnabledEl = uifactory.addCheckboxesHorizontal("admin.thumbnails.enabled", formLayout, ENABLED_KEYS, enabledValues);
		thumbnailsEnabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isThumbnailsEnabled());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void initEditorValues() {
		for (Mode mode : onlyOfficeModule.getMobileModes()) {
			mobileModeEl.select(mode.name(), true);
		}
		
		mobileQueryEl.setValue(onlyOfficeModule.getMobileQuery());
		
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], onlyOfficeModule.isDataTransferConfirmationEnabled());
		
		Integer licenseEdit = onlyOfficeModule.getLicenseEdit();
		boolean editEnabled = licenseEdit == null || licenseEdit.intValue() >= 1;
		editEnabledEl.select(ENABLED_KEYS[0], editEnabled);
		
		usageRolesEl.select(USAGE_AUTHOR, onlyOfficeModule.isUsageRestrictedToAuthors());
		usageRolesEl.select(USAGE_COACH, onlyOfficeModule.isUsageRestrictedToCoaches());
		usageRolesEl.select(USAGE_MANAGERS, onlyOfficeModule.isUsageRestrictedToManagers());
	}

	private void initLicenseValues() {
		Integer licenseEdit = onlyOfficeModule.getLicenseEdit();
		String licenseEditValue = licenseEdit != null && licenseEdit.intValue() > -1? licenseEdit.toString() : null;
		licenseEditEl.setValue(licenseEditValue);
		
		Long editLicensesInUse = onlyOfficeService.getEditLicensesInUse();
		editLicensesInUse = editLicensesInUse != null? editLicensesInUse: 0;
		licenseInUseEl.setValue(editLicensesInUse.toString());
	}
	
	private void updateUI() {
		boolean editorEnabled = editorEnabledEl.isAtLeastSelected(1);
		if (editorEnabled) {
			initEditorValues();
		}
		mobileModeEl.setVisible(editorEnabled);
		dataTransferConfirmationEnabledEl.setVisible(editorEnabled);
		editEnabledEl.setVisible(editorEnabled);
		usageRolesEl.setVisible(editorEnabled);
		
		updateMobileUI();
		updateLicenseUI();
	}

	private void updateMobileUI() {
		boolean editorEnabled = editorEnabledEl.isAtLeastSelected(1);
		boolean mobileEnabled = mobileModeEl.isAtLeastSelected(1);
		
		mobileQueryEl.setVisible(editorEnabled && mobileEnabled);
	}

	private void updateLicenseUI() {
		boolean editorEnabled = editorEnabledEl.isAtLeastSelected(1);
		boolean editEnabled = editEnabledEl.isAtLeastSelected(1);
		
		if (editorEnabled && editEnabled) {
			initLicenseValues();
		}
		
		licenseEditEl.setVisible(editorEnabled && editEnabled);
		licenseInUseEl.setVisible(editorEnabled && editEnabled);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editorEnabledEl) {
			updateUI();
		} else if (source == editEnabledEl) {
			updateLicenseUI();
		} else if (source == mobileModeEl) {
			updateMobileUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(baseUrlEl);
			
			boolean jwtSecretOk = validateIsMandatory(jwtSecretEl);
			if (jwtSecretOk && !onlyOfficeSecurityService.isValidSecret(jwtSecretEl.getValue())) {
				jwtSecretEl.setErrorKey("admin.jwt.secret.invalid", null);
				jwtSecretOk = false;
			}
			allOk &= jwtSecretOk;
			
			if (mobileModeEl.isAtLeastSelected(1)) {
				allOk &= validateIsMandatory(mobileQueryEl);
			}
			
			allOk &= validatePositiveInteger(licenseEditEl);
		}
		
		return allOk;
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
		
		boolean editorEnabled = editorEnabledEl.isAtLeastSelected(1);
		onlyOfficeModule.setEditorEnabled(editorEnabled);
		
		if (editorEnabled) {
			Set<Mode> mobileModes = mobileModeEl.getSelectedKeys().stream().map(Mode::valueOf).collect(Collectors.toSet());
			onlyOfficeModule.setMobileModes(mobileModes);
			
			String mobileQuery = mobileQueryEl.getValue();
			onlyOfficeModule.setMobileQuery(mobileQuery);
			
			boolean dataTransferConfirmationEnabled = dataTransferConfirmationEnabledEl.isAtLeastSelected(1);
			onlyOfficeModule.setDataTransferConfirmationEnabled(dataTransferConfirmationEnabled);
			
			Integer licenseEdit = -1;
			boolean editEnabled = editEnabledEl.isAtLeastSelected(1);
			if (editEnabled) {
				String licenseEditValue = licenseEditEl.getValue();
				licenseEdit = StringHelper.containsNonWhitespace(licenseEditValue)
						? Integer.valueOf(licenseEditValue)
						: null;
			}
			onlyOfficeModule.setLicenseEdit(licenseEdit);
			
			Collection<String> restrictionKeys = usageRolesEl.getSelectedKeys();
			onlyOfficeModule.setUsageRestrictedToAuthors(restrictionKeys.contains(USAGE_AUTHOR));
			onlyOfficeModule.setUsageRestrictedToCoaches(restrictionKeys.contains(USAGE_COACH));
			onlyOfficeModule.setUsageRestrictedToManagers(restrictionKeys.contains(USAGE_MANAGERS));
		}
		
		boolean thumbnailsEnabled = thumbnailsEnabledEl.isAtLeastSelected(1);
		onlyOfficeModule.setThumbnailsEnabled(thumbnailsEnabled);
	}

	@Override
	protected void doDispose() {
		//
	}

}
