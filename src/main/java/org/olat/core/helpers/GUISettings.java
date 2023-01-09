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
package org.olat.core.helpers;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.themes.Theme;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GUISettings extends AbstractSpringModule {

	private static final String KEY_GUI_THEME_IDENTIFYER = "layout.theme";
	
	private Theme guiTheme;
	
	/**
	 * Set the system theme here. Make sure the directory webapp/WEB-INF/static/themes/YOURTHEME exists. 
	 * This is only the default value in case no user configuration is found. Use the administration GUI to
	 * Set a specific theme.
	 */
	@Value("${layout.theme:light}")
	private String guiThemeIdentifyer;
	
	@Autowired
	public GUISettings(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, "org.olat.core.helpers.Settings");
	}

	@Override
	public void init() {
		//module enabled/disabled
		String guiThemeIdentifyerObj = getStringPropertyValue(KEY_GUI_THEME_IDENTIFYER, true);
		if(StringHelper.containsNonWhitespace(guiThemeIdentifyerObj)) {
			guiThemeIdentifyer = guiThemeIdentifyerObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	/**
	 * @return the CSS theme used for this webapp
	 */
	public String getGuiThemeIdentifyer() {
		return guiThemeIdentifyer;
	}
	
	public synchronized Theme getGuiTheme() {
		if(guiTheme == null) {
			guiTheme = new Theme(getGuiThemeIdentifyer());
		}
		return guiTheme;
	}

	/**
	 * Set the CSS theme used for this webapp. Only used by spring. Use static
	 * method to change the theme at runtime!
	 * 
	 * @param guiTheme
	 */
	public void setGuiThemeIdentifyer(String guiThemeIdentifyer) {
		this.guiThemeIdentifyer = guiThemeIdentifyer;
		setStringProperty(KEY_GUI_THEME_IDENTIFYER, guiThemeIdentifyer, true);
		getGuiTheme().init(guiThemeIdentifyer);
	}
}
