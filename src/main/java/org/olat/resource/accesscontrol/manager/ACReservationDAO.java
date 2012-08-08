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

import javax.persistence.EntityManager;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.ResourceReservation;
import org.olat.resource.accesscontrol.model.ResourceReservationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("acReservationDAO")
public class ACReservationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ResourceReservation createReservation(Identity identity, OLATResource resource) {
		ResourceReservationImpl reservation = new ResourceReservationImpl();
		reservation.setIdentity(identity);
		reservation.setResource(resource);
		reservation.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().persist(reservation);
		return reservation;
	}
	
	public ResourceReservation loadReservation(Identity identity, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select reservation from ").append(ResourceReservationImpl.class.getName()).append(" as reservation ")
		  .append(" where reservation.resource.key=:resourceKey and reservation.identity.key=:identityKey");
		
		List<ResourceReservation> reservations = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ResourceReservation.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		if(reservations.isEmpty()) return null;
		return reservations.get(0);
	}
	
	public ResourceReservation loadReservation(Long reservationKey) {
		return dbInstance.getCurrentEntityManager().find(ResourceReservationImpl.class, reservationKey);
	}
	
	public List<ResourceReservation> loadReservationOlderThan(Date date) {
		StringBuilder sb = new StringBuilder();
		sb.append("select reservation from ").append(ResourceReservationImpl.class.getName()).append(" as reservation ")
		  .append(" where reservation.creationDate<:date");
		
		List<ResourceReservation> reservations = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ResourceReservation.class)
				.setParameter("date", date)
				.getResultList();
		return reservations;
		
	}
	
	public int countReservations(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(reservation) from ").append(ResourceReservationImpl.class.getName()).append(" as reservation ")
		  .append(" where reservation.resource.key=:resourceKey");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.getSingleResult();
		return count.intValue();
	}
	
	public void deleteReservation(ResourceReservation reservation) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		ResourceReservation reloaded = em.getReference(ResourceReservationImpl.class, reservation.getKey());
		em.remove(reloaded);
	}

}
