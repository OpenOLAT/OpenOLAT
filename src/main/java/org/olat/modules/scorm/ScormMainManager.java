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
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.manager.BasicManager;


/**
 * Description:<br>
 * TODO:
 * 
 * <P>
 * Initial Date:  08.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ScormMainManager extends BasicManager {
	private static ScormMainManager INSTANCE = new ScormMainManager();
	
	private ScormMainManager() {
		// singleton
	}
	
	public static ScormMainManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param showMenu if true, the ims cp menu is shown
	 * @param apiCallback the callback to where lmssetvalue data is mirrored, or null if no callback is desired
	 * @param cpRoot
	 * @param resourceId
	 * @param lesson_mode add null for the default value or "normal", "browse" or
	 *          "review"
	 * @param credit_mode add null for the default value or "credit", "no-credit"
	 */
	//fxdiff FXOLAT-116: SCORM improvements
	public ScormAPIandDisplayController createScormAPIandDisplayController(UserRequest ureq, WindowControl wControl, boolean showMenu, ScormAPICallback apiCallback, 
			File cpRoot, String resourceId, String courseId, String lesson_mode, String credit_mode, boolean previewMode, boolean assessable, boolean activate, boolean fullWindow) {
		return new ScormAPIandDisplayController(ureq, wControl, showMenu, apiCallback, cpRoot, resourceId, courseId, lesson_mode, credit_mode, previewMode, assessable, activate, fullWindow);
	}
	
}
