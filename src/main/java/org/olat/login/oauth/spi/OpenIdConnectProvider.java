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

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 15.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OpenIdConnectProvider implements OAuthSPI {
	
	private static final Logger log = Tracing.createLoggerFor(OpenIdConnectProvider.class);

	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isOpenIdConnectIFEnabled();
	}
	
	@Override
	public boolean isRootEnabled() {
		return oauthModule.isOpenIdConnectIFRootEnabled();
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return true;
	}

	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(oauthModule.getOpenIdConnectIFApiKey())
                .apiSecret(oauthModule.getOpenIdConnectIFApiSecret())
                .callback(oauthModule.getCallbackUrl())
                .defaultScope("openid email")
                .responseType("id_token token")
                .build(new OpenIdConnectApi(this));
	}
	
	public String getEndPoint() {
		return oauthModule.getOpenIdConnectIFAuthorizationEndPoint();
	}

	@Override
	public String getName() {
		return "OpenIDConnect";
	}

	@Override
	public String getProviderName() {
		return "OPENIDCO";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_openid";
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		try {
			String idToken = ((OAuth2AccessToken)accessToken).getAccessToken();
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
			user.setId(getValue(obj, "sub"));
			user.setEmail(getValue(obj, "sub"));
		} catch (JSONException e) {
			log.error("", e);
		}
		
		return user;
	}
	
	private String getValue(JSONObject obj, String property) {
		String value = obj.optString(property);
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}

	@Override
	public String getIssuerIdentifier() {
		return oauthModule.getOpenIdConnectIFIssuer();
	}
}
