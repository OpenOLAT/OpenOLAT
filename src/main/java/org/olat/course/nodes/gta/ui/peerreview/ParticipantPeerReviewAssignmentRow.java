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

import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantPeerReviewAssignmentRow {
	
	private BoxPlot boxPlot;
	private FormItem ratingItem;
	private FormLink viewSessionLink;
	private FormLink executeSessionLink;
	private ProgressBarItem progressBar;
	
	private final String assigneeFullName;
	private final String assessedFullName;
	private final Integer numOfDocumentsToReview;
	private final TaskReviewAssignment assignment;
	
	public ParticipantPeerReviewAssignmentRow(TaskReviewAssignment assignment, Task task,
			String assigneeFullName, String assessedFullName) {
		this.assignment = assignment;
		this.assigneeFullName = assigneeFullName;
		this.assessedFullName = assessedFullName;
		this.numOfDocumentsToReview = task.getSubmissionNumOfDocs();
	}
	
	public TaskReviewAssignment getAssignment() {
		return assignment;
	}
	
	public Identity getReviewer() {
		return assignment.getAssignee();
	}
	
	public Integer getNumOfDocumentsToReview() {
		return numOfDocumentsToReview;
	}
	
	public String getReviewerName() {
		return assigneeFullName;
	}
	
	public String getAssessedIdentityName() {
		return assessedFullName;
	}
	
	public double getProgress() {
		return 0.0d;
	}
	
	public TaskReviewAssignmentStatus getStatus() {
		return assignment == null ? null : assignment.getStatus();
	}

	public FormLink getExecuteSessionLink() {
		return executeSessionLink;
	}

	public void setExecuteSessionLink(FormLink executeSessionLink) {
		this.executeSessionLink = executeSessionLink;
	}

	public FormLink getViewSessionLink() {
		return viewSessionLink;
	}

	public void setViewSessionLink(FormLink viewSessionLink) {
		this.viewSessionLink = viewSessionLink;
	}
	
	public ProgressBarItem getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBarItem progress) {
		this.progressBar = progress;
	}

	public BoxPlot getBoxPlot() {
		return boxPlot;
	}

	public void setBoxPlot(BoxPlot boxPlot) {
		this.boxPlot = boxPlot;
	}

	public FormItem getRatingItem() {
		return ratingItem;
	}

	public void setRatingItem(FormItem ratingItem) {
		this.ratingItem = ratingItem;
	}
}
