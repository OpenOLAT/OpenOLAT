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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

public interface FormToggle extends FormItem {

	/**
	 * switch to on / off depending on previous state
	 */
	public void toggle();
	
	/**
	 * get state of the toggle
	 * @return true if toggled on
	 */
	public boolean isOn();
	
	/**
	 * set state to on and change the layout
	 */
	public void toggleOn();
	
	/**
	 * set state to off and change the layout
	 */
	public void toggleOff();
	
	/**
	 * set your custom css for the on-state of the toggle
	 * @param toggledOnCSS
	 */
	public void setToggledOnCSS(String toggledOnCSS);
	
	/**
	 * set your custom css for the off-state of the toggle
	 * @param toggledOffCSS
	 */
	public void setToggledOffCSS(String toggledOffCSS);
	
	/**
	 * set the i18n key
	 * @param i18n
	 */
	public void setI18nKey(String i18n, String... args);

}