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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CreateSmartOrder;
import org.olat.resource.accesscontrol.ui.AccessEvent;

/**
 * 
 * Initial date: 6 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalSmartButtonMapper implements Mapper {
	
	private CreateSmartOrder order;
	private final OfferAccess link;
	private final Identity identity;
	private final PaypalCheckoutManager paypalManager;
	private final PaypalSmartButtonPaymentController controller;
	
	public PaypalSmartButtonMapper(Identity identity, OfferAccess link, PaypalSmartButtonPaymentController controller) {
		this.link = link;
		this.identity = identity;
		this.controller = controller;
		paypalManager = CoreSpringFactory.getImpl(PaypalCheckoutManager.class);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		MediaResource resource = null;
		if(relPath.contains("create-paypal-transaction")) {
			resource = createPaypalTransaction();
		} else if(relPath.contains("approve-paypal-transaction")) {
			resource = approvePaypalTransaction(request);
		} else if(relPath.contains("cancel-paypal-transaction")) {
			resource = cancelPaypalTransaction();
		} else if(relPath.contains("error-paypal-transaction")) {
			resource = errorPaypalTransaction();
		}
		return resource;
	}
	
	private MediaResource createPaypalTransaction() {
		if(order == null) {
			order = paypalManager.createOrder(identity, link);
		}
		if(order != null) {
			if(StringHelper.containsNonWhitespace(order.getPaypalOrderId())) {
				return buildOrderResource(order.getPaypalOrderId());
			} else if(!order.isReservationOk()) {
				JSONObject obj = new JSONObject();
				obj.put("reservation", false);
				return new JSONMediaResource(obj, "UTF-8");
			}
		}
		return null;
	}
	
	private MediaResource approvePaypalTransaction(HttpServletRequest request) {
		if(order != null && StringHelper.containsNonWhitespace(order.getPaypalOrderId())) {
			paypalManager.approveTransaction(order.getPaypalOrderId());

			UserRequest ureq = new UserRequestImpl("m", request, null);
			controller.fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
			return buildOrderResource(order.getPaypalOrderId());
		}
		return null;
	}
	
	private MediaResource cancelPaypalTransaction() {
		if(order != null && StringHelper.containsNonWhitespace(order.getPaypalOrderId())) {
			paypalManager.cancelTransaction(order.getPaypalOrderId());
			return buildOrderResource(order.getPaypalOrderId());
		}
		return null;
	}
	
	private MediaResource errorPaypalTransaction() {
		if(order != null && StringHelper.containsNonWhitespace(order.getPaypalOrderId())) {
			paypalManager.errorTransaction(order.getPaypalOrderId());
			return buildOrderResource(order.getPaypalOrderId());
		}
		return null;
	}
	
	private JSONMediaResource buildOrderResource(String paypalOrderId) {
		JSONObject obj = new JSONObject();
		obj.put("orderID", paypalOrderId);
		obj.put("reservation", true);
		return new JSONMediaResource(obj, "UTF-8");
	}
}
