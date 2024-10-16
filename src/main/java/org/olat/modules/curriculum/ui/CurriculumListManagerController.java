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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.manager.ExportCurriculumMediaResource;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerDataModel.CurriculumCols;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.lecture.LectureModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumListManagerController extends FormBasicController implements Activateable2 {

	public static final String CONTEXT_OVERVIEW = "Details";
	public static final String CONTEXT_STRUCTURE = "Structure";
	public static final String CONTEXT_LECTURES = "Lectures";
	public static final String CONTEXT_IMPLEMENTATIONS = "Implementations";
	public static final String CONTEXT_PREPARATION = "Preparation";
	public static final String CONTEXT_PROVISIONAL = "Provisional";
	public static final String CONTEXT_CONFIRMED = "Confirmed";
	public static final String CONTEXT_ACTIVE = "Active";
	public static final String CONTEXT_CANCELLED = "Cancelled";
	public static final String CONTEXT_FINISHED = "Finished";
	public static final String CONTEXT_DELETED = "Deleted";
	public static final List<String> CONTEXTS = List.of(CONTEXT_OVERVIEW, CONTEXT_IMPLEMENTATIONS,
			CONTEXT_PREPARATION, CONTEXT_PROVISIONAL, CONTEXT_CONFIRMED, CONTEXT_ACTIVE,
			CONTEXT_CANCELLED, CONTEXT_FINISHED, CONTEXT_DELETED);
	
	public static final String SUB_PATH_OVERVIEW = "/" + CONTEXT_OVERVIEW + "/0";
	public static final String SUB_PATH_IMPLEMENTATIONS = "/" + CONTEXT_IMPLEMENTATIONS + "/0";
	public static final String SUB_PATH_PREPARATION = SUB_PATH_IMPLEMENTATIONS + "/" + CONTEXT_PREPARATION + "/0";
	public static final String SUB_PATH_PROVISIONAL = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_PROVISIONAL + "/0";
	public static final String SUB_PATH_CONFIRMED = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_CONFIRMED + "/0";
	public static final String SUB_PATH_ACTIVE = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_ACTIVE + "/0";
	public static final String SUB_PATH_CANCELLED = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_CANCELLED + "/0";
	public static final String SUB_PATH_FINISHED = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_FINISHED + "/0";
	public static final String SUB_PATH_DELETED = SUB_PATH_IMPLEMENTATIONS +  "/" + CONTEXT_DELETED + "/0";
	
	private static final String ALL_TAB_ID = "All";

	private static final String FILTER_STATUS = "Status";
	private static final String FILTER_ORGANISATIONS = "Organisations";
	
	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private FormLink newCurriculumButton;
	private FormLink importCurriculumButton;
	private CurriculumManagerDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CurriculumComposerController composerCtrl;
	private EditCurriculumController newCurriculumCtrl;
	private ImportCurriculumController importCurriculumCtrl;
	private CurriculumDetailsController editCurriculumCtrl;
	private ConfirmCurriculumDeleteController deleteCurriculumCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private int counter = 0;
	private final Roles roles;
	private final boolean isMultiOrganisations;
	private final CurriculumSecurityCallback secCallback;

	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public CurriculumListManagerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manage_curriculum");
		this.toolbarPanel = toolbarPanel;
		this.secCallback = secCallback;
		roles = ureq.getUserSession().getRoles();
		toolbarPanel.addListener(this);
		isMultiOrganisations = organisationService.isMultiOrganisations();

		initForm(ureq);
		loadModel(null, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canNewCurriculum()) {
			newCurriculumButton = uifactory.addFormLink("add.curriculum", "add.curriculum", null, formLayout, Link.BUTTON);
			newCurriculumButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			newCurriculumButton.setElementCssClass("o_sel_add_curriculum");
			
			DropdownItem moreDropdown = uifactory.addDropdownMenuMore("more.menu", formLayout, getTranslator());
			moreDropdown.setEmbbeded(true);
			moreDropdown.setButton(true);
			
			importCurriculumButton = uifactory.addFormLink("import.curriculum", "import.curriculum", null, formLayout, Link.LINK);
			importCurriculumButton.setElementCssClass("o_sel_import_curriculum");
			moreDropdown.addElement(importCurriculumButton);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.externalId));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(isMultiOrganisations, CurriculumCols.organisation));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.numOfElements, CONTEXT_IMPLEMENTATIONS));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.numOfPreparationRootElements, CONTEXT_PREPARATION));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.numOfProvisionalRootElements, CONTEXT_PROVISIONAL));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.numOfConfirmedRootElements, CONTEXT_CONFIRMED));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.numOfActiveRootElements, CONTEXT_ACTIVE));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.numOfCancelledRootElements, CONTEXT_CANCELLED));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.numOfFinishedRootElements, CONTEXT_FINISHED));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.numOfDeletedRootElements, CONTEXT_DELETED));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));

		if(lectureModule.isEnabled()) {
			DefaultFlexiColumnModel lecturesCol = new DefaultFlexiColumnModel("table.header.lectures", CurriculumCols.lectures.ordinal(), "lectures",
					new BooleanCellRenderer(new StaticFlexiCellRenderer("", "lectures", null,
							"o_icon o_icon-fw o_icon-lg o_icon_lecture", translate("table.header.lectures")),
							null));
			lecturesCol.setExportable(false);
			lecturesCol.setIconHeader("o_icon o_icon-lg o_icon_lecture");
			columnsModel.addFlexiColumnModel(lecturesCol);
		}
		
		if(secCallback.canEditCurriculum()) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(CurriculumCols.tools);
			toolsCol.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_actions");
			toolsCol.setExportable(false);
			toolsCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		tableModel = new CurriculumManagerDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("table.curriculum.empty", null, "o_icon_curriculum_element", "add.curriculum", "o_icon_add", true);
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-manage");
		
		initFilters();
		initFiltersPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(roles.isAdministrator()) {
			SelectionValues statusValues = new SelectionValues();
			statusValues.add(SelectionValues.entry(CurriculumStatus.active.name(), translate("filter.active")));
			statusValues.add(SelectionValues.entry(CurriculumStatus.deleted.name(), translate("filter.deleted")));
			FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
					FILTER_STATUS, statusValues, true);
			filters.add(statusFilter);
		}
		
		if(isMultiOrganisations) {
			List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
					OrganisationRoles.administrator, OrganisationRoles.curriculummanager);
			SelectionValues organisationsValues = new SelectionValues();
			for(Organisation organisation:organisations) {
				organisationsValues.add(SelectionValues
						.entry(organisation.getKey().toString(), StringHelper.escapeHtml(organisation.getDisplayName())));
			}
			FlexiTableMultiSelectionFilter organisationsFilter = new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
					FILTER_ORGANISATIONS, organisationsValues, true);
			filters.add(organisationsFilter);
		}

		if(!filters.isEmpty()) {
			tableEl.setFilters(true, filters, false, false);
		}
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel(String searchString, boolean reset) {
		CurriculumSearchParameters managerParams = getSearchParams(searchString);
		managerParams.setCurriculumAdmin(getIdentity());
		List<CurriculumInfos> managerCurriculums = curriculumService.getCurriculumsWithInfos(managerParams);
		List<CurriculumRow> rows = managerCurriculums.stream()
				.map(cur -> forgeManagedRow(cur, true))
				.collect(Collectors.toList());
		Set<CurriculumRow> deduplicateRows = new HashSet<>(rows);
		
		if(roles.isPrincipal()) {
			CurriculumSearchParameters principalParams = getSearchParams(searchString);
			principalParams.setCurriculumPrincipal(getIdentity());
			List<CurriculumInfos> principalsCurriculums = curriculumService.getCurriculumsWithInfos(principalParams);
			List<CurriculumRow> principalsRows = principalsCurriculums.stream()
					.map(cur -> forgeManagedRow(cur, false))
					.filter(row -> !deduplicateRows.contains(row))
					.toList();
			rows.addAll(principalsRows);
			deduplicateRows.addAll(principalsRows);
		}
		
		CurriculumSearchParameters ownerParams = getSearchParams(searchString);
		ownerParams.setElementOwner(getIdentity());
		List<CurriculumInfos> reOwnersCurriculums = curriculumService.getCurriculumsWithInfos(ownerParams);
		List<CurriculumRow> reOwnerRows = reOwnersCurriculums.stream()
				.filter(c -> !managerCurriculums.contains(c))
				.map(c -> new CurriculumRow(c, getBusinessPathUrl(c)))
				.filter(row -> !deduplicateRows.contains(row))
				.toList();
		
		rows.addAll(reOwnerRows);
		
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	private String getBusinessPathUrl(CurriculumInfos curriculum) {
		String path = "[CurriculumAdmin:0][Curriculum:" + curriculum.curriculum().getKey() + "]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(path);
		return BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
	}
	
	private CurriculumSearchParameters getSearchParams(String searchString) {
		// curriculum owners, curriculum manages and administrators can edit curriculums
		// principals can only view them
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		searchParams.setSearchString(searchString);
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_STATUS);
		if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumStatus> status = filterValues.stream()
						.map(CurriculumStatus::valueOf).toList();
				searchParams.setStatusList(status);
			}
		}
		
		FlexiTableFilter organisationsFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_ORGANISATIONS);
		if (organisationsFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<OrganisationRef> organisations = filterValues.stream()
						.filter(StringHelper::isLong)
						.map(Long::valueOf)
						.map(OrganisationRefImpl::new)
						.map(OrganisationRef.class::cast)
						.toList();
				searchParams.setOrganisations(organisations);
			}
		}
		
		return searchParams;
	}
	
	/**
	 * This create a row with management rights.
	 * 
	 * @param curriculum The curriculum informations
	 * @return A curriculum row
	 */
	private CurriculumRow forgeManagedRow(CurriculumInfos curriculum, boolean canManage) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		String businessPathUrl = getBusinessPathUrl(curriculum);
		CurriculumRow row = new CurriculumRow(curriculum, businessPathUrl, toolsLink, canManage);
		toolsLink.setUserObject(row);
		return row;
	}

	@Override
	protected void doDispose() {
		if(toolbarPanel != null) {
			toolbarPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Curriculum".equalsIgnoreCase(type)) {
			Long curriculumKey = entries.get(0).getOLATResourceable().getResourceableId();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			activateCurriculum(ureq, curriculumKey, subEntries);
		}
	}
	
	private void activateCurriculum(UserRequest ureq, Long curriculumKey, List<ContextEntry> entries) {
		if(composerCtrl != null && curriculumKey.equals(composerCtrl.getCurriculum().getKey())) return;
		
		List<CurriculumRow> rows = tableModel.getObjects();
		for(CurriculumRow row:rows) {
			if(curriculumKey.equals(row.getKey())) {
				doOpenCurriculumDetails(ureq, row, entries);
				break;
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(tableEl.getQuickSearchString(), true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editCurriculumCtrl == source || importCurriculumCtrl == source
				|| deleteCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(tableEl.getQuickSearchString(), false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(importCurriculumCtrl);
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(newCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		importCurriculumCtrl = null;
		deleteCurriculumCtrl = null;
		newCurriculumCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(toolbarPanel == source) {
			if(event instanceof PopEvent pe && pe.getController() instanceof CurriculumComposerController) {
				removeAsListenerAndDispose(composerCtrl);
				composerCtrl = null;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newCurriculumButton == source) {
			doNewCurriculum(ureq);
		} else if(importCurriculumButton == source) {
			doImportCurriculum(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("select".equals(cmd) || CONTEXT_OVERVIEW.equals(cmd)) {
					doOpenCurriculumDetails(ureq, tableModel.getObject(se.getIndex()));
				} else if("lectures".equals(cmd)) {
					doOpenCurriculumLectures(ureq, tableModel.getObject(se.getIndex()));
				} else if(CurriculumElementStatus.isValueOf(cmd.toLowerCase())) {
					doOpenCurriculumImplementations(ureq, tableModel.getObject(se.getIndex()), cmd);
				}
			} else if(event instanceof FlexiTableSearchEvent ftse) {
				doSearch(ftse);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doNewCurriculum(ureq);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSearch(FlexiTableSearchEvent event) {
		loadModel(event.getSearch(), true);
	}
	
	private void doImportCurriculum(UserRequest ureq) {
		if(guardModalController(importCurriculumCtrl)) return;

		importCurriculumCtrl = new ImportCurriculumController(ureq, getWindowControl());
		listenTo(importCurriculumCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importCurriculumCtrl.getInitialComponent(), true, translate("import.curriculum"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doNewCurriculum(UserRequest ureq) {
		if(guardModalController(newCurriculumCtrl)) return;

		newCurriculumCtrl = new EditCurriculumController(ureq, getWindowControl(), secCallback);
		listenTo(newCurriculumCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newCurriculumCtrl.getInitialComponent(), true, translate("add.curriculum"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteCurriculum(UserRequest ureq, CurriculumRow row) {
		removeAsListenerAndDispose(deleteCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		
		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			deleteCurriculumCtrl = new ConfirmCurriculumDeleteController(ureq, getWindowControl(), row);
			listenTo(deleteCurriculumCtrl);
			
			String title = translate("delete.curriculum.title", StringHelper.escapeHtml(row.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCurriculumCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doExportCurriculum(UserRequest ureq, CurriculumRow row) {
		Curriculum curriculum = curriculumService.getCurriculum(row);
		MediaResource mr = new ExportCurriculumMediaResource(curriculum);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
	
	private void doOpenCurriculumDetails(UserRequest ureq, CurriculumRow row) {
		doOpenCurriculumDetails(ureq, row, List.of());
	}
	
	private void doOpenCurriculumImplementations(UserRequest ureq, CurriculumRow row, String status) {
		List<ContextEntry> context = BusinessControlFactory.getInstance()
				.createCEListFromString("[Implementations:0][" + status + ":0]");
		doOpenCurriculumDetails(ureq, row, context);
	}
	
	private void doOpenCurriculumLectures(UserRequest ureq, CurriculumRow row) {
		List<ContextEntry> context = BusinessControlFactory.getInstance()
				.createCEListFromString("[Lectures:0]");
		doOpenCurriculumDetails(ureq, row, context);
	}
	
	private void doOpenCurriculumDetails(UserRequest ureq, CurriculumRow row, List<ContextEntry> entries) {
		removeAsListenerAndDispose(editCurriculumCtrl);
		
		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			showWarning("warning.curriculum.deleted");
		} else {
			editCurriculumCtrl = new CurriculumDetailsController(ureq, getWindowControl(), toolbarPanel, curriculum, secCallback);
			listenTo(editCurriculumCtrl);
			
			String crumb = row.getExternalRef();
			if(!StringHelper.containsNonWhitespace(crumb)) {
				crumb = row.getDisplayName();
			}
			toolbarPanel.pushController(crumb, editCurriculumCtrl);
			editCurriculumCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		Curriculum curriculum = curriculumService.getCurriculum(row);
		if(curriculum == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, curriculum);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link deleteLink;
		private Link exportLink;
		private final VelocityContainer mainVC;

		private CurriculumRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumRow row, Curriculum curriculum) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			exportLink = addLink("export", "o_icon_export", links);
			
			if(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doOpenCurriculumDetails(ureq, row, List.of());
			} else if(deleteLink == source) {
				close();
				doDeleteCurriculum(ureq, row);
			} else if(exportLink == source) {
				close();
				doExportCurriculum(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
