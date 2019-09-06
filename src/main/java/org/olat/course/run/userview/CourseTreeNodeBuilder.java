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

import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 2 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class CourseTreeNodeBuilder {
	
	public CourseTreeNode build(CourseNode courseNode, TreeEvaluation treeEval,
			TreeFilter filter) {
		return getNodeEvaluation(courseNode, treeEval, filter);
	}

	private CourseTreeNode getNodeEvaluation(CourseNode courseNode, TreeEvaluation treeEval, TreeFilter filter) {
		CourseTreeNode treeNode = createNodeEvaluation(courseNode);
		if(filter != null && !filter.isVisible(courseNode)) {
			treeNode.setVisible(false);
		}
		
		treeEval.cacheCourseToTreeNode(courseNode, treeNode);
		if (treeNode.isVisible()) {
			int childcnt = courseNode.getChildCount();
			for (int i = 0; i < childcnt; i++) {
				CourseNode cn = (CourseNode) courseNode.getChildAt(i);
				CourseTreeNode child = getNodeEvaluation(cn, treeEval, filter);
				if (child.isVisible()) {
					treeNode.addChild(child);
				}
			}
		}
		return treeNode;
	}

	protected abstract CourseTreeNode createNodeEvaluation(CourseNode courseNode);

}
