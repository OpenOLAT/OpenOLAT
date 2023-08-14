/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.login.webauthn.ui;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.Authentication;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.model.CredentialCreation;
import org.olat.login.webauthn.model.CredentialRequest;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.validator.exception.ValidationException;


/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 */
public class WebAuthnAuthenticationForm extends FormBasicController {

	private TextElement loginEl;
	private TextElement pass;
	private TextElement recoveryKeyEl;
	private FormLink notNowButton;
	private FormLink anotherWayButton;
	private FormLink recoveryKeyButton;
	private FormSubmit submitButton;
	private StaticTextElement usernameEl;
	private StaticTextElement passkeyCreatedEl;
	
	private Flow step = Flow.username;
	private CredentialRequest requestData;
	private CredentialCreation registrationData;
	private Identity authenticatedIdentity;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OLATWebAuthnManager olatWebAuthnManager;
	
	/**
	 * Login form used by the OLAT Authentication Provider
	 * @param name
	 */
	public WebAuthnAuthenticationForm(UserRequest ureq, WindowControl wControl, String id, Translator translator) {
		super(ureq, wControl, id, "passkey");
		setTranslator(translator);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("login.form");
		setFormDescription("login.intro");
		
		String[] jss = new String[] {
			"js/passkey/passkey.js"
		};
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", jss);
		formLayout.add("js", js);
	
		loginEl = uifactory.addTextElement(mainForm.getFormId() + "_name", "lf_login", "lf.login", 128, "", formLayout);
		loginEl.setAutocomplete("username webauthn");
		loginEl.setFocus(true);
		
		usernameEl = uifactory.addStaticTextElement(mainForm.getFormId() + "_uname", null, "", formLayout);
		usernameEl.setElementCssClass("o_login_username");
		usernameEl.setVisible(false);
		
		recoveryKeyEl = uifactory.addPasswordElement(mainForm.getFormId() + "_rkey", "lf_rkey",  "lf.rkey", 128, "", formLayout);
		recoveryKeyEl.setVisible(false);
		
		pass  = uifactory.addPasswordElement(mainForm.getFormId() + "_pass", "lf_pass",  "lf.pass", 128, "", formLayout);
		pass.setAutocomplete("current-password");
		pass.setVisible(false);
		
		passkeyCreatedEl = uifactory.addStaticTextElement(mainForm.getFormId() + "_pkey_created", null, translate("passkey.created"), formLayout);
		passkeyCreatedEl.setVisible(false);
		
		String buttonsPage = velocity_root + "/passkey_buttons.html";
		FormLayoutContainer buttonsLayout = uifactory.addCustomFormLayout("buttons", null, buttonsPage, formLayout);

		anotherWayButton = uifactory.addFormLink("another.way", buttonsLayout, Link.BUTTON);
		anotherWayButton.setGhost(true);
		anotherWayButton.setVisible(false);

		recoveryKeyButton = uifactory.addFormLink("use.recovery.key", buttonsLayout, Link.BUTTON);
		recoveryKeyButton.setGhost(true);
		recoveryKeyButton.setVisible(false);
		
		notNowButton = uifactory.addFormLink("not.now", buttonsLayout, Link.BUTTON);
		notNowButton.setGhost(true);
		notNowButton.setVisible(false);
		
		String submitId = mainForm.getFormId() + "_button";
		submitButton = uifactory.addFormSubmitButton(submitId, "login.button", "login.button", null, buttonsLayout);
		submitButton.setI18nKey("next", null);
		buttonsLayout.contextPut("submitId", submitButton.getComponent().getComponentName());
		
		// turn off the dirty message when leaving the login form without loggin in (e.g. pressing guest login)
		flc.getRootForm().setHideDirtyMarkingMessage(true);
		flc.getRootForm().setCsrfProtection(false);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		loginEl.clearError();
		//only POST is allowed
		if(!"POST".equals(ureq.getHttpReq().getMethod())) {
			loginEl.setErrorKey("error.post.method.mandatory");
			valid = false;
		}
		valid &= !loginEl.isEmpty("lf.error.loginempty");
		return valid;
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(notNowButton == source) {
			if(authenticatedIdentity != null) {
				fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
			}
		} else if(anotherWayButton == source) {
			doLoginAnotherWay();
		} else if(recoveryKeyButton == source) {
			doRecovery();
		} else {
			String type = ureq.getParameter("type");
			if("registration".equals(type)) {
				String clientDataJSON = ureq.getParameter("clientDataJSON");
				if(StringHelper.containsNonWhitespace(clientDataJSON)) {
					String attestationObject = ureq.getParameter("attestationObject");
					doValidateRegistration(registrationData, clientDataJSON, attestationObject);
				}
			} else if("registration-error".equals(type)) {
				doError("error.unkown");
			} else if("request".equals(type)) {
				String clientDataJSON = ureq.getParameter("clientDataJSON");
				String authenticator = ureq.getParameter("authenticator");
				String signature = ureq.getParameter("signature");
				String userHandle = ureq.getParameter("userHandle");
				String rawId = ureq.getParameter("rawId");
				doValidateRequest(ureq, requestData, clientDataJSON, authenticator, rawId, signature, userHandle);
			} else if("request-error".equals(type)) {
				doError("error.unkown");
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(step == Flow.username) {
			doRequestUsername();
		} else if(step == Flow.loginWithPassword) {
			doAuthenticate();
		} else if(step == Flow.loginAnotherWay) {
			doAuthenticateAnotherWay(ureq);
		} else if(step == Flow.authenticatedWithPassword) {
			registrationData = fillRegistration(authenticatedIdentity);
		} else if(step == Flow.passkeySuccessfullyCreated) {
			fireEvent(ureq, new AuthenticationEvent(registrationData.identity()));
		} else if(step == Flow.passkey && !requestData.matchUsername(loginEl.getValue())) {
			doRequestUsername();
		} else if(step == Flow.recovery) {
			doValidateRecovery(ureq);
		}
	}
	
	private void doError(String i18nKey) {
		step = Flow.username;

		anotherWayButton.setVisible(true);
		recoveryKeyButton.setVisible(true);
		
		notNowButton.setVisible(false);
		usernameEl.setVisible(false);
		usernameEl.setValue("");
		pass.setVisible(false);
		
		loginEl.setVisible(true);
		loginEl.setErrorKey(i18nKey);

		updateUI(false, false);
		authenticatedIdentity = null;
		registrationData = null;
		requestData = null;
	}
	
	private void doRequestUsername() {
		String username = loginEl.getValue();
		loginEl.clearError();
		flc.contextRemove("off_error");
		if(StringHelper.containsNonWhitespace(username)) {
			List<Authentication> passkeyAuthentications = olatWebAuthnManager.getPasskeyAuthentications(username);
			if(passkeyAuthentications != null && !passkeyAuthentications.isEmpty()) {
				// Has passkey, validate
				step = Flow.passkey;
				requestData = fillRequest(passkeyAuthentications);
				anotherWayButton.setVisible(true);
				recoveryKeyButton.setVisible(true);
			} else {
				step = Flow.loginWithPassword;
				updateUILoginWithPassword();
			}
		} else {
			loginEl.setErrorKey("form.legende.mandatory");
		}
	}
	
	private void doLoginAnotherWay() {
		step = Flow.loginAnotherWay;
		updateUILoginWithPassword();
		updateUI(false, false);
	}
	
	private void doRecovery() {
		step = Flow.recovery;
		
		updateUILoginWithPassword();
		submitButton.setI18nKey("next", null);
		pass.setVisible(false);
		recoveryKeyEl.setVisible(true);
	}
	
	private void updateUILoginWithPassword() {
		pass.setVisible(true);
		pass.setFocus(true);
		usernameEl.setValue("<i class='o_icon o_icon_user'> </i> " + loginEl.getValue());
		usernameEl.setVisible(true);
		recoveryKeyEl.setVisible(false);
		loginEl.setVisible(false);
		loginEl.setFocus(false);
		submitButton.setI18nKey("login.button", null);
		anotherWayButton.setVisible(false);
		recoveryKeyButton.setVisible(false);
	}

	private void doAuthenticate() {
		authenticatedIdentity = olatWebAuthnManager.authenticate(loginEl.getValue(), pass.getValue());
		if(authenticatedIdentity != null) {
			// Propose passkey registration
			step = Flow.authenticatedWithPassword;
			pass.setVisible(false);
			submitButton.setI18nKey("generate.passkey", null);
			notNowButton.setVisible(true);
		} else {
			showError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
		}
	}
	
	private void doAuthenticateAnotherWay(UserRequest ureq) {
		authenticatedIdentity = olatWebAuthnManager.authenticate(loginEl.getValue(), pass.getValue());
		if(authenticatedIdentity != null) {
			fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
		} else {
			showError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
		}
	}
	
	private CredentialRequest fillRequest(List<Authentication> authentications) {
		CredentialRequest credentialRequest = olatWebAuthnManager.prepareCredentialRequest(authentications);
		fillRelyingPartySettings(credentialRequest.serverProperty());
		
		Challenge challenge = credentialRequest.serverProperty().getChallenge();
		flc.contextPut("challenge", olatWebAuthnManager.encodeToString(challenge.getValue()));
		flc.contextPut("allowCredentials", credentialRequest.credentials());
		updateUI(false, true);
		return credentialRequest;
	}

	private void doValidateRequest(UserRequest ureq, CredentialRequest request, String clientDataBase64, String authenticatorData,
			String rawId, String signature, String userHandle) {
		try {
			if(olatWebAuthnManager.validateRequest(request, clientDataBase64, authenticatorData, rawId, signature, userHandle)) {
				fireEvent(ureq, new AuthenticationEvent(request.getAuthentication(rawId).getIdentity()));
			}
		} catch (DataConversionException | ValidationException e) {
			getLogger().error("", e);
		}
	}
	
	/**
	 * Propose passkey registration
	 * 
	 * @param authIdentity
	 * @return
	 */
	private CredentialCreation fillRegistration(Identity authIdentity) {
		String userName = loginEl.getValue();
		
		CredentialCreation credentialCreation = olatWebAuthnManager.prepareCredentialCreation(userName, authIdentity);
		// Relying party information
		fillRelyingPartySettings(credentialCreation.serverProperty());
		
		// Identity
		flc.contextPut("userName", credentialCreation.userName());
		flc.contextPut("userDisplayName", olatWebAuthnManager.getUserDisplayName(authIdentity));
		flc.contextPut("userId", credentialCreation.userId());
		
		updateUI(true, false);
		return credentialCreation;
	}
	
	private void fillRelyingPartySettings(ServerProperty serverProperty) {
		flc.contextPut("rpId", serverProperty.getRpId());
		flc.contextPut("rpName", Settings.getApplicationName());
		Challenge challenge = serverProperty.getChallenge();
		flc.contextPut("challenge", olatWebAuthnManager.encodeToString(challenge.getValue()));
		
		flc.contextPut("timeout", Integer.valueOf(loginModule.getPasskeyTimeout() * 1000));
		flc.contextPut("userVerification", loginModule.getPasskeyUserVerification().getValue());
		flc.contextPut("attestation", loginModule.getPasskeyAttestationConveyancePreference().getValue());	
	}
	
	private void updateUI(boolean creation, boolean request) {
		flc.contextPut("credentialCreate", Boolean.valueOf(creation));
		flc.contextPut("credentialRequest", Boolean.valueOf(request));
	}
	
	private void doValidateRecovery(UserRequest ureq) {
		String recoveryKey = recoveryKeyEl.getValue();
		String username = loginEl.getValue();
		
		List<Authentication> authentications = olatWebAuthnManager.getPasskeyAuthentications(username);
		Set<Identity> identities = authentications.stream().map(Authentication::getIdentity).collect(Collectors.toSet());
		for(Identity identity: identities) {
			if(olatWebAuthnManager.validateRecoveryKey(recoveryKey, identity)) {
				authenticatedIdentity = identity;
				fireEvent(ureq, new AuthenticationEvent(identity));
				return;
			}
		}
		
		// Error
		doError("error.recovery.key");
	}
	
	private void doValidateRegistration(CredentialCreation registration, String clientDataBase64, String attestationObjectBase64) {
		Authentication auth = olatWebAuthnManager.validateRegistration(registration, clientDataBase64, attestationObjectBase64);
		if(auth != null) {
			step = Flow.passkeySuccessfullyCreated;
			submitButton.setI18nKey("next", null);
			authenticatedIdentity = registration.identity();
			passkeyCreatedEl.setVisible(true);
			notNowButton.setVisible(false);
			anotherWayButton.setVisible(false);
			recoveryKeyButton.setVisible(false);
			recoveryKeyEl.setVisible(false);
			
			List<String> recoveryKeys = olatWebAuthnManager.generateRecoveryKeys(registration.identity());
			flc.contextPut("recoveryKeys", recoveryKeys);
		} else {
			authenticatedIdentity = null;
		}
	}
	
    public enum Flow {
    	username,
    	loginWithPassword,
    	loginAnotherWay,
    	recovery,
    	authenticatedWithPassword,
    	passkeySuccessfullyCreated,
    	passkey
    }
}
