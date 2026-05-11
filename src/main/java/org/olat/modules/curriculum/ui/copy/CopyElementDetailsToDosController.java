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
package org.olat.modules.curriculum.ui.copy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyToDos;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.copy.CopyElementDetailsToDosTableModel.CopyToDoCols;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.ui.ToDoDueCellRenderer;
import org.olat.modules.todo.ui.ToDoDueDateCellRenderer;
import org.olat.modules.todo.ui.ToDoPriorityCellRenderer;
import org.olat.modules.todo.ui.ToDoStatusCellRenderer;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.modules.todo.ui.ToDoUIFactory.Due;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.UsersPortraitsComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 12 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementDetailsToDosController extends FormBasicController {

	private FlexiTableElement tableEl;
	private CopyElementDetailsToDosTableModel tableModel;

	private int counter = 0;
	private final boolean copyEnabled;
	private final CopyElementContext context;
	private final CurriculumElement curriculumElement;

	@Autowired
	private CurriculumElementToDoProvider curriculumElementToDoProvider;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserPortraitService userPortraitService;

	public CopyElementDetailsToDosController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElement curriculumElement, CopyElementContext context) {
		super(ureq, wControl, LAYOUT_CUSTOM, "element_details_todos", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(),
				Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator())));
		this.curriculumElement = curriculumElement;
		this.context = context;
		this.copyEnabled = context.getToDosCopySetting() != CopyToDos.dont;

		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyToDoCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.activity,
				new CopySettingCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.priority,
				new ToDoPriorityCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.dateKind));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.dueDate,
				new ToDoDueDateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.due,
				new ToDoDueCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.status,
				new ToDoStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.assigned));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.delegated));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyToDoCols.tags,
				new TextFlexiCellRenderer(EscapeMode.none)));

		tableModel = new CopyElementDetailsToDosTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "todosTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("empty.todos")
				.withIconCss("o_icon_todo_task")
				.build(), false);
		if (copyEnabled) {
			tableEl.setMultiSelect(true);
		}
	}

	private void loadModel(UserRequest ureq) {
		if (curriculumElement.getCurriculum() == null) {
			tableModel.setObjects(List.of());
			tableEl.reset(true, true, true);
			return;
		}

		ToDoTaskSearchParams searchParams = curriculumElementToDoProvider.createActiveSearchParams(
				List.of(curriculumElement.getKey().toString()));

		List<ToDoTask> tasks = toDoService.getToDoTasks(searchParams);
		if (tasks.isEmpty()) {
			tableModel.setObjects(List.of());
			tableEl.reset(true, true, true);
			return;
		}

		Map<ToDoTask, List<ToDoTaskTag>> tagsByTask = toDoService.getToDoTaskTags(searchParams).stream()
				.collect(Collectors.groupingBy(ToDoTaskTag::getToDoTask));
		boolean copyAssignments = context.getToDosCopySetting() == CopyToDos.todosWithAssignments;
		Map<Long, ToDoTaskMembers> membersByGroup = copyAssignments
				? toDoService.getToDoTaskGroupKeyToMembers(tasks, ToDoRole.ASSIGNEE_DELEGATEE)
				: Map.of();

		CopyResources copySetting = context.getToDosCopySetting() != CopyToDos.dont
				? CopyResources.resource
				: CopyResources.dont;

		LocalDate today = LocalDate.now();
		Formatter formatter = Formatter.getInstance(getLocale());
		List<CopyElementDetailsToDosRow> rows = new ArrayList<>(tasks.size());

		for (ToDoTask task : tasks) {
			String dateKind = task.getRelativeDates() != null
					? translate("copy.todos.date.relative")
					: translate("copy.todos.date.absolute");
			String formattedDueDate = task.getDueDate() != null ? formatter.formatDate(task.getDueDate()) : "";
			String due = "";
			Boolean overdue = null;
			if (task.getDueDate() != null) {
				LocalDate dueLocalDate = DateUtils.toLocalDate(task.getDueDate());
				Due dueResult = ToDoUIFactory.getDue(getTranslator(), dueLocalDate, today, task.getStatus());
				due = dueResult.name();
				overdue = dueResult.overdue();
			}
			String formattedTags = TagUIFactory.getFormattedTags(getLocale(),
					tagsByTask.getOrDefault(task, List.of()).stream().map(ToDoTaskTag::getTag).toList());

			CopyElementDetailsToDosRow row = new CopyElementDetailsToDosRow(task, copySetting, dateKind);
			row.setStatus(ToDoStatus.open);
			row.setFormattedDueDate(formattedDueDate);
			row.setDue(due);
			row.setOverdue(overdue);
			row.setFormattedTags(formattedTags);

			if (copyAssignments) {
				ToDoTaskMembers members = task.getBaseGroup() != null
						? membersByGroup.get(task.getBaseGroup().getKey())
						: null;
				if (members != null) {
					Set<Identity> assignees = members.getMembers(ToDoRole.assignee);
					if (!assignees.isEmpty()) {
						row.setAssigneesPortraits(createUsersPortraits(ureq, assignees, "task.assigned"));
					}
					Set<Identity> delegatees = members.getMembers(ToDoRole.delegatee);
					if (!delegatees.isEmpty()) {
						row.setDelegateesPortraits(createUsersPortraits(ureq, delegatees, "task.delegated"));
					}
				}
			}

			rows.add(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		if (copyEnabled) {
			Set<Long> storedKeys = context.getSelectedToDoTaskKeys(curriculumElement.getKey());
			if (storedKeys == null) {
				tableEl.selectAll();
				saveToContext();
			} else {
				Set<Integer> indices = new HashSet<>();
				for (int i = 0; i < rows.size(); i++) {
					CopyElementDetailsToDosRow row = rows.get(i);
					if (storedKeys.contains(row.getKey())) {
						indices.add(i);
					} else {
						row.setCopySetting(CopyResources.dont);
					}
				}
				tableEl.setMultiSelectedIndex(indices);
			}
		}
	}

	private UsersPortraitsComponent createUsersPortraits(UserRequest ureq, Set<Identity> members, String ariaI18nKey) {
		List<PortraitUser> portraitUsers = userPortraitService.createPortraitUsers(getLocale(), new ArrayList<>(members));
		UsersPortraitsComponent cmp = UserPortraitFactory.createUsersPortraits(ureq, "users_" + counter++, flc.getFormItemComponent());
		cmp.setAriaLabel(translate(ariaI18nKey));
		cmp.setSize(PortraitSize.small);
		cmp.setMaxUsersVisible(10);
		cmp.setUsers(portraitUsers);
		return cmp;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && event instanceof SelectionEvent se) {
			String cmd = se.getCommand();
			if (FlexiTableElement.ROW_CHECKED_EVENT.equals(cmd)
					|| FlexiTableElement.ROW_UNCHECKED_EVENT.equals(cmd)) {
				syncRowCopySettings();
				tableEl.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void syncRowCopySettings() {
		List<CopyElementDetailsToDosRow> rows = tableModel.getObjects();
		for (int i = 0; i < rows.size(); i++) {
			rows.get(i).setCopySetting(tableEl.isMultiSelectedIndex(i)
					? CopyResources.resource
					: CopyResources.dont);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void saveToContext() {
		if (!copyEnabled) return;
		Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
		Set<Long> selectedKeys = new HashSet<>();
		for (Integer idx : selectedIndices) {
			CopyElementDetailsToDosRow row = tableModel.getObject(idx.intValue());
			if (row != null) {
				selectedKeys.add(row.getKey());
			}
		}
		context.setSelectedToDoTaskKeys(curriculumElement.getKey(), selectedKeys);
	}
	
}
