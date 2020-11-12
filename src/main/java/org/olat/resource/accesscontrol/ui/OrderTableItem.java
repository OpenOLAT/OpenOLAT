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

package org.olat.resource.accesscontrol.ui;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PSPTransaction;

/**
 * 
 * Description:<br>
 * Wrapper for the OrdersDataModel
 * 
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderTableItem {
	
	private static final OrderTableItemStatusComparator statusComparator = new OrderTableItemStatusComparator();
	
	private final Collection<AccessTransaction> transactions = new HashSet<>();
	private final Collection<PSPTransaction> pspTransactions = new HashSet<>();
	
	private final Long orderKey;
	private final String orderNr;
	private final Price total;
	private final Date creationDate;
	private final OrderStatus orderStatus;
	private String resourceDisplayname;
	private Long deliveryKey;
	
	private String username;
	private String[] userProperties;
	
	private Status status;
	private List<AccessMethod> methods;
	
	public OrderTableItem(Long orderKey, String orderNr, Price total, Date creationDate,
			OrderStatus orderStatus, Status status, Long deliveryKey,
			String username, String[] userProperties, List<AccessMethod> methods) {
		this.orderKey = orderKey;
		this.orderNr = orderNr;
		this.total = total;
		this.orderStatus = orderStatus;
		this.creationDate = creationDate;
		this.status = status;
		this.deliveryKey = deliveryKey;
		this.methods = methods;
		this.username = username;
		this.userProperties = userProperties;
	}
	
	public Long getOrderKey() {
		return orderKey;
	}
	
	public Long getDeliveryKey() {
		return deliveryKey;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getOrderNr() {
		return orderNr;
	}
	
	public OrderStatus getOrderStatus() {
		return orderStatus;	
	}
	
	public Price getTotal() {
		return total;
	}
	
	public String getResourceDisplayname() {
		return resourceDisplayname;
	}

	public void setResourceDisplayname(String resourceDisplayname) {
		this.resourceDisplayname = resourceDisplayname;
	}

	public String getUsername() {
		return username;
	}

	public String[] getUserProperties() {
		return userProperties;
	}

	public List<AccessMethod> getMethods() {
		return methods;
	}

	public Collection<AccessTransaction> getTransactions2() {
		return transactions;
	}
	
	public Collection<PSPTransaction> getPSPTransactions2() {
		return pspTransactions;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public int compareStatusTo(OrderTableItem item) {
		return statusComparator.compare(this, item);
	}
	
	public enum Status {
		ERROR,
		WARNING,
		OK,
		PENDING,
		CANCELED,
	}
	
	public static class OrderTableItemStatusComparator implements Comparator<OrderTableItem> {
		@Override
		public int compare(OrderTableItem o1, OrderTableItem o2) {
			Status s1 = o1.getStatus();
			Status s2 = o2.getStatus();
			
			if(s1 == null) return -1;
			if(s2 == null) return 1;
			return s1.ordinal() - s2.ordinal();
		}

	}
}