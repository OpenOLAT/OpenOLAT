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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<br>
 * Implement this abstract class to provide a TinyMCE plugin to the rich text
 * editor. See the TinyMCE plugin documentation to learn how to build a plugin
 * for this editor http://wiki.moxiecode.com/index.php/TinyMCE:Create_plugin/3.x
 * <p>
 * Create a package _static.js in the package where your XYZCustomPlugin is
 * located. There you can place all your javascript plugin files. Have a look at
 * the custom plugins provided in the same directory as this abstract class.
 * Note that your plugin can be located anywhere, you can even deploy it in a
 * jar file.
 * <p>
 * To enable the plugin, make sure you add it to the TinyMCECustomPluginFactory in the
 * spring configuration.
 * 
 * <P>
 * Initial Date: 11.06.2009 <br>
 * 
 * @author gnaegi
 */
public abstract class TinyMCECustomPlugin {
	
	// Optional plugin parameters
	private final Map<String, String> pluginParameters = new ConcurrentHashMap<>();

	/**
	 * @return The name of the plugin, must be URL save. E.g. 'myplugin'
	 */
	abstract public String getPluginName();
	
	/**
	 * Get the optional plugin parameters if available. 
	 * @return the parameter map or NULL if not available
	 */
	public Map<String, String> getPluginParameters(@SuppressWarnings("unused") Locale locale) {
		return pluginParameters;
	}

	/**
	 * Spring setter method to inject the optional plugin parameters
	 * @param pluginParameters
	 */
	public void setPluginParameters(Map<String,String> pluginParameters) {
		this.pluginParameters.putAll(pluginParameters);
	}

}