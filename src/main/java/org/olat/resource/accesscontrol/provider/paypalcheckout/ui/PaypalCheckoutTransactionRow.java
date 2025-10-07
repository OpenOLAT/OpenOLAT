/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 7 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionRow extends UserPropertiesRow {
	
	private final PaypalCheckoutTransaction transaction;

	public PaypalCheckoutTransactionRow(PaypalCheckoutTransaction transaction, Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.transaction = transaction;
	}
	
	public Long getKey() {
		return transaction.getKey();
	}

	public Date getCreationDate() {
		return transaction.getCreationDate();
	}

	public String getOrderNr() {
		return transaction.getOrderNr();
	}

	public String getPaypalOrderId() {
		return transaction.getPaypalOrderId();
	}

	public Price getSecurePrice() {
		return transaction.getSecurePrice();
	}

	public PaypalCheckoutStatus getStatus() {
		return transaction.getStatus();
	}

	public String getPaypalOrderStatus() {
		return transaction.getPaypalOrderStatus();
	}

	public PaypalCheckoutTransaction getTransaction() {
		return transaction;
	}
}
