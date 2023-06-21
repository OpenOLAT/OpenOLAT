/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.todo.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoUIFactory.Due;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ToDoTaskListController extends FormBasicController
		implements Activateable2, FlexiTableComponentDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(ToDoTaskListController.class);
	
	public static final String TYPE_TODO = "ToDo";
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_OVERDUE = "Overdue";
	private static final String TAB_ID_RECENTLY = "Recently";
	private static final String TAB_ID_NEW = "New";
	private static final String TAB_ID_DONE = "Done";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_KEY_MY = "my";
	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_GOTO_ORIGIN = "origin";
	
	private final MapperKey avatarMapperKey;
	protected FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabOverdue;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDone;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ToDoTaskDataModel dataModel;
	private VelocityContainer detailsVC;
	
	private CloseableModalController cmc;
	private Controller toToTaskEditCtrl;
	private Controller deleteConfirmationCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;

	private final String createType;
	private final Long createOriginId;
	private final String createOriginSubPath;
	private ToDoTask toDoTaskToDelete;
	private int counter;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private UserManager userManager;

	protected ToDoTaskListController(UserRequest ureq, WindowControl wControl, String pageName,
			MapperKey avatarMapperKey, String createType, Long createOriginId, String createOriginSubPath) {
		super(ureq, wControl, pageName);
		setTranslator(Util.createPackageTranslator(ToDoTaskListController.class, getLocale(), getTranslator()));
		this.avatarMapperKey = avatarMapperKey != null
				? avatarMapperKey
				: mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		this.createType = createType;
		this.createOriginId = createOriginId;
		this.createOriginSubPath = createOriginSubPath;
	}
	
	protected abstract Date getLastVisitDate();

	protected List<String> getFilterContextTypes() {
		return List.of();
	}
	
	protected abstract List<TagInfo> getFilterTags();
	
	protected boolean isNumOfRowsEnabled() {
		return true;
	}
	
	protected boolean isCustomizeColumns() {
		return true;
	}
	
	protected String getEmptyMessageI18nKey() {
		return "task.empty.message";
	}
	
	protected boolean isShowDetails() {
		return true;
	}
	
	protected boolean isOriginDeletedStatusDeleted() {
		return false;
	}

	protected abstract ToDoTaskSearchParams createSearchParams();
	
	protected abstract ToDoTaskSecurityCallback getSecurityCallback();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (isVisible(ToDoTaskCols.id) && ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.id));
		}
		if (isVisible(ToDoTaskCols.creationDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.creationDate));
		}
		if (isVisible(ToDoTaskCols.contentLastModifiedDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.contentLastModifiedDate));
		}
		if (isVisible(ToDoTaskCols.doIt)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.doIt));
		}
		if (isVisible(ToDoTaskCols.title)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.title));
		}
		if (isVisible(ToDoTaskCols.priority)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.priority, new ToDoPriorityCellRenderer(getTranslator())));
		}
		if (isVisible(ToDoTaskCols.expenditureOfWork)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.expenditureOfWork));
		}
		if (isVisible(ToDoTaskCols.startDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.startDate, new DateFlexiCellRenderer(getLocale())));
		}
		if (isVisible(ToDoTaskCols.dueDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.dueDate, new ToDoDueDateCellRenderer()));
		}
		if (isVisible(ToDoTaskCols.due)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.due, new ToDoDueCellRenderer()));
		}
		if (isVisible(ToDoTaskCols.doneDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.doneDate, new DateFlexiCellRenderer(getLocale())));
		}
		if (isVisible(ToDoTaskCols.status)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.status, new ToDoStatusCellRenderer(getTranslator())));
		}
		if (isVisible(ToDoTaskCols.contextType)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.contextType));
		}
		if (isVisible(ToDoTaskCols.contextTitle)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.contextTitle));
		}
		if (isVisible(ToDoTaskCols.assigned)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.assigned));
		}
		if (isVisible(ToDoTaskCols.delegated)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.delegated));
		}
		if (isVisible(ToDoTaskCols.tags)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToDoTaskCols.tags, new TextFlexiCellRenderer(EscapeMode.none)));
		}
		if (isVisible(ToDoTaskCols.deletedDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.deletedDate));
		}
		if (isVisible(ToDoTaskCols.deletedBy)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ToDoTaskCols.deletedBy));
		}
		if (isVisible(ToDoTaskCols.tools)) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(ToDoTaskCols.tools);
			toolsCol.setAlwaysVisible(true);
			toolsCol.setSortable(false);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		dataModel = new ToDoTaskDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_todo_task_list");
		tableEl.setNumOfRowsEnabled(isNumOfRowsEnabled());
		tableEl.setCustomizeColumns(isCustomizeColumns());
		
		if (isShowDetails()) {
			String page = Util.getPackageVelocityRoot(ToDoTaskListController.class) + "/todo_task_list_details.html";
			detailsVC = new VelocityContainer("details_" + counter++, "vc_details", page, getTranslator(), this);
			tableEl.setDetailsRenderer(detailsVC, this);
			tableEl.setMultiDetails(true);
		}
	}
	
	@SuppressWarnings("unused") 
	protected boolean isVisible(ToDoTaskCols col) {
		return true;
	}

	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues myValues = new SelectionValues();
		myValues.add(SelectionValues.entry(FILTER_KEY_MY, translate("filter.my.value")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.my"), ToDoTaskFilter.my.name(), myValues, true));
		
		SelectionValues priorityValues = new SelectionValues();
		addPrioritySVEntry(priorityValues, ToDoPriority.urgent);
		addPrioritySVEntry(priorityValues, ToDoPriority.high);
		addPrioritySVEntry(priorityValues, ToDoPriority.medium);
		addPrioritySVEntry(priorityValues, ToDoPriority.low);
		filters.add(new FlexiTableMultiSelectionFilter(translate("task.priority"), ToDoTaskFilter.priority.name(), priorityValues, true));
		
		SelectionValues dueValues = new SelectionValues();
		dueValues.add(SelectionValues.entry(ToDoDueFilter.overdue.name(), translate("filter.due.overdue")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.today.name(), translate("filter.due.today")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.thisWeek.name(), translate("filter.due.this.week")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.nextWeek.name(), translate("filter.due.next.week")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.next2Weeks.name(), translate("filter.due.next.2.weeks")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.future.name(), translate("filter.due.future")));
		dueValues.add(SelectionValues.entry(ToDoDueFilter.noDueDate.name(), translate("filter.due.anytime")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.due"), ToDoTaskFilter.due.name(), dueValues, true));
		
		SelectionValues statusValues = new SelectionValues();
		addStatusSVEntry(statusValues, ToDoStatus.open);
		addStatusSVEntry(statusValues, ToDoStatus.inProgress);
		addStatusSVEntry(statusValues, ToDoStatus.done);
		addStatusSVEntry(statusValues, ToDoStatus.deleted);
		filters.add(new FlexiTableMultiSelectionFilter(translate("task.status"), ToDoTaskFilter.status.name(), statusValues, true));
		
		List<String> contextTypes = getFilterContextTypes();
		if (!contextTypes.isEmpty()) {
			SelectionValues typeValues = new SelectionValues();
			for (String contextType : contextTypes) {
				ToDoProvider provider = toDoService.getProvider(contextType);
				if (provider != null) {
					String displayName = provider.getDisplayName(getLocale());
					typeValues.add(SelectionValues.entry(contextType, displayName));
				}
			}
			if (typeValues.size() > 0) {
				filters.add(new FlexiTableMultiSelectionFilter(translate("task.context.type"), ToDoTaskFilter.contextType.name(), typeValues, true));
			}
		}
		
		List<TagInfo> tagInfos = getFilterTags();
		if (!tagInfos.isEmpty()) {
			filters.add(new FlexiTableTagFilter(translate("tags"), ToDoTaskFilter.tag.name(), tagInfos, true));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void addPrioritySVEntry(SelectionValues priorityValues, ToDoPriority priority) {
		priorityValues.add(SelectionValues.entry(priority.name(), ToDoUIFactory.getDisplayName(getTranslator(), priority),
				null, "o_icon o_icon-fw " + ToDoUIFactory.getIconCss(priority), null, true));
	}
	
	private void addStatusSVEntry(SelectionValues priorityValues, ToDoStatus status) {
		priorityValues.add(SelectionValues.entry(status.name(), ToDoUIFactory.getDisplayName(getTranslator(), status),
				null, "o_icon o_icon-fw " + ToDoUIFactory.getIconCss(status), null, true));
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		List<String> statusActive = List.of(ToDoStatus.open.name(), ToDoStatus.inProgress.name(), ToDoStatus.done.name());
		tabMy = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_MY,
				translate("tab.my.tasks"),
				TabSelectionBehavior.reloadData,
				List.of(
						FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, List.of(ToDoStatus.open.name(), ToDoStatus.inProgress.name())),
						FlexiTableFilterValue.valueOf(ToDoTaskFilter.my, FILTER_KEY_MY)));
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, statusActive)));
		tabs.add(tabAll);
		
		tabOverdue = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_OVERDUE,
				translate("tab.overdue"),
				TabSelectionBehavior.reloadData,
				List.of(
						FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, statusActive),
						FlexiTableFilterValue.valueOf(ToDoTaskFilter.due, ToDoDueFilter.overdue.name())));
		tabs.add(tabOverdue);
		
		tabRecently = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RECENTLY,
				translate("tab.recently"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, statusActive)));
		tabs.add(tabRecently);
		
		tabNew = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_NEW,
				translate("tab.new"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, statusActive)));
		tabs.add(tabNew);
		
		tabDone = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DONE,
				translate("tab.done"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, ToDoStatus.done.name())));
		tabs.add(tabDone);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ToDoTaskFilter.status, ToDoStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	protected void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq, true);
	}
	
	protected void doSelectFilterTab(FlexiFiltersTab tab) {
		if (getSecurityCallback().canCreateToDoTasks() && (tabDeleted == null || tabDeleted != tab)) {
			tableEl.setEmptyTableSettings(getEmptyMessageI18nKey(), null, "o_icon_todo_task", "task.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings(getEmptyMessageI18nKey(), null, "o_icon_todo_task");
		}
	}
	
	public void reload(UserRequest ureq) {
		loadModel(ureq, true);
	}
	
	private void loadModel(UserRequest ureq, boolean sort) {
		boolean contextTypeVisible = isVisible(ToDoTaskCols.contextType);
		LocalDate now = LocalDate.now();
		
		ToDoTaskSearchParams searchParams = createSearchParams();
		applyFilters(searchParams);
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		Map<ToDoTask, List<ToDoTaskTag>> toDoTaskToTags = toDoService.getToDoTaskTags(searchParams).stream()
				.collect(Collectors.groupingBy(ToDoTaskTag::getToDoTask));
		Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers = toDoService.getToDoTaskGroupKeyToMembers(toDoTasks, ToDoRole.ALL);
		
		List<ToDoTaskRow> rows = new ArrayList<>(toDoTasks.size());
		for (ToDoTask toDoTask : toDoTasks) {
			ToDoTaskRow row = new ToDoTaskRow(toDoTask);
			
			String displayName = ToDoUIFactory.getDisplayName(getTranslator(), toDoTask);
			row.setDisplayName(displayName);
			
			ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork());
			String formattedExpenditureOfWork = ToDoUIFactory.format(expenditureOfWork);
			row.setFormattedExpenditureOfWork(formattedExpenditureOfWork);
			
			updateDueUI(row, toDoTask.getStatus(), now);
			
			if (contextTypeVisible) {
				ToDoProvider provider = toDoService.getProvider(row.getType());
				if (provider != null) {
					row.setTranslatedType(provider.getDisplayName(getLocale()));
				}
			}
			
			List<Tag> tags = toDoTaskToTags.getOrDefault(toDoTask, List.of()).stream().map(ToDoTaskTag::getTag).collect(Collectors.toList());
			row.setTags(tags);
			row.setTagKeys(tags.stream().map(Tag::getKey).collect(Collectors.toSet()));
			row.setFormattedTags(TagUIFactory.getFormattedTags(getLocale(), tags));
			
			ToDoTaskMembers toDoTaskMembers = toDoTaskGroupKeyToMembers.get(toDoTask.getBaseGroup().getKey());
			Set<Identity> creators = toDoTaskMembers.getMembers(ToDoRole.creator);
			row.setCreator(creators.stream().findFirst().orElse(null));
			Set<Identity> modifiers = toDoTaskMembers.getMembers(ToDoRole.modifier);
			row.setModifier(modifiers.stream().findFirst().orElse(null));
			Set<Identity> assignees = toDoTaskMembers.getMembers(ToDoRole.assignee);
			row.setAssignees(assignees);
			if (!assignees.isEmpty()) {
				row.setAssigneesPortraits(createUsersPortraits(ureq, assignees, "task.assigned"));
			}
			Set<Identity> delegatees = toDoTaskMembers.getMembers(ToDoRole.delegatee);
			row.setDelegatees(delegatees);
			if (!delegatees.isEmpty()) {
				row.setDelegateesPortraits(createUsersPortraits(ureq, delegatees, "task.delegated"));
			}
			
			if (isOriginDeletedStatusDeleted() && toDoTask.isOriginDeleted()) {
				row.setStatus(ToDoStatus.deleted);
			}
			if (ToDoStatus.deleted == row.getStatus()) {
				Date deletedDate = toDoTask.getDeletedDate() != null? toDoTask.getDeletedDate(): toDoTask.getOriginDeletedDate();
				row.setDeletedDate(deletedDate);
				Identity deletedBy = toDoTask.getDeletedBy() != null? toDoTask.getDeletedBy(): toDoTask.getOriginDeletedBy();
				row.setDeletedBy(deletedBy);
				if (deletedBy != null) {
					String deletedByName = userManager.getUserDisplayName(deletedBy.getKey());
					row.setDeletedByName(deletedByName);
				}
			}
			
			boolean creator = row.getCreator() != null && row.getCreator().getKey().equals(getIdentity().getKey());
			boolean assignee = row.getAssignees().contains(getIdentity());
			boolean delegatee = row.getDelegatees().contains(getIdentity());
			row.setCanEdit(getSecurityCallback().canEdit(toDoTask, assignee, delegatee));
			row.setCanDelete(getSecurityCallback().canDelete(toDoTask, creator, assignee, delegatee));
			forgeDoItem(row);
			forgeTitleItem(row);
			forgeGoToOriginLink(row);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		if (sort) {
			sortTable();
		}
		rows = limitRows(rows);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void applyFilters(ToDoTaskSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(getLastVisitDate());
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ToDoTaskFilter.status.name() == filter.getFilter()) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ToDoStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
			if (ToDoTaskFilter.priority.name() == filter.getFilter()) {
				List<String> priorities = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (priorities != null && !priorities.isEmpty()) {
					searchParams.setPriorities(priorities.stream().map(ToDoPriority::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setPriorities(null);
				}
			}
			if (ToDoTaskFilter.contextType.name() == filter.getFilter()) {
				List<String> contextTypes = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (contextTypes != null && !contextTypes.isEmpty()) {
					searchParams.setTypes(contextTypes);
				}
				// Do not set the types to null. Probably they are restricted by the list subclass.
			}
			if (ToDoTaskFilter.due.name() == filter.getFilter()) {
				List<String> dueRanges = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (dueRanges != null && !dueRanges.isEmpty()) {
					// copy to prevent loss if filter selection
					dueRanges = new ArrayList<>(dueRanges);
					// No due date
					if (dueRanges.contains(ToDoDueFilter.noDueDate.name())) {
						searchParams.setDueDateNull(true);
					} else {
						searchParams.setDueDateNull(false);
					}
					// All other due filter values
					dueRanges.removeIf(due -> ToDoDueFilter.noDueDate.name().equals(due));
					if (!dueRanges.isEmpty()) {
						Date now = new Date();
						List<DateRange> dueDateRanges = dueRanges.stream()
								.map(ToDoDueFilter::valueOf)
								.map(due -> due.getDateRange(now)).toList();
						searchParams.setDueDateRanges(dueDateRanges);
					} else {
						searchParams.setDueDateRanges(null);
					}
				} else {
					searchParams.setDueDateNull(false);
					searchParams.setDueDateRanges(null);
				}
			}
		}
	}
	
	protected void applyFilters(List<ToDoTaskRow> rows) {
		applyFiltersOfTable(rows);
	}

	private void applyFiltersOfTable(List<ToDoTaskRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ToDoTaskFilter.my.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_MY)) {
					applyFilterMy(rows);
				}
			}
			if (ToDoTaskFilter.status.name() == filter.getFilter() && isOriginDeletedStatusDeleted()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && !values.contains(ToDoStatus.deleted.name())) {
					rows.removeIf(ToDoTaskRow::isOriginDeleted);
				}
			}
			if (ToDoTaskFilter.tag.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableTagFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedTagKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					rows.removeIf(row -> row.getTagKeys() == null || !row.getTagKeys().stream().anyMatch(key -> selectedTagKeys.contains(key)));
				}
			}
			if (ToDoTaskFilter.due.name() == filter.getFilter()) {
				List<String> dueRanges = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (dueRanges != null && !dueRanges.isEmpty()) {
					/// Done task have never a due
					rows.removeIf(row -> !ToDoStatus.STATUS_OVERDUE.contains(row.getStatus()));
				}
			}
		}
	}
	
	protected void applyFilterMy(List<ToDoTaskRow> rows) {
		rows.removeIf(row -> !row.getAssignees().contains(getIdentity()) && !row.getDelegatees().contains(getIdentity()));
	}
	
	private void sortTable() {
		SortKey sortKey = getSortKey();
		if (sortKey != null) {
			tableEl.sort(sortKey);
		}
	}
	
	protected SortKey getSortKey() {
		return getSortKeyByTabs();
	}

	private SortKey getSortKeyByTabs() {
		if (tableEl.getSelectedFilterTab() == null || tableEl.getSelectedFilterTab() == tabRecently) {
			return new SortKey(ToDoTaskCols.contentLastModifiedDate.name(), false);
		} else if (tableEl.getSelectedFilterTab() == tabMy || tableEl.getSelectedFilterTab() == tabAll
				|| tableEl.getSelectedFilterTab() == tabOverdue || tableEl.getSelectedFilterTab() == tabDone) {
			return new SortKey(ToDoTaskCols.dueDate.name(), true);
		} else if (tableEl.getSelectedFilterTab() == tabDeleted) {
			return new SortKey(ToDoTaskCols.title.name(), true);
		}
		return null;
	}
	
	private List<ToDoTaskRow> limitRows(List<ToDoTaskRow> rows) {
		Integer maxRows = getMaxRows();
		if (maxRows != null && rows.size() > maxRows.intValue()) {
			rows = rows.subList(0, maxRows.intValue()-1);
		}
		return rows;
	}
	
	protected Integer getMaxRows() {
		return null;
	}

	private void updateDueUI(ToDoTaskRow row, ToDoStatus status, LocalDate now) {
		Due due = ToDoUIFactory.getDue(getTranslator(), DateUtils.toLocalDate(row.getDueDate()), now, status);
		row.setDue(due.name());
		row.setOverdue(due.overdue());
	}

	private UsersPortraitsComponent createUsersPortraits(UserRequest ureq, Set<Identity> members, String ariaI18nKey) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(members));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + counter++, flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate(ariaI18nKey));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(10);
		usersPortraitCmp.setUsers(portraitUsers);
		return usersPortraitCmp;
	}

	private void forgeDoItem(ToDoTaskRow row) {
		if (!row.canEdit()) {
			return;
		}
		
		MultipleSelectionElement doEl = uifactory.addCheckboxesHorizontal("task.do." + counter++, null, flc, new String[] {"do"}, new String[] {""});
		doEl.setElementCssClass("o_todo_task_check");
		doEl.setAjaxOnly(true);
		doEl.addActionListener(FormEvent.ONCHANGE);
		if (ToDoStatus.done == row.getStatus()) {
			doEl.select(doEl.getKey(0), true);
		}
		doEl.setUserObject(row);
		row.setDoItem(doEl);
	}

	private void forgeTitleItem(ToDoTaskRow row) {
		if (row.canEdit()) {
			FormLink link = uifactory.addFormLink("select_" + counter++, CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);	
			link.setUserObject(row);
			row.setTitleItem(link);
		} else {
			StaticTextElement titleItem = uifactory.addStaticTextElement("title_" + counter++, "", flc);
			row.setTitleItem(titleItem);
		}
		updateTitleItemUI(row);
	}
	
	private void updateTitleItemUI(ToDoTaskRow row) {
		String title = ToDoStatus.done == row.getStatus() || ToDoStatus.deleted == row.getStatus()
				? "<span class=\"o_todo_title_done_cell\">" + row.getDisplayName() + "</span>"
				: row.getDisplayName	();
		if (row.getTitleItem() instanceof FormLink link) {
			link.setI18nKey(title);
		}
		if (row.getTitleItem() instanceof StaticTextElement ele) {
			ele.setValue(title);
		}
	}
	
	private void forgeGoToOriginLink(ToDoTaskRow row) {
		if (isVisible(ToDoTaskCols.contextTitle) && StringHelper.containsNonWhitespace(row.getOriginTitle())) {
			FormLink link = uifactory.addFormLink("origin_" + row.getKey(), CMD_GOTO_ORIGIN, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(row.getOriginTitle());
			link.setUserObject(row);
			row.setGoToOriginLink(link);
		}
	}
	
	private void forgeToolsLink(ToDoTaskRow row) {
		if (row.canEdit() || row.canDelete()) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			FlexiFiltersTab tab = tableEl.getFilterTabById(type);
			if (tab != null) {
				selectFilterTab(ureq, tab);
			} else {
				selectFilterTab(ureq, tabAll);
				if (TYPE_TODO.equals(type)) {
					Long key = getToDoTaskKey(entry.getOLATResourceable().getResourceableId());
					activate(ureq, key);
				}
			}
		}
	}
	
	protected Long getToDoTaskKey(Long acrivateOresKey) {
		return acrivateOresKey;
	}
	
	private void activate(UserRequest ureq, Long key) {
		ToDoTaskRow row = dataModel.getObjectByKey(key);
		if (row != null) {
			int index = dataModel.getObjects().indexOf(row);
			if (index >= 1 && tableEl.getPageSize() > 1) {
				int page = index / tableEl.getPageSize();
				tableEl.setPage(page);
			}
			doShowDetails(ureq, row);
			tableEl.expandDetails(index);
		}
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof OpenArtefactEvent) {
			fireEvent(ureq, event);
		} else if (event instanceof ToDoTaskEditEvent editEvent) {
			doEditToDoTask(ureq, editEvent.getToDoTask());
		} else if (source == toToTaskEditCtrl) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			loadModel(ureq, false);
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(toToTaskEditCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		toToTaskEditCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent sEvent) {
				String cmd = sEvent.getCommand();
				ToDoTaskRow row = dataModel.getObject(sEvent.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doEditToDoTask(ureq, row);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreateToDoTask(ureq);
			} else if(event instanceof DetailsToggleEvent) {
				DetailsToggleEvent dte = (DetailsToggleEvent)event;
				if (dte.isVisible()) {
					ToDoTaskRow row = dataModel.getObject(dte.getRowIndex());
					if(row != null) {
						doShowDetails(ureq, row);
					}
				}
			}
		} else if (source instanceof MultipleSelectionElement multiEl) {
			if (multiEl.getUserObject() instanceof ToDoTaskRow row) {
				doSetDone(row, multiEl.isAtLeastSelected(1));
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ToDoTaskRow row) {
				doEditToDoTask(ureq, row);
			} else if (CMD_GOTO_ORIGIN.equals(link.getCmd()) && link.getUserObject() instanceof ToDoTaskRow row) {
				doOpenOrigin(ureq, row);
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ToDoTaskRow row) {
				doOpenTools(ureq, row, link);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	protected void doCreateToDoTask(UserRequest ureq) {
		if (guardModalController(toToTaskEditCtrl)) return;
		
		ToDoProvider provider = toDoService.getProvider(createType);
		toToTaskEditCtrl = provider.createCreateController(ureq, getWindowControl(), getIdentity(), createOriginId, createOriginSubPath);
		if (toToTaskEditCtrl == null) {
			return;
		}
		listenTo(toToTaskEditCtrl);
		
		String title = translate("task.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), toToTaskEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditToDoTask(UserRequest ureq, ToDoTaskRef toDoTaskRef) {
		if (guardModalController(toToTaskEditCtrl)) return;
		
		ToDoTask toDoTask = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTask == null) {
			return;
		}
		
		ToDoProvider provider = toDoService.getProvider(toDoTask.getType());
		toToTaskEditCtrl = provider.createEditController(ureq, getWindowControl(), toDoTask);
		if (toToTaskEditCtrl == null) {
			return;
		}
		listenTo(toToTaskEditCtrl);
		
		String title = translate("task.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), toToTaskEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doShowDetails(UserRequest ureq, ToDoTaskRow row) {
		ToDoTask toDoTask = toDoService.getToDoTask(row);
		if (toDoTask == null) {
			return;
		}
		
		ToDoProvider provider = toDoService.getProvider(toDoTask.getType());
		FormBasicController toToTaskDetailCtrl = provider.createDetailController(ureq, getWindowControl(), mainForm, getSecurityCallback(), toDoTask,
				row.getTags(), row.getModifier(), row.getAssignees(), row.getDelegatees());
		if (toToTaskDetailCtrl == null) {
			return;
		}
		listenTo(toToTaskDetailCtrl);
		// Add as form item to catch the edit event...
		flc.add(toToTaskDetailCtrl.getInitialFormItem());
		
		// ... and add the component to the details container.
		String detailsComponentName = "details_" + counter++;
		row.setDetailsComponentName(detailsComponentName);
		detailsVC.put(detailsComponentName, toToTaskDetailCtrl.getInitialComponent());
	}

	private void doSetDone(ToDoTaskRow row, boolean done) {
		ToDoStatus status = done? ToDoStatus.done: ToDoStatus.open;
		ToDoProvider provider = toDoService.getProvider(row.getType());
		provider.upateStatus(getIdentity(), row, row.getOriginId(), row.getOriginSubPath(), status);
		row.setStatus(status);
		row.setDoneDate(done? new Date(): null);
		row.getDoItem().select(row.getDoItem().getKey(0), done);
		updateTitleItemUI(row);
		updateDueUI(row, status, LocalDate.now());
		tableEl.reset(false, false, true);
	}
	
	private void doConfirmDelete(UserRequest ureq, ToDoTaskRef toDoTaskRef) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		toDoTaskToDelete = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTaskToDelete == null || ToDoStatus.deleted == toDoTaskToDelete.getStatus()) {
			return;
		}
		
		ToDoProvider provider = toDoService.getProvider(toDoTaskToDelete.getType());
		deleteConfirmationCtrl = provider.createDeleteConfirmationController(ureq, getWindowControl(), getLocale(), toDoTaskToDelete);
		if (deleteConfirmationCtrl == null) {
			return;
		}
		
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("task.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq) {
		ToDoProvider provider = toDoService.getProvider(toDoTaskToDelete.getType());
		provider.deleteToDoTaskSoftly(getIdentity(), toDoTaskToDelete);
		loadModel(ureq, false);
	}
	
	private void doOpenOrigin(UserRequest ureq, ToDoTaskRow row) {
		ToDoTask toDoTask = toDoService.getToDoTask(row);
		if (toDoTask == null) {
			return;
		}

		ToDoProvider provider = toDoService.getProvider(toDoTask.getType());
		if (provider == null) {
			return;
		}
		String businessPath = provider.getBusinessPath(toDoTask);
		if (businessPath == null) {
			return;
		}
		
		try {
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	private void doOpenTools(UserRequest ureq, ToDoTaskRow toDoTaskRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), toDoTaskRow);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ToDoTaskRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ToDoTaskRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("todo_task_tools");
			
			if (row.canEdit()) {
				addLink("edit", CMD_EDIT, "o_icon o_icon_edit");
			}
			
			if (row.canEdit() && row.canEdit()) {
				mainVC.contextPut("divider", Boolean.TRUE);
			}
			
			if (row.canEdit()) {
				addLink("delete", CMD_DELETE, "o_icon " + ToDoUIFactory.getIconCss(ToDoStatus.deleted));
			}
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					doEditToDoTask(ureq, row);
				} else if(CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}

}
