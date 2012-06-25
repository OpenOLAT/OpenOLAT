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
import java.util.Set;

import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.Offer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACOfferManager {
	
	/**
	 * Find the offers related to the specified resource
	 * @param resource
	 * @param valid Valid/invalid offer
	 * @param date Valid at the given date
	 * @return List of offers or empty list
	 */
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date date);
	
	/**
	 * Load an offer by primary key
	 * @param key The primary key of the offer
	 * @return An offer or null if not found
	 */
	public Offer loadOfferByKey(Long key);
	
	/**
	 * Filter the list of given resource primary keys and return only the ones
	 * which as a valid offer at this time.
	 * @param resourceKeys
	 * @return A set of resource primary keys
	 */
	public Set<Long> filterResourceWithOffer(Collection<Long> resourceKeys);
	
	/**
	 * Create an offer to access for a resource. The resource name is saved
	 * in the case of the deletion of the resource as fallback.
	 * @param resource
	 * @param resourceName
	 * @return
	 */
	public Offer createOffer(OLATResource resource, String resourceName);
	
	/**
	 * Set the offer as invalid, but not delete it really on the database. In
	 * the case of a payment, the offer msut survive for the orders.
	 * @param offer
	 */
	public void deleteOffer(Offer offer);
	
	/**
	 * Persist/update the offer on the database
	 * @param offer
	 */
	public void saveOffer(Offer offer);
}
