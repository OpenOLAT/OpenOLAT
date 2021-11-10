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
package org.olat.group.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupRelationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private GroupDAO groupDao;
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
		Assert.assertNotNull(businessGroupRelationDao);
	}
	
	@Test
	public void addRelation() {
		//create a relation
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group, resource);
		
		dbInstance.commitAndCloseSession();
		
		//check
		List<RepositoryEntry> resources = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		Assert.assertNotNull(resources);
		Assert.assertEquals(1, resources.size());
		Assert.assertTrue(resources.contains(resource));
		
		int count = businessGroupRelationDao.countResources(group);
		Assert.assertEquals(1, count);
	}
	
	@Test
	public void addRelation_v2() {
		//create a relation
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-v2-");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		Group group = ((BusinessGroupImpl)businessGroup).getBaseGroup();
		groupDao.addMembershipTwoWay(group, coach, "coach");
		dbInstance.commitAndCloseSession();
		
		List<String> roles = businessGroupRelationDao.getRoles(coach, businessGroup);
		Assert.assertNotNull(roles);
		Assert.assertEquals(1, roles.size());

		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRole(coach, businessGroup, "participant");
		dbInstance.commitAndCloseSession();
		
		List<String> multiRoles = businessGroupRelationDao.getRoles(coach, businessGroup);
		Assert.assertNotNull(multiRoles);
		Assert.assertEquals(2, multiRoles.size());
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.removeRole(coach, businessGroup, "participant");
		List<String> reducedRoles = businessGroupRelationDao.getRoles(coach, businessGroup);
		Assert.assertNotNull(reducedRoles);
		Assert.assertEquals(1, reducedRoles.size());
		Assert.assertEquals("coach", reducedRoles.get(0));
		dbInstance.commitAndCloseSession();
		
		int numOfCoaches = businessGroupRelationDao.countRoles(businessGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, numOfCoaches);
	}
	
	@Test
	public void addRelations() {
		//create a relation
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-bg-1", "rel-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group, resource1);
		businessGroupRelationDao.addRelationToResource(group, resource2);
		businessGroupRelationDao.addRelationToResource(group, resource3);
		businessGroupRelationDao.addRelationToResource(group, resource3);
		
		dbInstance.commitAndCloseSession();
		
		//check
		List<RepositoryEntry> resources = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		Assert.assertNotNull(resources);
		Assert.assertEquals(3, resources.size());
		Assert.assertTrue(resources.contains(resource1));
		Assert.assertTrue(resources.contains(resource2));
		Assert.assertTrue(resources.contains(resource3));
	}
	
	@Test
	public void addGroupsAndRelations() {
		//create a relation
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bg-1", "rel-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "rel-bg-2", "rel-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource2);
		businessGroupRelationDao.addRelationToResource(group2, resource3);
		
		
		dbInstance.commitAndCloseSession();
		
		//check group1
		List<RepositoryEntry> resources1 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group1), 0, -1);
		Assert.assertNotNull(resources1);
		Assert.assertEquals(2, resources1.size());
		Assert.assertTrue(resources1.contains(resource1));
		Assert.assertTrue(resources1.contains(resource2));
		Assert.assertFalse(resources1.contains(resource3));
		
		//check group2
		List<RepositoryEntry> resources2 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group2), 0, -1);
		Assert.assertNotNull(resources2);
		Assert.assertEquals(2, resources2.size());
		Assert.assertFalse(resources2.contains(resource1));
		Assert.assertTrue(resources2.contains(resource2));
		Assert.assertTrue(resources2.contains(resource3));
	}

	@Test
	public void deleteRelation() {
		//create relations
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bg-1", "rel-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "rel-bg-2", "rel-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource2);
		businessGroupRelationDao.addRelationToResource(group2, resource3);
		dbInstance.commitAndCloseSession();
		
		//delete relation
		businessGroupRelationDao.deleteRelation(group1, resource1);
		dbInstance.commitAndCloseSession();
		
		//check group1
		List<RepositoryEntry> resources1 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group1), 0, -1);
		Assert.assertNotNull(resources1);
		Assert.assertEquals(1, resources1.size());
		Assert.assertFalse(resources1.contains(resource1));
		Assert.assertTrue(resources1.contains(resource2));
		Assert.assertFalse(resources1.contains(resource3));
		
		//check group2
		List<RepositoryEntry> resources2 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group2), 0, -1);
		Assert.assertNotNull(resources2);
		Assert.assertEquals(2, resources2.size());
		Assert.assertFalse(resources2.contains(resource1));
		Assert.assertTrue(resources2.contains(resource2));
		Assert.assertTrue(resources2.contains(resource3));
	}
	
	@Test
	public void deleteRelations() {
		//create relations
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bg-1", "rel-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "rel-bg-2", "rel-bg-2-desc", BusinessGroup.BUSINESS_TYPE,-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource2);
		businessGroupRelationDao.addRelationToResource(group2, resource3);
		dbInstance.commitAndCloseSession();
		
		//delete relation
		businessGroupRelationDao.deleteRelationsToRepositoryEntry(group1);
		dbInstance.commitAndCloseSession();
		
		//check group1
		List<RepositoryEntry> resources1 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group1), 0, -1);
		Assert.assertNotNull(resources1);
		Assert.assertEquals(0, resources1.size());
		
		//check group2
		List<RepositoryEntry> resources2 = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group2), 0, -1);
		Assert.assertNotNull(resources2);
		Assert.assertEquals(2, resources2.size());
		Assert.assertFalse(resources2.contains(resource1));
		Assert.assertTrue(resources2.contains(resource2));
		Assert.assertTrue(resources2.contains(resource3));
	}
	
	@Test
	public void isIdentityInBusinessGroupNameOwner() {
		//create relations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bgis-1", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		businessGroupRelationDao.addRole(id, group1, GroupRoles.coach.name());

		dbInstance.commitAndCloseSession();
		
		//check
		boolean test1 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource1); 
		Assert.assertTrue(test1);
		//name doesn't exist 
		boolean test2 = businessGroupRelationDao.isIdentityInBusinessGroup(id, 1l, true, true, resource1); 
		Assert.assertFalse(test2);
		//wrong resource
		boolean test4 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource3); 
		Assert.assertFalse(test4);
		//check null
		boolean test5 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource1); 
		Assert.assertTrue(test5);
	}
	
	@Test
	public void isIdentityInBusinessGroupKeyOwner() {
		//create relations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bgiskey-1", "rel-bgiskey-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		businessGroupRelationDao.addRole(id, group1, GroupRoles.coach.name());

		dbInstance.commitAndCloseSession();
		
		//check
		boolean test1 = businessGroupRelationDao.isIdentityInBusinessGroup(id, group1.getKey(), true, true, resource1); 
		Assert.assertTrue(test1);
		//key doesn't exist 
		boolean test2 = businessGroupRelationDao.isIdentityInBusinessGroup(id, 1l, true, true, resource1); 
		Assert.assertFalse(test2);
		boolean test3 = businessGroupRelationDao.isIdentityInBusinessGroup(id, group1.getKey(),true, true,  resource3); 
		Assert.assertFalse(test3);
		//check null
		boolean test5 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource1); 
		Assert.assertTrue(test5);
	}
	
	@Test
	public void isIdentityInBusinessGroupNameParticipant() {
		//create relations
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("named-1");
		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bg-part-1", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		businessGroupRelationDao.addRole(id, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		boolean test1 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource1); 
		Assert.assertTrue(test1);
		//wrong resource
		boolean test4 = businessGroupRelationDao.isIdentityInBusinessGroup(id, null, true, true, resource3); 
		Assert.assertFalse(test4);
	}

	@Test
	public void findBusinessGroupsWithWaitingListAttendedBy() {
		//prepare 2 resources + 3 groups and 4 ids
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-3");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-4");

		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "rel-bg-part-one", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id4, group1, GroupRoles.waiting.name());
		
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "rel-bg-part-two", "rel-bgis-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource1);
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.waiting.name());
		
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "rel-bg-part-three", "rel-bgis-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group3, resource2);
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.waiting.name());
		
		dbInstance.commitAndCloseSession();
		
		//check id 1 is in resource 1
		List<BusinessGroup> groups1_1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1, resource1);
		Assert.assertNotNull(groups1_1);
		Assert.assertEquals(1, groups1_1.size());
		Assert.assertTrue(groups1_1.contains(group1));
		//check if id 1 is in resource 2
		List<BusinessGroup> groups1_2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1, resource2);
		Assert.assertNotNull(groups1_2);
		Assert.assertEquals(1, groups1_2.size());
		Assert.assertTrue(groups1_2.contains(group1));

		//check id 2 is in resource 1
		List<BusinessGroup> groups2_1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2, resource1);
		Assert.assertNotNull(groups2_1);
		Assert.assertEquals(1, groups2_1.size());
		Assert.assertTrue(groups2_1.contains(group2));
		//check if id 2 is in resource 2
		List<BusinessGroup> groups2_2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2, resource2);
		Assert.assertNotNull(groups2_2);
		Assert.assertEquals(0, groups2_2.size());

		//check id 4 is in resource 1
		List<BusinessGroup> groups4_1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id4, resource1);
		Assert.assertNotNull(groups4_1);
		Assert.assertEquals(2, groups4_1.size());
		Assert.assertTrue(groups4_1.contains(group1));
		Assert.assertTrue(groups4_1.contains(group2));
		//check if id 2 is in resource 2
		List<BusinessGroup> groups4_2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id4, resource2);
		Assert.assertNotNull(groups4_2);
		Assert.assertEquals(1, groups4_2.size());
		Assert.assertTrue(groups4_2.contains(group1));	
	}

	@Test
	public void findAndCountBusinessGroups() {
		//prepare 2 resources + 3 groups with 3 owners
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("wait-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("wait-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("wait-3-" + UUID.randomUUID().toString());

		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "rel-bg-part-one", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRelationToResource(group1, resource2);
		
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "rel-bg-part-two", "rel-bgis-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource1);
		
		BusinessGroup group3 = businessGroupDao.createAndPersist(id3, "rel-bg-part-three", "rel-bgis-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group3, resource2);
		
		dbInstance.commitAndCloseSession();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		//check resource 1
		int count1_1 = businessGroupDao.countBusinessGroups(params, resource1);
		Assert.assertEquals(2, count1_1);
		List<BusinessGroup> groups1_1 = businessGroupDao.findBusinessGroups(params, resource1, 0, -1);
		Assert.assertNotNull(groups1_1);
		Assert.assertEquals(2, groups1_1.size());
		Assert.assertTrue(groups1_1.contains(group1));
		Assert.assertTrue(groups1_1.contains(group2));

		//check owner 1 + resource 1
		SearchBusinessGroupParams paramsRestricted = new SearchBusinessGroupParams(id1, true, true);
		int count3_1 = businessGroupDao.countBusinessGroups(paramsRestricted, resource1);
		Assert.assertEquals(1, count3_1);
		List<BusinessGroup> groups3_1 = businessGroupDao.findBusinessGroups(paramsRestricted, resource1, 0, -1);
		Assert.assertNotNull(groups3_1);
		Assert.assertEquals(1, groups3_1.size());
		Assert.assertEquals(group1, groups3_1.get(0));
	}
	
	@Test
	public void countAndFindMembers() {
		//prepare 2 resources + 3 groups with 3 owners
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsUser("own-1-" + UUID.randomUUID().toString());
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsUser("own-2-" + UUID.randomUUID().toString());
		Identity owner3 = JunitTestHelper.createAndPersistIdentityAsUser("own-3-" + UUID.randomUUID().toString());

		Identity part1 = JunitTestHelper.createAndPersistIdentityAsUser("part-1-" + UUID.randomUUID().toString());
		Identity part2 = JunitTestHelper.createAndPersistIdentityAsUser("part-2-" + UUID.randomUUID().toString());
		Identity part3 = JunitTestHelper.createAndPersistIdentityAsUser("part-3-" + UUID.randomUUID().toString());
		Identity part4 = JunitTestHelper.createAndPersistIdentityAsUser("part-4-" + UUID.randomUUID().toString());

		RepositoryEntry resource1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner1, "rel-bg-part-one", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource1);
		businessGroupRelationDao.addRole(part1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part2, group1, GroupRoles.participant.name());

		BusinessGroup group2 = businessGroupDao.createAndPersist(owner2, "rel-bg-part-two", "rel-bgis-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource1);
		businessGroupRelationDao.addRole(owner1, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part3, group2, GroupRoles.participant.name());
		
		BusinessGroup group3 = businessGroupDao.createAndPersist(owner3, "rel-bg-part-three", "rel-bgis-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group3, resource2);
		businessGroupRelationDao.addRole(part4, group3, GroupRoles.participant.name());

		dbInstance.commitAndCloseSession();
		
		//resource 1 owners and participants
		List<Identity> partAndOwners1 = businessGroupRelationDao.getMembersOf(resource1, true, true);
		Assert.assertNotNull(partAndOwners1);
		//Assert.assertEquals(5, partAndOwners1.size());
		Assert.assertTrue(partAndOwners1.contains(owner1));
		Assert.assertTrue(partAndOwners1.contains(owner2));
		Assert.assertTrue(partAndOwners1.contains(part1));
		Assert.assertTrue(partAndOwners1.contains(part2));
		Assert.assertTrue(partAndOwners1.contains(part3));

		//resource 1 owners
		List<Identity> owners2 = businessGroupRelationDao.getMembersOf(resource1, true, false);
		Assert.assertNotNull(owners2);
		Assert.assertEquals(2, owners2.size());
		Assert.assertTrue(owners2.contains(owner1));
		Assert.assertTrue(owners2.contains(owner2));
		
		//resource 1 participants
		List<Identity> participant3 = businessGroupRelationDao.getMembersOf(resource1, false, true);
		Assert.assertNotNull(participant3);
		Assert.assertEquals(4, participant3.size());
		Assert.assertTrue(participant3.contains(owner1));
		Assert.assertTrue(participant3.contains(part1));
		Assert.assertTrue(participant3.contains(part2));
		Assert.assertTrue(participant3.contains(part3));

		//resource 2 owners and participants
		List<Identity> partAndOwners4 = businessGroupRelationDao.getMembersOf(resource2, true, true);
		Assert.assertNotNull(partAndOwners4);
		Assert.assertEquals(2, partAndOwners4.size());
		Assert.assertTrue(partAndOwners4.contains(owner3));
		Assert.assertTrue(partAndOwners4.contains(part4));

		//resource 1 owners
		List<Identity> owners5 = businessGroupRelationDao.getMembersOf(resource2, true, false);
		Assert.assertNotNull(owners5);
		Assert.assertEquals(1, owners5.size());
		Assert.assertTrue(owners5.contains(owner3));
		
		//resource 1 participants
		List<Identity> participant6 = businessGroupRelationDao.getMembersOf(resource2, false, true);
		Assert.assertNotNull(participant6);
		Assert.assertEquals(1, participant6.size());
		Assert.assertTrue(participant6.contains(part4));
	}
	
	@Test
	public void getFirstEnrollmentDate() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("rel-user-20");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commit();
		
		//no participant enrolled
		Date noEnrollmentDate = businessGroupRelationDao.getFirstEnrollmentDate(businessGroup, GroupRole.participant.name());
		Assert.assertNull(noEnrollmentDate);
		
		//add participant
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rel-user-21");
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		
		// there is a participant businessGroupRelationDao
		Date enrollmentDate = businessGroupRelationDao.getFirstEnrollmentDate(businessGroup, GroupRole.participant.name());
		Assert.assertNotNull(enrollmentDate);
	}
	
	@Test
	public void filterMembership() {
		//create 3 groups with owner and participants
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("own-1-" + UUID.randomUUID().toString());
		Identity part1 = JunitTestHelper.createAndPersistIdentityAsUser("part-1-" + UUID.randomUUID().toString());
		Identity part2 = JunitTestHelper.createAndPersistIdentityAsUser("part-2-" + UUID.randomUUID().toString());

		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "filter-membership-one", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRole(part1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part2, group1, GroupRoles.participant.name());

		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "filter-membership-two", "rel-bgis-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRole(owner, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part2, group2, GroupRoles.participant.name());
		
		BusinessGroup group3 = businessGroupDao.createAndPersist(owner, "filter-membership-three", "rel-bgis-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();

		List<BusinessGroup> groups = new ArrayList<>(3);
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		
		//filter owner as owner
		List<BusinessGroup> ownerAsOwnerGroups = businessGroupRelationDao.filterMembership(groups, owner, GroupRoles.coach.name());
		Assert.assertNotNull(ownerAsOwnerGroups);
		Assert.assertEquals(3, ownerAsOwnerGroups.size());
		Assert.assertTrue(ownerAsOwnerGroups.contains(group1));
		Assert.assertTrue(ownerAsOwnerGroups.contains(group2));
		Assert.assertTrue(ownerAsOwnerGroups.contains(group3));
		
		//filter owner as participant
		List<BusinessGroup> ownerAsParticipantGroups = businessGroupRelationDao.filterMembership(groups, owner, GroupRoles.participant.name());
		Assert.assertNotNull(ownerAsParticipantGroups);
		Assert.assertEquals(1, ownerAsParticipantGroups.size());
		Assert.assertTrue(ownerAsParticipantGroups.contains(group2));
		
		//filter part1 as owner
		List<BusinessGroup> part1AsOwnerGroups = businessGroupRelationDao.filterMembership(groups, part1, GroupRoles.coach.name());
		Assert.assertNotNull(part1AsOwnerGroups);
		Assert.assertEquals(0, part1AsOwnerGroups.size());

		//filter part1 as participant
		List<BusinessGroup> part1AsParticipantGroups = businessGroupRelationDao.filterMembership(groups, part1, GroupRoles.participant.name());
		Assert.assertNotNull(part1AsParticipantGroups);
		Assert.assertEquals(1, part1AsParticipantGroups.size());
		Assert.assertTrue(part1AsParticipantGroups.contains(group1));
		
		//filter part2
		List<BusinessGroup> part2Groups = businessGroupRelationDao.filterMembership(groups, part2);
		Assert.assertNotNull(part2Groups);
		Assert.assertEquals(2, part2Groups.size());
		Assert.assertTrue(part2Groups.contains(group1));
		Assert.assertTrue(part2Groups.contains(group2));
		
		//try stupid things
		List<BusinessGroup>  emptyGroups = businessGroupRelationDao.filterMembership(Collections.<BusinessGroup>emptyList(), part2);
		Assert.assertNotNull(emptyGroups);
		Assert.assertEquals(0, emptyGroups.size());
		
		List<BusinessGroup>  noIdentityGroups = businessGroupRelationDao.filterMembership(groups, null);
		Assert.assertNotNull(noIdentityGroups);
		Assert.assertEquals(0, noIdentityGroups.size());
	}
	
	@Test
	public void countAuthors() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("auth-" + UUID.randomUUID().toString());
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("not-auth");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRole(author, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(test, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		int numOfAuthors = businessGroupRelationDao.countAuthors(group);
		Assert.assertEquals(1, numOfAuthors);
	}
	
	@Test
	public void countRoles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("auth-" + UUID.randomUUID().toString());
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("not-auth");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRole(author, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(test, group, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		int numOfCoachs = businessGroupRelationDao.countRoles(group, GroupRoles.coach.name());
		Assert.assertEquals(2, numOfCoachs);
		int numOfParticipants = businessGroupRelationDao.countRoles(group, GroupRoles.participant.name());
		Assert.assertEquals(0, numOfParticipants);
	}
	
	@Test
	public void countEnrollment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("auth-" + UUID.randomUUID().toString());
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("not-auth");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("not-auth");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		businessGroupRelationDao.addRole(author, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		int numOfParticipants = businessGroupRelationDao.countEnrollment(group);
		Assert.assertEquals(2, numOfParticipants);
	}
	
	@Test
	public void loadForUpdate() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup groupToUpdate = businessGroupDao.loadForUpdate(group.getKey());
		Assert.assertNotNull(groupToUpdate);
		Assert.assertEquals(group, groupToUpdate);
	}
	
	@Test
	public void findRelationToRepositoryEntries() {
		//create 3 entries and 1 group
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRelationToResource(group, re1);
		businessGroupRelationDao.addRelationToResource(group, re2);
		businessGroupRelationDao.addRelationToResource(group, re3);
		dbInstance.commitAndCloseSession();
		
		//check with empty list of groups
		List<BGRepositoryEntryRelation> emptyRelations = businessGroupRelationDao.findRelationToRepositoryEntries(Collections.<Long>emptyList(), 0, -1); 
		Assert.assertNotNull(emptyRelations);
		Assert.assertEquals(0, emptyRelations.size());
		
		//check with the group
		List<BGRepositoryEntryRelation> relations = businessGroupRelationDao.findRelationToRepositoryEntries(Collections.singletonList(group.getKey()), 0, -1); 
		Assert.assertNotNull(relations);
		Assert.assertEquals(3, relations.size());
		
		int count = 0;
		for(BGRepositoryEntryRelation relation:relations) {
			if(relation.getRepositoryEntryKey().equals(re1.getKey())
					|| relation.getRepositoryEntryKey().equals(re2.getKey())
					|| relation.getRepositoryEntryKey().equals(re3.getKey())) {
				count++;
			}
		}
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void getRepositoryEntryKeys() {
		//create 2 entries and 1 group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRelationToResource(group, re1);
		businessGroupRelationDao.addRelationToResource(group, re2);
		dbInstance.commitAndCloseSession();
		
		List<Long> repositoryEntryKeys = businessGroupRelationDao.getRepositoryEntryKeys(group);
		Assert.assertNotNull(repositoryEntryKeys);
		Assert.assertEquals(2, repositoryEntryKeys.size());
		Assert.assertTrue(repositoryEntryKeys.contains(re1.getKey()));
		Assert.assertTrue(repositoryEntryKeys.contains(re2.getKey()));
	}
	
	@Test
	public void findRepositoryEntries() {
		//create 3 entries and 1 group
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRelationToResource(group, re1);
		businessGroupRelationDao.addRelationToResource(group, re2);
		businessGroupRelationDao.addRelationToResource(group, re3);
		dbInstance.commitAndCloseSession();
		
		//check with empty list of groups
		List<RepositoryEntry> emptyRelations = businessGroupRelationDao.findRepositoryEntries(Collections.<BusinessGroup>emptyList(), 0, -1); 
		Assert.assertNotNull(emptyRelations);
		Assert.assertEquals(0, emptyRelations.size());
		
		List<RepositoryEntry> repoEntries = businessGroupRelationDao.findRepositoryEntries(Collections.singletonList(group), 0, -1); 
		Assert.assertNotNull(repoEntries);
		Assert.assertEquals(3, repoEntries.size());
		
		int count = 0;
		for(RepositoryEntry repoEntry:repoEntries) {
			if(repoEntry.getKey().equals(re1.getKey())
					|| repoEntry.getKey().equals(re2.getKey())
					|| repoEntry.getKey().equals(re3.getKey())) {
				count++;
			}
		}
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void findShortRepositoryEntries() {
		//create 3 entries and 1 group
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRelationToResource(group, re1);
		businessGroupRelationDao.addRelationToResource(group, re2);
		businessGroupRelationDao.addRelationToResource(group, re3);
		dbInstance.commitAndCloseSession();
		
		//check with empty list of groups
		List<RepositoryEntryShort> emptyRelations = businessGroupRelationDao.findShortRepositoryEntries(Collections.<BusinessGroupShort>emptyList(), 0, -1); 
		Assert.assertNotNull(emptyRelations);
		Assert.assertEquals(0, emptyRelations.size());
		
		List<RepositoryEntryShort> repoEntries = businessGroupRelationDao.findShortRepositoryEntries(Collections.<BusinessGroupShort>singletonList(group), 0, -1); 
		Assert.assertNotNull(repoEntries);
		Assert.assertEquals(3, repoEntries.size());
		
		int count = 0;
		for(RepositoryEntryShort repoEntry:repoEntries) {
			if(repoEntry.getKey().equals(re1.getKey())
					|| repoEntry.getKey().equals(re2.getKey())
					|| repoEntry.getKey().equals(re3.getKey())) {
				count++;
			}
		}
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void toGroupKeys() {
		//create a resource with 2 groups
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, resource);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, resource);
		dbInstance.commitAndCloseSession();
		
		List<Long> groupKeys = businessGroupRelationDao.toGroupKeys("to-group-1", resource);
		Assert.assertNotNull(groupKeys);
		Assert.assertEquals(1, groupKeys.size());
		Assert.assertTrue(groupKeys.contains(group1.getKey()));
	}
	
	@Test
	public void countResourcesOfBusinessGroups() {
		//create 3 entries and 1 group
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry re3 = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-repo", "rel-repo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		businessGroupRelationDao.addRelationToResource(group, re1);
		businessGroupRelationDao.addRelationToResource(group, re2);
		businessGroupRelationDao.addRelationToResource(group, re3);
		dbInstance.commitAndCloseSession();
		
		//check with empty list of groups
		boolean numOfResources1 = businessGroupRelationDao.hasResources(Collections.<BusinessGroup>emptyList()); 
		Assert.assertFalse(numOfResources1);
		
		//check with the group
		boolean numOfResources2 = businessGroupRelationDao.hasResources(Collections.singletonList(group)); 
		Assert.assertTrue(numOfResources2);
	}
	
	@Test
	public void getRoles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-1");
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check
		List<BusinessGroup> groups = new ArrayList<>(2);
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		
		List<String> roles = businessGroupRelationDao.getRoles(id, groups);
		Assert.assertNotNull(roles);
		Assert.assertEquals(3, roles.size());
		Assert.assertTrue(roles.contains(GroupRoles.participant.name()));
		Assert.assertTrue(roles.contains(GroupRoles.coach.name()));
	}
	
	@Test
	public void getDuplicateMemberships() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("wait-1");
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//id1 is a duplicate
		List<BusinessGroup> groups = new ArrayList<>(2);
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		List<IdentityRef> duplicates = businessGroupRelationDao.getDuplicateMemberships(groups);
		Assert.assertNotNull(duplicates);
		Assert.assertEquals(1, duplicates.size());
		Assert.assertEquals(id1.getKey(), duplicates.get(0).getKey());
	}
	
	@Test
	public void getMembers_all() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-3");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();
		
		// load all 
		List<Identity> members = businessGroupRelationDao.getMembers(group);
		Assert.assertNotNull(members);
		Assert.assertEquals(3, members.size());
		Assert.assertTrue(members.contains(id1));
		Assert.assertTrue(members.contains(id2));
		Assert.assertTrue(members.contains(id3));
	}
	
	@Test
	public void getMembers_roles() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-3");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();
		
		// load coaches 
		List<Identity> coaches = businessGroupRelationDao.getMembers(group, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(1, coaches.size());
		Assert.assertTrue(coaches.contains(id1));
		
		// load coaches and participants
		List<Identity> members = businessGroupRelationDao.getMembers(group, GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(2, members.size());
		Assert.assertTrue(members.contains(id1));
		Assert.assertTrue(members.contains(id2));
	}
	
	@Test
	public void getMemberKeys_roles() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("m-3");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();
		
		// load coaches 
		List<Long> coacheKeys = businessGroupRelationDao.getMemberKeys(group, GroupRoles.coach.name());
		Assert.assertNotNull(coacheKeys);
		Assert.assertEquals(1, coacheKeys.size());
		Assert.assertTrue(coacheKeys.contains(id1.getKey()));
		
		// load coaches and participants
		List<Long> memberKeys = businessGroupRelationDao.getMemberKeys(group, GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertNotNull(memberKeys);
		Assert.assertEquals(2, memberKeys.size());
		Assert.assertTrue(memberKeys.contains(id1.getKey()));
		Assert.assertTrue(memberKeys.contains(id2.getKey()));
	}
	
	@Test
	public void getMembersOrderByDate() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//load the identities
		List<Identity> ids = businessGroupRelationDao.getMembersOrderByDate(group, GroupRoles.participant.name());
		Assert.assertNotNull(ids);
		Assert.assertEquals(3, ids.size());
		Assert.assertTrue(ids.contains(id1));
		Assert.assertTrue(ids.contains(id2));
		Assert.assertTrue(ids.contains(id3));
	}
	
	@Test
	public void getMemberKeysOrderByDate() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//load the identities
		List<Long> ids = businessGroupRelationDao.getMemberKeysOrderByDate(group, GroupRoles.participant.name());
		Assert.assertNotNull(ids);
		Assert.assertEquals(3, ids.size());
		Assert.assertTrue(ids.contains(id1.getKey()));
		Assert.assertTrue(ids.contains(id2.getKey()));
		Assert.assertTrue(ids.contains(id3.getKey()));
	}
	
	@Test
	public void getMemberKeys() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-3");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-4");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-5");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-2", "to-group-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//load the identities
		List<BusinessGroupRef> businessGroupRefs = Collections.singletonList(group);
		List<Long> ids = businessGroupRelationDao.getMemberKeys(businessGroupRefs, GroupRoles.participant.name());
		Assert.assertNotNull(ids);
		Assert.assertEquals(3, ids.size());
		Assert.assertTrue(ids.contains(id1.getKey()));
		Assert.assertTrue(ids.contains(id2.getKey()));
		Assert.assertTrue(ids.contains(id3.getKey()));
	}
	
	@Test
	public void getMembers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-6");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-7");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ordered-8");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-3", "to-group-3-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//load the identities
		List<BusinessGroupRef> businessGroupRefs = Collections.singletonList(group);
		List<Identity> ids = businessGroupRelationDao.getMembers(businessGroupRefs, GroupRoles.participant.name());
		Assert.assertNotNull(ids);
		Assert.assertEquals(3, ids.size());
		Assert.assertTrue(ids.contains(id1));
		Assert.assertTrue(ids.contains(id2));
		Assert.assertTrue(ids.contains(id3));
	}
	
	@Test
	public void getIdentitiesWithRole() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-1");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "to-group-1", "to-group-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//load the identities
		List<Identity> coaches = businessGroupRelationDao.getIdentitiesWithRole(GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertTrue(coaches.contains(id1));
		Assert.assertTrue(coaches.contains(id2));
		Assert.assertFalse(coaches.contains(id3));
	}
	
	
	
}