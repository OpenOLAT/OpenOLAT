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
import java.util.Arrays;
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
	private static final String ABOUT_ME_ENABLED = "user.about.me";
	private static final String USER_AUTOMATIC_DEACTIVATION = "user.automatic.deactivation";
	private static final String USER_AUTOMATIC_DELETION = "user.automatic.deletion";
	
	private static final String USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DEACTIVATION = "user.days.before.deactivation";
	private static final String USER_MAIL_BEFORE_AUTOMATIC_DEACTIVATION = "user.mail.before.automatic.deactivation";
	private static final String USER_MAIL_AFTER_AUTOMATIC_DEACTIVATION = "user.mail.after.automatic.deactivation";
	private static final String USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DEACTIVATION = "user.days.before.mail.automatic.deactivation";
	private static final String USER_MAIL_COPY_AFTER_AUTOMATIC_DEACTIVATION = "user.mail.copy.after.automatic.deactivation";
	private static final String USER_MAIL_COPY_BEFORE_AUTOMATIC_DEACTIVATION = "user.mail.copy.before.automatic.deactivation";
	
	private static final String USER_NUM_OF_DAYS_BEFORE_MAIL_EXPIRATION = "user.days.before.mail.expiration";
	private static final String USER_MAIL_BEFORE_EXPIRATION = "user.mail.before.expiration";
	private static final String USER_MAIL_AFTER_EXPIRATION = "user.mail.after.expiration";
	private static final String USER_MAIL_COPY_AFTER_AUTOMATIC_EXPIRATION = "user.mail.copy.after.automatic.expiration";
	private static final String USER_MAIL_COPY_BEFORE_AUTOMATIC_EXPIRATION = "user.mail.copy.before.automatic.expiration";
	
	private static final String USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DELETION = "user.days.before.deletion";
	private static final String USER_MAIL_BEFORE_AUTOMATIC_DELETION = "user.mail.before.automatic.deletion";
	private static final String USER_MAIL_AFTER_AUTOMATIC_DELETION = "user.mail.after.automatic.deletion";
	private static final String USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DELETION = "user.days.before.mail.automatic.deletion";
	private static final String USER_MAIL_COPY_AFTER_AUTOMATIC_DELETION = "user.mail.copy.after.automatic.deletion";
	private static final String USER_MAIL_COPY_BEFORE_AUTOMATIC_DELETION = "user.mail.copy.before.automatic.deletion";
	
	
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
	
	@Value("${user.about.me:true}")
	private boolean userAboutMeEnabled;
	
	@Value("${user.automatic.deactivation:false}")
	private boolean userAutomaticDeactivation;
	@Value("${user.days.before.deactivation:720}")
	private int numberOfInactiveDayBeforeDeactivation;
	@Value("${user.mail.before.automatic.deactivation:false}")
	private boolean mailBeforeDeactivation;
	@Value("${user.mail.after.automatic.deactivation:false}")
	private boolean mailAfterDeactivation;
	@Value("${user.days.before.mail.automatic.deactivation:30}")
	private int numberOfDayBeforeDeactivationMail;
	@Value("${user.days.reactivation.period:30}")
	private int numberOfDayReactivationPeriod;
	@Value("${user.mail.copy.after.automatic.deactivation:}")
	private String mailCopyAfterDeactivation;
	@Value("${user.mail.copy.before.automatic.deactivation:}")
	private String mailCopyBeforeDeactivation;
	
	@Value("${user.days.before.mail.expiration:0}")
	private int numberOfDayBeforeExpirationMail;
	@Value("${user.mail.before.expiration:false}")
	private boolean mailBeforeExpiration;
	@Value("${user.mail.after.expiration:false}")
	private boolean mailAfterExpiration;
	@Value("${user.mail.copy.after.automatic.expiration:}")
	private String mailCopyAfterExpiration;
	@Value("${user.mail.copy.before.automatic.expiration:}")
	private String mailCopyBeforeExpiration;
	
	@Value("${user.automatic.deletion:false}")
	private boolean userAutomaticDeletion;
	@Value("${user.automatic.delete.users.percentage:50}")
	private int userAutomaticDeletionUsersPercentage;
	@Value("${user.days.before.deletion:180}")
	private int numberOfInactiveDayBeforeDeletion;
	@Value("${user.mail.before.automatic.deletion:false}")
	private boolean mailBeforeDeletion;
	@Value("${user.mail.after.automatic.deletion:false}")
	private boolean mailAfterDeletion;
	@Value("${user.days.before.mail.automatic.deletion:30}")
	private int numberOfDayBeforeDeletionMail;
	@Value("${user.mail.copy.after.automatic.deletion:}")
	private String mailCopyAfterDeletion;
	@Value("${user.mail.copy.before.automatic.deletion:}")
	private String mailCopyBeforeDeletion;

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
				log.error("Invalid pattern syntax in blacklist. Pattern: {}. Removing from this entry from list ", regexp);
			}
			count ++;
		}
		
		log.info("Successfully added {} entries to login blacklist.", count);
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
		
		// deactivation
		String userAutoDeactivationObj = getStringPropertyValue(USER_AUTOMATIC_DEACTIVATION, false);
		if(StringHelper.containsNonWhitespace(userAutoDeactivationObj)) {
			userAutomaticDeactivation = "true".equalsIgnoreCase(userAutoDeactivationObj);
		}
		
		numberOfInactiveDayBeforeDeactivation = getIntPropertyValue(USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DEACTIVATION, numberOfInactiveDayBeforeDeactivation);
		
		String mailBeforeDeactivationObj = getStringPropertyValue(USER_MAIL_BEFORE_AUTOMATIC_DEACTIVATION, false);
		if(StringHelper.containsNonWhitespace(mailBeforeDeactivationObj)) {
			mailBeforeDeactivation = "true".equalsIgnoreCase(mailBeforeDeactivationObj);
		}
		
		numberOfDayBeforeDeactivationMail = getIntPropertyValue(USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DEACTIVATION, numberOfDayBeforeDeactivationMail);

		String mailAfterDeactivationObj = getStringPropertyValue(USER_MAIL_AFTER_AUTOMATIC_DEACTIVATION, false);
		if(StringHelper.containsNonWhitespace(mailAfterDeactivationObj)) {
			mailAfterDeactivation = "true".equalsIgnoreCase(mailAfterDeactivationObj);
		}
		
		mailCopyAfterDeactivation = getStringPropertyValue(USER_MAIL_COPY_AFTER_AUTOMATIC_DEACTIVATION, mailCopyAfterDeactivation);
		mailCopyBeforeDeactivation = getStringPropertyValue(USER_MAIL_COPY_BEFORE_AUTOMATIC_DEACTIVATION, mailCopyBeforeDeactivation);
		
		// expiration
		numberOfDayBeforeExpirationMail = getIntPropertyValue(USER_NUM_OF_DAYS_BEFORE_MAIL_EXPIRATION, numberOfDayBeforeExpirationMail);
		
		String mailBeforeExpirationObj = getStringPropertyValue(USER_MAIL_BEFORE_EXPIRATION, false);
		if(StringHelper.containsNonWhitespace(mailBeforeExpirationObj)) {
			mailBeforeExpiration = "true".equalsIgnoreCase(mailBeforeExpirationObj);
		}
		
		String mailAfterExpirationObj = getStringPropertyValue(USER_MAIL_AFTER_EXPIRATION, false);
		if(StringHelper.containsNonWhitespace(mailAfterExpirationObj)) {
			mailAfterExpiration = "true".equalsIgnoreCase(mailAfterExpirationObj);
		}
		
		mailCopyAfterExpiration = getStringPropertyValue(USER_MAIL_COPY_AFTER_AUTOMATIC_EXPIRATION, mailCopyAfterExpiration);
		mailCopyBeforeExpiration = getStringPropertyValue(USER_MAIL_COPY_BEFORE_AUTOMATIC_EXPIRATION, mailCopyBeforeExpiration);
		
		// deletion
		String userAutoDeletionObj = getStringPropertyValue(USER_AUTOMATIC_DELETION, false);
		if(StringHelper.containsNonWhitespace(userAutoDeletionObj)) {
			userAutomaticDeletion = "true".equalsIgnoreCase(userAutoDeletionObj);
		}
		
		numberOfInactiveDayBeforeDeletion = getIntPropertyValue(USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DELETION, numberOfInactiveDayBeforeDeletion);
		
		String mailBeforeDeletionObj = getStringPropertyValue(USER_MAIL_BEFORE_AUTOMATIC_DELETION, false);
		if(StringHelper.containsNonWhitespace(mailBeforeDeletionObj)) {
			mailBeforeDeletion = "true".equalsIgnoreCase(mailBeforeDeletionObj);
		}
		
		numberOfDayBeforeDeletionMail = getIntPropertyValue(USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DELETION, numberOfDayBeforeDeletionMail);

		String mailAfterDeletionObj = getStringPropertyValue(USER_MAIL_AFTER_AUTOMATIC_DELETION, false);
		if(StringHelper.containsNonWhitespace(mailAfterDeletionObj)) {
			mailAfterDeletion = "true".equalsIgnoreCase(mailAfterDeletionObj);
		}
		
		mailCopyAfterDeletion = getStringPropertyValue(USER_MAIL_COPY_AFTER_AUTOMATIC_DELETION, mailCopyAfterDeletion);
		mailCopyBeforeDeletion = getStringPropertyValue(USER_MAIL_COPY_BEFORE_AUTOMATIC_DELETION, mailCopyBeforeDeletion);
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
				log.info(Tracing.M_AUDIT, "Blacklist entry match for login '{}' with regexp '{}'.", login, regexp);
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
				.findAuthentication(id, BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
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
	
	public boolean isPasswordChangeWithoutAuthenticationAllowed() {
		return pwdChangeWithoutAuthenticationAllowed;
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

	public boolean isUserAboutMeEnabled() {
		return userAboutMeEnabled;
	}

	public void setUserAboutMeEnabled(boolean enabled) {
		this.userAboutMeEnabled = enabled;
		setStringProperty(ABOUT_ME_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isUserAutomaticDeactivation() {
		return userAutomaticDeactivation;
	}

	public void setUserAutomaticDeactivation(boolean enabled) {
		this.userAutomaticDeactivation = enabled;
		setStringProperty(USER_AUTOMATIC_DEACTIVATION, Boolean.toString(enabled), true);
	}

	public int getNumberOfInactiveDayBeforeDeactivation() {
		return numberOfInactiveDayBeforeDeactivation;
	}

	public void setNumberOfInactiveDayBeforeDeactivation(int days) {
		this.numberOfInactiveDayBeforeDeactivation = days;
		setIntProperty(USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DEACTIVATION, days, true);
	}

	public boolean isMailBeforeDeactivation() {
		return mailBeforeDeactivation;
	}

	public void setMailBeforeDeactivation(boolean enabled) {
		this.mailBeforeDeactivation = enabled;
		setStringProperty(USER_MAIL_BEFORE_AUTOMATIC_DEACTIVATION, Boolean.toString(enabled), true);
	}

	public boolean isMailAfterDeactivation() {
		return mailAfterDeactivation;
	}

	public void setMailAfterDeactivation(boolean enabled) {
		this.mailAfterDeactivation = enabled;
		setStringProperty(USER_MAIL_AFTER_AUTOMATIC_DEACTIVATION, Boolean.toString(enabled), true);
	}

	public int getNumberOfDayBeforeDeactivationMail() {
		return numberOfDayBeforeDeactivationMail;
	}

	public void setNumberOfDayBeforeDeactivationMail(int days) {
		this.numberOfDayBeforeDeactivationMail = days;
		setIntProperty(USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DEACTIVATION, days, true);
	}
	
	public boolean isMailBeforeExpiration() {
		return mailBeforeExpiration;
	}

	public void setMailBeforeExpiration(boolean enabled) {
		this.mailBeforeExpiration = enabled;
		setStringProperty(USER_MAIL_BEFORE_EXPIRATION, Boolean.toString(enabled), true);
	}
	
	public int getNumberOfDayBeforeExpirationMail() {
		return numberOfDayBeforeExpirationMail;
	}

	public void setNumberOfDayBeforeExpirationMail(int days) {
		this.numberOfDayBeforeExpirationMail = days;
		setIntProperty(USER_NUM_OF_DAYS_BEFORE_MAIL_EXPIRATION, days, true);
	}
	
	public boolean isMailAfterExpiration() {
		return mailAfterExpiration;
	}

	public void setMailAfterExpiration(boolean enabled) {
		this.mailAfterExpiration = enabled;
		setStringProperty(USER_MAIL_AFTER_EXPIRATION, Boolean.toString(enabled), true);
	}
	
	public int getNumberOfDayReactivationPeriod() {
		return numberOfDayReactivationPeriod;
	}

	public void setNumberOfDayReactivationPeriod(int numberOfDayReactivationPeriod) {
		this.numberOfDayReactivationPeriod = numberOfDayReactivationPeriod;
	}

	public boolean isUserAutomaticDeletion() {
		return userAutomaticDeletion;
	}

	public void setUserAutomaticDeletion(boolean enabled) {
		this.userAutomaticDeletion = enabled;
		setStringProperty(USER_AUTOMATIC_DELETION, Boolean.toString(enabled), true);
	}
	
	public int getUserAutomaticDeletionUsersPercentage() {
		return userAutomaticDeletionUsersPercentage;
	}

	public int getNumberOfInactiveDayBeforeDeletion() {
		return numberOfInactiveDayBeforeDeletion;
	}

	public void setNumberOfInactiveDayBeforeDeletion(int days) {
		this.numberOfInactiveDayBeforeDeletion = days;
		setIntProperty(USER_NUM_OF_DAYS_BEFORE_AUTOMATIC_DELETION, days, true);
	}
	
	public boolean isMailBeforeDeletion() {
		return mailBeforeDeletion;
	}

	public void setMailBeforeDeletion(boolean enabled) {
		this.mailBeforeDeletion = enabled;
		setStringProperty(USER_MAIL_BEFORE_AUTOMATIC_DELETION, Boolean.toString(enabled), true);
	}

	public boolean isMailAfterDeletion() {
		return mailAfterDeletion;
	}

	public void setMailAfterDeletion(boolean enabled) {
		this.mailAfterDeletion = enabled;
		setStringProperty(USER_MAIL_AFTER_AUTOMATIC_DELETION, Boolean.toString(enabled), true);
	}

	public int getNumberOfDayBeforeDeletionMail() {
		return numberOfDayBeforeDeletionMail;
	}

	public void setNumberOfDayBeforeDeletionMail(int days) {
		this.numberOfDayBeforeDeletionMail = days;
		setIntProperty(USER_NUM_OF_DAYS_BEFORE_MAIL_AUTOMATIC_DELETION, days, true);
	}
	
	public void setMailCopyAfterDeactivation(String mailCopyAfterDeactivation) {
		this.mailCopyAfterDeactivation = cleanStringProperty(mailCopyAfterDeactivation);
		setStringProperty(USER_MAIL_COPY_AFTER_AUTOMATIC_DEACTIVATION, this.mailCopyAfterDeactivation, true);
	}
	
	public List<String> getMailCopyAfterDeactivation() {
		return convertStringToList(mailCopyAfterDeactivation);
	}
	
	public void setMailCopyBeforeDeactivation(String mailCopyBeforeDeactivation) {
		this.mailCopyBeforeDeactivation = cleanStringProperty(mailCopyBeforeDeactivation);
		setStringProperty(USER_MAIL_COPY_BEFORE_AUTOMATIC_DEACTIVATION, this.mailCopyBeforeDeactivation, true);
	}
	
	public List<String> getMailCopyBeforeDeactivation() {
		return convertStringToList(mailCopyBeforeDeactivation);
	}
	
	public void setMailCopyAfterDeletion(String mailCopyAfterDeletion) {
		this.mailCopyAfterDeletion = cleanStringProperty(mailCopyAfterDeletion);
		setStringProperty(USER_MAIL_COPY_AFTER_AUTOMATIC_DELETION, this.mailCopyAfterDeletion, true);
	}
	
	public List<String> getMailCopyAfterDeletion() {
		return convertStringToList(mailCopyAfterDeletion);
	}
	
	public void setMailCopyBeforeDeletion(String mailCopyBeforeDeletion) {
		this.mailCopyBeforeDeletion = cleanStringProperty(mailCopyBeforeDeletion);
		setStringProperty(USER_MAIL_COPY_BEFORE_AUTOMATIC_DELETION, this.mailCopyBeforeDeletion, true);
	}
	
	public List<String> getMailCopyBeforeDeletion() {
		return convertStringToList(mailCopyBeforeDeletion);
	}
	
	public void setMailCopyAfterExpiration(String mailCopyAfterExpiration) {
		this.mailCopyAfterExpiration = cleanStringProperty(mailCopyAfterExpiration);
		setStringProperty(USER_MAIL_COPY_AFTER_AUTOMATIC_EXPIRATION, this.mailCopyAfterExpiration, true);
	}
	
	public List<String> getMailCopyAfterExpiration() {
		return convertStringToList(mailCopyAfterExpiration);
	}
	
	public void setMailCopyBeforeExpiration(String mailCopyBeforeExpiration) {
		this.mailCopyBeforeExpiration = cleanStringProperty(mailCopyBeforeExpiration);
		setStringProperty(USER_MAIL_COPY_BEFORE_AUTOMATIC_EXPIRATION, this.mailCopyBeforeExpiration, true);
	}
	
	public List<String> getMailCopyBeforeExpiration() {
		return convertStringToList(mailCopyBeforeExpiration);
	}
	
	private String cleanStringProperty(String stringToClean) {
		if (stringToClean == null) {
			return null;
		}
		
		return stringToClean.replace(" ", "");
	}
	
	private List<String> convertStringToList(String toList) {
		if (!StringHelper.containsNonWhitespace(toList)) {
			return new ArrayList<>();
		}
		
		toList = toList.replace(" ", "");
		
		return Arrays.asList(toList.split(","));
	}

}