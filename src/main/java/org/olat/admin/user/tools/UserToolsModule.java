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
package org.olat.admin.user.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.help.HelpUserToolExtension;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.prefs.Preferences;
import org.olat.home.HomeMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("userToolsModule")
public class UserToolsModule extends AbstractSpringModule {
	
	private static final String CONFIG_USER_TOOLS = "availableUserTools";
	private static final String CONFIG_PRESET_USERTOOLES = "presetUserTools";

	private String availableUserTools;
	private String defaultPresetOfUserTools;
	
	@Autowired
	private ExtManager extManager;
	
	@Autowired
	public UserToolsModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String aToolsObj = getStringPropertyValue(CONFIG_USER_TOOLS, true);
		if(StringHelper.containsNonWhitespace(aToolsObj)) {
			availableUserTools = aToolsObj;
		}
		
		String presetToolsObj = getStringPropertyValue(CONFIG_PRESET_USERTOOLES, true);
		if(StringHelper.containsNonWhitespace(presetToolsObj)) {
			defaultPresetOfUserTools = presetToolsObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public String getUserTools(Preferences prefs) {
		if(prefs == null) return null;
		
		String selectedToolV2s = (String)prefs.get(WindowManager.class, "user-tools-v2");
		if(!StringHelper.containsNonWhitespace(selectedToolV2s)) {
			String selectedTools = (String)prefs.get(WindowManager.class, "user-tools");
			if(StringHelper.containsNonWhitespace(selectedTools)) {
				//upgrade
				
				StringBuilder selectedToolSb = new StringBuilder(selectedTools);
				String[] newPresets = new String[]{
						"org.olat.home.HomeMainController:org.olat.gui.control.PrintUserToolExtension",
						"org.olat.home.HomeMainController:org.olat.gui.control.HelpUserToolExtension",
						"org.olat.home.HomeMainController:org.olat.instantMessaging.ui.ImpressumMainController"
				};
				
				for(String newPreset:newPresets) {
					if(selectedToolSb.indexOf(newPreset) < 0) {
						if(selectedToolSb.length() > 0) selectedToolSb.append(",");
						selectedToolSb.append(newPreset);
					}
				}
				prefs.putAndSave(WindowManager.class, "user-tools-v2", selectedToolSb.toString());
				selectedToolV2s = selectedToolSb.toString();
			}
		}
		return selectedToolV2s;
	}
	
	public void setUserTools(Preferences prefs, String settings) {
		prefs.putAndSave(WindowManager.class, "user-tools-v2", settings);
	}
	
	public List<UserToolExtension> getUserToolExtensions(UserRequest ureq) {
		List<UserToolExtension> tools = new ArrayList<>();
		if(!isUserToolsDisabled()) {
			List<UserToolExtension> extensions = getAllUserToolExtensions(ureq);
			Set<String> availableToolSet = getAvailableUserToolSet();
			UserSession usess = ureq.getUserSession();
			if(usess != null && usess.getRoles().isInviteeOnly()) {
				// Invitee are limited to subscription, help and password
				availableToolSet.retainAll(getInviteeToolSet());	
			}
			
			for(UserToolExtension extension:extensions) {
				if(extension.isEnabled() &&
						(availableToolSet.isEmpty() || availableToolSet.contains(extension.getUniqueExtensionID()))) {
					tools.add(extension);
				}
			}
		}
		return tools;
	}
	
	public Set<String> getInviteeToolSet() {
		return Set.of(HelpUserToolExtension.HELP_USER_TOOL_ID,
				"org.olat.home.HomeMainController:org.olat.home.controllerCreators.NotificationsControllerCreator",
				"org.olat.home.HomeMainController:org.olat.user.ChangePasswordController");
	}
	
	public List<UserToolExtension> getAllUserToolExtensions(UserRequest ureq) {
		List<UserToolExtension> userTools = new ArrayList<>();
		for (Extension anExt : extManager.getExtensions()) {
			if(anExt.isEnabled()) {
				ExtensionElement ae = anExt.getExtensionFor(HomeMainController.class.getName(), ureq);
				if (ae instanceof UserToolExtension) {
					userTools.add((UserToolExtension)ae);
				}
			}
		}
		
		return userTools;
	}
	
	public boolean isUserToolsDisabled() {
		return "none".equals(availableUserTools);
	}
	
	public String getAvailableUserTools() {
		return availableUserTools;
	}
	
	/**
	 * Empty set is the same as all tools are available.
	 * @return
	 */
	public Set<String> getAvailableUserToolSet() {
		Set<String> toolSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(availableUserTools)) {
			String[] tools = availableUserTools.split(",");
			for(String tool:tools) {
				toolSet.add(stripToolKey(tool));
			}
		}
		return toolSet;
	}
	
	public static String stripToolKey(String uniqueExtensionId) {
		String toolKey = uniqueExtensionId;
		if(toolKey.startsWith("org.olat.home.HomeMainController")) {
			int nextIndex = toolKey.indexOf(":", "org.olat.home.HomeMainController".length() + 2);
			if(nextIndex > 0) {
				toolKey = toolKey.substring(0, nextIndex);
			}
		}
		return toolKey;
	}

	public void setAvailableUserTools(String tools) {
		availableUserTools = tools;
		setStringProperty(CONFIG_USER_TOOLS, tools, true);
	}
	
	public String getDefaultPresetOfUserTools() {
		return defaultPresetOfUserTools;
	}
	
	public Set<String> getDefaultPresetOfUserToolSet() {
		Set<String> toolSet = new HashSet<>();
		if(StringHelper.containsNonWhitespace(defaultPresetOfUserTools)) {
			String[] tools = defaultPresetOfUserTools.split(",");
			for(String tool:tools) {
				toolSet.add(stripToolKey(tool));
			}
		}
		return toolSet;
	}
	
	public void setDefaultPresetOfUserTools(String preset) {
		defaultPresetOfUserTools = preset;
		setStringProperty(CONFIG_PRESET_USERTOOLES, preset, true);
	}
}
