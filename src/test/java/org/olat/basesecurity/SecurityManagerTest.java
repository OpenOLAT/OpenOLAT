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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
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

	private IdentityImpl s1,s2,s3,testAdmin;
	private static String testLogin = "test-login";
	
	@Autowired
	private BaseSecurity securityManager;

	// Already tested in BusinessGroupTest :
	//  - getGroupsWithPermissionOnOlatResourceable
	//  - getIdentitiesWithPermissionOnOlatResourceable
	/**
	 * 
	 */
	@Test
	public void testGetIdentitiesByPowerSearch() {
		// test using visibility search
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, null, null, null, null);
		assertEquals(1,userList.size());
		Identity identity = userList.get(0);
		assertEquals(testLogin,identity.getName());
		// test using powser search
		userList = securityManager.getIdentitiesByPowerSearch(testLogin, null, true, null, null, null, null, null, null, null, null);
		assertEquals(1,userList.size());
		identity = userList.get(0);
		assertEquals(testLogin,identity.getName());
	}
	
	@Test
	public void testGetIdentitiesByPowerSearchWithuserProperties() {
		Map<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "first"+ testLogin);
		userProperties.put(UserConstants.LASTNAME, "last"+ testLogin);
		// test using visibility search
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, userProperties, true, null, null, null, null, null);
		assertEquals(1,userList.size());
		Identity identity = userList.get(0);
		assertEquals("first" + testLogin,identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
		// test using powser search
		userList = securityManager.getIdentitiesByPowerSearch(testLogin, userProperties, true, null, null, null, null, null, null, null, null);
		assertEquals(1,userList.size());
		identity = userList.get(0);
		assertEquals("first" + testLogin,identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
	}

	@Test
	public void testGetIdentitiesByPowerSearchWithConjunctionFlag() {
		// 1) two fields that match to two different users
		Map<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, s2.getUser().getProperty(UserConstants.LASTNAME, null));
		// with AND search (conjunction) no identity is found
		List<Identity> userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
		assertEquals(0, userList.size());
		// with OR search both identities are found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
		assertEquals(2, userList.size());

		// 2) two fields wheras only one matches to one single user
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
		assertEquals(0, userList.size());
		// with OR search first identity ist found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
		assertEquals(1, userList.size());
	}

	
	@Test
	public void testGetIdentitiesByPowerSearchWithAuthProviders() {
		// 1) only auth providers and login
	  String[] authProviders = {BaseSecurityModule.getDefaultAuthProviderIdentifier()};
		for (int i = 0; i < authProviders.length; i++) {
			assertTrue("Provider name.length must be <= 8", authProviders[i].length() <= 8);
		}
	  List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, null, authProviders, null, null);
		assertEquals(1,userList.size());
		Identity identity =  userList.get(0);
		assertEquals(testLogin,identity.getName());
	  String[] nonAuthProviders = {"NonAuth"};
		for (int i = 0; i < nonAuthProviders.length; i++) {
			assertTrue("Provider name.length must be <= 8", nonAuthProviders[i].length() <= 8);
		}
	  userList = securityManager.getVisibleIdentitiesByPowerSearch(testLogin, null, true, null, null, nonAuthProviders, null, null);
		assertEquals(0,userList.size());
		
		// 2) two fields wheras only one matches to one single user
		Map<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, authProviders, null, null, null, null, null);
		assertEquals(0, userList.size());
		// with OR search first identity ist found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, authProviders, null, null, null, null, null);
		assertEquals(1, userList.size());

		// 3) two fields wheras only one matches to one single user
		securityManager.createAndPersistAuthentication(s1, "mytest_p", s1.getName(), "sdf", Encoder.Algorithm.sha512);
		String[] myProviders = new String[] {"mytest_p", "non-prov"};
		for (int i = 0; i < myProviders.length; i++) {
			assertTrue("Provider name.length must be <= 8", myProviders[i].length() <= 8);
		}
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, s1.getUser().getProperty(UserConstants.FIRSTNAME, null));
		userProperties.put(UserConstants.LASTNAME, "some nonexisting value");
		// with AND search (conjunction) no identity is found
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, myProviders, null, null, null, null, null);
		assertEquals(0, userList.size());
		// with OR search identity is found via auth provider and via first name
		userList = securityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, myProviders, null, null, null, null, null);
		assertEquals(1, userList.size());
	}
	
	@Test
	public void testRemoveIdentityFromSecurityGroup() {
		SecurityGroup olatUsersGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		assertTrue(securityManager.isIdentityInSecurityGroup(s1, olatUsersGroup));
		securityManager.removeIdentityFromSecurityGroup(s1, olatUsersGroup);
		assertFalse(securityManager.isIdentityInSecurityGroup(s1, olatUsersGroup));
		securityManager.addIdentityToSecurityGroup(s1, olatUsersGroup);
		assertTrue(securityManager.isIdentityInSecurityGroup(s1, olatUsersGroup));
	}
	
	@Test
	public void testGetIdentitiesAndDateOfSecurityGroup() {
		SecurityGroup olatUsersGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(olatUsersGroup);// not sortedByAddDate
		assertTrue("Found no users", identities.size() > 0);
		Object[] firstIdentity = identities.get(0);
		assertTrue("Wrong type, Identity[0] must be an Identity", firstIdentity[0] instanceof Identity);
		assertTrue("Wrong type, Identity[1] must be a Date", firstIdentity[1] instanceof Date);
	}
	
	@Test
	public void testGetAuthentications() {
		List<Authentication> authentications = securityManager.getAuthentications(s1);
		Authentication authentication = authentications.get(0);
		assertEquals(testLogin,authentication.getAuthusername());
	}

	@Test public void testFindAuthenticationByAuthusername() {
		Authentication authentication = securityManager.findAuthenticationByAuthusername(testLogin, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		assertEquals(testLogin,authentication.getAuthusername());
	}

	@Test @Ignore
	public void testCountUniqueUserLoginsSince(){

   	// Set lastLogin for 4 test users to now
		DB db = DBFactory.getInstance();
		Date now = new Date();
		s1.setLastLogin(now);
		db.updateObject(s1);
		s2.setLastLogin(now);
		db.updateObject(s2);
		s3.setLastLogin(now);
		db.updateObject(s3);
		testAdmin.setLastLogin(now);
		db.updateObject(testAdmin);
		db.closeSession();

		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.DAY_OF_YEAR, -100);// -100
		assertTrue("Found no user-logins", securityManager.countUniqueUserLoginsSince(c1.getTime()) >= 4); 
		Long initialUserLogins = securityManager.countUniqueUserLoginsSince(c1.getTime());
		
		// Set lastLogin for the 4 test-users
		c1 = Calendar.getInstance();
		c1.add(Calendar.DAY_OF_YEAR, -100);
		s1.setLastLogin(c1.getTime());
		c1 = Calendar.getInstance();
		c1.add(Calendar.DAY_OF_YEAR, -15); 
		s2.setLastLogin(c1.getTime());
		s3.setLastLogin(c1.getTime());
		testAdmin.setLastLogin(c1.getTime());
	
		// Set lastLogin for 4 test users
		db.updateObject(s1);
		db.updateObject(s2);
		db.updateObject(s3);
		db.updateObject(testAdmin);
		db.closeSession();
		
		Calendar c2 = Calendar.getInstance();		
		c2.add(Calendar.DAY_OF_YEAR, -2); 
		assertEquals(initialUserLogins - 4, securityManager.countUniqueUserLoginsSince(c2.getTime()).intValue() );
		c2 = Calendar.getInstance();
		c2.add(Calendar.DAY_OF_YEAR, - 14); 
		assertEquals(initialUserLogins - 4, securityManager.countUniqueUserLoginsSince(c2.getTime()).intValue());
		c2 = Calendar.getInstance();
		c2.add(Calendar.DAY_OF_YEAR, - 16); 
		assertEquals(initialUserLogins - 1, securityManager.countUniqueUserLoginsSince(c2.getTime()).intValue());
		c2 = Calendar.getInstance();
		c2.add(Calendar.DAY_OF_YEAR, - 99); 
		assertEquals(initialUserLogins - 1, securityManager.countUniqueUserLoginsSince(c2.getTime()).intValue());
		c2 = Calendar.getInstance();
		c2.add(Calendar.DAY_OF_YEAR, - 101); 
		assertEquals(initialUserLogins, securityManager.countUniqueUserLoginsSince(c2.getTime()));
	}

	@Before
	public void setup() throws Exception {
		s1 = (IdentityImpl)JunitTestHelper.createAndPersistIdentityAsUser(testLogin);
		s2 = (IdentityImpl)JunitTestHelper.createAndPersistIdentityAsUser("coop");
		s3 = (IdentityImpl)JunitTestHelper.createAndPersistIdentityAsAuthor("diesbach");
		testAdmin = (IdentityImpl)JunitTestHelper.createAndPersistIdentityAsAdmin("testAdmin");
	}
}