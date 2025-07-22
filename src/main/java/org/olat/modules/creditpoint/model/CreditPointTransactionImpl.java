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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="creditpointtransaction")
@Table(name="o_cp_transaction")
@NamedQuery(name="updateTransactionRemaingAmountLaunchDates", query="update creditpointtransaction set remainingAmount=:remainingAmount where key=:transactionKey")
public class CreditPointTransactionImpl implements Persistable, CreditPointTransaction {
	
	private static final long serialVersionUID = -1880483616168082508L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Enumerated(EnumType.STRING)
	@Column(name="c_type", nullable=false, insertable=true, updatable=false)
	private CreditPointTransactionType transactionType;
	
	@Column(name="c_amount", nullable=false, insertable=true, updatable=false)
	private BigDecimal amount;
	@Column(name="c_remaining_amount", nullable=true, insertable=true, updatable=true)
	private BigDecimal remainingAmount;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="c_expiration_date", nullable=true, insertable=true, updatable=false)
	private Date expirationDate;
	
	@Column(name="c_note", nullable=true, insertable=true, updatable=false)
	private String note;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_creator", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=CreditPointWalletImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_wallet", nullable=false, insertable=true, updatable=false)
	private CreditPointWallet wallet;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_transfert_origin", nullable=true, insertable=true, updatable=false)
	private OLATResource transfertOrigin;
	
	@Column(name="c_origin_run", nullable=true, insertable=true, updatable=false)
	private Integer originRun;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_transfert_destination", nullable=true, insertable=true, updatable=false)
	private OLATResource transfertDestination;
	
	@Column(name="c_destination_run", nullable=true, insertable=true, updatable=false)
	private Integer destinationRun;
	
	@ManyToOne(targetEntity=CreditPointTransactionImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_trx_reference", nullable=true, insertable=true, updatable=false)
	private CreditPointTransaction transactionReference;
	
	public CreditPointTransactionImpl() {
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
	public Long getOrderNumber() {
		Long k = getKey();
		return k == null ? null : Long.valueOf(k.longValue() + 120000);
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public CreditPointTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(CreditPointTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(BigDecimal remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public CreditPointWallet getWallet() {
		return wallet;
	}

	public void setWallet(CreditPointWallet wallet) {
		this.wallet = wallet;
	}
	
	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public OLATResource getTransfertOrigin() {
		return transfertOrigin;
	}

	public void setTransfertOrigin(OLATResource transfertOrigin) {
		this.transfertOrigin = transfertOrigin;
	}

	public Integer getOriginRun() {
		return originRun;
	}

	public void setOriginRun(Integer originRun) {
		this.originRun = originRun;
	}

	@Override
	public OLATResource getTransfertDestination() {
		return transfertDestination;
	}

	public void setTransfertDestination(OLATResource transfertDestination) {
		this.transfertDestination = transfertDestination;
	}

	public Integer getDestinationRun() {
		return destinationRun;
	}

	public void setDestinationRun(Integer destinationRun) {
		this.destinationRun = destinationRun;
	}

	@Override
	public CreditPointTransaction getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(CreditPointTransaction transactionReference) {
		this.transactionReference = transactionReference;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -28910546 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CreditPointTransactionImpl transaction) {
			return getKey() != null && getKey().equals(transaction.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
