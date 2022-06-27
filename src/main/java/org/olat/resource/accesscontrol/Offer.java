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

package org.olat.resource.accesscontrol;

import java.util.Date;

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
public interface Offer extends OfferRef {
	
	public OLATResource getResource();
	
	public String getResourceTypeName();
	
	public Long getResourceId();
	
	public String getResourceDisplayName();
	
	public Date getValidFrom();

	public void setValidFrom(Date validFrom);

	public Date getValidTo();

	public void setValidTo(Date validTo);
	
	public boolean isValid();
	
	public Price getPrice();
	
	public void setPrice(Price price);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	/**
	 * @return true if the method allow it, and if configured for the offer.
	 */
	public boolean isAutoBooking();
	
	public void setAutoBooking(boolean autoBooking);
	
	public boolean isConfirmationEmail();

	public void setConfirmationEmail(boolean confirmationEmail);

	boolean isOpenAccess();

	public void setOpenAccess(boolean openAccess);

	public boolean isGuestAccess();

	public void setGuestAccess(boolean guestAccess);

	public boolean isCatalogPublish();

	public void setCatalogPublish(boolean catalogPublish);

	public boolean isCatalogWebPublish();

	public void setCatalogWebPublish(boolean catalogWebPublish);

}
