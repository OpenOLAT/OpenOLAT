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

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarConfig;
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
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.modules.co.ContactFormController;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.PortfolioModule;

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
public class UserInfoMainController extends MainLayoutBasicController {

	private static final String CMD_HOMEPAGE = "homepage";
	private static final String CMD_CALENDAR = "calendar";
	private static final String CMD_FOLDER = "folder";
	private static final String CMD_CONTACT = "contact";
	private static final String CMD_WEBLOG = "weblog";
	private static final String CMD_PORTFOLIO = "portfolio";

	private MenuTree menuTree;
	private VelocityContainer myContent;
	private Panel main;
	
	public static final OLATResourceable BUSINESS_CONTROL_TYPE_FOLDER = OresHelper.createOLATResourceableTypeWithoutCheck(FolderRunController.class
			.getSimpleName());

	private HomePageDisplayController homePageDisplayController;
	private WeeklyCalendarController calendarController;
	private ContactFormController contactFormController;
	private FolderRunController folderRunController;

	private Identity chosenIdentity;
	private String firstLastName;
	private Controller portfolioController;

	/**
	 * @param ureq
	 * @param wControl
	 * @param chosenIdentity
	 */
	public UserInfoMainController(UserRequest ureq, WindowControl wControl, Identity chosenIdentity) {
		super(ureq, wControl);
		
		this.chosenIdentity = chosenIdentity;

		main = new Panel("userinfomain");

		main.setContent(createComponent(ureq, CMD_HOMEPAGE, chosenIdentity));

		StringBuilder sb = new StringBuilder();
		sb.append(chosenIdentity.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()));
		sb.append(" ");
		sb.append(chosenIdentity.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()));
		this.firstLastName = sb.toString();

		// Navigation menu
		this.menuTree = new MenuTree("menuTree");
		GenericTreeModel tm = buildTreeModel(firstLastName);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getChildAt(0).getIdent());
		menuTree.addListener(this);

		LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, main, "userinfomain");
		listenTo(columnLayoutCtr);
		//
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		// Activate child controllers if a usable context entry is found
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if (ce != null) { // a context path is left for me
			OLATResourceable ores = ce.getOLATResourceable();
			if (OresHelper.equals(ores, BUSINESS_CONTROL_TYPE_FOLDER)) {
				// Activate folder controller
				menuTree.setSelectedNode(tm.findNodeByUserObject(CMD_FOLDER));
				main.setContent(createComponent(ureq, CMD_FOLDER, chosenIdentity));
			}
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // goto
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				main.setContent(createComponent(ureq, cmd, chosenIdentity));
			}
		}
		// no events from main
		// no events from intro
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
	//
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
		boolean isInvitee = BaseSecurityManager.getInstance().isIdentityInvited(chosenIdentity);
		boolean isDeleted = chosenIdentity.getStatus().equals(Identity.STATUS_DELETED);
		
		if ( !isDeleted && ! isInvitee) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.calendar"));
			gtn.setUserObject(CMD_CALENDAR);
			gtn.setAltText(translate("menu.calendar.alt"));
			root.addChild(gtn);
	
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.folder"));
			gtn.setUserObject(CMD_FOLDER);
			gtn.setAltText(translate("menu.folder.alt"));
			root.addChild(gtn);
		}	
		if ( !isDeleted) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.contact"));
			gtn.setUserObject(CMD_CONTACT);
			gtn.setAltText(translate("menu.contact.alt"));
			root.addChild(gtn);
		}
		if ( !isDeleted && ! isInvitee) {
			PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
			if (portfolioModule.isEnabled()) {
				gtn = new GenericTreeNode();
				gtn.setTitle(translate("menu.portfolio"));
				gtn.setUserObject(CMD_PORTFOLIO);
				gtn.setAltText(translate("menu.portfolio.alt"));
				root.addChild(gtn);
			}
		}			
		return gtm;
	}

	private Component createComponent(UserRequest ureq, String menuCommand, Identity identity) {
		myContent = createVelocityContainer("userinfo");

		if (menuCommand.equals(CMD_HOMEPAGE)) {
			String userName = identity.getName();
			HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
			HomePageConfig homePageConfig = hpcm.loadConfigFor(userName);
			removeAsListenerAndDispose(homePageDisplayController);
			homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), homePageConfig);
			listenTo(homePageDisplayController);
			myContent.put("userinfo", homePageDisplayController.getInitialComponent());

		} else if (menuCommand.equals(CMD_CALENDAR)) {
			CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
			KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(identity);
			calendarWrapper.setKalendarConfig(new KalendarConfig(identity.getName(), KalendarRenderWrapper.CALENDAR_COLOR_BLUE, true));
			KalendarConfig config = calendarManager.findKalendarConfigForIdentity(calendarWrapper.getKalendar(), ureq);
			if (config != null) {
				calendarWrapper.getKalendarConfig().setCss(config.getCss());
				calendarWrapper.getKalendarConfig().setVis(config.isVis());
			}
			if (ureq.getUserSession().getRoles().isOLATAdmin() || identity.getName().equals(ureq.getIdentity().getName()))
				calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			else
				calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			List calendars = new ArrayList();
			calendars.add(calendarWrapper);
			removeAsListenerAndDispose(calendarController);
			calendarController = new WeeklyCalendarController(ureq, getWindowControl(), calendars,
					WeeklyCalendarController.CALLER_PROFILE, true);
			listenTo(calendarController);
			myContent.put("userinfo", calendarController.getInitialComponent());
		} else if (menuCommand.equals(CMD_FOLDER)) {

			String chosenUserFolderRelPath = FolderConfig.getUserHome(identity.getName()) + "/public";

			OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(chosenUserFolderRelPath, null);
			OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(firstLastName, rootFolder);
			
			//decided in plenum to have read only view in the personal visit card, even for admin
			VFSSecurityCallback secCallback = new ReadOnlyCallback();
			namedFolder.setLocalSecurityCallback(secCallback);
			
			removeAsListenerAndDispose(folderRunController);
			folderRunController = new FolderRunController(namedFolder, false, true, false, ureq, getWindowControl());
			listenTo(folderRunController);
			myContent.put("userinfo", folderRunController.getInitialComponent());

		} else if (menuCommand.equals(CMD_CONTACT)) {
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			ContactList emailList = new ContactList(firstLastName);
			emailList.add(identity);
			cmsg.addEmailTo(emailList);
			removeAsListenerAndDispose(contactFormController);
			contactFormController = new ContactFormController(ureq, getWindowControl(), true,true,false,false,cmsg);
			listenTo(contactFormController);
			myContent.put("userinfo", contactFormController.getInitialComponent());
		} else if (menuCommand.equals(CMD_WEBLOG)) {
//			weblogController = new WeblogMainController(ureq, getWindowControl(), chosenIdentity);
//			listenTo(weblogController);
//			myContent.put("userinfo", weblogController.getInitialComponent());
		} else if (menuCommand.equals(CMD_PORTFOLIO)) {
			removeAsListenerAndDispose(portfolioController);
			portfolioController = EPUIFactory.createPortfolioMapsVisibleToOthersController(ureq, getWindowControl(), chosenIdentity);
			listenTo(portfolioController);
			myContent.put("userinfo", portfolioController.getInitialComponent());
		}
		return myContent;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers are disposed by BasicController
	}

}