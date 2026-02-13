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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserChangePasswordController;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.ExternalLink;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.coach.CoachingModule;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.ui.AbstractParticipantsListController.NextPreviousController;
import org.olat.modules.coach.ui.CoursesIdentityTableDataModel.Columns;
import org.olat.modules.coach.ui.UserDetailsController.Segment;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.modules.coach.ui.component.LastVisitCellRenderer;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.ui.ParticipantListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.modules.lecture.ui.component.RateWarningCellRenderer;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.TechnicalTypeRenderer;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CoursesIdentityController extends FormBasicController implements NextPreviousController, GenericEventListener {

	private static final String CMD_SELECT = "select";

	protected static final String FILTER_MARKED = "Marked";
	protected static final String FILTER_STATUS = "Status";
	protected static final String FILTER_PERIOD = "Period";
	protected static final String FILTER_LAST_VISIT = "LastVisit";
	protected static final String FILTER_CERTIFICATES = "certificates";
	protected static final String FILTER_ASSESSMENT = "assessment";
	
	protected static final String ASSESSMENT_PASSED = "assessment-passed-none";
	protected static final String ASSESSMENT_NOT_PASSED = "assessment-not-passed-none";
	protected static final String CERTIFICATES_WITHOUT = "without-certificates";
	protected static final String CERTIFICATES_WITH = "with-certificates";
	protected static final String CERTIFICATES_INVALID = "invalid-certificates";
	protected static final String VISIT_LESS_1_DAY = "less-1-day";
	protected static final String VISIT_LESS_1_WEEK = "less-1-week";
	protected static final String VISIT_LESS_4_WEEKS = "less-4-weeks";
	protected static final String VISIT_LESS_12_MONTHS = "less-12-months";
	protected static final String VISIT_MORE_12_MONTS = "more-12-months";
	
	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String FINISHED_TAB_ID = "Finished";

	private FlexiFiltersTab allTab;
	
	private FormLink resetLink;
	private FormLink nextStudent;
	private FormLink previousStudent;

	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private CoursesIdentityTableDataModel tableModel;
	
	private int counter = 0;
	private boolean hasChanged = false;
	
	private final int numOfStudents;
	private final boolean fullAccess;
	private final Object userObject;
	private final Identity assessedIdentity;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private UserDetailsController statementCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private UserChangePasswordController userChangePasswordCtlr;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LifecycleModule lifecycleModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CoachingModule coachingModule;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsMgr;
	
	public CoursesIdentityController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Object userObject, Identity student, int numOfStudents, boolean fullAccess) {
		super(ureq, wControl, "student_course_list");
		setTranslator(userManager.getPropertyHandlerTranslator(Util
				.createPackageTranslator(RepositoryService.class, getLocale(), Util
						.createPackageTranslator(ParticipantListRepositoryController.class, getLocale(), getTranslator()))));

		this.assessedIdentity = student;
		this.userObject = userObject;
		this.fullAccess = fullAccess;
		this.stackPanel = stackPanel;
		this.numOfStudents = numOfStudents;
		
		initForm(ureq);
		loadModel();

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initUserProfileForm(formLayout, ureq);
		initTableForm(formLayout, ureq);
	}
	
	private void initButtonsForm(FormItemContainer formLayout) {
		previousStudent = uifactory.addFormLink("previous.student", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		previousStudent.setIconLeftCSS("o_icon o_icon_slide_backward");
		previousStudent.setTitle(translate("previous.student"));
		previousStudent.setEnabled(numOfStudents > 1);

		nextStudent = uifactory.addFormLink("next.student", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		nextStudent.setIconLeftCSS("o_icon o_icon_slide_forward");
		nextStudent.setTitle(translate("previous.student"));
		nextStudent.setEnabled(numOfStudents > 1);
		
		Roles roles = securityManager.getRoles(assessedIdentity);
		if (coachingModule.isResetPasswordEnabled() && !(roles.isMoreThanUser())) {
			DropdownItem cmdDropdown = uifactory.addDropdownMenuMore("cmds", flc, getTranslator());
			
			resetLink = uifactory.addFormLink("reset.link", formLayout, Link.LINK);
			resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_password");
			cmdDropdown.addElement(resetLink);

			formLayout.add("cmds", cmdDropdown);
		}
	}
	
	private void initUserProfileForm(FormItemContainer formLayout, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String fullName = userManager.getUserDisplayName(assessedIdentity);
			layoutCont.contextPut("studentName", StringHelper.escapeHtml(fullName));
			layoutCont.contextPut("authorizedAbsenceEnabled", Boolean.valueOf(lectureModule.isAuthorizedAbsenceEnabled()));
			layoutCont.contextPut("absenceNoticeEnabled", Boolean.valueOf(lectureModule.isAbsenceNoticeEnabled()));
			layoutCont.contextPut("rollCallEnabled", Boolean.valueOf(lectureModule.isEnabled()));
		}

		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), assessedIdentity);
		CoachedIdentityInfoController profile = new CoachedIdentityInfoController(ureq, getWindowControl(), mainForm, assessedIdentity, profileConfig, portraitUser);
		listenTo(profile);
		formLayout.add("portrait", profile.getInitialFormItem());
	}
		
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel markColumn = new DefaultFlexiColumnModel(Columns.mark);
		markColumn.setIconHeader("o_icon o_icon_bookmark_header");
		markColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(markColumn);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.repoKey, CMD_SELECT));
		DefaultFlexiColumnModel courseNameCol = new DefaultFlexiColumnModel(Columns.repoName, CMD_SELECT);
		courseNameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(courseNameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoExternalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.repoExternalId));
		DefaultFlexiColumnModel technicalTypeCol = new DefaultFlexiColumnModel(false, Columns.technicalType);
		technicalTypeCol.setCellRenderer(new TechnicalTypeRenderer());
		columnsModel.addFlexiColumnModel(technicalTypeCol);
		
		if (lifecycleModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lifecycleSoftkey));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lifecycleLabel));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lifecycleStart,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lifecycleEnd,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.access,
				new AccessRenderer(getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastVisit,
				new LastVisitCellRenderer(getTranslator())));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion,
				new CompletionCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed,
				new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score,
				new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.numberAssessments,
				new ProgressOfCellRenderer()));
		
		// Certificates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate,
				new DownloadCertificateCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificateValidity,
				new DateFlexiCellRenderer(getLocale())));

		// Lectures
		if(lectureModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lecturesProgress,
					new LectureStatisticsCellRenderer(true)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.plannedLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.attendedLectures));
			if(lectureModule.isAuthorizedAbsenceEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.authorizedAbsenceLectures));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.unauthorizedAbsenceLectures));
				if(lectureModule.isAbsenceNoticeEnabled()) {
					columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.dispensedLectures));
				}
			} else {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.absentLectures));
			}
			
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.rateWarning,
					new RateWarningCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.rate,
					new PercentCellRenderer()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastModification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastCoachModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastUserModified));
		
		// Tools
        ActionsColumnModel actionsCol = new ActionsColumnModel(Columns.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CoursesIdentityTableDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_coached_identity_courses");
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
		
		initFilters();
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "fStudentCourseListController-v3.2");
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues markedKeyValue = new SelectionValues();
		markedKeyValue.add(SelectionValues.entry(FILTER_MARKED, translate("search.mark")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
				FILTER_MARKED, markedKeyValue, true));
		
		filters.add(new FlexiTableDateRangeFilter(translate("filter.date.range"), FILTER_PERIOD, true, false,
				getLocale()));
		
		SelectionValues lastVisitPK = new SelectionValues();
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_DAY, translate("filter.visit.less.1.day")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_WEEK, translate("filter.visit.less.1.week")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_4_WEEKS, translate("filter.visit.less.4.weeks")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_12_MONTHS, translate("filter.visit.less.12.months")));
		lastVisitPK.add(SelectionValues.entry(VISIT_MORE_12_MONTS, translate("filter.visit.more.12.months")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.last.visit"),
				FILTER_LAST_VISIT, lastVisitPK, true));
		
		SelectionValues assessmentPK = new SelectionValues();
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED, translate("filter.assessment.passed")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED, translate("filter.assessment.not.passed")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.assessment"),
				FILTER_ASSESSMENT, assessmentPK, true));

		SelectionValues statusPK = new SelectionValues();
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.coachpublished.name(), translate("cif.status.coachpublished")));
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.published.name(), translate("cif.status.published")));
		statusPK.add(SelectionValues.entry(RepositoryEntryStatusEnum.closed.name(), translate("status.closed")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.course.access"),
				FILTER_STATUS, statusPK, false));
		
		SelectionValues certificatesPK = new SelectionValues();
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITHOUT, translate("filter.certificate.without")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITH, translate("filter.certificate.with")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_INVALID, translate("filter.certificate.invalid")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.certificates"),
				FILTER_CERTIFICATES, certificatesPK, false));

    	tableEl.setFilters(true, filters, true, false);
	}
	
    private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		FlexiFiltersTab relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						RepositoryEntryStatusEnum.published.name())));
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		
		FlexiFiltersTab finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB_ID, translate("filter.finished"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						RepositoryEntryStatusEnum.closed.name())));
		finishedTab.setFiltersExpanded(true);
		tabs.add(finishedTab);
		

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
    }

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(assessedIdentity.getKey().equals(ce.getOwnerKey())) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	private void updateCertificate(Long certificateKey) {
		CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
		tableModel.putCertificate(certificate);
	}
	
	@Override
	public Object getUserObject() {
		return userObject;
	}
	
	private List<EfficiencyStatementEntry> loadModel() {
		List<Mark> marks = markManager.getMarks(getIdentity(), List.of("RepositoryEntry"));
		Set<Long> markedKeys = marks.stream()
				.map(Mark::getOLATResourceable)
				.map(OLATResourceable::getResourceableId)
				.collect(Collectors.toSet());
		
		List<RepositoryEntry> courses = fullAccess
				? coachingService.getUserCourses(assessedIdentity, true)
				: coachingService.getStudentsCourses(getIdentity(), assessedIdentity, true);
		
		List<EfficiencyStatementEntry> statements = coachingService
				.getEfficencyStatements(assessedIdentity, courses, List.of(), getLocale());
		Map<Long,EfficiencyStatementEntry> statementsMap = statements.stream()
				.collect(Collectors.toMap(EfficiencyStatementEntry::getRepositoryEntryKey, eff -> eff, (u, v) -> u));
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
		Map<Long, CertificateLight> certificateMap = new HashMap<>();
		for(CertificateLight certificate:certificates) {
			certificateMap.put(certificate.getOlatResourceKey(), certificate);
		}
		
		List<Long> courseEntryKeys = courses.stream()
				.map(RepositoryEntry::getKey).toList();
		List<AssessmentEntryScoring> assessmentEntries = assessmentService
				.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, courseEntryKeys);
		Map<Long, AssessmentEntryScoring> assessmentEntriesMap = new HashMap<>();
		for (AssessmentEntryScoring assessmentEntry : assessmentEntries) {
			assessmentEntriesMap.put(assessmentEntry.getRepositoryEntryKey(), assessmentEntry);
		}
		
		List<UserCourseInformations> userCourseInfos = userCourseInformationsMgr.getUserCourseInformations(assessedIdentity);
		Map<Long, UserCourseInformations> userCourseInfosMap = userCourseInfos.stream()
				.collect(Collectors.toMap(infos -> infos.getResource().getKey(), infos -> infos, (u, v) -> u));
		
		Map<Long, LectureBlockStatistics> lecturesMap = new HashMap<>();
		if(lectureModule.isEnabled()) {
			List<LectureBlockStatistics> lectureStats = lectureService.getParticipantLecturesStatistics(assessedIdentity, null);
			for(LectureBlockStatistics lectureStat:lectureStats) {
				lecturesMap.put(lectureStat.getRepoKey(), lectureStat);
			}
		}
		
		List<CourseIdentityRow> rows = new ArrayList<>(statements.size());
		for(RepositoryEntry courseEntry:courses) {
			boolean marked = markedKeys.contains(courseEntry.getKey());
			CertificateLight certificate = certificateMap.get(courseEntry.getOlatResource().getKey());
			UserCourseInformations userInfos = userCourseInfosMap.get(courseEntry.getOlatResource().getKey());
			Date recentLaunch = userInfos == null ? null: userInfos.getRecentLaunch();
			
			LectureBlockStatistics lectureStatistics = lecturesMap.get(courseEntry.getKey());
			AssessmentEntryScoring assessmentEntryScoring = assessmentEntriesMap.get(courseEntry.getKey());
			EfficiencyStatementEntry statement = statementsMap.get(courseEntry.getKey());
			
			CourseIdentityRow row = new CourseIdentityRow(courseEntry, statement, certificate,
					lectureStatistics, assessmentEntryScoring, recentLaunch, marked);
			forgeActionsLinks(row);
			rows.add(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		return statements;
	}
	
	private void forgeActionsLinks(CourseIdentityRow row) {
		//mark
		String count = Integer.toString(++counter);
		FormLink markLink = uifactory.addFormLink("mark_".concat(count), "mark", "", tableEl, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
	}
	
	private void reloadModel() {
		if(hasChanged) {
			//reload
			loadModel();
			hasChanged = false;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nextStudent == source) {
			fireEvent(ureq, new Event("next.student"));
		} else if(previousStudent == source) {
			fireEvent(ureq, new Event("previous.student"));
		} else if(resetLink == source) {
			resetPassword(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(CMD_SELECT.equals(se.getCommand())) {
					CourseIdentityRow selectedRow = tableModel.getObject(se.getIndex());
					doSelectDetails(ureq, selectedRow);
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CourseIdentityRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("mark".equals(cmd) && link.getUserObject() instanceof CourseIdentityRow row) {
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark" : "o_icon o_icon_bookmark_add");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == statementCtrl && hasChanged) {
					reloadModel();
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == statementCtrl) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, event);
			} else if ("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if ("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} 
		} else if (calloutCtrl == source || cmc == source) {
			cleanUp();
		} else if (userChangePasswordCtlr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
        	cleanUp();
        } else if(toolsCtrl == source) {
        	if(event == Event.CLOSE_EVENT) {
        		calloutCtrl.deactivate();
        		cleanUp();
        	}
        }
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userChangePasswordCtlr);
		removeControllerListener(calloutCtrl);
		removeControllerListener(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		userChangePasswordCtlr = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("RepositoryEntry".equals(ores.getResourceableTypeName())) {
			Long entryKey = ores.getResourceableId();
			for(CourseIdentityRow row:tableModel.getObjects()) {
				if(entryKey.equals(row.getRepositoryEntryKey())) {
					doSelectDetails(ureq, row);
					statementCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	private void resetPassword(UserRequest ureq) {
		removeAsListenerAndDispose(userChangePasswordCtlr);
		removeAsListenerAndDispose(cmc);
		
		userChangePasswordCtlr = new UserChangePasswordController(ureq, getWindowControl(), assessedIdentity);
		listenTo(userChangePasswordCtlr);
		String name = assessedIdentity.getUser().getFirstName() + " " + assessedIdentity.getUser().getLastName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userChangePasswordCtlr.getInitialComponent(), true, translate("reset.title", name));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = tableModel.getIndexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableModel.getRowCount()) {
			nextIndex = 0;
		}
		CourseIdentityRow nextEntry = tableModel.getObject(nextIndex);
		doSelectDetails(ureq, nextEntry);
	}
	
	private void previousEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int previousIndex = tableModel.getIndexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableModel.getRowCount()) {
			previousIndex = tableModel.getRowCount() - 1;
		}
		CourseIdentityRow previousEntry = tableModel.getObject(previousIndex);
		doSelectDetails(ureq, previousEntry);
	}
	
	private boolean doMark(UserRequest ureq, CourseIdentityRow row) {
		final Long repositoryEntryKey = row.getRepositoryEntryKey();
		final OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", repositoryEntryKey);
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			dbInstance.commit();//before sending, save the changes
			
			EntryChangedEvent e = new EntryChangedEvent(() -> repositoryEntryKey, getIdentity(), Change.removeBookmark, "coaching.student");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		}
		
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		dbInstance.commit();//before sending, save the changes
		
		EntryChangedEvent e = new EntryChangedEvent(() -> repositoryEntryKey, getIdentity(), Change.addBookmark, "coaching.student");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		return true;
	}
	
	private void doSelectDetails(UserRequest ureq, CourseIdentityRow entry) {
		Segment selectedTool = null;
		if(statementCtrl != null) {
			selectedTool = statementCtrl.getSelectedSegment();
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(RepositoryEntry.class, entry.getRepositoryEntryKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		String displayName = entry.getRepositoryEntryDisplayname();
		int entryIndex = tableModel.getObjects().indexOf(entry);
		String details = translate("students.details", displayName, String.valueOf(entryIndex), String.valueOf(tableModel.getRowCount()));
		
		statementCtrl = new UserDetailsController(ureq, bwControl, stackPanel,
				entry.getStatementEntry(), assessedIdentity, details, entryIndex, tableModel.getRowCount(), selectedTool, false);
		listenTo(statementCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(displayName, statementCtrl);
	}
	
	private void doOpenTools(UserRequest ureq, CourseIdentityRow entry, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), entry);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		public ToolsController(UserRequest ureq, WindowControl wControl, CourseIdentityRow row) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("tool_courses");
			
			String url = BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + row.getRepositoryEntryKey() + "]");
			ExternalLink openCourseLink = LinkFactory.createExternalLink("open.course", translate("open.course"), url);
			openCourseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_content_popup");
			openCourseLink.setName(translate("open.course"));
			mainVC.put("open.course", openCourseLink);
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
