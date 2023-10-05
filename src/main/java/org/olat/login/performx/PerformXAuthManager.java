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
package org.olat.login.performx;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.validation.AllOkValidationResult;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PerformXAuthManager implements AuthenticationSPI {
	
	private static final Logger log = Tracing.createLoggerFor(PerformXAuthManager.class);

	@Autowired
	private UserManager userManager;
	@Autowired
	private PerformXModule performxModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	
	@Override
	public boolean isEnabled() {
		return performxModule.isEnabled();
	}

	@Override
	public List<String> getProviderNames() {
		return Collections.singletonList(PerformXModule.PERFORMX_AUTH);
	}
	
	@Override
	public boolean canAddAuthenticationUsername(String provider) {
		return canChangeAuthenticationUsername(provider);
	}

	@Override
	public boolean canChangeAuthenticationUsername(String provider) {
		return performxModule.isEnabled() && PerformXModule.PERFORMX_AUTH.equals(provider);
	}

	@Override
	public boolean changeAuthenticationUsername(Authentication authentication, String newUsername) {
		authentication.setAuthusername(newUsername);
		authentication = authenticationDao.updateAuthentication(authentication);
		webDAVAuthManager.removeDigestAuthentications(authentication.getIdentity());
		return authentication != null;
	}

	@Override
	public ValidationResult validateAuthenticationUsername(String name, String provider, Identity identity) {
		return new AllOkValidationResult();
	}

	@Override
	public Identity authenticate(String login, String password, AuthenticationStatus status) {
		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
			String clientId = UUID.randomUUID().toString().replace("-", "");
			StringBuilder query = new StringBuilder();
			query.append("name=")
			    .append(login)
			    .append("&password=")
			    .append(password)
			    .append("&clientid=")
			    .append(clientId);
			
			String path = "/rest2/SecUserMethods/method/Authenticate";
			URI performxUri = URI.create(performxModule.getPerformxServerUrl());
			URI uri = new URI(performxUri.getScheme(), null, performxUri.getHost(), performxUri.getPort(),
					path, query.toString(), null);
			
			HttpGet httpget = new HttpGet(uri);
			httpget.addHeader("Accept", MediaType.APPLICATION_JSON);
			//httpget.addHeader("Accept-Language", "de");
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			String content = EntityUtils.toString(entity);
			if(statusCode == 200) {
				String token = extractToken(content);
				if(StringHelper.containsNonWhitespace(token)) {
					Authentication authentication;
					Identity identity = null;
					if (MailHelper.isValidEmailAddress(login)){
						identity = userManager.findUniqueIdentityByEmail(login);
						if(identity == null) {
							authentication = securityManager.findAuthenticationByAuthusername(login, PerformXModule.PERFORMX_AUTH, BaseSecurity.DEFAULT_ISSUER);
						} else {
							authentication = securityManager.findAuthentication(identity, PerformXModule.PERFORMX_AUTH, BaseSecurity.DEFAULT_ISSUER);
						}
					} else {
						authentication = securityManager.findAuthenticationByAuthusername(login, PerformXModule.PERFORMX_AUTH, BaseSecurity.DEFAULT_ISSUER);
					}
					
					if (authentication == null) {
						log.info(Tracing.M_AUDIT, "Error authenticating user {} via provider PerformX", login);
						return null;
					}
					
					identity = authentication.getIdentity();
					if(identity != null && webDAVAuthManager != null) {
						webDAVAuthManager.upgradePassword(identity, login, password);
					}
					status.setProvider(PerformXModule.PERFORMX_AUTH);
					status.setStatus(AuthHelper.LOGIN_OK);
					return identity;
				}
				status.setStatus(AuthHelper.LOGIN_FAILED);
				return null;
			}
			status.setStatus(AuthHelper.LOGIN_NOTAVAILABLE);
			return null;
		} catch (URISyntaxException | IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private String extractToken(String content) {
		try {
			JSONObject obj = new JSONObject(content);
			JSONArray resourceArr = obj.getJSONArray("resource");
			JSONObject resource = resourceArr.getJSONObject(0);
			JSONArray data = resource.getJSONArray("data");
			JSONArray dataL1 = data.getJSONArray(0);
			return dataL1.getString(0);
		} catch (JSONException e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		//
	}
}
