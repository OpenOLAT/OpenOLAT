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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CheckoutRequest;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CreateSmartOrder;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PaypalCheckoutManager {
	
	public CheckoutRequest request(Identity identity, OfferAccess link, String mapperUri, String sessionId);
	
	public void updateTransaction(String uuid);
	
	/**
	 * Create an order in OpenOlat and in Paypal, reserve the access
	 * if needed.
	 * 
	 * @param delivery The identity which buy the access
	 * @param offerAccess The offer
	 * @return The order id or null if the reservation fails
	 */
	public CreateSmartOrder createOrder(Identity delivery, OfferAccess offerAccess);
	
	/**
	 * 
	 * @param paypalOrderId The order id of the Paypal order.
	 */
	public void approveTransaction(String paypalOrderId);
	
	/**
	 * 
	 * @param paypalOrderId The order id of the Paypal order.
	 */
	public void cancelTransaction(String paypalOrderId);
	
	/**
	 * 
	 * @param paypalOrderId The order id of the Paypal order.
	 */
	public void errorTransaction(String paypalOrderId);
	

	public PaypalCheckoutTransaction loadTransaction(Order order, OrderPart part);
	
	public PaypalCheckoutTransaction loadTransactionByUUID(String uuid);

	public List<PSPTransaction> loadTransactions(List<Order> orders);
	
	public Order getOrder(PaypalCheckoutTransaction trx);
	
	public List<PaypalCheckoutTransaction> searchTransactions(String id);
	
}
