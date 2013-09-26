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

package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;
import org.olat.core.util.WebappHelper;

/**
 * Description:<br>
 * The OLAT movie viewer plugin provides a flash based movie player that can
 * playback streamed movies.
 * <p>
 * In addition to the normal movie playback feature the plugin implements some
 * pseudo security. For a hardcoded list of streaming servers the movie player
 * adds a token to the URL. Thus, when looking at the HTML sourcecode the movie
 * URL is not the real movie URL. This prevents user from copy/paste the movie
 * URL and making them available to not authorized persons. However, whe using a
 * proxy or sniffer software the real movie URL can easily be revealed, but it
 * requires some extra effort. For unknown streaming servers the movie player
 * uses just the normal movie URL.
 * 
 * <P>
 * Initial Date: 28.05.2009 <br>
 * 
 * @author gnaegi
 */
public class OlatMovieViewerPlugin extends TinyMCECustomPlugin {
	public static final String PLUGIN_NAME = "olatmovieviewer";
	public static final String BUTTONS = "olatmovieviewer";
	private static final String BUTTONS_LOCATION = "theme_advanced_buttons2_add";

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginName()
	 */
	public String getPluginName() {
		return PLUGIN_NAME;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#isEnabledForProfile(int)
	 */
	public boolean isEnabledForProfile(int profile) {
		// The movie player plugin is always available, even when no
		// mediabrowser is available since it does not need the mediabrowser
		// (users have to enter remote URL's)
		switch (profile) {
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC:
			return false;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE:
			return false;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE_WITH_MEDIABROWSER:
			return true;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL:
			return false;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL_WITH_MEDIABROWSER:
			return true;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL:
			return true;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL_WITH_MEDIABROWSER:
			return true;
		default:
			// not enabled by default
			return false;
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtonsRowForProfile(int)
	 */
	@Override
	public int getPluginButtonsRowForProfile(int profile) {
		switch (profile) {
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC:
			return -1;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE:
			return -1;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_SIMPLE_WITH_MEDIABROWSER:
			return 2;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL:
			return -1;
		case RichTextConfiguration.CONFIG_PROFILE_FORM_EDITOR_FULL_WITH_MEDIABROWSER:
			return 2;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL:
			return 2;
		case RichTextConfiguration.CONFIG_PROFILE_FILE_EDITOR_FULL_WITH_MEDIABROWSER:
			return 2;
		default:
			// not enabled by default
			return -1;
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtons()
	 */
	public String getPluginButtons() {
		return BUTTONS;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin#getPluginButtonsLocation()
	 */
	public String getPluginButtonsLocation() {
		return BUTTONS_LOCATION;
	}

	@Override
	public Map<String, String> getPluginParameters() {

		// Create only if not already present.
		Map<String, String> params = super.getPluginParameters();
		if (params == null) {
			params = new HashMap<String, String>();
		}

		// Get static URI for transparent GIF.
		params.put("transparentImage", StaticMediaDispatcher
				.createStaticURIFor("images/transparent.gif", false));
		
		params.put("playerScript", StaticMediaDispatcher
				.createStaticURIFor("movie/player.js", false));

		setPluginParameters(params);
		return params;
	}
	
	public void setPluginParameters(Map<String,String> pluginParameters) {
		//use only one source for the servlet context path
		if(pluginParameters.containsKey("movieViewerUrl") && !pluginParameters.get("movieViewerUrl").startsWith(WebappHelper.getServletContextPath())) {
			pluginParameters.put("movieViewerUrl", WebappHelper.getServletContextPath() + pluginParameters.get("movieViewerUrl"));
		}
		super.setPluginParameters(pluginParameters);
	}

}
