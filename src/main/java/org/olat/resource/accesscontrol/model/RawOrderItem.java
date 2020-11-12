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
package org.olat.resource.accesscontrol.model;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Data holder for the native queries.
 * 
 * Initial date: 04.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RawOrderItem {
	
	private final Long orderKey;
	private final String orderNr;
	private final Price total;
	private final Date creationDate;
	private final String orderStatus;
	private final Long deliveryKey;
	private final String resourceName;
	
	private final String trxStatus;
	private final String trxMethodIds;
	private final String pspTrxStatus;
	private final String checkoutTrxStatus;
	private final String checkoutOrderTrxStatus;
	
	private final String username;
	private final String[] userProperties;
	
	public RawOrderItem(Long orderKey, String orderNr, String totalCurrencyCode, BigDecimal totalAmount,
			Date creationDate, String orderStatus, Long deliveryKey, String resourceName,
			String trxStatus, String trxMethodIds, String pspTrxStatus,
			String checkoutTrxStatus, String checkoutOrderTrxStatus,
			String username, String[] userProperties) {
		this.orderKey = orderKey;
		this.orderNr = orderNr;
		this.total = new PriceImpl(totalAmount, totalCurrencyCode);
		this.creationDate = creationDate;
		this.orderStatus = orderStatus;
		this.deliveryKey = deliveryKey;
		this.resourceName = resourceName;
		this.trxStatus = trxStatus;
		this.trxMethodIds = trxMethodIds;
		this.pspTrxStatus = pspTrxStatus;
		this.checkoutTrxStatus = checkoutTrxStatus;
		this.checkoutOrderTrxStatus = checkoutOrderTrxStatus;
		this.username = username;
		this.userProperties = userProperties;
	}

	public Long getOrderKey() {
		return orderKey;
	}

	public String getOrderNr() {
		return orderNr;
	}

	public Price getTotal() {
		return total;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public Long getDeliveryKey() {
		return deliveryKey;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getTrxStatus() {
		return trxStatus;
	}

	public String getTrxMethodIds() {
		return trxMethodIds;
	}

	public String getPspTrxStatus() {
		return pspTrxStatus;
	}

	public String getCheckoutTrxStatus() {
		return checkoutTrxStatus;
	}

	public String getCheckoutOrderTrxStatus() {
		return checkoutOrderTrxStatus;
	}

	public String getUsername() {
		return username;
	}

	public String[] getUserProperties() {
		return userProperties;
	}
}
