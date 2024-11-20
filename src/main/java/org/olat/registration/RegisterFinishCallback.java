/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.registration;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 14, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegisterFinishCallback implements StepRunnerCallback {

	private static final Logger log = Tracing.createLoggerFor(RegisterFinishCallback.class);

	private Invitation invitation;
	private final MainLayoutBasicController loginCtrl;

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;


	public RegisterFinishCallback(Invitation invitation, MainLayoutBasicController loginCtrl) {
		CoreSpringFactory.autowireObject(this);
		this.invitation = invitation;
		this.loginCtrl = loginCtrl;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		Identity identity = (invitation != null && invitation.getIdentity() != null) ? invitation.getIdentity() : null;

		// Make sure we have an identity
		if (identity == null) {
			identity = createNewUser(runContext);
			if (identity == null) {
				((LoginHandler) loginCtrl).showError("user.notregistered");
				return null;
			}
		} else {
			handleExistingIdentity(identity, runContext);
		}

		updateUserData(identity, runContext);
		if (invitation != null) {
			invitationService.acceptInvitation(invitation, identity);
		}

		if (loginCtrl instanceof LoginHandler loginHandler) {
			loginHandler.doLogin(ureq, identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		}
		return StepsMainRunController.DONE_MODIFIED;
	}

	private void handleExistingIdentity(Identity identity, StepsRunContext runContext) {
		String username = (String) runContext.get(RegWizardConstants.USERNAME);
		String password = (String) runContext.get(RegWizardConstants.PASSWORD);
		List<Authentication> passkeys = (List<Authentication>) runContext.get(RegWizardConstants.PASSKEYS);

		if (StringHelper.containsNonWhitespace(password)) {
			ensurePasswordAuthentication(identity, username, password);
		}

		if (passkeys != null && !passkeys.isEmpty()) {
			securityManager.persistAuthentications(identity, passkeys);
		}
	}

	private void ensurePasswordAuthentication(Identity identity, String username, String password) {
		Authentication auth = securityManager.findAuthentication(identity,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER);
		if (auth == null) {
			securityManager.createAndPersistAuthentication(identity,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null,
					username, password, loginModule.getDefaultHashAlgorithm());
		}
	}

	private Identity createNewUser(StepsRunContext runContext) {
		String firstName = (String) runContext.get(RegWizardConstants.FIRSTNAME);
		String lastName = (String) runContext.get(RegWizardConstants.LASTNAME);
		String email = (String) runContext.get(RegWizardConstants.EMAIL);
		String username = (String) runContext.get(RegWizardConstants.USERNAME);
		String password = (String) runContext.get(RegWizardConstants.PASSWORD);

		// create user with mandatory fields from registration-form
		User volatileUser = userManager.createUser(firstName, lastName, email);

		// create an identity with the given username / pwd and the user object
		List<Authentication> passkeys = (List<Authentication>) runContext.get(RegWizardConstants.PASSKEYS);
		Identity identity = registrationManager.createNewUserAndIdentityFromTemporaryKey(username, password, volatileUser, (TemporaryKey) runContext.get(RegWizardConstants.TEMPORARYKEY));

		if (identity != null && passkeys != null && !passkeys.isEmpty()) {
			securityManager.persistAuthentications(identity, passkeys);
		}
		return identity;
	}

	private void updateUserData(Identity identity, StepsRunContext runContext) {
		User user = identity.getUser();

		// Set user configured language
		Preferences preferences = user.getPreferences();
		preferences.setLanguage((String) runContext.get(RegWizardConstants.CHOSEN_LANG));
		user.setPreferences(preferences);

		// Enroll user to auto-enrolled courses if not invited
		if (invitation == null) {
			autoEnrollUser(identity);
		}

		// Add static properties if enabled and not invited
		if (invitation == null && registrationModule.isStaticPropertyMappingEnabled()) {
			addStaticProperty(user);
		}

		// Add user property values from registration forms
		populateUserPropertiesFromForm(user, RegistrationPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.PROPFORMITEMS));

		boolean isAdditionalRegistrationFormEnabled = !userManager
				.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
		if (isAdditionalRegistrationFormEnabled) {
			populateUserPropertiesFromForm(user, RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.ADDITIONALPROPFORMITEMS));
		}

		// Persist changes and send notifications
		userManager.updateUserFromIdentity(identity);
		notifyAdminOnNewUser(identity);

		// Register user's disclaimer acceptance
		registrationManager.setHasConfirmedDislaimer(identity);

		if (invitation != null && invitation.getIdentity() == null) {
			invitation = invitationService.update(invitation, identity);
		}
	}

	private void autoEnrollUser(Identity identity) {
		SelfRegistrationAdvanceOrderInput input = new SelfRegistrationAdvanceOrderInput();
		input.setIdentity(identity);
		input.setRawValues(registrationModule.getAutoEnrolmentRawValue());
		autoAccessManager.createAdvanceOrders(input);
		autoAccessManager.grantAccessToCourse(identity);
	}

	private void addStaticProperty(User user) {
		String propertyName = registrationModule.getStaticPropertyMappingName();
		String propertyValue = registrationModule.getStaticPropertyMappingValue();

		if (StringHelper.containsNonWhitespace(propertyName) && StringHelper.containsNonWhitespace(propertyValue)
				&& userPropertiesConfig.getPropertyHandler(propertyName) != null) {
			try {
				user.setProperty(propertyName, propertyValue);
			} catch (Exception e) {
				log.error("Cannot set the static property value", e);
			}
		}
	}

	private void populateUserPropertiesFromForm(User user, String formIdentifier, Map<String, FormItem> propFormItems) {
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifier, false);
		for (UserPropertyHandler handler : userPropertyHandlers) {
			FormItem formItem = propFormItems.get(handler.getName());
			handler.updateUserFromFormItem(user, formItem);
		}
	}

	private void notifyAdminOnNewUser(Identity identity) {
		String notificationEmail = registrationModule.getRegistrationNotificationEmail();
		if (notificationEmail != null) {
			registrationManager.sendNewUserNotificationMessage(notificationEmail, identity);
		}
	}
}
