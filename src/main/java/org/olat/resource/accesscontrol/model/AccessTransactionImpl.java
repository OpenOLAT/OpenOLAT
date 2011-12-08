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
