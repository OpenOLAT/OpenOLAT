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

import org.olat.core.gui.components.Window;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.AssertException;

/**
 * Description: <BR>
 * Controller to list all buddy groups where the user is owner or participant.
 * This controller does also feature create and delete methods
 * <P>
 * 
 * Initial Date: Aug 5, 2004
 * @author Felix Jost
 */
public class DispatchResult {

	private MediaResource resultingMediaResource = null;
	private Window resultingWindow = null;
	
	
	/**
	 * @param resultingMediaResource The resultingMediaResource to set.
	 */
	public void setResultingMediaResource(MediaResource resultingMediaResource) {
		if (this.resultingMediaResource != null) 
			throw new AssertException("can only call this method once per click = can only dispatch one image or such at a time");
		this.resultingMediaResource = resultingMediaResource;
	}
	
	/**
	 * @param resultingWindow The resultingWindow to set.
	 */
	public void setResultingWindow(Window resultingWindow) {
		if (this.resultingWindow != null) 
			throw new AssertException("can only set the resultingNewWindow once in a user dispatch");
		this.resultingWindow = resultingWindow;
	}
	
	/**
	 * @return Returns the resultingMediaResource.
	 */
	public MediaResource getResultingMediaResource() {
		return resultingMediaResource;
	}
	
	/**
	 * @return Returns the resultingWindow.
	 */
	public Window getResultingWindow() {
		return resultingWindow;
	}
}
