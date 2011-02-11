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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.login.auth;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.login.OLATAuthenticationController;


/**
 * 
 * Description:<br>
 * Authentication provider for WebDAV
 * 
 * <P>
 * Initial Date:  13 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WebDAVAuthManager {
	
	public static final String PROVIDER_WEBDAV = "WEBDAV";
	
	private static final OLog log = Tracing.createLoggerFor(WebDAVAuthManager.class);
	
	
	/**
	 * Change the WEBDAV-Password of an identity
	 * @param doer Identity who is changing the password
	 * @param identity Identity who's password is being changed.
	 * @param newPwd New password.
	 * @return True upon success.
	 */
	public static boolean changePassword(Identity doer, Identity identity, String newPwd) {
		if (doer==null) throw new AssertException("password changing identity cannot be undefined!");
		
		if (identity.getKey() == null) throw new AssertException("cannot change password on a nonpersisted identity");
		// password's length is limited to 128 chars. 
		// The 128 bit MD5 hash is converted into a 32 character long String
		String hashedPwd = Encoder.encrypt(newPwd);
		
		identity = (Identity) DBFactory.getInstance().loadObject(identity);
		
		return changeWebDAVPassword(doer, identity, hashedPwd);
	}
	
	private static boolean changeWebDAVPassword(Identity doer, Identity identity, String hashedPwd) {
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(identity, PROVIDER_WEBDAV);
		if (auth == null) { // create new authentication for provider OLAT
			auth = BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, PROVIDER_WEBDAV, identity.getName(), hashedPwd);
			log.audit(doer.getName() + " created new WebDAV authenticatin for identity: " + identity.getName());
		}

		auth.setCredential(hashedPwd);
		DBFactory.getInstance().updateObject(auth);
		log.audit(doer.getName() + " set new WebDAV password for identity: " +identity.getName());
		return true;
	}
	
	/**
	 * Authenticate against the WEBDAV Authentication provider.
	 * @param login
	 * @param pass
	 * @return Identity if authentication was successful, null otherwise.
	 */
	public static Identity authenticate(String login, String pass) {
		Identity ident = BaseSecurityManager.getInstance().findIdentityByName(login);
		if (ident == null) return null;
		boolean visible = BaseSecurityManager.getInstance().isIdentityVisible(login);
		if (!visible) return null;
		
		//find WEBDAV authentication provider
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(ident, PROVIDER_WEBDAV);
		if (auth != null && auth.getCredential().equals(Encoder.encrypt(pass)))	return ident;
		//fallback to OLAT authentication provider 
		return OLATAuthenticationController.authenticate(login, pass);
	}
}
