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
* <p>
*/ 

package org.olat.admin;

import org.olat.admin.extensions.ExtensionsAdminController;
import org.olat.admin.instantMessaging.InstantMessagingAdminController;
import org.olat.admin.jmx.JMXInfoController;
import org.olat.admin.layout.LayoutAdminController;
import org.olat.admin.notifications.NotificationsEmailAdminController;
import org.olat.admin.properties.AdvancedPropertiesController;
import org.olat.admin.quota.QuotaController;
import org.olat.admin.registration.SystemRegistrationAdminController;
import org.olat.admin.restapi.RestapiAdminController;
import org.olat.admin.search.SearchAdminController;
import org.olat.admin.statistics.StatisticsAdminController;
import org.olat.admin.sysinfo.SysinfoController;
import org.olat.admin.version.VersionAdminController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.i18n.ui.I18nUIFactory;
import org.olat.core.util.nodes.INode;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.search.service.SearchServiceFactory;

/**
 * Description:
 * <p>
 * The System admin main controller offers various administrative tools for
 * users with administative rights. The controller menu can be extended using
 * the ActionExtension interface
 * <p>
 * Initial Date: Apr 27, 2004
 * 
 * @author Felix Jost
 */
public class SystemAdminMainController extends MainLayoutBasicController {
	private static boolean extensionLogged = false;
	private static boolean configExtensionLogged = false;

	private MenuTree olatMenuTree;
	private Panel content;
	
	private LayoutMain3ColsController columnsLayoutCtr;
	private Controller contentCtr;
	
