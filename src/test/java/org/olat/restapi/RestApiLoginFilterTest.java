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

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.olat.core.util.StringHelper;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.test.OlatJerseyTestCase;

import com.oreilly.servlet.Base64Encoder;

/**
 * Description:<br>
 * Test the filter
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestApiLoginFilterTest extends OlatJerseyTestCase {
	
	public RestApiLoginFilterTest() {
		super();
	}
	
	/**
	 * Test if a session cookie is created
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void testCookieAuthentication() throws HttpException, IOException {
		HttpClient c = getAuthenticatedCookieBasedClient("administrator", "olat");
		Cookie[] cookies = c.getState().getCookies();
		assertNotNull(cookies);
		assertTrue(cookies.length > 0);
	}
	
	/**
	 * Test if a token is created and send as header
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void testTokenAuthentication() throws HttpException, IOException {
		String securityToken = getAuthenticatedTokenBasedClient("administrator", "olat");
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
	}
	
	
	/**
	 * Test if the token survive several requests
	 * @throws HttpException
	 * @throws IOException
	 */
	@Test
	public void testFollowTokenBasedDiscussion() throws HttpException, IOException {
		String securityToken = getAuthenticatedTokenBasedClient("administrator", "olat");
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		//path is protected
		GetMethod method1 = createGet("/users/version", MediaType.TEXT_PLAIN, false);
		method1.setRequestHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		int code1 = getHttpClient().executeMethod(method1);
		method1.releaseConnection();
		securityToken = getToken(method1);
		assertEquals(code1, 200);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		//path is protected
		GetMethod method2 = createGet("/repo/entries", MediaType.TEXT_HTML, false);
		method2.setRequestHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		int code2 = getHttpClient().executeMethod(method2);
		method2.releaseConnection();
		securityToken = getToken(method2);
		assertEquals(code2, 200);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		//path is not protected
		GetMethod method3 = createGet("/api/copyright", MediaType.TEXT_PLAIN, false);
		method3.setRequestHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		int code3 = getHttpClient().executeMethod(method3);
		method3.releaseConnection();
		securityToken = getToken(method3);
		assertEquals(code3, 200);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
		
		//path is protected
		GetMethod method4 = createGet("/repo/entries", MediaType.TEXT_HTML, false);
		method4.setRequestHeader(RestSecurityHelper.SEC_TOKEN, securityToken);
		int code4 = getHttpClient().executeMethod(method4);
		method4.releaseConnection();
		securityToken = getToken(method4);
		assertEquals(code4, 200);
		assertTrue(StringHelper.containsNonWhitespace(securityToken));
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
