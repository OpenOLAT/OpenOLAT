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
package org.olat.resource.accesscontrol.provider.paypalcheckout.model;

import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckoutRequest {
	
	private String status;
	private String redirectToPaypalUrl;
	private PaypalCheckoutTransaction checkoutTransactionn;

	public String getRedirectToPaypalUrl() {
		return redirectToPaypalUrl;
	}

	public void setRedirectToPaypalUrl(String url) {
		this.redirectToPaypalUrl = url;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public PaypalCheckoutTransaction getCheckoutTransactionn() {
		return checkoutTransactionn;
	}

	public void setCheckoutTransactionn(PaypalCheckoutTransaction checkoutTransactionn) {
		this.checkoutTransactionn = checkoutTransactionn;
	}
}
