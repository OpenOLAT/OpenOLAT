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
package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> The structure node peek view controller displays the
 * title, the description and the first level of child nodes
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 23.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class STPeekViewController extends BasicController {
	
	private VelocityContainer genericPeekViewVC;
	
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public STPeekViewController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		genericPeekViewVC = createVelocityContainer("stPeekView");
		
		CourseTreeNode courseTreeNode = nodeAccessService.getNodeEvaluationBuilder(userCourseEnv)
				.build(courseNode, new TreeEvaluation(), new VisibleTreeFilter());
		List<CourseNode> childNodes = new ArrayList<>();
		// Loop over node evaluations of visible nodes
		int chdCnt = courseTreeNode.getChildCount();
		for (int i = 0; i < chdCnt; i++) {
			INode childNode = courseTreeNode.getChildAt(i);
			if (childNode instanceof CourseTreeNode) {
				CourseTreeNode childTreeNode = (CourseTreeNode) childNode;
				if (childTreeNode.isVisible() && childTreeNode.isAccessible()) {
					// Build and add child generic or specific peek view
					CourseNode child = childTreeNode.getCourseNode();
					childNodes.add(child);
					// Add link to jump to course node
					Link nodeLink = LinkFactory.createLink("nodeLink_" + child.getIdent(), genericPeekViewVC, this);
					nodeLink.setCustomDisplayText(StringHelper.escapeHtml(child.getShortTitle()));
					// Add css class for course node type
					String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(child.getType()).getIconCSSClass();
					nodeLink.setIconLeftCSS("o_icon o_icon-fw " + iconCSSClass);
					nodeLink.setUserObject(child.getIdent());
					nodeLink.setElementCssClass("o_gotoNode");
				}
			}
		}
		// Add course node to get title etc
		genericPeekViewVC.contextPut("childNodes", childNodes);
		// Add css class for course node type
		CourseNodeFactory courseNodeFactory = CourseNodeFactory.getInstance();
		genericPeekViewVC.contextPut("courseNodeFactory", courseNodeFactory);
		//
		putInitialPanel(genericPeekViewVC);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link nodeLink = (Link) source;
			// get node ID and fire activation event
			String nodeId = (String) nodeLink.getUserObject();
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
		}
	}

}
