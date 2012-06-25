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

package org.olat.course.groupsandrights;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.right.BGRightManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/>
 * 
 * Initial Date:  Aug 18, 2004
 * @author gnaegi
 */
public class CourseGroupManagementTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(CourseGroupManagementTest.class.getName());
	private Identity id1, id2, id3;
	private OLATResource course1;
	
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	
	@Before
	public void setUp() {
		try {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
			id2 = JunitTestHelper.createAndPersistIdentityAsUser("twoo");
			id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");
			
			OLATResourceManager rm = OLATResourceManager.getInstance();
			// create course and persist as OLATResourceImpl
			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
			course1 =  rm.createOLATResourceInstance(resourceable);
			DBFactory.getInstance().saveObject(course1);
			
			
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
			e.printStackTrace();
		}
	}


	@After
	public void tearDown() throws Exception {
		try {
            DBFactory.getInstance().closeSession();
        } catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
            e.printStackTrace();
            throw e;
        }
	}

	/** rights tests */
	@Test
	public void testHasRightIsInMethods() {
	    BGContextManagerImpl cm = (BGContextManagerImpl)BGContextManagerImpl.getInstance();
	    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
	    BaseSecurity secm = BaseSecurityManager.getInstance();
	    
	    // 1) context one: learning groups
	    BGContext ctxt1 = cm.createAndAddBGContextToResource("c1name", course1, BusinessGroup.TYPE_LEARNINGROUP, id1, true);
	    OLATResource c1 = resourceManager.findOrPersistResourceable(course1);
	    
	    // create groups without waitinglist
	    BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.TYPE_LEARNINGROUP,new Integer(0), new Integer(10), false, false, c1);
	    BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.TYPE_LEARNINGROUP, new Integer(0), new Integer(10), false, false, c1);
	    // members
	    secm.addIdentityToSecurityGroup(id1, g2.getOwnerGroup());
	    secm.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(id2, g2.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
	    // areas
	    BGArea a1 = areaManager.createAndPersistBGAreaIfNotExists("a1", "desca1",c1);
	    BGArea a2 = areaManager.createAndPersistBGAreaIfNotExists("a2", null, c1);
	    BGArea a3 = areaManager.createAndPersistBGAreaIfNotExists("a3", null, c1);
	    areaManager.addBGToBGArea(g1, a1);    
	    areaManager.addBGToBGArea(g2, a1);
	    areaManager.addBGToBGArea(g1, a2);	
	    areaManager.addBGToBGArea(g2, a3);
	    
	    // 2) context two: right groups
	    BGContext c2 = cm.createAndAddBGContextToResource("c2name", course1, BusinessGroup.TYPE_RIGHTGROUP, id2, true);
	    // groups
	    BusinessGroup g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
	    BusinessGroup g4 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g4", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
	    // members
	    secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(id1, g4.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(id3, g4.getPartipiciantGroup());
	    // rights
	    rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
	    rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
	    rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g4);
	    rightManager.addBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g4);
	    
	    DBFactory.getInstance().closeSession(); // simulate user clicks
	    
	    // test groups
	    CourseGroupManager gm = PersistingCourseGroupManager.getInstance(course1);
	    assertTrue(gm.isIdentityInLearningGroup(id1, g1.getName()));
	    assertTrue(gm.isIdentityInLearningGroup(id1, g2.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id1, g3.getName())); // not a learning group
	    assertFalse(gm.isIdentityInLearningGroup(id1, g4.getName())); // not a learning group

	    assertTrue(gm.isIdentityInLearningGroup(id2, g1.getName()));
	    assertTrue(gm.isIdentityInLearningGroup(id2, g2.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id2, g3.getName())); // not a learning group
	    assertFalse(gm.isIdentityInLearningGroup(id2, g4.getName())); // not a learning group

	    DBFactory.getInstance().closeSession();
	    assertTrue(gm.isIdentityInLearningGroup(id3, g1.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id3, g2.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id3, g3.getName())); // not a learning group
	    assertFalse(gm.isIdentityInLearningGroup(id3, g4.getName())); // not a learning group

	    /*
	    assertTrue(gm.isIdentityInLearningGroup(id1, g1.getName(), c1.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id1, g1.getName(), c2.getName()));
	    assertTrue(gm.isIdentityInLearningGroup(id3, g1.getName(), c1.getName()));
	    assertFalse(gm.isIdentityInLearningGroup(id3, g1.getName(), c2.getName()));
	    */
	    
	    // test areas
	    DBFactory.getInstance().closeSession();
	    assertTrue(gm.isIdentityInLearningArea(id1, a1.getName()));
	    assertTrue(gm.isIdentityInLearningArea(id1, a2.getName()));
	    assertTrue(gm.isIdentityInLearningArea(id1, a3.getName()));

	    assertTrue(gm.isIdentityInLearningArea(id2, a1.getName()));
	    assertTrue(gm.isIdentityInLearningArea(id2, a2.getName()));
	    assertTrue(gm.isIdentityInLearningArea(id2, a3.getName()));

	    DBFactory.getInstance().closeSession();
	    assertTrue(gm.isIdentityInLearningArea(id3, a1.getName()));
	    assertTrue(gm.isIdentityInLearningArea(id3, a2.getName()));
	    assertFalse(gm.isIdentityInLearningArea(id3, a3.getName()));

	    DBFactory.getInstance().closeSession();
	    assertTrue(gm.getLearningAreasOfGroupFromAllContexts(g1.getName()).size() == 2);
	    assertTrue(gm.getLearningAreasOfGroupFromAllContexts(g2.getName()).size() == 2);
	    
	    // test rights
	    DBFactory.getInstance().closeSession();
	    assertTrue(gm.hasRight(id1, CourseRights.RIGHT_ARCHIVING));
	    assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR));
	    assertTrue(gm.hasRight(id1, CourseRights.RIGHT_GROUPMANAGEMENT));
	    assertFalse(gm.hasRight(id1, CourseRights.RIGHT_ASSESSMENT));
	    assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, c2.getName()));
	  //TODO gm assertFalse(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, c1.getName()));
	    assertFalse(gm.hasRight(id2, CourseRights.RIGHT_COURSEEDITOR));
	    
	    // test context
	    DBFactory.getInstance().closeSession();
	  //TODO gm assertTrue(gm.isIdentityInGroupContext(id1,c1.getName()));
	    assertTrue(gm.isIdentityInGroupContext(id1,c2.getName()));
	    //TODO gm assertTrue(gm.isIdentityInGroupContext(id2,c1.getName()));
	    assertFalse(gm.isIdentityInGroupContext(id2,c2.getName()));
	  //TODO gm  assertTrue(gm.isIdentityInGroupContext(id3,c1.getName()));
	    assertTrue(gm.isIdentityInGroupContext(id3,c2.getName()));
	}
		
}