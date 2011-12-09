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
import java.util.HashSet;

import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;

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
	
	private final Order order;
	private final Collection<AccessTransaction> transactions = new HashSet<AccessTransaction>();
	private final Collection<PSPTransaction> pspTransactions = new HashSet<PSPTransaction>();
	
	public OrderTableItem(Order order) {
		this.order = order;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public Collection<AccessTransaction> getTransactions() {
		return transactions;
	}
	
	public Collection<PSPTransaction> getPSPTransactions() {
		return pspTransactions;
	}
	
	public Status getStatus() {
		boolean warning = false;
		boolean error = false;
		boolean canceled = false;
		
		if(getOrder().getOrderStatus() == OrderStatus.CANCELED) {
			canceled = true;
		} else if(getOrder().getOrderStatus() == OrderStatus.ERROR) {
			error = true;
		} else if(getOrder().getOrderStatus() == OrderStatus.PREPAYMENT) {
			warning = true;
		}
		
		for(AccessTransaction transaction:getTransactions()) {
			if(transaction.getStatus() == AccessTransactionStatus.CANCELED) {
				canceled = true;
			} else if(transaction.getStatus() == AccessTransactionStatus.ERROR) {
				error = true;
			}
		}
		
		for(PSPTransaction transaction:getPSPTransactions()) {
			if(transaction.getSimplifiedStatus() == PSPTransactionStatus.ERROR) {
				error = true;
			} else if(transaction.getSimplifiedStatus() == PSPTransactionStatus.WARNING) {
				warning = true;
			}
		}
		
		if(error) {
			return Status.ERROR;
		} else if (warning) {
			return Status.WARNING;
		} else if(canceled) {
			return Status.CANCELED;
		} else {
			return Status.OK;
		}	
	}
	
	public int compareStatusTo(OrderTableItem item) {
		return statusComparator.compare(this, item);
	}
	
	public enum Status {
		ERROR,
		WARNING,
		OK,
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