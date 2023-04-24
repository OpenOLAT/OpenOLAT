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
package org.olat.modules.quality.manager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.quality.QualityAuditLog;
import org.olat.modules.quality.QualityAuditLogSearchParams;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.model.QualityAuditLogImpl;
import org.olat.modules.quality.model.QualityDataCollectionImpl;
import org.olat.modules.todo.ToDoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class QualityAuditLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public QualityAuditLog create(QualityAuditLog.Action action, String before, String after, Identity doer,
			QualityDataCollection dataCollection) {
		return create(action, before, after, doer, dataCollection, null, null);
	}
	
	public QualityAuditLog create(QualityAuditLog.Action action, String before, String after, Identity doer,
			Long dataCollectionKey, ToDoTask toDoTask, Identity identity) {
		QualityDataCollection dataCollection = dataCollectionKey != null
				? dbInstance.getCurrentEntityManager().getReference(QualityDataCollectionImpl.class, dataCollectionKey)
				: null;
		return create(action, before, after, doer, dataCollection, toDoTask, identity, new Date());
	}
	
	public QualityAuditLog create(QualityAuditLog.Action action, String before, String after, Identity doer,
			QualityDataCollection dataCollection, ToDoTask toDoTask, Identity identity) {
		return create(action, before, after, doer, dataCollection, toDoTask, identity, new Date());
	}
	
	QualityAuditLog create(QualityAuditLog.Action action, String before, String after, Identity doer,
			QualityDataCollection dataCollection, ToDoTask toDoTask, Identity identity, Date creationDate) {
		QualityAuditLogImpl auditLog = new QualityAuditLogImpl();
		auditLog.setCreationDate(creationDate);
		auditLog.setAction(action);
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setDoer(doer);
		auditLog.setDataCollection(dataCollection);
		auditLog.setToDoTask(toDoTask);
		auditLog.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(auditLog);
		return auditLog;
	}
	
	public void delete(List<QualityAuditLog> activities) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from qualityauditlog auditLog");
		sb.and().append("auditLog.key in :activitiesKeys");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("activitiesKeys", activities.stream().map(QualityAuditLog::getKey).collect(Collectors.toList()))
				.executeUpdate();
	}
	
	public List<Identity> loadAuditLogDoers(QualityAuditLogSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct auditLog.doer");
		sb.append("  from qualityauditlog auditLog");
		if (searchParams.isFetchDoer()) {
			sb.append(" inner join auditLog.doer doer");
			sb.append(" inner join fetch doer.user user");
		}
		appendQuery(searchParams, sb);
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}
	
	public List<QualityAuditLog> loadAuditLogs(QualityAuditLogSearchParams searchParams, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select auditLog");
		sb.append("  from qualityauditlog auditLog");
		if (searchParams.isFetchDoer()) {
			sb.append(" inner join fetch auditLog.doer doer");
			sb.append(" inner join fetch doer.user user");
		}
		appendQuery(searchParams, sb);
		
		TypedQuery<QualityAuditLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityAuditLog.class);
		addParameters(query, searchParams);
		query.setFirstResult(firstResult);
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}

	private void appendQuery(QualityAuditLogSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			sb.and().append("auditLog.action in :actions");
		}
		if (searchParams.getDoerKeys() != null && !searchParams.getDoerKeys().isEmpty()) {
			sb.and().append("auditLog.doer.key in :doerKeys");
		}
		if (searchParams.getDataCollectionKeys() != null && !searchParams.getDataCollectionKeys().isEmpty()) {
			sb.and().append("auditLog.dataCollection.key in :dataCollectionKeys");
		}
		if (searchParams.getToDoTaskKeys() != null && !searchParams.getToDoTaskKeys().isEmpty()) {
			sb.and().append("auditLog.toDoTask.key in :toDoTaskKeys");
		}
	}

	private void addParameters(TypedQuery<?> query, QualityAuditLogSearchParams searchParams) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			query.setParameter("actions", searchParams.getActions());
		}
		if (searchParams.getDoerKeys() != null && !searchParams.getDoerKeys().isEmpty()) {
			query.setParameter("doerKeys", searchParams.getDoerKeys());
		}
		if (searchParams.getDataCollectionKeys() != null && !searchParams.getDataCollectionKeys().isEmpty()) {
			query.setParameter("dataCollectionKeys", searchParams.getDataCollectionKeys());
		}
		if (searchParams.getToDoTaskKeys() != null && !searchParams.getToDoTaskKeys().isEmpty()) {
			query.setParameter("toDoTaskKeys", searchParams.getToDoTaskKeys());
		}
	}

}
