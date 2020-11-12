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
package org.olat.resource.accesscontrol.provider.paypalcheckout.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.EventType;
import com.paypal.api.payments.Webhook;
import com.paypal.api.payments.WebhookList;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

/**
 * Helper methods to manage PayPal Webhooks (deprecated but useful).
 * 
 * Initial date: 12 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PaypalRESTWebhookProvider {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalRESTWebhookProvider.class);
	
	public String hasWebhook(String clientId, String clientSecret, boolean sandbox) {
		try {
			String mode = sandbox ? Constants.SANDBOX : Constants.LIVE;
			APIContext apiContext = new APIContext(clientId, clientSecret, mode);
			
			WebhookList webhookList = new WebhookList();
			webhookList = webhookList.getAll(apiContext);
			List<Webhook> webhooks = webhookList.getWebhooks();
			
			String webhookEndpoint = webhookEndpoint();
			
			for(Webhook webhook:webhooks) {
				String url = webhook.getUrl();
				if(url.equals(webhookEndpoint)) {
					return webhook.getId();
				}
			}
		} catch (PayPalRESTException e) {
			log.error("", e);
		}
		return null;
	}
	
	public String webhookEndpoint() {
		String serverContext = Settings.getServerContextPathURI();
		if(!serverContext.endsWith("/")) {
			serverContext += "/";
		}
		return serverContext + "checkoutv2";
	}
	
	public String createWebhook(String clientId, String clientSecret, List<String> eventNames, boolean sandbox) {
		List<EventType> eventTypes = new ArrayList<>();
		for(String eventName:eventNames) {
			EventType eventType = new EventType();
			eventType.setName(eventName);
			eventTypes.add(eventType);
		}
		
		String webhookEndpoint = webhookEndpoint();
		Webhook webhook = new Webhook();
		webhook.setUrl(webhookEndpoint);
		webhook.setEventTypes(eventTypes);
		
		try{
			String mode = sandbox ? Constants.SANDBOX : Constants.LIVE;
			APIContext apiContext = new APIContext(clientId, clientSecret, mode);
			Webhook createdWebhook = webhook.create(apiContext, webhook);
			String webhookId = createdWebhook.getId();
			log.info(Tracing.M_AUDIT, "PayPal webhook created: {}", webhookId);
			return webhookId;
		} catch (PayPalRESTException e) {
			log.error("", e);
			return null;
		}
	}
}
