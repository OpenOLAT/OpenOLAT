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
package org.olat.login.tocco;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manage the authentication via Tocco. The API doesn't support user name changes
 * or password changes. Tocco support 2-Factors authentication but not OpenOlat.
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("toccoAuthenticationSpi")
public class ToccoAuthManager implements AuthenticationSPI {

	private static final Logger log = Tracing.createLoggerFor(ToccoAuthManager.class);
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ToccoLoginModule toccoLoginModule;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	@Autowired
	private HttpClientService httpClientService;

	@Override
	public List<String> getProviderNames() {
		return List.of(ToccoLoginModule.TOCCO_PROVIDER);
	}

	@Override
	public Identity authenticate(String login, String password) {
		try(CloseableHttpClient httpclient = httpClientService.createThreadSafeHttpClient(false)) {
			String serverUrl = toccoLoginModule.getToccoServerUrl();
			URL loginUri = new URL(serverUrl);
			HttpPost loginPost = new HttpPost(loginUri.toURI());
			
			List<NameValuePair> pairList = new ArrayList<>();
			pairList.add(new BasicNameValuePair("username", "openolat"));
			pairList.add(new BasicNameValuePair("password", "fxOO2021!"));
			HttpEntity myEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
			loginPost.setEntity(myEntity);

			HttpResponse response = httpclient.execute(loginPost);
			int status = response.getStatusLine().getStatusCode();
			String content = EntityUtils.toString(response.getEntity());
			if(checkResponse(status, content)) {
				Authentication authentication = securityManager
						.findAuthenticationByAuthusername(login, ToccoLoginModule.TOCCO_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
				if(authentication != null) {
					Identity identity = authentication.getIdentity();
					if(identity != null && webDAVAuthManager != null) {
						webDAVAuthManager.upgradePassword(identity, login, password);
					}
					return identity;
				}
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	private boolean checkResponse(int status, String content) {
		if(status == 200) {
			try {
				JSONObject obj = new JSONObject(content);
				return obj.optBoolean("success");
			} catch (JSONException e) {
				log.error("Cannot parse response: {}", content, e);
			}
		}
		return false;
	}

	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		return false;
	}

	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		return false;
	}

	@Override
	public ValidationResult validateAuthenticationUsername(String name, Identity identity) {
		return null;
	}

	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		//
	}
}
