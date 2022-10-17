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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * Implementation of the interface OrderPart
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acorderpart")
@Table(name="o_ac_order_part")
public class OrderPartImpl implements Persistable, OrderPart {

	private static final long serialVersionUID = -3572049955754185583L;
	
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
	@Column(name="order_part_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Embedded
    @AttributeOverrides( {
    	@AttributeOverride(name="amount", column = @Column(name="total_amount") ),
    	@AttributeOverride(name="currencyCode", column = @Column(name="total_currency_code") )
    })
	private PriceImpl total;
	@Embedded
    @AttributeOverrides( {
    	@AttributeOverride(name="amount", column = @Column(name="total_lines_amount") ),
    	@AttributeOverride(name="currencyCode", column = @Column(name="total_lines_currency_code") )
    })
	private PriceImpl totalOrderLines;
	
	@OneToMany(targetEntity=OrderLineImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_order_part_id")
	@OrderColumn(name="pos")
	private List<OrderLine> lines;
	
	public OrderPartImpl() {
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
	public Price getTotal() {
		return total;
	}

	public void setTotal(Price total) {
		this.total = (PriceImpl)total;
	}

	@Override
	public Price getTotalOrderLines() {
		return totalOrderLines;
	}

	public void setTotalOrderLines(Price totalOrderLines) {
		this.totalOrderLines = (PriceImpl)totalOrderLines;
	}

	@Override
	public List<OrderLine> getOrderLines() {
		if(lines == null) {
			lines = new ArrayList<>();
		}
		return lines;
	}

	public void setOrderLines(List<OrderLine> lines) {
		this.lines = lines;
	}
	
	public void recalculate(String currencyCode) {
		totalOrderLines = new PriceImpl(BigDecimal.ZERO, currencyCode);
		for(OrderLine orderLine : getOrderLines()) {
			totalOrderLines = totalOrderLines.add(orderLine.getTotal());
		}
		
		total = totalOrderLines.clone();
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
		if(obj instanceof OrderPartImpl) {
			OrderPartImpl orderPart = (OrderPartImpl)obj;
			return getKey() != null && getKey().equals(orderPart.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
