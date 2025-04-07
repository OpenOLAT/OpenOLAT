/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* https://www.apache.org/licenses/LICENSE-2.0
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
* <a href="https://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.login.LoginModule;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.organisation.element.OrgSelectorElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Initial Date:  Jul 31, 2003
 *  @author gnaegi
 *  
 *  Comment:  
 *  Displays a form to create a new user on the OLAT plattform
 */
public class UserCreateController extends BasicController  {

	private final NewUserForm createUserForm;
	
	@Autowired
	private UserManager userManager;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public UserCreateController (UserRequest ureq, WindowControl wControl,
			Organisation preselectedOrganisation, boolean canCreateOLATPassword) {
		super(ureq, wControl, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		
		Translator pT = userManager.getPropertyHandlerTranslator(getTranslator());
		createUserForm = new NewUserForm(ureq, wControl, preselectedOrganisation, canCreateOLATPassword, pT);
		listenTo(createUserForm);

		putInitialPanel(createUserForm.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//empty
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == createUserForm) {
			if (event instanceof SingleIdentityChosenEvent) {
				showInfo("new.user.successful");
				fireEvent(ureq, event);
			} else if(event == Event.FAILED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}
}
/**
 * <pre>
 * 
 *  Initial Date:  Jul 31, 2003
 * 
 *  @author gnaegi
 *  
 *  Comment:  
 *  Form for creating new a new user as administrator
 *  
 * </pre>
 */

class NewUserForm extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(NewUserForm.class);
	
	private static final String USER_PROPS_IDENTIFIER = NewUserForm.class.getCanonicalName();
	private static final String FIELD_NEW1 = "passwordnew1";
	private static final String FIELD_NEW2 = "passwordnew2";
	private static final String LOGINNAME = "loginname";

	private boolean showPasswordFields = false;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final List<Organisation> manageableOrganisations;
	private final Organisation preselectedOrganisation;
	
	private TextElement emailTextElement;
	private TextElement usernameTextElement;
	private TextElement psw1TextElement;
	private TextElement psw2TextElement;
	private OrgSelectorElement organisationsElement;
	private SingleSelection languageSingleSelection;
	private SelectionElement authCheckbox;
	private DateChooser expirationDateEl;

	private final SyntaxValidator passwordSyntaxValidator;
	private final SyntaxValidator usernameSyntaxValidator;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param showPasswordFields: true the password fields are used, the user can 
	 * enter a password for the new user; false: the passwort is not used at all
	 */
	public NewUserForm(UserRequest ureq, WindowControl wControl,
			Organisation preselectedOrganisation, boolean showPasswordFields, Translator translator) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), translator));
		setTranslator(Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale(), getTranslator()));
		this.showPasswordFields = showPasswordFields;
		this.passwordSyntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		this.usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		this.preselectedOrganisation = preselectedOrganisation;
		
		Roles managerRoles = ureq.getUserSession().getRoles();
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), managerRoles,
				OrganisationRoles.administrator, OrganisationRoles.rolesmanager, OrganisationRoles.usermanager);
		initForm(ureq);
	}	 
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.newuser");
		setFormDescription("new.form.please.enter");
		setFormContextHelp("manual_admin/usermanagement/");
		formLayout.setElementCssClass("o_sel_id_create");
		
		usernameTextElement = uifactory.addTextElement(LOGINNAME, "username", 128, "", formLayout);
		usernameTextElement.setMandatory(true);
		usernameTextElement.setDisplaySize(30);
		usernameTextElement.setElementCssClass("o_sel_id_username");

		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_IDENTIFIER, true);
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem formItem = userPropertyHandler.addFormItem(ureq.getLocale(), null, USER_PROPS_IDENTIFIER, true, formLayout);
			// special case to handle email field
			if(userPropertyHandler.getName().equals(UserConstants.EMAIL)) {
				emailTextElement = (TextElement) formItem;
				emailTextElement.addActionListener(FormEvent.ONCHANGE);
				if (!userModule.isEmailMandatory()) {
					formItem.setMandatory(false);
				}
			}

			formItem.setElementCssClass("o_sel_id_" + userPropertyHandler.getName().toLowerCase());
		}
		
		Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		// Build css classes for reference languages
		languageSingleSelection = uifactory.addDropdownSingleselect("new.form.language", formLayout, langKeys, langValues, null); 
		// select default language in form
		languageSingleSelection.select(I18nModule.getDefaultLocale().toString(), true);
		
		organisationsElement = uifactory.addOrgSelectorElement("new.form.organisations", formLayout, getWindowControl(), manageableOrganisations);
		organisationsElement.setVisible(manageableOrganisations.size() > 1);
		organisationsElement.addActionListener(FormEvent.ONCHANGE);
		boolean selected = false;
		for(Organisation organisation:manageableOrganisations) {
			if(preselectedOrganisation.getKey().equals(organisation.getKey())) {
				organisationsElement.setSelection(organisation.getKey());
				selected = true;
			}
		}
		
		if(!selected && !manageableOrganisations.isEmpty()) {
			organisationsElement.setSelection(manageableOrganisations.get(0).getKey());
		}

		expirationDateEl = uifactory.addDateChooser("rightsForm.expiration.date", null, formLayout);
		
		//add password fields!!!
		if (showPasswordFields) {
			String descriptions = formatDescriptionAsList(passwordSyntaxValidator.getAllDescriptions(), getLocale());
			uifactory.addStaticTextElement("heading2", null,
					translate("new.form.please.enter.pwd", descriptions), formLayout);

			// checkBox: generate user with OLAT authentication or not
			String[] authKeys = {"xx"};
			String[] authValues = {translate("new.form.auth.true")};
			authCheckbox = uifactory.addCheckboxesHorizontal("new.form.auth", formLayout, authKeys, authValues);
			authCheckbox.select("xx", showPasswordFields);
			authCheckbox.addActionListener(FormEvent.ONCLICK);

			// if OLAT authentication is used, use the pwd below
			psw1TextElement = uifactory.addPasswordElement(FIELD_NEW1, "new.form.password.new1", 5000, "", formLayout);
			psw1TextElement.setMandatory(true);
			psw1TextElement.setDisplaySize(30);
			psw1TextElement.setVisible(showPasswordFields);
			psw1TextElement.setElementCssClass("o_sel_id_password1");
			psw1TextElement.setAutocomplete("new-password");

			psw2TextElement = uifactory.addPasswordElement(FIELD_NEW2, "new.form.password.new2", 5000, "", formLayout);
			psw2TextElement.setMandatory(true);
			psw2TextElement.setDisplaySize(30);		
			psw2TextElement.setVisible(showPasswordFields);
			psw2TextElement.setElementCssClass("o_sel_id_password2");
			psw2TextElement.setAutocomplete("new-password");
		}
		
		uifactory.addFormSubmitButton("save", "submit.save", formLayout);
	}
	
	private void updateEmailDomainUI() {
		if (organisationModule.isEmailDomainEnabled() && organisationsElement.isExactlyOneSelected()) {
			List<OrganisationEmailDomain> emailDomains = organisationService.getEnabledEmailDomains(() -> organisationsElement.getSingleSelection());
			boolean emailDomainAllowed = organisationService.isEmailDomainAllowed(emailDomains, emailTextElement.getValue());
			if (!emailDomainAllowed) {
				emailTextElement.setWarningKey("error.email.domain.not.allowed");
			} else {
				emailTextElement.clearWarning();
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (showPasswordFields && source == authCheckbox) {
			psw1TextElement.setVisible(authCheckbox.isSelected(0));
			psw2TextElement.setVisible(authCheckbox.isSelected(0));
		} else if (source == organisationsElement) {
			updateEmailDomainUI();
		} else if (source == emailTextElement) {
			updateEmailDomainUI();
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(emailTextElement != fiSrc && organisationsElement != fiSrc) { // E-mail checks itself
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// Transient identity for validations
		String username = usernameTextElement.getValue();
		TransientIdentity newIdentity = new TransientIdentity();
		newIdentity.setName(username);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
			newIdentity.setProperty(userPropertyHandler.getName(), userPropertyHandler.getStringValue(propertyItem));
		}
		
		// validate if username does match the syntactical login requirements
		usernameTextElement.clearError();
		if (!StringHelper.containsNonWhitespace(username)) {
			usernameTextElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			ValidationResult validationResult = usernameSyntaxValidator.validate(username, newIdentity);
			if (!validationResult.isValid()) {
				String descriptions = validationResult.getInvalidDescriptions().get(0).getText(getLocale());
				usernameTextElement.setErrorKey("error.username.invalid", descriptions);
				allOk &= false;
			}
		}
		
		// validate special rules for each user property
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			//we assume here that there are only textElements for the user properties
			FormItem formItem = flc.getFormComponent(userPropertyHandler.getName());
			formItem.clearError();
			if ( ! userPropertyHandler.isValid(null, formItem, null) || formItem.hasError()) {
				allOk &= false;
			}
		}

		// special test on email address: validate if email is already used
		String email = emailTextElement.getValue();
		emailTextElement.clearError();
		if(emailTextElement.isVisible() && emailTextElement.isEnabled()) {
			allOk &= emailTextElement.validate();
		}
		if (!userManager.isEmailAllowed(email)) {
			emailTextElement.setErrorKey("new.error.email.choosen");
			allOk &= false;
		}
		
		// organization
		organisationsElement.clearError();
		organisationsElement.clearWarning();
		if(organisationsElement.isVisible() && !organisationsElement.isExactlyOneSelected()) {
			organisationsElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		// expiration
		expirationDateEl.clearError();
		if(expirationDateEl.getDate() != null && expirationDateEl.getDate().before(ureq.getRequestTimestamp())) {
			expirationDateEl.setErrorKey("error.date.in.past");
			allOk &= false;
		}

		// validate if new password does match the syntactical password requirement
		// password fields depend on form configuration
		if (showPasswordFields && psw1TextElement != null && psw2TextElement != null && authCheckbox.isSelected(0)) {
			psw1TextElement.clearError();
			psw2TextElement.clearError();
			
			String pwd = psw1TextElement.getValue();
			if(psw1TextElement.isEmpty("new.form.mandatory") || psw1TextElement.hasError()) {
				allOk &= false;
			} else {
				String newPassword = psw1TextElement.getValue();
				
				ValidationResult validationResult = passwordSyntaxValidator.validate(newPassword, newIdentity);
				if (!validationResult.isValid()) {
					String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
					psw1TextElement.setErrorKey("error.password.invalid", descriptions);
					allOk &= false;
				}
			}
			if(psw2TextElement.isEmpty("new.form.mandatory") || psw2TextElement.hasError()) {
				allOk &= false;
			} else if (!pwd.equals(psw2TextElement.getValue())) {
				psw2TextElement.setErrorKey("new.error.password.nomatch");
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Create user on database
		Identity s = doCreateAndPersistIdentity();
		if (s != null) {
			log.info(Tracing.M_AUDIT, "user successfully created: {}", s.getKey());
			fireEvent(ureq, new SingleIdentityChosenEvent(s));
		} else {
			// Could not save form, display error
			getWindowControl().setError(translate("new.user.unsuccessful"));
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}
	
	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private Identity doCreateAndPersistIdentity() {
		String lang = languageSingleSelection.getSelectedKey();
		String username = usernameTextElement.getValue();
		String pwd = null;
		// use password only when configured to do so
		if (showPasswordFields && authCheckbox.isSelected(0)) {
			pwd = psw1TextElement.getValue();
			if (!StringHelper.containsNonWhitespace(pwd)) {
				// treat white-space passwords as no-password. This is fine, a password can be set later on
				pwd = null;
			}
		}
		// Create new user and identity and put user to users group
		// Create transient user without firstName,lastName, email
		User newUser = userManager.createUser(null, null, null);
		// Now add data from user fields (firstName,lastName and email are mandatory)
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
			userPropertyHandler.updateUserFromFormItem(newUser, propertyItem);
		}
		// Init preferences
		newUser.getPreferences().setLanguage(lang);
		newUser.getPreferences().setInformSessionTimeout(true);
		// Save everything in database
		Organisation userOrganisation = null;
		if(organisationsElement.isExactlyOneSelected()) {
			Long selectedOrganisationKey = organisationsElement.getSingleSelection();
			for(Organisation organisation: manageableOrganisations) {
				if(selectedOrganisationKey.equals(organisation.getKey())) {
					userOrganisation = organisation;
					break;
				}
			}
		}
		
		Date expirationDate = expirationDateEl.getDate();
		String identityName = securityModule.isIdentityNameAutoGenerated() ? null : username;
		String provider = pwd == null ? null : BaseSecurityModule.getDefaultAuthProviderIdentifier();
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(identityName, username, null, newUser,
				provider, BaseSecurity.DEFAULT_ISSUER, null, username, pwd, userOrganisation, expirationDate, getIdentity());
	}
}