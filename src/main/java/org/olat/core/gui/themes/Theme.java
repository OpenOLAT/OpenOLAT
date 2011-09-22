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

import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;

/**
 * <h3>Description:</h3>
 * A class that represents a GUI theme
 * <p>
 * Initial Date: 31.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Theme {
	private String identifyer;
	private String baseURI;

	/**
	 * @param name The unique theme identifyer
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
	 * @return The base URI for this theme, e.g. 'http://www.myserver.com/olat/raw/61x/themes/default/'
	 */
	public String getBaseURI() {
		return baseURI;
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
