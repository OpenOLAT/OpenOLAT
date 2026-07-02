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

import java.text.Collator;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.AccessibleCurriculumSearchParams;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskFilter;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 6 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMangerToDoListController extends ToDoTaskListController {
	
	private Collection<String> subPaths;
	private ToDoTaskSecurityCallback securityCallback;
	private final SelectionValues contextOriginIds;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumMangerToDoListController(UserRequest ureq, WindowControl wControl,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manager_todos", CurriculumElementToDoProvider.TYPE, null, null);

		AccessibleCurriculumSearchParams searchParams = new AccessibleCurriculumSearchParams(getIdentity());
		searchParams.setIncludeImplementationOwnership(false);
		Set<Long> elementKeys = curriculumService.getAccessibleCurriculumKeys(searchParams).curriculumElementKeys();
		subPaths = elementKeys.stream().map(String::valueOf).toList();
		if (subPaths.isEmpty()) {
			// Not existing key to prevent loading all to-dos.
			subPaths = List.of("-1");
		}
		securityCallback = new CurriculumManagerToDoSecurityCallback(secCallback, elementKeys, curriculumService);

		Collator collator = Collator.getInstance(getLocale());
		contextOriginIds = new SelectionValues();
		toDoService.getToDoTasks(createSearchParams()).stream()
				.filter(task -> task.getOriginId() != null && StringHelper.containsNonWhitespace(task.getOriginTitle()))
				.collect(Collectors.toMap(ToDoTask::getOriginId, ToDoTask::getOriginTitle, (t1, t2) -> t1))
				.entrySet().stream()
				.sorted((e1, e2) -> collator.compare(e1.getValue(), e2.getValue()))
				.forEach(e -> contextOriginIds.add(SelectionValues.entry(e.getKey().toString(),
						StringHelper.escapeHtml(e.getValue()))));

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
	protected boolean isDefaultVisible(ToDoTaskCols col) {
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
	protected SelectionValues getFilterOriginIds() {
		return contextOriginIds;
	}

	@Override
	protected String getFilterLabel(ToDoTaskFilter filter) {
		if (filter == ToDoTaskFilter.originId) {
			return translate("curriculum.title");
		}
		return super.getFilterLabel(filter);
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
		return securityCallback;
	}

}
