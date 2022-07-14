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

package org.olat.registration;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class RegistrationForm2 extends FormBasicController {
	public static final String USERPROPERTIES_FORM_IDENTIFIER = RegistrationForm2.class.getCanonicalName();
	
	private String languageKey;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	
	private SingleSelection lang;
	private TextElement usernameEl;
	private StaticTextElement usernameStatic;
	private TextElement newpass1;
	private TextElement newpass2; // confirm
	
	private final String proposedUsername;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final boolean userInUse;
	private final boolean usernameReadonly;
	private final SyntaxValidator passwordSyntaxValidator;
	private final SyntaxValidator usernameSyntaxValidator;

	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private RegistrationManager registrationManager;

	public RegistrationForm2(UserRequest ureq, WindowControl wControl, String languageKey, String proposedUsername,
			String firstName, String lastName, String email, boolean userInUse, boolean usernameReadonly) {
		super(ureq, wControl, "registration_form_2", Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.languageKey = languageKey;
		this.proposedUsername = proposedUsername;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.userInUse = userInUse;
		this.usernameReadonly = usernameReadonly;
		this.passwordSyntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		this.usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);

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
		} else if (usernameStatic != null) {
			return usernameStatic.getValue().trim();
		}
		return null;
	}
	
	private void setLogin(String login) {
		if(usernameEl != null) {
			usernameEl.setValue(login);
		} else if (usernameStatic != null) {
			usernameStatic.setValue(login);
		}
	}
	
	private void setLoginErrorKey(String errorKey) {
		if(usernameEl != null) {
			usernameEl.setErrorKey(errorKey, new String[0]);
		} else if (usernameStatic != null) {
			usernameStatic.setErrorKey(errorKey, new String[0]);
		}
	}
	
	protected String getPassword() {
		return newpass1.getValue().trim();
	}
	
	protected FormItem getPropFormItem(String k) {
		return propFormItems.get(k);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer accessCont = FormLayoutContainer.createDefaultFormLayout("access", getTranslator());
		accessCont.setFormTitle(translate("registration.form.login.data.title"));
		formLayout.add(accessCont);
		initLoginDataForm(accessCont);
		
		FormLayoutContainer userCont = FormLayoutContainer.createDefaultFormLayout("user", getTranslator());
		userCont.setFormTitle(translate("registration.form.personal.data.title"));
		formLayout.add(userCont);
		initUserDataForm(userCont, ureq);
	}
	
	private void initLoginDataForm(FormLayoutContainer formLayout) {
		if(usernameReadonly) {
			usernameStatic = uifactory.addStaticTextElement("username", "user.login", proposedUsername, formLayout);
			usernameStatic.setMandatory(true);
			if(proposedUsername != null && proposedUsername.equals(email)) {
				usernameStatic.setLabel("user.login.email", null);
			}
		} else {
			uifactory.addStaticTextElement("form.username.rules", null, translate("form.username.rules"), formLayout);
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
		
		String descriptions = formatDescriptionAsList(passwordSyntaxValidator.getAllDescriptions(), getLocale());
		descriptions = "<div class='o_desc'>" + translate("form.password.rules", descriptions) + "</div>";
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

	private void initUserDataForm(FormLayoutContainer formLayout, UserRequest ureq) {
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			FormItem fi = userPropertyHandler
					.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
			fi.setElementCssClass("o_sel_registration_" + userPropertyHandler.getName());
			propFormItems.put(userPropertyHandler.getName(), fi);
			
			if (UserConstants.EMAIL.equals(userPropertyHandler.getName()) && fi instanceof TextElement) {
				((TextElement)fi).setValue(email);
			} else if (UserConstants.FIRSTNAME.equals(userPropertyHandler.getName()) && fi instanceof TextElement) {
				((TextElement)fi).setValue(firstName);
			} else if (UserConstants.LASTNAME.equals(userPropertyHandler.getName()) && fi instanceof TextElement) {
				((TextElement)fi).setValue(lastName);
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
	
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit.speichernUndweiter", buttonLayout);	
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = propFormItems.get(userPropertyHandler.getName());
			if (fi.isEnabled() && !userPropertyHandler.isValid(null, fi, null)) {
				if (userPropertyHandler instanceof EmailProperty) {
					allOk &= registrationManager.isEmailReserved(getEmail());
				} else {
					allOk &= false;
				}
			}
		}
		
		// Transient identity for validations
		allOk &= validateUsername();
		
		if (newpass1.getValue().equals("")) {
			newpass1.setErrorKey("form.check4", null);
			allOk &= false;
		}
		
		if (newpass2.getValue().equals("")) {
			newpass2.setErrorKey("form.check4", null);
			allOk &= false;
		}
		
		allOk &= validatePassword();
		return allOk;
	}
	
	private boolean validateUsername() {
		boolean allOk = true;
		
		if(usernameEl != null) {
			String username = usernameEl.getValue();
			TransientIdentity newIdentity = new TransientIdentity();
			newIdentity.setName(username);
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
				newIdentity.setProperty(userPropertyHandler.getName(), userPropertyHandler.getStringValue(propertyItem));
			}
			
			// validate if username does match the syntactical login requirements
			usernameEl.clearError();
			if (!StringHelper.containsNonWhitespace(username)) {
				usernameEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else {
				ValidationResult validationResult = usernameSyntaxValidator.validate(username, newIdentity);
				if (!validationResult.isValid()) {
					String descriptions = validationResult.getInvalidDescriptions().get(0).getText(getLocale());
					usernameEl.setErrorKey("error.username.invalid", new String[] { descriptions });
					allOk &= false;
				}
			}
		}
	
		return allOk;
	}
	
	private boolean validatePassword() {
		boolean allOk = true;
		
		newpass1.clearError();
		newpass2.clearError();
		
		String username = usernameEl == null ? proposedUsername : usernameEl.getValue();
		TransientIdentity newIdentity = new TransientIdentity();
		newIdentity.setName(username);
		
		String newPassword = newpass1.getValue();
		ValidationResult validationResult = passwordSyntaxValidator.validate(newPassword, newIdentity);
		if (!validationResult.isValid()) {
			String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
			newpass1.setErrorKey("error.password.invalid", new String[] { descriptions });
			allOk &= false;
		} 
		if (!newpass1.getValue().equals(newpass2.getValue())) {
			newpass2.setErrorKey("form.check5", null);
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
}