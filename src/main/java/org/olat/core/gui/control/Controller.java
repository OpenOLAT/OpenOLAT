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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.logging.activity.IUserActivityLogger;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public interface Controller extends ComponentEventListener, Disposable {

	/**
	 * Adds a controller as listener to this controller
	 * 
	 * @param el The controller that should be added as listener
	 */
	public void addControllerListener(ControllerEventListener el);
	
	public void removeControllerListener(ControllerEventListener el);
	
	public boolean isControllerListeningTo(ControllerEventListener el);

	/**
	 * Get the initial component from this controller.
	 * 
	 * @return Component The initial component
	 */
	public Component getInitialComponent();



	/**
	 * disposes the controller. to be called only by the same user that created
	 * the controller (or better: make sure event(...) and dispose run
	 * sequentially). the behavior must be stable even if multiple invocations
	 * occur
	 */
	public void dispose();
	
	/**
	 * @return true if the controller is already disposed
	 */
	public boolean isDisposed();
	
	/**
	 * @return UserActivityLogger of this controller or null if no logger is used
	 */
	public IUserActivityLogger getUserActivityLogger();
	
	/** 
	 * used for debugging and errorlog information only!!!
	 * 
	 * @return
	 */
	public WindowControl getWindowControlForDebug();
	
}