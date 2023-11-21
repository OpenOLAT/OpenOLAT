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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormItemList;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FormItemCollectonFlexiCellRenderer.FormItemCollectionCell;
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
public class ToDoTaskRow implements ToDoTaskRef, FlexiTreeTableNode {
	
	private Long key;
	private Date creationDate;
	private Date contentModifiedDate;
	private String title;
	private String displayName;
	private ToDoStatus status;
	private String statusText;
	private ToDoPriority priority;
	private Long expenditureOfWork;
	private String formattedExpenditureOfWork;
	private Date startDate;
	private Date dueDate;
	private String formattedDueDate;
	private String due;
	private Boolean overdue;
	private Date doneDate;
	private String formattedDoneDate;
	private String type;
	private String translatedType;
	private Date deletedDate;
	private Identity deletedBy;
	private String deletedByName;
	private Long originId;
	private String originSubPath;
	private String originTitle;
	private String originSubTitle;
	private boolean originDeleted;
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
	private FormToggle detailsItem;
	private FormItem titleItem;
	private FormToggle doItem;
	private FormLink goToOriginLink;
	private FormLink goToSubOriginLink;
	private FormLink toolsLink;
	private String detailsComponentName;
	private List<ToDoTaskRow> children;
	private ToDoTaskRow parent;
	
	public ToDoTaskRow() {
		//
	}
	
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
		this.originSubTitle = toDoTask.getOriginSubTitle();
		this.originDeleted = toDoTask.isOriginDeleted();
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getContentModifiedDate() {
		return contentModifiedDate;
	}

	public void setContentModifiedDate(Date contentModifiedDate) {
		this.contentModifiedDate = contentModifiedDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public ToDoPriority getPriority() {
		return priority;
	}

	public void setPriority(ToDoPriority priority) {
		this.priority = priority;
	}

	public Long getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public void setExpenditureOfWork(Long expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
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

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getFormattedDueDate() {
		return formattedDueDate;
	}

	public void setFormattedDueDate(String formattedDueDate) {
		this.formattedDueDate = formattedDueDate;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public String getFormattedDoneDate() {
		return formattedDoneDate;
	}

	public void setFormattedDoneDate(String formattedDoneDate) {
		this.formattedDoneDate = formattedDoneDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public void setOriginId(Long originId) {
		this.originId = originId;
	}

	public String getOriginSubPath() {
		return originSubPath;
	}

	public void setOriginSubPath(String originSubPath) {
		this.originSubPath = originSubPath;
	}

	public String getOriginTitle() {
		return originTitle;
	}

	public void setOriginTitle(String originTitle) {
		this.originTitle = originTitle;
	}

	public boolean isOriginDeleted() {
		return originDeleted;
	}

	public void setOriginDeleted(boolean originDeleted) {
		this.originDeleted = originDeleted;
	}

	public String getOriginSubTitle() {
		return originSubTitle;
	}

	public void setOriginSubTitle(String originSubTitle) {
		this.originSubTitle = originSubTitle;
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

	public FormToggle getDetailsItem() {
		return detailsItem;
	}

	public void setDetailsItem(FormToggle detailsItem) {
		this.detailsItem = detailsItem;
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
	
	public FormItemCollectionCell getTitleItems() {
		FormItemList items = new FormItemList(3);
		if (detailsItem != null) {
			items.add(detailsItem);
		}
		if (doItem != null) {
			items.add(doItem);
		}
		if (titleItem != null) {
			items.add(titleItem);
		}
		return new FormItemCollectionCell(items);
	}

	public FormLink getGoToOriginLink() {
		return goToOriginLink;
	}

	public void setGoToOriginLink(FormLink goToOriginLink) {
		this.goToOriginLink = goToOriginLink;
	}

	public FormLink getGoToSubOriginLink() {
		return goToSubOriginLink;
	}

	public void setGoToSubOriginLink(FormLink goToSubOriginLink) {
		this.goToSubOriginLink = goToSubOriginLink;
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

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}
	
	public void addChild(ToDoTaskRow child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
		child.parent = this;
	}
	
	public List<ToDoTaskRow> getChildren() {
		return children != null? children: List.of();
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	@Override
	public String getCrump() {
		return null;
	}
	
}
