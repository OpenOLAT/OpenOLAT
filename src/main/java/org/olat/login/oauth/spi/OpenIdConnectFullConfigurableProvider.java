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
import org.olat.login.oauth.OAuthDisplayName;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 6 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenIdConnectFullConfigurableProvider implements OAuthSPI, OAuthDisplayName {
	
	private static final Logger log = Tracing.createLoggerFor(Google2Provider.class);

	private final String name;
	private final String displayName;
	private final String providerName;
	private final String appKey;
	private final String appSecret;
	private final String issuer;
	private final String endPoint;
	
	private final boolean rootEnabled;
	
	private final OAuthLoginModule oauthModule;
	
	public OpenIdConnectFullConfigurableProvider(String name, String displayName, String providerName,
			String appKey, String appSecret, String issuer, String endPoint, boolean rootEnabled, OAuthLoginModule oauthModule) {
		this.name = name;
		this.displayName = displayName;
		this.providerName = providerName;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.issuer = issuer;
		this.endPoint = endPoint;
		this.rootEnabled = rootEnabled;
		this.oauthModule = oauthModule;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean isRootEnabled() {
		return rootEnabled;
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return true;
	}

	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(getAppKey())
                .apiSecret(getAppSecret())
                .callback(oauthModule.getCallbackUrl())
                .defaultScope("openid email")
                .responseType("id_token token")
                .build(new OpenIdConnectFullConfigurableApi(this));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getProviderName() {
		return providerName;
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_" + name;
	}

	public String getAppKey() {
		return appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getEndPoint() {
		return endPoint;
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
		return issuer; 
	}
}
