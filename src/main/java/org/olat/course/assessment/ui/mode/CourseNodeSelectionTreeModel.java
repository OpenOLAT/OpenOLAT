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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeSelectionTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 7708592865968524515L;

	public CourseNodeSelectionTreeModel(ICourse course) {
		TreeNode rootNode = buildTree(course.getRunStructure().getRootNode());
		setRootNode(rootNode);
	}

	private TreeNode buildTree(CourseNode courseNode) {
		GenericTreeNode node = new GenericTreeNode(courseNode.getIdent(), courseNode.getShortTitle(), courseNode);
		node.setIconCssClass(CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		node.setAltText(courseNode.getLongTitle());
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode childNode = (CourseNode)courseNode.getChildAt(i);
			node.addChild(buildTree(childNode));
		}
		return node;
	}
}