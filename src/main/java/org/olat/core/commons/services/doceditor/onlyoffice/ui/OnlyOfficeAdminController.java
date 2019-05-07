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
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
	
	private MultipleSelectionElement enabledEl;
	private TextElement baseUrlEl;
	private TextElement jwtSecretEl;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;

	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
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
	}

	@Override
	protected void doDispose() {
		//
	}

}
