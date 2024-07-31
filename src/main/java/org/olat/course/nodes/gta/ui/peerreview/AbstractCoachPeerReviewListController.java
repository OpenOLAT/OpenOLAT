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
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
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
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.peerreview.CoachPeerReviewRow.NumOf;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantStatus;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.portfolio.ui.MultiEvaluationFormController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractCoachPeerReviewListController extends FormBasicController {

	public static final String ALL_TAB_ID = "All";
	public static final String OPEN_TAB_ID = "Open";
	public static final String IN_PROGRESS_TAB_ID = "InProgress";
	public static final String DONE_TAB_ID = "Done";
	public static final String INVALID_TAB_ID = "Invalid";
	public static final String UNSUFFICIENT_REVIEWS_TAB_ID = "UnsufficientReviews";
	public static final String UNSUFFICIENT_REVIEWERS_TAB_ID = "UnsufficientReviewers";
	public static final String STEP_OPEN_TAB_ID = "StepOpen";
	public static final String STEP_NOT_AVAILABLE_TAB_ID = "StepNotAvailable";
	public static final String STEP_DONE_TAB_ID = "StepDone";
	
	public static final String FILTER_ASSIGNMENT_SESSION_STATUS = "assignment-session-status";
	public static final String FILTER_ASSIGNMENT_STEP_STATUS = "assignment-step-status";
	public static final String FILTER_UNSUFFICIENT_REVIEWERS = "assignment-unsufficient-reviewers";
	public static final String FILTER_UNSUFFICIENT_REVIEWS = "assignment-unsufficient-reviews";
	
	private FlexiFiltersTab allTab;
	protected FlexiTableElement tableEl;
	protected GTACoachPeerReviewTreeTableModel tableModel;
	
	protected int counter = 0;
	private final String toolsCmd;
	protected final int numOfReviews;
	protected final GTACourseNode gtaNode;
	protected final RepositoryEntry courseEntry;
	private final EvaluationFormSurvey survey;
	protected final CourseEnvironment courseEnv;
	
	protected CloseableModalController cmc;
	private MultiEvaluationFormController multiEvaluationFormCtrl;
	private GTAEvaluationFormExecutionController evaluationFormExecCtrl;
	private ConfirmInvalidateReviewController confirmInvalidateReviewCtrl;
	private ConfirmReopenReviewController confirmReopenReviewCtrl;

	@Autowired
	private GTAManager gtaManager;
	@Autowired
	protected GTAPeerReviewManager peerReviewManager;
	
	public AbstractCoachPeerReviewListController(UserRequest ureq, WindowControl wControl, String toolsCmd,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "coach_peer_review_list", Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		this.courseEnv = courseEnv;
		this.gtaNode = gtaNode;
		this.toolsCmd = toolsCmd;
		
		String numOfReviewsVal = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		numOfReviews = Integer.parseInt(numOfReviewsVal);
		
		survey = peerReviewManager.loadOrCreateSurvey(courseEntry, gtaNode, getIdentity());
	}
	
	public AbstractCoachPeerReviewListController(UserRequest ureq, WindowControl wControl, String toolsCmd,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "coach_peer_review_list", rootForm);
		setTranslator(Util.createPackageTranslator(GTACoachController.class, getLocale(), getTranslator()));
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		this.courseEnv = courseEnv;
		this.gtaNode = gtaNode;
		this.toolsCmd = toolsCmd;
		
		String numOfReviewsVal = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		numOfReviews = Integer.parseInt(numOfReviewsVal);
		
		survey = peerReviewManager.loadOrCreateSurvey(courseEntry, gtaNode, getIdentity());
	}
	
	protected boolean isSumConfigured() {
		String scoreKey = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
		return MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey);
	}
	
	protected boolean isAverageConfigured() {
		String scoreKey = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_EVAL_FORM);
		return MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);

		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(TaskReviewAssignmentStatus.open.name(), translate("filter.assignment.status.open")));
		assignmentStatusPK.add(SelectionValues.entry(TaskReviewAssignmentStatus.inProgress.name(), translate("filter.assignment.status.in.progress")));
		assignmentStatusPK.add(SelectionValues.entry(TaskReviewAssignmentStatus.done.name(), translate("filter.assignment.status.done")));
		assignmentStatusPK.add(SelectionValues.entry(TaskReviewAssignmentStatus.invalidate.name(), translate("filter.assignment.status.invalid")));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_ASSIGNMENT_SESSION_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
		
		SelectionValues stepStatusPK = new SelectionValues();
		stepStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.waiting.name(), translate(CoachedParticipantStatus.waiting.i18nKey())));
		stepStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.done.name(), translate(CoachedParticipantStatus.done.i18nKey())));
		stepStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter stepStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.step.status"),
				FILTER_ASSIGNMENT_STEP_STATUS, stepStatusPK, true);
		filters.add(stepStatusFilter);
		
		initFilters(filters);
	
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected abstract void initFilters(List<FlexiTableExtendedFilter> filters);
	
	protected void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithImplicitFilters(OPEN_TAB_ID, translate("filter.assignment.status.open"),
					TabSelectionBehavior.clear, List.of(
							FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_SESSION_STATUS, List.of(TaskReviewAssignmentStatus.open.name()))));
		tabs.add(openTab);
		
		FlexiFiltersTab inProgressTab = FlexiFiltersTabFactory.tabWithImplicitFilters(IN_PROGRESS_TAB_ID, translate("filter.assignment.status.in.progress"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_SESSION_STATUS, List.of(TaskReviewAssignmentStatus.inProgress.name()))));
		tabs.add(inProgressTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate("filter.assignment.status.done"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_SESSION_STATUS, List.of(TaskReviewAssignmentStatus.done.name()))));
		tabs.add(doneTab);
		
		FlexiFiltersTab invalidTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INVALID_TAB_ID, translate("filter.assignment.status.invalid"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_SESSION_STATUS, List.of(TaskReviewAssignmentStatus.invalidate.name()))));
		tabs.add(invalidTab);
		
		initFiltersPresets(ureq, tabs);
		
		FlexiFiltersTab stepOpenTab = FlexiFiltersTabFactory.tabWithImplicitFilters(STEP_OPEN_TAB_ID, translate(CoachedParticipantStatus.waiting.i18nKey()),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_STEP_STATUS, List.of(CoachedParticipantStatus.waiting.name()))));
		tabs.add(stepOpenTab);
		
		FlexiFiltersTab stepNotAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(STEP_NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_STEP_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(stepNotAvailableTab);
		
		FlexiFiltersTab stepDoneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(STEP_DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_STEP_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(stepDoneTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	protected abstract void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs);
	
	protected void decorateWithAggregatedStatistics(CoachPeerReviewRow aggreagtedRow, SessionStatistics aggregatedStatistics) {
		List<CoachPeerReviewRow> subRows = aggreagtedRow.getChildrenRows();

		aggreagtedRow.setNumOfReviews(new NumOf(subRows.size(), numOfReviews));
		aggreagtedRow.setNumOfReviewers(aggreagtedRow.getNumOfReviews());
		
		double progress = aggregatedStatistics == null ? 0.0d : aggregatedStatistics.progress() * 100d;
		if(progress > 0.0d && aggregatedStatistics != SessionStatistics.NO_STATISTICS) {
			String id = Integer.toString(counter++);
			createBoxPlot(id, aggreagtedRow, aggregatedStatistics);
		}
	}

	protected void decorateWithStatistics(CoachPeerReviewRow row, SessionParticipationStatistics sessionStatistics) {
		if(sessionStatistics != null && sessionStatistics.statistics() != null
				&& sessionStatistics.statistics() != SessionStatistics.NO_STATISTICS) {
			SessionStatistics statistics = sessionStatistics.statistics();
			double progress = statistics.progress() * 100d;
			if(progress > 0.0d) {
				String id = Integer.toString(counter++);
				createBoxPlot(id, row, statistics);
			}
		}
	}
	
	protected BoxPlot createBoxPlot(String id, CoachPeerReviewRow row, SessionStatistics statistics) {
		double firstQuartile = 0.0d;
		double thirdQuartile = 0.0d;
		double median = 0.0d;
		
		double min = statistics.min();
		double max = statistics.max();
		
		double average = statistics.average();
		row.setAverage(Double.valueOf(average));
		row.setSum(Double.valueOf(statistics.sum()));
		row.setMedian(Double.valueOf(statistics.median()));

		// feed the box
		if(statistics.numOfQuestions() > 10) {
			firstQuartile = statistics.firstQuartile();
			median = statistics.median();
			thirdQuartile = statistics.thirdQuartile();
		}
		
		BoxPlot assessmentsPlot = new BoxPlot("plot-assessments-".concat(id), statistics.maxSteps(),
				(float)min, (float)max, (float)average,
				(float)firstQuartile, (float)thirdQuartile, (float)median, null);
		row.setAssessmentPlot(assessmentsPlot);
		return assessmentsPlot;
	}
	
	protected void decorateWithTools(CoachPeerReviewRow row) {
		// tools
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, toolsCmd, "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		
		row.setToolsLink(toolsLink);
	}
	
	protected void decorateWithStepStatus(CoachPeerReviewRow row, Task identityTask) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(identityTask == null || identityTask.getTaskStatus() == TaskProcess.assignment || identityTask.getTaskStatus() == TaskProcess.submit
					|| identityTask.getTaskStatus() == TaskProcess.review || identityTask.getTaskStatus() == TaskProcess.correction
					|| identityTask.getTaskStatus() == TaskProcess.revision) {

				row.setStepStatus(CoachedParticipantStatus.notAvailable);
			} else if(identityTask.getTaskStatus() == TaskProcess.peerreview) {
				if(isPeerReviewStarted(identityTask)) {
					row.setStepStatus(CoachedParticipantStatus.waiting);
				} else {
					row.setStepStatus(CoachedParticipantStatus.notAvailable);
				}
			} else {
				row.setStepStatus(CoachedParticipantStatus.done);
			}
		} else if(identityTask == null || identityTask.getTaskStatus() == TaskProcess.peerreview) {
			if(identityTask != null && isPeerReviewStarted(identityTask)) {
				row.setStepStatus(CoachedParticipantStatus.waiting);
			} else {
				row.setStepStatus(CoachedParticipantStatus.notAvailable);
			}
		} else {
			row.setStepStatus(CoachedParticipantStatus.done);
		}
	}
	
	protected final boolean isPeerReviewStarted(Task assignedTask) {
		DueDate availableDate = gtaManager.getPeerReviewDueDate(assignedTask, assignedTask.getIdentity(), null, gtaNode, courseEntry, true);
		return availableDate == null || availableDate.getStartDate() == null ||
				(availableDate.getStartDate() != null && availableDate.getStartDate().compareTo(new Date()) <= 0);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(evaluationFormExecCtrl == source || multiEvaluationFormCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(confirmInvalidateReviewCtrl == source || confirmReopenReviewCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmInvalidateReviewCtrl);
		removeAsListenerAndDispose(multiEvaluationFormCtrl);
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		removeAsListenerAndDispose(cmc);
		confirmInvalidateReviewCtrl = null;
		multiEvaluationFormCtrl = null;
		evaluationFormExecCtrl = null;
		cmc = null;
	}
	
	protected abstract void loadModel();

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(event instanceof FlexiTableSearchEvent
				||event instanceof FlexiTableFilterTabEvent) {
			tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
			tableEl.reset(true, true, true);
		}
	}
	
	protected void doCompareEvaluationFormSessions(UserRequest ureq, CoachPeerReviewRow row) {
		List<Identity> otherEvaluators = row.getChildrenRows().stream()
				.map(CoachPeerReviewRow::getAssignee)
				.collect(Collectors.toList());
			
		multiEvaluationFormCtrl = new MultiEvaluationFormController(ureq, getWindowControl(), row.getAssignee(),
				otherEvaluators, survey, false, true, true, false);
		
		String title = translate("review.assessment.all.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				multiEvaluationFormCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doViewEvaluationFormSession(UserRequest ureq, CoachPeerReviewRow row) {
		TaskReviewAssignment assignment = row.getAssignment();
		GTAEvaluationFormExecutionOptions options = GTAEvaluationFormExecutionOptions.valueOf(false, false, false, null, false, false, false);
		evaluationFormExecCtrl = new GTAEvaluationFormExecutionController(ureq, getWindowControl(), assignment,
				courseEnv, gtaNode, options, false, false);
		listenTo(evaluationFormExecCtrl);
		
		String title = translate("review.assessment.title", row.getFullName());
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				evaluationFormExecCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doInvalidateReview(UserRequest ureq, CoachPeerReviewRow row) {
		TaskReviewAssignment assignment = row.getAssignment();
		confirmInvalidateReviewCtrl = new ConfirmInvalidateReviewController(ureq, getWindowControl(), assignment, courseEnv, gtaNode);
		listenTo(confirmInvalidateReviewCtrl);
		
		String title = row.getFullName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				confirmInvalidateReviewCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doReopenReview(UserRequest ureq, CoachPeerReviewRow row) {
		TaskReviewAssignment assignment = row.getAssignment();
		confirmReopenReviewCtrl = new ConfirmReopenReviewController(ureq, getWindowControl(), assignment, courseEnv, gtaNode);
		listenTo(confirmReopenReviewCtrl);
		
		String title = row.getFullName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				confirmReopenReviewCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
