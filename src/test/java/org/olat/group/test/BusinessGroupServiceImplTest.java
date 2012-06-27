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

package org.olat.group.test;

// um click emulieren:
/*
 * 1) generiere Persistentes Object 2) -> DB...evict() entferne Instanz aus
 * HibernateSession 3) aktionen testen, z.b. update failed, falls object nicht
 * in session
 */
// DB.getInstance().evict();
// DB.getInstance().loadObject(); püft ob schon in hibernate session.
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.util.Encoder;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGConfigFlags;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>TODO: Class Description for BusinessGroupManagerImplTest
 * <P/> Initial Date: Jul 28, 2004
 * 
 * @author patrick
 */

public class BusinessGroupServiceImplTest extends OlatTestCase implements WindowControl {
	//
	private static Logger log = Logger.getLogger(BusinessGroupServiceImplTest.class.getName());
	/*
	 * ::Test Setup::
	 */
	private static Identity id1 = null;
	private static Identity id2 = null;
	private static Identity id3 = null;
	private static Identity id4 = null;
	// For WaitingGroup tests
	private static Identity wg1 = null;
	private static Identity wg2 = null;
	private static Identity wg3 = null;
	private static Identity wg4 = null;

	/*
	 * BuddyGroup one
	 */
	private static BusinessGroup one = null;
	private String oneName = "First BuddyGroup";
	private String oneDesc = "some short description for first buddygroup";
	// private String oneIntr = "bla blusch blip blup blep";
	/*
	 * BuddyGroup two
	 */
	private static BusinessGroup two = null;
	private String twoName = "Second BuddyGroup";
	private String twoDesc = "some short description for second buddygroup";
	// private String twoIntr = "notting";
	/*
	 * BuddyGroup three
	 */
	private static BusinessGroup three = null;
	private String threeName = "Third BuddyGroup";
	private String threeDesc = "some short description for second buddygroup";
	// private String threeIntr = "notting more";
	//
	private static final int NROFTESTCASES = 3;
	private static int nrOfTestCasesAlreadyRun = 0;
	private static boolean suiteIsAborted = true;
	// For WaitingGroup tests
	private static BusinessGroup bgWithWaitingList = null;
	
	
	@Autowired
	private BusinessGroupService businessGroupService;

	private static boolean initialize = false;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		if(initialize) return;
		
