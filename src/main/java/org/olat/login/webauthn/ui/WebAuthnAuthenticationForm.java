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
package org.olat.login.webauthn.ui;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginEvent;
import org.olat.login.LoginModule;
import org.olat.login.PasswordBasedLoginManager;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.model.CredentialRequest;
import org.olat.user.ChangePasswordForm;
import org.olat.user.ui.identity.UserOpenOlatAuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.validator.exception.ValidationException;


/**
 * This authentication form can do:
 * <ul>
 *  <li>OLAT login with or without WebAuthn</li>
 *  <li>LDAP login</li>
 *  <li>Tocco</li>
 *  <li>PerformX</li>
 * </ul>
 * 
 * Initial date: 3 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebAuthnAuthenticationForm extends FormBasicController {

	private TextElement loginEl;
	private TextElement pass;
	private TextElement recoveryKeyEl;
	private FormLink backButton;
	private FormLink notNowButton;
	private FormLink tryAgainButton;
	private FormLink recoveryKeyButton;
	private FormSubmit submitButton;
	private StaticTextElement usernameEl;
	
	private Flow step = Flow.username;
	private CredentialRequest requestData;
	private Identity authenticatedIdentity;

	private CloseableModalController cmc;
	private ChangePasswordForm newPasswordCtrl;
	private NewPasskeyController newPasskeyCtrl;
	private RecoveryKeysController recoveryKeysCtrl;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	@Autowired
	private PasswordBasedLoginManager loginManager;
	@Autowired
	private OLATWebAuthnManager olatWebAuthnManager;
	
	/**
	 * Login form used by the OLAT Authentication Provider
	 * @param name
	 */
	public WebAuthnAuthenticationForm(UserRequest ureq, WindowControl wControl, String id, Translator translator) {
		super(ureq, wControl, id, "passkey", Util.createPackageTranslator(UserOpenOlatAuthenticationController.class, ureq.getLocale(), translator));
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
		recoveryKeyEl.setElementCssClass("o_sel_auth_recovery_key");
		recoveryKeyEl.setVisible(false);
		
		pass  = uifactory.addPasswordElement(mainForm.getFormId() + "_pass", "lf_pass",  "lf.pass", 128, "", formLayout);
		pass.setAutocomplete("current-password");
		pass.setShowHideEye(true);
		pass.setVisible(false);
		
		String buttonsPage = velocity_root + "/passkey_buttons.html";
		FormLayoutContainer buttonsLayout = uifactory.addCustomFormLayout("buttons", null, buttonsPage, formLayout);

		recoveryKeyButton = uifactory.addFormLink("use.recovery.key", buttonsLayout, Link.BUTTON);
		recoveryKeyButton.setElementCssClass("o_sel_auth_recovery_key_send");
		recoveryKeyButton.setVisible(false);
		
		tryAgainButton = uifactory.addFormLink("try.again", buttonsLayout, Link.BUTTON);
		tryAgainButton.setElementCssClass("btn btn-primary");
		tryAgainButton.setVisible(false);
		
		notNowButton = uifactory.addFormLink("not.now", buttonsLayout, Link.BUTTON);
		notNowButton.setVisible(false);
		
		backButton = uifactory.addFormLink("back", buttonsLayout, Link.BUTTON);
		backButton.setVisible(false);
		
		String submitId = mainForm.getFormId() + "_button";
		submitButton = uifactory.addFormSubmitButton(submitId, "login.button", "login.button", null, buttonsLayout);
		submitButton.setElementCssClass("o_sel_auth_next");
		submitButton.setI18nKey("next", null);
		buttonsLayout.contextPut("submitId", submitButton.getComponent().getComponentName());
		
		// turn off the dirty message when leaving the login form without login in (e.g. pressing guest login)
		flc.getRootForm().setHideDirtyMarkingMessage(true);
		flc.getRootForm().setCsrfProtection(false);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		loginEl.clearError();
		//only POST is allowed
		if(!"POST".equals(ureq.getHttpReq().getMethod())) {
			setError("error.post.method.mandatory");
			valid &= false;
		}
		valid &= !loginEl.isEmpty("lf.error.loginempty");
		return valid;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newPasskeyCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				authenticatedIdentity = newPasskeyCtrl.getIdentityToPasskey();
				fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doShowRecoveryKey(ureq);
			} 
		} else if(newPasswordCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doFinishChangePassword(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(recoveryKeysCtrl == source) {
			if(cmc != null) {
				cmc.deactivate();
				cleanUp();
			}
			fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newPasswordCtrl);
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		newPasswordCtrl = null;
		newPasskeyCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(notNowButton == source) {
			if(authenticatedIdentity != null) {
				fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
			}
		} else if(recoveryKeyButton == source) {
			doRecovery();
		} else if(backButton == source) {
			doBack(ureq);
		}  else if(tryAgainButton == source) {
			doTryAgain();
		} else {
			String type = ureq.getParameter("type");
			if("request".equals(type)) {
				String clientDataJSON = ureq.getParameter("clientDataJSON");
				String authenticator = ureq.getParameter("authenticator");
				String signature = ureq.getParameter("signature");
				String userHandle = ureq.getParameter("userHandle");
				String rawId = ureq.getParameter("rawId");
				doValidateRequest(ureq, requestData, clientDataJSON, authenticator, rawId, signature, userHandle);
			} else if("request-error".equals(type)) {
				String message = ureq.getParameter("error-message");
				getLogger().warn("Authentication failed: {}", message);
				doError("error.unkown", true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(step == Flow.username) {
			doProcessUsername();
			fireEvent(ureq, new LoginEvent());
		} else if(step == Flow.loginWithPassword) {
			doAuthenticate(ureq, true);
		} else if(step == Flow.loginWithPassword2FA) {
			doAuthenticate(ureq, false);
		} else if(step == Flow.authenticatedWithPassword) {
			doNewPasskey(ureq, authenticatedIdentity);
		} else if(step == Flow.passkey && !requestData.matchUsername(loginEl.getValue())) {
			doProcessUsername();
		} else if(step == Flow.recovery) {
			doValidateRecovery(ureq);
		}
	}
	
	private void doError(String i18nKey, boolean withTryAgain) {
		step = Flow.username;

		recoveryKeyButton.setVisible(true);
		tryAgainButton.setVisible(withTryAgain);
		backButton.setVisible(true);
		
		notNowButton.setVisible(false);
		usernameEl.setVisible(false);
		usernameEl.setValue("");
		pass.setVisible(false);
		
		loginEl.setVisible(true);
		setError(i18nKey);

		updateUI(false);
		authenticatedIdentity = null;
		requestData = null;
	}
	
	private void doRecoveryError() {
		recoveryKeyButton.setVisible(true);
		tryAgainButton.setVisible(false);
		backButton.setVisible(true);
		
		notNowButton.setVisible(false);
		usernameEl.setVisible(false);
		usernameEl.setValue("");
		pass.setVisible(false);
		
		loginEl.setVisible(true);
		setError("error.recovery.key");
		recoveryKeyEl.setValue("");

		updateUI(false);
		authenticatedIdentity = null;
		requestData = null;
	}
	
	private void doProcessUsername() {
		String username = loginEl.getValue();
		clearError();
		loginEl.clearError();
		flc.contextRemove("off_error");
		if(StringHelper.containsNonWhitespace(username)) {
			List<Authentication> passkeyAuthentications = loginModule.isOlatProviderWithPasskey()
					? olatWebAuthnManager.getPasskeyAuthentications(username) : null;
			if(passkeyAuthentications != null && !passkeyAuthentications.isEmpty()) {
				// Has passkey, validate
				step = Flow.passkey;
				requestData = doPasskey(passkeyAuthentications);
			} else {
				step = Flow.loginWithPassword;
				updateUIForLoginWithPassword();
			}
		} else {
			loginEl.setErrorKey("form.legende.mandatory");
		}
	}
	
	private CredentialRequest doPasskey(List<Authentication> passkeyAuthentications) {
		clearError();
		CredentialRequest data = fillRequest(passkeyAuthentications);

		recoveryKeyButton.setVisible(false);
		tryAgainButton.setVisible(false);
		backButton.setVisible(false);
		submitButton.setVisible(false);
		return data;
	}
	
	private void doRecovery() {
		clearError();
		step = Flow.recovery;
		
		updateUIForLoginWithPassword();
		submitButton.setI18nKey("next", null);
		submitButton.setElementCssClass("o_sel_auth_next");
		submitButton.setVisible(true);
		pass.setVisible(false);
		recoveryKeyEl.setVisible(true);
	}
	
	private void updateUIForLoginWithPassword() {
		pass.setVisible(true);
		pass.setFocus(true);
		usernameEl.setValue("<i class='o_icon o_icon_user'> </i> " + loginEl.getValue());
		usernameEl.setVisible(true);
		recoveryKeyEl.setVisible(false);
		loginEl.setVisible(false);
		loginEl.setFocus(false);
		submitButton.setI18nKey("login.button", null);
		submitButton.setElementCssClass("o_sel_auth_password");
		submitButton.setVisible(true);
		recoveryKeyButton.setVisible(false);
		tryAgainButton.setVisible(false);
		backButton.setVisible(true);
		
		updateUI(false);
	}

	private void doAuthenticate(UserRequest ureq, boolean proposePasskey) {
		String login = loginEl.getValue();
		String pwd = pass.getValue();
		pass.setValue("");
		
		if (loginModule.isLoginBlocked(login)) {
			// do not proceed when already blocked
			setError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
			getLogger().info(Tracing.M_AUDIT, "Login attempt on already blocked login for {}. IP::{}", login, ureq.getHttpReq().getRemoteAddr());
		}  else {
			AuthenticationStatus status = new AuthenticationStatus();
			authenticatedIdentity = loginManager.authenticate(login, pwd, status);
			if(authenticatedIdentity == null) {
				if (loginModule.registerFailedLoginAttempt(login)) {
					logAudit("Too many failed login attempts for " + login + ". Login blocked. IP::" + ureq.getHttpReq().getRemoteAddr());
					setError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
				} else {
					setError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
				}
			} else if(Identity.STATUS_INACTIVE.equals(authenticatedIdentity.getStatus())) {
				setError("login.error.inactive", WebappHelper.getMailConfig("mailSupport"));
			} else {
				step = Flow.authenticatedWithPassword;
				
				pass.setVisible(false);
				submitButton.setVisible(false);
				notNowButton.setVisible(false);
				
				if(proposePasskey && loginModule.isOlatProviderWithPasskey() && "OLAT".equals(status.getProvider())) {
					// Propose passkey registration but only for OLAT provider
					Roles roles = securityManager.getRoles(authenticatedIdentity);
					PasskeyLevels levels = loginModule.getPasskeyLevel(roles);
					if(levels == PasskeyLevels.level1) {
						fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
					} else {
						doNewPasskey(ureq, authenticatedIdentity);
					}
				} else {
					fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
				}
			}
		}
	}
	
	private CredentialRequest fillRequest(List<Authentication> authentications) {
		CredentialRequest credentialRequest = olatWebAuthnManager.prepareCredentialRequest(authentications);
		fillRelyingPartySettings(credentialRequest.serverProperty());
		
		Challenge challenge = credentialRequest.serverProperty().getChallenge();
		flc.contextPut("challenge", olatWebAuthnManager.encodeToString(challenge.getValue()));
		flc.contextPut("allowCredentials", credentialRequest.credentials());

		updateUI(true);
		return credentialRequest;
	}

	private void doValidateRequest(UserRequest ureq, CredentialRequest request, String clientDataBase64, String authenticatorData,
			String rawId, String signature, String userHandle) {
		try {
			if(olatWebAuthnManager.validateRequest(request, clientDataBase64, authenticatorData, rawId, signature, userHandle)) {
				authenticatedIdentity = request.getAuthentication(rawId).getIdentity();
				List<Authentication> authentications = securityManager.getAuthentications(authenticatedIdentity);
				PasskeyLevels level = PasskeyLevels.currentLevel(authentications);
				if(level == PasskeyLevels.level3) {
					step = Flow.loginWithPassword2FA;
					updateUIForLoginWithPassword();
				} else {
					Roles roles = securityManager.getRoles(authenticatedIdentity);
					PasskeyLevels requiredLevel = loginModule.getPasskeyLevel(roles);
					if(requiredLevel == PasskeyLevels.level3 && level == PasskeyLevels.level2) {
						doNewPassword( ureq, authenticatedIdentity);
					} else {
						fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
					}
				}	
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
	private void doNewPasskey(UserRequest ureq, Identity authIdentity) {
		List<Authentication> authentications = securityManager.getAuthentications(authIdentity);
		PasskeyLevels currentLevel = PasskeyLevels.currentLevel(authentications);
		Roles roles = securityManager.getRoles(authIdentity);
		PasskeyLevels level = loginModule.getPasskeyLevel(roles);
		
		boolean delete = level == PasskeyLevels.level2 && currentLevel == PasskeyLevels.level1;
		
		long current = olatWebAuthnManager.getPasskeyCounter(authIdentity);
		long maxSkip = loginModule.getPasskeyMaxSkip();
		boolean withLater = maxSkip < 0 || (current + 1) <= maxSkip;
		newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), authIdentity, delete, withLater, false);
		
		String infoI18nKey;
		if(level == PasskeyLevels.level3 && currentLevel == PasskeyLevels.level1) {
			infoI18nKey = "new.passkey.level3.from.1.hint";
		} else if(level == PasskeyLevels.level3 &&currentLevel == PasskeyLevels.level2) {
			infoI18nKey = "new.passkey.level3.from.2.hint";
		} else {
			infoI18nKey = "new.passkey.level2.hint";
		}
		newPasskeyCtrl.setFormInfo(translate(infoI18nKey), UserOpenOlatAuthenticationController.HELP_URL);
		if(withLater) {
			String laterInfo = translate("new.passkey.later.hint", Long.toString(maxSkip - current));
			newPasskeyCtrl.setFormLaterInfo(laterInfo);
		}

		listenTo(newPasskeyCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, translate("new.passkey"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doNewPassword(UserRequest ureq, Identity authIdentity) {
		updateUI(false);
		
		List<Authentication> authentications = securityManager.getAuthentications(authIdentity);
		PasskeyLevels currentLevel = PasskeyLevels.currentLevel(authentications);
		Roles roles = securityManager.getRoles(authIdentity);
		PasskeyLevels level = loginModule.getPasskeyLevel(roles);
		
		newPasswordCtrl = new ChangePasswordForm(ureq, getWindowControl(), authIdentity, false);
		listenTo(newPasswordCtrl);
		
		if(level == PasskeyLevels.level3 && currentLevel == PasskeyLevels.level2) {
			newPasswordCtrl.setFormInfo(translate("new.password.level3.from.2.hint"), UserOpenOlatAuthenticationController.HELP_URL);
		}
		
		String title = translate("new.password.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasswordCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doFinishChangePassword(UserRequest ureq) {
		String newPwd = newPasswordCtrl.getNewPasswordValue();
		Identity identityToChange = newPasswordCtrl.getIdentityToChange();
		if(olatAuthenticationSpi.changePassword(identityToChange, identityToChange, newPwd)) {		
			getLogger().info(Tracing.M_AUDIT, "Changed password for identity: {}", identityToChange.getKey());
			authenticatedIdentity = identityToChange;
			fireEvent(ureq, new AuthenticationEvent(identityToChange));
		} else {
			doError("error.unkown", false);
		}
	}
	
	private void doShowRecoveryKey(UserRequest ureq) {
		recoveryKeysCtrl = new RecoveryKeysController(ureq, getWindowControl(), authenticatedIdentity);
		listenTo(recoveryKeysCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recoveryKeysCtrl.getInitialComponent(), true, translate("new.passkey"));
		cmc.activate();
		listenTo(cmc);
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
	
	private void updateUI(boolean request) {
		flc.contextPut("credentialRequest", Boolean.valueOf(request));
	}
	
	private void doBack(UserRequest ureq) {
		clearError();
		
		step = Flow.username;
		
		loginEl.setValue("");
		loginEl.setVisible(true);
		loginEl.setFocus(true);
		usernameEl.setValue("");
		usernameEl.setVisible(false);
		pass.setValue("");
		pass.setVisible(false);
		pass.setFocus(false);
		
		submitButton.setI18nKey("login.button", null);
		submitButton.setElementCssClass("o_sel_auth_password");
		submitButton.setVisible(true);
		
		recoveryKeyEl.setVisible(false);
		backButton.setVisible(false);
		recoveryKeyButton.setVisible(false);
		tryAgainButton.setVisible(false);
		
		updateUI(false);
		flc.setDirty(true);
		
		fireEvent(ureq, Event.BACK_EVENT);
	}
	
	private void doTryAgain() {
		flc.setDirty(true);
		if(step == Flow.username) {
			String username = loginEl.getValue();
			List<Authentication> passkeyAuthentications =  olatWebAuthnManager.getPasskeyAuthentications(username);
			requestData = doPasskey(passkeyAuthentications);
		}
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
		doRecoveryError();
	}
	
	private void clearError() {
		flc.contextRemove("error");
	}
	
	private void setError(String i18nKey, String... args) {
		flc.contextPut("error", translate(i18nKey, args));
	}
	
    public enum Flow {
    	username,
    	loginWithPassword,
    	loginWithPassword2FA,
    	recovery,
    	recoveryError,
    	authenticatedWithPassword,
    	passkeySuccessfullyCreated,
    	passkey
    }
}
