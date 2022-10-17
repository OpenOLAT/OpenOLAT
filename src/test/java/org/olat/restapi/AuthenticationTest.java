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
* <p>
*/

package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Test the authentication service
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class AuthenticationTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(AuthenticationTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;

	@Test
	public void testSessionCookieLogin() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("administrator").queryParam("password", "openolat").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpResponse code = conn.execute(method);
		assertEquals(200, code.getStatusLine().getStatusCode());
		String response = EntityUtils.toString(code.getEntity());
		assertTrue(response.startsWith("<hello"));
		assertTrue(response.endsWith("Hello administrator</hello>"));
	
		List<Cookie> cookies = conn.getCookieStore().getCookies();
		assertNotNull(cookies);
		assertTrue(cookies.size() > 0);
		
		conn.shutdown();
  }
	
	@Test
	public void testSessionCookieLoginHttpClient4() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("administrator").queryParam("password", "openolat").build();

		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String hello = EntityUtils.toString(response.getEntity());
		assertTrue(hello.startsWith("<hello"));
		assertTrue(hello.endsWith("Hello administrator</hello>"));
		List<org.apache.http.cookie.Cookie> cookies = conn.getCookieStore().getCookies();

		assertNotNull(cookies);
		assertFalse(cookies.isEmpty());
		assertNotNull(response.getFirstHeader(RestSecurityHelper.SEC_TOKEN));
		
		conn.shutdown();
  }
	
	@Test
	public void testWrongPassword() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("administrator").queryParam("password", "blabla").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpResponse code = conn.execute(method);
		assertEquals(401, code.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}
	
	@Test
	public void testUnkownUser() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("treuitr").queryParam("password", "blabla").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpResponse code = conn.execute(method);
		assertEquals(401, code.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}
	
	@Test
	public void testBasicAuthentication() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		
		//path is protected
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path("version").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
		method.setHeader("Authorization", "Basic " + StringHelper.encodeBase64("administrator:openolat"));
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String securityToken = conn.getSecurityToken(response);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	@Test
	public void testWebStandardAuthentication() throws IOException, URISyntaxException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path("version").build();
		RestConnection conn = new RestConnection(uri.toURL(), "administrator", "openolat");
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String securityToken = conn.getSecurityToken(response);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	@Test
	public void testAuthenticationDenied() throws IOException, URISyntaxException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-denied");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		conn.shutdown();
		
		Identity savedIdentity = securityManager.saveIdentityStatus(id.getIdentity(), Identity.STATUS_LOGIN_DENIED, id.getIdentity());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedIdentity);
		
		RestConnection conn2 = new RestConnection();
		Assert.assertFalse(conn2.login(id.getLogin(), id.getPassword()));
		conn2.shutdown();
	}
	
	@Test
	public void testBasicAuthentication_concurrent() throws IOException, URISyntaxException {
		
		int numOfThreads = 25;
		final CountDownLatch doneSignal = new CountDownLatch(numOfThreads);
		
		AuthenticationThread[] threads = new AuthenticationThread[numOfThreads];
		for(int i=numOfThreads; i-->0; ) {
			threads[i] = new AuthenticationThread(doneSignal);
		}
		
		for(int i=numOfThreads; i-->0; ) {
			threads[i].start();
		}

		try {
			boolean interrupt = doneSignal.await(2400, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		int errorCount = 0;
		for(int i=numOfThreads; i-->0; ) {
			errorCount += threads[i].getErrorCount();
		}
		Assert.assertEquals(0, errorCount);
	}
	
	private class AuthenticationThread extends Thread {

		private int errorCount;
		private final CountDownLatch doneSignal;
		
		public AuthenticationThread(CountDownLatch doneSignal) {
			this.doneSignal = doneSignal;
		}
		
		public int getErrorCount() {
			return errorCount;
		}
		
		@Override
		public void run() {
			RestConnection conn = new RestConnection();
			try {
				sleep(10);
				
				//path is protected
				URI uri = UriBuilder.fromUri(getContextURI()).path("users").path("version").build();
				HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
				method.setHeader("Authorization", "Basic " + StringHelper.encodeBase64("administrator:openolat"));
				HttpResponse response = conn.execute(method);
				if(200 != response.getStatusLine().getStatusCode()) {
					errorCount++;
				} else if(!StringHelper.containsNonWhitespace(conn.getSecurityToken(response))) {
					errorCount++;
				}
			} catch (Exception e) {
				log.error("", e);
				errorCount++;
			} finally {
				doneSignal.countDown();
				conn.shutdown();
			}
		}
	}
}
