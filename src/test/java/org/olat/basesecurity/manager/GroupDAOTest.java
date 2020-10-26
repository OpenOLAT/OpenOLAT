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


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Grant;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	@Test
	public void createGroup() {
		Group group = groupDao.createGroup();
		dbInstance.commit();
		
		Assert.assertNotNull(group);
	}
	
	@Test
	public void createGroupMembership() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembershipTwoWay(group, id, "author");

		dbInstance.commit();
		
		Assert.assertNotNull(membership);
	}
	
	@Test
	public void createGroupMembershipOneWay() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		groupDao.addMembershipOneWay(group, id, "author");
		dbInstance.commit();
		
		GroupMembership membership = groupDao.getMembership(group, id, "author");
		Assert.assertNotNull(membership);
		Assert.assertEquals(id, membership.getIdentity());
		Assert.assertEquals(group, membership.getGroup());
		Assert.assertEquals("author", membership.getRole());
		Assert.assertEquals(GroupMembershipInheritance.none, membership.getInheritanceMode());
	}
	
	@Test
	public void createGroupMembership_v2() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembershipTwoWay(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.getCurrentEntityManager().detach(group);
		dbInstance.commitAndCloseSession();
		
		GroupImpl loadedGroup = (GroupImpl)groupDao.loadGroup(group.getKey());
		Assert.assertNotNull(loadedGroup);
		Set<GroupMembership> members = loadedGroup.getMembers();
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void createGroupMembership_oneWay_v2() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		groupDao.addMembershipOneWay(group, id, "author");
		dbInstance.commitAndCloseSession();
		
		GroupImpl loadedGroup = (GroupImpl)groupDao.loadGroup(group.getKey());
		Assert.assertNotNull(loadedGroup);
		Set<GroupMembership> members = loadedGroup.getMembers();
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void getMemberships() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembershipTwoWay(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.getCurrentEntityManager().detach(group);
		dbInstance.commitAndCloseSession();
		
		List<GroupMembership> members = groupDao.getMemberships(group, "author");
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void getMemberships_oneWay() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		groupDao.addMembershipOneWay(group, id, "author");
		dbInstance.commitAndCloseSession();
		
		List<GroupMembership> members = groupDao.getMemberships(group, "author");
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void getMembershipsByGroup() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-30-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-31-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "author");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "author", GroupMembershipInheritance.root);
		dbInstance.commit();
		
		List<GroupMembership> memberships = groupDao.getMemberships(group);
		assertThat(memberships)
			.hasSize(2)
			.containsExactlyInAnyOrder(membership1, membership2);
	}
	
	@Test
	public void getMembershipsByGroupAndInheritance() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-30-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-31-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "coach");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "author", GroupMembershipInheritance.root);
		dbInstance.commit();
		
		// root only
		List<GroupMembership> rootMemberships = groupDao.getMemberships(group, GroupMembershipInheritance.root);
		assertThat(rootMemberships)
			.hasSize(1)
			.containsExactlyInAnyOrder(membership2);
		
		// default only
		List<GroupMembership> noneMemberships = groupDao.getMemberships(group, GroupMembershipInheritance.none);
		assertThat(noneMemberships)
			.hasSize(1)
			.containsExactlyInAnyOrder(membership1);
		
		// root and none
		List<GroupMembership> rootNoneMemberships = groupDao.getMemberships(group, GroupMembershipInheritance.root, GroupMembershipInheritance.none);
		assertThat(rootNoneMemberships)
			.hasSize(2)
			.containsExactlyInAnyOrder(membership1, membership2);
		
		// inherited
		List<GroupMembership> inheritedMemberships = groupDao.getMemberships(group, GroupMembershipInheritance.inherited);
		Assert.assertTrue(inheritedMemberships.isEmpty());
	}
	

	@Test
	public void hasRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-2-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-2b-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembershipTwoWay(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.commitAndCloseSession();
		
		boolean hasRole = groupDao.hasRole(group, id, "author");
		Assert.assertTrue(hasRole);
		//negative tests
		boolean hasNotRole = groupDao.hasRole(group, id, "pilot");
		Assert.assertFalse(hasNotRole);
		boolean id2_hasNotRole = groupDao.hasRole(group, id2, "author");
		Assert.assertFalse(id2_hasNotRole);
	}
	
	@Test
	public void getMembers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembershipTwoWay(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = groupDao.getMembers(group, "author");
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void getMembersOfGroupCollection() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3a-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3a-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3a-3");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3a-4");
		Group group1 = groupDao.createGroup();
		Group group2 = groupDao.createGroup();
		groupDao.addMembershipTwoWay(group1, id1, "author");
		groupDao.addMembershipTwoWay(group1, id2, "author");
		groupDao.addMembershipTwoWay(group2, id1, "author");
		groupDao.addMembershipTwoWay(group2, id3, "author");
		groupDao.addMembershipTwoWay(group2, id4, "participant");
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = groupDao.getMembers(asList(group1, group2), "author");
		Assert.assertNotNull(members);
		Assert.assertEquals(3, members.size());
	}
	
	@Test
	public void countMembers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-4-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-5-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-6-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "pilot");
		GroupMembership membership3 = groupDao.addMembershipTwoWay(group, id3, "copilot");
		dbInstance.commit();
		
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		Assert.assertNotNull(membership3);
		dbInstance.commitAndCloseSession();
		
		int numOfMembers = groupDao.countMembers(group);
		Assert.assertEquals(3, numOfMembers);
	}
	
	@Test
	public void removeMembership() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-7-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-8-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "pilot");
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		dbInstance.commitAndCloseSession();
		
		//check
		List<GroupMembership> memberships = groupDao.getMemberships(group, "pilot");
		Assert.assertEquals(2, memberships.size());
		
		//remove
		groupDao.removeMembership(group, id1);
		dbInstance.commitAndCloseSession();
		
		//check 
		List<GroupMembership> deletedMemberships = groupDao.getMemberships(group, "pilot");
		Assert.assertEquals(1, deletedMemberships.size());
		Identity stayingMember = deletedMemberships.get(0).getIdentity();
		Assert.assertNotNull(stayingMember);
		Assert.assertEquals(id2, stayingMember);
	}
	
	@Test
	public void removeMembership_byRole() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-7-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-8-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "pilot");
		GroupMembership membership2alt = groupDao.addMembershipTwoWay(group, id2, "commander");
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		Assert.assertNotNull(membership2alt);
		dbInstance.commitAndCloseSession();
		
		//check
		List<GroupMembership> memberships = groupDao.getMemberships(group, "pilot");
		Assert.assertEquals(2, memberships.size());
		List<GroupMembership> membershipsAlt = groupDao.getMemberships(group, "commander");
		Assert.assertEquals(1, membershipsAlt.size());

		//remove
		groupDao.removeMembership(group, id2, "pilot");
		dbInstance.commitAndCloseSession();
		
		//check pilots
		List<GroupMembership> stayingMemberships = groupDao.getMemberships(group, "pilot");
		Assert.assertEquals(1, stayingMemberships.size());
		Identity stayingMember = stayingMemberships.get(0).getIdentity();
		Assert.assertNotNull(stayingMember);
		Assert.assertEquals(id1, stayingMember);
		//check commanders
		List<GroupMembership> stayingMembershipsAlt = groupDao.getMemberships(group, "commander");
		Assert.assertEquals(1, stayingMembershipsAlt.size());
		Identity stayingMemberAlt = stayingMembershipsAlt.get(0).getIdentity();
		Assert.assertNotNull(stayingMemberAlt);
		Assert.assertEquals(id2, stayingMemberAlt);
	}
	
	@Test
	public void removeMemberships_group() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-7-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-8-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "pilot");
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		dbInstance.commitAndCloseSession();
		
		//check
		List<GroupMembership> memberships = groupDao.getMemberships(group, "pilot");
		Assert.assertEquals(2, memberships.size());
		
		//remove
		groupDao.removeMemberships(group);
		dbInstance.commitAndCloseSession();
		
		//check 
		List<GroupMembership> deletedMemberships = groupDao.getMemberships(group, "pilot");
		Assert.assertTrue(deletedMemberships.isEmpty());
	}
	
	@Test
	public void removeMemberships_groupAndRole() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-12-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-13-");
		Group group = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group, id2, "copilot");
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		dbInstance.commitAndCloseSession();
		
		//check
		int numOfMembers = groupDao.countMembers(group);
		Assert.assertEquals(2, numOfMembers);
		
		//remove
		int numOfDeletedRows = groupDao.removeMemberships(group, "pilot");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, numOfDeletedRows);
		
		//check
		List<GroupMembership> deletedMemberships = groupDao.getMemberships(group, "pilot");
		Assert.assertTrue(deletedMemberships.isEmpty());
		List<GroupMembership> lastMemberships = groupDao.getMemberships(group, "copilot");
		Assert.assertEquals(1, lastMemberships.size());
	}
	
	@Test
	public void removeMemberships_identity() {
		//
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-9-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-10-");
		Group group1 = groupDao.createGroup();
		GroupMembership membership1 = groupDao.addMembershipTwoWay(group1, id1, "pilot");
		GroupMembership membership2 = groupDao.addMembershipTwoWay(group1, id2, "pilot");
		Assert.assertNotNull(membership1);
		Assert.assertNotNull(membership2);
		dbInstance.commitAndCloseSession();
		Group group2 = groupDao.createGroup();
		GroupMembership membership3 = groupDao.addMembershipTwoWay(group2, id1, "passanger");
		GroupMembership membership4 = groupDao.addMembershipTwoWay(group2, id2, "passanger");
		Assert.assertNotNull(membership3);
		Assert.assertNotNull(membership4);
		dbInstance.commitAndCloseSession();

		//check
		List<GroupMembership> memberships = groupDao.getMemberships(group1, "pilot");
		Assert.assertEquals(2, memberships.size());
		
		//remove
		groupDao.removeMemberships(id1);
		dbInstance.commitAndCloseSession();
		
		//check 
		List<GroupMembership> deletedMemberships1 = groupDao.getMemberships(group1, "pilot");
		Assert.assertEquals(1, deletedMemberships1.size());
		Assert.assertEquals(membership2, deletedMemberships1.get(0));
		
		List<GroupMembership> deletedMemberships2 = groupDao.getMemberships(group2, "passanger");
		Assert.assertEquals(1, deletedMemberships2.size());
		Assert.assertEquals(membership4, deletedMemberships2.get(0));
	}
	
	@Test
	public void addGrant() {
		Group group = groupDao.createGroup();
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, "grant-role", "read-only", resource);
		dbInstance.commitAndCloseSession();
		
		List<Grant> grants = groupDao.getGrants(group, "grant-role");
		Assert.assertNotNull(grants);
		
	}
	
	@Test
	public void getGrants_withResource() {
		Group group = groupDao.createGroup();
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, "getGrants-res", "getGrants-res-perm", resource);
		dbInstance.commitAndCloseSession();
		
		List<Group> groups = Collections.singletonList(group);
		List<Grant> grants = groupDao.getGrants(groups, resource);
		Assert.assertNotNull(grants);
		Assert.assertEquals(1, grants.size());
		Grant grant = grants.get(0);
		Assert.assertNotNull(grant);
		Assert.assertEquals(group, grant.getGroup());
		Assert.assertEquals(resource, grant.getResource());
		Assert.assertEquals("getGrants-res", grant.getRole());
		Assert.assertEquals("getGrants-res-perm", grant.getPermission());
	}
	
	@Test
	public void getGrants_withResource_withRole() {
		Group group = groupDao.createGroup();
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, "getGrants-role-1", "getGrants-role-1-perm", resource);
		groupDao.addGrant(group, "getGrants-role-2", "getGrants-role-2-perm", resource);
		dbInstance.commitAndCloseSession();
		
		List<Grant> grants = groupDao.getGrants(group, "getGrants-role-2", resource);
		Assert.assertNotNull(grants);
		Assert.assertEquals(1, grants.size());
		Grant grant = grants.get(0);
		Assert.assertNotNull(grant);
		Assert.assertEquals(group, grant.getGroup());
		Assert.assertEquals(resource, grant.getResource());
		Assert.assertEquals("getGrants-role-2", grant.getRole());
		Assert.assertEquals("getGrants-role-2-perm", grant.getPermission());
	}
	
	@Test
	public void hasGrant() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("grant-1-");
		Group group = groupDao.createGroup();
		String role = "hasGrant-role";
		groupDao.addMembershipTwoWay(group, id, role);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role, "hasGrant-perm", resource);
		dbInstance.commitAndCloseSession();
		
		boolean hasGrant = groupDao.hasGrant(id, "hasGrant-perm", resource, role);
		Assert.assertTrue(hasGrant);
	}
	
	
	@Test
	public void hasGrant_currentRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("grant-1-");
		Group group = groupDao.createGroup();
		String role1 = "hasGrant-role-1";
		groupDao.addMembershipTwoWay(group, id, role1);
		String role2 = "hasGrant-role-2";
		groupDao.addMembershipTwoWay(group, id, role2);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role1, "hasGrant-perm", resource);
		dbInstance.commitAndCloseSession();
		
		boolean hasGrantRole1 = groupDao.hasGrant(id, "hasGrant-perm", resource, role1);
		Assert.assertTrue(hasGrantRole1);
		boolean hasGrantRole2 = groupDao.hasGrant(id, "hasGrant-perm", resource, role2);
		Assert.assertFalse(hasGrantRole2);
	}
	
	@Test
	public void getPermissions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("grant-1-");
		Group group = groupDao.createGroup();
		String role = "getPermissions-role";
		groupDao.addMembershipTwoWay(group, id, role);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role, "getPermissions-perm", resource);
		dbInstance.commitAndCloseSession();
		
		List<String> permissions = groupDao.getPermissions(id, resource, role);
		Assert.assertNotNull(permissions);
		Assert.assertEquals(1, permissions.size());
		Assert.assertEquals("getPermissions-perm", permissions.get(0));
	}
	
	@Test
	public void getPermissions_complex() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("grant-1-");
		Group group = groupDao.createGroup();
		String role1 = "getPermissions-role-1";
		groupDao.addMembershipTwoWay(group, id, role1);
		String role2 = "getPermissions-role-2";
		groupDao.addMembershipTwoWay(group, id, role2);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role1, "getPermissions-perm-1-1", resource);
		groupDao.addGrant(group, role1, "getPermissions-perm-1-2", resource);
		groupDao.addGrant(group, role2, "getPermissions-perm-2-1", resource);
		groupDao.addGrant(group, role2, "getPermissions-perm-2-2", resource);
		dbInstance.commitAndCloseSession();
		
		List<String> permissions = groupDao.getPermissions(id, resource, role2);
		Assert.assertNotNull(permissions);
		Assert.assertEquals(2, permissions.size());
		Assert.assertFalse(permissions.contains("getPermissions-perm-1-1"));
		Assert.assertFalse(permissions.contains("getPermissions-perm-1-2"));
		Assert.assertTrue(permissions.contains("getPermissions-perm-2-1"));
		Assert.assertTrue(permissions.contains("getPermissions-perm-2-2"));
	}
	
	@Test
	public void addRemoveGrant() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("addremove-1-");
		Group group = groupDao.createGroup();
		String role1 = "addremove-1";
		groupDao.addMembershipTwoWay(group, id, role1);
		String role2 = "addremove-2";
		groupDao.addMembershipTwoWay(group, id, role2);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role1, "addremove-1-perm", resource);
		groupDao.addGrant(group, role2, "addremove-2-perm", resource);
		dbInstance.commitAndCloseSession();
		
		//setup check
		boolean hasPerm1 = groupDao.hasGrant(id, "addremove-1-perm", resource, role1);
		Assert.assertTrue(hasPerm1);
		boolean hasPerm2 = groupDao.hasGrant(id, "addremove-2-perm", resource, role2);
		Assert.assertTrue(hasPerm2);
		
		//remove perm 1
		groupDao.removeGrant(group, role1, "addremove-1-perm", resource);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasStillPerm1 = groupDao.hasGrant(id, "addremove-1-perm", resource, role1);
		Assert.assertFalse(hasStillPerm1);
		boolean hasStillPerm2 = groupDao.hasGrant(id, "addremove-2-perm", resource, role2);
		Assert.assertTrue(hasStillPerm2);
	}
	
	@Test
	public void addRemoveGrants() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("addremove-1-");
		Group group = groupDao.createGroup();
		String role1 = "addremove-1";
		groupDao.addMembershipTwoWay(group, id, role1);
		String role2 = "addremove-2";
		groupDao.addMembershipTwoWay(group, id, role2);
		OLATResource resource = JunitTestHelper.createRandomResource();
		groupDao.addGrant(group, role1, "addremove-1-perm", resource);
		groupDao.addGrant(group, role1, "addremove-11-perm", resource);
		groupDao.addGrant(group, role2, "addremove-2-perm", resource);
		groupDao.addGrant(group, role2, "addremove-22-perm", resource);
		dbInstance.commitAndCloseSession();
		
		//setup check
		boolean hasPerm1 = groupDao.hasGrant(id, "addremove-1-perm", resource, role1);
		Assert.assertTrue(hasPerm1);
		boolean hasPerm11 = groupDao.hasGrant(id, "addremove-11-perm", resource, role1);
		Assert.assertTrue(hasPerm11);
		boolean hasPerm2 = groupDao.hasGrant(id, "addremove-2-perm", resource, role2);
		Assert.assertTrue(hasPerm2);
		boolean hasPerm22 = groupDao.hasGrant(id, "addremove-22-perm", resource, role2);
		Assert.assertTrue(hasPerm22);
		
		//remove perm 1
		groupDao.removeGrants(group, role1, resource);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasStillPerm1 = groupDao.hasGrant(id, "addremove-1-perm", resource, role1);
		Assert.assertFalse(hasStillPerm1);
		boolean hasStillPerm11 = groupDao.hasGrant(id, "addremove-11-perm", resource, role1);
		Assert.assertFalse(hasStillPerm11);
		boolean hasStillPerm2 = groupDao.hasGrant(id, "addremove-2-perm", resource, role2);
		Assert.assertTrue(hasStillPerm2);
		boolean hasStillPerm22 = groupDao.hasGrant(id, "addremove-22-perm", resource, role2);
		Assert.assertTrue(hasStillPerm22);
	}
}