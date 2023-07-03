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
package org.olat.modules.todo.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskStatusStats;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.model.ToDoTaskImpl;
import org.olat.modules.todo.model.ToDoTaskStatusStatsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToDoTaskDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public ToDoTask create(Identity doer, String type, Long originId, String originSubPath, String originTitle) {
		Group baseGroup = groupDao.createGroup();
		groupDao.addMembershipOneWay(baseGroup, doer, ToDoRole.creator.name());
		groupDao.addMembershipOneWay(baseGroup, doer, ToDoRole.modifier.name());
		
		ToDoTaskImpl toDoTask = new ToDoTaskImpl();
		toDoTask.setCreationDate(new Date());
		toDoTask.setLastModified(toDoTask.getCreationDate());
		toDoTask.setContentModifiedDate(toDoTask.getCreationDate());
		toDoTask.setType(type);
		toDoTask.setStatus(ToDoStatus.open);
		toDoTask.setOriginId(originId);
		toDoTask.setOriginSubPath(originSubPath);
		toDoTask.setOriginTitle(originTitle);
		toDoTask.setOriginDeleted(false);
		toDoTask.setBaseGroup(baseGroup);
		dbInstance.getCurrentEntityManager().persist(toDoTask);
		return toDoTask;
	}
	
	public ToDoTask save(ToDoTask toDoTask) {
		if (toDoTask instanceof ToDoTaskImpl impl) {
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(toDoTask);
		}
		return toDoTask;
	}
	
	public void save(String type, Long originId, String originSubPath, String originTitle) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update todotask toDoTask");
		sb.append("   set toDoTask.originTitle = :originTitle");
		sb.append("     , toDoTask.lastModified = :now");
		sb.and().append("toDoTask.type = :type");
		sb.and().append("toDoTask.originId = :originId");
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			sb.and().append("toDoTask.originSubPath = :originSubPath");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("type", type)
				.setParameter("originId", originId)
				.setParameter("originTitle", originTitle)
				.setParameter("now", new Date());
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			query.setParameter("originSubPath", originSubPath);
		}
		query.executeUpdate();
	}
	
	public void save(String type, Long originId, String originSubPath, boolean originDeleted, Date originDeletedDate, Identity originDeletedBy) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update todotask toDoTask");
		sb.append("   set toDoTask.originDeleted = :originDeleted");
		sb.append("     , toDoTask.originDeletedDate = :originDeletedDate");
		sb.append("     , toDoTask.originDeletedBy = :originDeletedBy");
		sb.append("     , toDoTask.lastModified = :now");
		sb.and().append("toDoTask.type = :type");
		sb.and().append("toDoTask.originId = :originId");
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			sb.and().append("toDoTask.originSubPath = :originSubPath");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("type", type)
				.setParameter("originId", originId)
				.setParameter("originDeleted", originDeleted)
				.setParameter("originDeletedDate", originDeletedDate)
				.setParameter("originDeletedBy", originDeletedBy)
				.setParameter("now", new Date());
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			query.setParameter("originSubPath", originSubPath);
		}
		query.executeUpdate();
	}
	
	public void delete(ToDoTaskRef toDoTask) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from todotask toDoTask");
		sb.and().append("toDoTask.key = :toDoTaskKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("toDoTaskKey", toDoTask.getKey())
				.executeUpdate();
	}

	public ToDoTask load(String type, Long originId, String originSubPath) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDoTask");
		sb.append("  from todotask toDoTask");
		sb.and().append("toDoTask.type = :type");
		sb.and().append("toDoTask.originId = :originId");
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			sb.and().append("toDoTask.originSubPath = :originSubPath");
		}
		
		TypedQuery<ToDoTask> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ToDoTask.class)
				.setParameter("type", type)
				.setParameter("originId", originId);
		if (StringHelper.containsNonWhitespace(originSubPath)) {
			query.setParameter("originSubPath", originSubPath);
		}
		List<ToDoTask> results = query.getResultList();
		
		return !results.isEmpty()? results.get(0): null;
	}

	public List<ToDoTask> loadToDoTasks(ToDoTaskSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDoTask");
		sb.append("  from todotask toDoTask");
		appendQuery(searchParams, sb);
		
		TypedQuery<ToDoTask> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ToDoTask.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}
	
	public ToDoTaskStatusStats loadToDoTaskStatusStats(ToDoTaskSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select status, count(toDoTask)");
		sb.append("  from todotask toDoTask");
		appendQuery(searchParams, sb);
		sb.groupBy().append("status");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		addParameters(query, searchParams);
		
		ToDoTaskStatusStatsImpl statusStats = new ToDoTaskStatusStatsImpl();
		query.getResultList().forEach(result -> statusStats.put((ToDoStatus)result[0], (Long)result[1]));
		return statusStats;
	}
	
	public Long loadToDoTaskCount(ToDoTaskSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(toDoTask)");
		sb.append("  from todotask toDoTask");
		appendQuery(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult();
	}

	public List<ToDoTaskTag> loadToDoTaskTags(ToDoTaskSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDoTaskTag");
		sb.append("  from todotasktag toDoTaskTag");
		sb.append("       inner join toDoTaskTag.toDoTask toDoTask");
		sb.append("       inner join fetch toDoTaskTag.tag tag");
		appendQuery(searchParams, sb);
		
		TypedQuery<ToDoTaskTag> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ToDoTaskTag.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	public List<TagInfo> loadTagInfos(ToDoTaskSearchParams searchParams, ToDoTaskRef selectionTask) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		sb.append("       tag.key");
		sb.append("     , min(tag.creationDate)");
		sb.append("     , min(tag.displayName)");
		sb.append("     , count(toDoTask.key)");
		if (selectionTask != null) {
			sb.append(" , sum(case when (toDoTask.key=").append(selectionTask.getKey()).append(") then 1 else 0 end) as selected");
		} else {
			sb.append(" , cast(0 as long) as selected");
		}
		sb.append(")");
		sb.append("  from todotasktag toDoTaskTag");
		sb.append("       inner join toDoTaskTag.toDoTask toDoTask");
		sb.append("       inner join toDoTaskTag.tag tag");
		appendQuery(searchParams, sb);
		sb.groupBy().append("tag.key");
		
		TypedQuery<TagInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TagInfo.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(ToDoTaskSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getToDoTaskKeys() != null && !searchParams.getToDoTaskKeys().isEmpty()) {
			sb.and().append("toDoTask.key in :toDoTaskKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("toDoTask.status in :status");
		}
		if (searchParams.getPriorities() != null && !searchParams.getPriorities().isEmpty()) {
			sb.and().append("toDoTask.priority in :priorities");
		}
		if (searchParams.getTypes() != null && !searchParams.getTypes().isEmpty()) {
			sb.and().append("toDoTask.type in :types");
		}
		if (searchParams.getOriginIds() != null && !searchParams.getOriginIds().isEmpty()) {
			sb.and().append("toDoTask.originId in :originIds");
		}
		if (searchParams.getOriginDeleted() != null) {
			sb.and().append("toDoTask.originDeleted = :originDeleted");
		}
		if (searchParams.getCreatedAfter() != null) {
			sb.and().append("toDoTask.creationDate >= :createdAfter");
		}
		if ((searchParams.getDueDateRanges() != null && !searchParams.getDueDateRanges().isEmpty()) || searchParams.isDueDateNull()) {
			sb.and().append(" (");
			boolean or = false;
			if (searchParams.getDueDateRanges() != null && !searchParams.getDueDateRanges().isEmpty()) {
				for (int i = 0; i < searchParams.getDueDateRanges().size(); i++) {
					if (or) {
						sb.append(" or ");
					}
					or = true;
					
					sb.append("(");
					sb.append("toDoTask.dueDate >= :dueDateAfter").append(i);
					sb.append(" and ");
					sb.append("toDoTask.dueDate < :dueDateBefore").append(i);
					sb.append(")");
				}
			}
			if (searchParams.isDueDateNull()) {
				if (or) {
					sb.append(" or ");
				}
				sb.append("(toDoTask.dueDate is null)");
			}
			sb.append(")");
		}
		if (searchParams.getAssigneeOrDelegatee() != null) {
			sb.and().append("toDoTask.baseGroup.key in (");
			sb.append("select membership.group.key");
			sb.append("  from bgroupmember as membership");
			sb.append(" where membership.group.key = toDoTask.baseGroup.key");
			sb.append("   and membership.identity.key = :assigneeOrDelegatee");
			sb.append("   and membership.role").in(ToDoRole.assignee, ToDoRole.delegatee);
			sb.append(")");
		}
		if (searchParams.getCustomQuery() != null) {
			searchParams.getCustomQuery().appendQuery(sb);
		}
	}

	private void addParameters(TypedQuery<?> query, ToDoTaskSearchParams searchParams) {
		if (searchParams.getToDoTaskKeys() != null && !searchParams.getToDoTaskKeys().isEmpty()) {
			query.setParameter("toDoTaskKeys", searchParams.getToDoTaskKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
		if (searchParams.getPriorities() != null && !searchParams.getPriorities().isEmpty()) {
			query.setParameter("priorities", searchParams.getPriorities());
		}
		if (searchParams.getTypes() != null && !searchParams.getTypes().isEmpty()) {
			query.setParameter("types", searchParams.getTypes());
		}
		if (searchParams.getOriginIds() != null && !searchParams.getOriginIds().isEmpty()) {
			query.setParameter("originIds", searchParams.getOriginIds());
		}
		if (searchParams.getOriginDeleted() != null) {
			query.setParameter("originDeleted", searchParams.getOriginDeleted());
		}
		if (searchParams.getCreatedAfter() != null) {
			query.setParameter("createdAfter", searchParams.getCreatedAfter());
		}
		if (searchParams.getDueDateRanges() != null && !searchParams.getDueDateRanges().isEmpty()) {
			for (int i = 0; i < searchParams.getDueDateRanges().size(); i++) {
				DateRange dateRange = searchParams.getDueDateRanges().get(i);
				query.setParameter("dueDateAfter" + i, dateRange.getFrom());
				query.setParameter("dueDateBefore" + i, dateRange.getTo());
			}
		}
		if (searchParams.getAssigneeOrDelegatee() != null) {
			query.setParameter("assigneeOrDelegatee", searchParams.getAssigneeOrDelegatee().getKey());
		}
		if (searchParams.getCustomQuery() != null) {
			searchParams.getCustomQuery().addParameters(query);
		}
	}

}
