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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * <h3>Description:</h3>
 * <p>
 * The PeekViewWrapperController displays the course node type icon, the node
 * title and description and an optional peekview controller if provided.
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 15.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class PeekViewWrapperController extends BasicController {

	private final Link nodeLink;
	private Controller peekViewController;
	
	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 * @param peekViewController an optional peek view implementation for this
	 *          node or NULL if not available
	 */
	public PeekViewWrapperController(UserRequest ureq, WindowControl wControl,
			CourseNode courseNode, Controller peekViewController, boolean accessible) {
		super(ureq, wControl);

		VelocityContainer peekViewWrapperVC = createVelocityContainer("peekViewWrapper");
		peekViewWrapperVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		// Add course node to get title etc
		peekViewWrapperVC.contextPut("coursenode", courseNode);
		peekViewWrapperVC.contextPut("accessible", Boolean.valueOf(accessible));
		// Add link to jump to course node
		nodeLink = LinkFactory.createLink("nodeLink", peekViewWrapperVC, this);
		nodeLink.setCustomDisplayText(StringHelper.escapeHtml(courseNode.getShortTitle()));
		// Add css class for course node type
		String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		nodeLink.setIconLeftCSS("o_icon o_icon-fw " + iconCSSClass);
		nodeLink.setUserObject(courseNode.getIdent());
		nodeLink.setElementCssClass("o_gotoNode");
		// Add optional peekViewController
		if (accessible && peekViewController != null) {
			this.peekViewController = peekViewController;
			peekViewWrapperVC.put("peekViewController", this.peekViewController.getInitialComponent());
			// register for auto cleanup on dispose
			listenTo(this.peekViewController);
		}
		putInitialPanel(peekViewWrapperVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == nodeLink) {
			// get node ID and fire activation event
			String nodeID = (String) nodeLink.getUserObject();
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeID));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == peekViewController) {
			// forward event to ST controller
			fireEvent(ureq, event);
		}
	}
}
