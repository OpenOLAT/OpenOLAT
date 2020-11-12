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
package org.olat.resource.accesscontrol.provider.paypalcheckout;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.manager.CheckoutV2Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.payments.Capture;
import com.paypal.payments.LinkDescription;

/**
 * 
 * Initial date: 11 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("paypalCheckoutCallback")
public class PaypalCheckoutDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalCheckoutDispatcher.class);

	@Autowired
	private AccessControlModule accessModule;
	@Autowired
	private PaypalCheckoutManager paypalCheckoutManager;
	@Autowired
	private CheckoutV2Provider checkoutV2Provider;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if(accessModule.isPaypalCheckoutEnabled()) {
				String body = getBody(request);
				if(StringHelper.containsNonWhitespace(body)) {
					JSONObject object = new JSONObject(body);
					String eventType = object.getString("event_type");
					String resourceType = object.getString("resource_type");
					if("PAYMENT.CAPTURE.COMPLETED".equals(eventType) && "capture".equals(resourceType)) {
						processCaptureCompletedNotification(object);
					}
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (Exception e) {
			log.error("", e);
			DispatcherModule.setServerError(response);
		}
	}
	
	private void processCaptureCompletedNotification(JSONObject object) {
		JSONObject resource = object.getJSONObject("resource");
		String resourceId = resource.getString("id");
		String resourceStatus = resource.getString("status");
		log.info(Tracing.M_AUDIT, "Process capture event completed: {} with status {}", resourceId, resourceStatus);
		
		if(StringHelper.containsNonWhitespace(resourceId) && ("PENDING".equals(resourceStatus) || "COMPLETED".equals(resourceStatus))) {
			Capture capture = checkoutV2Provider.getCapture(resourceId);
			if(capture != null) {
				String authorizationId = authorizationIdFrom(capture.links());
				if("PENDING".equals(resourceStatus) && StringHelper.containsNonWhitespace(authorizationId)) {
					log.info(Tracing.M_AUDIT, "Process capture completed but pending: {} with capture status {}", authorizationId, capture.status());
					paypalCheckoutManager.approveAuthorization(authorizationId, capture);
				} else if("COMPLETED".equals(resourceStatus) && StringHelper.containsNonWhitespace(authorizationId)) {
					log.info(Tracing.M_AUDIT, "Process capture completed: {} with capture status {}", authorizationId, capture.status());
					paypalCheckoutManager.approveAuthorization(authorizationId, capture);
				}
			}
		}
	}

	private String authorizationIdFrom(List<LinkDescription> links) {
		for(LinkDescription link:links) {
			String authorizationId = authorizationIdFrom(link);
			if(StringHelper.containsNonWhitespace(authorizationId)) {
				return authorizationId;
			}
		}
		return null;
	}
	
	private String authorizationIdFrom(LinkDescription link) {
		String href = link.href();
		if("up".equals(link.rel()) && href.contains("/v2/payments/authorizations/")) {
			int last = href.lastIndexOf('/') + 1;
			String val = href.substring(last);
			if(StringHelper.containsNonWhitespace(val)) {
				return val;
			}
		}
		return null;
		
	}
	
	private String getBody(HttpServletRequest request) {
		try(InputStream in=request.getInputStream()) {
			return FileUtils.load(in, "UTF-8");
		} catch (IOException e) {
			log.error("", e);
			return "";
		}
	}
}
