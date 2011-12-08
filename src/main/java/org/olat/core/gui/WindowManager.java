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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui;

import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ContentableChiefController;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.media.MediaResource;

/**
 * handles all windows of a user
 * <P>
 * Initial Date: 23.03.2006 <br>
 * 
 * @author Felix Jost
 */
public interface WindowManager extends Disposable {
	
	/**
	 * call this method after having e.g. logged on, and when you would like to switch to ajax mode.<br>
	 * ajax will be enable if all of the following criterion meet:
	 * a) it is globally enabled in olat.properties
	 * b) the browser of the user supports it
	 * c) the given argument "enabled" is true
	 * 
	 * @param ureq
	 * @param enabled if true, ajax should be enabled if possible
	 */
	public void setAjaxWanted(UserRequest ureq, boolean enabled);

	/**
	 * creates (or gets) a path for a given class
	 * @param baseClass
	 * @return
	 */
	public String getMapPathFor(Class baseClass);
	
	/**
	 * locates and returns the resource found under <package-of-baseclass>/relpath;
	 * e.g. for baseClass org.olat.demo.MyDemo and relPath /js/myfunc.js -> org/olat/demo/_static/js/myfunc.js
	 * 
	 * @param baseClass the class which packages denotes the base path for the lookup
	 * @param relPath e.g. /js/myfunc.js
	 * @return the MediaResource which delivers the resource
	 */
	public MediaResource createMediaResourceFor(final Class baseClass, String relPath);
	
	public GlobalSettings getGlobalSettings();
	
	/**
	 * 
	 * @param windowName
	 * @param owner
	 * @return
	 */
	public WindowBackOffice createWindowBackOffice(String windowName, ChiefController owner);
	
	public void setAjaxEnabled(boolean enabled);
	
	public void setForScreenReader(boolean forScreenReader);
	
	public void setHighLightingEnabled(boolean enabled);

	/**
	 * @param fontSize relative to default font size
	 */
	public void setFontSize(int fontSize);
	
	/**
	 * @param ureq
	 * @return
	 */
	public ContentableChiefController createContentableChiefController(UserRequest ureq);

	/**
	 * @return
	 */
	public boolean isForScreenReader();
	
	/**
	 * whether or not ajax mode ("web 2.0") is enabled. should only called by controllers to determine whether they can offer additional ui capabilites due to ajax turned on.
	 * e.g. provide an autocompletion
	 * @return
	 */
	public boolean isAjaxEnabled();

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
	
	//fxdiff
	public PopupBrowserWindow createNewUnauthenticatedPopupWindowFor(UserRequest ureq, ControllerCreator controllerCreator);


	/**
	 * 
	 * @return the chiefcontroller which set itself to be the one (and only one) to receive jump in URLs.
	 */
	//public ChiefController getMainChiefController();

	//public GlobalSettings getGlobalSettings();
	
}
