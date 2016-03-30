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

package org.olat.user;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.olat.NewControllerFactory;
import org.olat.admin.site.UserAdminSite;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.ldap.LDAPLoginManager;
import org.olat.login.AfterLoginConfig;
import org.olat.login.AfterLoginInterceptionManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.util.StringUtils;

/**
 * Desciption: The user module represents an implementation of
 * the OLAT user with its database object, business managers and page actions.
 * 
 * @author Florian Gnägi
 */
public class UserModule extends AbstractOLATModule {
	private List<String> loginBlacklist;
	private static List<String> loginBlacklistChecked = new ArrayList<String>();
	private static boolean hasTestUsers;
	private BaseSecurity securityManager;
	private SecurityGroup adminGroup, authorGroup, olatuserGroup, anonymousGroup, groupmanagerGroup, usermanagerGroup;
	private static boolean pwdchangeallowed;
	private static boolean pwdchangeallowedLDAP;
	private static String adminUserName;
	private static boolean enabledLogoByProfile;
	private List<DefaultUser> defaultUsers;
	private List<DefaultUser> testUsers;
	private static OLog log = Tracing.createLoggerFor(UserModule.class);
	private String authenticationProviderConstant;
	private UserManager userManger;
	private AfterLoginConfig afterLoginConfig; 
	private AfterLoginInterceptionManager afterLoginInterceptionManager;
	

	/**
	 * [used by spring]
	 * @param authenticationProviderConstant
	 */
	private UserModule(String authenticationProviderConstant, UserManager userManager, AfterLoginInterceptionManager afterLoginInterceptionManager) {
		this.authenticationProviderConstant = authenticationProviderConstant;
		this.userManger = userManager;
		this.afterLoginInterceptionManager = afterLoginInterceptionManager;
	}

	/**
	 * [used by spring]
	 * @param afterLoginConfig
	 */
	public void setAfterLoginConfig(AfterLoginConfig afterLoginConfig) {
		this.afterLoginConfig = afterLoginConfig;
	}

	/**
	 * [used by spring]
	 * @param loginBlacklist
	 */
	public void setLoginBlacklist(List<String> loginBlacklist) {
		this.loginBlacklist = loginBlacklist;
	}
	
