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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.NumOfCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskReviewAssignmentStatusCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskStepStatusCellRenderer;
import org.olat.course.nodes.gta.ui.peerreview.GTACoachPeerReviewTreeTableModel.CoachReviewCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * List of reviews done by other for the assigned task.
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewReceivedListController extends AbstractCoachPeerReviewListController {
	
	private static final String CMD_TOOLS = "tools-R";
	private static final List<TaskReviewAssignmentStatus> STATUS_FOR_STATS = List.of(TaskReviewAssignmentStatus.done);
	
	private FormLink assignReviewers;
	private BreadcrumbPanel stackPanel;

	private final TaskList taskList;
	private final Task assignedTask;
	private final List<Identity> assessedIdentities;

	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private GTAPeerReviewAssignmentController assignmentsCtrl;
	private GTAEvaluationFormExecutionController evaluationFormExecCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	
	public GTACoachPeerReviewReceivedListController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, TaskList taskList, Task assignedTask,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, CMD_TOOLS, courseEnv, gtaNode);
		this.assignedTask = assignedTask;
		this.stackPanel = stackPanel;
		this.taskList = taskList;
		this.assessedIdentities = null;
		
		initForm(ureq);
		loadModel();
	}
	
	public GTACoachPeerReviewReceivedListController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, TaskList taskList, List<Identity> assessedIdentities,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, Form rootForm) {
		super(ureq, wControl, CMD_TOOLS, courseEnv, gtaNode, rootForm);
		assignedTask = null;
		this.stackPanel = stackPanel;
		this.taskList = taskList;
		this.assessedIdentities = assessedIdentities;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		assignReviewers = uifactory.addFormLink("assign.reviewers", formLayout, Link.BUTTON);
		assignReviewers.setIconLeftCSS("o_icon o_icon_shuffle");
		assignReviewers.setVisible(assignedTask != null);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(new FullNameNodeRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.identityFullName, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.numOfReviewers,
				new NumOfCellRenderer(assessedIdentities != null, translate("warning.received.reviews"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.plot));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.median));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.average));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sessionStatus,
				new TaskReviewAssignmentStatusCellRenderer(getLocale(), true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.taskStepStatus,
				new TaskStepStatusCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel leaveCol = new DefaultFlexiColumnModel(CoachReviewCols.editReview.i18nHeaderKey(),
				CoachReviewCols.editReview.ordinal(), "view",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("review.execute"), "execute"),
						new StaticFlexiCellRenderer(translate("review.view"), "view")));
		leaveCol.setAlwaysVisible(true);
		leaveCol.setExportable(false);
		columnsModel.addFlexiColumnModel(leaveCol);
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CoachReviewCols.tools);
		toolsCol.setIconHeader("o_icon o_icon-fw o_icon_actions");
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
		statusValues.add(SelectionValues.entry(FILTER_UNSUFFICIENT_REVIEWERS, translate("filter.unsufficient.reviewers")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.unsufficient.reviewers"),
				FILTER_UNSUFFICIENT_REVIEWERS, statusValues, true));
	}
	
	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab insufficientTab = FlexiFiltersTabFactory.tabWithImplicitFilters(UNSUFFICIENT_REVIEWERS_TAB_ID, translate("filter.unsufficient.reviewers"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_UNSUFFICIENT_REVIEWERS, List.of(FILTER_UNSUFFICIENT_REVIEWERS))));
		tabs.add(insufficientTab);
	}
	
	@Override
	public void loadModel() {
		List<CoachPeerReviewRow> rows = new ArrayList<>();
		
		if(assignedTask != null) {
			Identity assessedIdentity = assignedTask.getIdentity();
			List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsForTask(assignedTask, false);
			SessionParticipationListStatistics statistics = peerReviewManager
					.loadStatistics(assignedTask, assignments, gtaNode, STATUS_FOR_STATS);
			loadModelRow(assessedIdentity, assignedTask, assignments, statistics, rows);
		} else {
			loadModelList(rows);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	protected void loadModelList(List<CoachPeerReviewRow> rows) {
		List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		Map<Identity,Task> identityToTask = tasks.stream()
				.collect(Collectors.toMap(Task::getIdentity, task -> task, (u, v) -> u));	
		
		List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsForTaskList(taskList, false);
		Map<Task,List<TaskReviewAssignment>> taskToAssignments = new HashMap<>();
		for(TaskReviewAssignment assignment:assignments) {
			List<TaskReviewAssignment> taskAssignments = taskToAssignments
					.computeIfAbsent(assignment.getTask(), t -> new ArrayList<>());
			taskAssignments.add(assignment);
		}
		
		Map<Task,SessionParticipationListStatistics> taskToStatistics = peerReviewManager
				.loadStatisticsProTask(taskList, assignments, gtaNode, STATUS_FOR_STATS);
		
		for(Identity assessedIdentity:assessedIdentities) {
			Task task = identityToTask.get(assessedIdentity);
			List<TaskReviewAssignment> taskAssignments = taskToAssignments.get(task);
			if(taskAssignments == null) {
				taskAssignments = List.of();
			}
			SessionParticipationListStatistics statistics = taskToStatistics.get(task);
			if(statistics == null) {
				statistics = SessionParticipationListStatistics.noStatistics();
			}
			loadModelRow(assessedIdentity, task, taskAssignments, statistics, rows);
		}
	}
	
	private void loadModelRow(Identity assessedIdentity, Task task, List<TaskReviewAssignment> assignments,
			SessionParticipationListStatistics statistics, List<CoachPeerReviewRow> rows) {
		String assessedIdentityFullname = userManager.getUserDisplayName(assessedIdentity);
		CoachPeerReviewRow assessedIdentityRow = new CoachPeerReviewRow(task, assessedIdentityFullname);
		
		List<CoachPeerReviewRow> sessionRows = new ArrayList<>();
		rows.add(assessedIdentityRow);
		assessedIdentityRow.setChildrenRows(sessionRows);
		
		Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap = statistics.toParticipationsMap();
		
		for(TaskReviewAssignment assignment : assignments) {
			SessionParticipationStatistics participationStatistics = null;
			if(assignment.getParticipation() != null) {
				participationStatistics = statisticsMap.get(assignment.getParticipation());
			}
			// This is the task of the assignee, not the reviewed one
			CoachPeerReviewRow sessionRow = forgeAssignmentRow(task, assignment, participationStatistics);
			sessionRow.setParent(assessedIdentityRow);
			rows.add(sessionRow);
			sessionRows.add(sessionRow);
		}
		
		// Fill statistics
		forgeAssessedIdentityRow(assessedIdentityRow, statistics.aggregatedStatistics());
	}
	
	private void forgeAssessedIdentityRow(CoachPeerReviewRow assessedIdentityRow, SessionStatistics aggregatedStatistics) {
		decorateWithAggregatedStatistics(assessedIdentityRow, aggregatedStatistics);
		decorateWithTools(assessedIdentityRow);
		decorateWithStepStatus(assessedIdentityRow, assessedIdentityRow.getTask());
	}
	
	private CoachPeerReviewRow forgeAssignmentRow(Task task, TaskReviewAssignment assignment,
			SessionParticipationStatistics statistics) {
		String assigneeFullname = userManager.getUserDisplayName(assignment.getAssignee());
		boolean canEdit = getIdentity().equals(assignment.getAssignee());
		CoachPeerReviewRow sessionRow = new CoachPeerReviewRow(task, assignment, assigneeFullname, canEdit);
		
		decorateWithStatistics(sessionRow, statistics);
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
		} else if(evaluationFormExecCtrl == source) {
			doCloseReviewController();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
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
		if(assignReviewers == source) {
			doAssignReviewers(ureq, tableModel.getObject(0));
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				CoachPeerReviewRow row = tableModel.getObject(se.getIndex());
				if(row.getParent() == null) {
					doCompareEvaluationFormSessions(ureq, row);
				} else if("view".equals(se.getCommand())) {
					doViewEvaluationFormSession(ureq, row);
				} else if("execute".equals(se.getCommand())) {
					doOpenOwnReview(ureq, row);
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
	
	private void doAssignReviewers(UserRequest ureq, CoachPeerReviewRow row) {
		assignmentsCtrl = new GTAPeerReviewAssignmentController(ureq, getWindowControl(),
				taskList, row.getTask(), gtaNode);
		listenTo(assignmentsCtrl);
		
		String title = translate("review.assignment.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				assignmentsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doCreateOwnReview(UserRequest ureq, CoachPeerReviewRow row) {
		// Create an assignment for me and the task
		Task taskToReview = gtaManager.getTask(row.getTask());
		TaskReviewAssignment assignment = peerReviewManager.createAssignment(taskToReview, getIdentity());
		dbInstance.commit();
		// Reload UI
		loadModel();
		// Edit review
		doOpenOwnReview(ureq, taskToReview, assignment);
	}
	
	protected void doOpenOwnReview(UserRequest ureq, CoachPeerReviewRow row) {
		doOpenOwnReview(ureq, row.getTask(), row.getAssignment());
	}

	protected void doOpenOwnReview(UserRequest ureq, Task taskToReview, TaskReviewAssignment assignment) {
		String assessedFullName = userManager.getUserDisplayName(taskToReview.getIdentity());
		GTAEvaluationFormExecutionOptions options = GTAEvaluationFormExecutionOptions.valueOf(true, false, false, assessedFullName, true, true, false);
		
		evaluationFormExecCtrl = new GTAEvaluationFormExecutionController(ureq, getWindowControl(),
				assignment, courseEnv, gtaNode,	options, true, true);
		listenTo(evaluationFormExecCtrl);
		
		String title = translate("review.assessment.title", assessedFullName);
		stackPanel.pushController(title, evaluationFormExecCtrl);
	}
	
	private void doCloseReviewController() {
		stackPanel.popController(evaluationFormExecCtrl);
		
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		evaluationFormExecCtrl = null;
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
	
	private class ToolsController extends BasicController {
		
		private Link showReviewLink;
		private Link showReviewsLink;
		private Link addNewReviewLink;
		private Link assignReviewerLink;
		private Link invalidateReviewLink;
		private VelocityContainer mainVC;
		
		private CoachPeerReviewRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CoachPeerReviewRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			if(row.getParent() == null) {
				if(row.getTask() != null) {
					assignReviewerLink = LinkFactory.createLink("assign.reviewers", "assign.reviewers", getTranslator(), mainVC, this, Link.LINK);
					assignReviewerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_shuffle");
				}
				
				showReviewsLink = LinkFactory.createLink("show.reviews", "show.reviews", getTranslator(), mainVC, this, Link.LINK);
				showReviewsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");
				
				addNewReviewLink = LinkFactory.createLink("add.new.review", "add.new.review", getTranslator(), mainVC, this, Link.LINK);
				addNewReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			} else {
				showReviewLink = LinkFactory.createLink("show.review", "show.review", getTranslator(), mainVC, this, Link.LINK);
				showReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");
				
				invalidateReviewLink = LinkFactory.createLink("invalidate.review", "invalidate.review", getTranslator(), mainVC, this, Link.LINK);
				invalidateReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_invalidate");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(assignReviewerLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doAssignReviewers(ureq, row);
			} else if(showReviewsLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doCompareEvaluationFormSessions(ureq, row);
			} else if(showReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doViewEvaluationFormSession(ureq, row);
			} else if(invalidateReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doInvalidateReview(ureq, row);
			} else if(addNewReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doCreateOwnReview(ureq, row);
			}
		}
	}
}
