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

package org.olat.group.ui.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.edit.BusinessGroupEditController;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.lifecycle.InactiveMessageController;
import org.olat.instantMessaging.CloseInstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRunController;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.ui.TeamsMeetingsRunController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiModule;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.AccessListController;
import org.olat.resource.accesscontrol.ui.OrdersAdminController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description: <BR>
 * Runtime environment for a business group. Use the BGControllerFactory and not
 * the constructor to create an instance of this controller.
 * <P>
 * 
 * @version Initial Date: Aug 11, 2004
 * @author patrick
 */

public class BusinessGroupMainRunController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {

	private static final String INITVIEW_TOOLFOLDER = "toolfolder";
	public static final OLATResourceable ORES_TOOLFOLDER = OresHelper.createOLATResourceableType(INITVIEW_TOOLFOLDER);	
	
	private static final String INITVIEW_TOOLFORUM = "toolforum";
	public static final OLATResourceable ORES_TOOLFORUM = OresHelper.createOLATResourceableType(INITVIEW_TOOLFORUM);
	
	public static final OLATResourceable ORES_TOOLCHAT = OresHelper.createOLATResourceableType("toolchat");
	public static final OLATResourceable ORES_TOOLCAL = OresHelper.createOLATResourceableType("toolcalendar");
	public static final OLATResourceable ORES_TOOLMSG = OresHelper.createOLATResourceableType("toolmsg");
	public static final OLATResourceable ORES_TOOLADMIN = OresHelper.createOLATResourceableType("tooladmin");
	public static final OLATResourceable ORES_TOOLCONTACT = OresHelper.createOLATResourceableType("toolcontact");
	public static final OLATResourceable ORES_TOOLMEMBERS = OresHelper.createOLATResourceableType("toolmembers");
	public static final OLATResourceable ORES_TOOLRESOURCES = OresHelper.createOLATResourceableType("toolresources");
	public static final OLATResourceable ORES_TOOLPORTFOLIO = OresHelper.createOLATResourceableType("toolportfolio");
	public static final OLATResourceable ORES_TOOLBOOKING = OresHelper.createOLATResourceableType("toolbooking");
	public static final OLATResourceable ORES_TOOLTEAMS = OresHelper.createOLATResourceableType("toolteams");
	public static final OLATResourceable ORES_TOOLOPENMEETINGS = OresHelper.createOLATResourceableType("toolopenmeetings");
	public static final OLATResourceable ORES_TOOLADOBECONNECT = OresHelper.createOLATResourceableType("tooladobeconnect");
	public static final OLATResourceable ORES_TOOLBIGBLUEBUTTON = OresHelper.createOLATResourceableType("toolbigbluebutton");
	public static final OLATResourceable ORES_TOOLWIKI = OresHelper.createOLATResourceableType(WikiManager.WIKI_RESOURCE_FOLDER_NAME);

	// activity identifiers are used as menu user objects and for the user
	// activity events
	// change value with care, used in logfiles etc!!
	/** activity identifier: user selected overview in menu * */
	public static final String ACTIVITY_MENUSELECT_OVERVIEW = "MENU_OVERVIEW";
	/** activity identifier: user selected information in menu * */
	public static final String ACTIVITY_MENUSELECT_INFORMATION = "MENU_INFORMATION";
	/** activity identifier: user selected memberlist in menu * */
	public static final String ACTIVITY_MENUSELECT_MEMBERSLIST = "MENU_MEMBERLIST";
	/** activity identifier: user selected contactform in menu * */
	public static final String ACTIVITY_MENUSELECT_CONTACTFORM = "MENU_CONTACTFORM";
	/** activity identifier: user selected forum in menu * */
	public static final String ACTIVITY_MENUSELECT_FORUM = "MENU_FORUM";
	/** activity identifier: user selected folder in menu * */
	public static final String ACTIVITY_MENUSELECT_FOLDER = "MENU_FOLDER";
	/** activity identifier: user selected chat in menu * */
	public static final String ACTIVITY_MENUSELECT_CHAT = "MENU_CHAT";
	/** activity identifier: user selected calendar in menu * */
	public static final String ACTIVITY_MENUSELECT_CALENDAR = "MENU_CALENDAR";
	/** activity identifier: user selected administration in menu * */
	public static final String ACTIVITY_MENUSELECT_ADMINISTRATION = "MENU_ADMINISTRATION";
	/** activity identifier: user selected show resources in menu * */
	public static final String ACTIVITY_MENUSELECT_SHOW_RESOURCES = "MENU_SHOW_RESOURCES";
	public static final String ACTIVITY_MENUSELECT_WIKI = "MENU_SHOW_WIKI";
	/* activity identifier: user selected show portoflio in menu */
	public static final String ACTIVITY_MENUSELECT_PORTFOLIO = "MENU_SHOW_PORTFOLIO";
	/* activity identifier: user selected show OPENMEETINGS in menu */
	public static final String ACTIVITY_MENUSELECT_OPENMEETINGS = "MENU_SHOW_OPENMEETINGS";
	/* activity identifier: user selected show Adobe Connect in menu */
	public static final String ACTIVITY_MENUSELECT_ADOBECONNECT = "MENU_SHOW_ADOBECONNECT";
	/* activity identifier: user selected show BigBlueButton in menu */
	public static final String ACTIVITY_MENUSELECT_BIGBLUEBUTTON = "MENU_SHOW_BIGBLUEBUTTON";
	/* activity identifier: user selected show Teams in menu */
	public static final String ACTIVITY_MENUSELECT_TEAMS = "MENU_SHOW_TEAMS";
	/* activity identifier: user selected show access control in menu */
	/* access control of resources */
	public static final String ACTIVITY_MENUSELECT_AC = "MENU_SHOW_AC";

	private Panel mainPanel;
	private VelocityContainer main;
	private VelocityContainer contactFromVC;
	private final TooledStackedPanel toolbarPanel;

	private BusinessGroup businessGroup;
	private OLATResourceable businessGroupOres;

	private MenuTree bgTree;
	private LayoutMain3ColsController columnLayoutCtr;

	private Controller collabToolCtr;
	
