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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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

import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 3 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SwitchEduIDProvider implements OAuthSPI {

	private static final Logger log = Tracing.createLoggerFor(SwitchEduIDProvider.class);
	
	@Value("${switch.eduid.test.env:false}")
	private boolean test;
	
	@Autowired
	private OAuthLoginModule oauthModule;

	@Override
	public boolean isEnabled() {
		return oauthModule.isSwitchEduIDEnabled();
	}
	
	@Override
	public boolean isRootEnabled() {
		return oauthModule.isSwitchEduIDRootEnabled();
	}
	
	public boolean isMFAEnabled() {
		return oauthModule.isSwitchEduIDMFAEnabled();
	}
	
	public boolean isTestEnvironment() {
		return test;
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(oauthModule.getSwitchEduIDApiKey())
                .apiSecret(oauthModule.getSwitchEduIDApiSecret())
                .callback(oauthModule.getCallbackUrl())
                .defaultScope("openid profile email https://login.eduid.ch/authz/User.Read")
                .responseType("code")
                .build(new SwitchEduIDApi(this));
	}

	@Override
	public String getName() {
		return "switcheduid";
	}

	@Override
	public String getProviderName() {
		return "SEDUID";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_switch_eduid";
	}

	@Override
	public String getIssuerIdentifier() {
		return "https://login.eduid.ch/";
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken)
			throws IOException, InterruptedException, ExecutionException {
		try {
			String idToken = ((OpenIdOAuth2AccessToken)accessToken).getOpenIdToken();
			JSONWebToken token = JSONWebToken.parse(idToken);

			String userInfosEndPoint = test ? SwitchEduIDApi.SWITCH_EDUID_TEST_USERINFOS_ENDPOINT : SwitchEduIDApi.SWITCH_EDUID_USERINFOS_ENDPOINT;
			if(token != null && StringHelper.containsNonWhitespace(userInfosEndPoint)) {
				OAuth20Service oauthService = (OAuth20Service)service;
				OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, userInfosEndPoint); 
				oauthService.signRequest((OAuth2AccessToken)accessToken, oauthRequest);
				Response oauthResponse = oauthService.execute(oauthRequest);
				
				OAuthUser user = new OAuthUser();
				parseUserInfos(user, oauthResponse.getBody());
				return user;
			}
		} catch (JSONException e) {
			log.error("", e);
		}
		return null;
	}

	public void parseUserInfos(OAuthUser user, String body) {
		try {
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, "swissEduPersonUniqueID"));
			user.setEmail(getValue(obj, "email"));
			user.setFirstName(getValue(obj, "given_name"));
			user.setLastName(getValue(obj, "family_name"));
			user.setInstitutionalUserIdentifier(getFirstArrayValue(obj, "swissEduIDLinkedAffiliationUniqueID"));
			user.setInstitutionalEmail(getFirstArrayValue(obj, "swissEduIDLinkedAffiliationMail"));
			user.setAuthenticationExternalIds(getArrayValues(obj, "swissEduIDLinkedAffiliationUniqueID"));
			user.setLang(getValue(obj, "locale"));
			log.debug("User infos: {}", obj);
		} catch (JSONException e) {
			log.error("", e);
		}
	}
	
	private String getValue(JSONObject obj, String property) {
		String value = obj.optString(property);
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}
	
	private String getFirstArrayValue(JSONObject obj, String property) {
		JSONArray value = obj.optJSONArray(property);
		return value == null || value.isEmpty() ? null : value.iterator().next().toString();
	}
	
	private List<String> getArrayValues(JSONObject obj, String property) {
		JSONArray value = obj.optJSONArray(property);
		if(value == null) return null;
		
		List<String> list = new ArrayList<>(value.length());
		for(Object object:value) {
			if(object instanceof String string) {
				list.add(string);
			}
		}
		return list;
	}
}
