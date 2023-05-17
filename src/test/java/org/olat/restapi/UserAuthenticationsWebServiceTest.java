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
package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 10 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAuthenticationsWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(UserAuthenticationsWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager authManager;
	
	
	@Test
	public void getAuthentications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		Identity administrator = securityManager.findIdentityByLogin("administrator");
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(administrator.getKey().toString()).path("authentications")
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<AuthenticationVO> vos = parseAuthenticationArray(response.getEntity());
		Assert.assertNotNull(vos);
		Assert.assertFalse(vos.isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void createAuthentications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Identity adminIdent = JunitTestHelper.findIdentityByLogin("administrator");
		try {
			Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-API", BaseSecurity.DEFAULT_ISSUER);
			if(refAuth != null) {
				securityManager.deleteAuthentication(refAuth);
			}
		} catch(Exception e) {
			//
		}
		dbInstance.commitAndCloseSession();
		
		assertTrue(conn.login("administrator", "openolat"));

		AuthenticationVO vo = new AuthenticationVO();
		vo.setAuthUsername("administrator");
		vo.setIdentityKey(adminIdent.getKey());
		vo.setProvider("REST-API");
		vo.setCredential("credentials");
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(adminIdent.getKey().toString()).path("authentications")
				.build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);

		HttpResponse response = conn.execute(method);
		Assert.assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		AuthenticationVO savedAuth = conn.parse(response, AuthenticationVO.class);
		Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-API", BaseSecurity.DEFAULT_ISSUER);

		Assert.assertNotNull(refAuth);
		Assert.assertNotNull(refAuth.getKey());
		Assert.assertTrue(refAuth.getKey().longValue() > 0);
		Assert.assertNotNull(savedAuth);
		Assert.assertNotNull(savedAuth.getKey());
		assertTrue(savedAuth.getKey().longValue() > 0);
		assertEquals(refAuth.getKey(), savedAuth.getKey());
		assertEquals(refAuth.getAuthusername(), savedAuth.getAuthUsername());
		assertEquals(refAuth.getIdentity().getKey(), savedAuth.getIdentityKey());
		assertEquals(refAuth.getProvider(), savedAuth.getProvider());
		assertEquals(refAuth.getCredential(), savedAuth.getCredential());
		
		conn.shutdown();
	}
	
	/**
	 * Check if the REST call return a specific error if the pair authentication user name and provider
	 * is already used.
	 * 
	 */
	@Test
	public void createAuthentications_checkDuplicate() throws IOException, URISyntaxException {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-auth-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-auth-2");
		String authUsername = UUID.randomUUID().toString();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		//set the first authentication
		AuthenticationVO vo1 = new AuthenticationVO();
		vo1.setAuthUsername(authUsername);
		vo1.setIdentityKey(id1.getKey());
		vo1.setProvider("REST-API");
		vo1.setCredential("credentials");
		URI request1 = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("authentications").build();
		HttpPut method1 = conn.createPut(request1, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method1, vo1);

		HttpResponse response1 = conn.execute(method1);
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		conn.parse(response1, AuthenticationVO.class);
		Authentication refAuth1 = securityManager.findAuthentication(id1, "REST-API", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(refAuth1);
		Assert.assertEquals(id1, refAuth1.getIdentity());

		// set the second which duplicates the first
		AuthenticationVO vo2 = new AuthenticationVO();
		vo2.setAuthUsername(authUsername);
		vo2.setIdentityKey(id2.getKey());
		vo2.setProvider("REST-API");
		vo2.setCredential("credentials");
		URI request2 = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("authentications").build();
		HttpPut method2 = conn.createPut(request2, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method2, vo2);

		HttpResponse response2 = conn.execute(method2);
		Assert.assertEquals(409, response2.getStatusLine().getStatusCode());
		ErrorVO error = conn.parse(response2, ErrorVO.class);
		Assert.assertNotNull(error);

		conn.shutdown();
	}
	
	@Test
	public void deleteAuthentications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an authentication token
		Identity adminIdent = JunitTestHelper.findIdentityByLogin("administrator");
		Authentication authentication = securityManager.createAndPersistAuthentication(adminIdent, "REST-A-2", BaseSecurity.DEFAULT_ISSUER, null,
				"administrator", "credentials", Encoder.Algorithm.sha512);
		assertTrue(authentication != null && authentication.getKey() != null && authentication.getKey().longValue() > 0);
		dbInstance.intermediateCommit();
		
		//delete an authentication token
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(adminIdent.getKey().toString())
				.path("authentications").path(authentication.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_XML);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-A-2", BaseSecurity.DEFAULT_ISSUER);
		assertNull(refAuth);
		
		conn.shutdown();
	}
	
	@Test
	public void updateAuthentication() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an authentication token
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("rest-auth-1");
		Authentication authentication = securityManager
				.createAndPersistAuthentication(ident.getIdentity(), "REST-A-*", BaseSecurity.DEFAULT_ISSUER, null,
						ident.getLogin(), "credentials", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		//update an authentication token
		String newUsername = ident.getLogin() + "-v2";
		// set the second which duplicates the first
		AuthenticationVO vo = new AuthenticationVO();
		vo.setKey(authentication.getKey());
		vo.setAuthUsername(newUsername);
		vo.setIdentityKey(ident.getKey());
		vo.setProvider("REST-A-*");
		vo.setCredential("my-credentials");
		vo.setExternalId("my-external-id");
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(ident.getKey().toString()).path("authentications").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		AuthenticationVO updatedAuthentication = conn.parse(response, AuthenticationVO.class);
		Assert.assertEquals(authentication.getKey(), updatedAuthentication.getKey());
		Assert.assertEquals(ident.getKey(), updatedAuthentication.getIdentityKey());
		Assert.assertEquals("REST-A-*", updatedAuthentication.getProvider());
		Assert.assertEquals(newUsername, updatedAuthentication.getAuthUsername());
		Assert.assertEquals("my-external-id", updatedAuthentication.getExternalId());
		// credentials are not updated, only the authentication user name
		Assert.assertNotEquals("my-credentials", updatedAuthentication.getCredential());

		conn.shutdown();
		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(updatedAuthentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(newUsername, refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
	}
	
	@Test
	public void updateAuthenticationConflictProvider() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an authentication token
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("rest-auth-2");
		Authentication authentication = securityManager
				.createAndPersistAuthentication(ident.getIdentity(), "REST-A-*", BaseSecurity.DEFAULT_ISSUER, null,
						ident.getLogin(), "credentials", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		//update an authentication token
		// set the second which duplicates the first
		AuthenticationVO vo = new AuthenticationVO();
		vo.setKey(authentication.getKey());
		vo.setAuthUsername(ident.getLogin() + "-v2");
		vo.setIdentityKey(ident.getKey());
		vo.setProvider("REST-B-*");
		vo.setCredential("my-credentials");
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(ident.getKey().toString()).path("authentications").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(authentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(ident.getLogin(), refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
	}
	
	@Test
	public void updateAuthenticationConflictIdentity() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an authentication token
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("rest-auth-3");
		IdentityWithLogin otherIdent = JunitTestHelper.createAndPersistRndUser("rest-auth-3");
		Authentication authentication = securityManager
				.createAndPersistAuthentication(ident.getIdentity(), "REST-A-*", BaseSecurity.DEFAULT_ISSUER,  null,
						ident.getLogin(), "credentials", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		//update an authentication token
		// set the second which duplicates the first
		AuthenticationVO vo = new AuthenticationVO();
		vo.setKey(authentication.getKey());
		vo.setAuthUsername(otherIdent.getLogin() + "-v2");
		vo.setIdentityKey(otherIdent.getKey());
		vo.setProvider("REST-A-*");
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(ident.getKey().toString()).path("authentications").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(authentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(ident.getLogin(), refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
	}
	
	@Test
	public void changePassword() throws IOException, URISyntaxException {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-chg-pwd");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(user.getKey().toString()).path("authentications").path("password")
				.build();
		HttpPost method = conn.createPost(request, "*/*");
		conn.addEntity(method, new BasicNameValuePair("newPassword", "top-secret"));
		HttpResponse response = conn.execute(method);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check
		Identity reloadedUser = authManager.authenticate(user, user.getName(), "top-secret", new AuthenticationStatus());
		Assert.assertNotNull(reloadedUser);
		Assert.assertEquals(user, reloadedUser);
	}
	
	private List<AuthenticationVO> parseAuthenticationArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<AuthenticationVO>>(){/* */});
		} catch (Exception e) {
			log.error("Cannot parse an array of AuthenticationVO", e);
			return null;
		}
	}
}