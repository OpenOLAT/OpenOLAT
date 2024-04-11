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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
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
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionFilter;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjDecisionRef;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjFileFilter;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjDecisionDataModel.DecisionCols;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
abstract class ProjDecisionListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RECENTLY = "Recently";
	private static final String TAB_ID_NEW = "New";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_KEY_MY = "my";
	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLink createLink;
	private FormLink bulkDeleteButton;
	private FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabRecently;
	private FlexiFiltersTab tabNew;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjDecisionDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjDecisionEditController decisionEditCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private ConfirmationController bulkDeleteConfirmationCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	protected final ProjectBCFactory bcFactory;
	protected final ProjProject project;
	protected final ProjProjectSecurityCallback secCallback;
	private final Date lastVisitDate;
	private final MapperKey avatarMapperKey;
	
	@Autowired
	protected ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjDecisionListController(UserRequest ureq, WindowControl wControl, String pageName, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate, MapperKey avatarMapperKey) {
		super(ureq, wControl, pageName);
		this.bcFactory = bcFactory;
		this.project = project;
		this.secCallback = secCallback;
		this.lastVisitDate = lastVisitDate;
		this.avatarMapperKey = avatarMapperKey;
	}
	
	protected abstract boolean isFullTable();

	protected abstract boolean isVisible(DecisionCols col);
	
	protected abstract Integer getNumLastModified();

	protected abstract void onModelLoaded();
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (isVisible(DecisionCols.id) &&  ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.id));
		}
		if (isVisible(DecisionCols.displayName)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DecisionCols.displayName, CMD_SELECT));
		}
		if (isVisible(DecisionCols.decisionDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DecisionCols.decisionDate));
		}
		if (isVisible(DecisionCols.involved)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DecisionCols.involved));
		}
		if (isVisible(DecisionCols.details)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DecisionCols.details));
		}
		if (isVisible(DecisionCols.tags)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DecisionCols.tags, new TextFlexiCellRenderer(EscapeMode.none)));
		}
		if (isVisible(DecisionCols.creationDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.creationDate));
		}
		if (isVisible(DecisionCols.lastModifiedDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.lastModifiedDate));
		}
		if (isVisible(DecisionCols.lastModifiedBy)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.lastModifiedBy));
		}
		if (isVisible(DecisionCols.deletedDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.deletedDate));
		}
		if (isVisible(DecisionCols.deletedDate)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DecisionCols.deletedDate));
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(DecisionCols.tools);
			toolsCol.setAlwaysVisible(true);
			toolsCol.setSortable(false);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		dataModel = new ProjDecisionDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(isFullTable());
		tableEl.setCustomizeColumns(isFullTable());
		if (isFullTable()) {
			tableEl.setAndLoadPersistedPreferences(ureq, "project-decisions-all");
		}

		tableEl.setCssDelegate(ProjDecisionListCssDelegate.DELEGATE);
		if (isFullTable()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		} else {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.classic);
		}
		tableEl.setRendererType(FlexiTableRendererType.classic);
		VelocityContainer rowVC = createVelocityContainer("decision_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		if (isFullTable()) {
			initBulkLinks();
			
			initFilters();
			initFilterTabs(ureq);
		}
		doSelectFilterTab(null);
	}

	private void initBulkLinks() {
		if (secCallback.canEditDecisions()) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			
			bulkDeleteButton = uifactory.addFormLink("delete", flc, Link.BUTTON);
			bulkDeleteButton.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
			tableEl.addBatchButton(bulkDeleteButton);
		}
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues myValues = new SelectionValues();
		myValues.add(SelectionValues.entry(FILTER_KEY_MY, translate("decision.filter.my.value")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("decision.filter.my"), ProjDecisionFilter.my.name(), myValues, true));
		
		List<TagInfo> tagInfos = projectService.getTagInfos(project, null);
		if (!tagInfos.isEmpty()) {
			filters.add(new FlexiTableTagFilter(translate("tags"), ProjDecisionFilter.tag.name(), tagInfos, true));
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), ProjDecisionFilter.status.name(), statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabMy = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_MY,
				translate("decision.list.tab.my"),
				TabSelectionBehavior.reloadData,
				List.of(
						FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, ProjectStatus.active.name()),
						FlexiTableFilterValue.valueOf(ProjDecisionFilter.my, FILTER_KEY_MY)));
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, ProjectStatus.active.name())));
		tabs.add(tabAll);
		
		tabRecently = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RECENTLY,
				translate("tab.recently"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, ProjectStatus.active.name())));
		tabs.add(tabRecently);
		
		tabNew = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_NEW,
				translate("tab.new"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, ProjectStatus.active.name())));
		tabs.add(tabNew);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq, true);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if (secCallback.canCreateDecisions() && (tabDeleted == null || tabDeleted != tab)) {
			tableEl.setEmptyTableSettings("decision.list.empty.message", null, "o_icon_proj_decision", "decision.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("decision.list.empty.message", null, "o_icon_proj_decision");
		}
		
		if (bulkDeleteButton != null) {
			bulkDeleteButton.setVisible(tab != tabDeleted);
		}
	}

	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}
	
	protected void loadModel(UserRequest ureq, boolean sort) {
		ProjDecisionSearchParams searchParams = createSearchParams();
		applyFilters(searchParams);
		List<ProjDecisionInfo> decisionInfos = projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		List<ProjDecisionRow> rows = new ArrayList<>(decisionInfos.size());
		
		for (ProjDecisionInfo info : decisionInfos) {
			ProjDecisionRow row = new ProjDecisionRow(info);
			
			row.setDisplayName(ProjectUIFactory.getDisplayName(getTranslator(), info.getDecision()));
			String details = Formatter.truncate(info.getDecision().getDetails(), 250);
			row.setDetails(details);
			
			String modifiedBy = userManager.getUserDisplayName(info.getDecision().getArtefact().getContentModifiedBy().getKey());
			row.setContentModifiedByName(modifiedBy);
			
			if (row.getDeletedBy() != null) {
				row.setDeletedByName(userManager.getUserDisplayName(row.getDeletedBy().getKey()));
			}
			
			row.setTagKeys(info.getTags().stream().map(Tag::getKey).collect(Collectors.toSet()));
			row.setFormattedTags(TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
			
			row.setMemberKeys(info.getMembers().stream().map(Identity::getKey).collect(Collectors.toSet()));
			
			forgeUsersPortraits(ureq, row, info.getMembers());
			forgeSelectLink(row);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		dataModel.setObjects(rows);
		if (sort) {
			sortTable();
		}
		tableEl.reset(true, true, true);
		
		onModelLoaded();
	}

	private void forgeUsersPortraits(UserRequest ureq, ProjDecisionRow row, Set<Identity> members) {
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(new ArrayList<>(members));
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + row.getKey(), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}
	
	private ProjDecisionSearchParams createSearchParams() {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setNumLastModified(getNumLastModified());
		return searchParams;
	}

	private void applyFilters(ProjDecisionSearchParams searchParams) {
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabNew) {
			searchParams.setCreatedAfter(lastVisitDate);
		} else {
			searchParams.setCreatedAfter(null);
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjDecisionFilter.status.name() == filter.getFilter()) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjDecisionRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (ProjFileFilter.my.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_MY)) {
					Long identityKey = getIdentity().getKey();
					rows.removeIf(row -> !row.getMemberKeys().contains(identityKey));
				}
			}
			
			if (ProjDecisionFilter.tag.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableTagFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedTagKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					rows.removeIf(row -> row.getTagKeys() == null || !row.getTagKeys().stream().anyMatch(key -> selectedTagKeys.contains(key)));
				}
			}
		}
	}
	
	private void sortTable() {
		if (tableEl.getSelectedFilterTab() == null) {
			tableEl.sort(new SortKey(DecisionCols.decisionDate.name(), false));
		} else if ( tableEl.getSelectedFilterTab() == tabRecently) {
			tableEl.sort(new SortKey(DecisionCols.lastModifiedDate.name(), false));
		} else if (tableEl.getSelectedFilterTab() == tabMy || tableEl.getSelectedFilterTab() == tabAll || tableEl.getSelectedFilterTab() == tabDeleted) {
			tableEl.sort( new SortKey(DecisionCols.displayName.name(), true));
		} else if (tableEl.getSelectedFilterTab() == tabNew) {
			tableEl.sort(new SortKey(DecisionCols.creationDate.name(), false));
		}
	}
	
	private void forgeSelectLink(ProjDecisionRow row) {
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		link.setI18nKey(StringHelper.escapeHtml(row.getDisplayName()));
		link.setElementCssClass("o_link_plain");
		link.setUserObject(row);
		row.setSelectLink(link);
	}
	
	private void forgeToolsLink(ProjDecisionRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(3);
		if (rowObject instanceof ProjDecisionRow) {
			ProjDecisionRow projRow = (ProjDecisionRow)rowObject;
			if (projRow.getUserPortraits() != null) {
				cmps.add(projRow.getUserPortraits());
			}
			if (projRow.getSelectLink() != null) {
				cmps.add(projRow.getSelectLink().getComponent());
			}
			if (projRow.getToolsLink() != null) {
				cmps.add(projRow.getToolsLink().getComponent());
			}
		}
		return cmps;
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
				if (ProjectBCFactory.TYPE_DECISION.equals(type)) {
					Long key = entry.getOLATResourceable().getResourceableId();
					activate(ureq, key);
				}
			}
		}
	}
	
	private void activate(UserRequest ureq, Long key) {
		ProjDecisionRow row = dataModel.getObjectByKey(key);
		if (row != null) {
			int index = dataModel.getObjects().indexOf(row);
			if (index >= 1 && tableEl.getPageSize() > 1) {
				int page = index / tableEl.getPageSize();
				tableEl.setPage(page);
			}
			doOpenDecision(ureq, () -> row.getKey());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (decisionEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq, false);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (ProjDecisionRef)deleteConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (bulkDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doBulkDelete(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
		removeAsListenerAndDispose(bulkDeleteConfirmationCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(decisionEditCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		bulkDeleteConfirmationCtrl = null;
		deleteConfirmationCtrl = null;
		decisionEditCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				doOpenDecision(ureq, () -> Long.valueOf(key));
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doCreateDecision(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjDecisionRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doOpenDecision(ureq, row);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq, false);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq, true);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreateDecision(ureq);
			}
		} else if (bulkDeleteButton == source) {
			doConfirmBulkDelete(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjDecisionRow) {
				ProjDecisionRow row = (ProjDecisionRow)link.getUserObject();
				doOpenDecision(ureq, row);
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjDecisionRow) {
				doOpenTools(ureq, (ProjDecisionRow)link.getUserObject(), link);
			} 
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void doCreateDecision(UserRequest ureq) {
		if (guardModalController(decisionEditCtrl)) return;
		
		decisionEditCtrl = new ProjDecisionEditController(ureq, getWindowControl(), bcFactory, project, Set.of(getIdentity()), false);
		listenTo(decisionEditCtrl);
		
		String title = translate("decision.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), decisionEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doOpenDecision(UserRequest ureq, ProjDecisionRef decisionRef) {
		if (guardModalController(decisionEditCtrl)) return;
		
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setDecisions(List.of(decisionRef));
		List<ProjDecisionInfo> decisionInfos = projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (decisionInfos == null || decisionInfos.isEmpty()) {
			loadModel(ureq, false);
			return;
		}
		
		ProjDecisionInfo decisionInfo = decisionInfos.get(0);
		ProjDecision decision = decisionInfo.getDecision();
		decisionEditCtrl = new ProjDecisionEditController(ureq, getWindowControl(), bcFactory, decision,
				decisionInfo.getMembers(), !secCallback.canEditDecision(decision), false);
		listenTo(decisionEditCtrl);
		
		String title = translate("decision.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), decisionEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, ProjDecisionRef decisionRef) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjDecision decision = projectService.getDecision(decisionRef);
		if (decision == null || ProjectStatus.deleted == decision.getArtefact().getStatus()) {
			return;
		}
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("decision.delete.confirmation.message", StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(getTranslator(), decision))),
				translate("decision.delete.confirmation.confirm"),
				translate("decision.delete.confirmation.button"), true);
		deleteConfirmationCtrl.setUserObject(decision);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("decision.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, ProjDecisionRef decision) {
		projectService.deleteDecisionSoftly(getIdentity(), decision);
		loadModel(ureq, false);
	}
	private void doConfirmBulkDelete(UserRequest ureq) {
		if (guardModalController(bulkDeleteConfirmationCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		bulkDeleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), 
				translate("decision.bulk.delete.message", Integer.toString(selectedIndex.size())),
				translate("decision.bulk.delete.confirm"),
				translate("decision.bulk.delete.button"), true);
		listenTo(bulkDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("decision.bulk.delete.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDelete(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		List<ProjDecisionRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjDecisionSearchParams dearchParams = new ProjDecisionSearchParams();
		dearchParams.setDecisions(selectedRows);
		dearchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjDecision> decisions = projectService.getDecisions(dearchParams);
		
		decisions.stream()
				.filter(decision -> secCallback.canDeleteDecision(decision, getIdentity()))
				.forEach(decision -> projectService.deleteDecisionSoftly(getIdentity(), decision));
		
		loadModel(ureq, false);
	}
	
	private void doOpenTools(UserRequest ureq, ProjDecisionRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private static final class ProjDecisionListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final ProjDecisionListCssDelegate DELEGATE = new ProjDecisionListCssDelegate();
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_proj_decision_list";
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return FlexiTableRendererType.custom == type
					? "o_proj_decision_rows o_block_top o_proj_cards"
					: "o_proj_decision_rows o_block_top";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_proj_decision_row";
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final ProjDecisionRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjDecisionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("decision_tools");
			
			ProjDecision decision = projectService.getDecision(row);
			if (decision != null) {
				if (secCallback.canEditDecision(decision)) {
					addLink("decision.edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
				} else {
					addLink("decision.open", CMD_EDIT, "o_icon o_icon-fw o_icon_view");
				}
				
				if (secCallback.canDeleteDecision(decision, getIdentity())) {
					addLink("delete", CMD_DELETE, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
				}
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
					doOpenDecision(ureq, row);
				} else if(CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}

}
