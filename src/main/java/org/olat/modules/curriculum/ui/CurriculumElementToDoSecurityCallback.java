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
package org.olat.modules.curriculum.ui;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSecurityCallback;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementToDoSecurityCallback implements ToDoTaskSecurityCallback {

	protected final CurriculumSecurityCallback secCallback;
	protected final CurriculumElement element;

	public CurriculumElementToDoSecurityCallback(CurriculumSecurityCallback secCallback, CurriculumElement element) {
		this.secCallback = secCallback;
		this.element = element;
	}

	/**
	 * @param toDoTask 
	 */
	protected CurriculumElement getCurriculumElement(ToDoTask toDoTask) {
		return element;
	}

	@Override
	public boolean canCreateToDoTasks() {
		return element != null && secCallback.canEditCurriculumElement(element);
	}

	@Override
	public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		return canEdit(toDoTask, creator, assignee, delegatee);
	}

	@Override
	public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		if (ToDoStatus.deleted == toDoTask.getStatus()) {
			return false;
		}
		CurriculumElement el = getCurriculumElement(toDoTask);
		return el != null && secCallback.canEditCurriculumElement(el);
	}

	@Override
	public boolean canBulkDeleteToDoTasks() {
		return element != null && secCallback.canEditCurriculumElement(element);
	}

	@Override
	public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		if (ToDoStatus.deleted == toDoTask.getStatus()) {
			return false;
		}
		CurriculumElement el = getCurriculumElement(toDoTask);
		return el != null && secCallback.canEditCurriculumElement(el);
	}

	@Override
	public boolean canRestore(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		if (ToDoStatus.deleted != toDoTask.getStatus()) {
			return false;
		}
		CurriculumElement el = getCurriculumElement(toDoTask);
		return el != null && secCallback.canEditCurriculumElement(el);
	}

	@Override
	public ToDoRight[] getAssigneeRightsOverride(ToDoTask toDoTask) {
		CurriculumElement el = getCurriculumElement(toDoTask);
		return el != null && secCallback.canEditCurriculumElement(el)
				? new ToDoRight[] { ToDoRight.all }
				: null;
	}

}
