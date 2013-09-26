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

package org.olat.core.dispatcher.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;

/**
 * Description:<br>
 * Example Dispatcher which redirects requests to / (root) to the default path 
 * defined in the <code>_spring/defaultconfig.xml</code> or the respective <code>_spring/extconfig.xml</code>
 * for example in a default installation this will be:<br>
 * http://www.yourthingy.org/olat/ -> http://www.yourthingy.org/olat/dmz/
 * 
 * <P>
 * Initial Date:  08.07.2006 <br>
 * @author patrickb
 */
public class RedirectToDefaultDispatcher implements Dispatcher {
	
	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		DispatcherAction.redirectToDefaultDispatcher(response);
	}

}
