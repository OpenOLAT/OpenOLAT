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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.ACResourceInfo;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.ui.OrderTableItem;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACService {

	/**
	 *
	 * @param resource
	 * @param atDate
	 * @return
	 */
	public boolean isResourceAccessControled(OLATResource resource, Date atDate);

	/**
	 * The rule to access a business group:<br/>
	 * -No offer, access is free<br/>
	 * -Owners have always access to the resource<br/>
	 * -Tutors have access to the resource<br/>
	 * -Participants have access to the resource<br/>
	 * @param group
	 * @param forId
	 * @return
	 */
	public AccessResult isAccessible(BusinessGroup group, Identity forId, boolean allowNonInteractiveAccess);

	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, boolean allowNonInteractiveAccess);

	/**
	 *
	 * @param entry
	 * @param forId
	 * @param knowMember If you know that the forId is a member
	 * @param allowNonInteractiveAccess
	 * @return
	 */
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, Boolean knowMember, boolean allowNonInteractiveAccess);
	
	public boolean isAccessToResourcePending(OLATResource resource, IdentityRef identity);


	public Offer createOffer(OLATResource resource, String resourceName);

	public Offer save(Offer offer);

	public void deleteOffer(Offer offer);


	public List<OLATResourceAccess> filterRepositoryEntriesWithAC(List<RepositoryEntry> repoEntries);

	public List<OLATResourceAccess> filterResourceWithAC(List<OLATResource> resources);

	public Set<Long> filterResourcesWithAC(Collection<Long> resourceKeys);

	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate);

	/**
	 *
	 * @param resourceKeys This parameter is mandatory and must not be empty
	 * @param resourceType
	 * @param valid
	 * @param atDate
	 * @return
	 */
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, String resourceType, boolean valid, Date atDate);

	/**
	 * Get the list of access methods for a business group that are currently available
	 * @param group
	 * @param valid
	 * @param atDate
	 * @return The list of OfferAccess objects that represent available access methods
	 */
	public List<OfferAccess> getAccessMethodForBusinessGroup(BusinessGroup group, boolean valid, Date atDate);

	public List<OfferAccess> getValidOfferAccess(OLATResource resource, AccessMethod method);

	public List<AccessMethod> getAvailableMethodsByType(Class<? extends AccessMethod> type);

	public OfferAccess saveOfferAccess(OfferAccess link);

	public AccessResult accessResource(Identity identity, OfferAccess link, Object argument);

	public boolean allowAccesToResource(Identity identity, Offer offer);

	public boolean denyAccesToResource(Identity identity, Offer offer);


	/**
	 * Get the reservation form an identity on a resource
	 * @param identity
	 * @param resource
	 * @return
	 */
	public ResourceReservation getReservation(IdentityRef identity, OLATResource resource);

	/**
	 * Get the reservations pending a list of resources.
	 * @param resources
	 * @return
	 */
	public List<ResourceReservation> getReservations(List<OLATResource> resources);

	/**
	 * The list of pending reservations
	 * @param identity
	 * @return
	 */
	public List<ResourceReservation> getReservations(IdentityRef identity);

	/**
	 * Reserve a resource
	 * @param identity
	 * @param offer
	 * @return
	 */
	public boolean reserveAccessToResource(Identity identity, OfferAccess offer);

	/**
	 * A user must sometimes explicitly accept a reservation.
	 * @param identity
	 * @param reservation
	 */
	public void acceptReservationToResource(Identity identity, ResourceReservation reservation);

	/**
	 * Cancel a reservation
	 * @param identity
	 * @param reservation
	 */
	public void removeReservation(Identity ureqIdentity, Identity identity, ResourceReservation reservation);

	public int countReservations(OLATResource resource);

	public void cleanupReservations();

	/**
	 *
	 * @param resources
	 * @return
	 */
	public List<ACResourceInfo> getResourceInfos(List<OLATResource> resources);

	public String resolveDisplayName(OLATResource resource);

	public void enableMethod(Class<? extends AccessMethod> type, boolean enable);

	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles);

	public OfferAccess createOfferAccess(Offer offer, AccessMethod method);

	public void deletedLinkToMethod(OfferAccess link);

	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid);

	public Order loadOrderByKey(Long key);

	public List<Order> findOrders(Identity delivery, OrderStatus... status);

	public List<AccessTransaction> findAccessTransactions(Order order);

	public List<Order> findOrders(OLATResource resource, OrderStatus... status);

	public List<Order> findOrder(OLATResource resource, Identity identity, AccessMethod method);

	public int countOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from, Date to,
			OrderStatus[] statuss);

	public List<OrderTableItem> findOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from, Date to,
			OrderStatus[] status, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers, SortKey... orderBy);

}
