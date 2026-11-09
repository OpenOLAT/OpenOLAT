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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Encoder;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
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
	public void getAuthentications() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity administrator = securityManager.findIdentityByLogin("administrator");
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(administrator.getKey().toString()).path("authentications")
				.build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<AuthenticationVO> vos = parseAuthenticationArray(response);
		Assert.assertNotNull(vos);
		Assert.assertFalse(vos.isEmpty());
		conn.close();

	}
	
	@Test
	public void createAuthentications() throws IOException, URISyntaxException, InterruptedException {
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

		RestConnection conn = new RestConnection("administrator", "openolat");

		AuthenticationVO vo = new AuthenticationVO();
		vo.setAuthUsername("administrator");
		vo.setIdentityKey(adminIdent.getKey());
		vo.setProvider("REST-API");
		vo.setCredential("credentials");
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(adminIdent.getKey().toString()).path("authentications")
				.build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
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
		conn.close();

	}
	
	/**
	 * Check if the REST call return a specific error if the pair authentication user name and provider
	 * is already used.
	 * 
	 */
	@Test
	public void createAuthentications_checkDuplicate() throws IOException, URISyntaxException, InterruptedException {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-auth-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-auth-2");
		String authUsername = UUID.randomUUID().toString();
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");

		//set the first authentication
		AuthenticationVO vo1 = new AuthenticationVO();
		vo1.setAuthUsername(authUsername);
		vo1.setIdentityKey(id1.getKey());
		vo1.setProvider("REST-API");
		vo1.setCredential("credentials");
		URI request1 = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("authentications").build();
		HttpRequest method1 = conn.createPut(request1, vo1, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response1 = conn.execute(method1);
		Assert.assertEquals(200, response1.statusCode());
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
		HttpRequest method2 = conn.createPut(request2, vo2, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response2 = conn.execute(method2);
		Assert.assertEquals(409, response2.statusCode());
		ErrorVO error = conn.parse(response2, ErrorVO.class);
		Assert.assertNotNull(error);
		conn.close();

	}
	
	@Test
	public void deleteAuthentications() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//create an authentication token
		Identity adminIdent = JunitTestHelper.findIdentityByLogin("administrator");
		Authentication authentication = securityManager.createAndPersistAuthentication(adminIdent, "REST-A-2", BaseSecurity.DEFAULT_ISSUER, null,
				"administrator", "credentials", Encoder.Algorithm.sha512);
		assertTrue(authentication != null && authentication.getKey() != null && authentication.getKey().longValue() > 0);
		dbInstance.intermediateCommit();
		
		//delete an authentication token
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(adminIdent.getKey().toString())
				.path("authentications").path(authentication.getKey().toString()).build();
		HttpRequest method = conn.createDelete(request, MediaType.APPLICATION_XML);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-A-2", BaseSecurity.DEFAULT_ISSUER);
		assertNull(refAuth);
		conn.close();

	}
	
	@Test
	public void updateAuthentication() throws IOException, URISyntaxException, InterruptedException {
		
		//create an authentication token
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("rest-auth-1");
		Authentication authentication = securityManager
				.createAndPersistAuthentication(ident.getIdentity(), "REST-A-*", BaseSecurity.DEFAULT_ISSUER,
						CodeHelper.getUniqueID(), ident.getLogin(), "credentials", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		conn.callMeForSecurityToken();
		
		//update an authentication token
		String newUsername = ident.getLogin() + "-v2";
		String newExternalId = "my-external-" + System.nanoTime();
		// set the second which duplicates the first
		AuthenticationVO vo = new AuthenticationVO();
		vo.setKey(authentication.getKey());
		vo.setAuthUsername(newUsername);
		vo.setIdentityKey(ident.getKey());
		vo.setProvider("REST-A-*");
		vo.setCredential("my-credentials");
		vo.setExternalId(newExternalId);
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(ident.getKey().toString()).path("authentications").build();
		HttpRequest method = conn.createPost(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		AuthenticationVO updatedAuthentication = conn.parse(response, AuthenticationVO.class);
		Assert.assertEquals(authentication.getKey(), updatedAuthentication.getKey());
		Assert.assertEquals(ident.getKey(), updatedAuthentication.getIdentityKey());
		Assert.assertEquals("REST-A-*", updatedAuthentication.getProvider());
		Assert.assertEquals(newUsername, updatedAuthentication.getAuthUsername());
		Assert.assertEquals(newExternalId, updatedAuthentication.getExternalId());
		// credentials are not updated, only the authentication user name
		Assert.assertNotEquals("my-credentials", updatedAuthentication.getCredential());

		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(updatedAuthentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(newUsername, refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
		conn.close();
	}
	
	@Test
	public void updateAuthenticationConflictProvider() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
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
		HttpRequest method = conn.createPost(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(409, response.statusCode());
		RestConnection.consume(response);

		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(authentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(ident.getLogin(), refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
		conn.close();
	}
	
	@Test
	public void updateAuthenticationConflictIdentity() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
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
		HttpRequest method = conn.createPost(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(409, response.statusCode());
		RestConnection.consume(response);

		
		// check database
		Authentication refAuth = securityManager.findAuthenticationByKey(authentication.getKey());
		Assert.assertEquals(authentication.getKey(), refAuth.getKey());
		Assert.assertEquals(ident.getKey(), refAuth.getIdentity().getKey());
		Assert.assertEquals(ident.getLogin(), refAuth.getAuthusername());
		Assert.assertEquals("REST-A-*", refAuth.getProvider());
		conn.close();
	}
	
	@Test
	public void changePassword() throws IOException, URISyntaxException, InterruptedException {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-chg-pwd");
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(user.getKey().toString()).path("authentications").path("password")
				.build();
		List<NameValuePair> formParameters = List.of(new NameValuePair("newPassword", "top-secret"));
		HttpRequest method = conn.createPost(request, formParameters, "*/*");
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		//check
		Identity reloadedUser = authManager.authenticate(user, user.getName(), "top-secret", new AuthenticationStatus());
		Assert.assertNotNull(reloadedUser);
		Assert.assertEquals(user, reloadedUser);
		conn.close();
	}
	
	private List<AuthenticationVO> parseAuthenticationArray(HttpResponse<InputStream> entity) {
		try(InputStream in=entity.body()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<AuthenticationVO>>(){/* */});
		} catch (Exception e) {
			log.error("Cannot parse an array of AuthenticationVO", e);
			return null;
		}
	}
}