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

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 6 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseTreeNode extends GenericTreeNode implements CourseNodeSecurityCallback {

	private static final long serialVersionUID = -2463822839337921991L;
	
	private final CourseNode courseNode;
	private final int treeLevel;
	private boolean visible;
	private NodeEvaluation nodeEvaluation;

	public CourseTreeNode(CourseNode courseNode, int treeLevel) {
		super(courseNode.getIdent());
		this.courseNode = courseNode;
		this.treeLevel = treeLevel;
		setTitle(courseNode.getShortTitle());
		setAltText(courseNode.getLongTitle());
		String type = courseNode.getType();
		CourseNodeFactory courseNodeFactory = CourseNodeFactory.getInstance();
		// courseNodeFactory may be null in pure JUnitTests.
		// Usually this code does not mater in JUnitTests, so just ignore it.
		if (courseNodeFactory != null) {
			CourseNodeConfiguration cnConfig = courseNodeFactory.getCourseNodeConfigurationEvenForDisabledBB(type);
			if(cnConfig != null) {
				String nodeCssClass = null;
				if (courseNode.getParent() == null) {
					// Spacial case for root node
					nodeCssClass = "o_CourseModule_icon";
				} else {
					nodeCssClass = cnConfig.getIconCSSClass();
				}
				setIconCssClass(nodeCssClass);
			}
		}
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}
	
	public int getTreeLevel() {
		return treeLevel;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public NodeEvaluation getNodeEvaluation() {
		return nodeEvaluation;
	}
	
	public void setNodeEvaluation(NodeEvaluation nodeEvaluation) {
		this.nodeEvaluation = nodeEvaluation;
	}

}
