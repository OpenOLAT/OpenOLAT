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

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
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
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.DateScopeDropdown.DateScopeOption;
import org.olat.core.gui.components.scope.FormDateScopeSelection;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.ui.mode.AssessmentModeForLectureEditController;
import org.olat.course.assessment.ui.mode.TimeCellRenderer;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.LectureListRepositoryConfig.Visibility;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
import org.olat.modules.lecture.ui.addwizard.AddLectureBlock1ResourcesStep;
import org.olat.modules.lecture.ui.addwizard.AddLectureBlockStepCallback;
import org.olat.modules.lecture.ui.addwizard.AddLectureContext;
import org.olat.modules.lecture.ui.addwizard.AssignNewRepositoryEntryController;
import org.olat.modules.lecture.ui.blockimport.BlocksImport_1_InputStep;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlock;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlocks;
import org.olat.modules.lecture.ui.component.IdentityCoachesCellRenderer;
import org.olat.modules.lecture.ui.component.IdentityComparator;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallBasicStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;
import org.olat.modules.lecture.ui.component.LocationCellRenderer;
import org.olat.modules.lecture.ui.component.OpenOnlineMeetingEvent;
import org.olat.modules.lecture.ui.component.ReferenceRenderer;
import org.olat.modules.lecture.ui.event.EditLectureBlockRowEvent;
import org.olat.modules.lecture.ui.export.LectureBlockAuditLogExport;
import org.olat.modules.lecture.ui.export.LectureBlockExport;
import org.olat.modules.lecture.ui.export.LecturesBlockPDFExport;
import org.olat.modules.lecture.ui.export.LecturesBlockSignaturePDFExport;
import org.olat.modules.lecture.ui.teacher.ManageTeachersController;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Show the list of lecture blocks / events for a given context. The data are only
 * loaded by the activate method and not on creation of the controller.
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureListRepositoryController extends FormBasicController implements FlexiTableComponentDelegate, FlexiTableCssDelegate, Activateable2 {

	private static final String ALL_TAB_ID = "All";
	private static final String PAST_TAB_ID = "Past";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String TODAY_TAB_ID = "Today";
	private static final String UPCOMING_TAB_ID = "Upcoming";
	private static final String CLOSED_TAB_ID = "Closed";
	private static final String PENDING_TAB_ID = "Pending";
	private static final String WITHOUT_TEACHERS_TAB_ID = "WithoutTeachers";

	private static final String FILTER_TEACHERS = "Teachers";
	private static final String FILTER_CURRICULUM = "Curriculum";
	private static final String FILTER_ROLL_CALL_STATUS = "Status";
	private static final String FILTER_VIRTUAL_STATUS = "VirtualStatus";
	
	private static final String NO_TEACHER = "noteacher";
	
	private static final String CMD_ROLLCALL = "lrollcall";
	protected static final String CMD_REPOSITORY_ENTRY = "lentry";
	protected static final String CMD_CURRICULUM_ELEMENT = "element";
	protected static final String CMD_OPEN_ONLINE_MEETING = "lopenonlinemeeting";
	protected static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private String switchPrefsId = "Events-v1";

	private FormLink allLevelsButton;
	private FormLink thisLevelButton;
	private FormLink addLectureButton;
	private FormLink copyLecturesButton;
	private FormLink deleteLecturesButton;
	private FormLink importLecturesButton;
	private FormLink allTeachersButton;
	private FormLink onlyMineButton;
	private FormLink pendingRollCallLink;
	private FormLink startButton;
	private FormLink startWizardButton;
	private FormLink manageTeachersButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	private final VelocityContainer detailsVC;
	private FormDateScopeSelection scopeEl;
	private final BreadcrumbedStackedPanel stackPanel;

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
	private FlexiTableMultiSelectionFilter virtualStatusFilter;
	private final SelectionValues teachersValues = new SelectionValues();
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private TeacherRollCallController rollCallCtrl;
	private StepsMainRunController importBlockWizard;
	private EditLectureBlockController addLectureCtrl;
	private EditLectureBlockController editLectureCtrl;
	private StepsMainRunController addLectureWizardCtrl;
	private ManageTeachersController manageTeachersCtrl;
	private EditTeamsMeetingController editTeamsMeetingCtrl;
	private IdentitySmallListController teacherSmallListCtrl; 
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private DialogBoxController deleteAssessmentModeDialogBox;
	private TeacherRollCallWizardController rollCallWizardCtrl;
	private LectureBlockOnlineMeetingController onlineMeetingCtrl;
	private AssignNewRepositoryEntryController assignNewEntryCtrl;
	private ConfirmationController confirmChangeToLocationMeetingCtrl;
	private ConfirmDeleteLectureBlockController deleteLectureBlocksCtrl;
	private AssessmentModeForLectureEditController assessmentModeEditCtrl;
	private EditBigBlueButtonMeetingController editBigBlueButtonMeetingCtrl;

	private final RepositoryEntry entry;
	private final Curriculum curriculum;
	private final boolean lectureManagementManaged;
	private final boolean authorizedAbsenceEnabled;
	private final CurriculumElement curriculumElement;
	private final LecturesSecurityCallback secCallback;
	private final LectureListRepositoryConfig config;
	private final IdentityComparator identityComparator;

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private LifecycleModule lifecycleModule;
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			LectureListRepositoryConfig config, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.stackPanel = stackPanel;
		entry = null;
		curriculum = null;
		curriculumElement = null;
		this.config = config;
		this.secCallback = secCallback;
		identityComparator = new IdentityComparator(getLocale());
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		initForm(ureq);
		updateUI();
		loadWarning(ureq);
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			RepositoryEntry entry, LectureListRepositoryConfig config, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.stackPanel = stackPanel;
		this.entry = entry;
		curriculum = null;
		curriculumElement = null;
		this.config = config;
		this.secCallback = secCallback;
		identityComparator = new IdentityComparator(getLocale());
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		initForm(ureq);
		updateUI();
		loadWarning(ureq);
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			CurriculumElement curriculumElement, LectureListRepositoryConfig config, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.stackPanel = stackPanel;
		this.entry = null;
		curriculum = curriculumElement == null ? null : curriculumElement.getCurriculum();
		this.curriculumElement = curriculumElement;
		this.config = config;
		this.secCallback = secCallback;
		identityComparator = new IdentityComparator(getLocale());
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		initForm(ureq);
		updateUI();
		loadWarning(ureq);
	}
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			Curriculum curriculum, LectureListRepositoryConfig config, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.stackPanel = stackPanel;
		entry = null;
		this.curriculum = curriculum;
		curriculumElement = null;
		this.config = config;
		this.secCallback = secCallback;
		identityComparator = new IdentityComparator(getLocale());
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		detailsVC = createVelocityContainer("lecture_details");
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		initForm(ureq);
		updateUI();
		loadWarning(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(ureq, formLayout);
		initScopes(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private  void initButtonsForm(UserRequest ureq, FormItemContainer formLayout) {
		allLevelsButton = uifactory.addFormLink("search.all.levels", formLayout, Link.BUTTON);
		allLevelsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_structure");
		allLevelsButton.setPrimary(true);
		thisLevelButton = uifactory.addFormLink("search.this.level", formLayout, Link.BUTTON);
		thisLevelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
		
		pendingRollCallLink = uifactory.addFormLink("pending.rollcall", formLayout, Link.LINK);

		startButton = uifactory.addFormLink("start.desktop", formLayout, Link.BUTTON);
		startButton.setVisible(false);
		startWizardButton = uifactory.addFormLink("start.mobile", formLayout, Link.BUTTON);
		startWizardButton.setVisible(false);
		
		if(config.withAllMineSwitch()) {
			boolean all = isAllTeachersSwitch(ureq, config.showMineAsDefault());
			allTeachersButton = uifactory.addFormLink("all.teachers.switch", formLayout, Link.BUTTON);
			allTeachersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_coach");
			allTeachersButton.setPrimary(all);
			onlyMineButton = uifactory.addFormLink("all.teachers.switch.off", formLayout, Link.BUTTON);
			onlyMineButton.setIconLeftCSS("o_icon o_icon-fw o_icon_exact_location");
			onlyMineButton.setPrimary(!all);
		}
		
		if(!lectureManagementManaged && secCallback.canNewLectureBlock()) {
			if(entry != null || curriculum != null || curriculumElement != null) {
				addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);
				addLectureButton.setIconLeftCSS("o_icon o_icon_add");
				addLectureButton.setElementCssClass("o_sel_repo_add_lecture");
			}
			copyLecturesButton = uifactory.addFormLink("copy", formLayout, Link.BUTTON);
			
			deleteLecturesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			
			// Don't manage all
			if(entry != null || curriculumElement != null || curriculum != null) {
				manageTeachersButton = uifactory.addFormLink("manage.teachers", formLayout, Link.BUTTON);
			}
			
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
			String titleSize = config.getTitleSize() <= 0 ? "" : "h" + config.getTitleSize();
			layoutCont.contextPut("titleSize", titleSize);
		}
	}

	private  void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date,
				new DateWithDayFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime,
				new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.followUptime, new TimeCellRenderer(getTranslator())));
		 
		if(config.withNumberOfLectures() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withNumberOfLectures() == Visibility.HIDE, BlockCols.lecturesNumber));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title, TOGGLE_DETAILS_CMD));
		if(config.withExternalRef() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withExternalRef() == Visibility.SHOW,
					BlockCols.externalRef));
		}
		
		if(config.withCurriculum() != Visibility.NO) {
			String elementCmd = entry == null ? CMD_CURRICULUM_ELEMENT : null;
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withCurriculum() == Visibility.SHOW, BlockCols.curriculumElement, elementCmd,
					new ReferenceRenderer()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.status,
				new LectureBlockStatusCellRenderer(getTranslator())));
		
		if(entry != null && config.withExam() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withExam() == Visibility.SHOW, BlockCols.assessmentMode,
				new BooleanCellRenderer(new CSSIconFlexiCellRenderer("o_icon_assessment_mode"), null)));
		}
		if(config.withRepositoryEntry() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withRepositoryEntry() == Visibility.SHOW, BlockCols.entry,
					CMD_REPOSITORY_ENTRY, new ReferenceRenderer()));
		}

		if(config.withLocation() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withLocation() == Visibility.SHOW, BlockCols.location,
					new LocationCellRenderer(getTranslator())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teachers,
				new IdentityCoachesCellRenderer(userManager)));
		if(config.withNumberOfParticipants() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withNumberOfParticipants() == Visibility.SHOW, BlockCols.numParticipants));
		}

		if(config.withRollCall() != Visibility.NO) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withRollCall() == Visibility.SHOW, BlockCols.rollCallStatus,
					new LectureBlockRollCallBasicStatusCellRenderer(getTranslator())));
			if(secCallback.viewAs() != LectureRoles.participant) {
				DefaultFlexiColumnModel detailsCol = new DefaultFlexiColumnModel(BlockCols.rollCall);
				detailsCol.setIconHeader("o_icon o_icon-lg o_icon_lecture");
				columnsModel.addFlexiColumnModel(detailsCol);
			}
		}
		
		if(config.withCompulsoryPresence() != Visibility.NO) {
			DefaultFlexiColumnModel compulsoryColumn = new DefaultFlexiColumnModel(config.withCompulsoryPresence() == Visibility.SHOW, BlockCols.compulsory,
					new YesNoCellRenderer());
			compulsoryColumn.setIconHeader("o_icon o_icon_compulsory o_icon-lg");
			columnsModel.addFlexiColumnModel(compulsoryColumn);
		}
		
		if(config.withOnlineMeeting() != Visibility.NO && isOnlineMeetingEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.withOnlineMeeting() == Visibility.SHOW, BlockCols.onlineMeeting));
		}

		if(!lectureManagementManaged && secCallback.canNewLectureBlock() && config.withCompulsoryPresence() != Visibility.NO) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("edit", -1);
			editColumn.setCellRenderer(new StaticFlexiCellRenderer(null, "edit", null, "o_icon o_icon-lg o_icon_edit", translate("edit")));
			editColumn.setIconHeader("o_icon o_icon-lg o_icon_edit");
			editColumn.setExportable(false);
			editColumn.setAlwaysVisible(config.withCompulsoryPresence() == Visibility.SHOW);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		
		if(secCallback.viewAs() != LectureRoles.participant) {
			ActionsColumnModel actionsCol = new ActionsColumnModel(BlockCols.tools);
			actionsCol.setIconHeader("o_icon o_icon-lg o_icon_actions");
			columnsModel.addFlexiColumnModel(actionsCol);
		}
		
		tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.verticalTimeLine, FlexiTableRendererType.classic);
		if(secCallback.viewAs() == LectureRoles.participant) {
			tableEl.setRendererType(FlexiTableRendererType.verticalTimeLine);
		} else {
			tableEl.setRendererType(FlexiTableRendererType.classic);
		}
		
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmptyTableMessageKey("empty.table.lectures.blocks.admin");
		tableEl.setSearchEnabled(true);
		tableEl.setCssDelegate(this);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/row_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(BlockCols.date.name(), false));
		options.setFromColumnModel(true);
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, config.getPrefsId());
		tableEl.addBatchButton(manageTeachersButton);
		tableEl.addBatchButton(copyLecturesButton);
		tableEl.addBatchButton(deleteLecturesButton);
		
		initFilters();
		initFiltersPresets();
	}
	
	private void initScopes(FormItemContainer formLayout) {
		DateScopeOption preselectedOption = null;
		List<DateScopeOption> cyclesScopes = new ArrayList<>();

		if (lifecycleModule.isEnabled()) {
			List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
			for (RepositoryEntryLifecycle cycle : cycles) {
				String label = cycle.getLabel();
				if (StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
					label = cycle.getSoftKey();
				}
				DateScope scope = ScopeFactory.createDateScope("cycle_" + cycle.getKey(), label, null, cycle.getDateRange());
				DateScopeOption option = new DateScopeOption(getSelectionName(cycle), scope);
				cyclesScopes.add(option);

				if (preselectedOption == null || cycle.isDefaultPublicCycle()) {
					preselectedOption = option;
				}
			}
		}
		
		String dropdownLabel = translate("cif.dates.public");
		List<DateScope> scopes = ScopeFactory.dateScopesBuilder(getLocale())
				.todayAndUpcoming()
				.dropdown(dropdownLabel, cyclesScopes, preselectedOption)
				.lastMonths(3)
				.build();
		scopeEl = uifactory.addDateScopeSelection(getWindowControl(), "scope", null, formLayout, scopes, getLocale());
		scopeEl.setVisible(config.withScopes());
	}
	
	private String getSelectionName(RepositoryEntryLifecycle cycle) {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(cycle.getLabel())) {
			sb.append(cycle.getLabel());
		}
		
		if(StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
			if(sb.length() > 0) {
				sb.append(" \u00B7 ");
			}
			sb.append(cycle.getSoftKey());
		}
		return sb.toString();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(entry == null && curriculumElement == null && curriculum == null) {
			CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
			searchParams.setCurriculumAdmin(getIdentity());
			List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
			
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum cur:curriculums) {
				curriculumValues.add(SelectionValues.entry(cur.getKey().toString(), cur.getDisplayName()));
			}
			
			curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
		}
		
		SelectionValues virtualStatusValues = new SelectionValues();
		virtualStatusValues.add(SelectionValues.entry(LectureBlockVirtualStatus.PLANNED.name(), translate("planned")));
		virtualStatusValues.add(SelectionValues.entry(LectureBlockVirtualStatus.RUNNING.name(), translate("running")));
		virtualStatusValues.add(SelectionValues.entry(LectureBlockVirtualStatus.DONE.name(), translate("done")));
		virtualStatusValues.add(SelectionValues.entry(LectureBlockVirtualStatus.CANCELLED.name(), translate("cancelled")));
		virtualStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_VIRTUAL_STATUS, virtualStatusValues, true);
		filters.add(virtualStatusFilter);
		
		teachersFilter = new FlexiTableMultiSelectionFilter(translate("filter.teachers"),
				FILTER_TEACHERS, teachersValues, true);
		filters.add(teachersFilter);
	
		if(config.withFilterPresetPending() || config.withFilterPresetClosed()) {
			SelectionValues rollCallStatusValues = new SelectionValues();
			rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.open.name(), translate("search.form.status.open")));
			rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.closed.name(), translate("search.form.status.closed")));
			rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.autoclosed.name(), translate("search.form.status.autoclosed")));
			rollCallStatusValues.add(SelectionValues.entry(LectureRollCallStatus.reopen.name(), translate("search.form.status.reopen")));
			rollCallStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.rollcall.status"),
					FILTER_ROLL_CALL_STATUS, rollCallStatusValues, true);
			filters.add(rollCallStatusFilter);
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	private boolean scopeInFuture() {
		boolean withPast = true;
		
		if(scopeEl.isVisible()) {
			Date now = DateUtils.getStartOfDay(new Date());
			DateRange range = scopeEl.getSelectedDateRange();
			if(range != null && range.getFrom() != null && range.getFrom().compareTo(now) >= 0) {
				withPast = false;
			}
		}
		return withPast;
	}
	
	private boolean scopeInPast() {
		boolean withPast = false;
		if(scopeEl.isVisible()) {
			Date now = DateUtils.getStartOfDay(new Date());
			DateRange range = scopeEl.getSelectedDateRange();
			if(range != null && range.getTo() != null && range.getTo().compareTo(now) <= 0) {
				withPast = true;
			}
		}
		return withPast;
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
		
		if(config.withFilterPresetRelevant()) {
			relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
					TabSelectionBehavior.nothing, List.of());
			relevantTab.setFiltersExpanded(true);
			tabs.add(relevantTab);
			map.put(RELEVANT_TAB_ID.toLowerCase(), relevantTab);
		}

		if(!scopeInPast()) {
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
		}
		
		if(scopeInFuture() && !scopeInPast()) {
			pastTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PAST_TAB_ID, translate("filter.past"),
					TabSelectionBehavior.nothing, List.of());
			pastTab.setFiltersExpanded(true);
			tabs.add(pastTab);
			map.put(PAST_TAB_ID.toLowerCase(), pastTab);
		}
		
		if(config.withFilterPresetWithoutTeachers()) {
			withoutTeachersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHOUT_TEACHERS_TAB_ID, translate("filter.without.teachers"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_TEACHERS, NO_TEACHER)));
			withoutTeachersTab.setFiltersExpanded(true);
			tabs.add(withoutTeachersTab);
			map.put(WITHOUT_TEACHERS_TAB_ID.toLowerCase(), withoutTeachersTab);
		}
		
		if(config.withFilterPresetPending()) {
			pendingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PENDING_TAB_ID, translate("filter.pending"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
							List.of(LectureRollCallStatus.open.name(), LectureRollCallStatus.reopen.name()))));
			pendingTab.setFiltersExpanded(true);
			tabs.add(pendingTab);
			map.put(PENDING_TAB_ID.toLowerCase(), pendingTab);
		}

		if(config.withFilterPresetClosed()) {
			closedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CLOSED_TAB_ID, translate("filter.closed"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ROLL_CALL_STATUS,
							List.of(LectureRollCallStatus.closed.name(), LectureRollCallStatus.autoclosed.name()))));
			closedTab.setFiltersExpanded(true);
			tabs.add(closedTab);
			map.put(CLOSED_TAB_ID.toLowerCase(), closedTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
		tabsMap = Map.copyOf(map);
	}
	
	protected void updateUI() {
		boolean canSubelements = curriculumElement != null
				&& (curriculumElement.getType() == null || !curriculumElement.getType().isSingleElement());
		allLevelsButton.setVisible(canSubelements);
		thisLevelButton.setVisible(canSubelements);
	}
	
	private boolean isOnlineMeetingEnabled() {
		return (teamsModule.isEnabled() && teamsModule.isLecturesEnabled())
				|| (bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isLecturesEnabled());
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof LectureBlockRow lectureRow) {
			if(lectureRow.getDetailsController() != null) {
				components.add(lectureRow.getDetailsController().getInitialFormItem().getComponent());
			}
			if(lectureRow.getOpenOnlineMeetingButton() != null) {
				components.add(lectureRow.getOpenOnlineMeetingButton().getComponent());
			}
			if(lectureRow.getRollCallLink() != null) {
				components.add(lectureRow.getRollCallLink().getComponent());
			}
			if(lectureRow.getToolsLink() != null) {
				components.add(lectureRow.getToolsLink().getComponent());
			}
		}
		return components;
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		if(type == FlexiTableRendererType.verticalTimeLine) {
			LectureBlockRow row = tableModel.getObject(pos);
			
			Date now = new Date();
			if(row.getStartDate() != null && row.getStartDate().compareTo(now) <= 0
					&& row.getEndDate() != null && row.getEndDate().compareTo(now) >= 0) {
				return "o_vertical_timeline_item o_lecture_running";
			} else if(row.isNextScheduled()) {
				return "o_vertical_timeline_item o_lecture_next";
			}
		}
		return null;
	}

	private void selectScope(UserRequest ureq) {
		boolean asc = !scopeInPast();
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(BlockCols.date.name(), asc));
		tableEl.setSortSettings(options);
		
		initFiltersPresets();
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(ureq);
	}

	public void loadModel(UserRequest ureq) {
		loadModel(ureq, false);
	}

	private void loadModel(UserRequest ureq, boolean replaceOnly) {
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
		
		LectureBlockRow canStart = null;
		Date now = ureq.getRequestTimestamp();
		LecturesBlockSearchParameters nextParams = getSearchParams();
		nextParams.setStartDate(now);
		LectureBlockRef nextScheduledBlock = lectureService.getNextScheduledLectureBlock(nextParams);

		LecturesBlockSearchParameters searchParams = getSearchParamsWithFilters(ureq);
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
		
		List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlockWithTeachers block:blocks) {
			LectureBlockRow row = forgeRow(now, block, displayname, externalRef, nextScheduledBlock);
			rows.add(row);
			if(canStartRollCall(row)) {
				canStart = row;
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(!replaceOnly, !replaceOnly, true);

		if(deleteLecturesButton != null) {
			deleteLecturesButton.setVisible(!rows.isEmpty());
		}
		
		boolean startRollCall = canStart != null && config.withRollCall() != Visibility.NO;
		startButton.setVisible(startRollCall);
		startButton.setUserObject(canStart);
		startWizardButton.setVisible(startRollCall);
		startWizardButton.setUserObject(canStart);
		
		updateTeachersFilters();
	}
	
	private boolean canStartRollCall(LectureBlockRow blockWithTeachers) {
		LectureBlock lectureBlock = blockWithTeachers.getLectureBlock();
		if(blockWithTeachers.isIamTeacher()
				&& lectureBlock.getStatus() != LectureBlockStatus.done
				&& lectureBlock.getStatus() != LectureBlockStatus.cancelled) {
			Date start = lectureBlock.getStartDate();
			Date end = lectureBlock.getEndDate();
			Date now = new Date();
			if(start.compareTo(now) <= 0 && end.compareTo(now) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	private void loadWarning(UserRequest ureq) {
		if(secCallback.viewAs() == LectureRoles.teacher && pendingTab != null) {
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setTeacher(getIdentity());
			searchParams.setEndDate(ureq.getRequestTimestamp());
			searchParams.addRollCallStatus(LectureRollCallStatus.open, LectureRollCallStatus.reopen);
			searchParams.setLectureConfiguredRepositoryEntry(true);
			
			long blocks = lectureService.countLectureBlocks(searchParams);
			if(blocks > 0) {
				String msg = translate("warning.open.rollcall", Long.toString(blocks));
				flc.contextPut("warningRollCall", msg);
			} else {
				flc.contextRemove("warningRollCall");
			}
		}
	}
	
	private LectureBlockRow forgeRow(Date now, LectureBlockWithTeachers block,
			String displayname, String externalRef, LectureBlockRef nextScheduledBlock ) {
		LectureBlock b = block.getLectureBlock();
		StringBuilder teachers = new StringBuilder();
		String separator = translate("user.fullname.separator");
		List<Identity> teachersList = new ArrayList<>(block.getTeachers());
		if(teachersList.size() > 1) {
			Collections.sort(teachersList, identityComparator);
		}
		
		boolean iAmTeacher = false;
		for(Identity teacher:teachersList) {
			if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
			teachers.append(userManager.getUserDisplayName(teacher));
			iAmTeacher |= getIdentity().getKey().equals(teacher.getKey());
		}
		
		final boolean rollCallEnabled = config.withRollCall() != Visibility.NO
				&& ConfigurationHelper.isRollCallEnabled(block.getLecturesConfigurations(), lectureModule);
		final ZonedDateTime date = DateUtils.toZonedDateTime(block.getLectureBlock().getStartDate(), calendarModule.getDefaultZoneId());
		
		LectureBlockRow row = new LectureBlockRow(b, date, displayname, externalRef,
				teachers.toString(), iAmTeacher, block.getCurriculumElementRef(), block.getEntryRef(),
				block.getNumOfParticipants(), block.getLeadTime(), block.getFollowupTime(),
				block.isAssessmentMode(), rollCallEnabled, getTranslator());
		row.setTeachersList(teachersList);
		
		if(isOnlineMeetingEnabled() && (b.getBBBMeeting() != null || b.getTeamsMeeting() != null)) {
			FormLink onlineMeetingLink = uifactory.addFormLink("oom_" + b.getKey(), CMD_OPEN_ONLINE_MEETING, "open.online.meeting", tableEl, Link.LINK_CUSTOM_CSS);
			onlineMeetingLink.setDomReplacementWrapperRequired(false);
			onlineMeetingLink.setIconLeftCSS("o_icon o_icon-fw o_icon-lg o_vc_icon");
			if(b.getStartDate() != null && b.getStartDate().compareTo(now) <= 0
					&& b.getEndDate() != null && b.getEndDate().compareTo(now) >= 0) {
				onlineMeetingLink.setPrimary(true);
			}
			onlineMeetingLink.setUserObject(row);
			row.setOpenOnlineMeetingLink(onlineMeetingLink);
		}
		
		row.setNextScheduled(nextScheduledBlock != null && nextScheduledBlock.getKey().equals(b.getKey()));
		
		if(rollCallEnabled && secCallback.viewAs() != LectureRoles.participant && hasRollCall(row)) {
			FormLink rollCallLink = uifactory.addFormLink("rcall_" + b.getKey(), CMD_ROLLCALL, "", tableEl, Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
			rollCallLink.setDomReplacementWrapperRequired(false);
			rollCallLink.setTitle(translate("edit.type.absence"));
			rollCallLink.setIconLeftCSS("o_icon o_icon-lg o_icon_lecture");
			rollCallLink.setUserObject(row);
			row.setRollCallButton(rollCallLink);
		}

		if(secCallback.viewAs() != LectureRoles.participant) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + CodeHelper.getRAMUniqueID(), "tools", "", null, null, Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
			toolsLink.setDomReplacementWrapperRequired(false);
			toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions");
			toolsLink.setTitle(translate("action.more"));
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		
		return row;
	}
	
	private boolean hasRollCall(LectureBlockRow row) {
		Date end = row.getLectureBlock().getEndDate();
		Date start = row.getLectureBlock().getStartDate();
		Date now = new Date();
		return end.before(now) || (row.isIamTeacher() && start.compareTo(now) <= 0);
	}
	
	private void updateTeachersFilters() {
		List<LectureBlockRow> rows = tableModel.getObjects();
		Set<Identity> teachers = new HashSet<>();
		for(LectureBlockRow row:rows) {
			teachers.addAll(row.getTeachersList());
		}
		List<Identity> teachersList = new ArrayList<>(teachers);
		if(teachersList.size() > 1) {
			Collections.sort(teachersList, new IdentityComparator(getLocale()));
		}

		teachersValues.clear();
		teachersValues.add(SelectionValues.entry(NO_TEACHER, translate("filter.no.teachers")));
		for(Identity teacher: teachersList) {
			String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(teacher));
			teachersValues.add(SelectionValues.entry(teacher.getKey().toString(), fullName));
		}
	}
	
	private LecturesBlockSearchParameters getSearchParams() {
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
		
		if(secCallback.viewAs() == LectureRoles.participant) {
			searchParams.setParticipant(getIdentity());
		} else if(secCallback.viewAs() == LectureRoles.teacher) {
			if(allTeachersButton != null && allTeachersButton.isPrimary()) {
				if(entry == null) {
					searchParams.setManager(getIdentity());
				}
				// else can see all lecture blocks of the course
			} else {
				searchParams.setTeacher(getIdentity());
			}
		} else {
			searchParams.setManager(getIdentity());
		}
		
		return searchParams;
	}
	
	private LecturesBlockSearchParameters getSearchParamsWithFilters(UserRequest ureq) {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		
		if(scopeEl.isVisible() && scopeEl.isEnabled() && scopeEl.isSelected()) {
			DateRange range = scopeEl.getSelectedDateRange();
			searchParams.setStartDate(range.getFrom());
			searchParams.setEndDate(range.getTo());
		}

		FlexiTableFilter vFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_VIRTUAL_STATUS);
		if (vFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				List<LectureBlockVirtualStatus> status = filterValues.stream()
						.map(LectureBlockVirtualStatus::valueOf)
						.toList();
				searchParams.setVirtualStatus(status);
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
				if(filterValues.contains(NO_TEACHER)) {
					searchParams.setWithTeachers(Boolean.FALSE);
				}
			}
		}
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		Date now = ureq.getRequestTimestamp();
		if(selectedTab == relevantTab) {
			searchParams.setStartDate(DateUtils.getStartOfDay(now));
		} else if(selectedTab == todayTab) {
			searchParams.setStartDate(DateUtils.getStartOfDay(now));
			searchParams.setEndDate(DateUtils.getEndOfDay(now));
		} else if(selectedTab == upcomingTab) {
			searchParams.setStartDate(DateUtils.getEndOfDay(now));
		} else if(selectedTab == pastTab) {
			searchParams.setEndDate(now);
		} else if(selectedTab == pendingTab) {
			searchParams.setEndDate(now);
			searchParams.addRollCallStatus(LectureRollCallStatus.open, LectureRollCallStatus.reopen);
			searchParams.setLectureConfiguredRepositoryEntry(true);
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
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureBlocks(List.of(lectureBlock));
			List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithOptionalTeachers(searchParams);
			if(blocks.size() == 1) {
				LectureBlockWithTeachers block = blocks.get(0);
				row.setLectureBlock(block.getLectureBlock());
				List<Identity> teachersList = new ArrayList<>(block.getTeachers());
				if(teachersList.size() > 1) {
					Collections.sort(teachersList, identityComparator);
				}
				row.setTeachersList(teachersList);
			} else {
				row.setLectureBlock(lectureBlock);
			}
			
			if(row.getDetailsController() != null) {
				doOpenLectureBlockDetails(ureq, row);
				tableEl.reset(false, false, false);
			} else {
				tableEl.reset(false, false, true);
			}
		}
		loadWarning(ureq);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(config.withFilterPresetRelevant()) {
				activateFilterTab(ureq, relevantTab);
			} else {
				activateFilterTab(ureq, allTab);
			}
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName().toLowerCase();
			Long id = entries.get(0).getOLATResourceable().getResourceableId();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("lecture".equalsIgnoreCase(type) || "lectureblock".equalsIgnoreCase(type)) {
				activateFilterTab(ureq, allTab);
				activateLecture(ureq, id, subEntries);
			} else if("OnlineMeeting".equalsIgnoreCase(type)) {
				activateFilterTab(ureq, allTab);
				activateLecture(ureq, id, entries);
			} else if(tabsMap.containsKey(type.toLowerCase())) {
				activateFilterTab(ureq, tabsMap.get(type.toLowerCase()));
				if(entries.size() > 1) {
					Long subId = subEntries.get(0).getOLATResourceable().getResourceableId();
					String subType = subEntries.get(0).getOLATResourceable().getResourceableTypeName().toLowerCase();
					if("lecture".equalsIgnoreCase(subType) || "lectureblock".equalsIgnoreCase(subType)) {
						List<ContextEntry> subSubEntries = subEntries.subList(1, subEntries.size());
						activateLecture(ureq, subId, subSubEntries);
					}
				}
			} else {
				activateFilterTab(ureq, allTab);
			}
		}
	}
	
	private void activateFilterTab(UserRequest ureq, FlexiFiltersTab filterTab) {
		tableEl.setSelectedFilterTab(ureq, filterTab);
		if(filterTab == relevantTab) {
			FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
			sortOptions.setDefaultOrderBy(new SortKey(BlockCols.startTime.name(), true));
			sortOptions.setFromColumnModel(true);
			tableEl.setSortSettings(sortOptions);
		}
		loadModel(ureq);
		updateTeachersFilters();
	}

	/**
	 * 
	 * @param ureq The user request
	 * @param lectureBlockKey The key of the lecture block
	 * @param subEntries An optional additional business path to resolve (OnlineMeeting, Start roll call)
	 * @return 
	 */
	private void activateLecture(UserRequest ureq, Long lectureBlockKey, List<ContextEntry> subEntries) {
		int index = tableModel.getIndexByKey(lectureBlockKey);
		if(index >= 0) {
			int page = index / tableEl.getPageSize();
			tableEl.setPage(page);
			LectureBlockRow row = tableModel.getObject(index);
			doOpenLectureBlockDetails(ureq, row);
			tableEl.expandDetails(index);
			
			if(subEntries != null && !subEntries.isEmpty()) {
				String subType = subEntries.get(0).getOLATResourceable().getResourceableTypeName();
				if("OnlineMeeting".equalsIgnoreCase(subType)) {
					doOpenOnlineMeeting(ureq, row);
				} else if("Start".equalsIgnoreCase(subType)) {
					doRollCall(ureq, row);
				} else if("StartWizard".equalsIgnoreCase(subType)) {
					doRollCallWizard(ureq, row);
				}
			} else {
				String elemId = "#row_o_fi" + tableEl.getComponent().getDispatchID() + "-" + index;
				getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollToElemId(elemId));
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLectureButton == source) {
			doAddLectureBlock(ureq);
		} else if(deleteLecturesButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(manageTeachersButton == source) {
			doBulkManageTeachers(ureq);
		} else if(copyLecturesButton == source) {
			doBulkCopy(ureq);
		} else if(importLecturesButton == source) {
			doImportLecturesBlock(ureq);
		} else if(allLevelsButton == source) {
			doToggleLevels(ureq, false);
		} else if(thisLevelButton == source) {
			doToggleLevels(ureq, true);
		} else if (source == scopeEl) {
			selectScope(ureq);
		} else if(allTeachersButton == source) {
			saveAllTeachersSwitch(ureq, true);
			loadModel(ureq);
		} else if(onlyMineButton == source) {
			saveAllTeachersSwitch(ureq, false);
			loadModel(ureq);
		} else if(pendingRollCallLink == source) {
			doPending(ureq);
		} else if(startButton == source && startButton.getUserObject() instanceof LectureBlockRow row) {
			doRollCall(ureq, row);
		} else if(startWizardButton == source && startWizardButton.getUserObject() instanceof LectureBlockRow row) {
			doRollCallWizard(ureq, row);
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
				} else if(CMD_CURRICULUM_ELEMENT.equals(cmd)) {
					doOpenCurriculumElement(ureq, row);
				} else if(CMD_REPOSITORY_ENTRY.equals(cmd)) {
					doOpenRepositoryEntry(ureq, row);
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
			if(cmd != null && cmd.equals("tools")
					&& link.getUserObject() instanceof LectureBlockRow row) {
				doOpenTools(ureq, row, link);
			} else if(CMD_OPEN_ONLINE_MEETING.equals(cmd)
					&& link.getUserObject() instanceof LectureBlockRef ref) {
				doOpenOnlineMeeting(ureq, ref);
			} else if(CMD_ROLLCALL.equals(cmd)
					&& link.getUserObject() instanceof LectureBlockRow row) {
				doRollCall(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addLectureCtrl == source || deleteLectureBlocksCtrl == source
				|| assignNewEntryCtrl == source || manageTeachersCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editLectureCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addLectureWizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel(ureq);
				}
				cleanUp();
			}
		} else if(rollCallCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.BACK_EVENT) {
				reloadModel(ureq, rollCallCtrl.getLectureBlock());
				stackPanel.popController(rollCallCtrl);
				cleanUp();
			}
		} else if(rollCallWizardCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reloadModel(ureq, rollCallWizardCtrl.getLectureBlock());
			}
			getWindowControl().pop();
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			getWindowControl().getWindowBackOffice()
				.getChiefController().getScreenMode().setMode(Mode.standard, businessPath);
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
				loadModel(ureq);
			}
		} else if(assessmentModeEditCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel(ureq);
				stackPanel.popController(assessmentModeEditCtrl);
				cleanUp();
			}
		} else if(deleteAssessmentModeDialogBox == source) {
			if((DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event))
					&& deleteAssessmentModeDialogBox.getUserObject() instanceof LectureBlockRef row) {
				doDeleteAssessmentMode(ureq, row);
			}
		} else if(confirmChangeToLocationMeetingCtrl == source) {
			if(event == Event.DONE_EVENT
					&& confirmChangeToLocationMeetingCtrl.getUserObject() instanceof LectureBlockRef row) {
				doChangeToLocationMeeting(ureq, row);
			}
			cmc.deactivate();
			cleanUp();
		} else if(onlineMeetingCtrl == source) {
			if(event == Event.BACK_EVENT) {
				stackPanel.popController(onlineMeetingCtrl);
			}
		} else if(editBigBlueButtonMeetingCtrl == source) {
			if(event == Event.DONE_EVENT
					&& editBigBlueButtonMeetingCtrl.getUserObject() instanceof LectureBlockRef row) {
				doFinalizeChangeToOnlineMeeting(ureq, row, editBigBlueButtonMeetingCtrl.getMeeting());
			}
			cmc.deactivate();
			cleanUp();
		} else if(editTeamsMeetingCtrl == source) {
			if(event == Event.DONE_EVENT
					&& editTeamsMeetingCtrl.getUserObject() instanceof LectureBlockRow row) {
				doFinalizeChangeToOnlineMeeting(ureq, row, editTeamsMeetingCtrl.getMeeting());
			}
			cmc.deactivate();
			cleanUp();
		} else if(source instanceof LectureListDetailsController) {
			if(event instanceof OpenOnlineMeetingEvent meetingEvent) {
				doOpenOnlineMeeting(ureq, meetingEvent.getLectureBlock());
			} else if(event instanceof EditLectureBlockRowEvent editRowEvent) {
				doEditLectureBlock(ureq, editRowEvent.getRow());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmChangeToLocationMeetingCtrl);
		removeAsListenerAndDispose(deleteAssessmentModeDialogBox);
		removeAsListenerAndDispose(editBigBlueButtonMeetingCtrl);
		removeAsListenerAndDispose(deleteLectureBlocksCtrl);
		removeAsListenerAndDispose(assessmentModeEditCtrl);
		removeAsListenerAndDispose(addLectureWizardCtrl);
		removeAsListenerAndDispose(rollCallWizardCtrl);
		removeAsListenerAndDispose(assignNewEntryCtrl);
		removeAsListenerAndDispose(manageTeachersCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(addLectureCtrl);
		removeAsListenerAndDispose(rollCallCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmChangeToLocationMeetingCtrl = null;
		deleteAssessmentModeDialogBox = null;
		editBigBlueButtonMeetingCtrl = null;
		deleteLectureBlocksCtrl = null;
		assessmentModeEditCtrl = null;
		addLectureWizardCtrl = null;
		rollCallWizardCtrl = null;
		assignNewEntryCtrl = null;
		manageTeachersCtrl = null;
		toolsCalloutCtrl = null;
		editLectureCtrl = null;
		addLectureCtrl = null;
		rollCallCtrl = null;
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
	
	private boolean isAllTeachersSwitch(UserRequest ureq, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(AbstractTeacherOverviewController.class, switchPrefsId);
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveAllTeachersSwitch(UserRequest ureq, boolean all) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(AbstractTeacherOverviewController.class, switchPrefsId, Boolean.valueOf(all));
		}
		allTeachersButton.setPrimary(all);
		onlyMineButton.setPrimary(!all);
	}
	
	private void doPending(UserRequest ureq) {
		activateFilterTab(ureq, pendingTab);
		scopeEl.setSelectedKey(null);
		loadModel(ureq);
	}

	private void doEditLectureBlock(UserRequest ureq, LectureBlockRow row) {
		if(guardModalController(editLectureCtrl)) return;
		
		LectureBlock block = lectureService.getLectureBlock(row);
		if(block == null) {
			loadModel(ureq);
		} else {
			doEditLectureBlock(ureq, block);
		}
	}
	
	private void doEditLectureBlock(UserRequest ureq, LectureBlock block) {
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
		if(guardModalController(addLectureWizardCtrl) || !secCallback.canNewLectureBlock()) return;
		
		if(entry == null && curriculumElement == null && curriculum == null) {
			showWarning("error.no.entry.curriculum");
		} else if(entry != null) {
			doAddLectureBlockSimplified(ureq, entry);
		} else if(curriculumElement != null) {
			List<RepositoryEntry> entries = this.curriculumService.getRepositoryEntries(curriculumElement);
			AddLectureContext addLecture = new AddLectureContext(curriculum, curriculumElement);
			addLecture.setCurriculumElement(curriculumElement);
			
			List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
			if(descendants.isEmpty() && entries.isEmpty()) {
				doAddLectureBlockSimplified(ureq, curriculumElement);
			} else {
				doAddLectureBlockWizard(ureq, addLecture);
			}
		} else {
			AddLectureContext addLecture = new AddLectureContext(curriculum, null);
			doAddLectureBlockWizard(ureq, addLecture);
		}
	}
	
	private void doAddLectureBlockSimplified(UserRequest ureq, RepositoryEntry repositoryEntry) {
		removeAsListenerAndDispose(addLectureCtrl);
		
		addLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), repositoryEntry, null, false);
		listenTo(addLectureCtrl);

		String title = translate("add.lecture");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addLectureCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddLectureBlockSimplified(UserRequest ureq, CurriculumElement element) {
		removeAsListenerAndDispose(addLectureCtrl);
		
		addLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), element, null, false);
		listenTo(addLectureCtrl);

		String title = translate("add.lecture");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addLectureCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddLectureBlockWizard(UserRequest ureq, AddLectureContext addLecture) {	
		AddLectureBlock1ResourcesStep step = new AddLectureBlock1ResourcesStep(ureq, addLecture);
		AddLectureBlockStepCallback stop = new AddLectureBlockStepCallback(addLecture);
		String title = translate("add.lecture");
		
		removeAsListenerAndDispose(addLectureWizardCtrl);
		addLectureWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, null, title, "");
		listenTo(addLectureWizardCtrl);
		getWindowControl().pushAsModalDialog(addLectureWizardCtrl.getInitialComponent());
		
		if(step.canJumpStep()) {
			addLectureWizardCtrl.next(ureq);
		}
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
	
	private void doBulkCopy(UserRequest ureq) {
		int count = 0;
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<LectureBlock> selectedBlocks = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexes) {
			LectureBlockRow row = tableModel.getObject(selectedIndex.intValue());
			if(row != null) {
				LectureBlock block = lectureService.getLectureBlock(row);
				if(block != null) {
					selectedBlocks.add(block);
				}
			}
		}
		if(selectedBlocks.isEmpty()) {
			showWarning("error.atleastone.lecture");
		} else if(selectedBlocks.size() == 1) {
			LectureBlock block = selectedBlocks.get(0);
			String newTitle = translate("lecture.block.copy", block.getTitle());
			LectureBlock copiedBlock = lectureService.copyLectureBlock(newTitle, block, false);
			doEditLectureBlock(ureq, copiedBlock);
		} else {
			for(LectureBlock block:selectedBlocks) {
				String newTitle = translate("lecture.block.copy", block.getTitle());
				lectureService.copyLectureBlock(newTitle, block, true);
				dbInstance.commitAndCloseSession();
				count++;
			}
			loadModel(ureq);
			
			if(count == 1) {
				showInfo("lecture.block.copied");
			} else if(count > 1) {
				showInfo("lecture.block.copied.plural", Integer.toString(count));
			} else {
				showWarning("error.atleastone.lecture");
			}
		}
	}
	
	private void doCopy(UserRequest ureq, LectureBlockRow row) {
		LectureBlock block = lectureService.getLectureBlock(row);
		String newTitle = translate("lecture.block.copy", block.getTitle());
		LectureBlock copiedBlock = lectureService.copyLectureBlock(newTitle, block, false);
		doEditLectureBlock(ureq, copiedBlock);
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		List<LectureBlock> blocks = getSelectableLectureBlocks(LectureBlockManagedFlag.delete);
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
	
	private List<LectureBlock> getSelectableLectureBlocks(LectureBlockManagedFlag managedFlag) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<LectureBlock> blocks = new ArrayList<>();
		for(Integer selection:selections) {
			LectureBlockRow blockRow = tableModel.getObject(selection);
			if(!LectureBlockManagedFlag.isManaged(blockRow.getLectureBlock(), managedFlag)) {
				blocks.add(blockRow.getLectureBlock());
			}
		}
		return blocks;
	}
	
	private void doConfirmDelete(UserRequest ureq, LectureBlockRow row) {
		LectureBlock block = lectureService.getLectureBlock(row);
		deleteLectureBlocksCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), List.of(block));
		listenTo(deleteLectureBlocksCtrl);
		
		String title = translate("delete.lectures.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteLectureBlocksCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkManageTeachers(UserRequest ureq) {
		List<LectureBlockRow> blocks = getSelectableLectureBlocksRows(LectureBlockManagedFlag.teachers);
		if(blocks.isEmpty()) {
			showWarning("error.atleastone.lecture");
		} else {
			manageTeachersCtrl = new ManageTeachersController(ureq, getWindowControl(), blocks,
					config, secCallback, entry);
			listenTo(manageTeachersCtrl);
			
			String title = translate("manage.teachers");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), manageTeachersCtrl.getInitialComponent(), true, title);
			cmc.setCustomWindowCSS("o_modal_large");
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private List<LectureBlockRow> getSelectableLectureBlocksRows(LectureBlockManagedFlag managedFlag) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<LectureBlockRow> blocks = new ArrayList<>();
		for(Integer selection:selections) {
			LectureBlockRow blockRow = tableModel.getObject(selection);
			if(!LectureBlockManagedFlag.isManaged(blockRow.getLectureBlock(), managedFlag)) {
				blocks.add(blockRow);
			}
		}
		return blocks;
	}

	private void doExportLog(UserRequest ureq, LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(row);
		RepositoryEntry re = entry == null ? lectureBlock.getEntry() : entry;
		LectureBlockAuditLogExport export = new LectureBlockAuditLogExport(re, lectureBlock, auditLog, authorizedAbsenceEnabled, getTranslator());
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
				mainForm, config, secCallback, lectureManagementManaged, entry != null);
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
	
	private void doRollCall(UserRequest ureq, LectureBlockRow row) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(row);
		if(reloadedBlock == null) {
			loadModel(ureq);
		} else {
			RollCallSecurityCallback rollCallSecCallback = getRollCallSecurityCallback(reloadedBlock, row.isIamTeacher());
			List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("LectureBlock", reloadedBlock.getKey());
			WindowControl swControl = addToHistory(ureq, ores, null);
			rollCallCtrl = new TeacherRollCallController(ureq, swControl, reloadedBlock, participants, rollCallSecCallback, true);
			listenTo(rollCallCtrl);
			stackPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void doRollCallWizard(UserRequest ureq, LectureBlockRow row) {
		if(rollCallWizardCtrl != null) return;
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(row);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		RollCallSecurityCallback rollCallSecCallback = getRollCallSecurityCallback(reloadedBlock, row.isIamTeacher());
		rollCallWizardCtrl = new TeacherRollCallWizardController(ureq, getWindowControl(), reloadedBlock, participants, rollCallSecCallback);
		if(entry != null) {
			rollCallWizardCtrl.addLoggingResourceable(CoreLoggingResourceable.wrap(entry.getOlatResource(),
					OlatResourceableType.course, reloadedBlock.getEntry().getDisplayname()));
		}
		listenTo(rollCallWizardCtrl);
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		cc.getScreenMode().setMode(Mode.full, null);
		getWindowControl().pushToMainArea(rollCallWizardCtrl.getInitialComponent());
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_STARTED, getClass(),
				CoreLoggingResourceable.wrap(reloadedBlock, OlatResourceableType.lectureBlock, reloadedBlock.getTitle()));
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block, boolean iAmTeacher) {
		boolean admin = secCallback.viewAs() == LectureRoles.lecturemanager;
		boolean masterCoach = secCallback.viewAs() == LectureRoles.mastercoach;
		return new RollCallSecurityCallbackImpl(admin, masterCoach, iAmTeacher, block, lectureModule);
	}
	
	private void doOpenCurriculumElement(UserRequest ureq, LectureBlockRow row) {
		if(row.getCurriculumElement() == null) return;
		
		StringBuilder elementPath = new StringBuilder();
		CurriculumElement el = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(row.getCurriculumElement().key()));
		if(curriculumElement != null && curriculumElement.equals(el)) {
			elementPath.append("[Overview:0]");
		} else {
			if(curriculum == null && curriculumElement == null) {
				elementPath.append("[Curriculum:").append(el.getCurriculum().getKey()).append("]");
			}
			elementPath.append("[CurriculumElement:").append(el.getKey()).append("]");
		}

		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(elementPath.toString());
		fireEvent(ureq, new ActivateEvent(entries));
	}
	
	private void doOpenRepositoryEntry(UserRequest ureq, LectureBlockRow row) {
		if(row.getCurriculumElement() == null || row.getEntry().key() == null) return;
		
		String path = "[RepositoryEntry:" + row.getEntry().key() + "]";
		NewControllerFactory.getInstance().launch(path, ureq, getWindowControl());
	}
	
	private void doAssignNewEntry(UserRequest ureq, LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		RepositoryEntry currentEntry = lectureBlock.getEntry();
		CurriculumElement element = lectureBlock.getCurriculumElement();
		assignNewEntryCtrl = new AssignNewRepositoryEntryController(ureq, getWindowControl(),
				lectureBlock, element, currentEntry);
		listenTo(assignNewEntryCtrl);
		
		String title = translate("assign.new.entry");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), assignNewEntryCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenOnlineMeeting(UserRequest ureq, LectureBlockRef lectureBlockRef) {
		onlineMeetingCtrl = new LectureBlockOnlineMeetingController(ureq, getWindowControl(), lectureBlockRef, config, secCallback);
		listenTo(onlineMeetingCtrl);
		stackPanel.pushController(translate("online.meeting"), onlineMeetingCtrl);
	}
	
	private void doChangeToOnlineMeeting(UserRequest ureq, LectureBlockRow lectureBlockRef) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(lectureBlockRef);
		if(lectureBlock.getTeamsMeeting() == null && bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isLecturesEnabled()) {
			doEditBigBlueButtonMeeting(ureq, lectureBlock);
		} else if(lectureBlock.getBBBMeeting() == null && teamsModule.isEnabled() && teamsModule.isLecturesEnabled()) {
			doEditTeamsMeeting(ureq, lectureBlock);
		}
	}
	
	private void doEditBigBlueButtonMeeting(UserRequest ureq, LectureBlock lectureBlock) {
		BigBlueButtonMeeting bigBlueButtonMeeting;
		if(lectureBlock.getBBBMeeting() == null) {
			bigBlueButtonMeeting = bigBlueButtonManager.createMeeting(lectureBlock.getTitle(),
					lectureBlock.getStartDate(), lectureBlock.getEndDate(), null, null, null, getIdentity());
		} else {
			bigBlueButtonMeeting = bigBlueButtonManager.getMeeting(lectureBlock.getBBBMeeting());
		}
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, null, getIdentity(), ureq.getUserSession().getRoles());
		editBigBlueButtonMeetingCtrl = new EditBigBlueButtonMeetingController(ureq, getWindowControl(), bigBlueButtonMeeting, permissions);
		editBigBlueButtonMeetingCtrl.setUserObject(lectureBlock);
		listenTo(editBigBlueButtonMeetingCtrl);

		String title = translate("edit.online.meeting.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editBigBlueButtonMeetingCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditTeamsMeeting(UserRequest ureq, LectureBlock lectureBlock) {
		TeamsMeeting teamsMeeting;
		if(lectureBlock.getTeamsMeeting() == null) {
			teamsMeeting = teamsService.createMeeting(lectureBlock.getTitle(),
					lectureBlock.getStartDate(), lectureBlock.getEndDate(), null, null, null, getIdentity());
		} else {
			teamsMeeting = teamsService.getMeeting(lectureBlock.getTeamsMeeting());
		}
		editTeamsMeetingCtrl = new EditTeamsMeetingController(ureq, getWindowControl(), teamsMeeting);
		listenTo(editTeamsMeetingCtrl);

		String title = translate("edit.online.meeting.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editTeamsMeetingCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeChangeToOnlineMeeting(UserRequest ureq, LectureBlockRef row, BigBlueButtonMeeting meeting) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		TeamsMeeting teamsMeeting = lectureBlock.getTeamsMeeting();
		lectureBlock.setTeamsMeeting(null);
		lectureBlock.setBBBMeeting(meeting);
		lectureService.save(lectureBlock, null);
		if(teamsMeeting != null) {
			teamsService.deleteMeeting(teamsMeeting);
		}
		loadModel(ureq);
	}
	
	private void doFinalizeChangeToOnlineMeeting(UserRequest ureq, LectureBlockRef row, TeamsMeeting meeting) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		BigBlueButtonMeeting bigBlueButtonMeeting = lectureBlock.getBBBMeeting();
		lectureBlock.setTeamsMeeting(meeting);
		lectureBlock.setBBBMeeting(null);
		lectureService.save(lectureBlock, null);
		if(bigBlueButtonMeeting != null) {
			bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
		}
		loadModel(ureq);
	}
	
	private void doConfirmChangeToLocationMeeting(UserRequest ureq, LectureBlockRow row) {
		String escapedTitle = StringHelper.escapeHtml(row.getTitle());
		String message = translate("confirmation.change.to.location.meeting.text", escapedTitle);
		String confirmButton = translate("confirmation.change.to.location.button");
		translate("confirmation.delete.curriculum");
		confirmChangeToLocationMeetingCtrl = new ConfirmationController(ureq, getWindowControl(),
				message, message, confirmButton, ButtonType.danger, translate("cancel"), true);
		listenTo(confirmChangeToLocationMeetingCtrl);
		confirmChangeToLocationMeetingCtrl.setUserObject(row);

		String title = translate("confirmation.change.to.location.meeting.title", escapedTitle);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeToLocationMeetingCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChangeToLocationMeeting(UserRequest ureq, LectureBlockRef row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		TeamsMeeting teamsMeeting = lectureBlock.getTeamsMeeting();
		BigBlueButtonMeeting bigBlueButtonMeeting = lectureBlock.getBBBMeeting();
		lectureBlock.setTeamsMeeting(null);
		lectureBlock.setBBBMeeting(null);
		lectureService.save(lectureBlock, null);
		
		if(teamsMeeting != null) {
			teamsService.deleteMeeting(teamsMeeting);
		}
		if(bigBlueButtonMeeting != null) {
			bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
		}
		loadModel(ureq);
	}
	
	private void doExportLectureBlock(UserRequest ureq, LectureBlockRef row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		if(lectureBlock == null) {
			loadModel(ureq);
		} else {
			List<Identity> teachers = lectureService.getTeachers(lectureBlock);
			Roles roles = ureq.getUserSession().getRoles();
			boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
			LectureBlockExport export = new LectureBlockExport(lectureBlock, teachers, isAdministrativeUser, authorizedAbsenceEnabled, getTranslator());
			ureq.getDispatchResult().setResultingMediaResource(export);
		}
	}
	
	private void doExportAttendanceList(UserRequest ureq, LectureBlockRef row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		if(lectureBlock == null) {
			loadModel(ureq);
		} else {
			List<Identity> participants = lectureService.getParticipants(lectureBlock);
			if(participants.size() > 1) {
				Collections.sort(participants, new IdentityComparator(getLocale()));
			}
			List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(row);
			List<AbsenceNotice> notices = lectureService.getAbsenceNoticeRelatedTo(lectureBlock);
	
			try {
				LecturesBlockPDFExport export = new LecturesBlockPDFExport(lectureBlock, authorizedAbsenceEnabled, getTranslator());
				export.setTeacher(userManager.getUserDisplayName(getIdentity()));
				export.create(participants, rollCalls, notices);
				ureq.getDispatchResult().setResultingMediaResource(export);
			} catch (IOException | TransformerException e) {
				logError("", e);
			}
		}
	}
	
	private void doExportAttendanceListForSignature(UserRequest ureq, LectureBlockRef row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		if(lectureBlock == null) {
			loadModel(ureq);
		} else {
			List<Identity> participants = lectureService.getParticipants(lectureBlock);
			if(participants.size() > 1) {
				Collections.sort(participants, new IdentityComparator(getLocale()));
			}
			try {
				LecturesBlockSignaturePDFExport export = new LecturesBlockSignaturePDFExport(lectureBlock, getTranslator());
				export.setTeacher(userManager.getUserDisplayName(getIdentity()));
				export.create(participants);
				ureq.getDispatchResult().setResultingMediaResource(export);
			} catch (IOException | TransformerException e) {
				logError("", e);
			}
		}
	}
	
	private void doAddAssessmentMode(UserRequest ureq, LectureBlockRef row) {
		removeControllerListener(assessmentModeEditCtrl);
		
		LectureBlock block = lectureService.getLectureBlock(row);
		if(block == null) {
			loadModel(ureq);
		} else {
			RepositoryEntry blockEntry = block.getEntry();
			RepositoryEntryLectureConfiguration lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(blockEntry);
			AssessmentMode newMode = assessmentModeMgr.getAssessmentMode(block);
			if(newMode == null) {
				int leadTime = ConfigurationHelper.getLeadTime(lectureConfig, lectureModule);
				int followupTime = ConfigurationHelper.getFollowupTime(lectureConfig, lectureModule);
				String ipList = ConfigurationHelper.getAdmissibleIps(lectureConfig, lectureModule);
				newMode = assessmentModeMgr.createAssessmentMode(block, leadTime, followupTime, ipList);
			}
			assessmentModeEditCtrl = new AssessmentModeForLectureEditController(ureq, getWindowControl(), blockEntry, newMode);
			listenTo(assessmentModeEditCtrl);
			stackPanel.pushController(block.getTitle(), assessmentModeEditCtrl);
		}
	}
	
	private void doConfirmDeleteAssessmentMode(UserRequest ureq, LectureBlockRef row) {
		LectureBlock block = lectureService.getLectureBlock(row);
		if(block == null) {
			loadModel(ureq);
		} else {
			String names = StringHelper.escapeHtml(block.getTitle());
			String title = translate("confirm.delete.assessment.mode.title");
			String text = translate("confirm.delete.assessment.mode.text", names);
			deleteAssessmentModeDialogBox = activateYesNoDialog(ureq, title, text, deleteAssessmentModeDialogBox);
			deleteAssessmentModeDialogBox.setUserObject(row);
		}
	}
	
	private void doDeleteAssessmentMode(UserRequest ureq, LectureBlockRef row) {
		LectureBlock block = lectureService.getLectureBlock(row);
		if(block != null) {
			assessmentModeMgr.delete(block);
			loadModel(ureq);
		}
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
		private Link exportLink;
		private Link assignNewEntry;
		private Link attendanceListLink;
		private Link openOnlineMeetingLink;
		private Link changeToOnlineMeetingLink;
		private Link changeToLocationMeetingLink;
		private Link attendanceListForSignatureLink;
		private Link addAssessmentModeLink;
		private Link editAssessmentModeLink;
		private Link deleteAssessmentModeLink;
		
		private final LectureBlockRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("lectures_tools");
			
			LectureBlock lectureBlock = row.getLectureBlock();
			if((bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isLecturesEnabled())
					|| (teamsModule.isEnabled() && teamsModule.isLecturesEnabled())) {
				if(lectureBlock.getBBBMeeting() != null || lectureBlock.getTeamsMeeting() != null) {			
					openOnlineMeetingLink = LinkFactory.createLink("open.online.meeting", CMD_OPEN_ONLINE_MEETING, getTranslator(), mainVC, this, Link.LINK);
					openOnlineMeetingLink.setIconLeftCSS("o_icon o_icon-fw o_vc_icon");
					changeToLocationMeetingLink = LinkFactory.createLink("change.to.location.meeting", "change.to.location.meeting", getTranslator(), mainVC, this, Link.LINK);
					changeToLocationMeetingLink.setIconLeftCSS("o_icon o_icon-fw o_icon_location");
				} else if(lectureBlock.getEndDate() != null && lectureBlock.getEndDate().after(ureq.getRequestTimestamp())) {
					changeToOnlineMeetingLink = LinkFactory.createLink("change.to.online.meeting", "change.to.online.meeting", getTranslator(), mainVC, this, Link.LINK);
					changeToOnlineMeetingLink.setIconLeftCSS("o_icon o_icon-fw o_vc_icon");
				}
			}
			
			RepositoryEntryLectureConfiguration entryConfig = null;
			if(entry != null) {
				entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
			} else if(lectureBlock.getEntry() != null) {
				entryConfig = lectureService.getRepositoryEntryLectureConfiguration(lectureBlock.getEntry());
			}
			boolean withAssessment = entryConfig != null && ConfigurationHelper.isAssessmentModeEnabled(entryConfig, lectureModule);
			
			if(secCallback.canViewList()) {
				exportLink = addLink("export", "export", "o_icon o_icon-fw o_filetype_xlsx", mainVC);
				if(row.getEntry() != null && row.getEntry().key() != null) {
					attendanceListLink = addLink("attendance.list", "attendance.list", "o_icon o_icon-fw o_filetype_pdf", mainVC);
				}
				attendanceListForSignatureLink = addLink("attendance.list.to.sign", "attendance.list.to.sign", "o_icon o_icon-fw o_filetype_pdf", mainVC);
			}
			
			if(secCallback.canAssessmentMode()) {
				if(row.isAssessmentMode()) {
					editAssessmentModeLink = addLink("edit.assessment.mode", "add.assessment.mode", "o_icon o_icon-fw o_icon_assessment_mode", mainVC);
					deleteAssessmentModeLink = addLink("delete.assessment.mode", "delete.assessment.mode", "o_icon o_icon-fw o_icon_delete_item", mainVC);
				} else if(withAssessment && lectureBlock.getBBBMeeting() == null && lectureBlock.getTeamsMeeting() == null) {
					addAssessmentModeLink = addLink("add.assessment.mode", "add.assessment.mode", "o_icon o_icon-fw o_icon_assessment_mode", mainVC);
				}
			}
			
			if(!lectureManagementManaged && secCallback.canNewLectureBlock()) {
				editLink = addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", mainVC);
			}

			if(secCallback.canReopenLectureBlock() && (lectureBlock.getStatus() == LectureBlockStatus.cancelled
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.closed
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.autoclosed)) {
				reopenLink = addLink("reopen.lecture.blocks", "reopen", "o_icon o_icon-fw o_icon_reopen", mainVC);
			}
			
			if(secCallback.canNewLectureBlock()) {
				copyLink = addLink("copy", "copy", "o_icon o_icon-fw o_icon_copy", mainVC);
			}

			if(secCallback.canViewLog()) {
				logLink = addLink("log", "log", "o_icon o_icon-fw o_icon_log", mainVC);
			}
			
			if(secCallback.canNewLectureBlock()) {
				if(canAssignNewRepositoryEntry()) {
					assignNewEntry = addLink("assign.new.entry", "assign.new.entry", "o_icon o_icon-fw o_icon_assign_new_item", mainVC);
				}
				if(!LectureBlockManagedFlag.isManaged(row.getLectureBlock(), LectureBlockManagedFlag.delete)) {
					deleteLink = addLink("delete", "delete", "o_icon o_icon-fw o_icon_delete_item", mainVC);
				}
			}
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			return link;
		}
		
		private boolean canAssignNewRepositoryEntry() {
			if((curriculum != null || curriculumElement != null)
					&& row.getCurriculumElement() != null && row.getCurriculumElement().key() != null) {
				List<RepositoryEntry> entries = curriculumService
						.getRepositoryEntries(new CurriculumElementRefImpl(row.getCurriculumElement().key()));
				if(row.getEntry() == null || row.getEntry().key() == null) {
					return !entries.isEmpty();
				}
				return entries.size() > 1;
			}
			return false;
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
			} else if(assignNewEntry == source) {
				doAssignNewEntry(ureq, row);
			} else if(openOnlineMeetingLink == source) {
				doOpenOnlineMeeting(ureq, row);
			} else if(changeToOnlineMeetingLink == source) {
				doChangeToOnlineMeeting(ureq, row);
			} else if(changeToLocationMeetingLink == source) {
				doConfirmChangeToLocationMeeting(ureq, row);
			} else if(exportLink == source) {
				doExportLectureBlock(ureq, row);
			} else if(attendanceListLink == source) {
				doExportAttendanceList(ureq, row);
			} else if(attendanceListForSignatureLink == source) {
				doExportAttendanceListForSignature(ureq, row);
			} else if(addAssessmentModeLink == source || editAssessmentModeLink == source) {
				doAddAssessmentMode(ureq, row);
			} else if(deleteAssessmentModeLink == source) {
				doConfirmDeleteAssessmentMode(ureq, row);
			}
		}
	}
}