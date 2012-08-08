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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.BusinessGroupAccess;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.model.ResourceReservation;
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
public class ACFrontendManager extends BasicManager implements ACService {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AccessControlModule accessModule;
	@Autowired
	private ACOfferManager accessManager;
	@Autowired
	private ACMethodManager methodManager;
	@Autowired
	private ACOrderManager orderManager;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private ACTransactionManager transactionManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	

	/**
	 * The rule to access the repository entry:<br/>
	 * -No offer, access is free<br/>
	 * -Owners have always access to the resource<br/>
	 * -Tutors have access to the resource<br/>
	 * -Participants have access to the resource<br/>
	 * @param entry
	 * @param forId
	 * @return
	 */
	public AccessResult isAccessible(RepositoryEntry entry, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}
		
		boolean member = repositoryManager.isMember(forId, entry);
		if(member) {
			return new AccessResult(true);
		}
		
		List<Offer> offers = accessManager.findOfferByResource(entry.getOlatResource(), true, new Date());
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
	
	/**
	 * 
	 * @param resource
	 * @param atDate 
	 * @return
	 */
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
	public AccessResult isAccessible(BusinessGroup group, Identity forId, boolean allowNonInteractiveAccess) {
		if(!accessModule.isEnabled()) {
			return new AccessResult(true);
		}

		boolean tutor = securityManager.isIdentityInSecurityGroup(forId, group.getOwnerGroup());
		if(tutor) {
			return new AccessResult(true);
		}
		
		if(group.getPartipiciantGroup() != null) {
			boolean participant = securityManager.isIdentityInSecurityGroup(forId, group.getPartipiciantGroup());
			if(participant) {
				return new AccessResult(true);
			}
		}
		
		OLATResource resource = OLATResourceManager.getInstance().findResourceable(group);
		List<Offer> offers = accessManager.findOfferByResource(resource, true, new Date());
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
	
	public Offer createOffer(OLATResource resource, String resourceName) {
		return accessManager.createOffer(resource, resourceName);
	}
	
	public void deleteOffer(Offer offer) {
		accessManager.deleteOffer(offer);
	}
	
	public List<OLATResourceAccess> filterRepositoryEntriesWithAC(List<RepositoryEntry> repoEntries) {
		List<Long> resourceKeys = new ArrayList<Long>();
		for(RepositoryEntry entry:repoEntries) {
			OLATResource ores = entry.getOlatResource();
			resourceKeys.add(ores.getKey());
		}
		
		List<OLATResourceAccess> resourceWithOffers = methodManager.getAccessMethodForResources(resourceKeys, null, true, new Date());
		return resourceWithOffers;
	}
	
	public Set<Long> filterResourcesWithAC(Collection<Long> resourceKeys) {
		Set<Long> resourceWithOffers = accessManager.filterResourceWithOffer(resourceKeys);
		return resourceWithOffers;
	}
	
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate) {
		return accessManager.findOfferByResource(resource, valid, atDate);
	}
	
