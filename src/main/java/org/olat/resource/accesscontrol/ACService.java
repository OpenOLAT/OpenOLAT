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
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.model.ACResourceInfo;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.olat.resource.accesscontrol.ui.OrderTableItem;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ACService {
	
	public static final RepositoryEntryStatusEnum[] RESTATUS_ACTIVE_GUEST = new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.published };
	public static final RepositoryEntryStatusEnum[] RESTATUS_ACTIVE_OPEN = new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.published };
	public static final RepositoryEntryStatusEnum[] RESTATUS_ACTIVE_METHOD = new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.published };
	public static final RepositoryEntryStatusEnum[] RESTATUS_ACTIVE_METHOD_PERIOD = RepositoryEntryStatusEnum.preparationToPublished();
	public static final CurriculumElementStatus[] CESTATUS_ACTIVE_METHOD = new CurriculumElementStatus[] {
			CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed, CurriculumElementStatus.active };
	public static final CurriculumElementStatus[] CESTATUS_ACTIVE_METHOD_PERIOD = new CurriculumElementStatus[] {
			CurriculumElementStatus.preparation, CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed, CurriculumElementStatus.active };

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

	/**
	 *
	 * @param entry
	 * @param forId
	 * @param knowMember If you know that the forId is a member
	 * @param isGuest
	 * @param webPublish
	 * @param allowNonInteractiveAccess
	 * @return
	 */
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, Boolean knowMember, boolean isGuest,
			Boolean webPublish, boolean allowNonInteractiveAccess);

	public AccessResult isAccessible(CurriculumElement element, Identity forId, Boolean knowMember, boolean isGuest,
			Boolean webPublish, boolean allowNonInteractiveAccess);
	
	public boolean isGuestAccessible(RepositoryEntry entry, boolean filterStatus);
	
	/**
	 * Check if an order is pending.
	 * 
	 * @param resource The resource
	 * @param identity The user
	 * @return true if an order is still in the pending status
	 */
	public boolean isAccessToResourcePending(OLATResource resource, IdentityRef identity);

	public boolean isAccessRefusedByStatus(RepositoryEntry entry, IdentityRef identity);
	
	public boolean isAccessRefusedByStatus(CurriculumElement element, IdentityRef identity);
	
	/**
	 * A user can see and book the offers of this organisations.
	 * 
	 * @param identity
	 * @return
	 */
	public List<OrganisationRef> getOfferOrganisations(IdentityRef identity);
	
	/**
	 * An author can select these organisations when he edits an offer.
	 * 
	 * @param identity
	 * @return
	 */
	public List<Organisation> getSelectionOfferOrganisations(Identity identity);
	
	public Offer createOffer(OLATResource resource, String resourceName);

	public Offer save(Offer offer);

	public void deleteOffer(Offer offer);
	
	public void deleteOffers(OLATResource resource);
	
	/**
	 * Manages the relation of the offer to the organisations.
	 * Creates missing relations and deletes unneeded relations.
	 * Does not save the offer.
	 * 
	 * @param offer
	 * @param organisations
	 */
	public void updateOfferOrganisations(Offer offer, Collection<Organisation> organisations);
	
	public List<Organisation> getOfferOrganisations(OfferRef offer);
	
	public Map<Long, List<Organisation>> getOfferKeyToOrganisations(Collection<? extends OfferRef> offers);

	public List<OLATResourceAccess> filterRepositoryEntriesWithAC(List<RepositoryEntry> repoEntries);

	public List<OLATResourceAccess> filterResourceWithAC(List<OLATResource> resources, List<? extends OrganisationRef> offerOrganisations);
	
	public List<OLATResource> filterResourceWithOpenAccess(List<OLATResource> resources, Boolean webPublish, List<? extends OrganisationRef> offerOrganisations);

	public List<OLATResource> filterResourceWithGuestAccess(List<OLATResource> resources);

	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate, List<? extends OrganisationRef> offerOrganisations);
	
	public List<OfferAndAccessInfos> findOfferAndAccessByResource(OLATResource resource, boolean valid);

	
	public List<Offer> getOffers(RepositoryEntry entry, boolean valid, boolean filterByStatus, Date atDate,
			boolean dateMandatory, Boolean webPublish, List<? extends OrganisationRef> offerOrganisations);

	public List<Offer> getOffers(CurriculumElement element, boolean valid, boolean filterByStatus, Date atDate,
			boolean dateMandatory, Boolean webPublish, List<? extends OrganisationRef> offerOrganisations);
	
	/**
	 *
	 * @param resourceKeys This parameter is mandatory and must not be empty
	 * @param resourceType
	 * @param valid
	 * @param atDate
	 * @param offerOrganisations
	 * @return
	 */
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, String resourceType,
			boolean valid, Date atDate, List<? extends OrganisationRef> offerOrganisations);

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

	public AccessResult accessResource(Identity identity, OfferAccess link, OrderStatus orderStatus,
			Object argument, Identity doer);
	
	public AccessResult accessResource(Identity identity, OfferAccess link, OrderStatus orderStatus,
			Object argument, MailPackage mailing, Identity doer, String adminNote);

	public boolean allowAccesToResource(Identity identity, Offer offer, AccessMethod method,
			MailPackage mailing, Identity doer, String adminNote);

	public boolean denyAccesToResource(Identity identity, Offer offer);
	
	public boolean tryAutoBooking(Identity identity, RepositoryEntry entry, AccessResult acResult);
	
	public boolean tryAutoBooking(Identity identity, CurriculumElement element, AccessResult acResult);
	
	public Order createAndSaveOrder(Identity identity, OfferAccess link, OrderStatus orderStatus,
			BillingAddress billingAddress, String purchaseOrderNumber, String comment);
	
	public void cancelOrder(Order order, Identity doer, String adminNote, MailPackage mailing);
	
	/**
	 * Change to order status to pay and only the order status. Use wisely!
	 * 
	 * @param order The order
	 * @param status The new status
	 */
	public Order changeOrderStatus(Order order, OrderStatus status);

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
	 * @return A list of reservations
	 */
	public List<ResourceReservation> getReservations(List<OLATResource> resources);
	
	/**
	 * Get the reservations pending a list of resources, confirmation settings...
	 * @param The search parameters
	 * @return A list of reservations
	 */
	public List<ResourceReservation> getReservations(SearchReservationParameters searchParams);

	/**
	 * The list of pending reservations
	 * @param identity
	 * @return
	 */
	public List<ResourceReservation> getReservations(IdentityRef identity);

	/**
	 * Retrieves a list of resource reservations associated with the specified identity. 
	 * Only includes reservations that have corresponding orders.
	 *
	 * @param identity the identity used to filter and retrieve reservations
	 * @return a list of resource reservations containing corresponding orders
	 */
	public List<ResourceReservation> getReservationsWithOrders(IdentityRef identity);

	/**
	 * Reserve a resource
	 * @param identity
	 * @param offer
	 * @param method
	 * @return
	 */
	public boolean reserveAccessToResource(Identity identity, Offer offer, AccessMethod method,
			Date expirationDate, MailPackage mailing, Identity doer, String adminNote);

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
	public void removeReservation(Identity ureqIdentity, Identity identity, ResourceReservation reservation, String adminNote);

	public void cleanupReservations();

	/**
	 *
	 * @param resources
	 * @return
	 */
	public List<ACResourceInfo> getResourceInfos(List<OLATResource> resources);

	public String resolveDisplayName(OLATResource resource);

	public void enableMethod(Class<? extends AccessMethod> type, boolean enable);

	public List<AccessMethod> getAvailableMethods(OLATResource resource, Identity identity, Roles roles);
	
	public List<AccessMethod> getAvailableMethods();
	
	public boolean isMethodAvailable(String methodType);

	public OfferAccess createOfferAccess(Offer offer, AccessMethod method);
	
	/**
	 * Copy the offer and the settings of the link to a new resource.
	 * 
	 * @param link The offer access
	 * @param resource The resource
	 * @return A copy of the offer acces and the initial offer linked to the specified resource
	 */
	public OfferAccess copyOfferAccess(OfferAccess link, Date validFrom, Date validTo, OLATResource resource, String resourceName);

	public void deletedLinkToMethod(OfferAccess link);

	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid);
	
	public CostCenter createCostCenter();
	
	public CostCenter updateCostCenter(CostCenter costCenter);

	public void deleteCostCenter(CostCenter costCenter);
	
	public List<CostCenter> getCostCenters(CostCenterSearchParams searchParams);

	public Map<Long, Long> getCostCenterKeyToOfferCount(Collection<CostCenter> costCenters);

	public Offer addCostCenter(Offer ofer, CostCenter costCenter);
	
	public BillingAddress createBillingAddress(Organisation organisation, Identity identity);
	
	public BillingAddress updateBillingAddress(BillingAddress billingAddress);

	public void deleteBillingAddress(BillingAddress billingAddress);
	
	public List<BillingAddress> getBillingAddresses(BillingAddressSearchParams searchParams);

	public Map<Long, Long> getBillingAddressKeyToOrderCount(Collection<BillingAddress> billingAddresss);

	public Order addBillingAddress(Order order, BillingAddress billingAddress);
	
	public Order updateOrder(Order order);

	public Order loadOrderByKey(Long key);
	
	public List<Order> findOrders(Identity delivery, OrderStatus... status);
	
	public List<Order> findOrders(Identity delivery, OLATResource resource, OrderStatus... status);

	public List<AccessTransaction> findAccessTransactions(Order order);
	
	public List<AccessMethod> findAccessMethods(Order order);

	public List<Order> findOrders(OLATResource resource, OrderStatus... status);

	public int countOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from, Date to,
			OrderStatus[] statuss);

	public List<OrderTableItem> findOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from,
			Date to, OrderStatus[] status, List<Long> methodsKeys, List<Long> offerAccessKeys,
			boolean filterAdjustedAmount, boolean filterAddressProposal, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers, SortKey... orderBy);
	
	public boolean hasOrder(OfferRef offer);
	
	public Date getBeginDate(OLATResource resource);
	
	public Price getCancellationFee(OLATResource recource, Date resourceBeginDate, List<Order> orders);
	
	public ParticipantsAvailabilityNum getParticipantsAvailability(Long maxParticipants, Long numParticipants, boolean distinguishOverbooked);

}
