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
package org.olat.modules.curriculum.ui.widgets;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskRow;
import org.olat.modules.todo.ui.ToDoTasksWidgetController;

/**
 *
 * Initial date: 12 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumToDoTasksWidgetController extends ToDoTasksWidgetController {

	private final Long curriculumKey;

	public CurriculumToDoTasksWidgetController(UserRequest ureq, WindowControl wControl, Curriculum curriculum) {
		super(ureq, wControl);
		this.curriculumKey = curriculum.getKey();
		initForm(ureq);
	}

	@Override
	public String getId() {
		return "curriculum-todo-widget-v1";
	}

	@Override
	protected String getBaseBusinessPath() {
		return "[CurriculumAdmin:0][Curriculum:" + curriculumKey + "]";
	}

	@Override
	protected ToDoTaskSearchParams createBaseParams() {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		params.setOriginIds(List.of(curriculumKey));
		return params;
	}

	@Override
	protected void doOpenRow(UserRequest ureq, ToDoTaskRow row) {
		if (row.getOriginId() == null || row.getOriginSubPath() == null) return;

		String fullPath = "[CurriculumElement:" + row.getOriginSubPath() + "]"
				+ "[ToDos:0][" + ToDoTaskListController.TYPE_TODO + ":" + row.getKey() + "]";
		doOpen(ureq, fullPath);
	}

}
