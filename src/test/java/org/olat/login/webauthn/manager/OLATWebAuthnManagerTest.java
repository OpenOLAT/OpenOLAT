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
package org.olat.login.webauthn.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey;

/**
 * 
 * Initial date: 13 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATWebAuthnManagerTest extends OlatTestCase {
	
	@Autowired
	private OLATWebAuthnManagerImpl webAuthnManager;

	@Test
	public void readCoseKeyPlayGround() throws Exception {
		String val = "{\"1\":\"2\",\"3\":-7,\"-1\":1,\"-2\":\"rewHgUMdxTrgD7tELlXKFwAYEKnLUTaODOIBIPL8Joo=\",\"-3\":\"YGq+nrRgIWczBBg4hzho/G4dyN64DMQhjYEOIqGm5P0=\",\"1\":2}";
		
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, false);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
   
		COSEKey key = jsonMapper.readValue(val, COSEKey.class);
		Assert.assertNotNull(key);
	}
	
	@Test
	public void readCoseKey() {
		String val = "{\"1\":\"2\",\"3\":-7,\"-1\":1,\"-2\":\"rewHgUMdxTrgD7tELlXKFwAYEKnLUTaODOIBIPL8Joo=\",\"-3\":\"YGq+nrRgIWczBBg4hzho/G4dyN64DMQhjYEOIqGm5P0=\",\"1\":2}";
		
		COSEKey key = webAuthnManager.convertToCOSEKey(val.getBytes());
		Assert.assertNotNull(key);
		Assert.assertTrue(key instanceof EC2COSEKey);
	}
}
