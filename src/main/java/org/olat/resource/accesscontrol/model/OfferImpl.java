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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OfferImpl extends PersistentObject implements Offer, ModifiedInfo {
	private static final long serialVersionUID = 4734372430854498130L;

	private boolean valid = true;
	
	private Date lastModified;
	private Date validFrom;
	private Date validTo;
	
	private String token;
	private boolean autoBooking;
	
	private Long resourceId;
	private String resourceTypeName;
	private String resourceDisplayName;
	
	private Price price;
	private OLATResource resource;
	
	private String description;

	public OfferImpl() {
		//
	}

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

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

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
	
	public boolean isAutoBooking() {
		return autoBooking;
	}
	
	public void setAutoBooking(boolean autoBooking) {
		this.autoBooking = autoBooking;
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
		this.price = price;
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
		if(obj instanceof OfferImpl) {
			OfferImpl offer = (OfferImpl)obj;
			return equalsByPersistableKey(offer);
		}
		return false;
	}
}