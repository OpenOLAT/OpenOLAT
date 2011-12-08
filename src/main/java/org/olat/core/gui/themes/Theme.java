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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.gui.themes;

import java.io.File;

import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
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
	private String identifyer;
	private String baseURI;

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
		return (getCustomJSFile().exists());
	}

	/**
	 * returns a new File-instance that points to the "theme.js" file in the
	 * current OLAT-Theme folder
	 * 
	 * @return
	 */
	private File getCustomJSFile() {
		String staticThemesPath = WebappHelper.getContextRoot() + "/static/themes/";
		File themeFolder = new File(staticThemesPath, Settings.getGuiThemeIdentifyer());
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
	 * returns the path to the custom js <br />
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
	 *         /olat/raw/fx-olat7/themes/frentix/theme.js
	 */
	public String getFullPathToCustomJS() {
		return baseURI + CUSTOMFILENAME;
	}

	/**
	 * Update values in this theme with the values from the given identifyer.
	 * 
	 * @param theme
	 */
	public void init(String themeIdentifyer) {
		this.identifyer = themeIdentifyer;
		// Themes are deliverd as static resources by StaticMediaDispatcher
		StringOutput themePath = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(themePath, "themes/" + themeIdentifyer + "/");
		this.baseURI = themePath.toString();
	}

}
