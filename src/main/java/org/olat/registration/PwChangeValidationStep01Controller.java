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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jan 31, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PwChangeValidationStep01Controller extends StepFormBasicController {

	private static final String SEPARATOR = "____________________________________________________________________\n";

	private String sentToken;
	private final String selectedValidationType;
	private final String initialEmail;
	private final String recipientEmail;

	private final Identity recipientIdentity;
	private TemporaryKey temporaryKey;

	private FormLayoutContainer infoBoxContainer;
	private FormLayoutContainer otpContainer;
	private TextElement otpEl;
	private FormLink resendOtpLink;

	private boolean done = false;

	private enum State { initial, valid, invalid, newCode }
	private State state = State.initial;

	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private SimpleMessageService messageService;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private UserManager userManager;

	public PwChangeValidationStep01Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		this.selectedValidationType =
				runContext.get(PwChangeWizardConstants.VALTYPE) != null
						? (String) runContext.get(PwChangeWizardConstants.VALTYPE)
						: PwChangeVSelectionStep01Controller.PW_CHANGE_VAL_TYPE_MAIL;
		this.recipientIdentity = (Identity) runContext.get(PwChangeWizardConstants.IDENTITY);
		this.recipientEmail = recipientIdentity.getUser().getEmail();
		initialEmail = (String) runContext.get(PwChangeWizardConstants.INITIALMAIL);
		sendValidationToken(ureq);

		initForm(ureq);
		state = State.initial;
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("validation.title");
		setFormStyle("o_pw_otp_validation");

		String infoBoxPage = velocity_root + "/info_box.html";
		infoBoxContainer = FormLayoutContainer.createCustomFormLayout("info.box", getTranslator(), infoBoxPage);
		formLayout.add(infoBoxContainer);

		StaticTextElement enterValidation = uifactory.addStaticTextElement("enter.validation", translate("validation.enter"), formLayout);
		enterValidation.showLabel(false);

		String otpPage = velocity_root + "/otp_box.html";
		otpContainer = FormLayoutContainer.createCustomFormLayout("otp.cont", getTranslator(), otpPage);
		formLayout.add(otpContainer);
		otpContainer.setMandatory(true);
		otpContainer.setLabel("reg.otp.label", null);
		
		otpEl = uifactory.addTextElement("validation.code", null, 8, "", otpContainer);
		otpEl.setElementCssClass("o_sel_registration_otp");
		otpEl.setDomReplacementWrapperRequired(false);
		otpEl.setAutocomplete("one-time-code");
		otpEl.setOneTimePassword(true);

		uifactory.addStaticTextElement("validation.not.received", null, translate("validation.not.received"), formLayout);
		
		resendOtpLink = uifactory.addFormLink("pwchange.otp.resend", formLayout, Link.BUTTON);
	}
	
	private void updateUI() {
		flc.setElementCssClass("");
		resendOtpLink.setEnabled(true);
		otpEl.setElementCssClass("o_sel_registration_otp");
		switch (state) {
			case initial -> {
				infoBoxContainer.getFormItemComponent().contextPut("text", translate("validation.message.initial"));
				infoBoxContainer.getFormItemComponent().contextPut("cssClass", "o_assistance_message o_info_with_icon");
				flc.setElementCssClass("o_validation o_validation_initial");
			}
			case valid -> {
				infoBoxContainer.getFormItemComponent().contextPut("text", translate("validation.message.valid"));
				infoBoxContainer.getFormItemComponent().contextPut("cssClass", "o_assistance_message o_success_with_icon");
				flc.setElementCssClass("o_validation o_validation_valid");
				resendOtpLink.setEnabled(false);
			}
			case invalid -> {
				infoBoxContainer.getFormItemComponent().contextPut("text", translate("validation.message.invalid"));
				infoBoxContainer.getFormItemComponent().contextPut("cssClass", "o_assistance_message o_error_with_icon");
				flc.setElementCssClass("o_validation o_validation_invalid");
				otpEl.setElementCssClass("o_sel_registration_otp o_invalid");
			}
			case newCode -> {
				infoBoxContainer.getFormItemComponent().contextPut("text", translate("validation.message.newCode"));
				infoBoxContainer.getFormItemComponent().contextPut("cssClass", "o_assistance_message o_success_with_icon");
				flc.setElementCssClass("o_validation o_validation_new_code");
			}
		}
	}

	private void sendValidationToken(UserRequest ureq) {
		if (selectedValidationType.equals(PwChangeVSelectionStep01Controller.PW_CHANGE_VAL_TYPE_MAIL)) {
			// for security reason, don't show an error, simply proceed without sending a mail
			if (StringHelper.containsNonWhitespace(recipientEmail)) {
				processEmail(ureq);
			}
		} else if (selectedValidationType.equals(PwChangeVSelectionStep01Controller.PW_CHANGE_VAL_TYPE_SMS)) {
			processSms();
		}
	}

	private void processSms() {
		try {
			sentToken = messageService.generateToken();
			String msg = translate("sms.token", sentToken);
			messageService.sendMessage(msg, recipientIdentity);
		} catch (SimpleMessageException e) {
			showWarning("warning.message.not.send");
		}
	}

	private void processEmail(UserRequest ureq) {
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String serverPath = Settings.getServerContextPathURI();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		String[] whereFromAttrs = new String[]{ serverPath, today };

		createTemporaryKey(ip, whereFromAttrs);
	}

	private void createTemporaryKey(String ip, String[] whereFromAttrs) {
		Integer validityPeriod = null;
		if (StringHelper.containsNonWhitespace(initialEmail)) {
			validityPeriod = loginModule.getValidUntilMinutesGui();
		}
		temporaryKey = registrationManager.loadOrCreateTemporaryKeyByEmail(
				recipientEmail, ip, RegistrationManager.PW_CHANGE, validityPeriod
		);
		sendPasswordChangeEmail(whereFromAttrs);
	}

	private void sendPasswordChangeEmail(String[] whereFromAttrs) {
		addToRunContext(PwChangeWizardConstants.TEMPORARYREGKEY, temporaryKey.getRegistrationKey());

		Locale locale = getUserLocale(recipientIdentity);
		Translator userTrans = Util.createPackageTranslator(PwChangeController.class, locale);

		String userDisplayName = userManager.getUserDisplayName(recipientIdentity);
		String userName = getAuthenticationNameOrDefault(recipientIdentity);

		String body;
		String subject;
		StringBuilder i18nBody = new StringBuilder(2048);
		i18nBody.append("<p>")
				.append(userTrans.translate("pwchange.intro.greeting.with.name", userDisplayName))
				.append("</p>");

		if (hasPasskeyAuthentication(recipientIdentity)) {
			subject = userTrans.translate("pwchange.subject.passkey");
			i18nBody.append(userTrans.translate("pwchange.intro.passkey", userName))
					.append(userTrans.translate("pwchange.body.passkey"));
		} else {
			subject = userTrans.translate("pwchange.subject");
			i18nBody.append(userTrans.translate("pwchange.intro", userName))
					.append(userTrans.translate("pwchange.body", temporaryKey.getRegistrationKey()));
		}

		body = buildEmailBody(i18nBody.toString(), whereFromAttrs);

		// nothing to do if it was successful. showInfo is not supported with wizards
		if (!sendMessage(recipientEmail, subject, body)) {
			showError("email.notsent");
		}
	}

	/**
	 * Retrieves the user's locale from their preferences.
	 */
	private Locale getUserLocale(Identity identity) {
		String lang = identity.getUser().getPreferences().getLanguage();
		return i18nManager.getLocaleOrDefault(lang);
	}

	private String getAuthenticationNameOrDefault(Identity identity) {
		String authName = securityManager.findAuthenticationName(identity, "OLAT", BaseSecurity.DEFAULT_ISSUER);

		if (authName == null || StringHelper.isLong(authName)) {
			return loginModule.isAllowLoginUsingEmail() ? recipientEmail : identity.getUser().getNickName();
		}
		return authName;
	}

	/**
	 * Checks if the user has any passkey authentication method
	 */
	private boolean hasPasskeyAuthentication(Identity identity) {
		return !webAuthnManager.getPasskeyAuthentications(identity).isEmpty();
	}

	private String buildEmailBody(String body, String[] whereFromAttrs) {
		boolean htmlBody = StringHelper.isHtml(body);
		if (!htmlBody) {
			body += SEPARATOR + translate("reg.wherefrom", whereFromAttrs);
		}
		return body;
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

	private void resendNewOtp(UserRequest ureq) {
		if (selectedValidationType.equals(PwChangeVSelectionStep01Controller.PW_CHANGE_VAL_TYPE_MAIL)) {
			temporaryKey = registrationManager.updateTemporaryRegistrationKey(recipientEmail);
			String serverPath = Settings.getServerContextPathURI();
			String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
			String[] whereFromAttrs = new String[]{ serverPath, today };

			if (temporaryKey != null) {
				sendPasswordChangeEmail(whereFromAttrs);
			}
		} else {
			processSms();
		}
		otpEl.reset();
	}

	private boolean isOtpValid() {
		if (selectedValidationType.equals(PwChangeVSelectionStep01Controller.PW_CHANGE_VAL_TYPE_MAIL)) {
			TemporaryKey otpToken = registrationManager.loadTemporaryKeyByEmail(recipientEmail);
			return otpToken != null && otpToken.getRegistrationKey().equals(otpEl.getValue());
		} else {
			return otpEl.getValue().equals(sentToken);
		}
	}

	@Override
	public void back() {
		removeAsListenerAndDispose(this);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		if(done) return true;

		boolean allOk = super.validateFormLogic(ureq);

		otpContainer.clearError();
		if (otpEl.isEmpty()) {
			otpContainer.setErrorKey("reg.otp.may.not.be.empty");
			allOk = false;
		} else if (!isOtpValid()) {
			setState(State.invalid);
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == otpEl) {
			otpEl.clearSuccess();
			otpEl.clearError();
			if (isOtpValid()) {
				setState(State.valid);
			} else {
				setState(State.invalid);
			}
		} else if (source == resendOtpLink) {
			resendNewOtp(ureq);
			setState(State.newCode);
		}
	}

	private void setState(State state) {
		if (!this.state.equals(state)) {
			this.state = state;
			updateUI();
		}
	}

	@Override
	protected void formNext(UserRequest ureq) {
		// to prevent validation problems in last step
		// since there is no way to get back to this step, this is okay to bypass the validation
		done = true;
		// Do not remove temporaryKey entry yet. The user might still not add and save a password and the 
		// user might have a long lasting token that was created via REST to initiate the password reset workflow
		// because the user has no OpenOlat password. He might had some issues, e.g. did not receive the OTP
		// and might try it again within the time frame of the temp key validity period. 
		// The temp key will be removed automatically when expired. 
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
