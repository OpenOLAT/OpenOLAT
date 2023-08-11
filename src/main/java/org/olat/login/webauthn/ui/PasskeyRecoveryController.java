package org.olat.login.webauthn.ui;

import org.olat.basesecurity.Authentication;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.model.CredentialCreation;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasskeyRecoveryController extends FormBasicController {
	
	private final TemporaryKey tempKey;
	private final Identity identityToChange;
	private CredentialCreation registrationData;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;

	public PasskeyRecoveryController(UserRequest ureq, WindowControl wControl, Identity identityToChange, TemporaryKey tempKey) {
		super(ureq, wControl, "recovery");
		this.tempKey = tempKey;
		this.identityToChange = identityToChange;
		
		if(tempKey != null && !identityToChange.getKey().equals(tempKey.getIdentityKey())) {
			throw new OLATRuntimeException("Temporary key doesn't match logged in user");
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormSubmitButton("recover.passkey", formLayout);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		String type = ureq.getParameter("type");
		if("registration".equals(type)) {
			String clientDataJSON = ureq.getParameter("clientDataJSON");
			if(StringHelper.containsNonWhitespace(clientDataJSON)) {
				String attestationObject = ureq.getParameter("attestationObject");
				doValidateRegistration(ureq, registrationData, clientDataJSON, attestationObject);
			}
		} else if("registration-error".equals(type)) {
			doError();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		registrationData = fillRegistration();
	}
	
	private void doError() {
		flc.contextPut("off_error", translate("error.unkown"));
		flc.contextPut("credentialCreate", Boolean.FALSE);
	}
	
	private void doValidateRegistration(UserRequest ureq, CredentialCreation registration,
			String clientDataBase64, String attestationObjectBase64) {
		Authentication auth = webAuthnManager.validateRegistration(registration, clientDataBase64, attestationObjectBase64);
		if(auth != null) {
			if(tempKey != null) {
				registrationManager.deleteTemporaryKey(tempKey);
			}
			fireEvent (ureq, Event.DONE_EVENT);
		}
	}
	
	private CredentialCreation fillRegistration() {
		String username = identityToChange.getUser().getNickName();
		CredentialCreation credentialCreation = webAuthnManager.prepareCredentialCreation(username, identityToChange);
		
		// Relying party information
		ServerProperty serverProperty = credentialCreation.serverProperty();
		flc.contextPut("rpId", serverProperty.getRpId());
		Challenge challenge = serverProperty.getChallenge();
		flc.contextPut("challenge", webAuthnManager.encodeToString(challenge.getValue()));
		
		flc.contextPut("timeout", Integer.valueOf(loginModule.getPasskeyTimeout() * 1000));
		flc.contextPut("userVerification", loginModule.getPasskeyUserVerification().getValue());
		flc.contextPut("attestation", loginModule.getPasskeyAttestationConveyancePreference().getValue());	
		
		// Identity
		flc.contextPut("userName", credentialCreation.userName());
		flc.contextPut("userDisplayName", webAuthnManager.getUserDisplayName(identityToChange));
		flc.contextPut("userId", credentialCreation.userId());
		flc.contextPut("credentialCreate", Boolean.TRUE);
		return credentialCreation;
	}

}
