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
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TequilaProvider implements OAuthSPI {
	
	private static final OLog log = Tracing.createLoggerFor(TequilaProvider.class);
	
	private static final String SCOPE = "Tequila.profile";
	
	@Autowired
	private OAuthLoginModule oauthModule;

	@Override
	public boolean isEnabled() {
		return oauthModule.isTequilaEnabled();
	}

	@Override
	public Api getScribeProvider() {
		return new TequilaApi();
	}

	@Override
	public String getName() {
		return "tequila";
	}

	@Override
	public boolean isRootEnabled() {
		return false;
	}

	@Override
	public String getProviderName() {
		return "TEQUILA";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_tequila";
	}

	@Override
	public String getAppKey() {
		return oauthModule.getTequilaApiKey();
	}

	@Override
	public String getAppSecret() {
		return oauthModule.getTequilaApiSecret();
	}

	@Override
	public String[] getScopes() {
		return new String[] { SCOPE };
	}

	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		String endpoint = oauthModule.getTequilaOAuth2Endpoint();
		if(!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		
		OAuthRequest request = new OAuthRequest(Verb.GET, endpoint + "userinfo");
	    service.signRequest(accessToken, request);
	    request.addHeader("Accept", "application/json");
	    Response oauthResponse = request.send();
	    String body = oauthResponse.getBody();
		return parseResponse(body);
	}
	
	public OAuthUser parseResponse(String body) {
		OAuthUser user = new OAuthUser();
		try {
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, "Sciper"));
			user.setFirstName(getValue(obj, "Firstname"));
			user.setLastName(getValue(obj, "Name"));
			user.setEmail(getValue(obj, "Email"));
			user.setInstitutionalUserIdentifier(getValue(obj, "Sciper"));
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
		return oauthModule.getTequilaOAuth2Endpoint();
	}
}
