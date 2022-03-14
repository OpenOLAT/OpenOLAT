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

package org.olat.core.util.i18n.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * <h3>Description:</h3> Use this factory to create controllers and GUI elements
 * for translating your webapp
 * <p>
 * Initial Date: 24.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class I18nUIFactory {

	/**
	 * Creates a controller that offers a panel for translators. On the panel,
	 * translators can launch the translation tool in a new window and modify some
	 * settings.
	 * 
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	public static Controller createTranslationToolLauncherController(UserRequest ureq, WindowControl windowControl) {
		return new TranslationToolLauncherController(ureq, windowControl);
	}

	/**
	 * Creates a controller to configure the i18n system, add or remove languages
	 * 
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	public static I18nConfigController createI18nConfigurationController(UserRequest ureq, WindowControl windowControl) {
		return new I18nConfigController(ureq, windowControl);
	}
}
