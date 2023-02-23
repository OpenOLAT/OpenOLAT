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
package org.olat.modules.coach.ui;

import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCoachAssignmentRow implements IndentedCourseNode {
	
	private RepositoryEntry courseEntry;
	private CourseElement courseElement;
	private long numOfAssignments;
	
	public CourseCoachAssignmentRow(RepositoryEntry courseEntry, CourseElement courseElement, long numOfAssignments) {
		this.courseEntry = courseEntry;
		this.courseElement = courseElement;
		this.numOfAssignments = numOfAssignments;
	}
	
	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}

	public String getCourseElementShortTitle() {
		return courseElement.getShortTitle();
	}

	public long getNumOfAssignments() {
		return numOfAssignments;
	}
	
	public String getSubIdent() {
		return courseElement.getSubIdent();
	}

	@Override
	public String getType() {
		return courseElement.getType();
	}

	@Override
	public String getShortTitle() {
		return getCourseElementShortTitle();
	}

	@Override
	public String getLongTitle() {
		return getCourseElementShortTitle();
	}

	@Override
	public int getRecursionLevel() {
		return 0;
	}
	
	
	

}
