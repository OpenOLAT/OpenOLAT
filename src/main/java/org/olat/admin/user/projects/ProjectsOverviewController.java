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
package org.olat.admin.user.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.projects.ProjectsOverviewDataModel.ProjectOverviewCols;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjProjectListController;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectStatusRenderer;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 23, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjectsOverviewController extends FormBasicController {

	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_ACTIVE = "Active";
	private static final String TAB_ID_DELETED = "Deleted";
	private static final String TAB_ID_DONE = "Done";
	private static final String FILTER_STATUS = "status";
	private static final String TABLE_ACTION_REMOVE = "remove";

	private final Identity editedIdentity;
	private final boolean canModify;
	private final boolean isLastVisitVisible;
	private final Translator projTranslator;
	private FormLink bulkRemoveEl;
	private FlexiTableElement tableEl;
	private ProjectsOverviewDataModel dataModel;

	private CloseableModalController cmc;
	private ProjectRemoveUserDialogBoxController projectRemoveUserDialogBoxCtrl;

	@Autowired
	private ProjectService projectService;
	@Autowired
	private BaseSecurityModule securityModule;

	public ProjectsOverviewController(UserRequest ureq, WindowControl wControl, Identity identity, boolean canModify) {
		super(ureq, wControl, "projects_overview");

		this.editedIdentity = identity;
		this.canModify = canModify;
		isLastVisitVisible = securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles());
		projTranslator = Util.createPackageTranslator(ProjProjectListController.class, ureq.getLocale());

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectOverviewCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProjectOverviewCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectOverviewCols.status, new ProjectStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectOverviewCols.roles));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectOverviewCols.regDate));
		if (isLastVisitVisible) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProjectOverviewCols.lastVisitDate));
		}

		if (canModify) {
			DefaultFlexiColumnModel leaveCol = new DefaultFlexiColumnModel(ProjectOverviewCols.removes.i18nHeaderKey(),
					ProjectOverviewCols.removes.ordinal(), TABLE_ACTION_REMOVE,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(ProjectOverviewCols.removes.i18nHeaderKey()), TABLE_ACTION_REMOVE), null));
			leaveCol.setAlwaysVisible(true);
			leaveCol.setExportable(false);
			columnsModel.addFlexiColumnModel(leaveCol);
		}

		dataModel = new ProjectsOverviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table.projects", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "user-projects-overview");
		if (canModify) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			bulkRemoveEl = uifactory.addFormLink("table.header.proj.overview.removes", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkRemoveEl);
		}

		initFilters();
		initFilterTabs(ureq);
	}

	private void loadModel() {
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setIdentity(editedIdentity);
		applyFilters(searchParams);
		List<ProjProject> projects = projectService.getProjects(searchParams);

		List<ProjectsOverviewRow> rows = projects.stream()
				.map(this::forgeRow)
				.toList();

		dataModel.setObjects(rows);
		tableEl.sort(new SortKey(ProjectOverviewCols.title.name(), false));
		tableEl.reset(true, true, true);
	}

	private ProjectsOverviewRow forgeRow(ProjProject project) {
		ProjectsOverviewRow overviewRow = new ProjectsOverviewRow(project);

		Set<ProjectRole> roles = projectService.getRoles(project, editedIdentity);
		String rolesString = roles.stream()
				.map(role -> ProjectUIFactory.translateRole(projTranslator, role))
				.collect(Collectors.joining(", "));
		overviewRow.setRoles(rolesString);

		overviewRow.setTranslatedStatus(ProjectUIFactory.translateStatus(projTranslator, project.getStatus()));

		ProjProjectUserInfo projectUserInfo = projectService.getOrCreateProjectUserInfo(project, editedIdentity);
		overviewRow.setRegistrationDate(projectUserInfo.getCreationDate());
		overviewRow.setLastActivityDate(projectUserInfo.getLastVisitDate());

		return overviewRow;
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(ProjectStatus.active.name(), ProjectUIFactory.translateStatus(projTranslator, ProjectStatus.active)));
		statusValues.add(SelectionValues.entry(ProjectStatus.done.name(), ProjectUIFactory.translateStatus(projTranslator, ProjectStatus.done)));
		statusValues.add(SelectionValues.entry(ProjectStatus.deleted.name(), ProjectUIFactory.translateStatus(projTranslator, ProjectStatus.deleted)));
		filters.add(new FlexiTableMultiSelectionFilter(projTranslator.translate("status"), FILTER_STATUS, statusValues, true));

		tableEl.setFilters(true, filters, false, false);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);

		FlexiFiltersTab tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				projTranslator.translate("tab.all"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabAll);

		FlexiFiltersTab tabActive = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ACTIVE,
				projTranslator.translate("project.list.tab.active"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.active.name())));
		tabs.add(tabActive);

		FlexiFiltersTab tabDone = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DONE,
				projTranslator.translate("project.list.tab.done"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.done.name())));
		tabs.add(tabDone);

		FlexiFiltersTab tabDeleted = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_DELETED,
				projTranslator.translate("tab.deleted"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, ProjectStatus.deleted.name())));
		tabs.add(tabDeleted);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}

	private void applyFilters(ProjProjectSearchParams searchParams) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;

		for (FlexiTableFilter filter : filters) {
			if (FILTER_STATUS.equals(filter.getFilter())) {
				List<String> status = ((FlexiTableMultiSelectionFilter) filter).getValues();
				if (status != null && !status.isEmpty()) {
					searchParams.setStatus(status.stream().map(ProjectStatus::valueOf).toList());
				} else {
					searchParams.setStatus(null);
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se && TABLE_ACTION_REMOVE.equals(se.getCommand())) {
				ProjectsOverviewRow row = dataModel.getObject(se.getIndex());
				doRemove(ureq, Collections.singleton(row));
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		} else if (source == bulkRemoveEl) {
			List<ProjectsOverviewRow> rows = getSelectedRows();
			doRemove(ureq, rows);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == projectRemoveUserDialogBoxCtrl) {
			if (event == Event.DONE_EVENT) {
				List<ProjProject> projectsToLeave = projectRemoveUserDialogBoxCtrl.getProjectsToLeave();
				removeUserFromProjects(projectsToLeave);
			}

			cmc.deactivate();
			cleanUp();
		}
	}

	private void removeUserFromProjects(List<ProjProject> projectsToLeave) {
		List<Identity> membersToRemove = Collections.singletonList(editedIdentity);

		for (ProjProject project : projectsToLeave) {
			projectService.removeMembers(getIdentity(), ProjectBCFactory.createFactory(project), project, membersToRemove);
		}

		loadModel();
		showInfo("remove.user.successful");
	}

	private List<ProjectsOverviewRow> getSelectedRows() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<ProjectsOverviewRow> rows = new ArrayList<>(selectedIndexes.size());
		for (Integer selectedIndex : selectedIndexes) {
			ProjectsOverviewRow row = dataModel.getObject(selectedIndex);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	private void doRemove(UserRequest ureq, Collection<ProjectsOverviewRow> selectedRows) {
		List<ProjProject> projectsToLeave = new ArrayList<>();
		for (ProjectsOverviewRow row : selectedRows) {
			projectsToLeave.add(row.getProject());
			List<Identity> projectOwners = projectService.getMembers(row.getProject(), Collections.singleton(ProjectRole.owner));
			if (projectOwners.size() == 1) {
				showError("error.atleastone", row.getTitle());
				return;
			}
		}

		projectRemoveUserDialogBoxCtrl = new ProjectRemoveUserDialogBoxController(ureq, getWindowControl(), editedIdentity, projectsToLeave);
		listenTo(projectRemoveUserDialogBoxCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), projectRemoveUserDialogBoxCtrl.getInitialComponent(),
				true, translate("remove.user.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(projectRemoveUserDialogBoxCtrl);
		cmc = null;
		projectRemoveUserDialogBoxCtrl = null;
	}
}
