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
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class BaseSecurityTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(BaseSecurityTest.class);
	
	@Autowired
	private BaseSecurity baseSecurityManager;
	

	
	@Test
	public void testGetIdentitiesByPowerSearch() {
		System.out.println("start testGetIdentitiesByPowerSearch");
		try {
			Identity ident = getOrCreateIdentity("anIdentity");
			Identity ident2 = getOrCreateTestIdentity("extremegroovy");
			Assert.assertNotNull(ident2);
			Identity deletedIdent = getOrCreateTestIdentity("delete");
			deletedIdent = baseSecurityManager.saveIdentityStatus(deletedIdent, Identity.STATUS_DELETED);

			SecurityGroup admins = baseSecurityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
			baseSecurityManager.addIdentityToSecurityGroup(deletedIdent, admins);

			
			// basic query to find all system users without restrictions
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size()>0); 
			int numberOfAllUsers = results.size();
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null,Identity.STATUS_ACTIV);
			assertTrue(results.size()>0);
			int numberOfActiveUsers = results.size();
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_DELETED);
			assertTrue(results.size() >0);
			int numberOfDeletedUsers = results.size();
			assertEquals("Number of all users != activeUsers + deletedUsers" , numberOfAllUsers, numberOfActiveUsers + numberOfDeletedUsers);
			
			// user attributes search test
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getIdentitiesByPowerSearch(ident.getName(), null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			assertEquals("Wrong search result (search with username)" + ident.getName() + "' ",ident.getName() , results.get(0).getName());
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(ident.getName(), null, true, null, null, null, null, null);
			assertTrue(results.size() == 1);
			assertEquals("Wrong search result (search with username)" + ident.getName() + "' ",ident.getName() , results.get(0).getName());
			
			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, null, null, null, null, null);
			assertTrue(results.size() == 1);

			results = baseSecurityManager.getIdentitiesByPowerSearch("lalal", null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("lalal", null, true, null, null, null, null, null);
			assertTrue(results.size() == 0);
			
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "*zh2");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			sysoutResults(results);
			assertEquals("Wrong search result 'UserConstants.INSTITUTIONALNAME='*zh2'", 2, results.size());
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 1);
			assertEquals("Wrong search result for visible 'UserConstants.INSTITUTIONALNAME='*zh2'", 1, results.size());

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
			userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "678"); // per default the % is only attached at the end of the query. 
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 0);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
			userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "%678");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 2);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 1);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
			userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "12-345-678");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 2);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 1);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
			userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "888");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 0);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 2);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, Identity.STATUS_ACTIV);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, Identity.STATUS_ACTIV);
			assertTrue(results.size() == 1);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		System.out.println("stop testGetIdentitiesByPowerSearch");
		
	}
	
	@Test public void testGetIdentitiesByPowerSearchWithGroups() {
		System.out.println("start testGetIdentitiesByPowerSearchWithGroups");
		try {
			Identity ident = getOrCreateIdentity("anIdentity");
			Identity ident2 = getOrCreateTestIdentity("extremegroovy");

			// add some stats
			ident = baseSecurityManager.saveIdentityStatus(ident, Identity.STATUS_ACTIV);
			ident2 = baseSecurityManager.saveIdentityStatus(ident2, Identity.STATUS_ACTIV);
			
			// check on those four default groups
			SecurityGroup admins, authors, anonymous;
			admins = baseSecurityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
			anonymous = baseSecurityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
			authors = baseSecurityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
			// test setup: identity is admin and author
			baseSecurityManager.addIdentityToSecurityGroup(ident, admins);
			baseSecurityManager.addIdentityToSecurityGroup(ident2, admins);
			baseSecurityManager.addIdentityToSecurityGroup(ident, authors);

			// security group search test
			DBFactory.getInstance().closeSession();
			SecurityGroup[] groups1 = {admins};
			SecurityGroup[] groups2 = {admins, authors};
			SecurityGroup[] groups3 = {authors};
			SecurityGroup[] groupsInvalid = {anonymous};

			// basic query to find all system users without restrictions
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size()>0); 

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_DELETED);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, null, null, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, null, null, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups2, null, null, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups2, null, null, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups3, null, null, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups3, null, null, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groupsInvalid, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groupsInvalid, null, null, null, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups2, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups2, null, null, null, null);
			assertTrue(results.size() == 1);

			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups1, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups1, null, null, null, null);
			assertTrue(results.size() == 1);

			// policy search test
			DBFactory.getInstance().closeSession();
			List<Policy> policies = baseSecurityManager.getPoliciesOfSecurityGroup(admins);
			PermissionOnResourceable[] adminPermissions = convertPoliciesListToPermissionOnResourceArray(policies);
			policies = baseSecurityManager.getPoliciesOfSecurityGroup(anonymous);
			PermissionOnResourceable[] anonymousPermissions = convertPoliciesListToPermissionOnResourceArray(policies);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, adminPermissions, null, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, adminPermissions, null, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, anonymousPermissions, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, anonymousPermissions, null, null, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups2, anonymousPermissions, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups2, anonymousPermissions, null, null, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups2, adminPermissions, null, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups2, adminPermissions, null, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups3, adminPermissions, null, null, null, null, null, null);
			assertTrue("Found no identities for group 'authors'",results.size() >0);
			boolean isAuthor = true;
			checkIdentitiesHasRoles(results, isAuthor);

			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups3, adminPermissions, null, null, null);
			assertTrue("Found no identities for group 'authors'",results.size() >0);
			checkIdentitiesHasRoles(results, isAuthor);

			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, null, null, null);
			assertTrue(results.size() == 1);
			
			results = baseSecurityManager.getIdentitiesByPowerSearch("dontexist", null, true, groups2, adminPermissions, null, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("dontexist", null, true, groups2, adminPermissions, null, null, null);
			assertTrue(results.size() == 0);

			// authentication provider search
			String[] authProviders = {BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib"};
			String[] authProvidersInvalid = {"nonexist"};// max length 8 !		
			String[] authProviderNone = { null };
			String[] authProvidersAll = {BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib", null };
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProviders, null, null, null, null, null);
			assertTrue(results.size() >0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProviders, null, null);
			assertTrue(results.size() >0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProvidersInvalid, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProvidersInvalid, null, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, authProviders, null, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, authProviders, null, null);
			assertTrue(results.size() == 1);
			
			results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, authProvidersInvalid, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups2, adminPermissions, authProvidersInvalid, null, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch("dontexist", null, true, groups2, adminPermissions, authProviders, null, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("dontexist", null, true, groups2, adminPermissions, authProviders, null, null);
			assertTrue(results.size() == 0);

			Authentication auth = baseSecurityManager.findAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier());
			baseSecurityManager.deleteAuthentication(auth);
			DBFactory.getInstance().closeSession();

			// ultimate tests
			//Identity ident = getOrCreateIdentity("anIdentity");
			Date created = ident.getCreationDate();
			Calendar cal = Calendar.getInstance();
			cal.setTime(created);
			cal.add(Calendar.DAY_OF_MONTH, -5);
			Date before = cal.getTime();

			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getIdentitiesByPowerSearch("groovy", null, true, groups1, adminPermissions, null, before, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("groovy", null, true, groups1, adminPermissions, null, before, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch("extremegroovy", null, true, groups1, adminPermissions, null, before, null, null, null, null);
			assertTrue(results.size() == 1);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("extremegroovy", null, true, groups1, adminPermissions, null, before, null);
			assertTrue(results.size() == 1);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, authProviders, before, null, null, null, null);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasAuthProvider(results,authProviders );
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, authProviders, before, null);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasAuthProvider(results,authProviders );
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, authProviderNone, before, null, null, null, null);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasPermissions(results, adminPermissions);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, authProviderNone, before, null);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasPermissions(results, adminPermissions);

			results = baseSecurityManager.getIdentitiesByPowerSearch("%y", null, true, groups1, adminPermissions, authProvidersAll, before, null, null, null, null);
			assertTrue(results.size() == 2);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("%y", null, true, groups1, adminPermissions, authProvidersAll, before, null);
			assertTrue(results.size() == 2);

			results = baseSecurityManager.getIdentitiesByPowerSearch("%y", null, true, groups1, adminPermissions, authProvidersAll, null, before, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("%y", null, true, groups1, adminPermissions, authProvidersAll, null, before);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, null, before, null, null, null, null);
			sysoutResults(results);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasPermissions(results, adminPermissions);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, adminPermissions, null, before, null);
			assertTrue("Found no results", results.size() > 0);
			checkIdentitiesAreInGroups(results, groups1);
			checkIdentitiesHasPermissions(results, adminPermissions);
			
			System.out.println("Tschagaaa, good job!");
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		System.out.println("stop testGetIdentitiesByPowerSearchWithGroups");
	}
	
	
	@Test public void testGetIdentitiesByPowerSearchAuthProvider(){
	

		System.out.println("start testGetIdentitiesByPowerSearchAuthProvider");
		try {
			//authentication provider search		
			String[] authProviderNone = { null };
			String[] authProvidersAll = {BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib", null };
			
			//check count before adding
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null, null, null, null);
			int prevProviderNoneCount = results.size();
			//add two new users with authProviderNone
			getOrCreateTestIdentityWithAuth("authNoneOne", null);
			getOrCreateTestIdentityWithAuth("authNoneTwo", null);
			DBFactory.getInstance().closeSession();
			// special case: no auth provider
			// test if 2 new users are found.
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null, null, null, null);
			assertEquals("Wrong number of identities, search with (authProviderNone)", prevProviderNoneCount, results.size() - 2);
			
			
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null);
			prevProviderNoneCount = results.size();
			getOrCreateTestIdentityWithAuth("authNoneThree", null);
			getOrCreateTestIdentityWithAuth("authNoneFour", null);
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null);
			assertEquals("Wrong number of visible identities, search with (authProviderNone)", prevProviderNoneCount, results.size() - 2  );
			
			
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null, null, null, null);
			prevProviderNoneCount = results.size();
			getOrCreateTestIdentityWithAuth("authNoneFive", null);
			getOrCreateTestIdentityWithAuth("authNoneSix", null);
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null, null, null, null);
			assertEquals("Wrong number of identities, search with (authProviderNone)", prevProviderNoneCount, results.size() - 2  );
			

			
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null);
			prevProviderNoneCount = results.size();
			getOrCreateTestIdentityWithAuth("authNoneSeven", null);
			getOrCreateTestIdentityWithAuth("authNoneEight", null);
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProviderNone, null, null);
			assertEquals("Wrong number of visible identities, search with (authProviderNone)", prevProviderNoneCount, results.size() - 2);
			
			
			
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProvidersAll, null, null, null, null, null);
			prevProviderNoneCount = results.size();
			//add a new identity per entry of AuthProvidersAll
			getOrCreateTestIdentityWithAuth("authTwelve", "Shib");
			getOrCreateTestIdentityWithAuth("authThirteen", BaseSecurityModule.getDefaultAuthProviderIdentifier());
			getOrCreateTestIdentityWithAuth("authForteen", null);
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, authProvidersAll, null, null, null, null, null);
			assertTrue(results.size() - prevProviderNoneCount == 3);
			
			
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProvidersAll, null, null);
			prevProviderNoneCount = results.size();
