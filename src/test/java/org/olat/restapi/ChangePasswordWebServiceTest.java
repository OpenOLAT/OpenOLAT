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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.registration.RegistrationManager;
import org.olat.registration.restapi.TemporaryKeyVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangePasswordWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationManager registrationManager;
	
	/**
	 * An administrator can request an URL for a user to change its password.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void changePasswordAsAdmin() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pwchange-1-", Locale.ENGLISH);
		dbInstance.commitAndCloseSession();
		
		List<Authentication> authentications = securityManager.getAuthentications(id);
		Assert.assertTrue(authentications.isEmpty());
		
		URI uri = conn.getContextURI().path("pwchange").queryParam("identityKey", id.getKey()).build();
		HttpRequest put = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(put);
		Assert.assertEquals(200, response.statusCode());
		
		TemporaryKeyVO tk = conn.parse(response, TemporaryKeyVO.class);
		Assert.assertNotNull(tk);
		Assert.assertNotNull(tk.getIpAddress());
		Assert.assertNotNull(tk.getUrl());
		Assert.assertEquals(RegistrationManager.PW_CHANGE, tk.getRegAction());
		Assert.assertEquals(id.getUser().getProperty(UserConstants.EMAIL, null), tk.getEmailAddress());
		Assert.assertFalse(tk.isMailSent());
		conn.close();

	}
	
	/**
	 * As a standard user, I cannot request an URL to change my password.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void changePasswordAsMe() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("pwchange-1-");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(id);

		URI uri = conn.getContextURI().path("pwchange").queryParam("identityKey", id.getKey()).build();
		HttpRequest put = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(put);
		Assert.assertEquals(403, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	/**
	 * An administrator can request an URL for a user to change its password.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void hasChangePasswordAsAdmin() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pwchange-1-", Locale.ENGLISH);
		registrationManager.createAndDeleteOldTemporaryKey(id.getKey(), id.getUser().getEmail(), "192.168.1.200", RegistrationManager.PW_CHANGE, Integer.valueOf(30));
		dbInstance.commitAndCloseSession();
		
		List<Authentication> authentications = securityManager.getAuthentications(id);
		Assert.assertTrue(authentications.isEmpty());
		
		URI uri = conn.getContextURI().path("pwchange").queryParam("identityKey", id.getKey()).build();
		HttpRequest get = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(get);
		Assert.assertEquals(200, response.statusCode());
		
		TemporaryKeyVO tk = conn.parse(response, TemporaryKeyVO.class);
		Assert.assertNotNull(tk);
		Assert.assertNotNull(tk.getIpAddress());
		Assert.assertNotNull(tk.getUrl());
		Assert.assertEquals(RegistrationManager.PW_CHANGE, tk.getRegAction());
		Assert.assertEquals(id.getUser().getEmail(), tk.getEmailAddress());
		conn.close();

	}
}
