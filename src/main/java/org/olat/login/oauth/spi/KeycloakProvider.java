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
package org.olat.login.oauth.spi;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 17 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class KeycloakProvider implements OAuthSPI {
	
	private static final Logger log = Tracing.createLoggerFor(KeycloakProvider.class);
	
	@Value("${oauth.keycloak.attributename.useridentifyer:sub}")
	private String idAttributeName;
	@Value("${oauth.keycloak.attributename.nickName:preferred_username}")
	private String nickNameAttributeName;
	@Value("${oauth.keycloak.attributename.firstName:given_name}")
	private String firstNameAttributeName;
	@Value("${oauth.keycloak.attributename.lastName:family_name}")
	private String lastNameAttributeName;
	@Value("${oauth.keycloak.attributename.email:email}")
	private String emailAttributeName;
	@Value("${oauth.keycloak.attributename.institutionalUserIdentifier}")
	private String institutionalUserIdentifierAttributeName;
	@Value("${oauth.keycloak.attributename.institutionalName}")
	private String institutionalNameAttributeName;
	@Value("${oauth.keycloak.default.value.institutionalName}")
	private String institutionalNameDefaultValue;
	@Value("${oauth.keycloak.attributename.department}")
	private String departmentAttributeName;
	@Value("${oauth.keycloak.attributename.country}")
	private String countryAttributeName;

	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public String getName() {
		return "keycloak";
	}
	
	@Override
	public String getProviderName() {
		return "KEYCLOAK";
	}
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isKeycloakEnabled();
	}
	
	@Override
	public boolean isRootEnabled() {
		return oauthModule.isKeycloakRootEnabled();
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}
	
	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_keycloak";
	}
	
	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(oauthModule.getKeycloakClientId())
                .apiSecret(oauthModule.getKeycloakClientSecret())
                .callback(oauthModule.getCallbackUrl())
                .build(KeycloakApi.instance(oauthModule.getKeycloakEndpoint(), oauthModule.getKeycloakRealm()));
	}
	
	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken)
			throws IOException, InterruptedException, ExecutionException {
		String idToken = ((OAuth2AccessToken)accessToken).getAccessToken();
		JSONWebToken token = JSONWebToken.parse(idToken);
		return parseInfos(token.getPayload());
	}
	
	public OAuthUser parseInfos(String body) {
		OAuthUser user = new OAuthUser();
		
		try {
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, idAttributeName, null));
			user.setNickName(getValue(obj, nickNameAttributeName, null));
			user.setEmail(getValue(obj, emailAttributeName, null));
			user.setFirstName(getValue(obj, firstNameAttributeName, null));
			user.setLastName(getValue(obj, lastNameAttributeName, null));
			user.setInstitutionalUserIdentifier(getValue(obj, institutionalUserIdentifierAttributeName, null));
			user.setInstitutionalName(getValue(obj, institutionalNameAttributeName, institutionalNameDefaultValue));
			user.setDepartment(getValue(obj, departmentAttributeName, null));
			user.setCountry(getValue(obj, countryAttributeName, null));
		} catch (JSONException e) {
			log.error("", e);
		}
		
		return user;
	}
	
	private String getValue(JSONObject obj, String property, String defaultValue) {
		String value = null;
		if(StringHelper.containsNonWhitespace(property)) {
			value = obj.optString(property);
		}
		return StringHelper.containsNonWhitespace(value) ? value : defaultValue;
	}
	
	@Override
	public String getIssuerIdentifier() {
		return oauthModule.getKeycloakEndpoint();
	}
}
