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

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.appointments.model.TopicImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 11 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class TopicDAO {
	
	@Autowired
	private DB dbInstance;
	
	Topic createTopic(RepositoryEntry entry, String subIdent) {
		TopicImpl topic = new TopicImpl();
		topic.setCreationDate(new Date());
		topic.setLastModified(topic.getCreationDate());
		topic.setType(Type.enrollment);
		topic.setMultiParticipation(true);
		topic.setAutoConfirmation(false);
		topic.setParticipationVisible(true);
		topic.setEntry(entry);
		topic.setSubIdent(subIdent);
		
		dbInstance.getCurrentEntityManager().persist(topic);
		return topic;
	}
	
	Topic loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topic");
		sb.append("  from appointmenttopic topic");
		sb.and().append(" topic.key = :topicKey");
		
		List<Topic> topics = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Topic.class)
				.setParameter("topicKey", key)
				.getResultList();
		return topics.isEmpty() ? null : topics.get(0);
	}
	
	Topic updateTopic(Topic topic) {
		if (topic instanceof TopicImpl) {
			TopicImpl impl = (TopicImpl)topic;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(topic);
		return topic;
	}
	
	Topic setGroup(Topic topic, Group group) {
		if (topic instanceof TopicImpl) {
			TopicImpl topicImpl = (TopicImpl) topic;
			topicImpl.setGroup(group);
			return updateTopic(topicImpl);
		}
		return topic;
	}
	
	void delete(TopicRef topic) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopic topic");
		sb.and().append(" topic.key = :topicKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("topicKey", topic.getKey())
				.executeUpdate();
	}
	
	void delete(RepositoryEntry entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete");
		sb.append("  from appointmenttopic topic");
		sb.and().append(" topic.entry.key = :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("topic.subIdent = :subIdent");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		query.executeUpdate();
	}


	List<Topic> loadTopics(RepositoryEntryRef entryRef, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topic");
		sb.append("  from appointmenttopic topic");
		sb.and().append(" topic.entry.key = :entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append(" topic.subIdent = :subIdent");
		}
		
		TypedQuery<Topic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Topic.class)
				.setParameter("entryKey", entryRef.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}

}
