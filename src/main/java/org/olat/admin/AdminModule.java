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
* <p>
*/ 

package org.olat.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.olat.NewControllerFactory;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.commons.fullWebApp.util.GlobalStickyMessage;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.instantMessaging.rosterandchat.ChangePresenceJob;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:<BR>
 * The administration module takes care of loading and unloading administration
 * specific configuration. 
 * <P>
 * Initial Date:  Apr 13, 2005
 *
 * @author gnaegi 
 */
public class AdminModule extends AbstractOLATModule {

	/** Category for system properties **/
	public static String SYSTEM_PROPERTY_CATEGORY = "_o3_";
	public static final String PROPERTY_MAINTENANCE_MESSAGE    = "maintenanceMessageToken";
	public static final String PROPERTY_SESSION_ADMINISTRATION = "sessionAdministrationToken";
	private static final String CONFIG_ADMIN_MAX_SESSION = "maxNumberOfSessions";
	private PropertyManager propertyManager;

	/**
	 * [used by spring]
	 */
	public AdminModule(PropertyManager propertyManager) {
		super();
		this.propertyManager = propertyManager;
	}

/**
 * Check if system property for maintenance message exists, create one if it
 * doesn't
 * This generated token is used by the remote http maintenance message
 * setting mechanism, see method below
 * @param tokenPropertyName
 */
	private void initializeSystemTokenProperty(String tokenPropertyName) {
		Property p = propertyManager.findProperty(null, null, null, SYSTEM_PROPERTY_CATEGORY, tokenPropertyName);
		if (p == null) {
			String token = RandomStringUtils.randomAlphanumeric(8);
			p = propertyManager.createPropertyInstance(null, null, null, SYSTEM_PROPERTY_CATEGORY, tokenPropertyName, null, null, token, null);
			propertyManager.saveProperty(p);
		}
	}


	/**
	 * Sets the new maintenance message based on a http parameter. The request must use a valid
	 * token. The token can be looked up in the properties table.
	 * The maintenance message itself is managed by the OLATContext from the brasato core
	 * @param message
	 */
	public static void setMaintenanceMessage(String message) {
			GlobalStickyMessage.setGlobalStickyMessage(message, true);
	}
	
	public static boolean checkMaintenanceMessageToken(HttpServletRequest request, HttpServletResponse response) {
		return checkToken(request, PROPERTY_MAINTENANCE_MESSAGE);
	}

	public static boolean checkSessionAdminToken(HttpServletRequest request, HttpServletResponse response) {
		return checkToken(request, PROPERTY_SESSION_ADMINISTRATION);
	}

	private static boolean checkToken(HttpServletRequest request, String tokenPropertyName) {
		String submittedToken = request.getParameter("token");
		if (submittedToken == null) {
			Tracing.logAudit("Trying to set maintenance message without using a token. Remote address::" + request.getRemoteAddr(), AdminModule.class);
			return false;
		}
		// get token and compare
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, tokenPropertyName);
		String token = (p == null ? "" : p.getStringValue());
		if (token.matches(submittedToken)) { // limit access to token
			return true;
		} else {
			Tracing.logAudit("Trying to set maintenance message using a wrong token. Remote address::" + request.getRemoteAddr(), AdminModule.class);
			return false;
		}
	}
	
	/**
	 * Does not allow any further login except administrator-logins.
	 * @param newLoginBlocked
	 */
	public static void setLoginBlocked(boolean newLoginBlocked) {
		Tracing.logAudit("Session administration: Set login-blocked=" + newLoginBlocked, AdminModule.class);
		AuthHelper.setLoginBlocked(newLoginBlocked);
	}

	/**
	 * Check if login is blocked
	 * @return  true = login is blocked 
	 */
	public static boolean isLoginBlocked() {
		return AuthHelper.isLoginBlocked();
	}

	/**
	 * Set the rejectDMZRequests flag - if true this will reject all requests to dmz to other nodes
	 * @param rejectDMZRequests
	 */
	public static void setRejectDMZRequests(boolean rejectDMZRequests) {
		Tracing.logAudit("Session administration: Set rejectDMZRequests=" + rejectDMZRequests, AdminModule.class);
		AuthHelper.setRejectDMZRequests(rejectDMZRequests);
	}

	/**
	 * Check if requests to DMZ are rejected resulting in clients to go to another node
	 * @return  true = reject all requests to dmz (to other nodes)
	 */
	public static boolean isRejectDMZRequests() {
		return AuthHelper.isRejectDMZRequests();
	}

	/**
	 * Set limit for session. The login-process check this number and allows only this number of sessions.
	 * 0 = unlimited number of sessions
	 * @param maxSession
	 */
	public static void setMaxSessions(int maxSession) {
		Tracing.logAudit("Session administration: Set maxSession=" + maxSession, AdminModule.class);
		AuthHelper.setMaxSessions(maxSession);
	}
	
	/**
	 * Invalidated all session except administrator-sessions. 
	 * @return  Number of invalidated sessions
	 */
	public static int invalidateAllSessions() {
		Tracing.logAudit("Session administration: Invalidate all sessions.", AdminModule.class);
		return UserSession.invalidateAllSessions();
	}

	/**
	 * Invalidate a number of oldest (last-click time) sessions.
	 * @param nbrSessions
	 * @return  Number of invalidated sessions
	 */
	public static int invalidateOldestSessions(int nbrSessions) {
		Tracing.logAudit("Session administration: Invalidate oldest sessions Nbr-Sessions=" + nbrSessions, AdminModule.class);
		return UserSession.invalidateOldestSessions(nbrSessions);
	}

	/**
	 * Set global session timeout in msec.
	 * @param sessionTimeout
	 */
	public static void setSessionTimeout(int sessionTimeout) {
		Tracing.logAudit("Session administration: Set session-timeout=" + sessionTimeout, AdminModule.class);
		//in seconds
		UserSession.setGlobalSessionTimeout(sessionTimeout);
		//in milliseconds for presence job
		ChangePresenceJob.setAutoLogOutCutTimeValue(sessionTimeout*1000);
	}

	/**
	 * @return  Current session timeout in msec.
	 */
	public static int getSessionTimeout() {
		//changepresencejob holds the session timeout so far in milliseconds
		int sessionTimeoutSeconds = Math.round((ChangePresenceJob.getAutoLogOutCutTimeValue() / 1000));
		return sessionTimeoutSeconds;
	}

	/**
	 * @return  Current session-limit.
	 */
	public static int getMaxSessions() {
		return AuthHelper.getMaxSessions();
	}

	/**
	 * Enable hibernate-statistics (for JMX interface).
	 */
	public void enableHibernateStatistics(boolean enableStatistics) {
		if (enableStatistics) {
			// clear statistics when enable it
			DBFactory.getInstance().getStatistics().clear();
		}
		DBFactory.getInstance().getStatistics().setStatisticsEnabled(enableStatistics);
	}

	@Override
	public void init() {
		initializeSystemTokenProperty(PROPERTY_MAINTENANCE_MESSAGE);
		initializeSystemTokenProperty(PROPERTY_SESSION_ADMINISTRATION);
		
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("NewIdentityCreated",
				new UserAdminContextEntryControllerCreator());
	}

	@Override
	protected void initDefaultProperties() {
			int maxNumberOfSessions = getIntConfigParameter(CONFIG_ADMIN_MAX_SESSION, 0);
			AuthHelper.setMaxSessions(maxNumberOfSessions);
	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
		
	}
	
}
