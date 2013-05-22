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

package org.olat.login;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.auth.AuthenticationController;
import org.olat.login.auth.OLATAuthentcationForm;
import org.olat.registration.DisclaimerController;
import org.olat.registration.PwChangeController;
import org.olat.registration.RegistrationController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserManager;
import org.olat.user.UserModule;

import com.thoughtworks.xstream.XStream;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 */
public class OLATAuthenticationController extends AuthenticationController implements Activateable2 {

	public static final String PARAM_LOGINERROR = "loginerror";
	
	private VelocityContainer loginComp;
	private OLATAuthentcationForm loginForm;
	private Identity authenticatedIdentity;
	private Controller subController;
	private DisclaimerController disclaimerCtr;

	private CloseableModalController cmc;

	private Link pwLink;
	private Link registerLink;
	private Link anoLink;
	
	/**
	 * @see org.olat.login.auth.AuthenticationController#init(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public OLATAuthenticationController(UserRequest ureq, WindowControl winControl) {
		// use fallback translator to registration module
		super(ureq, winControl, Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale()));

		loginComp = createVelocityContainer("olat_log", "olatlogin");
		
		if(UserModule.isPwdchangeallowed(null)) {
			pwLink = LinkFactory.createLink("_olat_login_change_pwd", "menu.pw", loginComp, this);
			pwLink.setCustomEnabledLinkCSS("o_login_pwd b_with_small_icon_left");
		}
		
		if (CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationEnabled()
				&& CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationLoginEnabled()) {
			registerLink = LinkFactory.createLink("_olat_login_register", "menu.register", loginComp, this);
			registerLink.setCustomEnabledLinkCSS("o_login_register b_with_small_icon_left");
		}
		
		if (LoginModule.isGuestLoginLinksEnabled()) {
			anoLink = LinkFactory.createLink("_olat_login_guest", "menu.guest", loginComp, this);
			anoLink.setCustomEnabledLinkCSS("o_login_guests b_with_small_icon_left");
			anoLink.setEnabled(!AuthHelper.isLoginBlocked());
		}
		
		
		// prepare login form
		loginForm = new OLATAuthentcationForm(ureq, winControl, "olat_login", getTranslator());
		listenTo(loginForm);
		
		loginComp.put("loginForm",loginForm.getInitialComponent());
		
		// Check if form is triggered by external loginworkflow that has been failed
		if (ureq.getParameterSet().contains(PARAM_LOGINERROR)) {
			showError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
		}

		putInitialPanel(loginComp);
	}

	/**
	 * @see org.olat.login.auth.AuthenticationController#changeLocale(java.util.Locale)
	 */
	public void changeLocale(Locale newLocale) {
		setLocale(newLocale, true);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == registerLink) {
			//fxdiff FXOLAT-113: business path in DMZ
			openRegistration(ureq);
		} else if (source == pwLink) {
			//fxdiff FXOLAT-113: business path in DMZ
			openChangePassword(ureq, null);
		} else if (source == anoLink){
			
			int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return;
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				showError("login.notavailable", null);
			} else {
				showError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
			}
		}
	}
	//fxdiff FXOLAT-113: business path in DMZ
	protected RegistrationController openRegistration(UserRequest ureq) {
		removeAsListenerAndDispose(subController);
		subController = new RegistrationController(ureq, getWindowControl());
		listenTo(subController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
		listenTo(cmc);

		cmc.activate();
		return (RegistrationController)subController;
	}
	//fxdiff FXOLAT-113: business path in DMZ
	protected void openChangePassword(UserRequest ureq, String initialEmail) {
		// double-check if allowed first
		if (!UserModule.isPwdchangeallowed(ureq.getIdentity())) throw new OLATSecurityException("chose password to be changed, but disallowed by config");
		
		removeAsListenerAndDispose(subController);
		subController = new PwChangeController(ureq, getWindowControl(), initialEmail);
		listenTo(subController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		
		if (source == loginForm && event == Event.DONE_EVENT) {
			String login = loginForm.getLogin();
			String pass = loginForm.getPass();
			authenticatedIdentity = authenticate(login, pass);
			if (authenticatedIdentity == null) {
				if (LoginModule.registerFailedLoginAttempt(login)) {
					getLogger().audit("Too many failed login attempts for " + login + ". Login blocked.", null);
					showError("login.blocked", LoginModule.getAttackPreventionTimeoutMin().toString());
					return;
				} else {
					showError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
					return;
				}
			} else {
				try {
					String language = authenticatedIdentity.getUser().getPreferences().getLanguage();
					UserSession usess = ureq.getUserSession();
					if(StringHelper.containsNonWhitespace(language)) {
						usess.setLocale(I18nManager.getInstance().getLocaleOrDefault(language));
					}
				} catch (Exception e) {
					logError("Cannot set the user language", e);
				}
			}
			
			LoginModule.clearFailedLoginAttempts(login);
			
			// Check if disclaimer has been accepted
			if (RegistrationManager.getInstance().needsToConfirmDisclaimer(authenticatedIdentity)) {
				// accept disclaimer first
				
				removeAsListenerAndDispose(disclaimerCtr);
				disclaimerCtr = new DisclaimerController(ureq, getWindowControl());
				listenTo(disclaimerCtr);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent());
				listenTo(cmc);
				
				cmc.activate();	
				
			} else {
				// disclaimer acceptance not required		
				authenticated(ureq, authenticatedIdentity);	
			}
		}
		
		if (source == disclaimerCtr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// disclaimer accepted 
				RegistrationManager.getInstance().setHasConfirmedDislaimer(authenticatedIdentity);
				authenticated(ureq, authenticatedIdentity);
			}
		}
		
		if (source == subController && event == Event.CANCELLED_EVENT) {
			cmc.deactivate();
		}
	}
	
	@Override
	//fxdiff FXOLAT-113: business path in DMZ
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("changepw".equals(type)) {
			String email = null;
			if(entries.size() > 1) {
				email = entries.get(1).getOLATResourceable().getResourceableTypeName();
			}
			openChangePassword(ureq, email);
		} else if("registration".equals(type)) {
			if (CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationEnabled()
					&& CoreSpringFactory.getImpl(RegistrationModule.class).isSelfRegistrationLinkEnabled()) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				openRegistration(ureq).activate(ureq, subEntries, entry.getTransientState());
			}
		}
	}

	/**
	 * @param login
	 * @param pass
	 * @return Identity if authentication was successfull, null otherwise.
	 * @deprecated should not be part of the controller
	 */
	public static Identity authenticate(String login, String pass) {
		if (pass == null) return null; // do never accept empty passwords
		Identity ident = BaseSecurityManager.getInstance().findIdentityByName(login);
		return authenticate(ident, login, pass);
	}
	
	/**
	 * @param login
	 * @param pass
	 * @return Identity if authentication was successfull, null otherwise.
	 * @deprecated should not be part of the controller
	 */
	public static Identity authenticate(Identity ident, String login, String pass) {
		// check for email instead of username if ident is null
		if (ident == null && LoginModule.allowLoginUsingEmail()) {
			if (MailHelper.isValidEmailAddress(login)){
  	 	 	ident = UserManager.getInstance().findIdentityByEmail(login);
  	 	}
			// check for email changed with verification workflow
			if (ident == null) {
				ident = findIdentInChangingEmailWorkflow(login);
			}
		}

		if (ident == null) {
			return null;
		}
		
		// find OLAT authentication provider
		String auth = BaseSecurityManager.getInstance().findCredentials(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		if (auth != null && auth.equals(Encoder.encrypt(pass)))	return ident;
		
		Tracing.createLoggerFor(OLATAuthenticationController.class).audit(
				"Error authenticating user "+login+" via provider OLAT",
				OLATAuthenticationController.class.getName()
		);
		
		return null;
	}
	
	private static Identity findIdentInChangingEmailWorkflow(String login){
		RegistrationManager rm = RegistrationManager.getInstance();
		List<TemporaryKey> tk = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
		if (tk != null) {
			for (TemporaryKey temporaryKey : tk) {
				XStream xml = new XStream();
				HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
				if (login.equals(mails.get("changedEMail"))) {
					return BaseSecurityManager.getInstance().findIdentityByName(mails.get("currentEMail"));
				}
			}
		}
		return null;		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
