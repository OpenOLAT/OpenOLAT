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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.manager.CourseCollectionElementToDoTaskProvider;
import org.olat.course.todo.manager.CourseIndividualToDoTaskProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseMyToDoTaskController extends ToDoTaskListController {

	private static final String GUIPREF_KEY_LAST_VISIT = "course.my.todos.last.visit";
	
	private final RepositoryEntry repositoryEntry;
	private final CourseMyToDoTaskSecurityCallback secCallback;
	private final ArrayList<String> poviderTypes;
	private Date lastVisitDate;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseToDoService courseToDoService;

	public CourseMyToDoTaskController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "course_my_todos", null, null, null, null);
		this.repositoryEntry = repositoryEntry;
		this.secCallback = new CourseMyToDoTaskSecurityCallback();
		
		poviderTypes = new ArrayList<>(courseToDoService.getCourseNodeToDoTaskProviderTypes());
		poviderTypes.add(CourseIndividualToDoTaskProvider.TYPE);
		poviderTypes.add(CourseCollectionElementToDoTaskProvider.TYPE);
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			String guiPrefsKey = GUIPREF_KEY_LAST_VISIT + "::" + repositoryEntry.getKey();
			Object pref = guiPrefs.get(CourseMyToDoTaskController.class, guiPrefsKey);
			if (pref instanceof String prefDate) {
				try {
					lastVisitDate = Formatter.parseDatetime(prefDate);
				} catch (ParseException e) {
					//
				}
			}
			
			String lastVisit = Formatter.formatDatetime(new Date());
			guiPrefs.putAndSave(CourseNodeToDoTaskController.class, guiPrefsKey, lastVisit);
		}
		
		initForm(ureq);
		
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "course-my-todos");
		
		reload(ureq);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.assigned
				&& col != ToDoTaskCols.delegated
				&& col != ToDoTaskCols.contextType
				&& col != ToDoTaskCols.contextTitle;
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		ToDoTaskSearchParams tagSearchParams = courseToDoService.createCourseTagSearchParams(repositoryEntry);
		return toDoService.getTagInfos(tagSearchParams, null);
	}
	
	@Override
	protected boolean isFilterMyEnabled() {
		return false;
	}
	
	@Override
	protected void reorderFilterTabs(List<FlexiFiltersTab> tabs) {
		tabs.remove(tabAll);
		tabs.add(tabs.size()-1, tabAll);
	}
	
	@Override
	protected FlexiFiltersTab getDefaultFilterTab() {
		return tabMy;
	}
	
	@Override
	protected boolean isShowSingleAssigneeInEditDialog() {
		return false;
	}

	@Override
	protected Collection<String> getTypes() {
		return poviderTypes;
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(repositoryEntry.getKey()));
		searchParams.setTypes(getTypes());
		searchParams.setAssigneeOrDelegatee(List.of(getIdentity()));
		return searchParams;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return secCallback;
	}

	private final class CourseMyToDoTaskSecurityCallback implements ToDoTaskSecurityCallback {

		@Override
		public boolean canCreateToDoTasks() {
			return false;
		}

		@Override
		public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return false;
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus()
					&& !toDoTask.isOriginDeleted()
					&& ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.EDIT_CHILDREN);
		}
		
		@Override
		public boolean canBulkDeleteToDoTasks() {
			return false;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus()
					&& !toDoTask.isOriginDeleted()
					&& ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.delete);
		}
		
	}

}

