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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PantherProvider implements OAuthSPI {
	
	private static final OLog log = Tracing.createLoggerFor(PantherProvider.class);

	@Value("${oauth.panther.enabled:disabled}")
	private String enabled;
	@Value("${oauth.panther.appKey:null}")
	private String appKey;
	@Value("${oauth.panther.appSecret:null}")
	private String appSecret;
	
	@Override
	public boolean isEnabled() {
		return "enabled".equals(enabled);
	}

	@Override
	public Class<? extends Api> getScribeProvider() {
		return PantherApi.class;
	}

	@Override
	public String getName() {
		return "panther";
	}

	@Override
	public String getProviderName() {
		return "PANTHER";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_panther";
	}

	@Override
	public String getAppKey() {
		return appKey;
	}

	@Override
	public String getAppSecret() {
		return appSecret;
	}

	@Override
	public String[] getScopes() {
		return new String[0];
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, "https://author.hamilton-medical.com/.oauth/userinfo"); 
		service.signRequest(accessToken, oauthRequest);
		Response oauthResponse = oauthRequest.send();
		String body = oauthResponse.getBody();
		return parseInfos(body);
	}
	
	public OAuthUser parseInfos(String body) {
		OAuthUser user = new OAuthUser();
		
		try {
			JSONObject obj = new JSONObject(body);
			JSONObject properties = obj.getJSONObject("properties");
			user.setId(getValue(properties, "username"));
			user.setFirstName(getValue(properties, "firstName"));
			user.setLastName(getValue(properties, "lastName"));
			user.setLang(getValue(properties, "language"));
			user.setInstitutionalUserIdentifier(getValue(properties, "aarcMemberID"));
			user.setEmail(getValue(properties, "email"));
		} catch (JSONException e) {
			log.error("", e);
		}
		
		return user;
	}
	
	private String getValue(JSONObject obj, String property) {
		String value = obj.optString(property);
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}
}
