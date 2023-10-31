/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.admin.user;

import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
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
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Form to send a email to the user with a link to change its password.
 * 
 * <P>
 * Initial Date:  26 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SendTokenToUserForm extends FormBasicController {
	
	private final Identity user;
	private TextElement mailText;
	private TextElement subjectText;
	
	private String dummyKey;
	private final boolean withTitle;
	private final boolean withCancel;
	private final boolean withDescription;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private LoginModule loginModule;

	public SendTokenToUserForm(UserRequest ureq, WindowControl wControl, Identity treatedIdentity,
			boolean withTitle, boolean withDescription, boolean withCancel) {
		super(ureq, wControl, Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale()));
		this.withTitle = withTitle;
		this.withCancel = withCancel;
		this.withDescription = withDescription;
		user = treatedIdentity;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(withTitle) {
			setFormTitle("form.token.new.title");
		}
		if(withDescription) {
			setFormDescription("form.token.new.description");
		}

		MailContent content = generateMailText();
		subjectText = uifactory.addTextElement("subjecttext", "form.token.new.subject", 255, content.subject(), formLayout);
		subjectText.setMandatory(true);
		
		mailText = uifactory.addTextAreaElement("mailtext", "form.token.new.text", 4000, 12, 255, false, false, content.body(), formLayout);
		mailText.setMandatory(true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("submit", "form.token.new.title", buttonsCont);
		if(withCancel) {
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
	}
	
	@Override
	public FormItem getInitialFormItem() {
		return flc;
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
		String text = mailText.getValue();
		String subject = subjectText.getValue();
		sendToken(ureq, subject, text);
		mailText.setValue(text);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private MailContent generateMailText() {
		Preferences prefs = user.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
		if (emailAdress != null) {
			dummyKey = Encoder.md5hash(emailAdress);

			String serverpath = Settings.getServerContextPathURI();
			String serverLoginPath = Settings.getServerContextPathURI() + DispatcherModule.getPathDefault();
			Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;
			String authenticationName = securityManager.findAuthenticationName(user, "OLAT", BaseSecurity.DEFAULT_ISSUER);
			String userName = authenticationName;
			if((userName == null || StringHelper.isLong(authenticationName))) {
				if(loginModule.isAllowLoginUsingEmail()) {
					userName = emailAdress;
				} else {
					userName = user.getUser().getNickName();
				}
			}
			String body = "<p>" + userTrans.translate("pwchange.intro.before") + "</p>"
					+ userTrans.translate("pwchange.intro", userName, authenticationName, emailAdress)
					+ userTrans.translate("pwchange.body", serverpath, dummyKey, i18nModule.getLocaleKey(locale), serverLoginPath);
			String subject = userTrans.translate("pwchange.subject");
			return new MailContent(subject, body);
		} 
		return new MailContent(translate("pwchange.subject"), "This function is not available for users without an email-adress!");

	}
	
	private void sendToken(UserRequest ureq, String subject, String text) {
		Preferences prefs = user.getUser().getPreferences();
		Locale locale = i18nManager.getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);

		String ip = ureq.getHttpReq().getRemoteAddr();
		TemporaryKey tk = registrationManager.createAndDeleteOldTemporaryKey(user.getKey(), emailAdress, ip,
				RegistrationManager.PW_CHANGE, loginModule.getValidUntilHoursGui());
		
		if(text.indexOf(dummyKey) < 0) {
			showWarning("changeuserpwd.failed");
			logWarn("Can not replace temporary registration token in change pwd mail token dialog, user probably changed temporary token in mai template", null);
			return;
		}
		String body = text.replace(dummyKey, tk.getRegistrationKey());

		MailBundle bundle = new MailBundle();
		bundle.setToId(user);
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