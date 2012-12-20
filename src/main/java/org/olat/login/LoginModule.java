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

package org.olat.login;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.login.auth.AuthenticationProvider;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 * @author guido
 */
public class LoginModule extends AbstractOLATModule {

	private static final String CONF_ATTACK_ENABLED = "AttackPreventionEnabled";
	private static final String CONF_ATTACK_MAXATTEMPTS = "AttackPreventionMaxattempts";
	private static final String CONF_ATTACK_TIMEOUTMIN = "AttackPreventionTimeoutmin";
	private static final String CONF_GUESTLINKS_ENABLED = "GuestLoginLinksEnabled";
	private static final String CONF_INVITATION_ENABLED = "InvitationEnabled";
	private static final String ALLOW_LOGIN_USING_EMAIL = "allowLoginUsingEmail";

	private static Map<String, AuthenticationProvider> authenticationProviders;
	private static boolean attackPreventionEnabled;
	private static int attackPreventionMaxAttempts;
	private static int attackPreventionTimeout;
	private static boolean guestLoginLinksEnabled;
	private static CacheWrapper failedLoginCache;
	private static String defaultProviderName;
	private static boolean allowLoginUsingEmail;
	private CoordinatorManager coordinatorManager;
	private static boolean invitationEnabled ;
	
	/**
	 * [used by spring]
	 */
	private LoginModule() {
		//
	}
	
	@Override
	protected void initDefaultProperties() {
		attackPreventionEnabled = getBooleanConfigParameter(CONF_ATTACK_ENABLED, true);
		if (attackPreventionEnabled) {
			Tracing.logInfo("Attack prevention enabled. Max number of attempts: " + attackPreventionMaxAttempts + ", timeout: " + attackPreventionTimeout + " minutes.", LoginModule.class);
		} else {
			Tracing.logInfo("Attack prevention is disabled.", LoginModule.class);
		}
		attackPreventionMaxAttempts = getIntConfigParameter(CONF_ATTACK_MAXATTEMPTS, 5);
		attackPreventionTimeout = getIntConfigParameter(CONF_ATTACK_TIMEOUTMIN, 5);
		
		guestLoginLinksEnabled = getBooleanConfigParameter(CONF_GUESTLINKS_ENABLED, true);
		if (guestLoginLinksEnabled) {
			Tracing.logInfo("Guest login links on login page enabled", LoginModule.class);
		} else {
			guestLoginLinksEnabled = false;
			Tracing.logInfo("Guest login links on login page disabled or not properly configured. " , LoginModule.class);
		}
		invitationEnabled = getBooleanConfigParameter(CONF_INVITATION_ENABLED, true);
		if (invitationEnabled) {
			Tracing.logInfo("Invitation login enabled", LoginModule.class);
		} else {
			Tracing.logInfo("Invitation login disabled" , LoginModule.class);
		}
		
		
		allowLoginUsingEmail = getBooleanConfigParameter(ALLOW_LOGIN_USING_EMAIL, true);
		
		
		
	}
	
	/**
	 * [used by spring]
	 * @param coordinatorManager
	 */
	public void setCoordinator(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}
	
	/**
	 * [used by spring]
	 * @param authProviders
	 */
	public void setAuthenticaionProviders(Map<String, AuthenticationProvider> authProviders) {
		this.authenticationProviders = authProviders;
	}

	/**
	 * @return The configured default login provider.
	 */
	public static String getDefaultProviderName() {
		return defaultProviderName;
	}
	
	/**
	 * @param provider
	 * @return AuthenticationProvider implementation.
	 */
	public static AuthenticationProvider getAuthenticationProvider(String provider) {
		return authenticationProviders.get(provider);
	}
	
	/**
	 * @return Collection of available AuthenticationProviders
	 */
	public static Collection<AuthenticationProvider> getAuthenticationProviders() {
		return authenticationProviders.values();
	}
	
	/**
	 * Must be called upon each login attempt. Returns true
	 * if number of login attempts has reached the set limit.
	 * @param login
	 * @return True if further logins will be prevented (i.e. max attempts reached).
	 */
	public static final boolean registerFailedLoginAttempt(String login) {
		if (!attackPreventionEnabled) return false;
		Integer numAttempts = (Integer)failedLoginCache.get(login);
		
		if (numAttempts == null) { // create new entry
			numAttempts = new Integer(1);
		} else { // update entry
			numAttempts = new Integer(numAttempts.intValue() + 1);
		}
		// do not use putSilent(...) here, since failed login attempts should propagate to all cluster nodes 
		// o_clusterREVIEW todo: this is fine, however loading the data (numAttempts == null) ... should be via db e.g properties table, 
		// otherwise it cannot be clustersafe
		failedLoginCache.update(login, numAttempts);
		
		return (numAttempts.intValue() > attackPreventionMaxAttempts);
	}
	
	/**
	 * Clear all failed login attempts for a given login.
	 * @param login
	 */
	public static final void clearFailedLoginAttempts(String login) {
		if (!attackPreventionEnabled) return;
		//EHCacheManager.getInstance().removeFromCache(failedLoginCache, login);
		failedLoginCache.remove(login);
	}
	
	/**
	 * Tells wether a login is blocked to prevent brute force attacks or not.
	 * @param login
	 * @return True if login is blocked by attack prevention mechanism
	 */
	public static final boolean isLoginBlocked(String login) {
		if (!attackPreventionEnabled) return false;
		//Integer numAttempts = (Integer)EHCacheManager.getInstance().get(failedLoginCache, login);
		Integer numAttempts = (Integer)failedLoginCache.get(login);
		
		if (numAttempts == null) return false;
		else return (numAttempts.intValue() > attackPreventionMaxAttempts);
	}
	
	/**
	 * @return True if guest login kinks must be shown on login screen, false
	 *         otherwhise
	 */
	public static final boolean isGuestLoginLinksEnabled() {
		return guestLoginLinksEnabled;
	}
	
	public static final boolean isInvitationEnabled() {
		return invitationEnabled;
	}
	
	/**
	 * @return Number of minutes a login gets blocked after too many attempts.
	 */
	public static Integer getAttackPreventionTimeoutMin() {
		return new Integer(attackPreventionTimeout);
	}
	
	/**
	 * @return True if login with email is allowed (set in olat.properties)
	 */
	public static boolean allowLoginUsingEmail() {
		return allowLoginUsingEmail;
	}

	@Override
	public void init() {
		
		boolean defaultProviderFound = false;
		for (Iterator<AuthenticationProvider> iterator = authenticationProviders.values().iterator(); iterator.hasNext();) {
			AuthenticationProvider provider = iterator.next();
			if (provider.isDefault()) {
				defaultProviderFound = true;
				defaultProviderName = provider.getName();
				Tracing.logInfo("Using default authentication provider '" + defaultProviderName + "'.", LoginModule.class);
			}
		}
		
		if (!defaultProviderFound) {
			throw new StartupException("Defined DefaultAuthProvider::" + defaultProviderName + " not existent or not enabled. Please fix.");
		}
				
		// configure timed cache default params: refresh 1 minute, timeout according to configuration
		failedLoginCache = coordinatorManager.getCoordinator().getCacher().getCache(LoginModule.class.getSimpleName(), "blockafterfailedattempts");
		
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
