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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.NativeQueryBuilder;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferRef;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.OrderImpl;
import org.olat.resource.accesscontrol.model.OrderLineImpl;
import org.olat.resource.accesscontrol.model.OrderPartImpl;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.resource.accesscontrol.model.UserOrder;
import org.olat.resource.accesscontrol.model.UserResourceReservation;
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
		if(offer.getCancellingFee() != null) {
			line.setCancellationFee(offer.getCancellingFee().clone());
		}
		line.setCancellingFeeDeadlineDays(offer.getCancellingFeeDeadlineDays());
		return line;
	}

	public Order save(Order order) {
		if(order.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(order);
		} else {
			((OrderImpl)order).setLastModified(new Date());
			order = dbInstance.getCurrentEntityManager().merge(order);
		}
		return order;
	}

	public Order save(Order order, OrderStatus status) {
		((OrderImpl)order).setOrderStatus(status);
		return save(order);
	}

	public Order save(Order order, BillingAddress billingAddress) {
		((OrderImpl)order).setBillingAddress(billingAddress);
		return save(order);
	}

	public Order saveOneClick(Identity delivery, OfferAccess link) {
		return saveOneClick(delivery, link, OrderStatus.PAYED, null, null, null);
	}

	public Order saveOneClick(Identity delivery, OfferAccess link, OrderStatus status,
			BillingAddress billingAddress, String purchaseOrderNumber, String comment) {
		OrderImpl order = createOrder(delivery);
		order.setPurchaseOrderNumber(purchaseOrderNumber);
		order.setComment(comment);
		order.setOrderStatus(status);
		order.setBillingAddress(billingAddress);
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
	 * @param billingAddressProposal2 
	 *
	 * @return
	 */
	public List<RawOrderItem> findNativeOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from,
			Date to, OrderStatus[] status, List<Long> methodsKeys, List<Long> offerAccessKeys,
			boolean filterAdjustedAmount, boolean filterAddressProposal, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers, SortKey... orderBy) {

		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append("  o.order_id as order_id,")
		  .append("  o.total_currency_code as total_currency_code,")
		  .append("  o.total_amount as total_amount,")
		  .append("  o.cancellation_fee_amount as cancellation_fee_amount,")
		  .append("  min(billingAddress.id) as billing_address_key,")
		  .append("  min(billingAddress.fk_organisation) as billing_address_organisation_key,")
		  .append("  min(billingAddress.fk_identity) as billing_address_identity_key,")
		  .append("  billingAddress.a_identifier as billing_address_identifier,")
		  .append("  o.purchase_order_number as purchase_order_number,")
		  .append("  o.order_comment as comment,")
		  .append("  o.creationdate as creationdate,")
		  .append("  o.order_status as o_status,")
		  .append("  o.fk_delivery_id as delivery_id,")
		  .append("  o.total_lines_amount as total_lines_amount,")
		  .append("  o.cancellation_fee_lines_amount as cancellation_fee_lines_amount,")
		  .append("  ").appendToArray("offer.resourcedisplayname").append(" as resDisplaynames,")
		  .append("  ").appendToArray("offer.offer_label").append(" as labels,")
		  .append("  ").appendToArray("costCenter.a_name").append(" as cost_center_names,")
		  .append("  ").appendToArray("costCenter.a_account").append(" as cost_center_accounts,")
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
		sb.append(" left join o_ac_billing_address billingAddress on (o.fk_billing_address = billingAddress.id)");
		sb.append(" left join o_ac_cost_center costCenter on (offer.fk_cost_center = costCenter.id)");
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
		if (filterAdjustedAmount) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("(o.total_amount <> o.total_lines_amount or o.cancellation_fee_amount <> o.cancellation_fee_lines_amount)");
		}
		
		boolean withMethods = (methodsKeys != null && !methodsKeys.isEmpty());
		boolean withOfferAccess = (offerAccessKeys != null && !offerAccessKeys.isEmpty());
		if(withMethods || withOfferAccess) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("o.order_id in (select order_part2.fk_order_id from o_ac_order_part order_part2")
			  .append(" inner join o_ac_order_line order_line2 on (order_part2.order_part_id=order_line2.fk_order_part_id and order_line2.pos=0)")
			  .append(" inner join o_ac_offer offer2 on (order_line2.fk_offer_id=offer2.offer_id)")
			  .append(" inner join o_ac_offer_access offer_access2 on (offer_access2.fk_offer_id=offer2.offer_id)")
			  .append(" where ");
			if(withMethods) {
				sb.append(" offer_access2.fk_method_id in (:methodsKeys)");
			}			
			if(withOfferAccess) {
				if(withMethods) {
					sb.append(" and ");
				}
				sb.append(" offer_access2.offer_method_id in (:offerAccessKeys)");	
			}
			sb.append(")");
		}
		
		sb.append(" group by o.order_id, billingAddress.a_identifier");
		if(dbInstance.isOracle()) {
			sb.append(", o.total_currency_code, o.total_amount, o.cancellation_fee_amount, o.purchase_order_number ,o.creationdate, o.order_status, o.fk_delivery_id");
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
		
		if (filterAddressProposal) {
			sb.append(" having min(billingAddress.id) is not null and min(billingAddress.fk_organisation) is null and min(billingAddress.fk_identity) is null");
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
		
		if(withMethods) {
			query.setParameter("methodsKeys", methodsKeys);
		}			
		if(withOfferAccess) {
			query.setParameter("offerAccessKeys", offerAccessKeys);
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
			BigDecimal orderAmount = (BigDecimal)order[pos++];
			BigDecimal orderCancellationFee = (BigDecimal)order[pos++];
			Number billingAddressKey = ((Number)order[pos++]);
			Number billingAddressOrganisationKey = ((Number)order[pos++]);
			Number billingAddressIdentityKey = ((Number)order[pos++]);
			String billingAddressIdentifier = (String)order[pos++];
			String purchseOrderNumber = (String)order[pos++];
			String comment = (String)order[pos++];
			Date creationDate = PersistenceHelper.extractDate(order, pos++);
			String orderStatus = (String)order[pos++];
			Long deliveryKey = ((Number)order[pos++]).longValue();
			BigDecimal orderAmountLines = (BigDecimal)order[pos++];
			BigDecimal orderCancellationFeeLines = (BigDecimal)order[pos++];
			String resourceName = (String)order[pos++];
			String label = (String)order[pos++];
			String costCenterName = (String)order[pos++];
			String costCenterAccount = (String)order[pos++];
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
			
			boolean billingAddressProposal = billingAddressKey != null && billingAddressOrganisationKey == null && billingAddressIdentityKey == null;
			
			RawOrderItem item = new RawOrderItem(orderKey, orderKey.toString(), label, totalCurrencyCode,
					orderAmount, orderCancellationFee, orderAmountLines, orderCancellationFeeLines,
					billingAddressProposal, billingAddressIdentifier, purchseOrderNumber, comment, creationDate,
					orderStatus, deliveryKey, resourceName, costCenterName, costCenterAccount, trxStatus, trxMethodIds,
					pspTrxStatus, checkoutTrxStatus, checkoutOrderTrxStatus, username, userProperties);
			items.add(item);
		}
		return items;
	}

	public List<Order> findOrdersByResource(OLATResource resource, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from acorder o")
			.append(" inner join o.parts orderPart")
			.append(" inner join orderPart.lines orderLine")
			.append(" inner join orderLine.offer offer")
			.append(" inner join offer.resource rsrc")
			.append(" left join fetch o.delivery delivery")
			.append(" left join fetch delivery.user deliveryUser")
			.append(" where rsrc.key=:resourceKey");
		if(status != null && status.length > 0) {
			sb.append(" and o.orderStatus in (:status)");
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
	
	public List<Order> findOrdersBy(IdentityRef identity, OLATResource resource, OrderStatus... status) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from acorder o")
			.append(" inner join fetch o.parts orderPart")
			.append(" inner join fetch orderPart.lines orderLine")
			.append(" inner join fetch orderLine.offer offer")
			.append(" inner join offer.resource rsrc")
			.append(" where o.delivery.key=:deliveryKey and rsrc.key=:resourceKey");
		if(status != null && status.length > 0) {
			sb.append(" and o.orderStatus in (:status)");
		}

		TypedQuery<Order> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Order.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("deliveryKey", identity.getKey());
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
	
	public boolean hasOrder(OfferRef offer) {
		if (offer == null) {
			return false;
		}
		
		String query = """
				select orderline.key
				  from acorderline orderline
				 where orderline.offer.key = :offerKey
				""";
		
		return !dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.setParameter("offerKey", offer.getKey())
			.setMaxResults(1)
			.getResultList()
			.isEmpty();
	}
	
	public Map<Long, Long> getBillingAddressKeyToOrderCount(Collection<BillingAddress> billingAddresss) {
		String query = """
				select order.billingAddress.key
				     , count(*)
				  from acorder order
				 where order.billingAddress.key in :billingAddressKeys
				 group by order.billingAddress.key
				""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class)
				.setParameter("billingAddressKeys", billingAddresss.stream().map(BillingAddress::getKey).toList())
				.getResultList()
				.stream()
				.collect(Collectors.toMap(row -> (Long)row[0], row -> (Long)row[1]));
	}
	
	public List<ResourceReservation> getReservationsWithOrders(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		
		sb.append("select reservation from resourcereservation as reservation ");
		sb.append(" inner join acorder o on o.delivery.key = reservation.identity.key");
		sb.append(" inner join o.parts orderPart");
		sb.append(" inner join orderPart.lines orderLine");
		sb.append(" inner join orderLine.offer offer");
		sb.and().append("reservation.identity.key=:identityKey");
		sb.and().append("reservation.resource.key = offer.resource.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ResourceReservation.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<UserResourceReservation> getReservationsWithOrders(IdentityRef identity, String right, List<UserPropertyHandler> userPropertyHandlers) {
		QueryBuilder sb = new QueryBuilder();

		sb.append("select distinct reservation");
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			sb.append(", user.").append(userPropertyHandler.getName()).append(" as p_").append(userPropertyHandler.getName());
		}
		sb.append(" from resourcereservation as reservation");
		sb.append(" inner join bgroupmember membership on membership.identity.key = reservation.identity.key");
		sb.append(" inner join organisation org on org.group.key = membership.group.key");
		sb.append(" inner join organisation rootOrg on org.root.key = rootOrg.key");
		sb.append(" inner join bgroupmember managerMembership on membership.group.key = managerMembership.group.key");
		sb.append(" inner join organisationroleright rootOrgR2r on (rootOrgR2r.organisation.key = rootOrg.key and rootOrgR2r.role = managerMembership.role)");
		sb.append(" inner join acorder o on o.delivery.key = reservation.identity.key");
		sb.append(" inner join o.parts orderPart");
		sb.append(" inner join orderPart.lines orderLine");
		sb.append(" inner join orderLine.offer offer");
		sb.append(" inner join o.delivery.user user");
		sb.and().append("managerMembership.identity.key = :identityKey");
		sb.and().append("rootOrgR2r.right = :right");
		sb.and().append("reservation.resource.key = offer.resource.key");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("right", right)
				.getResultList().stream()
				.map(objects -> mapToUserResourceReservation(objects, userPropertyHandlers)).toList();
	}

	private UserResourceReservation mapToUserResourceReservation(Object[] objects, List<UserPropertyHandler> userPropertyHandlers) {
		UserResourceReservation userResourceReservation = new UserResourceReservation();
		int srcIdx = 0;
		if (objects[srcIdx++] instanceof ResourceReservation resourceReservation) {
			userResourceReservation.setResourceReservation(resourceReservation);
		}

		for (int dstIdx = 0; dstIdx < userPropertyHandlers.size() && srcIdx < objects.length; dstIdx++, srcIdx++) {
			if (objects[srcIdx] instanceof String sourceString) {
				userResourceReservation.setIdentityProp(dstIdx, sourceString);
			}
		}
		return userResourceReservation;
	}
	
	public List<UserOrder> getUserBookings(BookingOrdersSearchParams params, List<UserPropertyHandler> userPropertyHandlers) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct o, billingAddress, billingAddressOrg.identifier, billingAddressOrg.displayName,");
		sb.append(" offer.resourceDisplayName, offer.resourceTypeName, offerCostCenter.name, offerCostCenter.account ");
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			sb.append(", user.").append(userPropertyHandler.getName()).append(" as p_").append(userPropertyHandler.getName());
		}
		
		if (params.getIdentity() != null && params.getOrganisationRoles() != null && !params.getOrganisationRoles().isEmpty()) {
			sb.append(" from organisation org");
			sb.append(" inner join org.group orgGroup");
			sb.append(" inner join orgGroup.members mgmtMembership");
			sb.append(" inner join orgGroup.members userMembership");
		} else if (params.getIdentity() != null && params.getGroupRoles() != null && !params.getGroupRoles().isEmpty()) {
			sb.append(" from repositoryentry entry");
			sb.append(" inner join repoentrytogroup r2g on r2g.entry = entry");
			sb.append(" inner join bgroupmember groupRoleMembership on groupRoleMembership.group = r2g.group");
			sb.append(" inner join bgroupmember userMembership on userMembership.group = r2g.group");
		} else {
			throw new AssertException("Either organization or group roles must be set");
		}
		
		sb.append(" inner join acorder o on o.delivery = userMembership.identity");
		sb.append(" inner join fetch o.billingAddress billingAddress");
		sb.append(" left join billingAddress.organisation billingAddressOrg");
		sb.append(" inner join o.parts orderPart");
		sb.append(" inner join orderPart.lines orderLine");
		sb.append(" inner join orderLine.offer offer");
		sb.append(" left join offer.costCenter offerCostCenter");
		sb.append(" inner join o.delivery.user user");
		
		if (params.getIdentity() != null && params.getOrganisationRoles() != null && !params.getOrganisationRoles().isEmpty()) {
			sb.and().append("mgmtMembership.identity.key = :identityKey ");
			sb.and().append("mgmtMembership.role").in(params.getOrganisationRoles().toArray());
			sb.and().append("userMembership.role = 'user'");
		} else if (params.getIdentity() != null && params.getGroupRoles() != null && !params.getGroupRoles().isEmpty()) {
			sb.and().append("groupRoleMembership.identity.key = :identityKey ");
			sb.and().append("groupRoleMembership.role").in(params.getGroupRoles().toArray());
			sb.and().append("userMembership.role = 'user'");
		}
		
		if (params.getFromDate() != null) {
			sb.and().append("o.creationDate >= :fromDate");
		}
		if (params.getToDate() != null) {
			sb.and().append("o.creationDate <= :toDate");
		}
		
		TypedQuery<Object[]> typedQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if (params.getIdentity() != null && params.getOrganisationRoles() != null && !params.getOrganisationRoles().isEmpty()) {
			typedQuery.setParameter("identityKey", params.getIdentity().getKey());
		} else if (params.getIdentity() != null && params.getGroupRoles() != null && !params.getGroupRoles().isEmpty()) {
			typedQuery.setParameter("identityKey", params.getIdentity().getKey());
		}
		if (params.getFromDate() != null) {
			typedQuery.setParameter("fromDate", params.getFromDate());
		}
		if (params.getToDate() != null) {
			typedQuery.setParameter("toDate", params.getToDate());
		}
		return typedQuery.getResultList().stream().map(objects -> mapToUserBooking(objects, userPropertyHandlers)).toList();
	}

	private UserOrder mapToUserBooking(Object[] objects, List<UserPropertyHandler> userPropertyHandlers) {
		UserOrder userOrder = new UserOrder();
		int srcIdx = 0;
		if (objects[srcIdx++] instanceof Order order) {
			userOrder.setOrder(order);
		}
		if (objects[srcIdx++] instanceof BillingAddress billingAddress) {
			userOrder.setBillingAddress(billingAddress);
		}
		if (objects[srcIdx++] instanceof String billingAddressOrgId) {
			userOrder.setBillingAddressOrgId(billingAddressOrgId);
		}
		if (objects[srcIdx++] instanceof String billingAddressOrgName) {
			userOrder.setBillingAddressOrgName(billingAddressOrgName);
		}
		if (objects[srcIdx++] instanceof String offerName) {
			userOrder.setOfferName(offerName);
		}
		if (objects[srcIdx++] instanceof String offerType) {
			userOrder.setOfferType(offerType);
		}
		if (objects[srcIdx++] instanceof String offerCostCenter) {
			userOrder.setOfferCostCenter(offerCostCenter);
		}
		if (objects[srcIdx++] instanceof String offerAccount) {
			userOrder.setOfferAccount(offerAccount);
		}

		for (int dstIdx = 0; dstIdx < userPropertyHandlers.size() && srcIdx < objects.length; dstIdx++, srcIdx++) {
			if (objects[srcIdx] instanceof String sourceString) {
				userOrder.setIdentityProp(dstIdx, sourceString);
			}
		}
		return userOrder;
	}
}
