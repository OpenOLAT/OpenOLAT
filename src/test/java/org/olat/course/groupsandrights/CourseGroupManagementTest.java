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

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
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

	private static final Logger log = Tracing.createLoggerFor(CourseGroupManagementTest.class);
	private Identity id1, id2, id3;
	
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private BusinessGroupService businessGroupService;


	@Before
	public void setUp() {
		try {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			id2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			id3 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			

			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	/** rights tests */
	@Test
	public void testHasRightIsInMethodsByGroups() {
		RepositoryEntry course1 =  JunitTestHelper.createAndPersistRepositoryEntry();

		// create groups without waitinglist
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, course1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, course1);
		// members
		businessGroupRelationDao.addRole(id1, g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, g1, GroupRoles.coach.name());

		// groups
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, course1);
		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g4", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, course1);
		// members
		businessGroupRelationDao.addRole(id1, g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, g4, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, g4, GroupRoles.participant.name());
		// rights
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g3, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g4, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g4, BGRightsRole.participant);

		DBFactory.getInstance().closeSession(); // simulate user clicks

		// test groups
		CourseGroupManager gm = PersistingCourseGroupManager.getInstance(course1.getOlatResource());
		assertTrue(gm.isIdentityInGroup(id1, g1.getKey()));
		assertTrue(gm.isIdentityInGroup(id1, g2.getKey()));
		assertTrue(gm.isIdentityInGroup(id1, g3.getKey()));
		assertTrue(gm.isIdentityInGroup(id1, g4.getKey()));

		assertTrue(gm.isIdentityInGroup(id2, g1.getKey()));
		assertTrue(gm.isIdentityInGroup(id2, g2.getKey()));
		assertFalse(gm.isIdentityInGroup(id2, g3.getKey()));
		assertFalse(gm.isIdentityInGroup(id2, g4.getKey()));

		DBFactory.getInstance().closeSession();
		assertTrue(gm.isIdentityInGroup(id3, g1.getKey()));
		assertFalse(gm.isIdentityInGroup(id3, g2.getKey()));
		assertFalse(gm.isIdentityInGroup(id3, g3.getKey()));
		assertTrue(gm.isIdentityInGroup(id3, g4.getKey()));

		// test rights
		DBFactory.getInstance().closeSession();
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_ARCHIVING, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_GROUPMANAGEMENT, GroupRoles.participant));
		assertFalse(gm.hasRight(id1, CourseRights.RIGHT_ASSESSMENT, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertFalse(gm.hasRight(id2, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));

		// test context
		DBFactory.getInstance().closeSession();
	}

	/** rights tests */
	@Test
	public void testHasRightIsInMethodsByArea() {
		RepositoryEntry course1 =  JunitTestHelper.createAndPersistRepositoryEntry();

		// create groups without waitinglist
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, course1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, course1);
		// members
		businessGroupRelationDao.addRole(id1, g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, g1, GroupRoles.coach.name());
		// areas
		BGArea a1 = areaManager.createAndPersistBGArea("a1", "desca1", course1.getOlatResource());
		BGArea a2 = areaManager.createAndPersistBGArea("a2", null, course1.getOlatResource());
		BGArea a3 = areaManager.createAndPersistBGArea("a3", null, course1.getOlatResource());
		areaManager.addBGToBGArea(g1, a1);
		areaManager.addBGToBGArea(g2, a1);
		areaManager.addBGToBGArea(g1, a2);
		areaManager.addBGToBGArea(g2, a3);

		// groups
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, course1);
		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g4", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, course1);
		// members
		businessGroupRelationDao.addRole(id1, g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, g4, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, g4, GroupRoles.participant.name());
		// rights
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g3, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g4, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g4, BGRightsRole.participant);

		DBFactory.getInstance().closeSession(); // simulate user clicks

		// test groups
		CourseGroupManager gm = PersistingCourseGroupManager.getInstance(course1.getOlatResource());

		// test areas
		DBFactory.getInstance().closeSession();
		assertTrue(gm.isIdentityInLearningArea(id1, a1.getKey()));
		assertTrue(gm.isIdentityInLearningArea(id1, a2.getKey()));
		assertTrue(gm.isIdentityInLearningArea(id1, a3.getKey()));

		assertTrue(gm.isIdentityInLearningArea(id2, a1.getKey()));
		assertTrue(gm.isIdentityInLearningArea(id2, a2.getKey()));
		assertTrue(gm.isIdentityInLearningArea(id2, a3.getKey()));

		DBFactory.getInstance().closeSession();
		assertTrue(gm.isIdentityInLearningArea(id3, a1.getKey()));
		assertTrue(gm.isIdentityInLearningArea(id3, a2.getKey()));
		assertFalse(gm.isIdentityInLearningArea(id3, a3.getKey()));

		DBFactory.getInstance().closeSession();
		
		// test rights
		DBFactory.getInstance().closeSession();
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_ARCHIVING, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_GROUPMANAGEMENT, GroupRoles.participant));
		assertFalse(gm.hasRight(id1, CourseRights.RIGHT_ASSESSMENT, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));
		assertFalse(gm.hasRight(id2, CourseRights.RIGHT_COURSEEDITOR, GroupRoles.participant));

		// test context
		DBFactory.getInstance().closeSession();
	}
		
}