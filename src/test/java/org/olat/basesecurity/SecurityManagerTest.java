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

package org.olat.basesecurity;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SecurityTestSuite is a container of all Tests in this package.
 * 
 * @author Andreas Ch. Kapp
 */
public class SecurityManagerTest extends OlatTestCase {

	private Identity s1, s2;
	private static String testLogin = "test-login";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	
	@Before
	public void setup() throws Exception {
		s1 = JunitTestHelper.createAndPersistIdentityAsUser(testLogin);
		s2 = JunitTestHelper.createAndPersistIdentityAsUser("coop");
	}

	// Already tested in BusinessGroupTest :
	//  - getGroupsWithPermissionOnOlatResourceable
	//  - getIdentitiesWithPermissionOnOlatResourceable
	/**
	 * 
	 */
	@Test
	public void testGetIdentitiesByPowerSearch() {
		// test using visibility search
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, null, null, null);
		Assert.assertEquals(1,userList.size());
		Identity identity = userList.get(0);
		Assert.assertEquals(testLogin,identity.getName());
		// test using powser search
		userList = securityManager.getIdentitiesByPowerSearch(testLogin, null, true, null, null, null, null, null, null, null);
		Assert.assertEquals(1,userList.size());
		identity = userList.get(0);
		Assert.assertEquals(testLogin,identity.getName());
	}
	
	@Test
	public void testGetIdentitiesByPowerSearchWithuserProperties() {
		Map<String, String> userProperties = new HashMap<>();
		userProperties.put(UserConstants.FIRSTNAME, "first"+ testLogin);
		userProperties.put(UserConstants.LASTNAME, "last"+ testLogin);
		// test using visibility search
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, userProperties, true, null, null, null, null);
		Assert.assertEquals(1,userList.size());
		Identity identity = userList.get(0);
		Assert.assertEquals("first" + testLogin,identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
		// test using powser search
		userList = securityManager.getIdentitiesByPowerSearch(testLogin, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(1,userList.size());
		identity = userList.get(0);
		Assert.assertEquals("first" + testLogin,identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
	}

	@Test
	public void testGetIdentitiesByPowerSearchWithConjunctionFlag() {
		// 1) two fields that match to two different users
		Map<String, String> userProperties = new HashMap<>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, s2.getUser().getProperty(UserConstants.LASTNAME, null));
		// with AND search (conjunction) no identity is found
		List<Identity> userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(0, userList.size());
		// with OR search both identities are found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null);
		Assert.assertEquals(2, userList.size());

		// 2) two fields wheras only one matches to one single user
		userProperties = new HashMap<>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(0, userList.size());
		// with OR search first identity ist found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null);
		Assert.assertEquals(1, userList.size());
	}
	
	@Test
	public void testGetIdentitiesByPowerSearchWithAuthProviders() {
		// 1) only auth providers and login
		String[] authProviders = {BaseSecurityModule.getDefaultAuthProviderIdentifier()};
		for (int i = 0; i < authProviders.length; i++) {
			Assert.assertTrue("Provider name.length must be <= 8", authProviders[i].length() <= 8);
		}
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, authProviders, null, null);
		Assert.assertEquals(1,userList.size());
		Identity identity =  userList.get(0);
		Assert.assertEquals(testLogin,identity.getName());
		String[] nonAuthProviders = {"NonAuth"};
		for (int i = 0; i < nonAuthProviders.length; i++) {
			assertTrue("Provider name.length must be <= 8", nonAuthProviders[i].length() <= 8);
		}
		userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, nonAuthProviders, null, null);
	  	Assert.assertEquals(0,userList.size());
		
		// 2) two fields wheras only one matches to one single user
		Map<String, String> userProperties = new HashMap<>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, authProviders, null, null, null, null, null);
		Assert.assertEquals(0, userList.size());
		// with OR search first identity ist found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, authProviders, null, null, null, null, null);
		Assert.assertEquals(1, userList.size());

		// 3) two fields wheras only one matches to one single user
		securityManager.createAndPersistAuthentication(s1, "mytest_p", s1.getName(), "sdf", Encoder.Algorithm.sha512);
		String[] myProviders = new String[] {"mytest_p", "non-prov"};
		for (int i = 0; i < myProviders.length; i++) {
			Assert.assertTrue("Provider name.length must be <= 8", myProviders[i].length() <= 8);
		}
		userProperties = new HashMap<>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, myProviders, null, null, null, null, null);
		Assert.assertEquals(0, userList.size());
		// with OR search identity is found via auth provider and via first name
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, myProviders, null, null, null, null, null);
		Assert.assertEquals(1, userList.size());
	}
	
	@Test
	public void testGetAuthentications() {
		List<Authentication> authentications = securityManager.getAuthentications(s1);
		Authentication authentication = authentications.get(0);
		Assert.assertEquals(testLogin,authentication.getAuthusername());
	}

	@Test
	public void testFindAuthenticationByAuthusername() {
		Authentication authentication = securityManager.findAuthenticationByAuthusername(testLogin, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertEquals(testLogin,authentication.getAuthusername());
	}
	
	@Test
	public void testFindAuthenticationByAuthusername_attack() {
		String testLoginHacked = "*est-logi*";
		Authentication authentication1 = securityManager.findAuthenticationByAuthusername(testLoginHacked, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertNull(authentication1);
		
		String testLoginHacked2 = "$est-login";
		Authentication authentication2 = securityManager.findAuthenticationByAuthusername(testLoginHacked2, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertNull(authentication2);	
	}

	@Test
	public void testUpdateLatLogin() {
		securityManager.setIdentityLastLogin(s1);
		dbInstance.commitAndCloseSession();

		s1 = securityManager.loadIdentityByKey(s1.getKey());
		Date lastLogin = s1.getLastLogin();
		Assert.assertNotNull(lastLogin);
	}

	@Test
	public void testCountUniqueUserLoginsSince() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -100);
		Long initialUserLogins = securityManager.countUniqueUserLoginsSince(cal.getTime());
		Assert.assertNotNull(initialUserLogins);
		Assert.assertTrue(initialUserLogins.longValue() >= 0);
	}
}