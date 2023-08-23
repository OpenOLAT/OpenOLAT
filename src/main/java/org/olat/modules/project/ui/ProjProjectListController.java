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

import static java.util.Collections.singletonList;
import static org.olat.modules.project.ProjectSecurityCallbackFactory.createDefaultCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectCopyService;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjProjectDataModel.ProjectCols;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;
import org.olat.modules.project.ui.event.OpenProjectEvent;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsComponent.PortraitUser;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class ProjProjectListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_ACTIVE = "Active";
	private static final String TAB_ID_TEMPLATE_ACCESS_PRIVATE = "MyTemplates";
	private static final String TAB_ID_TEMPLATE_ACCESS_PUBLIC = "SharedWithMeTemplates";
	private static final String TAB_ID_NO_ACTIVITY = "NoActivity";
	private static final String TAB_ID_TO_DELETE = "ToDelete";
	private static final String TAB_ID_DONE = "Done";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_STATUS = "status";
	private static final String FILTER_ORPHANS = "orphans";
	private static final String FILTER_ORPHANS_KEY = "orphans.key";
	private static final String FILTER_MEMBER = "member";
	private static final String FILTER_TEMPLATE_ACCESS = "templateAccess";
	private static final String FILTER_TEMPLATE_ACCESS_PRIVATE = "templateAccessPrivate";
	private static final String FILTER_TEMPLATE_ACCESS_PUBLIC = "templateAccessPublic";
	private static final String CMD_SELECT = "select";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_CREATE_FROM_TEMPLATE = "create.from.template";
	private static final String CMD_CREATE_FROM_TEMPLATELINK = "create.from.template.link";
	private static final String CMD_MEMBERS = "members";
	private static final String CMD_STATUS_DONE = "done";
	private static final String CMD_STATUS_REOPEN = "reopen";
	private static final String CMD_STATUS_DELETED = "deleted";

	
	private final BreadcrumbedStackedPanel stackPanel;
	private FormLink createLink;
	private FormLink createTemplateLink;
	private FormLink bulkDoneButton;
	private FormLink bulkReopenButton;
	private FormLink bulkDeletedButton;
	protected FlexiFiltersTab tabAll;
	protected FlexiFiltersTab tabActive;
	private FlexiFiltersTab tabTemplateAccessPrivate;
	private FlexiFiltersTab tabTemplateAccessPublic;
	private FlexiFiltersTab tabNoActivity;
	private FlexiFiltersTab tabToDelete;
	private FlexiFiltersTab tabDone;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjProjectDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjProjectEditController editCtrl;
	private ProjProjectDashboardController projectCtrl;
	private ProjConfirmationController doneConfirmationCtrl;
	private ProjConfirmationController deleteConfirmationCtrl;
	private ProjConfirmationController doneConfirmationBulkCtrl;
	private ProjConfirmationController reopenConfirmationBulkCtrl;
	private ProjConfirmationController deleteConfirmationBulkCtrl;
	private DialogBoxController reopenConfirmationCtrl;
	private ProjMembersManagementController membersManagementCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private final boolean canCreateProject;
	private final boolean canCreateTemplate;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;
	private final ProjProjectImageMapper projectImageMapper;
	private final String projectMapperUrl;

	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectCopyService projectCopyService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MapperService mapperService;

	public ProjProjectListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "project_list");
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.canCreateProject = isCreateProjectEnabled() && canCreateProject(ureq);
		this.canCreateTemplate = isCreateTemplateEnabled() && canCreateProject(ureq);
		this.avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		this.formatter = Formatter.getInstance(getLocale());
		this.projectImageMapper = new ProjProjectImageMapper(projectService);
		this.projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
	}
	
	protected abstract String getTitleI18n();

	protected abstract boolean isCreateProjectEnabled();
	
	protected abstract boolean isCreateTemplateEnabled();
	
	protected abstract boolean isCreateFromTemplateEnabled();
	
	protected abstract boolean isCreateForEnabled();
	
	protected abstract boolean isBulkEnabled();

	protected abstract boolean isToolsEnabled();
	
	protected abstract boolean isColumnTypeEnabled();
	
	protected abstract boolean isColumnCreateFromTemplateEnabled();
	
	protected abstract boolean isCustomRendererEnabled();
	
	protected abstract boolean isTabActivityEnabled();
	
	protected abstract boolean isTabsTemplateAccessEnabled();
	
	protected abstract boolean isTabNoActivityEnabled();
	
	protected abstract boolean isTabToDeleteEnabled();
	
	protected abstract boolean isFilterOrphanEnabled();

	protected abstract boolean isFilterMemberEnabled();
	
	protected abstract boolean isFilterTemplateAccessEnabled();
	
	protected abstract ProjProjectSearchParams createSearchParams();
	
	protected abstract Boolean getSearchTemplates();

	private boolean canCreateProject(UserRequest ureq) {
		return projectModule.canCreateProject(ureq.getUserSession().getRoles());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("project.info");
		setFormInfoHelp("manual_user/area_modules/Project_Overview/");
		flc.contextPut("titleI18n", getTitleI18n());
		
		if (canCreateProject) {
			createLink = uifactory.addFormLink("project.create", formLayout, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		} else if (canCreateTemplate) {
			createTemplateLink = uifactory.addFormLink("project.create", "project.template.create", "project.template.create", formLayout, Link.BUTTON);
			createTemplateLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		}
		
		if (canCreateProject(ureq) && isCreateFromTemplateEnabled()) {
			initCreateFromTemplateLinks(ureq, formLayout);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectCols.title, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectCols.teaser));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectCols.status, new ProjectStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectCols.lastAcitivityDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectCols.owners));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectCols.deletedBy));
		if (isColumnTypeEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectCols.type));
		}
		if (isColumnCreateFromTemplateEnabled()) {
			DefaultFlexiColumnModel createFromTemplateColumn = new DefaultFlexiColumnModel(
					ProjectCols.createFromTemplate.i18nHeaderKey(), ProjectCols.createFromTemplate.ordinal(),
					CMD_CREATE_FROM_TEMPLATE, new BooleanCellRenderer(new StaticFlexiCellRenderer(
							translate(ProjectCols.createFromTemplate.i18nHeaderKey()), CMD_CREATE_FROM_TEMPLATE), null));
			createFromTemplateColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(createFromTemplateColumn);
		}
		if (isToolsEnabled()) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(ProjectCols.tools);
			toolsCol.setAlwaysVisible(true);
			toolsCol.setSortable(false);
			toolsCol.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		dataModel = new ProjProjectDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "project-list");

		tableEl.setCssDelegate(ProjProjectListCssDelegate.DELEGATE);
		if (isCustomRendererEnabled()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
			tableEl.setRendererType(FlexiTableRendererType.custom);
			VelocityContainer rowVC = createVelocityContainer("project_row");
			rowVC.setDomReplacementWrapperRequired(false);
			tableEl.setRowRenderer(rowVC, this);
		} else {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.classic);
			tableEl.setRendererType(FlexiTableRendererType.classic);
		}
		if (isBulkEnabled()) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		}
		
		initBulkLinks();
		initFilters();
		initFilterTabs(ureq);
	}
	
	private void initCreateFromTemplateLinks(UserRequest ureq, FormItemContainer formLayout) {
		ProjProjectSearchParams templateSearchParams = createSearchParams();
		templateSearchParams.setTemplate(Boolean.TRUE);
		templateSearchParams.setTemplateOrganisations(ureq.getUserSession().getRoles().getOrganisations());
		templateSearchParams.setStatus(List.of(ProjectStatus.active, ProjectStatus.done));
		List<ProjProject> projects = projectService.getProjects(templateSearchParams);
		if (projects.isEmpty()) {
			return;
		}
		
		projects = projects.stream().sorted((p1, p2) -> p1.getTitle().compareTo(p2.getTitle())).toList();
		DropdownItem templateDropdown = uifactory.addDropdownMenu("project.template.links", "project.create.template.link.button", null, formLayout, getTranslator());
		templateDropdown.setCarretIconCSS("o_icon o_icon_commands");
		templateDropdown.setOrientation(DropdownOrientation.right);
		for (ProjProject project : projects) {
			FormLink link = uifactory.addFormLink("tem" + project.getKey(), CMD_CREATE_FROM_TEMPLATELINK, null, null, formLayout, Link.LINK + Link.NONTRANSLATED);
			String name = translate("project.create.template.link", project.getTitle());
			link.setI18nKey(name);
			link.setUserObject(project);
			templateDropdown.addElement(link);
		}
	}

	private void initBulkLinks() {
		if (isBulkEnabled()) {
			bulkDoneButton = uifactory.addFormLink("project.list.bulk.done", flc, Link.BUTTON);
			bulkDoneButton.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.done));
			tableEl.addBatchButton(bulkDoneButton);
			
			bulkReopenButton = uifactory.addFormLink("project.list.bulk.reopen", flc, Link.BUTTON);
			bulkReopenButton.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.active));
			tableEl.addBatchButton(bulkReopenButton);
			
			bulkDeletedButton = uifactory.addFormLink("project.list.bulk.deleted", flc, Link.BUTTON);
			bulkDeletedButton.setIconLeftCSS("o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
			tableEl.addBatchButton(bulkDeletedButton);
		}
		
	}

	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabAll);
		
		if (isTabActivityEnabled()) {
			tabActive = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_ACTIVE,
					translate("project.list.tab.active"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.active.name())));
			tabs.add(tabActive);
		}
		
		if (isTabsTemplateAccessEnabled()) {
			tabTemplateAccessPrivate = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_TEMPLATE_ACCESS_PRIVATE,
					translate("project.list.tab.template.access.private"),
					TabSelectionBehavior.reloadData,
					List.of(
							FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.active.name()),
							FlexiTableFilterValue.valueOf(FILTER_TEMPLATE_ACCESS, FILTER_TEMPLATE_ACCESS_PRIVATE))
					);
			tabs.add(tabTemplateAccessPrivate);
			
			tabTemplateAccessPublic = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_TEMPLATE_ACCESS_PUBLIC,
					translate("project.list.tab.template.access.public"),
					TabSelectionBehavior.reloadData,
					List.of(
							FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.active.name()),
							FlexiTableFilterValue.valueOf(FILTER_TEMPLATE_ACCESS, FILTER_TEMPLATE_ACCESS_PUBLIC))
					);
			tabs.add(tabTemplateAccessPublic);
		}
		
		if (isTabNoActivityEnabled()) {
			tabNoActivity = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_NO_ACTIVITY,
					translate("project.list.tab.no.activity"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(ProjectStatus.active.name(), ProjectStatus.done.name()))));
			tabs.add(tabNoActivity);
		}
		
		if (isTabToDeleteEnabled()) {
			tabToDelete = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_TO_DELETE,
					translate("project.list.tab.to.delete"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.done.name())));
			tabs.add(tabToDelete);
		}
		
		tabDone = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DONE,
				translate("project.list.tab.done"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.done.name())));
		tabs.add(tabDone);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.done.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.done)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), FILTER_STATUS, statusValues, true));
		
		if (isFilterOrphanEnabled()) {
			SelectionValues orphansValues = new SelectionValues();
			orphansValues.add(SelectionValues.entry(FILTER_ORPHANS_KEY, translate("filter.orphans.orphans")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.orphans"), FILTER_ORPHANS, orphansValues, true));
		}
		
		if (isFilterMemberEnabled()) {
			filters.add(new FlexiTableTextFilter(translate("filter.member"), FILTER_MEMBER, true));
		}
		
		if (isFilterTemplateAccessEnabled()) {
			SelectionValues values = new SelectionValues();
			values.add(SelectionValues.entry(FILTER_TEMPLATE_ACCESS_PRIVATE, translate("filter.template.access.private")));
			values.add(SelectionValues.entry(FILTER_TEMPLATE_ACCESS_PUBLIC, translate("filter.template.access.public")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.template.access"), FILTER_TEMPLATE_ACCESS, values, true));
			
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if (canCreateProject && !(tabNoActivity == tab || tabToDelete == tab || tabDone == tab || tabDeleted == tab)) {
			tableEl.setEmptyTableSettings("project.list.empty.message", null, "o_icon_proj_project", "project.create", "o_icon_add", false);
		} else if (canCreateTemplate && !(tabNoActivity == tab || tabToDelete == tab || tabDone == tab || tabDeleted == tab)) {
			tableEl.setEmptyTableSettings("project.list.empty.message", null, "o_icon_proj_project", "project.template.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("project.list.empty.message", null, "o_icon_proj_project");
		}
		if (isBulkEnabled() && tabDeleted != tab) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
		} else {
			tableEl.setMultiSelect(false);
		}
		if (isBulkEnabled()) {
			bulkDoneButton.setVisible(tabDone != tab);
			bulkReopenButton.setVisible(tabDone == tab);
		}
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(4);
		if (rowObject instanceof ProjProjectRow) {
			ProjProjectRow projRow = (ProjProjectRow)rowObject;
			if (projRow.getProjAvatar() != null) {
				cmps.add(projRow.getProjAvatar());
			}
			if (projRow.getUserPortraits() != null) {
				cmps.add(projRow.getUserPortraits());
			}
			if (projRow.getSelectLink() != null) {
				cmps.add(projRow.getSelectLink().getComponent());
			}
			if (projRow.getCreateFromTemplateLink() != null) {
				cmps.add(projRow.getCreateFromTemplateLink().getComponent());
			}
		}
		return cmps;
	}
	
	protected void loadModel(UserRequest ureq) {
		ProjProjectSearchParams searchParams = createSearchParams();
		applyTemplateFilter(ureq, searchParams);
		applyFilters(searchParams);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		
		Map<Long, Set<Long>> projectKeyToOrganisationKey = getSearchTemplates() != null
				? projectService.getProjectKeyToOrganisationKey(projects)
				: Map.of();
		
		Map<Long, List<Identity>> projectKeyToOwners = projectService.getProjectGroupKeyToMembers(projects, List.of(ProjectRole.owner));
		Map<Long, List<Identity>> projectKeyToMembers = projectService.getProjectGroupKeyToMembers(projects, ProjectRole.PROJECT_ROLES);
		
		ProjActivitySearchParams activitySearchParams = new ProjActivitySearchParams();
		activitySearchParams.setProjects(projects);
		Map<Long, ProjActivity> projectKeyToLastActivity = projectService.getProjectKeyToLastActivity(activitySearchParams);
		
		List<ProjProjectRow> rows = new ArrayList<>(projects.size());
		for (ProjProject project : projects) {
			ProjProjectRow row = new ProjProjectRow(project);
			
			row.setTranslatedStatus(ProjectUIFactory.translateStatus(getTranslator(), project.getStatus()));
			if (row.getDeletedBy() != null) {
				row.setDeletedByName(userManager.getUserDisplayName(row.getDeletedBy().getKey()));
			}
			
			List<Identity> owners = projectKeyToOwners.get(project.getBaseGroup().getKey());
			if (owners != null && !owners.isEmpty()) {
				row.setOwnerKeys(owners.stream().map(Identity::getKey).collect(Collectors.toSet()));
				String ownersNames = owners.stream()
						.map(userManager::getUserDisplayName)
						.sorted()
						.collect(Collectors.joining(" / "));
				row.setOwnersNames(ownersNames);
				row.setProjectOf(translate(ProjectUIFactory.templateSuffix("project.of", project), ownersNames));
			} else {
				row.setOwnerKeys(Set.of());
			}
			
			ProjActivity activity = projectKeyToLastActivity.get(project.getKey());
			if (activity != null) {
				row.setLastActivityDate(activity.getCreationDate());
				String modifiedDate = formatter.formatDateRelative(activity.getCreationDate());
				String modifiedBy = userManager.getUserDisplayName(activity.getDoer().getKey());
				String modified = translate("date.by", modifiedDate, modifiedBy);
				row.setModified(modified);
			}
			
			String url = ProjectBCFactory.getProjectUrl(project);
			row.setUrl(url);
			
			row.setTemplate(project.isTemplatePrivate() || project.isTemplatePublic());
			String template = row.isTemplate()
					? translate("project.template.template")
					: translate("project.normal");
			row.setTemplateName(template);
			
			List<Identity> members = projectKeyToMembers.getOrDefault(project.getBaseGroup().getKey(), List.of());
			row.setMemberKeys(members.stream().map(Identity::getKey).collect(Collectors.toSet()));
			forgeProjAvatar(row, project);
			forgeUsersPortraits(ureq, row, members);
			forgeSelectLink(ureq, row, projectKeyToOrganisationKey.getOrDefault(project.getKey(), Set.of()));
			forgeCreateFormTemplateLink(ureq, row);
			forgeToolsLink(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		dataModel.setObjects(rows);
		tableEl.sort(new SortKey(ProjectCols.lastAcitivityDate.name(), false));
		tableEl.reset(true, true, true);
	}

	private void applyTemplateFilter(UserRequest ureq, ProjProjectSearchParams searchParams) {
		if (getSearchTemplates() != null) {
			if (getSearchTemplates()) {
				searchParams.setTemplate(Boolean.TRUE);
				searchParams.setTemplateOrganisations(ureq.getUserSession().getRoles().getOrganisations());
			} else {
				searchParams.setTemplate(Boolean.FALSE);
			}
		}
	}

	private void applyFilters(ProjProjectSearchParams searchParams) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_STATUS.equals(filter.getFilter())) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
			if (FILTER_ORPHANS.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_ORPHANS_KEY)) {
					searchParams.setArtefactAvailable(Boolean.FALSE);
				} else {
					searchParams.setArtefactAvailable(null);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjProjectRow> rows) {
		if (tableEl.getSelectedFilterTab() != null 
				&& (tableEl.getSelectedFilterTab() == tabNoActivity || tableEl.getSelectedFilterTab() == tabToDelete)) {
			Date lastActivityDate = DateUtils.addDays(new Date(), -28);
			rows.removeIf(row -> lastActivityDate.before(row.getLastActivityDate()));
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_ORPHANS.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_ORPHANS_KEY)) {
					rows.removeIf(row -> row.getMemberKeys().size() > 1);
				}
			}
			if (FILTER_MEMBER.equals(filter.getFilter())) {
				String value = filter.getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					SearchIdentityParams params = new SearchIdentityParams();
					params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
					params.setSearchString(value);
					Set<Long> memberKeys = securityManager.getIdentitiesByPowerSearch(params, 0, -1).stream()
							.map(Identity::getKey)
							.collect(Collectors.toSet());
					rows.removeIf(row -> !row.getMemberKeys().stream().anyMatch(key -> memberKeys.contains(key)));
				}
			}
			if (FILTER_TEMPLATE_ACCESS.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && values.size() == 1) {
					if (FILTER_TEMPLATE_ACCESS_PRIVATE.equals(values.get(0))) {
						rows.removeIf(row -> !row.getOwnerKeys().contains(getIdentity().getKey()));
					} else if (FILTER_TEMPLATE_ACCESS_PUBLIC.equals(values.get(0))) {
						rows.removeIf(row -> row.getOwnerKeys().contains(getIdentity().getKey()));
					}
				}
			}
		}
	}
	
	private void forgeProjAvatar(ProjProjectRow row, ProjProject project) {
		String avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		ProjAvatarComponent projAvatar = new ProjAvatarComponent("avatar", project, avatarUrl, Size.small, false);
		row.setProjAvatar(projAvatar);
	}
	
	private void forgeUsersPortraits(UserRequest ureq, ProjProjectRow row, List<Identity> members) {
		if (members == null || members.isEmpty()) {
			return;
		}
		
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(members);
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + row.getKey(), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("member.list.aria"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}

	private void forgeSelectLink(UserRequest ureq, ProjProjectRow row, Set<Long> organisationKeys) {
		if (!row.isTemplate() || isOwnerOrManager(ureq, row, organisationKeys)) {
			FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "project.open", null, flc, Link.LINK);
			link.setIconRightCSS("o_icon o_icon_start");
			link.setUrl(row.getUrl());
			link.setUserObject(row);
			row.setSelectLink(link);
		}
	}
	
	private boolean isOwnerOrManager(UserRequest ureq, ProjProjectRow row, Set<Long> organisationKeys) {
		return row.getOwnerKeys().contains(getIdentity().getKey())
				|| ureq.getUserSession().getRoles()
					.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.projectmanager)
					.stream().map(OrganisationRef::getKey)
					.anyMatch(key -> organisationKeys.contains(key));
	}

	private void forgeCreateFormTemplateLink(UserRequest ureq, ProjProjectRow row) {
		if (row.isTemplate() && canCreateProject(ureq)) {
			FormLink link = uifactory.addFormLink("ctemp_" + row.getKey(), CMD_CREATE_FROM_TEMPLATE, "project.create.from.template", null, flc, Link.LINK);
			link.setIconRightCSS("o_icon o_icon_start");
			link.setUserObject(row);
			row.setCreateFromTemplateLink(link);
		}
	}
	
	private void forgeToolsLink(ProjProjectRow row) {
		if (!isToolsEnabled()) return;
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getKey(), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
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
				if (ProjectBCFactory.TYPE_PROJECT.equals(type)) {
					selectFilterTab(ureq, tabAll);
					Long key = entry.getOLATResourceable().getResourceableId();
					ProjProjectRow row = dataModel.getObjectByKey(key);
					if (row != null) {
						int index = dataModel.getObjects().indexOf(row);
						if (index >= 1 && tableEl.getPageSize() > 1) {
							int page = index / tableEl.getPageSize();
							tableEl.setPage(page);
						}
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						Activateable2 activateable2 = doOpenProject(ureq, row.getKey(), true, true);
						activateable2.activate(ureq, subEntries, entries.get(0).getTransientState());
					}
				}
			}
		} else if (dataModel.getRowCount() == 1) {
			ProjProjectRow row = dataModel.getObject(0);
			doOpenProject(ureq, row.getKey(), true, true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doCreateProject(ureq);
		} else if (createTemplateLink == source) {
			doCreateTemplate(ureq);
		} else if (bulkDoneButton == source) {
			doConfirmBulkStatusDone(ureq);
		} else if (bulkReopenButton == source) {
			doConfirmBulkReopen(ureq);
		} else if (bulkDeletedButton == source) {
			doConfirmBulkStatusDeleted(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjProjectRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doOpenProject(ureq, row.getKey(), true, true);
				} else if (CMD_CREATE_FROM_TEMPLATE.equals(cmd)) {
					doCreateFromTemplate(ureq, row.getKey());
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				if (canCreateProject) {
					doCreateProject(ureq);
				} else if (canCreateTemplate) {
					doCreateTemplate(ureq);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjProjectRow projectRow) {
				doOpenProject(ureq, projectRow.getKey(), true, true);
			} else if (CMD_CREATE_FROM_TEMPLATE.equals(link.getCmd()) && link.getUserObject() instanceof ProjProjectRow projectRow) {
				doCreateFromTemplate(ureq, projectRow.getKey());
			} else if (CMD_CREATE_FROM_TEMPLATELINK.equals(link.getCmd()) && link.getUserObject() instanceof ProjProject project) {
				doCreateFromTemplate(ureq, project.getKey());
			} else if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ProjProjectRow projectRow) {
				doOpenTools(ureq, projectRow, link);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == stackPanel.getRootController()) {
					loadModel(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (projectCtrl == source) {
			if (event instanceof OpenProjectEvent) {
				ProjProjectRef project = ((OpenProjectEvent)event).getProject();
				loadModel(ureq);
				doOpenProject(ureq, project.getKey(), true, false);
			} else if (event == Event.DONE_EVENT) {
				stackPanel.popUpToRootController(ureq);
				loadModel(ureq);
			}
		} else if (editCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (doneConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && doneConfirmationCtrl.getUserObject() instanceof ProjProject project) {
				doSetStatusDone(ureq, project);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && deleteConfirmationCtrl.getUserObject() instanceof ProjProject project) {
				doSetStatusDeleted(ureq, project);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == reopenConfirmationCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event) && reopenConfirmationCtrl.getUserObject() instanceof ProjProject project) {
				doReopen(ureq, project);
			}
		} else if (doneConfirmationBulkCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doBulkSetStatusDone(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (reopenConfirmationBulkCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doBulkReopen(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationBulkCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doBulkSetStatusDeleted(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
		removeAsListenerAndDispose(deleteConfirmationBulkCtrl);
		removeAsListenerAndDispose(reopenConfirmationBulkCtrl);
		removeAsListenerAndDispose(doneConfirmationBulkCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(doneConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationBulkCtrl = null;
		reopenConfirmationBulkCtrl = null;
		doneConfirmationBulkCtrl = null;
		deleteConfirmationCtrl = null;
		doneConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		editCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void doDispose() {
		super.doDispose();
		mapperService.cleanUp(singletonList(avatarMapperKey));
		stackPanel.removeListener(this);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCreateProject(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = ProjProjectEditController.createCreateCtrl(ureq, getWindowControl(), isCreateForEnabled());
		listenTo(editCtrl);
		
		String title = translate("project.create");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateTemplate(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = ProjProjectEditController.createTemplateCtrl(ureq, getWindowControl(), null);
		listenTo(editCtrl);
		
		String title = translate("project.template.create");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditProject(UserRequest ureq, ProjProjectRef projectRef) {
		if (guardModalController(editCtrl)) return;
		
		ProjProject project = projectService.getProject(projectRef);
		if (project == null) return;
		
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
		ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
		if (!secCallback.canViewProjectMetadata()) {
			return;
		}
		
		editCtrl = ProjProjectEditController.createEditCtrl(ureq, getWindowControl(), project, !secCallback.canEditProjectMetadata());
		listenTo(editCtrl);
		
		String title = translate(ProjectUIFactory.templateSuffix("project.edit", project));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private Activateable2 doOpenProject(UserRequest ureq, Long projectKey, boolean searchParamsRestrictions, boolean readActivity) {
		removeAsListenerAndDispose(projectCtrl);
		stackPanel.popUpToRootController(ureq);
		
		ProjProjectSearchParams searchParams = null;
		if (searchParamsRestrictions) {
			searchParams = createSearchParams();
			applyTemplateFilter(ureq, searchParams);
		} else {
			searchParams = new ProjProjectSearchParams();
		}
		searchParams.setProjectKeys(List.of(() -> projectKey));
		List<ProjProject> projects = projectService.getProjects(searchParams);
		if (projects != null && !projects.isEmpty()) {
			ProjProject project = projects.get(0);
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ProjProject.TYPE, project.getKey()), null);
			Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
			boolean manager = projectService.isInOrganisation(project, ureq.getUserSession().getRoles()
					.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.projectmanager));
			ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, manager, canCreateProject(ureq));
			projectCtrl = new ProjProjectDashboardController(ureq, swControl, stackPanel, project, secCallback, isCreateForEnabled());
			listenTo(projectCtrl);
			String title = Formatter.truncate(project.getTitle(), 50);
			stackPanel.pushController(title, projectCtrl);
			
			if (readActivity) {
				projectService.createActivityRead(getIdentity(), project);
			}
			return projectCtrl;
		}
		
		showWarning("error.project.not.open");
		return null;
	}
	
	private void doCreateFromTemplate(UserRequest ureq, Long key) {
		ProjProject projectCopy = projectCopyService.copyProjectFromTemplate(getIdentity(), () -> key);
		if (projectCopy == null) {
			return;
		}
		
		loadModel(ureq);
		doOpenProject(ureq, projectCopy.getKey(), false, true);
	}
	
	private void doOpenMembersManagement(UserRequest ureq, ProjProjectRef projectRef) {
		removeAsListenerAndDispose(membersManagementCtrl);
		
		ProjProject project = projectService.getProject(projectRef);
		if (project == null) return;
		
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
		ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
		if (!secCallback.canEditProjectMetadata()) {
			return;
		}
		
		membersManagementCtrl = new ProjMembersManagementController(ureq, getWindowControl(), stackPanel, project, secCallback);
		listenTo(membersManagementCtrl);
		stackPanel.pushController(translate("members.management"), membersManagementCtrl);
	}
	
	private void doConfirmStatusDone(UserRequest ureq, ProjProjectRef projectRef) {
		if (guardModalController(doneConfirmationCtrl)) return;
		
		ProjProject project = projectService.getProject(projectRef);
		if (project == null) return;
		
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
		ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
		if (!secCallback.canEditProjectStatus()) {
			return;
		}
		
		// Project has not the right status anymore to set the target status.
		if (ProjectStatus.active != project.getStatus()) {
			fireEvent(ureq, new OpenProjectEvent(project));
			return;
		}
		
		int numOfMembers = projectService.countMembers(project);
		String message = translate(ProjectUIFactory.templateSuffix("project.set.status.done.message", project), Integer.toString(numOfMembers));
		doneConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				ProjectUIFactory.templateSuffix("project.set.status.done.confirm", project),
				ProjectUIFactory.templateSuffix("project.set.status.done.button", project), false);
		doneConfirmationCtrl.setUserObject(project);
		listenTo(doneConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), doneConfirmationCtrl.getInitialComponent(),
				true, translate(ProjectUIFactory.templateSuffix("project.set.status.done.title", project)), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmStatusDeleted(UserRequest ureq, ProjProjectRef projectRef) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjProject project = projectService.getProject(projectRef);
		if (project == null) return;
		
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
		ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
		if (!secCallback.canEditProjectStatus()) {
			return;
		}
		
		// Project has not the right status anymore to set the target status.
		if (ProjectStatus.deleted == project.getStatus()) {
			fireEvent(ureq, new OpenProjectEvent(project));
			return;
		}
		
		int numOfMembers = projectService.countMembers(project);
		String message = translate(ProjectUIFactory.templateSuffix("project.set.status.deleted.message", project), Integer.toString(numOfMembers));
		deleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				ProjectUIFactory.templateSuffix("project.set.status.deleted.confirm", project),
				ProjectUIFactory.templateSuffix("project.set.status.deleted.button", project), true);
		deleteConfirmationCtrl.setUserObject(project);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate(ProjectUIFactory.templateSuffix("project.set.status.deleted.title", project)), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmReopen(UserRequest ureq, ProjProjectRef projectRef) {
		ProjProject project = projectService.getProject(projectRef);
		if (project == null) return;
		
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
		ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
		if (!secCallback.canEditProjectStatus()) {
			return;
		}
		
		String title = translate(ProjectUIFactory.templateSuffix("project.reopen.title", project));
		String msg = translate(ProjectUIFactory.templateSuffix("project.reopen.text", project));
		reopenConfirmationCtrl = activateOkCancelDialog(ureq, title, msg, reopenConfirmationCtrl);
		reopenConfirmationCtrl.setUserObject(project);
	}
	
	private void doSetStatusDone(UserRequest ureq, ProjProject project) {
		project = projectService.setStatusDone(getIdentity(), project);
		loadModel(ureq);
	}
	
	private void doReopen(UserRequest ureq, ProjProject project) {
		project = projectService.reopen(getIdentity(), project);
		loadModel(ureq);
	}

	private void doSetStatusDeleted(UserRequest ureq, ProjProject project) {
		project = projectService.setStatusDeleted(getIdentity(), project);
		loadModel(ureq);
	}
	
	private void doOpenTools(UserRequest ureq, ProjProjectRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doConfirmBulkStatusDone(UserRequest ureq) {
		if (guardModalController(doneConfirmationBulkCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		String message = translate("project.set.status.done.bulk.message", Integer.toString(selectedIndex.size()));
		doneConfirmationBulkCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"project.set.status.done.bulk.confirm", "project.set.status.done.bulk.button", false);
		listenTo(doneConfirmationBulkCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), doneConfirmationBulkCtrl.getInitialComponent(),
				true, translate("project.set.status.done.bulk.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmBulkReopen(UserRequest ureq) {
		if (guardModalController(reopenConfirmationBulkCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		String message = translate("project.reopen.bulk.message", Integer.toString(selectedIndex.size()));
		reopenConfirmationBulkCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"project.reopen.bulk.confirm", "project.reopen.bulk.button", false);
		listenTo(reopenConfirmationBulkCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), reopenConfirmationBulkCtrl.getInitialComponent(),
				true, translate("project.reopen.bulk.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmBulkStatusDeleted(UserRequest ureq) {
		if (guardModalController(deleteConfirmationBulkCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		String message = translate("project.set.status.deleted.bulk.message", Integer.toString(selectedIndex.size()));
		deleteConfirmationBulkCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"project.set.status.deleted.bulk.confirm", "project.set.status.deleted.bulk.button", true);
		listenTo(deleteConfirmationBulkCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationBulkCtrl.getInitialComponent(),
				true, translate("project.set.status.deleted.bulk.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkSetStatusDone(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		
		List<ProjProjectRow> selectedProjects = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setProjectKeys(selectedProjects);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		
		for (ProjProject project: projects) {
		Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
			ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
			if (secCallback.canEditProjectStatus()) {
				projectService.setStatusDone(getIdentity(), project);
			}
		}
		
		loadModel(ureq);
	}
	
	private void doBulkReopen(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		
		List<ProjProjectRow> selectedProjects = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setProjectKeys(selectedProjects);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		
		for (ProjProject project: projects) {
			Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
			ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
			if (secCallback.canEditProjectStatus()) {
				projectService.reopen(getIdentity(), project);
			}
		}
		
		loadModel(ureq);
	}
	
	private void doBulkSetStatusDeleted(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			return;
		}
		
		List<ProjProjectRow> selectedProjects = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setProjectKeys(selectedProjects);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		
		for (ProjProject project: projects) {
			Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
			ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
			if (secCallback.canDeleteProject()) {
				projectService.setStatusDeleted(getIdentity(), project);
			}
		}
		
		loadModel(ureq);
	}
	
	private static final class ProjProjectListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final ProjProjectListCssDelegate DELEGATE = new ProjProjectListCssDelegate();
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_proj_project_rows o_block_top";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_proj_project_row";
		}
	}
	
	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		
		private final ProjProject project;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ProjProjectRow row) {
			super(ureq, wControl);
			
			mainVC = createVelocityContainer("project_tools");
			
			project = projectService.getProject(row);
			if (project != null) {
				boolean dividerDelete = false;
				Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
				ProjProjectSecurityCallback secCallback = createDefaultCallback(project, roles, true, canCreateProject(ureq));
				if (secCallback.canViewProjectMetadata()) {
					addLink("project.edit", ProjectUIFactory.templateSuffix("project.edit", project), CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
					dividerDelete &= true;
				}
				if (secCallback.canEditMembers()) {
					addLink("members.management", "members.management", CMD_MEMBERS, "o_icon o_icon-fw o_icon_membersmanagement");
					dividerDelete &= true;
				}
				if (secCallback.canEditProjectStatus() && ProjectStatus.active == project.getStatus()) {
					addLink("project.set.status.done",
							ProjectUIFactory.templateSuffix("project.set.status.done", project), CMD_STATUS_DONE,
							"o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.done));
					dividerDelete &= true;
				}
				if (secCallback.canEditProjectStatus() && ProjectStatus.done == project.getStatus()) {
					addLink("project.reopen", ProjectUIFactory.templateSuffix("project.reopen", project),
							CMD_STATUS_REOPEN, "o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.active));
					dividerDelete &= true;
				}
				mainVC.contextPut("dividerDelete", dividerDelete);
				if (secCallback.canDeleteProject() && ProjectStatus.deleted != project.getStatus()) {
					addLink("project.set.status.deleted",
							ProjectUIFactory.templateSuffix("project.set.status.deleted", project), CMD_STATUS_DELETED,
							"o_icon o_icon-fw " + ProjectUIFactory.getStatusIconCss(ProjectStatus.deleted));
				}
			}
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String i18n, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, name, cmd, i18n, getTranslator(), mainVC, this, Link.LINK);
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
					doEditProject(ureq, project);
				} else if (CMD_MEMBERS.equals(cmd)) {
					doOpenMembersManagement(ureq, project);
				} else if (CMD_STATUS_DONE.equals(cmd)) {
					doConfirmStatusDone(ureq, project);
				} else if (CMD_STATUS_REOPEN.equals(cmd)) {
					doConfirmReopen(ureq, project);
				} else if(CMD_STATUS_DELETED.equals(cmd)) {
					doConfirmStatusDeleted(ureq, project);
				}
			}
		}
	}

}
