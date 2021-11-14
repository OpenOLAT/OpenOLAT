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

package org.olat.core.gui.control.generic.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * <h3>Description:</h3>
 * The BreadCrumbController implements a simple bred crumb navigation. This type
 * of navigation is useful when users can launch loosely coupled work-flows that
 * don't span up in a new tab and that are not modal.
 * <p>
 * Don't use it to implement wizard like work-flows. Use the StepsController for
 * this, the StepsController offers a modal wizard infrastructure.
 * <p>
 * When a controller in the bread crumb path is re-activated by the user, the
 * child crumb controller of the activated crumb is disposed.
 * <p>
 * Initial Date: 08.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class BreadCrumbController extends BasicController {
	public static final Event CRUMB_VIEW_ACTIVATED = new Event("CRUMB_VIEW_ACTIVATED");
	private VelocityContainer breadCrumbVC;
	private List<Link> breadCrumbLinks;

	/**
	 * Constructor to create a bread crumb navigation controller. Use the
	 * activateFirstCrumbController() method to add the first crumb controller to
	 * the crumb stack
	 * 
	 * @param ureq
	 * @param control
	 */
	public BreadCrumbController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		breadCrumbVC = createVelocityContainer("breadCrumb");
		breadCrumbLinks = new ArrayList<>();
		breadCrumbVC.contextPut("breadCrumbs", breadCrumbLinks);
		putInitialPanel(breadCrumbVC);
	}

	/**
	 * Add the first crumb controller to the crumb stack. To add followup crumbs
	 * to the stack you must use the
	 * crumbController.activateAndListenToChildCrumbController() method
	 * 
	 * @param firstCrumbController The crumb controller that serves as the home
	 *          crumb
	 */
	public void activateFirstCrumbController(CrumbController firstCrumbController) {
		firstCrumbController.setBreadCrumbController(this);
		putToBreadCrumbStack(firstCrumbController);
	}

	/**
	 * Put a crumb controller with it's view to the bread crumb stack. Use the
	 * crumbController.activateAndListenToChildCrumbController() to put new crumbs
	 * to the stack in your code
	 * 
	 * @param crumbController
	 */
	void putToBreadCrumbStack(CrumbController crumbController) {
		// re-enable last link
		if (breadCrumbLinks.size() > 0) breadCrumbLinks.get(breadCrumbLinks.size() - 1).setEnabled(true);
		// create new link for this crumb and add it to data model
		String cmd = "crumb-" + breadCrumbLinks.size();
		Link link = LinkFactory.createCustomLink(cmd, cmd, cmd, Link.NONTRANSLATED, breadCrumbVC, this);
		link.setCustomDisplayText(crumbController.getCrumbLinkText());			
		link.setTitle(crumbController.getCrumbLinkHooverText());
		link.setUserObject(crumbController);
		link.setEnabled(false);
		breadCrumbLinks.add(link);
		breadCrumbVC.put("content", crumbController.getInitialComponent());
		// set bread crumb navigation controller
		crumbController.setBreadCrumbController(this);
	}

	/**
	 * Reset all texts on the crumb path
	 */
	public void resetCrumbTexts() {
		for (Link link : breadCrumbLinks) {
			CrumbController crumbController = (CrumbController) link.getUserObject();
			link.setCustomDisplayText(crumbController.getCrumbLinkText());			
			link.setTitle(crumbController.getCrumbLinkHooverText());
		}
	}
	
	/**
	 * Remove a crumb controller and all it's child controllers that are created
	 * by this controller or it's children from the bread crumb stack and calls
	 * dispose on the crumb controller. Use
	 * crumbController.removeFromBreadCrumbPathAndDispose() if you manually want
	 * to remove a crumb controller from the bread crumb
	 * 
	 * @param crumbController
	 */
	void removeFromBreadCrumb(CrumbController crumbController) {
		int activateLinkPos = 0;
		for (Link link : breadCrumbLinks) {
			CrumbController linkController = (CrumbController) link.getUserObject();
			if (linkController.equals(crumbController)) {
				linkController.deactivateAndDisposeChildCrumbController();
				linkController.dispose();
				break;
			}
			activateLinkPos++;
		}
		if (activateLinkPos > 0) {
			// remove children elements from list and reput to hibernate
			breadCrumbLinks = breadCrumbLinks.subList(0, activateLinkPos);
			breadCrumbVC.contextPut("breadCrumbs", breadCrumbLinks);
			// disable current link and update content view from current controller
			Link parentLink = breadCrumbLinks.get(activateLinkPos - 1);
			parentLink.setEnabled(false);
			CrumbController parentController = (CrumbController) parentLink.getUserObject();
			breadCrumbVC.put("content", parentController.getInitialComponent());
		}
	}

	@Override
	protected void doDispose() {
		if (breadCrumbLinks.size() > 0) {
			removeFromBreadCrumb((CrumbController) breadCrumbLinks.get(0).getUserObject());
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		for (Link link : breadCrumbLinks) {
			if (source == link) {
				// set content to new controller view and disable the corresponding link
				link.setEnabled(false);
				CrumbController crumbController = (CrumbController) link.getUserObject();
				breadCrumbVC.put("content", crumbController.getInitialComponent());
				// remove all children from this new controller
				CrumbController childCrumb = crumbController.getChildCrumbController();
				if (childCrumb != null) {
					removeFromBreadCrumb(childCrumb);
				}
				// manually fire an event to the crumb controller
				crumbController.dispatchEvent(ureq, this, CRUMB_VIEW_ACTIVATED);
				break;
			} else {
				link.setEnabled(true);
			}
		}
	}
}
