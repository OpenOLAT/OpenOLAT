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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
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

	private static OLog log = Tracing.createLoggerFor(UserTest.class);
	// variables for test fixture
	private User u1, u2, u3;
	private Identity i1, i2, i3;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private DB dbInstance;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup()throws Exception {
		System.out.println("running before...: "+this.hashCode());
		// create some users with user manager
		// set up fixture using the user manager
		UserManager um = UserManager.getInstance();
		
		BaseSecurity sm = BaseSecurityManager.getInstance();
		if (sm.findIdentityByName("judihui") == null) {
			u1 = um.createUser("judihui", "judihui", "judihui@id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALEMAIL, "instjudihui@id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALNAME, "id.uzh.ch");
			u1.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i1 = sm.createAndPersistIdentityAndUser(u1.getProperty(UserConstants.LASTNAME, new Locale("en")), null, u1, "OLAT", u1.getProperty(UserConstants.LASTNAME, new Locale("en")),"");
		} else {
			System.out.println("Does not create user, found 'judihui' already in db");
			i1 = sm.findIdentityByName("judihui");
			u1 = i1.getUser();
		}
		if (sm.findIdentityByName("migros") == null) {
			u2 = um.createUser("migros", "migros", "migros@id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALEMAIL, "instmigros@id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALNAME, "id.migros.uzh.ch");
			u2.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i2 = sm.createAndPersistIdentityAndUser(u2.getProperty(UserConstants.LASTNAME, new Locale("en")), null, u2, "OLAT", u2.getProperty(UserConstants.LASTNAME, new Locale("en")),"");
		} else {
			System.out.println("Does not create user, found 'migros' already in db");
			i2 = sm.findIdentityByName("migros");
			u2 = i2.getUser();
		}
		if (sm.findIdentityByName("salat") == null) {
			u3 = um.createUser("salat", "salat", "salat@id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALEMAIL,"instsalat@id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALNAME, "id.salat.uzh.ch");
			u3.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
			i3 = sm.createAndPersistIdentityAndUser(u3.getProperty(UserConstants.LASTNAME, new Locale("en")), null, u3," OLAT", u3.getProperty(UserConstants.LASTNAME, new Locale("en")),"");
		} else {
			System.out.println("Does not create user, found 'salat' already in db");
			i3 = sm.findIdentityByName("salat");
			u3 = i3.getUser();
		}
	}

	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test public void testUmCreateUser() throws Exception {
		// search for user u1 manually. SetUp puts the user in the database
		// so we look if we can find the user in the database
		log.debug("Entering testUmCreateUser()");
		UserManager um = UserManager.getInstance();
		User found = um.findUserByEmail("judihui@id.uzh.ch");
		assertTrue(u1.getKey().equals(found.getKey()));
	}

	
	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test public void testFindUserByEmail() throws Exception {
		log.debug("Entering testFindUserByEmail()");
		UserManager um = UserManager.getInstance();
		// find via users email
		User found = um.findUserByEmail("judihui@id.uzh.ch");
		assertTrue(u1.getKey().equals(found.getKey()));
		// find via users institutional email
		found = um.findUserByEmail("judihui@id.uzh.ch");
		assertTrue(u1.getKey().equals(found.getKey()));
	}
	
	@Test public void testEmailInUse() throws Exception {
		log.debug("Entering testEmailInUse()");
		UserManager um = UserManager.getInstance();
		// find via users email
		boolean found = um.userExist("judihui@id.uzh.ch");
		assertTrue(found);
		// find via users institutional email
		found = um.userExist("judihui@id.uzh.ch");
		assertTrue(found);
		// i don't like like
		found = um.userExist("judihui@id.uzh.ch.ch");
		assertFalse(found);
		// doesn't exists
		found = um.userExist("judihui@hkfls.com");
		assertFalse(found);
	}

	/**
	 *  Test if usermanager.createUser() works
	 * @throws Exception
	 */
	@Test public void testFindIdentityByEmail() throws Exception {
		log.debug("Entering testFindIdentityByEmail()");
		UserManager um = UserManager.getInstance();
		// find via users email
		Identity found = um.findIdentityByEmail("judihui@id.uzh.ch");
		Assert.assertNotNull(found);
		assertTrue(i1.getKey().equals(found.getKey()));
		// find via users institutional email
		found = um.findIdentityByEmail("instjudihui@id.uzh.ch");
		Assert.assertNotNull(found);
		assertTrue(i1.getKey().equals(found.getKey()));
		// find must be equals
		found = um.findIdentityByEmail("instjudihui@id.uzh.ch.ch");
		assertNull(found);
	}

	/**
	 * Test if usermanager.findUserByKey() works
	 * @throws Exception
	 */
	@Test public void testUmFindUserByKey() throws Exception {
		log.debug("Entering testUmFindUserByKey()");
		UserManager um = UserManager.getInstance();
		// find users that do exist
		User u3test = um.loadUserByKey(u1.getKey());
		assertTrue(u1.getKey().equals(u3test.getKey()));
	}

	/**
	 * if usermanager finds users by institutional user identifier
	 * @throws Exception
	 */
	@Test
	public void testUmFindUserByInstitutionalUserIdentifier() throws Exception {
		Map<String, String> searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
		List<Identity> result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
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
	@Test public void testUpdateNewUser() throws Exception {
		UserManager um1 = UserManager.getInstance();
		DB db = DBFactory.getInstance();
		UserImpl u5 = new UserImpl();
		u5.setFirstName("newuser");
		u5.setLastName("newuser");
		u5.setEmail("new@user.com");
		u5.setCreationDate(new Date());
		u5.getPreferences().setFontsize("normal");
		db.saveObject(u5);
		u5.setProperty(UserConstants.EMAIL, "updated@email.com");
		um1.updateUser(u5);
		um1.loadUserByKey(u5.getKey());
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
		UserManager um = UserManager.getInstance();
		um.updateUser(u1);
		User u1test = um.loadUserByKey(u1.getKey());
		assertTrue(u1test.getPreferences().getLanguage().matches("de"));
	}

	/**
	 * test set and get the user's charset 
	 * 
	 * @throws Exception
	 */
	@Test public void testUmFindCharsetPropertyByIdentity() throws Exception{
	   UserManager um = UserManager.getInstance();
	   User testuser = um.loadUserByKey(u1.getKey());
	   Assert.assertNotNull(testuser);
	   
	   BaseSecurity sm = BaseSecurityManager.getInstance();
	   Identity identity = sm.findIdentityByName(u1.getProperty(UserConstants.LASTNAME, null));
	   
	   um.setUserCharset(identity, WebappHelper.getDefaultCharset());

	   DBFactory.getInstance().closeSession(); // simulate user clicks
	   String charset = um.getUserCharset(identity);
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
		UserManager um = UserManager.getInstance();
		// search with power search (to compare result later on with same search)
		Map<String, String> searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		// find identity 1
		List<Identity> result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// setting null should remove this property but first reload user
		User user = um.loadUserByKey(identity.getUser().getKey());
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "bla@bla.ch");		
		um.updateUser(user);
		dbInstance.commitAndCloseSession();
		
		// try to find it via deleted property
		searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		// find identity 1
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(0, result.size());

		//2. begin the tests: update the first name
		// search via first name
		searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		// find identity 1
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		
		//update user first name
		user = um.loadUserByKey(identity.getUser().getKey());
		user.setProperty(UserConstants.FIRSTNAME, "rotwein");
		um.updateUser(user);
		dbInstance.commitAndCloseSession();

		// try to find it via old property
		searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		// find identity 1
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(0, result.size());
		// try to find it via updated property
		Map<String,String> searchRotweinValue = new HashMap<String, String>();
		searchRotweinValue.put(UserConstants.FIRSTNAME, "rotwein");
		// find identity 1
		List<Identity> rotweinList = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchRotweinValue, true, null, null, null, null, null, null, null, null);
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
		String email = login + "@" + institutionName;
		dbInstance.commitAndCloseSession();
		
		//test user deletion
		
		UserManager um = UserManager.getInstance();
		// user still exists
		List<Identity> result = BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(login, null, true, null, null, null, null, null);
		assertEquals(1, result.size());
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// search with power search (to compare result later on with same search)
		Map<String, String> searchValue = new HashMap<String, String>();
		searchValue.put(UserConstants.FIRSTNAME, login);
		searchValue.put(UserConstants.LASTNAME, login);
		searchValue.put(UserConstants.FIRSTNAME, login);
		searchValue.put(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		searchValue.put(UserConstants.INSTITUTIONALNAME, institutionName);
		searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, institutionName);
		// find identity
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// find identity via institutional id
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null, null);
		assertEquals(1, result.size());
		// delete user now
		UserDeletionManager udm = UserDeletionManager.getInstance();
		udm.deleteIdentity(identity);
		// check if deleted successfully
		result = BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(login, null, true, null, null, null, null, null);
		assertEquals(0, result.size());
		// not visible, but still there when using power search
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null, null);
		assertEquals("Check first your olat.properties. This test runs only with following olat.properties : keepUserEmailAfterDeletion=true, keepUserLoginAfterDeletion=true",1, result.size());
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null, Identity.STATUS_DELETED);
		assertEquals(1, result.size());
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(login, null, true, null, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		assertEquals(0, result.size());
		// test if user attributes have been deleted successfully
		// find identity 1 not anymore
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
		assertEquals(0, result.size());
		// find identity via first, last and instuser id (non-deletable fields)
		result = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null, null);
		//fxdiff
		assertEquals(1, result.size());
		
		// check using other methods
		Identity loadByInstitutionalEmail = um.findIdentityByEmail(institutionalEmail);
		assertNull("Deleted identity with email '" + institutionalEmail + "' should not be found with 'UserManager.findIdentityByEmail'", loadByInstitutionalEmail);
		// this method must find also the deleted identities
		Identity loadByName = BaseSecurityManager.getInstance().findIdentityByName(login);
		assertNotNull("Deleted identity with username '" + login + "' must be found with 'UserManager.findIdentityByName'", loadByName);
		// Because 'keepUserEmailAfterDeletion=true, keepUserLoginAfterDeletion=true', deleted user must be found 
		Identity loadByEmail = um.findIdentityByEmail(email);
		assertNotNull("Deleted identity with email '" + email + "' must be found with 'UserManager.findIdentityByEmail'", loadByEmail);
	}


	/**
	 * Test how to order by a certain user field
	 */
	@Test public void testOrderByFirstName() {
		DB db = DBFactory.getInstance();
		// join with user to sort by name
		StringBuilder slct = new StringBuilder();
		slct.append("select identity from ");
		slct.append("org.olat.core.id.Identity identity, ");
		slct.append("org.olat.user.UserImpl usr ");
		slct.append("where ");
		slct.append("identity.user = usr.key ");
		slct.append("order by usr.firstName desc");
		List<Identity> results = db.find(slct.toString());
		Identity ident1 = results.get(0);
		Identity ident2 = results.get(1);
		assertTrue((ident2.getName().compareTo(ident1.getName()) < 0));
	}

	@Test public void testEquals() {
		UserManager um = UserManager.getInstance();
		User user1 = um.findUserByEmail("salat@id.salat.uzh.ch");
		User user2 = um.findUserByEmail("migros@id.migros.uzh.ch");
		User user1_2 = um.findUserByEmail("salat@id.salat.uzh.ch");

		assertFalse("Wrong equals implementation, different types are recognized as equals ",user1.equals(new Integer(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",user1.equals(user2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",user1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",user1.equals(user1));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",user1.equals(user1_2));
	}
	
	@Test public void testEqualsIdentity() {
		UserManager um = UserManager.getInstance();
		Identity ident1 = um.findIdentityByEmail("salat@id.salat.uzh.ch");
		Identity ident2 = um.findIdentityByEmail("migros@id.migros.uzh.ch");
		Identity ident1_2 = um.findIdentityByEmail("salat@id.salat.uzh.ch");

		assertFalse("Wrong equals implementation, different types are recognized as equals ",ident1.equals(new Integer(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",ident1.equals(ident2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",ident1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1_2));
	}
	
	@Test public void testHashCode() {
		UserManager um = UserManager.getInstance();
		User user1 = um.findUserByEmail("salat@id.salat.uzh.ch");
		User user2 = um.findUserByEmail("migros@id.migros.uzh.ch");
		User user1_2 = um.findUserByEmail("salat@id.salat.uzh.ch");

		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",user1.hashCode() == user1.hashCode());
		assertFalse("Wrong hashCode implementation, different users have same hash-code",user1.hashCode() == user2.hashCode());
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",user1.hashCode() == user1_2.hashCode());
	}
	
	private Identity createIdentityWithProperties(String login, String institution) {
		User user = userManager.createUser(login, login, login + "@" + institution);
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "inst" + login + "@" + institution);
		user.setProperty(UserConstants.INSTITUTIONALNAME, institution);
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, institution);
		Identity identity = securityManager.createAndPersistIdentityAndUser(login, null, user, "OLAT", login,"secret");
		return identity;
	}

}
