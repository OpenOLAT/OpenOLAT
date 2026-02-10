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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Form to email the user with a link to change its password.
 * 
 * <P>
 * Initial Date:  26 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SendTokenToUserForm extends FormBasicController {
	
	private final Identity identityToModify;
	private TextElement bodyText;
	private TextElement subjectText;

	private final boolean withTitle;
	private final boolean withCancel;
	private final boolean withDescription;

	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private UserManager userManager;

	public SendTokenToUserForm(UserRequest ureq, WindowControl wControl, Identity identityToModify,
			boolean withTitle, boolean withDescription, boolean withCancel) {
		super(ureq, wControl, Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale()));
		this.withTitle = withTitle;
		this.withCancel = withCancel;
		this.withDescription = withDescription;
		this.identityToModify = identityToModify;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(withTitle) {
			setFormTitle("send.invitation.link");
		}
		if(withDescription) {
			setFormDescription("form.token.new.description");
		}
		formLayout.setElementCssClass("o_sel_send_token_form");

		MailContent content = generateMailText();
		if (content != null) {
			subjectText = uifactory.addTextElement("subjecttext", "form.token.new.subject", 255, content.subject(), formLayout);
			subjectText.setElementCssClass("o_sel_send_subject");
			subjectText.setMandatory(true);

			bodyText = uifactory.addTextAreaElement("mailtext", "form.token.new.text", 4000, 12, 255, false, false, content.body(), formLayout);
			bodyText.setElementCssClass("o_sel_send_body");
			bodyText.setMandatory(true);

			FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
			uifactory.addFormSubmitButton("submit", "send.invitation.link", buttonsCont);
			if(withCancel) {
				uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			}
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
			allOk = false;
		}
		
		bodyText.clearError();
		if(!StringHelper.containsNonWhitespace(bodyText.getValue())) {
			bodyText.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String body = bodyText.getValue();
		String subject = subjectText.getValue();
		sendToken(ureq, subject, body);
		bodyText.setValue(body);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private MailContent generateMailText() {
		Preferences prefs = identityToModify.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAddress = identityToModify.getUser().getProperty(UserConstants.EMAIL, locale);
		if (emailAddress != null) {
			String serverpath = Settings.getServerContextPathURI();
			String serverLoginPath = Settings.getServerContextPathURI() + DispatcherModule.getPathDefault();
			Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale);
			String authenticationName = securityManager.findAuthenticationName(identityToModify, "OLAT", BaseSecurity.DEFAULT_ISSUER);
			String userName = authenticationName;
			if((userName == null || StringHelper.isLong(authenticationName))) {
				if(loginModule.isAllowLoginUsingEmail()) {
					userName = emailAddress;
				} else {
					userName = identityToModify.getUser().getNickName();
				}
			}
			// Plus need to be URL encoded (+ is for space)
			String userDisplayName = userManager.getUserDisplayName(identityToModify);

			emailAddress = emailAddress.replace("+", "%2B");
			String resetUrlString = serverpath + "/url/changepw/0/" + emailAddress + "/0";
			String body = userTrans.translate("set.login.credentials.body", userDisplayName, userName, 
					resetUrlString, serverLoginPath);
			String subject = userTrans.translate("set.login.credentials.subject");
			return new MailContent(subject, body);
		}
		return null;
	}
	
	private void sendToken(UserRequest ureq, String subject, String body) {
		MailBundle bundle = new MailBundle();
		bundle.setToId(identityToModify);
		bundle.setContent(subject, body);
		MailerResult result = mailManager.sendExternMessage(bundle, new MailerResult(), true);
		if(result.getReturnCode() == MailerResult.OK) {
			String ip = ureq.getHttpReq().getRemoteAddr();
			String emailAdress = identityToModify.getUser().getProperty(UserConstants.EMAIL, getLocale());
			registrationManager.createAndDeleteOldTemporaryKey(identityToModify.getKey(), emailAdress, ip,
					RegistrationManager.PW_CHANGE, registrationModule.getRESTValidityOfTemporaryKey());
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}
	
	private record MailContent(String subject, String body) {
		//
	}
}