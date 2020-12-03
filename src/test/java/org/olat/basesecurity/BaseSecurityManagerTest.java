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
package org.olat.basesecurity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.LoginModule;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the basic functions of the base security manager.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BaseSecurityManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	
	
	@Test
	public void createIdentity() {
		String name = "createid-" + UUID.randomUUID().toString();
		User user = userManager.createUser("first" + name, "last" + name, name + "@frentix.com");
		Identity identity = securityManager.createAndPersistIdentityAndUser(null, name, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), name, "secret");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(identity);
		Assert.assertNotNull(identity.getKey());
		Assert.assertNotNull(identity.getUser());
		Assert.assertNotNull(identity.getName());
		Assert.assertEquals(user, identity.getUser());
		Assert.assertEquals("u" + user.getKey(), identity.getName());
		Assert.assertEquals("first" + name, identity.getUser().getFirstName());
		Assert.assertEquals("last" + name, identity.getUser().getLastName());
		Assert.assertEquals("first" + name, identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
		Assert.assertEquals("last" + name, identity.getUser().getProperty(UserConstants.LASTNAME, null));
		Assert.assertEquals(name + "@frentix.com", identity.getUser().getProperty(UserConstants.EMAIL, null));
	}
	
	@Test
	public void createIdentityWithIdentityName() {
		String name = "createid-" + UUID.randomUUID().toString();
		User user = userManager.createUser("first" + name, "last" + name, name + "@openolat.com");
		Identity identity = securityManager.createAndPersistIdentityAndUser(name, name, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), name, "secret");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(identity);
		Assert.assertNotNull(identity.getKey());
		Assert.assertNotNull(identity.getUser());
		Assert.assertEquals(user, identity.getUser());
		Assert.assertEquals(name, identity.getName());
		Assert.assertEquals("first" + name, identity.getUser().getFirstName());
		Assert.assertEquals("last" + name, identity.getUser().getLastName());
		Assert.assertEquals("first" + name, identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
		Assert.assertEquals("last" + name, identity.getUser().getProperty(UserConstants.LASTNAME, null));
		Assert.assertEquals(name + "@openolat.com", identity.getUser().getProperty(UserConstants.EMAIL, null));
	}
	
	@Test
	public void createAndPersistIdentityAndUserWithOrganisationAndName() {
		String name = "createid-" + UUID.randomUUID().toString();
		User user = userManager.createUser("first" + name, "last" + name, name + "@openolat.com");
		Identity identity = securityManager.createAndPersistIdentityAndUserWithOrganisation(name, name, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), name, "secret", null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(identity);
		Assert.assertNotNull(identity.getKey());
		Assert.assertNotNull(identity.getUser());
		Assert.assertEquals(user, identity.getUser());
		Assert.assertEquals(name, identity.getName());
		
		List<Organisation> organisations = organisationService.getOrganisations(identity, OrganisationRoles.user);
		Assert.assertEquals(1, organisations.size());
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Assert.assertEquals(defaultOrganisation, organisations.get(0));
	}
	
	/**
	 * This test is primarily made against Oracle
	 */
	@Test
	public void createUpdateIdentity() {
		String authusername = "update-id-" + UUID.randomUUID().toString();
		String nickName = "nn" + authusername;
		User user = userManager.createUser("first" + authusername, "last" + authusername, authusername + "@frentix.com");
		user.setProperty(UserConstants.COUNTRY, "");
		user.setProperty(UserConstants.CITY, "Basel");
		Identity identity = securityManager.createAndPersistIdentityAndUser(null, nickName, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), authusername, "secret");
		dbInstance.commitAndCloseSession();
		
		//reload and update
		Identity identityPrime = securityManager.loadIdentityByKey(identity.getKey());
		identityPrime.getUser().setProperty(UserConstants.FIRSTNAME, "firstname");
		identityPrime.getUser().setProperty(UserConstants.COUNTRY, "CH");
		identityPrime.getUser().setProperty(UserConstants.CITY, "Lausanne");
		identityPrime.getUser().setProperty(UserConstants.NICKNAME, nickName);
		userManager.updateUserFromIdentity(identityPrime);
		dbInstance.commitAndCloseSession();
		
		//reload and check
		Identity identitySecond = securityManager.loadIdentityByKey(identity.getKey());
		dbInstance.commitAndCloseSession();//check the fetch join on user
		Assert.assertEquals("firstname", identitySecond.getUser().getProperty(UserConstants.FIRSTNAME, null));
		Assert.assertEquals("last" + authusername, identitySecond.getUser().getProperty(UserConstants.LASTNAME, null));
		Assert.assertEquals(authusername + "@frentix.com", identitySecond.getUser().getProperty(UserConstants.EMAIL, null));
		Assert.assertEquals("CH", identitySecond.getUser().getProperty(UserConstants.COUNTRY, null));
		Assert.assertEquals("Lausanne", identitySecond.getUser().getProperty(UserConstants.CITY, null));
	}
	
	
	@Test
	public void testEquals() {
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("eq-1-");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("eq-2-");
		
		assertFalse("Wrong equals implementation, different types are recognized as equals ",ident1.equals(Integer.valueOf(1)));
		assertFalse("Wrong equals implementation, different users are recognized as equals ",ident1.equals(ident2));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",ident1.equals(null));
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1));
		Identity ident1_2 = securityManager.loadIdentityByKey(ident1.getKey());
		assertTrue("Wrong equals implementation, same users are NOT recognized as equals ",ident1.equals(ident1_2));
	}
	
	@Test
	public void testHashCode() {
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("hash-1");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("hash-2");
		
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",ident1.hashCode() == ident1.hashCode());
		assertFalse("Wrong hashCode implementation, different users have same hash-code",ident1.hashCode() == ident2.hashCode());
		Identity ident1_2 = securityManager.loadIdentityByKey(ident1.getKey());
		assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ",ident1.hashCode() == ident1_2.hashCode());
	}

	@Test
	public void testFindIdentityByUser() {
		//create a user it
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("by-user");
		Assert.assertNotNull(id);
		Assert.assertNotNull(id.getUser());
		dbInstance.commitAndCloseSession();
		
		//find it
		Identity foundId = securityManager.findIdentityByUser(id.getUser());
		Assert.assertNotNull(foundId);
		Assert.assertEquals(id, foundId);
		Assert.assertEquals(id.getUser(), foundId.getUser());
	}
	
	@Test
	public void testFindIdentityByName() {
		//create a user it
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("find-name-3");
		Assert.assertNotNull(id);
		dbInstance.commitAndCloseSession();
		
		//find it
		Identity foundId = securityManager.findIdentityByName(id.getName());
		Assert.assertNotNull(foundId);
		Assert.assertEquals(id, foundId);
	}
	
	@Test
	public void findIdentityByNameCaseInsensitive() {
		//create a user it
		IdentityWithLogin idWithLogin = JunitTestHelper.createAndPersistRndUser("find-ME-2");
		Identity id = idWithLogin.getIdentity();
		String name = id.getName();
		dbInstance.commitAndCloseSession();
		
		//find it
		Identity foundId = securityManager.findIdentityByNameCaseInsensitive(name);
		Assert.assertNotNull(foundId);
		Assert.assertEquals(name, foundId.getName());
		Assert.assertEquals(id, foundId);
		
		//find it lower case
		Identity foundLoweredId = securityManager.findIdentityByNameCaseInsensitive(name.toLowerCase());
		Assert.assertNotNull(foundLoweredId);
		Assert.assertEquals(name, foundLoweredId.getName());
		Assert.assertEquals(id, foundLoweredId);
		
		//find it upper case
		Identity foundUpperedId = securityManager.findIdentityByNameCaseInsensitive(name.toUpperCase());
		Assert.assertNotNull(foundUpperedId);
		Assert.assertEquals(name, foundUpperedId.getName());
		Assert.assertEquals(id, foundUpperedId);
	}
	
	@Test
	public void loadIdentityShortByKey() {
		//create a user it
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("find-me-short-1-");
		dbInstance.commitAndCloseSession();
		
		//find it
		IdentityShort foundId = securityManager.loadIdentityShortByKey(id.getKey());
		Assert.assertNotNull(foundId);
		Assert.assertEquals(id.getKey(), foundId.getKey());
		Assert.assertEquals(id.getName(), foundId.getName());
		Assert.assertEquals(id.getUser().getEmail(), foundId.getEmail());
		Assert.assertEquals(id.getUser().getFirstName(), foundId.getFirstName());
		Assert.assertEquals(id.getUser().getLastName(), foundId.getLastName());
		Assert.assertNull(foundId.getLastLogin());// no login, no last login date
		Assert.assertEquals(id.getUser().getKey(), foundId.getUserKey());
		Assert.assertTrue(foundId.getStatus() < Identity.STATUS_VISIBLE_LIMIT);
	}
	
	@Test
	public void loadIdentityShortByKeys() {
		//create a user it
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("find-me-short-2-");
		dbInstance.commitAndCloseSession();
		
		//find it
		List<IdentityShort> foundIdList = securityManager.loadIdentityShortByKeys(List.of(id.getKey()));
		Assert.assertNotNull(foundIdList);
		Assert.assertEquals(1, foundIdList.size());
		Assert.assertEquals(id.getKey(), foundIdList.get(0).getKey());
	}
	
	@Test
	public void loadIdentityShortByKeysLarge() {
		//create a user it
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("find-me-short-3-");
		dbInstance.commitAndCloseSession();
		
		List<Long> lofOfkeys = new ArrayList<>(64004);
		for(int i=1; i<64000; i++) {
			lofOfkeys.add(Long.valueOf(i));
		}
		lofOfkeys.add(id.getKey());

		//find it
		List<IdentityShort> foundIdList = securityManager.loadIdentityShortByKeys(List.of(id.getKey()));
		Assert.assertNotNull(foundIdList);
		assertThat(foundIdList)
			.extracting(idShort -> idShort.getKey())
			.contains(id.getKey());
	}

	@Test
	public void loadIdentityByKeys() {
		//create a security group with 2 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("load-1-sec-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("load-2-sec-");
		dbInstance.commitAndCloseSession();
		
		List<Long> keys = new ArrayList<>(2);
		keys.add(id1.getKey());
		keys.add(id2.getKey());
		List<Identity> identities = securityManager.loadIdentityByKeys(keys);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}
	
	@Test
	public void loadIdentityByKeysLarge() {
		//create a security group with 2 identities
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("load-8-sec-");
		dbInstance.commitAndCloseSession();
		
		List<Long> lofOfkeys = new ArrayList<>(64004);
		for(int i=1; i<64000; i++) {
			lofOfkeys.add(Long.valueOf(i + 128000));
		}
		lofOfkeys.add(id.getKey());
		
		List<Identity> identities = securityManager.loadIdentityByKeys(lofOfkeys);
		Assert.assertNotNull(identities);
		Assert.assertTrue(identities.contains(id));
	}
	
	@Test
	public void loadIdentityByKey() {
		//create a security group with 2 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("load-1-sec-");
		dbInstance.commitAndCloseSession();

		Identity identity = securityManager.loadIdentityByKey(id1.getKey());
		Assert.assertNotNull(identity);
		Assert.assertEquals(id1, identity);
	}
	
	@Test
	public void searchIdentityShort() {
		//create a security group with 2 identities
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("short-1-search-");
		dbInstance.commitAndCloseSession();

		String login = id.getLogin().substring(0, 12);
		List<IdentityShort> identities = securityManager.searchIdentityShort(login, 32000);
		assertThat(identities)
			.isNotNull()
			.extracting(identity -> identity.getKey())
			.contains(id.getKey());
	}
	
	@Test
	public void searchIdentityShort_multiWords() {
		//create a security group with 2 identities
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("short-2-search-");
		dbInstance.commitAndCloseSession();

		String login = id.getLogin().substring(0, 12);
		List<IdentityShort> identities = securityManager.searchIdentityShort(login + " hello world", 32000);
		assertThat(identities)
			.isNotNull()
			.extracting(identity -> identity.getKey())
			.contains(id.getKey());
	}
	
	/**
	 * The test checks only if the query is valid.
	 * 
	 */
	@Test
	public void searchIdentityShortLongAllParameters() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("short-2-search-");
		dbInstance.commitAndCloseSession();

		List<Organisation> organisations = organisationService.getOrganisations();

		String login = id.getLogin().substring(0, 12);
		List<IdentityShort> identities = securityManager.searchIdentityShort(login + " hello world",
				organisations, GroupRoles.participant,  32000);
		assertThat(identities)
			.isNotNull();
	}
	
	@Test
	public void searchIdentityShortParameters() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("short-2-search-");
		dbInstance.commitAndCloseSession();

		Organisation defOrganisation = organisationService.getDefaultOrganisation();

		String login = id.getLogin().substring(0, 12);
		List<IdentityShort> identities = securityManager.searchIdentityShort(login + " and    () <!>",
				Collections.singletonList(defOrganisation), null,  32000);
		assertThat(identities)
			.isNotNull()
			.extracting(identity -> identity.getKey())
			.contains(id.getKey());
	}
	
	@Test
	public void searchIdentityShortLongEmpty() {
		List<IdentityShort> identities = securityManager.searchIdentityShort(null, null, null,  32000);
		Assert.assertTrue(identities.isEmpty());

		identities = securityManager.searchIdentityShort("", null, null,  32000);
		Assert.assertTrue(identities.isEmpty());
		
		identities = securityManager.searchIdentityShort(" ", null, null,  32000);
		Assert.assertTrue(identities.isEmpty());
	}
	
	/**
	 * Update roles
	 */
	@Test
	public void testUpdateRoles_giveAllRights() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser( "roles-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser( "roles-2");
		Roles roles = securityManager.getRoles(id1);
		Assert.assertNotNull(roles);
		dbInstance.commitAndCloseSession();

		//update roles
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RolesByOrganisation modifiedRoles = RolesByOrganisation.roles(defOrganisation, false, false, false, true, true, true, true, true, true, true);
		securityManager.updateRoles(id2, id1, modifiedRoles);
		dbInstance.commitAndCloseSession();
		
		//check roles
		Roles reloadRoles = securityManager.getRoles(id1);
		Assert.assertNotNull(reloadRoles);
		Assert.assertTrue(reloadRoles.isAuthor());
		Assert.assertTrue(reloadRoles.isGroupManager());
		Assert.assertFalse(reloadRoles.isGuestOnly());
		Assert.assertTrue(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertTrue(reloadRoles.isAdministrator());
		Assert.assertTrue(reloadRoles.isPoolManager());
		Assert.assertTrue(reloadRoles.isCurriculumManager());
		Assert.assertTrue(reloadRoles.isUserManager());
	}
	
	/**
	 * Update roles
	 */
	@Test
	public void testUpdateRoles_someRights() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser( "roles-3");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser( "roles-4");
		Roles roles = securityManager.getRoles(id1);
		Assert.assertNotNull(roles);
		dbInstance.commitAndCloseSession();

		//update roles
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RolesByOrganisation modifiedRoles = RolesByOrganisation.roles(defOrganisation, false, false, false, true, false, false, true, true, false, false);
		securityManager.updateRoles(id2, id1, modifiedRoles);
		dbInstance.commitAndCloseSession();
		
		//check roles
		Roles reloadRoles = securityManager.getRoles(id1);
		Assert.assertNotNull(reloadRoles);
		Assert.assertTrue(reloadRoles.isAuthor());
		Assert.assertFalse(reloadRoles.isGroupManager());
		Assert.assertFalse(reloadRoles.isGuestOnly());
		Assert.assertFalse(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertFalse(reloadRoles.isAdministrator());
		Assert.assertFalse(reloadRoles.isPoolManager());
		Assert.assertTrue(reloadRoles.isCurriculumManager());
		Assert.assertTrue(reloadRoles.isUserManager());
	}
	
	/**
	 * Update roles, check that invitee don't become rights
	 */
	@Test
	public void testUpdateRoles_guest() {
		Identity invitee = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-1");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("invitee-2");
		Roles roles = securityManager.getRoles(invitee);
		Assert.assertNotNull(roles);
		dbInstance.commitAndCloseSession();

		//update roles
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RolesByOrganisation modifiedRoles = new RolesByOrganisation(defOrganisation, new OrganisationRoles[] { OrganisationRoles.guest, OrganisationRoles.administrator });
		securityManager.updateRoles(user, invitee, modifiedRoles);
		dbInstance.commitAndCloseSession();

		//check roles
		Roles reloadRoles = securityManager.getRoles(invitee);
		Assert.assertNotNull(reloadRoles);
		Assert.assertFalse(reloadRoles.isAuthor());
		Assert.assertFalse(reloadRoles.isGroupManager());
		Assert.assertTrue(reloadRoles.isGuestOnly());
		Assert.assertFalse(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertFalse(reloadRoles.isAdministrator());
		Assert.assertFalse(reloadRoles.isUserManager());
	}
	
	/**
	 * Test method @see org.olat.basesecurity.BaseSecurityManager.getIdentitiesByPowerSearch()
	 * with a list of identity keys as parameters.<br/>
	 * getIdentitiesByPowerSearch is a dynamic generated query and we need
	 * to test some aspects of it.
	 */
	@Test
	public void testGetIdentityByPowerSearch_IdentityKeys() {
		String login = "pow-1-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setIdentityKeys(Collections.singletonList(id.getKey()));
		
		List<Identity> ids = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(ids);
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals(id, ids.get(0));
	}
	
	/**
	 * Check the method @see getIdentitiesByPowerSearch
	 * with a login as parameter.<br/>
	 * getIdentitiesByPowerSearch is a dynamic generated query and we need
	 * to test some aspects of it.
	 */
	@Test
	public void testGetIdentityByPowerSearch_Login() {
		String login = "pow-2-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setLogin(login);
		
		List<Identity> ids = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(ids);
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals(id, ids.get(0));
	}
	
	/**
	 * Check the method @see getIdentitiesByPowerSearch
	 * with a user property as parameter.<br/>
	 * getIdentitiesByPowerSearch is a dynamic generated query and we need
	 * to test some aspects of it.
	 */
	@Test
	public void testGetIdentityByPowerSearch_UserProperty() {
		//create a user with a first name
		String login = "pow-3-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		String firstName = id.getUser().getProperty(UserConstants.FIRSTNAME, null);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		Map<String,String> props = new HashMap<>();
		props.put(UserConstants.FIRSTNAME, firstName);
		params.setUserProperties(props);
		
		List<Identity> ids = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(ids);
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals(id, ids.get(0));
	}
	
	/**
	 * Check the method @see getIdentitiesByPowerSearch
	 * with a login and a list of identity keys as parameters.<br/>
	 * getIdentitiesByPowerSearch is a dynamic generated query and we need
	 * to test some aspects of it.
	 */
	@Test
	public void testGetIdentityByPowerSearch_LoginIdentityKeys() {
		String login = "pow-4-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setLogin(login);
		params.setIdentityKeys(Collections.singletonList(id.getKey()));
		
		List<Identity> ids = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(ids);
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals(id, ids.get(0));
	}
	
	/**
	 * Test the method @see getIdentitiesByPowerSearch
	 * with 2 user properties and a list of identity keys as parameters.<br/>
	 * getIdentitiesByPowerSearch is a dynamic generated query and we need
	 * to test some aspects of it.
	 */
	@Test
	public void testGetIdentityByPowerSearch_LoginIdentityKeysProperty() {
		String login = "pow-5-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		dbInstance.commitAndCloseSession();
		
		SearchIdentityParams params = new SearchIdentityParams();
		params.setLogin(login);
		Map<String,String> props = new HashMap<>();
		props.put(UserConstants.FIRSTNAME, id.getUser().getProperty(UserConstants.FIRSTNAME, null));
		props.put(UserConstants.LASTNAME, id.getUser().getProperty(UserConstants.LASTNAME, null));
		params.setUserProperties(props);
		params.setIdentityKeys(Collections.singletonList(id.getKey()));
		
		List<Identity> ids = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(ids);
		Assert.assertEquals(1, ids.size());
		Assert.assertEquals(id, ids.get(0));
	}
	
	@Test
	public void testGetIdentityByPowerSearch_managed() {
		String login = "pow-6-" + UUID.randomUUID();
		String externalId = UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(login);
		dbInstance.commitAndCloseSession();
		securityManager.setExternalId(id, externalId);
		dbInstance.commitAndCloseSession();
		
		//search managed
		SearchIdentityParams params = new SearchIdentityParams();
		params.setManaged(Boolean.TRUE);
		List<Identity> managedIds = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(managedIds);
		Assert.assertFalse(managedIds.isEmpty());
		Assert.assertTrue(managedIds.contains(id));
		for(Identity managedId:managedIds) {
			Assert.assertNotNull(managedId.getExternalId());
		}
		
		//search not managed
		params.setManaged(Boolean.FALSE);
		List<Identity> naturalIds = securityManager.getIdentitiesByPowerSearch(params, 0, -1);
		Assert.assertNotNull(naturalIds);
		Assert.assertFalse(naturalIds.contains(id));
		for(Identity naturalId:naturalIds) {
			Assert.assertNull(naturalId.getExternalId());
		}
	}
	
	
	@Test
	public void testGetIdentitiesByPowerSearchWithGroups() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-1");
		dbInstance.commitAndCloseSession();
		
		//test positive result
		OrganisationRoles[] groups = { OrganisationRoles.user };
		List<Identity> userList = securityManager.getVisibleIdentitiesByPowerSearch(id.getLogin(), null, true, groups, null, null, null);
		Assert.assertNotNull(userList);
		Assert.assertEquals(1, userList.size());
		Assert.assertEquals(id.getIdentity(), userList.get(0));
	  
		//test negatif -> with author security group
		OrganisationRoles[] authors = { OrganisationRoles.author };
		List<Identity> authorList = securityManager.getVisibleIdentitiesByPowerSearch(id.getLogin(), null, true, authors, null, null, null);
		Assert.assertNotNull(authorList);
		Assert.assertTrue(authorList.isEmpty());
	}

	
	@Test
	public void findAuthenticationName() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-d-");
		dbInstance.commitAndCloseSession();
		
		Authentication auth = securityManager.findAuthentication(ident, "OLAT");
		Assert.assertNotNull(auth);
		
		String authName = securityManager.findAuthenticationName(ident, "OLAT");
		Assert.assertNotNull(authName);
	}
	
	@Test
	public void updateToSaltedAuthentication() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("auth-c-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		Authentication auth = securityManager.findAuthentication(ident, "OLAT");
		String credentials = auth.getCredential();
		Authentication updatedAuth = securityManager.updateCredentials(auth, "secret", loginModule.getDefaultHashAlgorithm());
		Assert.assertNotNull(auth);
		Assert.assertNotNull(updatedAuth);
		Assert.assertEquals(auth, updatedAuth);
		Assert.assertFalse(credentials.equals(updatedAuth.getCredential()));
		dbInstance.commitAndCloseSession();
		
		Authentication auth2 = securityManager.findAuthentication(ident, "OLAT");
		String credentials2 = auth2.getCredential();
		Authentication notUpdatedAuth = securityManager.updateCredentials(auth2, "secret", loginModule.getDefaultHashAlgorithm());
		Assert.assertNotNull(auth2);
		Assert.assertNotNull(notUpdatedAuth);
		Assert.assertSame(auth2, notUpdatedAuth);
		Assert.assertEquals(credentials2, notUpdatedAuth.getCredential());
		Assert.assertFalse(credentials.equals(notUpdatedAuth.getCredential()));
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteAuthentication() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("auth-del-" + UUID.randomUUID().toString());
		Authentication auth = securityManager.createAndPersistAuthentication(identity, "del-test", identity.getName(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		//reload and check
		Authentication reloadedAuth = securityManager.findAuthentication(identity, "del-test");
		Assert.assertNotNull(reloadedAuth);
		Assert.assertEquals(auth, reloadedAuth);
		dbInstance.commitAndCloseSession();
		
		//delete
		securityManager.deleteAuthentication(auth);
	}
	
	@Test
	public void deleteAuthentication_checkTransactionSurvive() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("auth-del-" + UUID.randomUUID().toString());
		Authentication auth = securityManager.createAndPersistAuthentication(identity, "del-test", identity.getName(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		//delete
		securityManager.deleteAuthentication(auth);
		dbInstance.commitAndCloseSession();
		
		//delete deleted auth
		securityManager.deleteAuthentication(auth);
		//check that the transaction is not in "rollback" mode
		Identity reloadedId = securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertEquals(identity, reloadedId);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteInvalidAuthenticationsByEmail() {
		IdentityWithLogin identityWithLogin = JunitTestHelper.createAndPersistRndUser("auth-del-email-");
		Identity identity = identityWithLogin.getIdentity();
		User user = identity.getUser();
		String email = user.getEmail();
		securityManager.createAndPersistAuthentication(identity, "OLAT", email, "secret", Encoder.Algorithm.sha512);
		securityManager.createAndPersistAuthentication(identity, "del-mail", email, "secret", Encoder.Algorithm.sha512);
		securityManager.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_EMAIL, email, "secret", Encoder.Algorithm.sha512);
		securityManager.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL, email, "secret", Encoder.Algorithm.sha512);
		securityManager.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL, email, "secret", Encoder.Algorithm.sha512);
		securityManager.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL, email, "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		// User with email address exists: The authentications are valid.
		securityManager.deleteInvalidAuthenticationsByEmail(email);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_HA1_EMAIL));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL));
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, "OLAT"));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(identityWithLogin.getLogin(), "OLAT"));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, "del-mail"));
		
		// Email of the user changed: The authentications are not valid any longer.
		user.setProperty(UserConstants.EMAIL, "new@trashcmail.com");
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "new@trashcmail.com");
		userManager.updateUser(user);
		dbInstance.commitAndCloseSession();
		
		securityManager.deleteInvalidAuthenticationsByEmail(email);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_HA1_EMAIL));
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL));
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL));
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL));
		Assert.assertNull(securityManager.findAuthenticationByAuthusername(email, "OLAT"));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(identityWithLogin.getLogin(), "OLAT"));
		Assert.assertNotNull(securityManager.findAuthenticationByAuthusername(email, "del-mail"));
	}
	
	@Test
	public void getAuthentications() {
		IdentityWithLogin test = JunitTestHelper.createAndPersistRndUser("auth-0");
		dbInstance.commitAndCloseSession();
		
		List<Authentication> authentications = securityManager.getAuthentications(test.getIdentity());
		Authentication authentication = authentications.get(0);
		Assert.assertEquals(test.getLogin(), authentication.getAuthusername());
	}
	
	@Test
	public void findAuthenticationNameOLAT() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("auth-0");
		String testLogin = id.getLogin();
		
		String name = securityManager.findAuthenticationName(id.getIdentity());
		Assert.assertEquals(testLogin,name);
	}
	
	@Test
	public void findAuthenticationNameLDAP() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("auth-0");
		String ldapAuthusername = UUID.randomUUID().toString();
		securityManager.createAndPersistAuthentication(id.getIdentity(), LDAPAuthenticationController.PROVIDER_LDAP, ldapAuthusername, null, null);
		securityManager.createAndPersistAuthentication(id.getIdentity(), WebDAVAuthManager.PROVIDER_HA1, UUID.randomUUID().toString(), "secret", Encoder.Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		String name = securityManager.findAuthenticationName(id.getIdentity());
		Assert.assertEquals(ldapAuthusername, name);
	}

	@Test
	public void findAuthenticationByAuthusername() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("auth-0");
		String testLogin = id.getLogin();
		
		
		Authentication authentication = securityManager.findAuthenticationByAuthusername(testLogin, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertEquals(testLogin, authentication.getAuthusername());
	}
	
	@Test
	public void findAuthenticationByAuthusername_attack() {
		String testLoginHacked = "*est-logi*";
		Authentication authentication1 = securityManager.findAuthenticationByAuthusername(testLoginHacked, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertNull(authentication1);
		
		String testLoginHacked2 = "$est-login";
		Authentication authentication2 = securityManager.findAuthenticationByAuthusername(testLoginHacked2, BaseSecurityModule.getDefaultAuthProviderIdentifier());
		Assert.assertNull(authentication2);	
	}

	@Test
	public void updateLastLogin() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("last-login-0");
		dbInstance.commitAndCloseSession();
		
		securityManager.setIdentityLastLogin(id);
		dbInstance.commitAndCloseSession();

		id = securityManager.loadIdentityByKey(id.getKey());
		Date lastLogin = id.getLastLogin();
		Assert.assertNotNull(lastLogin);
	}

	@Test
	public void updateLastLoginAndInactivationDate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("last-login-0");
		((IdentityImpl)id).setInactivationEmailDate(new Date());
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();

		id = securityManager.loadIdentityByKey(id.getKey());
		Date mergedInactivationDate = ((IdentityImpl)id).getInactivationEmailDate();
		Assert.assertNotNull(mergedInactivationDate);
		dbInstance.commitAndCloseSession();
		
		securityManager.setIdentityLastLogin(id);
		dbInstance.commitAndCloseSession();

		id = securityManager.loadIdentityByKey(id.getKey());
		Date lastLogin = id.getLastLogin();
		Assert.assertNotNull(lastLogin);
		Date inactivationDate = ((IdentityImpl)id).getInactivationEmailDate();
		Assert.assertNull(inactivationDate);
	}

	@Test
	public void reactivatedIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-login-0");
		((IdentityImpl)id).setStatus(Identity.STATUS_INACTIVE);
		((IdentityImpl)id).setInactivationDate(new Date());
		((IdentityImpl)id).setInactivationEmailDate(new Date());
		((IdentityImpl)id).setReactivationDate(new Date());
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();
		
		Identity reactivatedIdentity = securityManager.reactivatedIdentity(id);
		Assert.assertNull(reactivatedIdentity.getInactivationDate());
		Assert.assertNull(reactivatedIdentity.getReactivationDate());
		Assert.assertNull(((IdentityImpl)reactivatedIdentity).getInactivationEmailDate());
		dbInstance.commitAndCloseSession();
		
		Identity reloadedIdentity = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertNull(reloadedIdentity.getInactivationDate());
		Assert.assertNull(reloadedIdentity.getReactivationDate());
		Assert.assertNull(((IdentityImpl)reloadedIdentity).getInactivationEmailDate());
	}
	
	/**
	 * Little stress test for two locked queries
	 */
	@Test
	public void reactivatedIdentityAndSetLastLogin() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-login-0");
		((IdentityImpl)id).setStatus(Identity.STATUS_INACTIVE);
		((IdentityImpl)id).setInactivationDate(new Date());
		((IdentityImpl)id).setInactivationEmailDate(new Date());
		((IdentityImpl)id).setReactivationDate(new Date());
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();
		
		for(int i=0; i<5; i++) {
			securityManager.setIdentityLastLogin(id);
			securityManager.reactivatedIdentity(id);
		}
		
		Identity reloadedIdentity = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertNull(reloadedIdentity.getInactivationDate());
		Assert.assertNull(reloadedIdentity.getReactivationDate());
		Assert.assertNull(((IdentityImpl)reloadedIdentity).getInactivationEmailDate());
	}
	
	@Test
	public void isIdentityLoginAllowed() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-login-0");
		((IdentityImpl)id).setStatus(Identity.STATUS_ACTIV);
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(securityManager.isIdentityLoginAllowed(id, "OLAT"));
		Assert.assertTrue(securityManager.isIdentityLoginAllowed(id, null));
		Assert.assertTrue(securityManager.isIdentityLoginAllowed(id, "Shib"));
	}
	
	@Test
	public void isIdentityLoginAllowedInactive() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-login-1");
		((IdentityImpl)id).setStatus(Identity.STATUS_INACTIVE);
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(securityManager.isIdentityLoginAllowed(id, "OLAT"));
		Assert.assertFalse(securityManager.isIdentityLoginAllowed(id, null));
		Assert.assertTrue(securityManager.isIdentityLoginAllowed(id, "Shib"));
	}
	
	@Test
	public void isIdentityLoginAllowedLoginDenied() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("allowed-login-2");
		((IdentityImpl)id).setStatus(Identity.STATUS_LOGIN_DENIED);
		id = dbInstance.getCurrentEntityManager().merge(id);
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(securityManager.isIdentityLoginAllowed(id, "OLAT"));
		Assert.assertFalse(securityManager.isIdentityLoginAllowed(id, null));
		Assert.assertFalse(securityManager.isIdentityLoginAllowed(id, "Shib"));
	}
	
	
	@Test
	public void countUniqueUserLoginsSince() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -100);
		Long initialUserLogins = securityManager.countUniqueUserLoginsSince(cal.getTime());
		Assert.assertNotNull(initialUserLogins);
		Assert.assertTrue(initialUserLogins.longValue() >= 0);
	}


	@Test
	public void setIdentityAsActiv() throws InterruptedException {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity");
		
		final int maxLoop = 2000; // => 2000 x 11ms => 22sec => finished in 120sec

		CountDownLatch latch = new CountDownLatch(4);
		ActivThread[] threads = new ActivThread[4];
		for(int i=0; i<threads.length;i++) {
			threads[i] = new ActivThread(ident, maxLoop, latch);
		}

		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}

		latch.await(120, TimeUnit.SECONDS);

		List<Exception> exceptionsHolder = new ArrayList<>();
		for(int i=0; i<threads.length;i++) {
			exceptionsHolder.addAll(threads[i].exceptionHolder);
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionsHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionsHolder.size(), exceptionsHolder.isEmpty());				
	}
	
	private static class ActivThread extends Thread {
		
		private final int maxLoop;
		private final Identity identity;
		private final CountDownLatch countDown;
		private final List<Exception> exceptionHolder = new ArrayList<>();
		private final BaseSecurity securityManager;
		
		public ActivThread(Identity identity, int maxLoop, CountDownLatch countDown) {
			this.identity = identity;
			this.maxLoop = maxLoop;
			this.countDown = countDown;
			securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		}
		
		@Override
		public void run() {
			try {
				sleep(10);
				for (int i=0; i<maxLoop; i++) {
					try {
						securityManager.setIdentityLastLogin(identity);
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().closeSession();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			} catch (Exception e) {
				exceptionHolder.add(e);
			} finally {
				countDown.countDown();
			}
		}
	}
	
}
