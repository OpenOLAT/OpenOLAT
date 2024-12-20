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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskLateStatus;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantStatus;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachPeerReviewRow implements FlexiTreeTableNode {
	
	private CoachPeerReviewRow parentRow;
	private List<CoachPeerReviewRow> childrenRows;
	
	private final Task task;
	private final String fullName;
	private final Identity identity;
	
	private NumOf numOfReviews;
	private NumOf numOfReviewers;
	private TaskReviewAssignment assignment;
	
	private EvaluationFormParticipation participation;
	
	private Double median;
	private Double average;
	private Double sum;
	
	private CoachedParticipantStatus stepStatus;
	private CoachedParticipantStatus submissionStatus;
	private DueDate peerReviewDueDate;
	private TaskLateStatus lateStatus;
	private Date reviewCompleteDate;
	
	private Identity assignee;
	
	private boolean canEdit;
	private FormLink markLink;
	private FormLink toolsLink;
	private BoxPlot assessmentPlot;
	
	public CoachPeerReviewRow(Task task, Identity identity, String fullName) {
		this.task = task;
		this.fullName = fullName;
		this.identity = identity;
	}
	
	public CoachPeerReviewRow(Task task, TaskReviewAssignment assignment, Identity identity, String fullName, boolean canEdit) {
		this(task, identity, fullName);
		this.canEdit = canEdit;
		this.assignment = assignment;
		this.assignee = assignment.getAssignee();
		this.participation = assignment.getParticipation();
	}
	
	@Override
	public CoachPeerReviewRow getParent() {
		return parentRow;
	}

	public void setParent(CoachPeerReviewRow parentRow) {
		this.parentRow = parentRow;
	}

	public List<CoachPeerReviewRow> getChildrenRows() {
		return childrenRows;
	}

	public void setChildrenRows(List<CoachPeerReviewRow> childrenRows) {
		this.childrenRows = childrenRows;
	}

	@Override
	public String getCrump() {
		return fullName;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public String getFullName() {
		return fullName;
	}
	
	public Task getTask() {
		return task;
	}
	
	public boolean canEdit() {
		return canEdit && this.assignment != null
				&& (assignment.getStatus() == TaskReviewAssignmentStatus.open || assignment.getStatus() == TaskReviewAssignmentStatus.inProgress);
	}
	
	public Identity getAssignee() {
		return assignee;
	}

	public NumOf getNumOfReviews() {
		return numOfReviews;
	}

	public void setNumOfReviews(NumOf numOfReviews) {
		this.numOfReviews = numOfReviews;
	}

	public NumOf getNumOfReviewers() {
		return numOfReviewers;
	}

	public void setNumOfReviewers(NumOf numOfReviewers) {
		this.numOfReviewers = numOfReviewers;
	}

	public TaskReviewAssignment getAssignment() {
		return assignment;
	}
	
	public TaskReviewAssignmentStatus getAssignmentStatus() {
		return assignment == null ? null : assignment.getStatus();
	}
	
	public EvaluationFormParticipation getParticipation() {
		return participation;
	}
	
	public EvaluationFormParticipationStatus getParticipationStatus() {
		return participation == null ? null : participation.getStatus();
	}

	public void setAssignment(TaskReviewAssignment assignment) {
		this.assignment = assignment;
	}

	public Double getMedian() {
		return median;
	}

	public void setMedian(Double median) {
		this.median = median;
	}

	public Double getAverage() {
		return average;
	}

	public void setAverage(Double average) {
		this.average = average;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}
	
	public CoachedParticipantStatus getStepStatus() {
		return stepStatus;
	}

	public void setStepStatus(CoachedParticipantStatus status) {
		this.stepStatus = status;
	}

	public CoachedParticipantStatus getSubmissionStatus() {
		return submissionStatus;
	}

	public void setSubmissionStatus(CoachedParticipantStatus submissionStatus) {
		this.submissionStatus = submissionStatus;
	}

	public BoxPlot getAssessmentPlot() {
		return assessmentPlot;
	}

	public void setAssessmentPlot(BoxPlot assessmentPlot) {
		this.assessmentPlot = assessmentPlot;
	}
	
	public TaskLateStatus getLateStatus() {
		return lateStatus;
	}

	public void setLateStatus(TaskLateStatus lateStatus) {
		this.lateStatus = lateStatus;
	}
	
	public Role getPeerReviewCompletedDoerRole() {
		return task == null ? null : task.getPeerReviewCompletedDoerRole();
	}

	public DueDate getPeerReviewDueDate() {
		return peerReviewDueDate;
	}

	public void setPeerReviewDueDate(DueDate dueDate) {
		this.peerReviewDueDate = dueDate;
	}
	
	public Date getReviewCompleteDate() {
		return reviewCompleteDate;
	}

	public void setReviewCompleteDate(Date date) {
		this.reviewCompleteDate = date;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
	
	public record NumOf(int number, int reference) {
		//
	}
}
