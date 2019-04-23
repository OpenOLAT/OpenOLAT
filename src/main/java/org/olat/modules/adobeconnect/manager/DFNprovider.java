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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DFNprovider extends AbstractAdobeConnectProvider {
	
	private static final OLog log = Tracing.createLoggerFor(DFNprovider.class);
	
	@Override
	public String getId() {
		return "dfn";
	}

	@Override
	public String getName() {
		return "DFN";
	}

	@Override
	public AdobeConnectPrincipal getPrincipalByLogin(String login, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "lms-user-exists")
			.queryParam("login", login);

		List<AdobeConnectPrincipal> users = null;
		HttpGet get = createAdminMethod(builder, errors);
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200 || statusCode == 201) {
				users = parseUsers(response.getEntity(), errors);
			} else {
				EntityUtils.consume(response.getEntity());
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return users != null && !users.isEmpty() ? users.get(0) : null;
	}

	@Override
	public AdobeConnectPrincipal createPrincipal(Identity identity, String login, String password, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "lms-user-create")
			.queryParam("login", identity.getUser().getEmail())
			.queryParam("first-name", identity.getUser().getFirstName())
			.queryParam("last-name", identity.getUser().getLastName());

		HttpGet get = createAdminMethod(builder, errors);
		List<AdobeConnectPrincipal> users = null;
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200 || statusCode == 201) {
				users = parsePrincipals(response.getEntity(), errors);
			} else {
				EntityUtils.consume(response.getEntity());
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return users != null && !users.isEmpty() ? users.get(0) : null;
	}
	
	/**
	 * https://server/lmsapi/xml?action=lms-user-login
	 *     &login=email
	 *     &session=SessionCookie
	 */
	@Override
	public BreezeSession commonInfo(Authentication authentication, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "lms-user-login")
			.queryParam("login", authentication.getAuthusername());

		HttpGet get = createAdminMethod(builder, errors);
		BreezeSession session = null;
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				session = null;//TODO adobe extract cookie
			} else {
				EntityUtils.consume(response.getEntity());
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return session;
	}
	
	protected List<AdobeConnectPrincipal> parseUsers(HttpEntity entity, AdobeConnectErrors errors) {
		List<AdobeConnectPrincipal> users = new ArrayList<>();
		try {
			Document doc = AdobeConnectDOMHelper.getDocumentFromEntity(entity);
			if(AdobeConnectDOMHelper.isStatusOk(doc)) {
				AdobeConnectDOMHelper.print(doc);
				NodeList userList = doc.getElementsByTagName("user");
				int numOfElements = userList.getLength();
				for(int i=0; i<numOfElements; i++) {
					Element userEl = (Element)userList.item(i);
					AdobeConnectPrincipal user = new AdobeConnectPrincipal();
					user.setPrincipalId(userEl.getAttribute("principal-id"));
					users.add(user);
				}
			} else {
				AdobeConnectDOMHelper.print(doc);
				AdobeConnectDOMHelper.error(doc, errors);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return users;
	}
}
