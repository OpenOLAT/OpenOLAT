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
package org.olat.course.learningpath.ui;

import java.util.Date;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathTreeNode extends GenericTreeNode {

	private static final long serialVersionUID = -9033714563825036957L;
	
	private final CourseNode courseNode;
	private final int recursionLevel;
	private LearningPathStatus status;
	private LearningPathObligation obligation;
	private Date dateDone;
	private Integer duration;
	private Integer progress;

	public LearningPathTreeNode(CourseNode courseNode, int recursionLevel) {
		this.courseNode = courseNode;
		this.recursionLevel = recursionLevel;
		if (courseNode != null) {
			setIdent(courseNode.getIdent());
		}
	}

	public LearningPathStatus getStatus() {
		return status;
	}

	public void setStatus(LearningPathStatus status) {
		this.status = status;
	}

	public LearningPathObligation getObligation() {
		return obligation;
	}

	public void setObligation(LearningPathObligation obligation) {
		this.obligation = obligation;
	}

	public Date getDateDone() {
		return dateDone;
	}

	public void setDateDone(Date dateDone) {
		this.dateDone = dateDone;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public int getRecursionLevel() {
		return recursionLevel;
	}

}
