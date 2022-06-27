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

import java.util.List;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.event.GenericEventListener;

/**
 * Initial Date: 10.02.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface WindowBackOffice extends Disposable{

	Event IGNORE_BOOKMARK_ONE_TIME = new Event("ibot");

	WindowManager getWindowManager();
	
	/**
	 * @return the window associated
	 */
	public Window getWindow();
	
	public ChiefController getChiefController();

	/**
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	Controller createDevelopmentController(UserRequest ureq, WindowControl windowControl);

	/**
	 * @return
	 */
	GlobalSettings getGlobalSettings();
	
	/**
	 * Some settings for the current window
	 * @return
	 */
	public WindowSettings getWindowSettings();
	
	public void setWindowSettings(WindowSettings settings);

	/**
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	Controller createDebugDispatcherController(UserRequest ureq, WindowControl windowControl);

	/**
	 * @param ureq
	 * @return
	 */
	Controller createAJAXController(UserRequest ureq);

	/**
	 * @return
	 */
	boolean isDebuging();
	
	/**
	 * @param initialComponent
	 * @return
	 */
	public GuiStack createGuiStack(Component initialComponent);

	/**
	 * @param wco the Command to be sent to the client (all requests are queued and sent in a batch at the end of the request)
	 */
	public void sendCommandTo(Command wco);
	
	/**
	 * gets the data
	 * @see putData(String key, Object value)
	 * @param key the key
	 * @return
	 */
	public List<ZIndexWrapper> getGuiMessages();
	
	/**
	 * not used normally! normally you do not have to care about when happens what - you should only need to know what to do in your event-methods of your controllers. 
	 * when listeners want to be informed about cycles in the dispatching/validating/rendering
	 * @param gel the listener
	 */
	public void addCycleListener(GenericEventListener gel);
	
	public void removeCycleListener(GenericEventListener gel);
}