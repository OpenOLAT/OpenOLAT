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
package org.olat.modules.topicbroker.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBTopicDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBTopic createTopic(Identity creator, TBBroker broker) {
		TBTopicImpl topic = new TBTopicImpl();
		topic.setCreationDate(new Date());
		topic.setLastModified(topic.getCreationDate());
		topic.setCreator(creator);
		topic.setBroker(broker);
		topic.setSortOrder(getNextSortOrder(broker));
		
		dbInstance.getCurrentEntityManager().persist(topic);
		return topic;
	}
	
	public int getNextSortOrder(TBBroker broker) {
		String query = """
		select  max(topic.sortOrder) + 1
		  from topicbrokertopic topic
		 where topic.broker.key = :brokerKey
		   and topic.deletedDate is null
		""";
		
		List<Integer> next = dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.setParameter("brokerKey", broker.getKey())
				.getResultList();
		return next != null && !next.isEmpty() && next.get(0) != null? next.get(0).intValue(): 1;
	}
	
	TBTopic loadNext(TBTopic topic, boolean up) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topic");
		sb.append("  from topicbrokertopic topic");
		sb.and().append("topic.deletedDate is null");
		sb.and().append("topic.broker.key = :brokerKey");
		sb.and().append("topic.sortOrder ").append("<", ">", up).append(" :sortOrder");
		sb.orderBy().append("topic.sortOrder").appendAsc(!up);
		
		List<TBTopic> topics = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBTopic.class)
				.setParameter("brokerKey", topic.getBroker().getKey())
				.setParameter("sortOrder", topic.getSortOrder())
				.setMaxResults(1)
				.getResultList();
		
		return !topics.isEmpty()? topics.get(0): null;
	}
	
	TBTopic updateTopic(TBTopic topic) {
		if (topic instanceof TBTopicImpl) {
			TBTopicImpl impl = (TBTopicImpl)topic;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(topic);
		return topic;
	}
	
	void deleteTopic(TBTopicRef topic) {
		String query = """
		delete
		  from topicbrokertopic topic
		 where topic.key = :topicKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("topicKey", topic.getKey())
				.executeUpdate();
	}

	void deleteTopics(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokertopic topic
		 where topic.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
		
	}

	List<TBTopic> loadTopics(TBTopicSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select topic");
		sb.append("  from topicbrokertopic topic");
		if (params.isFetchIdentities()) {
			sb.append(" join fetch topic.creator");
			sb.append(" left join fetch topic.deletedBy");
		}
		if (params.isFetchBroker()) {
			sb.append(" join fetch topic.broker");
		}
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("topic.broker.key in :brokerKeys");
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			sb.and().append("topic.key in :topicKeys");
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			sb.and().append("topic.identifier in :identifiers");
		}
		if (params.getDeleted() != null) {
			sb.and().append("topic.deletedDate is ").append("not ", params.getDeleted()).append("null");
		}
		
		TypedQuery<TBTopic> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBTopic.class);
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			query.setParameter("topicKeys", params.getTopicKeys());
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			query.setParameter("identifiers", params.getIdentifiers());
		}
		return query.getResultList();
	}

}
