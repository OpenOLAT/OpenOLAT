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

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.admin.site.UserAdminSite;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.registration.RegistrationModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Desciption: The user module represents an implementation of
 * the OLAT user with its database object, business managers and page actions.
 * 
 * @author Florian Gnägi
 */
@Service
public class UserModule extends AbstractSpringModule {

	private static final Logger log = Tracing.createLoggerFor(UserModule.class);
	
	private static final String USER_EMAIL_MANDATORY = "userEmailMandatory";
	private static final String USER_EMAIL_UNIQUE = "userEmailUnique";
	private static final String ALLOW_REQUEST_DELETE_ACCOUNT = "allow.request.delete.account";
	private static final String ALLOW_REQUEST_DELETE_ACCOUNT_DISCLAIMER = "allow.request.delete.account.disclaimer";
	private static final String MAIL_REQUEST_DELETE_ACCOUNT = "request.delete.account.mail";
	private static final String PORTRAIT_MANAGED = "user.portrait.managed";
	
	@Autowired @Qualifier("loginBlacklist")
	private ArrayList<String> loginBlacklist;
	private List<String> loginBlacklistChecked = new ArrayList<>();
	
	@Value("${password.change.allowed}")
	private boolean pwdchangeallowed;
	@Value("${password.change.allowed.without.authentications:false}")
	private boolean pwdChangeWithoutAuthenticationAllowed;
	
	@Value("${allow.request.delete.account:false}")
	private boolean allowRequestToDeleteAccount;
	@Value("${allow.request.delete.account.disclaimer:false}")
	private boolean allowRequestToDeleteAccountDisclaimer;
	@Value("${request.delete.account.mail}")
	private String mailToRequestAccountDeletion;

	private String adminUserName = "administrator";
	@Value("${user.logoByProfile:disabled}")
	private String enabledLogoByProfile;
	
	@Value("${user.email.mandatory:true}")
	private boolean isEmailMandatory;
	@Value("${user.email.unique:true}")
	private boolean isEmailUnique;
	
	@Value("${user.portrait.managed:false}")
	private boolean portraitManaged;
	
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	@Autowired
	private RegistrationModule registrationModule;

	@Autowired
	public UserModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
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
		
		log.info("Successfully added " + count + " entries to login blacklist.");
		updateProperties();

		// Check if user manager is configured properly and has user property
		// handlers for the mandatory user properties used in OLAT
		checkMandatoryUserProperty(UserConstants.FIRSTNAME);
		checkMandatoryUserProperty(UserConstants.LASTNAME);
		if (isEmailMandatory()) {
			checkMandatoryUserProperty(UserConstants.EMAIL);
		}

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
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String userEmailOptionalValue = getStringPropertyValue(USER_EMAIL_MANDATORY, false);
		if(StringHelper.containsNonWhitespace(userEmailOptionalValue)) {
			isEmailMandatory = "true".equalsIgnoreCase(userEmailOptionalValue);
		}
		registrationModule.resetEmailUserProperty();
		
		String userEmailUniquenessOptionalValue = getStringPropertyValue(USER_EMAIL_UNIQUE, false);
		if(StringHelper.containsNonWhitespace(userEmailUniquenessOptionalValue)) {
			isEmailUnique = "true".equalsIgnoreCase(userEmailUniquenessOptionalValue);
		}
		
		String allowRequestDeleteObj = getStringPropertyValue(ALLOW_REQUEST_DELETE_ACCOUNT, false);
		if(StringHelper.containsNonWhitespace(allowRequestDeleteObj)) {
			allowRequestToDeleteAccount = "true".equalsIgnoreCase(allowRequestDeleteObj);
		}
		String allowRequestDeleteDisclaimerObj = getStringPropertyValue(ALLOW_REQUEST_DELETE_ACCOUNT_DISCLAIMER, false);
		if(StringHelper.containsNonWhitespace(allowRequestDeleteDisclaimerObj)) {
			allowRequestToDeleteAccountDisclaimer = "true".equalsIgnoreCase(allowRequestDeleteDisclaimerObj);
		}
		String mailRequestDeleteObj = getStringPropertyValue(MAIL_REQUEST_DELETE_ACCOUNT, false);
		if(StringHelper.containsNonWhitespace(mailRequestDeleteObj)) {
			mailToRequestAccountDeletion = mailRequestDeleteObj;
		}
		
