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
package org.olat.group.ui.homepage;

import java.util.ArrayList;
import java.util.List;

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The main controller for the group business card.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupInfoMainController extends MainLayoutBasicController implements Activateable2 {
	
	public final static String COMMAND_MENU_GROUPINFO = "COMMAND_MENU_GROUPINFO";
	public final static String COMMAND_MENU_GROUPMEMBERS = "COMMAND_MENU_GROUPMEMBERS";
	public final static String COMMAND_MENU_GROUPCONTACT = "COMMAND_MENU_GROUPCONTACT";
	public final static String COMMAND_MENU_GROUPCALENDAR = "COMMAND_MENU_GROUPCALENDAR";
	
	/** The three columns layout controller */
	private LayoutMain3ColsController layoutController;
	
	private CollaborationTools tools;
	/** The business group we're dealing with */
	private BusinessGroup businessGroup;
	
	/** The navigation tree */
	private MenuTree menuTree;
	
	private final GroupInfoDisplayController groupInfoDisplayController;
	private GroupMembersDisplayController groupMembersDisplayController;
	private GroupContactController groupContactController;
	private WeeklyCalendarController calendarController;
	
	@Autowired
	private BusinessGroupModule module;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;
	
	public GroupInfoMainController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		// Initialize
		super(ureq, wControl);
		this.businessGroup = businessGroup;

		menuTree = new MenuTree("menuTree");
		menuTree.setRootVisible(false);
		menuTree.setTreeModel(buildTreeModel());
		menuTree.addListener(this);
		menuTree.setSelectedNode(menuTree.getTreeModel().getRootNode().getDelegate());
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, wControl);
		groupInfoDisplayController = new GroupInfoDisplayController(ureq, bwControl, businessGroup);
		listenTo(groupInfoDisplayController);
		
		layoutController = new LayoutMain3ColsController(ureq, wControl, menuTree, groupInfoDisplayController.getInitialComponent(), "group_card");
		layoutController.addCssClassToMain("o_groups");
		listenTo(layoutController);
		
		putInitialPanel(layoutController.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			addToHistory(ureq);
			return;
		}
		
		ContextEntry entry = entries.get(0);
		String menuItem = entry.getOLATResourceable().getResourceableTypeName();
		
		Object nodeUserObject = null;
		Controller selectedController = null;
		if("Infos".equals(menuItem)) {
			selectedController = getInfosController(ureq);
			nodeUserObject = COMMAND_MENU_GROUPINFO;
		} else if("Contact".equals(menuItem)) {
			selectedController = getContactController(ureq);
			nodeUserObject = COMMAND_MENU_GROUPCONTACT;
		} else if("Members".equals(menuItem)) {
			selectedController = getMembersController(ureq);
			nodeUserObject = COMMAND_MENU_GROUPMEMBERS;
		}

		if(selectedController != null) {
			TreeNode node = TreeHelper.findNodeByUserObject(nodeUserObject, menuTree.getTreeModel().getRootNode());
			if(node != null) {
				menuTree.setSelectedNode(node);
			}
			addToHistory(ureq, selectedController);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selectedNode = menuTree.getSelectedNode();
				String command = (String) selectedNode.getUserObject();
				if (command == null) {
					// use the delegate if available
					selectedNode = menuTree.getSelectedNode().getDelegate(); 
					if (selectedNode != null) {
						menuTree.setSelectedNode(selectedNode);
						command = (String) selectedNode.getUserObject();				
					}
					if (command == null) {
						command = COMMAND_MENU_GROUPINFO; // fallback
					}
				}
				if (command.equals(COMMAND_MENU_GROUPINFO)) {
					getInfosController(ureq);
				} else if (command.equals(COMMAND_MENU_GROUPMEMBERS)) {
					getMembersController(ureq);
				} else if (command.equals(COMMAND_MENU_GROUPCONTACT)) {
					getContactController(ureq);
				} else if (command.equals(COMMAND_MENU_GROUPCALENDAR)) {
					getCalendarController(ureq);
				}
			}
		}
	}
	
	private GroupInfoDisplayController getInfosController(UserRequest ureq) {
		layoutController.setCol3(groupInfoDisplayController.getInitialComponent());
		addToHistory(ureq, groupInfoDisplayController);
		return groupInfoDisplayController;
	}
	
	private GroupMembersDisplayController getMembersController(UserRequest ureq) {
		if(groupMembersDisplayController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Members", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			groupMembersDisplayController = new GroupMembersDisplayController(ureq, bwControl, businessGroup);
			listenTo(groupMembersDisplayController);
		}
		
		layoutController.setCol3(groupMembersDisplayController.getInitialComponent());
		addToHistory(ureq, groupMembersDisplayController);
		return groupMembersDisplayController;
	}
	
	private GroupContactController getContactController(UserRequest ureq) {
		if(groupContactController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Contact", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			groupContactController = new GroupContactController(ureq, bwControl, businessGroup);
			listenTo(groupContactController);
		}
		
		layoutController.setCol3(groupContactController.getInitialComponent());
		addToHistory(ureq, groupContactController);
		return groupContactController;
	}
	
	private WeeklyCalendarController getCalendarController(UserRequest ureq) {
		if(calendarController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendar", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			List<KalendarRenderWrapper> calendarWrappers = new ArrayList<>(2);
			KalendarRenderWrapper groupCalendar = calendarManager.getGroupCalendar(businessGroup);
			groupCalendar.setPrivateEventsVisible(false);
			calendarWrappers.add(groupCalendar);
			calendarController = new WeeklyCalendarController(ureq, bwControl, calendarWrappers,
					WeeklyCalendarController.CALLER_COLLAB, businessGroup, false);
			listenTo(calendarController);
		}
		
		layoutController.setCol3(calendarController.getInitialComponent());
		addToHistory(ureq, calendarController);
		return calendarController;
	}
	
	private TreeModel buildTreeModel() {
		// Builds the model for the navigation tree
		GenericTreeModel treeModel = new GenericTreeModel();
		GenericTreeNode rootNode = new GenericTreeNode();
		
		rootNode.setTitle(translate("main.menu.title"));
		treeModel.setRootNode(rootNode);
		
		GenericTreeNode childNode = new GenericTreeNode();
		childNode.setTitle(translate("main.menu.title"));
		childNode.setUserObject(COMMAND_MENU_GROUPINFO);
		childNode.setCssClass("o_sel_groupcard_infos");
		childNode.setSelected(true);
		rootNode.addChild(childNode);
		rootNode.setDelegate(childNode);
		
		if(calendarModule.isEnableGroupCalendar() && isCalendarEnabled()) {
			childNode = new GenericTreeNode();
			childNode.setTitle(translate("main.menu.calendar"));
			childNode.setUserObject(COMMAND_MENU_GROUPCALENDAR);
			childNode.setCssClass("o_sel_groupcard_calendar");
			rootNode.addChild(childNode);
		}
		
		if(businessGroup.isOwnersVisiblePublic() || businessGroup.isParticipantsVisiblePublic() || businessGroup.isWaitingListVisiblePublic()) {
			childNode = new GenericTreeNode();
			childNode.setTitle(translate("main.menu.members"));
			childNode.setUserObject(COMMAND_MENU_GROUPMEMBERS);
			childNode.setCssClass("o_sel_groupcard_members");
			rootNode.addChild(childNode);
		}
		
		if(isContactEnabled()) {
			childNode = new GenericTreeNode();
			childNode.setTitle(translate("main.menu.contact"));
			childNode.setUserObject(COMMAND_MENU_GROUPCONTACT);
			childNode.setCssClass("o_sel_groupcard_contact");
			rootNode.addChild(childNode);
		}
		
		return treeModel;
	}
	
	private boolean isCalendarEnabled() {
		if(tools == null) {
			tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		}
		return tools == null ? false : tools.isToolEnabled(CollaborationTools.TOOL_CALENDAR);
	}
	
	private boolean isContactEnabled() {
		String contactConfig = module.getContactBusinessCard();
		if(BusinessGroupModule.CONTACT_BUSINESS_CARD_ALWAYS.equals(contactConfig)) {
			return true;
		}
		if(BusinessGroupModule.CONTACT_BUSINESS_CARD_GROUP_CONFIG.equals(contactConfig)) {
			if(tools == null) {
				tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
			}
			return tools == null ? false : tools.isToolEnabled(CollaborationTools.TOOL_CONTACT);
		}
		return false;
	}
}
