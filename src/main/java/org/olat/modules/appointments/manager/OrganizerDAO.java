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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.appointments.model.OrganizerImpl;
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
class OrganizerDAO {
	
	@Autowired
	private DB dbInstance;
	
	Organizer createOrganizer(Topic topic, Identity identity) {
		OrganizerImpl organizer = new OrganizerImpl();
		organizer.setCreationDate(new Date());
		organizer.setLastModified(organizer.getCreationDate());
		organizer.setTopic(topic);
		organizer.setIdentity(identity);
		
		dbInstance.getCurrentEntityManager().persist(organizer);
		return organizer;
	}
	
	void deleteOrganizers(Collection<Organizer> organizers) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentorganizer organizer");
		sb.and().append(" organizer.key in (:organizerKeys)");
		
		List<Long> organizerKeys = organizers.stream().map(Organizer::getKey).collect(Collectors.toList());
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("organizerKeys", organizerKeys)
				.executeUpdate();
	}
	
	void delete(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentorganizer organizer");
		sb.and().append(" organizer.topic.key = :topicKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("topicKey", topic.getKey())
				.executeUpdate();
	}

	void delete(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmentorganizer organizer");
		sb.append(" where exists (select 1");
		sb.append("                 from appointmenttopic topic");
		sb.append("                where topic.key = organizer.topic.key");
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

	Organizer loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select organizer");
		sb.append("  from appointmentorganizer organizer");
		sb.and().append(" organizer.key = :organizerKey");
		
		List<Organizer> organizers = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organizer.class)
				.setParameter("organizerKey", key)
				.getResultList();
		return organizers.isEmpty() ? null : organizers.get(0);
	}

	List<Organizer> loadOrganizers(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select organizer");
		sb.append("  from appointmentorganizer organizer");
		sb.and().append(" organizer.topic.key = :topicKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organizer.class)
				.setParameter("topicKey", topic.getKey())
				.getResultList();
	}

	List<Organizer> loadOrganizers(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select organizer");
		sb.append("  from appointmentorganizer organizer");
		sb.append("      join organizer.topic topic");
		sb.and().append(" topic.entry.key = :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append(" topic.subIdent = :subIdent");
		}
		
		TypedQuery<Organizer> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organizer.class)
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}

}
