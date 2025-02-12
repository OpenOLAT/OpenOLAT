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

import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jan 31, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PwChangeAuthStep00Controller extends StepFormBasicController {

	private final StepsRunContext runContext;
	private final RegistrationStepsListener registrationStepsListener;
	private final EmailOrUsernameFormController emailOrUsernameCtrl;

	@Autowired
	private SimpleMessageModule smsModule;
	@Autowired
	private UserModule userModule;

	public PwChangeAuthStep00Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
										String initialEmail, RegistrationStepsListener registrationStepsListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;
		this.registrationStepsListener = registrationStepsListener;
		this.emailOrUsernameCtrl = new EmailOrUsernameFormController(ureq, wControl, rootForm, initialEmail, runContext);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(emailOrUsernameCtrl.getInitialFormItem());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return emailOrUsernameCtrl.validateFormLogic(ureq);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		Identity identity = (Identity) runContext.get(PwChangeWizardConstants.IDENTITY);
		// specifically choosing the Boolean wrapper class to be able to handle three cases:
		// null, true and false - because we have three different outcomes depending on the user
		Boolean isSmsResetEnabled;
		if (!userModule.isPwdChangeAllowed(identity)) {
			isSmsResetEnabled = null;
		} else {
			isSmsResetEnabled = smsModule.isEnabled() && smsModule.isResetPasswordEnabled()
					&& StringHelper.containsNonWhitespace(identity.getUser().getProperty(UserConstants.SMSTELMOBILE, getLocale()));
		}
		registrationStepsListener.onStepsChanged(ureq, isSmsResetEnabled);

		runContext.put(PwChangeWizardConstants.EMAILORUSERNAME, emailOrUsernameCtrl.getEmailOrUsernameEl());
		fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
