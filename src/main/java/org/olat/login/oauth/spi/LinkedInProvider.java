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
import org.springframework.stereotype.Service;

import com.github.scribejava.apis.LinkedInApi20;
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
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LinkedInProvider implements OAuthSPI {
	
	private static final Logger log = Tracing.createLoggerFor(LinkedInProvider.class);
	
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isLinkedInEnabled();
	}
	
	@Override
	public boolean isRootEnabled() {
		return false;
	}
	
	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public String getName() {
		return "linkedin";
	}

	@Override
	public String getProviderName() {
		return "LINKEDIN";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_linkedin";
	}

	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(oauthModule.getLinkedInApiKey())
                .apiSecret(oauthModule.getLinkedInApiSecret())
                .callback(oauthModule.getCallbackUrl())
                .defaultScope("r_liteprofile r_emailaddress")
                .build(LinkedInApi20.instance());
	}

	@Override
	public  OAuthUser getUser(OAuthService service, Token accessToken)
	throws InterruptedException, ExecutionException, IOException {
		String profile = getInfos((OAuth20Service)service, (OAuth2AccessToken)accessToken,
				"https://api.linkedin.com/v2/me?projection=(id,localizedFirstName,localizedLastName)");
		String email = getInfos((OAuth20Service)service, (OAuth2AccessToken)accessToken,
				"https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))");
		return parseInfos(profile, email);
	}
	
	private String getInfos(OAuth20Service oauthService, OAuth2AccessToken accessToken, String url)
	throws InterruptedException, ExecutionException, IOException {
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, url);
		oauthRequest.addHeader("x-li-format", "json");
		oauthRequest.addHeader("Accept-Language", "en-GB");
		oauthService.signRequest(accessToken, oauthRequest);
		Response oauthResponse = oauthService.execute(oauthRequest);
		return oauthResponse.getBody();
	}
	
	public OAuthUser parseInfos(String profile, String email) {
		OAuthUser user = new OAuthUser();
		
		try {
			JSONObject obj = new JSONObject(profile);
			user.setId(getValue(obj, "id"));
			user.setFirstName(getValue(obj, "localizedFirstName"));
			user.setLastName(getValue(obj, "localizedLastName"));
			user.setEmail(getValue(obj, "emailAddress"));
		} catch (JSONException e) {
			log.error("", e);
		}
		
		try {
			JSONObject obj = new JSONObject(email);
			JSONArray elements = obj.getJSONArray("elements");
			JSONObject firstElement = elements.getJSONObject(0);
			JSONObject handle = firstElement.getJSONObject("handle~");
			user.setEmail(getValue(handle, "emailAddress"));
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
		return "https://linkedin.com";
	}
}
