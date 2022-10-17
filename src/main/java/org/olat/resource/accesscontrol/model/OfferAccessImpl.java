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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import org.olat.resource.accesscontrol.OfferAccess;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */

@Entity(name="acofferaccess")
@Table(name="o_ac_offer_access")
public class OfferAccessImpl implements OfferAccess, Persistable {

	private static final long serialVersionUID = 2538200023418491237L;
	
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
	@Column(name="offer_method_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="is_valid", nullable=true, insertable=true, updatable=true)
	private boolean valid;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="validfrom", nullable=true, insertable=true, updatable=true)
	private Date validFrom;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="validto", nullable=true, insertable=true, updatable=true)
	private Date validTo;
	
	@ManyToOne(targetEntity=OfferImpl.class,fetch=FetchType.LAZY,optional=false,
			cascade={CascadeType.MERGE})
	@JoinColumn(name="fk_offer_id", nullable=false, insertable=true, updatable=false)
	private Offer offer;

	@ManyToOne(targetEntity=AbstractAccessMethod.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_method_id", nullable=false, insertable=true, updatable=false)
	private AccessMethod method;

	public OfferAccessImpl() {
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
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	@Override
	public AccessMethod getMethod() {
		return method;
	}

	public void setMethod(AccessMethod method) {
		this.method = method;
	}
	
	@Override
	public Date getValidFrom() {
		return validFrom;
	}
	
	@Override
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}
	
	@Override
	public Date getValidTo() {
		return validTo;
	}
	
	@Override
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OfferAccess[key=").append(getKey()).append("]")
			.append("[method=").append(method == null ? "null" : method.getClass().getSimpleName()).append("]");
		if(offer == null) {
			sb.append("[resource=null]");
		} else {
			sb.append("[resource=").append(offer.getResourceId()).append(":").append(offer.getResourceTypeName()).append(":").append(offer.getResourceDisplayName()).append("]");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 9191 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OfferAccessImpl) {
			OfferAccessImpl o = (OfferAccessImpl)obj;
			return getKey() != null && getKey().equals(o.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}