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
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseElementRow implements FlexiTreeTableNode, IndentedCourseNode {
	
	private final CourseNode courseNode;
	private final CourseElementRow parent;
	
	public CourseElementRow(CourseNode courseNode, CourseElementRow parent) {
		this.courseNode = courseNode;
		this.parent = parent;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	@Override
	public String getType() {
		return getCourseNode().getType();
	}

	@Override
	public String getShortTitle() {
		return getCourseNode().getShortTitle();
	}

	@Override
	public String getLongTitle() {
		return getCourseNode().getLongTitle();
	}

	@Override
	public int getRecursionLevel() {
		return 0;
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	@Override
	public String getCrump() {
		return getShortTitle();
	}
	
	
}
