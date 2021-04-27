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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Christian Guretzki, srosse
 */
public class BGAreaManagerTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(BGAreaManagerTest.class);

	private OLATResource c1, c2;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

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
	
	@Test
	public void testCreateLoadBGArea() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		String description = "description:" + areaName;
		BGArea area = areaManager.createAndPersistBGArea(areaName, description, resource);
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
	public void testLoadByKeys() {
		//create a resource with areas
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("load-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGArea("load-2-" + areaName, "description:" + areaName, resource);
		dbInstance.commitAndCloseSession();
		
		//check if it's robust agains empty argument
		List<BGArea> emptyList = areaManager.loadAreas(Collections.<Long>emptyList());
		Assert.assertNotNull(emptyList);
		Assert.assertTrue(emptyList.isEmpty());

		//check by loading 1 area
		List<BGArea> single = areaManager.loadAreas(Collections.singletonList(area2.getKey()));
		Assert.assertNotNull(single);
		Assert.assertEquals(1, single.size());
		Assert.assertEquals(area2, single.get(0));
		
		//check by loading 2 areas
		List<Long> areaKeys = new ArrayList<>();
		areaKeys.add(area1.getKey());
		areaKeys.add(area2.getKey());
		List<BGArea> areaList = areaManager.loadAreas(areaKeys);
		Assert.assertNotNull(areaList);
		Assert.assertEquals(2, areaList.size());
		Assert.assertTrue(areaList.contains(area1));
		Assert.assertTrue(areaList.contains(area2));
	}
	
	@Test
	public void testExistArea() {
		//create a resource with areas
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area = areaManager.createAndPersistBGArea("exists-" + areaName, "description:" + areaName, resource);
		dbInstance.commitAndCloseSession();
		
		//check exist by key
		boolean exist1 = areaManager.existArea(area.getKey().toString(), resource);
		Assert.assertTrue(exist1);
		//check by name
		boolean exist2 = areaManager.existArea(area.getName(), resource);
		Assert.assertTrue(exist2);
		//check negative by key
		boolean exist3 = areaManager.existArea("120", resource);
		Assert.assertFalse(exist3);
		//check negative by key
		boolean exist4 = areaManager.existArea("dummy", resource);
		Assert.assertFalse(exist4);
	}
	
	@Test
	public void findBGArea() {
		//create a resource with areas
		OLATResource resource = JunitTestHelper.createRandomResource();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("find-1-" + areaName, "description:" + areaName, resource);
		BGArea area2 = areaManager.createAndPersistBGArea("find-2-" + areaName, "description:" + areaName, resource);
		
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
		BGArea area = areaManager.createAndPersistBGArea("upd-1-" + areaName, "description:" + areaName, resource);
		
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
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area = areaManager.createAndPersistBGArea("area-" + areaName, "description:" + areaName, resource.getOlatResource());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "area-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);

		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group, area);
	}
	
	@Test
	public void deleteBGArea() {
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area3 = areaManager.createAndPersistBGArea("area-3-" + areaName, "description:" + areaName, resource.getOlatResource());

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		
		//add the relation
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group1, area3);
		areaManager.addBGToBGArea(group2, area3);
		dbInstance.commitAndCloseSession();
		
		//count
		int numOfAreas = areaManager.countBGAreasInContext(resource.getOlatResource());
		Assert.assertEquals(3, numOfAreas);
		
		//find areas
		List<BGArea> areas = areaManager.findBGAreasInContext(resource.getOlatResource());
		Assert.assertNotNull(areas);
		Assert.assertEquals(3, areas.size());
		Assert.assertTrue(areas.contains(area1));
		Assert.assertTrue(areas.contains(area2));
		Assert.assertTrue(areas.contains(area3));
	}
	
	@Test
	public void addAndFindByGroup() {
		//create a resource, an area, a group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());

		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group4 = businessGroupService.createBusinessGroup(null, "area-4-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
		List<BusinessGroup> groups2_4 = new ArrayList<>();
		groups2_4.add(group2);
		groups2_4.add(group4);
		List<BGArea> areasGroup2_4 = areaManager.findBGAreasOfBusinessGroups(groups2_4);
		Assert.assertNotNull(areasGroup2_4);
		Assert.assertEquals(1, areasGroup2_4.size());
		Assert.assertTrue(areasGroup2_4.contains(area2));
		
		//check find all groups
		List<BusinessGroup> allGroups = new ArrayList<>();
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
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
	
	/**
	 * Check that the remove resource from business group service remove
	 * the relation to area too
	 */
	@Test
	public void removeResourceFrom_withArea() {
		//create a resource, an area, a group
		RepositoryEntry resource1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry resource2 =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource1.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource2.getOlatResource());
		//create the group
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		//add relations group to courses
		businessGroupService.addResourceTo(group, resource1);
		businessGroupService.addResourceTo(group, resource2);
		//add the relations to areas
		areaManager.addBGToBGArea(group, area1);
		areaManager.addBGToBGArea(group, area2);
		dbInstance.commitAndCloseSession();
		
		//check find groups
		List<BusinessGroup> groupArea1s = areaManager.findBusinessGroupsOfArea(area1);
		Assert.assertNotNull(groupArea1s);
		Assert.assertEquals(1, groupArea1s.size());
		Assert.assertTrue(groupArea1s.contains(group));
		
		List<BusinessGroup> groupArea2s = areaManager.findBusinessGroupsOfArea(area2);
		Assert.assertNotNull(groupArea2s);
		Assert.assertEquals(1, groupArea2s.size());
		Assert.assertTrue(groupArea2s.contains(group));
		
		// remove resource2 from group
		businessGroupService.removeResourceFrom(Collections.singletonList(group), resource2);
		
		List<BusinessGroup> groupArea1s_remove = areaManager.findBusinessGroupsOfArea(area1);
		Assert.assertNotNull(groupArea1s_remove);
		Assert.assertEquals(1, groupArea1s.size());
		Assert.assertTrue(groupArea1s_remove.contains(group));
		
		List<BusinessGroup> groupArea2s_remove = areaManager.findBusinessGroupsOfArea(area2);
		Assert.assertTrue(groupArea2s_remove.isEmpty());
	}
	
	@Test
	public void findGroupsByAreas() {
		//create a resource, 3 area and 2 group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area3 = areaManager.createAndPersistBGArea("area-3-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
		List<BGArea> allAreas = new ArrayList<>();
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
	public void findBusinessGroupsOfAreaKeys_andGroupKeys() {
		//create a resource, 3 area and 2 group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("find-group-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("find-group-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "find-group-1-area", "find-group-in-area-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "find-group-2-area", "find-group-in-area-2-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "find-group-2-area", "find-group-in-area-3-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group1, area2);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area2);
		dbInstance.commitAndCloseSession();

		//check find area 2 -> all 3 groups
		List<Long> area2Key = Collections.singletonList(area2.getKey());
		List<BusinessGroup> groupsArea2 = areaManager.findBusinessGroupsOfAreaKeys(area2Key);
		Assert.assertNotNull(groupsArea2);
		Assert.assertEquals(3, groupsArea2.size());
		Assert.assertTrue(groupsArea2.contains(group1));
		Assert.assertTrue(groupsArea2.contains(group2));
		Assert.assertTrue(groupsArea2.contains(group3));
		
		List<Long> groupKeysArea2 = areaManager.findBusinessGroupKeysOfAreaKeys(area2Key);
		Assert.assertNotNull(groupKeysArea2);
		Assert.assertEquals(3, groupKeysArea2.size());
		Assert.assertTrue(groupKeysArea2.contains(group1.getKey()));
		Assert.assertTrue(groupKeysArea2.contains(group2.getKey()));
		Assert.assertTrue(groupKeysArea2.contains(group3.getKey()));	
		
		//check find area 1 -> only group 1
		List<Long> area1Key = Collections.singletonList(area1.getKey());
		List<BusinessGroup> groupsArea1 = areaManager.findBusinessGroupsOfAreaKeys(area1Key);
		Assert.assertNotNull(groupsArea1);
		Assert.assertEquals(1, groupsArea1.size());
		Assert.assertTrue(groupsArea1.contains(group1));
		
		List<Long> groupKeysArea1 = areaManager.findBusinessGroupKeysOfAreaKeys(area1Key);
		Assert.assertNotNull(groupKeysArea1);
		Assert.assertEquals(1, groupKeysArea1.size());
		Assert.assertTrue(groupKeysArea1.contains(group1.getKey()));	

		//check find area 1 and 2 -> all 3 groups
		List<Long> areaKeys = new ArrayList<>(2);
		areaKeys.add(area1.getKey());
		areaKeys.add(area2.getKey());
		List<BusinessGroup> groupsAreas = areaManager.findBusinessGroupsOfAreaKeys(areaKeys);
		Assert.assertNotNull(groupsAreas);
		Assert.assertEquals(3, groupsAreas.size());
		Assert.assertTrue(groupsAreas.contains(group1));
		Assert.assertTrue(groupsAreas.contains(group2));
		Assert.assertTrue(groupsAreas.contains(group3));
		
		List<Long> groupKeysAreas = areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys);
		Assert.assertNotNull(groupKeysAreas);
		Assert.assertEquals(3, groupKeysAreas.size());
		Assert.assertTrue(groupKeysAreas.contains(group1.getKey()));
		Assert.assertTrue(groupKeysAreas.contains(group2.getKey()));
		Assert.assertTrue(groupKeysAreas.contains(group3.getKey()));
		
		//check with empty list
		List<BusinessGroup> emptyGroups = areaManager.findBusinessGroupsOfAreaKeys(Collections.<Long>emptyList());
		Assert.assertNotNull(emptyGroups);
		Assert.assertEquals(0, emptyGroups.size());
		List<Long> emptyGroupKeys = areaManager.findBusinessGroupKeysOfAreaKeys(Collections.<Long>emptyList());
		Assert.assertNotNull(emptyGroupKeys);
		Assert.assertEquals(0, emptyGroupKeys.size());
	}
	
	@Test
	public void countBGAreasOfBusinessGroups() {
		//create a resource, 3 area and 2 group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("count-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("count-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area3 = areaManager.createAndPersistBGArea("count-3-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "count-1-group", "count-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "count-2-group", "count-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group1, area2);
		areaManager.addBGToBGArea(group1, area3);
		areaManager.addBGToBGArea(group2, area1);
		dbInstance.commitAndCloseSession();
		
		//check with empty list
		int numOfAreas1 = areaManager.countBGAreasOfBusinessGroups(Collections.<BusinessGroup>emptyList());
		Assert.assertEquals(0, numOfAreas1);
		
		//num of areas of group2 -> only area1
		int numOfAreas2 = areaManager.countBGAreasOfBusinessGroups(Collections.singletonList(group2));
		Assert.assertEquals(1, numOfAreas2);
		
		//num of areas of group 1 and 2
		List<BusinessGroup> groups = new ArrayList<>(2);
		groups.add(group1);
		groups.add(group2);
		int numOfAreas3 = areaManager.countBGAreasOfBusinessGroups(groups);
		Assert.assertEquals(3, numOfAreas3);
	}
	
	@Test
	public void addAndDeleteRelations() {
		//create a resource, an area, a group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
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
	public void findBusinessGroupsOfAreaAttendedBy() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-3-" + UUID.randomUUID().toString());
		//create a resource, an area, a group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area1);
		dbInstance.commitAndCloseSession();
		//add attendee
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//find with resource
		List<BusinessGroup> groupId1 = areaManager.findBusinessGroupsOfAreaAttendedBy(id1, null, resource.getOlatResource());
		Assert.assertNotNull(groupId1);
		Assert.assertEquals(1, groupId1.size());
		Assert.assertTrue(groupId1.contains(group1));
		
		//find nothing with name and resource
		List<Long> area2Keys = Collections.singletonList(area2.getKey());
		List<BusinessGroup> groupId1Area2 = areaManager.findBusinessGroupsOfAreaAttendedBy(id1, area2Keys, resource.getOlatResource());
		Assert.assertNotNull(groupId1Area2);
		Assert.assertEquals(0, groupId1Area2.size());
		
		//find groups id 2 with name and resource
		List<Long> area1Keys = Collections.singletonList(area1.getKey());
		List<BusinessGroup> groupId2Area1 = areaManager.findBusinessGroupsOfAreaAttendedBy(id2, area1Keys, resource.getOlatResource());
		Assert.assertNotNull(groupId2Area1);
		Assert.assertEquals(2, groupId2Area1.size());
		Assert.assertTrue(groupId2Area1.contains(group2));
		Assert.assertTrue(groupId2Area1.contains(group3));
	}
	
	@Test
	public void isIdentityInBGArea() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("attendee-1-" + UUID.randomUUID().toString());
		//create a resource, an area, a group
		RepositoryEntry resource =  JunitTestHelper.createAndPersistRepositoryEntry();
		String areaName = UUID.randomUUID().toString();
		BGArea area1 = areaManager.createAndPersistBGArea("area-1-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area2 = areaManager.createAndPersistBGArea("area-2-" + areaName, "description:" + areaName, resource.getOlatResource());
		BGArea area3 = areaManager.createAndPersistBGArea("area-3-" + areaName, "description:" + areaName, resource.getOlatResource());
		//create 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "area-1-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "area-2-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "area-3-group", "area-group-desc", BusinessGroup.BUSINESS_TYPE,
				0, -1, false, false, resource);
		dbInstance.commitAndCloseSession();
		//add the relations
		areaManager.addBGToBGArea(group1, area1);
		areaManager.addBGToBGArea(group2, area1);
		areaManager.addBGToBGArea(group2, area2);
		areaManager.addBGToBGArea(group3, area3);
		dbInstance.commitAndCloseSession();
		//add attendee
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		//check in area 1
		boolean testArea1 = areaManager.isIdentityInBGArea(id1, "area-1-" + areaName, null, resource.getOlatResource());
		Assert.assertTrue(testArea1);
		//check in area 1
		boolean testArea2 = areaManager.isIdentityInBGArea(id1, "area-2-" + areaName, null, resource.getOlatResource());
		Assert.assertTrue(testArea2);
		//check in area 1
		boolean testArea3 = areaManager.isIdentityInBGArea(id1, "area-3-" + areaName, null, resource.getOlatResource());
		Assert.assertFalse(testArea3);
		
		//check with keys
		//check in area 1
		boolean testArea4 = areaManager.isIdentityInBGArea(id1, null, area1.getKey(), resource.getOlatResource());
		Assert.assertTrue(testArea4);
		//check in area 1
		boolean testArea5 = areaManager.isIdentityInBGArea(id1, null, area2.getKey(), resource.getOlatResource());
		Assert.assertTrue(testArea5);
		//check in area 1
		boolean testArea6 = areaManager.isIdentityInBGArea(id1, null, area3.getKey(), resource.getOlatResource());
		Assert.assertFalse(testArea6);
	}
	
	/** 
	 * Do in different threads ant check that no exception happens :
	 * 1. create BG-Area
	 * 5. delete
	 */
	@Test
	public void testSynchronisationCreateBGArea() {

		final int maxLoop = 75; // => 400 x 100ms => 40sec => finished in 50sec
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
			log.error("", e);
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
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
						BGArea bgArea = areaManager.createAndPersistBGArea(areaName, "description:" + areaName, c1);
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

		final int maxLoop = 30;
		final String areaName = "BGArea_2";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finfishCount = new CountDownLatch(3);
		
		
		BGArea bgArea = areaManager.findBGArea(areaName, c1);
		assertNull(bgArea);
		bgArea = areaManager.createAndPersistBGArea(areaName, "description:" + areaName, c1);
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
			log.error("exception: ", exception);
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
}