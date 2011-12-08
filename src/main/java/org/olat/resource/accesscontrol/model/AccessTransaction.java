package org.olat.resource.accesscontrol.model;

import java.util.Date;

public interface AccessTransaction {

	public Long getKey();

	public Date getCreationDate();
	
	public AccessTransactionStatus getStatus();
	
	public Price getAmount();
	
	public Order getOrder();
	
	public OrderPart getOrderPart();
	
	public AccessMethod getMethod();
	
}
