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
package org.olat.login.webauthn;

import java.util.Base64;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.webauthn.model.CredentialCreation;
import org.olat.login.webauthn.model.CredentialRequest;

/**
 * 
 * Initial date: 9 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OLATWebAuthnManager extends AuthenticationSPI {
	
	public static final String PASSKEY = "PASSKEY";
	
	List<Authentication> getPasskeyAuthentications(Identity identity);
	
	List<Authentication> getPasskeyAuthentications(String username);
	
	CredentialRequest prepareCredentialRequest(List<Authentication> authentications);
	
	boolean validateRequest(CredentialRequest request, String clientDataBase64, String authenticatorData,
			String rawId, String signature, String userHandle);
	
	CredentialCreation prepareCredentialCreation(String userName, Identity identity);
	
	Authentication validateRegistration(CredentialCreation registration, String clientDataBase64,
			String attestationObjectBase64, String transports);
	
	List<String> generateRecoveryKeys(Identity identity);
	
	boolean validateRecoveryKey(String key, IdentityRef identity);
	
	public default String encodeToString(byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}
	
	public default String getUserDisplayName(Identity identity) {
		StringBuilder sb = new StringBuilder();
		User user = identity.getUser();
		
		if(StringHelper.containsNonWhitespace(user.getFirstName())) {
			sb.append(user.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(user.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(user.getLastName());
		}
		return sb.toString();
	}
}
