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
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ldap.LDAPLoginManager;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.OAuthUserCreator;
import org.olat.login.oauth.model.OAuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
 * Initial date: 10.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PantherProvider implements OAuthSPI, OAuthUserCreator {
	
	private static final Logger log = Tracing.createLoggerFor(PantherProvider.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OAuthLoginModule oauthModule;

	@Value("${oauth.panther.enabled:disabled}")
	private String enabled;
	@Value("${oauth.panther.appKey:null}")
	private String appKey;
	@Value("${oauth.panther.appSecret:null}")
	private String appSecret;
	@Value("${oauth.panther.root:true}")
	private boolean rootEnabled;
	@Value("${oauth.panther.endpoint:null}")
	private String endpoint;
	
	@Override
	public boolean isEnabled() {
		return "enabled".equals(enabled);
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
		return new ServiceBuilder(getAppKey())
                .apiSecret(getAppSecret())
                .callback(oauthModule.getCallbackUrl())
                .build(new PantherApi(this));
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

	public String getAppKey() {
		return appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}
	
	public String getEndpoint() {
		return endpoint;
	}

	@Override
	public Identity createUser(OAuthUser user) {
		String uid = user.getId();
		Identity newIdentity = ldapManager.createAndPersistUser(uid);
		if(newIdentity != null) {
			log.info("create user identifier by uid: {}", uid);
			securityManager.createAndPersistAuthentication(newIdentity, getProviderName(), BaseSecurity.DEFAULT_ISSUER, uid, null, null);
		}
		dbInstance.commit();
		return newIdentity;
	}
	
	@Override
	public Identity updateUser(OAuthUser user, Identity identity) {
		ldapManager.doSyncSingleUserWithLoginAttribute(identity);
		dbInstance.commit();
		return securityManager.loadIdentityByKey(identity.getKey());
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken)
	throws InterruptedException, ExecutionException, IOException {
		OAuth20Service oauthService = (OAuth20Service)service;
		OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, endpoint + "/userinfo"); 
		oauthService.signRequest((OAuth2AccessToken)accessToken, oauthRequest);
		Response oauthResponse = oauthService.execute(oauthRequest);
		String body = oauthResponse.getBody();
		return parseInfos(body);
	}

	@Override
	public String getIssuerIdentifier() {
		return endpoint;
	}

	public OAuthUser parseInfos(String body) {
		OAuthUser user = new OAuthUser();
		
		try {
			JSONObject obj = new JSONObject(body);
			JSONObject properties = obj.getJSONObject("properties");
			user.setId(getValue(properties, "uid"));
			user.setFirstName(getValue(properties, "firstName"));
			user.setLastName(getValue(properties, "lastName"));
			user.setLang(getValue(properties, "language"));
			user.setInstitutionalUserIdentifier(getValue(properties, "aarcMemberID"));
			user.setEmail(getValue(properties, "email"));
			
			log.info("User uid: " + getValue(properties, "uid") + " email:" + getValue(properties, "email"));
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
