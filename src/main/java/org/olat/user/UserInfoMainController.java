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
* <p>
* Initial code contributed and copyrighted by<br>
* 2012 by frentix GmbH, http://www.frentix.com
*/

package org.olat.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.modules.co.ContactFormController;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.portfolio.ui.EPMapRunController;
import org.olat.portfolio.ui.EPMapRunViewOption;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: July 26, 2005
 * 
 * @author Alexander Schneider
 * @author Florian Gnägi
 * 
 *         Comment: Controller creates a main layout controller that represents
 *         the users visiting card. It has access to the users homepage, public
 *         folder, public calendar items, published eportfolios and an email
 *         form.
 * 
 */
public class UserInfoMainController extends MainLayoutBasicController implements Activateable2 {

	private static final String CMD_HOMEPAGE = "homepage";
	private static final String CMD_CALENDAR = "calendar";
	private static final String CMD_FOLDER = "userfolder";
	private static final String CMD_CONTACT = "contact";
	private static final String CMD_PORTFOLIO = "portfolio";

	private MenuTree menuTree;
	private Panel main;
	private TooledStackedPanel toolbarPanel;
	
	public static final OLATResourceable BUSINESS_CONTROL_TYPE_FOLDER = OresHelper.createOLATResourceableTypeWithoutCheck(FolderRunController.class
			.getSimpleName());

	private EPMapRunController portfolioController;
	private FolderRunController folderRunController;
	private WeeklyCalendarController calendarController;
	private ContactFormController contactFormController;
	private HomePageDisplayController homePageDisplayController;

	private final Identity chosenIdentity;
	private final String firstLastName;
	private HistoryPoint launchedFromPoint;

	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param chosenIdentity
	 */
	public UserInfoMainController(UserRequest ureq, WindowControl wControl, Identity chosenIdentity,
			boolean showRootNode, boolean showToolbar) {
		super(ureq, wControl);
				
		this.chosenIdentity = chosenIdentity;
		
		UserSession session = ureq.getUserSession();
		if(showToolbar && session != null &&  session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
			// Set previous business path as back link for this course - brings user back to place from which he launched the course
			List<HistoryPoint> stack = session.getHistoryStack();
			for(int i=stack.size() - 2; i-->0; ) {
				HistoryPoint point = stack.get(stack.size() - 2);
				if(point.getEntries().size() > 0) {
					OLATResourceable ores = point.getEntries().get(0).getOLATResourceable();
					if(!chosenIdentity.getKey().equals(ores.getResourceableId())) {
						launchedFromPoint = point;
						break;
					}
				}
			}
		}

		main = new Panel("userinfomain");
		Controller homeCtrl = createComponent(ureq, CMD_HOMEPAGE);
		main.setContent(homeCtrl.getInitialComponent());
		firstLastName = userManager.getUserDisplayName(chosenIdentity);

		// Navigation menu
		if (!chosenIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
			menuTree = new MenuTree("menuTree");
			GenericTreeModel tm = buildTreeModel(firstLastName);
			menuTree.setTreeModel(tm);
			menuTree.setSelectedNodeId(tm.getRootNode().getChildAt(0).getIdent());
			menuTree.addListener(this);
			menuTree.setRootVisible(showRootNode);
		}

		// override if user is guest, don't show anything
		if (ureq.getUserSession().getRoles().isGuestOnly()) {
			main = new Panel("empty");
			menuTree = null;
		}

		LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, main, "userinfomain");
		listenTo(columnLayoutCtr);
		
