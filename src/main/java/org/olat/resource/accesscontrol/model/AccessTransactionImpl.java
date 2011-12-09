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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.StringHelper;

public class AccessTransactionImpl extends PersistentObject implements AccessTransaction {

	private Price amount;
	private Order order;
	private OrderPart orderPart;
	
	private AccessMethod method;
	private String statusStr = AccessTransactionStatus.NEW.name();
	
	public AccessTransactionImpl(){
		//
	}
	
	@Override
	public AccessMethod getMethod() {
		return method;
	}
	
	public void setMethod(AccessMethod method) {
		this.method = method;
	}
	
	@Override
	public Price getAmount() {
		return amount;
	}

	public void setAmount(Price amount) {
		this.amount = amount;
	}

	@Override
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	@Override
	public OrderPart getOrderPart() {
		return orderPart;
	}
	
	public void setOrderPart(OrderPart orderPart) {
		this.orderPart = orderPart;
	}
	
	public AccessTransactionStatus getStatus() {
		if(StringHelper.containsNonWhitespace(statusStr)) {
			return AccessTransactionStatus.valueOf(statusStr);
		}
		return null;
	}

	public void setStatus(AccessTransactionStatus status) {
		if(status == null) {
			statusStr = null;
		} else {
			statusStr = status.name();
		}
	}

	public String getStatusStr() {
		return statusStr;
	}

	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 93791 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AccessTransactionImpl) {
			AccessTransactionImpl accessTransaction = (AccessTransactionImpl)obj;
			return equalsByPersistableKey(accessTransaction);
		}
		return false;
	}
}
