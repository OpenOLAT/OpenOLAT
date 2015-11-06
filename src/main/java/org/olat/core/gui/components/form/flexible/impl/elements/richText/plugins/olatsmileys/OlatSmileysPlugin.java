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

package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys;

import java.util.Locale;
import java.util.Map;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;

/**
 * Provides a plugin for the rich text editor to insert CSS-based (i.e. themeable) emoticons.
 * 
 * <P>
 * Initial Date:  Jun 12, 2009 <br>
 * @author twuersch
 */
public class OlatSmileysPlugin extends TinyMCECustomPlugin {
	/** Param for JS code **/
	private static final String PARAM_TRANSPARENT_IMAGE = "transparentImage";

	/** The TinyMCE plugin name */
	public static final String PLUGIN_NAME = "olatsmileys";
	
	/** The TinyMCE button name for this plugin */
	public static final String BUTTONS = "olatsmileys";
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
	
	/**
	 * Creates a map for this plugin's parameters which can be read by the plugin's JavaScript code.
	 * NOTE: Theoretically, this should be happening inside the constructor of this class, but
	 * this is not possible at the moment due to createStaticURIFor() depending on beans which
	 * aren't loaded yet at constructor time. 
	 * 
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginParameters()
	 */
	@Override
	public Map<String, String> getPluginParameters(Locale locale) {
		// Create only if not already present.
		Map<String, String> params = super.getPluginParameters(locale);
		if (!params.containsKey(PARAM_TRANSPARENT_IMAGE)) {
			// Get static URI for transparent GIF.
			params.put(PARAM_TRANSPARENT_IMAGE, StaticMediaDispatcher.createStaticURIFor("images/transparent.gif", false));
		}
		return params;
	}
}
