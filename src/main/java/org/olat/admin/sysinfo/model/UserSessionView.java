package org.olat.admin.sysinfo.model;

import java.util.Date;

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
	private String firstname, lastname;
	private String login, authProvider;
	private String fromFQN;
	private Date lastClickTime;
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
			login = sessInfo.getLogin();
			authProvider = sessInfo.getAuthProvider();
			fromFQN = sessInfo.getFromFQN();
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
	
	public String getLogin() {
		return login;
	}
	
	public String getAuthProvider() {
		return authProvider;
	}
	
	public String getFromFQN() {
		return fromFQN;
	}
	
	public Date getLastClickTime() {
		return lastClickTime;
	} 
	
	public long getSessionDuration() {
		return sessionDuration;
	}
	
	public String getMode() {
		return mode;
	}
}
