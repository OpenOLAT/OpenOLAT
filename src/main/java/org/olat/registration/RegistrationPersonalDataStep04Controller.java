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

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.User;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Okt 28, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationPersonalDataStep04Controller extends StepFormBasicController {


	private final StepsRunContext runContext;
	private final RegistrationPersonalDataController registrationPersonalDataCtrl;

	@Autowired
	private I18nModule i18nModule;

	public RegistrationPersonalDataStep04Controller(UserRequest ureq, WindowControl wControl,
													Form rootForm, StepsRunContext runContext,
													Invitation invitation) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;
		String proposedUsername = invitation != null ? invitation.getMail() : null;
		String firstName = invitation != null ? invitation.getFirstName() : null;
		String lastName = invitation != null ? invitation.getLastName() : null;
		String email = invitation != null ? invitation.getMail() : (String) runContext.get(RegWizardConstants.EMAIL);
		User user = invitation != null ? invitation.getIdentity().getUser() : null;
		boolean username = invitation == null;

		registrationPersonalDataCtrl = new RegistrationPersonalDataController(ureq, getWindowControl(), runContext, i18nModule.getLocaleKey(getLocale()),
				proposedUsername, firstName, lastName, email, user, false, username, rootForm);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(registrationPersonalDataCtrl.getInitialFormItem());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return registrationPersonalDataCtrl.validateFormLogic(ureq);
	}

	private void populateRunContext() {
		runContext.put(RegWizardConstants.FIRSTNAME, registrationPersonalDataCtrl.getFirstName());
		runContext.put(RegWizardConstants.LASTNAME, registrationPersonalDataCtrl.getLastName());
		runContext.put(RegWizardConstants.EMAIL, registrationPersonalDataCtrl.getEmail());
		runContext.put(RegWizardConstants.PASSWORD, registrationPersonalDataCtrl.getPassword());
		runContext.put(RegWizardConstants.USERNAME, registrationPersonalDataCtrl.getLogin());
		runContext.put(RegWizardConstants.PASSKEYS, registrationPersonalDataCtrl.getPasskeys());
		runContext.put(RegWizardConstants.PROPFORMITEMS, registrationPersonalDataCtrl.getPropFormItems());
		runContext.put(RegWizardConstants.SELECTEDORGANIZATIONKEY, registrationPersonalDataCtrl.getSelectedOrganisationKey());
	}

	@Override
	protected void formNext(UserRequest ureq) {
		populateRunContext();
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		populateRunContext();
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
