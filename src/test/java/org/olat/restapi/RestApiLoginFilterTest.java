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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.restapi.RestModule.ApiAccess;
import org.olat.restapi.security.RestApiAuthenticationProvider;
import org.olat.restapi.security.RestSecurityBean;
import org.olat.restapi.security.RestSecurityBeanImpl;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the filter
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestApiLoginFilterTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private RestApiAuthenticationProvider restApiAuthenticationProvider;
	
	/**
	 * Test if a session cookie is created
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void cookieAuthentication() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		conn.callMeForSecurityToken();
		
		List<Cookie> cookies = conn.getCookieStore().getCookies();
		Assert.assertNotNull(cookies);
		Assert.assertFalse(cookies.isEmpty());
		
		conn.shutdown();
	}
	
	/**
	 * Test if a token is created and send as header
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void tokenAuthentication() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		String securityToken = conn.callMeForSecurityToken();
		Assert.assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	/**
	 * Test if a standard user can get a security token too.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void tokenAuthenticationUser() throws IOException, URISyntaxException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("x-token-1");
		RestConnection conn = new RestConnection(id);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/me").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		//log user in
		String securityToken = conn.getSecurityToken(response);
		Assert.assertTrue(StringHelper.containsNonWhitespace(securityToken));
		conn.shutdown();
	}
	
	/**
	 * Test if the token survive several requests
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void followTokenBasedDiscussion() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		String securityToken = conn.callMeForSecurityToken();
		Assert.assertTrue(StringHelper.containsNonWhitespace(securityToken));
		conn.shutdown();
		
		//path is protected
		RestConnection c1 = new RestConnection();
		URI uri1 = UriBuilder.fromUri(getContextURI()).path("/users/version").build();
		HttpGet method1 = c1.createGet(uri1, MediaType.TEXT_PLAIN, false);
		method1.setHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		HttpResponse r1 = c1.execute(method1);
		securityToken = c1.getSecurityToken(r1);
		Assert.assertEquals(200, r1.getStatusLine().getStatusCode());
		Assert.assertTrue(StringHelper.containsNonWhitespace(securityToken));
		c1.shutdown();
		
		//path is protected
		RestConnection c2 = new RestConnection();
		URI uri2 = UriBuilder.fromUri(getContextURI()).path("/repo/entries").build();
		HttpGet method2 = c2.createGet(uri2, MediaType.APPLICATION_JSON, false);
		method2.setHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		HttpResponse r2 = c2.execute(method2);
		securityToken = c2.getSecurityToken(r2);
		Assert.assertEquals(200, r2.getStatusLine().getStatusCode());
		Assert.assertTrue(StringHelper.containsNonWhitespace(securityToken));
		c2.shutdown();
		
		//path is not protected
		RestConnection c3 = new RestConnection();
		URI uri3 = UriBuilder.fromUri(getContextURI()).path("/ping").build();
		HttpGet method3 = c3.createGet(uri3, MediaType.TEXT_PLAIN, false);
		method3.setHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		HttpResponse r3 = c3.execute(method3);
		securityToken = c3.getSecurityToken(r3);
		assertEquals(200, r3.getStatusLine().getStatusCode());
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		c3.shutdown();
		
		//path is protected
		RestConnection c4 = new RestConnection();
		URI uri4 = UriBuilder.fromUri(getContextURI()).path("/repo/entries").build();
		HttpGet method4 = c4.createGet(uri4, MediaType.APPLICATION_XML, false);
		method4.setHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		HttpResponse r4 = c4.execute(method4);
		securityToken = c4.getSecurityToken(r4);
		assertEquals(200, r4.getStatusLine().getStatusCode());
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		c4.shutdown();
	}
	
	/**
	 * Test if the token survive several requests
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void followTokenBasedDiscussion_flushSession() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		String securityToken = conn.callMeForSecurityToken();
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		conn.shutdown();
		
		RestSecurityBeanImpl beanImpl = (RestSecurityBeanImpl)CoreSpringFactory.getImpl(RestSecurityBean.class);
		beanImpl.clearCaches();
		
		//path is protected
		RestConnection c1 = new RestConnection();
		URI uri1 = UriBuilder.fromUri(getContextURI()).path("/users/version").build();
		HttpGet method1 = c1.createGet(uri1, MediaType.TEXT_PLAIN, false);
		method1.setHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		HttpResponse r1 = c1.execute(method1);
		securityToken = c1.getSecurityToken(r1);
		assertEquals(200, r1.getStatusLine().getStatusCode());
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		c1.shutdown();
		
	}
	
	@Test
	public void basicAuthentication() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		//path is protected
		URI uri = UriBuilder.fromUri(getContextURI()).path("/users/version").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
		method.setHeader("Authorization", "Basic " + encodeBase64NoPadding("administrator:openolat"));
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String securityToken = conn.getSecurityToken(response);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	@Test
	public void basicAuthenticationWithClient() throws IOException, URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-10");
		String clientId = restApiAuthenticationProvider.generateClientId();
		String clientSecret = restApiAuthenticationProvider.generateClientSecret();
		restApiAuthenticationProvider.setClientAuthentication(id, clientId, clientSecret);
		
		RestConnection conn = new RestConnection();
		//path is protected
		URI uri = UriBuilder.fromUri(getContextURI()).path("/users/version").build();
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
		method.setHeader("Authorization", "Basic " + encodeBase64NoPadding(clientId + ":" + clientSecret));
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String securityToken = conn.getSecurityToken(response);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	private String encodeBase64NoPadding(String string) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}
	
	
	@Test
	public void webStandardAuthentication() throws IOException, URISyntaxException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("/users/version").build();
		RestConnection conn = new RestConnection(uri.toURL(), "administrator", "openolat");
		HttpGet method = conn.createGet(uri, MediaType.TEXT_PLAIN, false);
		HttpResponse response = conn.execute(method);
		
		assertEquals(200, response.getStatusLine().getStatusCode());
		String securityToken = conn.getSecurityToken(response);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		conn.shutdown();
	}
	
	@Test
	public void apiKeyAuthentication() throws IOException, URISyntaxException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-1");
		String clientId = restApiAuthenticationProvider.generateClientId();
		String clientSecret = restApiAuthenticationProvider.generateClientSecret();
		restApiAuthenticationProvider.setClientAuthentication(id.getIdentity(), clientId, clientSecret);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(clientId, clientSecret);
		URI request = UriBuilder.fromUri(getContextURI()).path("users/me").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		conn.shutdown();
	}
	
	@Test
	public void apiKeyOnlyAuthentication() throws IOException, URISyntaxException {
		ApiAccess currentAccess = restModule.getApiAccess();
		restModule.setApiAccess(ApiAccess.apikey);
		
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-2");
		String clientId = restApiAuthenticationProvider.generateClientId();
		String clientSecret = restApiAuthenticationProvider.generateClientSecret();
		restApiAuthenticationProvider.setClientAuthentication(id.getIdentity(), clientId, clientSecret);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(clientId, clientSecret);
		URI request = UriBuilder.fromUri(getContextURI()).path("users/me").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		restModule.setApiAccess(currentAccess);
		waitMessageAreConsumed();
	}
	
	@Test
	public void apiKeyOnlyForbiddenAuthentication() throws IOException, URISyntaxException {
		ApiAccess currentAccess = restModule.getApiAccess();
		restModule.setApiAccess(ApiAccess.apikey);
		
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-3");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(id);
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("me").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		restModule.setApiAccess(currentAccess);
		waitMessageAreConsumed();
	}
}
