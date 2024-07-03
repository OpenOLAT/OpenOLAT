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
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBAuditLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBAuditLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBBroker broker) {
		return create(action, before, after, doer, broker, null, null, null, null);
	}

	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBParticipant participant) {
		return create(action, before, after, doer, participant.getBroker(), participant, null, null, null);
	}
	
	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBTopic topic) {
		return create(action, before, after, doer, topic.getBroker(), null, topic, null, null);
	}
	
	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBCustomFieldDefinition definition) {
		return create(action, before, after, doer, definition.getBroker(), null, null, definition, null);
	}

	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBCustomFieldDefinition definition, TBTopic topic) {
		return create(action, before, after, doer, definition.getBroker(), null, topic, definition, null);
	}

	public TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBSelection selection) {
		return create(action, before, after, doer, selection.getTopic().getBroker(), selection.getParticipant(),
				selection.getTopic(), null, selection);
	}
	
	TBAuditLog create(TBAuditLog.Action action, String before, String after, Identity doer, TBBroker broker,
			TBParticipant participant, TBTopic topic, TBCustomFieldDefinition definition, TBSelection selection) {
		TBAuditLogImpl auditLog = new TBAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action);
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setDoer(doer);
		auditLog.setBroker(broker);
		auditLog.setParticipant(participant);
		auditLog.setTopic(topic);
		auditLog.setDefinition(definition);
		auditLog.setSelection(selection);
		dbInstance.getCurrentEntityManager().persist(auditLog);
		return auditLog;
	}
	
	public void delete(List<TBAuditLog> activities) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from topicbrokerauditlog auditLog");
		sb.and().append("auditLog.key in :activitiesKeys");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("activitiesKeys", activities.stream().map(TBAuditLog::getKey).collect(Collectors.toList()))
				.executeUpdate();
	}
	
	public void delete(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokerauditlog auditLog
		 where auditLog.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}
	
	public List<Identity> loadAuditLogDoers(TBAuditLogSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct auditLog.doer");
		sb.append("  from topicbrokerauditlog auditLog");
		if (searchParams.isFetchDoer()) {
			sb.append(" inner join auditLog.doer doer");
			sb.append(" inner join fetch doer.user user");
		}
		appendQuery(searchParams, sb);
		if (searchParams.getOrderAsc() != null) {
			sb.orderBy().append("auditLog.creationDate").appendAsc(searchParams.getOrderAsc());
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}
	
	public List<TBAuditLog> loadAuditLogs(TBAuditLogSearchParams searchParams, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select auditLog");
		sb.append("  from topicbrokerauditlog auditLog");
		if (searchParams.isFetchDoer()) {
			sb.append(" inner join fetch auditLog.doer doer");
			sb.append(" inner join fetch doer.user user");
		}
		appendQuery(searchParams, sb);
		if (searchParams.getOrderAsc() != null) {
			sb.orderBy().append("auditLog.creationDate").appendAsc(searchParams.getOrderAsc());
		}
		
		TypedQuery<TBAuditLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBAuditLog.class);
		addParameters(query, searchParams);
		query.setFirstResult(firstResult);
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}

	private void appendQuery(TBAuditLogSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			sb.and().append("auditLog.action in :actions");
		}
		if (searchParams.getDoerKeys() != null && !searchParams.getDoerKeys().isEmpty()) {
			sb.and().append("auditLog.doer.key in :doerKeys");
		}
		if (searchParams.getBrokerKeys() != null && !searchParams.getBrokerKeys().isEmpty()) {
			sb.and().append("auditLog.broker.key in :brokerKeys");
		}
		if (searchParams.getTopicKeys() != null && !searchParams.getTopicKeys().isEmpty()) {
			sb.and().append("auditLog.topic.key in :topicKeys");
		}
	}

	private void addParameters(TypedQuery<?> query, TBAuditLogSearchParams searchParams) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			query.setParameter("actions", searchParams.getActions());
		}
		if (searchParams.getDoerKeys() != null && !searchParams.getDoerKeys().isEmpty()) {
			query.setParameter("doerKeys", searchParams.getDoerKeys());
		}
		if (searchParams.getBrokerKeys() != null && !searchParams.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", searchParams.getBrokerKeys());
		}
		if (searchParams.getTopicKeys() != null && !searchParams.getTopicKeys().isEmpty()) {
			query.setParameter("topicKeys", searchParams.getTopicKeys());
		}
	}

}
