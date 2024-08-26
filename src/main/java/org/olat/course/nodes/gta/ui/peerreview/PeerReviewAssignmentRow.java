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
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 12 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PeerReviewAssignmentRow extends UserPropertiesRow {
	
	private final Task task;
	private final Identity assignee;
	private int numOfReviewers = 0;
	private int numOfTasksToReviews = 0;
	private TaskReviewAssignment assignment;
	
	private MultipleSelectionElement assignmentEl;
	
	public PeerReviewAssignmentRow(Task task, TaskReviewAssignment assignment,
			Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.task = task;
		this.assignee = identity;
		this.assignment = assignment;
	}
	
	public Task getTask() {
		return task;
	}
	
	public String getTaskName() {
		return task == null ? null : task.getTaskName();
	}
	
	public TaskReviewAssignment getAssignment() {
		return assignment;
	}
	
	public Identity getAssignee() {
		return assignee;
	}

	public int getNumOfReviewers() {
		return numOfReviewers;
	}

	public void setNumOfReviewers(int numOfReviewers) {
		this.numOfReviewers = numOfReviewers;
	}

	public int getNumOfTasksToReviews() {
		return numOfTasksToReviews;
	}

	public void setNumOfTasksToReviews(int numOfTasksToReviews) {
		this.numOfTasksToReviews = numOfTasksToReviews;
	}
	
	public boolean isAssigned() {
		return assignmentEl != null && assignmentEl.isAtLeastSelected(1);
	}

	public MultipleSelectionElement getAssignmentEl() {
		return assignmentEl;
	}

	public void setAssignmentEl(MultipleSelectionElement assignmentEl) {
		this.assignmentEl = assignmentEl;
	}
}
