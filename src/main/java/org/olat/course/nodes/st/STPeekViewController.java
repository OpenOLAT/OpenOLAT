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
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.CourseStyleService;
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
	private CourseStyleService courseStyleService;
	
	public STPeekViewController(UserRequest ureq, WindowControl wControl, CourseTreeNode courseTreeNode, CourseConfig courseConfig, boolean large) {
		super(ureq, wControl);
		ColorCategoryResolver colorCategoryResolver = courseStyleService.getColorCategoryResolver(null,
				courseConfig.getColorCategoryIdentifier());
		
		genericPeekViewVC = createVelocityContainer("stPeekView");
		
		List<CourseNodeWrapper> wrappers = new ArrayList<>();
		for (int i = 0; i < courseTreeNode.getChildCount(); i++) {
			INode childNode = courseTreeNode.getChildAt(i);
			if (childNode instanceof CourseTreeNode) {
				CourseTreeNode childTreeNode = (CourseTreeNode) childNode;
				if (childTreeNode.isVisible() && childTreeNode.isAccessible()) {
					CourseNode child = childTreeNode.getCourseNode();
					CourseNodeWrapper wrapper = new CourseNodeWrapper();
					
					Link nodeLink = LinkFactory.createLink("nodeLink_" + child.getIdent(), genericPeekViewVC, this);
					nodeLink.setCustomDisplayText(StringHelper.escapeHtml(child.getShortTitle()));
					String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(child.getType()).getIconCSSClass();
					nodeLink.setIconLeftCSS("o_icon o_icon-fw " + iconCSSClass);
					nodeLink.setUserObject(child.getIdent());
					nodeLink.setElementCssClass("o_gotoNode");
					wrapper.setNodeLinkName(nodeLink.getComponentName());
					
					wrapper.setDescription(child.getDescription());
					
					String colorCategoryCss = colorCategoryResolver.getColorCategoryCss(child);
					wrapper.setColorCategoryCss(colorCategoryCss);
					
					wrappers.add(wrapper)
;				}
			}
		}
		genericPeekViewVC.contextPut("items", wrappers);
		genericPeekViewVC.contextPut("large", Boolean.valueOf(large));
		
		putInitialPanel(genericPeekViewVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link nodeLink = (Link) source;
			String nodeId = (String) nodeLink.getUserObject();
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
		}
	}
	
	public static final class CourseNodeWrapper {
		
		private String nodeLinkName;
		private String description;
		private String colorCategoryCss;
		
		public String getNodeLinkName() {
			return nodeLinkName;
		}
		
		public void setNodeLinkName(String nodeLinkName) {
			this.nodeLinkName = nodeLinkName;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getColorCategoryCss() {
			return colorCategoryCss;
		}
		
		public void setColorCategoryCss(String colorCategoryCss) {
			this.colorCategoryCss = colorCategoryCss;
		}
		
	}

}
