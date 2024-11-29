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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 29, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationSupportFormStep03Controller extends StepFormBasicController {

	private final StepsRunContext runContext;

	private TextElement mailEl;
	private TextAreaElement mailBodyEl;

	@Autowired
	private MailManager mailManager;

	public RegistrationSupportFormStep03Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.runContext = runContext;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("step3.reg.support.form.title");
		setFormInfo("step3.reg.support.form.text");

		mailEl = uifactory.addTextElement("mail", "email.address", 255, "", formLayout);
		mailEl.setElementCssClass("o_sel_registration_email");
		mailEl.setMandatory(true);

		mailBodyEl = uifactory.addTextAreaElement("bodyElem", "step3.reg.support.mail.body", -1, 15, 60, true, false, "", formLayout);
		mailBodyEl.setMandatory(true);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateMail();

		mailBodyEl.clearError();
		if (!StringHelper.containsNonWhitespace(mailBodyEl.getValue())) {
			mailBodyEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		if (allOk && handleSupportRequest(getEmailAddress(), mailBodyEl.getValue())) {
			showInfo("email.sent");
		} else {
			allOk = false;
			showError("step3.reg.support.mail.failed");
		}

		return allOk;
	}

	private boolean validateMail() {
		boolean allOk = true;

		mailEl.clearError();
		if (mailEl.isEmpty("email.address.maynotbeempty")) {
			allOk = false;
		} else if (!MailHelper.isValidEmailAddress(getEmailAddress())) {
			mailEl.setErrorKey("email.address.notregular");
			allOk = false;
		}

		return allOk;
	}

	private String getEmailAddress() {
		return mailEl.getValue().toLowerCase().trim();
	}

	private boolean handleSupportRequest(String mail, String detailsMailBody) {
		boolean isMailSent = false;

		try {
			MailBundle bundle = new MailBundle();
			String supportMailAddress = WebappHelper.getMailConfig("mailSupport");
			bundle.setTo(supportMailAddress);
			// TODO: OO-8243: Set correct subject and maybe a body template?
			bundle.setContent(mail, detailsMailBody);
			boolean htmlBody = StringHelper.isHtml(detailsMailBody);
			MailerResult result = mailManager.sendExternMessage(bundle, null, htmlBody);
			if (result.isSuccessful()) {
				isMailSent = true;
			}
		} catch (Exception e) {
			// nothing to do, emailSent flag is false, errors will be reported to user
		}

		return isMailSent;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		runContext.put(RegWizardConstants.RECURRINGDETAILS, mailBodyEl.getValue());
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}


}
