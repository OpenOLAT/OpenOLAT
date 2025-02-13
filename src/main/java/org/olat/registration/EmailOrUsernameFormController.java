/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.registration;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Forgot password workflow: enter email or username address to retrieve the
 * password by email.
 * 
 * <P>
 * Initial Date: Sep 25, 2009 <br>
 * 
 * @author Gregor Wassmann, frentix GmbH, https://www.frentix.com
 */
public class EmailOrUsernameFormController extends FormBasicController {

	private final String initialEmail;//fxdiff FXOLAT-113: business path in DMZ
	private final StepsRunContext runContext;
	private TextElement emailOrUsernameEl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;


	public EmailOrUsernameFormController(UserRequest ureq, WindowControl wControl, Form rootForm,
										 String initialEmail, StepsRunContext runContext) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.initialEmail = initialEmail;
		this.runContext = runContext;

		runContext.put(PwChangeWizardConstants.INITIALMAIL, initialEmail);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("step1.pw.text");

		emailOrUsernameEl = uifactory.addTextElement("emailOrUsername", "email.or.username", 255, initialEmail, formLayout);
		emailOrUsernameEl.setElementCssClass("o_sel_pw_change");
		emailOrUsernameEl.setMandatory(true);
		emailOrUsernameEl.setFocus(true);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		emailOrUsernameEl.clearError();
		if (getEmailOrUsernameEl().isBlank()) {
			emailOrUsernameEl.setErrorKey("email.or.username.maynotbeempty");
			allOk = false;
		} else {
			String emailOrUsername = getEmailOrUsernameEl();
			Identity identity = findIdentityByUsernameOrEmail(emailOrUsername);

			if (identity == null) {
				emailOrUsernameEl.setErrorKey("email.or.username.not.identified");
				allOk = false;
			} else {
				runContext.put(PwChangeWizardConstants.IDENTITY, identity);
			}
		}

		return allOk;
	}

	private Identity findIdentityByUsernameOrEmail(String emailOrUsername) {
		// See if the entered value is the authusername of an authentication
		Identity identity = securityManager.findIdentityByLogin(emailOrUsername);
		if (identity == null) {
			// Try fallback with email, maybe user used his email address instead
			identity = userManager.findUniqueIdentityByEmail(emailOrUsername);
		}
		if (identity == null) {
			identity = securityManager.findIdentityByNickName(emailOrUsername);
		}
		return identity;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @return The email address or username entered by the user
	 */
	public String getEmailOrUsernameEl() {
		return emailOrUsernameEl.getValue().toLowerCase().trim();
	}

	public void setUserNotIdentifiedError() {
		//REVIEW:pb:2009-11-23:gw, setter should not be necessary. Is there a reason that it is not possible to 
		// check the business rule within the validateFormLogic(ureq)?

	}
}
