/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.sysinfo.model;

import java.util.Date;

import jakarta.servlet.http.HttpSession;

import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionView {
	
	private boolean authenticated;
	private Long identityKey;
	private String firstname;
	private String lastname;
	private String fromIP;
	private String authProvider;
	private Date lastClickTime;
	private Date lastAccessTime;
	private long sessionDuration;
	private String mode;
	
	private UserSession userSession;
	
	public UserSessionView(UserSession usess) {
		userSession = usess;
		
		SessionInfo sessInfo = usess.getSessionInfo();
		authenticated = usess.isAuthenticated();
		if (authenticated) {
			identityKey = sessInfo.getIdentityKey();
			firstname = sessInfo.getFirstname();
			lastname = sessInfo.getLastname();
			fromIP = sessInfo.getFromIP();
			authProvider = sessInfo.getAuthProvider();
			try {
				lastClickTime = new Date(sessInfo.getLastClickTime());
			} catch (Exception ise) {
				lastClickTime = null; // "Invalidated"; but need to return a date or
															// null
			}

			try {
				sessionDuration = sessInfo.getSessionDuration() / 1000;
			} catch (Exception ise) {
				sessionDuration = -1;
			}

			if (sessInfo.isWebDAV()) {
				mode = "WebDAV";
			} else if (sessInfo.isREST()) {
				mode = "REST";
			} else {
				mode = sessInfo.getWebMode();
			}
		}
		
		HttpSession se = sessInfo.getSession();
		if(se != null) {
			try {
				lastAccessTime = new Date(se.getLastAccessedTime());
			} catch (Exception e) {
				lastAccessTime = null;
			}
		}
		
	}
	
	public UserSession getUserSession() {
		return userSession;
	}
	
	public boolean isAuthenticated() {
		return authenticated;	
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getAuthProvider() {
		return authProvider;
	}
	
	public String getFromIP() {
		return fromIP;
	}
	
	public Date getLastClickTime() {
		return lastClickTime;
	}
	
	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public long getSessionDuration() {
		return sessionDuration;
	}
	
	public String getMode() {
		return mode;
	}
}
