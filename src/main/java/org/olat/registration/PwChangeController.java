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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.registration;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controlls the change password workflow.
 * <P>
 * @author Sabina Jeger
 */
public class PwChangeController extends BasicController {

	private Panel passwordPanel;
	private Link pwchangeHomelink;
	private final VelocityContainer myContent;
	
	private PwChangeForm pwf;
	private WizardInfoController wic;
	private SendMessageController sendSmsCtr;
	private ConfirmTokenController confirmTokenCtr;
	private EmailOrUsernameFormController emailOrUsernameCtr;
	
	private String pwKey;
	private TemporaryKey tempKey;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserModule userModule;
	@Autowired
	private RegistrationManager rm;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SimpleMessageModule smsModule;
	
	/**
	 * Controller to change a user's password.
	 * @param ureq
	 * @param wControl
	 */
	public PwChangeController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, false);
	}
	
	/**
	 * Controller to change a user's password.
	 * @param ureq
	 * @param wControl
	 */
	public PwChangeController(UserRequest ureq, WindowControl wControl, String initialEmail, boolean modal) {
		super(ureq, wControl);
		myContent = createVelocityContainer("pwchange");
		wic = new WizardInfoController(ureq, 4);
		myContent.put("pwwizard", wic.getInitialComponent());
		passwordPanel = new Panel("pwarea");
		myContent.put("pwarea", passwordPanel);
		pwKey = ureq.getHttpReq().getParameter("key");
		
		if (StringHelper.containsNonWhitespace(pwKey)) {
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(pwKey);
			// if key is not valid we redirect to first page
			if (tempKey == null) {
				// error, there should be an entry
				showError("pwkey.missingentry");
				createEmailForm(ureq, wControl, initialEmail);
			} else {
				showChangePasswordForm(ureq, tempKey);
			}
		} else {
			// no temporarykey is given, we assume step 1
			createEmailForm(ureq, wControl, initialEmail);
		}
		
		if(modal) {
			putInitialPanel(myContent);
		} else {
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, myContent, null);
			listenTo(layoutCtr);
			putInitialPanel(layoutCtr.getInitialComponent());
		}
	}
	
	public String getWizardTitle() {
		return translate("step1.pw.title");
	}

	/**
	 * Create the email / username form, the first step of the workflow.
	 */
	private void createEmailForm(UserRequest ureq, WindowControl wControl, String initialEmail) {
		myContent.contextPut("title", translate("step1.pw.title"));
		myContent.contextPut("text", translate("step1.pw.text"));
		removeAsListenerAndDispose(emailOrUsernameCtr);
		emailOrUsernameCtr = new EmailOrUsernameFormController(ureq, wControl, initialEmail);
		listenTo(emailOrUsernameCtr);
		passwordPanel.setContent(emailOrUsernameCtr.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == pwchangeHomelink) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());				
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == pwf) {
			// pwchange Form was clicked
			if (event == Event.DONE_EVENT) { // form
				showChangePasswordEnd();
			} else if (event == Event.CANCELLED_EVENT) {
				showInfo("pwform.cancelled");
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		} else if (source == emailOrUsernameCtr) {
			// eMail Form was clicked
			if (event == Event.DONE_EVENT) { // form
				doProcessEmailOrUsername(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (source == sendSmsCtr) {
			if (event == Event.DONE_EVENT) { // form
				doConfirmSendToken(ureq, sendSmsCtr.getRecipient(), sendSmsCtr.getSentToken());
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(source == confirmTokenCtr) {
			if (event == Event.DONE_EVENT) { // form
				showChangePasswordForm(ureq, confirmTokenCtr.getRecipient());
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}
	
	private void doProcessEmailOrUsername(UserRequest ureq) {
		// Email requested for tempkey save the fields somewhere
		String emailOrUsername = emailOrUsernameCtr.getEmailOrUsername();
		emailOrUsername = emailOrUsername.trim();

		Identity identity = findIdentityByUsernameOrEmail(emailOrUsername);
		if (identity != null) {
			if(smsModule.isEnabled() && smsModule.isResetPasswordEnabled()
					&& StringHelper.containsNonWhitespace(identity.getUser().getProperty(UserConstants.SMSTELMOBILE, getLocale()))) {
				tempKey = sendEmail(ureq, identity);
				sendSms(ureq, identity);
			} else {
				sendEmail(ureq, identity);
			}
		} else {
			logWarn("Failed to identify user in password change workflow: " + emailOrUsername, null);
			stepSendEmailConfiration();
		}
	}
	
	private void sendSms(UserRequest ureq, Identity recipient) {
		removeAsListenerAndDispose(sendSmsCtr);
		
		sendSmsCtr = new SendMessageController(ureq, getWindowControl(), recipient);
		listenTo(sendSmsCtr);
		passwordPanel.setContent(sendSmsCtr.getInitialComponent());	
		
		wic.setCurStep(2);
		myContent.contextPut("text", translate("step2.pw.text"));
	}
	
	private void doConfirmSendToken(UserRequest ureq, Identity recipient, String sentToken) {
		confirmTokenCtr = new ConfirmTokenController(ureq, getWindowControl(), recipient, sentToken);
		listenTo(confirmTokenCtr);
		passwordPanel.setContent(confirmTokenCtr.getInitialComponent());	
		
		wic.setCurStep(3);
		myContent.contextPut("text", translate("pw.change.confirm.descr"));
	}
	
	private TemporaryKey sendEmail(UserRequest ureq, Identity identity) {
		if (!userModule.isPwdChangeAllowed(identity)) { 
			getWindowControl().setWarning(translate("password.cantchange"));
			return null;
		}

		Preferences prefs = identity.getUser().getPreferences();
		Locale locale = i18nManager.getLocaleOrDefault(prefs.getLanguage());
		ureq.getUserSession().setLocale(locale);
		myContent.contextPut("locale", locale);
		Translator userTrans = Util.createPackageTranslator(PwChangeController.class, locale) ;

		String emailAdress = identity.getUser().getProperty(UserConstants.EMAIL, locale); 
		if (!StringHelper.containsNonWhitespace(emailAdress)) {
			stepSendEmailConfiration();//for security reason, don't show an error, go simply to the next step
			return null;
		}
		
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		// mailer configuration
		String serverpath = Settings.getServerContextPathURI();
		String serverLoginPath = Settings.getServerContextPathURI() + DispatcherModule.getPathDefault();
		String authenticationName = securityManager.findAuthenticationName(identity, "OLAT");
		String userName = authenticationName;
		if((userName == null || StringHelper.isLong(authenticationName)) && loginModule.isAllowLoginUsingEmail()) {
			userName = emailAdress;
		}
		
		TemporaryKey tk = rm.createAndDeleteOldTemporaryKey(identity.getKey(), emailAdress, ip,
				RegistrationManager.PW_CHANGE, loginModule.getValidUntilHoursGui());
		
		myContent.contextPut("pwKey", tk.getRegistrationKey());
		StringBuilder body = new StringBuilder(2048);
		body.append("<style>")
			.append(".o_footer {background: #FAFAFA; border: 1px solid #eee; border-radius: 5px; padding: 1em; margin: 1em;}")
			.append(".o_body {background: #FAFAFA; padding: 1em; margin: 1em;}")
			.append("</style>")
			.append("<div class='o_body'>")
			.append(userTrans.translate("pwchange.headline"))
			.append(userTrans.translate("pwchange.intro", new String[] { userName, authenticationName, emailAdress }))
		    .append(userTrans.translate("pwchange.body", new String[] { serverpath, tk.getRegistrationKey(), i18nModule.getLocaleKey(ureq.getLocale()), serverLoginPath }))
		    .append(userTrans.translate("pwchange.body.alt", new String[] { serverpath, tk.getRegistrationKey(), i18nModule.getLocaleKey(ureq.getLocale()), serverLoginPath }))
		    .append("</div>")
		    .append("<div class='o_footer'>")
		    .append(userTrans.translate("reg.wherefrom", new String[] { serverpath, today }))
		    .append("</div>");

		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(userTrans.translate("pwchange.subject"), body.toString());
		MailerResult result = mailManager.sendExternMessage(bundle, null, false);
		if(result.getReturnCode() == MailerResult.OK) {
			getWindowControl().setInfo(translate("email.sent"));
		}
		stepSendEmailConfiration();
		return tk;
	}
	
	/**
	 * Activate the step 2
	 */
	private void stepSendEmailConfiration() {
		wic.setCurStep(2);
		myContent.contextPut("text", translate("step2.pw.text"));
		emailOrUsernameCtr.getInitialComponent().setVisible(false);
	}
	
	/**
	 * Look for user in "Person" and "user" tables. Fist search by user name
	 * and if the user cannot be found, search by email address.
	 * @return Identity or null if not found.
	 */
	private Identity findIdentityByUsernameOrEmail(String emailOrUsername) {
		// See if the entered value is the authusername of an authentication
		Identity identity = securityManager.findIdentityByLogin(emailOrUsername);
		if (identity == null) {
			// Try fallback with email, maybe user used his email address instead
			identity = userManager.findUniqueIdentityByEmail(emailOrUsername);
		}
		if (identity == null) {
			identity = securityManager.findIdentityByNickName(emailOrUsername);
		}
		return identity;
	}
	
	private void showChangePasswordForm(UserRequest ureq, TemporaryKey temporaryKey) {
		wic.setCurStep(3);
		pwf = new PwChangeForm(ureq, getWindowControl(), temporaryKey);
		listenTo(pwf);
		myContent.contextPut("text", translate("step3.pw.text"));
		passwordPanel.setContent(pwf.getInitialComponent());
	}
	
	private void showChangePasswordForm(UserRequest ureq, Identity identityToChange) {
		wic.setCurStep(3);
		pwf = new PwChangeForm(ureq, getWindowControl(), identityToChange, tempKey);
		listenTo(pwf);
		myContent.contextPut("text", translate("step3.pw.text"));
		passwordPanel.setContent(pwf.getInitialComponent());
	}

	/**
	 * Change the password of a confirmed identity (per SMS).
	 * 
	 * @param identToChange The identity to change the password
	 */
	private void showChangePasswordEnd() {
		// validation was ok
		wic.setCurStep(4);
		myContent.contextPut("text", translate("step4.pw.text"));
		pwchangeHomelink = LinkFactory.createLink("pwchange.homelink", myContent, this);
		pwchangeHomelink.setCustomEnabledLinkCSS("btn btn-primary");
		passwordPanel.setVisible(false);
	}

	@Override
	protected void doDispose() {
		if (wic != null) {
			wic.dispose();
			wic = null;
		}
	}
}