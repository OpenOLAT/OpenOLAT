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

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.auth.AuthenticationController;
import org.olat.login.auth.AuthenticationEvent;
import org.olat.login.webauthn.ui.WebAuthnAuthenticationForm;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 */
public class OLATAuthenticationController extends AuthenticationController implements Activateable2 {

	public static final String PARAM_LOGINERROR = "loginerror";

	private VelocityContainer loginComp;
	
	private Controller loginForm;
	private Controller subController;
	private CloseableModalController cmc;
	private DisclaimerController disclaimerCtr;

	private Identity authenticatedIdentity;
	private String authenticationProvider;

	@Autowired
	private RegistrationManager registrationManager;
	
	/**
	 * @see org.olat.login.auth.AuthenticationController#init(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public OLATAuthenticationController(UserRequest ureq, WindowControl winControl) {
		// use fallback translator to registration module
		super(ureq, winControl, Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale()));
		
		loginComp = createVelocityContainer("olat_log", "olatlogin");

		// prepare login form
		loginForm = new WebAuthnAuthenticationForm(ureq, winControl, "olat_login", getTranslator());
		listenTo(loginForm);
		
		loginComp.put("loginForm",loginForm.getInitialComponent());
		
		// Check if form is triggered by external loginworkflow that has been failed
		if (ureq.getParameterSet().contains(PARAM_LOGINERROR)) {
			showError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
		}

		putInitialPanel(loginComp);
	}

	@Override
	public void changeLocale(Locale newLocale) {
		setLocale(newLocale, true);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == loginForm) {
			if(event instanceof AuthenticationEvent ae) {
				authenticatedIdentity = ae.getIdentity();
				authenticationProvider = ae.getProvider();
				postAuthentication(ureq);
			} else if(event == Event.BACK_EVENT || event instanceof LoginEvent) {
				fireEvent(ureq, event);
			}
		} else if (source == disclaimerCtr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// disclaimer accepted 
				registrationManager.setHasConfirmedDislaimer(authenticatedIdentity);
				authenticated(ureq, authenticatedIdentity, authenticationProvider);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		
		if (source == subController && event == Event.CANCELLED_EVENT) {
			cmc.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(subController);
		removeAsListenerAndDispose(cmc);
		subController = null;
		cmc = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	private void postAuthentication(UserRequest ureq) {
		// Check if disclaimer has been accepted
		if (registrationManager.needsToConfirmDisclaimer(authenticatedIdentity)) {
			// accept disclaimer first
			
			removeAsListenerAndDispose(disclaimerCtr);
			disclaimerCtr = new DisclaimerController(ureq, getWindowControl(), authenticatedIdentity, false);
			listenTo(disclaimerCtr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();	
			
		} else {
			// disclaimer acceptance not required		
			authenticated(ureq, authenticatedIdentity, authenticationProvider);
		}
	}
}
