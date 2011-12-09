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

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderLine;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.OrderStatus;

/**
 * 
 * Description:<br>
 * Manage the orders. An order is a part of the confirmation of an
 * access which inlude the resource being access and an acnhor point
 * for the transaction (payment or other).
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACOrderManager {
	
	/**
	 * Create an order for the specified identity. The order
	 * is not persisted on the database.
	 * @param delivery
	 * @return The order
	 */
	public Order createOrder(Identity delivery);
	
	/**
	 * An order part is a subset of the order with the same access
	 * method and resolved to a single valid transaction (single is
	 * the normal case but it's not a constraint).
	 * @param order
	 * @return The order part
	 */
	public OrderPart addOrderPart(Order order);
	
	/**
	 * Add an order line
	 * @param part
	 * @param offer
	 * @return The order line
	 */
	public OrderLine addOrderLine(OrderPart part, Offer offer);
	
	/**
	 * Persist the order on the database
	 * @param order
	 * @return The order
	 */
	public Order save(Order order);
	
	/**
	 * Persist the order on the database
	 * @param order
	 * @return The order
	 */
	public Order save(Order order, OrderStatus status);
	
	/**
	 * Save a simple order for a single pair offer/access method and set the status to payed.
	 * @param delivery
	 * @param link
	 * @return
	 */
	public Order saveOneClick(Identity delivery, OfferAccess link);
	
	/**
	 * Save a simple order for a single pair offer/access method and set the status.
	 * @param delivery
	 * @param link
	 * @param status
	 * @return
	 */
	public Order saveOneClick(Identity delivery, OfferAccess link, OrderStatus status);
	
	/**
	 * Load an order by its primary key.
	 * @param orderKey
	 * @return The order
	 */
	public Order loadOrderByKey(Long orderKey);
	
	/**
	 * Load an order by its primary key.
	 * @param orderKey
	 * @return The order
	 */
	public Order loadOrderByNr(String orderNr);
	
	/**
	 * Load the orders of a specific identity.
	 * @param delivery The identity
	 * @return List of orders
	 */
	public List<Order> findOrders(OLATResource resource, Identity delivery, Long orderNr, Date from, Date to, OrderStatus... status);
	
	/**
	 * Load the orders of a specific identity.
	 * @param delivery The identity
	 * @return List of orders
	 */
	public List<Order> findOrdersByDelivery(Identity delivery, OrderStatus... status);
	
	/**
	 * Load the orders related to a specified resource.
	 * @param resource
	 * @return List of orders
	 */
	public List<Order> findOrdersByResource(OLATResource resource, OrderStatus... status);

}
