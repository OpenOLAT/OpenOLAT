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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.resource.OLATResource;
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
	private static Identity wg2 = null;
	private static Identity wg3 = null;
	private static Identity wg4 = null;

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
			OLATResource resource = JunitTestHelper.createRandomResource();
			System.out.println("testAddToWaitingListAndFireEvent: resource=" + resource);
			bgWithWaitingList = businessGroupService.createBusinessGroup(id1, bgWithWaitingListName,
					bgWithWaitingListDesc, -1, -1, enableWaitinglist, enableAutoCloseRanks, resource);
			bgWithWaitingList.setMaxParticipants(new Integer(2));
			// Identities
			String suffix = UUID.randomUUID().toString();
			User UserWg1 = userManager.createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg1 = securityManager.createAndPersistIdentity(suffix, UserWg1,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg1"));
			suffix = UUID.randomUUID().toString();
			User UserWg2 = userManager.createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg2 = securityManager.createAndPersistIdentity(suffix, UserWg2,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg2"));
			suffix = UUID.randomUUID().toString();
			User UserWg3 = userManager.createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg3 = securityManager.createAndPersistIdentity(suffix, UserWg3,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg3"));
			suffix = UUID.randomUUID().toString();
			User UserWg4 = userManager.createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg4 = securityManager.createAndPersistIdentity(suffix, UserWg4,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg4"));

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
		OLATResource resource =  JunitTestHelper.createRandomResource();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "gdao", "gdao-desc", -1, -1, false, false, resource);
		
		//commit the group
		dbInstance.commit();
		Assert.assertNotNull(group);
	}
	
	
	@Test
	public void loadBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(null, false, false);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, 5);
		Assert.assertNotNull(groups);
	}
	
	@Test
	public void testGroupsOfBGContext() {
		OLATResource c1 = JunitTestHelper.createRandomResource();
		OLATResource c2 = JunitTestHelper.createRandomResource();

		dbInstance.commitAndCloseSession(); // simulate user clicks
		SearchBusinessGroupParams params1 = new SearchBusinessGroupParams(null, false, false);
		assertTrue(businessGroupService.findBusinessGroups(params1, c1, 0, -1).isEmpty());
		assertTrue(businessGroupService.countBusinessGroups(params1, c1) == 0);

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
		Assert.assertEquals(3, businessGroupService.findBusinessGroups(params2, c1, 0, -1).size());
		Assert.assertEquals(3, businessGroupService.countBusinessGroups(params2, c1));
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

	/**
	 * checks if tools can be enabled disabled or checked against being enabled.
	 * TOols are configured with the help of the generic properties storage.
	 * 
	 * @throws Exception
	 */
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

	///////////////////////
	// Test for WaitingList
	///////////////////////
	/**
	 * Add 3 idenities to the waiting list and check the position. before test
	 * Waitinglist=[]<br>
	 * after test Waitinglist=[wg2,wg3,wg4]
	 */
	@Test
	public void testAddToWaitingListAndFireEvent() throws Exception {
		System.out.println("testAddToWaitingListAndFireEvent: start...");
		// Add wg2
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg2);
		businessGroupService.addToWaitingList(wg2, identities, bgWithWaitingList);
		// Add wg3
		identities = new ArrayList<Identity>();
		identities.add(wg3);
		businessGroupService.addToWaitingList(wg3, identities, bgWithWaitingList);
		// Add wg4
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.addToWaitingList(wg4, identities, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent: 3 user added to waiting list");

		// Check position of 'wg2'
		int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check position of 'wg3'
		pos = businessGroupService.getPositionInWaitingListFor(wg3, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent wg3: pos=" + pos);
		assertTrue("pos must be 2, bit is=" + pos, pos == 2);
		// Check position of 'wg4'
		pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent wg4: pos=" + pos);
		assertTrue("pos must be 3, bit is=" + pos, pos == 3);
	}

	/**
	 * Remove identity 2 (wg3) from the waiting list and check the position of
	 * identity 2. before test Waitinglist=[wg2,wg3,wg4]<br>
	 * after test Waitinglist=[wg2,wg4]
	 */
	@Test
	public void testRemoveFromWaitingListAndFireEvent() throws Exception {
		System.out.println("testRemoveFromWaitingListAndFireEvent: start...");
		// Remove wg3
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg3);
		businessGroupService.removeFromWaitingList(wg1, identities, bgWithWaitingList);
		// Check position of 'wg2'
		int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testRemoveFromWaitingListAndFireEvent: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check position of 'wg4'
		pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
		System.out.println("testRemoveFromWaitingListAndFireEvent wg4: pos=" + pos);
		assertTrue("pos must be 2, bit is=" + pos, pos == 2);

	}

	/**
	 * Move identity 4 (wg4) from the waiting list to participant list. before
	 * test Waitinglist=[wg2,wg4]<br>
	 * after test Waitinglist=[wg2]<br>
	 * participant-list=[wg4]
	 */
	@Test
	public void testMoveIdenityFromWaitingListToParticipant() throws Exception {
		System.out.println("testMoveIdenityFromWaitingListToParticipant: start...");
		// Check that 'wg4' is not in participant list
		assertFalse("Identity is allready in participant-list, remove it(dbsetup?)", businessGroupService
				.isIdentityInBusinessGroup(wg4, bgWithWaitingList));

		// Move wg4 from waiting-list to participant
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.moveIdentityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList);
		// Check position of 'wg2'
		int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testMoveIdenityFromWaitingListToParticipant: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check if 'wg4' is in participant-list
		assertTrue("Identity is not in participant-list", businessGroupService.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
	}
	
	@Test
	public void testMoveRegisteredIdentityFromWaitingToParticipant() throws Exception {
		System.out.println("testMoveRegisteredIdentityFromWaitingToParticipant: start...");
		// Add a user to waiting-list which is already in participant-list and try
		// and try to move this user => user will be removed from waiting-list
		// Add again wg2
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg1);
		businessGroupService.addToWaitingList(wg4, identities, bgWithWaitingList);
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.moveIdentityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList);
		// Check position of 'wg4'
		int pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
		System.out.println("testMoveIdenityFromWaitingListToParticipant: wg4 pos=" + pos);
		assertTrue("pos must be 0, bit is=" + pos, pos == 0);
		// Check if 'wg4' is still in participant-list
		assertTrue("Identity is not in participant-list", businessGroupService.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
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
		OLATResource resource = JunitTestHelper.createRandomResource();

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