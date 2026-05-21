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
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.AccessibleCurriculumSearchParams;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 13 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumToDoListController extends ToDoTaskListController {

	private final Long curriculumKey;
	private List<String> subPaths;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumToDoListController(UserRequest ureq, WindowControl wControl, Curriculum curriculum) {
		super(ureq, wControl, "manager_todos", CurriculumElementToDoProvider.TYPE, null, null);

		this.curriculumKey = curriculum.getKey();
		AccessibleCurriculumSearchParams searchParams = new AccessibleCurriculumSearchParams(getIdentity());
		searchParams.setIncludeImplementationOwnership(false);
		searchParams.setCurriculums(List.of(curriculum));
		subPaths = curriculumService.getAccessibleCurriculumKeys(searchParams).curriculumElementKeys().stream()
				.map(String::valueOf)
				.toList();
		if (subPaths.isEmpty()) {
			// Not existing key to prevent loading all to-dos.
			subPaths = List.of("-1");
		}

		initForm(ureq);

		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "curriculum-todos");

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
	protected boolean isDefaultVisible(ToDoTaskCols col) {
		if (col == ToDoTaskCols.contextTitle) {
			return false;
		}
		if (col == ToDoTaskCols.contextSubTitle) {
			return true;
		}
		return super.isDefaultVisible(col);
	}

	@Override
	protected String getColumnLabel(ToDoTaskCols col) {
		return switch (col) {
			case contextTitle -> translate("curriculum.title");
			case contextSubTitle -> translate("curriculum.element.todo.element");
			default -> null;
		};
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
		params.setOriginIds(List.of(curriculumKey));
		params.setOriginSubPaths(subPaths);
		return params;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return CurriculumSecurityCallbackImpl.INSTANCE;
	}

	private final static class CurriculumSecurityCallbackImpl implements ToDoTaskSecurityCallback {

		private final static CurriculumSecurityCallbackImpl INSTANCE = new CurriculumSecurityCallbackImpl();

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
			return ToDoStatus.deleted != toDoTask.getStatus();
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
