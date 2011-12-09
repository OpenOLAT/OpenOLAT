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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * The order contains a list of order part. Every Order part links
 * a set of order lines to a payment.
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderImpl extends PersistentObject implements Order, ModifiedInfo {

	private boolean valid;
	private Date lastModified;
	private Identity delivery;
	private String orderStatus;
	
	private Price total;
	private Price totalOrderLines;
	private Price discount;
	
	private String currencyCode;
	
	private List<OrderPart> parts;
	
	@Override
	public String getOrderNr() {
		return getKey() == null ? "" : getKey().toString();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	@Override
	public Identity getDelivery() {
		return delivery;
	}

	public void setDelivery(Identity delivery) {
		this.delivery = delivery;
	}

	@Override
	public OrderStatus getOrderStatus() {
		if(StringHelper.containsNonWhitespace(orderStatus)) {
			return OrderStatus.valueOf(orderStatus);
		}
		return null;
	}
	
	public void setOrderStatus(OrderStatus status) {
		if(status == null) {
			orderStatus = null;
		} else {
			orderStatus = status.name();
		}
	}

	public String getOrderStatusStr() {
		return orderStatus;
	}

	public void setOrderStatusStr(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@Override
	public String getCurrencyCode() {
		return currencyCode;
	}
	
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Override
	public Price getTotal() {
		return total;
	}

	public void setTotal(Price total) {
		this.total = total;
	}

	@Override
	public Price getTotalOrderLines() {
		return totalOrderLines;
	}

	public void setTotalOrderLines(Price totalOrderLines) {
		this.totalOrderLines = totalOrderLines;
	}

	@Override
	public Price getDiscount() {
		return discount;
	}

	public void setDiscount(Price discount) {
		this.discount = discount;
	}

	@Override
	public List<OrderPart> getParts() {
		if(parts == null) {
			parts = new ArrayList<OrderPart>();
		}
		return parts;
	}

	public void setParts(List<OrderPart> parts) {
		this.parts = parts;
	}
	
	public void recalculate() {
		totalOrderLines = new PriceImpl(BigDecimal.ZERO, getCurrencyCode());
		for(OrderPart part : getParts()) {
			((OrderPartImpl)part).recalculate(getCurrencyCode());
			totalOrderLines = totalOrderLines.add(part.getTotalOrderLines());
		}

		total = totalOrderLines.clone();
		
		if(discount == null) {
			discount = new PriceImpl(BigDecimal.ZERO, getCurrencyCode());
		}
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderImpl) {
			OrderImpl order = (OrderImpl)obj;
			return equalsByPersistableKey(order);
		}
		return false;
	}
}
