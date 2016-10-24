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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
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
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
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

	private static String SEPARATOR = "____________________________________________________________________\n";
	private VelocityContainer myContent;
	
	private Panel pwarea;
	private WizardInfoController wic;
	private final MailManager mailManager;
	private String pwKey;
	private PwChangeForm pwf;
	private TemporaryKeyImpl tempKey;
	private EmailOrUsernameFormController emailOrUsernameCtr;
	private Link pwchangeHomelink;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private RegistrationManager rm;
	@Autowired
	private UserManager userManager;
	
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
		mailManager = CoreSpringFactory.getImpl(MailManager.class);
		myContent = createVelocityContainer("pwchange");
		wic = new WizardInfoController(ureq, 4);
		myContent.put("pwwizard", wic.getInitialComponent());
		pwarea = new Panel("pwarea");
		myContent.put("pwarea", pwarea);
		pwKey = ureq.getHttpReq().getParameter("key");
		if (pwKey == null || pwKey.equals("")) {
			// no temporarykey is given, we assume step 1
			createEmailForm(ureq, wControl, initialEmail);
		} else {
			// we check if given key is a valid temporary key
			tempKey = rm.loadTemporaryKeyByRegistrationKey(pwKey);
			// if key is not valid we redirect to first page
			if (tempKey == null) {
				// error, there should be an entry
				getWindowControl().setError(translate("pwkey.missingentry"));
				createEmailForm(ureq, wControl, initialEmail);
			} else {
				wic.setCurStep(3);
				pwf = new PwChangeForm(ureq, wControl);
				listenTo(pwf);
				myContent.contextPut("pwdhelp", translate("pwdhelp"));
				myContent.contextPut("text", translate("step3.pw.text"));
				pwarea.setContent(pwf.getInitialComponent());				
			}
		}
		
		if(modal) {
			putInitialPanel(myContent);
		} else {
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, myContent, null);
			putInitialPanel(layoutCtr.getInitialComponent());
		}
	}
	
	public String getWizardTitle() {
		return translate("step1.pw.title");
	}

	/**
	 * just needed for creating EmailForm
	 */
	//fxdiff FXOLAT-113: business path in DMZ
	private void createEmailForm(UserRequest ureq, WindowControl wControl, String initialEmail) {
		myContent.contextPut("title", translate("step1.pw.title"));
		myContent.contextPut("text", translate("step1.pw.text"));
		removeAsListenerAndDispose(emailOrUsernameCtr);
		emailOrUsernameCtr = new EmailOrUsernameFormController(ureq, wControl, initialEmail);
		listenTo(emailOrUsernameCtr);
		pwarea.setContent(emailOrUsernameCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == pwchangeHomelink) {
			DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());				
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == pwf) {
			// pwchange Form was clicked
			if (event == Event.DONE_EVENT) { // form
				// validation was ok
				wic.setCurStep(4);
				myContent.contextPut("pwdhelp", "");
				myContent.contextPut("text", translate("step4.pw.text"));
				pwchangeHomelink = LinkFactory.createLink("pwchange.homelink", myContent, this);
				pwchangeHomelink.setCustomEnabledLinkCSS("btn btn-primary");
				//pwf.setVisible(false);
				pwarea.setVisible(false);
				List<Identity> identToChanges = userManager.findIdentitiesByEmail(Collections.singletonList(tempKey.getEmailAddress()));
				if(identToChanges == null || identToChanges.size() == 0 || identToChanges.size() > 1) {
					getWindowControl().setError(translate("pwchange.failed"));
				} else {
					Identity identToChange = identToChanges.get(0);
					if(!pwf.saveFormData(identToChange)) {
						getWindowControl().setError(translate("pwchange.failed"));
					}
				}
				rm.deleteTemporaryKeyWithId(tempKey.getRegistrationKey());				
			} else if (event == Event.CANCELLED_EVENT) {
				getWindowControl().setInfo(translate("pwform.cancelled"));
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		} else if (source == emailOrUsernameCtr) {
			// eMail Form was clicked
			if (event == Event.DONE_EVENT) { // form
				// Email requested for tempkey save the fields somewhere
				String emailOrUsername = emailOrUsernameCtr.getEmailOrUsername();
				emailOrUsername = emailOrUsername.trim();

				// get remote address
				String ip = ureq.getHttpReq().getRemoteAddr();
				String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
				// mailer configuration
				String serverpath = Settings.getServerContextPathURI();
				String servername = ureq.getHttpReq().getServerName();
				if(isLogDebugEnabled()) {
					logDebug("this servername is " + servername + " and serverpath is " + serverpath, null);
				}

				// Look for user in "Person" and "user" tables
				Identity identity = null;
				// See if the entered value is a username
				identity = BaseSecurityManager.getInstance().findIdentityByName(emailOrUsername);
				if (identity == null) {
					// Try fallback with email, maybe user used his email address instead
					// only do this, if its really an email, may lead to multiple results else.
					if (MailHelper.isValidEmailAddress(emailOrUsername)) {
						List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(emailOrUsername));
						if(identities.size() == 1) {
							identity = identities.get(0);
						}
					}
				}
				if (identity != null) {
					// check if user has an OLAT provider token, otherwhise a pwd change makes no sense
					Authentication auth = BaseSecurityManager.getInstance().findAuthentication(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());
					if (auth == null || !userModule.isPwdChangeAllowed(identity)) { 
						getWindowControl().setWarning(translate("password.cantchange"));
						return;
					}
					Preferences prefs = identity.getUser().getPreferences();
					Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
					ureq.getUserSession().setLocale(locale);
					myContent.contextPut("locale", locale);
					Translator userTrans = Util.createPackageTranslator(PwChangeController.class, locale) ;
					String emailAdress = identity.getUser().getProperty(UserConstants.EMAIL, locale); 
					TemporaryKey tk = rm.loadTemporaryKeyByEmail(emailAdress);
					if (tk == null) tk = rm.createTemporaryKeyByEmail(emailAdress, ip, RegistrationManager.PW_CHANGE);
					myContent.contextPut("pwKey", tk.getRegistrationKey());
					StringBuilder body = new StringBuilder();
					body.append(userTrans.translate("pwchange.intro", new String[] { identity.getName() }))
					    .append(userTrans.translate("pwchange.body", new String[] { serverpath, tk.getRegistrationKey(), I18nManager.getInstance().getLocaleKey(ureq.getLocale()) }))
					    .append(SEPARATOR)
					    .append(userTrans.translate("reg.wherefrom", new String[] { serverpath, today, ip }));
		
					MailBundle bundle = new MailBundle();
					bundle.setToId(identity);
					bundle.setContent(userTrans.translate("pwchange.subject"), body.toString());
					MailerResult result = mailManager.sendExternMessage(bundle, null, false);
					if(result.getReturnCode() == 0) {
						getWindowControl().setInfo(translate("email.sent"));
						// prepare next step
						wic.setCurStep(2);
						myContent.contextPut("text", translate("step2.pw.text"));
						emailOrUsernameCtr.getInitialComponent().setVisible(false);
					} else {
						getWindowControl().setError(translate("email.notsent"));
					}
				} else {
					// no user exists, this is an error in the pwchange page
					// REVIEW:pb:2009-11-23:gw, setter should not be necessary. -> check the error already in th emailOrUsernameCtr
					emailOrUsernameCtr.setUserNotIdentifiedError();
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (wic != null) {
			wic.dispose();
			wic = null;
		}
	}

}