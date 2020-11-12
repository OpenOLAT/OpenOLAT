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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.EnrollState;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryMailing;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.ACResourceInfo;
import org.olat.resource.accesscontrol.model.ACResourceInfoImpl;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PSPTransactionStatus;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.ui.OrderTableItem;
import org.olat.resource.accesscontrol.ui.OrderTableItem.Status;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Description:<br>
 * The access control is not intend for security check.
 *
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("acService")
public class ACFrontendManager implements ACService, UserDataExportable {

	private static final Logger log = Tracing.createLoggerFor(ACFrontendManager.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AccessControlModule accessModule;
	@Autowired
	private ACOfferDAO accessManager;
	@Autowired
	private ACMethodDAO methodManager;
	@Autowired
	private ACOrderDAO orderManager;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private ACTransactionDAO transactionManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	/**
	 * The rule to access the repository entry:<br/>
	 * -No offer, access is free<br/>
	 * -Owners have always access to the resource<br/>
	 * -Tutors have access to the resource<br/>
	 * -Participants have access to the resource<br/>
	 * @param entry
	 * @param forId
	 * @param knowMember give it if already know as a member
	 * @return
	 */
	@Override
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, Boolean knowMember, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}

		boolean member;
		if(knowMember == null) {
			member = repositoryService.isMember(forId, entry);
		} else {
			member = knowMember.booleanValue();
		}
		if(member) {
			return new AccessResult(true);
		}