	private BusinessGroupEditController bgEditCntrllr;
	private Controller bgACHistoryCtrl;
	private BusinessGroupResourceController resourcesCtr;
	private GroupMembersRunController groupMembersToggleViewController;
	private InactiveMessageController warningCtrl;

	private BusinessGroupSendToChooserForm sendToChooserForm;

	/**
	 * Business group administrator
	 */
	private boolean isAdmin;
	/**
	 * Group manager of OLAT administrator.
	 */
	private boolean isGroupsAdmin;
	
	private boolean readOnly;
	private final UserSession usess;

	private EventBus singleUserEventBus;
	private String adminNodeId; // reference to admin menu item
	private HistoryPoint launchedFromPoint;

	// not null indicates tool is enabled
	private final String nodeIdPrefix;
	private GenericTreeNode nodeFolder;
	private GenericTreeNode nodeForum;
	private GenericTreeNode nodeWiki;
	private GenericTreeNode nodeCal;
	private GenericTreeNode nodePortfolio;
	private GenericTreeNode nodeOpenMeetings;
	private GenericTreeNode nodeAdobeConnect;
	private GenericTreeNode nodeBigBlueButton;
	private GenericTreeNode nodeTeams;
	private GenericTreeNode nodeContact;
	private GenericTreeNode nodeGroupOwners;
	private GenericTreeNode nodeResources;
	private GenericTreeNode nodeInformation;
	private GenericTreeNode nodeAdmin;
	private boolean groupRunDisabled;
	private OLATResourceable assessmentEventOres;
	private Controller accessController;
	
	private boolean needActivation;
	private boolean chatAvailable;
	private boolean wildcard;
	
	@Autowired
	private ACService acService;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BusinessGroupService businessGroupService;	

	/**
	 * Do not use this constructor! Use the BGControllerFactory instead!
	 *
	 * @param ureq
	 * @param control
	 * @param currBusinessGroup
	 * @param flags
	 * @param initialViewIdentifier supported are null, "toolforum", "toolfolder"
	 */
	public BusinessGroupMainRunController(UserRequest ureq, WindowControl control, BusinessGroup bGroup) {
		super(ureq, control);
		
		usess = ureq.getUserSession();
		
		assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
		nodeIdPrefix = "bgmr".concat(Long.toString(CodeHelper.getRAMUniqueID()));
		
		readOnly = BusinessGroupStatusEnum.isReadOnly(bGroup);
		
		toolbarPanel = new TooledStackedPanel("groupStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root (course) level
		toolbarPanel.setToolbarAutoEnabled(true);
		toolbarPanel.setShowCloseLink(true, true);

		UserSession session = ureq.getUserSession();
		if(session != null &&  session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
			// Set previous business path as back link for this course - brings user back to place from which he launched the course
			List<HistoryPoint> stack = session.getHistoryStack();
			for(int i=stack.size() - 2; i-->0; ) {
				HistoryPoint point = stack.get(stack.size() - 2);
				if(!point.getEntries().isEmpty()) {
					OLATResourceable ores = point.getEntries().get(0).getOLATResourceable();
					if(!OresHelper.equals(bGroup, ores) && !OresHelper.equals(bGroup.getResource(), ores)) {
						launchedFromPoint = point;
						break;
					}
				}
			}
		}

		List<BusinessGroupMembership> memberships = businessGroupService.getBusinessGroupMembership(Collections.singletonList(bGroup.getKey()), getIdentity());
		if(isOnWaitinglist(memberships)) {
			putInitialPanel(getOnWaitingListMessage(ureq, bGroup));
			chatAvailable = false;
			return;
		} else if(ureq.getUserSession().getRoles().isGuestOnly()) {
			//not a member
			putInitialPanel(getNoAccessMessage(ureq, bGroup));
			chatAvailable = false;
			return;
		}
		
		/*
		 * lastUsage, update lastUsage if group is run if you can acquire the lock
		 * on the group for a very short time. If this is not possible, then the
		 * lastUsage is already up to date within one-day-precision.
		 */
		if(memberships == null || memberships.isEmpty()) {
			businessGroup = businessGroupService.loadBusinessGroup(bGroup); 
		} else {
			businessGroup = businessGroupService.setLastUsageFor(getIdentity(), bGroup);
		}
		if(businessGroup == null) {
			VelocityContainer vc = createVelocityContainer("deleted");
			vc.contextPut("name", bGroup.getName());
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, vc, "grouprun");
			listenTo(columnLayoutCtr); // cleanup on dispose
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			chatAvailable = false;
			return;
		}
		
		businessGroupOres = OresHelper.clone(businessGroup.getResource());

		addLoggingResourceable(LoggingResourceable.wrap(businessGroup));
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OPEN, getClass());

		Object wcard = usess.removeEntry("wild_card_" + businessGroup.getKey());
		
		isGroupsAdmin = usess.getRoles().isAdministrator()
				|| usess.getRoles().isGroupManager();
		
		chatAvailable = isChatAvailable();
		isAdmin = (wcard != null && Boolean.TRUE.equals(wcard))
				|| isGroupsAdmin
				|| businessGroupService.isIdentityInBusinessGroup(getIdentity(), businessGroup.getKey(), true, false, null);
		
		updateWarning(ureq);

		// Initialize translator:
		// package translator with default group fallback translators and type
		// translator
		setTranslator(Util.createPackageTranslator(BGControllerFactory.class, getLocale(), getTranslator()));

		// main component layed out in panel
		main = createVelocityContainer("bgrun");
		exposeGroupDetailsToVC(businessGroup);

		mainPanel = new Panel("p_buddygroupRun");
		mainPanel.setContent(main);
		
