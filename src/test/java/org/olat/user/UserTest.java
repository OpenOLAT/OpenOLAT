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

package org.olat.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desciption: jUnit testsuite to test the OLAT user module.
 * Most tests are method tests of the user manager. Currently no tests for
 * actions are available du to missing servlet stuff.
 * 
 * @author Florian Gnägi
 */
public class UserTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(UserTest.class);
	
	// variables for test fixture
	private User u1, u2, u3;
	private Identity i1, i2, i3;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UserLifecycleManager userLifecycleManager;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup()throws Exception {
		log.info("running before...: "+this.hashCode());
		// create some users with user manager
		// set up fixture using the user manager
		
		if (JunitTestHelper.findIdentityByLogin("judihui") == null) {
			u1 = userManager.createUser("judihui", "judihui", "judihui@id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALEMAIL, "instjudihui@id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALNAME, "id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i1 = securityManager.createAndPersistIdentityAndUser(null, u1.getProperty(UserConstants.LASTNAME, new Locale("en")), null,
					u1, "OLAT", u1.getProperty(UserConstants.LASTNAME, new Locale("en")), "", null);
		} else {
			log.info("Does not create user, found 'judihui' already in db");
			i1 = JunitTestHelper.findIdentityByLogin("judihui");
			u1 = i1.getUser();
		}
		if (JunitTestHelper.findIdentityByLogin("migros") == null) {
			u2 = userManager.createUser("migros", "migros", "migros@id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALEMAIL, "instmigros@id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALNAME, "id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i2 = securityManager.createAndPersistIdentityAndUser(null, u2.getProperty(UserConstants.LASTNAME, new Locale("en")), null,
					u2, "OLAT", u2.getProperty(UserConstants.LASTNAME, new Locale("en")), "", null);
		} else {
			log.info("Does not create user, found 'migros' already in db");
			i2 = JunitTestHelper.findIdentityByLogin("migros");
			u2 = i2.getUser();
		}
		if (JunitTestHelper.findIdentityByLogin("salat") == null) {
			u3 = userManager.createUser("salat", "salat", "salat@id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALEMAIL,"instsalat@id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALNAME, "id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i3 = securityManager.createAndPersistIdentityAndUser(null, u3.getProperty(UserConstants.LASTNAME, new Locale("en")), null,
					u3, "OLAT", u3.getProperty(UserConstants.LASTNAME, new Locale("en")), "", null);
		} else {
			log.info("Does not create user, found 'salat' already in db");
			i3 = JunitTestHelper.findIdentityByLogin("salat");
			u3 = i3.getUser();
		}
	}

	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test
	public void testUmCreateUser() throws Exception {
		// search for user u1 manually. SetUp puts the user in the database
		// so we look if we can find the user in the database
		log.debug("Entering testUmCreateUser()");
		User found = userManager.findUniqueIdentityByEmail("judihui@id.uzh.ch").getUser();
		assertTrue(u1.getKey().equals(found.getKey()));
	}

	
	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test public void testFindUserByEmail() throws Exception {
		log.debug("Entering testFindUserByEmail()");
		// find via users email
		User found = userManager.findUniqueIdentityByEmail("judihui@id.uzh.ch").getUser();
		assertTrue(u1.getKey().equals(found.getKey()));
		// find via users institutional email
		found = userManager.findUniqueIdentityByEmail("judihui@id.uzh.ch").getUser();
		assertTrue(u1.getKey().equals(found.getKey()));
	}
	
	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test public void testFindIdentityByEmail() throws Exception {
		log.debug("Entering testFindIdentityByEmail()");
		// find via users email
		Identity found = userManager.findUniqueIdentityByEmail("judihui@id.uzh.ch");
		Assert.assertNotNull(found);
		assertTrue(i1.getKey().equals(found.getKey()));
		// find via users institutional email
		found = userManager.findUniqueIdentityByEmail("instjudihui@id.uzh.ch");
		Assert.assertNotNull(found);
		assertTrue(i1.getKey().equals(found.getKey()));
		// find must be equals
		found = userManager.findUniqueIdentityByEmail("instjudihui@id.uzh.ch.ch");
		assertNull(found);
	}

	/**
	 * Test if usermanager.findUserByKey() works
	 * @throws Exception
	 */
	@Test public void testUmFindUserByKey() throws Exception {
		log.debug("Entering testUmFindUserByKey()");
		// find users that do exist
		User u3test = userManager.loadUserByKey(u1.getKey());
		assertTrue(u1.getKey().equals(u3test.getKey()));
	}

	/**
	 * if usermanager finds users by institutional user identifier
	 * @throws Exception
	 */
	@Test
	public void testUmFindUserByInstitutionalUserIdentifier() throws Exception {
		Map<String, String> searchValue = new HashMap<>();
		searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
		List<Identity> result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertTrue("must have elements", result != null);
		assertEquals("at least three elements", 3, result.size());

		String instEmailU1 = result.get(0).getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		String instEmailU2 = result.get(1).getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		String instEmailU3 = result.get(2).getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		
		//check that the three found results correspond with the configured
		boolean found1 = instEmailU1.equals("instjudihui@id.uzh.ch") || instEmailU2.equals("instjudihui@id.uzh.ch") || instEmailU3.equals("instjudihui@id.uzh.ch");
		assertTrue("find instjudihui@id.uzh.ch", found1);
		
		boolean found2 = instEmailU1.equals("instmigros@id.migros.uzh.ch") || instEmailU2.equals("instmigros@id.migros.uzh.ch") || instEmailU3.equals("instmigros@id.migros.uzh.ch");
		assertTrue("find instmigros@id.migros.uzh.ch", found2);
		
		boolean found3 = instEmailU1.equals("instsalat@id.salat.uzh.ch") || instEmailU2.equals("instsalat@id.salat.uzh.ch") || instEmailU3.equals("instsalat@id.salat.uzh.ch");
		assertTrue("find instsalat@id.salat.uzh.ch", found3);
	}
		
	/**
	 * persist a user that did not exist previously in the database These is the
	 * case if the key of a user is null
	 * @throws Exception
	 */
	@Test
	public void testUpdateNewUser() throws Exception {
		UserImpl u5 = new UserImpl();
		u5.setFirstName("newuser");
		u5.setLastName("newuser");
		u5.setEmail("new@user.com");
		u5.setCreationDate(new Date());
		dbInstance.saveObject(u5);
		u5.setProperty(UserConstants.EMAIL, "updated@email.com");
		userManager.updateUser(u5);
		userManager.loadUserByKey(u5.getKey());
		assertTrue(u5.getProperty(UserConstants.EMAIL, null).equals("updated@email.com"));
	}

	/**
	 * test if user profile does work 
	 * @throws Exception
	 */
	@Test public void testSetUserProfile() throws Exception {
		// preferences that are not set to a value must not return null
		// The preferences object itself must not be null - a user always has
		// a preferences object
		String fs = u1.getPreferences().getLanguage();
		assertTrue(fs != null);
		// change preferences values and look it up (test only one
		// attribute, we assume that getters and setters do work!)
		u1.getPreferences().setLanguage("de");
		userManager.updateUser(u1);
		User u1test = userManager.loadUserByKey(u1.getKey());
		assertTrue(u1test.getPreferences().getLanguage().matches("de"));
	}

	/**
	 * test set and get the user's charset 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUmFindCharsetPropertyByIdentity() throws Exception{
	   User testuser = userManager.loadUserByKey(u1.getKey());
	   Assert.assertNotNull(testuser);
	   
	   Identity identity = securityManager.findIdentityByName(u1.getProperty(UserConstants.LASTNAME, null));
	   
	   userManager.setUserCharset(identity, WebappHelper.getDefaultCharset());

	   dbInstance.closeSession(); // simulate user clicks
	   String charset = userManager.getUserCharset(identity);
	   assertTrue(charset.matches(WebappHelper.getDefaultCharset()));
	}
	
	@Test
	public void testUpdateUserProperties() {
		//create a user
		String login = UUID.randomUUID().toString().replace("-", "");
		Identity identity = createIdentityWithProperties(login, "id.salat.uzh.ch");
		String institutionalEmail = "inst" + login + "@id.salat.uzh.ch";
		dbInstance.commitAndCloseSession();

		//1. begin the tests: update the institutional email
		// search with power search (to compare result later on with same search)
		Map<String, String> searchValue = new HashMap<>();
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		// find identity 1
		List<Identity> result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// setting null should remove this property but first reload user
		User user = userManager.loadUserByKey(identity.getUser().getKey());
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "bla@bla.ch");		
		userManager.updateUser(user);
		dbInstance.commitAndCloseSession();
		
		// try to find it via deleted property
		searchValue = new HashMap<>();
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		// find identity 1
		result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertEquals(0, result.size());

		//2. begin the tests: update the first name
		// search via first name
		searchValue = new HashMap<>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		// find identity 1
		result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		
		//update user first name
		user = userManager.loadUserByKey(identity.getUser().getKey());
		user.setProperty(UserConstants.FIRSTNAME, "rotwein");
		userManager.updateUser(user);
		dbInstance.commitAndCloseSession();

		// try to find it via old property
		searchValue = new HashMap<>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		// find identity 1
		result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertEquals(0, result.size());
		// try to find it via updated property
		Map<String,String> searchRotweinValue = new HashMap<>();
		searchRotweinValue.put(UserConstants.FIRSTNAME, "rotwein");
		// find identity 1
		List<Identity> rotweinList = securityManager.getIdentitiesByPowerSearch(null, searchRotweinValue, true, null, null, null, null, null, null, null);
		assertFalse(rotweinList.isEmpty());
		for(Identity id:result) {
			Assert.assertEquals("rotwein", id.getUser().getProperty(UserConstants.FIRSTNAME, null));
		}
	}

	/**
	 * Test the user delete methods
	 */
	@Test
	public void testDeleteUser() {
		//create a user
		String login = UUID.randomUUID().toString().replace("-", "");
		String institutionName = "id." + login.toLowerCase() + ".ch";
		Identity identity = createIdentityWithProperties(login, institutionName);
		String institutionalEmail = "inst" + login + "@" + institutionName;
		dbInstance.commitAndCloseSession();
		
		//test user deletion
	
		// user still exists
		List<Identity> result = securityManager.getVisibleIdentitiesByPowerSearch(login, null, true, null, null, null, null);
		assertEquals(1, result.size());
		result = securityManager.getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// search with power search (to compare result later on with same search)
		Map<String, String> searchValue = new HashMap<>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		searchValue.put(UserConstants.LASTNAME, login);
		searchValue.put(UserConstants.FIRSTNAME, login);
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		searchValue.put(UserConstants.INSTITUTIONALNAME, institutionName);
		searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, institutionName);
		// find identity
		result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// find identity via institutional id
		result = securityManager.getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// delete user now
		userLifecycleManager.deleteIdentity(identity, null);
		dbInstance.commitAndCloseSession();
		
		// check if deleted successfully
		result = securityManager.getVisibleIdentitiesByPowerSearch(login, null, true, null, null, null, null);
		assertEquals(0, result.size());
		// not visible, but still there when using power search
		result = securityManager.getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null);
		
		List<Identity> deletedIdentities = securityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_DELETED);
		boolean deleted = false;
		for(Identity deletedIdentity:deletedIdentities) {
			if(identity.getKey().equals(deletedIdentity.getKey())) {
				deleted = true;
			}
		}
		Assert.assertTrue(deleted);

		List<Identity> activeIdentities = securityManager.getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		Assert.assertEquals(0, activeIdentities.size());
		
		List<Identity> allActiveIdentities = securityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		boolean active = false;
		for(Identity activeIdentity:allActiveIdentities) {
			if(identity.getKey().equals(activeIdentity.getKey())) {
				active = true;
			}
		}
		Assert.assertFalse(active);

		// test if user attributes have been deleted successfully
		// find identity 1 not anymore
		List<Identity> searchResult_10 = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null);
		Assert.assertEquals(0, searchResult_10.size());
		// find identity via first, last and instuser id (fields as 12.5 deleted too)
		List<Identity> searchResult_11 = securityManager.getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null);
		Assert.assertEquals(0, searchResult_11.size());
		
		// check using other methods
		Identity loadByInstitutionalEmail = userManager.findUniqueIdentityByEmail(institutionalEmail);
		assertNull("Deleted identity with email '" + institutionalEmail + "' should not be found with 'UserManager.findIdentityByEmail'", loadByInstitutionalEmail);
	}

	@Test
	public void testEquals() {
		User user1 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch").getUser();
		User user2 = userManager.findUniqueIdentityByEmail("migros@id.migros.uzh.ch").getUser();
		User user1_2 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch").getUser();

		assertFalse("Wrong equals implementation, different types are recognized as equals ",user1.equals(Integer.valueOf(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",user1.equals(user2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",user1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",user1.equals(user1));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",user1.equals(user1_2));
	}
	
	@Test
	public void testEqualsIdentity() {
		Identity ident1 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch");
		Identity ident2 = userManager.findUniqueIdentityByEmail("migros@id.migros.uzh.ch");
		Identity ident1_2 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch");

		assertFalse("Wrong equals implementation, different types are recognized as equals ",ident1.equals(Integer.valueOf(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",ident1.equals(ident2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",ident1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1_2));
	}
	
	@Test
	public void testHashCode() {
		User user1 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch").getUser();
		User user2 = userManager.findUniqueIdentityByEmail("migros@id.migros.uzh.ch").getUser();
		User user1_2 = userManager.findUniqueIdentityByEmail("salat@id.salat.uzh.ch").getUser();

		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",user1.hashCode() == user1.hashCode());
		assertFalse("Wrong hashCode implementation, different users have same hash-code",user1.hashCode() == user2.hashCode());
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",user1.hashCode() == user1_2.hashCode());
	}
	
	private Identity createIdentityWithProperties(String login, String institution) {
		User user = userManager.createUser(login, login, login + "@" + institution);
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "inst" + login + "@" + institution);
		user.setProperty(UserConstants.INSTITUTIONALNAME, institution);
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, institution);
		return securityManager.createAndPersistIdentityAndUser(null, login, null, user, "OLAT", login, "secret", null);
	}
}
