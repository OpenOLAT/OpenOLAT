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
package org.olat.resource.accesscontrol.provider.paypal.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;

/**
 * 
 * Description:<br>
 * Log the paypal transaction and persist them
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalTransaction extends PersistentObject implements PSPTransaction {
	
	private static final long serialVersionUID = -8111089587194349398L;
	
	private String payKey;
	private String refNo;
	private String status;
	private String secureSuccessUUID;
	private String secureCancelUUID;
	private Price securePrice;
	
	private Long orderId;
	private Long orderPartId;
	private Long methodId;
	
	private String ack;
	private String build;
	private String coorelationId;
	private Date payResponseDate;
	private String paymentExecStatus;
	
	//IPN
	private String transactionId;
	private String senderTransactionId;
	private String senderEmail;
	
	
	private String verifySign;
	private String senderTransactionStatus;
	private String transactionStatus;
	private String pendingReason;

	
	public String getPayKey() {
		return payKey;
	}
	
	public void setPayKey(String payKey) {
		this.payKey = payKey;
	}
	
	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	
	public PaypalTransactionStatus getStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			if("CANCELLED".equals(status)) {
				return PaypalTransactionStatus.CANCELED;
			}
			return PaypalTransactionStatus.valueOf(status);
		}
		return null;
	}
	
	public void setStatus(PaypalTransactionStatus state) {
		if(state == null) {
			status = null;
		} else {
			status = state.name();
		}
	}

	public String getStatusStr() {
		return status;
	}

	public void setStatusStr(String status) {
		this.status = status;
	}
	
	@Override
	public PSPTransactionStatus getSimplifiedStatus() {
		boolean warning = false;
		boolean error = false;
		
		if(getStatus() == PaypalTransactionStatus.SUCCESS || getStatus() == PaypalTransactionStatus.NEW || getStatus() == PaypalTransactionStatus.CANCELED) {
			//
		}
		if(getStatus() == PaypalTransactionStatus.DENIED || getStatus() == PaypalTransactionStatus.ERROR) {
			error = true;
		}
		if(getStatus() == PaypalTransactionStatus.PENDING || getStatus() == PaypalTransactionStatus.PREPAYMENT) {
			warning = true;
		}
		
		//SUCCESS – The sender’s transaction has completed
		//PENDING – The transaction is awaiting further processing
		//CREATED – The payment request was received; funds will be transferred
		//PARTIALLY_REFUNDED– Transaction was partially refunded
		//DENIED – The transaction was rejected by the receiver
		//PROCESSING – The transaction is in progress
		//REVERSED – The payment was returned to the sender
		//null, Success, Pending -> authorize
		
		//receiver status
		if(transactionStatus == null || "PENDING".equalsIgnoreCase(transactionStatus)) {
			warning = true;
		}
		if("DENIED".equalsIgnoreCase(transactionStatus)) {
			error = true;
		}
		
		//sender status
		if(senderTransactionStatus == null || "PENDING".equalsIgnoreCase(senderTransactionStatus)) {
			warning = true;
		}
		if("DENIED".equalsIgnoreCase(senderTransactionStatus)) {
			error = true;
		}
		
		if(error) {
			return PSPTransactionStatus.ERROR;
		}
		if (warning) {
			return PSPTransactionStatus.WARNING;
		}
		return PSPTransactionStatus.OK;
	}

	@Override
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	
	@Override
	public Long getOrderPartId() {
		return orderPartId;
	}

	public void setOrderPartId(Long orderPartId) {
		this.orderPartId = orderPartId;
	}

	public Long getMethodId() {
		return methodId;
	}

	public void setMethodId(Long methodId) {
		this.methodId = methodId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getSenderTransactionId() {
		return senderTransactionId;
	}

	public void setSenderTransactionId(String senderTransactionId) {
		this.senderTransactionId = senderTransactionId;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getVerifySign() {
		return verifySign;
	}

	public void setVerifySign(String verifySign) {
		this.verifySign = verifySign;
	}

	public String getSenderTransactionStatus() {
		return senderTransactionStatus;
	}

	public void setSenderTransactionStatus(String senderTransactionStatus) {
		this.senderTransactionStatus = senderTransactionStatus;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getPendingReason() {
		return pendingReason;
	}

	public void setPendingReason(String pendingReason) {
		this.pendingReason = pendingReason;
	}

	public String getAck() {
		return ack;
	}

	public void setAck(String ack) {
		this.ack = ack;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getCoorelationId() {
		return coorelationId;
	}

	public void setCoorelationId(String coorelationId) {
		this.coorelationId = coorelationId;
	}

	public Date getPayResponseDate() {
		return payResponseDate;
	}

	public void setPayResponseDate(Date payResponseDate) {
		this.payResponseDate = payResponseDate;
	}

	public String getPaymentExecStatus() {
		return paymentExecStatus;
	}

	public void setPaymentExecStatus(String paymentExecStatus) {
		this.paymentExecStatus = paymentExecStatus;
	}

	public String getSecureSuccessUUID() {
		return secureSuccessUUID;
	}
	
	public void setSecureSuccessUUID(String secureSuccessUUID) {
		this.secureSuccessUUID = secureSuccessUUID;
	}
	
	public String getSecureCancelUUID() {
		return secureCancelUUID;
	}
	
	public void setSecureCancelUUID(String secureCancelUUID) {
		this.secureCancelUUID = secureCancelUUID;
	}
	
	public Price getSecurePrice() {
		return securePrice;
	}
	
	public void setSecurePrice(Price securePrice) {
		this.securePrice = securePrice;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PaypalTransaction[key=").append(getKey()).append("]")
			.append("[successUUID=").append(secureSuccessUUID).append("]")
			.append("[cancelUUID=").append(secureCancelUUID).append("]")
			.append("[price=").append(securePrice).append("]")
			//order
			.append("[orderId=").append(orderId).append("]")
			.append("[orderPartId=").append(orderPartId).append("]")
			.append("[methodId=").append(methodId).append("]")
			//pay request
			.append("[payKey=").append(payKey).append("]")
			.append("[ack=").append(ack).append("]")
			.append("[paymentExecStatus=").append(paymentExecStatus).append("]")
			.append("[payResponseDate=").append(payResponseDate).append("]")
			//ipn
			.append("[transactionId=").append(transactionId).append("]")
			.append("[transactionStatus=").append(transactionStatus).append("]")
			.append("[senderTransactionId=").append(senderTransactionId).append("]")
			.append("[senderTransactionStatus=").append(senderTransactionStatus).append("]")
			.append("[senderEmail=").append(senderEmail).append("]")
			.append("[verifySign=").append(verifySign).append("]");
		
		if(StringHelper.containsNonWhitespace(pendingReason)) {
			sb.append("[pendingReason=").append(pendingReason).append("]");
		}
		return sb.toString();
	}
	
	
	@Override
	public int hashCode() {
		return getKey() == null ? 28062 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PaypalTransaction) {
			PaypalTransaction transaction = (PaypalTransaction)obj;
			return equalsByPersistableKey(transaction);
		}
		return false;
	}
}
