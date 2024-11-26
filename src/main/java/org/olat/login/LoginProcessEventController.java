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
package org.olat.login;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Invitation;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.registration.RegisterFinishCallback;
import org.olat.registration.RegistrationAdditionalPersonalDataController;
import org.olat.registration.RegistrationLangStep00;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LoginProcessEventController extends BasicController {


	private final StackedPanel stackPanel;
	private final Invitation invitation;

	private StepsMainRunController registrationWizardCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private RegistrationModule registrationModule;

	public LoginProcessEventController(UserRequest ureq, WindowControl wControl, StackedPanel stackPanel, Invitation invitation) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.invitation = invitation;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == registrationWizardCtrl) {
			if (stackPanel instanceof BreadcrumbedStackedPanel breadcrumbedStackedPanel) {
				breadcrumbedStackedPanel.popController(registrationWizardCtrl);
			} else {
				stackPanel.popContent();
			}
		}
	}

	public void doOpenRegistration(UserRequest ureq) {
		boolean isAdditionalRegistrationFormEnabled = !userManager
				.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
		Step startReg = new RegistrationLangStep00(ureq, invitation, registrationModule.isDisclaimerEnabled(),
				registrationModule.isEmailValidationEnabled(), isAdditionalRegistrationFormEnabled);
		registrationWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), startReg, new RegisterFinishCallback(invitation, this),
				new CancelCallback(), translate("menu.register"), "o_sel_registration_start_wizard");
		listenTo(registrationWizardCtrl);
		if (stackPanel instanceof BreadcrumbedStackedPanel breadcrumbedStackedPanel) {
			breadcrumbedStackedPanel.pushController(translate("menu.register"), registrationWizardCtrl);
		} else {
			stackPanel.pushContent(registrationWizardCtrl.getInitialComponent());
		}
	}

	public void doLogin(UserRequest ureq, Identity persistedIdentity, String authProvider) {
		int loginStatus = AuthHelper.doLogin(persistedIdentity, authProvider, ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			// it's ok
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
		} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
			getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
		}
	}

	public void showError(String errorKey) {
		super.showError(errorKey);
	}

	private static class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return Step.NOSTEP;
		}
	}
}
