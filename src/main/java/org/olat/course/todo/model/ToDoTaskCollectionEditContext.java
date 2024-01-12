/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.todo.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoSimpleViewController.SimpleToDoTask;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskCollectionEditContext implements SimpleToDoTask {
	
	public static final String KEY = "toDoTaskCollectionEditContext";
	
	public enum Field { title, description, status, priority, expenditureOfWork, startDate, dueDate, tagDisplayNames }
	
	private ToDoTask toDoTaskCollection;
	private List<String> collectionTagDisplayNames;
	private final Set<Field> selection = new HashSet<>();
	private final Set<Field> overrides = new HashSet<>();
	private String title;
	private String description;
	private ToDoStatus status;
	private ToDoPriority priority;
	private Long expenditureOfWork;
	private Date startDate;
	private Date dueDate;
	private List<String> tagDisplayNames;

	public ToDoTask getToDoTaskCollection() {
		return toDoTaskCollection;
	}

	public void setToDoTaskCollection(ToDoTask toDoTaskCollection) {
		this.toDoTaskCollection = toDoTaskCollection;
	}

	public List<String> getCollectionTagDisplayNames() {
		return collectionTagDisplayNames;
	}

	public void setCollectionTagDisplayNames(List<String> collectionTagDisplayNames) {
		this.collectionTagDisplayNames = collectionTagDisplayNames;
	}

	public void select(Field field, boolean select) {
		if (select) {
			selection.add(field);
		} else {
			selection.remove(field);
		}
	}
	
	public boolean isSelected(Field field) {
		return selection.contains(field);
	}
	
	public void override(Field field, boolean override) {
		if (override) {
			overrides.add(field);
		} else {
			overrides.remove(field);
		}
	}
	
	public boolean isOverride(Field field) {
		return overrides.contains(field);
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public ToDoStatus getStatus() {
		return status;
	}
	
	public void setStatus(ToDoStatus status) {
		this.status = status;
	}
	
	@Override
	public ToDoPriority getPriority() {
		return priority;
	}
	
	public void setPriority(ToDoPriority priority) {
		this.priority = priority;
	}
	
	@Override
	public Long getExpenditureOfWork() {
		return expenditureOfWork;
	}
	
	public void setExpenditureOfWork(Long expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}
	
	@Override
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@Override
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public List<String> getTagDisplayNames() {
		return tagDisplayNames != null? tagDisplayNames: List.of();
	}

	public void setTagDisplayNames(List<String> tagDisplayNames) {
		this.tagDisplayNames = tagDisplayNames;
	}

}
