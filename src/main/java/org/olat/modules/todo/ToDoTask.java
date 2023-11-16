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

import java.util.Date;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ToDoTask extends ToDoTaskRef, ToDoContext, CreateInfo, ModifiedInfo {
	
	public Date getContentModifiedDate();
	
	public void setContentModifiedDate(Date contentModifiedDate);

	public String getTitle();

	public void setTitle(String title);

	public String getDescription();

	public void setDescription(String description);

	public ToDoStatus getStatus();

	public void setStatus(ToDoStatus status);

	public ToDoPriority getPriority();

	public void setPriority(ToDoPriority priority);
	
	public Long getExpenditureOfWork();
	
	public void setExpenditureOfWork(Long expenditureOfWork);

	public Date getStartDate();

	public void setStartDate(Date startDate);

	public Date getDueDate();

	public void setDueDate(Date dueDate);

	public Date getDoneDate();
	
	public Date getDeletedDate();
	
	public void setDeletedDate(Date deletedDate);
	
	public Identity getDeletedBy();
	
	public void setDeletedBy(Identity deletedBy);
	
	public ToDoRight[] getAssigneeRights();
	
	public void setAssigneeRights(ToDoRight[] assigneeRights);
	
	public void setOriginTitle(String originTiltle);
	
	public void setOriginSubTitle(String originSubTiltle);
	
	public boolean isOriginDeleted();
	
	public Date getOriginDeletedDate();
	
	public Identity getOriginDeletedBy();
	
	public Group getBaseGroup();
	
}
