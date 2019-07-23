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
package org.olat.core.commons.fullWebApp;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * Initial Date:  29.01.2008 <br>
 * @author patrickb
 */
public interface BaseFullWebappControllerParts {

	/**
	 * @return list of (static) Sites to be displayed
	 */
	public List<SiteInstance> getSiteInstances(UserRequest ureq, WindowControl wControl);
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public Controller getContentController(UserRequest ureq, WindowControl wControl);
	
	/**
	 * header controller
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public Controller createHeaderController(UserRequest ureq, WindowControl wControl);
	
	/**
	 * top nav controller (logout, search, print, clipboard, etc)
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public LockableController createTopNavController(UserRequest ureq, WindowControl wControl);
	
	/**
	 * footer controller (user count, logged in user, logo, verson info, etc)
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public LockableController createFooterController(UserRequest ureq, WindowControl wControl);
	
}
