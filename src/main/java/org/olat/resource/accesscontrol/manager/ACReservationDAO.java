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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.ResourceReservationImpl;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ACReservationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ResourceReservation createReservation(Identity identity, String type, Date expirationDate, ConfirmationByEnum confirmableBy, OLATResource resource) {
		ResourceReservationImpl reservation = new ResourceReservationImpl();
		reservation.setCreationDate(new Date());
		reservation.setLastModified(reservation.getCreationDate());
		reservation.setIdentity(identity);
		reservation.setResource(resource);
		reservation.setExpirationDate(expirationDate);
		reservation.setType(type);
		// Only for backwards compatibility, don't use this column
		reservation.setUserConfirmable(confirmableBy ==  ConfirmationByEnum.ADMINISTRATIVE_ROLE ? Boolean.FALSE : Boolean.TRUE);
		reservation.setConfirmableBy(confirmableBy);
		dbInstance.getCurrentEntityManager().persist(reservation);
		return reservation;
	}
	
	public ResourceReservation loadReservation(IdentityRef identity, OLATResource resource) {
		String query = """
				select reservation from resourcereservation as reservation
				inner join fetch reservation.resource rsrc
				where rsrc.key=:resourceKey and reservation.identity.key=:identityKey""";
		
		List<ResourceReservation> reservations = dbInstance.getCurrentEntityManager().createQuery(query, ResourceReservation.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		if(reservations.isEmpty()) return null;
		return reservations.get(0);
	}
	
	public List<ResourceReservation> loadReservations(SearchReservationParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select reservation from resourcereservation as reservation ");

		if(searchParams.getResources() != null && !searchParams.getResources().isEmpty()) {
			sb.append(" inner join fetch reservation.resource rsrc")
			  .and().append("rsrc.key in (:resourceKey)");
		} else {
			sb.append(" left join fetch reservation.resource rsrc");
		}
		if(searchParams.getIdentities() != null && !searchParams.getIdentities().isEmpty()) {
			sb.and().append("reservation.identity.key in :identityKeys");
		}
		
		if(searchParams.getConfirmationByUser() != null) {
			sb.and().append("reservation.confirmableBy=:confirmableBy");
		}
		
		if(searchParams.isWithConfirmationDate()) {
			sb.and().append("reservation.expirationDate is not null");
		}
		
		TypedQuery<ResourceReservation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ResourceReservation.class);
		
		if(searchParams.getResources() != null && !searchParams.getResources().isEmpty()) {
			List<Long> resourceKeys = PersistenceHelper.toKeys(searchParams.getResources());
			query.setParameter("resourceKey", resourceKeys);
		}
		if(searchParams.getIdentities() != null && !searchParams.getIdentities().isEmpty()) {
			query.setParameter("identityKeys", searchParams.getIdentities().stream().map(IdentityRef::getKey).toList());
		}
		if(searchParams.getConfirmationByUser() != null) {
			if(Boolean.TRUE.equals(searchParams.getConfirmationByUser())) {
				query.setParameter("confirmableBy", ConfirmationByEnum.PARTICIPANT);
			} else {
				query.setParameter("confirmableBy", ConfirmationByEnum.PAYMENT_PROCESSOR);
			}
		}
		
		return query.getResultList();
	}
	
	public List<ResourceReservation> loadReservations(IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadReservationsByIdentity", ResourceReservation.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public ResourceReservation loadReservation(Long reservationKey) {
		return dbInstance.getCurrentEntityManager().find(ResourceReservationImpl.class, reservationKey);
	}
	
	public List<ResourceReservation> loadReservationOlderThan(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select reservation from resourcereservation as reservation ")
		  .append(" where reservation.creationDate<:date");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ResourceReservation.class)
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
		
	}
	
	public List<ResourceReservation> loadExpiredReservation(Date defaultDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select reservation from resourcereservation as reservation ")
		  .append(" where (reservation.expirationDate is null and reservation.creationDate<:date)")
		  .append(" or (reservation.expirationDate<:nowDate)");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ResourceReservation.class)
				.setParameter("date", defaultDate, TemporalType.TIMESTAMP)
				.setParameter("nowDate", new Date(), TemporalType.TIMESTAMP)
				.getResultList();
		
	}
	
	public int countReservations(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(reservation) from resourcereservation as reservation ")
		  .append(" where reservation.resource.key=:resourceKey");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.getSingleResult();
		return count.intValue();
	}
	
	public int countReservations(OLATResource resource, String type) {
		String sb = """
				select count(reservation) from resourcereservation as reservation
				 where reservation.resource.key=:resourceKey and reservation.type=:type""";
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("type", type)
				.getSingleResult();
		return count.intValue();
	}
	
	public int deleteReservation(ResourceReservation reservation) {
		String sb = "delete from resourcereservation as reservation where reservation.key=:reservationKey";
		return dbInstance.getCurrentEntityManager().createQuery(sb)
			.setParameter("reservationKey", reservation.getKey())
			.executeUpdate();
	}
	
	public void deleteReservations(OLATResource resource) {
		String sb = "delete from resourcereservation as reservation where reservation.resource.key=:resourceKey";
		dbInstance.getCurrentEntityManager().createQuery(sb)
			.setParameter("resourceKey", resource.getKey())
			.executeUpdate();
	}

}
