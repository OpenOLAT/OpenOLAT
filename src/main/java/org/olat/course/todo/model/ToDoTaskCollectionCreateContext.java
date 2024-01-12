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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ui.ToDoSimpleViewController.SimpleToDoTask;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskCollectionCreateContext implements SimpleToDoTask {
	
	public static final String KEY = "toDoTaskCollectionCreateContext";
	
	private Identity doer;
	private boolean coach;
	private RepositoryEntry repositoryEntry;
	private Long convertFromKey;
	private String title;
	private String description;
	private ToDoStatus status;
	private ToDoPriority priority;
	private Long expenditureOfWork;
	private Date startDate;
	private Date dueDate;
	private List<String> tagDisplayNames;
	private boolean assigneesSelected;
	private Collection<Long> assigneeKeys;
	
	public Identity getDoer() {
		return doer;
	}

	public void setDoer(Identity doer) {
		this.doer = doer;
	}

	public boolean isCoach() {
		return coach;
	}

	public void setCoach(boolean coach) {
		this.coach = coach;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	public Long getConvertFromKey() {
		return convertFromKey;
	}

	public void setConvertFromKey(Long convertFromKey) {
		this.convertFromKey = convertFromKey;
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
		return tagDisplayNames;
	}

	public void setTagDisplayNames(List<String> tagDisplayNames) {
		this.tagDisplayNames = tagDisplayNames;
	}

	public boolean isAssigneesSelected() {
		return assigneesSelected;
	}

	public void setAssigneesSelected(boolean assigneesSelected) {
		this.assigneesSelected = assigneesSelected;
	}

	public Collection<Long> getAssigneeKeys() {
		return assigneeKeys;
	}

	public void setAssigneeKeys(Collection<Long> assigneeKeys) {
		this.assigneeKeys = assigneeKeys;
	}

}
