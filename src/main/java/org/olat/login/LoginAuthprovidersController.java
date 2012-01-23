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
package org.olat.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.sysinfo.InfoMessageManager;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
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
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.nodes.INode;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.auth.AuthenticationProvider;

/**
 * Description:<br>
 * TODO: patrickb Class Description for LoginAuthprovidersController
 * 
 * <P>
 * Initial Date:  02.09.2007 <br>
 * @author patrickb
 */
public class LoginAuthprovidersController extends MainLayoutBasicController implements Activateable2 {


	private static final String ACTION_LOGIN = "login";
	public  static final String ATTR_LOGIN_PROVIDER = "lp";
	private static final String ACTION_COOKIES = "cookies";
	private static final String ACTION_ABOUT = "about";
	private static final String ACTION_ACCESSIBILITY = "accessibility";
	private static final String ACTION_BROWSERCHECK = "check";
	private static final String ACTION_GUEST = "guest";

	private VelocityContainer content;
	private Controller authController;
	private final List<Controller> authControllers = new ArrayList<Controller>();
	private Panel dmzPanel;
	private GenericTreeNode checkNode;
	private GenericTreeNode accessibilityNode;
	private GenericTreeNode aboutNode;
	private MenuTree olatMenuTree;
	private LayoutMain3ColsController columnLayoutCtr;
	

	public LoginAuthprovidersController(UserRequest ureq, WindowControl wControl) {
		// Use fallback translator from full webapp package to translate accessibility stuff
		super(ureq, wControl, Util.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));
		//
		if(ureq.getUserSession().getEntry("error.change.email") != null) {
			wControl.setError(ureq.getUserSession().getEntry("error.change.email").toString());
			ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email");
		}
		if(ureq.getUserSession().getEntry("error.change.email.time") != null) {
			wControl.setError(ureq.getUserSession().getEntry("error.change.email.time").toString());
			ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email.time");
		}
		
		dmzPanel = new Panel("content");
		content = initLoginContent(ureq, null);
		dmzPanel.pushContent(content);

		// DMZ navigation
		olatMenuTree = new MenuTree("dmz_menu", "olatMenuTree");				
		TreeModel tm = buildTreeModel(); 
		olatMenuTree.setTreeModel(tm);
		olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		olatMenuTree.addListener(this);

