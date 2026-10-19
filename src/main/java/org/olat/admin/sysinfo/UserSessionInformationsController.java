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
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.mcp.McpModule;
import org.olat.mcp.manager.McpIdentityCache;

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
		putChannelCounts();
		myContent.contextPut("usersessions", getUsersSessionAsString());
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	/**
	 * The sessions per channel. The web interface count is the count of the
	 * footer (cluster wide with a shared session cache); REST, WebDAV and the
	 * web interface sessions are the signed on sessions of this node; MCP is
	 * the number of entries of the MCP identity cache of this node, one per
	 * user and token in use.
	 */
	private void putChannelCounts() {
		int guiCount = 0;
		int restCount = 0;
		int webdavCount = 0;
		for(UserSession usess:sessionManager.getAuthenticatedUserSessions()) {
			SessionInfo sinfo = usess == null ? null : usess.getSessionInfo();
			if(sinfo == null || sinfo.isContentDelivery()) {
				continue;
			}
			if(sinfo.isREST()) {
				restCount++;
			} else if(sinfo.isWebDAV()) {
				webdavCount++;
			} else {
				guiCount++;
			}
		}
		int mcpCount = CoreSpringFactory.getImpl(McpIdentityCache.class).size();
		myContent.contextPut("webUsersCount", Integer.toString(sessionManager.getUserSessionWebCounter()));
		myContent.contextPut("guiCount", Integer.toString(guiCount));
		myContent.contextPut("restCount", Integer.toString(restCount));
		myContent.contextPut("webdavCount", Integer.toString(webdavCount));
		myContent.contextPut("mcpCount", Integer.toString(mcpCount));
		int mcpTimeout = CoreSpringFactory.getImpl(McpModule.class).getSessionTimeout();
		myContent.contextPut("mcpHint", translate("sess.channels.hint", Integer.toString(mcpTimeout > 0 ? mcpTimeout : 300)));
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