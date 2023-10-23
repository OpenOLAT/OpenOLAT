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
package org.olat.login.webauthn.ui;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.model.CredentialCreation;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;

/**
 * 
 * Initial date: 26 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewPasskeyController extends FormBasicController {
	
	private FormLink laterButton;
	
	private Identity identityPasskey;
	private final boolean withCancel;
	private final boolean withLaterOption;
	private final boolean transientPasskey;
	private CredentialCreation registrationData;
	private final boolean deleteOlatAuthentication;
	private final String username;
	
	private Authentication passkeyAuthentication;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;

	public NewPasskeyController(UserRequest ureq, WindowControl wControl, Identity identityPasskey,
			boolean deleteOlatAuthentication, boolean withLaterOption, boolean withCancel) {
		super(ureq, wControl, "passkey_new");
		this.withCancel = withCancel;
		this.identityPasskey = identityPasskey;
		transientPasskey = false;
		username = identityPasskey.getUser().getNickName();
		this.withLaterOption = withLaterOption;
		this.deleteOlatAuthentication = deleteOlatAuthentication;
		initForm(ureq);
	}
	
	public NewPasskeyController(UserRequest ureq, WindowControl wControl, String username,
			boolean deleteOlatAuthentication, boolean withLaterOption, boolean withCancel) {
		super(ureq, wControl, "passkey_new");
		this.withCancel = withCancel;
		this.username = username;
		transientPasskey = true;
		this.withLaterOption = withLaterOption;
		this.deleteOlatAuthentication = deleteOlatAuthentication;
		initForm(ureq);
	}
	
	public Identity getIdentityToPasskey() {
		return identityPasskey;
	}

	public Authentication getPasskeyAuthentication() {
		return passkeyAuthentication;
	}

	public void setFormInfo(String info, String infoUrl) {
		flc.setFormInfo(info);
		flc.setFormInfoHelp(infoUrl);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] jss = new String[] {
			"js/passkey/passkey.js"
		};
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", jss);
		formLayout.add("js", js);
		
		uifactory.addFormSubmitButton("new.passkey", formLayout);
		laterButton = uifactory.addFormLink("later", formLayout, Link.BUTTON);
		laterButton.setVisible(withLaterOption);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(withCancel);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(laterButton == source) {
			doLater(ureq);
		} else {
			String type = ureq.getParameter("type");
			if("registration".equals(type)) {
				String clientDataJSON = ureq.getParameter("clientDataJSON");
				if(StringHelper.containsNonWhitespace(clientDataJSON)) {
					String transports = ureq.getParameter("transports");
					String attestationObject = ureq.getParameter("attestationObject");
					doValidateRegistration(ureq, registrationData, clientDataJSON, attestationObject, transports);
					flc.contextPut("credentialCreate", Boolean.FALSE);
				}
			} else if("registration-error".equals(type) || "request-error".equals(type)) {
				doError();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		registrationData = fillRegistration();
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		flc.contextPut("credentialCreate", Boolean.FALSE);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doLater(UserRequest ureq) {
		flc.contextPut("credentialCreate", Boolean.FALSE);
		webAuthnManager.incrementPasskeyCounter(identityPasskey);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doError() {
		flc.contextPut("off_error", translate("error.unkown"));
		flc.contextPut("credentialCreate", Boolean.FALSE);
	}
	
	private void doValidateRegistration(UserRequest ureq, CredentialCreation registration,
			String clientDataBase64, String attestationObjectBase64, String transports) {
		passkeyAuthentication = webAuthnManager.validateRegistration(registration, clientDataBase64, attestationObjectBase64, transports);
		if(passkeyAuthentication != null) {
			if(transientPasskey) {
				// do nothing
			} else if(deleteOlatAuthentication) {
				Authentication olatAuthenticatin = securityManager.findAuthentication(identityPasskey, "OLAT", BaseSecurity.DEFAULT_ISSUER);
				if(olatAuthenticatin != null) {
					securityManager.deleteAuthentication(olatAuthenticatin);
				}
			}
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private CredentialCreation fillRegistration() {
		CredentialCreation credentialCreation = webAuthnManager.prepareCredentialCreation(username, identityPasskey);
		
		// Relying party information
		ServerProperty serverProperty = credentialCreation.serverProperty();
		flc.contextPut("rpId", serverProperty.getRpId());
		flc.contextPut("rpName", Settings.getApplicationName());
		Challenge challenge = serverProperty.getChallenge();
		flc.contextPut("challenge", webAuthnManager.encodeToString(challenge.getValue()));
		
		flc.contextPut("timeout", Integer.valueOf(loginModule.getPasskeyTimeout() * 1000));
		flc.contextPut("userVerification", loginModule.getPasskeyUserVerification().getValue());
		flc.contextPut("attestation", loginModule.getPasskeyAttestationConveyancePreference().getValue());	
		
		// Identity
		flc.contextPut("userName", credentialCreation.userName());
		if(identityPasskey != null) {
			flc.contextPut("userDisplayName", webAuthnManager.getUserDisplayName(identityPasskey));
		}
		flc.contextPut("userId", credentialCreation.userId());
		flc.contextPut("credentialCreate", Boolean.TRUE);
		return credentialCreation;
	}

}
