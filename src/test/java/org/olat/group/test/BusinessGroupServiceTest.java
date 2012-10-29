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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupServiceTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupServiceTest.class);
	private static boolean initialize = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	// Identities for tests
	private static Identity id1 = null;
	private static Identity id2 = null;
	private static Identity id3 = null;
	private static Identity id4 = null;
	// For WaitingGroup tests
	private static Identity wg1 = null;

	// Group one
	private static BusinessGroup one = null;
	private String oneName = "First BuddyGroup";
	private String oneDesc = "some short description for first buddygroup";
	// Group two
	private static BusinessGroup two = null;
	private String twoName = "Second BuddyGroup";
	private String twoDesc = "some short description for second buddygroup";
	// Group three
	private static BusinessGroup three = null;
	private String threeName = "Third BuddyGroup";
	private String threeDesc = "some short description for second buddygroup";
	// For WaitingGroup tests
	private static BusinessGroup bgWithWaitingList = null;
	
	@Before
	public void setUp() throws Exception {
		if(initialize) return;
		
			// Identities
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1-bgs-" + UUID.randomUUID().toString());
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("id2-bgs-" + UUID.randomUUID().toString());
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("id3-bgs-" + UUID.randomUUID().toString());
			id4 = JunitTestHelper.createAndPersistIdentityAsUser("id4-bgs-" + UUID.randomUUID().toString());
			// buddyGroups without waiting-list: groupcontext is null
			List<BusinessGroup> l = businessGroupService.findBusinessGroupsOwnedBy(id1, null);
			if (l.size() == 0) {
				one = businessGroupService.createBusinessGroup(id1, oneName, oneDesc, -1, -1, false, false, null);
			} else {
				List<BusinessGroup> groups = businessGroupService.findBusinessGroupsOwnedBy(id1, null);
				for(BusinessGroup group:groups) {
					if(oneName.equals(group.getName())) {
						one = group;
					}
				}
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(id2, null);
			if (l.size() == 0) {
				two = businessGroupService.createBusinessGroup(id2, twoName, twoDesc, -1, -1, false, false, null);
				SecurityGroup twoPartips = two.getPartipiciantGroup();
				securityManager.addIdentityToSecurityGroup(id3, twoPartips);
				securityManager.addIdentityToSecurityGroup(id4, twoPartips);
			} else {
				two = businessGroupService.findBusinessGroupsOwnedBy(id2, null).get(0);
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(id3, null);
			if (l.size() == 0) {
				three = businessGroupService.createBusinessGroup(id3, threeName, threeDesc, -1, -1, false, false, null);
				SecurityGroup threeOwner = three.getOwnerGroup();
				SecurityGroup threeOPartips = three.getPartipiciantGroup();
				securityManager.addIdentityToSecurityGroup(id2, threeOPartips);
				securityManager.addIdentityToSecurityGroup(id1, threeOwner);
			} else {
				three = businessGroupService.findBusinessGroupsOwnedBy(id3, null).get(0);
			}
			/*
			 * Membership in ParticipiantGroups............................. id1
			 * owns BuddyGroup one with participiantGroup:={}........... id2 owns
			 * BuddyGroup two with participiantGroup:={id3,id4} id3 owns BuddyGroup
			 * three participiantGroup:={id2}, ownerGroup:={id3,id1}
			 */

			dbInstance.commitAndCloseSession();

			// create business-group with waiting-list
			String bgWithWaitingListName = "Group with WaitingList";
			String bgWithWaitingListDesc = "some short description for Group with WaitingList";
			Boolean enableWaitinglist = new Boolean(true);
			Boolean enableAutoCloseRanks = new Boolean(true);
			RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
			System.out.println("testAddToWaitingListAndFireEvent: resource=" + resource);
			bgWithWaitingList = businessGroupService.createBusinessGroup(id1, bgWithWaitingListName,
					bgWithWaitingListDesc, -1, -1, enableWaitinglist, enableAutoCloseRanks, resource);
			bgWithWaitingList.setMaxParticipants(new Integer(2));
			// Identities
			String suffix = UUID.randomUUID().toString();
			User UserWg1 = userManager.createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg1 = securityManager.createAndPersistIdentity(suffix, UserWg1, BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg1"));

			dbInstance.commitAndCloseSession();

			initialize = true;
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupService);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "gdao", "gdao-desc", -1, -1, false, false, null);
		Assert.assertNotNull(group);
	}
	
	@Test
	public void createBusinessGroupWithResource() {
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "gdao", "gdao-desc", -1, -1, false, false, resource);
		
		//commit the group
		dbInstance.commit();
		Assert.assertNotNull(group);
	}

	@Test
	public void testLoadBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(null, false, false);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, 5);
		Assert.assertNotNull(groups);
	}
	
	@Test
	public void testCreateUpdateBusinessGroup_v1() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("grp-up-1-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(id, "up-1", "up-1-desc", -1, -1, false, false, resource);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check update
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id, group, "up-1-b", "up-1-desc-b", new Integer(2), new Integer(3));
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//reload to check update
		BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals("up-1-b", reloadedGroup.getName());
		Assert.assertEquals("up-1-desc-b", reloadedGroup.getDescription());
		Assert.assertEquals(new Integer(2), reloadedGroup.getMinParticipants());
		Assert.assertEquals(new Integer(3), reloadedGroup.getMaxParticipants());
	}
	
	@Test
	public void testCreateUpdateBusinessGroup_v2() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("grp-up-2-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(id, "up-2", "up-2-desc", -1, -1, false, false, resource);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check update
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id, group, "up-2-b", "up-2-desc-b", new Integer(2), new Integer(3), Boolean.TRUE, Boolean.TRUE);
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//reload to check update
		BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals("up-2-b", reloadedGroup.getName());
		Assert.assertEquals("up-2-desc-b", reloadedGroup.getDescription());
		Assert.assertEquals(new Integer(2), reloadedGroup.getMinParticipants());
		Assert.assertEquals(new Integer(3), reloadedGroup.getMaxParticipants());
		Assert.assertEquals(Boolean.TRUE, reloadedGroup.getWaitingListEnabled());
		Assert.assertEquals(Boolean.TRUE, reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void testUpdateBusinessGroupAndAutoRank_v1() {
		//create a group with 1 participant and 2 users in waiting list
		Identity id0 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-0-" + UUID.randomUUID().toString());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-4-" + UUID.randomUUID().toString());

		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(id0, "auto-1", "auto-1-desc", new Integer(0), new Integer(1), true, true, resource);
		Assert.assertNotNull(group);

		securityManager.addIdentityToSecurityGroup(id1, group.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id3, group.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id4, group.getWaitingGroup());
		dbInstance.commitAndCloseSession();

		//update max participants
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id0, group, "auto-1", "auto-1-desc", new Integer(0), new Integer(3));
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//check the auto rank 
		List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(id1));
		
		List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
	}
	
	@Test
	public void testUpdateBusinessGroupAndAutoRank_v2() {
		//create a group with 1 participant and 2 users in waiting list
		Identity id0 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-0-" + UUID.randomUUID().toString());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("grp-auto-4-" + UUID.randomUUID().toString());

		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(id0, "auto-1", "auto-1-desc", new Integer(0), new Integer(1), false, false, resource);
		Assert.assertNotNull(group);

		securityManager.addIdentityToSecurityGroup(id1, group.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id3, group.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id4, group.getWaitingGroup());
		dbInstance.commitAndCloseSession();

		//update max participants
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id0, group, "auto-1", "auto-1-desc", new Integer(0), new Integer(3), Boolean.TRUE, Boolean.TRUE);
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//check the auto rank 
		List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(id1));
		
		List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
	}
	
	@Test
	public void testGroupsOfBGContext() {
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry c2 =  JunitTestHelper.createAndPersistRepositoryEntry();

		dbInstance.commitAndCloseSession(); // simulate user clicks
		SearchBusinessGroupParams params1 = new SearchBusinessGroupParams(null, false, false);
		assertTrue(businessGroupService.findBusinessGroups(params1, c1.getOlatResource(), 0, -1).isEmpty());
		assertTrue(businessGroupService.countBusinessGroups(params1, c1.getOlatResource()) == 0);

		dbInstance.commitAndCloseSession(); // simulate user clicks
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, 0, 10, false, false, c1);
		assertNotNull(g1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c1);
		assertNotNull(g2);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, 0, 10, false, false, c2);
		assertNotNull(g3);

		BusinessGroup g2duplicate = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c1);
		assertNotNull(g2duplicate); // name duplicate names are allowed per group context

		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c2);
		assertNotNull(g4); // name duplicate in other context allowed

		dbInstance.commitAndCloseSession(); // simulate user clicks
		SearchBusinessGroupParams params2 = new SearchBusinessGroupParams(null, false, false);
		Assert.assertEquals(3, businessGroupService.findBusinessGroups(params2, c1.getOlatResource(), 0, -1).size());
		Assert.assertEquals(3, businessGroupService.countBusinessGroups(params2, c1.getOlatResource()));
	}
	
	@Test
	public void mergeGroups() {
		//create some identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("merge-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("merge-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("merge-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("merge-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("merge-5-" + UUID.randomUUID().toString());
		Identity id6 = JunitTestHelper.createAndPersistIdentityAsUser("merge-6-" + UUID.randomUUID().toString());
		//create groups and memberships
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "old-1", null, 0, 10, false, false, null);
		securityManager.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "old-2", null, 0, 10, false, false, null);
		securityManager.addIdentityToSecurityGroup(id2, g2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id4, g2.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id5, g2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id6, g2.getOwnerGroup());
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "target", null, 0, 10, false, false, null);
		securityManager.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
		dbInstance.commitAndCloseSession();
		
		//merge
		List<BusinessGroup> groupsToMerge = new ArrayList<BusinessGroup>();
		groupsToMerge.add(g1);
		groupsToMerge.add(g2);
		groupsToMerge.add(g3);
		BusinessGroup mergedGroup = businessGroupService.mergeBusinessGroups(wg1, g3, groupsToMerge);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(g3, mergedGroup);
		dbInstance.commitAndCloseSession();
		
		//check merge
		List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(mergedGroup.getOwnerGroup());
		Assert.assertNotNull(owners);
		Assert.assertEquals(2, owners.size());
		Assert.assertTrue(owners.contains(id3));
		Assert.assertTrue(owners.contains(id6));
		List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(mergedGroup.getPartipiciantGroup());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(id1));
		Assert.assertTrue(participants.contains(id2));
		Assert.assertTrue(participants.contains(id5));
		List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(mergedGroup.getWaitingGroup());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
		Assert.assertTrue(waitingList.contains(id4));
	}

	/**
	 * Test existence of BuddyGroups inserted in the setUp phase................
	 * this test rather tests the findXXX methods...............................
	 * so if the setup was ok, and this test also fulfilled, then it means that
	 * createAndPersistBuddyGroup works, and also the findXXX methods.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndPersistBuddyGroup() throws Exception {
		// id1
		List<BusinessGroup> groupOwnedId1 = businessGroupService.findBusinessGroupsOwnedBy(id1, null);
		Assert.assertEquals("2 BuddyGroups owned by id1", 3, groupOwnedId1.size());
		Assert.assertTrue(groupOwnedId1.contains(one));
		Assert.assertTrue(groupOwnedId1.contains(three));
		Assert.assertTrue(groupOwnedId1.contains(bgWithWaitingList));

		List<BusinessGroup> groupAttendeeId1 = businessGroupService.findBusinessGroupsAttendedBy(id1, null);
		Assert.assertEquals("0 BuddyGroup where id1 is partipicating", 0, groupAttendeeId1.size());

		// id2
		List<BusinessGroup> groupOwnedId2 = businessGroupService.findBusinessGroupsOwnedBy(id2, null);
		Assert.assertEquals("1 BuddyGroup owned by id2", 1, groupOwnedId2.size());
		Assert.assertTrue(groupOwnedId2.contains(two));
		
		List<BusinessGroup> groupAttendeeId2 = businessGroupService.findBusinessGroupsAttendedBy(id2, null);
		Assert.assertEquals("1 BuddyGroup where id2 is partipicating", 1, groupAttendeeId2.size());
		assertTrue("It's the correct BuddyGroup", groupAttendeeId2.contains(three));

		// id3
		List<BusinessGroup> groupOwnedId3 = businessGroupService.findBusinessGroupsOwnedBy(id3, null);
		Assert.assertEquals("1 BuddyGroup owned by id3", 1, groupOwnedId3.size());
		assertTrue("It's the correct BuddyGroup", groupOwnedId3.contains(three));
		
		List<BusinessGroup> groupAttendeeId3 = businessGroupService.findBusinessGroupsAttendedBy(id3, null);
		Assert.assertEquals("1 BuddyGroup where id3 is partipicating", 1, groupAttendeeId3.size());
		assertTrue("It's the correct BuddyGroup", groupAttendeeId3.contains(two));

		// id4
		List<BusinessGroup> groupOwnedId4 = businessGroupService.findBusinessGroupsOwnedBy(id4, null);
		Assert.assertEquals("0 BuddyGroup owned by id4", 0, groupOwnedId4.size());


		SearchBusinessGroupParams params4 = new SearchBusinessGroupParams(id4, false, true);
		List<BusinessGroup> groupAttendeeId4 = businessGroupService.findBusinessGroups(params4, null, 0, -1);
		Assert.assertEquals("1 BuddyGroup where id4 is partipicating", 1, groupAttendeeId4.size());
		assertTrue("It's the correct BuddyGroup", groupAttendeeId4.contains(two));
	}

	@Test
	public void testMoveIdentityFromWaitingListToParticipant()
	throws Exception {
		//add 2 identities in waiting group and 1 in as participant
		Identity admin = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-0-" + UUID.randomUUID().toString());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-1", "move-desc", 0, 10, true, false, null);
		businessGroupService.addToWaitingList(admin, Collections.singletonList(id1), group);
		businessGroupService.addToWaitingList(admin, Collections.singletonList(id2), group);
		businessGroupService.addParticipants(admin, JunitTestHelper.getAdminRoles(), Collections.singletonList(id3), group);
		
		dbInstance.commitAndCloseSession();
		
		//move id1 from waiting-list to participant
		List<Identity> identities = Collections.singletonList(id1);
		businessGroupService.moveIdentityFromWaitingListToParticipant(admin, identities, group);
		//check position of 'id2'
		int pos = businessGroupService.getPositionInWaitingListFor(id2, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos, 1, pos);
		//check if 'id3' is in participant-list
		boolean negatifCheck = businessGroupService.isIdentityInBusinessGroup(id3, group);
		assertTrue("Identity is not in participant-list", negatifCheck);
	}
	
	/**
	 * Add 3 identities to the waiting list and check the position.
	 */
	@Test
	public void testAddToWaitingListAndFireEventAndCheckPosition() throws Exception {
		//add 2 identities in waiting group and 1 in as participant
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-1", "move-desc", 0, 10, true, false, null);
		//add id1
		businessGroupService.addToWaitingList(id1, Collections.singletonList(id1), group);
		dbInstance.commitAndCloseSession();
		//add id2
		businessGroupService.addToWaitingList(id2, Collections.singletonList(id2), group);
		dbInstance.commitAndCloseSession();
		//add id3
		businessGroupService.addToWaitingList(id3, Collections.singletonList(id3), group);
		dbInstance.commitAndCloseSession();
		

		// Check position of 'id1'
		int pos1 = businessGroupService.getPositionInWaitingListFor(id1, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos1, 1, pos1);
		// Check position of 'id2'
		int pos2 = businessGroupService.getPositionInWaitingListFor(id2, group);
		Assert.assertEquals("pos must be 2, bit is=" + pos2, 2, pos2);
		// Check position of 'id3'
		int pos3 = businessGroupService.getPositionInWaitingListFor(id3, group);
		Assert.assertEquals("pos must be 3, bit is=" + pos3, 3, pos3);
	}
	
	/**
	 * Remove identity 2 (wg3) from the waiting list and check the position of
	 * identity 1 and 3.
	 */
	@Test
	public void testRemoveFromWaitingListAndFireEvent() throws Exception {
		//add 3 identities in waiting group
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-3", "move-desc", 0, 10, true, false, null);
		businessGroupService.addToWaitingList(id1, Collections.singletonList(id1), group);
		businessGroupService.addToWaitingList(id2, Collections.singletonList(id2), group);
		businessGroupService.addToWaitingList(id3, Collections.singletonList(id3), group);
		dbInstance.commitAndCloseSession();
		
		//remove id2
		businessGroupService.removeFromWaitingList(wg1, Collections.singletonList(id2), group);
		dbInstance.commitAndCloseSession();
		
		//check position of 'id1'
		int pos1 = businessGroupService.getPositionInWaitingListFor(id1, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos1, 1, pos1);
		//check position of 'id3'
		int pos3 = businessGroupService.getPositionInWaitingListFor(id3, group);
		Assert.assertEquals("pos must be 2, bit is=" + pos3, 2, pos3);
		//check position of id2
		int pos2 = businessGroupService.getPositionInWaitingListFor(id2, group);
		Assert.assertEquals("pos must be -1, not in list bit is=" + pos2, -1, pos2);
	}
	
	@Test
	public void testRemoveMembers() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-0-" + UUID.randomUUID().toString());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-4-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupService.createBusinessGroup(id1, "move-bg-3", "move-desc", 0, 10, true, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(id2, "move-bg-3", "move-desc", 0, 10, true, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(id3, "move-bg-3", "move-desc", 0, 10, true, false, resource);

		securityManager.addIdentityToSecurityGroup(id2, group1.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id3, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, group2.getWaitingGroup());
		securityManager.addIdentityToSecurityGroup(id2, group2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id1, group3.getOwnerGroup());
		securityManager.addIdentityToSecurityGroup(id2, group3.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id4, group3.getWaitingGroup());
		dbInstance.commitAndCloseSession();
		//this groups and relations have been created
		//group1: id1, id2, id3
		//group2: id2, id3
		//group3: id1, id2, id3, id4
		
		
		//-> remove id1, id3 from the resource
		List<Identity> identitiesToRemove = new ArrayList<Identity>();
		identitiesToRemove.add(id1);
		identitiesToRemove.add(id3);
		businessGroupService.removeMembers(admin, identitiesToRemove, resource.getOlatResource());
		dbInstance.commitAndCloseSession();

		//check in group1 stay only id2 in waiting list
		List<Identity> ownerGroup1 = securityManager.getIdentitiesOfSecurityGroup(group1.getOwnerGroup());
		Assert.assertNotNull(ownerGroup1);
		Assert.assertTrue(ownerGroup1.isEmpty());
		List<Identity> participantGroup1 = securityManager.getIdentitiesOfSecurityGroup(group1.getPartipiciantGroup());
		Assert.assertNotNull(participantGroup1);
		Assert.assertTrue(participantGroup1.isEmpty());
		List<Identity> waitingGroup1 = securityManager.getIdentitiesOfSecurityGroup(group1.getWaitingGroup());
		Assert.assertNotNull(waitingGroup1);
		Assert.assertEquals(1, waitingGroup1.size());
		Assert.assertEquals(id2, waitingGroup1.get(0));
		
		//check in group2 id2 as owner and participant
		List<Identity> ownerGroup2 = securityManager.getIdentitiesOfSecurityGroup(group2.getOwnerGroup());
		Assert.assertNotNull(ownerGroup2);
		Assert.assertEquals(1, ownerGroup2.size());
		Assert.assertEquals(id2, ownerGroup2.get(0));
		List<Identity> participantGroup2 = securityManager.getIdentitiesOfSecurityGroup(group2.getPartipiciantGroup());
		Assert.assertNotNull(participantGroup2);
		Assert.assertEquals(1, participantGroup2.size());
		Assert.assertEquals(id2, participantGroup2.get(0));
		List<Identity> waitingGroup2 = securityManager.getIdentitiesOfSecurityGroup(group2.getWaitingGroup());
		Assert.assertNotNull(waitingGroup2);
		Assert.assertTrue(waitingGroup2.isEmpty());
		
		//check in group3 id2 as owner and participant
		List<Identity> ownerGroup3 = securityManager.getIdentitiesOfSecurityGroup(group3.getOwnerGroup());
		Assert.assertNotNull(ownerGroup3);
		Assert.assertTrue(ownerGroup3.isEmpty());
		List<Identity> participantGroup3 = securityManager.getIdentitiesOfSecurityGroup(group3.getPartipiciantGroup());
		Assert.assertNotNull(participantGroup3);
		Assert.assertEquals(1, participantGroup3.size());
		Assert.assertEquals(id2, participantGroup3.get(0));
		List<Identity> waitingGroup3 = securityManager.getIdentitiesOfSecurityGroup(group3.getWaitingGroup());
		Assert.assertNotNull(waitingGroup3);
		Assert.assertEquals(1, waitingGroup3.size());
		Assert.assertEquals(id4, waitingGroup3.get(0));
	}
	
	@Test
	public void testMoveRegisteredIdentityFromWaitingToParticipant() throws Exception {
		//add 1 identity as participant
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w4-1-" + UUID.randomUUID().toString());
		Roles rolesId1 = securityManager.getRoles(id1);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-4", "move-desc", 0, 10, true, false, null);
		businessGroupService.addParticipants(id1, rolesId1, Collections.singletonList(id1), group);
		dbInstance.commitAndCloseSession();

		//add a user to waiting-list which is already in participant-list 
		businessGroupService.addToWaitingList(id1, Collections.singletonList(id1), group);
		dbInstance.commitAndCloseSession();
		//try to move this user => user will be removed from waiting-list
		businessGroupService.moveIdentityFromWaitingListToParticipant(id1, Collections.singletonList(id1), group);
		dbInstance.commitAndCloseSession();

		//check position of 'id1'
		int pos = businessGroupService.getPositionInWaitingListFor(id1, group);
		Assert.assertEquals("pos must be -1, bit is=" + pos, -1, pos);
		//check if 'id1' is still in participant-list
		boolean member = businessGroupService.isIdentityInBusinessGroup(id1, group);
		Assert.assertTrue("Identity is not in participant-list", member);
	}
	
	@Test
	public void testAutoTransferFromWaitingListToParticipants() {
		//add 1 identity as participant, 1 in waiting list
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w5-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w5-2-" + UUID.randomUUID().toString());;
		Roles rolesId1 = securityManager.getRoles(id1);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-5", "move-desc", 0, 1, true, true, null);
		businessGroupService.addParticipants(id1, rolesId1, Collections.singletonList(id1), group);
		businessGroupService.addToWaitingList(id2, Collections.singletonList(id2), group);
		dbInstance.commitAndCloseSession();

		//add a user to waiting-list which is already in participant-list 
		businessGroupService.removeParticipants(id1, Collections.singletonList(id1), group);
		dbInstance.commitAndCloseSession();

		//check position of 'id2'
		int pos = businessGroupService.getPositionInWaitingListFor(id2, group);
		Assert.assertEquals("pos must be -1, bit is=" + pos, -1, pos);
		//check if 'id1' is still in participant-list
		boolean member = businessGroupService.isIdentityInBusinessGroup(id2, group);
		Assert.assertTrue("Identity is in participant-list", member);
	}

	/**
	 * checks if tools can be enabled disabled or checked against being enabled.
	 * TOols are configured with the help of the generic properties storage.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEnableDisableAndCheckForTool() throws Exception {
		List<BusinessGroup>  sqlRes = businessGroupService.findBusinessGroupsOwnedBy(id2, null);
		BusinessGroup found = (BusinessGroup) sqlRes.get(0);
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			// all tools are disabled by default exept the news tool
			assertTrue(msg, !enabled);

		}

		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
		}

		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			assertTrue(msg, enabled);

		}

		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], false);
		}

		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is disabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			assertTrue(msg, !enabled);
		}
	}

	/**
	 * test if removing a BuddyGroup really deletes everything it should.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteBuddyGroup() throws Exception {
		List<BusinessGroup> sqlRes = businessGroupService.findBusinessGroupsOwnedBy(id2, null);
		assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
		BusinessGroup found = (BusinessGroup) sqlRes.get(0);
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);

		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
		}

		businessGroupService.deleteBusinessGroup(found);
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(id2, null);
		assertTrue("0 BuddyGroup owned by id2", sqlRes.size() == 0);
	}
	
	@Test
	public void testDeleteBusinessGroupWithWaitingGroup() {
		doTestDeleteBusinessGroup(true);
	}
	
	@Test
	public void testDeleteBusinessGroupWithoutWaitingGroup() {
		doTestDeleteBusinessGroup(false);
	}
	
	private void doTestDeleteBusinessGroup(boolean withWaitingList) {
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup deleteTestGroup = businessGroupService.createBusinessGroup(id1, "deleteTestGroup-1",
				"deleteTestGroup-1", -1, -1, withWaitingList, true, resource);
		
		Long ownerGroupKey = deleteTestGroup.getOwnerGroup().getKey();
		Long partipiciantGroupKey = deleteTestGroup.getPartipiciantGroup().getKey();
		Long waitingGroupKey = deleteTestGroup.getWaitingGroup().getKey();
		
		assertNotNull("Could not find owner-group", dbInstance.findObject(SecurityGroupImpl.class, ownerGroupKey));
		assertNotNull("Could not find partipiciant-group", dbInstance.findObject(SecurityGroupImpl.class, partipiciantGroupKey));
		assertNotNull("Could not find waiting-group", dbInstance.findObject(SecurityGroupImpl.class, waitingGroupKey));
		businessGroupService.deleteBusinessGroup(deleteTestGroup);
		assertNull("owner-group still exist after delete", dbInstance.findObject(SecurityGroupImpl.class, ownerGroupKey));
		assertNull("partipiciant-group still exist after delete", dbInstance.findObject(SecurityGroupImpl.class, partipiciantGroupKey));
		assertNull("waiting-group still exist after delete", dbInstance.findObject(SecurityGroupImpl.class, waitingGroupKey));
	}
	

}