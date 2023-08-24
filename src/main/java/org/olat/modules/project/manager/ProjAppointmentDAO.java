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
package org.olat.modules.project.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.jgroups.util.UUID;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.model.ProjAppointmentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 13 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjAppointmentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjAppointment create(ProjArtefact artefact, Date startDate, Date dateDate) {
		ProjAppointmentImpl appointment = new ProjAppointmentImpl();
		appointment.setCreationDate(new Date());
		appointment.setLastModified(appointment.getCreationDate());
		appointment.setIdentifier(UUID.randomUUID().toString());
		appointment.setEventId(UUID.randomUUID().toString());
		appointment.setStartDate(startDate);
		appointment.setEndDate(dateDate);
		appointment.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(appointment);
		return appointment;
	}
	
	public ProjAppointment save(ProjAppointment appointment) {
		if (appointment instanceof ProjAppointmentImpl) {
			ProjAppointmentImpl impl = (ProjAppointmentImpl)appointment;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(appointment);
		}
		return appointment;
	}

	public void delete(ProjAppointmentRef appointment) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projappointment appointment");
		sb.and().append("appointment.key = :appointmentKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("appointmentKey", appointment.getKey())
				.executeUpdate();
	}
	
	public long loadAppointmentsCount(ProjAppointmentSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projappointment appointment");
		sb.append("       inner join appointment.artefact artefact");
		appendQuery(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjAppointment> loadAppointments(ProjAppointmentSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select appointment");
		sb.append("  from projappointment appointment");
		sb.append("       inner join fetch appointment.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch artefact.creator creator");
		sb.append("       inner join fetch artefact.contentModifiedBy modifier");
		appendQuery(searchParams, sb);
		
		TypedQuery<ProjAppointment> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjAppointment.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(ProjAppointmentSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getAppointmentKeys() != null && !searchParams.getAppointmentKeys().isEmpty()) {
			sb.and().append("appointment.key in :appointmentKeys");
		}
		if (searchParams.getIdentifiers() != null && !searchParams.getIdentifiers().isEmpty()) {
			sb.and().append("appointment.identifier in :identifiers");
		}
		if (searchParams.getEventIds() != null && !searchParams.getEventIds().isEmpty()) {
			sb.and().append("appointment.eventId in :eventIds");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
		if (searchParams.getRecurrenceIdAvailable() != null) {
			sb.and().append("appointment.recurrenceId is ").append("not ", searchParams.getRecurrenceIdAvailable()).append("null");
		}
		if (searchParams.getCreatedAfter() != null) {
			sb.and().append("appointment.creationDate >= :createdAfter");
		}
		if (searchParams.getDatesNull() != null) {
			if (searchParams.getDatesNull()) {
				sb.and().append("(appointment.startDate is null or appointment.endDate is null)");
			} else {
				sb.and().append("appointment.startDate is not null and appointment.endDate is not null");
			}
		}
	}

	private void addParameters(TypedQuery<?> query, ProjAppointmentSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getAppointmentKeys() != null && !searchParams.getAppointmentKeys().isEmpty()) {
			query.setParameter("appointmentKeys", searchParams.getAppointmentKeys());
		}
		if (searchParams.getIdentifiers() != null && !searchParams.getIdentifiers().isEmpty()) {
			query.setParameter("identifiers", searchParams.getIdentifiers());
		}
		if (searchParams.getEventIds() != null && !searchParams.getEventIds().isEmpty()) {
			query.setParameter("eventIds", searchParams.getEventIds());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
		if (searchParams.getCreatedAfter() != null) {
			query.setParameter("createdAfter", searchParams.getCreatedAfter());
		}
	}

}
