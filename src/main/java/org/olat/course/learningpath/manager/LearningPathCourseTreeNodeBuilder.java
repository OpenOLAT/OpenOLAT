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
package org.olat.course.learningpath.manager;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.ui.LearningPathTreeModelBuilder;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.CourseTreeNodeBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 2 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathCourseTreeNodeBuilder extends CourseTreeNodeBuilder {

	private final GenericTreeModel learningPathModel;

	public LearningPathCourseTreeNodeBuilder(UserCourseEnvironment userCourseEnvironment) {
		learningPathModel = LearningPathTreeModelBuilder.builder(userCourseEnvironment).create();
	}

	@Override
	protected CourseTreeNode createNodeEvaluation(CourseNode courseNode) {
		boolean accessible = false;
		String iconDecorator1CssClass = null;
		TreeNode treeNode = learningPathModel.getNodeById(courseNode.getIdent());
		if (treeNode instanceof LearningPathTreeNode) {
			LearningPathTreeNode learningPathTreeNode = (LearningPathTreeNode)treeNode;
			iconDecorator1CssClass = getIconDecorator1CssClass(learningPathTreeNode.getStatus());
			accessible = treeNode.isAccessible();
		}
		
		CourseTreeNode courseTreeNode = new CourseTreeNode(courseNode);
		courseTreeNode.setVisible(true);
		courseTreeNode.setAccessible(accessible);
		courseTreeNode.setIconDecorator1CssClass(iconDecorator1CssClass);
		return courseTreeNode;
	}

	private String getIconDecorator1CssClass(LearningPathStatus status) {
		String cssClass = null;
				
		if (status != null) {
			switch(status) {
			case notAccessible: cssClass = "o_lp_not_accessible"; break;
			case ready: cssClass = "o_lp_ready"; break;
			case inProgress: cssClass = "o_lp_in_progress"; break;
			case done: cssClass = "o_lp_done"; break;
			}
		}
		
		return cssClass;
	}

}