			// Identities
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1");
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("id2");
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("id3");
			id4 = JunitTestHelper.createAndPersistIdentityAsUser("id4");
			// buddyGroups without waiting-list: groupcontext is null
			List<BusinessGroup> l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
			if (l.size() == 0) {
				one = businessGroupService.createBusinessGroup(id1, oneName, oneDesc, BusinessGroup.TYPE_BUDDYGROUP, -1, -1, false, false, null);
			} else {
				List<BusinessGroup> groups = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
				for(BusinessGroup group:groups) {
					if(oneName.equals(group.getName())) {
						one = group;
					}
				}
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
			if (l.size() == 0) {
				two = businessGroupService.createBusinessGroup(id2, twoName, twoDesc, BusinessGroup.TYPE_BUDDYGROUP, -1, -1, false, false, null);
				SecurityGroup twoPartips = two.getPartipiciantGroup();
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id3, twoPartips);
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id4, twoPartips);
			} else {
				two = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null).get(0);
			}
			l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
			if (l.size() == 0) {
				three = businessGroupService.createBusinessGroup(id3, threeName, threeDesc, BusinessGroup.TYPE_BUDDYGROUP, -1, -1, false, false, null);
				SecurityGroup threeOwner = three.getOwnerGroup();
				SecurityGroup threeOPartips = three.getPartipiciantGroup();
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id2, threeOPartips);
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id1, threeOwner);
			} else {
				three = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null).get(0);
			}
			/*
			 * Membership in ParticipiantGroups............................. id1
			 * owns BuddyGroup one with participiantGroup:={}........... id2 owns
			 * BuddyGroup two with participiantGroup:={id3,id4} id3 owns BuddyGroup
			 * three participiantGroup:={id2}, ownerGroup:={id3,id1}
			 */

			DBFactory.getInstance().closeSession();

			// create business-group with waiting-list
			String bgWithWaitingListName = "Group with WaitingList";
			String bgWithWaitingListDesc = "some short description for Group with WaitingList";
			Boolean enableWaitinglist = new Boolean(true);
			Boolean enableAutoCloseRanks = new Boolean(true);
			OLATResource resource = JunitTestHelper.createRandomResource();
			System.out.println("testAddToWaitingListAndFireEvent: resource=" + resource);
			bgWithWaitingList = businessGroupService.createBusinessGroup(id1, bgWithWaitingListName,
					bgWithWaitingListDesc, BusinessGroup.TYPE_LEARNINGROUP, -1, -1, enableWaitinglist, enableAutoCloseRanks, resource);
			bgWithWaitingList.setMaxParticipants(new Integer(2));
			// Identities
			String suffix = UUID.randomUUID().toString();
			User UserWg1 = UserManager.getInstance().createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg1 = BaseSecurityManager.getInstance().createAndPersistIdentity(suffix, UserWg1,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg1"));
			suffix = UUID.randomUUID().toString();
			User UserWg2 = UserManager.getInstance().createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg2 = BaseSecurityManager.getInstance().createAndPersistIdentity(suffix, UserWg2,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg2"));
			suffix = UUID.randomUUID().toString();
			User UserWg3 = UserManager.getInstance().createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg3 = BaseSecurityManager.getInstance().createAndPersistIdentity(suffix, UserWg3,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg3"));
			suffix = UUID.randomUUID().toString();
			User UserWg4 = UserManager.getInstance().createAndPersistUser("FirstName_" + suffix, "LastName_" + suffix, suffix + "_junittest@olat.unizh.ch");
			wg4 = BaseSecurityManager.getInstance().createAndPersistIdentity(suffix, UserWg4,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), suffix, Encoder.encrypt("wg4"));

			DBFactory.getInstance().closeSession();
			
			initialize = true;
	}

	@Test
	public void testCheckIfNamesExistsInContext() throws Exception {
		suiteIsAborted = true;

		OLATResource ctxA = JunitTestHelper.createRandomResource();
		OLATResource ctxB = JunitTestHelper.createRandomResource();

		String[] namesInCtxA = new String[] { "A-GroupOne", "A-GroupTwo", "A-GroupThree", "A-GroupFour", "A-GroupFive", "A-GroupSix" };
		String[] namesInCtxB = new String[] { "B-GroupAAA", "B-GroupBBB", "B-GroupCCC", "B-GroupDDD", "B-GroupEEE", "B-GroupFFF" };
		BusinessGroup[] ctxAgroups = new BusinessGroup[namesInCtxA.length];
		BusinessGroup[] ctxBgroups = new BusinessGroup[namesInCtxB.length];

		for (int i = 0; i < namesInCtxA.length; i++) {
			ctxAgroups[i] = businessGroupService.createBusinessGroup(id1, namesInCtxA[i], null, BusinessGroup.TYPE_LEARNINGROUP, 0, 0, false,
					false, ctxA);
		}
		for (int i = 0; i < namesInCtxB.length; i++) {
			ctxBgroups[i] = businessGroupService.createBusinessGroup(id1, namesInCtxB[i], null, BusinessGroup.TYPE_LEARNINGROUP, 0, 0, false,
					false, ctxB);
		}
		// first click created two context and each of them containg groups
		// evict all created and search
		System.out.println("Test: ctxAgroups.length=" + ctxAgroups.length);
		for (int i = 0; i < ctxAgroups.length; i++) {
			System.out.println("Test: i=" + i);
			System.out.println("Test: ctxAgroups[i]=" + ctxAgroups[i]);
		}
		DBFactory.getInstance().closeSession();

		// next click needs to check of a set of groupnames already exists.
		Set<String> subsetOkInA = new HashSet<String>();
		subsetOkInA.add("A-GroupTwo");
		subsetOkInA.add("A-GroupThree");
		subsetOkInA.add("A-GroupFour");
		
		Set<String> subsetNOkInA = new HashSet<String>();
		subsetNOkInA.add("A-GroupTwo");
		subsetNOkInA.add("NOT-IN-A");
		subsetNOkInA.add("A-GroupThree");
		subsetNOkInA.add("A-GroupFour");

		Set<String> subsetOkInB = new HashSet<String>();
		subsetOkInB.add("B-GroupCCC");
		subsetOkInB.add("B-GroupDDD");
		subsetOkInB.add("B-GroupEEE");
		subsetOkInB.add("B-GroupFFF");

		Set<String> subsetNOkInB = new HashSet<String>();
		subsetNOkInB.add("B-GroupCCC");
		subsetNOkInB.add("NOT-IN-B");
		subsetNOkInB.add("B-GroupEEE");
		subsetNOkInB.add("B-GroupFFF");

		Set<String> setSpansAandBNok = new HashSet<String>();
		setSpansAandBNok.add("B-GroupCCC");
		setSpansAandBNok.add("A-GroupTwo");
		setSpansAandBNok.add("A-GroupThree");
		setSpansAandBNok.add("B-GroupEEE");
		setSpansAandBNok.add("B-GroupFFF");

		boolean allExist = false;
		allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxA);
		assertTrue("Three A-Group.. should find all", allExist);
		// Check : one name does not exist, 3 exist 
		assertTrue("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetNOkInA, ctxA));
		// Check : no name exist in context
		assertFalse("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxA));
		//
		allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxB);
		assertTrue("Three B-Group.. should find all", allExist);
		// Check : one name does not exist, 3 exist
		assertTrue("A 'NOT-IN-B'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetNOkInB, ctxB));
		// Check : no name exist in context
		assertFalse("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxB));
		// Mix A (2x) and B (3x)
		allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxA);
		assertTrue("Groupnames spanning two context... should not find all in context A", allExist);
		// Mix A (2x) and B (3x)
		allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxB);
		assertTrue("Groupnames spanning two context... should not find all in context B", allExist);
		//

		suiteIsAborted = false;
		nrOfTestCasesAlreadyRun++;
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
		suiteIsAborted = true;
		/*
		 * 
		 */
		List<BusinessGroup> sqlRes;
		BusinessGroup found;
		/*
		 * id1
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
		assertTrue("2 BuddyGroups owned by id1", sqlRes.size() == 2);
		for (int i = 0; i < sqlRes.size(); i++) {
			assertTrue("It's a BuddyGroup Object", sqlRes.get(i) instanceof BusinessGroup);
			found = (BusinessGroup) sqlRes.get(i);
			// equality by comparing PersistenObject.getKey()!!!
			boolean ok = one.getKey().longValue() == found.getKey().longValue() || three.getKey().longValue() == found.getKey().longValue();
			assertTrue("It's the correct BuddyGroup", ok);

		}
		sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
		assertTrue("0 BuddyGroup where id1 is partipicating", sqlRes.size() == 0);

		/*
		 * id2
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		// equality by comparing PersistenObject.getKey()!!!
		assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());
		sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("1 BuddyGroup where id2 is partipicating", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());

		/*
		 * id3
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
		assertTrue("1 BuddyGroup owned by id3", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		// equality by comparing PersistenObject.getKey()!!!
		assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());
		sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
		assertTrue("1 BuddyGroup where id3 is partipicating", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());

		/*
		 * id4
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
		assertTrue("0 BuddyGroup owned by id4", sqlRes.size() == 0);
		//
		sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
		assertTrue("1 BuddyGroup where id4 is partipicating", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());
		/*
		 * 
		 */
		suiteIsAborted = false;
		nrOfTestCasesAlreadyRun++;
	}

	/**
	 * checks if tools can be enabled disabled or checked against being enabled.
	 * TOols are configured with the help of the generic properties storage.
	 * 
	 * @throws Exception
	 */
	public void testEnableDisableAndCheckForTool() throws Exception {
		suiteIsAborted = true;
		/*
		 * 
		 */
		List<BusinessGroup> sqlRes;
		BusinessGroup found;

		/*
		 * id2
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		found = (BusinessGroup) sqlRes.get(0);
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			// all tools are disabled by default exept the news tool
			assertTrue(msg, !enabled);

		}
		//
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
		}
		//
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			assertTrue(msg, enabled);

		}
		//
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], false);
		}
		//
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			String msg = "Tool " + CollaborationTools.TOOLS[i] + " is disabled";
			boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
			assertTrue(msg, !enabled);

		}
		/*
		 * 
		 */
		suiteIsAborted = false;
		nrOfTestCasesAlreadyRun++;
	}

	/**
	 * test if removing a BuddyGroup really deletes everything it should.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteBuddyGroup() throws Exception {
		suiteIsAborted = true;
		/*
		 * 
		 */
		List<BusinessGroup> sqlRes;
		BusinessGroup found;

		/*
		 * id2
		 */
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
		found = (BusinessGroup) sqlRes.get(0);
		CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
		//
		for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
			myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
		}
		/*
		 * 
		 */
		businessGroupService.deleteBusinessGroup(found);
		sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("0 BuddyGroup owned by id2", sqlRes.size() == 0);
		/*
		 * 
		 */
		suiteIsAborted = false;
		nrOfTestCasesAlreadyRun++;
	}

	// Test for WaitingList
	// /////////////////////
	/**
	 * Add 3 idenities to the waiting list and check the position. before test
	 * Waitinglist=[]<br>
	 * after test Waitinglist=[wg2,wg3,wg4]
	 */
	@Test
	public void testAddToWaitingListAndFireEvent() throws Exception {
		System.out.println("testAddToWaitingListAndFireEvent: start...");
		// Add wg2
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg2);
		businessGroupService.addToWaitingList(wg2, identities, bgWithWaitingList, flags);
		// Add wg3
		identities = new ArrayList<Identity>();
		identities.add(wg3);
		businessGroupService.addToWaitingList(wg3, identities, bgWithWaitingList, flags);
		// Add wg4
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.addToWaitingList(wg4, identities, bgWithWaitingList, flags);
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
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg3);
		businessGroupService.removeFromWaitingList(wg1, identities, bgWithWaitingList, flags);
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
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.moveIdentityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
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
		// Add a user to waiting-list which is allready in participant-list and try
		// and try to move this user => user will be removed from waiting-list
		// Add again wg2
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg1);
		businessGroupService.addToWaitingList(wg4, identities, bgWithWaitingList, flags);
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		businessGroupService.moveIdentityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
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
				"deleteTestGroup-1", BusinessGroup.TYPE_LEARNINGROUP, -1, -1, withWaitingList, true, resource);
		
		Long ownerGroupKey = deleteTestGroup.getOwnerGroup().getKey();
		Long partipiciantGroupKey = deleteTestGroup.getPartipiciantGroup().getKey();
		Long waitingGroupKey = deleteTestGroup.getWaitingGroup().getKey();
		
		assertNotNull("Could not find owner-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, ownerGroupKey));
		assertNotNull("Could not find partipiciant-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, partipiciantGroupKey));
		assertNotNull("Could not find waiting-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, waitingGroupKey));
		businessGroupService.deleteBusinessGroup(deleteTestGroup);
		assertNull("owner-group still exist after delete",DBFactory.getInstance().findObject(SecurityGroupImpl.class, ownerGroupKey));
		assertNull("partipiciant-group still exist after delete",DBFactory.getInstance().findObject(SecurityGroupImpl.class, partipiciantGroupKey));
		assertNull("waiting-group still exist after delete",DBFactory.getInstance().findObject(SecurityGroupImpl.class, waitingGroupKey));
	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		try {
			if (suiteIsAborted || (NROFTESTCASES - nrOfTestCasesAlreadyRun) == 0) {
				// DB.getInstance().clearDatabase();
			}
			DBFactory.getInstance().closeSession();

		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}



	// Implements interface WindowControl
	// ///////////////////////////////////
	public void pushToMainArea(Component comp) {};

	public void pushAsModalDialog(Component comp) {};

	public void pop() {};

	public void setInfo(String string) {};

	public void setError(String string) {};

	public void setWarning(String string) {};

	public DTabs getDTabs() {
		return null;
	};

	public WindowControlInfo getWindowControlInfo() {
		return null;
	};

	public void makeFlat() {};

	public BusinessControl getBusinessControl() {
		return null;
	}

	public WindowBackOffice getWindowBackOffice() {
		// TODO Auto-generated method stub
		return null;
	};

}