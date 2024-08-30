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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.NumOfCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskReviewAssignmentStatusCellRenderer;
import org.olat.course.nodes.gta.ui.peerreview.GTACoachPeerReviewTreeTableModel.CoachReviewCols;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantStatus;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * List of the reviews done by the assessed identity, the owner of the task.
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewAwardedListController extends AbstractCoachPeerReviewListController {

	private static final String CMD_TOOLS = "tools-A";
	private static final List<TaskReviewAssignmentStatus> STATUS_FOR_STATS = List.of(TaskReviewAssignmentStatus.done);

	private FormLink assignReviews;
	
	private final TaskList taskList;
	private final Identity reviewer;
	private final List<Identity> reviewers;
	
	private ToolsController toolsCtrl;
	private DialogBoxController confirmReopenPeerReviewCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private GTAPeerReviewsAssignmentController assignmentsCtrl;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	
	public GTACoachPeerReviewAwardedListController(UserRequest ureq, WindowControl wControl,
			TaskList taskList, Identity reviewer, CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, CMD_TOOLS, courseEnv, gtaNode);
		this.reviewer = reviewer;
		this.taskList = taskList;
		reviewers = null;
		
		initForm(ureq);
		loadModel();
	}
	
	public GTACoachPeerReviewAwardedListController(UserRequest ureq, WindowControl wControl,
			TaskList taskList, List<Identity> reviewers, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			Form rootForm) {
		super(ureq, wControl, CMD_TOOLS, courseEnv, gtaNode, rootForm);
		this.reviewers = reviewers;
		this.taskList = taskList;
		this.reviewer = null;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		assignReviews = uifactory.addFormLink("assign.reviews", formLayout, Link.BUTTON);
		assignReviews.setIconLeftCSS("o_icon o_icon_shuffle");
		
		assignReviews.setVisible(reviewer != null);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(new FullNameNodeRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.identityFullName, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.numOfReviews,
				new NumOfCellRenderer(reviewers != null, translate("warning.awarded.reviewers"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.plot));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.median));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.average));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sessionStatus,
				new TaskReviewAssignmentStatusCellRenderer(getLocale(), true)));
		if(reviewer == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.taskStepStatus,
					statusRenderer));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.submissionStatus,
					statusRenderer));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.review.view", translate("review.view"), "view"));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CoachReviewCols.tools);
		toolsCol.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_actions");
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new GTACoachPeerReviewTreeTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "reviews", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		initFilters();
		initFiltersPresets(ureq);
	}
	
	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(FILTER_UNSUFFICIENT_REVIEWS, translate("filter.unsufficient.reviews")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.unsufficient.reviews"),
				FILTER_UNSUFFICIENT_REVIEWS, statusValues, true));
	}

	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab insufficientTab = FlexiFiltersTabFactory.tabWithImplicitFilters(UNSUFFICIENT_REVIEWS_TAB_ID, translate("filter.unsufficient.reviews"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_UNSUFFICIENT_REVIEWS, List.of(FILTER_UNSUFFICIENT_REVIEWS))));
		tabs.add(insufficientTab);
	}

	@Override
	public void loadModel() {

		boolean enableAssignReviews = false;
		List<CoachPeerReviewRow> rows = new ArrayList<>();
		List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		Map<Identity,Task> identityToTask = tasks.stream()
				.collect(Collectors.toMap(Task::getIdentity, task -> task, (u, v) -> u));	
		
		if(reviewer != null) {
			List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsOfReviewer(taskList, reviewer);
			SessionParticipationListStatistics statistics = peerReviewManager.loadStatistics(taskList, assignments, reviewer, gtaNode, STATUS_FOR_STATS);
			CoachPeerReviewRow reviewerRow = loadModelRow(reviewer, assignments, statistics, identityToTask, rows);
			DueDate dueDate = gtaManager.getPeerReviewDueDate(reviewerRow.getTask(), reviewer, null, gtaNode, courseEntry, true);
			enableAssignReviews = (dueDate == null || dueDate.getDueDate() == null || new Date().before(dueDate.getDueDate()));
		} else {
			loadModelList(rows, identityToTask);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		assignReviews.setVisible(enableAssignReviews);
	}
	
	private void loadModelList(List<CoachPeerReviewRow> rows, Map<Identity,Task> identityToTask) {
		List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsForTaskList(taskList, false);
		Map<Identity,List<TaskReviewAssignment>> assigneeToAssignments = new HashMap<>();
		for(TaskReviewAssignment assignment:assignments) {
			List<TaskReviewAssignment> taskAssignments = assigneeToAssignments
					.computeIfAbsent(assignment.getAssignee(), t -> new ArrayList<>());
			taskAssignments.add(assignment);
		}
		
		Map<Identity, SessionParticipationListStatistics> assigneeToStatistics = peerReviewManager
				.loadStatisticsProAssignee(taskList, assignments, gtaNode, STATUS_FOR_STATS);
		
		for(Identity reviewerIdentity:reviewers) {
			List<TaskReviewAssignment> assigneeAssignments = assigneeToAssignments.get(reviewerIdentity);
			if(assigneeAssignments == null) {
				assigneeAssignments = new ArrayList<>();
			}
			SessionParticipationListStatistics statistics = assigneeToStatistics.get(reviewerIdentity);
			if(statistics == null) {
				statistics = SessionParticipationListStatistics.noStatistics();
			}
			loadModelRow(reviewerIdentity, assigneeAssignments, statistics, identityToTask, rows);
		}
	}
	
	private CoachPeerReviewRow loadModelRow(Identity reviewerIdentity, List<TaskReviewAssignment> assignments,
			SessionParticipationListStatistics statistics, Map<Identity,Task> identityToTask, List<CoachPeerReviewRow> rows) {
		String reviewerFullName = userManager.getUserDisplayName(reviewerIdentity);
		Task reviewerOwnTask = identityToTask.get(reviewerIdentity);
		CoachPeerReviewRow surveyExecutorIdentityRow = new CoachPeerReviewRow(reviewerOwnTask, reviewerIdentity, reviewerFullName);
		List<CoachPeerReviewRow> sessionRows = new ArrayList<>();
		rows.add(surveyExecutorIdentityRow);
		surveyExecutorIdentityRow.setChildrenRows(sessionRows);
		
		Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap = statistics.toParticipationsMap();
		
		for(TaskReviewAssignment assignment : assignments) {
			SessionParticipationStatistics sessionStatistics = null;
			if(assignment.getParticipation() != null) {
				sessionStatistics = statisticsMap.get(assignment.getParticipation());
			}
			CoachPeerReviewRow sessionRow = forgeSessionRow(assignment, sessionStatistics);
			sessionRow.setParent(surveyExecutorIdentityRow);
			rows.add(sessionRow);
			sessionRows.add(sessionRow);
		}
		
		// Fill statistics
		forgeSurveyExecutorIdentityRow(surveyExecutorIdentityRow, statistics.aggregatedStatistics(), reviewerOwnTask);
		return surveyExecutorIdentityRow;
	}
	
	private void forgeSurveyExecutorIdentityRow(CoachPeerReviewRow surveyExecutorIdentityRow, SessionStatistics aggregatedStatistics, Task reviewerOwnTask) {
		decorateWithAggregatedStatistics(surveyExecutorIdentityRow, aggregatedStatistics);
		decorateWithTools(surveyExecutorIdentityRow);
		decorateWithStepStatus(surveyExecutorIdentityRow, reviewerOwnTask);
		
		CoachedParticipantStatus submissionStatus = statusRenderer
				.calculateSubmissionStatus(surveyExecutorIdentityRow.getIdentity(), reviewerOwnTask);
		surveyExecutorIdentityRow.setSubmissionStatus(submissionStatus);
	}
	
	private CoachPeerReviewRow forgeSessionRow(TaskReviewAssignment assignment, SessionParticipationStatistics sessionStatistics) {
		Identity assessedIdentity = assignment.getTask().getIdentity();
		String assessedIdentityFullName = userManager.getUserDisplayName(assessedIdentity);
		Task task = assignment.getTask();
		CoachPeerReviewRow sessionRow = new CoachPeerReviewRow(task, assignment, assessedIdentity, assessedIdentityFullName, false);

		decorateWithStatistics(sessionRow, sessionStatistics);
		decorateWithTools(sessionRow);
		return sessionRow;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assignmentsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(confirmReopenPeerReviewCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CoachPeerReviewRow row = (CoachPeerReviewRow)confirmReopenPeerReviewCtrl.getUserObject();
				doReopenPeerReview(row);
			}
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(assignmentsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutCtrl = null;
		assignmentsCtrl = null;
		toolsCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assignReviews == source) {
			doAssignReviews(ureq, tableModel.getObject(0));
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				CoachPeerReviewRow row = tableModel.getObject(se.getIndex());
				if(row.getParent() == null) {
					doCompareEvaluationFormSessions(ureq, row);
				} else {
					doViewEvaluationFormSession(ureq, row);
				}
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof CoachPeerReviewRow row
				&& CMD_TOOLS.equals(link.getCmd())) {
			doOpenTools(ureq, row, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmReopenPeerReview(UserRequest ureq, CoachPeerReviewRow row) {
		String toName = row.getFullName();			
		String title = translate("coach.reopen.peerreview.confirm.title");
		String text = translate("coach.reopen.peerreview.confirm.text", toName);
		text = "<div class='o_warning'>" + text + "</div>";
		confirmReopenPeerReviewCtrl = activateOkCancelDialog(ureq, title, text, confirmReopenPeerReviewCtrl);
		confirmReopenPeerReviewCtrl.setUserObject(row);
		listenTo(confirmReopenPeerReviewCtrl);
	}
	
	private void doReopenPeerReview(CoachPeerReviewRow row) {
		if(row.getParent() != null || row.getTask() == null) return;
		
		Task task = row.getTask();
		task = gtaManager.updateTask(task, TaskProcess.peerreview, gtaNode, false, getIdentity(), Role.coach);
		Identity assessedIdentity = task.getIdentity();
		
		gtaManager.log("Reopen peer-review", "revert status of task to peer-review", task,
				getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
		
		loadModel();
	}

	private void doOpenTools(UserRequest ureq, CoachPeerReviewRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doAssignReviews(UserRequest ureq, CoachPeerReviewRow row) {
		if(row.getParent() != null) return;
		
		assignmentsCtrl = new GTAPeerReviewsAssignmentController(ureq, getWindowControl(),
				taskList, row.getIdentity(), row.getTask(), courseEntry, gtaNode);
		listenTo(assignmentsCtrl);
		
		String title = translate("review.assignment.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				assignmentsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class ToolsController extends BasicController {
		private Link showReviewLink;
		private Link showReviewsLink;
		private Link assignReviewLink;
		private Link invalidateReviewLink;
		private Link reopenReviewLink;
		private Link reopenPeerReviewLink;
		private VelocityContainer mainVC;
		
		private CoachPeerReviewRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CoachPeerReviewRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
	
			if(row.getParent() == null) {
				showReviewsLink = LinkFactory.createLink("show.reviews", "show.reviews", getTranslator(), mainVC, this, Link.LINK);
				showReviewsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");

				DueDate dueDate = gtaManager.getPeerReviewDueDate(row.getTask(), row.getIdentity(), null, gtaNode, courseEntry, true);
				if(dueDate == null || dueDate.getDueDate() == null || ureq.getRequestTimestamp().before(dueDate.getDueDate())) {
					assignReviewLink = LinkFactory.createLink("assign.reviews", "assign.reviews", getTranslator(), mainVC, this, Link.LINK);
					assignReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_shuffle");
				}

				if(row.getStepStatus() == CoachedParticipantStatus.done
						&& (dueDate == null || dueDate.getDueDate() == null || ureq.getRequestTimestamp().before(dueDate.getDueDate()))) {
					reopenPeerReviewLink = LinkFactory.createLink("coach.reopen.peerreview", "coach.reopen.peerreview", getTranslator(), mainVC, this, Link.LINK);
					reopenPeerReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
				}
			} else {
				showReviewLink = LinkFactory.createLink("show.review", "show.review", getTranslator(), mainVC, this, Link.LINK);
				showReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");
				
				invalidateReviewLink = LinkFactory.createLink("invalidate.review", "invalidate.review", getTranslator(), mainVC, this, Link.LINK);
				invalidateReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_invalidate");
				
				DueDate peerReviewDueDate = gtaManager.getPeerReviewDueDate(row.getTask(), row.getAssignee(), null, gtaNode, courseEntry, true);
				if(row.getAssignmentStatus() == TaskReviewAssignmentStatus.done
						&& (peerReviewDueDate == null || peerReviewDueDate.getDueDate() == null || ureq.getRequestTimestamp().before(peerReviewDueDate.getDueDate()))) {
					reopenReviewLink = LinkFactory.createLink("reopen.review", "reopen.review", getTranslator(), mainVC, this, Link.LINK);
					reopenReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
				}
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(showReviewsLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doCompareEvaluationFormSessions(ureq, row);
			} else if(showReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doViewEvaluationFormSession(ureq, row);
			} else if(assignReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doAssignReviews(ureq, row);
			} else if(invalidateReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doInvalidateReview(ureq, row);
			} else if(reopenReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doReopenReview(ureq, row);
			} else if(reopenPeerReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doConfirmReopenPeerReview(ureq, row);
			}
		}
	}
}
