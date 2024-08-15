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

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.Encoder;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.UserSession;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 * Initial date: 21.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OLATAuthManager authManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private WebDAVManagerImpl webDAVManager;
	
	@Test
	public void handleDigestAuthentication() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("dav-user-3");
		authManager.authenticate(id.getIdentity(), id.getLogin(), id.getPassword(), new AuthenticationStatus());
		dbInstance.commitAndCloseSession();// derived WebDAV authentications saved
		
		HttpServletRequest request = new MockHttpServletRequest();
		String username = id.getUser().getEmail();
		String nonce = UUID.randomUUID().toString();
		String uri = "https://www.openolat.com";
		String cnonce = UUID.randomUUID().toString();
		String nc = "nc";
		String qop = "auth";
		String token = username + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + JunitTestHelper.PWD;
		String digestedToken = Encoder.encrypt(token, null, Algorithm.md5_iso_8859_1);
		String ha2 = Encoder.md5hash( ":" + uri);
		String response = digestedToken + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
		String digestedReponse = Encoder.md5hash(response);
		
		DigestAuthentication digested = new DigestAuthentication(username, WebDAVManagerImpl.BASIC_AUTH_REALM, nonce, uri, cnonce, nc, digestedReponse, qop);
		UserSession usess = webDAVManager.handleDigestAuthentication(digested, request);
		Assert.assertNotNull(usess);
		dbInstance.commit();
	}
	
	@Test
	public void handleDigestAuthenticationSpecialCharacter() {
		String username = "zgc_1";
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(username, "w\u20ACbdav");
		authManager.authenticate(id, username, "w\u20ACbdav", new AuthenticationStatus());
		dbInstance.commitAndCloseSession();// derived WebDAV authentications saved
		
		HttpServletRequest request = new MockHttpServletRequest("PROPFIND", "/");
		String nonce = "57f6ad3c28094eeb88bca5791dc9c777";
		String uri = "/";
		String cnonce = "66285ba3e389aaeee6c536d06ea7fa3d";
		String nc = "00000001";
		String qop = "auth";
		String response = "341458001c109fbd3668bbac4cdf1e88";

		DigestAuthentication digested = new DigestAuthentication(username, WebDAVManagerImpl.BASIC_AUTH_REALM, nonce, uri, cnonce, nc, response, qop);
		UserSession usess = webDAVManager.handleDigestAuthentication(digested, request);
		Assert.assertNotNull(usess);
		dbInstance.commit();
	}
	
	@Test
	public void handleDigestAuthentication_denied() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("dav-user-3");
		authManager.authenticate(id.getIdentity(), id.getLogin(), id.getPassword(), new AuthenticationStatus());
		dbInstance.commitAndCloseSession();// derived WebDAV authentications saved
		
		HttpServletRequest request = new MockHttpServletRequest("POST", "https://www.openolat.col");
		String username = id.getUser().getEmail();
		String nonce = UUID.randomUUID().toString();
		String uri = "https://www.openolat.com";
		String cnonce = UUID.randomUUID().toString();
		String nc = "nc";
		String qop = "auth";
		String token = username + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + JunitTestHelper.PWD;
		String digestedToken = Encoder.encrypt(token, null, Algorithm.md5_iso_8859_1);
		String ha2 = Encoder.md5hash("POST:" + uri);
		String response = digestedToken + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
		String digestedReponse = Encoder.md5hash(response);
		
		// login successful
		DigestAuthentication digested = new DigestAuthentication(username, WebDAVManagerImpl.BASIC_AUTH_REALM, nonce, uri, cnonce, nc, digestedReponse, qop);
		UserSession usess = webDAVManager.handleDigestAuthentication(digested, request);
		Assert.assertNotNull(usess);
		dbInstance.commit();
		
		Identity identity = securityManager.saveIdentityStatus(id.getIdentity(), Identity.STATUS_LOGIN_DENIED, id.getIdentity());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identity);
		
		UserSession deniedSession = webDAVManager.handleDigestAuthentication(digested, request);
		Assert.assertNull(deniedSession);
	}
	
	@Test
	public void blockedDigestAuthentication() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("dav-user-6");
		authManager.authenticate(id.getIdentity(), id.getLogin(), id.getPassword(), new AuthenticationStatus());
		dbInstance.commitAndCloseSession();// derived WebDAV authentications saved
		
		HttpServletRequest request = new MockHttpServletRequest();
		String username = id.getUser().getEmail();
		String nonce = UUID.randomUUID().toString();
		String uri = "https://www.openolat.com";
		String cnonce = UUID.randomUUID().toString();
		String nc = "nc";
		String qop = "auth";
		String token = username + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + JunitTestHelper.PWD;
		String digestedToken = Encoder.encrypt(token, null, Algorithm.md5_iso_8859_1);
		String ha2 = Encoder.md5hash( ":" + uri);
		String response = digestedToken + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2 + "-error";// Add an error
		String digestedReponse = Encoder.md5hash(response);
		
		for(int i=10; i-->0; ) {
			DigestAuthentication digested = new DigestAuthentication(username, WebDAVManagerImpl.BASIC_AUTH_REALM, nonce, uri, cnonce, nc, digestedReponse, qop);
			UserSession usess = webDAVManager.handleDigestAuthentication(digested, request);
			Assert.assertNull(usess);
			dbInstance.commit();
		}
		
		boolean blocked = loginModule.isLoginBlocked(username);
		Assert.assertTrue(blocked);
	}
}