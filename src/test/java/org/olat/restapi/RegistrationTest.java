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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14 juil. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RegistrationTest extends OlatRestTestCase {
	
	@Test
	public void testRegistration() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();

		String randomEmail = UUID.randomUUID().toString().replace("-", "") + "@frentix.com";
		URI uri = conn.getContextURI().path("registration").queryParam("email", randomEmail).build();
		HttpPut put = conn.createPut(uri, "*/*", "de", true);
		
		HttpResponse response = conn.execute(put);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		conn.shutdown();
	}
	
	@Test
	public void testRedirectRegistration() throws IOException, URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("rest-reg");
		
		RestConnection conn = new RestConnection();

		URI uri = conn.getContextURI().path("registration")
				.queryParam("email", id.getUser().getProperty(UserConstants.EMAIL, null)).build();
		HttpPut put = conn.createPut(uri, "*/*", "de", true);

		HttpResponse response = conn.execute(put);
		assertEquals(304, response.getStatusLine().getStatusCode());
		Header locationHeader = response.getFirstHeader("location");
		assertNotNull(locationHeader);
		assertNotNull(locationHeader.getValue());
		
		conn.shutdown();
	}
}
