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
package org.olat.gui.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.impressum.ImpressumInformations;
import org.olat.core.commons.controllers.impressum.ImpressumMainController;
import org.olat.core.commons.controllers.impressum.ImpressumModule;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.home.HomeMainController;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.InstantMessagingMainController;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;
import org.olat.user.DisplayPortraitController;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OlatTopNavController extends BasicController implements GenericEventListener {
	
	private static final String ACTION_LOGOUT = "logout";
	private SearchInputController searchC;
	private Link helpLink, loginLink, impressumLink;

	private VelocityContainer menuVC;
	private VelocityContainer topNavVC;
	private InstantMessagingMainController imController;
	
	private EventBus singleUserEventCenter;
	private OLATResourceable ass;

	@Autowired
	private ExtManager extManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ImpressumModule impressumModule;
	@Autowired
	private UserToolsModule userToolsModule;
	@Autowired
	private DisplayPortraitManager portraitManager;
	
	public OlatTopNavController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, true);
	}
	
	public OlatTopNavController(UserRequest ureq, WindowControl wControl,  boolean search) {
		super(ureq, wControl);
		topNavVC = createVelocityContainer("topnav");
		topNavVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacmenet ID
		
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		boolean isInvitee = ureq.getUserSession().getRoles().isInvitee();
		topNavVC.contextPut("isGuest", new Boolean(isGuest));
		topNavVC.contextPut("isInvitee", new Boolean(isInvitee));
		
		// instant messaging area, only when enabled and user is not a guest user
		if (CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled() && !isGuest && !isInvitee) {
			imController = new InstantMessagingMainController(ureq, getWindowControl());
			listenTo(imController);
			topNavVC.put("imclient", imController.getInitialComponent());
		}

		// the help link
		if(!isInvitee && CourseModule.isHelpCourseEnabled()) {
			helpLink = LinkFactory.createLink("topnav.help", topNavVC, this);
			helpLink.setIconLeftCSS("o_icon o_icon_help o_icon-lg");
			helpLink.setTooltip("topnav.help.alt");
			helpLink.setTarget("_help");
		}
		
		// login link
		if (ureq.getIdentity() == null) {
			loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
			loginLink.setIconLeftCSS("o_icon o_icon_login o_icon-lg");
			loginLink.setTooltip("topnav.login.alt");
		}
		
		topNavVC.contextPut("impressumInfos", new ImpressumInformations(impressumModule));
		impressumLink = LinkFactory.createLink("topnav.impressum", topNavVC, this);
		impressumLink.setTooltip("topnav.impressum.alt");
		impressumLink.setIconLeftCSS("o_icon o_icon_impress o_icon-lg");
		impressumLink.setAjaxEnabled(false);
		impressumLink.setTarget("_blank");

		
		if(ureq.getIdentity() != null && !isGuest && !isInvitee) {
			if(search) {
				SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
				searchC = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
				searchC.setResourceContextEnable(false);
				topNavVC.put("search_input", searchC.getInitialComponent());
			}

			ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
			singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
			singleUserEventCenter.registerFor(this, getIdentity(), ass);
			
			Component menu =  getMenuCmp(ureq);
			topNavVC.put("myMenu", menu);
			// the user profile
			Component portrait = getPortraitCmp(ureq);
			topNavVC.put("portrait", portrait);
			
			// the label to open the personal menu
			User user = getIdentity().getUser();
			String[] attr = new String[] { user.getProperty(UserConstants.FIRSTNAME, getLocale()), user.getProperty(UserConstants.LASTNAME, getLocale()), getIdentity().getName()};
			String myMenuText = translate("topnav.my.menu.label", attr);
			topNavVC.contextPut("myMenuLabel", myMenuText);
		}
		
		putInitialPanel(topNavVC);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == helpLink) {
			doOpenHelp(ureq) ;
		} else if(source instanceof Link && source.getComponentName().startsWith("personal.tool.")) {
			doOpenPersonalTool(ureq, (Link)source);
		} else if (source == loginLink) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		} else if (source == menuVC) {
			String command = event.getCommand();
			if (command.equals(ACTION_LOGOUT)) {
				AuthHelper.doLogout(ureq);
			}
		} else if (source == impressumLink) {
			doOpenImpressum(ureq);
		}
	}
	
	private void doOpenPersonalTool(UserRequest ureq, Link link) {
		GenericActionExtension gAe = (GenericActionExtension)link.getUserObject();
		String navKey = gAe.getNavigationKey();
		String businessPath = "[HomeSite:" + getIdentity().getKey() + "][" + navKey + ":0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private Component getMenuCmp(UserRequest ureq) {
		VelocityContainer container = createVelocityContainer("menu");
		container.setDomReplacementWrapperRequired(false); // we do it ourself in menu.html file
		loadPersonalTools(ureq, container);
		menuVC = container;
		return menuVC;
	}
	
	private Component getPortraitCmp(UserRequest ureq) {
		Controller ctr = new DisplayPortraitController(ureq, getWindowControl(), getIdentity(), false, false, false, true);
		listenTo(ctr);
		return ctr.getInitialComponent();
	}
	
	private void loadPersonalTools(UserRequest ureq, VelocityContainer container) {
		List<String> linksName = new ArrayList<String>();
		List<String> configLinksName = new ArrayList<String>();
		List<String> toolSetLinksName = new ArrayList<String>();
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		String selectedTools = (String)prefs.get(WindowManager.class, "user-tools");
		if(!StringHelper.containsNonWhitespace(selectedTools)) {
			selectedTools = userToolsModule.getDefaultPresetOfUserTools();
		}
		Set<String> selectedToolSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(selectedTools)) {
			String[] selectedToolArr = selectedTools.split(",");
			for(String selectedTool:selectedToolArr) {
				selectedToolSet.add(selectedTool);
			}
		}
		
		Set<String> availableToolSet = userToolsModule.getAvailableUserToolSet();

		for (Extension anExt : extManager.getExtensions()) {
			// check for sites
			ExtensionElement ae = anExt.getExtensionFor(HomeMainController.class.getName(), ureq);
			if (anExt.isEnabled() && ae instanceof GenericActionExtension) {
				GenericActionExtension gAe = (GenericActionExtension) ae;
				String extensionId = gAe.getUniqueExtensionID();
				if(availableToolSet.contains(extensionId)) {
					GenericTreeNode node = gAe.createMenuNode(ureq);
					String linkName = "personal.tool." + node.getIdent();
					Link link = LinkFactory.createLink(linkName, container, this);
					link.setUserObject(gAe);
					String label = gAe.getActionText(getLocale());
					link.setCustomDisplayText(label);
					String iconCssClass = node.getIconCssClass();
					link.setIconLeftCSS(iconCssClass);
					link.setElementCssClass("o_sel_user_tools-" + gAe.getNavigationKey());
					
					if(!StringHelper.containsNonWhitespace(gAe.getParentTreeNodeIdentifier())) {
						linksName.add(linkName);
					} else if("config".equals(gAe.getParentTreeNodeIdentifier())) {
						configLinksName.add(linkName);
					}
					
					if(selectedToolSet.contains(extensionId)) {
						String linkAltName = "personal.tool.alt." + node.getIdent();
						Link linkAlt = LinkFactory.createLink(linkAltName, topNavVC, this);
						linkAlt.setUserObject(gAe);
						if(!StringHelper.containsNonWhitespace(iconCssClass)) {
							linkAlt.setCustomDisplayText(label);
						} else {
							linkAlt.setCustomDisplayText("");
						}
						linkAlt.setIconLeftCSS(iconCssClass + " o_icon-lg");
						toolSetLinksName.add(linkAltName);
					}
				}
			}
		}
		
		container.contextPut("personalTools", linksName);
		container.contextPut("configs", configLinksName);
		topNavVC.contextPut("toolSet", toolSetLinksName);
	}
	
	protected void doOpenHelp(UserRequest ureq) {
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return CourseFactory.createHelpCourseLaunchController(lureq, lwControl);
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	protected void doOpenImpressum(UserRequest ureq) {
		ControllerCreator impressumControllerCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return new ImpressumMainController(lureq, lwControl);
			}
		};
		PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewPopupBrowserWindowFor(ureq, impressumControllerCreator);
		popupBrowserWindow.open(ureq);
	}

	@Override
	protected void doDispose() {
		//controllers are disposed by BasicController
		// im header controller mus be disposed last - content or navigation control
		// controller
		// might try to send a IM presence message which would lazy generate a new
		// IM client.
		// the IM client gets disposed in the header controller
		
		if (singleUserEventCenter != null) {
			singleUserEventCenter.deregisterFor(this, ass);
		}
		if(searchC != null) {
			searchC.dispose();
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof AssessmentEvent) {
			AssessmentEvent ae = (AssessmentEvent)event;
			if(ae.getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
				topNavVC.contextPut("inAssessment", true);
			} else if(ae.getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
				OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
				if (singleUserEventCenter.getListeningIdentityCntFor(a)<1) {
					topNavVC.contextPut("inAssessment", false);
				}
			} 
		}
	}
}