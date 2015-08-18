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

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.auth.OLATAuthManager;
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
	public static final String PROVIDER_HA1 = "HA1";
	
	private static final OLog log = Tracing.createLoggerFor(WebDAVAuthManager.class);

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	public Identity digestAuthentication(String httpMethod, DigestAuthentication digestAuth) {
		String username = digestAuth.getUsername();
		
		Authentication olatAuth = securityManager.findAuthenticationByAuthusername(username, WebDAVAuthManager.PROVIDER_HA1);
		if(olatAuth != null) {
			if("auth".equals(digestAuth.getQop())) {
				String nonce = digestAuth.getNonce();
				String response = digestAuth.getResponse();

				String ha1 = olatAuth.getCredential();
				
				String a2 = httpMethod + ":" + digestAuth.getUri();
				String ha2 = Encoder.md5hash(a2);
				
				String ver = ha1 + ":" + nonce + ":" + digestAuth.getNc() + ":" + digestAuth.getCnonce() + ":" + digestAuth.getQop() + ":" + ha2;
				String verity = Encoder.md5hash(ver);
				if(verity.equals(response)) {
					Identity identity = olatAuth.getIdentity();
					return identity;
				} else {
					log.error("Verity doesn't equals response");
				}
			}
		}
		return null;
	}

	@Override
	public Identity authenticate(Identity identity, String login, String password) {
		Authentication authentication = null;
		if (identity != null) {
			authentication = securityManager.findAuthentication(identity, PROVIDER_WEBDAV);
		} else {
			authentication = securityManager.findAuthenticationByAuthusername(login, PROVIDER_WEBDAV);
		}

		if(authentication == null) {
			//fallback to standard OLAT authentication
			return olatAuthenticationSpi.authenticate(identity, login, password);
		}
		
		Identity authenticatedIdentity = authentication.getIdentity();
		boolean visible = securityManager.isIdentityVisible(authenticatedIdentity);
		if (!visible) {
			return null;
		}
		
		if (securityManager.checkCredentials(authentication, password))	{
			Algorithm algorithm = Algorithm.find(authentication.getAlgorithm());
			if(Algorithm.md5.equals(algorithm)) {
				authentication = securityManager.updateCredentials(authentication, password, loginModule.getDefaultHashAlgorithm());
			}
			return authentication.getIdentity();
		}
		return null;
	}
	
	@Override
	public void upgradePassword(Identity identity, String login, String password) {
		if(webDAVModule.isEnabled() && webDAVModule.isDigestAuthenticationEnabled()) {
			Authentication digestAuth = securityManager.findAuthentication(identity, PROVIDER_HA1);
			if(digestAuth == null) {
				String digestToken = identity.getName() + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + password;
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				securityManager.createAndPersistAuthentication(reloadedIdentity, PROVIDER_HA1, identity.getName(), digestToken, Encoder.Algorithm.md5_noSalt);
			}
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
		if (doer==null) throw new AssertException("password changing identity cannot be undefined!");
		if (identity == null || identity.getKey() == null)
			throw new AssertException("cannot change password on a nonpersisted identity");

		
		{//For Basic
			Authentication auth = securityManager.findAuthentication(identity, PROVIDER_WEBDAV);
			if (auth == null) { // create new authentication for provider OLAT
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				auth = securityManager.createAndPersistAuthentication(reloadedIdentity, PROVIDER_WEBDAV, identity.getName(), newPwd, loginModule.getDefaultHashAlgorithm());
				log.audit(doer.getName() + " created new WebDAV authentication for identity: " + identity.getName());
			} else {
				auth = securityManager.updateCredentials(auth, newPwd, loginModule.getDefaultHashAlgorithm());
				log.audit(doer.getName() + " set new WebDAV password for identity: " +identity.getName());
			}
		}
		
		//For Digest
		changeDigestPassword(doer, identity, newPwd);
		return true;
	}
	
	public boolean changeDigestPassword(Identity doer, Identity identity, String newPwd) {
		if (doer==null) throw new AssertException("password changing identity cannot be undefined!");
		if (identity == null || identity.getKey() == null)
			throw new AssertException("cannot change password on a nonpersisted identity");

		
		//For Digest
		if(webDAVModule.isDigestAuthenticationEnabled()) {
			Authentication authHa1 = securityManager.findAuthentication(identity, PROVIDER_HA1);
			String digestToken = identity.getName() + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + newPwd;
			if (authHa1 == null) { // create new authentication for provider OLAT
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				authHa1 = securityManager.createAndPersistAuthentication(reloadedIdentity, PROVIDER_HA1, identity.getName(), digestToken, Encoder.Algorithm.md5_noSalt);
				log.audit(doer.getName() + " created new WebDAV authenticatin for identity: " + identity.getName());
			} else {
				authHa1 = securityManager.updateCredentials(authHa1, digestToken, Encoder.Algorithm.md5_noSalt);
				log.audit(doer.getName() + " set new WebDAV password for identity: " +identity.getName());
			}
		}
		
		return true;
	}
}
