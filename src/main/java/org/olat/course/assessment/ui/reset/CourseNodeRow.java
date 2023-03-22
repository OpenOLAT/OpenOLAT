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
package org.olat.course.assessment.ui.reset;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeRow implements FlexiTreeTableNode, IndentedCourseNode {
	
	private final CourseNode courseNode;
	
	private final int recursionLevel;
	private final CourseNodeRow parent;
	
	private boolean selected;
	
	public CourseNodeRow(CourseNode courseNode, CourseNodeRow parent, int recursionLevel) {
		this.courseNode = courseNode;
		this.parent = parent;
		this.recursionLevel = recursionLevel;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String getShortTitle() {
		return courseNode.getShortTitle();
	}

	@Override
	public String getLongTitle() {
		return courseNode.getLongTitle();
	}

	@Override
	public String getType() {
		return courseNode.getType();
	}

	@Override
	public int getRecursionLevel() {
		return recursionLevel;
	}

	@Override
	public CourseNodeRow getParent() {
		return parent;
	}
	
	public boolean hasChildren() {
		return courseNode.getChildCount() > 0;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	@Override
	public String getCrump() {
		return null;
	}
}
