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
package org.olat.core.commons.services.sms.spi;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.commons.services.sms.MessagesSPI;
import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.ui.BulksSMSConfigurationController;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("messagesSpiBulkSMS")
public class BulkSMSProvider extends AbstractSpringModule implements MessagesSPI {
	
	private static final Logger log = Tracing.createLoggerFor(BulkSMSProvider.class);
	
	private static final String TOKEN_ID = "bulksms.token.id";
	private static final String TOKEN_SECRET = "bulksms.token.secret";
	
	@Value("${websms.url:https://api.bulksms.com/v1/messages}")
	private String url;
	
	@Value("${bulksms.token.id:}")
	private String tokenId;
	@Value("${bulksms.token.secret:}")
	private String tokenSecret;
	
	@Autowired
	private HttpClientService httpClientService;
	
	@Autowired
	public BulkSMSProvider(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		tokenId = getStringPropertyValue(TOKEN_ID, tokenId);
		tokenSecret = getStringPropertyValue(TOKEN_SECRET, tokenSecret);
	}

	@Override
	public String getId() {
		return "bulksms";
	}

	@Override
	public String getName() {
		return "BulkSMS";
	}

	@Override
	public boolean isValid() {
		return StringHelper.containsNonWhitespace(tokenId) && StringHelper.containsNonWhitespace(tokenSecret);
	}
	
	@Override
	public BulksSMSConfigurationController getConfigurationController(UserRequest ureq, WindowControl wControl, Form form) {
		return new BulksSMSConfigurationController(ureq, wControl, form);
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
		setSecretStringProperty(TOKEN_ID, tokenId, true);
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
		setSecretStringProperty(TOKEN_SECRET, tokenSecret, true);
	}

	@Override
	public boolean send(String messageId, String text, String recipient)
	throws SimpleMessageException {
		HttpPost send = new HttpPost(url + "?deduplication-id=" + messageId);
		String token = StringHelper.encodeBase64(tokenId + ":" + tokenSecret);
		send.setHeader(new BasicHeader("Authorization", "Basic " + token));//NOSONAR no other choice
		send.setHeader(new BasicHeader("Content-Type", "application/json"));
		
		String phone = recipient.replace("+", "").replace(" ", "");
		String objectStr = jsonPayload(text, phone);
		HttpEntity smsEntity = new StringEntity(objectStr, ContentType.APPLICATION_JSON);
		send.setEntity(smsEntity);
		
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpclient.execute(send)) {
			int returnCode = response.getStatusLine().getStatusCode();
			String responseString = EntityUtils.toString(response.getEntity());
			if(returnCode == 200 || returnCode == 201) {
				return true;
			}
			log.error("WebSMS return an error code {}: {}", returnCode, responseString);
			return false;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private String jsonPayload(String text, String recipient) {
		try {
			JSONObject message = new JSONObject();
			message.put("body", text);
			message.put("to", recipient);
			message.put("encoding", "UNICODE");
			return message.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
