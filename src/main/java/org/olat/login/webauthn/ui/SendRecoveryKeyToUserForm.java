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

import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SendRecoveryKeyToUserForm extends FormBasicController {
	
	private final Identity identityToSend;
	private TextElement mailText;
	private TextElement subjectText;
	
	private String dummyKey;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;

	public SendRecoveryKeyToUserForm(UserRequest ureq, WindowControl wControl, Identity identityToSend) {
		super(ureq, wControl);
		this.identityToSend = identityToSend;

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		MailContent initialText = generateMailText();
		subjectText = uifactory.addTextElement("subjecttext", "form.recovery.key.subject", 255, initialText.subject(), formLayout);
		subjectText.setMandatory(true);
		
		mailText = uifactory.addTextAreaElement("mailtext", "form.recovery.key.text", 4000, 12, 255, false, false, initialText.body(), formLayout);
		mailText.setMandatory(true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("submit", "form.recovery.key.title", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectText.clearError();
		if(!StringHelper.containsNonWhitespace(subjectText.getValue())) {
			subjectText.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		mailText.clearError();
		if(!StringHelper.containsNonWhitespace(mailText.getValue())) {
			mailText.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!mailText.getValue().contains(dummyKey)) {
			mailText.setErrorKey("error.link.missing");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		sendRecoveryKeys(ureq, subjectText.getValue(), mailText.getValue());
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	
	private MailContent generateMailText() {
		Preferences prefs = identityToSend.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = identityToSend.getUser().getProperty(UserConstants.EMAIL, locale);
		
		String body;
		String subject = "";
		if (emailAdress != null) {
			dummyKey = Encoder.md5hash(emailAdress);

			String serverpath = Settings.getServerContextPathURI();
			String serverLoginPath = Settings.getServerContextPathURI() + DispatcherModule.getPathDefault();
			Translator userTrans = Util.createPackageTranslator(SendRecoveryKeyToUserForm.class, locale) ;
			String authenticationName = securityManager.findAuthenticationName(identityToSend, "OLAT", BaseSecurity.DEFAULT_ISSUER);
			String userName = authenticationName;
			if((userName == null || StringHelper.isLong(authenticationName))) {
				if(loginModule.isAllowLoginUsingEmail()) {
					userName = emailAdress;
				} else {
					userName = identityToSend.getUser().getNickName();
				}
			}
			body = userTrans.translate("send.recovery.key.body", userName, serverpath, dummyKey, i18nModule.getLocaleKey(locale), serverLoginPath);
			subject = userTrans.translate("send.recovery.key.subject", userName, serverpath, dummyKey, i18nModule.getLocaleKey(locale), serverLoginPath);
		} else {
			body = "This function is not available for users without an email-adress!";
		}
		return new MailContent(subject, body);
	}
	
	private void sendRecoveryKeys(UserRequest ureq, String subject, String text) {
		Date validity = DateUtils.addHours(ureq.getRequestTimestamp(), 2);
		String key = webAuthnManager.generateRecoveryKey(identityToSend, validity, getIdentity());
		String body = text.replace(dummyKey, key);

		MailBundle bundle = new MailBundle();
		bundle.setToId(identityToSend);
		bundle.setContent(subject, body);
		MailerResult result = mailManager.sendExternMessage(bundle, null, false);
		if(result.getReturnCode() == 0) {
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}
	
	private record MailContent(String subject, String body) {
		//
	}
}