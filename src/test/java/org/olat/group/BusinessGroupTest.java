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

package org.olat.group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.right.BGRightManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(BusinessGroupTest.class.getName());
	private Identity id1, id2, id3, id4;
	private static OLATResource course1 = null;

	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BGRightManager rightManager;

	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() {
		try {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("twoo");
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");
			id4 = JunitTestHelper.createAndPersistIdentityAsUser("four");

			if (course1 == null) {
				DB db = DBFactory.getInstance();
				OLATResourceManager rm = OLATResourceManager.getInstance();
				// create course and persist as OLATResourceImpl
				OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse", new Long(456));
				course1 = rm.createOLATResourceInstance(resourceable);
				db.saveObject(course1);
	
				DBFactory.getInstance().closeSession();
			}
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
			e.printStackTrace();
		}
	}

	/**
	 * TearDown is called after each test.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		try {
//			OLATResourceManager.getInstance().deleteOLATResource(course1);
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			e.printStackTrace();
			throw e;
		}
	}

	/** BGContextManagerImpl:createAndPersistBGContext * */
	@Test
	public void testCreateAndPersistBGContext() {
		BGContextManagerImpl bgcm = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
		BGContext c1 = bgcm.createAndPersistBGContext("c1name", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
		assertNotNull(c1);
		BGContext c2 = bgcm.createAndPersistBGContext("c2name", "c2desc", BusinessGroup.TYPE_LEARNINGROUP, id1, false);
		assertNotNull(c2);
		try {
			bgcm.createAndPersistBGContext("name", "desc", null, id2, false);
			fail("context groupType can not be null");
		} catch (AssertException e) {
			// expected exception
			assertTrue(true);
		}
		try {
			bgcm.createAndPersistBGContext(null, "desc", BusinessGroup.TYPE_LEARNINGROUP, id2, false);
			fail("context name can not be null");
		} catch (AssertException e) {
			// expected exception
			assertTrue(true);
		}
	}

	/** BGContextManagerImpl:deleteBGContext() * */
	@Test
	@Ignore
	public void testDeleteBGContext() {
		/*BGContextManagerImpl bgcm = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
		BGContext c1 = bgcm.createAndPersistBGContext("c1name1", "c1desc1", BusinessGroup.TYPE_LEARNINGROUP, null, true);
		BGContext c2 = bgcm.createAndPersistBGContext("c2name1", "c2desc1", BusinessGroup.TYPE_RIGHTGROUP, id1, false);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		
		BusinessGroup g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0),
				new Integer(10), false, false, c1);
		BusinessGroup g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0),
				new Integer(10), false, false, c1);
		BusinessGroup g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, new Integer(0), 
				new Integer(10), false, false, c2);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BaseSecurity secm = BaseSecurityManager.getInstance();
		secm.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
		secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
		secm.addIdentityToSecurityGroup(id4, g2.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id4, g1.getOwnerGroup());
		secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id2, g3.getPartipiciantGroup());

		BGRightManagerImpl rm = BGRightManagerImpl.getInstance();
		rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
		rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
		DBFactory.getInstance().closeSession(); // simulate user clicks
		
		assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id1, c2));
		assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c2));
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BGAreaManager am = BGAreaManagerImpl.getInstance();
		BGArea a1 = am.createAndPersistBGAreaIfNotExists("a1-delete", "desca1", c1);
		BGArea a2 = am.createAndPersistBGAreaIfNotExists("a2-delete", null, c1);
		BGArea a3 = am.createAndPersistBGAreaIfNotExists("a3-delete", "desca3", c1);
		BGArea a4 = am.createAndPersistBGAreaIfNotExists("a4-delete", "desca4", c1);
		am.addBGToBGArea(g1, a1);
		am.addBGToBGArea(g2, a1);
		am.addBGToBGArea(g1, a2);
		am.addBGToBGArea(g2, a3);
		am.addBGToBGArea(g1, a4);
		DBFactory.getInstance().closeSession(); // simulate user clicks

		// test isIdentityInBGArea
		assertTrue(am.isIdentityInBGArea(id1, "a1-delete", c1));
		assertTrue(am.isIdentityInBGArea(id1, "a2-delete", c1));
		assertFalse(am.isIdentityInBGArea(id1, "a3-delete", c1)); // not in group g2
		assertTrue(am.isIdentityInBGArea(id1, "a4-delete", c1));
		assertFalse(am.isIdentityInBGArea(id1, "xx", c1)); // wrong area name
		assertFalse(am.isIdentityInBGArea(id1, "a1-delete", c2)); // wrong context
		assertTrue(am.isIdentityInBGArea(id2, "a1-delete", c1));
		assertTrue(am.isIdentityInBGArea(id2, "a2-delete", c1));
		assertFalse(am.isIdentityInBGArea(id2, "a3-delete", c1)); // not in group g2
		assertTrue(am.isIdentityInBGArea(id2, "a4-delete", c1));
		assertTrue(am.isIdentityInBGArea(id3, "a1-delete", c1));
		assertTrue(am.isIdentityInBGArea(id3, "a2-delete", c1));
		assertFalse(am.isIdentityInBGArea(id3, "a3-delete", c1)); // not in group g2
		assertTrue(am.isIdentityInBGArea(id3, "a4-delete", c1));
		assertTrue(am.isIdentityInBGArea(id4, "a1-delete", c1));
		assertTrue(am.isIdentityInBGArea(id4, "a2-delete", c1));
		assertTrue(am.isIdentityInBGArea(id4, "a3-delete", c1));
		assertTrue(am.isIdentityInBGArea(id4, "a4-delete", c1));

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(am.findBGAreasOfBusinessGroup(g1).size() == 3);
		assertTrue(am.findBGAreasOfBusinessGroup(g2).size() == 2);
		assertTrue(am.findBGAreasOfBusinessGroup(g3).size() == 0);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(am.findBGAreasOfBGContext(c1).size() == 4);
		assertTrue(am.findBGAreasOfBGContext(c2).size() == 0);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(am.findBusinessGroupsOfArea(a1).size() == 2);
		assertTrue(am.findBusinessGroupsOfArea(a2).size() == 1);
		assertTrue(am.findBusinessGroupsOfArea(a3).size() == 1);
		assertTrue(am.findBusinessGroupsOfArea(a4).size() == 1);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(am.countBGAreasOfBGContext(c1) == 4);
		assertTrue(am.countBGAreasOfBGContext(c2) == 0);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id1, "a1-delete", c1).size() == 1);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id1, "a2-delete", c1).size() == 1);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id1, "a3-delete", c1).size() == 0);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id1, "a4-delete", c1).size() == 1);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id4, "a1-delete", c1).size() == 1);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id4, "a2-delete", c1).size() == 0);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id4, "a3-delete", c1).size() == 1);
		assertTrue(am.findBusinessGroupsOfAreaAttendedBy(id4, "a4-delete", c1).size() == 0);

		bgcm.deleteBGContext(c1);
		// assertNull(DB.getInstance().findObject(BGContextImpl.class,
		// c1.getKey()));

		bgcm.deleteBGContext(c2);
		// assertNull(DB.getInstance().findObject(BGContextImpl.class,
		// c2.getKey()));

		assertTrue(am.findBGAreasOfBGContext(c1).size() == 0);
		assertNull(am.findBGArea("a1-delete", c1));
		assertTrue(am.findBGAreasOfBusinessGroup(g1).size() == 0);
		assertTrue(am.findBGAreasOfBGContext(c2).size() == 0);
		assertNull(am.findBGArea("a2-delete", c1));
		assertTrue(am.findBusinessGroupsOfArea(a1).size() == 0);
		assertTrue(am.findBusinessGroupsOfArea(a2).size() == 0);
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id1, c2));
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c2));
		assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));*/
	}

	/** BGContextManagerImpl:copyBGContext() * */
	@Test @Ignore
	public void testCopyBGContext() {
		/*
		BGContextManagerImpl bgcm = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
		BGContext c1 = bgcm.createAndPersistBGContext("c1name2", "c1desc2", BusinessGroup.TYPE_LEARNINGROUP, null, true);
		BGContext c2 = bgcm.createAndPersistBGContext("c2name2", "c2desc2", BusinessGroup.TYPE_RIGHTGROUP, id1, false);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BusinessGroup g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0),
				new Integer(10), false, false, c1);
		BusinessGroup g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0),
				new Integer(10), false, false, c1);
		BusinessGroup g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, new Integer(0), 
				new Integer(10), false, false, c2);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BaseSecurity secm = BaseSecurityManager.getInstance();
		secm.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
		secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
		secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id2, g3.getPartipiciantGroup());

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BGRightManagerImpl rm = BGRightManagerImpl.getInstance();
		rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
		rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BGAreaManager am = BGAreaManagerImpl.getInstance();
		BGArea a1 = am.createAndPersistBGAreaIfNotExists("a1-copy", "desca1", c1);
		BGArea a2 = am.createAndPersistBGAreaIfNotExists("a2-copy", null, c1);
		am.addBGToBGArea(g1, a1);
		am.addBGToBGArea(g2, a1);
		am.addBGToBGArea(g1, a2);
		DBFactory.getInstance().closeSession(); // simulate user clicks

		BGContext c1copy = bgcm.copyAndAddBGContextToResource(c1.getName(), course1, c1);
		DBFactory.getInstance().closeSession(); // simulate user clicks
		try {
			bgcm.copyAndAddBGContextToResource(c2.getName(), course1, c2);
			fail("expecting exeption");
		} catch (AssertException e) {
			// ok, passed
		}
		DBFactory.getInstance().closeSession(); // simulate user clicks

		assertTrue(am.findBGAreasOfBGContext(c1copy).size() == 2);
		assertNotNull(am.findBGArea("a1-copy", c1));
		assertNotNull(am.findBGArea("a2-copy", c1));
		assertNotNull(bgcm.findGroupOfBGContext(g1.getName(), c1copy));
		assertNotNull(bgcm.findGroupOfBGContext(g2.getName(), c1copy));
		assertTrue(bgcm.getGroupsOfBGContext(c1copy).size() == 2);
		bgcm.deleteBGContext(c1copy);
		*/
	}

	/** BGContextManagerImpl:deleteBGContext() * */
	@Test
	public void testBGRights() {
		BGContextManagerImpl bgcm = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
		BGContext bgctx1 = bgcm.createAndPersistBGContext("c1name3", "c1desc3", BusinessGroup.TYPE_RIGHTGROUP, null, true);
		BGContext bgctx2 = bgcm.createAndPersistBGContext("c2name3", "c2desc3", BusinessGroup.TYPE_RIGHTGROUP, id1, false);
		
		OLATResource c1 = OLATResourceManager.getInstance().createOLATResourceInstance(bgctx1);
		OLATResource c2 = OLATResourceManager.getInstance().createOLATResourceInstance(bgctx2);

		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.TYPE_RIGHTGROUP, -1, -1, false, false, c1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.TYPE_RIGHTGROUP, -1, -1, false, false, c1);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.TYPE_RIGHTGROUP, -1, -1, false, false, c2);

		BaseSecurity secm = BaseSecurityManager.getInstance();
		secm.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());
		secm.addIdentityToSecurityGroup(id3, g3.getPartipiciantGroup());

		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g1);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g2);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
		DBFactory.getInstance().closeSession(); // simulate user clicks

		// secm.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight,
		// rightGroup.getGroupContext());
		List<SecurityGroup> groups = secm.getGroupsWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, c1);
		assertTrue(groups.size() == 2);

		List<Identity> identities = secm.getIdentitiesWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, c2);
		assertTrue(identities.size() == 2);

		List<Policy> policies = secm.getPoliciesOfSecurityGroup(g1.getPartipiciantGroup());
		assertTrue(policies.size() == 3); // read, archiving, courseeditor

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c1));

		/*
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g1));
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, g1));
		 * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g2));
		 * assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g1));
		 */
		assertTrue(rightManager.findBGRights(g1).size() == 2);
		assertTrue(rightManager.findBGRights(g2).size() == 1);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g1);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g2);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));

		assertTrue(rightManager.findBGRights(g1).size() == 0);
		assertTrue(rightManager.findBGRights(g2).size() == 0);
	}

	/** BGContextManagerImpl:getGroupsOfBGContext and countGroupsOfBGContext* */
	@Test
	public void testGroupsOfBGContext() {
		OLATResource c1 = JunitTestHelper.createRandomResource();
		OLATResource c2 = JunitTestHelper.createRandomResource();

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(businessGroupService.findBusinessGroups(null, null, false, false, c1, 0, -1).isEmpty());
		assertTrue(businessGroupService.countBusinessGroups(null, null, false, false, c1) == 0);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0),
				new Integer(10), false, false, c1);
		assertNotNull(g1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0),
				new Integer(10), false, false, c1);
		assertNotNull(g2);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0),
				new Integer(10), false, false, c2);
		assertNotNull(g3);

		BusinessGroup g2douplicate = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0),
				new Integer(10), false, false, c1);
		assertNull(g2douplicate); // name douplicate names allowed per group context

		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0),
				new Integer(10), false, false, c2);
		assertNotNull(g4); // name douplicate in other context allowed

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(businessGroupService.findBusinessGroups(null, null, false, false, c1, 0, -1).size() == 2);
		assertTrue(businessGroupService.countBusinessGroups(null, null, false, false, c1) == 2);
	}
}