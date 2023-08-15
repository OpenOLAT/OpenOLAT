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
package org.olat.login.webauthn.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.RecoveryKey;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.basesecurity.manager.RecoveryKeyDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.login.LoginModule;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.AllOkValidationResult;
import org.olat.login.validation.ValidationResult;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.model.Credential;
import org.olat.login.webauthn.model.CredentialCreation;
import org.olat.login.webauthn.model.CredentialRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.Authenticator;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;

/**
 * 
 * Initial date: 9 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OLATWebAuthnManagerImpl implements OLATWebAuthnManager {
	
	private static final Logger log = Tracing.createLoggerFor(OLATWebAuthnManagerImpl.class);

	private final SecureRandom random = new SecureRandom();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private RecoveryKeyDAO recoveryKeyDao;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private OLATAuthManager olatAuthManager;
	
	@Override
	public List<String> getProviderNames() {
		return List.of(PASSKEY);
	}
	
	@Override
	public boolean canAddAuthenticationUsername(String provider) {
		return false;
	}
	
	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		return false;
	}
	
	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		return false;
	}
	
	@Override
	public ValidationResult validateAuthenticationUsername(String name, String provider, Identity identity) {
		return new AllOkValidationResult();
	}
	
	@Override
	public Identity authenticate(String login, String password) {
		return olatAuthManager.authenticate(login, password);
	}
	
	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		//
	}

	@Override
	public List<Authentication> getPasskeyAuthentications(String username) {
		return authenticationDao.getAuthenticationsByAuthusername(username, PASSKEY);
	}

	@Override
	public List<Authentication> getPasskeyAuthentications(Identity identity) {
		return authenticationDao.getAuthenticationsNoFetch(identity, PASSKEY);
	}

	@Override
	public List<String> generateRecoveryKeys(Identity identity) {
		recoveryKeyDao.deleteRecoveryKeys(identity);
		
		List<String> keys = new ArrayList<>();
		for(int i=0; i<10; i++) {
			String key = recoveryKeyDao.generateRecoveryKey();
			recoveryKeyDao.createRecoveryKey(key, Encoder.Algorithm.sha512, identity);
			keys.add(key);
		}
		return keys;
	}

	@Override
	public boolean validateRecoveryKey(String key, IdentityRef identity) {
		List<RecoveryKey> recoveryKeys = recoveryKeyDao.loadAvailableRecoveryKeys(identity);
		for(RecoveryKey recoveryKey:recoveryKeys) {
			if(recoveryKey.isSame(key)) {
				recoveryKey.setUseDate(new Date());
				recoveryKeyDao.updateRecoveryKey(recoveryKey);
				dbInstance.commit();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean validateRequest(CredentialRequest request, String clientDataBase64, String authenticatorData,
			String rawId, String signature, String userHandle) {
		
		AuthenticationImpl webAuthentication = (AuthenticationImpl)request.getAuthentication(rawId);

		AuthenticationRequest authenticationRequest = new AuthenticationRequest(Base64UrlUtil.decode(rawId),
				Base64UrlUtil.decode(userHandle), Base64UrlUtil.decode(authenticatorData),
				Base64UrlUtil.decode(clientDataBase64),
				(String)null, Base64UrlUtil.decode(signature));
		
		COSEKey coseKey = convertToCOSEKey(webAuthentication.getCoseKey());
		AttestedCredentialData attestedCredentialData = new AttestedCredentialData(new AAGUID(webAuthentication.getAaGuid()),
				webAuthentication.getCredentialId(), coseKey);

		ServerProperty serverProperty = request.serverProperty();
		AttestationStatement attestationStatement = null;
		if(webAuthentication.getAttestationObject() != null && webAuthentication.getAttestationObject().length() > 10) {// statement saved like {} cannot be read
			ObjectConverter converter = new ObjectConverter();
			AttestationObjectConverter attestationObjectConverter = new AttestationObjectConverter(converter);
			AttestationObject attestationObject = attestationObjectConverter.convert(webAuthentication.getAttestationObject());
			attestationStatement = attestationObject.getAttestationStatement();
		}

		Authenticator authenticator = new AuthenticatorImpl(attestedCredentialData, attestationStatement, webAuthentication.getCounter());
		AuthenticationParameters authenticationParameters = new AuthenticationParameters(serverProperty,
		                authenticator,
		                List.of(webAuthentication.getCredentialId()), false, false);

		WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
		AuthenticationData response = webAuthnManager.validate(authenticationRequest, authenticationParameters);
		return response != null;
	}

	@Override
	public Authentication validateRegistration(CredentialCreation registration, String clientDataBase64, String attestationObjectBase64) {
		
		byte[] userHandle = registration.userHandle();
		String userName = registration.userName();
		
    	RegistrationRequest request = new RegistrationRequest(Base64UrlUtil.decode(attestationObjectBase64),
    			Base64UrlUtil.decode(clientDataBase64), null, Set.of());
		
    	// Server properties
    	ServerProperty serverProperty = registration.serverProperty();
    	RegistrationParameters parameters = new RegistrationParameters(serverProperty, null, false, false);
		
		WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
		RegistrationData response = webAuthnManager.validate(request, parameters);
		AttestationObject attestation = response.getAttestationObject();
		
		AttestedCredentialData credentialData = attestation.getAuthenticatorData().getAttestedCredentialData();
		byte[] credentialId = credentialData.getCredentialId();
		AAGUID aaGuid = credentialData.getAaguid();
		COSEKey coseKey = credentialData.getCOSEKey();
		
		ObjectConverter converter = new ObjectConverter();
		AttestationObjectConverter attestationObjectConverter = new AttestationObjectConverter(converter);
		String attestationObject = attestationObjectConverter.convertToBase64urlString(attestation);
		String clientExtensions = null; // not saved for the moment
		String authenticatorExtensions = null; // not saved for the moment
		
		Authentication auth = authenticationDao.createAndPersistAuthenticationWebAuthn(registration.identity(), PASSKEY, userName,
				userHandle, credentialId, aaGuid.getBytes(), convertFromCOSEKey(coseKey),
				attestationObject, clientExtensions, authenticatorExtensions);
		dbInstance.commit();
		if(auth != null && this.loginModule.isPasskeyRemoveOlatToken()) {
			Authentication olatPassword = authenticationDao.getAuthentication(auth.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
			if(olatPassword != null) {
				authenticationDao.deleteAuthentication(olatPassword);
			}
		}
		return auth;
	}
	
	@Override
	public CredentialCreation prepareCredentialCreation(String userName, Identity identity) {
		byte[] userHandle = new byte[64];
        random.nextBytes(userHandle);

    	ServerProperty serverProperty = createServerPropertyWithChallenge();
		String userId = Base64.getUrlEncoder().withoutPadding().encodeToString(userHandle);
		return new CredentialCreation(userName, userId, userHandle, identity, serverProperty);
	}

	@Override
	public CredentialRequest prepareCredentialRequest(List<Authentication> authentications) {
    	ServerProperty serverProperty = createServerPropertyWithChallenge();
		List<Credential> credentials = authentications.stream().map(auth -> {
			byte[] credentialId = ((AuthenticationImpl)auth).getCredentialId();
			return new Credential(credentialId, auth);
		}).toList();
		return new CredentialRequest(credentials, serverProperty);
	}
	
	private ServerProperty createServerPropertyWithChallenge() {
		byte[] bchallenge = new byte[16];
		random.nextBytes(bchallenge);
		Challenge challenge = new DefaultChallenge(bchallenge);
		
		Origin origin = new Origin(Settings.createServerURI()) /* set origin */;
    	String rpId = Settings.getServerDomainName() /* set rpId */;
    	byte[] tokenBindingId = null /* set tokenBindingId */;
    	return new ServerProperty(origin, rpId, challenge, tokenBindingId);	
	}

	public byte[] convertFromCOSEKey(COSEKey coseKey) {
		ObjectConverter converter = new ObjectConverter();
		return converter.getJsonConverter().writeValueAsBytes(coseKey);
	}
	
	public COSEKey convertToCOSEKey(byte[] value) {
		try(InputStream in = new ByteArrayInputStream(value)) {
			ObjectConverter converter = new ObjectConverter();
			return converter.getJsonConverter().readValue(in, COSEKey.class);
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
}
