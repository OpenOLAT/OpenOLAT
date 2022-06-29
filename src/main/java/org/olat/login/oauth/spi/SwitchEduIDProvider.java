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
import com.github.scribejava.core.model.Token;
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
                .defaultScope("openid email")
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
			return parseInfos(token.getPayload());
		} catch (JSONException e) {
			log.error("", e);
			return null;
		}
	}
	
	public OAuthUser parseInfos(String body) {
		OAuthUser user = new OAuthUser();
		
		try {
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, "swissEduPersonUniqueID"));
			user.setEmail(getValue(obj, "email"));
			user.setFirstName(getValue(obj, "given_name"));
			user.setLastName(getValue(obj, "family_name"));
			user.setInstitutionalUserIdentifier(this.getFirstArrayValue(obj, "swissEduIDLinkedAffiliationUniqueID"));
		} catch (JSONException e) {
			log.error("", e);
		}
		
		return user;
	}
	
	private String getValue(JSONObject obj, String property) {
		String value = obj.optString(property);
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}
	
	private String getFirstArrayValue(JSONObject obj, String property) {
		JSONArray value = obj.optJSONArray(property);
		return value == null || value.isEmpty() ? null : value.iterator().next().toString();
	}
}
