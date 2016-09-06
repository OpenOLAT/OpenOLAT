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
package org.olat.core.gui.themes;

import java.io.File;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> A class that represents a GUI theme
 * <p>
 * Initial Date: 31.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Theme {
	public static final String DEFAULTTHEME = "light";
	
	private String identifyer;
	private String baseURI;
	private String relPathToThemesDir;
	private boolean hasCustomJSFile = false;

	private static String CUSTOMFILENAME = "theme.js";

	/**
	 * @param name
	 *            The unique theme identifyer
	 */
	public Theme(String themeIdentifyer) {
		init(themeIdentifyer);
	}

	/**
	 * @return The identifyer of the theme
	 */
	public String getIdentifyer() {
		return identifyer;
	}

	/**
	 * checks whether the OLAT-Theme-Folder of this theme contains a file
	 * "theme.js"
	 * 
	 * @return returns if the OLAT-Theme-Folder contains a file "theme.js"
	 */
	public boolean hasCustomJS() {
		return hasCustomJSFile;
	}

	/**
	 * returns a new File-instance that points to the "theme.js" file in the
	 * current OLAT-Theme folder
	 * 
	 * @return
	 */
	private File getCustomJSFile() {
		String staticThemesPath = WebappHelper.getContextRealPath("/static/themes/");
		if(staticThemesPath == null) {
			staticThemesPath = WebappHelper.getContextRoot() + "/static/themes/";
		}

		File themeFolder = new File(staticThemesPath, Settings.getGuiThemeIdentifyer());
		if (!themeFolder.exists() && Settings.getGuiCustomThemePath() != null) {
			// fallback to custom themes folder
			themeFolder = new File(Settings.getGuiCustomThemePath(), Settings.getGuiThemeIdentifyer());
		}
		return new File(themeFolder, CUSTOMFILENAME);
	}

	/**
	 * @return The base URI for this theme, e.g.
	 *         'http://www.myserver.com/olat/raw/61x/themes/default/'
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * returns the relative path to the custom js <br />
	 * ( check first with <code>hasCustomJS()</code> )<br />
	 * <p>
	 * Example usage:<br />
	 * <br />
	 * 
	 * <code>if (currTheme.hasCustomJS()) <br />
	 * 	  CustomJSComponent customJS = new CustomJSComponent("customThemejs", new String[] { currTheme.getFullPathToCustomJS() });</code>
	 * </p>
	 * 
	 * @return the path to the custom layout js ::
	 *         themes/frentix/theme.js
	 */
	public String getRelPathToCustomJS() {
		return relPathToThemesDir + CUSTOMFILENAME;
	}

	/**
	 * Update values in this theme with the values from the given identifyer.
	 * 
	 * @param theme
	 */
	public void init(String themeIdentifyer) {
		this.identifyer = themeIdentifyer;
		// Themes are deliverd as static resources by StaticMediaDispatcher
		this.relPathToThemesDir = "themes/" + themeIdentifyer + "/";
		StringOutput themePath = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(themePath, relPathToThemesDir);
		this.baseURI = themePath.toString();
		// Check if theme has a custom JS file to tweak UI on JS level
		hasCustomJSFile = getCustomJSFile().exists();
	}

}
