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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.ui;

import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Description:<br>
 * Show and manage security groups of a repository entry
 * 
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SecurityGroupsRepositoryMainController extends MainLayoutBasicController {
	
	private static final String CMD_CLOSE = "cmd.close";
	private static final String CMD_OWNERS = "owners";
	private static final String CMD_TUTORS = "tutors";
	private static final String CMD_PARTICIPANTS = "participants";
	private static final String CMD_ORDERS = "orders";

	private LayoutMain3ColsController columnLayoutCtr;
	private MenuTree menuTree;
	private ToolController toolC;

	private final Panel mainPanel;
	private SecurityGroupMembersController ownersController;
	private SecurityGroupMembersController tutorsController;
	private SecurityGroupMembersController participantsController;
	private OrdersAdminController ordersController;
	
	private final RepositoryEntry repoEntry;
	private final RepositoryManager rm;
	private final BusinessGroupManager bgm;
	private final BaseSecurity securityManager;
	private final AccessControlModule acModule;
	
	private final boolean mayModifyMembers;
	
	/**
	 * Constructor for the course group management main controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param groupType
	 */
	public SecurityGroupsRepositoryMainController(UserRequest ureq, WindowControl wControl, ICourse course,
			RepositoryEntry repoEntry, boolean mayModifyMembers) {
		super(ureq, wControl);
		
		this.repoEntry = repoEntry;
		this.mayModifyMembers = mayModifyMembers;
		bgm = BusinessGroupManagerImpl.getInstance();
		securityManager = BaseSecurityManager.getInstance();
		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		
		addLoggingResourceable(LoggingResourceable.wrap(course));
		
		//lazy build security groups
		rm = RepositoryManager.getInstance();
		if(repoEntry.getParticipantGroup() == null || repoEntry.getTutorGroup() == null){
			lazyUpdateRepositoryEntry();
		}
		
		// Navigation menu
		menuTree = new MenuTree("menuTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.addListener(this);
		INode firstChild = menuTree.getTreeModel().getRootNode().getChildAt(0);
		if (firstChild != null) {
			menuTree.setSelectedNodeId(firstChild.getIdent());			
		}
		
		//add tools (close)
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		toolC.addHeader(translate("cmd.title"));
		toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");
		
		//now build layout controller
		mainPanel = new Panel("MainSecGroups");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolC.getInitialComponent(), mainPanel, "SecGroups" + repoEntry.getKey());
		listenTo(columnLayoutCtr);
		
		//select the initial controller
		TreeNode initialNode = menuTree.getSelectedNode();
		if(initialNode != null) {
			Object cmd = initialNode.getDelegate() != null ? initialNode.getDelegate().getUserObject() : initialNode.getUserObject(); 
			selectSecurityGroup(ureq, cmd);
		} else {
			mainPanel.setContent(new Panel("empty"));
		}

		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}
	
	private void lazyUpdateRepositoryEntry() {
		if(repoEntry.getTutorGroup() == null){
			rm.createTutorSecurityGroup(repoEntry);
		}
		if(repoEntry.getParticipantGroup() == null) {
			rm.createParticipantSecurityGroup(repoEntry);
		}
		rm.updateRepositoryEntry(repoEntry);
	}

	private GenericTreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(translate("members.title"));
		root.setAltText(translate("members.title.alt"));
		gtm.setRootNode(root);
		
		if(repoEntry.getOwnerGroup() != null) {
			GenericTreeNode node = new GenericTreeNode(translate("members.owners"), CMD_OWNERS);
			node.setAltText(translate("members.owners.alt"));
			root.addChild(node);
			root.setDelegate(node);
		}
		
		if(repoEntry.getTutorGroup() != null) {
			GenericTreeNode node = new GenericTreeNode(translate("members.tutors"), CMD_TUTORS);
			node.setAltText(translate("members.tutors.alt"));
			root.addChild(node);
			if(root.getDelegate() == null) {
				root.setDelegate(node);
			}
		}
		
		if(repoEntry.getParticipantGroup() != null) {
			GenericTreeNode node = new GenericTreeNode(translate("members.participants"), CMD_PARTICIPANTS);
			node.setAltText(translate("members.participants.alt"));
			root.addChild(node);
			if(root.getDelegate() == null) {
				root.setDelegate(node);
			}
		}
		
		if(acModule.isEnabled()) {
			GenericTreeNode node = new GenericTreeNode(translate("menu.orders"), CMD_ORDERS);
			node.setAltText(translate("menu.orders.alt"));
			root.addChild(node);
			if(root.getDelegate() == null) {
				root.setDelegate(node);
			}
		}
		
		return gtm;
	}
	
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();				
				Object cmd = selTreeNode.getUserObject();
				selectSecurityGroup(ureq, cmd);
	
				// visually select delegate
				if (selTreeNode.getDelegate() != null) {
					menuTree.setSelectedNodeId(selTreeNode.getDelegate().getIdent());
				}
			}
		}
	}
	
	private void selectSecurityGroup(UserRequest ureq, Object cmd) {
		if(CMD_OWNERS.equals(cmd)) {
			if(ownersController != null) {
				removeAsListenerAndDispose(ownersController);
			}
			String title = translate("members.owners");
			String info = translate("members.owners.info");
			ownersController = new SecurityGroupMembersController(ureq, getWindowControl(), repoEntry.getOwnerGroup(), title, info, mayModifyMembers, true);
			listenTo(ownersController);
			mainPanel.setContent(ownersController.getInitialComponent());
		} else if(CMD_TUTORS.equals(cmd)) {
			if(tutorsController != null) {
				removeAsListenerAndDispose(tutorsController);
			}
			String title = translate("members.tutors");
			String info = translate("members.tutors.info");
			tutorsController = new SecurityGroupMembersController(ureq, getWindowControl(), repoEntry.getTutorGroup(), title, info, mayModifyMembers, false);
			listenTo(tutorsController);
			mainPanel.setContent(tutorsController.getInitialComponent());
		} else  if(CMD_PARTICIPANTS.equals(cmd)) {
			if(participantsController != null) {
				removeAsListenerAndDispose(participantsController);
			}
			String title = translate("members.participants");
			String info = translate("members.participants.info");
			participantsController = new SecurityGroupMembersController(ureq, getWindowControl(), repoEntry.getParticipantGroup(), title, info, mayModifyMembers, false);
			listenTo(participantsController);
			mainPanel.setContent(participantsController.getInitialComponent());
		} else if (CMD_ORDERS.equals(cmd)) {
			if(ordersController != null) {
				removeAsListenerAndDispose(ordersController);
			}

			ordersController = new OrdersAdminController(ureq, getWindowControl(), repoEntry.getOlatResource());
			listenTo(ordersController);
			mainPanel.setContent(ordersController.getInitialComponent());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == toolC) {
			if (event.getCommand().equals(CMD_CLOSE)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(event instanceof IdentitiesAddEvent) {
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
			SecurityGroup secGroup = null;
			if (source == ownersController) {
				secGroup = repoEntry.getOwnerGroup();
			} else if (source == tutorsController) {
				secGroup = repoEntry.getTutorGroup();
			} else if (source == participantsController) {
				secGroup = repoEntry.getParticipantGroup();							
			}
			BusinessGroupAddResponse response = bgm.addToSecurityGroupAndFireEvent(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), secGroup);
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());			
			fireEvent(ureq, Event.CHANGED_EVENT );
		} else if(event instanceof IdentitiesRemoveEvent) {
			IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent)event;
			List<Identity> identitiesToRemove = identitiesRemoveEvent.getRemovedIdentities();
			if (source == ownersController) {
				SecurityGroup ownerGroup = repoEntry.getOwnerGroup();
				bgm.removeAndFireEvent(ureq.getIdentity(), identitiesToRemove, ownerGroup);
			} else if (source == tutorsController) {
				SecurityGroup tutorGroup = repoEntry.getTutorGroup();
				bgm.removeAndFireEvent(ureq.getIdentity(), identitiesToRemove, tutorGroup);
				removeTutors(identitiesToRemove);
			} else if (source == participantsController) {
				SecurityGroup participantGroup = repoEntry.getParticipantGroup();	
				bgm.removeAndFireEvent(ureq.getIdentity(), identitiesToRemove, participantGroup);	
				removeParticipants(identitiesToRemove);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void removeTutors(List<Identity> identitiesToRemove) {
		for(BusinessGroup group:getCourseGroups()) {
			for(Identity identityToRemove : identitiesToRemove) {
				if(securityManager.isIdentityInSecurityGroup(identityToRemove, group.getOwnerGroup())) {
					securityManager.removeIdentityFromSecurityGroup(identityToRemove, group.getOwnerGroup());
				}
			}
		}
	}
	
	private void removeParticipants(List<Identity> identitiesToRemove) {
		for(BusinessGroup group:getCourseGroups()) {
			for(Identity identityToRemove : identitiesToRemove) {
				if(securityManager.isIdentityInSecurityGroup(identityToRemove, group.getPartipiciantGroup())) {
					securityManager.removeIdentityFromSecurityGroup(identityToRemove, group.getPartipiciantGroup());
				}
			}
		}
	}
	
	private List<BusinessGroup> getCourseGroups() {
		if("CourseModule".equals(repoEntry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(repoEntry.getOlatResource());
			CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
			List<BusinessGroup> groups = gm.getAllLearningGroupsFromAllContexts();
			return groups;
		}
		return Collections.emptyList();
	}
}