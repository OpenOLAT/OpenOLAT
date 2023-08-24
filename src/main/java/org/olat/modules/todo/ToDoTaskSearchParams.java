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
package org.olat.modules.todo;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.DateRange;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskSearchParams {
	
	private Collection<Long> toDoTaskKeys;
	private Collection<ToDoStatus> status;
	private Collection<ToDoPriority> priorities;
	private Collection<String> types;
	private Collection<Long> originIds;
	private Boolean originDeleted;
	private Date createdAfter;
	private boolean dueDateNull;
	private List<DateRange> dueDateRanges;
	private IdentityRef assigneeOrDelegatee;
	private ToDoTaskCustomQuery customQuery;

	public Collection<Long> getToDoTaskKeys() {
		return toDoTaskKeys;
	}
	
	public void setToDoTasks(Collection<? extends ToDoTaskRef> toDoTasks) {
		toDoTaskKeys = toDoTasks.stream().map(ToDoTaskRef::getKey).toList();
	}

	public Collection<ToDoStatus> getStatus() {
		return status;
	}

	public void setStatus(Collection<ToDoStatus> status) {
		this.status = status;
	}

	public Collection<ToDoPriority> getPriorities() {
		return priorities;
	}

	public void setPriorities(Collection<ToDoPriority> priorities) {
		this.priorities = priorities;
	}

	public Collection<String> getTypes() {
		return types;
	}

	public void setTypes(Collection<String> types) {
		this.types = types;
	}

	public Collection<Long> getOriginIds() {
		return originIds;
	}

	public void setOriginIds(Collection<Long> originIds) {
		this.originIds = originIds;
	}
	
	public Boolean getOriginDeleted() {
		return originDeleted;
	}

	public void setOriginDeleted(Boolean originDeleted) {
		this.originDeleted = originDeleted;
	}

	public Date getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}

	public boolean isDueDateNull() {
		return dueDateNull;
	}

	public void setDueDateNull(boolean dueDateNull) {
		this.dueDateNull = dueDateNull;
	}

	public List<DateRange> getDueDateRanges() {
		return dueDateRanges;
	}

	public void setDueDateRanges(List<DateRange> dueDateRanges) {
		this.dueDateRanges = dueDateRanges;
	}
	
	public IdentityRef getAssigneeOrDelegatee() {
		return assigneeOrDelegatee;
	}

	public void setAssigneeOrDelegatee(IdentityRef assigneeOrDelegatee) {
		this.assigneeOrDelegatee = assigneeOrDelegatee;
	}

	public ToDoTaskCustomQuery getCustomQuery() {
		return customQuery;
	}

	public void setCustomQuery(ToDoTaskCustomQuery customQuery) {
		this.customQuery = customQuery;
	}

	public interface ToDoTaskCustomQuery {
		
		public void appendQuery(QueryBuilder sb);
		
		public void addParameters(TypedQuery<?> query);
	}

}
