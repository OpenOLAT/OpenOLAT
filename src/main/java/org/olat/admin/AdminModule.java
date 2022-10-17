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

package org.olat.admin;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.admin.site.AdminSite;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.User;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<BR>
 * The administration module takes care of loading and unloading administration
 * specific configuration. 
 * <P>
 * Initial Date:  Apr 13, 2005
 *
 * @author gnaegi 
 */
@Service("adminModule")
public class AdminModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(AdminModule.class);

	private static final String CONFIG_LOGIN_BLOCKED = "loginBlocked";
	/** Category for system properties **/
	public static final String SYSTEM_PROPERTY_CATEGORY = "_o3_";
	public static final String PROPERTY_MAINTENANCE_MESSAGE    = "maintenanceMessageToken";
	public static final String PROPERTY_SESSION_ADMINISTRATION = "sessionAdministrationToken";
	
	@Value("${maxNumberOfSessions:0}")
	private int maxNumberOfSessions;
	
	@Autowired
	private PropertyManager propertyManager;

	/**
	 * [used by spring]
	 */
	@Autowired
	public AdminModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
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

	
	public boolean checkMaintenanceMessageToken(HttpServletRequest request) {
		return checkToken(request, PROPERTY_MAINTENANCE_MESSAGE);
	}

	public boolean checkSessionAdminToken(HttpServletRequest request) {
		return checkToken(request, PROPERTY_SESSION_ADMINISTRATION);
	}

	private boolean checkToken(HttpServletRequest request, String tokenPropertyName) {
		String submittedToken = request.getParameter("token");
		if (submittedToken == null) {
			log.info(Tracing.M_AUDIT, "Trying to set maintenance message without using a token. Remote address::" + request.getRemoteAddr());
			return false;
		}
		// get token and compare
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, tokenPropertyName);
		String token = (p == null ? "" : p.getStringValue());
		if (token.equals(submittedToken)) { // limit access to token
			return true;
		}
		log.info(Tracing.M_AUDIT, "Trying to set maintenance message using a wrong token. Remote address::" + request.getRemoteAddr());
		return false;
	}
	
	/**
	 * Does not allow any further login except administrator-logins.
	 * @param newLoginBlocked
	 */
	public void setLoginBlocked(boolean newLoginBlocked, boolean persist) {
		log.info(Tracing.M_AUDIT, "Session administration: Set login-blocked=" + newLoginBlocked);
		AuthHelper.setLoginBlocked(newLoginBlocked);
		setBooleanProperty(CONFIG_LOGIN_BLOCKED, newLoginBlocked, persist);
	}

	/**
	 * Check if login is blocked
	 * @return  true = login is blocked 
	 */
	public boolean isLoginBlocked() {
		return AuthHelper.isLoginBlocked();
	}

	/**
	 * Set the rejectDMZRequests flag - if true this will reject all requests to dmz to other nodes
	 * @param rejectDMZRequests
	 */
	public void setRejectDMZRequests(boolean rejectDMZRequests) {
		log.info(Tracing.M_AUDIT, "Session administration: Set rejectDMZRequests=" + rejectDMZRequests);
		AuthHelper.setRejectDMZRequests(rejectDMZRequests);
	}

	/**
	 * Check if requests to DMZ are rejected resulting in clients to go to another node
	 * @return  true = reject all requests to dmz (to other nodes)
	 */
	public boolean isRejectDMZRequests() {
		return AuthHelper.isRejectDMZRequests();
	}

	/**
	 * Set limit for session. The login-process check this number and allows only this number of sessions.
	 * 0 = unlimited number of sessions
	 * @param maxSession
	 */
	public void setMaxSessions(int maxSession) {
		log.info(Tracing.M_AUDIT, "Session administration: Set maxSession={}", maxSession);
		AuthHelper.setMaxSessions(maxSession);
	}

	/**
	 * Set global session timeout in msec.
	 * @param sessionTimeout
	 */
	public void setSessionTimeoutDepr(int sessionTimeout) {
		log.info(Tracing.M_AUDIT, "Session administration: Set session-timeout={}", sessionTimeout);
		//in seconds
		CoreSpringFactory.getImpl(UserSessionManager.class).setGlobalSessionTimeout(sessionTimeout);
	}

	/**
	 * @return  Current session-limit.
	 */
	public int getMaxSessions() {
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
		
		boolean loginBlocked = getBooleanPropertyValue(CONFIG_LOGIN_BLOCKED);
		if(loginBlocked) {
			AuthHelper.setLoginBlocked(loginBlocked);
		}
		
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("NewIdentityCreated",
				new UserAdminContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator(AdminSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(AdminSite.class));
		
	}

	@Override
	protected void initDefaultProperties() {
		AuthHelper.setMaxSessions(maxNumberOfSessions);
	}

	@Override
	protected void initFromChangedProperties() {
		//nothing to do
	}
}