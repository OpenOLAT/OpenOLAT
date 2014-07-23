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
* <p>
*/ 

package org.olat.core.gui.control;

import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public interface ChiefController extends Controller {
	/**
	 * @return the window
	 */
	public Window getWindow();

	/**
	 * @return the windowcontrol
	 */
	public WindowControl getWindowControl();
	
	public ScreenMode getScreenMode();
	
	public boolean wishReload(boolean erase);
	
	/**
	 * Set a class to the &lt;body&gt;
	 * @param cssClass
	 */
	public void addBodyCssClass(String cssClass);

	/**
	 * Remove the class
	 * @param cssClass
	 */
	public void removeBodyCssClass(String cssClass);
	
	/**
	 * Add manually the custom CSS with an special timing to the view.
	 * Don't forget to set the custom CSS in your main controller too. The
	 * custom CSS is removed and readded at every tab/site changes.
	 * 
	 * @param customCSS
	 */
	public void addCurrentCustomCSSToView(CustomCSS customCSS);
	
	/**
	 * 
	 */
	public void removeCurrentCustomCSSFromView();

}