		// Activate correct position in menu
		INode firstChild = tm.getRootNode().getChildAt(0);
		olatMenuTree.setSelectedNodeId(firstChild.getIdent());

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, dmzPanel, "useradminmain");
		columnLayoutCtr.addCssClassToMain("o_loginscreen");
		listenTo(columnLayoutCtr); // for later autodisposing
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	//fxdiff FXOLAT-113: business path in DMZ
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("browsercheck".equals(type)) {
			showBrowserCheckPage(ureq);
			olatMenuTree.setSelectedNodeId(checkNode.getIdent());
		} else if ("accessibility".equals(type)) {
			showAccessibilityPage();
			olatMenuTree.setSelectedNodeId(accessibilityNode.getIdent());
		} else if ("about".equals(type)) {
			showAboutPage(ureq);
			olatMenuTree.setSelectedNodeId(aboutNode.getIdent());
		} else if(authController instanceof Activateable2) {
			((Activateable2)authController).activate(ureq, entries, state);
		}
	}

	private VelocityContainer initLoginContent(UserRequest ureq, String provider) {
		// in every case we build the container for pages to fill the panel
		VelocityContainer contentBorn = createVelocityContainer("main_loging", "login");

		// browser not supported messages
		// true if browserwarning should be showed
		boolean bwo = Settings.isBrowserAjaxBlacklisted(ureq);
		contentBorn.contextPut("browserWarningOn", bwo ? Boolean.TRUE : Boolean.FALSE);
		
		// prepare login
		if (provider == null)	provider = LoginModule.getDefaultProviderName();
		AuthenticationProvider authProvider = LoginModule.getAuthenticationProvider(provider);
		if (authProvider == null)
			throw new AssertException("Invalid authentication provider: " + provider);
		
		//clean-up controllers
		if(authController != null) {
			removeAsListenerAndDispose(authController);
		}
		for(Controller controller:authControllers) {
			removeAsListenerAndDispose(controller);
		}
		authControllers.clear();
		
		//recreate controllers
		authController = authProvider.createController(ureq, getWindowControl());
		listenTo(authController);
		contentBorn.put("loginComp", authController.getInitialComponent());
		Collection<AuthenticationProvider> providers = LoginModule.getAuthenticationProviders();
		List<AuthenticationProvider> providerSet = new ArrayList<AuthenticationProvider>(providers.size());
		int count = 0;
		for (AuthenticationProvider prov : providers) {
			if (prov.isEnabled()) {
				providerSet.add(prov);
				if(!prov.getName().equals(authProvider.getName())) {
					//hang these components to the component tree, for state-less behavior
					Controller controller = prov.createController(ureq, getWindowControl());
					authControllers.add(controller);
					Component cmp = controller.getInitialComponent();
					contentBorn.put("dormant_" + count++, cmp);
					listenTo(controller);
				}
			}
		}
		providerSet.remove(authProvider); // remove active authProvider from list of alternate authProviders
		contentBorn.contextPut("providerSet", providerSet);
		contentBorn.contextPut("locale", ureq.getLocale());

		// prepare info message
		InfoMessageManager mrg = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		String infomsg = mrg.getInfoMessage();
		if (infomsg != null && infomsg.length() > 0)
			contentBorn.contextPut("infomsg", infomsg);
		
		String infomsgNode = mrg.getInfoMessageNodeOnly();
		if (infomsgNode != null && infomsgNode.length() > 0)
			contentBorn.contextPut("infomsgNode", infomsgNode);
		
		return contentBorn;
	}

	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//auto-disposed
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == olatMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // process menu commands
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				//
				dmzPanel.popContent();
				if (cmd.equals(ACTION_LOGIN)) {
					content = initLoginContent(ureq, LoginModule.getDefaultProviderName());
					dmzPanel.pushContent(content);
				} else if (cmd.equals(ACTION_GUEST)) {
					int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
					if (loginStatus == AuthHelper.LOGIN_OK) {
						return;
					} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
						getWindowControl().setError(translate("login.notavailable", WebappHelper.getMailConfig("mailSupport")));
					} else {
						getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
					}
				} else if (cmd.equals(ACTION_BROWSERCHECK)) {
					showBrowserCheckPage(ureq);//fxdiff FXOLAT-113: business path in DMZ
				} else if (cmd.equals(ACTION_COOKIES)) {
					dmzPanel.pushContent(createVelocityContainer("cookies"));
				} else if (cmd.equals(ACTION_ABOUT)) {
					showAboutPage(ureq);//fxdiff FXOLAT-113: business path in DMZ
				} else if (cmd.equals(ACTION_ACCESSIBILITY)) {
					showAccessibilityPage();//fxdiff FXOLAT-113: business path in DMZ
				}
			}
		} else if (event.getCommand().equals(ACTION_LOGIN)) { 
			// show traditional login page
			dmzPanel.popContent();
			content = initLoginContent(ureq, ureq.getParameter(ATTR_LOGIN_PROVIDER));
			dmzPanel.pushContent(content);
		}
	}
	//fxdiff FXOLAT-113: business path in DMZ
	protected void showAccessibilityPage() {
		VelocityContainer accessibilityVC = createVelocityContainer("accessibility");
		dmzPanel.pushContent(accessibilityVC);
	}
	//fxdiff FXOLAT-113: business path in DMZ
	protected void showBrowserCheckPage(UserRequest ureq) {
		VelocityContainer browserCheck = createVelocityContainer("browsercheck");
		browserCheck.contextPut("isBrowserAjaxReady", Boolean.valueOf(!Settings.isBrowserAjaxBlacklisted(ureq)));
		dmzPanel.pushContent(browserCheck);
	}
	//fxdiff FXOLAT-113: business path in DMZ
	protected void showAboutPage(UserRequest ureq) {
	//fxdiff FXOLAT-139
		VelocityContainer aboutVC = createVelocityContainer("about");
		// Add version info and licenses
		aboutVC.contextPut("version", Settings.getFullVersionInfo());
		aboutVC.contextPut("license", WebappHelper.getOlatLicense());
		// Add translator and languages info
		I18nManager i18nMgr = I18nManager.getInstance();
		Set<String> enabledKeysSet = I18nModule.getEnabledLanguageKeys();
		Map<String, String> langNames = new HashMap<String, String>();
		Map<String, String> langTranslators = new HashMap<String, String>();
		String[] enabledKeys = ArrayHelper.toArray(enabledKeysSet);
		String[] names = new String[enabledKeys.length];
		for (int i = 0; i < enabledKeys.length; i++) {
			String key = enabledKeys[i];
			String langName = i18nMgr.getLanguageInEnglish(key, I18nModule.isOverlayEnabled());
			langNames.put(key, langName);
			names[i] = langName;
			String author = i18nMgr.getLanguageAuthor(key);
			langTranslators.put(key, author);
		}
		ArrayHelper.sort(enabledKeys, names, true, true, true);
		aboutVC.contextPut("enabledKeys", enabledKeys);
		aboutVC.contextPut("langNames", langNames);
		aboutVC.contextPut("langTranslators", langTranslators);
		dmzPanel.pushContent(aboutVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event == Event.CANCELLED_EVENT) {
			// is a Form cancelled, show Login Form
			content = initLoginContent(ureq, null);
			dmzPanel.setContent(content);
		}else if (event instanceof AuthenticationEvent) {
			AuthenticationEvent authEvent = (AuthenticationEvent)event;
			Identity identity = authEvent.getIdentity();
			int loginStatus = AuthHelper.doLogin(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), ureq);
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return;
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				//getWindowControl().setError(translate("login.notavailable", OLATContext.getSupportaddress()));
				DispatcherAction.redirectToServiceNotAvailable( ureq.getHttpResp() );
			} else {
				// fxdiff: show useradmin-mail for pw-requests
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
			}
		
		}
	}

	
	private TreeModel buildTreeModel() {
		GenericTreeNode root, gtn;
		
		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode("dmz_login");
		root.setTitle(translate("menu.root"));
		root.setUserObject(ACTION_LOGIN);
		root.setAltText(translate("menu.root.alt"));
		gtm.setRootNode(root);
		
		gtn = new GenericTreeNode("login_item");
		gtn.setTitle(translate("menu.login"));
		gtn.setUserObject(ACTION_LOGIN);
		gtn.setAltText(translate("menu.login.alt"));
		root.addChild(gtn);
		root.setDelegate(gtn);		

		if (LoginModule.isGuestLoginLinksEnabled()) {
			gtn = new GenericTreeNode("guest_item");		
			gtn.setTitle(translate("menu.guest"));
			gtn.setUserObject(ACTION_GUEST);
			gtn.setAltText(translate("menu.guest.alt"));
			root.addChild(gtn);
		}
		
		gtn = checkNode = new GenericTreeNode("check_item");//fxdiff FXOLAT-113: business path in DMZ
		gtn.setTitle(translate("menu.check"));
		gtn.setUserObject(ACTION_BROWSERCHECK);
		gtn.setAltText(translate("menu.check.alt"));
		root.addChild(gtn);

		gtn = accessibilityNode = new GenericTreeNode("accessiblity_item");//fxdiff FXOLAT-113: business path in DMZ
		gtn.setTitle(translate("menu.accessibility"));
		gtn.setUserObject(ACTION_ACCESSIBILITY);
		gtn.setAltText(translate("menu.accessibility.alt"));
		root.addChild(gtn);

		gtn = aboutNode = new GenericTreeNode("about_item");//fxdiff FXOLAT-113: business path in DMZ
		gtn.setTitle(translate("menu.about"));
		gtn.setUserObject(ACTION_ABOUT);
		gtn.setAltText(translate("menu.about.alt"));
		root.addChild(gtn);

		return gtm;
	}
	
	
	
	
	
}
