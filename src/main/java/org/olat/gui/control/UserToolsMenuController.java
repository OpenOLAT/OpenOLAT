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

import ch.uzh.lms.listener.LogoutListener;
import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserToolsMenuController extends BasicController implements LockableController  {

	private static final String ACTION_LOGOUT = "logout";
	
	private final VelocityContainer menuVC;
	private List<Disposable> disposableTools = new ArrayList<>();
	
	@Autowired
	private UserToolsModule userToolsModule;

	@Autowired
	private LogoutListener[] logoutListeners;
	
	public UserToolsMenuController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		menuVC = createVelocityContainer("menu");
		menuVC.setDomReplacementWrapperRequired(false);
		if(ureq.getIdentity() != null) {
			UserSession usess = ureq.getUserSession();
			if(usess != null && usess.getRoles() != null) {
				boolean isGuest = usess.getRoles().isGuestOnly();
				boolean isInvitee = usess.getRoles().isInvitee();
				if(!isGuest && !isInvitee) {
					loadPersonalTools(ureq);
				}
			}
			menuVC.contextPut("authenticated", usess.isAuthenticated());
		}
		putInitialPanel(menuVC);
	}

	private void loadPersonalTools(UserRequest ureq) {
		List<String> linksName = new ArrayList<>();
		List<String> configLinksName = new ArrayList<>();
		List<String> searchLinksName = new ArrayList<>();
		List<String> systemLinksName = new ArrayList<>();
		
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
			UserTool tool = toolExtension.createUserTool(ureq, getWindowControl(), getLocale());
			if(tool != null) {
				UserToolCategory category = toolExtension.getUserToolCategory();
				boolean shortCutOnly = toolExtension.isShortCutOnly();
				if(!shortCutOnly && !selectedToolSet.contains(toolExtension.getUniqueExtensionID())) {
					Component link = tool.getMenuComponent(ureq, menuVC);
					String linkName = link.getComponentName();
					switch(category) {
						case search: searchLinksName.add(linkName); break;
						case personal: linksName.add(linkName); break;
						case config: configLinksName.add(linkName); break;
						case system: systemLinksName.add(linkName); break;
					}
					disposableTools.add(tool);
				}
			}
		}
		
		menuVC.contextPut("personalTools", linksName);
		menuVC.contextPut("configs", configLinksName);
		menuVC.contextPut("systems", systemLinksName);
		menuVC.contextPut("searchs", searchLinksName);
		menuVC.contextPut("locked", Boolean.FALSE);
		menuVC.contextPut("isAdmin", ureq.getUserSession().getRoles().isOLATAdmin());
	}
	
	@Override
	public void lock() {
		menuVC.contextPut("locked", Boolean.TRUE);
	}

	@Override
	public void unlock() {
		menuVC.contextPut("locked", Boolean.FALSE);	
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuVC) {
			String command = event.getCommand();
			if (command.equals(ACTION_LOGOUT)) {
				AuthHelper.doLogout(ureq);
				for (LogoutListener logoutListener : logoutListeners) {
					logoutListener.onLogout(ureq);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		for(Disposable disposableTool:disposableTools) {
			disposableTool.dispose();
		}
	}
}