	/**
	 * Check wether a login is on the blacklist.
	 * 
	 * @param login
	 * @return True if login is in blacklist
	 */
	public static boolean isLoginOnBlacklist(String login) {
		login = login.toLowerCase();
		for (String regexp: getLoginBlacklist()) {
			if (login.matches(regexp)) {
				log.audit("Blacklist entry match for login '" + login + "' with regexp '" + regexp + "'.");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see org.olat.core.configuration.OLATModule#init(com.anthonyeden.lib.config.Configuration)
	 */
	public void init() {
		pwdchangeallowed = getBooleanConfigParameter("passwordChangeAllowed", true);
		pwdchangeallowedLDAP = getBooleanConfigParameter("passwordChangeAllowedLDAP", false);
		
		int count = 0;
		for (String regexp : loginBlacklist) {
			try {
				Pattern.compile(regexp);
				loginBlacklistChecked.add(regexp);
			} catch (PatternSyntaxException pse) {
				log.error("Invalid pattern syntax in blacklist. Pattern: " + regexp+". Removing from this entry from list ");
			}
			count ++;
		}
		
		logInfo("Successfully added " + count + " entries to login blacklist.");
		

		// Autogeneration of test users
		hasTestUsers = getBooleanConfigParameter("generateTestUsers", true);
		enabledLogoByProfile = "enabled".equals(getStringConfigParameter("logoByProfileEnabled", "disabled", true));
		
		// Check if default users exists, if not create them
		securityManager = BaseSecurityManager.getInstance();
		adminGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
		authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		olatuserGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		anonymousGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		groupmanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
		usermanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);

		// read user editable fields configuration
		if (defaultUsers != null) {
			for (DefaultUser user:defaultUsers) {
				createUser(user);
			}
		}
		if (hasTestUsers) {
			// read user editable fields configuration
			if (testUsers != null) {
				for (DefaultUser user :testUsers) {
					createUser(user);
				}
			}
		}
		// Cleanup, otherwhise this subjects will have problems in normal OLAT
		// operation
		DBFactory.getInstance().commitAndCloseSession();
		
		adminUserName = getStringConfigParameter("adminUserName", "administrator", false);
		
		// Check if user manager is configured properly and has user property
		// handlers for the mandatory user properties used in OLAT
		checkMandatoryUserProperty(UserConstants.FIRSTNAME);
		checkMandatoryUserProperty(UserConstants.LASTNAME);
		checkMandatoryUserProperty(UserConstants.EMAIL);
		
		// Add controller factory extension point to launch user profile controller
		NewControllerFactory.getInstance().addContextEntryControllerCreator(Identity.class.getSimpleName(),
				new IdentityContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("HomeSite",
				new IdentityContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("HomePage",
				new HomePageContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator(UserAdminSite.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		
		
		// Append AfterLoginControllers if any configured
		if (afterLoginConfig != null) {
			afterLoginInterceptionManager.addAfterLoginControllerConfig(afterLoginConfig);
		}
	}

	@Override
	protected void initDefaultProperties() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	private void checkMandatoryUserProperty(String userPropertyIdentifyer) {
		List<UserPropertyHandler> propertyHandlers = userManger.getUserPropertiesConfig().getAllUserPropertyHandlers();
		boolean propertyDefined = false;
		for (UserPropertyHandler propertyHandler : propertyHandlers) {
			if (propertyHandler.getName().equals(userPropertyIdentifyer)) {
				propertyDefined = true;
				break;
			}
		}
		if ( ! propertyDefined) {
			throw new StartupException("The user property handler for the mandatory user property "
				+ userPropertyIdentifyer + " is not defined. Check your olat_userconfig.xml file!");
		}
	}

	/**
	 * Method to create a user with the given configuration
	 * 
	 * @return Identity or null
	 */
	protected Identity createUser(DefaultUser user) {
		Identity identity;
		identity = securityManager.findIdentityByName(user.getUserName());
		if (identity == null) {
			// Create new user and subject
			User newUser = new UserImpl(user.getFirstName(), user.getLastName(), user.getEmail());
			newUser.getPreferences().setLanguage(user.getLanguage());
			newUser.getPreferences().setInformSessionTimeout(true);
			
			if (!StringUtils.hasText(authenticationProviderConstant)){
				throw new OLATRuntimeException(this.getClass(), "Auth token not set! Please fix! " + authenticationProviderConstant, null);
			}

			// Now finally create that user thing on the database with all
			// credentials, person etc. in one transation context!
			identity = BaseSecurityManager.getInstance().createAndPersistIdentityAndUser(user.getUserName(), null, newUser, authenticationProviderConstant,
					user.getUserName(), user.getPassword());
			if (identity == null) {
				throw new OLATRuntimeException(this.getClass(), "Error, could not create  user and subject with name " + user.getUserName(), null);
			} else {
				
				if (user.isGuest()) {
					securityManager.addIdentityToSecurityGroup(identity, anonymousGroup);
					log .info("Created anonymous user " + user.getUserName());
				} else {
					if (user.isAdmin()) {
						securityManager.addIdentityToSecurityGroup(identity, adminGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created admin user " + user.getUserName());
					}  else if (user.isAuthor()) {
						securityManager.addIdentityToSecurityGroup(identity, authorGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log.info("Created author user " + user.getUserName());
					} else if (user.isUserManager()) {
						securityManager.addIdentityToSecurityGroup(identity, usermanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created userManager user " + user.getUserName());
					} else if (user.isGroupManager()) {
						securityManager.addIdentityToSecurityGroup(identity, groupmanagerGroup);
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created groupManager user " + user.getUserName());
					} else {
						securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
						log .info("Created user " + user.getUserName());
					}
				}
			}
		}
		return identity;
	}

	/**
	 * @return List of logins on blacklist.
	 */
	public static List<String> getLoginBlacklist() {
		return loginBlacklistChecked;
	}

	/**
	 * checks whether the given identity is allowed to change it's own password.
	 * default settings (olat.properties) : 
	 * <ul>
	 *  <li>LDAP-user are not allowed to change their pw</li>
	 *  <li>other users are allowed to change their pw</li>
	 * </ul>
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isPwdchangeallowed(Identity id) {
		
		if(id == null) return isAnyPwdchangeallowed();
		
		// if this is set to false, noone can change their pw
		if (!pwdchangeallowed)
			return false;
		LDAPLoginManager ldapLoginManager = (LDAPLoginManager) CoreSpringFactory.getBean("org.olat.ldap.LDAPLoginManager");
		if (ldapLoginManager.isIdentityInLDAPSecGroup(id)) {
			// it's an ldap-user
			return pwdchangeallowedLDAP;
		}
		return pwdchangeallowed;
	}
	
	/**
	 * use this if you don't have an identity-object (DMZ), and just want to
	 * check, if anyone could change his password
	 * 
	 * @return
	 */
	private static boolean isAnyPwdchangeallowed() {
		return pwdchangeallowed;
	}
	
	public static boolean isLogoByProfileEnabled() {
		return enabledLogoByProfile;
	}
	
	public static String getAdminUserName() {
		return adminUserName;
	}
	
	public void setDefaultUsers(List<DefaultUser> defaultUsers) {
		this.defaultUsers = defaultUsers;
	}

	public void setTestUsers(List<DefaultUser> testUsers) {
		this.testUsers = testUsers;
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
	
}