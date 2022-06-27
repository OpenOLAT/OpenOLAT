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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.disclaimer.ui.CourseDisclaimerConsentOverviewController;
import org.olat.course.groupsandrights.GroupsAndRightsController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberListSecurityCallbackFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the members management view. The controller is made for the course administrator
 * and doesn't make any security check. Especially, the course authors can access the business groups
 * without being members of these groups.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersManagementMainController extends MainLayoutBasicController implements Activateable2 {
	
	private static final String CMD_MEMBERS = "Members";
	private static final String CMD_GROUPS = "Groups";
	private static final String CMD_BOOKING = "Booking";
	private static final String CMD_RIGHTS = "Rights";
	private static final String CMD_CONSENTS = "Consents";

	private final MenuTree menuTree;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	private final LayoutMain3ColsController columnLayoutCtr;

	private OrdersAdminController ordersController;
	private CourseBusinessGroupListController groupsCtrl;
	private MembersOverviewController membersOverviewCtrl;
	private GroupsAndRightsController rightsController;
	private CourseDisclaimerConsentOverviewController disclaimerController;
	
	private boolean membersDirty;
	private RepositoryEntry repoEntry;
	private final UserCourseEnvironment coachCourseEnv;
	
	private final boolean entryAdmin;
	private final boolean principal;
	private final boolean groupManagementRight;
	private final boolean memberManagementRight;
	private final MemberListSecurityCallback secCallback;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired 
	private CourseModule courseModule;

	public MembersManagementMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry re, UserCourseEnvironment coachCourseEnv, boolean entryAdmin, boolean principal,
			boolean groupManagementRight, boolean memberManagementRight) {
		super(ureq, wControl);
		this.repoEntry = re;
		this.toolbarPanel = toolbarPanel;
		this.entryAdmin = entryAdmin;
		this.principal = principal;
		this.groupManagementRight = groupManagementRight;
		this.memberManagementRight = memberManagementRight;
		this.coachCourseEnv = coachCourseEnv;
		secCallback = MemberListSecurityCallbackFactory.getSecurityCallback(coachCourseEnv.isCourseReadOnly(),
				entryAdmin || groupManagementRight || memberManagementRight);

		//logging
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		ICourse course = CourseFactory.loadCourse(re);
		addLoggingResourceable(LoggingResourceable.wrap(course));
		
		//ui stuff
		menuTree = new MenuTree("menuTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setRootVisible(false);
		menuTree.addListener(this);

		mainVC = createVelocityContainer("main_members");

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainVC, "members-" + repoEntry.getKey());
		columnLayoutCtr.addCssClassToMain("o_members_mgmt");
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		if(entryAdmin || principal || memberManagementRight) {
			selectMenuItem(ureq, CMD_MEMBERS);
		} else if(groupManagementRight) {
			selectMenuItem(ureq, CMD_GROUPS);
		}
	}
	
	private GenericTreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(translate("menu.members"));
		root.setAltText(translate("menu.members.alt"));
		gtm.setRootNode(root);
		
		if(entryAdmin || principal || memberManagementRight) {
			GenericTreeNode node = new GenericTreeNode(translate("menu.members"), CMD_MEMBERS);
			node.setAltText(translate("menu.members.alt"));
			node.setCssClass("o_sel_membersmgt_members");
			root.addChild(node);
		}

		if(entryAdmin || principal || memberManagementRight || groupManagementRight) {
			GenericTreeNode node = new GenericTreeNode(translate("menu.groups"), CMD_GROUPS);
			node.setAltText(translate("menu.groups.alt"));
			node.setCssClass("o_sel_membersmgt_groups");
			root.addChild(node);
		}

		if(acModule.isEnabled() && (entryAdmin || principal ||  memberManagementRight)) {
			//check if the course is managed and/or has offers
			if(!RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.bookings)
					|| (repoEntry.isPublicVisible() && acService.isResourceAccessControled(repoEntry.getOlatResource(), null))) {
				GenericTreeNode node = new GenericTreeNode(translate("menu.orders"), CMD_BOOKING);
				node.setAltText(translate("menu.orders.alt"));
				node.setCssClass("o_sel_membersmgt_orders");
				root.addChild(node);
			}
		}

		if(entryAdmin || principal) {
			GenericTreeNode node = new GenericTreeNode(translate("menu.rights"), CMD_RIGHTS);
			node.setAltText(translate("menu.rights.alt"));
			node.setCssClass("o_sel_membersmgt_rights");
			root.addChild(node);
		}
		
		if ((entryAdmin || principal || memberManagementRight || groupManagementRight)
				&& courseModule.isDisclaimerEnabled() && coachCourseEnv.getCourseEnvironment().getCourseConfig().isDisclaimerEnabled()) {
			GenericTreeNode node = new GenericTreeNode(translate("menu.consents"), CMD_CONSENTS);
			node.setAltText("menu.consents.alt");
			node.setCssClass("o_sel_memebersmgt_consents");
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				membersDirty = true;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry currentEntry = entries.get(0);
		String cmd = currentEntry.getOLATResourceable().getResourceableTypeName();
		Controller selectedCtrl = selectMenuItem(ureq, cmd);
		if(selectedCtrl instanceof Activateable2) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			((Activateable2)selectedCtrl).activate(ureq, subEntries, currentEntry.getTransientState());
		}
	}
	
	private Controller selectMenuItem(UserRequest ureq, String cmd) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(cmd, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		Controller selectedCtrl = null;
		if(CMD_MEMBERS.equals(cmd)) {
			if(entryAdmin ||  principal || memberManagementRight) {
				if(membersOverviewCtrl == null) {
					membersOverviewCtrl = new MembersOverviewController(ureq, bwControl, toolbarPanel, repoEntry, coachCourseEnv, secCallback);
					listenTo(membersOverviewCtrl);
				} else if(membersDirty) {
					membersOverviewCtrl.reloadMembers();
				}
				mainVC.put("content", membersOverviewCtrl.getInitialComponent());
				selectedCtrl = membersOverviewCtrl;
			}
		} else if(CMD_GROUPS.equals(cmd)) {
			if(entryAdmin ||  principal || memberManagementRight || groupManagementRight) {
				if(groupsCtrl == null) {
					groupsCtrl = new CourseBusinessGroupListController(ureq, bwControl, repoEntry, entryAdmin || groupManagementRight, coachCourseEnv.isCourseReadOnly());
					listenTo(groupsCtrl);
				}
				groupsCtrl.reloadModel();
				mainVC.put("content", groupsCtrl.getInitialComponent());
				selectedCtrl = groupsCtrl;
			}
		} else if(CMD_BOOKING.equals(cmd)) {
			if(acModule.isEnabled() && (entryAdmin ||  principal || memberManagementRight)) {
				if(ordersController == null) {
					ordersController = new OrdersAdminController(ureq, bwControl, toolbarPanel, repoEntry.getOlatResource());
					listenTo(ordersController);
				}
				mainVC.put("content", ordersController.getInitialComponent());
				selectedCtrl = ordersController;
			}
		} else if(CMD_RIGHTS.equals(cmd)) {
			if(entryAdmin ||  principal) {
				if(rightsController == null) {
					rightsController = new GroupsAndRightsController(ureq, bwControl, repoEntry, coachCourseEnv.isCourseReadOnly());
					listenTo(rightsController);
				}
				mainVC.put("content", rightsController.getInitialComponent());
				selectedCtrl = rightsController;
			}
		} else if (CMD_CONSENTS.equals(cmd)) {
			if ((entryAdmin || principal || memberManagementRight || groupManagementRight) && courseModule.isDisclaimerEnabled()) {
				if(disclaimerController == null) {
					disclaimerController = new CourseDisclaimerConsentOverviewController(ureq, bwControl, repoEntry, toolbarPanel, entryAdmin || memberManagementRight || groupManagementRight);
					listenTo(disclaimerController);
				} else {
					disclaimerController.loadModel();
				}
				mainVC.put("content", disclaimerController.getInitialComponent());
				selectedCtrl = disclaimerController;
			}
		}
		
		TreeNode selTreeNode = TreeHelper.findNodeByUserObject(cmd, menuTree.getTreeModel().getRootNode());
		if (selTreeNode != null && !selTreeNode.getIdent().equals(menuTree.getSelectedNodeId())) {
			menuTree.setSelectedNodeId(selTreeNode.getIdent());
		}
		return selectedCtrl;
	}
}
