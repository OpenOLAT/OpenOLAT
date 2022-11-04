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
package org.olat.resource.accesscontrol.model;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.Price;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;


@Entity(name="actransaction")
@Table(name="o_ac_transaction")
public class AccessTransactionImpl implements Persistable, AccessTransaction {

	private static final long serialVersionUID = -5420630862571680567L;
	
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
	
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="amount_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="amount_currency_code"))
	private PriceImpl amount;

	@Column(name="trx_status", nullable=true, insertable=true, updatable=true)
	private String statusStr = AccessTransactionStatus.NEW.name();
	
	@ManyToOne(targetEntity=OrderImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_order_id", nullable=false, insertable=true, updatable=false)
	private Order order;
	@ManyToOne(targetEntity=OrderPartImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_order_part_id", nullable=false, insertable=true, updatable=false)
	private OrderPart orderPart;
	@ManyToOne(targetEntity=AbstractAccessMethod.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_method_id", nullable=false, insertable=true, updatable=false)
	private AccessMethod method;
	
	public AccessTransactionImpl(){
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

	@Override
	public AccessMethod getMethod() {
		return method;
	}
	
	public void setMethod(AccessMethod method) {
		this.method = method;
	}
	
	@Override
	public Price getAmount() {
		return amount;
	}

	public void setAmount(Price amount) {
		this.amount = (PriceImpl)amount;
	}

	@Override
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	@Override
	public OrderPart getOrderPart() {
		return orderPart;
	}
	
	public void setOrderPart(OrderPart orderPart) {
		this.orderPart = orderPart;
	}
	
	public AccessTransactionStatus getStatus() {
		if(StringHelper.containsNonWhitespace(statusStr)) {
			return AccessTransactionStatus.valueOf(statusStr);
		}
		return null;
	}

	public void setStatus(AccessTransactionStatus status) {
		if(status == null) {
			statusStr = null;
		} else {
			statusStr = status.name();
		}
	}

	public String getStatusStr() {
		return statusStr;
	}

	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 93791 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AccessTransactionImpl) {
			AccessTransactionImpl accessTransaction = (AccessTransactionImpl)obj;
			return key != null && key.equals(accessTransaction.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
