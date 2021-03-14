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
package org.olat.ims.lti13.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13EditSharedToolController extends FormBasicController {

	private TextElement issuerEl;
	private TextElement clientIdEl;
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	private TextElement authorizationUriEl;
	private TextElement tokenUriEl;
	private TextElement jwkSetUriEl;
	
	private LTI13SharedTool sharedTool;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13EditSharedToolController(UserRequest ureq, WindowControl wControl, LTI13SharedTool sharedTool) {
		super(ureq, wControl);
		this.sharedTool = sharedTool;
		initForm(ureq);
		updatePublicKeyUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String issuer = sharedTool.getIssuer();
		issuerEl = uifactory.addTextElement("tool.issuer", "tool.issuer", 255, issuer, formLayout);
		issuerEl.setMandatory(true);
		
		String clientId = sharedTool.getClientId();
		clientIdEl = uifactory.addTextElement("tool.client.id", "tool.client.id", 255, clientId, formLayout);
		clientIdEl.setMandatory(true);
		
		// tools url
		String url = sharedTool.getToolUrl();
		uifactory.addStaticTextElement("tool.url", url, formLayout);
		String loginInitiationUrl = lti13Module.getToolLoginInitiationUri();
		uifactory.addStaticTextElement("tool.login.initiation", loginInitiationUrl, formLayout);
		String loginRedirectUrl = lti13Module.getToolLoginRedirectUri();
		uifactory.addStaticTextElement("tool.login.redirection", loginRedirectUrl, formLayout);
		
		KeyValues kValues = new KeyValues();
		kValues.add(KeyValues.entry(PublicKeyType.KEY.name(), translate("tool.public.key.type.key")));
		kValues.add(KeyValues.entry(PublicKeyType.URL.name(), translate("tool.public.key.type.url")));
		publicKeyTypeEl = uifactory.addDropdownSingleselect("tool.public.key.type", "tool.public.key.type", formLayout, kValues.keys(), kValues.values());
		publicKeyTypeEl.addActionListener(FormEvent.ONCHANGE);
		publicKeyTypeEl.select(kValues.keys()[0], true);
		
		String publicKey = sharedTool.getPublicKey();
		publicKeyEl = uifactory.addTextAreaElement("tool.public.key.value", "tool.public.key.value", -1, 15, 60, false, true, true, publicKey, formLayout);
		publicKeyEl.setEnabled(false);
		
		String publicKeyUrl = sharedTool.getKeyId();
		publicKeyUrlEl = uifactory.addTextElement("tool.public.key.url", "tool.public.key.url", 255, publicKeyUrl, formLayout);
		publicKeyUrlEl.setEnabled(false);
		
		String authorizationUrl = sharedTool.getAuthorizationUri();
		authorizationUriEl = uifactory.addTextElement("tool.authorization.url", "tool.authorization.url", 255, authorizationUrl, formLayout);
		authorizationUriEl.setMandatory(true);
		String tokenUrl = sharedTool.getTokenUri();
		tokenUriEl = uifactory.addTextElement("tool.token.url", "tool.token.url", 255, tokenUrl, formLayout);
		tokenUriEl.setMandatory(true);
		String jwkSetUri = sharedTool.getJwkSetUri();
		jwkSetUriEl = uifactory.addTextElement("tool.jwk.set.url", "tool.jwk.set.url", 255, jwkSetUri, formLayout);
		jwkSetUriEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void updatePublicKeyUI() {
		if(!publicKeyTypeEl.isOneSelected()) return;
		
		String selectedKey = publicKeyTypeEl.getSelectedKey();
		publicKeyEl.setVisible(PublicKeyType.KEY.name().equals(selectedKey));
		publicKeyUrlEl.setVisible(PublicKeyType.URL.name().equals(selectedKey));
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateMandatory(issuerEl);
		allOk &= validateMandatory(clientIdEl);
		allOk &= validateMandatory(authorizationUriEl);
		allOk &= validateMandatory(tokenUriEl);
		allOk &= validateMandatory(jwkSetUriEl);
		allOk &= validatePublicKey();

		return allOk;
	}
	
	private boolean validatePublicKey() {
		boolean allOk = true;
		
		publicKeyTypeEl.clearError();
		publicKeyEl.clearError();
		publicKeyUrlEl.clearError();
		
		if(!publicKeyTypeEl.isOneSelected()) {
			publicKeyTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(PublicKeyType.KEY.name().equals(publicKeyTypeEl.getSelectedKey())) {
			allOk = validateMandatory(publicKeyEl);
		} else if(PublicKeyType.URL.name().equals(publicKeyTypeEl.getSelectedKey())) {
			allOk = validateMandatory(publicKeyUrlEl);
		}
		
		return allOk;
	}
	
	private boolean validateMandatory(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publicKeyTypeEl == source) {
			updatePublicKeyUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		sharedTool.setIssuer(issuerEl.getValue());
		sharedTool.setClientId(clientIdEl.getValue());
		sharedTool.setAuthorizationUri(authorizationUriEl.getValue());
		sharedTool.setTokenUri(tokenUriEl.getValue());
		sharedTool.setJwkSetUri(jwkSetUriEl.getValue());
		
		sharedTool = lti13Service.updateSharedTool(sharedTool);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
