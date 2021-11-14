/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
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

/**
 * Forgot password workflow: enter email or username address to retrieve the
 * password by email.
 * 
 * <P>
 * Initial Date: Sep 25, 2009 <br>
 * 
 * @author Gregor Wassmann, frentix GmbH, http://www.frentix.com
 */
public class EmailOrUsernameFormController extends FormBasicController {

	private final String initialEmail;//fxdiff FXOLAT-113: business path in DMZ
	private TextElement emailOrUsername;

	public EmailOrUsernameFormController(UserRequest ureq, WindowControl wControl, String initialEmail) {
		super(ureq, wControl);
		this.initialEmail = initialEmail;
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emailOrUsername = uifactory.addTextElement("emailOrUsername", "email.or.username", -1, initialEmail, formLayout);
		emailOrUsername.setMandatory(true);
		emailOrUsername.setNotEmptyCheck("email.or.username.maynotbeempty");
		emailOrUsername.setFocus(true);
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		flc.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	/**
	 * @return The email address or username entered by the user
	 */
	public String getEmailOrUsername() {
		return emailOrUsername.getValue().toLowerCase();
	}

	public void setUserNotIdentifiedError() {
		//REVIEW:pb:2009-11-23:gw, setter should not be necessary. Is there a reason that it is not possible to 
		// check the business rule within the validateFormLogic(ureq)?
		emailOrUsername.setErrorKey("email.or.username.not.identified", null);
	}
}
