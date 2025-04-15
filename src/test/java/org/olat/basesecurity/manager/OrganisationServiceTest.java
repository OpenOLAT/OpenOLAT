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
package org.olat.basesecurity.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@AfterClass()
	public static void waitAll() {
		waitMessageAreConsumed();
	}
	
	@Test
	public void getDefaultOrganisation() {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Assert.assertNotNull(organisation);
		Assert.assertEquals(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER, organisation.getIdentifier());
	}
	
	@Test
	public void getDefaultsSystemAdministator() {
		List<Identity> administrators = organisationService.getDefaultsSystemAdministator();
		Assert.assertNotNull(administrators);
		Assert.assertFalse(administrators.isEmpty());
	}

	@Test
	public void shouldDetectMultipleDefaultOrganisations() {
		// Setup second default organisation
		Organisation secondDefaultOrg = organisationService.createOrganisation(
				"Second Default Org", OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER,
				null, null, null, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		boolean multipleDefaults = organisationService.hasMultipleDefaultOrganisations();
		// delete afterwards
		organisationService.deleteOrganisation(secondDefaultOrg, secondDefaultOrg, null);
		Assert.assertTrue(multipleDefaults);
	}

	@Test
	public void shouldCountExactGlobalRolesOutsideDefault() {
		int before = organisationService.getGlobalRolesOutsideDefaultIdentities().size();

		Organisation defaultOrg = organisationService.getDefaultOrganisation();
		Organisation otherOrg = organisationService.createOrganisation("AltOrg", UUID.randomUUID().toString(), "", null, null, JunitTestHelper.getDefaultActor());

		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("global-multi-org");

		// Assign sysadmin in both orgs
		organisationService.addMember(defaultOrg, user, OrganisationRoles.sysadmin, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());
		organisationService.addMember(otherOrg, user, OrganisationRoles.sysadmin, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());

		// Assign poolmanager in only other org
		organisationService.addMember(otherOrg, user, OrganisationRoles.poolmanager, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		int after = organisationService.getGlobalRolesOutsideDefaultIdentities().size();
		Assert.assertEquals(1, after - before);
	}

	@Test
	public void shouldMoveGlobalRolesToDefault() {
		Organisation defaultOrg = organisationService.getDefaultOrganisation();
		Organisation altOrg = organisationService.createOrganisation("MigratingOrg", UUID.randomUUID().toString(), "", null, null, JunitTestHelper.getDefaultActor());

		// Add a global-role identity to the 'wrong' org
		Identity globalUser = JunitTestHelper.createAndPersistIdentityAsRndUser("global-to-move");
		organisationService.addMember(altOrg, globalUser, OrganisationRoles.poolmanager, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		// Make sure it's not in default org
		List<Long> defaultKeysBefore = organisationDao.getMemberKeys(defaultOrg, OrganisationRoles.poolmanager);
		Assert.assertFalse(defaultKeysBefore.contains(globalUser.getKey()));

		boolean moved = organisationService.moveGlobalRolesToDefault(null);
		Assert.assertTrue(moved);
		dbInstance.commitAndCloseSession();

		// Now the identity should be in default org
		List<Long> defaultKeysAfter = organisationDao.getMemberKeys(defaultOrg, OrganisationRoles.poolmanager);
		Assert.assertTrue(defaultKeysAfter.contains(globalUser.getKey()));
	}

	@Test
	public void addMembershipWithInheritance() {
		Identity user = createRandomUser("Org. user");
		
		Organisation organisation = defaultUnitTestOrganisation;
		String identifierLevel1 = UUID.randomUUID().toString();
		Organisation organisationLevel1 = organisationService.createOrganisation("Sub-organisation", identifierLevel1, "", organisation, null, JunitTestHelper.getDefaultActor());
		String identifierLevel2 = UUID.randomUUID().toString();
		Organisation organisationLevel2 = organisationService.createOrganisation("Sub-sub-organisation", identifierLevel2, "", organisationLevel1, null, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//add membership
		organisationService.addMember(organisationLevel1, user, OrganisationRoles.user, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//check level 1
		GroupMembership membershipLevel1 = groupDao.getMembership(organisationLevel1.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertNotNull(membershipLevel1);
		Assert.assertEquals(OrganisationRoles.user.name(), membershipLevel1.getRole());
		Assert.assertEquals(GroupMembershipInheritance.root, membershipLevel1.getInheritanceMode());
		
		//check level 2
		GroupMembership membershipLevel2 = groupDao.getMembership(organisationLevel2.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertEquals(OrganisationRoles.user.name(), membershipLevel2.getRole());
		Assert.assertEquals(GroupMembershipInheritance.inherited, membershipLevel2.getInheritanceMode());
	}

	@Test
	public void removeAllMembershipWithInheritance() {
		Identity user = createRandomUser("Org. user");
		
		Organisation organisation = defaultUnitTestOrganisation;
		String identifierLevel1 = UUID.randomUUID().toString();
		Organisation organisationLevel1 = organisationService.createOrganisation("1. Org.", identifierLevel1, "", organisation, null, JunitTestHelper.getDefaultActor());
		String identifierLevel1_1 = UUID.randomUUID().toString();
		Organisation organisationLevel1_1 = organisationService.createOrganisation("1.1.", identifierLevel1_1, "", organisationLevel1, null, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//add membership
		organisationService.addMember(organisationLevel1, user, OrganisationRoles.user, GroupMembershipInheritance.root, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisationLevel1_1, user, OrganisationRoles.author, GroupMembershipInheritance.none, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//check level 1
		GroupMembership membershipLevel1 = groupDao.getMembership(organisationLevel1.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertNotNull(membershipLevel1);
		Assert.assertEquals(OrganisationRoles.user.name(), membershipLevel1.getRole());
		Assert.assertEquals(GroupMembershipInheritance.root, membershipLevel1.getInheritanceMode());
		
		//check level 1-1
		GroupMembership membershipLevel2 = groupDao.getMembership(organisationLevel1_1.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertEquals(OrganisationRoles.user.name(), membershipLevel2.getRole());
		Assert.assertEquals(GroupMembershipInheritance.inherited, membershipLevel2.getInheritanceMode());
		
		// remove all the memberships
		organisationService.removeMember(organisationLevel1, user, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		// check there is no membership left
		List<GroupMembership> membershipsLevel1 = groupDao.getMemberships(organisationLevel1.getGroup(), user);
		Assert.assertTrue(membershipsLevel1.isEmpty());
		List<GroupMembership> membershipsLevel1_1 = groupDao.getMemberships(organisationLevel1_1.getGroup(), user);
		Assert.assertEquals(1, membershipsLevel1_1.size());
		
		GroupMembership lastMembersip = membershipsLevel1_1.get(0);
		Assert.assertEquals(OrganisationRoles.author.name(), lastMembersip.getRole());
	}
	
	@Test
	public void createSubOrganisationWithInheritedsMemberships() {
		Identity user = createRandomUser("Org. user");
		
		Organisation defOrganisation = defaultUnitTestOrganisation;
		Organisation organisation = organisationService.createOrganisation("Inherit-organisation", "Top", "", defOrganisation, null, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation, user, OrganisationRoles.usermanager, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation, user, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		

		Organisation subOrganisation = organisationService.createOrganisation("Sub-organisation", "Sub", "", organisation, null, JunitTestHelper.getDefaultActor());
		
		//check level user role (not inherited)
		GroupMembership userMembership = groupDao.getMembership(subOrganisation.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertNull(userMembership);

		//check level user manager role (inherited)
		GroupMembership userManagerMembership = groupDao.getMembership(subOrganisation.getGroup(), user, OrganisationRoles.usermanager.name());
		Assert.assertEquals(OrganisationRoles.usermanager.name(), userManagerMembership.getRole());
		Assert.assertEquals(GroupMembershipInheritance.inherited, userManagerMembership.getInheritanceMode());
	}
	
	/**
	 * Move the organisation1_1 from organisation1 to organisation2 and
	 * check the inheritance of membership.
	 */
	@Test
	public void moveOrganisation() {
		Organisation organisation1 = organisationService.createOrganisation("Top 1", "Top 1", "", null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1 = organisationService.createOrganisation("Medium 1", "Medium 1", "", organisation1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1_1 = organisationService.createOrganisation("Low 1", "Low 1", "", organisation1_1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1_2 = organisationService.createOrganisation("Low 2", "Low 2", "", organisation1_1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1_3 = organisationService.createOrganisation("Low 3", "Low 3", "", organisation1_1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1_3_1 = organisationService.createOrganisation("Underworld 1", "Underworld 1", "", organisation1_1_3, null, JunitTestHelper.getDefaultActor());
		Organisation organisation1_1_3_2 = organisationService.createOrganisation("Underworld 2", "Underworld 2", "", organisation1_1_3, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation("Top 2", "Top 2", "", null, null, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		Identity userManager1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-mgr-1");
		Identity userManager2 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-mgr-2");
		Identity author1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-1-1");
		Identity author1_1_3 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-1-1-3");
		Identity user1_1_2 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-1-1-2");
		organisationService.addMember(organisation1, userManager1, OrganisationRoles.usermanager, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation2, userManager2, OrganisationRoles.usermanager, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation1_1, author1_1, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation1_1_3, author1_1_3, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation1_1_2, user1_1_2, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		organisationService.moveOrganisation(organisation1_1, organisation2, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//check descendants
		List<Organisation> descendantsOrganisation1 = organisationDao.getDescendants(organisation1);
		Assert.assertTrue(descendantsOrganisation1.isEmpty());
		List<Organisation> descendantsOrganisation2 = organisationDao.getDescendants(organisation2);
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1));
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1_1));
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1_2));
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1_3));
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1_3_1));
		Assert.assertTrue(descendantsOrganisation2.contains(organisation1_1_3_2));
		
		// check memberships
		// user manager 1 lost it's membership on "Medium", it's under organization 2
		GroupMembership userManager1Membership = groupDao.getMembership(organisation1_1.getGroup(), userManager1, OrganisationRoles.usermanager.name());
		Assert.assertNull(userManager1Membership);
		// user manager 2 get power over "Medium"
		GroupMembership userManager2Membership = groupDao.getMembership(organisation1_1_3_1.getGroup(), userManager2, OrganisationRoles.usermanager.name());
		Assert.assertNotNull(userManager2Membership);
		Assert.assertEquals(GroupMembershipInheritance.inherited, userManager2Membership.getInheritanceMode());
		// author on "Medium" stay
		GroupMembership author1_1Membership = groupDao.getMembership(organisation1_1.getGroup(), author1_1, OrganisationRoles.author.name());
		Assert.assertNotNull(author1_1Membership);
		Assert.assertEquals(GroupMembershipInheritance.root, author1_1Membership.getInheritanceMode());
		GroupMembership author1_1MembershipInherited = groupDao.getMembership(organisation1_1_2.getGroup(), author1_1, OrganisationRoles.author.name());
		Assert.assertNotNull(author1_1MembershipInherited);
		Assert.assertEquals(GroupMembershipInheritance.inherited, author1_1MembershipInherited.getInheritanceMode());
		// author on "Low" stay
		GroupMembership author1_1_3Membership = groupDao.getMembership(organisation1_1_3.getGroup(), author1_1_3, OrganisationRoles.author.name());
		Assert.assertNotNull(author1_1_3Membership);
		Assert.assertEquals(GroupMembershipInheritance.root, author1_1_3Membership.getInheritanceMode());
		GroupMembership author1_1_3MembershipInherited = groupDao.getMembership(organisation1_1_3_2.getGroup(), author1_1_3, OrganisationRoles.author.name());
		Assert.assertNotNull(author1_1_3MembershipInherited);
		Assert.assertEquals(GroupMembershipInheritance.inherited, author1_1_3MembershipInherited.getInheritanceMode());	
	}
	
	@Test
	public void moveMembers() {
		Organisation defOrganisation = defaultUnitTestOrganisation;
		Organisation sourceOrganisation1 = organisationService.createOrganisation("Source 1", "Source 1", "", defOrganisation, null, JunitTestHelper.getDefaultActor());
		Organisation sourceOrganisation1_1 = organisationService.createOrganisation("Source 1.1", "Source 1.1", "", sourceOrganisation1, null, JunitTestHelper.getDefaultActor());
		Organisation sourceOrganisation1_1_1 = organisationService.createOrganisation("Source 1.1.1", "Source 1.1.1", "", sourceOrganisation1_1, null, JunitTestHelper.getDefaultActor());
		
		Organisation targetOrganisation1 = organisationService.createOrganisation("Target 1", "Target 1", "", defOrganisation, null, JunitTestHelper.getDefaultActor());
		Organisation targetOrganisation1_1 = organisationService.createOrganisation("Target 1", "Target 1", "", targetOrganisation1, null, JunitTestHelper.getDefaultActor());
		Organisation targetOrganisation1_1_1 = organisationService.createOrganisation("Target 1", "Target 1", "", targetOrganisation1_1, null, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		Identity user1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-1");
		organisationService.addMember(sourceOrganisation1, user1, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		Identity user1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-1-1");
		organisationService.addMember(sourceOrganisation1_1, user1_1, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		Identity user1_1_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-1-1-1");
		organisationService.addMember(sourceOrganisation1_1_1, user1_1_1, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		Identity admin1 = JunitTestHelper.createAndPersistIdentityAsRndUser("admin-1");
		organisationService.addMember(sourceOrganisation1, admin1, OrganisationRoles.administrator, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		// move the user1 and author1 from org. 1 source to target
		List<Identity> identitiesToMove = new ArrayList<>();
		identitiesToMove.add(user1);
		identitiesToMove.add(admin1);
		List<OrganisationRoles> roles = new ArrayList<>();
		roles.add(OrganisationRoles.administrator);
		roles.add(OrganisationRoles.author);
		roles.add(OrganisationRoles.user);
		organisationService.moveMembers(sourceOrganisation1, targetOrganisation1, identitiesToMove, roles, JunitTestHelper.getDefaultActor());
		
		// check user 1
		{
			GroupMembership userMembership_source1 = groupDao.getMembership(sourceOrganisation1.getGroup(), user1, OrganisationRoles.user.name());
			Assert.assertNull(userMembership_source1);
			GroupMembership userMembership_1 = groupDao.getMembership(targetOrganisation1.getGroup(), user1, OrganisationRoles.user.name());
			Assert.assertNotNull(userMembership_1);
			Assert.assertEquals(GroupMembershipInheritance.none, userMembership_1.getInheritanceMode());
			// make sure of unwanted inheritance
			GroupMembership userMembership_1_1 = groupDao.getMembership(targetOrganisation1_1.getGroup(), user1, OrganisationRoles.user.name());
			Assert.assertNull(userMembership_1_1);
		}
		
		// check user1_1 (no changes)
		{
			GroupMembership user11Membership_1_1 = groupDao.getMembership(sourceOrganisation1_1.getGroup(), user1_1, OrganisationRoles.user.name());
			Assert.assertNotNull(user11Membership_1_1);
			Assert.assertEquals(GroupMembershipInheritance.none, user11Membership_1_1.getInheritanceMode());
			GroupMembership user11TargetMembership_1 = groupDao.getMembership(targetOrganisation1.getGroup(), user1_1, OrganisationRoles.user.name());
			Assert.assertNull(user11TargetMembership_1);
			GroupMembership user11TargetMembership_1_1 = groupDao.getMembership(targetOrganisation1_1.getGroup(), user1_1, OrganisationRoles.user.name());
			Assert.assertNull(user11TargetMembership_1_1);
		}

		// check admin
		{
			GroupMembership adminMembership_source1_1_1 = groupDao.getMembership(sourceOrganisation1_1_1.getGroup(), admin1, OrganisationRoles.administrator.name());
			Assert.assertNull(adminMembership_source1_1_1);
			GroupMembership adminMembership1 = groupDao.getMembership(targetOrganisation1.getGroup(), admin1, OrganisationRoles.administrator.name());
			Assert.assertNotNull(adminMembership1);
			Assert.assertEquals(GroupMembershipInheritance.root, adminMembership1.getInheritanceMode());
			GroupMembership adminMembership1_1_1 = groupDao.getMembership(targetOrganisation1_1_1.getGroup(), admin1, OrganisationRoles.administrator.name());
			Assert.assertNotNull(adminMembership1_1_1);
			Assert.assertEquals(GroupMembershipInheritance.inherited, adminMembership1_1_1.getInheritanceMode());
		}
	}
	
	private Identity createRandomUser(String login) {
		login += UUID.randomUUID().toString();
		User user = userManager.createUser("first" + login, "last" + login, login + "@openolat.com");
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				null, null, null,
				null, null, defaultUnitTestOrganisation, null, JunitTestHelper.getDefaultActor());
	}
	
	@Test
	public void shouldGetOrganisationIdentityEmails() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		// Regular users
		createUser(organisation1, random(), "openolat.org");
		createUser(organisation1, random(), "openolat.org");
		createUser(organisation1, random(), "sub.openolat.org");
		createUser(organisation1, random(), "sub2.openolat.org");
		createUser(organisation1, random(), "openolat2.org");
		// Other domain
		createUser(organisation1, random(), "openolat3.org");
		// Not in query list
		createUser(organisation2, random(), "openolat.org");
		
		OrganisationEmailDomain oedOpenOlat = organisationService.createOrganisationEmailDomain(organisation1, "openolat.org");
		OrganisationEmailDomain oedOpenOlat2 = organisationService.createOrganisationEmailDomain(organisation1, "openolat2.org");
		OrganisationEmailDomain oedSubOpenOlat = organisationService.createOrganisationEmailDomain(organisation1, "sub.openolat.org");
		OrganisationEmailDomain oedOpenOlatSubAllowed = organisationService.createOrganisationEmailDomain(organisation1, "openolat.org");
		oedOpenOlatSubAllowed.setSubdomainsAllowed(true);
		oedOpenOlatSubAllowed = organisationService.updateOrganisationEmailDomain(oedOpenOlatSubAllowed);
		OrganisationEmailDomain oedFrentix = organisationService.createOrganisationEmailDomain(organisation1, "frentix.com");
		dbInstance.commitAndCloseSession();
		
		Map<Long, Integer> emailDomainKeyToUsersCount = organisationService.getEmailDomainKeyToUsersCount(
				List.of(oedOpenOlat, oedOpenOlat2, oedSubOpenOlat, oedOpenOlatSubAllowed));
		
		Assert.assertEquals(Integer.valueOf(2), emailDomainKeyToUsersCount.get(oedOpenOlat.getKey()));
		Assert.assertEquals(Integer.valueOf(1), emailDomainKeyToUsersCount.get(oedOpenOlat2.getKey()));
		Assert.assertEquals(Integer.valueOf(1), emailDomainKeyToUsersCount.get(oedSubOpenOlat.getKey()));
		Assert.assertEquals(Integer.valueOf(4), emailDomainKeyToUsersCount.get(oedOpenOlatSubAllowed.getKey()));
		Assert.assertNull(emailDomainKeyToUsersCount.get(oedFrentix.getKey()));
	}
	
	private Identity createUser(Organisation organisation, String login, String mailDomain) {
		User user = userManager.createUser("orged" + login, "orged" + login, login + "@" + mailDomain);
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(null, login, null, user,
				null, null, null, null, null, organisation, null, JunitTestHelper.getDefaultActor());
	}
	
	@Test
	public void shouldCheckIfEmailDomainAllowed() {
		// Simple domain
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());
		OrganisationEmailDomain emailDomain = organisationService.createOrganisationEmailDomain(organisation, "openolat.org");
		List<OrganisationEmailDomain> emailDomains = List.of(emailDomain);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@openolat.org"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@OPENOLAT.ORG"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@example.openolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@exampleopenolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@frentix.com"));
		
		// Domain with sub domains allowed
		emailDomain.setSubdomainsAllowed(true);
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@openolat.org"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@OPENOLAT.ORG"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@example.openolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@exampleopenolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@frentix.com"));
		
		// Disabled domain
		emailDomain.setEnabled(false);
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@openolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@OPENOLAT.ORG"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@example.openolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@exampleopenolat.org"));
		Assert.assertFalse(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@frentix.com"));
		
		// Wildcard domain
		OrganisationEmailDomain emailDomainWildcard = organisationService.createOrganisationEmailDomain(organisation, OrganisationEmailDomain.WILDCARD);
		emailDomains = List.of(emailDomain, emailDomainWildcard);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@openolat.org"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@OPENOLAT.ORG"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@example.openolat.org"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@exampleopenolat.org"));
		Assert.assertTrue(organisationService.isEmailDomainAllowed(emailDomains, "shankar.geeta@frentix.com"));
	}

	@Test
	public void shouldGetMatchingEmailDomains() {
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null, null, JunitTestHelper.getDefaultActor());

		// Case 1: Exact match found
		OrganisationEmailDomain emailDomain1 = organisationService.createOrganisationEmailDomain(organisation, "example.com");
		OrganisationEmailDomain emailDomain2 = organisationService.createOrganisationEmailDomain(organisation, "test.com");
		dbInstance.commitAndCloseSession();
		List<OrganisationEmailDomain> emailDomains = List.of(emailDomain1, emailDomain2);

		Assert.assertEquals(List.of(emailDomain1), organisationService.getMatchingEmailDomains(emailDomains, "example.com"));
		Assert.assertEquals(List.of(emailDomain2), organisationService.getMatchingEmailDomains(emailDomains, "test.com"));
		Assert.assertTrue(organisationService.getMatchingEmailDomains(emailDomains, "nomatch.com").isEmpty());

		// Case 2: Wildcard domain exists, but exact match takes priority
		OrganisationEmailDomain wildcardDomain = organisationService.createOrganisationEmailDomain(organisation, OrganisationEmailDomain.WILDCARD);
		dbInstance.commitAndCloseSession();
		emailDomains = List.of(emailDomain1, wildcardDomain);

		Assert.assertEquals(List.of(emailDomain1), organisationService.getMatchingEmailDomains(emailDomains, "example.com")); // Exact match should be prioritized
		Assert.assertEquals(List.of(wildcardDomain), organisationService.getMatchingEmailDomains(emailDomains, "random.com")); // No exact match, wildcard applies

		// Case 3: Only wildcard domains exist, should return all wildcard domains
		OrganisationEmailDomain wildcardDomain2 = organisationService.createOrganisationEmailDomain(organisation, OrganisationEmailDomain.WILDCARD);
		dbInstance.commitAndCloseSession();
		emailDomains = List.of(wildcardDomain, wildcardDomain2);

		Assert.assertEquals(List.of(wildcardDomain, wildcardDomain2), organisationService.getMatchingEmailDomains(emailDomains, "anything.com"));

		// Case 4: No matching domain and no wildcard → Return empty list
		emailDomains = List.of(emailDomain1, emailDomain2);
		Assert.assertTrue(organisationService.getMatchingEmailDomains(emailDomains, "unknown.com").isEmpty());

		// Case 5: Mixed case handling (case-insensitive match)
		Assert.assertEquals(List.of(emailDomain1), organisationService.getMatchingEmailDomains(emailDomains, "EXAMPLE.COM"));
		Assert.assertEquals(List.of(emailDomain2), organisationService.getMatchingEmailDomains(emailDomains, "TEST.COM"));

		// Case 6: Empty input list
		emailDomains = List.of();
		Assert.assertTrue(organisationService.getMatchingEmailDomains(emailDomains, "any.com").isEmpty());

		// Case 7: Domains with subdomains allowed
		emailDomain1.setSubdomainsAllowed(true);
		emailDomains = List.of(emailDomain1);

		Assert.assertEquals(List.of(emailDomain1), organisationService.getMatchingEmailDomains(emailDomains, "sub.example.com"));
		Assert.assertEquals(List.of(emailDomain1), organisationService.getMatchingEmailDomains(emailDomains, "deep.sub.example.com"));
		Assert.assertTrue(organisationService.getMatchingEmailDomains(emailDomains, "random.com").isEmpty());
	}
}
