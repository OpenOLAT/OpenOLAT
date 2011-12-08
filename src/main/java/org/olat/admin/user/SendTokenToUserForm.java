/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.admin.user;

import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;

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
		mailText = uifactory.addTextAreaElement("mailtext", "form.token.new.text", 4000, 12, 255, false, initialText, formLayout);
		
		uifactory.addFormSubmitButton("submit", "form.token.new.title", formLayout);
	}
	
	public String getMailText() {
		return mailText.getValue();
	}
	
	public void setMailText(String text) {
		mailText.setValue(text);
	}

	@Override
	protected void doDispose() {
		//auto disposed by basic controller
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String text = mailText.getValue();
		sendToken(ureq, text);
		mailText.setValue(escapeLanguage(text));
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public FormItem getInitialFormItem() {
		return flc;
	}
	
	/**
	 * Workaround for display where the &lang is considered as an html entity
	 * @param text
	 */
	private String escapeLanguage(String text) {
		return text.replace("&lang=", "&amp;lang=");
	}
	
	private String generateMailText() {
		Preferences prefs = user.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);
		if (emailAdress != null) {
			dummyKey = Encoder.encrypt(emailAdress);

			String serverpath = Settings.getServerContextPathURI();
			Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;
			String body = userTrans.translate("pwchange.intro", new String[] { user.getName() })
					+ userTrans.translate("pwchange.body", new String[] {
							serverpath, dummyKey, I18nManager.getInstance().getLocaleKey(locale)
					});
			return escapeLanguage(body);
		}
		else return "This function is not available for users without an email-adress!";
	}
	
	private void sendToken(UserRequest ureq, String text) {
		// mailer configuration
		// check if user has an OLAT provider token, otherwhise a pwd change makes no sense
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(user, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		if (auth == null) { 
			showWarning("changeuserpwd.failed");
			return;
		}
		
		Preferences prefs = user.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = user.getUser().getProperty(UserConstants.EMAIL, locale);

		RegistrationManager rm = RegistrationManager.getInstance();
		TemporaryKey tk = rm.loadTemporaryKeyByEmail(emailAdress);
		if (tk == null) {
			String ip = ureq.getHttpReq().getRemoteAddr();
			tk = rm.createTemporaryKeyByEmail(emailAdress, ip, rm.PW_CHANGE);
		}
		if(text.indexOf(dummyKey) < 0) {
			showWarning("changeuserpwd.failed");
			return;
		}
		String body = text.replace(dummyKey, tk.getRegistrationKey());

		Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale) ;
		String subject = userTrans.translate("pwchange.subject");
		MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
				// nothing to do
			}
		};
		//fxdiff VCRP-16: intern mail system
		MailerResult result = MailerWithTemplate.getInstance().sendRealMail(user, mailTempl);
		if(result.getReturnCode() == 0) {
			showInfo("email.sent");
		} else {
			showInfo("email.notsent");
		}
	}
}
