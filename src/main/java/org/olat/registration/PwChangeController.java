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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.login.auth.OLATAuthManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controlls the change password workflow.
 * <p>
 *
 * @author Sabina Jeger
 */
public class PwChangeController extends BasicController {

	private final String initialEmail;

	private StepsMainRunController pwChangeWizardCtrl;

	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private RegistrationManager registrationManager;
	

	/**
	 * Controller to change a user's password.
	 *
	 * @param ureq
	 * @param wControl
	 * @param initialEmail
	 * @param isContentCtrl
	 */
	public PwChangeController(UserRequest ureq, WindowControl wControl, String initialEmail, boolean isContentCtrl) {
		super(ureq, wControl);
		this.initialEmail = initialEmail;

		// Always initialize the wizard
		pwChangeWizardCtrl = doOpenPasswordChange(ureq);

		if (isContentCtrl) {
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, pwChangeWizardCtrl.getInitialComponent(), null);
			listenTo(layoutCtr);
			putInitialPanel(layoutCtr.getInitialComponent());
		} else {
			putInitialPanel(pwChangeWizardCtrl.getInitialComponent());
		}
	}

	private StepsMainRunController doOpenPasswordChange(UserRequest ureq) {
		Step startPwChangeStep = new PwChangeAuthStep00(ureq, initialEmail);
		StepRunnerCallback finishCallback =
				(uureq, swControl, runContext) -> {
					Identity identityToModify = (Identity) runContext.get(PwChangeWizardConstants.IDENTITY);
					String pw = (String) runContext.get(PwChangeWizardConstants.PASSWORD);
					boolean saved = saveFormData(identityToModify, pw);
					if(!saved) {
						// check if an information has already been shown
						if(!Boolean.TRUE.equals(runContext.get(PwChangeWizardConstants.CANNOTCHANGE))) {
							showError("password.failed");
						}
						return StepsMainRunController.DONE_UNCHANGED;
					} else {
						String temporaryRegKey = (String) runContext.get(PwChangeWizardConstants.TEMPORARYREGKEY);
						// Now finally remove the temp key, pwd successfully saved
						if (temporaryRegKey != null) {
							registrationManager.deleteTemporaryKeyWithId(temporaryRegKey);
						}
					}
					showInfo("step4.pw.text");
					return StepsMainRunController.DONE_MODIFIED;
				};
		pwChangeWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), startPwChangeStep, finishCallback,
				new CancelCallback(), translate("pwchange.wizard.title"), "o_sel_pw_change_start_wizard");
		listenTo(pwChangeWizardCtrl);

		return pwChangeWizardCtrl;
	}

	/**
	 * Saves the form data in the user object and the database
	 *
	 * @param s The identity to change the password.
	 */
	private boolean saveFormData(Identity identity, String pw) {
		if(identity == null || !StringHelper.containsNonWhitespace(pw)) {
			return false;
		}
		return olatAuthManager.changePasswordByPasswordForgottenLink(identity, pw);
	}


	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == pwChangeWizardCtrl) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private static class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			// Do not remove temporaryKey entry, if process gets canceled. 
			// User might have a long lasting token that was created via REST to initiate the password reset workflow
			// because the user has no OpenOlat password yet. He might had some issues, e.g. did not receive the OTP
			// and might try it again within the time frame of the temp key validity period. 
			// The temp key will be removed automatically when expired. 
			return Step.NOSTEP;
		}
	}
}