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
package org.olat.basesecurity.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
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
	private OrganisationService organisationService;
	
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
	public void addMembershipWithInheritance() {
		Identity user = createRandomUser("Org. user");
		
		Organisation organisation = organisationService.getDefaultOrganisation();
		String identifierLevel1 = UUID.randomUUID().toString();
		Organisation organisationLevel1 = organisationService.createOrganisation("Sub-organisation", identifierLevel1, "", organisation, null);
		String identifierLevel2 = UUID.randomUUID().toString();
		Organisation organisationLevel2 = organisationService.createOrganisation("Sub-sub-organisation", identifierLevel2, "", organisationLevel1, null);
		dbInstance.commitAndCloseSession();
		
		//add membership
		organisationService.addMember(organisationLevel1, user, OrganisationRoles.user, GroupMembershipInheritance.root);
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
		
		Organisation organisation = organisationService.getDefaultOrganisation();
		String identifierLevel1 = UUID.randomUUID().toString();
		Organisation organisationLevel1 = organisationService.createOrganisation("1. Org.", identifierLevel1, "", organisation, null);
		String identifierLevel1_1 = UUID.randomUUID().toString();
		Organisation organisationLevel1_1 = organisationService.createOrganisation("1.1.", identifierLevel1_1, "", organisationLevel1, null);
		dbInstance.commitAndCloseSession();
		
		//add membership
		organisationService.addMember(organisationLevel1, user, OrganisationRoles.user, GroupMembershipInheritance.root);
		organisationService.addMember(organisationLevel1_1, user, OrganisationRoles.author, GroupMembershipInheritance.none);
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
		organisationService.removeMember(organisationLevel1, user);
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
	public void createSubOrgnisationWithInheritedsMemberships() {
		Identity user = createRandomUser("Org. user");
		
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Inherit-organisation", "Top", "", defOrganisation, null);
		organisationService.addMember(organisation, user, OrganisationRoles.usermanager, GroupMembershipInheritance.root);
		organisationService.addMember(organisation, user, OrganisationRoles.user, GroupMembershipInheritance.none);
		dbInstance.commitAndCloseSession();
		

		Organisation subOrganisation = organisationService.createOrganisation("Sub-organisation", "Sub", "", organisation, null);
		
		//check level user role (not inherited)
		GroupMembership userMembership = groupDao.getMembership(subOrganisation.getGroup(), user, OrganisationRoles.user.name());
		Assert.assertNull(userMembership);

		//check level user manager role (inherited)
		GroupMembership userManagerMembership = groupDao.getMembership(subOrganisation.getGroup(), user, OrganisationRoles.usermanager.name());
		Assert.assertEquals(OrganisationRoles.usermanager.name(), userManagerMembership.getRole());
		Assert.assertEquals(GroupMembershipInheritance.inherited, userManagerMembership.getInheritanceMode());
	}
	
	private Identity createRandomUser(String login) {
		login += UUID.randomUUID().toString();
		User user = userManager.createUser("first" + login, "last" + login, login + "@openolat.com");
		return securityManager.createAndPersistIdentityAndUser(login, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), login, JunitTestHelper.PWD);
	}

}
