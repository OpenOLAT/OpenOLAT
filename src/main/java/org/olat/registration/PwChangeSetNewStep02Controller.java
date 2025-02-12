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

import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.ui.NewPasskeyController;
import org.olat.login.webauthn.ui.RegistrationPasskeyListController;
import org.olat.user.ui.identity.UserOpenOlatAuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jan 31, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PwChangeSetNewStep02Controller extends StepFormBasicController {

	private final Identity recipientIdentity;

	private CloseableModalController cmc;
	private PwChangeForm pwChangeFormCtrl;
	private RegistrationPasskeyListController regPasskeyListCtrl;
	private NewPasskeyController newPasskeyCtrl;

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;

	public PwChangeSetNewStep02Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		this.recipientIdentity = (Identity) runContext.get(PwChangeWizardConstants.IDENTITY);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(true);

		Roles roles = securityManager.getRoles(recipientIdentity);
		PasskeyLevels requiredLevel = loginModule.getPasskeyLevel(roles);

		List<Authentication> authentications = securityManager.getAuthentications(recipientIdentity);
		PasskeyLevels currentLevel = PasskeyLevels.currentLevel(authentications);

		if (requiredLevel == PasskeyLevels.level1 || requiredLevel == PasskeyLevels.level3 || currentLevel == PasskeyLevels.level3) {
			pwChangeFormCtrl = new PwChangeForm(ureq, getWindowControl(), recipientIdentity, mainForm);
			listenTo(pwChangeFormCtrl);
			formLayout.add("pwf", pwChangeFormCtrl.getInitialFormItem());
		}

		if (requiredLevel == PasskeyLevels.level2 || requiredLevel == PasskeyLevels.level3) {
			regPasskeyListCtrl = new RegistrationPasskeyListController(ureq, getWindowControl(), recipientIdentity, mainForm);
			listenTo(regPasskeyListCtrl);
			formLayout.add("pkf", regPasskeyListCtrl.getInitialFormItem());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (regPasskeyListCtrl != null && !regPasskeyListCtrl.hasPasskeys()) {
			allOk = false;
		}

		return pwChangeFormCtrl.validateFormLogic(ureq) && allOk;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == newPasskeyCtrl) {
			if (event == Event.DONE_EVENT) {
				finishGeneratePasskey(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == regPasskeyListCtrl) {
			if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doGeneratePasskey(ureq, regPasskeyListCtrl.getIdentityToChange());
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		newPasskeyCtrl = null;
		cmc = null;
	}

	private void doGeneratePasskey(UserRequest ureq, Identity identityToChange) {
		String username = identityToChange.getUser().getNickName();

		if (!StringHelper.containsNonWhitespace(username)) {
			showWarning("warning.need.username");
		} else {
			newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), identityToChange, false, false, true);
			newPasskeyCtrl.setFormInfo(username, username);
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level2.hint"),
					UserOpenOlatAuthenticationController.HELP_URL);
			listenTo(newPasskeyCtrl);

			String title = translate("new.passkey.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void finishGeneratePasskey(UserRequest ureq) {
		Authentication authentication = newPasskeyCtrl.getPasskeyAuthentication();
		regPasskeyListCtrl.loadAuthentication(ureq, authentication);
		if (authentication != null && authentication.getKey() != null) {
			securityManager.persistAuthentications(newPasskeyCtrl.getIdentityToPasskey(), List.of(authentication));
		}
	}

	@Override
	protected void formNext(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
