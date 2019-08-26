/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.bulk;

import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.course.assessment.manager.BulkAssessmentTask;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskData {
	
	private final Task task;
	private final String ownerFullName;
	private final CourseNode courseNode;
	
	private boolean hasScore;
	private boolean hasPassed;
	private boolean hasUserComment;
	private boolean hasReturnFiles;
	private int numOfAssessedIds;
	
	public TaskData(Task task, BulkAssessmentTask runnable, CourseNode courseNode, String ownerFullName) {
		this.task = task;
		this.courseNode = courseNode;
		this.ownerFullName = ownerFullName;
		
		hasScore = runnable.getSettings().isHasScore();
		hasPassed = runnable.getSettings().isHasPassed() && runnable.getSettings().getCut() == null;
		hasUserComment = runnable.getSettings().isHasUserComment();
		hasReturnFiles = runnable.getSettings().isHasReturnFiles();
		numOfAssessedIds = runnable.getDatas().getRowsSize();
	}

	public Task getTask() {
		return task;
	}

	public boolean isHasScore() {
		return hasScore;
	}
	
	public boolean isHasPassed() {
		return hasPassed;
	}

	public boolean isHasUserComment() {
		return hasUserComment;
	}

	public boolean isHasReturnFiles() {
		return hasReturnFiles;
	}

	public int getNumOfAssessedIds() {
		return numOfAssessedIds;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}
	
	public String getOwnerFullName() {
		return ownerFullName;
	}
}
