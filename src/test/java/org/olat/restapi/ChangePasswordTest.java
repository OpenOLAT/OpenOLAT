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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.registration.RegistrationManager;
import org.olat.registration.restapi.TemporaryKeyVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangePasswordTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	
	@Test
	public void testRegistration() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("pwchange-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		URI uri = conn.getContextURI().path("pwchange").queryParam("identityKey", id.getKey()).build();
		HttpPut put = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(put);
		assertEquals(200, response.getStatusLine().getStatusCode());
		TemporaryKeyVO tk = conn.parse(response, TemporaryKeyVO.class);
		Assert.assertNotNull(tk);
		Assert.assertNotNull(tk.getIpAddress());
		Assert.assertNotNull(tk.getRegistrationKey());
		Assert.assertEquals(RegistrationManager.PW_CHANGE, tk.getRegAction());
		Assert.assertEquals(id.getUser().getProperty(UserConstants.EMAIL, null), tk.getEmailAddress());
		Assert.assertFalse(tk.isMailSent());

		conn.shutdown();
	}
}
