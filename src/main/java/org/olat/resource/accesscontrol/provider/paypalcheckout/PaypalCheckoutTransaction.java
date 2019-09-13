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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PSPTransaction;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PaypalCheckoutTransaction extends PSPTransaction, ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getSecureSuccessUUID();
	
	public String getSecureCancelUUID();
	
	public String getOrderNr();
	
	
	public Long getMethodId();
	
	public Price getSecurePrice();
	
	public PaypalCheckoutStatus getStatus();
	
	public void setStatus(PaypalCheckoutStatus status);
	
	public String getPaypalOrderId();

	public void setPaypalOrderId(String id);
	

	public String getPaypalOrderStatus();

	public void setPaypalOrderStatus(String status);
	
	public String getPaypalOrderStatusReason();

	public void setPaypalOrderStatusReason(String reason);
	
	public String getPaypalAuthorizationId();

	public void setPaypalAuthorizationId(String id);
	
	public String getPaypalCaptureId();

	public void setPaypalCaptureId(String paypalCaptureId);

	public Price getCapturePrice();
	
	public void setCapturePrice(Price price);

	public String getPaypalInvoiceId();

	public void setPaypalInvoiceId(String paypalInvoiceId);

}
