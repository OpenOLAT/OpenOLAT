/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* https://www.apache.org/licenses/LICENSE-2.0
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
* <a href="https://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.registration;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 05, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MailValidationController extends FormBasicController {

	private static final String SEPARATOR = "____________________________________________________________________\n";

	private FormLink validateMailLink;
	private FormLink resendOtpLink;
	private FormLink changeMailLink;
	private TextElement mailEl;
	private TextElement otpEl;
	private StaticTextElement codeNotReceivedStaticText;

	private TemporaryKey temporaryKey;

	private FormLayoutContainer validationCont;
	private FormLayoutContainer codeNotReceivedCont;
	
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private UserModule userModule;
	@Autowired
	private MailManager mailManager;
	
	public MailValidationController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("reg.title");
		setFormInfo("reg.desc");
		formLayout.setElementCssClass("o_sel_registration_email_form");

		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("mail_cont", getTranslator());
		formLayout.add(mailCont);
		mailEl = uifactory.addTextElement("mail", "email.address", 255, "", mailCont);
		mailEl.setMandatory(true);

		validateMailLink = uifactory.addFormLink("submit.validate", mailCont, Link.BUTTON);
		validateMailLink.setPrimary(true);
	}

	private void initValidation() {
		if (validationCont == null) {
			validationCont = FormLayoutContainer.createVerticalFormLayout("validation_cont", getTranslator());
			validationCont.setFormTitle(translate("validation.title"));
			validationCont.setFormInfo(translate("validation.desc"));
			flc.add(validationCont);
			otpEl = uifactory.addTextElement("validation.code", "reg.otp.label", 8, "", validationCont);
			otpEl.setElementCssClass("o_sel_registration_otp");
			otpEl.setAutocomplete("one-time-code");
			otpEl.setOneTimePassword(true);
			validationCont.setVisible(false);
		}

		if (codeNotReceivedCont == null) {
			codeNotReceivedStaticText = uifactory.addStaticTextElement("reg.otp.not.received", null, flc);
			codeNotReceivedStaticText.showLabel(true);
			codeNotReceivedStaticText.setElementCssClass("o_sel_registration_code_not_received");
			codeNotReceivedCont = FormLayoutContainer.createHorizontalFormLayout("code_not_received_cont", getTranslator());
			flc.add(codeNotReceivedCont);
			codeNotReceivedCont.setElementCssClass("o_sel_registration_code_not_received");
			resendOtpLink = uifactory.addFormLink("reg.otp.resend", codeNotReceivedCont, Link.LINK);
			StaticTextElement orStaticText = uifactory.addStaticTextElement("reg.otp.or", translate("reg.otp.or"), codeNotReceivedCont);
			orStaticText.showLabel(false);
			changeMailLink = uifactory.addFormLink("reg.change.mail", codeNotReceivedCont, Link.LINK);

			codeNotReceivedStaticText.setVisible(false);
			codeNotReceivedCont.setVisible(false);
		}
	}

	private void toggleFormVisibility() {
		validationCont.setVisible(!validationCont.isVisible());
		codeNotReceivedCont.setVisible(!codeNotReceivedCont.isVisible());
		codeNotReceivedStaticText.setVisible(!codeNotReceivedStaticText.isVisible());
		validateMailLink.setVisible(!validationCont.isVisible());
		mailEl.setEnabled(!validationCont.isVisible());
	}

	protected String getEmailAddress() {
		return mailEl.getValue().toLowerCase().trim();
	}

	public TemporaryKey getTemporaryKey() {
		return temporaryKey;
	}

	private void processEmail(UserRequest ureq) {
		// Email requested for tempkey
		String email = getEmailAddress();
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String serverPath = Settings.getServerContextPathURI();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		String[] whereFromAttrs = new String[]{ serverPath, today };

		if (isEmailEligibleForRegistration(email)) {
			temporaryKey = loadOrCreateTemporaryKey(ureq, email, ip);
			sendRegistrationEmail(ureq, email, serverPath, temporaryKey, whereFromAttrs);
		} else {
			// if users with this email address exists, they are informed.
			informExistingUser(email, whereFromAttrs);
		}
	}

	private boolean isEmailEligibleForRegistration(String email) {
		return registrationManager.isRegistrationPending(email) || userManager.isEmailAllowed(email);
	}

	private TemporaryKey loadOrCreateTemporaryKey(UserRequest ureq, String email, String ip) {
		TemporaryKey tk = null;
		if (userModule.isEmailUnique()) {
			tk = registrationManager.loadTemporaryKeyByEmail(email);
			// if temporaryKey already exists, then update otp
			resendNewOtp(ureq);
		}
		if (tk == null) {
			tk = registrationManager.loadOrCreateTemporaryKeyByEmail(
					email, ip, RegistrationManager.REGISTRATION, registrationModule.getValidUntilMinutesGui()
			);
		}
		return tk;
	}

	private void sendRegistrationEmail(UserRequest ureq, String email, String serverPath, TemporaryKey tk, String[] whereFromAttrs) {
		String link = buildRegistrationLink(ureq, serverPath, tk);
		String[] bodyAttrs = new String[]{
				tk.getRegistrationKey(), //0
				"<a href=\"" + link + "\">" + link + "</a>" //1
		};
		String body = buildEmailBody(bodyAttrs, whereFromAttrs);

		if (sendMessage(email, translate("reg.subject"), body)) {
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}

	private String buildRegistrationLink(UserRequest ureq, String serverPath, TemporaryKey tk) {
		return serverPath + DispatcherModule.getPathDefault() + "registration/index.html?key="
				+ tk.getRegistrationKey() + "&language=" + i18nModule.getLocaleKey(ureq.getLocale());
	}

	private String buildEmailBody(String[] bodyAttrs, String[] whereFromAttrs) {
		String body = translate("reg.body", bodyAttrs);
		boolean htmlBody = StringHelper.isHtml(body);
		if (!htmlBody) {
			body += SEPARATOR + translate("reg.wherefrom", whereFromAttrs);
		}
		return body;
	}

	private void informExistingUser(String email, String[] whereFromAttrs) {
		List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(email));
		for (Identity identity : identities) {
			String subject = translate("login.subject");
			String username = resolveUsername(identity);
			String body = translate("login.body", username) + SEPARATOR + translate("reg.wherefrom", whereFromAttrs);
			sendMessage(email, subject, body);
		}
	}

	private String resolveUsername(Identity identity) {
		return StringHelper.containsNonWhitespace(identity.getUser().getNickName())
				? identity.getUser().getNickName()
				: identity.getName();
	}

	private boolean sendMessage(String email, String subject, String body) {
		boolean isMailSent = false;

		try {
			MailBundle bundle = new MailBundle();
			bundle.setTo(email);
			bundle.setContent(subject, body);
			boolean htmlBody = StringHelper.isHtml(body);
			MailerResult result = mailManager.sendExternMessage(bundle, null, htmlBody);
			if (result.isSuccessful()) {
				isMailSent = true;
			}
		} catch (Exception e) {
			// nothing to do, emailSent flag is false, errors will be reported to user
		}

		return isMailSent;
	}

	private boolean isOtpValid() {
		TemporaryKey otpToken = registrationManager.loadTemporaryKeyByEmail(getEmailAddress());
		return otpToken != null && otpToken.getRegistrationKey().equals(otpEl.getValue());
	}

	private void resendNewOtp(UserRequest ureq) {
		temporaryKey = registrationManager.updateTemporaryRegistrationKey(getEmailAddress());
		String serverPath = Settings.getServerContextPathURI();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		String[] whereFromAttrs = new String[]{ serverPath, today };

		if (temporaryKey != null) {
			sendRegistrationEmail(ureq, getEmailAddress(), "", temporaryKey, whereFromAttrs);
		}
		otpEl.reset();
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (mailEl.isEmpty("email.address.maynotbeempty")) {
			allOk &= false;
		} else if (!MailHelper.isValidEmailAddress(getEmailAddress())) {
			mailEl.setErrorKey("email.address.notregular");
			allOk &= false;
		} else {
			String val = getEmailAddress();
			
			boolean valid = registrationManager.validateEmailUsername(val);
			if(!valid) {
				mailEl.setErrorKey("form.mail.whitelist.error");
			}
			allOk &= valid;
		}

		if (validationCont != null && validationCont.isVisible() && otpEl != null) {
			if (otpEl.isEmpty("reg.otp.may.not.be.empty")) {
				allOk &= false;
			} else if (!isOtpValid()) {
				otpEl.setErrorKey("reg.otp.invalid");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == validateMailLink && validateFormLogic(ureq)) {
			initValidation();
			processEmail(ureq);
			toggleFormVisibility();
		} else if (source == otpEl) {
			otpEl.clearSuccess();
			otpEl.clearError();
			if (isOtpValid()) {
				otpEl.setSuccessKey("reg.otp.valid");
			} else {
				otpEl.setErrorKey("reg.otp.invalid");
			}
		} else if (source == resendOtpLink) {
			resendNewOtp(ureq);
		} else if (source == changeMailLink) {
			toggleFormVisibility();
			otpEl.reset();
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