//add a new identity per entry of AuthProvidersAll
			getOrCreateTestIdentityWithAuth("authSixteen", "Shib");
			getOrCreateTestIdentityWithAuth("authSeventeen", BaseSecurityModule.getDefaultAuthProviderIdentifier());
			getOrCreateTestIdentityWithAuth("authEighteen", null);
			DBFactory.getInstance().closeSession();
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, authProvidersAll, null, null);
			assertTrue(results.size() - prevProviderNoneCount == 3);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		System.out.println("stop testGetIdentitiesByPowerSearchAuthProvider");

		
		
	}
	
	
	
	// Hint : Properties for testing with HSQL must be lowercaseHSQL DB does not 
	//        mysql 'like' found results with upper and lowercase
	//        HSQL  'like' found only results with lowercase
	//        Our implementation of powersearch convert search-properties to lowercase ! 
	@Test public void testGetIdentitiesByPowerSearchWithUserPropertiesAndIntersectionOption() {
		// create two test users

		System.out.println("start testGetIdentitiesByPowerSearchWithUserPropertiesAndIntersectionOption");
		try {
			User onePropUser = UserManager.getInstance().createUser("onepropuser", "onepropuser", "onepropuser@lustig.com");
			onePropUser.setProperty(UserConstants.FIRSTNAME, "one");		
			Identity onePropeIdentity = baseSecurityManager.createAndPersistIdentityAndUser("onePropUser", onePropUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), "onepropuser", "ppp");
			Assert.assertNotNull(onePropeIdentity);
			
			User twoPropUser = UserManager.getInstance().createUser("twopropuser", "twopropuser", "twopropuser@lustig.com");
			twoPropUser.setProperty(UserConstants.FIRSTNAME, "two");
			twoPropUser.setProperty(UserConstants.LASTNAME, "prop");
			Identity twoPropeIdentity = baseSecurityManager.createAndPersistIdentityAndUser("twopropuser", twoPropUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), "twopropuser", "ppp");
			Assert.assertNotNull(twoPropeIdentity);
			// commit
			DBFactory.getInstance().closeSession();

			HashMap<String, String> userProperties;
			
			// find first
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "one");
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);

			// no intersection - all properties optional
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "one");
			userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);

			// no intersection - all properties optional
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "one");
			userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);

			// find second
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "two");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "two");
			userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 0);

			// no intersection - all properties optional
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "two");
			userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "two");
			userProperties.put(UserConstants.LASTNAME, "prop");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);
			
			// find all
			// 1. basic query to find all system users without restrictions
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size()>0); 
			int numberOfAllUsers = results.size();
			
			userProperties = new HashMap<String, String>();
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertEquals("Wrong search result 'empty userProperties'", numberOfAllUsers, results.size());
			
			userProperties = new HashMap<String, String>();
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
			assertEquals("Wrong search result 'empty userProperties and intersection=false'", numberOfAllUsers, results.size());

			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		System.out.println("stop testGetIdentitiesByPowerSearchWithUserPropertiesAndIntersectionOption");

	}

	@Test public void testMultipleUserPropertiesSearches() {
		System.out.println("start testMultipleUserPropertiesSearches");
		// create two test users
		try {
			User multiPropUser = UserManager.getInstance().createUser("multipropuser", "multipropuser", "multipropuser@lustig.com");
			multiPropUser.setProperty(UserConstants.FIRSTNAME, "multi");		
			multiPropUser.setProperty(UserConstants.LASTNAME, "prop");		
			multiPropUser.setProperty(UserConstants.INSTITUTIONALNAME, "multiinst");		
			multiPropUser.setProperty(UserConstants.INSTITUTIONALEMAIL, "multiinst@lustig.com");		
			multiPropUser.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "multiinst");		
			multiPropUser.setProperty(UserConstants.CITY, "züri");		
			Identity onePropeIdentity = baseSecurityManager.createAndPersistIdentityAndUser("multiPropUser", multiPropUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), "multipropuser", "ppp");
			Assert.assertNotNull(onePropeIdentity);
			
			// commit
			DBFactory.getInstance().closeSession();

			HashMap<String, String> userProperties;

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			sysoutResults(results);
			assertTrue(results.size() == 1);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			userProperties.put(UserConstants.LASTNAME, "prop");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1);

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			userProperties.put(UserConstants.LASTNAME, "prop");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 2); // multipropuser and twopropuser

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			userProperties.put(UserConstants.LASTNAME, "prop");
			userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1); 

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			userProperties.put(UserConstants.LASTNAME, "prop");
			userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
			userProperties.put(UserConstants.INSTITUTIONALEMAIL, "multiinst@lustig.com");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1); 

			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi");
			userProperties.put(UserConstants.LASTNAME, "prop");
			userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
			userProperties.put(UserConstants.INSTITUTIONALEMAIL, "multiinst@lustig.com");
			userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "multiinst");
			userProperties.put(UserConstants.CITY, "züri");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 1); 

			// Test to reproduce for OLAT-2820:
			// multipropuser matches firstname, lastname and city
			// twopropuser matches lastname and has a firstname but not city
			// Since it intersection flag is set to false it must find both users!
			userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, "multi"); 
			userProperties.put(UserConstants.LASTNAME, "prop");
