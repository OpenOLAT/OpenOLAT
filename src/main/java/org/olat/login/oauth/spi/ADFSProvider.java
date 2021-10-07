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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 06.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ADFSProvider implements OAuthSPI {
	
	private static final Logger log = Tracing.createLoggerFor(ADFSProvider.class);

	@Value("${adfs.attributename.useridentifyer:employeeNumber}")
	private String idAttributeName;
	@Value("${adfs.attributename.firstName:displayNamePrintable}")
	private String firstNameAttributeName;
	@Value("${adfs.attributename.lastName:Sn}")
	private String lastNameAttributeName;
	@Value("${adfs.attributename.email:mail}")
	private String emailAttributeName;
	@Value("${adfs.attributename.institutionalUserIdentifier:SAMAccountName}")
	private String institutionalUserIdentifierAttributeName;
	@Value("${adfs.attributename.institutionalName}")
	private String institutionalNameAttributeName;
	@Value("${adfs.attributename.department}")
	private String departmentAttributeName;
	@Value("${adfs.attributename.country}")
	private String countryAttributeName;
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Override
	public boolean isEnabled() {
		return oauthModule.isAdfsEnabled();
	}

	@Override
	public boolean isRootEnabled() {
		return oauthModule.isAdfsRootEnabled();
	}

	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public OAuthService getScribeProvider() {
		ServiceBuilder serviceBuilder = new ServiceBuilder(oauthModule.getAdfsApiKey());
		if(StringHelper.containsNonWhitespace(oauthModule.getAdfsApiSecret())) {
			serviceBuilder = serviceBuilder.apiSecret(oauthModule.getAdfsApiSecret());
		}
		return serviceBuilder
				.callback(oauthModule.getCallbackUrl())
                .build(new ADFSApi());
	}

	@Override
	public String getName() {
		return "adfs";
	}
	
	@Override
	public String getProviderName() {
		return "ADFS";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_icon_provider_adfs";
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		OAuthUser user = new OAuthUser();
		try {
			JSONWebToken jwt = JSONWebToken.parse((OAuth2AccessToken)accessToken);
			JSONObject obj = jwt.getJsonPayload();
			user.setId(getValue(obj, idAttributeName));
			user.setFirstName(getValue(obj, firstNameAttributeName));
			user.setLastName(getValue(obj, lastNameAttributeName));
			user.setEmail(getValue(obj, emailAttributeName));
			user.setInstitutionalUserIdentifier(getValue(obj, institutionalUserIdentifierAttributeName));
			if(!StringHelper.containsNonWhitespace(user.getId())) {
				user.setId(user.getInstitutionalUserIdentifier());
			}
			user.setInstitutionalName(getValue(obj, institutionalNameAttributeName));
			user.setDepartment(getValue(obj, departmentAttributeName));
			user.setCountry(getValue(obj, countryAttributeName));
		} catch (JSONException e) {
			log.error("", e);
		}
		return user;
	}
	
	private String getValue(JSONObject obj, String property) {
		if(StringHelper.containsNonWhitespace(property)) {
			String value = obj.optString(property);
			return StringHelper.containsNonWhitespace(value) ? value : null;
		}
		return null;
	}
	
	@Override
	public String getIssuerIdentifier() {
		return oauthModule.getAdfsOAuth2Endpoint();
	}
}
