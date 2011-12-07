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
* <p>
*/ 

package org.olat.login;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
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

import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;

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

		loginComp = createVelocityContainer("olatlogin");
		
		if(UserModule.isPwdchangeallowed()) {
			pwLink = LinkFactory.createLink("menu.pw", loginComp, this);
			pwLink.setCustomEnabledLinkCSS("o_login_pwd");
		}
		
		if (RegistrationModule.isSelfRegistrationEnabled()) {
			registerLink = LinkFactory.createLink("menu.register", loginComp, this);
			registerLink.setCustomEnabledLinkCSS("o_login_register");
		}
		
		if (LoginModule.isGuestLoginLinksEnabled()) {
			anoLink = LinkFactory.createLink("menu.guest", loginComp, this);
			anoLink.setCustomEnabledLinkCSS("o_login_guests");
		}
		
		
		// prepare login form
		loginForm = new OLATAuthentcationForm(ureq, winControl, getTranslator());
		listenTo(loginForm);
		
		loginComp.put("loginForm",loginForm.getInitialComponent());
		
		// Check if form is triggered by external loginworkflow that has been failed
		if (ureq.getParameterSet().contains(PARAM_LOGINERROR)) {
			showError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
		}
		
		// support email
		loginComp.contextPut("supportmailaddress", WebappHelper.getMailConfig("mailSupport"));
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
			removeAsListenerAndDispose(subController);
			subController = new RegistrationController(ureq, getWindowControl());
			listenTo(subController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
			listenTo(cmc);

			cmc.activate();
			
		} else if (source == pwLink) {
			
			// double-check if allowed first
			if (!UserModule.isPwdchangeallowed()) throw new OLATSecurityException("chose password to be changed, but disallowed by config");
			
			removeAsListenerAndDispose(subController);
			subController = new PwChangeController(ureq, getWindowControl());
			listenTo(subController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
			
		} else if (source == anoLink){
			
			int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
			if (loginStatus == AuthHelper.LOGIN_OK) {
				return;
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				showError("login.notavailable", WebappHelper.getMailConfig("mailSupport"));
			} else {
				showError("login.error", WebappHelper.getMailConfig("mailSupport"));
			}
		}
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
					showError("login.error", WebappHelper.getMailConfig("mailSupport"));
					return;
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
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
		
		if (ident == null) return null;
		
		// find OLAT authentication provider
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(
				ident, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		
		if (auth != null && auth.getCredential().equals(Encoder.encrypt(pass)))	return ident;
		
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
