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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.commons.services.sms.MessagesSPI;
import org.olat.core.commons.services.sms.ui.WebSMSConfigurationController;
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
 * Implementation for https://websms.ch
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("messagesSpiWebSMS")
public class WebSMSProvider extends AbstractSpringModule implements MessagesSPI {
	
	private static final Logger log = Tracing.createLoggerFor(WebSMSProvider.class);
	private final BasicCredentialsProvider provider = new BasicCredentialsProvider();
	
	private static final String NAME = "websms.username";
	private static final String CREDENTIALS = "websms.password";
	
	@Value("${websms.url:https://api.websms.com/rest/smsmessaging/text}")
	private String url;
	
	@Value("${websms.username:}")
	private String username;
	@Value("${websms.password:}")
	private String password;
	
	private boolean test = false;
	
	@Autowired
	private HttpClientService httpClientService;
	
	@Autowired
	public WebSMSProvider(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	/**
	 * Method means for unit tests. The changes are not persisted.
	 * 
	 * @param username
	 * @param password
	 */
	protected void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
		provider.setCredentials(new AuthScope("api.websms.com", 443),
				new UsernamePasswordCredentials(username, password));
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
		username = getStringPropertyValue(NAME, username);
		password = getStringPropertyValue(CREDENTIALS, password);

		provider.setCredentials(new AuthScope("api.websms.com", 443),
				new UsernamePasswordCredentials(username, password));
	}
	
	protected void setTest(boolean test) {
		this.test = test;
	}

	@Override
	public String getId() {
		return "websms";
	}

	@Override
	public String getName() {
		return "WebSMS";
	}

	@Override
	public boolean isValid() {
		return StringHelper.containsNonWhitespace(username) && StringHelper.containsNonWhitespace(password);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		setSecretStringProperty(NAME, username, true);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		setSecretStringProperty(CREDENTIALS, password, true);
	}

	@Override
	public WebSMSConfigurationController getConfigurationController(UserRequest ureq, WindowControl wControl, Form form) {
		return new WebSMSConfigurationController(ureq, wControl, form);
	}

	@Override
	public boolean send(String messageId, String text, String recipient) {
		HttpPost send = new HttpPost(url);
		
		String phone = recipient.replace("+", "").replace(" ", "");
		String objectStr = jsonPayload(messageId, text, Long.valueOf(phone));
		HttpEntity smsEntity = new StringEntity(objectStr, ContentType.APPLICATION_JSON);
		send.setEntity(smsEntity);
		
		try(CloseableHttpClient httpclient = httpClientService.createHttpClientBuilder()
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpclient.execute(send)) {
			int returnCode = response.getStatusLine().getStatusCode();
			String responseString = EntityUtils.toString(response.getEntity());
			if(returnCode == 200 || returnCode >= 2000) {
				return true;
			}
			log.error("WebSMS return an error code " + returnCode + ": " + responseString);
			return false;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}

	/**
	 * {
	 *  "userDataHeaderPresent" : false,
	 *  "messageContent" : [ "...", ... ],
	 *  "test" : false,
	 *  "recipientAddressList" : [ ..., ... ],
	 *  "senderAddress" : "...",
	 *  "senderAddressType" : "national",
	 *  "sendAsFlashSms" : false,
	 *  "notificationCallbackUrl" : "...",
	 *  "clientMessageId" : "...",
	 *  "priority" : ...,
	 * }
	 * @param obj
	 * @return
	 */
	private String jsonPayload(String messageId, String text, Long recipient) {
		try {
			JSONObject message = new JSONObject();
			message.put("userDataHeaderPresent", false);
			message.put("messageContent", text);
			message.put("test", test);
			
			JSONArray recipients = new JSONArray();
			recipients.put(recipient);
			message.put("recipientAddressList", recipients);//arrsx
			//optional message.put("senderAddress", "");
			//optional message.put("senderAddressType", "national");
			//optional message.put("sendAsFlashSms", false);
			//optional message.put("senderAddress", "");//national international shortcode alphanumeric
			//optional message.put("notificationCallbackUrl", "");
			message.put("clientMessageId", messageId);
			//optional message.put("priority", false);
			return message.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
