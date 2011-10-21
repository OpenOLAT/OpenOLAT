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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.group;

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
import java.util.Locale;
import java.util.Set;

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
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.util.Encoder;
import org.olat.core.util.Util;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.ui.BGConfigFlags;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;

/**
 * Description: <BR/>TODO: Class Description for BusinessGroupManagerImplTest
 * <P/> Initial Date: Jul 28, 2004
 * 
 * @author patrick
 */

public class BusinessGroupManagerImplTest extends OlatTestCase implements WindowControl {
	//
	private static Logger log = Logger.getLogger(BusinessGroupManagerImplTest.class.getName());
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
	private static boolean isInitialized;
	private static final int NROFTESTCASES = 3;
	private static int nrOfTestCasesAlreadyRun = 0;
	private static boolean suiteIsAborted = true;
	// For WaitingGroup tests
	private static Translator testTranslator = null;
	private static BusinessGroup bgWithWaitingList = null;

	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
			BusinessGroupManager bgManager = BusinessGroupManagerImpl.getInstance();
			// Identities
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1");
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("id2");
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("id3");
			id4 = JunitTestHelper.createAndPersistIdentityAsUser("id4");
			// buddyGroups without waiting-list: groupcontext is null
			List l = bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
			if (l.size() == 0) {
				one = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id1, oneName, oneDesc, null, null, false, false, null);
			} else {
				List<BusinessGroup> groups = bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
				for(BusinessGroup group:groups) {
					if(oneName.equals(group.getName())) {
						one = group;
					}
				}
			}
			l = bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
			if (l.size() == 0) {
				two = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id2, twoName, twoDesc, null, null, false, false, null);
				SecurityGroup twoPartips = two.getPartipiciantGroup();
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id3, twoPartips);
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id4, twoPartips);
			} else {
				two = (BusinessGroup) bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null).get(0);
			}
			l = bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
			if (l.size() == 0) {
				three = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id3, threeName, threeDesc, null,	null, false, false, null);
				SecurityGroup threeOwner = three.getOwnerGroup();
				SecurityGroup threeOPartips = three.getPartipiciantGroup();
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id2, threeOPartips);
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(id1, threeOwner);
			} else {
				three = (BusinessGroup) bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null).get(0);
			}
			/*
			 * Membership in ParticipiantGroups............................. id1
			 * owns BuddyGroup one with participiantGroup:={}........... id2 owns
			 * BuddyGroup two with participiantGroup:={id3,id4} id3 owns BuddyGroup
			 * three participiantGroup:={id2}, ownerGroup:={id3,id1}
			 */

			DBFactory.getInstance().closeSession();

			setupWaitingList(bgManager);
			/*
			 * phuuu finally initialized
			 */	
	}

	@Test
	public void testCheckIfNamesExistsInContext() throws Exception {
		suiteIsAborted = true;

		BusinessGroupManagerImpl bgManager = (BusinessGroupManagerImpl)BusinessGroupManagerImpl.getInstance();
		BGContextManager bgContextManager = BGContextManagerImpl.getInstance();
		BGContext ctxA = bgContextManager.createAndPersistBGContext("DefaultA", "Empty", BusinessGroup.TYPE_LEARNINGROUP, id1, true);
		BGContext ctxB = bgContextManager.createAndPersistBGContext("DefaultB", "Empty", BusinessGroup.TYPE_LEARNINGROUP, id1, true);

		String[] namesInCtxA = new String[] { "A-GroupOne", "A-GroupTwo", "A-GroupThree", "A-GroupFour", "A-GroupFive", "A-GroupSix" };
		String[] namesInCtxB = new String[] { "B-GroupAAA", "B-GroupBBB", "B-GroupCCC", "B-GroupDDD", "B-GroupEEE", "B-GroupFFF" };
		BusinessGroup[] ctxAgroups = new BusinessGroup[namesInCtxA.length];
		BusinessGroup[] ctxBgroups = new BusinessGroup[namesInCtxB.length];

		for (int i = 0; i < namesInCtxA.length; i++) {
			ctxAgroups[i] = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, namesInCtxA[i], null, 0, 0, false,
					false, ctxA);
		}
		for (int i = 0; i < namesInCtxB.length; i++) {
			ctxBgroups[i] = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, namesInCtxB[i], null, 0, 0, false,
					false, ctxB);
		}
		// first click created two context and each of them containg groups
		// evict all created and search
		System.out.println("Test: ctxAgroups.length=" + ctxAgroups.length);
		for (int i = 0; i < ctxAgroups.length; i++) {
			System.out.println("Test: i=" + i);
			System.out.println("Test: ctxAgroups[i]=" + ctxAgroups[i]);
			DBFactory.getInstance().closeSession();
		}
		for (int i = 0; i < ctxBgroups.length; i++) {
			DBFactory.getInstance().closeSession();
		}
		// next click needs to check of a set of groupnames already exists.
		Set subsetOkInA = new HashSet() {
			{
				add("A-GroupTwo");
				add("A-GroupThree");
				add("A-GroupFour");
			}
		};
		Set subsetNOkInA = new HashSet() {
			{
				add("A-GroupTwo");
				add("NOT-IN-A");
				add("A-GroupThree");
				add("A-GroupFour");
			}
		};
		Set subsetOkInB = new HashSet() {
			{
				add("B-GroupCCC");
				add("B-GroupDDD");
				add("B-GroupEEE");
				add("B-GroupFFF");
			}
		};
		Set subsetNOkInB = new HashSet() {
			{
				add("B-GroupCCC");
				add("NOT-IN-B");
				add("B-GroupEEE");
				add("B-GroupFFF");
			}
		};
		Set setSpansAandBNok = new HashSet() {
			{
				add("B-GroupCCC");
				add("A-GroupTwo");
				add("A-GroupThree");
				add("B-GroupEEE");
				add("B-GroupFFF");
			}
		};

		boolean allExist = false;
		allExist = bgManager.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxA);
		assertTrue("Three A-Group.. should find all", allExist);
		// Check : one name does not exist, 3 exist 
		assertTrue("A 'NOT-IN-A'.. should not find all", bgManager.checkIfOneOrMoreNameExistsInContext(subsetNOkInA, ctxA));
		// Check : no name exist in context
		assertFalse("A 'NOT-IN-A'.. should not find all", bgManager.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxA));
		//
		allExist = bgManager.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxB);
		assertTrue("Three B-Group.. should find all", allExist);
		// Check : one name does not exist, 3 exist
		assertTrue("A 'NOT-IN-B'.. should not find all", bgManager.checkIfOneOrMoreNameExistsInContext(subsetNOkInB, ctxB));
		// Check : no name exist in context
		assertFalse("A 'NOT-IN-A'.. should not find all", bgManager.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxB));
		// Mix A (2x) and B (3x)
		allExist = bgManager.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxA);
		assertTrue("Groupnames spanning two context... should not find all in context A", allExist);
		// Mix A (2x) and B (3x)
		allExist = bgManager.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxB);
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
		List sqlRes;
		BusinessGroup found;
		/*
		 * id1
		 */
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
		assertTrue("2 BuddyGroups owned by id1", sqlRes.size() == 2);
		for (int i = 0; i < sqlRes.size(); i++) {
			assertTrue("It's a BuddyGroup Object", sqlRes.get(i) instanceof BusinessGroup);
			found = (BusinessGroup) sqlRes.get(i);
			// equality by comparing PersistenObject.getKey()!!!
			boolean ok = one.getKey().longValue() == found.getKey().longValue() || three.getKey().longValue() == found.getKey().longValue();
			assertTrue("It's the correct BuddyGroup", ok);

		}
		sqlRes = myManager.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
		assertTrue("0 BuddyGroup where id1 is partipicating", sqlRes.size() == 0);

		/*
		 * id2
		 */
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		// equality by comparing PersistenObject.getKey()!!!
		assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());
		sqlRes = myManager.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
		assertTrue("1 BuddyGroup where id2 is partipicating", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());

		/*
		 * id3
		 */
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
		assertTrue("1 BuddyGroup owned by id3", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		// equality by comparing PersistenObject.getKey()!!!
		assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());
		sqlRes = myManager.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
		assertTrue("1 BuddyGroup where id3 is partipicating", sqlRes.size() == 1);
		assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
		found = (BusinessGroup) sqlRes.get(0);
		assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());

		/*
		 * id4
		 */
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
		assertTrue("0 BuddyGroup owned by id4", sqlRes.size() == 0);
		//
		sqlRes = myManager.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
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
		List sqlRes;
		BusinessGroup found;

		/*
		 * id2
		 */
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();

		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
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
		List sqlRes;
		BusinessGroup found;

		/*
		 * id2
		 */
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
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
		myManager.deleteBusinessGroup(found);
		sqlRes = myManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
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
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();

		// Add wg2
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg2);
		myManager.addToWaitingListAndFireEvent(wg2, identities, bgWithWaitingList, flags);
		// Add wg3
		identities = new ArrayList<Identity>();
		identities.add(wg3);
		myManager.addToWaitingListAndFireEvent(wg3, identities, bgWithWaitingList, flags);
		// Add wg4
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		myManager.addToWaitingListAndFireEvent(wg4, identities, bgWithWaitingList, flags);
		System.out.println("testAddToWaitingListAndFireEvent: 3 user added to waiting list");

		// Check position of 'wg2'
		int pos = myManager.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check position of 'wg3'
		pos = myManager.getPositionInWaitingListFor(wg3, bgWithWaitingList);
		System.out.println("testAddToWaitingListAndFireEvent wg3: pos=" + pos);
		assertTrue("pos must be 2, bit is=" + pos, pos == 2);
		// Check position of 'wg4'
		pos = myManager.getPositionInWaitingListFor(wg4, bgWithWaitingList);
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
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();
		// Remove wg3
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg3);
		myManager.removeFromWaitingListAndFireEvent(wg1, identities, bgWithWaitingList, flags);
		// Check position of 'wg2'
		int pos = myManager.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testRemoveFromWaitingListAndFireEvent: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check position of 'wg4'
		pos = myManager.getPositionInWaitingListFor(wg4, bgWithWaitingList);
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
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();
		// Check that 'wg4' is not in participant list
		assertFalse("Identity is allready in participant-list, remove it(dbsetup?)", myManager
				.isIdentityInBusinessGroup(wg4, bgWithWaitingList));

		// Move wg4 from waiting-list to participant
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg4);
		myManager.moveIdenityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
		// Check position of 'wg2'
		int pos = myManager.getPositionInWaitingListFor(wg2, bgWithWaitingList);
		System.out.println("testMoveIdenityFromWaitingListToParticipant: wg2 pos=" + pos);
		assertTrue("pos must be 1, bit is=" + pos, pos == 1);
		// Check if 'wg4' is in participant-list
		assertTrue("Identity is not in participant-list", myManager.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
	}
	@Test
	public void testMoveRegisteredIdentityFromWaitingToParticipant() throws Exception {
		System.out.println("testMoveRegisteredIdentityFromWaitingToParticipant: start...");
		BusinessGroupManager myManager = BusinessGroupManagerImpl.getInstance();
		// Add a user to waiting-list which is allready in participant-list and try
		// and try to move this user => user will be removed from waiting-list
		// Add again wg2
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		List<Identity> identities = new ArrayList<Identity>();
		identities.add(wg1);
		myManager.addToWaitingListAndFireEvent(wg4, identities, bgWithWaitingList, flags);
		identities = new ArrayList<Identity>();
		identities.add(wg4);
		myManager.moveIdenityFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
		// Check position of 'wg4'
		int pos = myManager.getPositionInWaitingListFor(wg4, bgWithWaitingList);
		System.out.println("testMoveIdenityFromWaitingListToParticipant: wg4 pos=" + pos);
		assertTrue("pos must be 0, bit is=" + pos, pos == 0);
		// Check if 'wg4' is still in participant-list
		assertTrue("Identity is not in participant-list", myManager.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
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
		BGContextManager bgcm = BGContextManagerImpl.getInstance();
		BGContext groupContext = bgcm.createAndPersistBGContext("c1delete", "c1delete", BusinessGroup.TYPE_LEARNINGROUP, null, true);

		BusinessGroup deleteTestGroup = BusinessGroupManagerImpl.getInstance().createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, "deleteTestGroup-1",
				"deleteTestGroup-1", null, null, withWaitingList, true, groupContext);
		
		Long ownerGroupKey = deleteTestGroup.getOwnerGroup().getKey();
		Long partipiciantGroupKey = deleteTestGroup.getPartipiciantGroup().getKey();
		Long waitingGroupKey = deleteTestGroup.getWaitingGroup().getKey();
		
		assertNotNull("Could not find owner-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, ownerGroupKey));
		assertNotNull("Could not find partipiciant-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, partipiciantGroupKey));
		assertNotNull("Could not find waiting-group",DBFactory.getInstance().findObject(SecurityGroupImpl.class, waitingGroupKey));
		BusinessGroupManagerImpl.getInstance().deleteBusinessGroup(deleteTestGroup);
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

	// Helper methods
	// ///////////////
	private void setupWaitingList(BusinessGroupManager bgManager) {
		if (bgManager.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, id1, null).size() == 0) {
			// create business-group with waiting-list
			String bgWithWaitingListName = "Group with WaitingList";
			String bgWithWaitingListDesc = "some short description for Group with WaitingList";
			Boolean enableWaitinglist = new Boolean(true);
			Boolean enableAutoCloseRanks = new Boolean(true);
			BGContextManager bgcm = BGContextManagerImpl.getInstance();
			BGContext groupContext = bgcm.createAndPersistBGContext("c1name", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
			System.out.println("testAddToWaitingListAndFireEvent: groupContext=" + groupContext);
			bgWithWaitingList = bgManager.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, bgWithWaitingListName,
					bgWithWaitingListDesc, null, null, enableWaitinglist, enableAutoCloseRanks, groupContext);
			bgWithWaitingList.setMaxParticipants(new Integer(2));
			// create mock objects
			String PACKAGE = Util.getPackageName(BusinessGroupManagerImplTest.class);
			testTranslator = new PackageTranslator(PACKAGE, new Locale("de"));
			// Identities
			User UserWg1 = UserManager.getInstance().createAndPersistUser("FirstName_wg1", "LastName_wg1", "wg1_junittest@olat.unizh.ch");
			wg1 = BaseSecurityManager.getInstance().createAndPersistIdentity("wg1", UserWg1,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), "wg1", Encoder.encrypt("wg1"));
			User UserWg2 = UserManager.getInstance().createAndPersistUser("FirstName_wg2", "LastName_wg2", "wg2_junittest@olat.unizh.ch");
			wg2 = BaseSecurityManager.getInstance().createAndPersistIdentity("wg2", UserWg2,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), "wg2", Encoder.encrypt("wg2"));
			User UserWg3 = UserManager.getInstance().createAndPersistUser("FirstName_wg3", "LastName_wg3", "wg3_junittest@olat.unizh.ch");
			wg3 = BaseSecurityManager.getInstance().createAndPersistIdentity("wg3", UserWg3,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), "wg3", Encoder.encrypt("wg3"));
			User UserWg4 = UserManager.getInstance().createAndPersistUser("FirstName_wg4", "LastName_wg4", "wg4_junittest@olat.unizh.ch");
			wg4 = BaseSecurityManager.getInstance().createAndPersistIdentity("wg4", UserWg4,
					BaseSecurityModule.getDefaultAuthProviderIdentifier(), "wg4", Encoder.encrypt("wg4"));
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