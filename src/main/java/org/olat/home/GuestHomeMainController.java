/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.home;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.portal.PortalImpl;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;

/**
 * <!--**************-->
 * <h3>Responsability:</h3>
 * display the first page the guest sees after she logged in successfully.
 * Registered users login have their own
 * {@link org.olat.home.HomeMainController first page} !
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * display guest portal.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3>
 * The guest is a special role inside the learning management system, hence the
 * registered user is handled by a different
 * {@link org.olat.home.HomeMainController controller}.
 * <P>
 * Initial Date: Apr 27, 2004
 * 
 * @author gnaegi
 */
public class GuestHomeMainController extends MainLayoutBasicController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(GuestHomeMainController.class);
	private MenuTree olatMenuTree;
	private VelocityContainer welcome;
	private LayoutMain3ColsController	columnLayoutCtr;
	private PortalImpl myPortal;

	/**
	 * Constructor of the guest home main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public GuestHomeMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		olatMenuTree = new MenuTree("olatMenuTree");
		TreeModel tm = buildTreeModel();
		olatMenuTree.setTreeModel(tm);
		olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		olatMenuTree.addListener(this);

		welcome = createVelocityContainer("guestwelcome");

		// add portal
		myPortal = ((PortalImpl)CoreSpringFactory.getBean("guestportal")).createInstance(getWindowControl(), ureq);
		welcome.put("myPortal", myPortal.getInitialComponent());

		// Activate correct position in menu
		INode firstNode = tm.getRootNode().getChildAt(0);
		olatMenuTree.setSelectedNodeId(firstNode.getIdent());

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, welcome, null);
		listenTo(columnLayoutCtr); // cleanup on dispose
		// add background image to home site
		columnLayoutCtr.addCssClassToMain("o_home");
		
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == olatMenuTree) {
			// process menu commands
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				if (cmd.equals("root") || cmd.equals("guestwelcome")) {
					welcome.setPage(VELOCITY_ROOT + "/guestwelcome.html");
				} else if (cmd.equals("guestinfo")) {
					welcome.setPage(VELOCITY_ROOT + "/guestinfo.html");
				}
			}
		} else {
			logWarn("Unhandled olatMenuTree event: " + event.getCommand(), null);
		}
	}

	private TreeModel buildTreeModel() {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.guest"));
		root.setUserObject("guest");
		root.setAltText(translate("menu.guest.alt"));
		gtm.setRootNode(root);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.guestwelcome"));
		gtn.setUserObject("guestwelcome");
		gtn.setAltText(translate("menu.guestwelcome.alt"));
		root.addChild(gtn);
		root.setDelegate(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.guestinfo"));
		gtn.setUserObject("guestinfo");
		gtn.setAltText(translate("menu.guestinfo.alt"));
		root.addChild(gtn);

		return gtm;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controller disposed by BasicController
		columnLayoutCtr = null;

		// clenup portal
		if (myPortal != null) {
			myPortal.dispose();
			myPortal = null;
		}
	}

}
