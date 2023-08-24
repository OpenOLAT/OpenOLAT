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
package org.olat.modules.todo.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 30 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "toDoTaskVO")
public class ToDoTaskVO {
	
	private Long key;
	private Date contentModifiedDate;
	private String title;
	private String description;
	private String status;
	private String priority;
	private Long expenditureOfWork;
	private Date startDate;
	private Date dueDate;
	private Date doneDate;
	private Date deletedDate;

	private String type;
	private Long originId;
	private String originSubPath;
	private String originTitle;
	private boolean originDeleted;
	private Date originDeletedDate;
	
	public ToDoTaskVO() {
		// make JAX-RS happy
	}
	
	public static ToDoTaskVO valueOf(ToDoTask toDoTask) {
		ToDoTaskVO vo = new ToDoTaskVO();
		
		vo.setKey(toDoTask.getKey());
		vo.setContentModifiedDate(toDoTask.getContentModifiedDate());
		vo.setTitle(toDoTask.getTitle());
		vo.setDescription(toDoTask.getDescription());
		vo.setStatus(toDoTask.getStatus() != null? toDoTask.getStatus().name(): null);
		vo.setPriority(toDoTask.getPriority() != null? toDoTask.getPriority().name(): null);
		vo.setExpenditureOfWork(toDoTask.getExpenditureOfWork());
		vo.setStartDate(toDoTask.getStartDate());
		vo.setDueDate(toDoTask.getDueDate());
		vo.setDoneDate(toDoTask.getDoneDate());
		vo.setDeletedDate(toDoTask.getDeletedDate());
		vo.setType(toDoTask.getType());
		vo.setOriginId(toDoTask.getOriginId());
		vo.setOriginSubPath(toDoTask.getOriginSubPath());
		vo.setOriginTitle(toDoTask.getOriginTitle());
		vo.setOriginDeleted(toDoTask.isOriginDeleted());
		vo.setOriginDeletedDate(toDoTask.getOriginDeletedDate());
		return vo;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Long getExpenditureOfWork() {
		return expenditureOfWork;
	}
	
	public void setExpenditureOfWork(Long expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
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
	
	public Date getDoneDate() {
		return doneDate;
	}
	
	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}
	
	public Date getDeletedDate() {
		return deletedDate;
	}
	
	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
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
	
	public Date getOriginDeletedDate() {
		return originDeletedDate;
	}
	
	public void setOriginDeletedDate(Date originDeletedDate) {
		this.originDeletedDate = originDeletedDate;
	}

}
