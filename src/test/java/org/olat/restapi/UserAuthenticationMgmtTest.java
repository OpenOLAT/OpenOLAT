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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

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
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Description:<br>
 * Test the authentication management per user
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Deprecated
public class UserAuthenticationMgmtTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(UserAuthenticationMgmtTest.class);
	
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
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/administrator/auth").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<AuthenticationVO> vos = parseAuthenticationArray(response.getEntity());
		assertNotNull(vos);
		assertFalse(vos.isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void createAuthentications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Identity adminIdent = JunitTestHelper.findIdentityByLogin("administrator");
		try {
			Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-API");
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
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/administrator/auth").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);

		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		AuthenticationVO savedAuth = conn.parse(response, AuthenticationVO.class);
		Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-API");

		assertNotNull(refAuth);
		assertNotNull(refAuth.getKey());
		assertTrue(refAuth.getKey().longValue() > 0);
		assertNotNull(savedAuth);
		assertNotNull(savedAuth.getKey());
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
		URI request1 = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getName() + "/auth").build();
		HttpPut method1 = conn.createPut(request1, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method1, vo1);

		HttpResponse response1 = conn.execute(method1);
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		conn.parse(response1, AuthenticationVO.class);
		Authentication refAuth1 = securityManager.findAuthentication(id1, "REST-API");
		Assert.assertNotNull(refAuth1);
		Assert.assertEquals(id1, refAuth1.getIdentity());

		// set the second which duplicates the first
		AuthenticationVO vo2 = new AuthenticationVO();
		vo2.setAuthUsername(authUsername);
		vo2.setIdentityKey(id2.getKey());
		vo2.setProvider("REST-API");
		vo2.setCredential("credentials");
		URI request2 = UriBuilder.fromUri(getContextURI()).path("/users/" + id2.getName() + "/auth").build();
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
		Authentication authentication = securityManager.createAndPersistAuthentication(adminIdent, "REST-A-2", "administrator", "credentials", Encoder.Algorithm.sha512);
		assertTrue(authentication != null && authentication.getKey() != null && authentication.getKey().longValue() > 0);
		dbInstance.intermediateCommit();
		
		//delete an authentication token
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/administrator/auth/" + authentication.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_XML);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Authentication refAuth = securityManager.findAuthentication(adminIdent, "REST-A-2");
		assertNull(refAuth);
		
		conn.shutdown();
	}
	
	@Test
	public void changePassword() throws IOException, URISyntaxException {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-chg-pwd");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(user.getName()).path("auth").path("password")
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