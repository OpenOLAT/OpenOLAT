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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
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

	private Identity userIdentity;
	
	@Autowired
	private UserManager userManager;

	/**
	 * Constructor for user pwd forms.
	 * 
	 * @param UserRequest
	 * @param WindowControl
	 * @param Identity of which password is to be changed
	 */
	public ChangeUserPasswordForm(UserRequest ureq, WindowControl wControl, Identity treatedIdentity) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		userIdentity = treatedIdentity;
		initForm(ureq);
	}
	
	@Override
	public boolean validateFormLogic (UserRequest ureq) {
		
		boolean newIsValid = userManager.syntaxCheckOlatPassword(pass1.getValue());
		if (!newIsValid) pass1.setErrorKey("form.checkPassword", null);
		
		boolean newDoesMatch = pass1.getValue().equals(pass2.getValue());
		if(!newDoesMatch) pass1.setErrorKey("error.password.nomatch", null);
			
		if (newIsValid && newDoesMatch) return true;
		
		pass1.setValue("");
		pass2.setValue("");
		
		return false;
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
		setFormDescription("form.please.enter.new");
		
		TextElement username = uifactory.addTextElement("username", "form.username", 255, userIdentity.getName(), formLayout);
		username.setEnabled(false);		
		
		pass1 = uifactory.addPasswordElement("pass1", "form.password.new1", 255, "", formLayout);
		pass1.setAutocomplete("new-password");
		pass2 = uifactory.addPasswordElement("pass2", "form.password.new2", 255, "", formLayout);
		pass2.setAutocomplete("new-password");
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}