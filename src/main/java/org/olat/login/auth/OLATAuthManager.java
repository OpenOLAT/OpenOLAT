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
package org.olat.login.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.LoginModule;
import org.olat.login.OLATAuthenticationController;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  26.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Service("olatAuthenticationSpi")
public class OLATAuthManager implements AuthenticationSPI {
	
	private static final OLog log = Tracing.createLoggerFor(OLATAuthManager.class);
	
	@Autowired
	private OAuthLoginModule oauthModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private RegistrationManager registrationManager;
	
	/**
	 * 
	 * @param identity
	 * @param password
	 * @param provider
	 * @return
	 */
	@Override
	public Identity authenticate(Identity ident, String login, String password) {
		Authentication authentication;	
		if (ident == null) {
			// check for email instead of username if ident is null
			if(loginModule.isAllowLoginUsingEmail()) {
				if (MailHelper.isValidEmailAddress(login)){
					List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(login));
					// check for email changed with verification workflow
					if(identities.size() == 1) {
						ident = identities.get(0);
					} else if(identities.size() > 1) {
						log.error("more than one identity found with email::" + login, null);
					}
					
					if (ident == null) {
						ident = findIdentInChangingEmailWorkflow(login);
					}
				}
			} 
			
			if(ident == null) {
				authentication = securityManager.findAuthenticationByAuthusername(login, "OLAT");
			} else {
				authentication = securityManager.findAuthentication(ident, "OLAT");
			}
		} else {
			authentication = securityManager.findAuthentication(ident,  "OLAT");
		}

		if (authentication == null) {
			log.audit("Cannot authenticate user " + login + " via provider OLAT", OLATAuthenticationController.class.getName());
			return null;
		}
		
