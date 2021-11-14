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

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
	
	private String dummyKey;
	
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

	public SendTokenToUserForm(UserRequest ureq, WindowControl wControl, Identity treatedIdentity) {
		super(ureq, wControl);
		user = treatedIdentity;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.token.new.title");
		setFormDescription("form.token.new.description");
		
		String initialText = generateMailText();
		mailText = uifactory.addTextAreaElement("mailtext", "form.token.new.text", 4000, 12, 255, false, false, initialText, formLayout);
		
		uifactory.addFormSubmitButton("submit", "form.token.new.title", formLayout);
	}
	
	public String getMailText() {
		return mailText.getValue();
	}
	
	public void setMailText(String text) {
		mailText.setValue(text);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String text = mailText.getValue();
		sendToken(ureq, text);
		mailText.setValue(text);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}
	
	private String generateMailText() {
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
			if((userName == null || StringHelper.isLong(authenticationName)) && loginModule.isAllowLoginUsingEmail()) {
				userName = emailAdress;
			}
			return userTrans.translate("pwchange.intro", new String[] { userName, authenticationName, emailAdress })
					+ userTrans.translate("pwchange.body", new String[] { serverpath, dummyKey, i18nModule.getLocaleKey(locale), serverLoginPath });
		}
		else return "This function is not available for users without an email-adress!";
	}
	
	private void sendToken(UserRequest ureq, String text) {
		// mailer configuration
		// We allow creation of password token when user has no password so far or when he as an OpenOLAT Password. 
		// For other cases such as Shibboleth, LDAP, oAuth etc. we don't allow creation of token as this is most 
		// likely not a desired action.
		List<Authentication> authentications = securityManager.getAuthentications(user);
		boolean isOOpwdAllowed = authentications.isEmpty();
		for (Authentication authentication : authentications) {
			if (authentication.getProvider().equals(BaseSecurityModule.getDefaultAuthProviderIdentifier())) {
				isOOpwdAllowed = true;
			}			
		}		
		if (!isOOpwdAllowed) { 
			showWarning("sendtoken.wrong.auth");
			return;
		}
		
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
		Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;

		MailBundle bundle = new MailBundle();
		bundle.setToId(user);
		bundle.setContent(userTrans.translate("pwchange.subject"), body);
		MailerResult result = mailManager.sendExternMessage(bundle, null, false);
		if(result.getReturnCode() == 0) {
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}
}