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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
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
	private List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	
	private SingleSelection lang;
	private TextElement username;
	private StaticTextElement usernameStatic;
	private TextElement newpass1;
	private TextElement newpass2; // confirm
	
	private final String proposedUsername;
	private final boolean userInUse;
	private final boolean usernameReadonly;

	@Autowired
	private UserModule userModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	/**
	 * @param name
	 * @param languageKey
	 */

	public RegistrationForm2(UserRequest ureq, WindowControl wControl, String languageKey, String proposedUsername, boolean userInUse, boolean usernameReadonly) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));

		this.languageKey = languageKey;
		this.proposedUsername = proposedUsername;
		this.userInUse = userInUse;
		this.usernameReadonly = usernameReadonly;

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
		FormItem fi = propFormItems.get("firstName");
		TextElement fn = (TextElement) fi;
		return fn.getValue().trim();
	}
	
	protected String getLastName() {
		FormItem fi = propFormItems.get("lastName");
		TextElement fn = (TextElement) fi;
		return fn.getValue().trim();
	}
	
	protected String getLogin() {
		if(username != null) {
			return username.getValue().trim();
		} else if (usernameStatic != null) {
			return usernameStatic.getValue().trim();
		}
		return null;
	}
	private void setLogin(String login) {
		if(username != null) {
			username.setValue(login);
		} else if (usernameStatic != null) {
			usernameStatic.setValue(login);
		}
	}
	private void setLoginErrorKey(String errorKey) {
		if(username != null) {
			username.setErrorKey(errorKey, new String[0]);
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
	/**
	 * Initialize the form
	 */

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.register");
		// first the configured user properties
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);
		
		Translator tr = Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator());
		
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			FormItem fi = userPropertyHandler
					.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
			fi.setTranslator(tr);
			propFormItems.put(userPropertyHandler.getName(), fi);
		}
		
		uifactory.addSpacerElement("lang", formLayout, true);
		// second the user language
		Map<String, String> languages = i18nManager.getEnabledLanguagesTranslated();
		lang = uifactory.addDropdownSingleselect("user.language", formLayout,
				StringHelper.getMapKeysAsStringArray(languages),
				StringHelper.getMapValuesAsStringArray(languages),
				null); 
		lang.select(languageKey, true);
		
		uifactory.addSpacerElement("loginstuff", formLayout, true);
		if(usernameReadonly) {
			usernameStatic = uifactory.addStaticTextElement("username", "user.login", proposedUsername, formLayout);
		} else {
			username = uifactory.addTextElement("username",  "user.login", 128, "", formLayout);
			username.setMandatory(true);
		}
		
		if(proposedUsername != null) {
			setLogin(proposedUsername);
		}
		if(userInUse) {
			setLoginErrorKey("form.check6");
		}
		
		newpass1 = uifactory.addPasswordElement("newpass1",  "form.password.new1", 128, "", formLayout);
		newpass1.setMandatory(true);
		newpass1.setAutocomplete("new-password");
		newpass2 = uifactory.addPasswordElement("newpass2",  "form.password.new2", 128, "", formLayout);
		newpass2.setMandatory(true);
		newpass2.setAutocomplete("new-password");
	
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit.speichernUndweiter", buttonLayout);
			
	}

	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = propFormItems.get(userPropertyHandler.getName());
			if (!userPropertyHandler.isValid(null, fi, null)) {
				return false;
			}
		}
		
		if (!UserManager.getInstance().syntaxCheckOlatLogin(getLogin())) {
			setLoginErrorKey("form.check3");
			return false;
		}
		
		Identity s = securityManager.findIdentityByName(getLogin());
		if (s != null || userModule.isLoginOnBlacklist(getLogin())) {
			setLoginErrorKey("form.check6");
			return false;
		}
		
		if (newpass1.getValue().equals("")) {
			newpass1.setErrorKey("form.check4", null);
			return false;
		}
		
		if (newpass2.getValue().equals("")) {
			newpass2.setErrorKey("form.check4", null);
			return false;
		}
		
		if (!UserManager.getInstance().syntaxCheckOlatPassword(newpass1.getValue())) {
			newpass1.setErrorKey("form.checkPassword", null);
			return false;
		}
		if (!newpass1.getValue().equals(newpass2.getValue())) {
			newpass2.setErrorKey("form.check5", null);
			// OO-92 : must return false, if passwords don't match!
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
}