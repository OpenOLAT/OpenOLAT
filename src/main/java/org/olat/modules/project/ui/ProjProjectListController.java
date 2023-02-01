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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectSecurityCallbackFactory;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjProjectDataModel.ProjectCols;
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
public class ProjProjectListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_ACTIVE = "Active";
	private static final String TAB_ID_DONE = "Done";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String FILTER_KEY_STATUS = "status";
	private static final String CMD_SELECT = "select";
	
	private final BreadcrumbedStackedPanel stackPanel;
	private FormLayoutContainer dummyCont;
	private FormLink createLink;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabActive;
	private FlexiFiltersTab tabDone;
	private FlexiFiltersTab tabDeleted;
	private FlexiTableElement tableEl;
	private ProjProjectDataModel dataModel;
	
	private CloseableModalController cmc;
	private ProjProjectEditController editCtrl;
	private ProjProjectDashboardController projectCtrl;
	
	private final boolean canCreateProject;
	private final MapperKey avatarMapperKey;
	private final Formatter formatter;

	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MapperService mapperService;

	public ProjProjectListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "project_list");
		this.stackPanel = stackPanel;
		canCreateProject = projectModule.canCreateProject(ureq.getUserSession().getRoles());
		this.avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dummyCont = FormLayoutContainer.createCustomFormLayout("dummy", getTranslator(), velocity_root + "/empty.html");
		dummyCont.setRootForm(mainForm);
		formLayout.add(dummyCont);
		
		if (canCreateProject) {
			createLink = uifactory.addFormLink("project.create", formLayout, Link.BUTTON);
			createLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
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
		
		dataModel = new ProjProjectDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "project-list");

		tableEl.setCssDelegate(ProjProjectListCssDelegate.DELEGATE);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("project_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initFilters();
		initFilterTabs(ureq);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabAll);
		
		tabActive = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ACTIVE,
				translate("project.list.tab.active"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_KEY_STATUS, ProjectStatus.active.name())));
		tabs.add(tabActive);
		
		tabDone = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DONE,
				translate("project.list.tab.done"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_KEY_STATUS, ProjectStatus.done.name())));
		tabs.add(tabDone);
		
		tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_KEY_STATUS, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabActive);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.done.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.done)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(getTranslator(), ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("status"), FILTER_KEY_STATUS, statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		doSelectFilterTab(tab);
		loadModel(ureq);
	}
	
	private void doSelectFilterTab(FlexiFiltersTab tab) {
		if (canCreateProject && !(tabDone == tab || tabDeleted == tab)) {
			tableEl.setEmptyTableSettings("project.list.empty.message", null, "o_icon_proj_project", "project.create", "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("project.list.empty.message", null, "o_icon_proj_project");
		}
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof ProjProjectRow) {
			ProjProjectRow projRow = (ProjProjectRow)rowObject;
			if (projRow.getUserPortraits() != null) {
				cmps.add(projRow.getUserPortraits());
			}
			if (projRow.getSelectLink() != null) {
				cmps.add(projRow.getSelectLink().getComponent());
			}
		}
		return cmps;
	}
	
	private void loadModel(UserRequest ureq) {
		ProjProjectSearchParams searchParams = createSearchParams(ureq);
		applyFilters(searchParams);
		List<ProjProject> projects = projectService.getProjects(searchParams);
		
		Map<Long, List<Identity>> projectKeyToOwners = projectService.getProjectGroupKeyToMembers(projects, List.of(ProjectRole.owner));
		Map<Long, List<Identity>> projectKeyToMembers = projectService.getProjectGroupKeyToMembers(projects, ProjectRole.PROJECT_ROLES);
		
		ProjActivitySearchParams activitySearchParams = new ProjActivitySearchParams();
		activitySearchParams.setProjects(projects);
		Map<Long, ProjActivity> projectKeyToLastActivity = projectService.getProjectKeyToLastActivity(activitySearchParams);
		
		List<ProjProjectRow> rows = new ArrayList<>(projects.size());
		for (ProjProject project : projects) {
			ProjProjectRow row = new ProjProjectRow(project);
			
			row.setTranslatedStatus(ProjectUIFactory.translateStatus(getTranslator(), project.getStatus()));
			
			List<Identity> owners = projectKeyToOwners.get(project.getBaseGroup().getKey());
			if (owners != null && !owners.isEmpty()) {
				String ownersNames = owners.stream()
						.map(userManager::getUserDisplayName)
						.sorted()
						.collect(Collectors.joining(" / "));
				row.setOwnersNames(ownersNames);
			}
			
			ProjActivity activity = projectKeyToLastActivity.get(project.getKey());
			if (activity != null) {
				row.setLastActivityDate(activity.getCreationDate());
				String modifiedDate = formatter.formatDateRelative(activity.getCreationDate());
				String modifiedBy = userManager.getUserDisplayName(activity.getDoer().getKey());
				String modified = translate("date.by", modifiedDate, modifiedBy);
				row.setModified(modified);
			}
			
			String path = "[Projects:0][Project:" + row.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			row.setUrl(url);
			
			List<Identity> members = projectKeyToMembers.get(project.getBaseGroup().getKey());
			forgeUsersPortraits(ureq, row, members);
			forgeSelectLink(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.sort(new SortKey(ProjectCols.lastAcitivityDate.name(), false));
		tableEl.reset(true, true, true);
		if (createLink != null) {
			createLink.setVisible(!rows.isEmpty());
		}
	}

	private ProjProjectSearchParams createSearchParams(UserRequest ureq) {
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setIdentity(getIdentity());
		List<OrganisationRef> projectManagerOrganisations = ureq.getUserSession().getRoles()
				.getOrganisationsWithRoles(OrganisationRoles.projectmanager, OrganisationRoles.administrator);
		searchParams.setProjectOrganisations(projectManagerOrganisations);
		return searchParams;
	}
	
	private void applyFilters(ProjProjectSearchParams searchParams) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_KEY_STATUS.equals(filter.getFilter())) {
				List<String> status = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).collect(Collectors.toList()));
				} else {
					searchParams.setStatus(null);
				}
			}
		}
	}
	
	private void forgeUsersPortraits(UserRequest ureq, ProjProjectRow row, List<Identity> members) {
		if (members == null || members.isEmpty()) {
			return;
		}
		
		List<PortraitUser> portraitUsers = UsersPortraitsFactory.createPortraitUsers(members);
		UsersPortraitsComponent usersPortraitCmp = UsersPortraitsFactory.create(ureq, "users_" + row.getKey(), flc.getFormItemComponent(), null, avatarMapperKey);
		usersPortraitCmp.setAriaLabel(translate("members"));
		usersPortraitCmp.setSize(PortraitSize.small);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		row.setUserPortraits(usersPortraitCmp);
	}

	private void forgeSelectLink(ProjProjectRow row) {
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, "project.open", null, dummyCont, Link.LINK);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setUrl(row.getUrl());
		link.setUserObject(row);
		row.setSelectLink(link);
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
				if (ProjProject.TYPE.equals(type)) {
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
						Activateable2 activateable2 = doOpenProject(ureq, row.getKey(), true);
						activateable2.activate(ureq, subEntries, entries.get(0).getTransientState());
					}
				}
			}
		} else if (dataModel.getRowCount() == 1) {
			ProjProjectRow row = dataModel.getObject(0);
			doOpenProject(ureq, row.getKey(), true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink){
			doCreateProject(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ProjProjectRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doOpenProject(ureq, row.getKey(), true);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				doSelectFilterTab(((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreateProject(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_SELECT.equals(link.getCmd()) && link.getUserObject() instanceof ProjProjectRow) {
				ProjProjectRow row = (ProjProjectRow)link.getUserObject();
				doOpenProject(ureq, row.getKey(), true);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (projectCtrl == source) {
			if (event instanceof OpenProjectEvent) {
				ProjProjectRef project = ((OpenProjectEvent)event).getProject();
				loadModel(ureq);
				doOpenProject(ureq, project.getKey(), false);
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
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void doDispose() {
		super.doDispose();
		mapperService.cleanUp(singletonList(avatarMapperKey));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCreateProject(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = new ProjProjectEditController(ureq, getWindowControl(), null, false);
		listenTo(editCtrl);
		
		String title = translate("project.create");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private Activateable2 doOpenProject(UserRequest ureq, Long projectKey, boolean readActivity) {
		removeAsListenerAndDispose(projectCtrl);
		stackPanel.popUpToRootController(ureq);
		
		ProjProjectSearchParams searchParams = createSearchParams(ureq);
		searchParams.setProjectKeys(List.of(() -> projectKey));
		List<ProjProject> projects = projectService.getProjects(searchParams);
		if (projects != null && !projects.isEmpty()) {
			ProjProject project = projects.get(0);
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ProjProject.TYPE, project.getKey()), null);
			Set<ProjectRole> roles = projectService.getRoles(project, getIdentity());
			ProjProjectSecurityCallback secCallback = ProjectSecurityCallbackFactory.createDefaultCallback(project.getStatus(), roles);
			projectCtrl = new ProjProjectDashboardController(ureq, swControl, stackPanel, project, secCallback);
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

}
