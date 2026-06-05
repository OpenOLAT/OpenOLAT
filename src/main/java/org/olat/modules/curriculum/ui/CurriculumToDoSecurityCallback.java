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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.todo.ToDoTask;

/**
 *
 * Initial date: 5 Jun 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumToDoSecurityCallback extends CurriculumElementToDoSecurityCallback {

	private final Map<String, CurriculumElement> elementBySubPath;

	public CurriculumToDoSecurityCallback(CurriculumSecurityCallback secCallback, Curriculum curriculum,
			CurriculumService curriculumService) {
		super(secCallback, null);
		CurriculumElementStatus[] notDeleted = Arrays.stream(CurriculumElementStatus.values())
				.filter(s -> s != CurriculumElementStatus.deleted)
				.toArray(CurriculumElementStatus[]::new);
		elementBySubPath = curriculumService
				.getCurriculumElements(curriculum, notDeleted)
				.stream()
				.collect(Collectors.toMap(el -> String.valueOf(el.getKey()), el -> el));
	}

	@Override
	protected CurriculumElement getCurriculumElement(ToDoTask toDoTask) {
		return toDoTask.getOriginSubPath() != null ? elementBySubPath.get(toDoTask.getOriginSubPath()) : null;
	}

	@Override
	public boolean canCreateToDoTasks() {
		return false;
	}

	@Override
	public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		return false;
	}

	@Override
	public boolean canBulkDeleteToDoTasks() {
		return true;
	}

}
