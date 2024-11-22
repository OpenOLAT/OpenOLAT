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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.IdentityRefImpl;
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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
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
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
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
import org.olat.modules.lecture.ui.addwizard.AddLectureBlock1ResourcesStep;
import org.olat.modules.lecture.ui.addwizard.AddLectureBlockStepCallback;
import org.olat.modules.lecture.ui.addwizard.AddLectureContext;
import org.olat.modules.lecture.ui.blockimport.BlocksImport_1_InputStep;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlock;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlocks;
import org.olat.modules.lecture.ui.component.IconDecoratorCellRenderer;
import org.olat.modules.lecture.ui.component.IdentityCoachesCellRenderer;
import org.olat.modules.lecture.ui.component.IdentityComparator;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.lecture.ui.event.EditLectureBlockRowEvent;
import org.olat.modules.lecture.ui.export.LectureBlockAuditLogExport;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositoryController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String ALL_TAB_ID = "All";
	private static final String PAST_TAB_ID = "Past";
	private static final String RELEVANT_TAB_ID = "Current";
	private static final String TODAY_TAB_ID = "Today";
	private static final String UPCOMING_TAB_ID = "Upcoming";
	private static final String CLOSED_TAB_ID = "Closed";
	private static final String PENDING_TAB_ID = "Pending";
	private static final String WITHOUT_TEACHERS_TAB_ID = "WithoutTeachers";
	
	private static final String FILTER_ROLL_CALL_STATUS = "Status";
	private static final String FILTER_CURRICULUM = "Curriculum";
	private static final String FILTER_TEACHERS = "Teachers";
	
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";

	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private FormLink addLectureButton;
	private FormLink deleteLecturesButton;
	private FormLink importLecturesButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	private final VelocityContainer detailsVC;

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab pastTab;
	private FlexiFiltersTab closedTab;
	private FlexiFiltersTab todayTab;
	private FlexiFiltersTab upcomingTab;
	private FlexiFiltersTab relevantTab;
	private FlexiFiltersTab pendingTab;
	private FlexiFiltersTab withoutTeachersTab;
	private Map<String,FlexiFiltersTab> tabsMap = Map.of();
	
	private FlexiTableMultiSelectionFilter teachersFilter;
	private FlexiTableMultiSelectionFilter curriculumFilter;
	private FlexiTableMultiSelectionFilter rollCallStatusFilter;
	private final SelectionValues teachersValues = new SelectionValues();
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private StepsMainRunController addLectureCtrl;
	private StepsMainRunController importBlockWizard;
	private EditLectureBlockController editLectureCtrl;
	private IdentitySmallListController teacherSmallListCtrl; 
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmDeleteLectureBlockController deleteLectureBlocksCtrl;

	private final RepositoryEntry entry;
	private final Curriculum curriculum;
	private final boolean lectureManagementManaged;
	private final CurriculumElement curriculumElement;
	private final LecturesSecurityCallback secCallback;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	private final String avatarMapperBaseURL;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		entry = null;
		curriculum = null;
		curriculumElement = null;
		this.secCallback = secCallback;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel(ureq);
		updateTeachersFilters();
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = entry;
		curriculum = null;
		curriculumElement = null;
		this.secCallback = secCallback;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel(ureq);
		updateTeachersFilters();
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = null;
		curriculum = curriculumElement == null ? null : curriculumElement.getCurriculum();
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel(ureq);
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, Curriculum curriculum, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		entry = null;
		this.curriculum = curriculum;
		curriculumElement = null;
		this.secCallback = secCallback;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private  void initButtonsForm(FormItemContainer formLayout) {
		if(curriculumElement != null) {
			allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
			allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_structure");
			allLevelsButton.setPrimary(true);
			thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
			thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
		}
	
		if(!lectureManagementManaged && secCallback.canNewLectureBlock()) {
			if(entry != null || curriculum != null || curriculumElement != null) {
				addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);
				addLectureButton.setIconLeftCSS("o_icon o_icon_add");
				addLectureButton.setElementCssClass("o_sel_repo_add_lecture");
			}
			
			deleteLecturesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			
			if(entry != null || curriculumElement != null) {
				DropdownItem addTaskDropdown = uifactory.addDropdownMenu("import.lectures.dropdown", null, null, formLayout, getTranslator());
				addTaskDropdown.setOrientation(DropdownOrientation.right);
				addTaskDropdown.setElementCssClass("o_sel_add_more");
				addTaskDropdown.setEmbbeded(true);
				addTaskDropdown.setButton(true);
				
				importLecturesButton = uifactory.addFormLink("import.lectures", formLayout, Link.LINK);
				importLecturesButton.setIconLeftCSS("o_icon o_icon_import");
				importLecturesButton.setElementCssClass("o_sel_repo_import_lectures");
				addTaskDropdown.addElement(importLecturesButton);
			}
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			boolean headless = (entry == null && curriculum == null && curriculumElement == null);
			String titleSize = headless ? "h2" : "h3";
			layoutCont.contextPut("titleSize", titleSize);
		}
	}

	private  void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
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
				new IdentityCoachesCellRenderer(userManager)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.numParticipants));
		
		DefaultFlexiColumnModel compulsoryColumn = new DefaultFlexiColumnModel(false, BlockCols.compulsory,
				new YesNoCellRenderer());
		compulsoryColumn.setIconHeader("o_icon o_icon_compulsory o_icon-lg");
		columnsModel.addFlexiColumnModel(compulsoryColumn);

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("edit", -1);
		editColumn.setCellRenderer(new StaticFlexiCellRenderer(null, "edit", null, "o_icon o_icon-lg o_icon_edit", translate("edit")));
		editColumn.setIconHeader("o_icon o_icon-lg o_icon_edit");
		editColumn.setExportable(false);
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);
		
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(BlockCols.tools));
		
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
		
		if(entry == null && curriculumElement == null && curriculum == null) {
			CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
			searchParams.setCurriculumAdmin(getIdentity());
			List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
			
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum curriculum:curriculums) {
				curriculumValues.add(SelectionValues.entry(curriculum.getKey().toString(), curriculum.getDisplayName()));
			}
			
			curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
			
		}
		
		SelectionValues rollCallStatusValues = new SelectionValues();
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.open.name(), translate("search.form.status.open")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.closed.name(), translate("search.form.status.closed")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.autoclosed.name(), translate("search.form.status.autoclosed")));
		rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.reopen.name(), translate("search.form.status.reopen")));
		rollCallStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_ROLL_CALL_STATUS, rollCallStatusValues, true);
		filters.add(rollCallStatusFilter);
		
		teachersFilter = new FlexiTableMultiSelectionFilter(translate("filter.teachers"),
				FILTER_TEACHERS, teachersValues, true);
		filters.add(teachersFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		Map<String,FlexiFiltersTab> map = new HashMap<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_elements_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		map.put(ALL_TAB_ID.toLowerCase(), allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of());
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		map.put(RELEVANT_TAB_ID.toLowerCase(), relevantTab);
		
		todayTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TODAY_TAB_ID, translate("filter.today"),
				TabSelectionBehavior.nothing, List.of());
		todayTab.setFiltersExpanded(true);
		tabs.add(todayTab);
		map.put(TODAY_TAB_ID.toLowerCase(), todayTab);
		
		upcomingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(UPCOMING_TAB_ID, translate("filter.next.days"),
				TabSelectionBehavior.nothing, List.of());
		upcomingTab.setFiltersExpanded(true);
		tabs.add(upcomingTab);
		map.put(UPCOMING_TAB_ID.toLowerCase(), upcomingTab);
		
		pastTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PAST_TAB_ID, translate("filter.past"),
				TabSelectionBehavior.nothing, List.of());
		pastTab.setFiltersExpanded(true);
		tabs.add(pastTab);
		map.put(PAST_TAB_ID.toLowerCase(), pastTab);
		
		withoutTeachersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHOUT_TEACHERS_TAB_ID, translate("filter.without.teachers"),
				TabSelectionBehavior.nothing, List.of());
		withoutTeachersTab.setFiltersExpanded(true);
		tabs.add(withoutTeachersTab);
		map.put(WITHOUT_TEACHERS_TAB_ID.toLowerCase(), withoutTeachersTab);
		
		pendingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PENDING_TAB_ID, translate("filter.pending"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
						List.of(LectureRollCallStatus.open.name(), LectureRollCallStatus.reopen.name()))));
		pendingTab.setFiltersExpanded(true);
		tabs.add(pendingTab);
		map.put(PENDING_TAB_ID.toLowerCase(), pendingTab);
		
		closedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CLOSED_TAB_ID, translate("filter.closed"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
						List.of(LectureRollCallStatus.closed.name(), LectureRollCallStatus.autoclosed.name()))));
		closedTab.setFiltersExpanded(true);
		tabs.add(closedTab);
		map.put(CLOSED_TAB_ID.toLowerCase(), closedTab);
		
		tableEl.setFilterTabs(true, tabs);
		tabsMap = Map.copyOf(map);
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

	private void loadModel(UserRequest ureq) {
		String displayname = null;
		String externalRef = null;
		if(curriculumElement != null) {
			displayname = curriculumElement.getDisplayName();
			externalRef = curriculumElement.getExternalId();
		} else if(curriculum != null) {
			displayname = curriculum.getDisplayName();
			externalRef = curriculum.getExternalId();
		} else if(entry != null) {
			displayname = entry.getDisplayname();
			externalRef = entry.getExternalRef();
		}
		
		IdentityComparator identityComparator = new IdentityComparator(getLocale());
		LecturesBlockSearchParameters searchParams = getSearchParams(ureq);
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
		
		List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlockWithTeachers block:blocks) {
			LectureBlock b = block.getLectureBlock();
			StringBuilder teachers = new StringBuilder();
			String separator = translate("user.fullname.separator");
			List<Identity> teachersList = new ArrayList<>(block.getTeachers());
			if(teachersList.size() > 1) {
				Collections.sort(teachersList, identityComparator);
			}
			
			for(Identity teacher:teachersList) {
				if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
				teachers.append(userManager.getUserDisplayName(teacher));
			}

			LectureBlockRow row = new LectureBlockRow(b, displayname, externalRef,
					teachers.toString(), false, block.getCurriculumElementRef(), block.getEntryRef(),
					block.getNumOfParticipants(), block.isAssessmentMode());
			row.setTeachersList(teachersList);
			rows.add(row);
			
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);

		if(deleteLecturesButton != null) {
			deleteLecturesButton.setVisible(!rows.isEmpty());
		}
	}
	
	private void updateTeachersFilters() {
		List<LectureBlockRow> rows = tableModel.getObjects();
		Set<Identity> teachers = new HashSet<>();
		for(LectureBlockRow row:rows) {
			teachers.addAll(row.getTeachersList());
		}
		List<Identity> teachersList = new ArrayList<>(teachers);
		for(Identity teacher: teachersList) {
			String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(teacher));
			teachersValues.add(SelectionValues.entry(teacher.getKey().toString(), fullName));
		}
	}
	
	private LecturesBlockSearchParameters getSearchParams(UserRequest ureq) {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		
		if(curriculumElement != null) {
			boolean oneLevelOnly = thisLevelButton.getComponent().isPrimary();
			searchParams.setCurriculumElementPath(oneLevelOnly ? null : curriculumElement.getMaterializedPathKeys());
			searchParams.setCurriculumElement(oneLevelOnly ? curriculumElement : null);
			searchParams.setLectureConfiguredRepositoryEntry(false);
		} else if(curriculum != null) {
			searchParams.setCurriculums(List.of(curriculum));
			searchParams.setLectureConfiguredRepositoryEntry(false);
		} else if(entry != null) {
			searchParams.setRepositoryEntry(entry);
			searchParams.setLectureConfiguredRepositoryEntry(true);
		} else {
			searchParams.setInSomeCurriculum(true);
			searchParams.setLectureConfiguredRepositoryEntry(false);
		}
		
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_ROLL_CALL_STATUS);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<LectureRollCallStatus> status = filterValues.stream()
						.map(LectureRollCallStatus::valueOf)
						.toList();
				searchParams.setRollCallStatus(status);
			}
		}
		
		FlexiTableFilter cFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_CURRICULUM);
		if (cFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<CurriculumRef> keys = filterValues.stream()
						.filter(StringHelper::isLong)
						.map(Long::valueOf)
						.map(CurriculumRefImpl::new)
						.map(CurriculumRef.class::cast)
						.toList();
				searchParams.setCurriculums(keys);
				searchParams.setInSomeCurriculum(false);
				searchParams.setLectureConfiguredRepositoryEntry(false);
			}
		}
		
		if(FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_TEACHERS) != null) {
			List<String> filterValues = teachersFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<IdentityRefImpl> teachersKeys = filterValues.stream()
						.filter(StringHelper::isLong)
						.map(key -> new IdentityRefImpl(Long.valueOf(key)))
						.toList();
				searchParams.setTeachersList(teachersKeys);
			}
		}
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		Date now = ureq.getRequestTimestamp();
		if(selectedTab == relevantTab) {
			searchParams.setStartDate(now);
		} else if(selectedTab == todayTab) {
			searchParams.setStartDate(DateUtils.getStartOfDay(now));
			searchParams.setEndDate(DateUtils.getEndOfDay(now));
		} else if(selectedTab == upcomingTab) {
			searchParams.setStartDate(DateUtils.getEndOfDay(now));
		} else if(selectedTab == pastTab || selectedTab == pendingTab) {
			searchParams.setEndDate(now);
		} else if(selectedTab == withoutTeachersTab) {
			searchParams.setWithTeachers(Boolean.FALSE);
		}
		
		String quickSearch = tableEl.getQuickSearchString();
		if(StringHelper.containsNonWhitespace(quickSearch)) {
			searchParams.setSearchString(quickSearch);
		}
		
		return searchParams;
	}
	
	private void reloadModel(UserRequest ureq, LectureBlock lectureBlock) {
		LectureBlockRow row = tableModel.getObject(lectureBlock);
		if(row == null) {
			loadModel(ureq);
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName().toLowerCase();
		if("lecture".equals(type) || "lectureblock".equals(type)) {
			activateLecture(ureq, entries.get(0).getOLATResourceable().getResourceableId());
		} else if(tabsMap.containsKey(type)) {
			tableEl.setSelectedFilterTab(ureq, tabsMap.get(type));
			loadModel(ureq);
			if(entries.size() > 1) {
				String subType = entries.get(1).getOLATResourceable().getResourceableTypeName().toLowerCase();
				if("lecture".equals(subType) || "lectureblock".equals(subType)) {
					activateLecture(ureq, entries.get(1).getOLATResourceable().getResourceableId());
				}
			}
		}
	}
	
	private void activateLecture(UserRequest ureq, Long resourceId) {
		int index = tableModel.getIndexByKey(resourceId);
		if(index >= 0) {
			int page = index / tableEl.getPageSize();
			tableEl.setPage(page);
			doOpenLectureBlockDetails(ureq, tableModel.getObject(index));
			tableEl.expandDetails(index);
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
		} else if(allLevelsButton == source) {
			doToggleLevels(ureq, false);
		} else if(thisLevelButton == source) {
			doToggleLevels(ureq, true);
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
						doOpenLectureBlockDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				} else if(IdentityCoachesCellRenderer.CMD_OTHER_TEACHERS.equals(cmd)) {
					String targetId = IdentityCoachesCellRenderer.getOtherTeachersId(se.getIndex());
					doShowTeachers(ureq, targetId, row);
				}
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel(ureq);
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
			if(cmd != null && cmd.equals("tools")) {
				LectureBlockRow row = (LectureBlockRow)link.getUserObject();
				doOpenTools(ureq, row, link);
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
		} else if(addLectureCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel(ureq);
				}
				cleanUp();
			}
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
				loadModel(ureq);
			}
		} else if(deleteLectureBlocksCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(ureq);
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
		removeAsListenerAndDispose(addLectureCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteLectureBlocksCtrl = null;
		toolsCalloutCtrl = null;
		editLectureCtrl = null;
		addLectureCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doToggleLevels(UserRequest ureq, boolean thisLevel) {
		allLevelsButton.setPrimary(!thisLevel);
		thisLevelButton.setPrimary(thisLevel);
		loadModel(ureq);
	}

	private void doEditLectureBlock(UserRequest ureq, LectureBlockRow row) {
		if(guardModalController(editLectureCtrl)) return;
		
		LectureBlock block = lectureService.getLectureBlock(row);
		boolean readOnly = lectureManagementManaged || !secCallback.canNewLectureBlock();
		if(entry != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry, block, readOnly);
		} else if(curriculumElement != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), curriculumElement, block, readOnly);
		} else if(block.getEntry() != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), block.getEntry(), block, readOnly);
		} else if(block.getCurriculumElement() != null) {
			editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), block.getCurriculumElement(), block, readOnly);
		} else {
			showWarning("error.no.entry.curriculum");
			return;
		}
		listenTo(editLectureCtrl);

		String title = translate("edit.lecture");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editLectureCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLectureBlock(UserRequest ureq) {
		if(guardModalController(addLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		if(entry == null && curriculumElement == null && curriculum == null) {
			showWarning("error.no.entry.curriculum");
			return;
		}
		
		List<RepositoryEntry> entries = entry == null ? List.of() : List.of(entry);
		AddLectureContext addLecture = new AddLectureContext(curriculum, curriculumElement, entries);
		addLecture.setEntry(entry);
		addLecture.setCurriculumElement(curriculumElement);
		
		AddLectureBlock1ResourcesStep step = new AddLectureBlock1ResourcesStep(ureq, addLecture);
		AddLectureBlockStepCallback stop = new AddLectureBlockStepCallback(addLecture);
		String title = translate("add.lecture");
		
		removeAsListenerAndDispose(addLectureCtrl);
		addLectureCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, null, title, "");
		listenTo(addLectureCtrl);
		getWindowControl().pushAsModalDialog(addLectureCtrl.getInitialComponent());
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
	
	private void doCopy(UserRequest ureq, LectureBlockRow row) {
		String newTitle = translate("lecture.block.copy",row.getLectureBlock().getTitle());
		lectureService.copyLectureBlock(newTitle, row.getLectureBlock());
		loadModel(ureq);
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
	
	private void doReopen(UserRequest ureq, LectureBlockRow row) {
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
		
		loadModel(ureq);
	}
	
	private void doShowTeachers(UserRequest ureq, String elementId, LectureBlockRow row) {
		List<Identity> teachers = row.getTeachersList();
		teacherSmallListCtrl = new IdentitySmallListController(ureq, getWindowControl(), teachers);
		listenTo(teacherSmallListCtrl);
		
		String title = translate("num.of.teachers", Integer.toString(teachers.size()));
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), teacherSmallListCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenLectureBlockDetails(UserRequest ureq, LectureBlockRow row) {
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		LectureListDetailsController detailsCtrl = new LectureListDetailsController(ureq, getWindowControl(), row,
				avatarMapper, avatarMapperBaseURL, mainForm);
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
				doCopy(ureq, row);
			} else if(editLink == source) {
				doEditLectureBlock(ureq, row);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, row);
			} else if(logLink == source) {
				doExportLog(ureq, row);
			} else if(reopenLink == source) {
				doReopen(ureq, row);
			}
		}
	}
}