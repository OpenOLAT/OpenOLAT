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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.AccessTransactionImpl;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for ACTransactionManagerImpl
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACTransactionManagerImpl implements ACTransactionManager {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public AccessTransaction createTransaction(Order order, OrderPart orderPart, AccessMethod method) {
		AccessTransactionImpl transaction = new AccessTransactionImpl();
		transaction.setOrder(order);
		transaction.setOrderPart(orderPart);
		transaction.setMethod(method);
		return transaction;
	}

	@Override
	public AccessTransaction save(AccessTransaction transaction) {
		if(transaction.getKey() == null) {
			dbInstance.saveObject(transaction);
		} else {
			dbInstance.updateObject(transaction);
		}
		return transaction;
	}
	
	@Override
	public AccessTransaction update(AccessTransaction transaction, AccessTransactionStatus status) {
		((AccessTransactionImpl)transaction).setStatus(status);
		return save(transaction);
	}
	
	@Override
	public AccessTransaction loadTransactionByKey(Long transactionKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(AccessTransactionImpl.class.getName()).append(" trx")
			.append(" where trx.key=:transactionKey");
		
		List<AccessTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AccessTransaction.class)
				.setParameter("transactionKey", transactionKey)
				.getResultList();
		if(transactions.isEmpty()) return null;
		return transactions.get(0);
	}
	
	@Override
	public List<AccessTransaction> loadTransactionsForOrders(List<Order> orders) {
		if(orders == null || orders.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select trx from ").append(AccessTransactionImpl.class.getName()).append(" trx")
			.append(" where trx.order in (:orders)");
		
		List<AccessTransaction> transactions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AccessTransaction.class)
				.setParameter("orders", orders)
				.getResultList();
		return transactions;
	}
}
