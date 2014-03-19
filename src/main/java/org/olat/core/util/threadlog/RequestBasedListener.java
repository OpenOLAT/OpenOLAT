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
package org.olat.core.util.threadlog;

import org.olat.core.gui.control.Event;
import org.olat.core.util.event.GenericEventListener;

/**
 * Listener passed on to the PersistentProperties for the
 * RequestBasedLogLevelManager.
 * <p>
 * The reason for having this as a separate class is simply
 * the fact that OlatResourceable names are capped in terms
 * of length
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Stefan
 */
public class RequestBasedListener implements GenericEventListener {

	/** the RequestBasedLogLevelManager to which this listener is associated **/ 
	private RequestBasedLogLevelManager manager;
	
	/**
	 * Sets the RequestBasedLogLevelManager to which this listener is associated.
	 * <p>
	 * Used by spring.
	 * @param manager the RequestBasedLogLevelManager to which this listener is associated.
	 */
	public void setManager(RequestBasedLogLevelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void event(Event event) {
		if (manager!=null) manager.init();
	}

}
