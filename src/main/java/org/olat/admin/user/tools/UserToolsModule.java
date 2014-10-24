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
import java.util.Locale;
import java.util.Set;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
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
	
	public List<UserTool> getAllUserTools(UserRequest ureq) {
		List<UserTool> userTools = new ArrayList<>();
		
		Locale locale = ureq.getLocale();
		for (Extension anExt : extManager.getExtensions()) {
			ExtensionElement ae = anExt.getExtensionFor(HomeMainController.class.getName(), ureq);
			if (ae != null && ae instanceof GenericActionExtension) {
				if(anExt.isEnabled()){
					GenericActionExtension gAe = (GenericActionExtension) ae;
					userTools.add(new UserTool(gAe, locale));
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
