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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.OfferAccessImpl;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * This class manages the methods available to access the resource.
 * As standard "static" (static as singleton), there is Token  and Free
 * based access.
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACMethodDAO implements GenericEventListener {
	
	private static final OLog log = Tracing.createLoggerFor(ACMethodDAO.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private AccessControlModule acModule;
	
	@Autowired
	public ACMethodDAO(CoordinatorManager coordinatorManager) {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, FrameworkStartupEventChannel.getStartupEventChannel());
	}

	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent && ((FrameworkStartedEvent) event).isEventOnThisNode()) {
			enableMethod(TokenAccessMethod.class, acModule.isTokenEnabled());
			enableMethod(FreeAccessMethod.class, acModule.isFreeEnabled());
			enableMethod(PaypalAccessMethod.class, acModule.isPaypalEnabled());
			dbInstance.commitAndCloseSession();
		}
	}

	public void enableMethod(Class<? extends AccessMethod> type, boolean enable) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(type.getName()).append(" method");
		
		List<AccessMethod> methods = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AccessMethod.class)
				.getResultList();
		if(methods.isEmpty() && enable) {
			try {
				AccessMethod method = type.newInstance();
				Date now = new Date();
				((AbstractAccessMethod)method).setCreationDate(now);
				((AbstractAccessMethod)method).setLastModified(now);
				dbInstance.saveObject(method);
			} catch (InstantiationException e) {
				log.error("Failed to instantiate an access method", e);
			} catch (IllegalAccessException e) {
				log.error("Failed to instantiate an access method", e);
			}
		} else {
			for(AccessMethod method:methods) {
				if(method.isEnabled() != enable) {
					((AbstractAccessMethod)method).setEnabled(enable);
					dbInstance.updateObject(method);
				}
			}
		}
	}

	public boolean isValidMethodAvailable(OLATResource resource, Date atDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(access.method) from acofferaccess access ")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource oResource")
			.append(" where access.valid=true")
			.append(" and offer.valid=true")
			.append(" and oResource.key=:resourceKey");
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
			  .append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		TypedQuery<Number> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class);
		query.setParameter("resourceKey", resource.getKey());
		if(atDate != null) {
			query.setParameter("atDate", atDate, TemporalType.TIMESTAMP);
		}

		Number methods = query.getSingleResult();
		return methods.intValue() > 0;
	}
	
	public List<AccessMethod> getAllMethods() {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AccessMethod.class)
				.getResultList();
	}

	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method")
			.append(" where method.valid=true and method.enabled=true");
		
		TypedQuery<AccessMethod> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), AccessMethod.class);
		if(identity != null) {
			//query.setLong("identityKey", identity.getKey());
		}
	
		List<AccessMethod> methods = query.getResultList();
		List<AccessMethod> allowedMethods = new ArrayList<AccessMethod>();
		for(AccessMethod method:methods) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
			AccessMethodSecurityCallback secCallback = handler.getSecurityCallback(identity, roles);
			if(secCallback.canUse()) {
				allowedMethods.add(method);
			}
		}
		return allowedMethods;
	}

	public List<AccessMethod> getAvailableMethodsByType(Class<? extends AccessMethod> type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method")
			.append(" where method.valid=true")
			.append(" and method.class=").append(type.getName());
		
		TypedQuery<AccessMethod> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), AccessMethod.class);
		List<AccessMethod> methods = query.getResultList();
		return methods;
	}

	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select access from acofferaccess access")
		  .append(" inner join fetch access.offer offer")
		  .append(" inner join fetch access.method method")
		  .append(" inner join fetch offer.resource resource")
		  .append(" where offer.key=:offerKey")
		  .append(" and access.valid=").append(valid);

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferAccess.class)
				.setParameter("offerKey", offer.getKey())
				.getResultList();
	}
	
	public List<OfferAccess> getOfferAccess(Collection<Offer> offers, boolean valid) {
		if(offers == null || offers.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select access from acofferaccess access")
		  .append(" inner join fetch access.offer offer")
		  .append(" inner join fetch access.method method")
		  .append(" inner join fetch offer.resource resource")
		  .append(" where offer.key in (:offersKey)")
		  .append(" and access.valid=:valid");
		
		List<Long> offersKey = PersistenceHelper.toKeys(offers);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferAccess.class)
				.setParameter("offersKey", offersKey)
				.setParameter("valid", valid)
				.getResultList();
	}
	
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys,
			String resourceType, String excludedResourceType, boolean valid, Date atDate) {

		final int maxResourcesEntries = 250;//quicker to filter in java, numerous keys in "in" are slow
		
		StringBuilder sb = new StringBuilder();
		sb.append("select access.method, resource, offer.price from acofferaccess access, ")
			.append(OLATResourceImpl.class.getName()).append(" resource")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource oResource")
			.append(" where access.valid=").append(valid).append(" and offer.valid=").append(valid)
			.append(" and resource.key=oResource.key");
		if(resourceKeys != null && !resourceKeys.isEmpty()) {
			if(resourceKeys.size() < maxResourcesEntries) {
				sb.append(" and resource.key in (:resourceKeys) ");
			}
			sb.append(" and oResource.key=resource.key");
		}
		if(StringHelper.containsNonWhitespace(resourceType)) {
			sb.append(" and oResource.resName =:resourceType ");
		}
		if(StringHelper.containsNonWhitespace(excludedResourceType)) {
			sb.append(" and not(oResource.resName=:excludedResourceType)");
		}
			
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class);
		if(atDate != null) {
			query.setParameter("atDate", atDate, TemporalType.TIMESTAMP);
		}

		Set<Long> resourceKeysSet = null;
		if(resourceKeys != null && !resourceKeys.isEmpty()) {
			if(resourceKeys.size() < maxResourcesEntries) {
				query.setParameter("resourceKeys", resourceKeys);
			} else {
				resourceKeysSet = new HashSet<Long>(resourceKeys);
			}
		}
		if(StringHelper.containsNonWhitespace(resourceType)) {
			query.setParameter("resourceType", resourceType);
		}
		if(StringHelper.containsNonWhitespace(excludedResourceType)) {
			query.setParameter("excludedResourceType", excludedResourceType);
		}
		
		List<Object[]> rawResults = query.getResultList();
		Map<Long,OLATResourceAccess> rawResultsMap = new HashMap<Long,OLATResourceAccess>();
		for(Object[] rawResult:rawResults) {
			AccessMethod method = (AccessMethod)rawResult[0];
			OLATResource resource = (OLATResource)rawResult[1];
			if(resourceKeysSet != null && !resourceKeysSet.contains(resource.getKey())) {
				continue;
			}
			
			Price price = (Price)rawResult[2];
			if(rawResultsMap.containsKey(resource.getKey())) {
				rawResultsMap.get(resource.getKey()).addBundle(price, method);
			} else {
				rawResultsMap.put(resource.getKey(), new OLATResourceAccess(resource, price, method));
			}
		}
		
		return new ArrayList<OLATResourceAccess>(rawResultsMap.values());
	}

	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		OfferAccessImpl access = new OfferAccessImpl();
		access.setCreationDate(new Date());
		access.setOffer(offer);
		access.setMethod(method);
		access.setValid(true);
		return access;
	}
	
	public OfferAccess save(OfferAccess link) {
		if(link.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(link);
		} else {
			link = dbInstance.getCurrentEntityManager().merge(link);
		}
		return link;
	}
	
	public void delete(OfferAccess link) {
		OfferAccessImpl access = (OfferAccessImpl)link;
		access.setValid(false);
	
		if(link.getKey() == null) return;
		dbInstance.updateObject(access);
	}
	
	/**
	 * Activate the token method if not already configured.
	 */
	protected void activateTokenMethod(boolean enable) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from actokenmethod method");
		
		List<AccessMethod> methods = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AccessMethod.class)
				.getResultList();
		if(methods.isEmpty() && enable) {
			TokenAccessMethod method = new TokenAccessMethod();
			method.setCreationDate(new Date());
			method.setLastModified(method.getCreationDate());
			dbInstance.saveObject(method);
		} else {
			for(AccessMethod method:methods) {
				if(method.isEnabled() != enable) {
					((AbstractAccessMethod)method).setEnabled(enable);
					dbInstance.updateObject(method);
				}
			}
		}
	}
	
	protected void activateFreeMethod(boolean enable) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName())
			.append(" method where method.class=").append(FreeAccessMethod.class.getName());
		
		TypedQuery<AccessMethod> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), AccessMethod.class);
		List<AccessMethod> methods = query.getResultList();
		if(methods.isEmpty() && enable) {
			FreeAccessMethod method = new FreeAccessMethod();
			method.setCreationDate(new Date());
			method.setLastModified(method.getCreationDate());
			dbInstance.saveObject(method);
		} else {
			for(AccessMethod method:methods) {
				if(method.isEnabled() != enable) {
					((AbstractAccessMethod)method).setEnabled(enable);
					dbInstance.updateObject(method);
				}
			}
		}
	}
}