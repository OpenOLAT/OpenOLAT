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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.gui.control.generic.popup;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for PopupBrowserWindowControllerCreator
 * 
 * <P>
 * Initial Date:  26.07.2007 <br>
 * @author patrickb
 */
public interface PopupBrowserWindowControllerCreator {
	/**
	 * create popup window controller 
	 * @param lureq
	 * @param lwControl
	 * @return
	 */
	public PopupBrowserWindowController createNewPopupBrowserController(UserRequest lureq, WindowControl lwControl, ControllerCreator contentControllerCreator);
	
	//fxdiff
	public PopupBrowserWindowController createNewUnauthenticatedPopupWindowController(UserRequest lureq, WindowControl lwControl, ControllerCreator contentControllerCreator);
}
