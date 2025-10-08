/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  23 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */

@Service("paypalManager")
public class PaypalManagerImpl  implements PaypalManager {

	@Autowired
	private DB dbInstance;
	
	@Override
	public PaypalTransaction loadTransactionByUUID(String uuid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("(trx.secureCancelUUID=:uuid or trx.secureSuccessUUID=:uuid)");
		
		TypedQuery<PaypalTransaction> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PaypalTransaction.class);
		if(StringHelper.containsNonWhitespace(uuid)) {
			query.setParameter("uuid", uuid);
		}
		List<PaypalTransaction> transactions = query.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	public PaypalTransaction loadTransactionByInvoiceId(String invoiceId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("trx.refNo=:invoiceId");
		
		List<PaypalTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalTransaction.class)
				.setParameter("invoiceId", invoiceId)
				.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	@Override
	public PaypalTransaction loadTransaction(Order order, OrderPart part) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("(trx.orderId=:orderId and trx.orderPartId=:orderPartId)");
		
		List<PaypalTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalTransaction.class)
				.setParameter("orderId", order.getKey())
				.setParameter("orderPartId", part.getKey())
				.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	@Override
	public List<PSPTransaction> loadTransactions(List<Order> orders) {
		if(orders == null || orders.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx where ");
		sb.append("trx.orderId in (:orderIds)");
		
		List<Long> orderIds = new ArrayList<>(orders.size());
		for(Order order:orders) {
			orderIds.add(order.getKey());
		}
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PSPTransaction.class)
				.setParameter("orderIds", orderIds)
				.getResultList();
	}

	@Override
	public List<PaypalTransaction> findTransactions(String transactionId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(PaypalTransaction.class.getName()).append(" as trx ");
		
		boolean where = false;
		if(StringHelper.containsNonWhitespace(transactionId)) {
			where = appendAnd(sb, where);
			sb.append(" (trx.transactionId=:transactionId or trx.senderTransactionId=:transactionId or trx.refNo=:transactionId) ");
		}
		sb.append(" order by trx.payResponseDate asc");
		
		TypedQuery<PaypalTransaction> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PaypalTransaction.class);
		if(StringHelper.containsNonWhitespace(transactionId)) {
			query.setParameter("transactionId", transactionId);
		}

		return query.getResultList();
	}
	
	private boolean appendAnd(StringBuilder sb, boolean where) {
		if(where) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}

	/*
	 * @return
	 */
	protected PaypalTransaction createAndPersistTransaction(Price amount, Order order, OrderPart part, AccessMethod method) {
		PaypalTransaction transaction = new PaypalTransaction();
		transaction.setCreationDate(new Date());
		transaction.setRefNo(order.getOrderNr());
		transaction.setSecureSuccessUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setSecureCancelUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setStatus(PaypalTransactionStatus.NEW);
		transaction.setOrderId(order.getKey());
		transaction.setOrderPartId(part.getKey());
		transaction.setMethodId(method.getKey());
		transaction.setSecurePrice(amount);
		dbInstance.saveObject(transaction);
		return transaction;
	}
}
