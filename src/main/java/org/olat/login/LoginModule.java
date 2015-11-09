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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.login.auth.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 * @author guido
 */
@Service("loginModule")
public class LoginModule extends AbstractSpringModule {
	
	private static final OLog log = Tracing.createLoggerFor(LoginModule.class);

	@Autowired
	private List<AuthenticationProvider> authenticationProviders;
	
	@Value("${login.attackPreventionEnabled:true}")
	private boolean attackPreventionEnabled;
	@Value("${login.AttackPreventionMaxattempts:5}")
	private int attackPreventionMaxAttempts;
	@Value("${login.AttackPreventionTimeoutmin:5}")
	private int attackPreventionTimeout;

	@Value("${invitation.login:enabled}")
	private String invitationEnabled;
	@Value("${guest.login:enabled}")
	private String guestLoginEnabled;
	@Value("${guest.login.links:enabled}")
	private String guestLoginLinksEnabled;
	
	private String defaultProviderName = "OLAT";
	@Value("${login.using.username.or.email.enabled:true}")
	private boolean allowLoginUsingEmail;

	private CoordinatorManager coordinatorManager;
	private CacheWrapper<String,Integer> failedLoginCache;

	@Autowired
	public LoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	public void init() {
		// configure timed cache default params: refresh 1 minute, timeout according to configuration
		failedLoginCache = coordinatorManager.getCoordinator().getCacher().getCache(LoginModule.class.getSimpleName(), "blockafterfailedattempts");
				
		updateProperties();
		
		boolean defaultProviderFound = false;
		for (Iterator<AuthenticationProvider> iterator = authenticationProviders.iterator(); iterator.hasNext();) {
			AuthenticationProvider provider = iterator.next();
			if (provider.isDefault()) {
				defaultProviderFound = true;
				defaultProviderName = provider.getName();
				log.info("Using default authentication provider '" + defaultProviderName + "'.");
			}
		}
		
		if (!defaultProviderFound) {
			throw new StartupException("Defined DefaultAuthProvider::" + defaultProviderName + " not existent or not enabled. Please fix.");
		}
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	@Override
	protected void initDefaultProperties() {
		super.initDefaultProperties();
		if (attackPreventionEnabled) {
			log.info("Attack prevention enabled. Max number of attempts: " + attackPreventionMaxAttempts + ", timeout: " + attackPreventionTimeout + " minutes.");
		} else {
			log.info("Attack prevention is disabled.");
		}
		
		//compatibility with older settings
		if("true".equals(guestLoginEnabled)) {
			guestLoginEnabled = "enabled";
		} else if("false".equals(guestLoginEnabled)) {
			guestLoginEnabled = "disabled";
		}
		
		if("true".equals(guestLoginLinksEnabled)) {
			guestLoginLinksEnabled = "enabled";
		} else if("false".equals(guestLoginLinksEnabled)) {
			guestLoginLinksEnabled = "disabled";
		}
		
		if("true".equals(invitationEnabled)) {
			invitationEnabled = "enabled";
		} else if("false".equals(guestLoginLinksEnabled)) {
			invitationEnabled = "disabled";
		}
		
		if (isGuestLoginEnabled()) {
			log.info("Guest login on login page enabled");
		} else {
			log.info("Guest login on login page disabled or not properly configured. ");
		}
		
		if (isInvitationEnabled()) {
			log.info("Invitation login enabled");
		} else {
			log.info("Invitation login disabled");
		}
	}
	
	private void updateProperties() {
		//set properties
		String invitation = getStringPropertyValue("invitation.login", true);
		if(StringHelper.containsNonWhitespace(invitation)) {
			invitationEnabled = invitation;
		}
		String guestLogin = getStringPropertyValue("guest.login", true);
		if(StringHelper.containsNonWhitespace(guestLogin)) {
			guestLoginEnabled = guestLogin;
		}
		String guestLoginLinks = getStringPropertyValue("guest.login.links", true);
		if(StringHelper.containsNonWhitespace(guestLoginLinks)) {
			guestLoginLinksEnabled = guestLoginLinks;
		}
		String usernameOrEmailLogin = getStringPropertyValue("login.using.username.or.email.enabled", true);
		if(StringHelper.containsNonWhitespace(usernameOrEmailLogin)) {
			allowLoginUsingEmail = "true".equals(usernameOrEmailLogin);
		}
	}

	/**
	 * @return The configured default login provider.
	 */
	public String getDefaultProviderName() {
		return defaultProviderName;
	}
	
	public Encoder.Algorithm getDefaultHashAlgorithm() {
		return Encoder.Algorithm.sha512;
	}
	
	/**
	 * @param provider
	 * @return AuthenticationProvider implementation.
	 */
	public AuthenticationProvider getAuthenticationProvider(String provider) {
		AuthenticationProvider authenticationProvider = null;
		for(AuthenticationProvider authProvider:authenticationProviders) {
			if(authProvider.getName().equalsIgnoreCase(provider)) {
				authenticationProvider = authProvider;
			}
		}
		return authenticationProvider;
	}
	
	/**
	 * This method will always return something and will try to find some
	 * matching provider. It will find LDAP'A0 -> LDAP or return the
	 * default provider.
	 * 
	 * @param provider
	 * @return
	 */
	public AuthenticationProvider getAuthenticationProviderHeuristic(String provider) {
		//first exact match
		AuthenticationProvider authenticationProvider = getAuthenticationProvider(provider);
		if(authenticationProvider == null && StringHelper.containsNonWhitespace(provider)) {
			String upperedCaseProvider = provider.toUpperCase();
			for(AuthenticationProvider authProvider:authenticationProviders) {
				if(upperedCaseProvider.contains(authProvider.getName().toUpperCase())) {
					authenticationProvider = authProvider;
					break;
				}
			}
		}
		if(authenticationProvider == null) {
			//return default
			for(AuthenticationProvider authProvider:authenticationProviders) {
				if(authProvider.isDefault()) {
					authenticationProvider = authProvider;
					break;
				}
			}
		}
		return authenticationProvider;
	}
	
	/**
	 * @return Collection of available AuthenticationProviders
	 */
	public Collection<AuthenticationProvider> getAuthenticationProviders() {
		return new ArrayList<>(authenticationProviders);
	}
	
	/**
	 * Must be called upon each login attempt. Returns true
	 * if number of login attempts has reached the set limit.
	 * @param login
	 * @return True if further logins will be prevented (i.e. max attempts reached).
	 */
	public final boolean registerFailedLoginAttempt(String login) {
		if (!attackPreventionEnabled) return false;
		Integer numAttempts = failedLoginCache.get(login);
		
		if (numAttempts == null) { // create new entry
			numAttempts = new Integer(1);
			failedLoginCache.put(login, numAttempts);
		} else { // update entry
			numAttempts = new Integer(numAttempts.intValue() + 1);
			failedLoginCache.update(login, numAttempts);
		}		
		return (numAttempts.intValue() > attackPreventionMaxAttempts);
	}
	
	/**
	 * Clear all failed login attempts for a given login.
	 * @param login
	 */
	public final void clearFailedLoginAttempts(String login) {
		if (attackPreventionEnabled) {
			failedLoginCache.remove(login);
		}
	}
	
	/**
	 * Tells whether a login is blocked to prevent brute force attacks or not.
	 * @param login
	 * @return True if login is blocked by attack prevention mechanism
	 */
	public final boolean isLoginBlocked(String login) {
		if (!attackPreventionEnabled) return false;
		Integer numAttempts = failedLoginCache.get(login);
		
		if (numAttempts == null) return false;
		else return (numAttempts.intValue() > attackPreventionMaxAttempts);
	}
	
	/**
	 * @return True if guest login must be shown on login screen, false
	 *         otherwise
	 */
	public boolean isGuestLoginEnabled() {
		return "enabled".equals(guestLoginEnabled);
	}
	
	public void setGuestLoginEnabled(boolean enabled) {
		guestLoginEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("guest.login", guestLoginEnabled, true);
	}
	
	public boolean isGuestLoginLinksEnabled() {
		return "enabled".equalsIgnoreCase(guestLoginLinksEnabled);
	}
	
	public void setGuestLoginLinksEnabled(boolean enabled) {
		guestLoginLinksEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("guest.login.links", guestLoginLinksEnabled, true);
	}
	
	public boolean isInvitationEnabled() {
		return "enabled".equals(invitationEnabled);
	}
	
	public void setInvitationEnabled(boolean enabled) {
		invitationEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("invitation.login", invitationEnabled, true);
	}
	
	/**
	 * @return Number of minutes a login gets blocked after too many attempts.
	 */
	public Integer getAttackPreventionTimeoutMin() {
		return new Integer(attackPreventionTimeout);
	}
	
	/**
	 * @return True if login with email is allowed (set in olat.properties)
	 */
	public boolean isAllowLoginUsingEmail() {
		return allowLoginUsingEmail;
	}
	
	public void setAllowLoginUsingEmail(boolean allow) {
		allowLoginUsingEmail = allow;
		setStringProperty("login.using.username.or.email.enabled", Boolean.toString(allow), true);
	}
}
