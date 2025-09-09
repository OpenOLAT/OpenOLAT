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

package org.olat.registration;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.ui.NewPasskeyController;
import org.olat.login.webauthn.ui.RegistrationPasskeyListController;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.identity.UserOpenOlatAuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class RegistrationPersonalDataController extends FormBasicController {

	public static final String USERPROPERTIES_FORM_IDENTIFIER = RegistrationPersonalDataController.class.getCanonicalName();
	
	private final String languageKey;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	private final StepsRunContext runContext;
	private final User invUser;

	private SingleSelection lang;
	private SingleSelection organisationSelection;
	private TextElement usernameEl;
	private TextElement newpass1;
	private TextElement newpass2; // confirm
	
	private final String proposedUsername;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final boolean userInUse;
	private final boolean username;
	private final PasskeyLevels requiredLevel;
	private final SyntaxValidator passwordSyntaxValidator;
	private final SyntaxValidator usernameSyntaxValidator;
	
	private CloseableModalController cmc;
	private NewPasskeyController newPasskeyCtrl;
	private RegistrationPasskeyListController passkeyListCtrl;

	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public RegistrationPersonalDataController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, String languageKey,
											  String proposedUsername, String firstName, String lastName,
											  String email, User invUser, boolean userInUse, boolean username, Form mainForm) {
		super(ureq, wControl, "registration_personal_data", Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		this.runContext = runContext;
		this.invUser = invUser;
		this.mainForm = mainForm;
		flc.setRootForm(mainForm);
		this.mainForm.addSubFormListener(this);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		wControl.getWindowBackOffice().getWindowManager().setAjaxEnabled(true);

		this.languageKey = languageKey;
		this.proposedUsername = proposedUsername;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.userInUse = userInUse;
		this.username = username;
		this.passwordSyntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		this.usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);
		requiredLevel = loginModule.isOlatProviderWithPasskey()
				? loginModule.getPasskeyLevel(OrganisationRoles.user)
				: PasskeyLevels.level1;

		initForm(ureq);
	}
	
	protected void freeze () {
		setFormTitle("step5.reg.yourdata", null);
		flc.setEnabled(false);
	}
	
	protected String getLangKey() {
		return lang.getSelectedKey();
	}
	
	protected String getFirstName() {
		FormItem fi = propFormItems.get(UserConstants.FIRSTNAME);
		TextElement fn = (TextElement) fi;
		return fn.getValue().trim();
	}
	
	protected String getLastName() {
		FormItem fi = propFormItems.get(UserConstants.LASTNAME);
		TextElement fn = (TextElement) fi;
		return fn.getValue().trim();
	}
	
	protected String getEmail() {
		FormItem fi = propFormItems.get(UserConstants.EMAIL);
		TextElement fn = (TextElement) fi;
		return fn.getValue().trim();
	}
	
	protected String getLogin() {
		if(usernameEl != null) {
			return usernameEl.getValue().trim();
		} else if (StringHelper.containsNonWhitespace(proposedUsername)) {
			return proposedUsername.trim();
		}
		return null;
	}
	
	private void setLogin(String login) {
		if(usernameEl != null) {
			usernameEl.setValue(login);
		}
	}
	
	private void setLoginErrorKey(String errorKey) {
		if(usernameEl != null) {
			usernameEl.setErrorKey(errorKey);
		}
	}
	
	protected String getPassword() {
		return newpass1 == null ? null : newpass1.getValue().trim();
	}
	
	protected List<Authentication> getPasskeys() {
		return passkeyListCtrl == null ? List.of() : passkeyListCtrl.getPasskeys();
	}
	
	protected FormItem getPropFormItem(String k) {
		return propFormItems.get(k);
	}

	public Map<String, FormItem> getPropFormItems() {
		return propFormItems;
	}

	public String getSelectedOrganisationKey() {
		if (organisationModule.isEnabled()
				&& organisationModule.isEmailDomainEnabled()
				&& organisationSelection != null
				&& organisationSelection.isOneSelected()) {
			return organisationSelection.getSelectedKey();
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer userCont = FormLayoutContainer.createDefaultFormLayout("user", getTranslator());
		userCont.setFormTitle(translate("registration.form.personal.data.title"));
		formLayout.add(userCont);
		initUserDataForm(userCont);

		FormLayoutContainer accessCont = FormLayoutContainer.createDefaultFormLayout("access", getTranslator());
		accessCont.setFormTitle(translate("registration.form.login.data.title"));
		formLayout.add(accessCont);
		initLoginDataForm(accessCont, ureq);
	}
	
	private void initLoginDataForm(FormLayoutContainer formLayout, UserRequest ureq) {
		if(username) {
			String usernameRules = "<div class='o_info_with_icon'>" + translate("form.username.rules") + "</div>";
			StaticTextElement hintEl = uifactory.addStaticTextElement("form.username.rules", null, usernameRules, formLayout);
			hintEl.setDomWrapperElement(DomWrapperElement.div);
			usernameEl = uifactory.addTextElement("username",  "user.login", 128, "", formLayout);
			usernameEl.setElementCssClass("o_sel_registration_login");
			usernameEl.setMandatory(true);
		}
		
		if(proposedUsername != null) {
			setLogin(proposedUsername);
		}
		if(userInUse) {
			setLoginErrorKey("form.check6");
		}
		
		if(requiredLevel == PasskeyLevels.level1 || requiredLevel == PasskeyLevels.level3) {
			String descriptions = formatDescriptionAsList(passwordSyntaxValidator.getAllDescriptions(), getLocale());
			descriptions = "<div class='o_info_with_icon'>" + translate("form.password.rules", descriptions) + "</div>";
			StaticTextElement hintEl = uifactory.addStaticTextElement("form.password.rules", null, descriptions, formLayout);
			hintEl.setDomWrapperElement(DomWrapperElement.div);
			newpass1 = uifactory.addPasswordElement("newpass1",  "form.password.new1", 5000, "", formLayout);
			newpass1.setElementCssClass("o_sel_registration_cred1");
			newpass1.setMandatory(true);
			newpass1.setAutocomplete("new-password");
			newpass2 = uifactory.addPasswordElement("newpass2",  "form.password.new2", 5000, "", formLayout);
			newpass2.setElementCssClass("o_sel_registration_cred2");
			newpass2.setMandatory(true);
			newpass2.setAutocomplete("new-password");
		}

		if(requiredLevel == PasskeyLevels.level2 || requiredLevel == PasskeyLevels.level3) {
			passkeyListCtrl = new RegistrationPasskeyListController(ureq, getWindowControl(), mainForm);
			listenTo(passkeyListCtrl);
			formLayout.add("passkeys", passkeyListCtrl.getInitialFormItem());
			if(usernameEl != null) {
				usernameEl.addActionListener(FormEvent.ONCHANGE);
			}
		}
	}

	private void initUserDataForm(FormLayoutContainer formLayout) {
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			FormItem fi = userPropertyHandler
					.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
			fi.setElementCssClass("o_sel_registration_" + userPropertyHandler.getName());
			propFormItems.put(userPropertyHandler.getName(), fi);
			
			if (UserConstants.EMAIL.equals(userPropertyHandler.getName()) && fi instanceof TextElement ft) {
				ft.setValue(email);
			} else if (UserConstants.FIRSTNAME.equals(userPropertyHandler.getName()) && fi instanceof TextElement ft) {
				ft.setValue(firstName);
			} else if (UserConstants.LASTNAME.equals(userPropertyHandler.getName()) && fi instanceof TextElement ft) {
				ft.setValue(lastName);
			}
		}
		
		// second the user language
		Map<String, String> languages = i18nManager.getEnabledLanguagesTranslated();
		String[] languageKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] languageValues = StringHelper.getMapValuesAsStringArray(languages);
		lang = uifactory.addDropdownSingleselect("user.language", formLayout, languageKeys, languageValues, null);
		if(languages.containsKey(languageKey)) {
			lang.select(languageKey, true);
		} else if(languageKeys.length > 0) {
			lang.select(languageKeys[0], true);
		}
		// hide language in UI if only one key is available anyway 
		if (languageKeys.length == 1) {	
			lang.setVisible(false);
		}

		if (organisationModule.isEnabled() && organisationModule.isEmailDomainEnabled()) {

			// at this stage, matchedDomains can not be empty, see in MailValidationCtrl
			@SuppressWarnings("unchecked")
			List<OrganisationEmailDomain> matchedDomains = (List<OrganisationEmailDomain>) runContext.get(RegWizardConstants.MAILDOMAINS);

			// can occur, for invited users
			if (matchedDomains == null) {
				OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
				searchParams.setEnabled(true);
				List<OrganisationEmailDomain> emailDomains = organisationService.getEmailDomains(searchParams);
				matchedDomains = organisationService.getMatchingEmailDomains(emailDomains, MailHelper.getMailDomain(email));
			}

			// Extract orgKey as keys
			matchedDomains = matchedDomains.stream().sorted(Comparator.comparing(domain -> domain.getOrganisation().getDisplayName())).toList();
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

			organisationSelection = uifactory.addDropdownSingleselect("user.organisation", formLayout, orgKeys, orgValues, null);
			if (matchedDomains.size() == 1) {
				organisationSelection.select(orgKeys[0], true);
				organisationSelection.setEnabled(false);
			} else {
				organisationSelection.enableNoneSelection(translate("user.organisation.select"));
				organisationSelection.setMandatory(true);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);


		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = propFormItems.get(userPropertyHandler.getName());
			if (fi.isEnabled() ) {
				if(fi instanceof TextElement textEl && !validateElement(textEl)
						|| !userPropertyHandler.isValid(invUser, fi, null)) {
					allOk = false;
				}
			}
		}
		
		// Transient identity for validations
		allOk &= validateUsername();

		if(newpass1 != null) {
			newpass1.clearError();
			newpass2.clearError();
			
			if(!StringHelper.containsNonWhitespace(newpass1.getValue())) {
				newpass1.setErrorKey("form.check4");
				allOk = false;
			}
			
			if (!StringHelper.containsNonWhitespace(newpass2.getValue())) {
				newpass2.setErrorKey("form.check4");
				allOk = false;
			}
		}
		
		allOk &= validatePassword();
		allOk &= validatePasskeys();

		if (organisationSelection != null && !organisationSelection.isOneSelected()) {
			organisationSelection.clearError();
			organisationSelection.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		return allOk;
	}
	
	private boolean validatePasskeys() {
		boolean allOk = true;
		
		if(passkeyListCtrl != null) {
			passkeyListCtrl.getInitialFormItem().clearError();
			if((requiredLevel == PasskeyLevels.level2 || requiredLevel == PasskeyLevels.level3) && !passkeyListCtrl.hasPasskeys()) {
				passkeyListCtrl.getInitialFormItem().setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}
	
	private boolean validateUsername() {
		boolean allOk = true;
		
		if(usernameEl != null) {
			String username = usernameEl.getValue();
			TransientIdentity newIdentity = new TransientIdentity();
			newIdentity.setName(username);
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				FormItem propertyItem = propFormItems.get(userPropertyHandler.getName());
				newIdentity.setProperty(userPropertyHandler.getName(), userPropertyHandler.getStringValue(propertyItem));
			}
			
			// validate if username does match the syntactical login requirements
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
				} else if(!validateElement(usernameEl)) {
					allOk &= false;
				}
			}
		}
	
		return allOk;
	}
	
	private boolean validatePassword() {
		boolean allOk = true;
		
		if(newpass1 != null) {
			newpass1.clearError();
			newpass2.clearError();
			
			String username = usernameEl == null ? proposedUsername : usernameEl.getValue();
			TransientIdentity newIdentity = new TransientIdentity();
			newIdentity.setName(username);
			
			String newPassword = newpass1.getValue();
			ValidationResult validationResult = passwordSyntaxValidator.validate(newPassword, newIdentity);
			if (!validationResult.isValid()) {
				String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
				newpass1.setErrorKey("error.password.invalid", descriptions);
				allOk &= false;
			} 
			if (!newpass1.getValue().equals(newpass2.getValue())) {
				newpass2.setErrorKey("form.check5");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(passkeyListCtrl == source) {
			if(event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doGeneratePasskey(ureq);
			}
		} else if(newPasskeyCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Authentication authentication = newPasskeyCtrl.getPasskeyAuthentication();
				passkeyListCtrl.loadAuthentication(ureq, authentication);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		newPasskeyCtrl = null;
		cmc = null;
	}

	protected static boolean validateElement(TextElement el) {
		boolean allOk = true;

		String value = el.getValue();
		if(value.contains("<") || value.contains(">") || value.contains("#")) {
			el.setErrorKey("error.invalid.character");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doGeneratePasskey(UserRequest ureq) {
		String username;
		if(usernameEl != null) {
			username = usernameEl.getValue();
		} else {
			username = proposedUsername;
		}
		
		if(!StringHelper.containsNonWhitespace(username)) {
			showWarning("warning.need.username");
		} else {
			newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), username, false, false, true);
			newPasskeyCtrl.setFormInfo(username, username);
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level2.hint"),
						UserOpenOlatAuthenticationController.HELP_URL);
			listenTo(newPasskeyCtrl);
			
			String title = translate("new.passkey.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
}