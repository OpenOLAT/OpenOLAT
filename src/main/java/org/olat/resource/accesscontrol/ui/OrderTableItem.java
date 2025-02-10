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

import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;

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
	private final String label;
	private final Price total;
	private final Price cancellationFees;
	private final Date creationDate;
	private final OrderStatus orderStatus;
	private final String resourceDisplayname;
	private Long deliveryKey;
	
	private String username;
	private String[] userProperties;
	
	private Status status;
	private List<AccessMethod> methods;
	
	public OrderTableItem(Long orderKey, String orderNr,
			String label, String resourceDisplayname,
			Price total, Price cancellationFees,
			Date creationDate, OrderStatus orderStatus, Status status, Long deliveryKey,
			String username, String[] userProperties, List<AccessMethod> methods) {
		this.orderKey = orderKey;
		this.orderNr = orderNr;
		this.total = total;
		this.label = label;
		this.resourceDisplayname = resourceDisplayname;
		this.cancellationFees = cancellationFees;
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
	
	public String getLabel() {
		return label;
	}
	
	public OrderStatus getOrderStatus() {
		return orderStatus;	
	}
	
	public Price getTotal() {
		return total;
	}
	
	public Price getCancellationFees() {
		return cancellationFees;
	}
	
	public String getResourceDisplayname() {
		return resourceDisplayname;
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

	public Collection<AccessTransaction> getTransactions() {
		return transactions;
	}
	
	public Collection<PSPTransaction> getPSPTransactions() {
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
		/**
		 * Same as OK but for a process with some payment
		 */
		PAYED,
		/**
		 * Especially PayPal, user has payed, but we wait for the confirmation
		 */
		OK_PENDING,
		/**
		 * Especially for PayPal, user is currently in PayPal
		 */
		IN_PROCESS,
		PENDING,
		CANCELED;
		
		public static final Status getStatus(String orderStatus, String trxStatus, String pspTrxStatus, List<AccessMethod> orderMethods) {
			boolean warning = false;
			boolean error = false;
			boolean canceled = false;
			boolean pending = false;
			boolean okPending = false;
			boolean inProcess = false;

			if(OrderStatus.CANCELED.name().equals(orderStatus)) {
				canceled = true;
			} else if(OrderStatus.ERROR.name().equals(orderStatus)) {
				error = true;
			} else if(OrderStatus.PREPAYMENT.name().equals(orderStatus)) {
				if((trxStatus != null && trxStatus.contains(PaypalCheckoutStatus.PENDING.name()))
						|| (pspTrxStatus != null && pspTrxStatus.contains(PaypalCheckoutStatus.PENDING.name()))) {
					pending = true;
				} else if((trxStatus != null && trxStatus.contains("SUCCESS"))
						&& (pspTrxStatus != null && pspTrxStatus.contains("INPROCESS"))) {
					okPending = true;
				} else if(!StringHelper.containsNonWhitespace(trxStatus) 
						&& (pspTrxStatus != null && pspTrxStatus.contains("INPROCESS"))) {
					inProcess = true;
				} else {
					warning = true;
				}
			}
			
			if(trxStatus != null) {
				if(trxStatus.contains(AccessTransactionStatus.SUCCESS.name())) {
					//has high prio
				} else if(trxStatus.contains(AccessTransactionStatus.CANCELED.name())) {
					canceled = true;
				} else if(trxStatus.contains(AccessTransactionStatus.ERROR.name())) {
					error = true;
				}
			}

			if(pspTrxStatus != null) {
				if(pspTrxStatus.contains(PSPTransactionStatus.ERROR.name())) {
					error = true;
				} else if(pspTrxStatus.contains(PSPTransactionStatus.WARNING.name())) {
					warning = true;
				}
			}

			if(okPending) {
				return Status.OK_PENDING;
			}
			if(inProcess) {
				return Status.IN_PROCESS;
			}
			if(pending) {
				return Status.PENDING;
			}
			if(error) {
				return Status.ERROR;
			}
			if (warning) {
				return Status.WARNING;
			}
			if(canceled) {
				return Status.CANCELED;
			} 
			
			if(!orderMethods.isEmpty() && orderMethods.get(0).isPaymentMethod()) {
				return Status.PAYED;
			}
			return Status.OK;
		}
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