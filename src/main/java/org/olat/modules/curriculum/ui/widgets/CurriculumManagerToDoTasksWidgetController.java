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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.AccessibleCurriculumSearchParams;
import org.olat.modules.curriculum.ui.CurriculumManagerToDoSecurityCallback;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskRow;
import org.olat.modules.todo.ui.ToDoTasksWidgetController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 6 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumManagerToDoTasksWidgetController extends ToDoTasksWidgetController {

	private final Set<String> originSubPaths;
	private ToDoTaskSecurityCallback securityCallback;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumManagerToDoTasksWidgetController(UserRequest ureq, WindowControl wControl,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);

		AccessibleCurriculumSearchParams searchParams = new AccessibleCurriculumSearchParams(getIdentity());
		searchParams.setIncludeImplementationOwnership(false);
		Set<Long> elementKeys = curriculumService.getAccessibleCurriculumKeys(searchParams).curriculumElementKeys();
		originSubPaths = elementKeys.stream().map(String::valueOf).collect(Collectors.toSet());
		securityCallback = new CurriculumManagerToDoSecurityCallback(secCallback, elementKeys, curriculumService);

		initForm(ureq);
	}

	@Override
	public String getId() {
		return "curriculum-manager-todo-widget-v1";
	}

	@Override
	protected String getBaseBusinessPath() {
		return "[CurriculumAdmin:0]";
	}

	@Override
	protected ToDoTaskSearchParams createBaseParams() {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		params.setOriginSubPaths(originSubPaths);
		return params;
	}

	@Override
	protected void doOpenRow(UserRequest ureq, ToDoTaskRow row) {
		if (row.getOriginId() == null || row.getOriginSubPath() == null) return;

		String fullPath = "[ToDos:0][" + ToDoTaskListController.TYPE_TODO + ":" + row.getKey() + "]";
		doOpen(ureq, fullPath);
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return securityCallback;
	}

}