		bgTree = new MenuTree("bgtree", "bgTree");
		TreeModel trMdl = buildTreeModel();
		bgTree.setTreeModel(trMdl);
		bgTree.addListener(this);
		
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), bgTree, mainPanel, "grouprun");
		toolbarPanel.pushController(bGroup.getName(), columnLayoutCtr);
		listenTo(columnLayoutCtr); // cleanup on dispose
		putInitialPanel(toolbarPanel);
		
		// register for AssessmentEvents triggered by this user
		singleUserEventBus = ureq.getUserSession().getSingleUserEventCenter();
		singleUserEventBus.registerFor(this, ureq.getIdentity(), assessmentEventOres);
		
		//disposed message controller
		//must be created beforehand
		Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
		Controller disposedBusinessGroup = new DisposedBusinessGroup(ureq, getWindowControl());
		LayoutMain3ColsController disposedController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, disposedBusinessGroup.getInitialComponent(), "disposed grouprun");
		disposedController.addDisposableChildController(disposedBusinessGroup);
		setDisposedMsgController(disposedController);

		// add as listener to BusinessGroup so we are being notified about changes.
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), businessGroup);

		// show disabled message when collaboration is disabled (e.g. in a test)		
		if(AssessmentEvent.isAssessmentStarted(ureq.getUserSession())){
			groupRunDisabled = true;
			showError("grouprun.disabled");				
		}
		
		if(wcard == null) {
			//check managed
			AccessResult acResult = acService.isAccessible(businessGroup, getIdentity(), false);
			if(acResult.isAccessible()) {
				needActivation = false;
			}  else if (businessGroup != null && !acResult.getAvailableMethods().isEmpty()) {
				accessController = new AccessListController(ureq, getWindowControl(), acResult.getAvailableMethods());
				listenTo(accessController);
				mainPanel.setContent(accessController.getInitialComponent());
				bgTree.setTreeModel(new GenericTreeModel());
				needActivation = true;
			} else {
				mainPanel.setContent(new Panel("empty"));
				bgTree.setTreeModel(new GenericTreeModel());
				needActivation = true;
			}
			wildcard = false;
		} else {
			needActivation = false;
			wildcard = true;
		}
	}
	
	private boolean isChatAvailable() {
		return imModule.isEnabled() && imModule.isGroupEnabled() && 
				CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup).isToolEnabled(CollaborationTools.TOOL_CHAT);
	}
	
	private Component getOnWaitingListMessage(UserRequest ureq, BusinessGroup group) {
		VelocityContainer vc = createVelocityContainer("waiting");
		vc.contextPut("name", group.getName());
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, vc, "grouprun");
		listenTo(columnLayoutCtr); // cleanup on dispose
		return columnLayoutCtr.getInitialComponent();
	}
	
	private Component getNoAccessMessage(UserRequest ureq, BusinessGroup group) {
		VelocityContainer vc = createVelocityContainer("access_denied");
		vc.contextPut("name", group.getName());
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, vc, "grouprun");
		listenTo(columnLayoutCtr); // cleanup on dispose
		return columnLayoutCtr.getInitialComponent();
	}
	
	private boolean isOnWaitinglist(List<BusinessGroupMembership> memberships) {
		boolean waiting = false;
		for(BusinessGroupMembership membership:memberships) {
			if(membership.isOwner() || membership.isParticipant()) {
				return false;
			} else if (membership.isWaiting()) {
				waiting = true;
			}
		}
		return waiting;
	}

	private void exposeGroupDetailsToVC(BusinessGroup currBusinessGroup) {
		main.contextPut("BuddyGroup", currBusinessGroup);
		main.contextPut("hasOwners", Boolean.TRUE);
	}
	
	private void updateWarning(UserRequest ureq) {
		if(businessGroup.getGroupStatus() == BusinessGroupStatusEnum.inactive
				|| (businessGroup.getGroupStatus() == BusinessGroupStatusEnum.active && businessGroup.getInactivationEmailDate() != null)) {
			removeAsListenerAndDispose(warningCtrl);
			warningCtrl = new InactiveMessageController(ureq, getWindowControl(), businessGroup, isAdmin);
			listenTo(warningCtrl);
			toolbarPanel.setMessageComponent(warningCtrl.getInitialComponent());
		} else if(warningCtrl != null) {
			removeAsListenerAndDispose(warningCtrl);
			warningCtrl = null;
			toolbarPanel.setMessageComponent(null);	
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// events from menutree
		if (source == bgTree) { // user chose news, contactform, forum, folder or
			// administration
			if (!groupRunDisabled && event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = bgTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				handleTreeActions(ureq, cmd);
			} else if (groupRunDisabled) {
				handleTreeActions(ureq, ACTIVITY_MENUSELECT_OVERVIEW);
				showError("grouprun.disabled");
			}
		} else if(source == toolbarPanel) {
			if (event == Event.CLOSE_EVENT) {
				doClose(ureq);
			} else if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				Controller popedCtrl = pe.getController();
				if(popedCtrl == collabToolCtr) {
					handleTreeActions(ureq, ACTIVITY_MENUSELECT_OVERVIEW);
					bgTree.setSelectedNode(bgTree.getTreeModel().getRootNode());
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == bgEditCntrllr) {
			// changes from the admin controller
			if (event == Event.CHANGED_EVENT) {
				businessGroup = bgEditCntrllr.getBusinessGroup();
				readOnly = BusinessGroupStatusEnum.isReadOnly(businessGroup);
				chatAvailable = isChatAvailable();
				
				TreeModel trMdl = buildTreeModel();
				bgTree.setTreeModel(trMdl);
				bgTree.setSelectedNode(nodeAdmin);
				updateWarning(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				// could not get lock on business group, back to inital screen
				bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
				mainPanel.setContent(main);
			} else if(event == Event.CLOSE_EVENT) {
				doClose(ureq);
			}
		} else if(warningCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				businessGroup = warningCtrl.getBusinessGroup();
				readOnly = BusinessGroupStatusEnum.isReadOnly(businessGroup);
				chatAvailable = isChatAvailable();
				updateWarning(ureq);
			}
		} else if (source == sendToChooserForm) {
			if (event == Event.DONE_EVENT) {
				removeAsListenerAndDispose(collabToolCtr);
				collabToolCtr = createContactFormController(ureq);
				listenTo(collabToolCtr);
				mainPanel.setContent(collabToolCtr.getInitialComponent());
			} else if (event == Event.CANCELLED_EVENT) {
				// back to group overview
				bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
				mainPanel.setContent(main);
			}
		} else if (source == collabToolCtr) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.BACK_EVENT || event == Event.FAILED_EVENT) {
				// In all cases (success or failure) we
				// go back to the group overview page.
				bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
				mainPanel.setContent(main);
			}
		} else if (source == accessController) {
			if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
				removeAsListenerAndDispose(accessController);
				accessController = null;
				
				//check if on waiting list
				List<BusinessGroupMembership> memberships = businessGroupService.getBusinessGroupMembership(Collections.singletonList(businessGroup.getKey()), getIdentity());
				if(isOnWaitinglist(memberships)) {
					Component cmp = getOnWaitingListMessage(ureq, businessGroup);
					mainPanel.setContent(cmp);
				} else {
					mainPanel.setContent(main);
					bgTree.setTreeModel(buildTreeModel());
					needActivation = false;
				}
			} else if(event.equals(AccessEvent.ACCESS_FAILED_EVENT)) {
				String msg = ((AccessEvent)event).getMessage();
				if(StringHelper.containsNonWhitespace(msg)) {
					getWindowControl().setError(msg);
				} else {
					showError("error.accesscontrol");
				}
			}
		}
	}

	/**
	 * Generates the email address list.
	 * 
	 * @param ureq
	 * @return a contact form controller for this group
	 */
	private ContactFormController createContactFormController(UserRequest ureq) {
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		// two named ContactLists, the new way using the contact form
		// the same name as in the checkboxes are taken as contactlist names
		ContactList ownerCntctLst;// = new ContactList(translate("sendtochooser.form.chckbx.owners"));
		ContactList partipCntctLst;// = new ContactList(translate("sendtochooser.form.chckbx.partip"));
		ContactList waitingListContactList;// = new ContactList(translate("sendtochooser.form.chckbx.waitingList"));

		if (sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
			ownerCntctLst = new ContactList(translate("sendtochooser.form.radio.owners.all"));
			List<Identity> ownerList = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());
			ownerCntctLst.addAllIdentites(ownerList);
			cmsg.addEmailTo(ownerCntctLst);
		} else {
			if (sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
				ownerCntctLst = new ContactList(translate("sendtochooser.form.radio.owners.choose"));
				List<Identity> ownerList = businessGroupService.getMembers(businessGroup, GroupRoles.coach.name());
				List<Identity> changeableOwnerList = new ArrayList<>(ownerList);
				for (Identity identity : ownerList) {
					boolean keyIsSelected = false;
					for (Long key : sendToChooserForm.getSelectedOwnerKeys()) {
						if (key.equals(identity.getKey())) {
							keyIsSelected = true;
							break;
						}
					}
					if (!keyIsSelected) {
						changeableOwnerList.remove(changeableOwnerList.indexOf(identity));
					}
				}
				ownerCntctLst.addAllIdentites(changeableOwnerList);
				cmsg.addEmailTo(ownerCntctLst);
			}
		}

		if (sendToChooserForm != null) {
			if  (sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
				partipCntctLst  = new ContactList(translate("sendtochooser.form.radio.partip.all"));
				List<Identity> participantsList = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
				partipCntctLst.addAllIdentites(participantsList);
				cmsg.addEmailTo(partipCntctLst);
			} else {
				if (sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
					partipCntctLst  = new ContactList(translate("sendtochooser.form.radio.partip.choose"));
					List<Identity> participantsList = businessGroupService.getMembers(businessGroup, GroupRoles.participant.name());
					List<Identity> changeableParticipantsList = new ArrayList<>(participantsList);
					for (Identity identity : participantsList) {
						boolean keyIsSelected = false;
						for (Long key : sendToChooserForm.getSelectedPartipKeys()) {
							if (key.equals(identity.getKey())) {
								keyIsSelected = true;
								break;
							}
						}
						if (!keyIsSelected) {
							changeableParticipantsList.remove(changeableParticipantsList.indexOf(identity));
						}
					}
					partipCntctLst.addAllIdentites(changeableParticipantsList);
					cmsg.addEmailTo(partipCntctLst);
				}
			}
			
		}
		if (sendToChooserForm != null && isAdmin && businessGroup.getWaitingListEnabled().booleanValue()) {
			if (sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL)) {
				waitingListContactList = new ContactList(translate("sendtochooser.form.radio.waitings.all"));
				List<Identity> waitingListIdentities = businessGroupService.getMembers(businessGroup, GroupRoles.waiting.name());
				waitingListContactList.addAllIdentites(waitingListIdentities);
				cmsg.addEmailTo(waitingListContactList);
			} else {
				if (sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE)) {
					waitingListContactList = new ContactList(translate("sendtochooser.form.radio.waitings.choose"));
					List<Identity> waitingListIdentities = businessGroupService.getMembers(businessGroup, GroupRoles.waiting.name());
					List<Identity> changeableWaitingListIdentities = new ArrayList<>(waitingListIdentities);
					for (Identity indentity : waitingListIdentities) {
						boolean keyIsSelected = false;
						for (Long key : sendToChooserForm.getSelectedWaitingKeys()) {
							if (key.equals(indentity.getKey())) {
								keyIsSelected = true;
								break;
							}
						}
						if (!keyIsSelected) {
							changeableWaitingListIdentities.remove(changeableWaitingListIdentities.indexOf(indentity));
						}
					}
					waitingListContactList.addAllIdentites(changeableWaitingListIdentities);
					cmsg.addEmailTo(waitingListContactList);
				}
			}
		}
		
		cmsg.setSubject( translate("businessgroup.contact.subject", businessGroup.getName() ) );
		
		if (sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_NOTHING)) {
			String restUrl = BusinessControlFactory.getInstance().getAsURIString(getWindowControl().getBusinessControl(), true);
			cmsg.setBodyText( getTranslator().translate("businessgroup.contact.bodytext", new String[]{ businessGroup.getName(), "<a href='" + restUrl + "'>" + restUrl + "</a>"} ) );
		} else {
			cmsg.setBodyText ("");
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		return collabTools.createContactFormController(ureq, getWindowControl(), cmsg);
	}

	/**
	 * handles the different tree actions
	 * 
	 * @param ureq
	 * @param cmd
	 */
	private void handleTreeActions(UserRequest ureq, String cmd) {
		// release edit lock if available
		removeAsListenerAndDispose(bgEditCntrllr);
		removeAsListenerAndDispose(collabToolCtr);
	
		// init new controller according to user click
		if (ACTIVITY_MENUSELECT_OVERVIEW.equals(cmd)) {
			doMain(ureq);
		} else if (ACTIVITY_MENUSELECT_FORUM.equals(cmd)) {
			doForum(ureq);
		} else if (ACTIVITY_MENUSELECT_CHAT.equals(cmd)) {
			doChat(ureq);
		} else if (ACTIVITY_MENUSELECT_CALENDAR.equals(cmd)) {
			doCalendar(ureq);
		} else if (ACTIVITY_MENUSELECT_INFORMATION.equals(cmd)) {
			doInformations(ureq);
		} else if (ACTIVITY_MENUSELECT_FOLDER.equals(cmd)) {
			doFolder(ureq);
		} else if (ACTIVITY_MENUSELECT_MEMBERSLIST.equals(cmd)) {
			doShowMembers(ureq);
		} else if (ACTIVITY_MENUSELECT_CONTACTFORM.equals(cmd)) {
			doContactForm(ureq);
		} else if (ACTIVITY_MENUSELECT_ADMINISTRATION.equals(cmd)) {
			doAdministration(ureq);
		} else if (ACTIVITY_MENUSELECT_SHOW_RESOURCES.equals(cmd)) {
			doShowResources(ureq);
		} else if (ACTIVITY_MENUSELECT_WIKI.equals(cmd)) {
			doWiki(ureq);
		} else if (ACTIVITY_MENUSELECT_PORTFOLIO.equals(cmd)) {
			doPortfolio(ureq);
		} else if (ACTIVITY_MENUSELECT_OPENMEETINGS.equals(cmd)) {
			doOpenMeetings(ureq);
		} else if (ACTIVITY_MENUSELECT_ADOBECONNECT.equals(cmd)) {
			doAdobeConnect(ureq);
		} else if(ACTIVITY_MENUSELECT_BIGBLUEBUTTON.equals(cmd)) {
			doBigBlueButton(ureq);
		} else if(ACTIVITY_MENUSELECT_TEAMS.equals(cmd)) {
			doTeams(ureq);
		} else if (ACTIVITY_MENUSELECT_AC.equals(cmd)) {
			doAccessControlHistory(ureq);
		} 
		
		// Update window title
		String newTitle = businessGroup.getName() + " - " + bgTree.getSelectedNode().getTitle();
		getWindowControl().getWindowBackOffice().getWindow().setTitle(getTranslator(), newTitle);						
	}
	
	private void doMain(UserRequest ureq) {
		// root node clicked display overview
		mainPanel.setContent(main);
		addToHistory(ureq, this);
	}
	
	private void doChat(UserRequest ureq) {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLCHAT);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createChatController(ureq, bwControl, businessGroup, isAdmin, readOnly);
		if(collabToolCtr == null) {
			showWarning("groupchat.not.available");
			mainPanel.setContent(new Panel("empty"));
		} else {
			mainPanel.setContent(collabToolCtr.getInitialComponent());
		}
	}
	
	private Activateable2 doForum(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLFORUM, OlatResourceableType.forum));
		SubscriptionContext sc = new SubscriptionContext(businessGroup, INITVIEW_TOOLFORUM);

		// calculate the new businesscontext for the forum clicked
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLFORUM);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createForumController(ureq, bwControl, isAdmin, ureq.getUserSession().getRoles().isGuestOnly(),	sc, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return (Activateable2)collabToolCtr;
	}
	
	private Activateable2 doCalendar(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLCAL, OlatResourceableType.calendar));
		
		// calculate the new business context
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLCAL);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ce.getOLATResourceable()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createCalendarController(ureq, bwControl, businessGroup, isAdmin, true, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return (Activateable2)collabToolCtr;
	}
	
	private void doInformations(UserRequest ureq) {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLMSG);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ce.getOLATResourceable()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createInfoMessageController(ureq, bwControl, isAdmin, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
	}
	
	private Activateable2 doFolder(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLFOLDER, OlatResourceableType.sharedFolder));

		SubscriptionContext sc = new SubscriptionContext(businessGroup, INITVIEW_TOOLFOLDER);
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLFOLDER);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ce.getOLATResourceable()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createFolderController(ureq, bwControl, businessGroup, isAdmin, sc, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return (Activateable2)collabToolCtr;
	}
	
	private Activateable2 doWiki(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLWIKI, OlatResourceableType.wiki));
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLWIKI);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapWikiOres(ce.getOLATResourceable()));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createWikiController(ureq, bwControl, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return (Activateable2)collabToolCtr;
	}
	
	private Controller doPortfolio(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLPORTFOLIO, OlatResourceableType.portfolio));

		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLPORTFOLIO);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createPortfolioController(ureq, bwControl, toolbarPanel, businessGroup, readOnly);
		listenTo(collabToolCtr);
		toolbarPanel.popUpToRootController(ureq);
		toolbarPanel.pushController("Portfolio", collabToolCtr);
		
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType("Toc");
		if(collabToolCtr instanceof Activateable2) {
			((Activateable2)collabToolCtr).activate(ureq, entries, null);
		}
		return collabToolCtr;
	}
	
	private void doOpenMeetings(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLOPENMEETINGS, OlatResourceableType.portfolio));
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLOPENMEETINGS);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createOpenMeetingsController(ureq, bwControl, businessGroup, isAdmin, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
	}
	
	private void doAdobeConnect(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLADOBECONNECT, OlatResourceableType.adobeconnect));
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLADOBECONNECT);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		collabToolCtr = collabTools.createAdobeConnectController(ureq, bwControl, businessGroup, isAdmin, readOnly);
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
	}
	
	private BigBlueButtonRunController doBigBlueButton(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLBIGBLUEBUTTON, OlatResourceableType.bigbluebutton));
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLBIGBLUEBUTTON);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		BigBlueButtonRunController bigBlueButtonToolCtrl = collabTools
				.createBigBlueButtonController(ureq, bwControl, businessGroup, isAdmin, readOnly);
		collabToolCtr = bigBlueButtonToolCtrl;
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return bigBlueButtonToolCtrl;
	}
	
	private TeamsMeetingsRunController doTeams(UserRequest ureq) {
		addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLTEAMS, OlatResourceableType.teams));
		
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLTEAMS);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));
		addToHistory(ureq, bwControl);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		TeamsMeetingsRunController teamsToolCtrl = collabTools
				.createTeamsController(ureq, bwControl, businessGroup, isAdmin, readOnly);
		collabToolCtr = teamsToolCtrl;
		listenTo(collabToolCtr);
		mainPanel.setContent(collabToolCtr.getInitialComponent());
		return teamsToolCtrl;
	}

	private Activateable2 doAdministration(UserRequest ureq) {
		removeAsListenerAndDispose(bgEditCntrllr);
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ORES_TOOLADMIN));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ORES_TOOLADMIN, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		collabToolCtr = bgEditCntrllr = new BusinessGroupEditController(ureq, bwControl, toolbarPanel, businessGroup);
		listenTo(bgEditCntrllr);
		mainPanel.setContent(bgEditCntrllr.getInitialComponent());
		return bgEditCntrllr;
	}
	
	private Activateable2 doAccessControlHistory(UserRequest ureq) {
		removeAsListenerAndDispose(bgACHistoryCtrl);
		
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ORES_TOOLBOOKING, null, getWindowControl());
		addToHistory(ureq, bwControl);
		
		OLATResource resource = businessGroup.getResource();
		bgACHistoryCtrl = new OrdersAdminController(ureq, bwControl, toolbarPanel, resource);
		listenTo(bgACHistoryCtrl);
		mainPanel.setContent(bgACHistoryCtrl.getInitialComponent());
		return (Activateable2)bgACHistoryCtrl;
	}

	private void doContactForm(UserRequest ureq) {
		if (contactFromVC == null) {
			contactFromVC = createVelocityContainer("cosendtochooser");
		}
		removeAsListenerAndDispose(sendToChooserForm);
		contactFromVC.remove("contactForm");
		contactFromVC.contextPut("readOnly", Boolean.valueOf(readOnly));
		
		if(readOnly) {
            String title = translate("freezenoaccess.title");
            String message = translate("freezenoaccess.message");
            Controller controller = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), title, message);
			contactFromVC.put("contactForm", controller.getInitialComponent());
		} else {
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ORES_TOOLCONTACT, null, getWindowControl());
			addToHistory(ureq, bwControl);
			
			sendToChooserForm = new BusinessGroupSendToChooserForm(ureq, bwControl, businessGroup, isAdmin);
			listenTo(sendToChooserForm);
			contactFromVC.put("contactForm", sendToChooserForm.getInitialComponent());
		}
		mainPanel.setContent(contactFromVC);
	}

	private void doShowMembers(UserRequest ureq) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(this.businessGroup);
		boolean canEmail = collabTools.isToolEnabled(CollaborationTools.TOOL_CONTACT) && !readOnly;
		groupMembersToggleViewController = new GroupMembersRunController(ureq, getWindowControl(), businessGroup, canEmail, readOnly);
		listenTo(groupMembersToggleViewController);
		mainPanel.setContent(groupMembersToggleViewController.getInitialComponent());
		collabToolCtr = null;
		addToHistory(ureq, ORES_TOOLMEMBERS, null);
	}
	
	protected final void doClose(UserRequest ureq) {
		getWindowControl().getWindowBackOffice().getWindow()
			.getDTabs().closeDTab(ureq, businessGroupOres, launchedFromPoint);
	}

	@Override
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CLOSED, getClass());
		
		if (chatAvailable) {
			CloseInstantMessagingEvent e = new CloseInstantMessagingEvent(businessGroup, null, null);
			singleUserEventBus.fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
		}
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, businessGroup);
		if(singleUserEventBus != null) {
			singleUserEventBus.deregisterFor(this, assessmentEventOres);
		}
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(needActivation) {
			return;
		}
		if(entries == null || entries.isEmpty()) {
			addToHistory(ureq);
			return;
		}
		
		// release edit lock if available
		removeAsListenerAndDispose(bgEditCntrllr);
		removeAsListenerAndDispose(collabToolCtr);
		
		ContextEntry ce = entries.remove(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if (OresHelper.equals(ores, ORES_TOOLFORUM)) {
			// start the forum
			if (nodeForum != null) {
				doForum(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeForum);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.forumnotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLFOLDER)) {
			if (nodeFolder != null) {
				doFolder(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeFolder);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.foldernotavailable");				
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLWIKI)) {
			if (nodeWiki != null) {
				doWiki(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeWiki);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.wikinotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLCAL)) {
			if (nodeCal != null) {
				doCalendar(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeCal);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.calnotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLPORTFOLIO)) {
			if (nodePortfolio != null) {
				Controller ctrl = doPortfolio(ureq);
				if(ctrl instanceof Activateable2) {
					((Activateable2)ctrl).activate(ureq, entries, ce.getTransientState());
				}
				bgTree.setSelectedNode(nodePortfolio);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.portfolionotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLOPENMEETINGS)) {
			if (nodeOpenMeetings != null) {
				doOpenMeetings(ureq);
				bgTree.setSelectedNode(nodeOpenMeetings);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.openmeetingnotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLADOBECONNECT)) {
			if (nodeAdobeConnect != null) {
				doAdobeConnect(ureq);
				bgTree.setSelectedNode(nodeAdobeConnect);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.adobeconnectnotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLBIGBLUEBUTTON)) {
			if (nodeBigBlueButton != null) {
				doBigBlueButton(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeBigBlueButton);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.bigbluebuttonnotavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLTEAMS)) {
			if (nodeTeams != null) {
				doTeams(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeTeams);
			} else if(mainPanel != null) { // not enabled
				String text = translate("warn.teamsavailable");
				Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
				listenTo(mc); // cleanup on dispose
				mainPanel.setContent(mc.getInitialComponent());
			}
		} else if (OresHelper.equals(ores, ORES_TOOLADMIN)) {
			if (nodeAdmin != null) {
				doAdministration(ureq).activate(ureq, entries, ce.getTransientState());
				bgTree.setSelectedNode(nodeAdmin);
			}
		} else if (OresHelper.equals(ores, ORES_TOOLMSG)) {
			if (nodeInformation != null) {
				doInformations(ureq);
				bgTree.setSelectedNode(nodeInformation);
			}
		} else if (OresHelper.equals(ores, ORES_TOOLCONTACT)) {
			if (nodeContact != null) {
				doContactForm(ureq);
				bgTree.setSelectedNode(nodeContact);
			}
		} else if (OresHelper.equals(ores, ORES_TOOLMEMBERS)) {
			if (nodeGroupOwners != null) {
				doShowMembers(ureq);
				bgTree.setSelectedNode(nodeGroupOwners);
			}
		} else if (OresHelper.equals(ores, ORES_TOOLRESOURCES)) {
			if (nodeResources != null) {
				doShowResources(ureq);
				bgTree.setSelectedNode(nodeResources);
			}
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
			if (delEvent.targetEquals(businessGroup) && delEvent.getDeletedByKey() != null) {
				if( delEvent.getDeletedByKey().equals(getIdentity().getKey())) {
					doClose(new SyntheticUserRequest(getIdentity(), getLocale(), usess));
				} else {
					dispose();
				}
			}	
		} else if (event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent bgmfe = (BusinessGroupModifiedEvent) event;
			if (bgmfe.getCommand().equals(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT)
					&& bgmfe.getAffectedRepositoryEntryKey() == null) {
				if(bgmfe.isSender(getIdentity())) {
					return;// receive event by other means
				}
				// reset business group property manager
				// update reference to update business group object
				businessGroup = businessGroupService.loadBusinessGroup(businessGroup);
				chatAvailable = isChatAvailable();
				
				main.contextPut("BuddyGroup", businessGroup);
				TreeModel trMdl = buildTreeModel();
				bgTree.setTreeModel(trMdl);
				if (bgEditCntrllr == null) {
					// change didn't origin by our own edit controller
					showInfo("grouprun.configurationchanged");
					bgTree.setSelectedNodeId(trMdl.getRootNode().getIdent());
					mainPanel.setContent(main);
				} else {
					// Activate edit menu item
					bgTree.setSelectedNodeId(ACTIVITY_MENUSELECT_ADMINISTRATION);
				}
			} else if (bgmfe.wasMyselfRemoved(getIdentity()) && !wildcard && !isGroupsAdmin
					&& bgmfe.getAffectedRepositoryEntryKey() == null && !isDisposed()) {
				//nothing more here!! The message will be created and displayed upon disposing
				dispose();//disposed message controller will be set
			}
		} else if(event instanceof AssessmentEvent) {
			if(((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
				groupRunDisabled = true;			 
			} else if (((AssessmentEvent)event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
				groupRunDisabled = false;
			}
		}
	}

	private void doShowResources(UserRequest ureq) {
		if (resourcesCtr == null) {
			resourcesCtr = new BusinessGroupResourceController(ureq, getWindowControl(), businessGroup);
			listenTo(resourcesCtr);
		} else {
			resourcesCtr.loadModel();
		}
		mainPanel.setContent(resourcesCtr.getInitialComponent());
		addToHistory(ureq, ORES_TOOLRESOURCES, null);
	}

	/**
	 * Activates the administration menu item. Make sure you have the rights to do
	 * this, otherwhise this will throw a nullpointer exception
	 * 
	 * @param ureq
	 */
	public void activateAdministrationMode(UserRequest ureq) {
		doAdministration(ureq);
		bgTree.setSelectedNodeId(adminNodeId);
	}

	/**
	 * @return The menu tree model
	 */
	private TreeModel buildTreeModel() {
		GenericTreeNode gtnChild, root;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode(nodeIdPrefix.concat("-root"));
		root.setTitle(businessGroup.getName());
		root.setUserObject(ACTIVITY_MENUSELECT_OVERVIEW);
		root.setAltText(translate("menutree.top.alt") + " " + businessGroup.getName());
		root.setIconCssClass("o_icon o_icon_group");
		gtm.setRootNode(root);
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(this.businessGroup);

		if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("new"));
			gtnChild.setTitle(translate("menutree.news"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_INFORMATION);
			gtnChild.setAltText(translate("menutree.news.alt"));
			gtnChild.setIconCssClass("o_icon_news");
			gtnChild.setCssClass("o_sel_group_news");
			root.addChild(gtnChild);
			nodeInformation = gtnChild;
		}

		if (calendarModule.isEnabled() && calendarModule.isEnableGroupCalendar() && collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("cal"));
			gtnChild.setTitle(translate("menutree.calendar"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_CALENDAR);
			gtnChild.setAltText(translate("menutree.calendar.alt"));
			gtnChild.setIconCssClass("o_calendar_icon");
			gtnChild.setCssClass("o_sel_group_calendar");
			root.addChild(gtnChild);
			nodeCal = gtnChild;
		}
		
		boolean hasResources = businessGroupService.hasResources(businessGroup);
		if(hasResources) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("courses"));
			gtnChild.setTitle(translate("menutree.resources"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_SHOW_RESOURCES);
			gtnChild.setAltText(translate("menutree.resources.alt"));
			gtnChild.setIconCssClass("o_CourseModule_icon");
			gtnChild.setCssClass("o_sel_group_resources");
			root.addChild(gtnChild);
			nodeResources = gtnChild;
		}

		if (businessGroup.isOwnersVisibleIntern() || businessGroup.isParticipantsVisibleIntern() || businessGroup.isWaitingListVisibleIntern()) {
			// either owners, participants, the waiting list or all three are visible
			// otherwise the node is not visible
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("members"));
			gtnChild.setTitle(translate("menutree.members"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_MEMBERSLIST);
			gtnChild.setAltText(translate("menutree.members.alt"));
			gtnChild.setIconCssClass("o_icon_group");
			StringBuilder selCss = new StringBuilder();
			selCss.append("o_sel_group_members");
			if(businessGroup.isOwnersVisibleIntern()) {
				selCss.append(" o_sel_group_owners_members");
			}
			if(businessGroup.isParticipantsVisibleIntern()) {
				selCss.append(" o_sel_group_participants_members");
			}
			if(businessGroup.isWaitingListVisibleIntern()) {
				selCss.append(" o_sel_group_waiting_members");	
			}
			gtnChild.setCssClass(selCss.toString());
			root.addChild(gtnChild);
			nodeGroupOwners = gtnChild;
		}

		if (collabTools.isToolEnabled(CollaborationTools.TOOL_CONTACT)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("contact"));
			gtnChild.setTitle(translate("menutree.contactform"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_CONTACTFORM);
			gtnChild.setAltText(translate("menutree.contactform.alt"));
			gtnChild.setIconCssClass("o_co_icon");
			gtnChild.setCssClass("o_sel_group_contact");
			root.addChild(gtnChild);
			nodeContact = gtnChild;
		}

		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("folder"));
			gtnChild.setTitle(translate("menutree.folder"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_FOLDER);
			gtnChild.setAltText(translate("menutree.folder.alt"));
			gtnChild.setIconCssClass("o_bc_icon");
			gtnChild.setCssClass("o_sel_group_folder");
			root.addChild(gtnChild);
			nodeFolder = gtnChild;
		}

		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("forum"));
			gtnChild.setTitle(translate("menutree.forum"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_FORUM);
			gtnChild.setAltText(translate("menutree.forum.alt"));
			gtnChild.setIconCssClass("o_fo_icon");
			gtnChild.setCssClass("o_sel_group_forum");
			root.addChild(gtnChild);
			nodeForum = gtnChild;
		}

		if (chatAvailable) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("chat"));
			gtnChild.setTitle(translate("menutree.chat"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_CHAT);
			gtnChild.setAltText(translate("menutree.chat.alt"));
			gtnChild.setIconCssClass("o_icon_chat");
			gtnChild.setCssClass("o_sel_group_chat");
			root.addChild(gtnChild);
		}

		WikiModule wikiModule = CoreSpringFactory.getImpl(WikiModule.class); 
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI) && wikiModule.isWikiEnabled()) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("wiki"));
			gtnChild.setTitle(translate("menutree.wiki"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_WIKI);
			gtnChild.setAltText(translate("menutree.wiki.alt"));
			gtnChild.setIconCssClass("o_wiki_icon");
			gtnChild.setCssClass("o_sel_group_wiki");
			root.addChild(gtnChild);
			nodeWiki = gtnChild;
		}
			
		if (portfolioV2Module.isEnabled() && collabTools.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("eportfolio"));
			gtnChild.setTitle(translate("menutree.portfolio"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_PORTFOLIO);
			gtnChild.setAltText(translate("menutree.portfolio.alt"));
			gtnChild.setIconCssClass("o_ep_icon");
			gtnChild.setCssClass("o_sel_group_portfolio");
			root.addChild(gtnChild);
			nodePortfolio = gtnChild;
		}
			
		if (openMeetingsModule.isEnabled() && collabTools.isToolEnabled(CollaborationTools.TOOL_OPENMEETINGS)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("meetings"));
			gtnChild.setTitle(translate("menutree.openmeetings"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_OPENMEETINGS);
			gtnChild.setAltText(translate("menutree.openmeetings.alt"));
			gtnChild.setIconCssClass("o_openmeetings_icon");
			root.addChild(gtnChild);
			nodeOpenMeetings = gtnChild;
		}
		
		if(adobeConnectModule.isEnabled() && adobeConnectModule.isGroupsEnabled()
				&& collabTools.isToolEnabled(CollaborationTools.TOOL_ADOBECONNECT)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("adobeconnect"));
			gtnChild.setTitle(translate("menutree.adobeconnect"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_ADOBECONNECT);
			gtnChild.setAltText(translate("menutree.adobeconnect.alt"));
			gtnChild.setIconCssClass("o_vc_icon");
			root.addChild(gtnChild);
			nodeAdobeConnect = gtnChild;
		}
		
		if(bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isGroupsEnabled()
				&& collabTools.isToolEnabled(CollaborationTools.TOOL_BIGBLUEBUTTON)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("bigbluebutton"));
			gtnChild.setTitle(translate("menutree.bigbluebutton"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_BIGBLUEBUTTON);
			gtnChild.setAltText(translate("menutree.bigbluebutton.alt"));
			gtnChild.setIconCssClass("o_vc_icon");
			root.addChild(gtnChild);
			nodeBigBlueButton = gtnChild;
		}
		
		if(teamsModule.isEnabled() && teamsModule.isGroupsEnabled()
				&& collabTools.isToolEnabled(CollaborationTools.TOOL_TEAMS)) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("teams"));
			gtnChild.setTitle(translate("menutree.teams"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_TEAMS);
			gtnChild.setAltText(translate("menutree.teams.alt"));
			gtnChild.setIconCssClass("o_vc_icon");
			root.addChild(gtnChild);
			nodeBigBlueButton = gtnChild;
		}
	
		if (isAdmin) {
			gtnChild = new GenericTreeNode(nodeIdPrefix.concat("admin"));
			gtnChild.setTitle(translate("menutree.administration"));
			gtnChild.setUserObject(ACTIVITY_MENUSELECT_ADMINISTRATION);
			gtnChild.setIdent(ACTIVITY_MENUSELECT_ADMINISTRATION);
			gtnChild.setAltText(translate("menutree.administration.alt"));
			gtnChild.setIconCssClass("o_icon_settings");
			root.addChild(gtnChild);
			adminNodeId = gtnChild.getIdent();
			nodeAdmin = gtnChild;

			AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
			if(acModule.isEnabled() && acService.isResourceAccessControled(businessGroup.getResource(), null)) {
				gtnChild = new GenericTreeNode(nodeIdPrefix.concat("ac"));
				gtnChild.setTitle(translate("menutree.ac"));
				gtnChild.setUserObject(ACTIVITY_MENUSELECT_AC);
				gtnChild.setIdent(ACTIVITY_MENUSELECT_AC);
				gtnChild.setAltText(translate("menutree.ac.alt"));
				gtnChild.setIconCssClass("o_icon_booking");
				root.addChild(gtnChild);
			}
		}
		return gtm;
	}
}