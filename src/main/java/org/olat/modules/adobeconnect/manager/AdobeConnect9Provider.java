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
package org.olat.modules.adobeconnect.manager;

import static org.olat.modules.adobeconnect.manager.AdobeConnectUtils.orDefault;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdobeConnect9Provider extends AbstractAdobeConnectProvider {
	
	private static final Logger log = Tracing.createLoggerFor(AdobeConnect9Provider.class);
	
	@Override
	public String getId() {
		return "connect9";
	}

	@Override
	public String getName() {
		return "Adobe Connect Cloud";
	}

	@Override
	public AdobeConnectPrincipal getPrincipalByLogin(String login, AdobeConnectErrors error) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "principal-list")
			.queryParam("filter-login", login);
		List<AdobeConnectPrincipal> users = sendPrincipalRequest(builder, error);
		return users != null && !users.isEmpty() ? users.get(0) : null;
	}

	/**
	 * https://example.com/api/xml?action=principal-update
	 *    &first-name=jazz
	 *    &last-name=doe
	 *    &login=jazz99@doe.com
	 *    &password=hello
	 *    &type=user
	 *    &send-email=true
	 *    &has-children=0
	 *    &email=jazz99@doe.com
	 */
	@Override
	public AdobeConnectPrincipal createPrincipal(Identity identity, String login, String password, AdobeConnectErrors error) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "principal-update")
			.queryParam("first-name", orDefault(identity.getUser().getFirstName(), "John"))
			.queryParam("last-name", orDefault(identity.getUser().getLastName(), "Doe"));
		if(!adobeConnectModule.isLoginCompatibilityMode()) {
			builder
				.queryParam("email", identity.getUser().getEmail());
		}
		builder
			.queryParam("login", login)
			.queryParam("ext-login", login)
			.queryParam("password", password)
			.queryParam("type", "user")
			.queryParam("send-email", "false")
			.queryParam("has-children", "0");
		List<AdobeConnectPrincipal> users = sendPrincipalRequest(builder, error);
		return users != null && !users.isEmpty() ? users.get(0) : null;
	}

	@Override
	public BreezeSession commonInfo(Authentication authentication, AdobeConnectErrors error) {
		if(authentication == null) {
			return null;
		}
		
		if(!StringHelper.containsNonWhitespace(authentication.getCredential())) {
			// special case of the administrator which setup the adobe account
			if(adobeConnectModule.getAdminLogin().equals(authentication.getAuthusername())) {
				return getAdminSession(error);
			}
			return null;
		}

		String login = Encoder.decrypt(authentication.getCredential(), authentication.getSalt(), Encoder.Algorithm.aes);
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "login")
			.queryParam("login", authentication.getAuthusername())
			.queryParam("password", login);
		if(StringHelper.containsNonWhitespace(adobeConnectModule.getAccountId())) {
			builder = builder.queryParam("account-id", adobeConnectModule.getAccountId());
		}
		
		URI uri = builder.build();
		BreezeSession session = null;
		HttpGet getInfo = new HttpGet(uri);
		try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
			CloseableHttpResponse response = httpClient.execute(getInfo)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200 && AdobeConnectUtils.isStatusOk(response.getEntity())) {
				Header header = response.getFirstHeader("Set-Cookie");
				session = BreezeSession.valueOf(header);
			} else {
				EntityUtils.consume(response.getEntity());
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return session;
	}
	
	@Override
	public boolean isManagedPassword() {
		return true;
	}
}
