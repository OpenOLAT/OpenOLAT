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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  09.08.2004
 *
 * @author Mike Stock 
 * 
 */
public class ShibbolethMigrationForm extends FormBasicController {
	private static final Logger log = Tracing.createLoggerFor(ShibbolethMigrationForm.class);
	
	private final Authentication authentication;
	private TextElement login;
	private TextElement password;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	
	public ShibbolethMigrationForm(UserRequest ureq, WindowControl wControl, Authentication authentication) {
		super(ureq, wControl);
		this.authentication = authentication;
		initForm(ureq);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (!securityManager.checkCredentials(authentication, password.getValue())) {
			if (loginModule.registerFailedLoginAttempt(login.getValue())) {
				password.setErrorKey("smf.error.blocked", null);
				log.info(Tracing.M_AUDIT, "Too many failed login attempts for {}. Login blocked.", login.getValue());
			} else {
				password.setErrorKey("smf.error.password", null);
				log.info(Tracing.M_AUDIT, "Invalid password in ShibbolethMigration for login: {}", login.getValue());
			}
			return false;
		}
		return true;
	}
	
	/**
	 * @return Authentication
	 */
	protected Authentication getAuthentication() {
		return authentication;
	}

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
		
		login = uifactory.addTextElement("smf_login", "smf.login", 128, authentication.getAuthusername(), formLayout);
		login.setEnabled(false);
		
		password = uifactory.addPasswordElement("smf_password", "smf.password", 255, "", formLayout);
		password.setAutocomplete("new-password");
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());	
	}
	
}