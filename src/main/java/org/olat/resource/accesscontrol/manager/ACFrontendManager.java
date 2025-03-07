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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.EnrollState;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
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
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.CostCenter;
import org.olat.resource.accesscontrol.CostCenterSearchParams;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferOrganisationSelection;
import org.olat.resource.accesscontrol.OfferRef;
import org.olat.resource.accesscontrol.OfferToOrganisation;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.ACResourceInfo;
import org.olat.resource.accesscontrol.model.ACResourceInfoImpl;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.AccessTransactionStatus;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.model.OrderAdditionalInfos;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.model.RawOrderItem;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
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
	private ACOfferToOrganisationDAO offerToOrganisationDAO;
	@Autowired
	private ACMethodDAO methodManager;
	@Autowired
	private ACCostCenterDAO costCenterDao;
	@Autowired
	private ACBillingAddressDAO billingAddressDao;
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
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, Boolean knowMember, boolean isGuest,
			Boolean webPublish, boolean allowNonInteractiveAccess) {
		// Guests
		if (isGuest) {
			boolean guestAccessible = isGuestAccessible(entry, true);
			return new AccessResult(guestAccessible);
		}
		
		// Already member
		boolean member;
		if(knowMember == null) {
			member = repositoryService.isMember(forId, entry);
		} else {
			member = knowMember.booleanValue();
		}
		if(member) {
			return new AccessResult(true);
		}
		
		// Open access
		List<OrganisationRef> offerOrganisations = webPublish == null || !webPublish? getOfferOrganisations(forId): null;
		if (RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), ACService.RESTATUS_ACTIVE_OPEN)) {
			boolean openAccessible = accessManager.isOpenAccessible(entry.getOlatResource(), webPublish, offerOrganisations);
			if (openAccessible) {
				return new AccessResult(true);
			}
		}
		
		// Bookings
		if(!accessModule.isEnabled()) {
			return new AccessResult(false);
		}
		
		Date now = new Date();
		List<Offer> offers = getOffers(entry, true, true, now, false, webPublish, offerOrganisations);
		if(offers.isEmpty()) {
			return new AccessResult(false);
		}
		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}
	
	@Override
	public AccessResult isAccessible(CurriculumElement element, Identity forId, Boolean knowMember, boolean isGuest,
			Boolean webPublish, boolean allowNonInteractiveAccess) {
		if (isGuest) {
			return new AccessResult(false);
		}
		
		// Already member
		boolean member;
		if (knowMember == null) {
			member = !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(forId)).isEmpty();
		} else {
			member = knowMember.booleanValue();
		}
		if(member) {
			return new AccessResult(true);
		}
		
		// Bookings
		if(!accessModule.isEnabled()) {
			return new AccessResult(false);
		}
		
		Date now = new Date();
		List<OrganisationRef> offerOrganisations = webPublish == null || !webPublish? getOfferOrganisations(forId): null;
		List<Offer> offers = getOffers(element, true, true, now, false, webPublish, offerOrganisations);
		if(offers.isEmpty()) {
			return new AccessResult(false);
		}
		return isAccessible(forId, offers, allowNonInteractiveAccess);
	}
	
	@Override
	public boolean isGuestAccessible(RepositoryEntry entry, boolean filterStatus) {
		if (filterStatus && !RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), ACService.RESTATUS_ACTIVE_GUEST)) {
			return false;
		}
		return accessManager.isGuestAccessible(entry.getOlatResource());
	}

	@Override
	public boolean isResourceAccessControled(OLATResource resource, Date atDate) {
		return methodManager.isValidMethodAvailable(resource, atDate);
	}

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
		
		if (BusinessGroupStatusEnum.active != group.getGroupStatus()) {
			return new AccessResult(false);
		}

		Date now = dateNow();
		OLATResource resource = OLATResourceManager.getInstance().findResourceable(group);
		List<Offer> offers = accessManager.findOfferByResource(resource, true, now, false, null, null);
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

	private AccessResult isAccessible(Identity identity, List<Offer> offers, boolean allowNonInteractiveAccess) {
		List<OfferAccess> offerAccess = methodManager.getOfferAccess(offers, true);
		if(offerAccess.isEmpty()) {
			return new AccessResult(false);
		}
		if(allowNonInteractiveAccess && offerAccess.size() == 1) {
			//is it a method without user interaction as the free access?
			OfferAccess link = offerAccess.get(0);
			if(!link.getMethod().isNeedUserInteraction() && link.getOffer().isAutoBooking()) {
				return accessResource(identity, link, OrderStatus.PAYED, null, identity);
			}
		}
		return new AccessResult(false, offerAccess);
	}
	
	@Override
	public List<OrganisationRef> getOfferOrganisations(IdentityRef identity) {
		if (!organisationModule.isEnabled() || identity == null) return List.of();
		
		return organisationService.getOrganisationsWithParentLines(identity, OrganisationRoles.user);
	}
	
	@Override
	public List<Organisation> getSelectionOfferOrganisations(Identity identity) {
		OfferOrganisationSelection offerOrganisationSelection = accessModule.getOfferOrganisationSelection();
		if (OfferOrganisationSelection.all == offerOrganisationSelection) {
			return organisationService.getOrganisations();
		} else if (OfferOrganisationSelection.sub == offerOrganisationSelection) {
			return organisationService.getOrganisations(identity, OrganisationRoles.administrator,
					OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		}
		return new ArrayList<>(1);
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
	public void deleteOffers(OLATResource resource) {
		accessManager.findOfferByResource(resource, true, null, false, null, null).forEach(offer -> accessManager.deleteOffer(offer));
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
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, "BusinessGroup", true, now, null);
	}

	@Override
	public List<OLATResourceAccess> filterResourceWithAC(List<OLATResource> resources, List<? extends OrganisationRef> offerOrganisations) {
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
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, "BusinessGroup", true, now, offerOrganisations);
	}
	
	@Override
	public List<OLATResource> filterResourceWithOpenAccess(List<OLATResource> resources,  Boolean webPublish, List<? extends OrganisationRef> offerOrganisations) {
		return accessManager.loadOpenAccessibleResources(resources, webPublish, offerOrganisations);
	}
	
	@Override
	public List<OLATResource> filterResourceWithGuestAccess(List<OLATResource> resources) {
		return accessManager.loadGuestAccessibleResources(resources);
	}

	@Override
	public List<AccessMethod> findAccessMethods(Order order) {
		return methodManager.getAccessMethods(order);
	}

	@Override
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate, List<? extends OrganisationRef> offerOrganisations) {
		return accessManager.findOfferByResource(resource, valid, atDate, false, null, offerOrganisations);
	}
	
	@Override
	public List<OfferAndAccessInfos> findOfferAndAccessByResource(OLATResource resource, boolean valid) {
		return accessManager.findOfferByResource(resource, valid);
	}

	@Override
	public List<Offer> getOffers(RepositoryEntry entry, boolean valid, boolean filterByStatus, Date atDate,
			boolean dateMandatory, Boolean webPublish, List<? extends OrganisationRef> offerOrganisations) {
		List<Offer> offers = accessManager.findOfferByResource(entry.getOlatResource(), valid, atDate, dateMandatory, webPublish, offerOrganisations);
		if (filterByStatus) {
			offers = offers.stream()
					.filter(offer -> filterByStatus(offer, entry.getEntryStatus()))
					.collect(Collectors.toList());
		}
		return offers;
	}

	private boolean filterByStatus(Offer offer, RepositoryEntryStatusEnum status) {
		if (offer.isGuestAccess()) {
			return RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_GUEST);
		}
		if (offer.isOpenAccess()) {
			return RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_OPEN);
		}
		return offer.getValidFrom() == null && offer.getValidTo() == null
				? RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_METHOD)
				: RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_METHOD_PERIOD);
	}
	
	@Override
	public List<Offer> getOffers(CurriculumElement element, boolean valid, boolean filterByStatus, Date atDate,
			boolean dateMandatory, Boolean webPublish, List<? extends OrganisationRef> offerOrganisations) {
		List<Offer> offers = accessManager.findOfferByResource(element.getResource(), valid, atDate, dateMandatory, webPublish, offerOrganisations);
		if (filterByStatus) {
			offers = offers.stream()
					.filter(offer -> filterByStatus(offer, element.getElementStatus()))
					.collect(Collectors.toList());
		}
		return offers;
	}

	private boolean filterByStatus(Offer offer, CurriculumElementStatus status) {
		return offer.getValidFrom() == null && offer.getValidTo() == null
				? Arrays.asList(ACService.CESTATUS_ACTIVE_METHOD).contains(status)
				: Arrays.asList(ACService.CESTATUS_ACTIVE_METHOD_PERIOD).contains(status);
	}
	
	@Override
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, String resourceType,
			boolean valid, Date atDate, List<? extends OrganisationRef> offerOrganisations) {
		if(resourceKeys == null || resourceKeys.isEmpty()) {
			return new ArrayList<>();
		}
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, null, valid, atDate, offerOrganisations);
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
		List<Offer> offers = accessManager.findOfferByResource(group.getResource(), valid, atDate, false, null, null);
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
	public void updateOfferOrganisations(Offer offer, Collection<Organisation> organisations) {
		if (organisations == null || organisations.isEmpty()) {
			offerToOrganisationDAO.delete(offer);
			return;
		}
		
		List<OfferToOrganisation> currentRelations = offerToOrganisationDAO.loadRelations(offer, null);
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(OfferToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		
		// Create relation for new organisations
		organisations.stream()
				.filter(org -> !currentOrganisations.contains(org))
				.forEach(org -> offerToOrganisationDAO.createRelation(offer, org));

		// Create relation of old organisations
		currentRelations.stream()
				.filter(rel -> !organisations.contains(rel.getOrganisation()))
				.forEach(rel -> offerToOrganisationDAO.delete(rel));
	}
	
	@Override
	public List<Organisation> getOfferOrganisations(OfferRef offer) {
		return offerToOrganisationDAO.loadOrganisations(offer);
	}
	
	@Override
	public Map<Long, List<Organisation>> getOfferKeyToOrganisations(Collection<? extends OfferRef> offers) {
		return offerToOrganisationDAO.loadRelations(offers).stream()
				.collect(Collectors.groupingBy(
						oto -> oto.getOffer().getKey(),
						Collectors.mapping(OfferToOrganisation::getOrganisation, Collectors.toList())));
	}

	@Override
	public AccessResult accessResource(Identity identity, OfferAccess link, OrderStatus orderStatus, Object argument, Identity doer) {
		if(link == null || link.getOffer() == null || link.getMethod() == null) {
			log.info(Tracing.M_AUDIT, "Access refused (no offer) to: {} for {}", link, identity);
			return new AccessResult(false);
		}
		
		MailPackage mailing = new MailPackage(link.getOffer().isConfirmationEmail());
		return accessResource(identity, link, orderStatus, argument, mailing, doer, null);
	}

	@Override
	public AccessResult accessResource(Identity identity, OfferAccess link, OrderStatus orderStatus,
			Object argument, MailPackage mailing, Identity doer, String adminNote) {
		
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
			if(allowAccesToResource(identity, link.getOffer(), link.getMethod(), mailing, doer, adminNote)) {
				String purchaseOrderNumber = (argument instanceof OrderAdditionalInfos infos) ? infos.purchaseOrderNumber() : null;
				String comment = (argument instanceof OrderAdditionalInfos infos) ? infos.comment() : null;
				BillingAddress billingAddress = (argument instanceof OrderAdditionalInfos infos) ? infos.billingAddress() : null;
				Order order = createAndSaveOrder(identity, link, orderStatus, billingAddress, purchaseOrderNumber, comment);
				log.info(Tracing.M_AUDIT, "Access granted to: {} for {}", link, identity);
				return new AccessResult(true, order);
			} else {
				log.info(Tracing.M_AUDIT, "Access error to: {} for {}", link, identity);
			}
		} else {
			log.info(Tracing.M_AUDIT, "Access refused to: {} for {}", link, identity);
		}
		return new AccessResult(false);
	}
	
	@Override
	public Order createAndSaveOrder(Identity identity, OfferAccess link, OrderStatus orderStatus,
			BillingAddress billingAddress, String purchaseOrderNumber, String comment) {
		Order order = orderManager.saveOneClick(identity, link, orderStatus,
				billingAddress, purchaseOrderNumber, comment);
		AccessTransaction transaction = transactionManager
				.createTransaction(order, order.getParts().get(0), link.getMethod());
		if(orderStatus == OrderStatus.NEW) {
			transactionManager.update(transaction, AccessTransactionStatus.NEW);
		} else if(orderStatus == OrderStatus.PREPAYMENT) {
			transactionManager.update(transaction, AccessTransactionStatus.PENDING);
		} else if(orderStatus == OrderStatus.PAYED) {
			transactionManager.update(transaction, AccessTransactionStatus.SUCCESS);
		} else if(orderStatus == OrderStatus.CANCELED) {
			transactionManager.update(transaction, AccessTransactionStatus.CANCELED);
		} else if(orderStatus == OrderStatus.ERROR) {
			transactionManager.update(transaction, AccessTransactionStatus.ERROR);
		} else {
			transactionManager.save(transaction);
		}
		dbInstance.commit();
		return order;
	}
	
	@Override
	public void cancelOrder(Order order, Identity doer, String adminNote, MailPackage mailing) {
		List<OLATResource> deniedRessources = new ArrayList<>();
		Identity identity = order.getDelivery();
		if(identity != null) {
			identity.getUser();// Load it to send email
		}
		
		PriceImpl orderFees = new PriceImpl(new BigDecimal("0"), order.getTotalOrderLines().getCurrencyCode());
		for(OrderPart part:order.getParts()) {
			for(OrderLine line:part.getOrderLines()) {
				Offer offer = line.getOffer();
				Date begin = getBeginDate(offer.getResource());
				Price cancellationFee = getCancellationFee(line, begin, DateUtils.getStartOfDay(new Date()));
				if (cancellationFee != null) {
					orderFees = orderFees.add(cancellationFee);
				}
				GroupMembershipStatus nextStatus = cancellationFee != null
						? GroupMembershipStatus.cancelWithFee
						: GroupMembershipStatus.cancel;
				if(internalDenyAccesToResource(identity, offer, nextStatus, doer, adminNote)) {
					deniedRessources.add(offer.getResource());
				}
			}
		}
		if (orderFees.getAmount().compareTo(BigDecimal.ZERO) == 0) {
			orderFees = null;
		}
		order.setCancellationFeesLines(orderFees);
		order.setCancellationFees(orderFees);
		order = orderManager.save(order, OrderStatus.CANCELED);
		
		if(mailing != null && mailing.isSendEmail()) {
			for(OLATResource deniedRessource:deniedRessources) {
				sendMail(identity, deniedRessource, doer, mailing);
			}
		}
	}
	
	private void sendMail(Identity delivery, OLATResource resource, Identity doer, MailPackage mailing) {
		if("CurriculumElement".equals(resource.getResourceableTypeName())) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
			Curriculum curriculum = curriculumElement.getCurriculum();
			CurriculumMailing.sendEmail(doer, delivery, curriculum, curriculumElement, mailing);
		}
	}
	
	@Override
	public Date getBeginDate(OLATResource resource) {
		if("CurriculumElement".equals(resource.getResourceableTypeName())) {
			CurriculumElement element = curriculumService.getCurriculumElement(resource);
			return element == null ? null : element.getBeginDate();
		} else if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			return null;
		}
		
		RepositoryEntry entry = repositoryEntryDao.loadByResource(resource);
		if(entry != null && entry.getLifecycle() != null) {
			return entry.getLifecycle().getValidFrom();
		}
		return null;
	}

	@Override
	public Order changeOrderStatus(Order order, OrderStatus newStatus) {
		log.info("Change order status {} ({}) from {} to {}", order.getOrderNr(), order.getKey(), order.getOrderStatus(), newStatus);
		return orderManager.save(order, newStatus);
	}

	@Override
	public void acceptReservationToResource(Identity identity, ResourceReservation reservation) {
		OLATResource resource = reservation.getResource();
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			//it's a reservation for a group
			businessGroupService.acceptPendingParticipation(identity, identity, resource);
		} else if("CurriculumElement".equals(resource.getResourceableTypeName())) {
			//it's a reservation for a curriculum
			curriculumService.acceptPendingParticipation(reservation, identity, identity);
		} else {
			repositoryManager.acceptPendingParticipation(identity, identity, resource, reservation);
		}
	}

	@Override
	public void removeReservation(Identity ureqIdentity, Identity identity, ResourceReservation reservation, String adminNote) {
		OLATResource resource = reservation.getResource();
		reservationDao.deleteReservation(reservation);
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			dbInstance.commit();//needed to have the right number of participants to calculate upgrade from waiting list
			businessGroupService.cancelPendingParticipation(ureqIdentity, reservation);
		} else if("CurriculumElement".equals(resource.getResourceableTypeName())) {
			dbInstance.commit();//needed to have the right number of participants to calculate upgrade from waiting list
			curriculumService.cancelPendingParticipation(reservation, identity, ureqIdentity, adminNote);
		}
	}

	@Override
	public ResourceReservation getReservation(IdentityRef identity, OLATResource resource) {
		return reservationDao.loadReservation(identity, resource);
	}

	@Override
	public List<ResourceReservation> getReservations(List<OLATResource> resources) {
		return reservationDao.loadReservations(new SearchReservationParameters(resources));
	}

	@Override
	public List<ResourceReservation> getReservations(SearchReservationParameters searchParams) {
		return reservationDao.loadReservations(searchParams);
	}

	@Override
	public List<ResourceReservation> getReservations(IdentityRef identity) {
		return reservationDao.loadReservations(identity);
	}

	@Override
	public boolean reserveAccessToResource(Identity identity, Offer offer, AccessMethod method, MailPackage mailing, Identity doer, String adminNote) {
		OLATResource resource = offer.getResource();
		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			return reserveAccessToBusinessGroup(identity, offer, resource, method);
		}
		if("CurriculumElement".equals(resourceType)) {
			return reserveAccessToCurriculumElement(identity, offer, resource, mailing, doer, adminNote);
		}
		RepositoryEntry entry = repositoryEntryDao.loadByResource(resource);
		if (entry != null) {
			if(!repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
				Group defaultGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
				groupMembershipHistoryDao.createMembershipHistory(defaultGroup, identity,
						GroupRoles.participant.name(), GroupMembershipStatus.reservation, true, null, null,
						identity, null);
				reservationDao.createReservation(identity, "repo_participant", null, Boolean.valueOf(!offer.isConfirmationByManagerRequired()), resource);
			}
			return true;
		}
		return false;
	}

	private boolean reserveAccessToCurriculumElement(Identity identity, Offer offer, OLATResource resource, MailPackage mailing,
			Identity doer, String adminNote) {
		boolean reserved = false;
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
		if (curriculumElement != null) {
			boolean isParticipant = curriculumService.getCurriculumElementMemberships(List.of(curriculumElement), identity).stream()
					.anyMatch(CurriculumElementMembership::isParticipant);
			if(!isParticipant) {
				groupMembershipHistoryDao.createMembershipHistory(curriculumElement.getGroup(), identity,
						GroupRoles.participant.name(), GroupMembershipStatus.reservation, true, null, null,
						doer, adminNote);
				reservationDao.createReservation(identity, CurriculumService.RESERVATION_PREFIX.concat("participant"),
						null, Boolean.valueOf(!offer.isConfirmationByManagerRequired()), resource);
				
				if(mailing != null && mailing.isSendEmail()) {
					Curriculum curriculum = curriculumElement.getCurriculum();
					if(mailing.getTemplate() == null) {
						MailTemplate template = CurriculumMailing
								.getMembershipBookedByParticipantNeedConfirmationTemplate(curriculum, curriculumElement, doer);
						mailing = mailing.copyWithTemplate(template);
					}
					CurriculumMailing.sendEmail(doer, identity, curriculum, curriculumElement, mailing);
				}
			}
			reserved = true;
		}
		return reserved;
	}
	
	private boolean reserveAccessToBusinessGroup(Identity identity, Offer offer, OLATResource resource, AccessMethod method) {
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
			int reservations = reservationDao.countReservations(resource, BusinessGroupService.GROUP_PARTICIPANT);
			if(currentCount + reservations < reloadedGroup.getMaxParticipants().intValue()) {
				groupMembershipHistoryDao.createMembershipHistory(group.getBaseGroup(), identity,
						GroupRoles.participant.name(), GroupMembershipStatus.reservation, true, null, null,
						identity, null);
				reservationDao.createReservation(identity, method.getType(), null, Boolean.valueOf(!offer.isConfirmationByManagerRequired()), resource);
				reserved = true;
			}
		}
		return reserved;
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
	public boolean isAccessToResourcePending(OLATResource resource, IdentityRef identity) {	
		List<Order> orders = orderManager.findPendingOrders(resource, identity);
		return !orders.isEmpty();
	}
	
	@Override
	public boolean isAccessRefusedByStatus(RepositoryEntry entry, IdentityRef identity) {
		return !RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.publishedAndClosed())
					&& !findOrderItems(entry.getOlatResource(), identity, null, null, null, new OrderStatus[] { OrderStatus.PAYED },
							null, null, false, false, 0, 1, null).isEmpty();
	}
	
	@Override
	public boolean isAccessRefusedByStatus(CurriculumElement element, IdentityRef identity) {
		return !Arrays.asList(ACService.CESTATUS_ACTIVE_METHOD).contains(element.getElementStatus())
				&& !findOrderItems(element.getResource(), identity, null, null, null, new OrderStatus[] { OrderStatus.PAYED },
						null, null, false, false, 0, 1, null).isEmpty();
	}

	@Override
	public boolean allowAccesToResource(Identity identity, Offer offer, AccessMethod method, MailPackage mailing,
			Identity doer, String adminNote) {
		//check if offer is ok: key is stupid but further check as date, validity...
		if(offer.getKey() == null) {
			return false;
		}

		//check the resource
		OLATResource resource = offer.getResource();
		if(resource == null || resource.getKey() == null || resource.getResourceableId() == null || resource.getResourceableTypeName() == null) {
			return false;
		}
		
		if (offer.isConfirmationByManagerRequired()) {
			return reserveAccessToResource(identity, offer, method, mailing, doer, adminNote);
		}

		String resourceType = resource.getResourceableTypeName();
		if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				EnrollState result = businessGroupService.enroll(doer, null, identity, group, mailing);
				return !result.isFailed();
			}
		} else if("CurriculumElement".equals(resourceType)) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
			if (curriculumElement != null) {
				boolean isParticipant = curriculumService.getCurriculumElementMemberships(List.of(curriculumElement), identity).stream()
						.anyMatch(CurriculumElementMembership::isParticipant);
				if (!isParticipant) {
					List<CurriculumElementMembershipChange> changes = new ArrayList<>();		
					changes.add(CurriculumElementMembershipChange.addMembership(identity, curriculumElement, true, CurriculumRoles.participant));
					
					List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
					for(CurriculumElement descendant:descendants) {
						changes.add(CurriculumElementMembershipChange.addMembership(identity, descendant, false, CurriculumRoles.participant));
					}
					curriculumService.updateCurriculumElementMemberships(doer, null, changes, mailing);
					return true;
				}
			}
		} else {
			RepositoryEntry entry = repositoryEntryDao.loadByResource(resource);
			if(entry != null) {
				if(!repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
					Group group = repositoryEntryRelationDao.getDefaultGroup(entry);
					repositoryEntryRelationDao.addRole(identity, group, GroupRoles.participant.name());
					groupMembershipHistoryDao.createMembershipHistory(group, identity,
							GroupRoles.participant.name(), GroupMembershipStatus.active, false, null, null,
							doer, null);
					if(mailing != null && mailing.isSendEmail()) {
						RepositoryMailing.sendEmail(doer, identity, entry, RepositoryMailing.Type.addParticipantItself, mailing);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean denyAccesToResource(Identity identity, Offer offer) {
		return internalDenyAccesToResource(identity, offer, GroupMembershipStatus.declined, identity, null);
	}
	
	private boolean internalDenyAccesToResource(Identity identity, Offer offer, GroupMembershipStatus status,
			Identity doer, String adminNote) {
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
					groupMembershipHistoryDao.createMembershipHistory(group.getBaseGroup(), identity,
							GroupRoles.participant.name(), GroupMembershipStatus.removed, false, null, null,
							doer, null);
				}
				return true;
			}
		} else if("CurriculumElement".equals(resourceType)) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
			if (curriculumElement != null) {
				// Delegate the job to the curriculum service, inherited memberships will be removed too
				boolean removed = curriculumService.removeMember(curriculumElement, identity, CurriculumRoles.participant,
						status, doer, adminNote);
				if (removed) {
					return removed;
				} // else check reservations below
			}
		} else {
			RepositoryEntryRef entry = repositoryEntryDao.loadByResource(resource);
			if(entry != null) {
				if(repositoryEntryRelationDao.hasRole(identity, entry, GroupRoles.participant.name())) {
					Group defaultGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
					repositoryEntryRelationDao.removeRole(identity, entry, GroupRoles.participant.name());
					groupMembershipHistoryDao.createMembershipHistory(defaultGroup, identity,
							GroupRoles.participant.name(), GroupMembershipStatus.removed, false, null, null,
							doer, null);
				}
				return true;
			}
		}
		ResourceReservation reservation = reservationDao.loadReservation(identity, resource);
		if(reservation != null) {
			removeReservation(doer, identity, reservation, adminNote);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean tryAutoBooking(Identity identity, RepositoryEntry entry, AccessResult acResult) {
		if (entry != null && !entry.getEntryStatus().decommissioned()) {
			return tryAutoBooking(identity, acResult);
		}
		return false;
	}
	
	@Override
	public boolean tryAutoBooking(Identity identity, CurriculumElement element, AccessResult acResult) {
		if (element != null && !element.getElementStatus().isCancelledOrClosed()) {
			return tryAutoBooking(identity, acResult);
		}
		return false;
	}

	private boolean tryAutoBooking(Identity identity, AccessResult acResult) {
		if (identity != null && acResult.getAvailableMethods().size() == 1) {
			OfferAccess offerAccess = acResult.getAvailableMethods().get(0);
			if (offerAccess.getOffer().isAutoBooking() && !offerAccess.getMethod().isNeedUserInteraction()) {
				return accessResource(identity, offerAccess, OrderStatus.PAYED, null, identity).isAccessible();
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
		} else if("CurriculumElement".equals(resourceType)) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(resource);
			if (curriculumElement != null) {
				return curriculumElement.getDisplayName();
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
		List<OLATResource> curriculumElementResources = new ArrayList<>(resources.size());
		for(OLATResource resource:resources) {
			String resourceType = resource.getResourceableTypeName();
			if("BusinessGroup".equals(resourceType)) {
				groupResources.add(resource);
			} else if("CurriculumElement".equals(resourceType)) {
				curriculumElementResources.add(resource);
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
		if(!curriculumElementResources.isEmpty()) {
			List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(curriculumElementResources);
			for(CurriculumElement curriculumElement:curriculumElements) {
				ACResourceInfoImpl info = new ACResourceInfoImpl();
				info.setName(curriculumElement.getDisplayName());
				info.setDescription(curriculumElement.getDescription());
				info.setResource(curriculumElement.getResource());
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
	public List<AccessMethod> getAvailableMethods() {
		return methodManager.getAvailableMethods();
	}
	
	@Override
	public boolean isMethodAvailable(String methodType) {
		return getAvailableMethods().stream()
				.map(AccessMethod::getType)
				.anyMatch(type -> methodType.equals(type));
	}

	@Override
	public List<AccessMethod> getAvailableMethods(OLATResource resource, Identity identity, Roles roles) {
		List<AccessMethod> methods = methodManager.getAvailableMethods();
		
		List<AccessMethod> allowedMethods = new ArrayList<>();
		for(AccessMethod method:methods) {
			AccessMethodHandler handler = accessModule.getAccessMethodHandler(method.getType());
			AccessMethodSecurityCallback secCallback = handler.getSecurityCallback(resource, identity, roles);
			if(secCallback.canUse()) {
				allowedMethods.add(method);
			}
		}
		
		return allowedMethods;
	}

	@Override
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		return methodManager.createOfferAccess(offer, method);
	}

	@Override
	public OfferAccess copyOfferAccess(OfferAccess link, Date validFrom, Date validTo, OLATResource resource, String resourceName) {
		Offer offer = link.getOffer();
		AccessMethod method = link.getMethod();
		Offer offerCopy = accessManager.copyAndPersistOffer(offer, validFrom, validTo, resource, resourceName);
		OfferAccess accessCopy = createOfferAccess(offerCopy, method);
		accessCopy.setValidFrom(validFrom);
		accessCopy.setValidTo(validTo);
		accessCopy = methodManager.save(accessCopy);
		List<Organisation> organisations = getOfferOrganisations(offer);
		if(organisations != null && !organisations.isEmpty()) {
			updateOfferOrganisations(offerCopy, organisations);
		}
		return accessCopy;
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
	public CostCenter createCostCenter() {
		return costCenterDao.create();
	}
	
	@Override
	public CostCenter updateCostCenter(CostCenter costCenter) {
		return costCenterDao.update(costCenter);
	}

	@Override
	public void deleteCostCenter(CostCenter costCenter) {
		costCenterDao.delete(costCenter);
	}
	
	@Override
	public List<CostCenter> getCostCenters(CostCenterSearchParams searchParams) {
		return costCenterDao.loadCostCenters(searchParams);
	}

	@Override
	public Map<Long, Long> getCostCenterKeyToOfferCount(Collection<CostCenter> costCenters) {
		return accessManager.getCostCenterKeyToOfferCount(costCenters);
	}

	@Override
	public Offer addCostCenter(Offer ofer, CostCenter costCenter) {
		return accessManager.save(ofer, costCenter);
	}
	
	@Override
	public BillingAddress createBillingAddress(Organisation organisation, Identity identity) {
		return billingAddressDao.create(organisation, identity);
	}
	
	@Override
	public BillingAddress updateBillingAddress(BillingAddress billingAddress) {
		return billingAddressDao.update(billingAddress);
	}
	
	@Override
	public void deleteBillingAddress(BillingAddress billingAddress) {
		billingAddressDao.delete(billingAddress);
	}
	
	@Override
	public List<BillingAddress> getBillingAddresses(BillingAddressSearchParams searchParams) {
		return billingAddressDao.loadBillingAddresses(searchParams);
	}

	@Override
	public Map<Long, Long> getBillingAddressKeyToOrderCount(Collection<BillingAddress> billingAddresss) {
		return orderManager.getBillingAddressKeyToOrderCount(billingAddresss);
	}
	
	@Override
	public Order addBillingAddress(Order order, BillingAddress billingAddress) {
		BillingAddress previousAddress = order.getBillingAddress();
		
		Order updatedOrder = orderManager.save(order, billingAddress);
		
		// Always delete temporary addresses if not used anymore
		if (previousAddress != null && previousAddress.getOrganisation() == null && previousAddress.getIdentity() == null) {
			dbInstance.commit();
			billingAddressDao.delete(previousAddress);
		}
		
		return updatedOrder;
	}
	
	@Override
	public Order updateOrder(Order order) {
		return orderManager.save(order);
	}

	@Override
	public List<Order> findOrders(Identity delivery, OrderStatus... status) {
		return orderManager.findOrdersByDelivery(delivery, status);
	}

	@Override
	public List<Order> findOrders(Identity delivery, OLATResource resource, OrderStatus... status) {
		return orderManager.findOrdersBy(delivery, resource, status);
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
	public List<OrderTableItem> findOrderItems(OLATResource resource, IdentityRef delivery, Long orderNr, Date from,
			Date to, OrderStatus[] status, List<Long> methodsKeys, List<Long> offerAccessKeys,
			boolean filterAdjustedAmount, boolean billingAddressProposal, int firstResult, int maxResults,
			List<UserPropertyHandler> userPropertyHandlers, SortKey... orderBy) {
		List<AccessMethod> methods = methodManager.getAllMethods();
		Map<String,AccessMethod> methodMap = new HashMap<>();
		for(AccessMethod method:methods) {
			methodMap.put(method.getKey().toString(), method);
		}

		List<RawOrderItem> rawOrders = orderManager.findNativeOrderItems(resource, delivery, orderNr, from, to, status,
				methodsKeys, offerAccessKeys, filterAdjustedAmount, billingAddressProposal, firstResult, maxResults,
				userPropertyHandlers, orderBy);
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
			
			Status finalStatus = Status.getStatus(orderStatusStr, rawOrder.getCancellationFee(), rawOrder.getTrxStatus(), pspTrxStatus, orderMethods);
			String offerLabel = deduplicate(rawOrder.getLabel());
			String resourceDisplayName = deduplicate(rawOrder.getResourceName());
			String costCenteryName = deduplicate(rawOrder.getCostCenterName());
			String costCenteryAccount = deduplicate(rawOrder.getCostCenterAccount());
			
			OrderTableItem item = new OrderTableItem(rawOrder.getOrderKey(), rawOrder.getOrderNr(), offerLabel,
					rawOrder.getPrice(), rawOrder.getPriceLines(), rawOrder.getCancellationFee(),
					rawOrder.getCancellationFeeLines(), rawOrder.isBillingAddressProposal(),
					rawOrder.getBillingAddressIdentifier(), rawOrder.getPurchaseOrderNumber(), rawOrder.getComment(),
					rawOrder.getCreationDate(), orderStatus, finalStatus, rawOrder.getDeliveryKey(),
					resourceDisplayName, costCenteryName, costCenteryAccount, rawOrder.getUsername(),
					rawOrder.getUserProperties(), orderMethods);
			items.add(item);
		}

		return items;
	}
	
	private String deduplicate(String string) {
		if(string != null) {
			String[] arr = string.split(",");
			List<String> values = new ArrayList<>(4);
			for(String str:arr) {
				if(StringHelper.containsNonWhitespace(str) && !values.contains(str)) {
					values.add(str);
				}
			}
			string = String.join(", ", values);
		}
		return string;
	}



	/**
	 * @return The current date without time
	 */
	private Date dateNow() {
		return CalendarUtils.removeTime(new Date());
	}

	@Override
	public boolean hasOrder(OfferRef offer) {
		return orderManager.hasOrder(offer);
	}
	
	@Override
	public Price getCancellationFee(OLATResource recource, Date resourceBeginDate, List<Order> orders) {
		Date cancellationDate = DateUtils.getStartOfDay(new Date());
		Price totalFee = null;
		
		for(Order order:orders) {
			for(OrderPart part:order.getParts()) {
				for(OrderLine line:part.getOrderLines()) {
					OLATResource offerResource = line.getOffer().getResource();
					if(recource.equals(offerResource)) {
						Price fee = getCancellationFee(line, resourceBeginDate, cancellationDate);
						if(fee != null) {
							if(totalFee == null) {
								totalFee = fee;
							} else {
								totalFee = totalFee.add(fee);
							}
						}
					}
				}
			}
		}
		
		return totalFee;
	}
	
	private Price getCancellationFee(OrderLine orderLine, Date resourceBeginDate, Date cancellationDate) {
		Price cancellingFee = orderLine.getCancellationFee();
		Integer cancellationDeadline = orderLine.getCancellingFeeDeadlineDays();

		Price fee;
		if(cancellingFee == null) {
			fee = null;
		} else if(cancellationDeadline == null) {
			fee = cancellingFee;
		} else if(cancellatioFeeApply(resourceBeginDate, cancellationDate, cancellationDeadline.intValue())) {
			fee = cancellingFee;
		} else {
			fee = null;
		}
		return fee;
	}
	
	private boolean cancellatioFeeApply(Date resourceBeginDate, Date cancellationDate, long days) {
		if(resourceBeginDate != null) {
			long countDays = DateUtils.countDays(cancellationDate, resourceBeginDate);
			if(days > countDays) {
				return true;
			}
		}
		return false;
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
			
			List<OrderTableItem> orders = findOrderItems(null, identity, null, null, null, null, null, null, false, false, 0, -1, null);
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
				row.addCell(col++, PriceFormat.fullFormat(order.getPrice()));
			}
		}
	}
}
