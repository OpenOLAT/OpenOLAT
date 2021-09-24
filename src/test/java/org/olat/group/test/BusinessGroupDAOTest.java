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
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupDao);
	}

	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commit();

		Assert.assertNotNull(group);
		Assert.assertNull(group.getMinParticipants());
		Assert.assertNull(group.getMaxParticipants());
		Assert.assertNotNull(group.getLastUsage());
		Assert.assertNotNull(group.getCreationDate());
		Assert.assertNotNull(group.getLastModified());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdao", group.getName());
		Assert.assertEquals("gdao-desc", group.getDescription());
		Assert.assertFalse(group.getWaitingListEnabled());
		Assert.assertFalse(group.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroupStandard() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdbo", "gdbo-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());

		Assert.assertNotNull(reloadedGroup);
		Assert.assertNull(reloadedGroup.getMinParticipants());
		Assert.assertNull(reloadedGroup.getMaxParticipants());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertNotNull(group.getResource());
		Assert.assertEquals("gdbo", reloadedGroup.getName());
		Assert.assertEquals("gdbo-desc", reloadedGroup.getDescription());
		Assert.assertFalse(reloadedGroup.getWaitingListEnabled());
		Assert.assertFalse(reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroup() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check the saved values
		Assert.assertNotNull(reloadedGroup);
		Assert.assertNotNull(reloadedGroup.getMinParticipants());
		Assert.assertNotNull(reloadedGroup.getMaxParticipants());
		Assert.assertEquals(0, reloadedGroup.getMinParticipants().intValue());
		Assert.assertEquals(10, reloadedGroup.getMaxParticipants().intValue());
		Assert.assertNotNull(reloadedGroup.getLastUsage());
		Assert.assertNotNull(reloadedGroup.getCreationDate());
		Assert.assertNotNull(reloadedGroup.getLastModified());
		Assert.assertEquals("gdco", reloadedGroup.getName());
		Assert.assertEquals("gdco-desc", reloadedGroup.getDescription());
		Assert.assertTrue(reloadedGroup.getWaitingListEnabled());
		Assert.assertTrue(reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void loadBusinessGroup_fetch() {
		//create business group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gd-fetch", "gd-fetch-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		dbInstance.commitAndCloseSession();

		//check lazy
		Group baseGroup = reloadedGroup.getBaseGroup();
		Assert.assertNotNull(baseGroup);
		Assert.assertNotNull(baseGroup.getKey());
		OLATResource resource = reloadedGroup.getResource();
		Assert.assertNotNull(resource);
		Assert.assertNotNull(resource.getKey());
	}

	@Test
	public void loadBusinessGroup_forUpdate() {
		//create a group
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdco", "gdco-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//load an lock
		BusinessGroup groupForUpdate = businessGroupDao.loadForUpdate(group.getKey());
		Assert.assertNotNull(groupForUpdate);
		Assert.assertEquals(group, groupForUpdate);
		dbInstance.commit();//release lock
	}

	@Test
	public void loadBusinessGroup_forUpdate_notFound() {
		//load and lock an inexistent group
		BusinessGroup groupForUpdate = businessGroupDao.loadForUpdate(Long.valueOf(0l));
		Assert.assertNull(groupForUpdate);
		dbInstance.commit();//release lock
	}

	@Test
	public void loadBusinessGroupWithOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gddo", "gddo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		//check if the owner is in the owner security group
		Assert.assertNotNull(reloadedGroup);
		boolean isOwner = businessGroupRelationDao.hasRole(owner, reloadedGroup, GroupRoles.coach.name());
		Assert.assertTrue(isOwner);
	}

	@Test
	public void loadBusinessGroupsByIds() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-2-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdeo", "gdeo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdfo", "gdfo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//check if the method is robust against empty list fo keys
		List<BusinessGroup> groups1 = businessGroupDao.load(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());

		//check load 1 group
		List<BusinessGroup> groups2 = businessGroupDao.load(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1, groups2.get(0));

		//check load 2 groups
		List<Long> groupKeys = new ArrayList<>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroup> groups3 = businessGroupDao.load(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		Assert.assertTrue(groups3.contains(group1));
		Assert.assertTrue(groups3.contains(group2));
	}

	@Test
	public void loadShortBusinessGroupsByKeys() {
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "shorty-1", "shorty-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "shorty-2", "shorty-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//check if the method is robust against empty list fo keys
		List<BusinessGroupShort> groups1 = businessGroupDao.loadShort(Collections.<Long>emptyList());
		Assert.assertNotNull(groups1);
		Assert.assertEquals(0, groups1.size());

		//check load 1 group
		List<BusinessGroupShort> groups2 = businessGroupDao.loadShort(Collections.singletonList(group1.getKey()));
		Assert.assertNotNull(groups2);
		Assert.assertEquals(1, groups2.size());
		Assert.assertEquals(group1.getKey(), groups2.get(0).getKey());
		Assert.assertEquals(group1.getName(), groups2.get(0).getName());

		//check load 2 groups
		List<Long> groupKeys = new ArrayList<>(2);
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		List<BusinessGroupShort> groups3 = businessGroupDao.loadShort(groupKeys);
		Assert.assertNotNull(groups3);
		Assert.assertEquals(2, groups3.size());
		List<Long> groupShortKeys3 = new ArrayList<>(3);
		for(BusinessGroupShort group:groups3) {
			groupShortKeys3.add(group.getKey());
		}
		Assert.assertTrue(groupShortKeys3.contains(group1.getKey()));
		Assert.assertTrue(groupShortKeys3.contains(group2.getKey()));
	}

	@Test
	public void loadDescription() {
		String description = "My desc " + UUID.randomUUID();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "load descr", description, BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		String loadDescription = businessGroupDao.loadDescription(group.getKey());
		Assert.assertNotNull(loadDescription);
		Assert.assertEquals(description, loadDescription);
	}

	@Test
	public void loadAllBusinessGroups() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(owner, "gdgo", "gdgo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//load all business groups
		List<BusinessGroup> allGroups = businessGroupDao.loadAll();
		Assert.assertNotNull(allGroups);
		Assert.assertTrue(allGroups.size() >= 2);
		Assert.assertTrue(allGroups.contains(group1));
		Assert.assertTrue(allGroups.contains(group2));
	}

	@Test
	public void mergeBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdho", "gdho-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//delete a business group
		group.setAutoCloseRanksEnabled(false);
		group.setName("gdho-2");

		//merge business group
		BusinessGroup mergedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(group, mergedGroup);
		Assert.assertEquals("gdho-2", mergedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, mergedGroup.getAutoCloseRanksEnabled());

		dbInstance.commitAndCloseSession();

		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdho-2", reloadedGroup.getName());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getAutoCloseRanksEnabled());
	}

	@Test
	public void updateBusinessGroup() {
		//create a business group
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("bdao-4-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdio", "gdio-desc", BusinessGroup.BUSINESS_TYPE,
				1, 10, true, true, false, false, false);
		dbInstance.commitAndCloseSession();

		//delete a business group
		group.setWaitingListEnabled(false);
		group.setDescription("gdio-2-desc");

		//update business group (semantic of Hibernate before JPA)
		BusinessGroup updatedGroup = businessGroupDao.merge(group);
		Assert.assertNotNull(updatedGroup);
		Assert.assertEquals(group, updatedGroup);
		Assert.assertEquals("gdio-2-desc", updatedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, updatedGroup.getWaitingListEnabled());
		Assert.assertEquals(group, updatedGroup);

		dbInstance.commitAndCloseSession();

		//reload the merged group and check values
		BusinessGroup reloadedGroup = businessGroupDao.load(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals(group, reloadedGroup);
		Assert.assertEquals("gdio-2-desc", reloadedGroup.getDescription());
		Assert.assertEquals(Boolean.FALSE, reloadedGroup.getWaitingListEnabled());
	}

	@Test
	public void findBusinessGroupsWithWaitingListAttendedBy() {
		//3 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-5-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-6-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("bdao-7-" + UUID.randomUUID().toString());

		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();

		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.waiting.name());
		//id2 -> group 1 and 3
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.waiting.name());

		//check:
		//id1: group 1 and 2
		List<BusinessGroup> groupOfId1 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id1,  null);
		Assert.assertNotNull(groupOfId1);
		Assert.assertTrue(groupOfId1.contains(group1));
		Assert.assertTrue(groupOfId1.contains(group2));
		//id2 -> group 1 and 3
		List<BusinessGroup> groupOfId2 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id2,  null);
		Assert.assertNotNull(groupOfId2);
		Assert.assertTrue(groupOfId2.contains(group1));
		Assert.assertTrue(groupOfId2.contains(group3));

		List<BusinessGroup> groupOfId3 = businessGroupDao.findBusinessGroupsWithWaitingListAttendedBy(id3,  null);
		Assert.assertNotNull(groupOfId3);
		Assert.assertTrue(groupOfId3.isEmpty());
	}

	@Test
	public void testVisibilityOfSecurityGroups() {
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdro", "gdro-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdso", "gdso-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdto", "gdto-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, true);
		dbInstance.commitAndCloseSession();

		//check the value
		Assert.assertTrue(group1.isOwnersVisibleIntern());
		Assert.assertTrue(group1.isParticipantsVisibleIntern());
		Assert.assertFalse(group1.isWaitingListVisibleIntern());

		Assert.assertFalse(group2.isOwnersVisibleIntern());
		Assert.assertTrue(group2.isParticipantsVisibleIntern());
		Assert.assertFalse(group2.isWaitingListVisibleIntern());

		Assert.assertFalse(group3.isOwnersVisibleIntern());
		Assert.assertFalse(group3.isParticipantsVisibleIntern());
		Assert.assertTrue(group3.isWaitingListVisibleIntern());
	}

	@Test
	public void findBusinessGroupsByExactName() {
		String exactName = UUID.randomUUID().toString();
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, exactName, "gdwo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, exactName + "x", "gdxo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "y" +exactName, "gdyo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName(exactName);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, null, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(group1));
		Assert.assertFalse(groups.contains(group2));
		Assert.assertFalse(groups.contains(group3));
	}

	@Test
	public void findManagedGroups() {
		//create a managed group with an external ID
		String externalId = UUID.randomUUID().toString();
		String managedFlags = "title,description";
		BusinessGroup managedGroup = businessGroupDao.createAndPersist(null, "managed-grp-1", "managed-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				externalId, managedFlags, 0, 5, true, false, true, false, false, null);
		BusinessGroup freeGroup = businessGroupDao.createAndPersist(null, "free-grp-1", "free-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		//search managed group
		SearchBusinessGroupParams paramsManaged = new SearchBusinessGroupParams();
		paramsManaged.setManaged(Boolean.TRUE);
		List<BusinessGroup> managedGroups = businessGroupDao.findBusinessGroups(paramsManaged, null, 0, 0);
		Assert.assertNotNull(managedGroups);
		Assert.assertTrue(managedGroups.size() >= 1);
		Assert.assertTrue(managedGroups.contains(managedGroup));
		Assert.assertFalse(managedGroups.contains(freeGroup));

		//search free group
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setManaged(Boolean.FALSE);
		List<BusinessGroup> freeGroups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(freeGroups);
		Assert.assertTrue(freeGroups.size() >= 1);
		Assert.assertTrue(freeGroups.contains(freeGroup));
		Assert.assertFalse(freeGroups.contains(managedGroup));
	}


	@Test
	public void findGroupByExternalId() {
		//create a managed group with an external ID
		String externalId = UUID.randomUUID().toString();
		String managedFlags = "all";
		BusinessGroup group = businessGroupDao.createAndPersist(null, "managed-grp-2", "managed-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				externalId, managedFlags, 0, 5, true, false, true, false, false, null);
		dbInstance.commitAndCloseSession();

		//search
		SearchBusinessGroupParams paramsAll = new SearchBusinessGroupParams();
		paramsAll.setExternalId(externalId);
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(paramsAll, null, 0, 0);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(group));
	}

	@Test
	public void findBusinessGroups_my() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//check
		List<BusinessGroup> myLazyGroups = businessGroupDao.findBusinessGroup(id, 0);
		Assert.assertNotNull(myLazyGroups);
		Assert.assertEquals(2, myLazyGroups.size());
		List<Long> originalKeys = PersistenceHelper.toKeys(myLazyGroups);
		Assert.assertTrue(originalKeys.contains(group1.getKey()));
		Assert.assertTrue(originalKeys.contains(group2.getKey()));
		Assert.assertFalse(originalKeys.contains(group3.getKey()));
	}

	@Test
	public void isIdentityInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		List<BusinessGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);

		//check owner + attendee
		List<Long> groupKeysA = businessGroupDao.isIdentityInBusinessGroups(id, true, true, false, groups);
		Assert.assertNotNull(groupKeysA);
		Assert.assertEquals(2, groupKeysA.size());
		Assert.assertTrue(groupKeysA.contains(group1.getKey()));
		Assert.assertTrue(groupKeysA.contains(group2.getKey()));

		//check owner
		List<Long> groupKeysB = businessGroupDao.isIdentityInBusinessGroups(id, true, false, false, groups);
		Assert.assertNotNull(groupKeysB);
		Assert.assertEquals(1, groupKeysB.size());
		Assert.assertTrue(groupKeysB.contains(group1.getKey()));

		//check attendee
		List<Long> groupKeysC = businessGroupDao.isIdentityInBusinessGroups(id, false, true, false, groups);
		Assert.assertNotNull(groupKeysC);
		Assert.assertEquals(1, groupKeysC.size());
		Assert.assertTrue(groupKeysC.contains(group2.getKey()));
	}

	@Test
	public void getMembershipInfoInBusinessGroups() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "is-in-grp-1", "is-in-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "is-in-grp-2", "is-in-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee
		int countMembershipA = businessGroupDao.countMembershipInfoInBusinessGroups(id, groupKeys);
		Assert.assertEquals(3, countMembershipA);
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys, id);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(3, memberships.size());

		int found = 0;
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
			if(membership.getGroupKey() != null && group1.getKey().equals(membership.getGroupKey())) {
				found++;
			}
			if(membership.getGroupKey() != null && group2.getKey().equals(membership.getGroupKey())) {
				found++;
			}
			if(membership.getGroupKey() != null && group3.getKey().equals(membership.getGroupKey())) {
				found++;
			}
		}
		Assert.assertEquals(3, found);
	}

	@Test
	public void getBusinessGroupsMembership() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-rev-1" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-rev-2" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "is-in-grp-rev-1", "is-in-grp-rev-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "is-in-grp-rev-2", "is-in-grp-rev-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-rev-3", "is-in-grp-rev-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group3, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<BusinessGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee
		int countMembership1 = businessGroupDao.countMembershipInfoInBusinessGroups(id1, groupKeys);
		int countMembership2 = businessGroupDao.countMembershipInfoInBusinessGroups(id2, groupKeys);
		Assert.assertEquals(3, countMembership1);
		Assert.assertEquals(3, countMembership2);
		List<BusinessGroupMembership> memberships = businessGroupDao.getBusinessGroupsMembership(groups);
		Assert.assertNotNull(memberships);
		//5: id1 3 membership in group1, group2, group3 and id2 2 memberships in group2 and group3
		Assert.assertEquals(5, memberships.size());

		int foundOwn = 0;
		int foundPart = 0;
		int foundWait = 0;
		for(BusinessGroupMembership membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
			Assert.assertNotNull(membership.getGroupKey());
			Assert.assertTrue(groupKeys.contains(membership.getGroupKey()));
			if(membership.isOwner()) {
				foundOwn++;
			}
			if(membership.isParticipant()) {
				foundPart++;
			}
			if(membership.isWaiting()) {
				foundWait++;
			}
		}
		Assert.assertEquals("Owners", 2, foundOwn);
		Assert.assertEquals("Participants", 3, foundPart);
		Assert.assertEquals("Waiting", 1, foundWait);
	}


	@Test
	public void getMembershipInfoInBusinessGroupsWithoutIdentityParam() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("is-in-grp-" + UUID.randomUUID().toString());

		BusinessGroup group1 = businessGroupDao.createAndPersist(id1, "is-in-grp-1", "is-in-grp-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id2, "is-in-grp-2", "is-in-grp-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "is-in-grp-3", "is-in-grp-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		dbInstance.commitAndCloseSession();

		businessGroupRelationDao.addRole(id1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<Long> groupKeys = new ArrayList<>();
		groupKeys.add(group1.getKey());
		groupKeys.add(group2.getKey());
		groupKeys.add(group3.getKey());

		//check owner + attendee + waiting
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDao.getMembershipInfoInBusinessGroups(groupKeys);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(7, memberships.size());
		for(BusinessGroupMembershipViewImpl membership:memberships) {
			Assert.assertNotNull(membership.getIdentityKey());
			Assert.assertNotNull(membership.getCreationDate());
			Assert.assertNotNull(membership.getLastModified());
		}
	}
}