		// find OLAT authentication provider
		if (securityManager.checkCredentials(authentication, password))	{
			Algorithm algorithm = Algorithm.find(authentication.getAlgorithm());
			if(Algorithm.md5.equals(algorithm)) {
				Algorithm defAlgorithm = loginModule.getDefaultHashAlgorithm();
				authentication = securityManager.updateCredentials(authentication, password, defAlgorithm);
			}
			Identity identity = authentication.getIdentity();
			if(identity != null && webDAVAuthManager != null) {
				webDAVAuthManager.upgradePassword(identity, login, password);
			}
			return identity;
		}
		log.audit("Cannot authenticate user " + login + " via provider OLAT", OLATAuthenticationController.class.getName());
		return null;
	}
	
	/**
	 * 
	 * @param identity The identity
	 * @param changeOnce If the password need to be changed once
	 * @param maxAge The max age of the password in seconds
	 * @return
	 */
	public boolean hasValidAuthentication(IdentityRef identity, boolean changeOnce, int maxAge) {
		List<String> fullProviders = new ArrayList<>();
		fullProviders.add(LDAPAuthenticationController.PROVIDER_LDAP);
		fullProviders.add(ShibbolethDispatcher.PROVIDER_SHIB);
		for(OAuthSPI spi:oauthModule.getAllSPIs()) {
			fullProviders.add(spi.getProviderName());
		}
		return authenticationDao.hasValidOlatAuthentication(identity, changeOnce, maxAge, fullProviders);
	}

	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		//nothing to do
	}

	private Identity findIdentInChangingEmailWorkflow(String login){
		List<TemporaryKey> tk = registrationManager.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
		if (tk != null) {
			for (TemporaryKey temporaryKey : tk) {
				@SuppressWarnings("unchecked")
				Map<String, String> mails = (Map<String, String>)XStreamHelper.createXStreamInstance()
					.fromXML(temporaryKey.getEmailAddress());
				String currentEmail = mails.get("currentEMail");
				String changedEmail = mails.get("changedEMail");
				if (login.equals(changedEmail) && StringHelper.containsNonWhitespace(currentEmail)) {
					return securityManager.findIdentityByName(currentEmail);
				}
			}
		}
		return null;		
	}
	
	/**
	 * Change the password of an identity. if the given identity is a LDAP-User,
	 * the pw-change is propagated to LDAP (according to config) NOTE: caller of
	 * this method should check if identity is allowed to change it's own pw [
	 * UserModule.isPwdchangeallowed(Identity ident) ], applies only if doer
	 * equals identity
	 * 
	 * @param doer
	 *            Identity who is changing the password
	 * @param identity
	 *            Identity who's password is beeing changed.
	 * @param newPwd
	 *            New password.
	 * @return True upon success.
	 */
	public boolean changePassword(Identity doer, Identity identity, String newPwd) {
		if (doer==null) throw new AssertException("password changing identity cannot be undefined!");
		if (identity.getKey() == null) throw new AssertException("cannot change password on a nonpersisted identity");

		//o_clusterREVIEW
		identity = securityManager.loadIdentityByKey(identity.getKey());
		
		boolean allOk = false;
		
		Authentication ldapAuth = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
		if(ldapAuth != null) {
			if(ldapLoginModule.isPropagatePasswordChangedOnLdapServer()) {
				LDAPError ldapError = new LDAPError();
				ldapLoginManager.changePassword(identity, newPwd, ldapError);
				log.audit(doer.getName() + " change the password on the LDAP server for identity: " + identity.getName());
				allOk = ldapError.isEmpty();

				if(allOk && ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()) {
					allOk &= changeOlatPassword(doer, identity, identity.getName(), newPwd);
				}
			}
		} else {
			allOk = changeOlatPassword(doer, identity, identity.getName(), newPwd);
		}
		if(allOk) {
			sendConfirmationEmail(doer, identity);
			//remove 
			try {
				loginModule.clearFailedLoginAttempts(identity.getName());
				loginModule.clearFailedLoginAttempts(identity.getUser().getEmail());
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return allOk;
	}
	
	private void sendConfirmationEmail(Identity doer, Identity identity) {
		String prefsLanguage = identity.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefsLanguage);
		Translator translator = Util.createPackageTranslator(OLATAuthenticationController.class, locale);

		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("changepw", 0l));
		String changePwUrl = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		String[] args = new String[] {
				identity.getName(),//0: changed users username
				UserManager.getInstance().getUserDisplayEmail(identity, locale),// 1: changed users email address
				userManager.getUserDisplayName(doer.getUser()),// 2: Name (first and last name) of user who changed the password
				WebappHelper.getMailConfig("mailSupport"), //3: configured support email address
				changePwUrl //4: direct link to change password workflow (e.g. https://xx.xx.xx/olat/url/changepw/0)
		};
		String subject = translator.translate("mail.pwd.subject", args);
		String body = translator.translate("mail.pwd.body", args);

		MailContext context = new MailContextImpl(null, null, "[Identity:" + identity.getKey() + "]");
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		bundle.setToId(identity);
		bundle.setContent(subject, body);
		mailManager.sendMessage(bundle);
	}
	
	/**
	 * This update the OLAT and the HA1 passwords
	 * @param doer
	 * @param identity
	 * @param newPwd
	 * @return
	 */
	public boolean changeOlatPassword(Identity doer, Identity identity, String username, String newPwd) {
		Authentication auth = securityManager.findAuthentication(identity, "OLAT");
		if (auth == null) { // create new authentication for provider OLAT
			securityManager.createAndPersistAuthentication(identity, "OLAT", identity.getName(), newPwd, loginModule.getDefaultHashAlgorithm());
			log.audit(doer.getName() + " created new authenticatin for identity: " + identity.getName());
		} else {
			securityManager.updateCredentials(auth, newPwd, loginModule.getDefaultHashAlgorithm());
			log.audit(doer.getName() + " set new password for identity: " + identity.getName());
		}
		
		if(identity != null && StringHelper.containsNonWhitespace(username) && webDAVAuthManager != null) {
			webDAVAuthManager.changeDigestPassword(doer, identity, newPwd);
		}
		return true;
	}
	
	public boolean synchronizeOlatPasswordAndUsername(Identity doer, Identity identity, String username, String newPwd) {
		Authentication auth = securityManager.findAuthentication(identity, "OLAT");
		if (auth == null) { // create new authentication for provider OLAT
			securityManager.createAndPersistAuthentication(identity, "OLAT", username, newPwd, loginModule.getDefaultHashAlgorithm());
			log.audit(doer.getName() + " created new authenticatin for identity: " + identity.getName());
		} else {
			//update credentials
			if(!securityManager.checkCredentials(auth, newPwd)) {
				auth = securityManager.updateCredentials(auth, newPwd, loginModule.getDefaultHashAlgorithm());
			}
			
			if(!username.equals(auth.getAuthusername())) {
				auth.setAuthusername(username);
				securityManager.updateAuthentication(auth);
			}

			log.audit(doer.getName() + " set new password for identity: " + identity.getName());
		}
		
		if(identity != null && StringHelper.containsNonWhitespace(username) && webDAVAuthManager != null) {
			webDAVAuthManager.changeDigestPassword(doer, identity, newPwd);
		}
		return true;
	}

	/**
	 * to change password without knowing exactly who is changing it -> change as admin
	 * @param identity
	 * @param newPwd
	 * @return
	 */
	public boolean changePasswordAsAdmin(Identity identity, String newPwd) {
		Identity adminUserIdentity = securityManager.findIdentityByName("administrator");
		return changePassword(adminUserIdentity, identity, newPwd);
	}
	
	/**
	 * to change password by password forgotten link at login screen
	 * @param identity
	 * @param newPwd
	 * @return
	 */
	public boolean changePasswordByPasswordForgottenLink(Identity identity, String newPwd) {
		return changePassword(identity, identity, newPwd);
	}
	

	/**
	 * Check the credential history if configured and if the user
	 * has not a LDAP credential.
	 * 
	 * @param identity The identity
	 * @param password The new password
	 * @return true if the new password is valid against the history
	 */
	public boolean checkCredentialHistory(Identity identity, String password) {
		boolean ok = true;
		int historyLength = loginModule.getPasswordHistory();
		if(historyLength > 0 && 
				(!ldapLoginModule.isLDAPEnabled()
						|| !authenticationDao.hasAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP))) {
			ok = securityManager.checkCredentialHistory(identity, BaseSecurityModule.getDefaultAuthProviderIdentifier(), password);
		}
		return ok;
	}
	
}
