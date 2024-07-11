/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.workflow;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Passed;
import org.olat.course.assessment.ui.tool.AssessmentForm;
import org.olat.course.assessment.ui.tool.UserVisibilityCellRenderer;
import org.olat.course.assessment.ui.tool.tools.ResetAttemptsConfirmationController;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.TaskColorizedScoreCellRenderer;
import org.olat.course.nodes.gta.ui.events.AssessTaskEvent;
import org.olat.course.nodes.gta.ui.events.ReopenEvent;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 28 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachGradingListController extends AbstractCoachWorkflowListController implements FlexiTableComponentDelegate {

	private final VelocityContainer detailsVC;
	
	private FormLink bulkDoneButton;
	private FormLink bulkVisibleButton;
	private FormLink bulkHiddenButton;
	private FormLink bulkDownloadButton;
	private FormLink bulkReopenButton;
	
	private AssessmentForm assessmentCtrl;
	private ResetAttemptsConfirmationController resetAttemptsConfirmationCtrl;
	
	public GTACoachGradingListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "grading_list", coachCourseEnv, assessableIdentities, gtaNode);

		detailsVC = createVelocityContainer("grading_details");
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
 
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}

	@Override
	protected void initConfigurationInfos(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder();
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD)) {
			String cutValue = AssessmentHelper.getRoundedScore(gtaNode.getModuleConfiguration().getFloatEntry(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE));
			if(StringHelper.containsNonWhitespace(cutValue)) {
				String cutValueInfos = translate("workflow.infos.cut.value", cutValue);
				infos.append("<p><i class='o_icon o_icon-fw o_icon_success_status'> </i> ").append(cutValueInfos).append("</p>");
			}
		}
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD)) {
			String maxValue = AssessmentHelper.getRoundedScore(gtaNode.getModuleConfiguration().getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX));
			String maxValueInfos = translate("workflow.infos.max.value", maxValue);
			infos.append("<p><i class='o_icon o_icon-fw o_icon_success_status'> </i> ").append(maxValueInfos).append("</p>");
		}
		
		panel.setInformations(infos.toString());
	}

	@Override
	protected void initColumnsModel(FlexiTableColumnModel columnsModel) {
		if(Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.userVisibility, new UserVisibilityCellRenderer(false)));
		}
		if(Mode.none != assessmentConfig.getScoreMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.score, new TaskColorizedScoreCellRenderer()));
		}
		if(Mode.none != assessmentConfig.getPassedMode()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.passed, new PassedCellRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.assessmentDone));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues reviewedValues = new SelectionValues();
		reviewedValues.add(SelectionValues.entry(FILTER_TO_RELEASE, translate("filter.to.release")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.to.release"),
				FILTER_TO_RELEASE, reviewedValues, true));

		if(Mode.none != assessmentConfig.getPassedMode()) {
			SelectionValues passedValues = new SelectionValues();
			passedValues.add(SelectionValues.entry(Passed.passed.name(), translate("filter.passed")));
			passedValues.add(SelectionValues.entry(Passed.failed.name(), translate("filter.failed")));
			passedValues.add(SelectionValues.entry(Passed.notGraded.name(), translate("filter.nopassed")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.passed.label"),
					FILTER_PASSED, passedValues, true));
		}
	
		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.open.name(), translate(CoachedParticipantStatus.open.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.done.name(), translate(CoachedParticipantStatus.done.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}

	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithImplicitFilters(OPEN_TAB_ID, translate(CoachedParticipantStatus.open.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.open.name()))));
		tabs.add(openTab);
		
		FlexiFiltersTab toReleaseTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TO_RELEASE_TAB_ID, translate("filter.to.release"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_TO_RELEASE, List.of(FILTER_TO_RELEASE))));
		tabs.add(toReleaseTab);
		
		if(Mode.none != assessmentConfig.getPassedMode()) {
			FlexiFiltersTab passedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PASSED_TAB_ID, translate("filter.passed"),
					TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_PASSED, "passed")));
			tabs.add(passedTab);
			
			FlexiFiltersTab failedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FAILED_TAB_ID, translate("filter.failed"),
					TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_PASSED, "failed")));
			tabs.add(failedTab);
		}
		
		FlexiFiltersTab notAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(notAvailableTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(doneTab);
	}
	
	@Override
	protected void initBulkTools(FormItemContainer formLayout) {
		bulkDoneButton = uifactory.addFormLink("bulk.done", formLayout, Link.BUTTON);
		bulkDoneButton.setElementCssClass("o_sel_assessment_bulk_done");
		bulkDoneButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
		bulkDoneButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkDoneButton);
		
		boolean canChangeUserVisibility = assessmentCallback.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		
		if (canChangeUserVisibility) {
			bulkVisibleButton = uifactory.addFormLink("bulk.visible", formLayout, Link.BUTTON);
			bulkVisibleButton.setElementCssClass("o_sel_assessment_bulk_visible");
			bulkVisibleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_results_visible");
			bulkVisibleButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkVisibleButton);
			
			bulkHiddenButton = uifactory.addFormLink("bulk.hidden", formLayout, Link.BUTTON);
			bulkHiddenButton.setElementCssClass("o_sel_assessment_bulk_hidden");
			bulkHiddenButton.setIconLeftCSS("o_icon o_icon-fw o_icon_results_hidden");
			bulkHiddenButton.setVisible(!coachCourseEnv.isCourseReadOnly());
			tableEl.addBatchButton(bulkHiddenButton);
		}
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			bulkDownloadButton = uifactory.addFormLink("batch.download", "bulk.download.title", null, formLayout, Link.BUTTON);
			bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			tableEl.addBatchButton(bulkDownloadButton);
		}
		
		bulkReopenButton = uifactory.addFormLink("bulk.reopen", formLayout, Link.BUTTON);
		bulkReopenButton.setElementCssClass("o_sel_assessment_bulk_reopen");
		bulkReopenButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
		bulkReopenButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkReopenButton);

		super.initBulkTools(formLayout);
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof CoachedParticipantRow participantRow) {
			if(participantRow.getDetailsControllerEl() != null) {
				components.add(participantRow.getDetailsControllerEl().getComponent());
			}
		}
		return components;
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		
		status(identityRow);
		return identityRow;
	}
	
	private void status(CoachedParticipantRow identityRow) {
		Task assignedTask = identityRow.getTask();
		
		if(assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.graded) {
			identityRow.setStatus(CoachedParticipantStatus.done);
		} else {
			identityRow.setStatus(CoachedParticipantStatus.open);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof GTACoachGradingDetailsController detailsCtrl) {
			if(event instanceof ReopenEvent) {
				doReopen(detailsCtrl.getRow());
			} else if(event instanceof AssessTaskEvent) {
				doOpenAssessment(ureq, detailsCtrl.getRow());
			}
		} else if(assessmentCtrl == source) {
			if(event instanceof AssessmentFormEvent afe) {
				if(AssessmentFormEvent.ASSESSMENT_DONE.equals(afe.getCommand())) {
					doSetTaskDone(assessmentCtrl.getAssessedIdentity());
				} else if(AssessmentFormEvent.ASSESSMENT_REOPEN.equals(afe.getCommand())) {
					doReopenTask(assessmentCtrl.getAssessedIdentity());
				}
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(resetAttemptsConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event instanceof AssessmentFormEvent) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(resetAttemptsConfirmationCtrl);
		resetAttemptsConfirmationCtrl = null;
		super.cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bulkDoneButton == source) {
			doBulkSetDone();
		} else if(bulkVisibleButton == source) {
			doBulkSetUserVisibility(true);
		} else if(bulkHiddenButton == source) {
			doBulkSetUserVisibility(false);
		} else if(bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if(bulkReopenButton == source) {
			doBulkReopen();
		} else if(tableEl == source) {
			if(event instanceof DetailsToggleEvent toggleEvent) {
				CoachedParticipantRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenDetails(ureq, row);
				} else {
					doCloseDetails(row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenDetails(UserRequest ureq, CoachedParticipantRow row) {
		GTACoachGradingDetailsController gradingDetailsCtrl = new GTACoachGradingDetailsController(ureq, getWindowControl(), row,
				assessmentConfig, courseEnv, gtaNode, mainForm);
		listenTo(gradingDetailsCtrl);
		row.setDetailsCtrl(gradingDetailsCtrl);
		flc.add(gradingDetailsCtrl.getInitialFormItem());
		row.setDetailsControllerEl(gradingDetailsCtrl.getInitialFormItem());
	}
	
	private void doCloseDetails(CoachedParticipantRow row) {
		if(row.getDetailsCtrl() == null) return;
		
		removeAsListenerAndDispose(row.getDetailsCtrl());
		flc.remove(row.getDetailsControllerEl());
		row.setDetailsCtrl(null);
	}
	
	private void doResetAttempts(UserRequest ureq, CoachedParticipantRow row) {
		resetAttemptsConfirmationCtrl = new ResetAttemptsConfirmationController(ureq, getWindowControl(),
				courseEnv, gtaNode, row.getAssessedIdentity());
		listenTo(resetAttemptsConfirmationCtrl);
		
		String title = translate("tool.reset.attempts");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), resetAttemptsConfirmationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetVisibility(CoachedParticipantRow row, boolean visible) {
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(row.getAssessedIdentity(), course);
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
		
		if (scoreEval != null) {
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getWeightedScore(),
					scoreEval.getScoreScale(), scoreEval.getGrade(),
					scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
					scoreEval.getAssessmentStatus(), Boolean.valueOf(visible), scoreEval.getCurrentRunStartDate(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(gtaNode, doneEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
		}
		
		loadModel();
	}
	
	private void doBulkReopen() {
		List<CoachedParticipantRow> rows = getSelectedRows(row -> row.getAssessmentStatus() == AssessmentEntryStatus.done);
		if(rows.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			for(CoachedParticipantRow row:rows) {
				doReopen(row);
			}
		}
	}
	
	private void doReopen(CoachedParticipantRow row) {
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(row.getAssessedIdentity(), course);
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
		
		if (scoreEval != null) {
			ScoreEvaluation reopenedEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getWeightedScore(),
					scoreEval.getScoreScale(), scoreEval.getGrade(),
					scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
					AssessmentEntryStatus.inReview, scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
					scoreEval.getCurrentRunCompletion(), AssessmentRunStatus.running, scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(gtaNode, reopenedEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
			
			doReopenTask(row.getAssessedIdentity());
		}
		
		loadModel();
	}
	
	private void doReopenTask(Identity assessedIdentity) {
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.getTaskList(courseEntry, gtaNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && task.getTaskStatus() == TaskProcess.graded) {
			gtaManager.updateTask(task, TaskProcess.grading, gtaNode, false, getIdentity(), Role.coach);
		}
	}
	
	private void doSetDone(CoachedParticipantRow row) {
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(row.getAssessedIdentity(), course);
		ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
		
		if (scoreEval != null) {
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getWeightedScore(),
					scoreEval.getScoreScale(), scoreEval.getGrade(),
					scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
					AssessmentEntryStatus.done, scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(gtaNode, doneEval, assessedUserCourseEnv, getIdentity(),
					false, Role.coach);
			
			doSetTaskDone(row.getAssessedIdentity());
		}

		loadModel();
	}
	
	private void doSetTaskDone(Identity assessedIdentity) {
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.getTaskList(courseEntry, gtaNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null) {
			gtaManager.updateTask(task, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
		}
	}
	
	private void doBulkSetDone() {
		List<CoachedParticipantRow> rows = getSelectedRows(row -> row.getAssessmentStatus() != AssessmentEntryStatus.done);
		if(rows.isEmpty()) {
			showWarning("warning.bulk.done");
		} else if(assessmentConfig.isAssessable()) {
			ICourse course = CourseFactory.loadCourse(courseEnv.getCourseGroupManager().getCourseEntry());
			
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
			if(taskList == null) {
				taskList = gtaManager.createIfNotExists(entry, gtaNode);
			}
			
			for(CoachedParticipantRow row:rows) {
				Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
				doSetStatus(assessedIdentity, AssessmentEntryStatus.done, gtaNode, taskList, course);
				dbInstance.commitAndCloseSession();
			}
			loadModel();
		}
	}
	
	private void doBulkSetUserVisibility(boolean visible) {
		List<Identity> rows = getSelectedIdentities(row -> true);
		if(rows.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			RepositoryEntry courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			ICourse course = CourseFactory.loadCourse(courseEntry);
			Boolean visibility = Boolean.valueOf(visible);
			rows.forEach(identity -> doSetUserVisibility(course, identity, visibility));
			loadModel();
		}
	}
	
	private void doSetUserVisibility(ICourse course, Identity assessedIdentity, Boolean userVisibility) {
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(gtaNode, assessedUserCourseEnv);
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getWeightedScore(),
				scoreEval.getScoreScale(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), userVisibility, scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(gtaNode, doneEval, assessedUserCourseEnv, getIdentity(),
				false, Role.coach);
		dbInstance.commitAndCloseSession();
	}
	
	private void doBulkDownload(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities(row -> true);
		if(identities.isEmpty()) {
			showWarning("warning.bulk.empty");
		} else {
			ArchiveOptions options = new ArchiveOptions();
			options.setIdentities(identities);
			options.setOnlySubmitted(true);
			
			OLATResource courseOres = coachCourseEnv.getCourseEnvironment()
					.getCourseGroupManager().getCourseResource();
			ArchiveResource resource = new ArchiveResource(gtaNode, courseOres, options, getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doOpenAssessment(UserRequest ureq, CoachedParticipantRow row) {
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(row.getAssessedIdentity(), course);

		assessmentCtrl = new AssessmentForm(ureq, getWindowControl(), gtaNode, coachCourseEnv, assessedUserCourseEnv);
		listenTo(assessmentCtrl);
		
		String title = translate("tool.assessment");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), assessmentCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new GradingToolsController(ureq, getWindowControl(), row);
	}
	
	private class GradingToolsController extends BasicController {
		
		private Link setDoneLink;
		private Link reopenLink;
		private Link visibleLink;
		private Link notVisibleLink;
		private Link resetAttemptsButton;
		
		private CoachedParticipantRow row;
		
		public GradingToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			if(row.getAssessmentStatus() == AssessmentEntryStatus.done) {
				reopenLink = LinkFactory.createLink("tool.reopen", "tool.reopen", getTranslator(), mainVC, this, Link.LINK);
				reopenLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			} else {
				setDoneLink = LinkFactory.createLink("tool.set.done", "tool.set.done", getTranslator(), mainVC, this, Link.LINK);
				setDoneLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_done");
			}
			
			// result as visible / not visible
			boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
					|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
			if (canChangeUserVisibility) {
				if(row.getUserVisibility() != null && row.getUserVisibility().booleanValue()) {
					notVisibleLink = LinkFactory.createLink("tool.set.hidden", "tool.set.hidden", getTranslator(), mainVC, this, Link.LINK);
					notVisibleLink.setIconLeftCSS("o_icon o_icon-fw o_icon_results_hidden");
				} else {
					visibleLink = LinkFactory.createLink("tool.set.visible", "tool.set.visible", getTranslator(), mainVC, this, Link.LINK);
					visibleLink.setIconLeftCSS("o_icon o_icon-fw o_icon_results_visible");
				}
			}
			
			resetAttemptsButton = LinkFactory.createLink("reset.attempts", "reset.attempts", getTranslator(), mainVC, this, Link.LINK);
			resetAttemptsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reset");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(resetAttemptsButton == source) {
				doResetAttempts(ureq, row);
			} else if(notVisibleLink == source) {
				doSetVisibility(row, false);
			} else if(visibleLink == source) {
				doSetVisibility(row, true);
			} else if(reopenLink == source) {
				doReopen(row);
			} else if(setDoneLink == source) {
				doSetDone(row);
			}
		}
	}
}
