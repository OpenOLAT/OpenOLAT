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

import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
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
	
	public boolean isLoginInterceptionInProgress();
	
	/**
	 * Specialized for launching a resource, if some interactions is needed before
	 * effectively launching it.
	 * 
	 * @return true if the dispatcher must not launch the resource
	 */
	public boolean delayLaunch(UserRequest ureq, BusinessControl bc);
	
	/**
	 * The method is called by the poller thread.
	 * 
	 * @param ureq
	 * @param erase
	 * @return
	 */
	public boolean wishAsyncReload(UserRequest ureq, boolean erase);
	
	/**
	 * The method is resolved on click
	 * @param ureq
	 * @param erase
	 * @return
	 */
	public boolean wishReload(UserRequest ureq, boolean erase);
	
	/**
	 * Make sure a reload will not be triggered.
	 */
	public void resetReload();
	
	/**
	 * Lock softly the chief after interaction with the user.
	 * 
	 * @param resource The resource to lock
	 */
	public void lockResource(OLATResourceable resource);
	
	/**
	 * Hard locking the chief controller after a copy/paste URL or
	 * such a thing. If the informations are null, the call is ignored.
	 * 
	 * @param lockInfos The lock informations
	 */
	public void hardLockResource(LockResourceInfos lockInfos);
	
	/**
	 * @return Some informations about the currently locked resource
	 */
	public LockResourceInfos getLockResourceInfos();
	
	/**
	 * The informations is set to null if a new resource is locked.
	 * 
	 * @return Some informations about the last resource which was unlocked.
	 */
	public LockResourceInfos getLastUnlockedResourceInfos();
	
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
	
	/**
	 * Check if the static site of a specific type is available
	 * @param type
	 * @return
	 */
	public boolean hasStaticSite(Class<? extends SiteInstance> type);
}