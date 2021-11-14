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

package org.olat.admin.sysinfo;

import java.util.Iterator;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionInformationsController extends BasicController {

	private final VelocityContainer myContent;
  private final UserSessionManager sessionManager;
  
	/**
	 * Controlls user session in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public UserSessionInformationsController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		
		myContent = createVelocityContainer("usersession_infos");
		myContent.contextPut("usersessions", getUsersSessionAsString());
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private String getUsersSessionAsString() {
		StringBuilder sb = new StringBuilder(50000);
		int ucCnt = sessionManager.getUserSessionsCnt();
		Set<UserSession> usesss = sessionManager.getAuthenticatedUserSessions();
		int contcnt = DefaultController.getControllerCount();
		sb.append("total usersessions (auth and non auth): "+ucCnt+"<br />auth usersessions: "+usesss.size()+"<br />Total Controllers (active, not disposed) of all users:"+contcnt+"<br /><br />");

		for (Iterator<UserSession> iter = usesss.iterator(); iter.hasNext();) {
			UserSession usess = iter.next();
			Identity iden = usess.getIdentity();
			sb.append("authusersession (").append(usess.hashCode()).append(") of ");
			if (iden != null) {
				sb.append(iden.getKey()).append(" ").append(iden.getKey());
			} else {
				sb.append(" - ");
			}
			sb.append("<br />");
			Windows ws = Windows.getWindows(usess);
			for (Iterator<Window> iterator = ws.getWindowIterator(); iterator.hasNext(); ) {
				Window window = iterator.next();
				sb.append("- window ").append(window.getDispatchID()).append(" ").append(window.getLatestDispatchComponentInfo()).append("<br />");
			}
			sb.append("<br />");
		}
		return sb.toString();
	}
}