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

package org.olat.shibboleth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
* Initial Date:  09.08.2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */

public class ShibbolethRegistrationForm extends FormBasicController {

	private TextElement login;
	private String proposedUsername;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	/**
	 * @param name
	 * @param translator
	 */

	public ShibbolethRegistrationForm(UserRequest ureq, WindowControl wControl, String proposedUsername) {
		super(ureq, wControl);
		this.proposedUsername = proposedUsername;
		initForm(ureq);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (login.isEmpty("srf.error.loginempty")) return false;
		if (!userManager.syntaxCheckOlatLogin(getLogin())) {
			login.setErrorKey("srf.error.loginregexp", null);
			return false;
		}
		if (userModule.isLoginOnBlacklist(getLogin())) {
			login.setErrorKey("srf.error.blacklist", null);
			return false;
		}
		return true;
	}

	/**
	 * @return Login field.
	 */
	protected String getLogin() { return login.getValue(); }

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String initialValue = proposedUsername == null ? "" : proposedUsername;
		login = uifactory.addTextElement("srf_login", "srf.login", 128, initialValue, formLayout);
		login.setExampleKey("srf.login.example", null);
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}	
}
