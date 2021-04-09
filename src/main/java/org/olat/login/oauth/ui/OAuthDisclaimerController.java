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
package org.olat.login.oauth.ui;

import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
import org.olat.login.oauth.OAuthLoginManager;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.OAuthUserCreator;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationForm2;
import org.olat.registration.RegistrationManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This controller opens the disclaimer and if accepted
 * create the user in background.
 * 
 * Initial date: 20 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthDisclaimerController extends FormBasicController implements Activateable2 {
	
	public static final String USERPROPERTIES_FORM_IDENTIFIER = OAuthDisclaimerController.class.getCanonicalName();
	
	private final OAuthUser user;
	private final OAuthSPI provider;

	private CloseableModalController cmc;
	private DisclaimerController disclaimerController;

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OAuthLoginManager oauthLoginManager;
	@Autowired
	private RegistrationManager registrationManager;
	
	public OAuthDisclaimerController(UserRequest ureq, WindowControl wControl,
			OAuthUser user, OAuthSPI provider) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RegistrationForm2.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));

		this.user = user;
		this.provider = provider;
		initForm(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		disclaimerController = new DisclaimerController(ureq, getWindowControl(), null, false);
		listenTo(disclaimerController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerController.getInitialComponent(),
				true, translate("disclaimer.title"));
		cmc.activate();
		listenTo(cmc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,	UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(disclaimerController == source) {
			cmc.deactivate();
			
			if (event == Event.DONE_EVENT) {
				// User accepted disclaimer, do login now
				doCreateUser(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				// User did not accept, workflow ends here
				showWarning("disclaimer.form.cancelled");
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(disclaimerController);
		removeAsListenerAndDispose(cmc);
		disclaimerController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doCreateUser(UserRequest ureq) {
		Identity authenticatedIdentity;
		if(provider instanceof OAuthUserCreator) {
			authenticatedIdentity =((OAuthUserCreator)provider).createUser(user);
		} else {
			authenticatedIdentity = oauthLoginManager.createIdentity(user, provider.getProviderName());
		}
		registrationManager.setHasConfirmedDislaimer(authenticatedIdentity);
		doLoginAndRegister(authenticatedIdentity, ureq);
	}
	
	private void doLoginAndRegister(Identity authIdentity, UserRequest ureq) {
		// prepare redirects to home etc, set status
		int loginStatus = AuthHelper.doLogin(authIdentity, null, ureq);
		if (loginStatus == AuthHelper.LOGIN_OK) {
			//update last login date and register active user
			securityManager.setIdentityLastLogin(authIdentity);
		} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
			DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
		} else {
			getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
		}
	}
}