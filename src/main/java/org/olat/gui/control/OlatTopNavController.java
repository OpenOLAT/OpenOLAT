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

import org.olat.admin.help.ui.HelpAdminController;
import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.user.DisplayPortraitController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OlatTopNavController extends BasicController implements LockableController {
	
	private Link loginLink;
	private VelocityContainer topNavVC;
	private List<Disposable> disposableTools = new ArrayList<>();

	@Autowired
	private UserToolsModule userToolsModule;
	@Autowired
	private HelpModule helpModule;
	
	public OlatTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		// Include translator for help
		setTranslator(Util.createPackageTranslator(HelpAdminController.class, getLocale(), getTranslator()));
		
		topNavVC = createVelocityContainer("topnav");
		topNavVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacmenet ID
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isGuest = roles.isGuestOnly();
		boolean isInvitee = roles.isInvitee();
		topNavVC.contextPut("isGuest", Boolean.valueOf(isGuest));
		topNavVC.contextPut("isInvitee", Boolean.valueOf(isInvitee));
		
		// login link
		if (ureq.getIdentity() == null || isGuest) {
			loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
			loginLink.setIconLeftCSS("o_icon o_icon_login o_icon-lg");
			loginLink.setTitle("topnav.login.alt");
		}
		
		if(ureq.getIdentity() != null && !isGuest && !isInvitee) {
			loadPersonalTools(ureq);
			
			// the user profile
			Component portrait = getPortraitCmp(ureq);
			topNavVC.put("portrait", portrait);
			
			// the label to open the personal menu
			User user = getIdentity().getUser();
			String[] attr = new String[] {
					StringHelper.escapeHtml(user.getProperty(UserConstants.FIRSTNAME, getLocale())),
					StringHelper.escapeHtml(user.getProperty(UserConstants.LASTNAME, getLocale())),
					getIdentity().getName()
			};
			String myMenuText = translate("topnav.my.menu.label", attr);
			topNavVC.contextPut("myMenuLabel", myMenuText);
		}
		
		topNavVC.contextPut("locked", Boolean.FALSE);
		putInitialPanel(topNavVC);
	}
	
	@Override
	public void lock() {
		topNavVC.contextPut("locked", Boolean.TRUE);
	}
	
	@Override
	public void unlock() {
		topNavVC.contextPut("locked", Boolean.FALSE);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == loginLink) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		}
	}
	
	private Component getPortraitCmp(UserRequest ureq) {
		Controller ctr = new DisplayPortraitController(ureq, getWindowControl(), getIdentity(), false, false, false, true);
		listenTo(ctr);
		return ctr.getInitialComponent();
	}
	
	private void loadPersonalTools(UserRequest ureq) {
		List<Tool> toolSetLinksName = new ArrayList<>();
		List<Tool> helpPluginLinksName = new ArrayList<>();
		
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		String selectedTools = userToolsModule.getUserTools(prefs);
		if(!StringHelper.containsNonWhitespace(selectedTools)) {
			selectedTools = userToolsModule.getDefaultPresetOfUserTools();
		}
		Set<String> selectedToolSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(selectedTools)) {
			String[] selectedToolArr = selectedTools.split(",");
			for(String selectedTool:selectedToolArr) {
				selectedToolSet.add(UserToolsModule.stripToolKey(selectedTool));
			}
		}
		
		List<UserToolExtension> toolExtensions = userToolsModule.getUserToolExtensions(ureq);
		for (UserToolExtension toolExtension : toolExtensions) {
			// check for sites
			if(toolExtension.isShortCutOnly() || selectedToolSet.contains(toolExtension.getUniqueExtensionID())) {
				UserTool tool = toolExtension.createUserTool(ureq, getWindowControl(), getLocale());
				if(tool != null) {
					if (toolExtension.getUserToolCategory().equals(UserToolCategory.help)) {
						for (HelpLinkSPI helpLinkSPI : helpModule.getUserToolHelpPlugins()) {
							UserTool helpTool = helpLinkSPI.getHelpUserTool(getWindowControl());
							if (helpTool != null) {
								Component cmp = helpTool.getMenuComponent(ureq, topNavVC);
								String CssId = toolExtension.getShortCutCssId() + "_" + helpLinkSPI.getPluginName();
								String cssClass = toolExtension.getShortCutCssClass();
								helpPluginLinksName.add(new Tool(CssId, cssClass, cmp.getComponentName()));
								disposableTools.add(helpTool);
							}
						}
					} else {
						Component cmp = tool.getMenuComponent(ureq, topNavVC);
						String cssId = toolExtension.getShortCutCssId();
						String cssClass = toolExtension.getShortCutCssClass();
						toolSetLinksName.add(new Tool(cssId, cssClass, cmp.getComponentName()));
						disposableTools.add(tool);
					}

				}
			}
		}
		topNavVC.contextPut("toolSet", toolSetLinksName);
		topNavVC.contextPut("helpPlugins", helpPluginLinksName);
	}

	@Override
	protected void doDispose() {
		for(Disposable disposableTool:disposableTools) {
			disposableTool.dispose();
		}
	}
	
	public static class Tool {
		
		private final String shortCutCssId;
		private final String shortCutCssClass;
		private final String shortCutComponentName;
		
		public Tool(String shortCutCssId, String shortCutCssClass, String shortCutComponentName) {
			this.shortCutCssId = shortCutCssId;
			this.shortCutCssClass = shortCutCssClass;
			this.shortCutComponentName = shortCutComponentName;
		}

		public String getShortCutCssId() {
			return shortCutCssId;
		}

		public String getShortCutCssClass() {
			return shortCutCssClass;
		}

		public String getShortCutComponentName() {
			return shortCutComponentName;
		}
	}
}