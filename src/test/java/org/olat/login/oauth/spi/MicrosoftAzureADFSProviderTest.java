/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.login.oauth.spi;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 28 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MicrosoftAzureADFSProviderTest {
	
	@Test
	public void deserializeUser() throws Exception {
		String userJson = "{\"@odata.context\":\"https://graph.microsoft.com/v1.0/$metadata#users/$entity\",\"businessPhones\":[\"+41 43 1234 56 78\"],\"displayName\":\"Albert Dupont\",\"givenName\":\"Albert\",\"jobTitle\":null,\"mail\":\"albert.dupont@frentix.com\",\"mobilePhone\":\"+41 79 123 45 56\",\"officeLocation\":null,\"preferredLanguage\":\"fr-FR\",\"surname\":\"Dupont\",\"userPrincipalName\":\"albert.dupont@frentix.com\",\"id\":\"917a456f-1234-abcd-edfg-hijklmnop\"}";
		
		User user = MicrosoftAzureADFSProvider.parseUser(userJson);
		Assert.assertNotNull(user);
		Assert.assertEquals("917a456f-1234-abcd-edfg-hijklmnop", user.getId());
		Assert.assertEquals("Albert", user.getGivenName());
		Assert.assertEquals("Dupont", user.getSurname());
	}

}
