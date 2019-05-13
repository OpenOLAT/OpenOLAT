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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.apache.logging.log4j.Logger;
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
public class GetIdentitiesByPowerSearchTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(GetIdentitiesByPowerSearchTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private BaseSecurity baseSecurityManager;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void testGetIdentitiesByPowerSearch() {
		String suffix = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity-" + suffix);
		Identity uniIdent = getOrCreateTestIdentity("extremegroovy-" + suffix);
		Assert.assertNotNull(uniIdent);
		Identity deletedIdent = getOrCreateTestIdentity("delete-" + suffix);
		deletedIdent = baseSecurityManager.saveIdentityStatus(deletedIdent, Identity.STATUS_DELETED, null);

		organisationService.addMember(deletedIdent, OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		// basic query to find all system users without restrictions
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null);
		assertTrue(results.size()>0); 
		int numberOfAllUsers = results.size();
		
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null,Identity.STATUS_ACTIV);
		assertTrue(results.size()>0);
		int numberOfActiveUsers = results.size();
		
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_DELETED);
		assertTrue(results.size() >0);
		int numberOfDeletedUsers = results.size();
		assertEquals("Number of all users != activeUsers + deletedUsers" , numberOfAllUsers, numberOfActiveUsers + numberOfDeletedUsers);
		
		// user attributes search test
		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getIdentitiesByPowerSearch(ident.getName(), null, true, null, null, null, null, null, null, null);
		assertTrue(results.size() == 1);
		assertEquals("Wrong search result (search with username)" + ident.getName() + "' ",ident.getName() , results.get(0).getName());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(ident.getName(), null, true, null, null, null, null);
		assertTrue(results.size() == 1);
		assertEquals("Wrong search result (search with username)" + ident.getName() + "' ",ident.getName() , results.get(0).getName());
		
		results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity-" + suffix, null, true, null, null, null, null, null, null, null);
		assertTrue(results.size() == 1);
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity-" + suffix, null, true, null, null, null, null);
		assertTrue(results.size() == 1);

		results = baseSecurityManager.getIdentitiesByPowerSearch("lalal", null, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("lalal", null, true, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testGetIdentitiesByPowerSearch_institution() {
		String suffix = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity-" + suffix);
		Identity uniIdent = getOrCreateTestIdentity("extremegroovy-" + suffix);
		Assert.assertNotNull(uniIdent);
		Identity deletedIdent = getOrCreateTestIdentity("delete-" + suffix);
		deletedIdent = baseSecurityManager.saveIdentityStatus(deletedIdent, Identity.STATUS_DELETED, null);

		organisationService.addMember(deletedIdent, OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		//search institutional name with *zh2
		Map<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "*zh2");
		List<Identity> zh2Results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse("Wrong search result 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2Results.contains(ident));
		Assert.assertTrue("Wrong search result 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2Results.contains(uniIdent));
		Assert.assertTrue("Wrong search result 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2Results.contains(deletedIdent));
		List<Identity> zh2VisibleResults = baseSecurityManager
				.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertFalse("Wrong search result for visible 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2VisibleResults.contains(ident));
		Assert.assertTrue("Wrong search result for visible 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2VisibleResults.contains(uniIdent));
		Assert.assertFalse("Wrong search result for visible 'UserConstants.INSTITUTIONALNAME='*zh2'", zh2VisibleResults.contains(deletedIdent));
		
		//search institutional not found (identifier)
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "678"); // per default the % is only attached at the end of the query. 
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertTrue(results.isEmpty());

		//search institutional name and user identifier
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "%678");
		List<Identity> results_678 = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results_678.contains(ident));
		Assert.assertTrue(results_678.contains(uniIdent));
		Assert.assertTrue(results_678.contains(deletedIdent));
		
		//search visible institutional name and user identifier
		List<Identity> visible_678_results = baseSecurityManager
				.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertFalse(visible_678_results.contains(ident));
		Assert.assertTrue(visible_678_results.contains(uniIdent));
		Assert.assertFalse(visible_678_results.contains(deletedIdent));

		//search institutional name and user identifier 12-345-678*
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "12-345-678");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.contains(ident));
		Assert.assertTrue(results.contains(uniIdent));
		Assert.assertTrue(results.contains(deletedIdent));
		
		//search visible institutional name and user identifier 12-345-678*
		results = baseSecurityManager
				.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertFalse(results.contains(ident));
		Assert.assertTrue(results.contains(uniIdent));
		Assert.assertFalse(results.contains(deletedIdent));

		//search institutional name and user identifier 888 (nothing to find)
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "888");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertTrue(results.isEmpty());

		//search institutional name
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.INSTITUTIONALNAME, "un");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.contains(ident));
		Assert.assertTrue(results.contains(uniIdent));
		Assert.assertTrue(results.contains(deletedIdent));
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null);
		Assert.assertFalse(results.contains(ident));
		Assert.assertTrue(results.contains(uniIdent));
		Assert.assertFalse(results.contains(deletedIdent));
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, Identity.STATUS_ACTIV);
		Assert.assertFalse(results.contains(ident));
		Assert.assertTrue(results.contains(uniIdent));
		Assert.assertFalse(results.contains(deletedIdent));

		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testGetIdentitiesByPowerSearch_groups() {
		String suffix = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity-" + suffix);
		Identity ident2 = getOrCreateTestIdentity("extremegroovy-" + suffix);

		// add some stats
		ident = baseSecurityManager.saveIdentityStatus(ident, Identity.STATUS_ACTIV, null);
		ident2 = baseSecurityManager.saveIdentityStatus(ident2, Identity.STATUS_ACTIV, null);
		organisationService.addMember(ident, OrganisationRoles.administrator);
		organisationService.addMember(ident, OrganisationRoles.author);
		organisationService.addMember(ident2, OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		// security group search test
		OrganisationRoles[] groups1 = { OrganisationRoles.administrator };
		OrganisationRoles[] groups2 = { OrganisationRoles.administrator, OrganisationRoles.author};
		OrganisationRoles[] groups3 = { OrganisationRoles.author };

		// basic query to find all system users without restrictions
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty()); 

		List<Identity> deletedIdentities = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_DELETED);
		for(Identity deletedIdentity:deletedIdentities) {
			Assert.assertEquals(Identity.STATUS_DELETED, deletedIdentity.getStatus());
		}

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, null, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, null, null, null);
		Assert.assertFalse(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups2, null, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups2, null, null, null);
		Assert.assertFalse(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups3, null, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups3, null, null, null);
		Assert.assertFalse(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups2, null, null, null, null, null, null);
		Assert.assertEquals(1, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups2, null, null, null);
		Assert.assertEquals(1, results.size());

		results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups1, null, null, null, null, null, null);
		Assert.assertEquals(1, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups1, null, null, null);
		Assert.assertEquals(1, results.size());	
	}
	
	@Test
	public void testGetIdentitiesByPowerSearch_authProvider() {
		String suffix = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity-" + suffix);
		Identity ident2 = getOrCreateTestIdentity("extremegroovy-" + suffix);
		// add some stats
		ident = baseSecurityManager.saveIdentityStatus(ident, Identity.STATUS_ACTIV, null);
		ident2 = baseSecurityManager.saveIdentityStatus(ident2, Identity.STATUS_ACTIV, null);
		organisationService.addMember(ident, OrganisationRoles.administrator);
		organisationService.addMember(ident, OrganisationRoles.author);
		organisationService.addMember(ident2, OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		OrganisationRoles[] groups1 = { OrganisationRoles.administrator };
		OrganisationRoles[] groups2 = { OrganisationRoles.administrator, OrganisationRoles.author };

		// authentication provider search
		String[] authProviders = {BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib"};
		String[] authProvidersInvalid = { "nonexist" };// max length 8 !		
		String[] authProvidersAll = { BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib", null };
		
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProviders, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProviders, null, null);
		Assert.assertFalse(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProvidersInvalid, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProvidersInvalid, null, null);
		Assert.assertTrue(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups2, authProviders, null, null, null, null, null);
		Assert.assertEquals(1, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity-" + suffix, null, true, groups2, authProviders, null, null);
		Assert.assertEquals(1, results.size());
		
		results = baseSecurityManager.getIdentitiesByPowerSearch("an*tity", null, true, groups2, authProvidersInvalid, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("an*tity", null, true, groups2, authProvidersInvalid, null, null);
		Assert.assertTrue(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch("dontexist", null, true, groups2, authProviders, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("dontexist", null, true, groups2, authProviders, null, null);
		Assert.assertTrue(results.isEmpty());

		Authentication auth = baseSecurityManager.findAuthentication(ident, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		baseSecurityManager.deleteAuthentication(auth);
		dbInstance.commitAndCloseSession();

		// ultimate tests
		//Identity ident = getOrCreateIdentity("anIdentity");
		Date created = ident.getCreationDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(created);
		cal.add(Calendar.DAY_OF_MONTH, -5);
		Date before = cal.getTime();

		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getIdentitiesByPowerSearch("groovy", null, true, groups1, null, before, null, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("groovy", null, true, groups1, null, before, null);
		Assert.assertTrue(results.isEmpty());

		results = baseSecurityManager.getIdentitiesByPowerSearch("extremegroovy-" + suffix, null, true, groups1, null, before, null, null, null, null);
		Assert.assertEquals(1, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("extremegroovy-" + suffix, null, true, groups1, null, before, null);
		Assert.assertEquals(1, results.size());

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, groups1, authProviders, before, null, null, null, null);
		Assert.assertFalse("Found no results", results.isEmpty());
		checkIdentitiesAreInGroups(results, groups1);
		checkIdentitiesHasAuthProvider(results,authProviders );
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, groups1, authProviders, before, null);
		Assert.assertFalse("Found no results", results.isEmpty());
		checkIdentitiesAreInGroups(results, groups1);
		checkIdentitiesHasAuthProvider(results,authProviders );

		results = baseSecurityManager.getIdentitiesByPowerSearch("%y", null, true, groups1, authProvidersAll, before, null, null, null, null);
		Assert.assertTrue(results.contains(ident));
		Assert.assertTrue(results.contains(ident2));
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("%y", null, true, groups1, authProvidersAll, before, null);
		Assert.assertTrue(results.contains(ident));
		Assert.assertTrue(results.contains(ident2));

		results = baseSecurityManager.getIdentitiesByPowerSearch("%y", null, true, groups1, authProvidersAll, null, before, null, null, null);
		Assert.assertTrue(results.isEmpty());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch("%y", null, true, groups1, authProvidersAll, null, before);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testGetIdentitiesByPowerSearch_authProviders() {
		//authentication provider search		
		String[] authProviderNone = { null };
		String[] authProvidersAll = { BaseSecurityModule.getDefaultAuthProviderIdentifier(), "Shib", null };
		
		//check count before adding
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null, null, null, null);
		int prevProviderNoneCount = results.size();
		
		long countResults = baseSecurityManager.countIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null, null, null, null);
		Assert.assertEquals(results.size(), countResults);
		
		//add two new users with authProviderNone
		String rnd = UUID.randomUUID().toString();
		Identity authNoneOne = getOrCreateTestIdentityWithAuth("authNoneOne-" + rnd, null);
		Identity authNoneTwo = getOrCreateTestIdentityWithAuth("authNoneTwo-" + rnd, null);
		dbInstance.commitAndCloseSession();
		
		// special case: no auth provider
		// test if 2 new users are found.
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null, null, null, null);
		Assert.assertTrue(results.contains(authNoneOne));
		Assert.assertTrue(results.contains(authNoneTwo));
		Assert.assertEquals(prevProviderNoneCount + 2, results.size());
		
		//same but check visible
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null);
		prevProviderNoneCount = results.size();
		
		Identity authNoneThree = getOrCreateTestIdentityWithAuth("authNoneThree-" + rnd, null);
		Identity authNoneFour = getOrCreateTestIdentityWithAuth("authNoneFour-" + rnd, null);
		dbInstance.commitAndCloseSession();
		
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null);
		Assert.assertTrue(results.contains(authNoneThree));
		Assert.assertTrue(results.contains(authNoneFour));
		Assert.assertEquals("Wrong number of visible identities, search with (authProviderNone)", prevProviderNoneCount + 2, results.size());

		//
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null, null, null, null);
		prevProviderNoneCount = results.size();
		
		getOrCreateTestIdentityWithAuth("authNoneFive-" + rnd, null);
		getOrCreateTestIdentityWithAuth("authNoneSix-" + rnd, null);
		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null, null, null, null);
		Assert.assertEquals("Wrong number of identities, search with (authProviderNone)", prevProviderNoneCount + 2, results.size());
		
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null);
		prevProviderNoneCount = results.size();
		getOrCreateTestIdentityWithAuth("authNoneSeven-" + rnd, null);
		getOrCreateTestIdentityWithAuth("authNoneEight-" + rnd, null);
		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProviderNone, null, null);
		Assert.assertEquals("Wrong number of visible identities, search with (authProviderNone)", prevProviderNoneCount + 2, results.size());
		
		
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProvidersAll, null, null, null, null, null);
		prevProviderNoneCount = results.size();
		//add a new identity per entry of AuthProvidersAll
		getOrCreateTestIdentityWithAuth("authTwelve-" + rnd, "Shib");
		getOrCreateTestIdentityWithAuth("authThirteen-" + rnd, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		getOrCreateTestIdentityWithAuth("authForteen-" + rnd, null);
		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, authProvidersAll, null, null, null, null, null);
		Assert.assertTrue(results.size() - prevProviderNoneCount == 3);
		
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProvidersAll, null, null);
		prevProviderNoneCount = results.size();
		//add a new identity per entry of AuthProvidersAll
		getOrCreateTestIdentityWithAuth("authSixteen-" + rnd, "Shib");
		getOrCreateTestIdentityWithAuth("authSeventeen-" + rnd, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		getOrCreateTestIdentityWithAuth("authEighteen-" + rnd, null);
		dbInstance.commitAndCloseSession();
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, authProvidersAll, null, null);
		Assert.assertTrue(results.size() - prevProviderNoneCount == 3);
	}
	
	// Hint : Properties for testing with HSQL must be lowercaseHSQL DB does not 
	//     mysql 'like' found results with upper and lowercase
	//     HSQL  'like' found only results with lowercase
	//     Our implementation of powersearch convert search-properties to lowercase ! 
	@Test
	public void testGetIdentitiesByPowerSearch_userPropertiesAndIntersectionOption() {
		// create two test users
		String one = "one" + UUID.randomUUID().toString().replace("-", "");
		String oneUsername = "onePropUser-" + UUID.randomUUID();

		User onePropUser = UserManager.getInstance().createUser("onepropuser", "onepropuser", one + "@lustig.com");
		onePropUser.setProperty(UserConstants.FIRSTNAME, "one");		
		Identity onePropIdentity = baseSecurityManager.createAndPersistIdentityAndUser(oneUsername, null, onePropUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), oneUsername, "ppp");
		Assert.assertNotNull(onePropIdentity);
		
		String two = "two" + UUID.randomUUID().toString().replace("-", "");
		String twoUsername = "twoPropUser-" + UUID.randomUUID();

		User twoPropUser = UserManager.getInstance().createUser("twopropuser", "twopropuser", two + "@lustig.com");
		twoPropUser.setProperty(UserConstants.FIRSTNAME, "two");
		twoPropUser.setProperty(UserConstants.LASTNAME, "prop");
		Identity twoPropIdentity = baseSecurityManager.createAndPersistIdentityAndUser(twoUsername, null, twoPropUser, BaseSecurityModule.getDefaultAuthProviderIdentifier(), twoUsername, "ppp");
		Assert.assertNotNull(twoPropIdentity);
		dbInstance.commitAndCloseSession();

		HashMap<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "one");
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.contains(onePropIdentity));

		// no intersection - all properties optional
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "one");
		userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());

		// no intersection - all properties optional
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "one");
		userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null);
		Assert.assertTrue(results.contains(onePropIdentity));
		Assert.assertFalse(results.contains(twoPropIdentity));

		// find second
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "two");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.contains(onePropIdentity));
		Assert.assertTrue(results.contains(twoPropIdentity));
		
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "two");
		userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.isEmpty());

		// no intersection - all properties optional
		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "two");
		userProperties.put(UserConstants.LASTNAME, "somewrongvalue");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null);
		Assert.assertFalse(results.contains(onePropIdentity));
		Assert.assertTrue(results.contains(twoPropIdentity));

		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "two");
		userProperties.put(UserConstants.LASTNAME, "prop");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.contains(onePropIdentity));
		Assert.assertTrue(results.contains(twoPropIdentity));
		
		// find all
		// 1. basic query to find all system users without restrictions
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null);
		Assert.assertFalse(results.isEmpty()); 
		int numberOfAllUsers = results.size();
		
		userProperties = new HashMap<String, String>();
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals("Wrong search result 'empty userProperties'", numberOfAllUsers, results.size());
		
		userProperties = new HashMap<String, String>();
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, false, null, null, null, null, null, null, null);
		Assert.assertEquals("Wrong search result 'empty userProperties and intersection=false'", numberOfAllUsers, results.size());
	}

	@Test
	public void testGetIdentitiesByPowerSearch_multipleUserProperties() {
		String multi = "multi" + UUID.randomUUID().toString().replace("-", "");
		String multiInst = "multiinst" + UUID.randomUUID().toString().replace("-", "");
		String multiUsername = "multiPropUser-" + UUID.randomUUID();

		User user = UserManager.getInstance().createUser("multipropuser", "multipropuser", multi + "@lustig.com");
		user.setProperty(UserConstants.FIRSTNAME, "multi");		
		user.setProperty(UserConstants.LASTNAME, "prop");		
		user.setProperty(UserConstants.INSTITUTIONALNAME, "multiinst");		
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, multiInst + "@lustig.com");		
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, multiInst);		
		user.setProperty(UserConstants.CITY, "züri");		
		Identity identity = baseSecurityManager.createAndPersistIdentityAndUser(multiUsername, null, user, BaseSecurityModule.getDefaultAuthProviderIdentifier(), multiUsername, "ppp");
		Assert.assertNotNull(identity);
		
		// commit
		dbInstance.commitAndCloseSession();

		HashMap<String, String> userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "multi");
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		sysoutResults(results);
		Assert.assertTrue(results.contains(identity));
		
		long countResults = baseSecurityManager.countIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(results.size(), countResults);

		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "multi");
		userProperties.put(UserConstants.LASTNAME, "prop");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.contains(identity));

		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "multi");
		userProperties.put(UserConstants.LASTNAME, "prop");
		userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertTrue(results.contains(identity));  

		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "multi");
		userProperties.put(UserConstants.LASTNAME, "prop");
		userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
		userProperties.put(UserConstants.INSTITUTIONALEMAIL, multiInst + "@lustig.com");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains(identity));

		userProperties = new HashMap<String, String>();
		userProperties.put(UserConstants.FIRSTNAME, "multi");
		userProperties.put(UserConstants.LASTNAME, "prop");
		userProperties.put(UserConstants.INSTITUTIONALNAME, "multiinst");
		userProperties.put(UserConstants.INSTITUTIONALEMAIL, multiInst + "@lustig.com");
		userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, multiInst);
		userProperties.put(UserConstants.CITY, "züri");
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains(identity));
	}
	
	@Test
	public void testGetIdentitiesByPowerSearch_withDate() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity-" + UUID.randomUUID());
		Date created = ident.getCreationDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(created);
		cal.add(Calendar.DAY_OF_MONTH, -5);
		Date before = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 10);
		Date after = cal.getTime();

		// basic query to find all system users without restrictions
		List<Identity> results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null);
		assertTrue(results.size()>0); 
		int numberOfAllUsers = results.size();

		List<Identity> deletedIdentities = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_DELETED);
		for(Identity deletedIdentity:deletedIdentities) {
			Assert.assertEquals(Identity.STATUS_DELETED, deletedIdentity.getStatus());
		}
		int numberOfDeletedUsers = deletedIdentities.size();

		Date createdAfter = before;
		Date createdBefore = after;
		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, createdAfter, createdBefore, null, null, null);
		assertEquals("Search with date (createdAfter,createdBefore) delivers not the same number of users", numberOfAllUsers, results.size());

		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, before, after);
		assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, after, null, null, null);
		assertEquals("Search with date (only after) delivers not the same number of users", numberOfAllUsers, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, after);
		assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, before, null, null, null, null);
		assertEquals("Search with date (only before) delivers not the same number of users", numberOfAllUsers, results.size());
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, before, null);
		assertEquals("Search (visible identities) with date (createdAfter,createdBefore) delivers not the same number of users", (numberOfAllUsers - numberOfDeletedUsers) , results.size()); // One identity is deleted

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, after, before, null, null, null);
		assertTrue(results.size() == 0);
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, after, before);
		assertTrue(results.size() == 0);

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, after, null, null, null, null);
		assertTrue(results.size() == 0);
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, after, null);
		assertTrue(results.size() == 0);

		results = baseSecurityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, before, null, null, null);
		assertTrue(results.size() == 0);
		results = baseSecurityManager.getVisibleIdentitiesByPowerSearch(null, null, true, null, null, null, before);
		assertTrue(results.size() == 0);
	}

	////////////////////
	// Helper
	///////////////////
	
	private Identity getOrCreateTestIdentity(String loginName) {
		return getOrCreateTestIdentityWithAuth(loginName, BaseSecurityModule.getDefaultAuthProviderIdentifier());
	}
	
	private Identity getOrCreateTestIdentityWithAuth(String loginName, String authProvider){
		Identity ident = baseSecurityManager.findIdentityByName(loginName);
		if (ident != null) {
			return ident;
		} else {
			User user = UserManager.getInstance().createUser(loginName+"_Firstname", loginName + "_Lastname", loginName + "@lustig.com");
			user.setProperty(UserConstants.INSTITUTIONALNAME, "unizh2");
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, "12-345-678-908");
			ident = baseSecurityManager.createAndPersistIdentityAndUser(loginName, null, user, authProvider, loginName, "ppp");
			return ident;
		}
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

	private void checkIdentitiesAreInGroups(List<Identity> results, OrganisationRoles[] roles) {
		for (Identity resultIdentity:results) {
			boolean foundIdentityInSecGroup = false;
			for (int i = 0; i < roles.length; i++) {
				if (organisationDao.hasRole(resultIdentity, null, null, roles[i].name())) {
					foundIdentityInSecGroup = true;
				}
			}
			assertTrue("Coud not found identity=" + resultIdentity, foundIdentityInSecGroup);
		}
	}
}