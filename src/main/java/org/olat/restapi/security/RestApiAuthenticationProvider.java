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
package org.olat.restapi.security;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.id.Identity;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.oauth.model.OAuthValidationResult;
import org.olat.login.validation.ValidationResult;
import org.olat.restapi.RestModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("restApiAuthenticationProvider")
public class RestApiAuthenticationProvider implements AuthenticationProviderSPI {
	
	private static final int length = 48;
	private static final SecureRandom numberGenerator = new SecureRandom();
	private static final String VALID_CLIENT_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"; //  

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private AuthenticationDAO authenticationDao;
	
	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public List<String> getProviderNames() {
		return List.of(RestModule.RESTAPI_AUTH);
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
		Authentication authentication = authenticationDao.getAuthentication(name, RestModule.RESTAPI_AUTH, BaseSecurity.DEFAULT_ISSUER);
		if(authentication == null || authentication.getIdentity().equals(identity)) {
			return OAuthValidationResult.allOk();
		}
		return OAuthValidationResult.error("username.rule.in.use");
	}
	
	public String generateClientId() {
		return UUID.randomUUID().toString();
	}
	
	public String generateClientSecret() {
		return RandomStringUtils.random(length, 0, VALID_CLIENT_ID_CHARS.length(), false, false,
				VALID_CLIENT_ID_CHARS.toCharArray(), numberGenerator);
	}
	
	public void setClientAuthentication(Identity identity, String clientId, String clientSecret) {
		authenticationDao.createAndPersistAuthenticationHash(identity, RestModule.RESTAPI_AUTH, BaseSecurity.DEFAULT_ISSUER, null,
				clientId, clientSecret, loginModule.getDefaultHashAlgorithm());
	}
}
