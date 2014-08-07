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

package org.olat.resource.accesscontrol.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderImpl;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderLineImpl;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.OrderPartImpl;
import org.olat.resource.accesscontrol.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * manager for the order. Orders are a part of the confirmation of an access
 * to a resource. The second part is the transaction.
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACOrderManagerImpl implements ACOrderManager {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public OrderImpl createOrder(Identity delivery) {
		OrderImpl order = new OrderImpl();
		order.setDelivery(delivery);
		order.setOrderStatus(OrderStatus.NEW);
		dbInstance.saveObject(order);
		return order;
	}

	@Override
	public OrderPart addOrderPart(Order order) {
		OrderPartImpl orderPart = new OrderPartImpl();
		dbInstance.saveObject(orderPart);
		order.getParts().add(orderPart);
		return orderPart;
	}

	@Override
	public OrderLine addOrderLine(OrderPart part, Offer offer) {
		OrderLineImpl line = new OrderLineImpl();
		line.setOffer(offer);
		line.setUnitPrice(offer.getPrice().clone());
		line.setTotal(line.getUnitPrice().clone());
		dbInstance.saveObject(line);
		part.getOrderLines().add(line);
		return line;
	}
	
	@Override
	public Order save(Order order) {
		if(order.getKey() == null) {
			dbInstance.saveObject(order);
		} else {
			dbInstance.updateObject(order);
		}
		return order;
	}
	
	@Override
	public Order save(Order order, OrderStatus status) {
		((OrderImpl)order).setOrderStatus(status);
		if(order.getKey() == null) {
			dbInstance.saveObject(order);
		} else {
			dbInstance.updateObject(order);
		}
		return order;
	}
	
	@Override
	public Order saveOneClick(Identity delivery, OfferAccess link) {
		return saveOneClick(delivery, link, OrderStatus.PAYED);
	}
	
	@Override
	public Order saveOneClick(Identity delivery, OfferAccess link, OrderStatus status) {
		OrderImpl order = createOrder(delivery);
		order.setOrderStatus(status);
		if(link.getOffer().getPrice().isEmpty()) {
			order.setCurrencyCode("CHF");
		} else {
			order.setCurrencyCode(link.getOffer().getPrice().getCurrencyCode());
		}
		OrderPart part = addOrderPart(order);
		addOrderLine(part, link.getOffer());
		order.recalculate();
		
		dbInstance.updateObject(order);
		dbInstance.updateObject(part);
		
		return order;
	}
	
	@Override
	public List<Order> findOrdersByDelivery(Identity delivery, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select order from ").append(OrderImpl.class.getName()).append(" order")
			.append(" where order.delivery.key=:deliveryKey");
		if(status != null && status.length > 0) {
			sb.append(" and order.orderStatusStr in (:status)");
		}
		
		TypedQuery<Order> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("deliveryKey", delivery.getKey());
		if(status != null && status.length > 0) {
			List<String> statusStr = new ArrayList<String>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
		}
	
		List<Order> orders = query.getResultList();
		return orders;
	}

	@Override
	public List<Order> findOrdersByResource(OLATResource resource, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from ").append(OrderImpl.class.getName()).append(" o")
			.append(" inner join o.parts orderPart ")
			.append(" inner join orderPart.orderLines orderLine ")
			.append(" inner join orderLine.offer offer ")
			.append(" where offer.resource.key=:resourceKey");
		if(status != null && status.length > 0) {
			sb.append(" and o.orderStatusStr in (:status)");
		}
		
		TypedQuery<Order> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("resourceKey", resource.getKey());
		if(status != null && status.length > 0) {
			List<String> statusStr = new ArrayList<String>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
		}
	
		List<Order> orders = query.getResultList();
		return orders;
	}
	
	@Override
	public List<Order> findOrders(OLATResource resource, Identity delivery, Long orderNr, Date from, Date to, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from ").append(OrderImpl.class.getName()).append(" o");
		if(resource != null) {
			sb.append(" inner join o.parts orderPart ")
				.append(" inner join orderPart.orderLines orderLine ")
				.append(" inner join orderLine.offer offer");
		}

		boolean where = false;
		if(resource != null) {
			where = appendAnd(sb, where);
			sb.append("offer.resource.key=:resourceKey");
		}
		if(delivery != null) {
			where = appendAnd(sb, where);
			sb.append("order.delivery.key=:deliveryKey");
		}
		if(status != null && status.length > 0) {
			where = appendAnd(sb, where);
			sb.append("o.orderStatusStr in (:status)");
		}
		if(from != null) {
			where = appendAnd(sb, where);
			sb.append("o.creationDate >=:from");
		}
		if(to != null) {
			where = appendAnd(sb, where);
			sb.append("o.creationDate <=:to");
		}
		if(orderNr != null) {
			where = appendAnd(sb, where);
			sb.append("o.key=:orderNr");
		}

		TypedQuery<Order> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Order.class);
		if(status != null && status.length > 0) {
			List<String> statusStr = new ArrayList<String>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
		}
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		if(delivery != null) {
			query.setParameter("deliveryKey", delivery.getKey());
		}
		if(orderNr != null) {
			query.setParameter("orderNr", orderNr);
		}
		if(from != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(from);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			query.setParameter("from", cal.getTime(), TemporalType.TIMESTAMP);
		}
		if(to != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(to);
			cal.set(Calendar.HOUR, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 0);
			query.setParameter("to", cal.getTime(), TemporalType.TIMESTAMP);
		}
	
		List<Order> orders = query.getResultList();
		return orders;
	}
	
	private boolean appendAnd(StringBuilder sb, boolean where) {
		if(where) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}

	@Override
	public Order loadOrderByKey(Long orderKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select order from ").append(OrderImpl.class.getName()).append(" order")
			.append(" where order.key=:orderKey");
		
		List<Order> orders = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("orderKey", orderKey)
				.getResultList();
		if(orders.isEmpty()) return null;
		return orders.get(0);
	}
	
	@Override
	public Order loadOrderByNr(String orderNr) {
		Long orderKey = new Long(orderNr);
		return loadOrderByKey(orderKey);
	}
}
