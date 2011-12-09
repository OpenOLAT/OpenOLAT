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

package org.olat.resource.accesscontrol.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.BusinessGroupAccess;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;

/**
 * 
 * Description:<br>
 * Manage the access methods to the resources
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACMethodManager {
	
	/**
	 * Enable/disable a type of access method
	 * @param type
	 * @param enable
	 */
	public void enableMethod(Class<? extends AccessMethod> type, boolean enable);
	
	
	/**
	 * Get the list of access methods which a user /author can use
	 * @param identity
	 * @return List of access methods
	 */
	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles);
	
	/**
	 * Return the list of access methods of a specific type.
	 * @param type
	 * @return List of access methods
	 */
	public List<AccessMethod> getAvailableMethodsByType(Class<? extends AccessMethod> type);
	
	/**
	 * Return a list of links offer to method for the specified offer.
	 * @param offer
	 * @param valid
	 * @return List of link offer to method
	 */
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid);
	
	/**
	 * Return a list of links offer to access method for the specified offers.
	 * @param offer
	 * @param valid
	 * @return List of link offer to access method
	 */
	public List<OfferAccess> getOfferAccess(Collection<Offer> offers, boolean valid);
	
	public List<OfferAccess> getOfferAccessByResource(Collection<Long> resourceKeys, boolean valid, Date atDate);
	
	
	/**
	 * Return true if the resource has a method valid
	 * @param resource The resource
	 * @param atDate The date for the access (optional)
	 * @return
	 */
	public boolean isValidMethodAvailable(OLATResource resource, Date atDate);
	
	public List<BusinessGroupAccess> getAccessMethodForBusinessGroup(boolean valid, Date atDate);
	
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, boolean valid, Date atDate);
	
	/**
	 * Create a link between offer and access method. The link is not persisted
	 * on the database with this method.
	 * @param offer
	 * @param method
	 * @return
	 */
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method);
	
	/**
	 * The link is not really deleted on the database but set as invalid.
	 * @param link
	 */
	public void delete(OfferAccess link);
	
	/**
	 * Persist/update the link offer to access method.
	 * @param link
	 */
	public void save(OfferAccess link);

}
