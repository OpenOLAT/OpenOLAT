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
 * <p>
 */

package org.olat.restapi;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.restapi.support.vo.AuthenticationVO;
import org.olat.test.OlatJerseyTestCase;

/**
 * 
 * Description:<br>
 * Test the authentication management per user
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserAuthenticationMgmtTest extends OlatJerseyTestCase {
	
	@Test
	public void testGetAuthentications() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users/administrator/auth", MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		List<AuthenticationVO> vos = parseAuthenticationArray(body);
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		method.releaseConnection();
	}
	
	@Test
	public void testCreateAuthentications() throws IOException {
		BaseSecurity baseSecurity = BaseSecurityManager.getInstance();
		Identity adminIdent = baseSecurity.findIdentityByName("administrator");
		try {
			Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-API");
			if(refAuth != null) {
				baseSecurity.deleteAuthentication(refAuth);
			}
		} catch(Exception e) {
			//
		}
		DBFactory.getInstance().commitAndCloseSession();
		
		HttpClient c = loginWithCookie("administrator", "olat");

		AuthenticationVO vo = new AuthenticationVO();
		vo.setAuthUsername("administrator");
		vo.setIdentityKey(adminIdent.getKey());
		vo.setProvider("REST-API");
		vo.setCredential("credentials");
		
		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users/administrator/auth", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);

    int code = c.executeMethod(method);
    assertTrue(code == 200 || code == 201);
    String body = method.getResponseBodyAsString();
    AuthenticationVO savedAuth = parse(body, AuthenticationVO.class);
    Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-API");
    method.releaseConnection();

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
	}
	
	@Test
	public void testDeleteAuthentications() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//create an authentication token
		BaseSecurity baseSecurity = BaseSecurityManager.getInstance();
		Identity adminIdent = baseSecurity.findIdentityByName("administrator");
		Authentication authentication = baseSecurity.createAndPersistAuthentication(adminIdent, "REST-A-2", "administrator", "credentials");
		assertTrue(authentication != null && authentication.getKey() != null && authentication.getKey().longValue() > 0);
		DBFactory.getInstance().intermediateCommit();
		
		//delete an authentication token
		String request = "/users/administrator/auth/" + authentication.getKey().toString();
		DeleteMethod method = createDelete(request, MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		method.releaseConnection();
		
		Authentication refAuth = baseSecurity.findAuthentication(adminIdent, "REST-A-2");
		assertNull(refAuth);
	}
	
	private List<AuthenticationVO> parseAuthenticationArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<AuthenticationVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}