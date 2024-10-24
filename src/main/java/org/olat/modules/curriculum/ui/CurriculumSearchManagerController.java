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
import java.util.List;
import java.util.Objects;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.BulkDeleteConfirmationController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumElementSearchDataModel.SearchCols;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.olat.modules.curriculum.ui.event.SelectLectureBlockEvent;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSearchManagerController extends FormBasicController {

	private static final String ALL_TAB_ID = "All";

	private static final String FILTER_TYPE = "Type";
	private static final String FILTER_STATUS = "Status";
	private static final String FILTER_CURRICULUM = "Curriculum";
	
	private FlexiFiltersTab allTab;
	
	private FormLink bulkDeleteButton;
	private FlexiTableElement tableEl;
	private CurriculumElementSearchDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ReferencesController referencesCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmCurriculumElementDeleteController confirmDeleteCtrl;
	private BulkDeleteConfirmationController bulkDeleteConfirmationCtrl;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;
	
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumSearchManagerController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, String searchString,
			CurriculumSecurityCallback secCallback, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl, "curriculum_element_search");
		this.toolbarPanel = toolbarPanel;
		this.secCallback = secCallback;
		this.lecturesSecCallback = lecturesSecCallback;
		initForm(ureq);
		if(StringHelper.containsNonWhitespace(searchString)) {
			tableEl.quickSearch( ureq, searchString);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SearchCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.structure));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SearchCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.curriculum));
		DateWithDayFlexiCellRenderer dateRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.typeDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				SearchCols.numOfMembers, "members"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				SearchCols.numOfParticipants, "participants"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				SearchCols.numOfCoaches, "coachs"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				SearchCols.numOfOwners, "owners"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(SearchCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);

		tableModel = new CurriculumElementSearchDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("table.search.curriculum.empty", null, "o_icon_curriculum_element");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-search-manage");
		
		initFilters();
		initFiltersPresets();
		
		tableEl.setSelectedFilterTab(ureq, allTab);
		
		if(secCallback.canNewCurriculumElement()) {
			bulkDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkDeleteButton);
			tableEl.setMultiSelect(true);
		}
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
		if(!curriculums.isEmpty()) {
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum curriculum:curriculums) {
				curriculumValues.add(SelectionValues.entry(curriculum.getKey().toString(),
						StringHelper.escapeHtml(curriculum.getDisplayName())));
			}
			
			FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.deleted.name(), translate("filter.deleted")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		SelectionValues typesValues = new SelectionValues();
		for(CurriculumElementType type:types) {
			typesValues.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		FlexiTableMultiSelectionFilter typeFilter = new FlexiTableMultiSelectionFilter(translate("filter.types"),
				FILTER_TYPE, typesValues, true);
		filters.add(typeFilter);
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tab(ALL_TAB_ID, translate("filter.all"), TabSelectionBehavior.clear);
		allTab.setLargeSearch(true);
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if( confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doSearch(tableEl.getQuickSearchString(), tableEl.getFilters());
			}
			cmc.deactivate();
			cleanUp();
		} else if (referencesCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent sre) {
				launch(ureq, sre.getEntry());
			}
		} else if(bulkDeleteConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doDeleteCurriculumElements((ToDelete)bulkDeleteConfirmationCtrl.getUserObject());
				doSearch(tableEl.getQuickSearchString(), tableEl.getFilters());
			}
			cmc.deactivate();
			cleanUp();
		} else if(curriculumStructureCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent sre) {
				launch(ureq, sre.getEntry());
			} else if(event instanceof SelectCurriculumElementRowEvent scee) {
				List<ContextEntry> subEntries = BusinessControlFactory.getInstance().createCEListFromString("[Structure:0]");
				doOpenCurriculumElementDetails(ureq, scee.getCurriculumElement(), subEntries);
			} else if(event instanceof SelectLectureBlockEvent slbe) {
				List<ContextEntry> subEntries = BusinessControlFactory.getInstance().createCEListFromString("[Lectures:0]");
				doOpenCurriculumElementDetails(ureq, slbe.getCurriculumElement(), subEntries);
			}
		} else if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumStructureCalloutCtrl);
		removeAsListenerAndDispose(bulkDeleteConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumStructureCalloutCtrl = null;
		bulkDeleteConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		referencesCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bulkDeleteButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementDetails(ureq, row.getCurriculumElement(), null);
				} else if("members".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, null);
				} else if("participants".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Participants");
				} else if("coachs".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Coachs");
				} else if("owners".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Owners");
				}
			} else if(event instanceof FlexiTableSearchEvent se) {
				doSearch(se.getSearch(), se.getFilters());
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd) && link.getUserObject() instanceof CurriculumElementSearchRow row) {
				doOpenTools(ureq, row, link);
			} else if("resources".equals(cmd) && link.getUserObject() instanceof CurriculumElementSearchRow row) {
				doOpenReferences(ureq, row, link);
			} else if("structure".equals(cmd) && link.getUserObject() instanceof CurriculumElementSearchRow row) {
				doOpenStructure(ureq, row, link);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSearch(String searchString, List<FlexiTableFilter> filters) {
		CurriculumElementSearchParams params = getSearchParams(searchString, filters);

		List<CurriculumElementSearchInfos> elements = curriculumService.searchCurriculumElements(params);
		List<CurriculumElementSearchRow> rows = new ArrayList<>(elements.size());
		for(CurriculumElementSearchInfos element:elements) {
			CurriculumElementSearchRow row = forgeRow(element);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementSearchParams getSearchParams(String searchString, List<FlexiTableFilter> filters) {
		CurriculumElementSearchParams params = new CurriculumElementSearchParams(getIdentity());
		params.setSearchString(searchString);
		
		FlexiTableFilter curriculumFilter = FlexiTableFilter.getFilter(filters, FILTER_CURRICULUM);
		if (curriculumFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumRef> curriculums = filterValues.stream()
						.map(Long::valueOf)
						.map(CurriculumRefImpl::new)
						.map(CurriculumRef.class::cast)
						.toList();
				params.setCurriculums(curriculums);
			}
		}
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, FILTER_STATUS);
		if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumElementStatus> status = filterValues.stream()
						.map(CurriculumElementStatus::valueOf)
						.toList();
				params.setStatus(status);
			}
		}
		
		FlexiTableFilter typeFilter = FlexiTableFilter.getFilter(filters, FILTER_TYPE);
		if (typeFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumElementTypeRef> types = filterValues.stream()
						.map(Long::valueOf)
						.map(CurriculumElementTypeRefImpl::new)
						.map(CurriculumElementTypeRef.class::cast)
						.toList();
				params.setElementTypes(types);
			}
		}
		
		return params;
	}
	
	private CurriculumElementSearchRow forgeRow(CurriculumElementSearchInfos element) {
		String id = element.curriculumElement().getKey().toString();
		
		FormLink toolsLink = uifactory.addFormLink("tools_".concat(id), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setTitle(translate("actions.more"));
		
		FormLink structureLink = uifactory.addFormLink("structure_".concat(id), "structure", "", null, null, Link.NONTRANSLATED);
		structureLink.setIconLeftCSS("o_icon o_icon-lg o_icon_curriculum_structure");
		structureLink.setTitle(translate("action.structure"));
		
		FormLink resourcesLink = null;
		if(element.numOfResources() > 0) {
			resourcesLink = uifactory.addFormLink("resources_".concat(id), "resources", String.valueOf(element.numOfResources()),
					null, null, Link.NONTRANSLATED);
		}
		
		CurriculumElementSearchRow row = new CurriculumElementSearchRow(element.curriculumElement(),
				element.numOfResources(), element.numOfParticipants(), element.numOfCoaches(), element.numOfOwners(),
				resourcesLink, structureLink, toolsLink);
		toolsLink.setUserObject(row);
		structureLink.setUserObject(row);
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}
		return row;
	}
	
	private void doOpenCurriculumElementUserManagement(UserRequest ureq, CurriculumElementSearchRow row, String memberType) {
		String path = "[Members:0]";
		if(memberType != null) {
			path += "[" + memberType + ":0]";
		}
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString(path);
		doOpenCurriculumElementDetails(ureq, row.getCurriculumElement(), overview);
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElement row, List<ContextEntry> entries) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			Curriculum curriculum = row.getCurriculum();
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, row.getKey()), null);
			CurriculumElementDetailsController editCtrl = new CurriculumElementDetailsController(ureq, swControl, toolbarPanel, curriculum, element,
					secCallback, lecturesSecCallback);
			listenTo(editCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCtrl);
			editCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doOpenCurriculumElementInNewWindow(CurriculumElementSearchRow row) {
		String path = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + row.getCurriculumKey() + "][Implementations:0][CurriculumElement:" + row.getKey() + "][Overview:0]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
	
	private void doOpenStructure(UserRequest ureq, CurriculumElementSearchRow row, FormLink link) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(curriculumElement);
		CurriculumElement rootElement = parentLine.get(0);
		
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(), rootElement, curriculumElement);
		listenTo(curriculumStructureCalloutCtrl);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true,  null);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				curriculumStructureCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementSearchRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, element);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementSearchRow row, FormLink link) {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null ) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), element);
			listenTo(referencesCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementSearchRow row) {
		if(guardModalController(confirmDeleteCtrl)) return;
		
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		if(curriculumElement == null) {
			showWarning("warning.curriculum.deleted");
			doSearch(tableEl.getQuickSearchString(), tableEl.getFilters());
		} else {
			confirmDeleteCtrl = new ConfirmCurriculumElementDeleteController(ureq, getWindowControl(), curriculumElement);
			listenTo(confirmDeleteCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true,
					translate("confirmation.delete.element.title", row.getDisplayName()));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		if(guardModalController(bulkDeleteConfirmationCtrl)) return;

		List<CurriculumElementSearchRow> curriculumElements =  tableEl.getMultiSelectedIndex().stream()
				.map(index  -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(element -> secCallback.canEditCurriculumElement(element.getCurriculumElement())
						&& !CurriculumElementManagedFlag.isManaged(element.getCurriculumElement(), CurriculumElementManagedFlag.delete))
				.toList();
		
		if(curriculumElements.isEmpty()) {
			showWarning("curriculums.elements.bulk.delete.empty.selection");
		} else {
			List<String> curriculumsNames = curriculumElements.stream()
					.map(CurriculumElementSearchRow::getDisplayName)
					.toList();

			bulkDeleteConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(), 
					translate("curriculums.elements.bulk.delete.text", String.valueOf(curriculumElements.size())),
					translate("curriculums.elements.bulk.delete.confirm", String.valueOf(curriculumElements.size())),
					translate("curriculums.elements.bulk.delete.button"),
					translate("curriculums.elements.bulk.delete.topics"),
					curriculumsNames,
					null);
			
			bulkDeleteConfirmationCtrl.setUserObject(new ToDelete(curriculumElements));
			listenTo(bulkDeleteConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
					true, translate("curriculums.elements.bulk.delete.title"), true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDeleteCurriculumElements(ToDelete toDelete) {
		for(CurriculumElementSearchRow element:toDelete.elements()) {
			CurriculumElement elementToDelete = curriculumService.getCurriculumElement(element);
			if(elementToDelete != null) {
				curriculumService.deleteCurriculumElement(element);
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link openLink;
		private Link deleteLink;
		private Link manageMembersLink;
		
		private CurriculumElementSearchRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementSearchRow row, CurriculumElement element) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			
			openLink = addLink("open.new.tab", "o_icon_arrow_up_right_from_square", links);
			openLink.setNewWindow(true, true);

			if(secCallback.canManagerCurriculumElementUsers(element)) {
				if(!links.isEmpty()) {
					links.add("-");
				}
				manageMembersLink = addLink("manage.members", "o_icon_group", links);
			}
			
			if(secCallback.canEditCurriculumElement(element) && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.delete)) {
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
			if(openLink == source) {
				close();
				doOpenCurriculumElementInNewWindow(row);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			} else if(manageMembersLink == source) {
				close();
				doOpenCurriculumElementUserManagement(ureq, row, null);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
	
	private record ToDelete(List<CurriculumElementSearchRow> elements) {
		//
	}
}
