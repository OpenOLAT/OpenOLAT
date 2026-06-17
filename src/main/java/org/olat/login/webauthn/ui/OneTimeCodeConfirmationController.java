/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.login.webauthn.ui;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.crypto.PasswordGenerator;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.OLATAuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The one time code is "In memory" only.
 * 
 * Initial date: 8 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OneTimeCodeConfirmationController extends FormBasicController {
	
	private TextElement otpEl;
	private FormLink resendEmailLink;
	
	private String otp;
	private final String email;
	
	@Autowired
	private MailManager mailManager;
	
	public OneTimeCodeConfirmationController(UserRequest ureq, WindowControl wControl, String email) {
		super(ureq, wControl, "onetimecode", Util.createPackageTranslator(OLATAuthenticationController.class, ureq.getLocale()));
		this.email = email;
		otp = doSendOneTimeCode();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("msg", translate("lf.otp.hint", "8"));
		}

		otpEl = uifactory.addTextElement("validation.code", "lf.otp.label", 8, "", formLayout);
		otpEl.setElementCssClass("o_sel_registration_otp");
		otpEl.setAutocomplete("one-time-code");
		otpEl.setOneTimePassword(true);
		
		resendEmailLink = uifactory.addFormLink("lf.otp.resend", formLayout);
		uifactory.addFormSubmitButton("lf.otp.login", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		otpEl.clearError();
		if(!StringHelper.containsNonWhitespace(otpEl.getValue())) {
			otpEl.setErrorKey("form.legende.mandatory");
			allOk &= false;	
		} else if(!otp.equals(otpEl.getValue())) {
			otpEl.setErrorKey("lf.error.otp.invalid");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == otpEl) {
			if(validateFormLogic(ureq)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(source == resendEmailLink) {
			otp = doSendOneTimeCode();
			showInfo("email.sent");
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(otp.equals(otpEl.getValue())) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}
	
	private String doSendOneTimeCode() {
		String code = PasswordGenerator.generateNumericalCode(8);
		try {
			String toolName = Util.createPackageTranslator(BaseChiefController.class, getLocale())
					.translate("topnav.home");
			String url = Settings.getServerContextPathURI();
			String[] args = new String[] {
				toolName,
				url,
				code
			};
			String subject = translate("lf.otp.mail.subject", args);
			String body = translate("lf.otp.mail.body", args);
			
			MailBundle bundle = new MailBundle();
			bundle.setTo(email);
			bundle.setContent(subject, body);
			MailerResult result = mailManager.sendExternMessage(bundle, null, true);
			if(!result.isSuccessful()) {
				logError("Cannot send OTP mail for login.", null);
			}
		} catch (Exception e) {
			logError("", e);
		}
		return code;
	}
}
