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

import java.util.List;

import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderPart;

/**
 * 
 * Description:<br>
 * this is the second part of the confirmation of an access to a resource.
 * The transaction log the attempt to access the resource. There is normally
 * one transaction for an order part. But if the user canceled, retry, pay 
 * severals time the same order part. All the transactions will be logged and
 * easily tracked for an admin (administrator of the resource)
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACTransactionManager {
	
	/**
	 * Create a transaction but not persist it on the database.
	 * @param order
	 * @param orderPart
	 * @param method
	 * @return The transaction
	 */
	public AccessTransaction createTransaction(Order order, OrderPart orderPart, AccessMethod method);
	
	/**
	 * Persist the transaction to the database.
	 * @param transaction
	 * @return The transaction
	 */
	public AccessTransaction save(AccessTransaction transaction);
	
	/**
	 * Update the transaction to the database.
	 * @param transaction
	 * @return The transaction
	 */
	public AccessTransaction update(AccessTransaction transaction, AccessTransactionStatus status);
	
	/**
	 * Load a transaction by its primary key.
	 * @param key
	 * @return The transaction
	 */
	public AccessTransaction loadTransactionByKey(Long key);
	
	/**
	 * Quick load of a list of transactions.
	 * @param orders
	 * @return List of transactions
	 */
	public List<AccessTransaction> loadTransactionsForOrders(List<Order> orders);

}