		Date now = dateNow();
		List<Offer> offers = accessManager.findOfferByResource(entry.getOlatResource(), true, now);
		if(offers.isEmpty()) {
			if(methodManager.isValidMethodAvailable(entry.getOlatResource(), null)) {
				//not open for the moment: no valid offer at this date but some methods are defined
				return new AccessResult(false);
			} else {
				return new AccessResult(true);
			}
		}
		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}

	@Override
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}

		boolean member = repositoryService.isMember(forId, entry);
		return isAccessible(entry, forId, Boolean.valueOf(member), allowNonInteractiveAccess);
	}

	/**
	 *
	 * @param resource
	 * @param atDate
	 * @return
	 */
	@Override
	public boolean isResourceAccessControled(OLATResource resource, Date atDate) {
		return methodManager.isValidMethodAvailable(resource, atDate);
	}

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
	@Override
	public AccessResult isAccessible(BusinessGroup group, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}

		List<String> roles = businessGroupRelationDao.getRoles(forId, group);
		if(roles.contains(GroupRoles.coach.name())) {
			return new AccessResult(true);
		}
		if(roles.contains(GroupRoles.participant.name())) {
			return new AccessResult(true);
		}

		Date now = dateNow();
		OLATResource resource = OLATResourceManager.getInstance().findResourceable(group);
		List<Offer> offers = accessManager.findOfferByResource(resource, true, now);
		if(offers.isEmpty()) {
			if(methodManager.isValidMethodAvailable(resource, null)) {
				//not open for the moment: no valid offer at this date but some methods are defined
				return new AccessResult(false);
			} else {
				return new AccessResult(true);
			}
		}

		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}

	protected AccessResult isAccessible(Identity identity, List<Offer> offers, boolean allowNonInteractiveAccess) {
		List<OfferAccess> offerAccess = methodManager.getOfferAccess(offers, true);
		if(offerAccess.isEmpty()) {
			return new AccessResult(false);
		}
		if(allowNonInteractiveAccess && offerAccess.size() == 1) {
			//is it a method without user interaction as the free access?
			OfferAccess link = offerAccess.get(0);
			if(!link.getMethod().isNeedUserInteraction()) {
				return accessResource(identity, link, null);
			}
		}
		return new AccessResult(false, offerAccess);
	}

	@Override
	public Offer createOffer(OLATResource resource, String resourceName) {
		return accessManager.createOffer(resource, resourceName);
	}

	@Override
	public void deleteOffer(Offer offer) {
		if(offer != null && offer.getKey() != null) {
			// only delete persisted offers
			accessManager.deleteOffer(offer);
		}
	}

	@Override
	public List<OLATResourceAccess> filterRepositoryEntriesWithAC(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> resourceTypes = new HashSet<>();
		List<Long> resourceKeys = new ArrayList<>();
		for(RepositoryEntry entry:repoEntries) {
			OLATResource ores = entry.getOlatResource();
			resourceKeys.add(ores.getKey());
			resourceTypes.add(ores.getResourceableTypeName());
		}

		String resourceType = null;
		if(resourceTypes.size() == 1) {
			resourceType = resourceTypes.iterator().next();
		}
		Date now = dateNow();
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, "BusinessGroup", true, now);
	}

	@Override
	public List<OLATResourceAccess> filterResourceWithAC(List<OLATResource> resources) {
		if(resources == null || resources.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> resourceTypes = new HashSet<>();
		List<Long> resourceKeys = new ArrayList<>();
		for(OLATResource resource:resources) {
			resourceKeys.add(resource.getKey());
			resourceTypes.add(resource.getResourceableTypeName());
		}

		String resourceType = null;
		if(resourceTypes.size() == 1) {
			resourceType = resourceTypes.iterator().next();
		}
		Date now = dateNow();
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, "BusinessGroup", true, now);
	}

	@Override
	public Set<Long> filterResourcesWithAC(Collection<Long> resourceKeys) {
		return accessManager.filterResourceWithOffer(resourceKeys);
	}

	@Override
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate) {
		return accessManager.findOfferByResource(resource, valid, atDate);
	}

	/**
	 *
	 * @param resourceKeys This parameter is mandatory and must not be empty!
	 */
	@Override
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, String resourceType, boolean valid, Date atDate) {
		if(resourceKeys == null || resourceKeys.isEmpty()) {
			return new ArrayList<>();
		}
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, null, valid, atDate);
	}

	/**
	 * Get the list of access methods for a business group that are currently available
	 * @param group
	 * @param valid
	 * @param atDate
	 * @return The list of OfferAccess objects that represent available access methods
	 */
	@Override
	public List<OfferAccess> getAccessMethodForBusinessGroup(BusinessGroup group, boolean valid, Date atDate) {
		List<Offer> offers = accessManager.findOfferByResource(group.getResource(), valid, atDate);
		if(offers.isEmpty()) {
			return Collections.<OfferAccess>emptyList();
		}
		List<OfferAccess> offerAccess = methodManager.getOfferAccess(offers, valid);
		if(offerAccess.isEmpty()) {
			return Collections.<OfferAccess>emptyList();
		}
		return offerAccess;
	}

	@Override
	public List<OfferAccess> getValidOfferAccess(OLATResource resource, AccessMethod method) {
		return methodManager.getValidOfferAccess(resource, method);
	}

	@Override
	public List<AccessMethod> getAvailableMethodsByType(Class<? extends AccessMethod> type) {
		return methodManager.getAvailableMethodsByType(type);
	}

	@Override
	public Offer save(Offer offer) {
		return accessManager.saveOffer(offer);
	}

	@Override
	public OfferAccess saveOfferAccess(OfferAccess link) {
		//offer access only cascade merge
		if(link.getOffer().getKey() == null) {
			accessManager.saveOffer(link.getOffer());
		}
		return methodManager.save(link);
	}

	@Override
	public AccessResult accessResource(Identity identity, OfferAccess link, Object argument) {
		if(link == null || link.getOffer() == null || link.getMethod() == null) {
			log.info(Tracing.M_AUDIT, "Access refused (no offer) to: {} for {}", link, identity);
			return new AccessResult(false);
		}

		AccessMethodHandler handler = accessModule.getAccessMethodHandler(link.getMethod().getType());
		if(handler == null) {
			log.info(Tracing.M_AUDIT, "Access refused (no handler method) to: {} for {}", link, identity);
			return new AccessResult(false);
		}

		if(handler.checkArgument(link, argument)) {
			if(allowAccesToResource(identity, link.getOffer())) {
				Order order = orderManager.saveOneClick(identity, link);
				AccessTransaction transaction = transactionManager.createTransaction(order, order.getParts().get(0), link.getMethod());
				transactionManager.save(transaction);
				dbInstance.commit();
				log.info(Tracing.M_AUDIT, "Access granted to: {} for {}", link, identity);
				return new AccessResult(true);
			} else {
				log.info(Tracing.M_AUDIT, "Access error to: {} for {}", link, identity);
			}
		} else {
			log.info(Tracing.M_AUDIT, "Access refused to: {} for {}", link, identity);
		}
		return new AccessResult(false);
	}

	@Override
	public void acceptReservationToResource(Identity identity, ResourceReservation reservation) {
		OLATResource resource = reservation.getResource();
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			//it's a reservation for a group
			businessGroupService.acceptPendingParticipation(identity, identity, resource);
		} else {
			repositoryManager.acceptPendingParticipation(identity, identity, resource, reservation);
		}
	}

	@Override
	public void removeReservation(Identity ureqIdentity, Identity identity, ResourceReservation reservation) {
		OLATResource resource = reservation.getResource();
		reservationDao.deleteReservation(reservation);
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			dbInstance.commit();//needed to have the right number of participants to calculate upgrade from waiting list
			businessGroupService.cancelPendingParticipation(ureqIdentity, reservation);
		}
	}

	@Override
	public ResourceReservation getReservation(Identity identity, OLATResource resource) {
		return reservationDao.loadReservation(identity, resource);
	}

	@Override
	public List<ResourceReservation> getReservations(List<OLATResource> resources) {
		return reservationDao.loadReservations(resources);
	}

	@Override
	public List<ResourceReservation> getReservations(Identity identity) {
		return reservationDao.loadReservations(identity);
	}

	@Override
	public int countReservations(OLATResource resource) {
		return reservationDao.countReservations(resource);
	}

	@Override
	public boolean reserveAccessToResource(final Identity identity, final OfferAccess offer) {
		final OLATResource resource = offer.getOffer().getResource();
		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			boolean reserved = false;
			final BusinessGroup group = businessGroupDao.loadForUpdate(resource.getResourceableId());
			if(group.getMaxParticipants() == null || group.getMaxParticipants().intValue() <= 0) {
				reserved = true;//don't need reservation
			} else {
				BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(resource);
				ResourceReservation reservation = reservationDao.loadReservation(identity, resource);
				if(reservation != null) {
					reserved = true;
				}

				int currentCount = businessGroupService.countMembers(reloadedGroup, GroupRoles.participant.name());
				int reservations = reservationDao.countReservations(resource);
				if(currentCount + reservations < reloadedGroup.getMaxParticipants().intValue()) {
					reservationDao.createReservation(identity, offer.getMethod().getType(), null, resource);
					reserved = true;
				}
			}
			return reserved;
		}
		return true;
	}

	@Override
	public void cleanupReservations() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date oneHourTimeout = cal.getTime();
		List<ResourceReservation> oldReservations = reservationDao.loadExpiredReservation(oneHourTimeout);
		for(ResourceReservation reservation:oldReservations) {
			log.info(Tracing.M_AUDIT, "Remove reservation: {}", reservation);
			reservationDao.deleteReservation(reservation);
		}
	}

	@Override
	public boolean allowAccesToResource(final Identity identity, final Offer offer) {
		//check if offer is ok: key is stupid but further check as date, validity...
		if(offer.getKey() == null) {
			return false;
		}

		//check the resource
		OLATResource resource = offer.getResource();
		if(resource == null || resource.getKey() == null || resource.getResourceableId() == null || resource.getResourceableTypeName() == null) {
			return false;
		}

		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				MailPackage mailing = new MailPackage(offer.isConfirmationEmail());
				EnrollState result = businessGroupService.enroll(identity, null, identity, group, mailing);
				return !result.isFailed();
			}
		} else {
			RepositoryEntry entry = repositoryEntryDao.loadByResource(resource);
			if(entry != null) {
				if(!repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
					repositoryEntryRelationDao.addRole(identity, entry, GroupRoles.participant.name());
					if(offer.isConfirmationEmail()) {
						MailPackage mailing = new MailPackage(offer.isConfirmationEmail());
						RepositoryMailing.sendEmail(identity, identity, entry, RepositoryMailing.Type.addParticipantItself, mailing);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean denyAccesToResource(Identity identity, Offer offer) {
		//check if offer is ok: key is stupid but further check as date, validity...
		if(offer.getKey() == null) {
			return false;
		}

		//check the resource
		OLATResource resource = offer.getResource();
		if(resource == null || resource.getKey() == null || resource.getResourceableId() == null || resource.getResourceableTypeName() == null) {
			return false;
		}

		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				if(businessGroupService.hasRoles(identity, group, GroupRoles.participant.name())) {
					businessGroupRelationDao.removeRole(identity, group, GroupRoles.participant.name());
				}
				return true;
			}
		} else {
			RepositoryEntryRef entry = repositoryEntryDao.loadByResource(resource);
			if(entry != null) {
				if(repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
					repositoryEntryRelationDao.removeRole(identity, entry, GroupRoles.participant.name());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String resolveDisplayName(OLATResource resource) {
		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				return group.getName();
			}
		} else {
			RepositoryEntry entry = repositoryEntryDao.loadByResource(resource);
			if(entry != null) {
				return entry.getDisplayname();
			}
		}
		return null;
	}

	@Override
	public List<ACResourceInfo> getResourceInfos(List<OLATResource> resources) {
		if(resources == null || resources.isEmpty()) {
			return Collections.emptyList();
		}

		List<OLATResource> groupResources = new ArrayList<>(resources.size());
		List<OLATResource> repositoryResources = new ArrayList<>(resources.size());
		for(OLATResource resource:resources) {
			String resourceType = resource.getResourceableTypeName();
			if("BusinessGroup".equals(resourceType)) {
				groupResources.add(resource);
			} else {
				repositoryResources.add(resource);
			}
		}

		List<ACResourceInfo> resourceInfos = new ArrayList<>(resources.size());
		if(!groupResources.isEmpty()) {
			List<Long> groupKeys = new ArrayList<>(groupResources.size());
			Map<Long, OLATResource> groupMapKeys = new HashMap<>(groupResources.size() * 2 + 1);
			for(OLATResource groupResource:groupResources) {
				groupKeys.add(groupResource.getResourceableId());
			}
			List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
			for(BusinessGroup group:groups) {
				ACResourceInfoImpl info = new ACResourceInfoImpl();
				info.setResource(groupMapKeys.get(group.getKey()));
				info.setName(group.getName());
				info.setDescription(group.getDescription());
				info.setResource(group.getResource());
				resourceInfos.add(info);
			}
		}
		if(!repositoryResources.isEmpty()) {
			List<RepositoryEntry> repoEntries = repositoryEntryDao.loadByResources(repositoryResources);
			for(RepositoryEntry repoEntry:repoEntries) {
				ACResourceInfoImpl info = new ACResourceInfoImpl();
				info.setName(repoEntry.getDisplayname());
				info.setDescription(repoEntry.getDescription());
				info.setResource(repoEntry.getOlatResource());
				resourceInfos.add(info);
			}
		}
		return resourceInfos;
	}

	@Override
	public void enableMethod(Class<? extends AccessMethod> type, boolean enable) {
		methodManager.enableMethod(type, enable);
	}

	@Override
	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles) {
		List<AccessMethod> methods = methodManager.getAvailableMethods();
		
		List<AccessMethod> allowedMethods = new ArrayList<>();
		for(AccessMethod method:methods) {
			AccessMethodHandler handler = accessModule.getAccessMethodHandler(method.getType());
			AccessMethodSecurityCallback secCallback = handler.getSecurityCallback(identity, roles);
			if(secCallback.canUse()) {
				allowedMethods.add(method);
			}
		}
		
		return methods;
	}

	@Override
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		return methodManager.createOfferAccess(offer, method);
	}

	@Override
	public void deletedLinkToMethod(OfferAccess link) {
		methodManager.delete(link);
	}

	@Override
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		return methodManager.getOfferAccess(offer, valid);
	}

	@Override
	public List<Order> findOrders(Identity delivery, OrderStatus... status) {
		return orderManager.findOrdersByDelivery(delivery, status);
	}

	@Override
	public List<Order> findOrder(OLATResource resource, Identity identity, AccessMethod method) {
		return orderManager.findOrdersByResource(resource, identity, method);
	}

	@Override
	public List<AccessTransaction> findAccessTransactions(Order order) {
		return transactionManager.loadTransactionsForOrder(order);
	}

	@Override
	public Order loadOrderByKey(Long key) {
		return orderManager.loadOrderByKey(key);
	}

	@Override
	public List<Order> findOrders(OLATResource resource, OrderStatus... status) {
		return orderManager.findOrdersByResource(resource, status);
	}

	@Override
	public int countOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from, Date to, OrderStatus[] status) {
		return orderManager.countNativeOrderItems(resource, delivery, orderNr, from, to, status);
	}

	@Override
	public List<OrderTableItem> findOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr,
			Date from, Date to, OrderStatus[] status, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers, SortKey... orderBy) {
		List<AccessMethod> methods = methodManager.getAllMethods();
		Map<String,AccessMethod> methodMap = new HashMap<>();
		for(AccessMethod method:methods) {
			methodMap.put(method.getKey().toString(), method);
		}

		List<RawOrderItem> rawOrders = orderManager.findNativeOrderItems(resource, delivery, orderNr, from, to, status,
				firstResult, maxResults, userPropertyHandlers, orderBy);
		List<OrderTableItem> items = new ArrayList<>(rawOrders.size());
		for(RawOrderItem rawOrder:rawOrders) {
			String orderStatusStr = rawOrder.getOrderStatus();
			OrderStatus orderStatus = OrderStatus.valueOf(orderStatusStr);
			
			String pspTrxStatus = null;
			if(StringHelper.containsNonWhitespace(rawOrder.getPspTrxStatus())) {
				pspTrxStatus = rawOrder.getPspTrxStatus();
			} else if(StringHelper.containsNonWhitespace(rawOrder.getCheckoutTrxStatus())) {
				pspTrxStatus = rawOrder.getCheckoutTrxStatus();
			}
			
			Status finalStatus = getStatus(orderStatusStr,  rawOrder.getTrxStatus(), pspTrxStatus);
			String methodIds = rawOrder.getTrxMethodIds();
			List<AccessMethod> orderMethods = new ArrayList<>(2);
			if(StringHelper.containsNonWhitespace(methodIds)) {
				String[] methodIdArr = methodIds.split(",");
				for(String methodId:methodIdArr) {
					if(methodMap.containsKey(methodId)) {
						orderMethods.add(methodMap.get(methodId));
					}
				}
			}

			OrderTableItem item = new OrderTableItem(rawOrder.getOrderKey(), rawOrder.getOrderNr(),
					rawOrder.getTotal(), rawOrder.getCreationDate(), orderStatus, finalStatus,
					rawOrder.getDeliveryKey(), rawOrder.getUsername(), rawOrder.getUserProperties(), orderMethods);
			item.setResourceDisplayname(rawOrder.getResourceName());

			items.add(item);
		}

		return items;
	}

	private Status getStatus(String orderStatus, String trxStatus, String pspTrxStatus) {
		boolean warning = false;
		boolean error = false;
		boolean canceled = false;
		boolean pending = false;

		if(OrderStatus.CANCELED.name().equals(orderStatus)) {
			canceled = true;
		} else if(OrderStatus.ERROR.name().equals(orderStatus)) {
			error = true;
		} else if(OrderStatus.PREPAYMENT.name().equals(orderStatus)) {
			if((trxStatus != null && trxStatus.contains(PaypalCheckoutStatus.PENDING.name()))
					|| (pspTrxStatus != null && pspTrxStatus.contains(PaypalCheckoutStatus.PENDING.name()))) {
				pending = true;
			} else {
				warning = true;
			}
		}

		if(StringHelper.containsNonWhitespace(trxStatus)) {
			if(trxStatus.contains(AccessTransactionStatus.SUCCESS.name())) {
				//has high prio
			} else if(trxStatus.contains(AccessTransactionStatus.CANCELED.name())) {
				canceled = true;
			} else if(trxStatus.contains(AccessTransactionStatus.ERROR.name())) {
				error = true;
			}
		}

		if(StringHelper.containsNonWhitespace(pspTrxStatus)) {
			if(pspTrxStatus.contains(PSPTransactionStatus.ERROR.name())) {
				error = true;
			} else if(pspTrxStatus.contains(PSPTransactionStatus.WARNING.name())) {
				warning = true;
			}
		}

		if(pending) {
			return Status.PENDING;
		} else if(error) {
			return Status.ERROR;
		} else if (warning) {
			return Status.WARNING;
		} else if(canceled) {
			return Status.CANCELED;
		} 
		return Status.OK;
	}

	/**
	 * @return The current date without time
	 */
	private Date dateNow() {
		return CalendarUtils.removeTime(new Date());
	}

	@Override
	public String getExporterID() {
		return "bookings";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File noteArchive = new File(archiveDirectory, "Bookings.xlsx");
		try(OutputStream out = new FileOutputStream(noteArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row header = sheet.newRow();
			header.addCell(0, "Status");
			header.addCell(1, "Booking number");
			header.addCell(2, "Date");
			header.addCell(3, "Content");
			header.addCell(4, "Method");
			header.addCell(5, "Total");
			
			List<OrderTableItem> orders = findOrderItems(null, identity, null, null, null, null, 0, -1, null);
			for(OrderTableItem order:orders) {
				exportNoteData(order, sheet, workbook, locale);
			}
			
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile("Bookings.xlsx");
	}

	private void exportNoteData(OrderTableItem order, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook, Locale locale) {
		int col = 0;
		Row row = sheet.newRow();
		Collection<AccessMethod> methods = order.getMethods();
		
		if(order.getOrderStatus() != null) {
			row.addCell(col++, order.getOrderStatus().name());
		}
		row.addCell(col++, order.getOrderNr());
		row.addCell(col++, order.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(col++, order.getResourceDisplayname());
		StringBuilder methodSb = new StringBuilder();
		for(AccessMethod method:methods) {
			AccessMethodHandler handler = accessModule.getAccessMethodHandler(method.getType());
			if(handler != null) {
				if(methodSb.length() > 0) methodSb.append(", ");
				methodSb.append(handler.getMethodName(locale));
			}
		}
		row.addCell(col++, methodSb.toString());
		for(AccessMethod method:methods) {
			if(method.isPaymentMethod()) {
				row.addCell(col++, PriceFormat.fullFormat(order.getTotal()));
			}
		}
	}
}
