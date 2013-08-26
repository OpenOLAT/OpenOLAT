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
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationSPI;
import org.olat.login.auth.OLATAuthManager;


/**
 * 
 * Description:<br>
 * Authentication provider for WebDAV
 * 
 * <P>
 * Initial Date:  13 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WebDAVAuthManager implements AuthenticationSPI {
	
	public static final String PROVIDER_WEBDAV = "WEBDAV";
	
	private static final OLog log = Tracing.createLoggerFor(WebDAVAuthManager.class);
	
	private BaseSecurity securityManager;
	private OLATAuthManager olatAuthenticationSpi;
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	/**
	 * [used by Spring]
	 * @param olatAuthenticationSpi
	 */
	public void setOlatAuthenticationSpi(OLATAuthManager olatAuthenticationSpi) {
		this.olatAuthenticationSpi = olatAuthenticationSpi;
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
				authentication = securityManager.updateCredentials(authentication, password, LoginModule.getDefaultHashAlgorithm());
			}
			return authentication.getIdentity();
		}
		return null;
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

		Authentication auth = securityManager.findAuthentication(identity, PROVIDER_WEBDAV);
		if (auth == null) { // create new authentication for provider OLAT
			Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
			auth = securityManager.createAndPersistAuthentication(reloadedIdentity, PROVIDER_WEBDAV, identity.getName(), newPwd, LoginModule.getDefaultHashAlgorithm());
			log.audit(doer.getName() + " created new WebDAV authenticatin for identity: " + identity.getName());
		} else {
			auth = securityManager.updateCredentials(auth, newPwd, LoginModule.getDefaultHashAlgorithm());
			log.audit(doer.getName() + " set new WebDAV password for identity: " +identity.getName());
		}
		return true;
	}
}
