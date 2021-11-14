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

package org.olat.admin.user;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Initial Date:  Jul 14, 2003
 * 
 *  @author gnaegi<br>
 *  Comment:  
 *  Form for changing a user password. 
 */
public class ChangeUserPasswordForm extends FormBasicController {
	
	private TextElement pass1;
	private TextElement pass2;
	
	private String cred = "";
	private final String authenticationUsername;

	private final SyntaxValidator syntaxValidator;
	private final Identity userIdentity;
	
	@Autowired
	private OLATAuthManager olatAuthManager;

	/**
	 * Constructor for user pwd forms.
	 * 
	 * @param UserRequest
	 * @param WindowControl
	 * @param Identity of which password is to be changed
	 */
	public ChangeUserPasswordForm(UserRequest ureq, WindowControl wControl, Identity treatedIdentity, String authenticationUsername) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		userIdentity = treatedIdentity;
		this.authenticationUsername = authenticationUsername;
		syntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		initForm(ureq);
	}
	
	@Override
	public boolean validateFormLogic (UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		pass1.clearError();
		String newPassword = pass1.getValue();
		ValidationResult validationResult = syntaxValidator.validate(newPassword, userIdentity);
		if (!validationResult.isValid()) {
			String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
			pass1.setErrorKey("error.password.invalid", new String[] { descriptions });
			allOk &= false;
		}

		pass2.clearError();
		if (!pass1.getValue().equals(pass2.getValue())) {
			pass2.setErrorKey("error.password.nomatch", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		cred = pass1.getValue();
		pass1.setValue("");
		pass2.setValue("");
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	protected String getNewPassword () {
		return cred;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.password.new1");

		String descriptions = formatDescriptionAsList(syntaxValidator.getAllDescriptions(), getLocale());
		setFormDescription("form.please.enter.new", new String[] { descriptions });
		
		TextElement username = uifactory.addTextElement("username", "form.username", 255, authenticationUsername, formLayout);
		username.setEnabled(false);		
		
		pass1 = uifactory.addPasswordElement("pass1", "form.password.new1", 255, "", formLayout);
		pass1.setAutocomplete("new-password");
		pass2 = uifactory.addPasswordElement("pass2", "form.password.new2", 255, "", formLayout);
		pass2.setAutocomplete("new-password");
		uifactory.addFormSubmitButton("submit", formLayout);
	}
}