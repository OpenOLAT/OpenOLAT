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
package org.olat.login.webauthn;

import java.util.List;

import org.olat.basesecurity.Authentication;

/**
 * 
 * 
 * Initial date: 22 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum PasskeyLevels {
	/**
	 * Only password
	 */
	level1,
	/**
	 * Only passkey
	 */
	level2,
	/**
	 * Password and passkey for 2FA
	 */
	level3;
	
	
	public static PasskeyLevels currentLevel(List<Authentication> authentications) {
		Authentication olatAuthentication = getAuthenticationbyProvider(authentications, "OLAT");
		Authentication somePasskey = getAuthenticationbyProvider(authentications, OLATWebAuthnManager.PASSKEY);
		
		PasskeyLevels level = null;
		if(olatAuthentication != null && somePasskey != null) {
			level = PasskeyLevels.level3;
		} else if(somePasskey != null) {
			level = PasskeyLevels.level2;
		} else if(olatAuthentication != null) {
			level = PasskeyLevels.level1;
		}
		return level;
	}
	
	private static Authentication getAuthenticationbyProvider(List<Authentication> authentications, String provider) {
		return authentications.stream()
				.filter(auth -> provider.equals(auth.getProvider()))
				.findFirst()
				.orElse(null);
	}

}