	public List<BusinessGroupAccess> getOfferAccessForBusinessGroup(boolean valid, Date atDate) {
		return methodManager.getAccessMethodForBusinessGroup(valid, atDate);
	}
	
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, String resourceType, boolean valid, Date atDate) {
		return methodManager.getAccessMethodForResources(resourceKeys, resourceType, valid, atDate);
	}

	/**
	 * Get the list of access methods for a business group that are currently available
	 * @param group 
	 * @param valid 
	 * @param atDate
	 * @return The list of OfferAccess objects that represent available access methods
	 */
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
	
	public List<OfferAccess> getOfferAccessByResource(Collection<Long> resourceKeys, boolean valid, Date atDate) {
		return methodManager.getOfferAccessByResource(resourceKeys, valid, atDate);
	}
	
	public void save(Offer offer) {
		accessManager.saveOffer(offer);
	}
	
	public OfferAccess saveOfferAccess(OfferAccess link) {
		accessManager.saveOffer(link.getOffer());
		methodManager.save(link);
		return link;
	}
	
	public void saveOfferAccess(List<OfferAccess> links) {
		for(OfferAccess link:links) {
			accessManager.saveOffer(link.getOffer());
			methodManager.save(link);
		}
	}
	
	public AccessResult accessResource(Identity identity, OfferAccess link, Object argument) {
		if(link == null || link.getOffer() == null || link.getMethod() == null) {
			logAudit("Access refused (no offer) to: " + link + " for " + identity);
			return new AccessResult(false);
		}
		
		AccessMethodHandler handler = accessModule.getAccessMethodHandler(link.getMethod().getType());
		if(handler == null) {
			logAudit("Access refused (no handler method) to: " + link + " for " + identity);
			return new AccessResult(false);
		}
		
		if(handler.checkArgument(link, argument)) {
			if(allowAccesToResource(identity, link.getOffer())) {
				Order order = orderManager.saveOneClick(identity, link);
				AccessTransaction transaction = transactionManager.createTransaction(order, order.getParts().get(0), link.getMethod());
				transactionManager.save(transaction);
				logAudit("Access granted to: " + link + " for " + identity);
				return new AccessResult(true);
			} else {
				logAudit("Access error to: " + link + " for " + identity);
			}
		} else {
			logAudit("Access refused to: " + link + " for " + identity);
		}
		return new AccessResult(false);
	}
	
	@Override
	public void removeReservation(ResourceReservation reservation) {
		reservationDao.deleteReservation(reservation);
	}

	@Override
	public ResourceReservation getReservation(Identity identity, OLATResource resource) {
		return reservationDao.loadReservation(identity, resource);
	}

	@Override
	public int countReservations(OLATResource resource) {
		return reservationDao.countReservations(resource);
	}

	@Override
	public boolean reserveAccessToResource(final Identity identity, final OfferAccess offer) {
		final OLATResource resource = offer.getOffer().getResource();
		
		String resourceType = resource.getResourceableTypeName();
		if("CourseModule".equals(resourceType)) {
			return true;//don't need reservation
		} else if("BusinessGroup".equals(resourceType)) {
			final BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group.getWaitingListEnabled() != null && group.getWaitingListEnabled() != null) {
				return true; //don't need reservation
			}
			if(group.getMaxParticipants() == null && group.getMaxParticipants() <= 0) {
				return true;//don't need reservation
			}

			ResourceReservation reservation = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<ResourceReservation>() {
				public ResourceReservation execute() {
					BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(resource);
					ResourceReservation reservation = reservationDao.loadReservation(identity, resource);
					if(reservation != null) {
						return reservation;
					}
					
					int currentCount = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());
					int reservations = reservationDao.countReservations(resource);
					if(currentCount + reservations < reloadedGroup.getMaxParticipants().intValue()) {
						return reservationDao.createReservation(identity, resource);
					} else {
						return null;
					}
				}});
			
			return reservation != null;
		}
		return false;
	}

	@Override
	public void cleanupReservations() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date oneHourTimeout = cal.getTime();
		List<ResourceReservation> oldReservations = reservationDao.loadReservationOlderThan(oneHourTimeout);
		for(ResourceReservation reservation:oldReservations) {
			logAudit("Remove reservation:" + reservation);
			reservationDao.deleteReservation(reservation);
		}
	}

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
		if("CourseModule".equals(resourceType)) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resource, false);
			if(entry != null) {
				if(!securityManager.isIdentityInSecurityGroup(identity, entry.getParticipantGroup())) {
					securityManager.addIdentityToSecurityGroup(identity, entry.getParticipantGroup());
				}
				return true;
			}
		} else if("BusinessGroup".equals(resourceType)) {
			final BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				Boolean success = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<Boolean>() {
					public Boolean execute() {
						
						ResourceReservation reservation = reservationDao.loadReservation(identity, group.getResource());
						if(group.getWaitingListEnabled() != null && group.getWaitingListEnabled().booleanValue()) {
							if(!securityManager.isIdentityInSecurityGroup(identity, group.getWaitingGroup())) {
								securityManager.addIdentityToSecurityGroup(identity, group.getWaitingGroup());
							}
						} else {
							if(reservation != null
									|| (group.getMaxParticipants() == null && group.getMaxParticipants().intValue() <=0)
									|| (group.getMaxParticipants() != null && (group.getMaxParticipants().intValue() > 
									   (countReservations(group.getResource()) + securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup()))))) {
								if(!securityManager.isIdentityInSecurityGroup(identity, group.getPartipiciantGroup())) {
									securityManager.addIdentityToSecurityGroup(identity, group.getPartipiciantGroup());
								}
							} else {
								return Boolean.FALSE;
							}
						}
						
						if(reservation != null) {
							reservationDao.deleteReservation(reservation);
						}
						return Boolean.TRUE;
					}});
				return success.booleanValue();
			}
		}
		return false;
	}
	
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
		if("CourseModule".equals(resourceType)) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resource, false);
			if(entry != null) {
				if(securityManager.isIdentityInSecurityGroup(identity, entry.getParticipantGroup())) {
					securityManager.removeIdentityFromSecurityGroup(identity, entry.getParticipantGroup());
				}
				return true;
			}
		} else if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				if(securityManager.isIdentityInSecurityGroup(identity, group.getPartipiciantGroup())) {
					securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());
				}
				return true;
			}
		}
		return false;
	}
	
	public String resolveDisplayName(OLATResource resource) {
		String resourceType = resource.getResourceableTypeName();
		if("CourseModule".equals(resourceType)) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(resource, false);
			if(entry != null) {
				return entry.getDisplayname();
			}
		} else if("BusinessGroup".equals(resourceType)) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(resource);
			if(group != null) {
				return group.getName();
			}
		}
		return null;
	}
	
	public void enableMethod(Class<? extends AccessMethod> type, boolean enable) {
		methodManager.enableMethod(type, enable);
	}
	
	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles) {
		return methodManager.getAvailableMethods(identity, roles);
	}
	
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		return methodManager.createOfferAccess(offer, method);
	}
	
	public void deletedLinkToMethod(OfferAccess link) {
		methodManager.delete(link);
	}
	
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		return methodManager.getOfferAccess(offer, valid);
	}
	
	public List<Order> findOrders(Identity delivery, OrderStatus... status) {
		return orderManager.findOrdersByDelivery(delivery, status);
	}
	
	public List<AccessTransaction> findAccessTransactions(List<Order> orders) {
		return transactionManager.loadTransactionsForOrders(orders);
	}
	
	public List<PSPTransaction> findPSPTransactions(List<Order> orders) {
		List<AccessMethodHandler> handlers = accessModule.getMethodHandlers();
		List<PSPTransaction> transactions = new ArrayList<PSPTransaction>();
		for(AccessMethodHandler handler:handlers) {
			transactions.addAll(handler.getPSPTransactions(orders));
		}
		return transactions;
	}
	
	public List<Order> findOrders(OLATResource resource, Identity delivery, Long orderNr, Date from, Date to, OrderStatus... status) {
		return orderManager.findOrders(resource, delivery, orderNr, from, to, status);
	}
	
	public List<Order> findOrders(OLATResource resource, OrderStatus... status) {
		return orderManager.findOrdersByResource(resource, status);
	}
}
