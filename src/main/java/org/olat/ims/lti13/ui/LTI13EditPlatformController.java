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
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13EditPlatformController extends FormBasicController {
	
	private static final String[] matchingKeys = new String[] { "yes", "no" };

	private TextElement nameEl;
	private SingleSelection emailMatchingEl;
	private TextElement issuerEl;
	private TextElement clientIdEl;
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	private TextElement authorizationUriEl;
	private TextElement tokenUriEl;
	private TextElement jwkSetUriEl;
	
	private LTI13Platform platform;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13EditPlatformController(UserRequest ureq, WindowControl wControl, LTI13Platform platform) {
		super(ureq, wControl);
		this.platform = platform;
		initForm(ureq);
		updatePublicKeyUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = platform.getName();
		nameEl = uifactory.addTextElement("platform.name", "platform.name", 255, name, formLayout);
		nameEl.setMandatory(true);
		
		String[] matchingValues = new String[] { translate("yes"), translate("no") };
		emailMatchingEl = uifactory.addRadiosHorizontal("platform.mail.matching", "platform.mail.matching", formLayout, matchingKeys, matchingValues);
		emailMatchingEl.setExampleKey("platform.mail.matching.hint", null);
		if(platform.isEmailMatching()) {
			emailMatchingEl.select(matchingKeys[0], true);
		} else {
			emailMatchingEl.select(matchingKeys[1], true);
		}
		
		String issuer = platform.getIssuer();
		issuerEl = uifactory.addTextElement("tool.issuer", "tool.issuer", 255, issuer, formLayout);
		issuerEl.setMandatory(true);
		
		String clientId = platform.getClientId();
		clientIdEl = uifactory.addTextElement("tool.client.id", "tool.client.id", 255, clientId, formLayout);
		clientIdEl.setMandatory(true);
		
		String loginInitiationUrl = lti13Module.getToolLoginInitiationUri();
		uifactory.addStaticTextElement("tool.login.initiation", loginInitiationUrl, formLayout);
		String loginRedirectUrl = lti13Module.getToolLoginRedirectUri();
		uifactory.addStaticTextElement("tool.login.redirection", loginRedirectUrl, formLayout);
		
		SelectionValues kValues = new SelectionValues();
		kValues.add(SelectionValues.entry(PublicKeyType.KEY.name(), translate("tool.public.key.type.key")));
		kValues.add(SelectionValues.entry(PublicKeyType.URL.name(), translate("tool.public.key.type.url")));
		publicKeyTypeEl = uifactory.addDropdownSingleselect("tool.public.key.type", "tool.public.key.type", formLayout, kValues.keys(), kValues.values());
		publicKeyTypeEl.addActionListener(FormEvent.ONCHANGE);
		publicKeyTypeEl.select(kValues.keys()[0], true);
		
		String publicKey = platform.getPublicKey();
		publicKeyEl = uifactory.addTextAreaElement("tool.public.key.value", "tool.public.key.value", -1, 15, 60, false, true, true, publicKey, formLayout);
		publicKeyEl.setEnabled(false);
		
		String publicKeyUrl = platform.getPublicKeyUrl();
		publicKeyUrlEl = uifactory.addTextElement("tool.public.key.url", "tool.public.key.url", 255, publicKeyUrl, formLayout);
		publicKeyUrlEl.setEnabled(false);
		
		String authorizationUrl = platform.getAuthorizationUri();
		authorizationUriEl = uifactory.addTextElement("tool.authorization.url", "tool.authorization.url", 255, authorizationUrl, formLayout);
		authorizationUriEl.setMandatory(true);
		String tokenUrl = platform.getTokenUri();
		tokenUriEl = uifactory.addTextElement("tool.token.url", "tool.token.url", 255, tokenUrl, formLayout);
		tokenUriEl.setMandatory(true);
		String jwkSetUri = platform.getJwkSetUri();
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

		allOk &= validateMandatory(emailMatchingEl);
		allOk &= validateMandatory(nameEl, 255);
		allOk &= validateMandatory(issuerEl, 255);
		allOk &= validateMandatory(clientIdEl, 128);
		allOk &= validateMandatory(authorizationUriEl, 2000);
		allOk &= validateMandatory(tokenUriEl, 2000);
		allOk &= validateMandatory(jwkSetUriEl, 2000);
		allOk &= validateMandatory(publicKeyTypeEl);

		return allOk;
	}
	
	private boolean validateMandatory(SingleSelection el) {
		boolean allOk = true;
		
		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateMandatory(TextElement el, int maxLength) {
		boolean allOk = true;
		
		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(val.length() > maxLength) {
			el.setErrorKey("form.error.toolong", new String[] { Integer.toString(maxLength) } );
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
		platform.setName(nameEl.getValue());
		platform.setEmailMatching(emailMatchingEl.isSelected(0));
		platform.setIssuer(issuerEl.getValue());
		platform.setClientId(clientIdEl.getValue());
		platform.setAuthorizationUri(authorizationUriEl.getValue());
		platform.setTokenUri(tokenUriEl.getValue());
		platform.setJwkSetUri(jwkSetUriEl.getValue());
		
		platform = lti13Service.updatePlatform(platform);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
