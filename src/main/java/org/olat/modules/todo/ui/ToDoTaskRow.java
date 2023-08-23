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
package org.olat.modules.todo.ui;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskRow implements ToDoTaskRef {
	
	private final Long key;
	private final Date creationDate;
	private final Date contentModifiedDate;
	private final String title;
	private String displayName;
	private ToDoStatus status;
	private final ToDoPriority priority;
	private final Long expenditureOfWork;
	private String formattedExpenditureOfWork;
	private final Date startDate;
	private final Date dueDate;
	private String due;
	private Boolean overdue;
	private Date doneDate;
	private final String type;
	private String translatedType;
	private Date deletedDate;
	private Identity deletedBy;
	private String deletedByName;
	private final Long originId;
	private final String originSubPath;
	private final String originTitle;
	private final boolean originDeleted;
	private Identity creator;
	private Identity modifier;
	private Set<Identity> assignees;
	private Component assigneesPortraits;
	private Set<Identity> delegatees;
	private Component delegateesPortraits;
	private List<Tag> tags;
	private Set<Long> tagKeys;
	private String formattedTags;
	private boolean canEdit;
	private boolean canDelete;
	private FormItem titleItem;
	private FormToggle doItem;
	private FormLink goToOriginLink;
	private FormLink toolsLink;
	private String detailsComponentName;
	
	public ToDoTaskRow(ToDoTask toDoTask) {
		this.key = toDoTask.getKey();
		this.creationDate = toDoTask.getCreationDate();
		this.contentModifiedDate = toDoTask.getContentModifiedDate();
		this.title = toDoTask.getTitle();
		this.status = toDoTask.getStatus();
		this.priority = toDoTask.getPriority();
		this.expenditureOfWork = toDoTask.getExpenditureOfWork();
		this.startDate = toDoTask.getStartDate();
		this.dueDate = toDoTask.getDueDate();
		this.doneDate = toDoTask.getDoneDate();
		this.type = toDoTask.getType();
		this.originId = toDoTask.getOriginId();
		this.originSubPath = toDoTask.getOriginSubPath();
		this.originTitle = toDoTask.getOriginTitle();
		this.originDeleted = toDoTask.isOriginDeleted();
	}

	@Override
	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getContentModifiedDate() {
		return contentModifiedDate;
	}

	public String getTitle() {
		return title;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public ToDoStatus getStatus() {
		return status;
	}

	public void setStatus(ToDoStatus status) {
		this.status = status;
	}

	public ToDoPriority getPriority() {
		return priority;
	}

	public Long getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public String getFormattedExpenditureOfWork() {
		return formattedExpenditureOfWork;
	}

	public void setFormattedExpenditureOfWork(String formattedExpenditureOfWork) {
		this.formattedExpenditureOfWork = formattedExpenditureOfWork;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public String getType() {
		return type;
	}

	public String getTranslatedType() {
		return translatedType;
	}

	public void setTranslatedType(String translatedType) {
		this.translatedType = translatedType;
	}

	public Date getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	public Identity getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Identity deletedBy) {
		this.deletedBy = deletedBy;
	}

	public String getDeletedByName() {
		return deletedByName;
	}

	public void setDeletedByName(String deletedByName) {
		this.deletedByName = deletedByName;
	}

	public Long getOriginId() {
		return originId;
	}

	public String getOriginSubPath() {
		return originSubPath;
	}

	public String getOriginTitle() {
		return originTitle;
	}

	public boolean isOriginDeleted() {
		return originDeleted;
	}

	public String getDue() {
		return due;
	}

	public void setDue(String due) {
		this.due = due;
	}

	public Boolean isOverdue() {
		return overdue;
	}

	public void setOverdue(Boolean overdue) {
		this.overdue = overdue;
	}

	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	public Identity getModifier() {
		return modifier;
	}

	public void setModifier(Identity modifier) {
		this.modifier = modifier;
	}

	public Set<Identity> getAssignees() {
		return assignees;
	}

	public void setAssignees(Set<Identity> assignees) {
		this.assignees = assignees;
	}

	public Component getAssigneesPortraits() {
		return assigneesPortraits;
	}

	public void setAssigneesPortraits(Component assigneesPortraits) {
		this.assigneesPortraits = assigneesPortraits;
	}

	public Set<Identity> getDelegatees() {
		return delegatees;
	}

	public void setDelegatees(Set<Identity> delegatees) {
		this.delegatees = delegatees;
	}

	public Component getDelegateesPortraits() {
		return delegateesPortraits;
	}

	public void setDelegateesPortraits(Component delegateesPortraits) {
		this.delegateesPortraits = delegateesPortraits;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public Set<Long> getTagKeys() {
		return tagKeys;
	}

	public void setTagKeys(Set<Long> tagKeys) {
		this.tagKeys = tagKeys;
	}

	public String getFormattedTags() {
		return formattedTags;
	}

	public void setFormattedTags(String formattedTags) {
		this.formattedTags = formattedTags;
	}

	public boolean canEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public boolean canDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public FormItem getTitleItem() {
		return titleItem;
	}

	public void setTitleItem(FormItem titleItem) {
		this.titleItem = titleItem;
	}

	public FormToggle getDoItem() {
		return doItem;
	}

	public void setDoItem(FormToggle doItem) {
		this.doItem = doItem;
	}

	public FormLink getGoToOriginLink() {
		return goToOriginLink;
	}

	public void setGoToOriginLink(FormLink goToOriginLink) {
		this.goToOriginLink = goToOriginLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public String getDetailsComponentName() {
		return detailsComponentName;
	}

	public void setDetailsComponentName(String detailsComponentName) {
		this.detailsComponentName = detailsComponentName;
	}
	
}
