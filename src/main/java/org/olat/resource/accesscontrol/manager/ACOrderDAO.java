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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.NativeQueryBuilder;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.OrderImpl;
import org.olat.resource.accesscontrol.model.OrderLineImpl;
import org.olat.resource.accesscontrol.model.OrderPartImpl;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.user.propertyhandlers.UserPropertyHandler;
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
public class ACOrderDAO {

	@Autowired
	private DB dbInstance;

	public OrderImpl createOrder(Identity delivery) {
		OrderImpl order = new OrderImpl();
		Date now = new Date();
		order.setLastModified(now);
		order.setCreationDate(now);
		order.setDelivery(delivery);
		order.setOrderStatus(OrderStatus.NEW);
		dbInstance.getCurrentEntityManager().persist(order);
		return order;
	}

	public OrderPart addOrderPart(Order order) {
		OrderPartImpl orderPart = new OrderPartImpl();
		orderPart.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(orderPart);
		order.getParts().add(orderPart);
		return orderPart;
	}

	public OrderLine addOrderLine(OrderPart part, Offer offer) {
		OrderLineImpl line = createOrderLine(offer);
		dbInstance.getCurrentEntityManager().persist(line);
		part.getOrderLines().add(line);
		return line;
	}

	private OrderLineImpl createOrderLine(Offer offer) {
		OrderLineImpl line = new OrderLineImpl();
		line.setCreationDate(new Date());
		line.setOffer(offer);
		line.setUnitPrice(offer.getPrice().clone());
		line.setTotal(line.getUnitPrice().clone());
		return line;
	}

	public Order save(Order order) {
		if(order.getKey() == null) {
			dbInstance.saveObject(order);
		} else {
			dbInstance.updateObject(order);
		}
		return order;
	}

	public Order save(Order order, OrderStatus status) {
		((OrderImpl)order).setOrderStatus(status);
		if(order.getKey() == null) {
			dbInstance.saveObject(order);
		} else {
			dbInstance.updateObject(order);
		}
		return order;
	}

	public Order saveOneClick(Identity delivery, OfferAccess link) {
		return saveOneClick(delivery, link, OrderStatus.PAYED);
	}

	public Order saveOneClick(Identity delivery, OfferAccess link, OrderStatus status) {
		OrderImpl order = createOrder(delivery);
		order.setOrderStatus(status);
		if(link.getOffer().getPrice().isEmpty()) {
			order.setCurrencyCode("CHF");
		} else {
			order.setCurrencyCode(link.getOffer().getPrice().getCurrencyCode());
		}
		OrderPartImpl part = new OrderPartImpl();
		part.setCreationDate(new Date());
		order.getParts().add(part);
		OrderLineImpl line = createOrderLine(link.getOffer());
		part.getOrderLines().add(line);
		order.recalculate();

		dbInstance.getCurrentEntityManager().persist(order);
		dbInstance.getCurrentEntityManager().persist(part);
		dbInstance.getCurrentEntityManager().persist(line);

		return order;
	}

	public List<Order> findOrdersByDelivery(IdentityRef delivery, OrderStatus... status) {
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
			List<String> statusStr = new ArrayList<>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
		}

