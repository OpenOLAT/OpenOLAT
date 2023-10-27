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
package org.olat.modules.todo.ui;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.quality.ui.QualityToDoListController;
import org.olat.modules.todo.ToDoModule;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoUserToolListController extends ToDoTaskListController {
	
	public static final String GUIPREF_KEY_LAST_VISIT = "personal.todos.last.visit";
	
	private FormLink createLink;
	
	private final boolean canCreateToDoTasks;
	private Date lastVisitDate;

	@Autowired
	private ToDoModule toDoModule;
	
	protected ToDoUserToolListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "user_tool", null, PersonalToDoProvider.TYPE, ureq.getIdentity().getKey(), null);
		canCreateToDoTasks = toDoModule.canCreatePersonalToDoTasks(ureq.getUserSession().getRoles());
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(QualityToDoListController.class, GUIPREF_KEY_LAST_VISIT);
			if (pref instanceof String prefDate) {
				try {
					lastVisitDate = Formatter.parseDatetime(prefDate);
				} catch (ParseException e) {
					//
				}
			}
			
			String lastVisit = Formatter.formatDatetime(new Date());
			guiPrefs.putAndSave(QualityToDoListController.class, GUIPREF_KEY_LAST_VISIT, lastVisit);
		}
		
		initForm(ureq);
		
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
		doSelectFilterTab(null);
		setAndLoadPersistedPreferences(ureq, "todo-user-tool");
		
		reload(ureq);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}
	
	@Override
	protected boolean isFilterMyEnabled() {
		return false;
	}
	
	@Override
	protected List<String> getFilterContextTypes() {
		return getEnabledTypes();
	}

	@Override
	protected List<TagInfo> getFilterTags() {
		return toDoService.getTagInfos(createSearchParams(), null);
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
	protected boolean isOriginDeletedStatusDeleted() {
		return true;
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(getEnabledTypes());
		searchParams.setAssigneeOrDelegatee(getIdentity());
		searchParams.setAssigneeRightsNull(Boolean.FALSE);
		return searchParams;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return new ToDoUserToolSecurityCallback();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		if (canCreateToDoTasks) {
			createLink = uifactory.addFormLink("task.create", formLayout, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon_add");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			doCreateToDoTask(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<String> getEnabledTypes() {
		return toDoService.getProviders().stream().filter(ToDoProvider::isEnabled).map(ToDoProvider::getType).toList();
	}
	
	private final class ToDoUserToolSecurityCallback implements ToDoTaskSecurityCallback {

		@Override
		public boolean canCreateToDoTasks() {
			return canCreateToDoTasks;
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus()
					&& !toDoTask.isOriginDeleted()
					&& ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.edit);
		}
		
		@Override
		public boolean canBulkDeleteToDoTasks() {
			return true;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus()
					&& !toDoTask.isOriginDeleted()
					&& ToDoRight.contains(toDoTask.getAssigneeRights(), ToDoRight.delete);
		}
		
	}

}
