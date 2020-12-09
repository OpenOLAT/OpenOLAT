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

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.apis.microsoftazureactivedirectory.BaseMicrosoftAzureActiveDirectoryApi;
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
 * Initial date: 7 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MicrosoftAzureADFSProvider implements OAuthSPI {
	
	private static final Logger log = Tracing.createLoggerFor(MicrosoftAzureADFSProvider.class);
	
	public static final String PROVIDER = "AZUREAD";

	@Value("${azure.adfs.attributename.useridentifyer:userPrincipalName}")
	private String idAttributeName;
	@Value("${azure.adfs.attributename.firstName:givenName}")
	private String firstNameAttributeName;
	@Value("${azure.adfs.attributename.lastName:surname}")
	private String lastNameAttributeName;
	@Value("${azure.adfs.attributename.email:mail}")
	private String emailAttributeName;
	@Value("${azure.adfs.attributename.institutionalUserIdentifier:userPrincipalName}")
	private String institutionalUserIdentifierAttributeName;
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isAzureAdfsEnabled();
	}

	@Override
	public boolean isRootEnabled() {
		return oauthModule.isAzureAdfsRootEnabled();
	}

	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public OAuthService getScribeProvider() {
		ServiceBuilder serviceBuilder = new ServiceBuilder(oauthModule.getAzureAdfsApiKey());
		if(StringHelper.containsNonWhitespace(oauthModule.getAzureAdfsApiSecret())) {
			serviceBuilder = serviceBuilder.apiSecret(oauthModule.getAzureAdfsApiSecret());
		}
		BaseMicrosoftAzureActiveDirectoryApi api;
		if(StringHelper.containsNonWhitespace(oauthModule.getAzureAdfsTenant())) {
			api = MicrosoftAzureActiveDirectory20Api.custom(oauthModule.getAzureAdfsTenant());
		} else {
			// common tenant
			api = MicrosoftAzureActiveDirectory20Api.instance();
		}
		return serviceBuilder
				.defaultScope("profile openid email User.Read")
				.callback(oauthModule.getCallbackUrl())
                .build(api);
	}

	@Override
	public String getName() {
		return "azureAdfs";
	}
	
	@Override
	public String getProviderName() {
		return PROVIDER;
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_adfs";
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		OAuthUser user = new OAuthUser();
		enrichUser(user, (OAuth2AccessToken)accessToken);
		enrichGraph(user, (OAuth20Service)service, (OAuth2AccessToken)accessToken);
		return user;
	}
	
	private void enrichUser(OAuthUser user, OAuth2AccessToken accessToken) {
		try {
			JSONWebToken jwt = JSONWebToken.parse(accessToken);
			JSONObject obj = jwt.getJsonPayload();
			user.setId(getValue(obj, idAttributeName, user.getId()));
			user.setFirstName(getValue(obj, firstNameAttributeName, user.getFirstName()));
			user.setLastName(getValue(obj, lastNameAttributeName, user.getLastName()));
			user.setEmail(getValue(obj, emailAttributeName, user.getEmail()));
			user.setInstitutionalUserIdentifier(getValue(obj, institutionalUserIdentifierAttributeName, user.getInstitutionalUserIdentifier()));
			if(!StringHelper.containsNonWhitespace(user.getId())) {
				user.setId(user.getInstitutionalUserIdentifier());
			}
		} catch (JSONException e) {
			log.error("", e);
		}
	}
	
	private void enrichGraph(OAuthUser user, OAuth20Service oauthService, OAuth2AccessToken accessToken) {
		try {
			OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, "https://graph.microsoft.com/v1.0/me");
			oauthRequest.addHeader("x-li-format", "json");
			oauthRequest.addHeader("Accept-Language", "en-GB");
			oauthService.signRequest(accessToken, oauthRequest);
			Response oauthResponse = oauthService.execute(oauthRequest);
			String body =  oauthResponse.getBody();
			JSONObject obj = new JSONObject(body);
			user.setId(getValue(obj, idAttributeName, user.getId()));
			user.setFirstName(getValue(obj, firstNameAttributeName, user.getFirstName()));
			user.setLastName(getValue(obj, lastNameAttributeName, user.getLastName()));
			user.setEmail(getValue(obj, emailAttributeName, user.getEmail()));
			user.setInstitutionalUserIdentifier(getValue(obj, institutionalUserIdentifierAttributeName, user.getInstitutionalUserIdentifier()));
			if(!StringHelper.containsNonWhitespace(user.getId())) {
				user.setId(user.getInstitutionalUserIdentifier());
			}
		} catch (JSONException | InterruptedException | ExecutionException | IOException e) {
			log.error("", e);
		}
	}
	
	private String getValue(JSONObject obj, String property, String currentValue) {
		String value = obj.optString(property);
		return StringHelper.containsNonWhitespace(value) ? value : currentValue;
	}
	
	@Override
	public String getIssuerIdentifier() {
		return "https://login.microsoftonline.com";
	}
}
