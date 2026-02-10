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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class PwChangeForm extends FormBasicController {

	private TextElement newpass1;
	private TextElement newpass2; // confirm
	
	private final Identity identityToChange;
	private final SyntaxValidator syntaxValidator;

	@Autowired
	private OLATAuthManager olatAuthManager;
	
	public PwChangeForm(UserRequest ureq, WindowControl wControl, Identity identityToChange, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "change_pwd", rootForm);
		this.identityToChange = identityToChange;
		this.syntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("set.login.credentials");
		String descriptions = formatDescriptionAsList(syntaxValidator.getAllDescriptions(), getLocale());
		setFormDescription("form.password.rules", new String[] { descriptions });

		setFormInfo("step3.pw.text");

		newpass1 = uifactory.addPasswordElement("newpass1",  "form.password.new1", 5000, "", formLayout);
		newpass1.setElementCssClass("o_sel_new_password");
		newpass1.setAutocomplete("new-password");
		newpass2 = uifactory.addPasswordElement("newpass2",  "form.password.new2", 5000, "", formLayout);
		newpass2.setElementCssClass("o_sel_password_confirmation");
		newpass2.setAutocomplete("new-password");
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		newpass1.clearError();
		String newPassword = newpass1.getValue();
		ValidationResult validationResult = syntaxValidator.validate(newPassword, identityToChange);
		if (!validationResult.isValid()) {
			String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
			newpass1.setErrorKey("error.password.invalid", descriptions);
			allOk &= false;
		}
		
		// validate that both passwords are the same
		newpass2.clearError();
		if (!newpass1.getValue().equals(newpass2.getValue())) {
			newpass2.setErrorKey("form.password.error.nomatch");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	public String getNewpass1Value() {
		return newpass1.getValue();
	}
}