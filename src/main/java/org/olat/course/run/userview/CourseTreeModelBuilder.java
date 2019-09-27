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
package org.olat.course.run.userview;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 25 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class CourseTreeModelBuilder {
	
	protected UserCourseEnvironment userCourseEnv;

	protected CourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}
	
	public GenericTreeModel build(TreeEvaluation treeEval, TreeFilter filter) {
		CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		int treeLevel = 0;
		CourseTreeNode rootTreeNode = getCourseTreeNode(rootNode, treeEval, filter, treeLevel);
		GenericTreeModel treeModel = new GenericTreeModel();
		treeModel.setRootNode(rootTreeNode);
		return treeModel;
	}
	
	private CourseTreeNode getCourseTreeNode(CourseNode courseNode, TreeEvaluation treeEval, TreeFilter filter, int treeLevel) {
		CourseTreeNode treeNode = createCourseTreeNode(courseNode, treeLevel);
		if(filter != null && !filter.isVisible(courseNode)) {
			treeNode.setVisible(false);
		}
		
		treeEval.cacheCourseToTreeNode(courseNode, treeNode);
		if (treeNode.isVisible()) {
			int childLevel = treeLevel + 1;
			int childCount = courseNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				CourseNode cn = (CourseNode) courseNode.getChildAt(i);
				CourseTreeNode child = getCourseTreeNode(cn, treeEval, filter, childLevel);
				if (child.isVisible()) {
					// if the parent is not accessible the child is not accessible as well!
					if (!treeNode.isAccessible()) {
						child.setAccessible(false);
					}
					treeNode.addChild(child);
				}
			}
		}
		return treeNode;
	}

	protected abstract CourseTreeNode createCourseTreeNode(CourseNode courseNode, int treeLevel);

}