	/**
	 * Constructor of the home main controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public SystemAdminMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
		
		olatMenuTree = new MenuTree("olatMenuTree");				
		TreeModel tm = buildTreeModel(); 
		olatMenuTree.setTreeModel(tm);
		INode firstNode = tm.getRootNode().getChildAt(0);
		olatMenuTree.setSelectedNodeId(firstNode.getIdent());
		olatMenuTree.addListener(this);

		String cmd = (String) tm.getRootNode().getUserObject();
		contentCtr = createController(cmd, ureq);
		listenTo(contentCtr); // auto dispose later
		Component resComp = contentCtr.getInitialComponent();
		
		content = new Panel("content");
		content.setContent(resComp);

		columnsLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree,null, content, "sysadminmain");
		columnsLayoutCtr.addCssClassToMain("o_admin");
		
		listenTo(columnsLayoutCtr); // auto dispose later
		putInitialPanel(columnsLayoutCtr.getInitialComponent());
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == olatMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // process menu commands
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				if (selTreeNode.getDelegate() != null) {
					selTreeNode= selTreeNode.getDelegate();
					olatMenuTree.setSelectedNode(selTreeNode); // enable right element
				}
				// cleanup old content controller (never null)
				removeAsListenerAndDispose(contentCtr);
				// create new content controller
				contentCtr = createController(selTreeNode.getUserObject(), ureq);
				listenTo(contentCtr);
				Component resComp = contentCtr.getInitialComponent();
				content.setContent(resComp);
			} else { // the action was not allowed anymore
				content.setContent(null); // display an empty field (empty panel)
			}
		} else {
			logWarn("Unhandled olatMenuTree event: " + event.getCommand(), null);
		}
	}

	private Controller createController(Object uobject, UserRequest ureq) {
		if (uobject.equals("jmx")) {
			return new JMXInfoController(ureq, getWindowControl());
		} else if (uobject.equals("sysinfo") || uobject.equals("admin")) {
			return new SysinfoController(ureq, getWindowControl());
		}	else if (uobject.equals("imadmin")) {
			return new InstantMessagingAdminController(ureq, getWindowControl());
		}	else if (uobject.equals("registration")) {
			return new SystemRegistrationAdminController(ureq, getWindowControl());
		}	else if (uobject.equals("quota")) {
			return new QuotaController(ureq, getWindowControl());
		}	else if (uobject.equals("advancedproperties")) {
			return new AdvancedPropertiesController(ureq, getWindowControl());
		}	else if (uobject.equals("onlinetranslation")) {
			return I18nUIFactory.createTranslationToolLauncherController(ureq, getWindowControl());
		} else if (uobject.equals("search")) {
			return new SearchAdminController(ureq, getWindowControl());
		} else if (uobject.equals("notifications")) {
			return new NotificationsEmailAdminController(ureq, getWindowControl());
		} else if (uobject.equals("statistics")) {
			return new StatisticsAdminController(ureq, getWindowControl());
		} else if(uobject.equals("layout")){
			return new LayoutAdminController(ureq, getWindowControl());
		} else if(uobject.equals("i18n")){
			return I18nUIFactory.createI18nConfigurationController(ureq, getWindowControl());
		} else if(uobject.equals("versions")){
			return new VersionAdminController(ureq, getWindowControl());
		} else if(uobject.equals("restapi")){
			return new RestapiAdminController(ureq, getWindowControl());
		} else if(uobject.equals("extensions")){
			return new ExtensionsAdminController(ureq, getWindowControl());
		} else if (uobject instanceof ActionExtension) {
			ActionExtension ae = (ActionExtension) uobject;
			return ae.createController(ureq, getWindowControl(), null);
		}
		return null;
	}

	private TreeModel buildTreeModel() {
		GenericTreeNode gtnChild, admin, confSub;
		Translator translator = getTranslator();
		
		GenericTreeModel gtm = new GenericTreeModel();
		admin = new GenericTreeNode();		
		admin.setTitle(translator.translate("menu.admin"));
		admin.setUserObject("admin");
		admin.setAltText(translator.translate("menu.admin.alt"));
		gtm.setRootNode(admin);

		//
		// The sysinfo stuff
		//

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.sysinfo"));
		gtnChild.setUserObject("sysinfo");
		gtnChild.setAltText(translator.translate("menu.sysinfo.alt"));
		admin.addChild(gtnChild);
		admin.setDelegate(gtnChild);

		//
		// The system config submenu
		//

		confSub = new GenericTreeNode();		
		confSub.setTitle(translator.translate("menu.config"));
		confSub.setUserObject("config");
		confSub.setAltText(translator.translate("menu.config.alt"));
		admin.addChild(confSub);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.layout"));
		gtnChild.setUserObject("layout");
		gtnChild.setAltText(translator.translate("menu.layout.alt"));
		confSub.addChild(gtnChild);
		confSub.setDelegate(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.i18n"));
		gtnChild.setUserObject("i18n");
		gtnChild.setAltText(translator.translate("menu.i18n.alt"));
		confSub.addChild(gtnChild);

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.quota"));
		gtnChild.setUserObject("quota");
		gtnChild.setAltText(translator.translate("menu.quota.alt"));
		confSub.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.versions"));
		gtnChild.setUserObject("versions");
		gtnChild.setAltText(translator.translate("menu.versions.alt"));
		confSub.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.restapi"));
		gtnChild.setUserObject("restapi");
		gtnChild.setAltText(translator.translate("menu.restapi.alt"));
		confSub.addChild(gtnChild);
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.extensions"));
		gtnChild.setUserObject("extensions");
		gtnChild.setAltText(translator.translate("menu.extensions.alt"));
		confSub.addChild(gtnChild);

		//
		// other tools and stuff
		//
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.registration"));
		gtnChild.setUserObject("registration");
		gtnChild.setAltText(translator.translate("menu.registration.alt"));
		admin.addChild(gtnChild);
		
		
		if (InstantMessagingModule.isEnabled()) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.imadmin"));
			gtnChild.setUserObject("imadmin");
			gtnChild.setAltText(translator.translate("menu.imadmin.alt"));
			admin.addChild(gtnChild);
		}

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.advancedproperties"));
		gtnChild.setUserObject("advancedproperties");
		gtnChild.setAltText(translator.translate("menu.advancedproperties.alt"));
		admin.addChild(gtnChild);
		
		// show translation tool or cusomize link, not both
		if (I18nModule.isTransToolEnabled()) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.onlinetranslation"));
			gtnChild.setUserObject("onlinetranslation");
			gtnChild.setAltText(translator.translate("menu.onlinetranslation.alt"));
			admin.addChild(gtnChild);					
		} else if (I18nModule.isOverlayEnabled()) {
			gtnChild = new GenericTreeNode();		
			gtnChild.setTitle(translator.translate("menu.onlinetranslation.customize"));
			gtnChild.setUserObject("onlinetranslation");
			gtnChild.setAltText(translator.translate("menu.onlinetranslation.customize.alt"));
			admin.addChild(gtnChild);								
		}
		
		
		if (SearchServiceFactory.getService().isEnabled()) {
			//since 6.0.3 the search service is distributed
			//we have to check if we are the instance having
			//the local search service, e.g. the one for creating the index
			//and deliver results for queries.
			gtnChild = new GenericTreeNode();
			gtnChild.setTitle(translator.translate("menu.search"));
			gtnChild.setUserObject("search");
			gtnChild.setAltText(translator.translate("menu.search.alt"));
			admin.addChild(gtnChild);
		}
		
		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.notifications"));
		gtnChild.setUserObject("notifications");
		gtnChild.setAltText(translator.translate("menu.notifications.alt"));
		admin.addChild(gtnChild);
		

		gtnChild = new GenericTreeNode();		
		gtnChild.setTitle(translator.translate("menu.statistics"));
		gtnChild.setUserObject("statistics");
		gtnChild.setAltText(translator.translate("menu.statistics.alt"));
		admin.addChild(gtnChild);
		
// todo: 2009-09.22/cg move JMX menu-item to testing tab, because it is a beta-version
//		gtnChild = new GenericTreeNode();		
//		gtnChild.setTitle(translator.translate("menu.jmx"));
//		gtnChild.setUserObject("jmx");
//		gtnChild.setAltText(translator.translate("menu.jmx.alt"));
//		admin.addChild(gtnChild);

		//add extension menues
		ExtManager extm = ExtManager.getInstance();
		int cnt = extm.getExtensionCnt();
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// 1) general menu extensions
			ActionExtension ae = (ActionExtension) anExt.getExtensionFor(SystemAdminMainController.class.getName());
			if (ae != null && anExt.isEnabled()) {
				gtnChild = new GenericTreeNode();
				String menuText = ae.getActionText(getLocale());
				gtnChild.setTitle(menuText);
				gtnChild.setUserObject(ae);
				gtnChild.setAltText(ae.getDescription(getLocale()));
				if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getNodeIdentifierIfParent() != null) {
					gtnChild.setIdent(((GenericActionExtension) ae).getNodeIdentifierIfParent());
				}
				if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getParentTreeNodeIdentifier() != null){
					GenericTreeNode parentNode = (GenericTreeNode) gtm.getNodeById(((GenericActionExtension) ae).getParentTreeNodeIdentifier());
					parentNode.addChild(gtnChild);
					if (parentNode.getDelegate() == null) parentNode.setDelegate(gtnChild);
				} else {
					admin.addChild(gtnChild);
				}
				// inform only once
				if (!extensionLogged) {
					extensionLogged = true;
					extm.inform(SystemAdminMainController.class, anExt, "added menu entry (for locale " + getLocale().toString() + " '" + menuText + "'");
				}
			} 
			// 2) check for system configuration submenu / dynamic action submenue extensions
			ae = (ActionExtension) anExt.getExtensionFor(SystemAdminMainController.class.getName() + "_configuration");
			if (ae != null) {
				gtnChild = new GenericTreeNode();
				String menuText = ae.getActionText(getLocale());
				gtnChild.setTitle(menuText);
				gtnChild.setUserObject(ae);
				gtnChild.setAltText(ae.getDescription(getLocale()));
				if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getParentTreeNodeIdentifier() != null){
					GenericTreeNode parentNode = (GenericTreeNode) gtm.getNodeById(((GenericActionExtension) ae).getParentTreeNodeIdentifier());
					parentNode.addChild(gtnChild);
					if (parentNode.getDelegate() == null) parentNode.setDelegate(gtnChild);
				} else {
					confSub.addChild(gtnChild);
				}
				// inform only once
				if (!configExtensionLogged) {
					configExtensionLogged = true;
					extm.inform(SystemAdminMainController.class, anExt, "added configuration submenu entry (for locale " + getLocale().toString() + " '" + menuText + "'");
				}
			} 
		}
		
		return gtm;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controller disposed by BasicController
	}

}
