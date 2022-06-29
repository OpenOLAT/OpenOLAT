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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthDisplayName;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthMapping;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthAttributeMapping;
import org.olat.login.oauth.model.OAuthAttributesMapping;
import org.olat.login.oauth.model.OAuthUser;

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
 * Initial date: 17 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenericOAuth2Provider implements OAuthSPI, OAuthDisplayName, OAuthMapping {

	private static final Logger log = Tracing.createLoggerFor(GenericOAuth2Provider.class);
	
	private final String name;
	private final String displayName;
	private final String providerName;
	private final String appKey;
	private final String appSecret;
	private final String responseType;
	private final String scopes;
	private final String issuer;
	private final String authorizationEndPoint;
	private final String tokenEndPoint;
	private final String userInfosEndPoint;
	
	private final OAuthAttributesMapping attributesMapping;
	
	private final boolean rootEnabled;
	
	private final OAuthLoginModule oauthModule;
	
	public GenericOAuth2Provider(String name, String displayName, String providerName,
			String appKey, String appSecret, String responseType, String scopes, String issuer,
			String authorizationEndPoint, String tokenEndPoint, String userInfosEndPoint,
			OAuthAttributesMapping attributesMapping, boolean rootEnabled, OAuthLoginModule oauthModule) {
		this.name = name;
		this.displayName = displayName;
		this.providerName = providerName;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.responseType = responseType;
		this.scopes = scopes;
		this.issuer = issuer;
		this.authorizationEndPoint = authorizationEndPoint;
		this.tokenEndPoint = tokenEndPoint;
		this.userInfosEndPoint = userInfosEndPoint;
		this.attributesMapping = attributesMapping;
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
		return false;
	}

	@Override
	public OAuthService getScribeProvider() {
		ServiceBuilder sb = new ServiceBuilder(appKey)
                .apiSecret(appSecret)
                .callback(oauthModule.getCallbackUrl());
		if(StringHelper.containsNonWhitespace(scopes)) {
			sb.defaultScope(scopes);
		}
        return sb        
                .responseType(responseType)
                .build(new GenericOAuth2Api(this));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProviderName() {
		return providerName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_oauth";
	}

	@Override
	public String getIssuerIdentifier() {
		return issuer;
	}
	
	public String getIssuer() {
		return issuer;
	}
		
	public String getAuthorizationBaseUrl() {
		return authorizationEndPoint;
	}
	
	public String getTokenBaseUrl() {
		return tokenEndPoint;
	}
	
	public String getUserInfosUrl() {
		return userInfosEndPoint;
	}
		
	public String getAppKey() {
		return appKey;
	}
	
	public String getAppSecret() {
		return appSecret;
	}
	
	public boolean hasScope(String scope) {
		return scopes.contains(scope);
	}
	
	public String getScopes() {
		return scopes;
	}
	
	public boolean hasResponseType(String type) {
		return responseType.contains(type);
	}
	
	public String getResponseType() {
		return responseType;
	}
	
	@Override
	public List<OAuthAttributeMapping> getMapping() {
		return attributesMapping == null ? List.of() : attributesMapping.getMapping();
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken)
			throws IOException, InterruptedException, ExecutionException {
		try {
			OAuthUser user = new OAuthUser();
			parseToken(user, accessToken);
			
			if(StringHelper.containsNonWhitespace(userInfosEndPoint)) {
				OAuth20Service oauthService = (OAuth20Service)service;
				OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, userInfosEndPoint); 
				oauthService.signRequest((OAuth2AccessToken)accessToken, oauthRequest);
				Response oauthResponse = oauthService.execute(oauthRequest);
				parseUserInfos(user, oauthResponse.getBody());
			} 
			
			return user;
		} catch (JSONException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void parseToken(OAuthUser user, Token accessToken) {
		if(accessToken instanceof OpenIdOAuth2AccessToken) {
			String idToken = ((OpenIdOAuth2AccessToken)accessToken).getOpenIdToken();
			JSONWebToken token = JSONWebToken.parse(idToken);
			parseIdTokenInfos(user, token.getPayload());
		} else if(accessToken instanceof OAuth2AccessToken) {
			JSONWebToken jwt = JSONWebToken.parse((OAuth2AccessToken)accessToken);
			parseIdTokenInfos(user, jwt.getPayload());
		}
	}
	
	public void parseIdTokenInfos(OAuthUser user, String body) {
		try {
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, "sub"));
			applyMapping(user, obj);
		} catch (JSONException e) {
			log.error("", e);
		}
	}
	
	public void parseUserInfos(OAuthUser user, String body) {
		try {
			JSONObject obj = new JSONObject(body);
			applyMapping(user, obj);
		} catch (JSONException e) {
			log.error("", e);
		}
	}
	
	public void applyMapping(OAuthUser user, JSONObject obj) {
		if(attributesMapping != null && attributesMapping.getMapping() != null) {
			List<OAuthAttributeMapping> attributes = attributesMapping.getMapping();
			for(OAuthAttributeMapping attribute:attributes) {
				String external = attribute.getExternalAttribute();
				String val = getValue(obj, external);
				if(val != null) {
					String openolatAttr = attribute.getOpenolatAttribute();
					if("id".equals(openolatAttr)) {
						user.setId(val);
					} else if("lang".equals(openolatAttr)) {
						user.setLang(val);
					} else {
						user.setProperty(openolatAttr, val);
					}
				}
			}
		}
	}
	
	private String getValue(JSONObject obj, String property) {
		Object val = obj.opt(property);
		if(val instanceof JSONArray) {
			JSONArray arr = (JSONArray)val;
			if(arr.length() > 0) {
				val = arr.get(0);
			} else {
				val = null;
			}
		}
		
		String value = null;
		if(val instanceof String) {
			value = (String)val;
		} else if(val != null) {
			value = val.toString();
		}
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}
}
