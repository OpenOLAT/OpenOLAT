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

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.model.BreezeSession;
import org.springframework.stereotype.Service;
/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DFNprovider extends AbstractAdobeConnectProvider {
	
	private static final Logger log = Tracing.createLoggerFor(DFNprovider.class);
	
	public static final String DFN_ID = "dfn";
	
	@Override
	public String getId() {
		return DFN_ID;
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
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
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
		}
		return users != null && !users.isEmpty() ? users.get(0) : null;
	}

	@Override
	public AdobeConnectPrincipal createPrincipal(Identity identity, String login, String password, AdobeConnectErrors errors) {
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder
			.queryParam("action", "lms-user-create")
			.queryParam("login", identity.getUser().getEmail())
			.queryParam("first-name", orDefault(identity.getUser().getFirstName(), "John"))
			.queryParam("last-name", orDefault(identity.getUser().getLastName(), "Doe"));

		List<AdobeConnectPrincipal> users = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
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
		
		BreezeSession session = null;
		HttpGet get = createAdminMethod(builder, errors);
		if(get != null) {
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
					CloseableHttpResponse response = httpClient.execute(get)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200) {
					session = AdobeConnectUtils.getBreezeSessionFromXml(response);
				} else {
					EntityUtils.consume(response.getEntity());
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return session;
	}
	
	@Override
	public AdobeConnectSco getScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		if(meeting == null || !StringHelper.containsNonWhitespace(meeting.getScoId())) return null;
		
		UriBuilder builder = adobeConnectModule.getAdobeConnectUriBuilder();
		builder.replacePath("/api/xml");
		builder
			.queryParam("action", "sco-info")
			.queryParam("sco-id", meeting.getScoId())
			.queryParam("filter-sco-id", meeting.getScoId())
			.queryParam("filter-type", "meeting");
		List<AdobeConnectSco> scos = sendScoRequest(builder, error);
		return scos == null || scos.isEmpty() ? null : scos.get(0);
	}

	@Override
	public boolean isManagedPassword() {
		return false;
	}
}
