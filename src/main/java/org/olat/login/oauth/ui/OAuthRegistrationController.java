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
package org.olat.login.oauth.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.model.OAuthRegistration;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.registration.DisclaimerFormController;
import org.olat.registration.MailValidationController;
import org.olat.registration.RegistrationPersonalDataController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.TemporaryKey;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OAuthRegistrationController extends FormBasicController {

	public static final String USERPROPERTIES_FORM_IDENTIFIER = OAuthRegistrationController.class.getCanonicalName();

	private String initialEmail = "";
	
	private final OAuthRegistration registration;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final SyntaxValidator usernameSyntaxValidator;
	private List<OrganisationEmailDomain> matchedDomains;
	private FormLayoutContainer orgContainer;

	private TextElement usernameEl;
	private SingleSelection langEl;
	private SingleSelection orgSelection;
	private FormSubmit submitBtn;

	private final Map<String,FormItem> propFormItems = new HashMap<>();
	private CloseableModalController cmc;
	private DisclaimerFormController disclaimerFormCtrl;
	private MailValidationController mailValidationCtrl;
	
	private Identity authenticatedIdentity;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;
	
	public OAuthRegistrationController(UserRequest ureq, WindowControl wControl, OAuthRegistration registration) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RegistrationPersonalDataController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale(), getTranslator()));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);
		this.usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		
		this.registration = registration;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,	UserRequest ureq) {
		setFormTitle("registration.form.personal.data.title");
		setFormInfo("registration.form.personal.data.desc");
		OAuthUser oauthUser = registration.getOauthUser();

		uifactory.addSpacerElement("lang", formLayout, true);
		// second the user language
		Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		langEl = uifactory.addDropdownSingleselect("user.language", formLayout, langKeys, langValues, null);

		usernameEl = uifactory.addTextElement("username",  "user.login", 128, "", formLayout);
		usernameEl.setEnabled(oauthLoginModule.isAllowChangeOfUsername());
		usernameEl.setMandatory(true);
		if(StringHelper.containsNonWhitespace(oauthUser.getNickName())) {
			usernameEl.setValue(oauthUser.getNickName());
		} else if(StringHelper.containsNonWhitespace(oauthUser.getId())) {
			usernameEl.setValue(oauthUser.getId());
		}

		TextElement mailEl = null;

		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
				propFormItems.put(userPropertyHandler.getName(), fi);

				if (fi instanceof TextElement textElement) {
					String value = oauthUser.getProperty(userPropertyHandler.getName());

					if (UserConstants.EMAIL.equals(userPropertyHandler.getName())) {
						initialEmail = value;
						mailEl = textElement;
					}

					if (StringHelper.containsNonWhitespace(value)) {
						textElement.setValue(value);
					}
				}
			}
		}

		submitBtn = uifactory.addFormSubmitButton("save", formLayout);
		submitBtn.setVisible(mailValidationCtrl == null && orgSelection == null);
		submitBtn.setFormLayout("default");

		if (mailEl != null) {
			initValidationSelection(ureq, mailEl);
		}
	}

	private void initValidationSelection(UserRequest ureq, TextElement mailEl) {
		// mail validation happens only if the mail element is enabled and the mail is empty or mail got changed by user
		// and it only happens once, hence the ctrl check for null
		if (mailEl.isEnabled()
				&& ((!StringHelper.containsNonWhitespace(initialEmail)
				|| !mailEl.getValue().equals(initialEmail)) && mailValidationCtrl == null)) {
			initEmailValidation(ureq, mailEl);
		} else if (organisationModule.isEnabled() && organisationModule.isEmailDomainEnabled()) {
			initOrgSelection(mailEl);
		}
	}

	private void initEmailValidation(UserRequest ureq, TextElement mailEl) {
		flc.remove(submitBtn);
		mailValidationCtrl = new MailValidationController(ureq, getWindowControl(), mainForm,
				true, false, null, mailEl);
		listenTo(mailValidationCtrl);
		flc.add(mailValidationCtrl.getInitialFormItem());
	}

	private void initOrgSelection(TextElement mailEl) {
		if (orgContainer == null) {
			orgContainer = FormLayoutContainer.createDefaultFormLayout("org_selection", getTranslator());
			orgContainer.setFormTitle(translate("user.organisation"));
			orgContainer.setFormLayout("default");
		}
		flc.add(orgContainer);
		// in case of that the org selection is being recalled
		// clear previous entries by creating a new reference
		// We need to do so, to prevent false entries from initialMail
		// which got changed, so new domains could be matching
		matchedDomains = new ArrayList<>();

		String mailDomain = MailHelper.getMailDomain(mailEl.getValue());
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		searchParams.setEnabled(true);
		List<OrganisationEmailDomain> emailDomains = organisationService.getEmailDomains(searchParams);

		matchedDomains = organisationService.getMatchingEmailDomains(emailDomains, mailDomain);

		if (matchedDomains.isEmpty()) {
			// Show error, that no org match was found
			mailEl.setErrorKey("step3.reg.mismatch.form.text", WebappHelper.getMailConfig("mailSupport"));
			if (mailValidationCtrl != null) {
				deleteTemporaryKeyIfExists(mailValidationCtrl.getTemporaryKey().getRegistrationKey());
			}
		} else {
			flc.remove(submitBtn);
			// Extract orgKey as keys
			matchedDomains = matchedDomains.stream()
					.sorted(Comparator.comparing(domain -> domain.getOrganisation().getDisplayName()))
					.toList();
			String[] orgKeys = matchedDomains.stream()
					.map(domain -> domain.getOrganisation().getKey().toString())
					.toArray(String[]::new);

			// Extract concatenated displayName and Location as values
			String[] orgValues = matchedDomains.stream()
					.map(domain -> {
						String displayName = domain.getOrganisation().getDisplayName();
						String location = domain.getOrganisation().getLocation();
						return StringHelper.containsNonWhitespace(location) ? displayName + " · " + location : displayName; // location can be null, ignore if it is empty/null
					})
					.toArray(String[]::new);

			// checking for null, which only happens once (first initialization of the org area)
			// otherwise just set the newly matching keys and values
			if (orgSelection == null) {
				orgSelection = uifactory.addDropdownSingleselect("user.organisation", orgContainer, orgKeys, orgValues, null);
			} else {
				orgSelection.setKeysAndValues(orgKeys, orgValues, null);
			}

			if (matchedDomains.size() == 1) {
				orgSelection.select(orgKeys[0], true);
				orgSelection.setEnabled(false);
			} else {
				orgSelection.enableNoneSelection(translate("user.organisation.select"));
				orgSelection.setMandatory(true);
			}
			flc.add(submitBtn);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(disclaimerFormCtrl == source) {
			cmc.deactivate();
			
			if (event == Event.DONE_EVENT) {
				// User accepted disclaimer, do login now
				registrationManager.setHasConfirmedDislaimer(authenticatedIdentity);
				doLoginAndRegister(authenticatedIdentity, ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				// User did not accept, workflow ends here and user gets redirected to login screen
				showWarning("disclaimer.form.cancelled");
				DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (mailValidationCtrl == source) {
			if (event == Event.CHANGED_EVENT
					&& mailValidationCtrl.isOtpSuccessful()) {
				// success in validation, now check if there is any org mapping (if that module is enabled)
				TextElement mailEl = (TextElement) flc.getFormComponent(UserConstants.EMAIL);
				if (organisationModule.isEnabled() && organisationModule.isEmailDomainEnabled()) {
					flc.remove(mailValidationCtrl.getInitialFormItem());
					initOrgSelection(mailEl);
				} else {
					flc.add(submitBtn);
				}
			} else if (event == Event.CANCELLED_EVENT && mailValidationCtrl.getTemporaryKey() != null) {
				cleanUp();
				deleteTemporaryKeyIfExists(mailValidationCtrl.getTemporaryKey().getRegistrationKey());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(mailValidationCtrl);
		removeAsListenerAndDispose(disclaimerFormCtrl);
		removeAsListenerAndDispose(cmc);
		mailValidationCtrl = null;
		disclaimerFormCtrl = null;
		cmc = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = propFormItems.get(userPropertyHandler.getName());
			if (!userPropertyHandler.isValid(null, fi, null)) {
				allOk &= false;
			}
		}
		
		String username = usernameEl.getValue();
		TransientIdentity newIdentity = new TransientIdentity();
		newIdentity.setName(username);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
			newIdentity.setProperty(userPropertyHandler.getName(), userPropertyHandler.getStringValue(propertyItem));
		}
		
		usernameEl.clearError();
		if (!StringHelper.containsNonWhitespace(username)) {
			usernameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			ValidationResult validationResult = usernameSyntaxValidator.validate(username, newIdentity);
			if (!validationResult.isValid()) {
				String descriptions = validationResult.getInvalidDescriptions().get(0).getText(getLocale());
				usernameEl.setErrorKey("error.username.invalid", descriptions);
				allOk &= false;
			}
		}

		TextElement mailEl = (TextElement) flc.getFormComponent(UserConstants.EMAIL);
		boolean isMailUnchanged = isMailUnchanged(mailEl);
		if (orgSelection != null && isMailUnchanged) {
			orgSelection.clearError();
			if (!orgSelection.isOneSelected()) {
				orgSelection.setErrorKey("change.org.selection.error");
				allOk = false;
			}
		}

		return allOk;
	}

	private void deleteTemporaryKeyIfExists(String key) {
		TemporaryKey tempKey = registrationManager.loadTemporaryKeyByRegistrationKey(key);
		if (tempKey != null) {
			registrationManager.deleteTemporaryKey(tempKey);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		TextElement mailEl = (TextElement) flc.getFormComponent(UserConstants.EMAIL);
		boolean isMailUnchanged = isMailUnchanged(mailEl);

		if (isMailUnchanged) {
			handleUserRegistration(ureq);
		} else if (mailEl != null) {
			if (orgContainer != null) {
				flc.remove(orgContainer);
			}
			initValidationSelection(ureq, mailEl);
		} else {
			handleUserRegistration(ureq);
		}
	}

	private boolean isMailUnchanged(TextElement mailEl) {
		return mailEl != null && (initialEmail.equals(mailEl.getValue()) || mailValidationCtrl != null);
	}

	private void handleUserRegistration(UserRequest ureq) {
		if (orgSelection != null && !orgSelection.isOneSelected()) {
			orgSelection.clearError();
			orgSelection.setErrorKey("change.org.selection.error");
			return;
		}

		String lang = langEl.getSelectedKey();
		String username = usernameEl.getValue();
		OAuthUser oauthUser = registration.getOauthUser();

		User newUser = createUserWithProperties();
		newUser.getPreferences().setLanguage(lang);
		newUser.getPreferences().setInformSessionTimeout(true);

		Organisation org = null;
		if (orgSelection != null) {
			String selectedKey = orgSelection.getSelectedKey();
			Optional<OrganisationEmailDomain> selectedDomain = matchedDomains.stream()
					.filter(domain -> domain.getOrganisation().getKey().toString().equals(selectedKey))
					.findFirst();

			if (selectedDomain.isPresent()) {
				OrganisationEmailDomain domain = selectedDomain.get();
				Organisation organisationEntity = domain.getOrganisation();
				org = organisationService.getOrganisation(organisationEntity);
			}
		}

		String id = determineUserId(oauthUser, username);
		authenticatedIdentity = securityManager.createAndPersistIdentityAndUserWithOrganisation(
				null, username, null, newUser, registration.getAuthProvider(),
				BaseSecurity.DEFAULT_ISSUER, null, id, null, org, null, null);

		if (oauthLoginModule.isSkipDisclaimerDialog() || !registrationModule.isDisclaimerEnabled()) {
			doLoginAndRegister(authenticatedIdentity, ureq);
		} else {
			doOpenDisclaimer(authenticatedIdentity, ureq);
		}
	}

	private User createUserWithProperties() {
		User newUser = userManager.createUser(null, null, null);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
			userPropertyHandler.updateUserFromFormItem(newUser, propertyItem);
		}
		return newUser;
	}

	private String determineUserId(OAuthUser oauthUser, String username) {
		if (StringHelper.containsNonWhitespace(oauthUser.getId())) {
			return oauthUser.getId();
		} else if (StringHelper.containsNonWhitespace(oauthUser.getEmail())) {
			return oauthUser.getEmail();
		} else {
			return username;
		}
	}


	private void doOpenDisclaimer(Identity authIdentity, UserRequest ureq) {
		removeAsListenerAndDispose(disclaimerFormCtrl);
		disclaimerFormCtrl = new DisclaimerFormController(ureq, getWindowControl(), authIdentity, false);
		listenTo(disclaimerFormCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerFormCtrl.getInitialComponent(),
				true, translate("disclaimer.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doLoginAndRegister(Identity authIdentity, UserRequest ureq) {
		// prepare redirects to home etc, set status
		int loginStatus = AuthHelper.doLogin(authIdentity, registration.getAuthProvider(), ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			//update last login date and register active user
			securityManager.setIdentityLastLogin(authIdentity);
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
			DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
		}
	}
}