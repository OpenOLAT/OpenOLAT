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
import java.util.Date;

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acoffer")
@Table(name="o_ac_offer")
public class OfferImpl implements Persistable, Offer, ModifiedInfo {
	private static final long serialVersionUID = 4734372430854498130L;
	
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
	@Column(name="offer_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="is_valid", nullable=true, insertable=true, updatable=true)
	private boolean valid = true;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="validfrom", nullable=true, insertable=true, updatable=true)
	private Date validFrom;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="validto", nullable=true, insertable=true, updatable=true)
	private Date validTo;

	@Column(name="token", nullable=true, insertable=true, updatable=true)
	private String token;
	@Column(name="autobooking", nullable=true, insertable=true, updatable=true)
	private boolean autoBooking;
	@Column(name="confirmation_email", nullable=true, insertable=true, updatable=true)
	private boolean confirmationEmail;
	@Column(name="open_access", nullable=false, insertable=true, updatable=true)
	private boolean openAccess;
	@Column(name="guest_access", nullable=false, insertable=true, updatable=true)
	private boolean guestAccess;
	@Column(name="catalog_publish", nullable=false, insertable=true, updatable=true)
	private boolean catalogPublish;
	@Column(name="catalog_web_publish", nullable=false, insertable=true, updatable=true)
	private boolean catalogWebPublish;

	@Column(name="resourceid", nullable=true, insertable=true, updatable=true)
	private Long resourceId;
	@Column(name="resourcetypename", nullable=true, insertable=true, updatable=true)
	private String resourceTypeName;
	@Column(name="resourcedisplayname", nullable=true, insertable=true, updatable=true)
	private String resourceDisplayName;

	@Column(name="offer_desc", nullable=true, insertable=true, updatable=true)
	private String description;
	
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="price_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="price_currency_code"))
	private PriceImpl price;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_resource_id", nullable=false, insertable=true, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private OLATResource resource;
	

	public OfferImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
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
	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	@Override
	public String getResourceDisplayName() {
		return resourceDisplayName;
	}

	public void setResourceDisplayName(String resourceDisplayName) {
		this.resourceDisplayName = resourceDisplayName;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isAutoBooking() {
		return autoBooking;
	}

	@Override
	public void setAutoBooking(boolean autoBooking) {
		this.autoBooking = autoBooking;
	}

	@Override
	public boolean isConfirmationEmail() {
		return confirmationEmail;
	}

	@Override
	public void setConfirmationEmail(boolean confirmationEmail) {
		this.confirmationEmail = confirmationEmail;
	}

	@Override
	public boolean isOpenAccess() {
		return openAccess;
	}

	@Override
	public void setOpenAccess(boolean openAccess) {
		this.openAccess = openAccess;
	}

	@Override
	public boolean isGuestAccess() {
		return guestAccess;
	}

	@Override
	public void setGuestAccess(boolean guestAccess) {
		this.guestAccess = guestAccess;
	}

	@Override
	public boolean isCatalogPublish() {
		return catalogPublish;
	}

	@Override
	public void setCatalogPublish(boolean catalogPublish) {
		this.catalogPublish = catalogPublish;
	}

	@Override
	public boolean isCatalogWebPublish() {
		return catalogWebPublish;
	}

	@Override
	public void setCatalogWebPublish(boolean catalogWebPublish) {
		this.catalogWebPublish = catalogWebPublish;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	@Override
	public OLATResource getResource() {
		return resource;
	}
	
	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public Price getPrice() {
		if(price == null) {
			PriceImpl p = new PriceImpl();
			p.setAmount(BigDecimal.ZERO);
			p.setCurrencyCode("CHF");
			price = p;
		}
		return price;
	}

	@Override
	public void setPrice(Price price) {
		this.price = (PriceImpl)price;
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
		if(obj instanceof OfferImpl offer) {
			return getKey() != null && getKey().equals(offer.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}