		String portraitManagedObj = getStringPropertyValue(PORTRAIT_MANAGED, false);
		if(StringHelper.containsNonWhitespace(portraitManagedObj)) {
			portraitManaged = "true".equalsIgnoreCase(portraitManagedObj);
		}
	}

	private void checkMandatoryUserProperty(String userPropertyIdentifyer) {
		List<UserPropertyHandler> propertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
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
	 * @return List of logins on blacklist.
	 */
	public List<String> getLoginBlacklist() {
		return loginBlacklistChecked;
	}
	
	/**
	 * Check wether a login is on the blacklist.
	 * 
	 * @param login
	 * @return True if login is in blacklist
	 */
	public boolean isLoginOnBlacklist(String login) {
		login = login.toLowerCase();
		for (String regexp: getLoginBlacklist()) {
			if (login.matches(regexp)) {
				log.info(Tracing.M_AUDIT, "Blacklist entry match for login '" + login + "' with regexp '" + regexp + "'.");
				return true;
			}
		}
		return false;
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
	public boolean isPwdChangeAllowed(Identity id) {
		if(id == null) {
			return isAnyPasswordChangeAllowed();
		}
		
		// if this is set to false, nobody can change their password
		if (!pwdchangeallowed) {
			return false;
		}
		
		// call to CoreSpringFactory to break dependencies cycles
		// (the method will only be called with a running application)
		
		// check if the user has an OLAT provider token, otherwise a password change makes no sense
		Authentication auth = CoreSpringFactory.getImpl(BaseSecurity.class)
				.findAuthentication(id, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		if(auth == null && !pwdChangeWithoutAuthenticationAllowed) {
			return false;
		}
		
		LDAPLoginManager ldapLoginManager = CoreSpringFactory.getImpl(LDAPLoginManager.class);
		if (ldapLoginManager.isIdentityInLDAPSecGroup(id)) {
			// it's an ldap-user
			return CoreSpringFactory.getImpl(LDAPLoginModule.class)
					.isPropagatePasswordChangedOnLdapServer();
		}
		return pwdchangeallowed;
	}
	
	/**
	 * use this if you don't have an identity-object (DMZ), and just want to
	 * check, if anyone could change his password
	 * 
	 * @return
	 */
	public boolean isAnyPasswordChangeAllowed() {
		return pwdchangeallowed;
	}
	
	public boolean isLogoByProfileEnabled() {
		return "enabled".equals(enabledLogoByProfile);
	}
	
	public String getAdminUserName() {
		return adminUserName;
	}

	public boolean isEmailMandatory() {
		return isEmailMandatory;
	}

	public void setEmailMandatory(boolean isEmailMandatory) {
		this.isEmailMandatory = isEmailMandatory;
		String isEmailMandatoryStr = isEmailMandatory ? "true" : "false";
		setStringProperty(USER_EMAIL_MANDATORY, isEmailMandatoryStr, true);
		registrationModule.resetEmailUserProperty();
	}

	public boolean isEmailUnique() {
		return isEmailUnique;
	}

	public void setEmailUnique(boolean isEmailUnique) {
		this.isEmailUnique = isEmailUnique;
		String isEmailUniqueStr = isEmailUnique ? "true" : "false";
		setStringProperty(USER_EMAIL_UNIQUE, isEmailUniqueStr, true);
	}

	public boolean isAllowRequestToDeleteAccount() {
		return allowRequestToDeleteAccount;
	}

	public void setAllowRequestToDeleteAccount(boolean allowRequestToDeleteAccount) {
		this.allowRequestToDeleteAccount = allowRequestToDeleteAccount;
		String allowed = allowRequestToDeleteAccount ? "true" : "false";
		setStringProperty(ALLOW_REQUEST_DELETE_ACCOUNT, allowed, true);
	}

	public boolean isAllowRequestToDeleteAccountDisclaimer() {
		return allowRequestToDeleteAccountDisclaimer;
	}

	public void setAllowRequestToDeleteAccountDisclaimer(boolean allowRequestToDeleteAccountDisclaimer) {
		this.allowRequestToDeleteAccountDisclaimer = allowRequestToDeleteAccountDisclaimer;
		String allowed = allowRequestToDeleteAccountDisclaimer ? "true" : "false";
		setStringProperty(ALLOW_REQUEST_DELETE_ACCOUNT_DISCLAIMER, allowed, true);
	}

	public String getMailToRequestAccountDeletion() {
		return mailToRequestAccountDeletion;
	}

	public void setMailToRequestAccountDeletion(String mailToRequestAccountDeletion) {
		this.mailToRequestAccountDeletion = mailToRequestAccountDeletion;
		setStringProperty(MAIL_REQUEST_DELETE_ACCOUNT, mailToRequestAccountDeletion, true);
	}

	public boolean isPortraitManaged() {
		return portraitManaged;
	}

	public void setPortraitManaged(boolean portraitManaged) {
		this.portraitManaged = portraitManaged;
		setStringProperty(PORTRAIT_MANAGED, Boolean.toString(portraitManaged), true);
	}
	
}