		return query.getResultList();
	}


	/**
	 * The method is optimized for our settings: 1 order -> 1 order part -> 1 order line
	 *
	 * @param resource
	 * @param delivery
	 * @return
	 */
	public int countNativeOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr,
			Date from, Date to, OrderStatus... status) {

		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select count(o.order_id)")
		  .append(" from o_ac_order o")
		  .append(" inner join o_ac_order_part order_part on (o.order_id=order_part.fk_order_id and order_part.pos=0)")
		  .append(" inner join o_ac_order_line order_line on (order_part.order_part_id=order_line.fk_order_part_id and order_line.pos=0)")
		  .append(" inner join o_ac_offer offer on (order_line.fk_offer_id=offer.offer_id)");
		boolean where = false;
		if(resource != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" offer.fk_resource_id=:resourceKey ");
		}
		if(delivery != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" o.fk_delivery_id=:deliveryKey ");
		}
		if(status != null && status.length > 0 && status[0] != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.order_status in (:status)");
		}
		if(from != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.creationdate >=:from");
		}
		if(to != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.creationdate <=:to");
		}
		if(orderNr != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.order_id=:orderNr");
		}

		Query query = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString());
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		if(delivery != null) {
			query.setParameter("deliveryKey", delivery.getKey());
		}
		if(status != null && status.length > 0 && status[0] != null) {
			List<String> statusStr = new ArrayList<>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
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

		Object rawOrders = query.getSingleResult();
		return rawOrders instanceof Number ? ((Number)rawOrders).intValue() : 0;
	}
	
	public List<Order> findPendingOrders(OLATResource resource, IdentityRef delivery) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct o from ").append(OrderImpl.class.getName()).append(" o")
		  .append(" inner join o.parts orderPart")
		  .append(" inner join orderPart.lines orderLine")
		  .append(" inner join orderLine.offer offer")
		  .append(" inner join offer.resource rsrc")
		  .append(" where o.delivery.key=:deliveryKey and rsrc.key=:resourceKey")
		  .append(" and o.orderStatus=:status")
		  .append(" and exists (select trx.key from actransaction as trx")
		  .append("   where trx.order.key=o.key and trx.statusStr ").in(AccessTransactionStatus.PENDING)
		  .append(" ) and not exists (select successTrx.key from actransaction as successTrx")
		  .append("   where successTrx.order.key=o.key and successTrx.statusStr ")
		  	.in(AccessTransactionStatus.SUCCESS, AccessTransactionStatus.ERROR, AccessTransactionStatus.CANCELED)
		  .append(" )");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("deliveryKey", delivery.getKey())
				.setParameter("status", OrderStatus.PREPAYMENT.name())
				.getResultList();
	}

	/**
	 * The method is optimized for our settings: 1 order -> 1 order part -> 1 order line
	 *
	 * @param resource
	 * @param delivery
	 * @return
	 */
	public List<RawOrderItem> findNativeOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr,
			Date from, Date to, OrderStatus[] status, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers,  SortKey... orderBy) {

		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append("  o.order_id as order_id,")
		  .append("  o.total_currency_code as total_currency_code,")
		  .append("  o.total_amount as total_amount,")
		  .append("  o.creationdate as creationdate,")
		  .append("  o.order_status as o_status,")
		  .append("  o.fk_delivery_id as delivery_id,")
		  .append("  ").appendToArray("offer.resourcedisplayname").append(" as resDisplaynames,")
		  .append("  ").appendToArray("trx.trx_status").append(" as trxStatus,")
		  .append("  ").appendToArray("trx.fk_method_id").append(" as trxMethodIds,")
		  .append("  ").appendToArray("pspTrx.trx_status").append(" as pspTrxStatus,")
		  .append("  ").appendToArray("checkoutTrx.p_status").append(" as checkoutTrxStatus,")
		  .append("  ").appendToArray("checkoutTrx.p_paypal_order_status").append(" as checkoutTrxPaypalStatus");
		if(delivery == null) {
			sb.append("  ,delivery.id as delivery_ident_id")
			  .append("  ,delivery.name as delivery_ident_name")
			  .append("  ,delivery_user.user_id as delivery_user_id");
			if(userPropertyHandlers != null) {
				for(UserPropertyHandler handler:userPropertyHandlers) {
					sb.append(" ,delivery_user.").append(handler.getDatabaseColumnName()).append(" as ")
					  .append(handler.getName());
				}
			}
		}
		sb.append(" from o_ac_order o")
		  .append(" inner join o_ac_order_part order_part on (o.order_id=order_part.fk_order_id and order_part.pos=0)")
		  .append(" inner join o_ac_order_line order_line on (order_part.order_part_id=order_line.fk_order_part_id and order_line.pos=0)")
		  .append(" inner join o_ac_offer offer on (order_line.fk_offer_id=offer.offer_id)");
		if(delivery == null) {
			sb.append(" inner join o_bs_identity delivery on (delivery.id=o.fk_delivery_id)")
			  .append(" inner join o_user delivery_user on (delivery_user.fk_identity=delivery.id)");
		}
		sb.append(" left join o_ac_paypal_transaction pspTrx on (o.order_id = pspTrx.order_id)")
		  .append(" left join o_ac_checkout_transaction checkoutTrx on (o.order_id = checkoutTrx.p_order_id)")
		  .append(" left join o_ac_transaction trx on (o.order_id = trx.fk_order_id)");

		boolean where = false;
		if(resource != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" offer.fk_resource_id=:resourceKey ");
		}
		if(delivery != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" o.fk_delivery_id=:deliveryKey ");
		}
		if(status != null && status.length > 0 && status[0] != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.order_status in (:status)");
		}
		if(from != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.creationdate >=:from");
		}
		if(to != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.creationdate <=:to");
		}
		if(orderNr != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.order_id=:orderNr");
		}

		sb.append(" group by o.order_id");
		if(dbInstance.isOracle()) {
			sb.append(", o.total_currency_code, o.total_amount, o.creationdate, o.order_status, o.fk_delivery_id");
		}
		if(delivery == null) {
			sb.append(", delivery.id, delivery_user.user_id");
			if(dbInstance.isOracle()) {
				sb.append(", delivery.name");
				if(userPropertyHandlers != null) {
					for(UserPropertyHandler handler:userPropertyHandlers) {
						sb.append(", delivery_user.").append(handler.getDatabaseColumnName());
					}
				}
			}
		}

		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			sb.appendOrderBy(orderBy[0]);
		}

		Query query = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString());
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		if(delivery != null) {
			query.setParameter("deliveryKey", delivery.getKey());
		}
		if(status != null && status.length > 0 && status[0] != null) {
			List<String> statusStr = new ArrayList<>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
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

		if(maxResults > 0) {
			query.setFirstResult(firstResult).setMaxResults(maxResults);
		}

		int numOfProperties = userPropertyHandlers == null ? 0 : userPropertyHandlers.size();
		List<?> rawOrders = query.getResultList();
		List<RawOrderItem> items = new ArrayList<>(rawOrders.size());
		for(Object rawOrder:rawOrders) {
			Object[] order = (Object[])rawOrder;
			int pos = 0;

			Long orderKey = ((Number)order[pos++]).longValue();
			String totalCurrencyCode = (String)order[pos++];
			BigDecimal totalAmount = (BigDecimal)order[pos++];
			Date creationDate = (Date)order[pos++];
			String orderStatus = (String)order[pos++];
			Long deliveryKey = ((Number)order[pos++]).longValue();
			String resourceName = (String)order[pos++];
			String trxStatus = (String)order[pos++];
			String trxMethodIds = (String)order[pos++];
			String pspTrxStatus = (String)order[pos++];
			String checkoutTrxStatus = (String)order[pos++];
			String checkoutOrderTrxStatus = (String)order[pos++];

			String username = null;
			String[] userProperties = null;
			if(numOfProperties > 0) {
				pos++;//identityKey
				username = (String)order[pos++];
				pos++;//userKey
				userProperties = new String[numOfProperties];
				for(int i=0; i<numOfProperties; i++) {
					userProperties[i] = (String)order[pos++];
				}
			}

			RawOrderItem item = new RawOrderItem(orderKey, orderKey.toString(), totalCurrencyCode, totalAmount,
					creationDate, orderStatus, deliveryKey, resourceName,
					trxStatus, trxMethodIds, pspTrxStatus, checkoutTrxStatus, checkoutOrderTrxStatus,
					username, userProperties);
			items.add(item);
		}
		return items;
	}

	public List<Order> findOrdersByResource(OLATResource resource, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from ").append(OrderImpl.class.getName()).append(" o")
			.append(" inner join o.parts orderPart ")
			.append(" inner join orderPart.lines orderLine ")
			.append(" inner join orderLine.offer offer ")
			.append(" inner join offer.resource rsrc ")
			.append(" where rsrc.key=:resourceKey");
		if(status != null && status.length > 0) {
			sb.append(" and o.orderStatusStr in (:status)");
		}

		TypedQuery<Order> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("resourceKey", resource.getKey());
		if(status != null && status.length > 0) {
			List<String> statusStr = new ArrayList<>();
			for(OrderStatus s:status) {
				statusStr.add(s.name());
			}
			query.setParameter("status", statusStr);
		}

		return query.getResultList();
	}

	public Order loadOrderByKey(Long orderKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ").append(OrderImpl.class.getName()).append(" o")
		  .append(" left join fetch o.parts parts where o.key=:orderKey");

		List<Order> orders = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("orderKey", orderKey)
				.getResultList();
		if(orders.isEmpty()) return null;
		return orders.get(0);
	}

	public Order loadOrderByNr(String orderNr) {
		Long orderKey = Long.valueOf(orderNr);
		return loadOrderByKey(orderKey);
	}
}
