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
package org.olat.modules.todo.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="todotask")
@Table(name="o_todo_task")
public class ToDoTaskImpl implements ToDoTask, Persistable {
	
	private static final long serialVersionUID = -6236852305352574699L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="t_content_modified_date", nullable=false, insertable=true, updatable=true)
	private Date contentModifiedDate;
	@Column(name="t_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="t_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(name="t_status", nullable=false, insertable=true, updatable=true)
	private ToDoStatus status;
	@Enumerated(EnumType.STRING)
	@Column(name="t_priority", nullable=true, insertable=true, updatable=true)
	private ToDoPriority priority;
	@Column(name="t_expenditure_of_work", nullable=true, insertable=true, updatable=true)
	private Long expenditureOfWork;
	@Column(name="t_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="t_due_date", nullable=true, insertable=true, updatable=true)
	private Date dueDate;
	@Column(name="t_done_date", nullable=true, insertable=true, updatable=true)
	private Date doneDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_deleted_date", nullable=true, insertable=true, updatable=true)
	private Date deletedDate;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_deleted_by", nullable=false, insertable=true, updatable=true)
	private Identity deletedBy;

	@Column(name="t_assignee_rights", nullable=true, insertable=true, updatable=true)
	private String assigneeRights;
	@Transient
	private ToDoRight[] assigneeRightsEnum;
	
	@Column(name="t_type", nullable=true, insertable=true, updatable=false)
	private String type;
	@Column(name="t_origin_id", nullable=true, insertable=true, updatable=false)
	private Long originId;
	@Column(name="t_origin_subpath", nullable=true, insertable=true, updatable=false)
	private String originSubPath;
	@Column(name="t_origin_title", nullable=true, insertable=true, updatable=true)
	private String originTitle;
	@Column(name="t_origin_sub_title", nullable=true, insertable=true, updatable=true)
	private String originSubTitle;
	@Column(name="t_origin_deleted", nullable=false, insertable=true, updatable=true)
	private boolean originDeleted;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_origin_deleted_date", nullable=true, insertable=true, updatable=true)
	private Date originDeletedDate;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_origin_deleted_by", nullable=false, insertable=true, updatable=true)
	private Identity originDeletedBy;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group", nullable=true, insertable=true, updatable=false)
	private Group baseGroup;
	@ManyToOne(targetEntity=ToDoTaskImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_collection", nullable=true, insertable=true, updatable=false)
	private ToDoTask collection;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public Date getContentModifiedDate() {
		return contentModifiedDate;
	}

	@Override
	public void setContentModifiedDate(Date contentModifiedDate) {
		this.contentModifiedDate = contentModifiedDate;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public ToDoStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(ToDoStatus status) {
		this.status = status;
		
		if (ToDoStatus.done == status && doneDate == null) {
			doneDate = new Date();
		} else if (ToDoStatus.open == status || ToDoStatus.inProgress == status) {
			doneDate = null;
		}
	}

	@Override
	public ToDoPriority getPriority() {
		return priority;
	}

	@Override
	public void setPriority(ToDoPriority priority) {
		this.priority = priority;
	}

	@Override
	public Long getExpenditureOfWork() {
		return expenditureOfWork;
	}

	@Override
	public void setExpenditureOfWork(Long expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getDueDate() {
		return dueDate;
	}

	@Override
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@Override
	public Date getDoneDate() {
		return doneDate;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Date getDeletedDate() {
		return deletedDate;
	}

	@Override
	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	@Override
	public Identity getDeletedBy() {
		return deletedBy;
	}

	@Override
	public void setDeletedBy(Identity deletedBy) {
		this.deletedBy = deletedBy;
	}

	@Override
	public ToDoRight[] getAssigneeRights() {
		if (assigneeRightsEnum == null) {
			assigneeRightsEnum = ToDoRight.toEnum(assigneeRights);
		}
		return assigneeRightsEnum;
	}

	@Override
	public void setAssigneeRights(ToDoRight[] assigneeRightsEnum) {
		this.assigneeRightsEnum = assigneeRightsEnum;
		this.assigneeRights = ToDoRight.toString(assigneeRightsEnum);
	}

	@Override
	public Long getOriginId() {
		return originId;
	}

	public void setOriginId(Long originId) {
		this.originId = originId;
	}

	@Override
	public String getOriginSubPath() {
		return originSubPath;
	}

	public void setOriginSubPath(String originSubPath) {
		this.originSubPath = originSubPath;
	}

	@Override
	public String getOriginTitle() {
		return originTitle;
	}

	@Override
	public void setOriginTitle(String originTitle) {
		this.originTitle = originTitle;
	}

	@Override
	public boolean isOriginDeleted() {
		return originDeleted;
	}

	public void setOriginDeleted(boolean originDeleted) {
		this.originDeleted = originDeleted;
	}

	@Override
	public String getOriginSubTitle() {
		return originSubTitle;
	}

	@Override
	public void setOriginSubTitle(String originSubTitle) {
		this.originSubTitle = originSubTitle;
	}

	@Override
	public Date getOriginDeletedDate() {
		return originDeletedDate;
	}

	public void setOriginDeletedDate(Date originDeletedDate) {
		this.originDeletedDate = originDeletedDate;
	}

	@Override
	public Identity getOriginDeletedBy() {
		return originDeletedBy;
	}

	public void setOriginDeletedBy(Identity originDeletedBy) {
		this.originDeletedBy = originDeletedBy;
	}

	@Override
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	@Override
	public ToDoTask getCollection() {
		return collection;
	}

	public void setCollection(ToDoTask collection) {
		this.collection = collection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToDoTaskImpl other = (ToDoTaskImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
