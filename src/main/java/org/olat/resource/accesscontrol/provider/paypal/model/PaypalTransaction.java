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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Target;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;
import org.olat.resource.accesscontrol.model.PriceImpl;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

/**
 * 
 * Description:<br>
 * Log the paypal transaction and persist them
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="paypaltransaction")
@Table(name="o_ac_paypal_transaction")
public class PaypalTransaction implements CreateInfo, Persistable, PSPTransaction {
	
	private static final long serialVersionUID = -8111089587194349398L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="transaction_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="pay_key", nullable=false, insertable=true, updatable=true)
	private String payKey;
	@Column(name="ref_no", nullable=false, insertable=true, updatable=true)
	private String refNo;
	@Column(name="trx_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="success_uuid", nullable=false, insertable=true, updatable=true)
	private String secureSuccessUUID;
	@Column(name="cancel_uuid", nullable=false, insertable=true, updatable=true)
	private String secureCancelUUID;
	
	@Embedded
	@Target(PriceImpl.class)
    @AttributeOverride(name="amount", column = @Column(name="amount_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="amount_currency_code"))
	private Price securePrice;

	@Column(name="order_id", nullable=false, insertable=true, updatable=true)
	private Long orderId;
	@Column(name="order_part_id", nullable=false, insertable=true, updatable=true)
	private Long orderPartId;
	@Column(name="method_id", nullable=false, insertable=true, updatable=true)
	private Long methodId;

	@Column(name="ack", nullable=true, insertable=true, updatable=true)
	private String ack;
	@Column(name="build", nullable=false, insertable=true, updatable=true)
	private String build;
	@Column(name="coorelation_id", nullable=false, insertable=true, updatable=true)
	private String coorelationId;
	@Column(name="pay_response_date", nullable=false, insertable=true, updatable=true)
	private Date payResponseDate;
	@Column(name="payment_exec_status", nullable=false, insertable=true, updatable=true)
	private String paymentExecStatus;
	
	//IPN
	@Column(name="ipn_transaction_id", nullable=false, insertable=true, updatable=true)
	private String transactionId;
	@Column(name="ipn_sender_transaction_id", nullable=false, insertable=true, updatable=true)
	private String senderTransactionId;
	@Column(name="ipn_sender_email", nullable=false, insertable=true, updatable=true)
	private String senderEmail;
	
	@Column(name="ipn_verify_sign", nullable=false, insertable=true, updatable=true)
	private String verifySign;
	@Column(name="ipn_sender_transaction_status", nullable=false, insertable=true, updatable=true)
	private String senderTransactionStatus;
	@Column(name="ipn_transaction_status", nullable=false, insertable=true, updatable=true)
	private String transactionStatus;
	@Column(name="ipn_pending_reason", nullable=false, insertable=true, updatable=true)
	private String pendingReason;
	
	public PaypalTransaction() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

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
		if(obj instanceof PaypalTransaction transaction) {
			return getKey() != null && getKey().equals(transaction.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
