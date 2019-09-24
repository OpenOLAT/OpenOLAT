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

package org.olat.core.commons.services.webdav.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.auth.OLATAuthManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * Authentication provider for WebDAV
 * 
 * <P>
 * Initial Date:  13 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service("webDAVAuthenticationSpi")
public class WebDAVAuthManager implements AuthenticationSPI {
	
	public static final String PROVIDER_WEBDAV = "WEBDAV";
	public static final String PROVIDER_WEBDAV_EMAIL = "WEBDAV-E";
	public static final String PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL = "WEBDAV-I";
	public static final String PROVIDER_HA1 = "HA1";
	public static final String PROVIDER_HA1_EMAIL = "HA1-E";
	public static final String PROVIDER_HA1_INSTITUTIONAL_EMAIL = "HA1-I";
	
	private static final Logger log = Tracing.createLoggerFor(WebDAVAuthManager.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	@Autowired
	private UserModule userModule;
	
	public Identity digestAuthentication(String httpMethod, DigestAuthentication digestAuth) {
		String username = digestAuth.getUsername();
		
		List<String> providers = new ArrayList<>(3);
		providers.add(PROVIDER_HA1);
		if (userModule.isEmailUnique()) {
			providers.add(PROVIDER_HA1_EMAIL);
			providers.add(PROVIDER_HA1_INSTITUTIONAL_EMAIL);
		}
		
		List<Authentication> authentications = securityManager.findAuthenticationByAuthusername(username, providers);
		if(authentications != null && !authentications.isEmpty()) {
			for(Authentication authentication:authentications) {
				if("auth".equals(digestAuth.getQop())) {
					String nonce = digestAuth.getNonce();
					String response = digestAuth.getResponse();
	
					String ha1 = authentication.getCredential();
					
					String a2 = httpMethod + ":" + digestAuth.getUri();
					String ha2 = Encoder.md5hash(a2);
					
					String ver = ha1 + ":" + nonce + ":" + digestAuth.getNc() + ":" + digestAuth.getCnonce() + ":" + digestAuth.getQop() + ":" + ha2;
					String verity = Encoder.md5hash(ver);
					if(verity.equals(response)) {
						return authentication.getIdentity();
					} else if(log.isInfoEnabled()) {
						// don't log as error, happens all the time with certain clients, e.g. Microsoft-WebDAV-MiniRedir
						log.info("Verity: {} doesn't equals response: {}", verity, response);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Identity authenticate(Identity identity, String login, String password) {
		List<String> providers = new ArrayList<>(3);
		providers.add(PROVIDER_WEBDAV);
		if (userModule.isEmailUnique()) {
			providers.add(PROVIDER_HA1_EMAIL);
			providers.add(PROVIDER_HA1_INSTITUTIONAL_EMAIL);
		}
		
		List<Authentication> authentications = null;
		if (identity != null) {
			authentications = securityManager.findAuthentications(identity, providers);
		} else {
			authentications = securityManager.findAuthenticationByAuthusername(login, providers);
		}

		if(authentications == null || authentications.isEmpty()) {
			//fallback to standard OLAT authentication
			return olatAuthenticationSpi.authenticate(identity, login, password);
		}
		
		Identity authenticatedIdentity = authentications.get(0).getIdentity();
		boolean visible = securityManager.isIdentityVisible(authenticatedIdentity);
		if (!visible) {
			return null;
		}
		
		for(Authentication authentication:authentications) {
			if (securityManager.checkCredentials(authentication, password))	{
				Algorithm algorithm = Algorithm.find(authentication.getAlgorithm());
				if(Algorithm.md5.equals(algorithm)) {
					authentication = securityManager.updateCredentials(authentication, password, loginModule.getDefaultHashAlgorithm());
				}
				return authentication.getIdentity();
			}
		}
		return null;
	}
	
	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		if(webDAVModule.isEnabled() && webDAVModule.isDigestAuthenticationEnabled()) {
			List<Authentication> digestAuths = securityManager.getAuthentications(identity);
			updateDigestPasswords(identity, identity, password, digestAuths);
		}
	}

	/**
	 * Change the WEBDAV-Password of an identity
	 * @param doer Identity who is changing the password
	 * @param identity Identity who's password is being changed.
	 * @param newPwd New password.
	 * @return True upon success.
	 */
	public boolean changePassword(Identity doer, Identity identity, String newPwd) {
		if (doer==null) {
			throw new AssertException("password changing identity cannot be undefined!");
		}
		if (identity == null || identity.getKey() == null) {
			throw new AssertException("cannot change password on a nonpersisted identity");
		}

		//For Basic
		List<Authentication> auths = securityManager.getAuthentications(identity);
		updateWebdavPassword(doer, identity, newPwd, auths);
		//For Digest
		changeDigestPassword(doer, identity,  newPwd);
		return true;
	}
	
	private void updateWebdavPassword(Identity doer, Identity identity, String password, List<Authentication> authentications) {
		updateWebDAVPassword(doer, identity, identity.getName(), password, PROVIDER_WEBDAV, authentications);
		if(userModule.isEmailUnique() && StringHelper.containsNonWhitespace(identity.getUser().getEmail())) {
			updateWebDAVPassword(doer, identity, identity.getUser().getEmail(), password, PROVIDER_WEBDAV_EMAIL, authentications);
		} else {
			removePassword(PROVIDER_WEBDAV_EMAIL, authentications);
		}
		if(userModule.isEmailUnique() && StringHelper.containsNonWhitespace(identity.getUser().getInstitutionalEmail())) {
			updateWebDAVPassword(doer, identity, identity.getUser().getInstitutionalEmail(), password, PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL, authentications);
		} else {
			removePassword(PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL, authentications);
		}

		for(Authentication authentication:authentications) {
			if(authentication.getProvider().startsWith(PROVIDER_WEBDAV)) {
				securityManager.deleteAuthentication(authentication);
			}
		}
	}
	
	private void updateWebDAVPassword(Identity doer, Identity identity, String authUsername, String password,
			String provider, List<Authentication> authentications) {
		Authentication authentication = getAndRemoveAuthentication(provider, authentications);
		if (authentication == null) { // create new authentication for provider OLAT
			try {
				dbInstance.commit();
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				securityManager.createAndPersistAuthentication(reloadedIdentity, provider, authUsername, password, loginModule.getDefaultHashAlgorithm());
				log.info(Tracing.M_AUDIT, doer.getKey() + " created new WebDAV authentication for identity: " + identity.getKey() + " (" + authUsername + ")");
			} catch (DBRuntimeException e) {
				log.error("Cannot create webdav password with provider " + provider + " for identity:" + identity, e);
				dbInstance.commit();
			}
		} else {
			try {
				dbInstance.commit();
				securityManager.updateCredentials(authentication, password, loginModule.getDefaultHashAlgorithm());
				log.info(Tracing.M_AUDIT, doer.getKey() + " set new WebDAV password for identity: " + identity.getKey() + " (" + authUsername + ")");
			} catch (Exception e) {
				log.error("Cannot update webdav password with provider " + provider + " for identity:" + identity, e);
				dbInstance.commit();
			}
		}
	}
	
	public boolean changeDigestPassword(Identity doer, Identity identity, String newPwd) {
		if (doer == null) {
			throw new AssertException("password changing identity cannot be undefined!");
		}
		if (identity == null || identity.getKey() == null) {
			throw new AssertException("cannot change password on a nonpersisted identity");
		}

		//For Digest
		if(webDAVModule.isDigestAuthenticationEnabled()) {
			List<Authentication> ha1Authentications = securityManager.getAuthentications(identity);
			updateDigestPasswords(doer, identity, newPwd, ha1Authentications);
		}
		return true;
	}
	
	private void updateDigestPasswords(Identity doer, Identity identity, String newPwd,
			List<Authentication> authentications) {
		updateDigestPassword(doer, identity, identity.getName(), newPwd, PROVIDER_HA1, authentications);

		if(userModule.isEmailUnique() && StringHelper.containsNonWhitespace(identity.getUser().getEmail())) {
			updateDigestPassword(doer, identity, identity.getUser().getEmail(), newPwd, PROVIDER_HA1_EMAIL, authentications);
		} else {
			removePassword(PROVIDER_HA1_EMAIL, authentications);
		}
		if(userModule.isEmailUnique() && StringHelper.containsNonWhitespace(identity.getUser().getInstitutionalEmail())) {
			updateDigestPassword(doer, identity, identity.getUser().getInstitutionalEmail(), newPwd, PROVIDER_HA1_INSTITUTIONAL_EMAIL, authentications);
		} else {
			removePassword(PROVIDER_HA1_INSTITUTIONAL_EMAIL, authentications);
		}
		
		for(Authentication authentication:authentications) {
			if(authentication.getProvider().startsWith(PROVIDER_HA1)) {
				securityManager.deleteAuthentication(authentication);
			}
		}
	}
	
	private void updateDigestPassword(Identity doer, Identity identity, String authUsername, String password,
			String provider, List<Authentication> authentications) {
		String digestToken = authUsername + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + password;
		Authentication authHa1 = getAndRemoveAuthentication(provider, authentications);
		if (authHa1 == null) { // create new authentication for provider OLAT
			try {
				dbInstance.commit();
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				securityManager.createAndPersistAuthentication(reloadedIdentity, provider, authUsername, digestToken, Encoder.Algorithm.md5_iso_8859_1);
				log.info(Tracing.M_AUDIT, doer.getKey() + " created new WebDAV (HA1) authentication for identity: " + identity.getKey() + " (" + authUsername + ")");
			} catch(DBRuntimeException e) {
				log.error("Cannot create digest password with provider " + provider + " for identity:" + identity, e);
				dbInstance.commit();
			}
		} else {
			String md5DigestToken = Encoder.encrypt(digestToken, null, Encoder.Algorithm.md5_iso_8859_1);
			if (!md5DigestToken.equals(authHa1.getCredential()) || !authHa1.getAuthusername().equals(authUsername)) {
				try {
					authHa1.setCredential(md5DigestToken);
					authHa1.setAuthusername(authUsername);
					authHa1.setAlgorithm(Encoder.Algorithm.md5_iso_8859_1.name());
					securityManager.updateAuthentication(authHa1);
					log.info(Tracing.M_AUDIT, doer.getKey() + " set new WebDAV (HA1) password for identity: " + identity.getKey() + " (" + authUsername + ")");
				} catch (DBRuntimeException e) {
					log.error("Cannot update digest password with provider " + provider + " for identity:" + identity, e);
					dbInstance.commit();
				}
			}
		}
	}
	
	private void removePassword(String provider, List<Authentication> authentications) {
		Authentication authentication = getAndRemoveAuthentication(provider, authentications);
		if(authentication != null) {
			securityManager.deleteAuthentication(authentication);
		}	
	}
	
	private Authentication getAndRemoveAuthentication(String provider, List<Authentication> authentications) {
		if(authentications != null && !authentications.isEmpty()) {
			for(Authentication authentication:authentications) {
				if(provider.equals(authentication.getProvider())) {
					authentications.remove(authentication);
					return authentication;
				}
			}
		}
		return null;
	}
}
