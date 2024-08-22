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
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.ui.component.TaskReviewAssignmentStatusCellRenderer;
import org.olat.course.nodes.gta.ui.peerreview.GTAParticipantPeerReviewTableModel.ReviewCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAParticipantPeerReviewsAwardedListController extends AbstractParticipantPeerReviewsAwardedListController {
	
	private static final String CMD_EXECUTE = "execute";

	private FlexiTableElement tableEl;
	private FormLink closeReviewsButton;
	private GTAParticipantPeerReviewTableModel tableModel;
	private final BreadcrumbedStackedPanel stackPanel;

	private final Task assignedTask;
	private final boolean readOnly;
	private final TaskList taskList;
	private final CourseEnvironment courseEnv;

	private CloseableModalController cmc;
	private ConfirmCloseReviewsController confirmCloseReviewsCtrl;
	private GTAEvaluationFormExecutionController evaluationFormExecCtrl;
	
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public GTAParticipantPeerReviewsAwardedListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, TaskList taskList, Task assignedTask, boolean readOnly) {
		super(ureq, wControl, "peerreviews_list", gtaNode);
		this.assignedTask = assignedTask;
		this.stackPanel = stackPanel;
		this.readOnly = readOnly;
		this.taskList = taskList;
		this.courseEnv = courseEnv;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		InfoPanelItem panel = uifactory.addInfoPanel("reviews.infos", null, formLayout);
		panel.setTitle(translate("peer.review.steps.instructions"));
		panel.setPersistedStatusId(ureq, "peer-review-as-participant");
		initInformations(panel);
		
		closeReviewsButton = uifactory.addFormLink("close.peer.reviews", formLayout, Link.BUTTON);
		closeReviewsButton.setVisible(!readOnly);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.assessedIdentity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.numOfDocuments));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.plot));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.sessionStatus,
				new TaskReviewAssignmentStatusCellRenderer(getLocale(), true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.executeReview));
		
		tableModel = new GTAParticipantPeerReviewTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "awarded.reviews", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		
		tableEl.setEmptyTableMessageKey("table.awarded.reviews.empty");
	}
	
	private void initInformations(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder(256);
		infos.append("<h4>").append(translate("peer.review.step1.title")).append("</h4>")
	    	 .append(translate("peer.review.step1.desc"));
		
		String pointsProReview = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_SCORE_PRO_REVIEW);
		if(StringHelper.containsNonWhitespace(pointsProReview)) {
			infos.append("<br>").append(translate("peer.review.step1.desc.point"));
		}
		
		infos.append("<h4>").append(translate("peer.review.step2.title")).append("</h4>")
		     .append(translate("peer.review.step2.desc"));
		
		panel.setInformations(infos.toString());
	}
	
	private void loadModel() {
		List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsOfReviewer(taskList, getIdentity());
		List<TaskReviewAssignmentStatus> statusForStats = List.of(TaskReviewAssignmentStatus.done);
		SessionParticipationListStatistics statisticsList = peerReviewManager.loadStatistics(taskList, assignments, getIdentity(), gtaNode, statusForStats);
		Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap = statisticsList.toParticipationsMap();
		
		int count = 0;
		List<ParticipantPeerReviewAssignmentRow> rows = new ArrayList<>(assignments.size());
		for(TaskReviewAssignment assignment:assignments) {
			SessionParticipationStatistics statistics = null;
			if(assignment.getParticipation() != null) {
				statistics = statisticsMap.get(assignment.getParticipation());
			}
			rows.add(forgeRow(assignment, assignment.getTask(), statistics, ++count));
		}
		tableModel.setObjects(rows);
	}
	
	@Override
	protected ParticipantPeerReviewAssignmentRow forgeRow(TaskReviewAssignment assignment, Task task, SessionParticipationStatistics sessionStatistics, int pos) {
		ParticipantPeerReviewAssignmentRow row = super.forgeRow(assignment, task, sessionStatistics, pos);
		// Start evaluation link
		if(!readOnly && (assignment.getParticipation() == null || assignment.getStatus() == null
				|| assignment.getStatus() ==  TaskReviewAssignmentStatus.open
				|| assignment.getStatus() == TaskReviewAssignmentStatus.inProgress)) {
			String executeLinkName = "execute-" + (++counter);
			FormLink executeLink = uifactory.addFormLink(executeLinkName, CMD_EXECUTE, "review.execute", null, flc, Link.LINK);
			executeLink.setUserObject(row);
			row.setExecuteSessionLink(executeLink);
		}
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(evaluationFormExecCtrl == source) {
			doCloseReviewController();
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel();
			}
		} else if(confirmCloseReviewsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCloseReviewsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCloseReviewsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(closeReviewsButton == source) {
			doCloseReviews(ureq);
		} else if(source instanceof FormLink link && CMD_EXECUTE.equals(link.getCmd())
				&& link.getUserObject() instanceof ParticipantPeerReviewAssignmentRow sessionRow) {
			doStartReview(ureq, sessionRow);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStartReview(UserRequest ureq, ParticipantPeerReviewAssignmentRow sessionRow) {
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		
		String mode = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW,
				GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW_DEFAULT);
		boolean anonym = GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW.equals(mode);
		String assessedFullName = sessionRow.getAssessedIdentityName();
		GTAEvaluationFormExecutionOptions options = GTAEvaluationFormExecutionOptions.valueOf(true, false, anonym, assessedFullName, true, true, false);
		
		TaskReviewAssignment assignment = sessionRow.getAssignment();
		evaluationFormExecCtrl = new GTAEvaluationFormExecutionController(ureq, getWindowControl(),
				assignment, courseEnv, gtaNode,	options, true, false);
		listenTo(evaluationFormExecCtrl);
		
		stackPanel.pushController(assessedFullName, evaluationFormExecCtrl);
	}
	
	private void doCloseReviewController() {
		stackPanel.popController(evaluationFormExecCtrl);
		
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		evaluationFormExecCtrl = null;
	}
	
	private void doCloseReviews(UserRequest ureq) {
		List<TaskReviewAssignment> assignments = tableModel.getObjects().stream()
			.filter(row -> row.getAssignment() != null)
			.map(ParticipantPeerReviewAssignmentRow::getAssignment)
			.toList();
		confirmCloseReviewsCtrl = new ConfirmCloseReviewsController(ureq, getWindowControl(),
				assignments, assignedTask, gtaNode);
		listenTo(confirmCloseReviewsCtrl);
		
		String title = translate("confirm.close.reviews.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				confirmCloseReviewsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
