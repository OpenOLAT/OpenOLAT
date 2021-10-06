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
package org.olat.modules.appointments.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.appointments.model.ParticipationImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 13 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class ParticipationDAO {
	
	@Autowired
	private DB dbInstance;
	
	Participation createParticipation(Appointment appointment, Identity identity, Identity createdBy) {
		ParticipationImpl participation = new ParticipationImpl();
		participation.setCreationDate(new Date());
		participation.setLastModified(participation.getCreationDate());
		participation.setAppointment(appointment);
		participation.setIdentity(identity);
		participation.setCreatedBy(createdBy);
		
		dbInstance.getCurrentEntityManager().persist(participation);
		return participation;
	}
	
	Participation updateParticipation(Participation participation) {
		if (participation instanceof ParticipationImpl) {
			ParticipationImpl impl = (ParticipationImpl)participation;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(participation);
		return participation;
	}

	void delete(Participation participation) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentparticipation participation");
		sb.and().append(" participation.key = :participationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("participationKey", participation.getKey())
				.executeUpdate();
	}
	
	public void delete(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentparticipation participation");
		sb.append(" where exists (select 1");
		sb.append("                 from appointment appointment");
		sb.append("                where participation.appointment.key = appointment.key");
		sb.append("                  and appointment.topic.key = :topicKey)");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("topicKey", topic.getKey())
				.executeUpdate();
	}
	
	void delete(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentparticipation participation");
		sb.append(" where exists (select 1");
		sb.append("                 from appointment appointment");
		sb.append("                    , appointmenttopic topic");
		sb.append("                where participation.appointment.key = appointment.key");
		sb.append("                  and topic.key = appointment.topic.key");
		sb.append("                  and topic.entry.key =  :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.append("              and topic.subIdent =  :subIdent");
		}
		sb.append("               )");
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		query.executeUpdate();
	}

	Participation loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select participation");
		sb.append("  from appointmentparticipation participation");
		sb.and().append(" participation.key = :participationKey");
		
		List<Participation> participations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Participation.class)
				.setParameter("participationKey", key)
				.getResultList();
		return participations.isEmpty() ? null : participations.get(0);
	}

	Long loadParticipationCount(ParticipationSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(participation)");
		appendQuery(sb, params);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, params);
		
		return query.getSingleResult();
	}
	
	List<Participation> loadParticipations(ParticipationSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select participation");
		appendQuery(sb, params);
		
		TypedQuery<Participation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Participation.class);
		addParameters(query, params);
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ParticipationSearchParams params) {
		sb.append("  from appointmentparticipation participation");
		if (isJoinIdentites(params)) {
			sb.append("      join").append(" fetch", params.isFetchIdentities() || params.isFetchUser()).append(" participation.identity ident");
		}
		if (isJoinUser(params)) {
			sb.append("      join").append(" fetch", params.isFetchUser()).append(" ident.user user");
		}
		if (isJoinAppointments(params)) {
			sb.append("      join").append(" fetch", params.isFetchAppointments() || params.isFetchTopics()).append(" participation.appointment appointment");
		}
		if (isJoinTopic(params)) {
			sb.append("      join").append(" fetch", params.isFetchTopics()).append(" appointment.topic topic");
		}
		if (isJoinOrganizer(params)) {
			sb.append("      join appointmentorganizer organizer");
			sb.append("        on organizer.topic.key = topic.key");
		}
		if (params.getEntry() != null ) {
			sb.and().append("topic.entry.key = :entryKey");
		}
		if (StringHelper.containsNonWhitespace(params.getSubIdent())) {
			sb.and().append("topic.subIdent = :subIdent");
		}
		if (params.getTopicKeys() != null) {
			sb.and().append("appointment.topic.key in (:topicKeys)");
		}
		if (params.getIdentityKeys() != null) {
			sb.and().append("participation.identity.key in (:identityKeys)");
		}
		if (params.getCreatedAfter() != null) {
			sb.and().append("participation.creationDate >= :createdAfter");
		}
		if (params.getParticipationKeys() != null) {
			sb.and().append("participation.key in (:participationKeys)");
		}
		if (params.getAppointmentKeys() != null) {
			sb.and().append("participation.appointment.key in (:appointmentKeys)");
		}
		if (params.getStartAfter() != null) {
			sb.and().append("appointment.start >= :startAfter");
		}
		if (params.getStatus() != null) {
			sb.and().append("appointment.status = :status");
		}
		if (params.getStatusModifiedAfter() != null) {
			sb.and().append("appointment.statusModified >= :statusModifiedAfter");
		}
		if (params.getOrganizer() != null) {
			sb.and().append("organizer.identity.key = :organizerIdentityKey");
		}
	}

	private void addParameters(TypedQuery<?> query, ParticipationSearchParams params) {
		if (params.getEntry() != null ) {
			query.setParameter("entryKey", params.getEntry().getKey());
		}
		if (StringHelper.containsNonWhitespace(params.getSubIdent())) {
			query.setParameter("subIdent", params.getSubIdent());
		}
		if (params.getTopicKeys() != null) {
				query.setParameter("topicKeys", params.getTopicKeys());
		}
		if (params.getIdentityKeys() != null) {
			query.setParameter("identityKeys", params.getIdentityKeys());
		}
		if (params.getCreatedAfter() != null) {
			query.setParameter("createdAfter", params.getCreatedAfter());
		}
		if (params.getParticipationKeys() != null) {
			query.setParameter("participationKeys", params.getParticipationKeys());
		}
		if (params.getAppointmentKeys() != null) {
			query.setParameter("appointmentKeys", params.getAppointmentKeys());
		}
		if (params.getStartAfter() != null) {
			query.setParameter("startAfter", params.getStartAfter());
		}
		if (params.getStatus() != null) {
			query.setParameter("status", params.getStatus());
		}
		if (params.getStatusModifiedAfter() != null) {
			query.setParameter("statusModifiedAfter", params.getStatusModifiedAfter());
		}
		if (params.getOrganizer() != null) {
			query.setParameter("organizerIdentityKey", params.getOrganizer().getKey());
		}
	}

	private boolean isJoinIdentites(ParticipationSearchParams params) {
		return params.isFetchIdentities() || isJoinUser(params);
	}
	
	private boolean isJoinUser(ParticipationSearchParams params) {
		return params.isFetchUser();
	}

	private boolean isJoinAppointments(ParticipationSearchParams params) {
		return params.isFetchAppointments()
				|| (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty())
				|| params.getStartAfter() != null
				|| params.getStatus() != null
				|| params.getStatusModifiedAfter() != null
				|| isJoinTopic(params);
	}

	private boolean isJoinTopic(ParticipationSearchParams params) {
		return  params.isFetchTopics() 
				|| params.getEntry() != null
				|| params.getSubIdent() != null
				|| isJoinOrganizer(params);
	}

	private boolean isJoinOrganizer(ParticipationSearchParams params) {
		return params.getOrganizer() != null;
	}

}
