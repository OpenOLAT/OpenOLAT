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
package org.olat.modules.quality.ui.security;

import org.olat.modules.quality.QualityReportAccess.ToDoAccess;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: Nov 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DataCollectionToDoSecurityCallback extends DataCollectionReadOnlySecurityCallback {
	
	private final ToDoAccess toDoAccess;

	public DataCollectionToDoSecurityCallback(ToDoAccess toDoAccess) {
		this.toDoAccess = toDoAccess;
	}

	@Override
	public boolean canViewToDos() {
		return true;
	}

	@Override
	public boolean canViewAllToDos() {
		return ToDoAccess.allFullAccess == toDoAccess
				|| ToDoAccess.allCreateEdit == toDoAccess
				|| ToDoAccess.allReadMyEdit == toDoAccess;
	}

	@Override
	public boolean canCreateToDoTasks() {
		return ToDoAccess.allFullAccess == toDoAccess
				|| ToDoAccess.allCreateEdit == toDoAccess
				|| ToDoAccess.myCreateEdit == toDoAccess;
	}

	@Override
	public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		return false;
	}

	@Override
	public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		if (ToDoStatus.deleted == toDoTask.getStatus()) {
			return false;
		}
		if (ToDoAccess.allFullAccess == toDoAccess || ToDoAccess.allCreateEdit == toDoAccess) {
			return true;
		}
		if (ToDoAccess.allReadMyEdit == toDoAccess || ToDoAccess.myCreateEdit == toDoAccess) {
			return assignee || delegatee;
		}
		
		return false;
	}
	
	@Override
	public boolean canBulkDeleteToDoTasks() {
		return false;
	}

	@Override
	public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		if (ToDoStatus.deleted == toDoTask.getStatus()) {
			return false;
		}
		return ToDoAccess.allFullAccess == toDoAccess;
	}
	
	@Override
	public boolean canRestore(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		return false;
	}

}
