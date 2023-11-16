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
package org.olat.course.todo.ui;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 14 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToDoTaskController extends ToDoTaskListController {

	private static final String GUIPREF_KEY_LAST_VISIT = "course.node.todos.last.visit";
	private static final CourseNodeSecurityCallback SECURITY_CALLBACK = new CourseNodeSecurityCallback();
	
	private FormToggle enabledEl;
	
	private final RepositoryEntry repositoryEntry;
	private final CourseNodeReminderProvider reminderProvider;
	private final boolean editor;
	private final CourseNode courseNode;
	private Date lastVisitDate;

	public CourseNodeToDoTaskController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
			CourseNodeReminderProvider reminderProvider, boolean editor) {
		super(ureq, wControl, "node_todos", null, null, null, null);
		this.repositoryEntry = repositoryEntry;
		this.reminderProvider = reminderProvider;
		this.editor = editor;
		
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		courseNode = editor
				? course.getEditorTreeModel().getCourseNode(reminderProvider.getCourseNodeIdent())
				: course.getRunStructure().getNode(reminderProvider.getCourseNodeIdent());
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(CourseNodeToDoTaskController.class, GUIPREF_KEY_LAST_VISIT + "::" + repositoryEntry.getKey() + "::" + reminderProvider.getCourseNodeIdent());
			if (pref instanceof String prefDate) {
				try {
					lastVisitDate = Formatter.parseDatetime(prefDate);
				} catch (ParseException e) {
					//
				}
			}
		}
		
		initForm(ureq);
		
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "course-node-todos");
		
		reload(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer configsCont = FormLayoutContainer.createDefaultFormLayout("configs", getTranslator());
		configsCont.setRootForm(mainForm);
		formLayout.add("configs", configsCont);
		
		boolean on = courseNode.getModuleConfiguration().getBooleanSafe(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED);
		enabledEl = uifactory.addToggleButton("todo.enabled", "todo.generation.enabled", translate("on"),
				translate("off"), configsCont);
		enabledEl.toggle(on);
		enabledEl.setEnabled(editor);
		
		super.initForm(formLayout, listener, ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			courseNode.getModuleConfiguration().setBooleanEntry(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED, enabledEl.isOn());
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.contextTitle
				&& col != ToDoTaskCols.contextSubTitle;
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		return List.of();
	}

	@Override
	protected Collection<String> getTypes() {
		return reminderProvider.getToDoProviderTypes();
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(repositoryEntry.getKey()));
		searchParams.setOriginSubPaths(List.of(reminderProvider.getCourseNodeIdent()));
		searchParams.setTypes(reminderProvider.getToDoProviderTypes());
		return searchParams;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return SECURITY_CALLBACK;
	}
	
	private static final class CourseNodeSecurityCallback implements ToDoTaskSecurityCallback {

		@Override
		public boolean canCreateToDoTasks() {
			return false;
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean assignee, boolean delegatee) {
			return false;
		}

		@Override
		public boolean canBulkDeleteToDoTasks() {
			return false;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return false;
		}
		
	}

}
