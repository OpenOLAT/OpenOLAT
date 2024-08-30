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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 26 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewsAssignmentController extends AbstractPeerReviewsAssignmentController {

	private final Identity reviewer;
	private final Task reviewerTask;
	
	private final TaskPortraitController taskPortraitCtrl;

	public GTAPeerReviewsAssignmentController(UserRequest ureq, WindowControl wControl, TaskList taskList,
			Identity reviewer, Task reviewerTask, RepositoryEntry courseEntry, GTACourseNode gtaNode) {
		super(ureq, wControl, taskList, courseEntry, gtaNode);
		this.reviewer = reviewer;
		this.reviewerTask = reviewerTask;
		
		taskPortraitCtrl = new TaskPortraitController(ureq, getWindowControl(), reviewer, reviewerTask);
		listenTo(taskPortraitCtrl);

		initForm(ureq);
		loadModel();
		initFilters();
		initFiltersPresets(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.put("portrait", taskPortraitCtrl.getInitialComponent());
		}
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void loadModel() {
		final List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		final List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsOfReviewer(taskList, reviewer);
		final List<TaskReviewAssignment> allAssignments = peerReviewManager.getAssignmentsForTaskList(taskList, false);
		List<PeerReviewAssignmentRow> assignmentRows = tasks.stream()
				.filter(task -> task.getIdentity() != null && !task.getIdentity().equals(reviewer))
				.map(task -> forgeRow(task, assignments, allAssignments))
				.toList();
		tableModel.setObjects(assignmentRows);
		tableEl.reset(true, true, true);
	}
	
	private PeerReviewAssignmentRow forgeRow(Task taskToReview, List<TaskReviewAssignment> assignments, List<TaskReviewAssignment> allAssignments) {
		Identity identity = taskToReview.getIdentity();
		TaskReviewAssignment assignment = getAssignmentFor(taskToReview, reviewer, assignments);
		PeerReviewAssignmentRow assignmentRow = new PeerReviewAssignmentRow(taskToReview, assignment, identity, userPropertyHandlers, getLocale());
		decorateRow(identity, assignment, assignmentRow, allAssignments);
		assignmentRow.setSubmissionStatus(statusRenderer.calculateSubmissionStatus(identity, taskToReview));
		return assignmentRow;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean newAssignment = false;
		List<PeerReviewAssignmentRow> assignmentRows = tableModel.getObjects();
		for(PeerReviewAssignmentRow assignmentRow:assignmentRows) {
			TaskReviewAssignment assignment = assignmentRow.getAssignment();
			if(assignmentRow.getAssignmentEl().isAtLeastSelected(1)) {
				Task taskToReview = assignmentRow.getTask();
				if(assignment == null) {
					peerReviewManager.createAssignment(taskToReview, reviewer);
					reopenTaskIfNeeded(taskToReview);
					newAssignment = true;
				} else if(!assignment.isAssigned()) {
					assignment.setAssigned(true);
					peerReviewManager.updateAssignment(assignment);
					reopenTaskIfNeeded(taskToReview);
					newAssignment = true;
				}
			} else if(assignment != null && assignment.isAssigned()) {
				assignment.setAssigned(false);
				peerReviewManager.updateAssignment(assignment);
			}
		}
		
		if(newAssignment && reviewerTask != null) {
			reopenTaskIfNeeded(reviewerTask);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
