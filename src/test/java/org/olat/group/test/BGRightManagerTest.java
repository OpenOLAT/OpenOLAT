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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Grant;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class BGRightManagerTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(BGRightManagerTest.class);
	private Identity id1, id2, id3, id4;

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
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
			Assert.assertNotNull(id1);
			Assert.assertNotNull(id2);
			Assert.assertNotNull(id3);
			Assert.assertNotNull(id4);
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}

	/**
	 * Test if the add right doesn't generate errors
	 */
	@Test
	public void addRight() {
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "addRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c1);
		rightManager.addBGRight("test-right", g1, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void addRightWithResource() {
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "addRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c1);
		rightManager.addBGRight("test-right", g1.getBaseGroup(), c1.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void hasBGRight() {
		//create a right for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("has-right-1-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "hasBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.has-right", group, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the right is set
		boolean right = rightManager.hasBGRight("bgr.has-right", identity, resource.getOlatResource(), null);
		Assert.assertTrue(right);
		//check if a dummy is not set
		boolean notright = rightManager.hasBGRight("bgrblabla", identity, resource.getOlatResource(), null);
		Assert.assertFalse(notright);
	}
	
	@Test
	public void hasBGRightWithResource_participant() {
		//create a right for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("has-right-2-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "hasBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.has-right", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the right is set
		boolean right = rightManager.hasBGRight("bgr.has-right", identity, resource.getOlatResource(), null);
		Assert.assertTrue(right);
		//check if a dummy is not set
		boolean notright = rightManager.hasBGRight("bgrblabla", identity, resource.getOlatResource(), null);
		Assert.assertFalse(notright);
	}
	
	@Test
	public void hasBGRightWithResource_tutor() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("tp-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "tpBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
	    businessGroupRelationDao.addRole(identity, group, GroupRoles.coach.name());
		rightManager.addBGRight("bgr.right1", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.tutor);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.tutor);
		Assert.assertEquals(1, rights.size());
		Assert.assertTrue(rightManager.hasBGRight("bgr.right1", identity, resource.getOlatResource(), null));
	}
	
	@Test
	public void hasBGRightWithResource_tutor_asParticipant() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("tp-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "tpBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.right1", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.tutor);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.tutor);
		Assert.assertEquals(1, rights.size());
		Assert.assertTrue(rights.contains("bgr.right1"));
		//check that a participant cannot have a tutor right
		Assert.assertFalse(rightManager.hasBGRight("bgr.right1", identity, resource.getOlatResource(), null));
	}
	
	@Test
	public void hasBGRightWithResource_tutor_participant() {
		//create 2 rights for the three identities
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser("tp-rights-" + UUID.randomUUID().toString());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("tp-rights-" + UUID.randomUUID().toString());
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsUser("tp-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "tpBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity1, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(identity2, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(identity2, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(identity3, group, GroupRoles.coach.name());
		rightManager.addBGRight("bgr.right1", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.right2", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> tutorRights = rightManager.findBGRights(group, BGRightsRole.tutor);
		Assert.assertEquals(1, tutorRights.size());
		Assert.assertEquals("bgr.right1", tutorRights.get(0));
		List<String> participantRights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(1, participantRights.size());
		Assert.assertEquals("bgr.right2", participantRights.get(0));
		
		//id1 -> right2
		Assert.assertFalse(rightManager.hasBGRight("bgr.right1", identity1, resource.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.right2", identity1, resource.getOlatResource(), null));
		//id2 -> right1 and right2
		Assert.assertTrue(rightManager.hasBGRight("bgr.right1", identity2, resource.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.right2", identity2, resource.getOlatResource(), null));
		//id3 -> right2
		Assert.assertTrue(rightManager.hasBGRight("bgr.right1", identity3, resource.getOlatResource(), null));
		Assert.assertFalse(rightManager.hasBGRight("bgr.right2", identity3, resource.getOlatResource(), null));
	}
	
	@Test
	public void findBGRights() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("find-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "findBGRights", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.findright1", group, BGRightsRole.participant);
		rightManager.addBGRight("bgr.findright2", group, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertNotNull(rights);
		Assert.assertEquals(2, rights.size());
		Assert.assertTrue(rights.contains("bgr.findright1"));
		Assert.assertTrue(rights.contains("bgr.findright2"));
	}
	
	@Test
	public void hasBGRights() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("find-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "findBGRights", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.findright1", group, BGRightsRole.participant);
		rightManager.addBGRight("bgr.findright2", group, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check with an empty list
		boolean hasRights1 = rightManager.hasBGRight(Collections.<BusinessGroup>emptyList());
		Assert.assertFalse(hasRights1);
		
		//check if the rights are set
		boolean hasRights2 = rightManager.hasBGRight(Collections.singletonList(group));
		Assert.assertTrue(hasRights2);
	}
	
	@Test
	public void removeBGRight() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "removeBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.removeright1", group, BGRightsRole.participant);
		rightManager.addBGRight("bgr.removeright2", group, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(2, rights.size());
		Assert.assertTrue(rightManager.hasBGRight("bgr.removeright1", identity, resource.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.removeright2", identity, resource.getOlatResource(), null));
		
		//remove right 1
		rightManager.removeBGRight("bgr.removeright1", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if there is only 1 right
		List<String> rightsAfterDelete = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(1, rightsAfterDelete.size());
		Assert.assertTrue(rightsAfterDelete.contains("bgr.removeright2"));
		Assert.assertFalse(rightManager.hasBGRight("bgr.removeright1", identity, resource.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.removeright2", identity, resource.getOlatResource(), null));
	}
	
	@Test
	public void removeBGRightWithResources_permissionBased() {
		//create 2 rights for the identity
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 =  JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "removeBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource1);
		businessGroupService.addResourceTo(group, resource2);
	    businessGroupRelationDao.addRole(tutor, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.removeright1", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.removeright2", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight("bgr.dontrmght3", group.getBaseGroup(), resource2.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.dontrmght4", group.getBaseGroup(), resource2.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(2, rights.size());
		Assert.assertFalse(rightManager.hasBGRight("bgr.removeright1", participant, resource1.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.removeright2", participant, resource1.getOlatResource(), null));
		
		//remove tutor right 1
		rightManager.removeBGRight("bgr.removeright1", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.tutor);
		dbInstance.commitAndCloseSession();
		
		//check if there is only 1 right
		List<String> participantRights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(2, participantRights.size());
		
		//remove participant right 2
		rightManager.removeBGRight("bgr.removeright2", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check tutor rights
		List<String> tutorRights = rightManager.findBGRights(group, BGRightsRole.tutor);
		Assert.assertEquals(1, tutorRights.size());
		Assert.assertTrue(tutorRights.contains("bgr.dontrmght3"));
		
		List<String> participantRights_2 = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(1, participantRights_2.size());
		Assert.assertTrue(participantRights_2.contains("bgr.dontrmght4"));
	}
	
	
	@Test
	public void removeBGRightWithResources() {
		//create 2 rights for the identity
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 =  JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "removeBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource1);
		businessGroupService.addResourceTo(group, resource2);
	    businessGroupRelationDao.addRole(tutor, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.removeright1", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.removeright2", group.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight("bgr.dontrmght3", group.getBaseGroup(), resource2.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.dontrmght4", group.getBaseGroup(), resource2.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check if the rights are set
		List<String> rights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(2, rights.size());
		Assert.assertFalse(rightManager.hasBGRight("bgr.removeright1", participant, resource1.getOlatResource(), null));
		Assert.assertTrue(rightManager.hasBGRight("bgr.removeright2", participant, resource1.getOlatResource(), null));
		
		//remove tutor right 1
		rightManager.removeBGRights(group, resource1.getOlatResource(), BGRightsRole.tutor);
		dbInstance.commitAndCloseSession();
		
		//check if there is only 1 right
		List<String> participantRights = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(2, participantRights.size());
		
		//remove participant right 2
		rightManager.removeBGRights(group, resource1.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check tutor rights
		List<String> tutorRights = rightManager.findBGRights(group, BGRightsRole.tutor);
		Assert.assertEquals(1, tutorRights.size());
		Assert.assertTrue(tutorRights.contains("bgr.dontrmght3"));
		
		List<String> participantRights_2 = rightManager.findBGRights(group, BGRightsRole.participant);
		Assert.assertEquals(1, participantRights_2.size());
		Assert.assertTrue(participantRights_2.contains("bgr.dontrmght4"));
	}
	
	@Test
	public void findBGRights_wrapped() {
		//create
		RepositoryEntry resource1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource3 =  JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "findRights", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource1);
		businessGroupService.addResourceTo(group1, resource2);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "findRights", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource3);

		rightManager.addBGRight("bgr.fr1", group1.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.fr2", group1.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight("bgr.fr3", group2.getBaseGroup(), resource1.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.fr4", group2.getBaseGroup(), resource2.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight("bgr.fr5", group2.getBaseGroup(), resource3.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();
		
		//check 
		List<BGRights> rights1_1 = rightManager.findBGRights(Collections.singletonList(group1.getBaseGroup()), resource1.getOlatResource());
		Assert.assertNotNull(rights1_1);
		Assert.assertEquals(2, rights1_1.size());

		List<BGRights> rights2_2 = rightManager.findBGRights(Collections.singletonList(group2.getBaseGroup()), resource2.getOlatResource());
		Assert.assertNotNull(rights2_2);
		Assert.assertEquals(1, rights2_2.size());
		
		List<BGRights> rights2_3 = rightManager.findBGRights(Collections.singletonList(group2.getBaseGroup()), resource3.getOlatResource());
		Assert.assertNotNull(rights2_3);
		Assert.assertEquals(1, rights2_3.size());

		List<Group> groups = new ArrayList<>();
		groups.add(group1.getBaseGroup());
		groups.add(group2.getBaseGroup());
		List<BGRights> rightsAll_1 = rightManager.findBGRights(groups, resource1.getOlatResource());
		Assert.assertNotNull(rightsAll_1);
		Assert.assertEquals(3, rightsAll_1.size());
	}
	
	@Test
	public void removeUnkownRight() {
		//create 2 rights for the identity
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("remove-rights-" + UUID.randomUUID().toString());
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "removeBGRight", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		businessGroupRelationDao.addRole(identity, group, GroupRoles.participant.name());
		rightManager.addBGRight("bgr.removeright1", group, BGRightsRole.participant);
		dbInstance.commitAndCloseSession();

		//remove a dummy right which doesn't exists
		rightManager.removeBGRight("bgr.removeblabla", group.getBaseGroup(), resource.getOlatResource(), BGRightsRole.participant);
	}

	/** BGContextManagerImpl:deleteBGContext() * */
	@Test
	public void testBGRights() {
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry c2 =  JunitTestHelper.createAndPersistRepositoryEntry();

		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c1);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c2);

		businessGroupRelationDao.addRole(id1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, g2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, g3, GroupRoles.participant.name());

		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g1, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g1, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, g2, BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3, BGRightsRole.participant);
		DBFactory.getInstance().closeSession(); // simulate user clicks

		List<Grant> grants = groupDao.getGrants(g1.getBaseGroup(), GroupRoles.participant.name());
		Assert.assertEquals(2, grants.size()); // read, parti, archiving, courseeditor

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2.getOlatResource(), null));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1.getOlatResource(), null));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1.getOlatResource(), null));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id2, c1.getOlatResource(), null));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2.getOlatResource(), null));
		assertTrue(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2.getOlatResource(), null));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c1.getOlatResource(), null));

		Assert.assertEquals(2, rightManager.findBGRights(g1, BGRightsRole.participant).size());
		Assert.assertEquals(1, rightManager.findBGRights(g2, BGRightsRole.participant).size());

		DBFactory.getInstance().closeSession(); // simulate user clicks
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g1.getBaseGroup(), c1.getOlatResource(), BGRightsRole.participant);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g1.getBaseGroup(), c1.getOlatResource(), BGRightsRole.participant);
		rightManager.removeBGRight(CourseRights.RIGHT_ARCHIVING, g2.getBaseGroup(), c1.getOlatResource(), BGRightsRole.participant);
		rightManager.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g3.getBaseGroup(), c2.getOlatResource(), BGRightsRole.participant);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1.getOlatResource(), null));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1.getOlatResource(), null));
		assertFalse(rightManager.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2.getOlatResource(), null));

		Assert.assertEquals(0, rightManager.findBGRights(g1, BGRightsRole.participant).size());
		Assert.assertEquals(0, rightManager.findBGRights(g2, BGRightsRole.participant).size());
	}
}