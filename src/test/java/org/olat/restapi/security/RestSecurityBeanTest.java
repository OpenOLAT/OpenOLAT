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
package org.olat.restapi.security;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

/**
 * 
 * Initial date: 5 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RestSecurityBeanTest extends OlatTestCase {
	
    @Autowired
    private RestSecurityBean restSecurityBean;

	@Test
	public void generatedToken() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-api");
		String token = restSecurityBean.generateToken(id, new MockHttpSession());
		String renewedToken = restSecurityBean.renewToken(token);
		Assert.assertEquals(token, renewedToken);
	}
	
	@Test
	public void renewToken() {
		String token = UUID.randomUUID().toString();
		String renewedToken = restSecurityBean.renewToken(token);
		Assert.assertEquals(token, renewedToken);
	}
	
	@Test
	public void renewToken_09() {
		String uuid = "0123456789";
		String renewedToken = restSecurityBean.renewToken(uuid);
		Assert.assertEquals(uuid, renewedToken);
	}
	
	@Test
	public void renewToken_az() {
		String uuid = "abcdefghijklmnopqrstuvwxyz";
		String renewedToken = restSecurityBean.renewToken(uuid);
		Assert.assertEquals(uuid, renewedToken);
	}
	
	@Test
	public void renewToken_AZ() {
		String uuid = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String renewedToken = restSecurityBean.renewToken(uuid);
		Assert.assertEquals(uuid, renewedToken);
	}
	
	@Test
	public void renewToken_tooLong() {
		String uuid = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String renewedToken = restSecurityBean.renewToken(uuid);
		Assert.assertNull(renewedToken);
	}
	
	@Test
	public void renewToken_notAllowed() {
		String uuid = "abc:test";
		String renewedToken = restSecurityBean.renewToken(uuid);
		Assert.assertNull(renewedToken);
	}
}
