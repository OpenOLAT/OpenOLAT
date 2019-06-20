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

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACOfferDAO {

	@Autowired
	private DB dbInstance;

	public List<Offer> findOfferByResource(OLATResource resource, boolean valid, Date atDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer, access.method from acofferaccess access ")
				.append(" inner join access.offer offer")
				.append(" left join offer.resource resource")
				.append(" where resource.key=:resourceKey")
				.append(" and offer.valid=").append(valid);

		if(atDate != null) {
			sb.append(" and (offer.validFrom is null or offer.validFrom<=:atDate)")
			  .append(" and (offer.validTo is null or offer.validTo>=:atDate)");
		}

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resourceKey", resource.getKey());
		if(atDate != null) {
			query.setParameter("atDate", atDate, TemporalType.TIMESTAMP);
		}

		List<Object[]> loadedObjects = query.getResultList();
		List<Offer> offers = new ArrayList<>();
		for(Object[] objects:loadedObjects) {
			Offer offer = (Offer)objects[0];
			AccessMethod method = (AccessMethod)objects[1];
			if(method.isVisibleInGui()) {
				offers.add(offer);
			}
		}

		return offers;
	}

	public Offer loadOfferByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer from acoffer offer")
		  .append(" left join fetch offer.resource resource")
		  .append(" where offer.key=:offerKey");

		List<Offer> offers = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Offer.class)
				.setParameter("offerKey", key)
				.getResultList();
		if(offers.isEmpty()) return null;
		return offers.get(0);
	}

	public Set<Long> filterResourceWithOffer(Collection<Long> resourceKeys) {
		if(resourceKeys == null || resourceKeys.isEmpty()) return Collections.emptySet();

		StringBuilder sb = new StringBuilder();
		sb.append("select offer.resource.key from acoffer offer")
		  .append(" inner join offer.resource resource")
		  .append(" where resource.key in (:resourceKeys)");
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);

		Set<Long> resourceWithOffers = new HashSet<>();
		List<Long> keys = new ArrayList<>(resourceKeys);

		//too much in with hibernate can generate a stack overflow
		int hibernateInBatch = 500;
		int firstResult = 0;
		do {
			int toIndex = Math.min(firstResult + hibernateInBatch, keys.size());
			List<Long> inParameter = keys.subList(firstResult, toIndex);
			query.setParameter("resourceKeys", inParameter);
			firstResult += inParameter.size();

			List<Long> offerKeys = query.getResultList();
			resourceWithOffers.addAll(offerKeys);
		} while(firstResult < keys.size());

		return resourceWithOffers;
	}

	public Offer createOffer(OLATResource resource, String resourceName) {
		OfferImpl offer = new OfferImpl();
		Date now = new Date();
		offer.setCreationDate(now);
		offer.setLastModified(now);
		offer.setResource(resource);
		offer.setValid(true);
		offer.setConfirmationEmail(false);
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

	public Offer deleteOffer(Offer offer) {
		offer = loadOfferByKey(offer.getKey());
		if(offer instanceof OfferImpl) {
			((OfferImpl)offer).setValid(false);
		}
		return saveOffer(offer);
	}

	public Offer saveOffer(Offer offer) {
		if(offer instanceof OfferImpl) {
			((OfferImpl)offer).setLastModified(new Date());
		}
		if(offer.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(offer);
		} else {
			offer = dbInstance.getCurrentEntityManager().merge(offer);
		}
		return offer;
	}
}