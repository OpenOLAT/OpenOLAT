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

import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
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
	
	private NumOf numOfReviews;
	private NumOf numOfReviewers;
	private TaskReviewAssignment assignment;
	private EvaluationFormParticipation participation;
	
	private Double median;
	private Double average;
	private Double sum;
	
	private Identity assignee;
	
	private boolean canEdit;
	private FormLink toolsLink;
	private BoxPlot assessmentPlot;
	private ProgressBarItem progressBar;
	
	public CoachPeerReviewRow(Task task, String fullName) {
		this.task = task;
		this.fullName = fullName;
	}
	
	public CoachPeerReviewRow(Task task, TaskReviewAssignment assignment, String fullName, boolean canEdit) {
		this(task, fullName);
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
	
	public EvaluationFormParticipationStatus getParticipationStatus() {
		return participation == null ? null : participation.getStatus();
	}

	public void setAssignment(TaskReviewAssignment assignment) {
		this.assignment = assignment;
	}
	
	public float getProgress() {
		return progressBar == null ? 0.0f : progressBar.getActual();
	}

	public ProgressBarItem getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBarItem progressBar) {
		this.progressBar = progressBar;
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

	public BoxPlot getAssessmentPlot() {
		return assessmentPlot;
	}

	public void setAssessmentPlot(BoxPlot assessmentPlot) {
		this.assessmentPlot = assessmentPlot;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public record NumOf(int number, int reference) {
		//
	}
}
