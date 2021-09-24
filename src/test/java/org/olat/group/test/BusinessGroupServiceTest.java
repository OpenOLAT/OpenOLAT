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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.LeaveOption;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessGroupServiceTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupServiceTest.class);
	private static boolean initialize = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private MailModule mailModule;
	
	// Identities for tests
	private static Identity id1;
	private static Identity id2;
	private static Identity id3;
	private static Identity id4;
	// For WaitingGroup tests
	private static Identity wg1;

	// Group one
	private static BusinessGroup one;
	private String oneName = "First BuddyGroup";
	private String oneDesc = "some short description for first buddygroup";
	// Group two
	private static BusinessGroup two;
	private String twoName = "Second BuddyGroup";
	private String twoDesc = "some short description for second buddygroup";
	// Group three
	private static BusinessGroup three;
	private String threeName = "Third BuddyGroup";
	private String threeDesc = "some short description for second buddygroup";
	// For WaitingGroup tests
	private static BusinessGroup bgWithWaitingList;
	
	@Before
	public void setUp() throws Exception {
		if(initialize) return;
		
			// Identities
			id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("id1-bgs-");
			id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("id2-bgs-");
			id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("id3-bgs-");
			id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("id4-bgs-");
			// buddyGroups without waiting-list: groupcontext is null
			List<BusinessGroup> l = businessGroupService.findBusinessGroupsOwnedBy(id1);
			if (l.size() == 0) {
				one = businessGroupService.createBusinessGroup(id1, oneName, oneDesc, BusinessGroup.BUSINESS_TYPE,
						-1, -1, false, false, null);
			} else {
				List<BusinessGroup> groups = businessGroupService.findBusinessGroupsOwnedBy(id1);
				for(BusinessGroup group:groups) {
					if(oneName.equals(group.getName())) {
						one = group;
					}
				}
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(id2);
			if (l.size() == 0) {
				two = businessGroupService.createBusinessGroup(id2, twoName, twoDesc, BusinessGroup.BUSINESS_TYPE,
						-1, -1, false, false, null);
				businessGroupRelationDao.addRole(id3, two, GroupRoles.participant.name());
				businessGroupRelationDao.addRole(id4, two, GroupRoles.participant.name());
			} else {
				two = businessGroupService.findBusinessGroupsOwnedBy(id2).get(0);
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(id3);
			if (l.size() == 0) {
				three = businessGroupService.createBusinessGroup(id3, threeName, threeDesc, BusinessGroup.BUSINESS_TYPE,
						-1, -1, false, false, null);
				businessGroupRelationDao.addRole(id2, three, GroupRoles.participant.name());
				businessGroupRelationDao.addRole(id1, three, GroupRoles.coach.name());
			} else {
				three = businessGroupService.findBusinessGroupsOwnedBy(id3).get(0);
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
			Boolean enableWaitinglist = Boolean.TRUE;
			Boolean enableAutoCloseRanks = Boolean.TRUE;
			RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
			log.info("testAddToWaitingListAndFireEvent: resource=" + resource);
			bgWithWaitingList = businessGroupService.createBusinessGroup(id1, bgWithWaitingListName, bgWithWaitingListDesc, BusinessGroup.BUSINESS_TYPE,
					-1, -1, enableWaitinglist, enableAutoCloseRanks, resource);
			bgWithWaitingList.setMaxParticipants(Integer.valueOf(2));
			// Identities
			wg1 = JunitTestHelper.createAndPersistIdentityAsRndUser("wg1");
			dbInstance.commitAndCloseSession();

			initialize = true;
	}
	
	@After
	public void resetBusinessGroupModule() {
		businessGroupModule.setAllowLeavingGroupCreatedByAuthors(true);
		businessGroupModule.setAllowLeavingGroupCreatedByLearners(true);
		businessGroupModule.setAllowLeavingGroupOverride(true);
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(businessGroupService);
	}
	
	@Test
	public void createBusinessGroup() {
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		Assert.assertNotNull(group);
	}
	
	@Test
	public void createBusinessGroupWithResource() {
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		
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
		BusinessGroup group = businessGroupService.createBusinessGroup(id, "up-1", "up-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check update
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id, group, "up-1-b", "up-1-desc-b",
				null, null, Integer.valueOf(2), Integer.valueOf(3));
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//reload to check update
		BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals("up-1-b", reloadedGroup.getName());
		Assert.assertEquals("up-1-desc-b", reloadedGroup.getDescription());
		Assert.assertEquals(Integer.valueOf(2), reloadedGroup.getMinParticipants());
		Assert.assertEquals(Integer.valueOf(3), reloadedGroup.getMaxParticipants());
	}
	
	@Test
	public void testCreateUpdateBusinessGroup_v2() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("grp-up-2-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(id, "up-2", "up-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check update
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(id, group, "up-2-b", "up-2-desc-b",
				Integer.valueOf(2), Integer.valueOf(3), Boolean.TRUE, Boolean.TRUE);
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//reload to check update
		BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group.getKey());
		Assert.assertNotNull(reloadedGroup);
		Assert.assertEquals("up-2-b", reloadedGroup.getName());
		Assert.assertEquals("up-2-desc-b", reloadedGroup.getDescription());
		Assert.assertEquals(Integer.valueOf(2), reloadedGroup.getMinParticipants());
		Assert.assertEquals(Integer.valueOf(3), reloadedGroup.getMaxParticipants());
		Assert.assertEquals(Boolean.TRUE, reloadedGroup.getWaitingListEnabled());
		Assert.assertEquals(Boolean.TRUE, reloadedGroup.getAutoCloseRanksEnabled());
	}
	
	@Test
	public void testUpdateBusinessGroupAndAutoRank_v1() {
		//create a group with 1 participant and 2 users in waiting list
		Identity ident0 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-0-");
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-1-");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-2-");
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-3-");
		Identity ident4 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-4-");

		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(ident0, "auto-1", "auto-1-desc", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(1), true, true, resource);
		Assert.assertNotNull(group);

		businessGroupRelationDao.addRole(ident1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident2, group, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident3, group, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident4, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		//update max participants
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(ident0, group, "auto-1", "auto-1-desc", null, null, Integer.valueOf(0), Integer.valueOf(3));
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//check the auto rank 
		List<Identity> participants = businessGroupRelationDao.getMembers(group, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(ident1));
		
		List<Identity> waitingList = businessGroupRelationDao.getMembers(group, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
	}
	
	@Test
	public void testUpdateBusinessGroupAndAutoRank_v2() {
		//create a group with 1 participant and 2 users in waiting list
		Identity ident0 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-0-");
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-1-");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-2-");
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-3-");
		Identity ident4 = JunitTestHelper.createAndPersistIdentityAsRndUser("grp-auto-4-");

		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(ident0, "auto-1", "auto-1-desc", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(1), false, false, resource);
		Assert.assertNotNull(group);

		businessGroupRelationDao.addRole(ident1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident2, group, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident3, group, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident4, group, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();

		//update max participants
		BusinessGroup updateGroup = businessGroupService.updateBusinessGroup(ident0, group, "auto-1", "auto-1-desc",
				Integer.valueOf(0), Integer.valueOf(3), Boolean.TRUE, Boolean.TRUE);
		Assert.assertNotNull(updateGroup);
		dbInstance.commitAndCloseSession();
		
		//check the auto rank 
		List<Identity> participants = businessGroupRelationDao.getMembers(group, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(ident1));
		
		List<Identity> waitingList = businessGroupRelationDao.getMembers(group, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
	}
	
	@Test
	public void testGroupsOfBGContext() {
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry c2 =  JunitTestHelper.createAndPersistRepositoryEntry();

		dbInstance.commitAndCloseSession(); // simulate user clicks
		SearchBusinessGroupParams params1 = new SearchBusinessGroupParams(null, false, false);
		assertTrue(businessGroupService.findBusinessGroups(params1, c1, 0, -1).isEmpty());
		assertTrue(businessGroupService.countBusinessGroups(params1, c1) == 0);

		dbInstance.commitAndCloseSession(); // simulate user clicks
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c1);
		assertNotNull(g1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c1);
		assertNotNull(g2);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c2);
		assertNotNull(g3);

		BusinessGroup g2duplicate = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c1);
		assertNotNull(g2duplicate); // name duplicate names are allowed per group context

		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c2);
		assertNotNull(g4); // name duplicate in other context allowed

		dbInstance.commitAndCloseSession(); // simulate user clicks
		SearchBusinessGroupParams params2 = new SearchBusinessGroupParams(null, false, false);
		Assert.assertEquals(3, businessGroupService.findBusinessGroups(params2, c1, 0, -1).size());
		Assert.assertEquals(3, businessGroupService.countBusinessGroups(params2, c1));
	}
	
	@Test
	public void mergeGroups() {
		//create some identities
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-1-");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-2-");
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-3-");
		Identity ident4 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-4-");
		Identity ident5 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-5-");
		Identity ident6 = JunitTestHelper.createAndPersistIdentityAsRndUser("merge-6-");
		//create groups and memberships
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "old-1", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		businessGroupRelationDao.addRole(ident1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident2, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident3, g1, GroupRoles.coach.name());
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "old-2", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		businessGroupRelationDao.addRole(ident2, g2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident4, g2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident5, g2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident6, g2, GroupRoles.coach.name());
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "target", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		businessGroupRelationDao.addRole(ident1, g3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//merge
		List<BusinessGroup> groupsToMerge = new ArrayList<>();
		groupsToMerge.add(g1);
		groupsToMerge.add(g2);
		groupsToMerge.add(g3);
		BusinessGroup mergedGroup = businessGroupService.mergeBusinessGroups(wg1, g3, groupsToMerge, null);
		Assert.assertNotNull(mergedGroup);
		Assert.assertEquals(g3, mergedGroup);
		dbInstance.commitAndCloseSession();
		
		//check merge
		List<Identity> coaches = businessGroupRelationDao.getMembers(mergedGroup, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(2, coaches.size());
		Assert.assertTrue(coaches.contains(ident3));
		Assert.assertTrue(coaches.contains(ident6));
		List<Identity> participants = businessGroupRelationDao.getMembers(mergedGroup, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(3, participants.size());
		Assert.assertTrue(participants.contains(ident1));
		Assert.assertTrue(participants.contains(ident2));
		Assert.assertTrue(participants.contains(ident5));
		List<Identity> waitingList = businessGroupRelationDao.getMembers(mergedGroup, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingList);
		Assert.assertEquals(1, waitingList.size());
		Assert.assertTrue(waitingList.contains(ident4));
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
		List<BusinessGroup> groupOwnedId1 = businessGroupService.findBusinessGroupsOwnedBy(id1);
		Assert.assertEquals("2 BuddyGroups owned by id1", 3, groupOwnedId1.size());
		Assert.assertTrue(groupOwnedId1.contains(one));
		Assert.assertTrue(groupOwnedId1.contains(three));
		Assert.assertTrue(groupOwnedId1.contains(bgWithWaitingList));

		List<BusinessGroup> groupAttendeeId1 = businessGroupService.findBusinessGroupsAttendedBy(id1);
		Assert.assertEquals("0 BuddyGroup where id1 is partipicating", 0, groupAttendeeId1.size());

		// id2
		List<BusinessGroup> groupOwnedId2 = businessGroupService.findBusinessGroupsOwnedBy(id2);
		Assert.assertEquals("1 BuddyGroup owned by id2", 1, groupOwnedId2.size());
		Assert.assertTrue(groupOwnedId2.contains(two));
		
		List<BusinessGroup> groupAttendeeId2 = businessGroupService.findBusinessGroupsAttendedBy(id2);
		Assert.assertEquals("1 BuddyGroup where id2 is partipicating", 1, groupAttendeeId2.size());
		assertTrue("It's the correct BuddyGroup", groupAttendeeId2.contains(three));

		// id3
		List<BusinessGroup> groupOwnedId3 = businessGroupService.findBusinessGroupsOwnedBy(id3);
		Assert.assertEquals("1 BuddyGroup owned by id3", 1, groupOwnedId3.size());
		assertTrue("It's the correct BuddyGroup", groupOwnedId3.contains(three));
		
		List<BusinessGroup> groupAttendeeId3 = businessGroupService.findBusinessGroupsAttendedBy(id3);
		Assert.assertEquals("1 BuddyGroup where id3 is partipicating", 1, groupAttendeeId3.size());
		assertTrue("It's the correct BuddyGroup", groupAttendeeId3.contains(two));

		// id4
		List<BusinessGroup> groupOwnedId4 = businessGroupService.findBusinessGroupsOwnedBy(id4);
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
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-2-" + UUID.randomUUID().toString());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w1-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-1", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, null);
		dbInstance.commitAndCloseSession();
		businessGroupService.addToWaitingList(admin, Collections.singletonList(ident1), group, null);
		businessGroupService.addToWaitingList(admin, Collections.singletonList(ident2), group, null);
		businessGroupService.addParticipants(admin, Roles.administratorRoles(), Collections.singletonList(ident3), group, null);
		dbInstance.commitAndCloseSession();
		
		//move id1 from waiting-list to participant
		List<Identity> identities = Collections.singletonList(ident1);
		businessGroupService.moveIdentityFromWaitingListToParticipant(admin, identities, group, null);
		//check position of 'id2'
		int pos = businessGroupService.getPositionInWaitingListFor(ident2, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos, 1, pos);
		//check if 'id3' is in participant-list
		boolean negatifCheck = businessGroupService.isIdentityInBusinessGroup(ident3, group);
		assertTrue("Identity is not in participant-list", negatifCheck);
	}
	
	/**
	 * Add 3 identities to the waiting list and check the position.
	 */
	@Test
	public void testAddToWaitingListAndFireEventAndCheckPosition() throws Exception {
		//add 2 identities in waiting group and 1 in as participant
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-2-" + UUID.randomUUID().toString());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w2-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-1", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, null);
		dbInstance.commitAndCloseSession();
		//add id1
		businessGroupService.addToWaitingList(ident1, Collections.singletonList(ident1), group, null);
		dbInstance.commitAndCloseSession();
		//add id2
		businessGroupService.addToWaitingList(ident2, Collections.singletonList(ident2), group, null);
		dbInstance.commitAndCloseSession();
		//add id3
		businessGroupService.addToWaitingList(ident3, Collections.singletonList(ident3), group, null);
		dbInstance.commitAndCloseSession();
		

		// Check position of 'id1'
		int pos1 = businessGroupService.getPositionInWaitingListFor(ident1, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos1, 1, pos1);
		// Check position of 'id2'
		int pos2 = businessGroupService.getPositionInWaitingListFor(ident2, group);
		Assert.assertEquals("pos must be 2, bit is=" + pos2, 2, pos2);
		// Check position of 'id3'
		int pos3 = businessGroupService.getPositionInWaitingListFor(ident3, group);
		Assert.assertEquals("pos must be 3, bit is=" + pos3, 3, pos3);
	}
	
	/**
	 * Remove identity 2 (wg3) from the waiting list and check the position of
	 * identity 1 and 3.
	 */
	@Test
	public void testRemoveFromWaitingListAndFireEvent() throws Exception {
		//add 3 identities in waiting group
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-1-" + UUID.randomUUID());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-2-" + UUID.randomUUID());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("move-w3-3-" + UUID.randomUUID());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-3", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, null);
		dbInstance.commitAndCloseSession();
		businessGroupService.addToWaitingList(ident1, Collections.singletonList(ident1), group, null);
		businessGroupService.addToWaitingList(ident2, Collections.singletonList(ident2), group, null);
		businessGroupService.addToWaitingList(ident3, Collections.singletonList(ident3), group, null);
		dbInstance.commitAndCloseSession();
		
		//remove id2
		businessGroupService.removeFromWaitingList(wg1, Collections.singletonList(ident2), group, null);
		dbInstance.commitAndCloseSession();
		
		//check position of 'id1'
		int pos1 = businessGroupService.getPositionInWaitingListFor(ident1, group);
		Assert.assertEquals("pos must be 1, bit is=" + pos1, 1, pos1);
		//check position of 'id3'
		int pos3 = businessGroupService.getPositionInWaitingListFor(ident3, group);
		Assert.assertEquals("pos must be 2, bit is=" + pos3, 2, pos3);
		//check position of id2
		int pos2 = businessGroupService.getPositionInWaitingListFor(ident2, group);
		Assert.assertEquals("pos must be -1, not in list bit is=" + pos2, -1, pos2);
	}
	
	@Test
	public void testRemoveMembers() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-0-" + UUID.randomUUID().toString());
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-2-" + UUID.randomUUID().toString());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-3-" + UUID.randomUUID().toString());
		Identity ident4 = JunitTestHelper.createAndPersistIdentityAsUser("rm-w3-4-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group1 = businessGroupService.createBusinessGroup(ident1, "move-bg-3", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(ident2, "move-bg-3", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(ident3, "move-bg-3", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, resource);

		businessGroupRelationDao.addRole(ident2, group1, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident3, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident3, group2, GroupRoles.waiting.name());
		businessGroupRelationDao.addRole(ident2, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident1, group3, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(ident2, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(ident4, group3, GroupRoles.waiting.name());
		dbInstance.commitAndCloseSession();
		//this groups and relations have been created
		//group1: id1, id2, id3
		//group2: id2, id3
		//group3: id1, id2, id3, id4
		
		
		//-> remove id1, id3 from the resource
		List<Identity> identitiesToRemove = new ArrayList<>();
		identitiesToRemove.add(ident1);
		identitiesToRemove.add(ident3);
		businessGroupService.removeMembers(admin, identitiesToRemove, resource.getOlatResource(), null, false);
		dbInstance.commitAndCloseSession();

		//check in group1 stay only id2 in waiting list
		List<Identity> ownerGroup1 = businessGroupService.getMembers(group1, GroupRoles.coach.name());
		Assert.assertNotNull(ownerGroup1);
		Assert.assertTrue(ownerGroup1.isEmpty());
		List<Identity> participantGroup1 = businessGroupRelationDao.getMembers(group1, GroupRoles.participant.name());
		Assert.assertNotNull(participantGroup1);
		Assert.assertTrue(participantGroup1.isEmpty());
		List<Identity> waitingGroup1 = businessGroupRelationDao.getMembers(group1, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingGroup1);
		Assert.assertEquals(1, waitingGroup1.size());
		Assert.assertEquals(ident2, waitingGroup1.get(0));
		
		//check in group2 id2 as owner and participant
		List<Identity> ownerGroup2 = businessGroupService.getMembers(group2, GroupRoles.coach.name());
		Assert.assertNotNull(ownerGroup2);
		Assert.assertEquals(1, ownerGroup2.size());
		Assert.assertEquals(ident2, ownerGroup2.get(0));
		List<Identity> participantGroup2 = businessGroupRelationDao.getMembers(group2, GroupRoles.participant.name());
		Assert.assertNotNull(participantGroup2);
		Assert.assertEquals(1, participantGroup2.size());
		Assert.assertEquals(ident2, participantGroup2.get(0));
		List<Identity> waitingGroup2 = businessGroupRelationDao.getMembers(group2, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingGroup2);
		Assert.assertTrue(waitingGroup2.isEmpty());
		
		//check in group3 id2 as owner and participant
		List<Identity> ownerGroup3 = businessGroupService.getMembers(group3, GroupRoles.coach.name());
		Assert.assertNotNull(ownerGroup3);
		Assert.assertTrue(ownerGroup3.isEmpty());
		List<Identity> participantGroup3 = businessGroupRelationDao.getMembers(group3, GroupRoles.participant.name());
		Assert.assertNotNull(participantGroup3);
		Assert.assertEquals(1, participantGroup3.size());
		Assert.assertEquals(ident2, participantGroup3.get(0));
		List<Identity> waitingGroup3 = businessGroupRelationDao.getMembers(group3, GroupRoles.waiting.name());
		Assert.assertNotNull(waitingGroup3);
		Assert.assertEquals(1, waitingGroup3.size());
		Assert.assertEquals(ident4, waitingGroup3.get(0));
	}
	
	@Test
	public void testMoveRegisteredIdentityFromWaitingToParticipant() throws Exception {
		//add 1 identity as participant
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w4-1-" + UUID.randomUUID().toString());
		Roles rolesId1 = securityManager.getRoles(ident1);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-4", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 10, true, false, null);
		dbInstance.commitAndCloseSession();
		businessGroupService.addParticipants(ident1, rolesId1, Collections.singletonList(ident1), group, null);
		dbInstance.commitAndCloseSession();

		//add a user to waiting-list which is already in participant-list 
		businessGroupService.addToWaitingList(ident1, Collections.singletonList(ident1), group, null);
		dbInstance.commitAndCloseSession();
		//try to move this user => user will be removed from waiting-list
		businessGroupService.moveIdentityFromWaitingListToParticipant(ident1, Collections.singletonList(ident1), group, null);
		dbInstance.commitAndCloseSession();

		//check position of 'id1'
		int pos = businessGroupService.getPositionInWaitingListFor(ident1, group);
		Assert.assertEquals("pos must be -1, bit is=" + pos, -1, pos);
		//check if 'id1' is still in participant-list
		boolean member = businessGroupService.isIdentityInBusinessGroup(ident1, group);
		Assert.assertTrue("Identity is not in participant-list", member);
	}
	
	@Test
	public void testAutoTransferFromWaitingListToParticipants() {
		//add 1 identity as participant, 1 in waiting list
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("move-w5-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("move-w5-2-" + UUID.randomUUID().toString());;
		Roles rolesId1 = securityManager.getRoles(ident1);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "move-bg-5", "move-desc", BusinessGroup.BUSINESS_TYPE,
				0, 1, true, true, null);
		dbInstance.commitAndCloseSession();
		businessGroupService.addParticipants(ident1, rolesId1, Collections.singletonList(ident1), group, null);
		businessGroupService.addToWaitingList(ident2, Collections.singletonList(ident2), group, null);
		dbInstance.commitAndCloseSession();

		//add a user to waiting-list which is already in participant-list 
		businessGroupService.removeParticipants(ident1, Collections.singletonList(ident1), group, null);
		dbInstance.commitAndCloseSession();

		//check position of 'id2'
		int pos = businessGroupService.getPositionInWaitingListFor(ident2, group);
		Assert.assertEquals("pos must be -1, bit is=" + pos, -1, pos);
		//check if 'id1' is still in participant-list
		boolean member = businessGroupService.isIdentityInBusinessGroup(ident2, group);
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
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coll-tools");
		BusinessGroup found = businessGroupService.createBusinessGroup(coach, "Collaboration", "Collaboration", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
		for (int i = 0; i < availableTools.length; i++) {
			String msg = "Tool " + availableTools[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(availableTools[i]);
			// all tools are disabled by default exept the news tool
			assertTrue(msg, !enabled);

		}

		for (int i = 0; i < availableTools.length; i++) {
			myCTSMngr.setToolEnabled(availableTools[i], true);
		}

		for (int i = 0; i < availableTools.length; i++) {
			String msg = "Tool " + availableTools[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(availableTools[i]);
			assertTrue(msg, enabled);

		}

		for (int i = 0; i < availableTools.length; i++) {
			myCTSMngr.setToolEnabled(availableTools[i], false);
		}

		for (int i = 0; i < availableTools.length; i++) {
			String msg = "Tool " + availableTools[i] + " is disabled";
			boolean enabled = myCTSMngr.isToolEnabled(availableTools[i]);
			assertTrue(msg, !enabled);
		}
	}
	
	@Test
	public void testUpdateMembership() {
		//create a group with owner and participant
		Identity ureqIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mship-u-");
		Identity ownerIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mship-o-");
		Identity partIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mship-p-");
		BusinessGroup group = businessGroupService.createBusinessGroup(ureqIdentity, "Up-mship", "updateMembership", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, null);
		businessGroupRelationDao.addRole(ownerIdentity, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(partIdentity, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//update memberships
		MailPackage mailing = new MailPackage(false);
		List<BusinessGroup> groups = Collections.singletonList(group);
		MembershipModification membersMod = new MembershipModification();
		membersMod.getAddOwners().add(partIdentity);
		membersMod.getAddParticipants().add(ownerIdentity);
		businessGroupService.updateMembership(ureqIdentity, membersMod, groups, mailing);
		dbInstance.commitAndCloseSession();
		
		//check if the participant is owner too and the owner is participant too
		boolean partIsOwner = businessGroupService.hasRoles(partIdentity, group, GroupRoles.coach.name());
		Assert.assertTrue(partIsOwner);
		boolean partIsPart = businessGroupService.hasRoles(partIdentity, group, GroupRoles.participant.name());
		Assert.assertTrue(partIsPart);
		boolean ownerIsOwner = businessGroupService.hasRoles(ownerIdentity, group, GroupRoles.coach.name());
		Assert.assertTrue(ownerIsOwner);
		boolean ownerIsPart = businessGroupService.hasRoles(ownerIdentity, group, GroupRoles.participant.name());
		Assert.assertTrue(ownerIsPart);
	}
	
	@Test
	public void testUpdateMemberships() {
		//create a group with owner and participant
		Identity ureqIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mships-u-");
		Identity ownerIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mships-o-");
		Identity partIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("Up-mships-p-");
		BusinessGroup group = businessGroupService.createBusinessGroup(ureqIdentity, "Up-mships", "updateMemberships", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, null);
		businessGroupRelationDao.addRole(ownerIdentity, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(partIdentity, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//invert the roles
		BusinessGroupMembershipChange change1 = new BusinessGroupMembershipChange(ownerIdentity, group.getKey());
		change1.setTutor(Boolean.FALSE);
		change1.setParticipant(Boolean.TRUE);
		BusinessGroupMembershipChange change2 = new BusinessGroupMembershipChange(partIdentity, group.getKey());
		change2.setTutor(Boolean.TRUE);
		change2.setParticipant(Boolean.FALSE);

		List<BusinessGroupMembershipChange> changes = new ArrayList<>();
		changes.add(change1);
		changes.add(change2);
		businessGroupService.updateMemberships(ureqIdentity, changes, new MailPackage(false));
		dbInstance.commitAndCloseSession();
		
		//check the result
		boolean partIsOwner = businessGroupService.hasRoles(partIdentity, group, GroupRoles.coach.name());
		Assert.assertTrue(partIsOwner);
		boolean partIsPart = businessGroupService.hasRoles(partIdentity, group, GroupRoles.participant.name());
		Assert.assertFalse(partIsPart);
		boolean ownerIsPart = businessGroupService.hasRoles(ownerIdentity, group, GroupRoles.participant.name());
		Assert.assertTrue(ownerIsPart);
		boolean ownerIsOwner = businessGroupService.hasRoles(ownerIdentity, group, GroupRoles.coach.name());
		Assert.assertFalse(ownerIsOwner);
	}

	/**
	 * test if removing a BuddyGroup really deletes everything it should.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteGroup() throws Exception {
		List<BusinessGroup> sqlRes = businessGroupService.findBusinessGroupsOwnedBy(id2);
		Assert.assertEquals("1 BuddyGroup owned by id2", 1, sqlRes.size());
		BusinessGroup found = sqlRes.get(0);
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();

		for (int i = 0; i < availableTools.length; i++) {
			myCTSMngr.setToolEnabled(availableTools[i], true);
		}

		businessGroupLifecycleManager.deleteBusinessGroup(found, null, false);
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(id2);
		assertTrue("0 BuddyGroup owned by id2", sqlRes.isEmpty());
	}
	
	@Test
	public void testDeleteBusinessGroupWithWaitingGroup() {
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup deleteTestGroup = businessGroupService.createBusinessGroup(id1, "deleteTestGroup-1",
				"deleteTestGroup-1", BusinessGroup.BUSINESS_TYPE,-1, -1, true, true, resource);
		dbInstance.commitAndCloseSession();
		
		businessGroupLifecycleManager.deleteBusinessGroup(deleteTestGroup, null, false);
		dbInstance.commitAndCloseSession();
		
		Group reloadedGroup = groupDao.loadGroup(deleteTestGroup.getBaseGroup().getKey());
		Assert.assertNull(reloadedGroup);
	}

	@Test
	public void testAcceptPendingParticipation_participant() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Reserv-bg-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		
		//create a reservation
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 6);
		ResourceReservation reservation = reservationDao.createReservation(id, "group_participant", cal.getTime(), group.getResource());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		Assert.assertEquals("group_participant", reservation.getType());
		Assert.assertEquals(group.getResource(), reservation.getResource());
		
		//check that the user is not participant
		Assert.assertFalse(businessGroupService.hasRoles(id, group, GroupRoles.participant.name()));
		
		//accept reservation
		businessGroupService.acceptPendingParticipation(id, id, group.getResource());
		dbInstance.commitAndCloseSession();
		
	//check that the user is participant
		boolean participant = businessGroupService.hasRoles(id, group, GroupRoles.participant.name());
		Assert.assertTrue(participant);
	}

	@Test
	public void testAcceptPendingParticipation_coach() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Reserv-bg-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		
		//create a reservation
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 6);
		ResourceReservation reservation = reservationDao.createReservation(id, "group_coach", cal.getTime(), group.getResource());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		
		//check that the user is not participant
		Assert.assertFalse(businessGroupService.hasRoles(id, group, GroupRoles.coach.name()));
		
		//accept reservation
		acService.acceptReservationToResource(id, reservation);
		dbInstance.commitAndCloseSession();
		
		//check that the user is participant
		Assert.assertTrue(businessGroupService.hasRoles(id, group, GroupRoles.coach.name()));
		//check that the reservations are deleted
		List<ResourceReservation> reservations = reservationDao.loadReservations(id);
		Assert.assertNotNull(reservations);
		Assert.assertTrue(reservations.isEmpty());
	}

	@Test
	public void testCancelPendingParticipation_participant() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Reserv-bg-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		
		//create a reservation
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 6);
		ResourceReservation reservation = reservationDao.createReservation(id, "group_participant", cal.getTime(), group.getResource());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		
		//check that the user is not participant
		Assert.assertFalse(businessGroupService.hasRoles(id, group, GroupRoles.participant.name()));
		
		//accept reservation
		acService.removeReservation(id, id, reservation);
		dbInstance.commitAndCloseSession();
		
		//check that the user is not participant
		Assert.assertFalse(businessGroupService.hasRoles(id, group, GroupRoles.participant.name()));
		//check that the reservations are deleted
		List<ResourceReservation> reservations = reservationDao.loadReservations(id);
		Assert.assertNotNull(reservations);
		Assert.assertTrue(reservations.isEmpty());
	}
	
	@Test
	public void testCancelPendingParticipation_deletedGroup() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Reserv-bg-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		
		//create a reservation
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 6);
		ResourceReservation reservation = reservationDao.createReservation(id, "group_participant", cal.getTime(), group.getResource());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		
		//delete the group
		businessGroupLifecycleManager.deleteBusinessGroup(group, id, false);
		dbInstance.commitAndCloseSession();
		
		//accept reservation
		acService.removeReservation(id, id, reservation);
		dbInstance.commitAndCloseSession();
		
		//check that the user is not participant
		boolean participant2 = businessGroupService.hasRoles(id, group, GroupRoles.participant.name());
		Assert.assertFalse(participant2);
		//check that the reservations are deleted
		List<ResourceReservation> reservations = reservationDao.loadReservations(id);
		Assert.assertNotNull(reservations);
		Assert.assertTrue(reservations.isEmpty());
	}
	
	@Test
	public void testAcceptPendingParticipation_deletedGroup() {
		//create a group
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Reserv-bg-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		
		//create a reservation
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 6);
		ResourceReservation reservation = reservationDao.createReservation(id, "group_coach", cal.getTime(), group.getResource());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		
		//delete the group
		businessGroupLifecycleManager.deleteBusinessGroup(group, null, false);
		dbInstance.commitAndCloseSession();
		
		//accept reservation
		acService.acceptReservationToResource(id, reservation);
		dbInstance.commitAndCloseSession();
		
		//check that the reservations are deleted
		List<ResourceReservation> reservations = reservationDao.loadReservations(id);
		Assert.assertNotNull(reservations);
		Assert.assertTrue(reservations.isEmpty());
	}
	
	/**
	 * Test the default settings. Participants are allowed to leave business groups.
	 */
	@Test
	public void allowToLeavingBusinessGroup_defaultSettings() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("leave-auth-1-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-1-");
		BusinessGroup group = businessGroupService.createBusinessGroup(author, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(participant, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertTrue(optionToLeave.isAllowToLeave());
	}
	
	/**
	 * Test the default settings but the author set the business group to "leaving not allowed".
	 */
	@Test
	public void allowToLeavingBusinessGroup_defaultSettings_groupOverride() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("leave-auth-2-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-2-");
		BusinessGroup group = businessGroupService.createBusinessGroup(author, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group = businessGroupService.updateAllowToLeaveBusinessGroup(group, false);
		dbInstance.commitAndCloseSession();

		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(participant, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertFalse(optionToLeave.isAllowToLeave());
		
		ContactList contacts = optionToLeave.getContacts();
		Assert.assertNotNull(contacts);
		Collection<Identity> contactList = contacts.getIdentiEmails().values();
		Assert.assertNotNull(contactList);
		Assert.assertEquals(1, contactList.size());
		Assert.assertTrue(contactList.contains(author));
	}
	
	/**
	 * Override of allow is forbidden system-wide. If a group have the settings not "not allowed to leave",
	 * the setting must be ignored and the participants allowed to leave the group.
	 */
	@Test
	public void allowToLeavingBusinessGroup_overrideForbidden() {
		businessGroupModule.setAllowLeavingGroupOverride(false);

		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("leave-auth-3-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-3-");
		BusinessGroup group = businessGroupService.createBusinessGroup(author, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group = businessGroupService.updateAllowToLeaveBusinessGroup(group, false);
		dbInstance.commitAndCloseSession();

		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(participant, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertTrue(optionToLeave.isAllowToLeave());
	}
	
	/**
	 * Override of allow is forbidden system-wide. If a group have the settings not "not allowed to leave",
	 * the setting must be ignored for learners group but not for authors group.
	 */
	@Test
	public void allowToLeavingBusinessGroup_overrideForbidden_notAllowForAuthorsGroups_butForLearnersGroup() {
		businessGroupModule.setAllowLeavingGroupOverride(false);
		businessGroupModule.setAllowLeavingGroupCreatedByAuthors(false);
		businessGroupModule.setAllowLeavingGroupCreatedByLearners(true);
		
		//authors group
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("leave-auth-4-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-4-");
		BusinessGroup authorsGroup = businessGroupService.createBusinessGroup(author, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, authorsGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		authorsGroup = businessGroupService.updateAllowToLeaveBusinessGroup(authorsGroup, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeaveAuthorsGroup = businessGroupService.isAllowToLeaveBusinessGroup(participant, authorsGroup);
		Assert.assertNotNull(optionToLeaveAuthorsGroup);
		Assert.assertFalse(optionToLeaveAuthorsGroup.isAllowToLeave());
		
		//learners group
		Identity learner = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-learn-4-" + UUID.randomUUID().toString());
		BusinessGroup learnersGroup = businessGroupService.createBusinessGroup(learner, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, learnersGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//set to not allowed to leave
		learnersGroup = businessGroupService.updateAllowToLeaveBusinessGroup(learnersGroup, false);
		dbInstance.commitAndCloseSession();
		
		//check the learners group leaving option
		LeaveOption optionToLeaveLearnersGroup = businessGroupService.isAllowToLeaveBusinessGroup(participant, learnersGroup);
		Assert.assertNotNull(optionToLeaveLearnersGroup);
		Assert.assertTrue(optionToLeaveLearnersGroup.isAllowToLeave());
	}
	
	/**
	 * Override of allow is forbidden system-wide. If a group have the settings not "not allowed to leave",
	 * the setting must be ignored for learners group but not for authors group.
	 */
	@Test
	public void allowToLeavingBusinessGroup_overrideForbidden_notAllowForAuthorsAndLearnersGroups() {
		businessGroupModule.setAllowLeavingGroupOverride(false);
		businessGroupModule.setAllowLeavingGroupCreatedByAuthors(false);
		businessGroupModule.setAllowLeavingGroupCreatedByLearners(false);
		
		//authors group
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("leave-auth-5-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-5-");
		BusinessGroup authorsGroup = businessGroupService.createBusinessGroup(author, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, authorsGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		authorsGroup = businessGroupService.updateAllowToLeaveBusinessGroup(authorsGroup, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeaveAuthorsGroup = businessGroupService.isAllowToLeaveBusinessGroup(participant, authorsGroup);
		Assert.assertNotNull(optionToLeaveAuthorsGroup);
		Assert.assertFalse(optionToLeaveAuthorsGroup.isAllowToLeave());
		
		//learners group
		Identity learner = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-learn-5-" + UUID.randomUUID().toString());
		BusinessGroup learnersGroup = businessGroupService.createBusinessGroup(learner, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(participant, learnersGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//set to not allowed to leave
		learnersGroup = businessGroupService.updateAllowToLeaveBusinessGroup(learnersGroup, false);
		dbInstance.commitAndCloseSession();
		
		//check the learners group leaving option
		LeaveOption optionToLeaveLearnersGroup = businessGroupService.isAllowToLeaveBusinessGroup(participant, learnersGroup);
		Assert.assertNotNull(optionToLeaveLearnersGroup);
		Assert.assertFalse(optionToLeaveLearnersGroup.isAllowToLeave());
	}
	
	@Test
	public void allowToLeavingBusinessGroup_withCourse() {
		//authors group
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-5-");
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, resource);
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group = businessGroupService.updateAllowToLeaveBusinessGroup(group, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(participant, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertFalse(optionToLeave.isAllowToLeave());
		ContactList contacts = optionToLeave.getContacts();
		Collection<Identity> contactList = contacts.getIdentiEmails().values();
		Assert.assertNotNull(contactList);
		Assert.assertFalse(contactList.isEmpty());
		
		for(Identity contact:contactList) {
			Roles roles = securityManager.getRoles(contact);
			Assert.assertNotNull(roles);
			Assert.assertTrue(roles.isAdministrator());
		}
	}
	
	@Test
	public void allowToLeavingBusinessGroup_withCourse_andGroups() {
		//authors group
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity coachCourse = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-8-");
		Identity coachGroup = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-8-");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("leave-bg-8-");
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "Leaving group 8a", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "Leaving group 8b", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, resource);
		repositoryEntryRelationDao.addRole(coachCourse, resource, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(coachGroup, group2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant, group1, GroupRoles.participant.name());

		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group1 = businessGroupService.updateAllowToLeaveBusinessGroup(group1, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(participant, group1);
		Assert.assertNotNull(optionToLeave);
		Assert.assertFalse(optionToLeave.isAllowToLeave());
		ContactList contacts = optionToLeave.getContacts();
		Collection<Identity> contactList = contacts.getIdentiEmails().values();
		Assert.assertNotNull(contactList);
		Assert.assertFalse(contactList.isEmpty());
		Assert.assertEquals(1, contactList.size());
		Assert.assertTrue(contactList.contains(coachCourse));
		Assert.assertFalse(contactList.contains(coachGroup));// coach of other group doesn't receive an email
	}
	
	@Test
	public void allowToLeavingBusinessGroup_subOrganisation() {
		// a special
		String uuid = UUID.randomUUID().toString();
		Organisation organisation = organisationService.getDefaultOrganisation();
		Organisation subOrganisation = organisationService
				.createOrganisation("Sub-organisation", uuid, "", organisation, null);
		
		// create an administrator
		String adminName = "admin" + uuid;
		User adminUser = userManager.createUser("Admin", "Istrator", uuid + "admin@openolat.org");
		Identity adminIdentity = securityManager.createAndPersistIdentityAndUser(null, adminName, null, adminUser,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, adminName, JunitTestHelper.PWD, null);
		organisationService.addMember(subOrganisation, adminIdentity, OrganisationRoles.user);
		organisationService.addMember(subOrganisation, adminIdentity, OrganisationRoles.administrator);
		//create a user
		String userName = "user" + uuid;
		User user = userManager.createUser("Us", "er", uuid + "user@openolat.org");
		Identity userIdentity = securityManager.createAndPersistIdentityAndUser(null, userName, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, userName, JunitTestHelper.PWD, null);
		organisationService.addMember(subOrganisation, userIdentity, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(userIdentity, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group = businessGroupService.updateAllowToLeaveBusinessGroup(group, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(userIdentity, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertFalse(optionToLeave.isAllowToLeave());
		ContactList contacts = optionToLeave.getContacts();
		Collection<Identity> contactList = contacts.getIdentiEmails().values();
		Assert.assertNotNull(contactList);
		Assert.assertFalse(contactList.isEmpty());
		Assert.assertTrue(contactList.contains(adminIdentity));
	}
	
	@Test
	public void allowToLeavingBusinessGroup_subAndMoreOrganisation() {
		// a special
		String uuid = UUID.randomUUID().toString();
		Organisation organisation = organisationService.getDefaultOrganisation();
		Organisation subOrganisation1 = organisationService
				.createOrganisation("Sub-organisation 1", uuid, "", organisation, null);
		Organisation subOrganisation1_1 = organisationService
				.createOrganisation("Sub-organisation 1.1", uuid, "", subOrganisation1, null);
		Organisation subOrganisation2 = organisationService
				.createOrganisation("Sub-organisation 2", uuid, "", organisation, null);
		
		// create an administrator
		String adminName = "admin" + uuid;
		User adminUser = userManager.createUser("Admin", "Istrator", uuid + "admin@openolat.org");
		Identity adminIdentity = securityManager.createAndPersistIdentityAndUser(null, adminName, null, adminUser,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, adminName, "secret-pw", null);
		organisationService.addMember(subOrganisation1, adminIdentity, OrganisationRoles.user);
		organisationService.addMember(subOrganisation1, adminIdentity, OrganisationRoles.administrator);
		// create a second administrator in the second sub organization
		String adminName2 = "admin2" + uuid;
		User adminUser2 = userManager.createUser("Admin", "Istrator", uuid + "admin@openolat.org");
		Identity adminIdentity2 = securityManager.createAndPersistIdentityAndUser(null, adminName2, null, adminUser2,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, adminName2, "secret-pw", null);
		organisationService.addMember(subOrganisation2, adminIdentity2, OrganisationRoles.user);
		organisationService.addMember(subOrganisation2, adminIdentity2, OrganisationRoles.administrator);
		// create a third administrator in the organization under the first sub organization
		String adminName3 = "admin3" + uuid;
		User adminUser3 = userManager.createUser("Admin", "Istrator", uuid + "admin@openolat.org");
		Identity adminIdentity3 = securityManager.createAndPersistIdentityAndUser(null, adminName3, null, adminUser3,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, adminName3, "secret-pw", null);
		organisationService.addMember(subOrganisation1, adminIdentity3, OrganisationRoles.user);
		organisationService.addMember(subOrganisation1_1, adminIdentity3, OrganisationRoles.administrator);
		//create a user
		String userName = "user" + uuid;
		User user = userManager.createUser("Us", "er", uuid + "user@openolat.org");
		Identity userIdentity = securityManager.createAndPersistIdentityAndUser(null, userName, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, userName, "secret-pw", null);
		organisationService.addMember(subOrganisation1, userIdentity, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Leaving group", "But you cannot leave :-(", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(userIdentity, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//set to not allowed to leave
		group = businessGroupService.updateAllowToLeaveBusinessGroup(group, false);
		dbInstance.commitAndCloseSession();

		//check the authors group leaving option
		LeaveOption optionToLeave = businessGroupService.isAllowToLeaveBusinessGroup(userIdentity, group);
		Assert.assertNotNull(optionToLeave);
		Assert.assertFalse(optionToLeave.isAllowToLeave());
		ContactList contacts = optionToLeave.getContacts();
		Collection<Identity> contactList = contacts.getIdentiEmails().values();
		Assert.assertNotNull(contactList);
		Assert.assertFalse(contactList.isEmpty());
		Assert.assertTrue(contactList.contains(adminIdentity));
		Assert.assertFalse(contactList.contains(adminIdentity2));
		Assert.assertFalse(contactList.contains(adminIdentity3));
	}
	
	@Test
	public void parallelRemoveParticipants() {
		mailModule.setInterSystem(true);
		businessGroupModule.setMandatoryEnrolmentEmailFor(OrganisationRoles.user, "true");
		
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndUser("remove-p1-1");
		
		int NUM_OF_THREADS = 5;
		int NUM_OF_GROUPS = 5;
		int NUM_OF_PARTICIPANTS = 10;
		
		//create the members
		List<Identity> members = new ArrayList<>();
		for(int i=0; i<NUM_OF_PARTICIPANTS; i++) {
			Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("remove-p1-1");
			for(int j=0;j<20;j++) {
				members.add(participant);
			}
		}
		dbInstance.commitAndCloseSession();
		
		//prepare the business groups
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<RemoveParticipantsThread> threads = new ArrayList<>(NUM_OF_THREADS);
		for(int i=0; i<NUM_OF_THREADS; i++) {
			List<BusinessGroup> groups = new ArrayList<>();
			for(int j=0;j<NUM_OF_GROUPS;j++) {
				BusinessGroup group = businessGroupService.createBusinessGroup(admin, "move-bg-5", "move-desc", BusinessGroup.BUSINESS_TYPE,
						0, 1, true, true, null);
				for(Identity identity:members) {
					businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
				}
				dbInstance.commitAndCloseSession();
			}
			threads.add(new RemoveParticipantsThread(groups, members, admin, finishCount));
		}		
		
		// remove the participants
		for(RemoveParticipantsThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
			Assert.fail();
		}

		for(RemoveParticipantsThread thread:threads) {
			assertTrue("Subscriber does not exists", thread.isOk());
		}	
		

		businessGroupModule.setMandatoryEnrolmentEmailFor(OrganisationRoles.user, "false");
	}
	
	private class RemoveParticipantsThread extends Thread {

		private boolean ok = false;
		private Identity uIdentity;
		private List<Identity> members;
		private List<BusinessGroup> businessGroups;

		private final List<Exception> exceptionHolder = new ArrayList<>();
		private final CountDownLatch countDown;

		public RemoveParticipantsThread(List<BusinessGroup> businessGroups, List<Identity> members, Identity uIdentity, CountDownLatch countDown) {
			this.members = members;
			this.uIdentity = uIdentity;
			this.businessGroups = businessGroups;
			this.countDown = countDown;
		}
		
		public boolean isOk() {
			return ok;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(10);
				for(BusinessGroup businessGroup:businessGroups) {
					businessGroupService.removeParticipants(uIdentity, members, businessGroup, null);
				}
				ok = true;
			} catch (Exception ex) {
				exceptionHolder.add(ex);// no exception should happen
				ex.printStackTrace();
			} finally {
				countDown.countDown();
				dbInstance.commitAndCloseSession();
			}
		}
	}
}