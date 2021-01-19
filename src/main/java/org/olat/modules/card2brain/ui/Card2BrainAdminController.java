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
package org.olat.modules.card2brain.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.card2brain.Card2BrainManager;
import org.olat.modules.card2brain.Card2BrainModule;
import org.olat.modules.card2brain.manager.Card2BrainVerificationResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Card2BrainAdminController extends FormBasicController {
	
	private static final String[] enabledKeys = new String[]{"on"};
	private static final String FORM_MISSING_MANDATORY = "form.legende.mandatory";
	
	private MultipleSelectionElement enabledEl;
	private MultipleSelectionElement enterpriseLoginEnabledEl;
	private MultipleSelectionElement privateLoginEnabledEl;
	private TextElement enterpriseKeyEl;
	private TextElement enterpriseSecretEl;
	private FormLink checkLoginButton;
	private TextElement baseUrlEl;
	private TextElement peekViewUrlEl;
	private TextElement verifyLtiUrlEl;

	@Autowired
	private Card2BrainModule card2BrainModule;
	@Autowired
	private Card2BrainManager card2BrainManager;

	public Card2BrainAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormContextHelp("KnowledgeTransfer#_card2brain_config");
		
		setFormDescription("admin.description");
		
		String[] enableValues = new String[]{ translate("on") };		
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, enabledKeys, enableValues);
		if (card2BrainModule.isEnabled()) {
			enabledEl.select(enabledKeys[0], true);
		}
		
		uifactory.addSpacerElement("Spacer", formLayout, false);
				
		enterpriseLoginEnabledEl = uifactory.addCheckboxesHorizontal("admin.enterpriseLoginEnabled", formLayout, enabledKeys, enableValues);
		if (card2BrainModule.isEnterpriseLoginEnabled()) {
			enterpriseLoginEnabledEl.select(enabledKeys[0], true);
		}
		enterpriseLoginEnabledEl.setHelpTextKey("admin.enterpriseLoginHelpText", null);
		enterpriseLoginEnabledEl.setHelpUrl(translate("admin.enterpriseLoginHelpUrl"));
		enterpriseLoginEnabledEl.addActionListener(FormEvent.ONCHANGE);

		String enterpriseKey = card2BrainModule.getEnterpriseKey();
		enterpriseKeyEl = uifactory.addTextElement("admin.enterpriseKey", "admin.enterpriseKey", 128, enterpriseKey, formLayout);
		enterpriseKeyEl.setMandatory(true);
		
		String enterpriseSecret = card2BrainModule.getEnterpriseSecret();
		enterpriseSecretEl = uifactory.addTextElement("admin.enterpriseSecret", "admin.enterpriseSecret", 128, enterpriseSecret, formLayout);
		enterpriseSecretEl.setMandatory(true);
		
		checkLoginButton = uifactory.addFormLink("admin.verifyKeySecret.button", formLayout, "btn btn-default");
		
		uifactory.addSpacerElement("Spacer", formLayout, false);

		privateLoginEnabledEl = uifactory.addCheckboxesHorizontal("admin.privateLoginEnabled", formLayout, enabledKeys, enableValues);
		if (card2BrainModule.isPrivateLoginEnabled()) {
			privateLoginEnabledEl.select(enabledKeys[0], true);
		}
		privateLoginEnabledEl.setHelpTextKey("admin.privateLoginHelpText", null);
		privateLoginEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("Spacer", formLayout, false);
		uifactory.addStaticTextElement("admin.expertSettings", null, formLayout);
		
		String baseUrl = card2BrainModule.getBaseUrl();
		baseUrlEl = uifactory.addTextElement("admin.baseUrl", "admin.baseUrl", 128, baseUrl, formLayout);
		baseUrlEl.setMandatory(true);
		baseUrlEl.setHelpTextKey("admin.baseUrlHelpText", null);
		
		String peekViewUrl = card2BrainModule.getPeekViewUrl();
		peekViewUrlEl = uifactory.addTextElement("admin.peekViewUrl", "admin.peekViewUrl", 128, peekViewUrl, formLayout);
		peekViewUrlEl.setMandatory(true);
		peekViewUrlEl.setHelpTextKey("admin.peekViewUrlHelpText", null);
		
		String verifyLtiUrl = card2BrainModule.getVerifyLtiUrl();
		verifyLtiUrlEl = uifactory.addTextElement("admin.verifyKeySecret.url", "admin.verifyKeySecret.url", 128, verifyLtiUrl, formLayout);
		verifyLtiUrlEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		
		showHideEnterpriseLoginFields();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enterpriseLoginEnabledEl == source) {
			showHideEnterpriseLoginFields();
		} else if (checkLoginButton == source) {
			checkKeySecret();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		card2BrainModule.setEnabled(enabled);

		boolean enterpriseLoginEnabled = enterpriseLoginEnabledEl.isAtLeastSelected(1);
		card2BrainModule.setEnterpriseLoginEnabled(enterpriseLoginEnabled);
		
		boolean privateLoginEnabled = privateLoginEnabledEl.isAtLeastSelected(1);
		card2BrainModule.setPrivateLoginEnabled(privateLoginEnabled);
		
		String enterpriseKey = enterpriseKeyEl.getValue();
		card2BrainModule.setEnterpriseKey(enterpriseKey);
		
		String enterpriseSecret = enterpriseSecretEl.getValue();
		card2BrainModule.setEnterpriseSecret(enterpriseSecret);
		
		String baseUrl = baseUrlEl.getValue();
		card2BrainModule.setBaseUrl(baseUrl);
		
		String peekViewUrl = peekViewUrlEl.getValue();
		card2BrainModule.setPeekViewUrl(peekViewUrl);
		
		String verifyLtiUrl = verifyLtiUrlEl.getValue();
		card2BrainModule.setVerifyLtiUrl(verifyLtiUrl);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		if(card2BrainModule.isEnabled()) {
			if (isEnterpriseLoginEnabled()) {
				allOk &= validateIsMandatory(enterpriseKeyEl);
				allOk &= validateIsMandatory(enterpriseSecretEl);
			}
			allOk &= validateIsMandatory(baseUrlEl);
			allOk &= validateIsMandatory(peekViewUrlEl);
			allOk &= validateIsMandatory(verifyLtiUrlEl);
		}
		
		return allOk;
	}

	private boolean validateIsMandatory(TextElement textElement) {
		boolean allOk = true;
		
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey(FORM_MISSING_MANDATORY, null);
			allOk &= false;
		}
		
		return allOk;
	}

	private void showHideEnterpriseLoginFields() {
		enterpriseKeyEl.setVisible(isEnterpriseLoginEnabled());
		enterpriseSecretEl.setVisible(isEnterpriseLoginEnabled());
		checkLoginButton.setVisible(isEnterpriseLoginEnabled());
	}
	
	private boolean isEnterpriseLoginEnabled() {
		return enterpriseLoginEnabledEl.isAtLeastSelected(1);
	}
	
	private void checkKeySecret() {
		String verifyLtiUrl = verifyLtiUrlEl.getValue();
		String key = enterpriseKeyEl.getValue();
		String secret = enterpriseSecretEl.getValue();

		Card2BrainVerificationResult verification = 
				card2BrainManager.checkEnterpriseLogin(verifyLtiUrl, key, secret);
		if(verification == null) {
			showError("admin.verifyKeySecret.unavaible");
		} else if (verification.isSuccess()) {
			showInfo("admin.verifyKeySecret.valid");
		} else {
			showError("admin.verifyKeySecret.invalid", verification.getMessage());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
