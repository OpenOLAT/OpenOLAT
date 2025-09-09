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
package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.registration.MailValidationController;


/**
 * Initial date: Dez 10, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChangeMailController extends FormBasicController {

	private final String currentMail;
	private final Identity identityToModify;
	private final boolean isUserManager;

	private FormSubmit submitButton;

	private MailValidationController mailValidationCtrl;


	public ChangeMailController(UserRequest ureq, WindowControl wControl,
								String currentMail, Identity identityToModify, boolean isUserManager) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.currentMail = currentMail;
		this.identityToModify = identityToModify;
		this.isUserManager = isUserManager;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("mail_cont", getTranslator());
		formLayout.add(mailCont);
		uifactory.addStaticTextElement("change.mail.current", currentMail, mailCont);

		mailValidationCtrl = new MailValidationController(ureq, getWindowControl(), formLayout.getRootForm(),
				null, false, isUserManager, null);
		listenTo(mailValidationCtrl);
		formLayout.add(mailValidationCtrl.getInitialFormItem());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		submitButton = uifactory.addFormSubmitButton("confirm.email.in.process", buttonsCont);
		submitButton.setEnabled(isUserManager);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof MailValidationController
				&& event == Event.CHANGED_EVENT
				&& !isUserManager) {
			submitButton.setEnabled(mailValidationCtrl.isOtpSuccessful());
			if (mailValidationCtrl.getTemporaryKey() != null) {
				identityToModify.getUser().setProperty("emchangeKey", mailValidationCtrl.getTemporaryKey().getRegistrationKey());
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new ChangeMailEvent(ChangeMailEvent.CHANGED_EMAIL_EVENT, mailValidationCtrl.getEmailAddress()));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
