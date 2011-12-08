/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.BusinessGroupAccess;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.OfferAccessImpl;
import org.olat.resource.accesscontrol.model.Price;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;

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
public class ACMethodManagerImpl extends BasicManager implements ACMethodManager, GenericEventListener {

	private DB dbInstance;
	private final AccessControlModule acModule;
	
	public ACMethodManagerImpl(CoordinatorManager coordinatorManager, AccessControlModule acModule) {
		this.acModule = acModule;
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, FrameworkStartupEventChannel.getStartupEventChannel());
	}

	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent && ((FrameworkStartedEvent) event).isEventOnThisNode()) {
			enableMethod(TokenAccessMethod.class, acModule.isTokenEnabled());
			enableMethod(FreeAccessMethod.class, acModule.isFreeEnabled());
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public void enableMethod(Class<? extends AccessMethod> type, boolean enable) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName())
			.append(" method where method.class=").append(type.getName());
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		if(methods.isEmpty() && enable) {
			try {
				dbInstance.saveObject(type.newInstance());
			} catch (InstantiationException e) {
				logError("Failed to instantiate an access method", e);
			} catch (IllegalAccessException e) {
				logError("Failed to instantiate an access method", e);
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

	@Override
	public boolean isValidMethodAvailable(OLATResource resource, Date atDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(access.method) from ").append(OfferAccessImpl.class.getName()).append(" access ")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource oResource")
			.append(" where access.valid=true")
			.append(" and offer.valid=true")
			.append(" and oResource.key=:resourceKey");
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("resourceKey", resource.getKey());
		if(atDate != null) {
			query.setTimestamp("atDate", atDate);
		}

		Number methods = (Number)query.uniqueResult();
		return methods.intValue() > 0;
	}

	@Override
	public List<AccessMethod> getAvailableMethods(Identity identity, Roles roles) {
		StringBuilder sb = new StringBuilder();
		sb.append("select method from ").append(AbstractAccessMethod.class.getName()).append(" method")
			.append(" where method.valid=true and method.enabled=true");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		if(identity != null) {
			//query.setLong("identityKey", identity.getKey());
		}
	
		List<AccessMethod> methods = query.list();
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
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		return methods;
	}

	@Override
	public List<OfferAccess> getOfferAccess(Offer offer, boolean valid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select access from ").append(OfferAccessImpl.class.getName()).append(" access")
			.append(" where access.offer=:offer")
			.append(" and access.valid=").append(valid);

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setEntity("offer", offer);
		
		List<OfferAccess> methods = query.list();
		return methods;
	}
	
	@Override
	public List<OfferAccess> getOfferAccess(Collection<Offer> offers, boolean valid) {
		if(offers == null || offers.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select access from ").append(OfferAccessImpl.class.getName()).append(" access")
			.append(" where access.offer in (:offers)")
			.append(" and access.valid=").append(valid);
		

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setParameterList("offers", offers);
		
		List<OfferAccess> methods = query.list();
		return methods;
	}
	
	@Override
	public List<OfferAccess> getOfferAccessByResource(Collection<Long> resourceKeys, boolean valid, Date atDate) {
		if(resourceKeys == null || resourceKeys.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select access from ").append(OfferAccessImpl.class.getName()).append(" access")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource resource")
			.append(" where resource.key in (:resourceKeys)")
			.append(" and access.valid=").append(valid)
			.append(" and offer.valid=").append(valid);
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setParameterList("resourceKeys", resourceKeys);
		if(atDate != null) {
			query.setTimestamp("atDate", atDate);
		}
		
		List<OfferAccess> methods = query.list();
		return methods;
	}
	
	@Override
	public List<BusinessGroupAccess> getAccessMethodForBusinessGroup(boolean valid, Date atDate) {

		StringBuilder sb = new StringBuilder();
		sb.append("select access.method, group.key, offer.price from ").append(OfferAccessImpl.class.getName()).append(" access, ")
			.append(BusinessGroupImpl.class.getName()).append(" group, ")
			.append(OLATResourceImpl.class.getName()).append(" gResource")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource oResource")
			.append(" where access.valid=").append(valid)
			.append(" and offer.valid=").append(valid)
			.append(" and group.key=gResource.resId and gResource.resName='BusinessGroup' and oResource.key=gResource.key");
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		DBQuery query = dbInstance.createQuery(sb.toString());
		if(atDate != null) {
			query.setTimestamp("atDate", atDate);
		}
		
		List<Object[]> rawResults = query.list();
		Map<Long,List<PriceMethodBundle>> rawResultsMap = new HashMap<Long,List<PriceMethodBundle>>();
		for(Object[] rawResult:rawResults) {
			AccessMethod method = (AccessMethod)rawResult[0];
			Long groupKey = (Long)rawResult[1];
			Price price = (Price)rawResult[2];
			if(!rawResultsMap.containsKey(groupKey)) {
				rawResultsMap.put(groupKey, new ArrayList<PriceMethodBundle>(3));
			}
			rawResultsMap.get(groupKey).add(new PriceMethodBundle(price, method));	
		}
		
		List<BusinessGroup> groups = BusinessGroupManagerImpl.getInstance().findBusinessGroups(rawResultsMap.keySet());
		List<BusinessGroupAccess> groupAccess = new ArrayList<BusinessGroupAccess>();
		for(BusinessGroup group:groups) {
			List<PriceMethodBundle> methods = rawResultsMap.get(group.getKey());
			if(methods != null && !methods.isEmpty()) {
				groupAccess.add(new BusinessGroupAccess(group, methods));
			}
		}
		return groupAccess;
	}
	
	@Override
	public List<OLATResourceAccess> getAccessMethodForResources(Collection<Long> resourceKeys, boolean valid, Date atDate) {
		if(resourceKeys == null || resourceKeys.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select access.method, resource, offer.price from ").append(OfferAccessImpl.class.getName()).append(" access, ")
			.append(OLATResourceImpl.class.getName()).append(" resource")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource oResource")
			.append(" where access.valid=").append(valid)
			.append(" and offer.valid=").append(valid)
			.append(" and resource.key in (:resourceKeys) and oResource.key=resource.key");
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		DBQuery query = dbInstance.createQuery(sb.toString());
		if(atDate != null) {
			query.setTimestamp("atDate", atDate);
		}
		query.setParameterList("resourceKeys", resourceKeys);
		
		List<Object[]> rawResults = query.list();
		Map<Long,OLATResourceAccess> rawResultsMap = new HashMap<Long,OLATResourceAccess>();
		for(Object[] rawResult:rawResults) {
			AccessMethod method = (AccessMethod)rawResult[0];
			OLATResource resource = (OLATResource)rawResult[1];
			Price price = (Price)rawResult[2];
			if(rawResultsMap.containsKey(resource.getKey())) {
				rawResultsMap.get(resource.getKey()).addBundle(price, method);
			} else {
				rawResultsMap.put(resource.getKey(), new OLATResourceAccess(resource, price, method));
			}
		}
		
		List<OLATResourceAccess> groupAccess = new ArrayList<OLATResourceAccess>(rawResultsMap.values());
		return groupAccess;
	}

	@Override
	public OfferAccess createOfferAccess(Offer offer, AccessMethod method) {
		OfferAccessImpl access = new OfferAccessImpl();
		access.setOffer(offer);
		access.setMethod(method);
		access.setValid(true);
		return access;
	}
	
	@Override
	public void save(OfferAccess link) {
		if(link.getKey() == null) {
			dbInstance.saveObject(link);
		} else {
			dbInstance.updateObject(link);
		}
	}
	
	@Override
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
		sb.append("select method from ").append(AbstractAccessMethod.class.getName())
			.append(" method where method.class=").append(TokenAccessMethod.class.getName());
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		if(methods.isEmpty() && enable) {
			dbInstance.saveObject(new TokenAccessMethod());
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
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		List<AccessMethod> methods = query.list();
		if(methods.isEmpty() && enable) {
			dbInstance.saveObject(new FreeAccessMethod());
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