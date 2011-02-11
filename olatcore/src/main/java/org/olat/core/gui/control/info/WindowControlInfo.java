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

package org.olat.core.gui.control.info;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.state.ExtendedControllerState;


/**
 * Description:<br>
 * Initial Date:  09.09.2005 <br>
 * @author Felix Jost
 */
public interface WindowControlInfo {
	
	/**
	 * @deprecated, use getExtendedControllerState().getControllerClassName() instead
	 * @return
	 */
	public String getControllerClassName();
	
	
	
	public WindowControlInfo getParentWindowControlInfo();

	/**
	 * 
	 * @return the controllerstate of the underlying controller
	 */
	public ExtendedControllerState getExtendedControllerState();
	
	

	/**
	 * the implementation must somehow call back to the controller to let it adjust its state to the new state.
	 * @param ecstate the new state to adjust to
	 * @param back if true, the transition direction is back, that is from end to start, otherwise it is forward (from start to end)
	 * @param ureq the UserRequest: using as normal, but calling ureq.getParameter(...) doesn't make sense here, since those are the parameters of a call in the past.
	 */
	public void adjustControllerState(boolean back, ExtendedControllerState ecstate, UserRequest ureq);

	/**
	 * @return null or a list (non-empty) of currently non-disposed WindowControlInfos
	 */
	public List<WindowControlInfo> getChildren();

	/**
	 * to be called only by the constructor of a windowcontrolinfo.
	 * @param impl
	 */
	public void addChild(WindowControlInfo child);

	/**
	 * @return
	 */
	public boolean isControllerDisposed();
}
