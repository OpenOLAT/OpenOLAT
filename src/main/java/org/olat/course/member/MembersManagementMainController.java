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
package org.olat.course.member;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersManagementMainController extends MainLayoutBasicController  implements Activateable2 {
	
	private static final String CMD_MEMBERS = "cmd.members";
	private static final String CMD_GROUPS = "cmd.groups";
	private static final String CMD_BOOKING = "cmd.booking";
	private static final String CMD_RIGHTS = "cmd.rights";

	private final Link back;
	private final MenuTree menuTree;
	private final VelocityContainer mainVC;
	private final LayoutMain3ColsController columnLayoutCtr;

	private OrdersAdminController ordersController;
	private CourseBusinessGroupListController groupsCtrl;
	private MembersOverviewController membersOverviewCtrl;
	
	private final RepositoryEntry repoEntry;
	private final AccessControlModule acModule;

	public MembersManagementMainController(UserRequest ureq, WindowControl wControl, RepositoryEntry re) {
		super(ureq, wControl);
		this.repoEntry = re;
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		
		//logging
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		ICourse course = CourseFactory.loadCourse(re.getOlatResource());
		addLoggingResourceable(LoggingResourceable.wrap(course));
		
		//ui stuff
		menuTree = new MenuTree("menuTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setRootVisible(false);
		menuTree.addListener(this);

		mainVC = createVelocityContainer("main_members");
		back = LinkFactory.createLinkBack(mainVC, this);
		mainVC.put("backLink", back);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, mainVC, "members-" + repoEntry.getKey());
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		selectMenuItem(ureq, CMD_MEMBERS);
	}
	
	private GenericTreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(translate("menu.members"));
		root.setAltText(translate("menu.members.alt"));
		gtm.setRootNode(root);
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.members"), CMD_MEMBERS);
		node.setAltText(translate("menu.members.alt"));
		root.addChild(node);
		
		node = new GenericTreeNode(translate("menu.groups"), CMD_GROUPS);
		node.setAltText(translate("menu.groups.alt"));
		root.addChild(node);

		if(acModule.isEnabled()) {
			node = new GenericTreeNode(translate("menu.orders"), CMD_BOOKING);
			node.setAltText(translate("menu.orders.alt"));
			root.addChild(node);
		}

		node = new GenericTreeNode(translate("menu.rights"), CMD_RIGHTS);
		node.setAltText(translate("menu.rights.alt"));
		root.addChild(node);
		return gtm;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();				
				Object cmd = selTreeNode.getUserObject();
				selectMenuItem(ureq, cmd);
			}
		} else if (source == back) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void selectMenuItem(UserRequest ureq, Object cmd) {
		if(CMD_MEMBERS.equals(cmd)) {
			if(membersOverviewCtrl == null) {
				membersOverviewCtrl = new MembersOverviewController(ureq, getWindowControl(), repoEntry);
				listenTo(membersOverviewCtrl);
			}
			mainVC.put("content", membersOverviewCtrl.getInitialComponent());
		} else if(CMD_GROUPS.equals(cmd)) {
			if(groupsCtrl == null) {
				groupsCtrl = new CourseBusinessGroupListController(ureq, getWindowControl(), repoEntry.getOlatResource());
				listenTo(groupsCtrl);
			}
			groupsCtrl.reloadModel();
			mainVC.put("content", groupsCtrl.getInitialComponent());
		} else if(CMD_BOOKING.equals(cmd)) {
			if(ordersController == null) {
				ordersController = new OrdersAdminController(ureq, getWindowControl(), repoEntry.getOlatResource());
				listenTo(ordersController);
			}
			mainVC.put("content", ordersController.getInitialComponent());
		} else if(CMD_RIGHTS.equals(cmd)) {
			mainVC.put("content", new Panel("empty"));
		}
		
		TreeNode selTreeNode = TreeHelper.findNodeByUserObject(cmd, menuTree.getTreeModel().getRootNode());
		if (selTreeNode != null && !selTreeNode.getIdent().equals(menuTree.getSelectedNodeId())) {
			menuTree.setSelectedNodeId(selTreeNode.getIdent());
		}
	}



	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

	}
	

}
