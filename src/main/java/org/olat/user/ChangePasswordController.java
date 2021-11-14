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

package org.olat.user;

import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.SimpleMessageController;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.LoginModule;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.springframework.beans.factory.annotation.Autowired;

/**

 * Initial Date:  Jul 29, 2003
 *
 * @author Felix Jost, Florian Gnaegi
 * 
 * Comment:  
 * Subworkflow that allows the user to search for a user and choose the user from 
 * the list of users that match the search criteria. Users can be searched by
 * <ul>
 * <li>Username</li>
 * <li>First name</li>
 * <li>Last name</li>
 * <li>Email address</li>
 * </ul>
 * 
 */
public class ChangePasswordController extends BasicController implements SupportsAfterLoginInterceptor {
	
	private VelocityContainer myContent;
	private ChangePasswordForm chPwdForm;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;

	public ChangePasswordController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// if a user is not allowed to change his/her own password, say it here
		if (!userModule.isPwdChangeAllowed(ureq.getIdentity())) {
			String text = translate("notallowedtochangepwd", new String[] { WebappHelper.getMailConfig("mailSupport") });
			Controller simpleMsg = new SimpleMessageController(ureq, wControl, text, "o_warning");
			listenTo(simpleMsg); //register controller to be disposed automatically on dispose of Change password controller
			putInitialPanel(simpleMsg.getInitialComponent());	
		} else {
			myContent = createVelocityContainer("pwd");
			//adds "provider_..." variables to myContent
			exposePwdProviders(ureq.getIdentity());
			chPwdForm = new ChangePasswordForm(ureq, wControl, getIdentity());
			listenTo(chPwdForm);
			myContent.put("chpwdform", chPwdForm.getInitialComponent());
			putInitialPanel(myContent);
		}
	}
	
	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess.getRoles() == null || usess.getRoles().isInvitee() || usess.getRoles().isGuestOnly()) {
			return false;
		}
		if(usess.getRoles().isSystemAdmin()) {
			return false;
		}
		
		if(loginModule.isPasswordChangeOnce() || loginModule.isPasswordAgePolicyConfigured()) {
			AuthenticationProvider olatProvider = loginModule.getAuthenticationProvider(BaseSecurityModule.getDefaultAuthProviderIdentifier());
			return olatProvider.isEnabled() && !olatAuthenticationSpi
					.hasValidAuthentication(getIdentity(), loginModule.isPasswordChangeOnce(), loginModule.getPasswordAgePolicy(usess.getRoles()));
		}
		return false;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == chPwdForm) {
			if (event == Event.DONE_EVENT) {

				String oldPwd = chPwdForm.getOldPasswordValue();
				Identity provenIdent = null;
				Authentication ldapAuthentication = securityManager.findAuthentication(ureq.getIdentity(), LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
				if (ldapAuthentication != null) {
					LDAPError ldapError = new LDAPError();
					//fallback to OLAT if enabled happen automatically in LDAPAuthenticationController
					String userName = ldapAuthentication.getAuthusername();
					provenIdent = ldapLoginManager.authenticate(userName, oldPwd, ldapError);
				} else if(securityManager.findAuthentication(ureq.getIdentity(), BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER) != null) {
					provenIdent = olatAuthenticationSpi.authenticate(ureq.getIdentity(), ureq.getIdentity().getName(), oldPwd, new AuthenticationStatus());
				}

				if (provenIdent == null) {
					showError("error.password.noauth");	
				} else {
					String newPwd = chPwdForm.getNewPasswordValue();
					if(olatAuthenticationSpi.changePassword(ureq.getIdentity(), provenIdent, newPwd)) {			
						fireEvent(ureq, Event.DONE_EVENT);
						getLogger().info(Tracing.M_AUDIT, "Changed password for identity: {}", provenIdent.getKey());
						showInfo("password.successful");
					} else {
						showError("password.failed");
					}
				}
			} else if (event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(chPwdForm);
				chPwdForm = new ChangePasswordForm(ureq, getWindowControl(), getIdentity());
				listenTo(chPwdForm);
				myContent.put("chpwdform", chPwdForm.getInitialComponent());
			}
		}
	}
	
	private void exposePwdProviders(Identity identity) {
		// check if user has OLAT provider
		List<Authentication> authentications = securityManager.getAuthentications(identity);
		for(Authentication auth: authentications) {
			myContent.contextPut("provider_" + auth.getProvider(), Boolean.TRUE);
		}
		
		//LDAP Module propagate changes to password
		if(ldapLoginModule.isPropagatePasswordChangedOnLdapServer()) {
			myContent.contextPut("provider_LDAP_pwdchange", Boolean.TRUE);
		}
	}

}
