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

package org.olat.core.gui;

import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;

/**
 * handles all windows of a user
 * <P>
 * Initial Date: 23.03.2006 <br>
 * 
 * @author Felix Jost
 */
public interface WindowManager {
	
	/**
	 * Call this method after having e.g. logged on, and when you would like to switch to ajax mode.<br>
	 * ajax will be enable if the browser of the user supports it.
	 * 
	 * @param ureq
	 */
	public void setAjaxWanted(UserRequest ureq);
	
	public GlobalSettings getGlobalSettings();
	
	/**
	 * 
	 * @param windowName
	 * @param owner
	 * @return
	 */
	public WindowBackOffice createWindowBackOffice(String windowName, String csrfToken, ChiefController owner, WindowSettings settings);

	/**
	 * whether or not ajax mode ("web 2.0") is enabled. should only called by controllers to determine whether they can offer additional ui capabilites due to ajax turned on.
	 * e.g. provide an autocompletion
	 * @return
	 */
	public boolean isAjaxEnabled();
	
	public void setAjaxEnabled(boolean enabled);

	/**
	 * a new browser window with content (controller) provided through the
	 * controller creator. If the different layouts in popup windows are needed,
	 * the application must provide wrapping layouting controller containing the
	 * content. Feed in the wrapping layouting controller. <br>
	 * please see the {@link org.olat.core.gui.components.link.Link} on how to
	 * define a link to act as a popup window link. Once such a link is clicked
	 * the listening controller uses the
	 * <code>popupBrowserWindow = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, this);</code>
	 * to create the popup window (<code>this</code> may be any
	 * ControllerCreator!). Just call the method
	 * {@link PopupBrowserWindow#open(UserRequest)} to activate/open/render the
	 * content in the new browserwindow. <br>
	 * 
	 * @param ureq
	 * @param controllerCreator
	 * @return
	 */
	public PopupBrowserWindow createNewPopupBrowserWindowFor(UserRequest ureq, ControllerCreator controllerCreator);
	
	public PopupBrowserWindow createNewUnauthenticatedPopupWindowFor(UserRequest ureq, ControllerCreator controllerCreator);	
}
