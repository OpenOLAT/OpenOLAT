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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
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
	
	/** Tells TinyMCE which menu bar to add this plugin button to */
	private static final String BUTTONS_LOCATION = RichTextConfiguration.THEME_ADVANCED_BUTTONS2_ADD;
	
	/**
	 * Creates a map for this plugin's parameters which can be read by the plugin's JavaScript code.
	 * NOTE: Theoretically, this should be happening inside the constructor of this class, but
	 * this is not possible at the moment due to createStaticURIFor() depending on beans which
	 * aren't loaded yet at constructor time. 
	 * 
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginParameters()
	 */
	@Override
	public Map<String, String> getPluginParameters() {
		// Create only if not already present.
		Map<String, String> params = super.getPluginParameters();
		if (params != null) return params;
		params = new HashMap<String, String>();
		// Get static URI for transparent GIF.
		params.put(PARAM_TRANSPARENT_IMAGE, StaticMediaDispatcher.createStaticURIFor("images/transparent.gif", false));
		setPluginParameters(params);
		return params;
	}


	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtons()
	 */
	@Override
	public String getPluginButtons() {
		return BUTTONS;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtonsLocation()
	 */
	@Override
	public String getPluginButtonsLocation() {
		return BUTTONS_LOCATION;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginName()
	 */
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}

	/**
	 * Decides in which configurations the math editor plugin is available (default: in all "full" profiles).
	 * 
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#isEnabledForProfile(int)
	 */
	@Override
	public boolean isEnabledForProfile(int profile) {
		return true;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtonsRowForProfile(int)
	 */
	@Override
	public int getPluginButtonsRowForProfile(int profile) {
		switch (profile) {
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC:
			return 1;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE:
			return 2;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE_WITH_MEDIABROWSER:
			return 2;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL:
			return 3;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL_WITH_MEDIABROWSER:
			return 3;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL:
			return 3;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL_WITH_MEDIABROWSER:
			return 3;
		default:
			// not enabled by default
			return 1;
		}
	}


}