		if(showToolbar) {
			toolbarPanel = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
			toolbarPanel.setInvisibleCrumb(0); // show root level
			toolbarPanel.setToolbarEnabled(false);
			toolbarPanel.setShowCloseLink(true, true);
			toolbarPanel.pushController(firstLastName, columnLayoutCtr);
			putInitialPanel(toolbarPanel);
		} else {
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // goto
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String)selTreeNode.getUserObject();
				Controller controller = createComponent(ureq, cmd);
				if(controller != null) {
					main.setContent(controller.getInitialComponent());
				}
			}
		} else if(source == toolbarPanel) {
			if (event == Event.CLOSE_EVENT) {
				doClose(ureq);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(StringHelper.containsNonWhitespace(type)) {
			Controller controller = createComponent(ureq, type);
			if(controller != null) {
				if(controller instanceof  Activateable2) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					((Activateable2)controller).activate(ureq, subEntries, entries.get(0).getTransientState());
				}
				
				main.setContent(controller.getInitialComponent());
				TreeNode selectedNode = TreeHelper.findNodeByUserObject(type, menuTree.getTreeModel().getRootNode());
				if(selectedNode != null) {
					menuTree.setSelectedNode(selectedNode);
				}
			}
		}
	}

	/**
	 * Generates the archiver menu
	 * 
	 * @return The generated menu tree model
	 * @param firstLastName
	 */
	private GenericTreeModel buildTreeModel(String name) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(name);
		root.setAltText(name);
		root.setAccessible(false);
		gtm.setRootNode(root);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.homepage"));
		gtn.setUserObject(CMD_HOMEPAGE);
		gtn.setAltText(translate("menu.homepage.alt"));
		root.addChild(gtn);

		// following user info elements are only shown for undeleted and real
		// users (not invited
		// eportfolio users)
		boolean isInvitee = invitationDao.isInvitee(chosenIdentity);
		boolean isDeleted = chosenIdentity.getStatus().equals(Identity.STATUS_DELETED);
		
		
		
		if ( !isDeleted && ! isInvitee) {
			if(calendarModule.isEnablePersonalCalendar()) {
				gtn = new GenericTreeNode();
				gtn.setTitle(translate("menu.calendar"));
				gtn.setUserObject(CMD_CALENDAR);
				gtn.setAltText(translate("menu.calendar.alt"));
				gtn.setCssClass("o_visiting_card_calendar");
				root.addChild(gtn);
			}
	
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.folder"));
			gtn.setUserObject(CMD_FOLDER);
			gtn.setAltText(translate("menu.folder.alt"));
			gtn.setCssClass("o_visiting_card_folder");
			root.addChild(gtn);
		}	
		if ( !isDeleted) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.contact"));
			gtn.setUserObject(CMD_CONTACT);
			gtn.setAltText(translate("menu.contact.alt"));
			gtn.setCssClass("o_visiting_card_contact");
			root.addChild(gtn);
		}
		if ( !isDeleted && ! isInvitee) {
			PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
			if (portfolioModule.isEnabled()) {
				gtn = new GenericTreeNode();
				gtn.setTitle(translate("menu.portfolio"));
				gtn.setUserObject(CMD_PORTFOLIO);
				gtn.setAltText(translate("menu.portfolio.alt"));
				gtn.setCssClass("o_visiting_card_portfolio");
				root.addChild(gtn);
			}
		}			
		return gtm;
	}

	private Controller createComponent(UserRequest ureq, String menuCommand) {
		Controller controller = null;
		if (menuCommand.equalsIgnoreCase(CMD_HOMEPAGE)) {
			controller = doOpenHomepage(ureq);
		} else if (menuCommand.equalsIgnoreCase(CMD_CALENDAR)) {
			controller = doOpenCalendar(ureq);
		} else if (menuCommand.equalsIgnoreCase(CMD_FOLDER)) {
			controller = doOpenFolder(ureq);
		} else if (menuCommand.equalsIgnoreCase(CMD_CONTACT)) {
			controller = doOpenContact(ureq);
		} else if (menuCommand.equalsIgnoreCase(CMD_PORTFOLIO)) {
			controller = doOpenPortfolio(ureq);
		}
		return controller;
	}
	
	private HomePageDisplayController doOpenHomepage(UserRequest ureq) {
		removeAsListenerAndDispose(homePageDisplayController);
		
		HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
		HomePageConfig homePageConfig = hpcm.loadConfigFor(chosenIdentity.getName());
		removeAsListenerAndDispose(homePageDisplayController);
		homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), chosenIdentity, homePageConfig);
		listenTo(homePageDisplayController);
		return homePageDisplayController;
	}
	
	private WeeklyCalendarController doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calendarController);
		
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(chosenIdentity);
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calendarWrapper.getKalendar(), getIdentity());
		if (config != null) {
			calendarWrapper.setConfiguration(config);
		}
		
		calendarWrapper.setPrivateEventsVisible(chosenIdentity.equals(ureq.getIdentity()));
		if (chosenIdentity.equals(ureq.getIdentity())) {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		} else {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		}
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		calendars.add(calendarWrapper);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType(CMD_CALENDAR);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		OLATResourceable callerOres = OresHelper.createOLATResourceableInstance(chosenIdentity.getName(), chosenIdentity.getKey());
		calendarController = new WeeklyCalendarController(ureq, bwControl, calendars,
				WeeklyCalendarController.CALLER_PROFILE, callerOres, false);
		listenTo(calendarController);
		return calendarController;
	}
	
	private FolderRunController doOpenFolder(UserRequest ureq) {
		removeAsListenerAndDispose(folderRunController);

		String chosenUserFolderRelPath = FolderConfig.getUserHome(chosenIdentity.getName()) + "/public";

		OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(chosenUserFolderRelPath, null);
		OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(firstLastName, rootFolder);
		
		//decided in plenum to have read only view in the personal visiting card, even for admin
		VFSSecurityCallback secCallback = new ReadOnlyCallback();
		namedFolder.setLocalSecurityCallback(secCallback);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("userfolder");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		folderRunController = new FolderRunController(namedFolder, false, true, false, ureq, bwControl);
		folderRunController.setResourceURL("[Identity:" + chosenIdentity.getKey() + "][userfolder:0]");
		listenTo(folderRunController);
		return folderRunController;
	}
	
	private ContactFormController doOpenContact(UserRequest ureq) {
		removeAsListenerAndDispose(contactFormController);
		
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList emailList = new ContactList(firstLastName);
		emailList.add(chosenIdentity);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType(CMD_CONTACT);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactFormController = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactFormController);
		return contactFormController;
	}
	
	private EPMapRunController doOpenPortfolio(UserRequest ureq) {
		removeAsListenerAndDispose(portfolioController);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType(CMD_PORTFOLIO);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		portfolioController = new EPMapRunController(ureq, bwControl, false, EPMapRunViewOption.OTHER_MAPS, chosenIdentity);
		listenTo(portfolioController);
		return portfolioController;
	}
	
	protected final void doClose(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("HomeSite", chosenIdentity.getKey());
		getWindowControl().getWindowBackOffice().getWindow().getDTabs().closeDTab(ureq, ores, launchedFromPoint);
	}
}