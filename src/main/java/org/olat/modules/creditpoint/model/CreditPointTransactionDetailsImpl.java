/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;

/**
 * 
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="creditpointtransactiondetails")
@Table(name="o_cp_transaction_details")
public class CreditPointTransactionDetailsImpl implements Persistable, CreditPointTransactionDetails {
	
	private static final long serialVersionUID = -4904352578376688669L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="c_amount", nullable=false, insertable=true, updatable=false)
	private BigDecimal amount;
	
	@ManyToOne(targetEntity=CreditPointTransactionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_source", nullable=false, insertable=true, updatable=false)
	private CreditPointTransaction source;
	@ManyToOne(targetEntity=CreditPointTransactionImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_target", nullable=false, insertable=true, updatable=false)
	private CreditPointTransaction target;
	
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
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public CreditPointTransaction getSource() {
		return source;
	}

	public void setSource(CreditPointTransaction source) {
		this.source = source;
	}

	@Override
	public CreditPointTransaction getTarget() {
		return target;
	}

	public void setTarget(CreditPointTransaction target) {
		this.target = target;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -3410546 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CreditPointTransactionDetailsImpl details) {
			return getKey() != null && getKey().equals(details.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
