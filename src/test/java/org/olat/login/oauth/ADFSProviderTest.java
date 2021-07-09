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
package org.olat.login.oauth;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.spi.ADFSApi;
import org.olat.login.oauth.spi.ADFSProvider;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;


/**
 * 
 * Initial date: 9 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ADFSProviderTest extends OlatTestCase {
	
	@Autowired
	private ADFSProvider adfsProvider;
	
	@Test
	public void getUser() throws IOException {
		String responseBody = "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkhnbmtjOVBMd0E3ampHMjlWbndpQk43WnlaYyJ9.eyJhdWQiOiJodHRwczovL2tpdmlrLmZyZW50aXguY29tL29sYXQiLCJpc3MiOiJodHRwOi8vYWRmcy5oYW1pbHRvbi5jaC9hZGZzL3NlcnZpY2VzL3RydXN0IiwiaWF0IjoxNDE1MzQ3MDE0LCJleHAiOjE0MTUzNTA2MTQsIlNuIjoiT3Blbk9sYXQiLCJkaXNwbGF5TmFtZVByaW50YWJsZSI6IlRlc3R1c2VyIiwiU0FNQWNjb3VudE5hbWUiOiJ0ZXN0X29wZW5vbGF0IiwiYXV0aF90aW1lIjoiMjAxNC0xMS0wN1QwNzo1Njo1NC4zOTFaIiwiYXV0aG1ldGhvZCI6InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0IiwidmVyIjoiMS4wIiwiYXBwaWQiOiIyNWU1M2VmNC02NTllLTExZTQtYjExNi0xMjNiOTNmNzVjYmEifQ.l17qB7LWkODD66OuRbhjDEdKEQWrEfaeR7hBpbdN8XqGIOMS2sc2xQNYJH9Lh061XOJt9WPqrAW8sHSu2eaR1qpw8o6LcWksKvh0LJbCmVPqQggLDj8Q4kSFIzbs9YAQautTAvobdb_hsoGT9rhGN4SDIcpJA8Uq8JWwYDjWfDCpCVRHZPmyZiOmh-5rBT8SxSiV0QgFexhmbvLAZhaEmsZGzSaj2r39cyK0dlt7OuR_1KjQeB86ycOMP1PT1OAGWJc1lgGP12gDo-FkcK5mOY6mgC8za7OOwgTUkE4pbXwygi4nPBXHQVPku-bWtigLZWfTln4Ght3fqMIzJOQXag\",\"token_type\":\"bearer\",\"expires_in\":3600}";
		Response response = new Response(200, "", new HashMap<>(), responseBody);
		OAuth2AccessToken accessToken = new ADFSApi().getAccessTokenExtractor().extract(response);

		OAuthUser user = adfsProvider.getUser(null, accessToken);
		Assert.assertEquals("test_openolat", user.getId());
		Assert.assertEquals("Testuser", user.getFirstName());
		Assert.assertEquals("OpenOlat", user.getLastName());
		Assert.assertEquals("test_openolat", user.getInstitutionalUserIdentifier());
	}
}
