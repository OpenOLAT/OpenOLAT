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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.olat.core.util.StringHelper;
import org.olat.test.OlatJerseyTestCase;

import com.oreilly.servlet.Base64Encoder;

/**
 * 
 * Description:<br>
 * Test the authentication service
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class AuthenticationTest extends OlatJerseyTestCase {

	public AuthenticationTest() {
		super();
  }
	
	@Test
	public void testSessionCookieLogin() throws HttpException, IOException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("administrator").queryParam("password", "olat").build();
		GetMethod method = createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpClient c = getHttpClient();
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String response = method.getResponseBodyAsString();
		assertTrue(response.startsWith("<hello"));
		assertTrue(response.endsWith("Hello administrator</hello>"));
		Cookie[] cookies = c.getState().getCookies();
		assertNotNull(cookies);
		assertTrue(cookies.length > 0);
  }
	
	@Test
	public void testWrongPassword() throws HttpException, IOException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("administrator").queryParam("password", "blabla").build();
		GetMethod method = createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpClient c = getHttpClient();
		int code = c.executeMethod(method);
		assertEquals(401, code);
	}
	
	@Test
	public void testUnkownUser() throws HttpException, IOException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("auth").path("treuitr").queryParam("password", "blabla").build();
		GetMethod method = createGet(uri, MediaType.TEXT_PLAIN, true);
		HttpClient c = getHttpClient();
		int code = c.executeMethod(method);
		assertEquals(401, code);
	}
	
	@Test
	public void testBasicAuthentication() throws HttpException, IOException {
		//path is protected
		GetMethod method1 = createGet("/users/version", MediaType.TEXT_PLAIN, false);
		method1.setRequestHeader("Authorization", "Basic " + Base64Encoder.encode("administrator:olat"));
		int code1 = getHttpClient().executeMethod(method1);
		method1.releaseConnection();
		assertEquals(code1, 200);
		String securityToken = getToken(method1);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
	}
	
	@Test
	public void testWebStandardAuthentication() throws HttpException, IOException {
		HttpClient c = getHttpClient();
		Credentials creds = new UsernamePasswordCredentials("administrator", "olat");
		c.getState().setCredentials(AuthScope.ANY, creds);
    
		GetMethod method = createGet("/users/version", MediaType.TEXT_PLAIN, false);
		int code = c.executeMethod(method);
		method.releaseConnection();
		assertEquals(code, 200);
		String securityToken = getToken(method);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
	}
}
