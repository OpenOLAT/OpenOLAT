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
package org.olat.modules.project.ui;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.ui.InvitationListController;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class ProjMembersManagementController extends MainLayoutBasicController implements Activateable2 {
	
	private static final String CMD_MEMBERS = "Members";
	private static final String CMD_INVITATIONS = "Invitations";

	private final MenuTree menuTree;
	private final VelocityContainer mainVC;
	private final BreadcrumbedStackedPanel stackPanel;
	private LayoutMain3ColsController columnLayoutCtr;

	private ProjMemberListController memberListCtrl;
	private InvitationListController invitationListCtrl;
	
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final boolean invitationsEnabled;
	
	@Autowired
	private InvitationModule invitationModule;

	public ProjMembersManagementController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			ProjProject project, ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.project = project;
		this.secCallback = secCallback;
		
		invitationsEnabled = invitationModule.isProjectInvitationEnabled();
		
		menuTree = new MenuTree("menuTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setRootVisible(false);
		menuTree.addListener(this);
		
		mainVC = createVelocityContainer("members");
		
		if (invitationsEnabled) {
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainVC, "project-" + project.getKey());
			listenTo(columnLayoutCtr);
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		} else {
			putInitialPanel(mainVC);
		}
		
		selectMenuItem(ureq, CMD_MEMBERS);
	}
	
	private GenericTreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(translate("menu.members"));
		root.setAltText(translate("menu.members.alt"));
		gtm.setRootNode(root);
		
		GenericTreeNode node = new GenericTreeNode(translate("members.menu.members"), CMD_MEMBERS);
		root.addChild(node);
		
		if (invitationsEnabled) {
			node = new GenericTreeNode(translate("members.menu.invitations"), CMD_INVITATIONS);
			root.addChild(node);
		}
		
		return gtm;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String)selTreeNode.getUserObject();
				selectMenuItem(ureq, cmd);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		ContextEntry currentEntry = entries.get(0);
		String cmd = currentEntry.getOLATResourceable().getResourceableTypeName();
		Controller selectedCtrl = selectMenuItem(ureq, cmd);
		if (selectedCtrl instanceof Activateable2) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			((Activateable2)selectedCtrl).activate(ureq, subEntries, currentEntry.getTransientState());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == memberListCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	private Controller selectMenuItem(UserRequest ureq, String cmd) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(cmd, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		Controller selectedCtrl = null;
		if (CMD_MEMBERS.equals(cmd)) {
			selectedCtrl = doOpenMembers(ureq, bwControl);
		} else if (CMD_INVITATIONS.equals(cmd)) {
			selectedCtrl = doOpenInvitations(ureq, bwControl);
		}
		
		TreeNode selTreeNode = TreeHelper.findNodeByUserObject(cmd, menuTree.getTreeModel().getRootNode());
		if (selTreeNode != null && !selTreeNode.getIdent().equals(menuTree.getSelectedNodeId())) {
			menuTree.setSelectedNodeId(selTreeNode.getIdent());
		}
		return selectedCtrl;
	}

	private Controller doOpenMembers(UserRequest ureq, WindowControl bwControl) {
		if (memberListCtrl == null) {
			memberListCtrl = new ProjMemberListController(ureq, bwControl, stackPanel, project, secCallback);
			listenTo(memberListCtrl);
		} else {
			memberListCtrl.loadModel();
		}
		mainVC.put("content", memberListCtrl.getInitialComponent());
		return memberListCtrl;
	}
	
	private InvitationListController doOpenInvitations(UserRequest ureq, WindowControl bwControl) {
		if (invitationListCtrl == null) {
			invitationListCtrl = new InvitationListController(ureq, bwControl, project, secCallback.canEditMembers());
			listenTo(invitationListCtrl);
		} else {
			invitationListCtrl.loadModel();
		}
		mainVC.put("content", invitationListCtrl.getInitialComponent());
		return invitationListCtrl;
	}
}
