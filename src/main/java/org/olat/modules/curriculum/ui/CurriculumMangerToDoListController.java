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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 6 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMangerToDoListController extends ToDoTaskListController {
	
	private final Collection<String> subPaths;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumMangerToDoListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "manager_todos", CurriculumElementToDoProvider.TYPE, null, null);
		
		subPaths = curriculumService.getAccessibleCurriculumKeys(getIdentity()).curriculumElementKeys().stream()
				.map(String::valueOf)
				.toList();

		initForm(ureq);

		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "curriculum-manager-todos");

		reload(ureq);
	}

	@Override
	protected Date getNewSinceDate() {
		return null;
	}

	@Override
	protected boolean isFilterMyEnabled() {
		return true;
	}

	@Override
	protected boolean isShowContextInEditDialog() {
		return false;
	}

	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.contextType;
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		return toDoService.getTagInfos(tagSearchParams, null);
	}

	@Override
	protected boolean isFilterTabUnassignedEnabled() {
		return true;
	}
	
	@Override
	protected void reorderFilterTabs(List<FlexiFiltersTab> tabs) {
		tabs.remove(tabAll);
		tabs.add(0, tabAll);
	}

	@Override
	protected Collection<String> getTypes() {
		return List.of(CurriculumElementToDoProvider.TYPE);
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(CurriculumElementToDoProvider.TYPE));
		params.setOriginSubPaths(subPaths);
		return params;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return CurriculumManagerSecurityCallback.INSTANCE;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
	}
	
	private final static class CurriculumManagerSecurityCallback implements ToDoTaskSecurityCallback {
		
		private final static CurriculumManagerSecurityCallback INSTANCE = new CurriculumManagerSecurityCallback();

		@Override
		public boolean canCreateToDoTasks() {
			return false;
		}

		@Override
		public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return canEdit(toDoTask, creator, assignee, delegatee);
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus() ;
		}

		@Override
		public boolean canBulkDeleteToDoTasks() {
			return true;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus();
		}

		@Override
		public boolean canRestore(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted == toDoTask.getStatus();
		}
		
	}

}
