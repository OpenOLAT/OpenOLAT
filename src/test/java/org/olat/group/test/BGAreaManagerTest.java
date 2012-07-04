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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Christian Guretzki, srosse
 */
public class BGAreaManagerTest extends OlatTestCase {

	private static OLog log = Tracing.createLoggerFor(BGAreaManagerTest.class);

	private OLATResource c1, c2;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BaseSecurity securityManager;

	@Before
	public void setUp() {
		try {
			c1 = JunitTestHelper.createRandomResource();
			Assert.assertNotNull(c1);
			c2 = JunitTestHelper.createRandomResource();
			Assert.assertNotNull(c2);

			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void testCreateBGArea() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		String description = "description:" + areaName;
		BGArea area = areaManager.createAndPersistBGAreaIfNotExists(areaName, description, resource);
		Assert.assertNotNull(area);
		dbInstance.commitAndCloseSession();

		//check by reloading the area
		BGArea reloadedArea = areaManager.reloadArea(area);
		Assert.assertNotNull(reloadedArea);
		Assert.assertNotNull(reloadedArea.getCreationDate());
		Assert.assertNotNull(reloadedArea.getResource());
		Assert.assertEquals(areaName, reloadedArea.getName());
		Assert.assertEquals(description, reloadedArea.getDescription());
		Assert.assertEquals(resource, reloadedArea.getResource());
	}
	
	@Test
	public void findBGArea() {
		//create a resource with areas
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("find-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("find-2-" + areaName, "description:" + areaName, resource);
		
		dbInstance.commitAndCloseSession();
		
		BGArea fArea1 = areaManager.findBGArea("find-1-" + areaName, resource);
		Assert.assertNotNull(fArea1);
		Assert.assertEquals(area1, fArea1);
		
		BGArea fArea2 = areaManager.findBGArea("find-2-" + areaName, resource);
		Assert.assertNotNull(fArea2);
		Assert.assertEquals(area2, fArea2);
	}
	
	@Test
	public void updateBGArea() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area = areaManager.createAndPersistBGAreaIfNotExists("upd-1-" + areaName, "description:" + areaName, resource);
		
		dbInstance.commitAndCloseSession();
		
		//update the area
		area.setName("Hello world");
		area.setDescription("The world is big");
		BGArea updatedArea = areaManager.updateBGArea(area);
		//check output
		Assert.assertNotNull(updatedArea);
		Assert.assertEquals("Hello world", updatedArea.getName());
		Assert.assertEquals("The world is big", updatedArea.getDescription());
		
		dbInstance.commitAndCloseSession();
		
		BGArea reloadedArea = areaManager.loadArea(area.getKey());
		Assert.assertNotNull(reloadedArea);
		Assert.assertEquals("Hello world", reloadedArea.getName());
		Assert.assertEquals("The world is big", reloadedArea.getDescription());
	}
	
	@Test
	public void addBGToBGArea() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area = areaManager.createAndPersistBGAreaIfNotExists("area-" + areaName, "description:" + areaName, resource);
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "area-group", "area-group-desc", 0, -1, false, false, resource);

		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group, area);
	}
	
	@Test
	public void deleteBGArea() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		dbInstance.commitAndCloseSession();
		
		//delete area 1
		areaManager.deleteBGArea(area1);
		dbInstance.commitAndCloseSession();
		
		//check that it's really deleted
		BGArea deletedArea = areaManager.loadArea(area1.getKey());
		Assert.assertNull(deletedArea);
		//but not all areas are deleted
		BGArea reloadedArea2 = areaManager.loadArea(area2.getKey());
		Assert.assertNotNull(reloadedArea2);
	}
	
	
	@Test
	public void addAndFindByResource() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		BGArea area3 = areaManager.createAndPersistBGAreaIfNotExists("area-3-" + areaName, "description:" + areaName, resource);

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group1, area3);
		areaManager.addBGToBGArea(group2, area3);
		dbInstance.commitAndCloseSession();
		
		//count
		int numOfAreas = areaManager.countBGAreasInContext(resource);
		Assert.assertEquals(3, numOfAreas);
		
		//find areas
		List<BGArea> areas = areaManager.findBGAreasInContext(resource);
		Assert.assertNotNull(areas);
		Assert.assertEquals(3, areas.size());
		Assert.assertTrue(areas.contains(area1));
		Assert.assertTrue(areas.contains(area2));
		Assert.assertTrue(areas.contains(area3));
	}
	
	@Test
	public void addAndFindByGroup() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area1);
		areaManager.addBGToBGArea(group3, area2);
		dbInstance.commitAndCloseSession();
		
		//check find group 1
		List<BGArea> areasGroup1 = areaManager.findBGAreasOfBusinessGroup(group1);
		Assert.assertNotNull(areasGroup1);
		Assert.assertEquals(1, areasGroup1.size());
		Assert.assertTrue(areasGroup1.contains(area1));
		
		//check find group 3
		List<BGArea> areasGroup3 = areaManager.findBGAreasOfBusinessGroup(group3);
		Assert.assertNotNull(areasGroup3);
		Assert.assertEquals(2, areasGroup3.size());
		Assert.assertTrue(areasGroup3.contains(area1));
		Assert.assertTrue(areasGroup3.contains(area2));
	}
	
	@Test
	public void addAndFindByGroups() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group4 = businessGroupService.createBusinessGroup(null, "area-4-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area1);
		areaManager.addBGToBGArea(group3, area2);
		areaManager.addBGToBGArea(group4, area2);
		dbInstance.commitAndCloseSession();
		
		//check find group 1
		List<BGArea> areasGroup1 = areaManager.findBGAreasOfBusinessGroups(Collections.singletonList(group1));
		Assert.assertNotNull(areasGroup1);
		Assert.assertEquals(1, areasGroup1.size());
		Assert.assertTrue(areasGroup1.contains(area1));
		
		//check find group 2 and 4 -> only area 2
		List<BusinessGroup> groups2_4 = new ArrayList<BusinessGroup>();
		groups2_4.add(group2);
		groups2_4.add(group4);
		List<BGArea> areasGroup2_4 = areaManager.findBGAreasOfBusinessGroups(groups2_4);
		Assert.assertNotNull(areasGroup2_4);
		Assert.assertEquals(1, areasGroup2_4.size());
		Assert.assertTrue(areasGroup2_4.contains(area2));
		
		//check find all groups
		List<BusinessGroup> allGroups = new ArrayList<BusinessGroup>();
		allGroups.add(group1);
		allGroups.add(group2);
		allGroups.add(group3);
		allGroups.add(group4);
		List<BGArea> areasAllGroups = areaManager.findBGAreasOfBusinessGroups(allGroups);
		Assert.assertNotNull(areasAllGroups);
		Assert.assertEquals(2, areasAllGroups.size());
		Assert.assertTrue(areasAllGroups.contains(area1));
		Assert.assertTrue(areasAllGroups.contains(area2));
		
		//check empty list
		List<BGArea> areasEmpty = areaManager.findBGAreasOfBusinessGroups(Collections.<BusinessGroup>emptyList());
		Assert.assertNotNull(areasEmpty);
		Assert.assertEquals(0, areasEmpty.size());
	}
	
	@Test
	public void addFindAndDeleteRelation() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		dbInstance.commitAndCloseSession();
		
		//check find groups
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfArea(area1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());
		Assert.assertTrue(groups.contains(group1));
		Assert.assertTrue(groups.contains(group2));
		
		dbInstance.commitAndCloseSession();
		//remove relation to group1
		areaManager.removeBGFromArea(group2, area1);
		dbInstance.commitAndCloseSession();
		
		//check find groups
		List<BusinessGroup> diminushedGroups = areaManager.findBusinessGroupsOfArea(area1);
		Assert.assertNotNull(diminushedGroups);
		Assert.assertEquals(1, diminushedGroups.size());
		Assert.assertTrue(diminushedGroups.contains(group1));
	}
	
	@Test
	public void findGroupsByAreas() {
		//create a resource, 3 area and 2 group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		BGArea area3 = areaManager.createAndPersistBGAreaIfNotExists("area-3-" + areaName, "description:" + areaName, resource);
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group1, area2);
		areaManager.addBGToBGArea(group1, area3);
		areaManager.addBGToBGArea(group2, area1);
		dbInstance.commitAndCloseSession();
		
		//check with empty list
		List<BusinessGroup> groupEmpty = areaManager.findBusinessGroupsOfAreas(Collections.<BGArea>emptyList());
		Assert.assertNotNull(groupEmpty);
		Assert.assertEquals(0, groupEmpty.size());
		
		//check find area 3 -> only group 1
		List<BusinessGroup> groupArea3 = areaManager.findBusinessGroupsOfAreas(Collections.singletonList(area3));
		Assert.assertNotNull(groupArea3);
		Assert.assertEquals(1, groupArea3.size());
		Assert.assertTrue(groupArea3.contains(group1));
		
		//check find area 1,2 and 3 -> only group 1 and 2
		List<BGArea> allAreas = new ArrayList<BGArea>();
		allAreas.add(area1);
		allAreas.add(area2);
		allAreas.add(area3);
		List<BusinessGroup> groupAllAreas = areaManager.findBusinessGroupsOfAreas(allAreas);
		Assert.assertNotNull(groupAllAreas);
		Assert.assertEquals(2, groupAllAreas.size());
		Assert.assertTrue(groupAllAreas.contains(group1));
		Assert.assertTrue(groupAllAreas.contains(group2));
	}
	
	@Test
	public void addAndDeleteRelations() {
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area1);
		dbInstance.commitAndCloseSession();
		
		//check with find groups
		List<BGArea> areaGroup2 = areaManager.findBGAreasOfBusinessGroup(group2);
		Assert.assertNotNull(areaGroup2);
		Assert.assertEquals(2, areaGroup2.size());
		Assert.assertTrue(areaGroup2.contains(area1));
		Assert.assertTrue(areaGroup2.contains(area2));

		//remove relation to group2
		areaManager.deleteBGtoAreaRelations(group2);
		dbInstance.commitAndCloseSession();

		//check find groups
		List<BGArea> areaGroup2After = areaManager.findBGAreasOfBusinessGroup(group2);
		Assert.assertNotNull(areaGroup2After);
		Assert.assertEquals(0, areaGroup2After.size());
	}

	@Test
	public void checkIfOneOrMoreNameExistsInContext() {
		//create a resource, an area, a group
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource1);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource1);
		//create 2 groups
		dbInstance.commitAndCloseSession();

		//check empty list
		boolean emptyTest = areaManager.checkIfOneOrMoreNameExistsInContext(Collections.<String>emptySet(), resource1);
		Assert.assertFalse(emptyTest);
		
		//check names
		Set<String> name1 = new HashSet<String>();
		name1.add("Hello OpenOLAT");
		name1.add(area1.getName());
		boolean test1 = areaManager.checkIfOneOrMoreNameExistsInContext(name1, resource1);
		Assert.assertTrue(test1);

		//check more names
		Set<String> name2 = new HashSet<String>();
		name2.add("Hello OpenOLAT");
		name2.add(area1.getName());
		name2.add(area2.getName());
		boolean test2 = areaManager.checkIfOneOrMoreNameExistsInContext(name2, resource1);
		Assert.assertTrue(test2);
		
		//check wrong names
		Set<String> name3 = new HashSet<String>();
		name3.add("Hello OpenOLAT");
		name3.add("area-1-");
		name3.add("area-2-");
		boolean test3 = areaManager.checkIfOneOrMoreNameExistsInContext(name3, resource1);
		Assert.assertFalse(test3);
	}
	
	@Test
	public void findBusinessGroupsOfAreaAttendedBy() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-3-" + UUID.randomUUID().toString());
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area1);
		dbInstance.commitAndCloseSession();
		//add attendee
		securityManager.addIdentityToSecurityGroup(id1, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group2.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group3.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup());
		dbInstance.commitAndCloseSession();
		
		//find with resource
		List<BusinessGroup> groupId1 = areaManager.findBusinessGroupsOfAreaAttendedBy(id1, null, resource);
		Assert.assertNotNull(groupId1);
		Assert.assertEquals(1, groupId1.size());
		Assert.assertTrue(groupId1.contains(group1));
		
		//find nothing with name and resource
		List<BusinessGroup> groupId1Area2 = areaManager.findBusinessGroupsOfAreaAttendedBy(id1, "area-2-" + areaName, resource);
		Assert.assertNotNull(groupId1Area2);
		Assert.assertEquals(0, groupId1Area2.size());
		
		//find groups id 2 with name and resource
		List<BusinessGroup> groupId2Area1 = areaManager.findBusinessGroupsOfAreaAttendedBy(id2, "area-1-" + areaName, resource);
		Assert.assertNotNull(groupId2Area1);
		Assert.assertEquals(2, groupId2Area1.size());
		Assert.assertTrue(groupId2Area1.contains(group2));
		Assert.assertTrue(groupId2Area1.contains(group3));
	}
	
	@Test
	public void isIdentityInBGArea() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-1-" + UUID.randomUUID().toString());
		//create a resource, an area, a group
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGAreaIfNotExists("area-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGAreaIfNotExists("area-2-" + areaName, "description:" + areaName, resource);
		BGArea area3 = areaManager.createAndPersistBGAreaIfNotExists("area-3-" + areaName, "description:" + areaName, resource);
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", 0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", 0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area3);
		dbInstance.commitAndCloseSession();
		//add attendee
		securityManager.addIdentityToSecurityGroup(id1, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id1, group2.getOwnerGroup());
		dbInstance.commitAndCloseSession();
		
		//check in area 1
		boolean testArea1 = areaManager.isIdentityInBGArea(id1, "area-1-" + areaName, resource);
		Assert.assertTrue(testArea1);
		//check in area 1
		boolean testArea2 = areaManager.isIdentityInBGArea(id1, "area-2-" + areaName, resource);
		Assert.assertTrue(testArea2);
		//check in area 1
		boolean testArea3 = areaManager.isIdentityInBGArea(id1, "area-3-" + areaName, resource);
		Assert.assertFalse(testArea3);
	}
	
	/** 
	 * Do in different threads ant check that no exception happens :
	 * 1. create BG-Area
	 * 5. delete
	 */
	@Test
	public void testSynchronisationCreateBGArea() {

		final int maxLoop = 400; // => 400 x 100ms => 40sec => finished in 50sec
		final String areaName = "BGArea_1";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finfishCount = new CountDownLatch(3);
		
		BGArea bgArea = areaManager.findBGArea(areaName, c1);
		assertNull(bgArea);
		
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 100, 20, finfishCount);
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 30, 40, finfishCount);
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 15, 20, finfishCount);
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finfishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
		assertEquals("Not all threads has finished", 0, finfishCount.getCount());
	}

	/**
	 * thread 1 : try to create - sleep - delete sleep

	 * @param areaName
	 * @param maxLoop
	 * @param exceptionHolder
	 * @param sleepAfterCreate
	 * @param sleepAfterDelete
	 */
	private void startThreadCreateDeleteBGArea(final String areaName, final int maxLoop, final List<Exception> exceptionHolder, 
			final int sleepAfterCreate, final int sleepAfterDelete, final CountDownLatch finishedCount) {
		new Thread(new Runnable() {
			public void run() {
				try {
				
				for (int i=0; i<maxLoop; i++) {
					try {
						BGArea bgArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, "description:" + areaName, c1);
						if (bgArea != null) {
							DBFactory.getInstance().closeSession();
							// created a new bg area
							sleep(sleepAfterCreate);
							areaManager.deleteBGArea(bgArea);
						}
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().closeSession();
						} catch (Exception e) {
							// ignore
						};
					}
					sleep(sleepAfterDelete);
				}
				} catch(Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishedCount.countDown();
				}
			}
		}).start();
	}


	/** 
	 * Do in different threads ant check that no exception happens :
	 * 1. create BG-Area
	 * 5. delete
	 */
	@Test
	public void testSynchronisationUpdateBGArea() {

		final int maxLoop = 400; // => 400 x 100ms => 40sec => finished in 50sec
		final String areaName = "BGArea_2";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finfishCount = new CountDownLatch(3);
		
		
		BGArea bgArea = areaManager.findBGArea(areaName, c1);
		assertNull(bgArea);
		bgArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, "description:" + areaName, c1);
		assertNotNull(bgArea);
		
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 20, finfishCount);
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 40, finfishCount);
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 15, finfishCount);
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finfishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			exceptionHolder.add(e);
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
		assertEquals("Not all threads has finished", 0, finfishCount.getCount());
	}
	
	private void startThreadUpdateBGArea(final String areaName, final int maxLoop, final List<Exception> exceptionHolder, 
			final int sleepTime, final CountDownLatch finishedCount) {
		// thread 2 : update,copy 
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i=0; i<maxLoop; i++) {
						try {
							BGArea bgArea = areaManager.findBGArea(areaName, c1);
							DBFactory.getInstance().closeSession();// Detached the bg-area object with closing session 
							if (bgArea != null) {
								bgArea.setDescription("description:" + areaName + i);
								areaManager.updateBGArea(bgArea);
							}
						} catch (Exception e) {
							exceptionHolder.add(e);
						} finally {
							try {
								DBFactory.getInstance().closeSession();
							} catch (Exception e) {
								// ignore
							};
						}
						sleep(sleepTime);
					}
				} catch(Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishedCount.countDown();
				}
			}}).start();
	}

	
	/**
	 * 
	 * @param millis the duration in milliseconds to sleep
	 */
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}