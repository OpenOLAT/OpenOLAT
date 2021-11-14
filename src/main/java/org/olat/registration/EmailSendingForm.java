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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.mail.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  Simple form to get an email address.
 * 
 * @author Sabina Jeger
 */
public class EmailSendingForm extends FormBasicController {
	
	private TextElement mail;
	
	@Autowired
	private RegistrationManager registrationManager;
	
	public EmailSendingForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_registration_email_form");

		mail = uifactory.addTextElement("mail", "email.address", 255, "", formLayout);
		mail.setElementCssClass("o_sel_registration_email");
		mail.setMandatory(true);
		
		// Button layout
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("submit.cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit.speichernUndweiter", buttonLayout);
	}

	protected String getEmailAddress() {
		return mail.getValue().toLowerCase().trim();
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (mail.isEmpty("email.address.maynotbeempty")) {
			allOk &= false;
		} else if (!MailHelper.isValidEmailAddress(mail.getValue())) {
			mail.setErrorKey("email.address.notregular", null);
			allOk &= false;
		} else {
			String val = mail.getValue();
			
			boolean valid = registrationManager.validateEmailUsername(val);
			if(!valid) {
				mail.setErrorKey("form.mail.whitelist.error", null);
			}
			allOk &= valid;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
