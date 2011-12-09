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
package org.olat.core.gui.control.generic.breadcrumb;

import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;

public interface CrumbController extends Controller, ControllerEventListener {

	/**
	 * @return the translated text that should appear in the bread crumb path
	 */
	public abstract String getCrumbLinkText();

	/**
	 * @return the translated hoover text that should appear in the bread crumb
	 *         path when hoovering over the link text
	 */
	public abstract String getCrumbLinkHooverText();

	/**
	 * Force the bread crumb path to reload each label on the path
	 */
	public void resetCrumbTexts();

	/**
	 * @return The current child crumb controller
	 */
	public abstract CrumbController getChildCrumbController();

	/**
	 * Set the bread crumb navigation. 
	 * @param breadCrumbCtr
	 */
	public abstract void setBreadCrumbController(BreadCrumbController breadCrumbCtr);

	/**
	 * Use this method to put another crumb on the bread crumb stack. This will
	 * register the child crumb within the bread crumb navigation and activate it.
	 * As as side effect, it will attach the current class as listener, so you
	 * don't have to do this yourself.
	 * 
	 * @param childCrumbController
	 */
	public void activateAndListenToChildCrumbController(CrumbController childCrumbController);
	
	/**
	 * De-activate a child crumb that has been activated using
	 * activateAndListenToChildCrumbController(). The method will also remove the
	 * crumb as listener from this class and dispose the crumb.
	 */
	public void deactivateAndDisposeChildCrumbController();
	
	/**
	 * This method can be used to de-activate the current crumb controller itself.
	 * It will remove the crumb from the bread crumb navigation and execute the
	 * necessary child crumb de-activation and disposal code
	 */
	public void removeFromBreadCrumbPathAndDispose();


}