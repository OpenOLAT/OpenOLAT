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
package org.olat.resource.accesscontrol.provider.paypalcheckout.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutTransactionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PaypalCheckoutTransactionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PaypalCheckoutTransaction createTransaction(Price amount, Order order, OrderPart part, AccessMethod method) {
		PaypalCheckoutTransactionImpl transaction = new PaypalCheckoutTransactionImpl();
		transaction.setCreationDate(new Date());
		transaction.setLastModified(transaction.getCreationDate());
		transaction.setOrderNr(order.getOrderNr());
		transaction.setSecureSuccessUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setSecureCancelUUID(UUID.randomUUID().toString().replace("-", ""));
		transaction.setStatus(PaypalCheckoutStatus.NEW);
		transaction.setOrderId(order.getKey());
		transaction.setOrderPartId(part.getKey());
		transaction.setMethodId(method.getKey());
		transaction.setSecurePrice(amount);
		
		dbInstance.getCurrentEntityManager().persist(transaction);
		return transaction;
	}
	
	public PaypalCheckoutTransaction loadTransactionBySecureUuid(String uuid) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx")
		  .append(" where (trx.secureCancelUUID=:uuid or trx.secureSuccessUUID=:uuid)");
		
		List<PaypalCheckoutTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalCheckoutTransaction.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public PaypalCheckoutTransaction loadTransactionBy(Order order, OrderPart part) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx")
		  .append(" where trx.orderId=:orderId and trx.orderPartId=:orderPartId");
		
		List<PaypalCheckoutTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalCheckoutTransaction.class)
				.setParameter("orderId", order.getKey())
				.setParameter("orderPartId", part.getKey())
				.getResultList();
		return transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public PaypalCheckoutTransaction loadTransactionByPaypalOrderId(String paypalOrderId) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx")
		  .append(" where trx.paypalOrderId=:paypalOrderId");
		
		List<PaypalCheckoutTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalCheckoutTransaction.class)
				.setParameter("paypalOrderId", paypalOrderId)
				.getResultList();
		return transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public PaypalCheckoutTransaction loadTransactionByAuthorizationId(String paypalAuthorizationId) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx")
		  .append(" where trx.paypalAuthorizationId=:paypalAuthorizationId");
		
		List<PaypalCheckoutTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalCheckoutTransaction.class)
				.setParameter("paypalAuthorizationId", paypalAuthorizationId)
				.getResultList();
		return transactions.isEmpty() ? null : transactions.get(0);
	}
	
	public List<PSPTransaction> loadTransactionBy(List<Order> orders) {
		if(orders == null || orders.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx")
		  .append(" where trx.orderId in (:ordersId)");
		
		List<Long> ordersId = orders.stream()
				.map(Order::getKey)
				.collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PSPTransaction.class)
				.setParameter("ordersId", ordersId)
				.getResultList();
	}
	
	public List<PaypalCheckoutTransaction> searchTransactions(String id) {
		QueryBuilder sb = new QueryBuilder(128);
		sb.append("select trx from paypalcheckouttransaction as trx");  
		if(StringHelper.containsNonWhitespace(id)) {
			sb.and()
			  .append(" (trx.orderNr=:searchId or trx.paypalOrderId=:searchId or trx.paypalAuthorizationId=:searchId or trx.paypalCaptureId=:searchId or trx.paypalInvoiceId=:searchId)");
		}
		
		TypedQuery<PaypalCheckoutTransaction> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PaypalCheckoutTransaction.class);
		
		if(StringHelper.containsNonWhitespace(id)) {
			query.setParameter("searchId", id);
		}	
		return query.getResultList();
	}
	
	public PaypalCheckoutTransaction update(PaypalCheckoutTransaction transaction) {
		((PaypalCheckoutTransactionImpl)transaction).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(transaction);
	}
}
