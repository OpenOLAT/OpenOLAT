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
package org.olat.core.gui.control;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.event.GenericEventListener;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Trans
 * 
 * <P>
 * Initial Date: 10.02.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface WindowBackOffice extends Disposable{

	Event IGNORE_BOOKMARK_ONE_TIME = new Event("ibot");

	WindowManager getWindowManager();
	
	/**
	 * @return the window associated
	 */
	Window getWindow();

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
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	Controller createDebugDispatcherController(UserRequest ureq, WindowControl windowControl);

	/**
	 * Factory method to create the inline translation tool dispatcher controller.
	 * This implicitly sets the translation controller on the window back office
	 * 
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	Controller createInlineTranslationDispatcherController(UserRequest ureq, WindowControl windowControl);
	
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
	 * Use this method for asynchronous updates to the gui. <br>
	 * invokes the runnable at a time when it is safe, that is the dispatching and rendering of the window is not disturbed.
	 * @param runnable the runnable
	 */
	public void invokeLater(Runnable runnable);

	/**
	 * @param wco the Command to be sent to the client (all requests are queued and sent in a batch at the end of the request)
	 */
	public void sendCommandTo(Command wco);
	
	/**
	 * together with getData and registerForCycleEvents, this serves to exchange request-transient render-related data amongst parties,
	 * e.g. layoutcontrollers telling the implementor of the windowcontrol/guimessage receiver where to visually put the guimessage. 
	 *<br>
	 * all the data is clear before the inline-rendering-about-to-start event, since after that point no state change must occur.
	 *
	 * @param key the key (to be agreed upon by the inter-communicating parties)
	 * @param value any object
	 */
	public void putData(String key, Object value);
	
	/**
	 * gets the data
	 * @see putData(String key, Object value)
	 * @param key the key
	 * @return
	 */
	public Object getData(String key);
	
	/**
	 * not used normally! normally you do not have to care about when happens what - you should only need to know what to do in your event-methods of your controllers. 
	 * when listeners want to be informed about cycles in the dispatching/validating/rendering
	 * @param gel the listener
	 */
	public void addCycleListener(GenericEventListener gel);
	
	public void removeCycleListener(GenericEventListener gel);
}