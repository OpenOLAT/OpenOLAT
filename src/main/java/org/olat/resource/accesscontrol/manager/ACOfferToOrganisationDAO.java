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
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferRef;
import org.olat.resource.accesscontrol.OfferToOrganisation;
import org.olat.resource.accesscontrol.model.OfferToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ACOfferToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public OfferToOrganisation createRelation(Offer offer, Organisation organisation) {
		OfferToOrganisationImpl relation = new OfferToOrganisationImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setOffer(offer);
		relation.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public List<OrganisationRef> getOrganisationReferences(OfferRef re) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select org.key from offertoorganisation as offerToOrganisation")
		  .append(" inner join offerToOrganisation.organisation org")
		  .append(" where offerToOrganisation.offer.key=:offerKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("offerKey", re.getKey())
				.getResultList();
		return keys.stream().map(OrganisationRefImpl::new)
				.collect(Collectors.toList());
	}
	
	public List<OfferToOrganisation> loadRelations(OfferRef offer, OrganisationRef organisation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select offerToOrganisation from offertoorganisation as offerToOrganisation");
		sb.append(" inner join fetch offerToOrganisation.offer offer");
		sb.append(" inner join fetch offerToOrganisation.organisation org");
		if (offer != null) {
			sb.and().append("offer.key=:offerKey");
		}
		if (organisation != null) {
			sb.and().append("org.key=:organisationKey");
		}
		
		TypedQuery<OfferToOrganisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferToOrganisation.class);
		if (offer != null) {
			query.setParameter("offerKey", offer.getKey());
		}
		if (organisation != null) {
			query.setParameter("organisationKey", organisation.getKey());
		}
		
		return query.getResultList();
	}
	
	public List<OfferToOrganisation> loadRelations(Collection<? extends OfferRef> offers) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select offerToOrganisation from offertoorganisation as offerToOrganisation");
		sb.append(" inner join fetch offerToOrganisation.organisation org");
		sb.and().append("offerToOrganisation.offer.key in :offerKeys");
		
		List<Long> offerKeys = offers.stream().map(OfferRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferToOrganisation.class)
				.setParameter("offerKeys", offerKeys)
				.getResultList();
	}
	
	public Map<Long, List<Long>> getOfferKeyToOrganisations(Collection<? extends OfferRef> offers) {
		return loadRelations(offers).stream()
				.collect(Collectors.groupingBy(
						oto -> oto.getOffer().getKey(),
						Collectors.mapping(oto -> oto.getOrganisation().getKey(), Collectors.toList())));
	}
	
	public List<Organisation> loadOrganisations(OfferRef offer) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select offerToOrganisation.organisation from offertoorganisation as offerToOrganisation");
		if (offer != null) {
			sb.and().append("offerToOrganisation.offer.key=:offerKey");
		}
		
		TypedQuery<Organisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class);
		if (offer != null) {
			query.setParameter("offerKey", offer.getKey());
		}
		
		return query.getResultList();
	}
	
	public void delete(OfferRef re, OrganisationRef organisation) {
		List<OfferToOrganisation> relations = loadRelations(re, organisation);
		for(OfferToOrganisation relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public void delete(OfferToOrganisation relation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("delete from offertoorganisation as offerToOrganisation");
		sb.and().append("offerToOrganisation.offer.key=:offerKey");
		sb.and().append("offerToOrganisation.organisation.key=:organisationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("offerKey", relation.getOffer().getKey())
				.setParameter("organisationKey", relation.getOrganisation().getKey())
				.executeUpdate();
	}
	
	public int delete(OfferRef offer) {
		String query = "delete from offertoorganisation as offerToOrganisation where offerToOrganisation.offer.key=:offerKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("offerKey", offer.getKey())
				.executeUpdate();
	}

}
