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

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *  Initial Date:  Jul 14, 2003
 * 
 *  @author gnaegi
 *  
 *  Comment:  
 *  Form for changing the password. It asks for the old and the new password
 *  
 */
public class ChangePasswordForm extends FormBasicController {

	private TextElement oldCredEl;
	private TextElement newCredEl;
	private TextElement newCredConfirmationEl;
	
	private String oldCred = "";
	private String newCred = "";
	
	private final SyntaxValidator syntaxValidator;
	private final Identity identityToChange;
	
	@Autowired
	private OLATAuthManager olatAuthManager;

	public ChangePasswordForm(UserRequest ureq, WindowControl wControl, Identity identityToChange) {
		super(ureq, wControl);
		this.identityToChange = identityToChange;
		this.syntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		initForm(ureq);
	}

	public String getOldPasswordValue() {
		return oldCred;
	}
	
	public String getNewPasswordValue() {
		return newCred;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String descriptions = formatDescriptionAsList(syntaxValidator.getAllDescriptions(), getLocale());
		setFormDescription("form.password.validation.rules", new String[] { descriptions });
		
		oldCredEl = uifactory.addPasswordElement("oldpass", "form.password.old", 5000, "", formLayout);
		oldCredEl.setElementCssClass("o_sel_home_pwd_old");
		oldCredEl.setNotEmptyCheck("form.please.enter.old");
		oldCredEl.setMandatory(true);
		
		newCredEl = uifactory.addPasswordElement("newpass1",  "form.password.new1", 5000, "", formLayout);
		newCredEl.setNotEmptyCheck("form.please.enter.new");
		newCredEl.setElementCssClass("o_sel_home_pwd_new_1");
		newCredEl.setAutocomplete("new-password");
		newCredEl.setMandatory(true);
		
		newCredConfirmationEl = uifactory.addPasswordElement("newpass2",  "form.password.new2", 5000, "", formLayout);
		newCredConfirmationEl.setNotEmptyCheck("form.please.enter.new");
		newCredConfirmationEl.setElementCssClass("o_sel_home_pwd_new_2");
		newCredConfirmationEl.setAutocomplete("new-password");
		newCredConfirmationEl.setMandatory(true);
		
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonLayout.setElementCssClass("o_sel_home_pwd_buttons");
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());	
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		newCredEl.clearError();
		String newPassword = newCredEl.getValue();
		ValidationResult validationResult = syntaxValidator.validate(newPassword, identityToChange);
		if (!validationResult.isValid()) {
			String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
			newCredEl.setErrorKey("error.password.invalid", new String[] { descriptions });
			allOk &= false;
		}

		newCredConfirmationEl.clearError();
		if (!newCredEl.getValue().equals(newCredConfirmationEl.getValue())) {
			newCredConfirmationEl.setErrorKey("error.password.nomatch", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		oldCred = oldCredEl.getValue(); 
		newCred = newCredEl.getValue();
		
		oldCredEl.setValue("");
		newCredEl.setValue("");
		newCredConfirmationEl.setValue("");
		
		fireEvent(ureq, Event.DONE_EVENT); 
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
}