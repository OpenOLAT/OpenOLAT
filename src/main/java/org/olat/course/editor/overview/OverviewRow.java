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
package org.olat.course.editor.overview;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRow implements FlexiTreeTableNode, IndentedCourseNode {
	
	private final CourseNode courseNode;
	private final int recursionLevel;
	private String translatedDisplayOption;
	private OverviewRow parent;
	private boolean hasChildren;
	
	public OverviewRow(CourseNode courseNode, int recursionLevel) {
		this.courseNode = courseNode;
		this.recursionLevel = recursionLevel;
	}

	@Override
	public int getRecursionLevel() {
		return recursionLevel;
	}

	@Override
	public String getType() {
		return courseNode.getType();
	}

	@Override
	public String getShortTitle() {
		return courseNode.getShortName();
	}

	@Override
	public String getLongTitle() {
		return courseNode.getLongTitle();
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public String getTranslatedDisplayOption() {
		return translatedDisplayOption;
	}

	public void setTranslatedDisplayOption(String translatedDisplayOption) {
		this.translatedDisplayOption = translatedDisplayOption;
	}

	@Override
	public String getCrump() {
		return null;
	}

	public void setParent(OverviewRow parent) {
		this.parent = parent;
		if (parent != null) {
			parent.hasChildren = true;
		}
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return hasChildren;
	}

}