// TODO: fg,cg: Did not found results with empty user-properties	
//		 twopro has same lastname but different firstname => will be found with userPropertiesInterseactionSearch = true
//		 twopro has no other user-properties, with current solution no OR-search runs correct with empty user-properties like twopro user has.

//		userProperties.put(UserConstants.INSTITUTIONALNAME, "MultiInst");
//		userProperties.put(UserConstants.INSTITUTIONALEMAIL, "MultiInst@lustig.com");
//		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "MultiInst");
//		userProperties.put(UserConstants.CITY, "Züri");
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null, null);
			assertTrue(results.size() == 2); // multipropuser and twopropuser
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}

		System.out.println("stop testMultipleUserPropertiesSearches");
	}
	
	@Test public void testGetIdentitiesByPowerSearchWithDate() {
		System.out.println("start testGetIdentitiesByPowerSearchWithDate");
		try {
			Identity ident = getOrCreateIdentity("anIdentity");
			Date created = ident.getCreationDate();
			Calendar cal = Calendar.getInstance();
			cal.setTime(created);
			cal.add(Calendar.DAY_OF_MONTH, -5);
			Date before = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 10);
			Date after = cal.getTime();

			// basic query to find all system users without restrictions
			List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
			assertTrue(results.size()>0); 
			int numberOfAllUsers = results.size();

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_DELETED);
			assertTrue(results.size() >0);
			int numberOfDeletedUsers = results.size();

			Date createdAfter = before;
			Date createdBefore = after;
			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, createdAfter, createdBefore, null, null, null);
			assertEquals("Search with date (createdAfter,createdBefore) delivers not the same number of users", numberOfAllUsers, results.size());

			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, before, after);
			assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, after, null, null, null);
			assertEquals("Search with date (only after) delivers not the same number of users", numberOfAllUsers, results.size());
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, null, after);
			assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, before, null, null, null, null);
			assertEquals("Search with date (only before) delivers not the same number of users", numberOfAllUsers, results.size());
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, before, null);
			assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, after, before, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, after, before);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, after, null, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, after, null);
			assertTrue(results.size() == 0);

			results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, before, null, null, null);
			assertTrue(results.size() == 0);
			results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, null, before);
			assertTrue(results.size() == 0);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error err) {
			err.printStackTrace();
		}
		

		System.out.println("end testGetIdentitiesByPowerSearchWithDate");
	}

	////////////////////
	// Helper
	///////////////////
	private Identity getOrCreateIdentity(String name) {
		Identity ident = baseSecurityManager.findIdentityByName(name);
		if (ident != null) {
			return ident;
		} else {
			return JunitTestHelper.createAndPersistIdentityAsUser(name);
		}
	}
	
	private Identity getOrCreateTestIdentity(String name) {
		return getOrCreateTestIdentityWithAuth(name,BaseSecurityModule.getDefaultAuthProviderIdentifier());
	}
	
	private Identity getOrCreateTestIdentityWithAuth(String name, String authProvider){
		Identity ident = baseSecurityManager.findIdentityByName(name);
		if (ident != null) {
			return ident;
		} else {
			User user = UserManager.getInstance().createUser(name+"_Firstname", name + "_Lastname", name + "@lustig.com");
			user.setProperty(UserConstants.INSTITUTIONALNAME, "unizh2");
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "12-345-678-908");
			ident = baseSecurityManager.createAndPersistIdentityAndUser(name, user, authProvider, name, "ppp");
			return ident;
		}
	}

	private PermissionOnResourceable[] convertPoliciesListToPermissionOnResourceArray(List<Policy> policies) {
		PermissionOnResourceable[] array = new PermissionOnResourceable[policies.size()];
		for (int i = 0; i < policies.size()	; i++) {
			Policy policy = policies.get(i);
			PermissionOnResourceable por = new PermissionOnResourceable(policy.getPermission(), policy.getOlatResource());
			array[i] = por;			
		}
		
		return array;
	}

	/*
	 * Only for debugging to see identities result list.
	 */
	private void sysoutResults(List<Identity> results) {
		log.info("TEST results.size()=" + results.size());
		for (Identity identity:results) {
			log.debug("TEST ident=" + identity);
		}		
	}

	// check Helper Methoden
	////////////////////////
	private void checkIdentitiesHasPermissions(List<Identity> results, PermissionOnResourceable[] adminPermissions) {
		for (Identity resultIdentity: results) {
			for (int i = 0; i < adminPermissions.length; i++) {
				assertTrue( baseSecurityManager.isIdentityPermittedOnResourceable(resultIdentity, adminPermissions[i].getPermission(), adminPermissions[i].getOlatResourceable() ) );
			}
		}
	}
	
	private void checkIdentitiesHasAuthProvider(List<Identity> results, String[] authProviders) {
		for (Identity resultIdentity : results) {
			boolean foundIdentityWithAuth = false;
			for (int i = 0; i < authProviders.length; i++) {
				Authentication authentication = baseSecurityManager.findAuthentication(resultIdentity, authProviders[i]);
				if (authentication != null) {
					foundIdentityWithAuth = true;
				}
 			}
			assertTrue("Coud not found any authentication for identity=" + resultIdentity, foundIdentityWithAuth);
		}
	}

	private void checkIdentitiesAreInGroups(List<Identity> results, SecurityGroup[] groups1) {
		for (Identity resultIdentity:results) {
			boolean foundIdentityInSecGroup = false;
			for (int i = 0; i < groups1.length; i++) {
				if (baseSecurityManager.isIdentityInSecurityGroup(resultIdentity, groups1[i]) ) {
					foundIdentityInSecGroup = true;
				}
			}
			assertTrue("Coud not found identity=" + resultIdentity, foundIdentityInSecGroup);
		}
	}
	
	private void checkIdentitiesHasRoles(List<Identity> results, boolean checkIsAuthor) {
		for (Identity resultIdentity: results) {
			Roles roles = baseSecurityManager.getRoles(resultIdentity);
			if (checkIsAuthor) {
				assertTrue("Identity has not roles author, identity=" + resultIdentity, roles.isAuthor());
			}
		}
	}
}