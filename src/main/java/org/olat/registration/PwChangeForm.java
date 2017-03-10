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

import java.util.Collections;
import java.util.List;

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
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class PwChangeForm extends FormBasicController {

	private TextElement newpass1;
	private TextElement newpass2; // confirm
	
	private TemporaryKey tempKey;
	private Identity identityToChange;
	
	@Autowired
	private RegistrationManager rm;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	/**
	 * Password change form.
	 * @param name
	 */
	public PwChangeForm(UserRequest ureq, WindowControl wControl, Identity identityToChange, TemporaryKey tempKey) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		this.identityToChange = identityToChange;
		this.tempKey = tempKey;
		initForm(ureq);
	}
	
	/**
	 * Password change form.
	 * @param name
	 */
	public PwChangeForm(UserRequest ureq, WindowControl wControl, TemporaryKey tempKey) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		this.tempKey = tempKey;
		initForm(ureq);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		
		boolean newIsValid = userManager.syntaxCheckOlatPassword(newpass1.getValue());
		if (!newIsValid) {
			newpass1.setErrorKey("form.checkPassword", null);
		}
		// validate that both passwords are the same
		boolean newDoesMatch = newpass1.getValue().equals(newpass2.getValue());
		if (!newDoesMatch) {
			newpass2.setErrorKey("form.password.error.nomatch", null);
		}
		return newIsValid && newDoesMatch;
	}

	/**
	 * Saves the form data in the user object and the database
	 * 
	 * @param doer The current identity.
	 * @param s The identity to change the password.
	 */
	public boolean saveFormData(Identity s) {
		return olatAuthenticationSpi.changePasswordByPasswordForgottenLink(s, newpass1.getValue());	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(tempKey != null) {
			List<Identity> identToChanges = userManager.findIdentitiesByEmail(Collections.singletonList(tempKey.getEmailAddress()));
			if(identToChanges == null || identToChanges.size() == 0 || identToChanges.size() > 1) {
				showError("password.failed");
			} else {
				Identity identToChange = identToChanges.get(0);
				if(!saveFormData(identToChange)) {
					showError("password.failed");
				}
			}
		} else if(identityToChange != null) {
			if(!saveFormData(identityToChange)) {
				showError("password.failed");
			}
		}
		if(tempKey != null) {
			rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());	
		}
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.password.enter.new");
		newpass1 = uifactory.addPasswordElement("newpass1",  "form.password.new1", 128, "", formLayout);
		newpass2 = uifactory.addPasswordElement("newpass2",  "form.password.new2", 128, "", formLayout);
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}