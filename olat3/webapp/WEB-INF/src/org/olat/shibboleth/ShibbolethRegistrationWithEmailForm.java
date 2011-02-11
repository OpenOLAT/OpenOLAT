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
* <p>
*/ 

package org.olat.shibboleth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;

/**
 * Initial Date:  01.02.2006
 *
 * @author Alexander Schneider
 * 
 * Comment:  
 * 
 */

public class ShibbolethRegistrationWithEmailForm extends FormBasicController {

	private TextElement login;
	private TextElement eMail;
	private String userName;
	
	/**
	 * @param name
	 * @param translator
	 */
	public ShibbolethRegistrationWithEmailForm(UserRequest ureq, WindowControl wControl, String userName) {
		super(ureq, wControl);
		this.userName = userName; 
		initForm(ureq);
	}
	
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {

		if (eMail.isEmpty("srf.error.email.empty")) {
			return false;
		}

		if (!eMail.getValue().matches(".+@.+\\..+")) {
			eMail.setErrorKey("srf.error.email.valid", null);
			return false;
		}

		if (eMail.getValue().matches("^[^<>\"' \t\n\r]*$")) {
			eMail.setErrorKey("srf.error.email.badcharacters", null);
			return false;
		}

		// check if email is already used by another useraccount
		Identity foundIdentity = UserManager.getInstance().findIdentityByEmail(eMail.getValue()); 
		if (foundIdentity != null) {
			eMail.setErrorKey("srf.error.email.usedByOtherUser", null);
			return false;
		}

		return true; 
	}

	/**
	 * @return Login field.
	 */
	protected String getLogin() { return login.getValue(); }
	
	/**
	 * 
	 * @return E-mail field
	 */
	protected String getEmail() { return eMail.getValue(); }

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);	
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		login = uifactory.addTextElement("srf_login", "srf.login", 128, userName, formLayout);
		login.setEnabled (false);
		
		
		eMail = uifactory.addTextElement("srf_email", "srf.email", 128, "", formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());	
	}

	@Override
	protected void doDispose() {
		//
	}	
}
