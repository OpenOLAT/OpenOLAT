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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.manager.CourseCollectionElementToDoTaskProvider;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.course.todo.manager.CourseIndividualToDoTaskProvider;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskRow;
import org.olat.modules.todo.ui.ToDoTaskRowGrouping;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseToDoTaskController extends ToDoTaskListController {

	public static final Collection<String> PROVIDER_TYPES = List.of(
			CourseIndividualToDoTaskProvider.TYPE,
			CourseCollectionElementToDoTaskProvider.TYPE);
	public static final Collection<String> GROUP_PROVIDER_TYPES = List.of(
			CourseCollectionToDoTaskProvider.TYPE);
	private static final String GUIPREF_KEY_LAST_VISIT = "course.todos.last.visit";
	private static final String CMD_ADD_ASSIGNEES = "add.assignees";
	private static final String CMD_TO_COLLECTION = "to.collection";
	
	private FormLink toDoCreateCollectionLink;
	private DropdownItem createDropdown;
	private FormLink toDoCreateIndividualLink;
	
	private StepsMainRunController toDoCreateCollectionCtrl;
	private CloseableModalController cmc;
	private Controller assigneesAddCtrl;
	
	private final RepositoryEntry repositoryEntry;
	private final CourseToDoTaskSecurityCallback secCallback;
	private final CourseToDoTaskRowGrouping rowGrouping;
	private boolean readOnly;
	private final Set<Long> coachedParticipantKeys;
	private Date lastVisitDate;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseToDoService courseToDoService;
	@Autowired
	private CourseCollectionToDoTaskProvider collectionProvider;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public CourseToDoTaskController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, boolean coachToDoTaskEdit) {
		super(ureq, wControl, "course_todos", null, CourseIndividualToDoTaskProvider.TYPE, repositoryEntry.getKey(), null);
		this.repositoryEntry = repositoryEntry;
		this.secCallback = new CourseToDoTaskSecurityCallback();
		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, repositoryEntry);
		readOnly = reSecurity.isReadOnly() || reSecurity.isOnlyPrincipal();
		if (reSecurity.isCoach() && !reSecurity.isEntryAdmin() && !reSecurity.isOnlyPrincipal()) {
			coachedParticipantKeys = repositoryService.getCoachedParticipants(getIdentity(), repositoryEntry).stream()
					.map(Identity::getKey)
					.collect(Collectors.toSet());
			if (!coachToDoTaskEdit) {
				readOnly = true;
			}
		} else {
			coachedParticipantKeys = null;
		}
		rowGrouping = new CourseToDoTaskRowGrouping(coachedParticipantKeys == null);
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			String guiPrefsKey = GUIPREF_KEY_LAST_VISIT + "::" + repositoryEntry.getKey();
			Object pref = guiPrefs.get(CourseToDoTaskController.class, guiPrefsKey);
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
		setAndLoadPersistedPreferences(ureq, "course-todos");
		
		reload(ureq);
	}

	@Override
	protected Date getLastVisitDate() {
		return lastVisitDate;
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return col != ToDoTaskCols.delegated
				&& col != ToDoTaskCols.contextType
				&& col != ToDoTaskCols.contextTitle
				&& col != ToDoTaskCols.contextSubTitle;
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
		tabs.remove(tabMy);
	}
	
	@Override
	protected boolean isShowContextInEditDialog() {
		return false;
	}

	@Override
	protected Collection<String> getTypes() {
		return PROVIDER_TYPES;
	}

	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(repositoryEntry.getKey()));
		searchParams.setTypes(getTypes());
		return searchParams;
	}
	
	@Override
	protected ToDoTaskSearchParams createGroupSearchParams() {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(repositoryEntry.getKey()));
		searchParams.setTypes(GROUP_PROVIDER_TYPES);
		return searchParams;
	}
	
	@Override
	protected void applyFilters(List<ToDoTaskRow> rows) {
		if (coachedParticipantKeys != null) {
			rows.removeIf(row -> {
				if (!CourseCollectionToDoTaskProvider.TYPE.equals(row.getType())) {
					return !row.getAssignees().stream().map(Identity::getKey).anyMatch(assigneeKey -> coachedParticipantKeys.contains(assigneeKey));
				}
				return false;
			});
		}
		super.applyFilters(rows);
	}
	
	@Override
	protected ToDoTaskRowGrouping getToDoTaskRowGrouping() {
		return rowGrouping;
	}

	@Override
	protected ToDoTaskSecurityCallback getSecurityCallback() {
		return secCallback;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!readOnly) {
			toDoCreateCollectionLink = uifactory.addFormLink("course.todo.create.collection", formLayout, Link.BUTTON);
			toDoCreateCollectionLink.setIconLeftCSS("o_icon o_icon_add");
		
			createDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
			createDropdown.setOrientation(DropdownOrientation.right);
			createDropdown.setEmbbeded(true);
		
			toDoCreateIndividualLink = uifactory.addFormLink("course.todo.create.individual", formLayout, Link.LINK);
			toDoCreateIndividualLink.setIconLeftCSS("o_icon o_icon_add");
			createDropdown.addElement(toDoCreateIndividualLink);
		}
	
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == toDoCreateIndividualLink) {
			doCreateToDoTask(ureq);
		} else if (source == toDoCreateCollectionLink) {
			doCreateToDoCollection(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == toDoCreateCollectionCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				reload(ureq);
			}
		} else if (assigneesAddCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assigneesAddCtrl);
		removeAsListenerAndDispose(cmc);
		assigneesAddCtrl = null;
		cmc = null;
	}

	@Override
	protected void doPrimaryTableAction(UserRequest ureq) {
		doCreateToDoCollection(ureq);
	}
	
	@Override
	protected void doSetStatus(UserRequest ureq, ToDoTaskRow row, ToDoStatus status) {
		super.doSetStatus(ureq, row, status);
		if (!CourseCollectionToDoTaskProvider.TYPE.equals(row.getType())) {
			return;
		}
		
		// Set the status of all collection elements e.g. the coach does not see all
		ToDoTaskSearchParams searchParams = createSearchParams();
		searchParams.setCollectionKeys(List.of(row.getKey()));
		if (ToDoStatus.done == status) {
			searchParams.setStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
		} else {
			searchParams.setStatus(List.of(ToDoStatus.done));
		}
		toDoService.getToDoTasks(searchParams).forEach(toDoTaskElement -> {
			ToDoStatus previousStatus = toDoTaskElement.getStatus();
			toDoTaskElement.setStatus(status);
			toDoTaskElement.setContentModifiedDate(new Date());
			toDoService.update(ureq.getIdentity(), toDoTaskElement, previousStatus);
		});
	}
	
	private void doCreateToDoCollection(UserRequest ureq) {
		removeAsListenerAndDispose(toDoCreateCollectionCtrl);
		
		toDoCreateCollectionCtrl = new StepsMainRunController(ureq, getWindowControl(),
				new ToDoCollectionCreateTaskStep(ureq, coachedParticipantKeys != null, repositoryEntry, null, false),
				collectionProvider.createCreateCallback(), null, translate("course.todo.collection.create.title"), "");
		listenTo(toDoCreateCollectionCtrl);
		getWindowControl().pushAsModalDialog(toDoCreateCollectionCtrl.getInitialComponent());
	}

	private void doConvertToToDoCollection(UserRequest ureq, ToDoTaskRow row) {
		removeAsListenerAndDispose(toDoCreateCollectionCtrl);
		
		ToDoTask toDoTask = toDoService.getToDoTask(() -> row.getKey());
		if (toDoTask == null || toDoTask.getStatus() == ToDoStatus.deleted) {
			showWarning("error.not.convertable.deleted");
			return;
		}

		toDoCreateCollectionCtrl = new StepsMainRunController(ureq, getWindowControl(),
				new ToDoCollectionCreateTaskStep(ureq, coachedParticipantKeys != null, repositoryEntry, toDoTask, true),
				collectionProvider.createCreateCallback(), null, translate("course.todo.convert.to.collection"), "");
		listenTo(toDoCreateCollectionCtrl);
		getWindowControl().pushAsModalDialog(toDoCreateCollectionCtrl.getInitialComponent());
	}
	
	private void doAddAssigneesToCollection(UserRequest ureq, ToDoTaskRef toDoTaskRef) {
		if (guardModalController(assigneesAddCtrl)) return;
		
		ToDoTask toDoTask = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTask == null || toDoTask.getStatus() == ToDoStatus.deleted) {
			showWarning("error.not.editable.deleted");
			return;
		}
		
		assigneesAddCtrl = new ToDoCollectionAddAssigneesController(ureq, getWindowControl(), toDoTask, coachedParticipantKeys != null);
		listenTo(assigneesAddCtrl);
		
		String title = translate("course.todo.collection.add.participants");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), assigneesAddCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected ToolsController createToolsCtrl(UserRequest ureq, ToDoTaskRow toDoTaskRow) {
		return new CourseToDoTaskToolsController(ureq, getWindowControl(), toDoTaskRow);
	}
	
	public class CourseToDoTaskToolsController extends ToolsController {

		public CourseToDoTaskToolsController(UserRequest ureq, WindowControl wControl, ToDoTaskRow row) {
			super(ureq, wControl, row);
		}

		@Override
		protected void createTools() {
			if (CourseCollectionToDoTaskProvider.TYPE.equals(row.getType()) && row.canEdit()) {
				addLink("course.todo.collection.add.participants", CMD_ADD_ASSIGNEES, "o_icon o_icon-fw o_icon_add_member");
			}
			if (CourseIndividualToDoTaskProvider.TYPE.equals(row.getType()) && row.canEdit()) {
				addLink("course.todo.convert.to.collection", CMD_TO_COLLECTION, "o_icon o_icon-fw o_icon_group");
			}
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			super.event(ureq, source, event);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_ADD_ASSIGNEES.equals(cmd)) {
					doAddAssigneesToCollection(ureq, row);
				}
				if (CMD_TO_COLLECTION.equals(cmd)) {
					doConvertToToDoCollection(ureq, row);
				}
			}
		}
		
	}

	private final class CourseToDoTaskSecurityCallback implements ToDoTaskSecurityCallback {

		@Override
		public boolean canCreateToDoTasks() {
			return !readOnly;
		}

		@Override
		public boolean canCopy(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return canEdit(toDoTask, creator, assignee, delegatee);
		}

		@Override
		public boolean canEdit(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted != toDoTask.getStatus() && canEdit(toDoTask, creator);
		}
		
		public boolean canEdit(ToDoTask toDoTask, boolean creator) {
			return !readOnly
					&& !toDoTask.isOriginDeleted()
					&& isNotCollectionCreatedByOtherCoach(toDoTask, creator);
		}
		
		private boolean isNotCollectionCreatedByOtherCoach(ToDoTask toDoTask, boolean creator) {
			return !CourseCollectionToDoTaskProvider.TYPE.equals(toDoTask.getType()) || creator || coachedParticipantKeys == null;
		}

		@Override
		public boolean canBulkDeleteToDoTasks() {
			return false;
		}

		@Override
		public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return canEdit(toDoTask, creator, assignee, delegatee);
		}
		
		@Override
		public boolean canRestore(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
			return ToDoStatus.deleted == toDoTask.getStatus() && canEdit(toDoTask, creator);
		}
		
	}

}

