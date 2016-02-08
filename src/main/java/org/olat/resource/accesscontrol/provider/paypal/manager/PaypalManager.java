/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.manager;

import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;

import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.PaymentDetailsResponse;

/**
 * 
 * Description:<br>
 * Manager for Paypal
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface PaypalManager {
	
	/**
	 * Return the URL to redirect the customer from OLAT to Paypal
	 * @param response
	 * @return
	 */
	public String getPayRedirectUrl(PayResponse response);
	
	/**
	 * Return the verification URL for IPN notifications
	 * @return
	 */
	public String getIpnVerificationUrl();
	
	public PaypalTransaction loadTransactionByUUID(String uuid);
	
	public PaypalTransaction loadTransaction(Order order, OrderPart part);
	
	public List<PSPTransaction> loadTransactions(List<Order> orders);
	
	public List<PaypalTransaction> findTransactions(String transactionId);
	
	/**
	 * Update the status of a transaction
	 * @param transaction
	 * @param status
	 */
	public void updateTransaction(PaypalTransaction transaction, PaypalTransactionStatus status);
	
	/**
	 * Update the paypal transaction with the uuid (success/cancel)
	 * @param uuid
	 */
	public void updateTransaction(String uuid);
	
	/**
	 * Update the paypal transaction with the informations obtain from IPN Notifications
	 * @param values
	 * @param verified
	 */
	public void updateTransactionByNotification(Map<String,String> values, boolean verified);
	
	/**
	 * Use as test method for login informations
	 * @return
	 */
	public boolean convertCurrency();
	
	/**
	 * Return the payment details from Paypal
	 * @param key
	 * @return
	 */
	public PaymentDetailsResponse paymentDetails(String key);
	
	/**
	 * Init a payment
	 * @param delivery
	 * @param offer
	 * @param mapperUri
	 * @param sessionId
	 * @return
	 */
	public PayResponse request(Identity delivery, OfferAccess offer, String mapperUri, String sessionId);
}
