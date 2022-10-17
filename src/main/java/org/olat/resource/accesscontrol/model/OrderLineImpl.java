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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * An implementation of the OrderLine interface
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acorderline")
@Table(name="o_ac_order_line")
public class OrderLineImpl implements Persistable, OrderLine {

	private static final long serialVersionUID = -2630817206449967033L;
	
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
	@Column(name="order_item_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Embedded
    @AttributeOverrides( {
    	@AttributeOverride(name="amount", column = @Column(name="unit_price_amount") ),
    	@AttributeOverride(name="currencyCode", column = @Column(name="unit_price_currency_code") )
    })
	private PriceImpl unitPrice;
	@Embedded
    @AttributeOverrides( {
    	@AttributeOverride(name="amount", column = @Column(name="total_amount") ),
    	@AttributeOverride(name="currencyCode", column = @Column(name="total_currency_code") )
    })
	private PriceImpl totalPrice;
	
	@ManyToOne(targetEntity=OfferImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_offer_id", nullable=false, insertable=true, updatable=false)
	private Offer offer;
	
	public OrderLineImpl() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	@Override
	public Price getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Price unitPrice) {
		this.unitPrice = (PriceImpl)unitPrice;
	}

	@Override
	public Price getTotal() {
		return totalPrice;
	}

	public void setTotal(Price totalPrice) {
		this.totalPrice = (PriceImpl)totalPrice;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderLineImpl) {
			OrderLineImpl orderLine = (OrderLineImpl)obj;
			return getKey() != null && getKey().equals(orderLine.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
