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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.SortKey;
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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
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
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
import org.olat.modules.lecture.ui.blockimport.BlocksImport_1_InputStep;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlock;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlocks;
import org.olat.modules.lecture.ui.component.IconDecoratorCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.lecture.ui.event.EditLectureBlockRowEvent;
import org.olat.modules.lecture.ui.export.LectureBlockAuditLogExport;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositoryController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String ALL_TAB_ID = "All";
	private static final String PAST_TAB_ID = "Past";
	private static final String CURRENT_TAB_ID = "Current";
	private static final String CLOSED_TAB_ID = "Closed";
	private static final String PENDING_TAB_ID = "Pending";
	private static final String WITHOUT_TEACHERS_TAB_ID = "WithoutTeachers";
	
	private static final String FILTER_ONE_LEVEL_ONLY = "OneLevelOnly";
	private static final String FILTER_ROLL_CALL_STATUS = "Status";
	
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink addLectureButton;
	private FormLink deleteLecturesButton;
	private FormLink importLecturesButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	private final VelocityContainer detailsVC;

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab pastTab;
	private FlexiFiltersTab closedTab;
	private FlexiFiltersTab currentTab;
	private FlexiFiltersTab pendingTab;
	private FlexiFiltersTab withoutTeachersTab;

	private FlexiTableMultiSelectionFilter rollCallStatusFilter;
	private FlexiTableOneClickSelectionFilter oneLevelOnlyFilter;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private StepsMainRunController importBlockWizard;
	private EditLectureBlockController editLectureCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmDeleteLectureBlockController deleteLectureBlocksCtrl;

	private int counter = 0;
	private final RepositoryEntry entry;
	private final boolean lectureManagementManaged;
	private final CurriculumElement curriculumElement;
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = entry;
		this.curriculumElement = null;
		this.secCallback = secCallback;
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel();
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = null;
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!lectureManagementManaged && secCallback.canNewLectureBlock()) {
			addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);
			addLectureButton.setIconLeftCSS("o_icon o_icon_add");
			addLectureButton.setElementCssClass("o_sel_repo_add_lecture");
			
			DropdownItem addTaskDropdown = uifactory.addDropdownMenu("import.lectures.dropdown", null, null, formLayout, getTranslator());
			addTaskDropdown.setOrientation(DropdownOrientation.right);
			addTaskDropdown.setElementCssClass("o_sel_add_more");
			addTaskDropdown.setEmbbeded(true);
			addTaskDropdown.setButton(true);

			importLecturesButton = uifactory.addFormLink("import.lectures", formLayout, Link.LINK);
			importLecturesButton.setIconLeftCSS("o_icon o_icon_import");
			importLecturesButton.setElementCssClass("o_sel_repo_import_lectures");
			addTaskDropdown.addElement(importLecturesButton);
			
			deleteLecturesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title, TOGGLE_DETAILS_CMD));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.externalRef));
		if(entry != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.assessmentMode,
				new BooleanCellRenderer(new CSSIconFlexiCellRenderer("o_icon_assessment_mode"), null)));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.curriculumElement,
				new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.entry,
				new ReferenceRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date,
				new DateWithDayFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.lecturesNumber));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location,
				new IconDecoratorCellRenderer("o_icon o_icon-fw o_icon_location")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teachers,
				new IconDecoratorCellRenderer("o_icon o_icon-fw o_icon_user")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.numParticipants));
		
		DefaultFlexiColumnModel compulsoryColumn = new DefaultFlexiColumnModel(false, BlockCols.compulsory,
				new YesNoCellRenderer());
		compulsoryColumn.setIconHeader("o_icon o_icon_compulsory o_icon-lg");
		columnsModel.addFlexiColumnModel(compulsoryColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.status,
				new LectureBlockStatusCellRenderer(getTranslator())));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit"), null));
		editColumn.setExportable(false);
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);
			
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(BlockCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmptyTableMessageKey("empty.table.lectures.blocks.admin");
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(BlockCols.date.name(), false));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, entry == null ? "curriculum-lecture-block-list-v1" : "repo-lecture-block-list-v3");
		tableEl.addBatchButton(deleteLecturesButton);
		
		initFilters();
		initFiltersPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(curriculumElement != null) {
			SelectionValues assessableValues = new SelectionValues();
			assessableValues.add(SelectionValues.entry(FILTER_ONE_LEVEL_ONLY, translate("filter.one.level.only")));
			oneLevelOnlyFilter = new FlexiTableOneClickSelectionFilter(translate("filter.one.level.only"),
					FILTER_ONE_LEVEL_ONLY, assessableValues, true);
			filters.add(oneLevelOnlyFilter);
		}

		SelectionValues rollCallStatusValues = new SelectionValues();
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.open.name(), translate("search.form.status.open")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.closed.name(), translate("search.form.status.closed")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.autoclosed.name(), translate("search.form.status.autoclosed")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.reopen.name(), translate("search.form.status.reopen")));
		rollCallStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_ROLL_CALL_STATUS, rollCallStatusValues, true);
		filters.add(rollCallStatusFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_elements_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		currentTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CURRENT_TAB_ID, translate("filter.current"),
				TabSelectionBehavior.nothing, List.of());
		currentTab.setFiltersExpanded(true);
		tabs.add(currentTab);
		
		pastTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PAST_TAB_ID, translate("filter.past"),
				TabSelectionBehavior.nothing, List.of());
		pastTab.setFiltersExpanded(true);
		tabs.add(pastTab);
		
		withoutTeachersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHOUT_TEACHERS_TAB_ID, translate("filter.without.teachers"),
				TabSelectionBehavior.nothing, List.of());
		withoutTeachersTab.setFiltersExpanded(true);
		tabs.add(withoutTeachersTab);
		
		pendingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PENDING_TAB_ID, translate("filter.pending"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
						List.of(LectureRollCallStatus.open.name(), LectureRollCallStatus.reopen.name()))));
		pendingTab.setFiltersExpanded(true);
		tabs.add(pendingTab);
		
		closedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CLOSED_TAB_ID, translate("filter.closed"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
						List.of(LectureRollCallStatus.closed.name(), LectureRollCallStatus.autoclosed.name()))));
		closedTab.setFiltersExpanded(true);
		tabs.add(closedTab);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof LectureBlockRow lectureRow
				&& lectureRow.getDetailsController() != null) {
			components.add(lectureRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	private void loadModel() {
		String displayname = null;
		String externalRef = null;
		if(curriculumElement != null) {
			displayname = curriculumElement.getDisplayName();
			externalRef = curriculumElement.getExternalId();
		} else if(entry != null) {
			displayname = entry.getDisplayname();
			externalRef = entry.getExternalRef();
		} else {
			return;
		}
		
		LecturesBlockSearchParameters searchParams = getSearchParams();
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
		
		List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlockWithTeachers block:blocks) {
			LectureBlock b = block.getLectureBlock();
			StringBuilder teachers = new StringBuilder();
			String separator = translate("user.fullname.separator");
			for(Identity teacher:block.getTeachers()) {
				if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
				teachers.append(userManager.getUserDisplayName(teacher));
			}

			LectureBlockRow row = new LectureBlockRow(b, displayname, externalRef,
					teachers.toString(), false, block.getCurriculumElementRef(), block.getEntryRef(),
					block.getNumOfParticipants(), block.isAssessmentMode());
			rows.add(row);
			
			String linkName = "tools-" + counter++;
			FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
			toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setTitle(translate("action.more"));
			toolsLink.setUserObject(row);
			flc.add(linkName, toolsLink);
			row.setToolsLink(toolsLink);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);

		if(deleteLecturesButton != null) {
			deleteLecturesButton.setVisible(!rows.isEmpty());
		}
	}
	
	private LecturesBlockSearchParameters getSearchParams() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		
		if(curriculumElement != null) {
			boolean oneLevelOnly =  isFilterSelected(FILTER_ONE_LEVEL_ONLY);
			searchParams.setCurriculumElementPath(oneLevelOnly ? null : curriculumElement.getMaterializedPathKeys());
			searchParams.setCurriculumElement(oneLevelOnly ? curriculumElement : null);
		} else if(entry != null) {
			searchParams.setEntry(entry);
		}
		
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_ROLL_CALL_STATUS);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<LectureRollCallStatus> status = filterValues.stream()
						.map(LectureRollCallStatus::valueOf).toList();
				searchParams.setRollCallStatus(status);
			}
		}
		
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if(selectedTab == currentTab) {
			searchParams.setStartDate(new Date());
		} else if(selectedTab == pastTab || selectedTab == pendingTab) {
			searchParams.setEndDate(new Date());
		} else if(selectedTab == withoutTeachersTab) {
			searchParams.setWithTeachers(Boolean.FALSE);
		}
		
		String quickSearch = tableEl.getQuickSearchString();
		if(StringHelper.containsNonWhitespace(quickSearch)) {
			searchParams.setSearchString(quickSearch);
		}
		
		return searchParams;
	}
	
	private boolean isFilterSelected(String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}
	
	private void reloadModel(UserRequest ureq, LectureBlock lectureBlock) {
		LectureBlockRow row = tableModel.getObject(lectureBlock);
		if(row == null) {
			loadModel();
		} else {
			row.setLectureBlock(lectureBlock);
			if(row.getDetailsController() != null) {
				doOpenLectureBlockDetails(ureq, row);
				tableEl.reset(false, false, false);
			} else {
				tableEl.reset(false, false, true);
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLectureButton == source) {
			doAddLectureBlock(ureq);
		} else if(deleteLecturesButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(importLecturesButton == source) {
			doImportLecturesBlock(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				LectureBlockRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditLectureBlock(ureq, row);
				} else if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					if(row.getDetailsController() != null) {
						doCloseLectureBlockDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						this.doOpenLectureBlockDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				LectureBlockRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenLectureBlockDetails(ureq, row);
				} else {
					doCloseLectureBlockDetails(row);
				}
			}
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools-")
					&& link.getUserObject() instanceof LectureBlockRow lectureBlockRow) {
				doOpenTools(ureq, lectureBlockRow, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editLectureCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reloadModel(ureq, editLectureCtrl.getLectureBlock());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(importBlockWizard == source) {
			getWindowControl().pop();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
		} else if(deleteLectureBlocksCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source instanceof LectureListDetailsController) {
			if(event instanceof EditLectureBlockRowEvent editRowEvent) {
				doEditLectureBlock(ureq, editRowEvent.getRow());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteLectureBlocksCtrl);
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteLectureBlocksCtrl = null;
		toolsCalloutCtrl = null;
		editLectureCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEditLectureBlock(UserRequest ureq, LectureBlockRow row) {
		if(guardModalController(editLectureCtrl)) return;
		
		LectureBlock block = lectureService.getLectureBlock(row);
		boolean readOnly = lectureManagementManaged || !secCallback.canNewLectureBlock();
		if(entry != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry, block, readOnly);
		} else if(curriculumElement != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), curriculumElement, block, readOnly);
		} else {
			showWarning("error.no.entry.curriculum");
			return;
		}
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLectureBlock(UserRequest ureq) {
		if(guardModalController(editLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		if(entry != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry);
		} else if(curriculumElement != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), curriculumElement);
		} else {
			showWarning("error.no.entry.curriculum");
			return;
		}
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doImportLecturesBlock(UserRequest ureq) {
		removeAsListenerAndDispose(importBlockWizard);

		final ImportedLectureBlocks lectureBlocks = new ImportedLectureBlocks();
		Step start = new BlocksImport_1_InputStep(ureq, entry, curriculumElement, lectureBlocks);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			doFinalizeImportedLectureBlocks(lectureBlocks);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importBlockWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("tools.import.table"), "o_sel_lecture_table_import_wizard");
		listenTo(importBlockWizard);
		getWindowControl().pushAsModalDialog(importBlockWizard.getInitialComponent());
	}
	
	private void doFinalizeImportedLectureBlocks(ImportedLectureBlocks lectureBlocks) {
		List<ImportedLectureBlock> importedBlocks = lectureBlocks.getLectureBlocks();
		for(ImportedLectureBlock importedBlock:importedBlocks) {
			LectureBlock lectureBlock = importedBlock.getLectureBlock();
			boolean exists = lectureBlock.getKey() != null;

			List<Group> groups;
			if(importedBlock.getGroupMapping() != null && importedBlock.getGroupMapping().getGroup() != null) {
				groups = Collections.singletonList(importedBlock.getGroupMapping().getGroup());
			} else {
				groups = new ArrayList<>();
			}
			lectureBlock = lectureService.save(lectureBlock, groups);
			
			if(exists) {
				lectureService.adaptRollCalls(lectureBlock);
			}
			for(Identity teacher:importedBlock.getTeachers()) {
				lectureService.addTeacher(lectureBlock, teacher);
			}
		}
	}
	
	private void doCopy(LectureBlockRow row) {
		String newTitle = translate("lecture.block.copy",row.getLectureBlock().getTitle());
		lectureService.copyLectureBlock(newTitle, row.getLectureBlock());
		loadModel();
		showInfo("lecture.block.copied");
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<LectureBlock> blocks = new ArrayList<>();
		for(Integer selection:selections) {
			LectureBlockRow blockRow = tableModel.getObject(selection);
			if(!LectureBlockManagedFlag.isManaged(blockRow.getLectureBlock(), LectureBlockManagedFlag.delete)) {
				blocks.add(blockRow.getLectureBlock());
			}
		}
		
		if(blocks.isEmpty()) {
			showWarning("error.atleastone.lecture");
		} else {
			deleteLectureBlocksCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), blocks);
			listenTo(deleteLectureBlocksCtrl);
			
			String title = translate("delete.lectures.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteLectureBlocksCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, LectureBlockRow row) {
		List<LectureBlock> blocks = Collections.singletonList(row.getLectureBlock());
		deleteLectureBlocksCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), blocks);
		listenTo(deleteLectureBlocksCtrl);
		
		String title = translate("delete.lectures.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteLectureBlocksCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doExportLog(UserRequest ureq, LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(row);
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		LectureBlockAuditLogExport export = new LectureBlockAuditLogExport(entry, lectureBlock, auditLog, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doReopen(LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		String before = lectureService.toAuditXml(lectureBlock);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.reopen);
		if(lectureBlock.getStatus() == LectureBlockStatus.cancelled) {
			lectureBlock.setStatus(LectureBlockStatus.active);
		}
		
		lectureBlock = lectureService.save(lectureBlock, null);
		
		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.reopenLectureBlock, before, after, null,
				lectureBlock, null, lectureBlock.getEntry(), null, null, getIdentity());
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_REOPENED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
		
		loadModel();
	}
	
	private void doOpenLectureBlockDetails(UserRequest ureq, LectureBlockRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		LectureListDetailsController detailsCtrl = new LectureListDetailsController(ureq, getWindowControl(), row, mainForm);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	private void doCloseLectureBlockDetails(LectureBlockRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private void doOpenTools(UserRequest ureq, LectureBlockRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private class ToolsController extends BasicController {
		
		private Link deleteLink;
		private Link editLink;
		private Link copyLink;
		private Link logLink;
		private Link reopenLink;
		
		private final LectureBlockRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("lectures_tools");
			
			LectureBlock lectureBlock = row.getLectureBlock();
			
			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");

			if(secCallback.canReopenLectureBlock() && (lectureBlock.getStatus() == LectureBlockStatus.cancelled
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.closed
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.autoclosed)) {
				reopenLink = LinkFactory.createLink("reopen.lecture.blocks", "reopen", getTranslator(), mainVC, this, Link.LINK);
				reopenLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
			}
			
			if(secCallback.canNewLectureBlock()) {
				copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
				copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			}
			
			logLink = LinkFactory.createLink("log", "log", getTranslator(), mainVC, this, Link.LINK);
			logLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
				
			if(secCallback.canNewLectureBlock() && !LectureBlockManagedFlag.isManaged(row.getLectureBlock(), LectureBlockManagedFlag.delete)) {
				deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(copyLink == source) {
				doCopy(row);
			} else if(editLink == source) {
				doEditLectureBlock(ureq, row);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, row);
			} else if(logLink == source) {
				doExportLog(ureq, row);
			} else if(reopenLink == source) {
				doReopen(row);
			}
		}
	}
}