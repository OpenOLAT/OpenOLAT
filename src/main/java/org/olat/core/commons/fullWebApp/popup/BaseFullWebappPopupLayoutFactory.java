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
package org.olat.core.commons.fullWebApp.popup;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * This factory offers methods to create popup layout creator controllers
 * <P>
 * Initial Date: 26.07.2007 <br>
 * 
 * @author patrickb
 */
public class BaseFullWebappPopupLayoutFactory {

	/**
	 * creates a controller creator which can be used for a popup browser
	 * window, allows only authenticated user session. <br>
	 * The popup browser windows layout contains:
	 * <ul>
	 * <li>minimal header (printview and close buttons)</li>
	 * <li>content controller provided by parameter controllerCreator
	 * <li>minmal footer</li<
	 * </ul>
	 * 
	 * @param ureq
	 * @param controllerCreator
	 * @return
	 */
	public static BaseFullWebappPopupLayout createAuthMinimalPopupLayout(
			UserRequest ureq, ControllerCreator controllerCreator) {
		if (!ureq.getUserSession().isAuthenticated())
			throw new AssertException("not authenticated!");
		return new BaseFullWebappMinimalLayoutControllerCreator(controllerCreator);
	}

	public static BaseFullWebappPopupLayout createMinimalPopupLayout(ControllerCreator controllerCreator) {
		return new BaseFullWebappMinimalLayoutControllerCreator(controllerCreator);
	}
	
	public static BaseFullWebappPopupLayout createPrintPopupLayout(ControllerCreator controllerCreator) {
		return new BaseFullWebappPrintLayoutControllerCreator(controllerCreator);
	}
}