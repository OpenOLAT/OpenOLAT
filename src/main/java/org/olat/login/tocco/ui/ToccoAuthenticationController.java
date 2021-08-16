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
package org.olat.login.tocco.ui;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationController;
import org.olat.login.auth.OLATAuthentcationForm;
import org.olat.login.tocco.ToccoAuthManager;
import org.olat.login.tocco.ToccoLoginModule;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToccoAuthenticationController extends AuthenticationController implements Activateable2 {
	
	private Identity authenticatedIdentity;

	private CloseableModalController cmc;
	private DisclaimerController disclaimerCtr;
	private final OLATAuthentcationForm loginForm;

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private ToccoLoginModule toccoModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private ToccoAuthManager toccoAuthenticationManager;
	
	public ToccoAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(LoginModule.class, ureq.getLocale(),
				Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale())));

		// prepare login form
		loginForm = new OLATAuthentcationForm(ureq, wControl, "tocco_login", getTranslator());
		listenTo(loginForm);
		
		VelocityContainer loginComp = createVelocityContainer("tocco_log", "tocco_login");
		loginComp.put("loginForm", loginForm.getInitialComponent());
		
		if(StringHelper.containsNonWhitespace(toccoModule.getChangePasswordUrl())) {
			ExternalLink link = new ExternalLink("_tocco_login_change_pwd", "menu.pw");
			link.setElementCssClass("o_login_pwd");
			link.setName(translate("menu.pw"));
			link.setUrl(toccoModule.getChangePasswordUrl());
			link.setTarget("_blank");
			loginComp.put("menu.pw", link);
		}
		
		putInitialPanel(loginComp);
		
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	public void changeLocale(Locale newLocale) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == loginForm && event == Event.DONE_EVENT) {
			doLogin(ureq);
		} else if (source == disclaimerCtr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// User accepted disclaimer, do login now
				registrationManager.setHasConfirmedDislaimer(authenticatedIdentity);
				doLoginAndRegister(authenticatedIdentity, ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				// User did not accept, workflow ends here
				showWarning("disclaimer.form.cancelled");
			}
		} else if (source == cmc) {
			// User did close disclaimer window, workflow ends here
			showWarning("disclaimer.form.cancelled");			
		}
		super.event(ureq, source, event);
	}
	
	private void doLogin(UserRequest ureq) {
		String login = loginForm.getLogin();
		if (loginModule.isLoginBlocked(login)) {
			// do not proceed when already blocked
			showError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
			getLogger().info(Tracing.M_AUDIT, "Login attempt on already blocked login for {}. IP::{}", login, ureq.getHttpReq().getRemoteAddr());
			return;
		}

		String pass = loginForm.getPass();
		authenticatedIdentity = toccoAuthenticationManager.authenticate(login, pass);
		if (authenticatedIdentity == null) {
			if (loginModule.registerFailedLoginAttempt(login)) {
				logAudit("Too many failed login attempts for " + login + ". Login blocked. IP::" + ureq.getHttpReq().getRemoteAddr());
				showError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
			} else {
				showError("login.error");
			}
		} else if(Identity.STATUS_INACTIVE.equals(authenticatedIdentity.getStatus())) {
			showError("login.error.inactive", WebappHelper.getMailConfig("mailSupport"));
		} else {
			loginModule.clearFailedLoginAttempts(login);
			// Check if disclaimer has been accepted
			if (registrationManager.needsToConfirmDisclaimer(authenticatedIdentity)) {
				doAcceptDisclaimer(ureq);
			} else {
				// disclaimer acceptance not required
				doLoginAndRegister(authenticatedIdentity, ureq);
			}
		}
	}
	
	private void doAcceptDisclaimer(UserRequest ureq) {
		// accept disclaimer first
		removeAsListenerAndDispose(disclaimerCtr);
		removeAsListenerAndDispose(cmc);
		
		disclaimerCtr = new DisclaimerController(ureq, getWindowControl(), authenticatedIdentity, false);
		listenTo(disclaimerCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent());
		listenTo(cmc);
		cmc.activate();	
	}

	private void doLoginAndRegister(Identity authIdentity, UserRequest ureq) {
		// prepare redirects to home etc, set status
		int loginStatus = AuthHelper.doLogin(authIdentity, ToccoLoginModule.TOCCO_PROVIDER, ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			//update last login date and register active user
			securityManager.setIdentityLastLogin(authIdentity);
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
			DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
		} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
			getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
		}
	}
}
