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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.card2brain.Card2BrainModule;
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
	private TextElement enterpriseKeyEl;
	private TextElement enterpriseSecretEl;
	private TextElement baseUrlEl;
	private TextElement peekViewUrlEl;

	@Autowired
	private Card2BrainModule card2BrainModule;

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
		enabledEl.select(enabledKeys[0], card2BrainModule.isEnabled());
		
		uifactory.addSpacerElement("Spacer", formLayout, false);
				
		enterpriseLoginEnabledEl = uifactory.addCheckboxesHorizontal("admin.enterpriseLoginEnabled", formLayout, enabledKeys, enableValues);
		enterpriseLoginEnabledEl.select(enabledKeys[0], card2BrainModule.isEnterpriseLoginEnabled());
		enterpriseLoginEnabledEl.setHelpTextKey("admin.enterpriseLoginHelpText", null);
		enterpriseLoginEnabledEl.setHelpUrl(translate("admin.enterpriseLoginHelpUrl"));
		enterpriseLoginEnabledEl.addActionListener(FormEvent.ONCHANGE);

		String enterpriseKey = card2BrainModule.getEnterpriseKey();
		enterpriseKeyEl = uifactory.addTextElement("admin.enterpriseKey", "admin.enterpriseKey", 128, enterpriseKey, formLayout);
		enterpriseKeyEl.setMandatory(true);
		
		String enterpriseSecret = card2BrainModule.getEnterpriseSecret();
		enterpriseSecretEl = uifactory.addPasswordElement("admin.enterpriseSecret", "admin.enterpriseSecret", 128, enterpriseSecret, formLayout);
		enterpriseSecretEl.setMandatory(true);
		
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
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		
		showHideEnterpriseLoginFields();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enterpriseLoginEnabledEl == source) {
			showHideEnterpriseLoginFields();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		card2BrainModule.setEnabled(enabled);

		boolean enterpriseLoginEnabled = enterpriseLoginEnabledEl.isAtLeastSelected(1);
		card2BrainModule.setEnterpriseLoginEnabled(enterpriseLoginEnabled);
		
		String enterpriseKey = enterpriseKeyEl.getValue();
		card2BrainModule.setEnterpriseKey(enterpriseKey);
		
		String enterpriseSecret = enterpriseSecretEl.getValue();
		card2BrainModule.setEnterpriseSecret(enterpriseSecret);
		
		String baseURL = baseUrlEl.getValue();
		card2BrainModule.setBaseUrl(baseURL);
		
		String peekViewURL = peekViewUrlEl.getValue();
		card2BrainModule.setPeekViewUrl(peekViewURL);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		//validate only if the module is enabled
		if(card2BrainModule.isEnabled()) {
			allOk &= validateEnterpriseLogin();
			allOk &= validateBaseUrl();
			allOk &= validatePeekViewUrl();
		}
		
		return allOk && super.validateFormLogic(ureq);
	}
	
	private boolean validateEnterpriseLogin() {
		boolean allOk = true;
		
		if (isEnterpriseLoginEnabled()) {
			if (!StringHelper.containsNonWhitespace(enterpriseKeyEl.getValue())) {
				enterpriseKeyEl.setErrorKey(FORM_MISSING_MANDATORY, null);
				allOk &= false;
			}
			if (!StringHelper.containsNonWhitespace(enterpriseSecretEl.getValue())) {
				enterpriseSecretEl.setErrorKey(FORM_MISSING_MANDATORY, null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	private boolean validateBaseUrl() {
		boolean allOk = true;
		
		if (!StringHelper.containsNonWhitespace(baseUrlEl.getValue())) {
			baseUrlEl.setErrorKey(FORM_MISSING_MANDATORY, null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validatePeekViewUrl() {
		boolean allOk = true;
		
		if (!StringHelper.containsNonWhitespace(peekViewUrlEl.getValue())) {
			peekViewUrlEl.setErrorKey(FORM_MISSING_MANDATORY, null);
			allOk &= false;
		}
		
		return allOk;
	}

	private void showHideEnterpriseLoginFields() {
		enterpriseKeyEl.setVisible(isEnterpriseLoginEnabled());
		enterpriseSecretEl.setVisible(isEnterpriseLoginEnabled());
	}
	
	private boolean isEnterpriseLoginEnabled() {
		return enterpriseLoginEnabledEl.isAtLeastSelected(1);
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
