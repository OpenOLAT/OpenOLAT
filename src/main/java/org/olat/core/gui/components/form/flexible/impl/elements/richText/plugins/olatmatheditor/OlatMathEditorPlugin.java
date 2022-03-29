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

package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor;

import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;

/**
 * Provides a plugin for the rich text editor to edit math formulae in LaTeX syntax.
 * 
 * <P>
 * Initial Date:  Jun 12, 2009 <br>
 * @author twuersch
 */
public class OlatMathEditorPlugin extends TinyMCECustomPlugin {
	
	/**
	 * Params handed over to the js code
	 */
	private static final String PARAM_TRANSPARENT_IMAGE = "transparentImage";

	/** The TinyMCE plugin name */
	public static final String PLUGIN_NAME = "olatmatheditor";
	
	/** The TinyMCE button name for this plugin */
	public static final String BUTTONS = "olatmatheditor";

	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
	
	@Override
	public Map<String, String> getPluginParameters(Locale locale) {
		// Create only if not already present.
		Map<String, String> params = super.getPluginParameters(locale);
		if (!params.containsKey(PARAM_TRANSPARENT_IMAGE)) {
			// Get static URI for transparent GIF.
			params.put(PARAM_TRANSPARENT_IMAGE, StaticMediaDispatcher.createStaticURIFor("images/transparent.gif", false));
		}
		
		
		String helpKey = "helpUrl" + locale.getLanguage();
		if(!params.containsKey(helpKey)) {
			String url = CoreSpringFactory.getImpl(HelpModule.class).getManualProvider().getURL(locale, "manual_user/personal/Math_formula");
			params.put(helpKey, url);
		}
		return params;
	}
}
