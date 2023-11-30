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
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.login.webauthn.ui.NewPasskeyController;
import org.olat.login.webauthn.ui.RegistrationPasskeyListController;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.ui.identity.UserOpenOlatAuthenticationController;
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
	private CloseableModalController cmc;
	private SendMessageController sendSmsCtr;
	private NewPasskeyController newPasskeyCtrl;
	private ConfirmTokenController confirmTokenCtr;
	private EmailOrUsernameFormController emailOrUsernameCtr;
	private RegistrationPasskeyListController passkeyListCtrl;
	
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
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	
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
		} else if(source == newPasskeyCtrl) {
			if(event == Event.DONE_EVENT) {
				finishGeneratePasskey(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == passkeyListCtrl) {
			if(event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doGeneratePasskey(ureq, passkeyListCtrl.getIdentityToChange());
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
	
	private void cleanUp() {
		removeAsListenerAndDispose(newPasskeyCtrl);
		removeAsListenerAndDispose(cmc);
		newPasskeyCtrl = null;
		cmc = null;
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
		String authenticationName = securityManager.findAuthenticationName(identity, "OLAT", BaseSecurity.DEFAULT_ISSUER);
		String userName = authenticationName;
		if((userName == null || StringHelper.isLong(authenticationName))) {
			if(loginModule.isAllowLoginUsingEmail()) {
				userName = emailAdress;
			} else {
				userName = identity.getUser().getNickName();
			}
		}
		
		TemporaryKey tk = rm.createAndDeleteOldTemporaryKey(identity.getKey(), emailAdress, ip,
				RegistrationManager.PW_CHANGE, loginModule.getValidUntilHoursGui());

		String subject;
		StringBuilder body = new StringBuilder(2048);
		body.append("<style>")
			.append(".o_footer {background: #FAFAFA; border: 1px solid #eee; border-radius: 5px; padding: 1em; margin: 1em;}")
			.append(".o_body {background: #FAFAFA; padding: 1em; margin: 1em;}")
			.append("</style>")
			.append("<div class='o_body'><p>")
			.append(translate("pwchange.intro.before"))
			.append("</p>");
		
		if(webAuthnManager.getPasskeyAuthentications(identity).isEmpty()) {
			subject = translate("pwchange.subject");
			body.append(userTrans.translate("pwchange.intro", userName, authenticationName, emailAdress))
				.append(userTrans.translate("pwchange.body", serverpath, tk.getRegistrationKey(), i18nModule.getLocaleKey(ureq.getLocale()), serverLoginPath, userName))
				.append(userTrans.translate("pwchange.body.alt", serverpath, tk.getRegistrationKey(), i18nModule.getLocaleKey(ureq.getLocale()), serverLoginPath));
		} else {
			subject = translate("pwchange.subject.passkey");
			body.append(userTrans.translate("pwchange.intro.passkey", userName, authenticationName, emailAdress))
				.append(userTrans.translate("pwchange.body.passkey", serverpath, tk.getRegistrationKey(), i18nModule.getLocaleKey(ureq.getLocale()), serverLoginPath, userName));
		}
		body.append("</div>")
			.append("<div class='o_footer'>")
			.append(userTrans.translate("reg.wherefrom", serverpath, today))
			.append("</div>");

		myContent.contextPut("pwKey", tk.getRegistrationKey());
		
		MailBundle bundle = new MailBundle();
		bundle.setToId(identity);
		bundle.setContent(subject, body.toString());
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
		Identity identityToChange = securityManager.loadIdentityByKey(tempKey.getIdentityKey());
		showChangePasswordForm(ureq, identityToChange, temporaryKey);
	}
	
	private void showChangePasswordForm(UserRequest ureq, Identity identityToChange) {
		showChangePasswordForm(ureq, identityToChange, tempKey);
	}
	
	private void showChangePasswordForm(UserRequest ureq, Identity identityToChange, TemporaryKey temporaryKey) {
		wic.setCurStep(3);

		getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(true);
		
		Roles roles = securityManager.getRoles(identityToChange);
		PasskeyLevels requiredLevel = loginModule.getPasskeyLevel(roles);
		
		List<Authentication> authentications = securityManager.getAuthentications(identityToChange);
		PasskeyLevels currentLevel = PasskeyLevels.currentLevel(authentications);

		VelocityContainer container = createVelocityContainer("pwchange_container");
		if(requiredLevel == PasskeyLevels.level1 || requiredLevel == PasskeyLevels.level3 || currentLevel == PasskeyLevels.level3) {
			pwf = new PwChangeForm(ureq, getWindowControl(), identityToChange, temporaryKey);
			listenTo(pwf);
			container.put("pwf", pwf.getInitialComponent());
		}
		
		if(requiredLevel == PasskeyLevels.level2 || requiredLevel == PasskeyLevels.level3) {
			passkeyListCtrl = new RegistrationPasskeyListController(ureq, getWindowControl(), identityToChange);
			listenTo(passkeyListCtrl);
			container.put("pkf", passkeyListCtrl.getInitialComponent());
		}

		passwordPanel.setContent(container);
	}
	
	private void doGeneratePasskey(UserRequest ureq, Identity identityToChange) {
		String username = identityToChange.getUser().getNickName();
		
		if(!StringHelper.containsNonWhitespace(username)) {
			showWarning("warning.need.username");
		} else {
			newPasskeyCtrl = new NewPasskeyController(ureq, getWindowControl(), identityToChange, false, false, true);
			newPasskeyCtrl.setFormInfo(username, username);
			newPasskeyCtrl.setFormInfo(translate("new.passkey.level2.hint"),
						UserOpenOlatAuthenticationController.HELP_URL);
			listenTo(newPasskeyCtrl);
			
			String title = translate("new.passkey.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newPasskeyCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void finishGeneratePasskey(UserRequest ureq) {
		Authentication authentication = newPasskeyCtrl.getPasskeyAuthentication();
		passkeyListCtrl.loadAuthentication(ureq, authentication);
		if(authentication != null && authentication.getKey() != null) {
			securityManager.persistAuthentications(newPasskeyCtrl.getIdentityToPasskey(), List.of(authentication));
		}
		
		showChangePasswordEnd();
	}

	/**
	 * Change the password of a confirmed identity (per SMS).
	 * 
	 * @param identToChange The identity to change the password
	 */
	private void showChangePasswordEnd() {
		if(passkeyListCtrl == null || passkeyListCtrl.hasPasskeys()) {
			// validation was ok
			wic.setCurStep(4);
			myContent.contextPut("text", translate("step4.pw.text"));
			pwchangeHomelink = LinkFactory.createLink("pwchange.homelink", myContent, this);
			pwchangeHomelink.setCustomEnabledLinkCSS("btn btn-primary");
			passwordPanel.setVisible(false);

			getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(false);
		}
	}

	@Override
	protected void doDispose() {
		if (wic != null) {
			wic.dispose();
			wic = null;
		}
        super.doDispose();
	}
}