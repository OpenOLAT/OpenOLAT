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

package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:<br>
 * The plugins factory can be configured via spring to add your custom TinyMCE
 * plugins to the HTML editor and the rich text formatted form element. It
 * offers methods to get all plugins that are enabled for a specific usage
 * profile. 
 * 
 * <P>
 * Initial Date: 11.06.2009 <br>
 * 
 * @author gnaegi
 */
public class TinyMCECustomPluginFactory {
	// default is an empty set
	private List<TinyMCECustomPlugin> customPlugins = new ArrayList<>();
	
	/**
	 * Sping setter method
	 * @param customPluginsFromConfiguration
	 */
	public void setCustomPlugins(List<TinyMCECustomPlugin> customPluginsFromConfiguration) {
		customPlugins = customPluginsFromConfiguration;
	}

	/**
	 * Method to programatically add a plugin. Alterativ to the spring config way
	 * @param customPlugin
	 */
	public void addCustomPlugin(TinyMCECustomPlugin customPlugin) {
		customPlugins.add(customPlugin);
	}
	
	/**
	 * Get all custom TinyMCE plugins
	 * @return List containing the plugins or an empty set.
	 */
	public List<TinyMCECustomPlugin> getCustomPlugins() {
		return customPlugins;
	}

	/**
	 * Get all custom TinyMCE plugins that are enabled for the given profile
	 * 
	 * @param profile
	 *            Profile defined in the RichtTextConfiguration.CONFIG_PROFILE_*
	 *            variables
	 * @return List containing the plugins or an empty set.
	 */
	public List<TinyMCECustomPlugin> getCustomPlugionsForProfile() {
		List<TinyMCECustomPlugin> profilePlugins = new ArrayList<>();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : customPlugins) {
			profilePlugins.add(tinyMCECustomPlugin);
		}
		return profilePlugins;
	}
}
