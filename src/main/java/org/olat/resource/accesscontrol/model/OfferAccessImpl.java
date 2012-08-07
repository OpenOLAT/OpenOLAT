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

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OfferAccessImpl extends PersistentObject implements OfferAccess {

	private static final long serialVersionUID = 2538200023418491237L;
	private Offer offer;
	private AccessMethod method;
	private boolean valid;
	private Date validFrom;
	private Date validTo;

	public OfferAccessImpl() {
		//
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
			return equalsByPersistableKey(o);
		}
		return false;
	}
}