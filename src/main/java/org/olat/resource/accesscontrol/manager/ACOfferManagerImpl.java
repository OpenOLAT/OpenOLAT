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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.manager.BasicManager;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferImpl;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOfferManagerImpl extends BasicManager implements ACOfferManager {
	
	private DB dbInstance;

	private ACOfferManagerImpl() {
		//
	}
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	@Override
	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer from ").append(OfferImpl.class.getName()).append(" offer")
			.append(" inner join offer.resource resource")
			.append(" where resource.key=:resourceKey")
			.append(" and offer.valid=").append(valid);
		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
				.append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("resourceKey", resource.getKey());
		if(atDate != null) {
			query.setTimestamp("atDate", atDate);
		}
	
		List<Offer> offers = query.list();
		return offers;
	}

	@Override
	public Offer loadOfferByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer from ").append(OfferImpl.class.getName()).append(" offer")
			.append(" where offer.key=:offerKey");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("offerKey", key);

	
		List<Offer> offers = query.list();
		if(offers.isEmpty()) return null;
		return offers.get(0);
	}

	public Set<Long> filterResourceWithOffer(Collection<Long> resourceKeys) {
		if(resourceKeys == null || resourceKeys.isEmpty()) return Collections.emptySet();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select offer.resource.key from ").append(OfferImpl.class.getName()).append(" offer")
			.append(" inner join offer.resource resource")
			.append(" where resource.key in (:resourceKeys)");
		DBQuery query = dbInstance.createQuery(sb.toString());

		Set<Long> resourceWithOffers = new HashSet<Long>();
		List<Long> keys = new ArrayList<Long>(resourceKeys);
		
		//too much in with hibernate can generate a stack overflow
		int hibernateInBatch = 500;
		int firstResult = 0;
		do {
			int toIndex = Math.min(firstResult + hibernateInBatch, keys.size());
			List<Long> inParameter = keys.subList(firstResult, toIndex);
			query.setParameterList("resourceKeys", inParameter);
			firstResult += inParameter.size();
			
			List<Long> offerKeys = query.list();
			resourceWithOffers.addAll(offerKeys);
		} while(firstResult < keys.size());

		return resourceWithOffers;
	}
	
	@Override
	public Offer createOffer(OLATResource resource, String resourceName) {
		OfferImpl offer = new OfferImpl();
		offer.setResource(resource);
		offer.setValid(true);
		if(resourceName != null && resourceName.length() > 255) {
			resourceName = resourceName.substring(0, 250);
		}
		offer.setResourceDisplayName(resourceName);
		offer.setResourceId(resource.getResourceableId());
		String resourceTypeName = resource.getResourceableTypeName();
		if(resourceTypeName != null && resourceTypeName.length() > 255) {
			resourceTypeName = resourceTypeName.substring(0, 250);
		}
		offer.setResourceTypeName(resourceTypeName);
		return offer;
	}
	
	@Override
	public void deleteOffer(Offer offer) {
		if(offer instanceof OfferImpl) {
			((OfferImpl)offer).setValid(false);
		}
		saveOffer(offer);
	}

	@Override
	public void saveOffer(Offer offer) {
		if(offer instanceof OfferImpl) {
			((OfferImpl)offer).setLastModified(new Date());
		}
		if(offer.getKey() == null) {
			dbInstance.saveObject(offer);
		} else {
			dbInstance.updateObject(offer);
		}
	}
}