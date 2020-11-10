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
package org.olat.resource.accesscontrol.provider.paypalcheckout.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Target;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;

/**
 * Log the data from paypal and link them to OpenOlat orders.
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="paypalcheckouttransaction")
@Table(name="o_ac_checkout_transaction")
public class PaypalCheckoutTransactionImpl implements Persistable, PaypalCheckoutTransaction {
	
	private static final long serialVersionUID = -8111089587194349398L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="p_success_uuid", nullable=false, insertable=true, updatable=false)
	private String secureSuccessUUID;
	@Column(name="p_cancel_uuid", nullable=false, insertable=true, updatable=false)
	private String secureCancelUUID;
	
	@Column(name="p_order_nr", nullable=false, insertable=true, updatable=false)
	private String orderNr;
	@Column(name="p_order_id", nullable=false, insertable=true, updatable=false)
	private Long orderId;
	@Column(name="p_order_part_id", nullable=false, insertable=true, updatable=false)
	private Long orderPartId;
	@Column(name="p_method_id", nullable=false, insertable=true, updatable=false)
	private Long methodId;
	
	@Embedded
	@Target(PriceImpl.class)
    @AttributeOverride(name="currencyCode", column = @Column(name="p_amount_currency_code") )
    @AttributeOverride(name="amount", column = @Column(name="p_amount_amount") )
	private Price securePrice;
	
	@Column(name="p_status", nullable=false, insertable=true, updatable=true)
	private String status;

	@Column(name="p_paypal_order_id", nullable=true, insertable=true, updatable=true)
	private String paypalOrderId;
	@Column(name="p_paypal_order_status", nullable=true, insertable=true, updatable=true)
	private String paypalOrderStatus;
	@Column(name="p_paypal_order_status_reason", nullable=true, insertable=true, updatable=true)
	private String paypalOrderStatusReason;
	
	@Column(name="p_paypal_authorization_id", nullable=true, insertable=true, updatable=true)
	private String paypalAuthorizationId;
	@Column(name="p_paypal_capture_id", nullable=true, insertable=true, updatable=true)
	private String paypalCaptureId;
	@Embedded
	@Target(PriceImpl.class)
    @AttributeOverride(name="currencyCode", column = @Column(name="p_capture_currency_code") )
    @AttributeOverride(name="amount", column = @Column(name="p_capture_amount") )
	private Price capturePrice;
	
	@Column(name="p_paypal_invoice_id", nullable=true, insertable=true, updatable=true)
	private String paypalInvoiceId;
	
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

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getSecureSuccessUUID() {
		return secureSuccessUUID;
	}

	public void setSecureSuccessUUID(String secureSuccessUUID) {
		this.secureSuccessUUID = secureSuccessUUID;
	}

	@Override
	public String getSecureCancelUUID() {
		return secureCancelUUID;
	}

	public void setSecureCancelUUID(String secureCancelUUID) {
		this.secureCancelUUID = secureCancelUUID;
	}


	public String getOrderNr() {
		return orderNr;
	}

	public void setOrderNr(String orderNr) {
		this.orderNr = orderNr;
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
	
	public Price getSecurePrice() {
		return securePrice;
	}
	
	public void setSecurePrice(Price securePrice) {
		this.securePrice = securePrice;
	}

	@Override
	public PSPTransactionStatus getSimplifiedStatus() {
		return null;
	}

	@Override
	public PaypalCheckoutStatus getStatus() {
		try {
			return PaypalCheckoutStatus.valueOf(status);
		} catch (Exception e) {
			return PaypalCheckoutStatus.ERROR;
		}
	}
	
	public void setStatus(PaypalCheckoutStatus status) {
		this.status = status.name();
	}

	@Override
	public String getPaypalOrderId() {
		return paypalOrderId;
	}

	@Override
	public void setPaypalOrderId(String paypalOrderId) {
		this.paypalOrderId = paypalOrderId;
	}

	@Override
	public String getPaypalOrderStatus() {
		return paypalOrderStatus;
	}

	@Override
	public void setPaypalOrderStatus(String paypalOrderStatus) {
		this.paypalOrderStatus = paypalOrderStatus;
	}

	@Override
	public String getPaypalOrderStatusReason() {
		return paypalOrderStatusReason;
	}

	@Override
	public void setPaypalOrderStatusReason(String reason) {
		this.paypalOrderStatusReason = reason;
	}

	@Override
	public String getPaypalAuthorizationId() {
		return paypalAuthorizationId;
	}

	@Override
	public void setPaypalAuthorizationId(String id) {
		this.paypalAuthorizationId = id;
	}

	@Override
	public String getPaypalCaptureId() {
		return paypalCaptureId;
	}

	@Override
	public void setPaypalCaptureId(String paypalCaptureId) {
		this.paypalCaptureId = paypalCaptureId;
	}

	@Override
	public Price getCapturePrice() {
		return capturePrice;
	}

	@Override
	public void setCapturePrice(Price capturePrice) {
		this.capturePrice = capturePrice;
	}

	@Override
	public String getPaypalInvoiceId() {
		return paypalInvoiceId;
	}

	@Override
	public void setPaypalInvoiceId(String paypalInvoiceId) {
		this.paypalInvoiceId = paypalInvoiceId;
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
		if(obj instanceof PaypalCheckoutTransactionImpl) {
			PaypalCheckoutTransactionImpl transaction = (PaypalCheckoutTransactionImpl)obj;
			return getKey().equals(transaction.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PaypalCheckoutTransaction[key=").append(getKey()).append("]")
			.append("[price=").append(securePrice).append("]")
			//order
			.append("[orderId=").append(orderId).append("]")
			.append("[orderPartId=").append(orderPartId).append("]")
			.append("[methodId=").append(methodId).append("]");
		if(StringHelper.containsNonWhitespace(paypalOrderId)) {
			sb.append("[paypalOrderId=").append(paypalOrderId).append("]");
		}
		if(StringHelper.containsNonWhitespace(paypalOrderStatus)) {
			sb.append("[paypalOrderStatus=").append(paypalOrderStatus).append("]");
		}
		if(StringHelper.containsNonWhitespace(paypalOrderStatusReason)) {
			sb.append("[paypalOrderStatusReason=").append(paypalOrderStatusReason).append("]");
		}
		if(StringHelper.containsNonWhitespace(paypalAuthorizationId)) {
			sb.append("[paypalAuthorizationId=").append(paypalAuthorizationId).append("]");
		}
		if(StringHelper.containsNonWhitespace(paypalCaptureId)) {
			sb.append("[paypalCaptureId=").append(paypalCaptureId).append("]");
		}
		if(StringHelper.containsNonWhitespace(secureSuccessUUID)) {
			sb.append("[successUUID=").append(secureSuccessUUID ).append("]");
		}
		if(StringHelper.containsNonWhitespace(secureCancelUUID)) {
			sb.append("[cancelUUID=").append(secureCancelUUID).append("]");
		}
		return sb.toString();
	}
}