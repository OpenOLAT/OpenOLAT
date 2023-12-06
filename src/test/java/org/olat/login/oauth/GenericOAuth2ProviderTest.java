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
package org.olat.login.oauth;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.login.oauth.model.OAuthAttributeMapping;
import org.olat.login.oauth.model.OAuthAttributesMapping;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.spi.GenericOAuth2Provider;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenericOAuth2ProviderTest extends OlatTestCase {
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	@Test
	public void parseIdTokenInfos() {
		OAuthUser user = new OAuthUser();
		OAuthAttributesMapping attributesMapping = new OAuthAttributesMapping();
		GenericOAuth2Provider genericOAuth2Provider = new GenericOAuth2Provider("PORTAL", "Unit", "PORTAL",
				null, null, null, null, null, null, null, null, attributesMapping, false, oauthModule);
		
		String json = "{\"sub\":\"3489\",\"email_verified\":true,\"role\":[\"participant\"],\"address\":null,\"iss\":\"https://myopenolat.ch/\",\"name\":\"Jacques Dupont\",\"preferred_username\":\"jacques@openolat.ch\",\"exp\":\"1701893961\",\"given_name\":\"Jacques\",\"iat\":\"1701850761\",\"family_name\":\"Dupont\",\"email\":\"jacques@openolat.ch\"}";
		genericOAuth2Provider.parseIdTokenInfos(user, json);
		Assert.assertEquals("3489", user.getId());
	}
	
	@Test
	public void parseIdTokenWithSubArray() {
		OAuthUser user = new OAuthUser();
		OAuthAttributesMapping attributesMapping = new OAuthAttributesMapping();
		GenericOAuth2Provider genericOAuth2Provider = new GenericOAuth2Provider("PORTAL", "Unit", "PORTAL",
				null, null, null, null, null, null, null, null, attributesMapping, false, oauthModule);
		
		genericOAuth2Provider.parseIdTokenInfos(user, "{\"sub\":[\"3489\",\"3489\"],\"oi_au_id\":\"9c34e94e-0211-430f-a489-5175c1567ae5\",\"azp\":\"frentix\",\"nonce\":\"9c1b-9031699b157b\",\"at_hash\":\"998xyxqHh3lXIwtEpPazXE\",\"oi_tkn_id\":\"b620ff3a-0ff8-4491-bbdb-ba5327a550\",\"aud\":\"frentix\",\"exp\":1701851961,\"iss\":\"https://myopenolat.ch/\",\"iat\":1701850761}");
		Assert.assertEquals("3489", user.getId());
	}
	
	/**
	 * Map address to the identifier but address is null. The default identifier
	 * must be kept intact and not overwritten with null.
	 */
	@Test
	public void parseIdTokenInfosNonSensicalConfiguration() {
		OAuthUser user = new OAuthUser();
		List<OAuthAttributeMapping> mappings = List.of(new OAuthAttributeMapping("address", "id"), new OAuthAttributeMapping("email", "email"));
		OAuthAttributesMapping attributesMapping = new OAuthAttributesMapping(mappings);
		GenericOAuth2Provider genericOAuth2Provider = new GenericOAuth2Provider("PORTAL", "Unit", "PORTAL",
				null, null, null, null, null, null, null, null, attributesMapping, false, oauthModule);
		
		String json = "{\"sub\":\"3489\",\"email_verified\":true,\"role\":[\"participant\"],\"address\":null,\"iss\":\"https://myopenolat.ch/\",\"name\":\"Jacques Dupont\",\"preferred_username\":\"jacques@openolat.ch\",\"exp\":\"1701893961\",\"given_name\":\"Jacques\",\"iat\":\"1701850761\",\"family_name\":\"Dupont\",\"email\":\"jacques@openolat.ch\"}";
		genericOAuth2Provider.parseIdTokenInfos(user, json);
		Assert.assertEquals("3489", user.getId());
	}

}
