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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupImportExportTest extends OlatTestCase {
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BGAreaManager areaManager;
	
	
	@Test
	public void importGroupsWithoutResource() throws URISyntaxException {
		URL input = BusinessGroupImportExportTest.class.getResource("learninggroupexport_2.xml");
		File importXml = new File(input.toURI());
		businessGroupService.importGroups(null, importXml);
		dbInstance.commitAndCloseSession();	
	}
	
	@Test
	public void importLearningGroupsWithResource() throws URISyntaxException {
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		
		URL input = BusinessGroupImportExportTest.class.getResource("learninggroupexport_2.xml");
		File importXml = new File(input.toURI());
		businessGroupService.importGroups(resource, importXml);
		dbInstance.commitAndCloseSession();	
		
		//check if all three groups are imported
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size());
		
		//get first group (members true, true, false) (no collaboration tools)
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("Export group 1");
		List<BusinessGroup> group1List = businessGroupService.findBusinessGroups(params, resource, 0, -1);
		Assert.assertNotNull(group1List);
		Assert.assertEquals(1, group1List.size());
		//check settings of the first group
		BusinessGroup group1 = group1List.get(0);
		Assert.assertEquals("Export group 1", group1.getName());
		Assert.assertEquals("<p>Export group 1</p>", group1.getDescription());
		Assert.assertFalse(group1.getAutoCloseRanksEnabled().booleanValue());
		Assert.assertFalse(group1.getWaitingListEnabled().booleanValue());
		//check display members settings
		Assert.assertTrue(group1.isOwnersVisibleIntern());
		Assert.assertTrue(group1.isParticipantsVisibleIntern());
		Assert.assertFalse(group1.isWaitingListVisibleIntern());
		//check collaboration tools
		CollaborationTools toolGroup1 = CollaborationToolsFactory.getInstance().getCollaborationToolsIfExists(group1);
		Assert.assertNotNull(toolGroup1);
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_CALENDAR));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_CHAT));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_CONTACT));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_FOLDER));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_FORUM));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_NEWS));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO));
		Assert.assertFalse(toolGroup1.isToolEnabled(CollaborationTools.TOOL_WIKI));

		//get third group (members true, true, true) (all collaboration tools)
		params.setExactName("Export group 3");
		List<BusinessGroup> group3List = businessGroupService.findBusinessGroups(params, resource, 0, -1);
		Assert.assertNotNull(group3List);
		Assert.assertEquals(1, group3List.size());
		//check settings of the first group
		BusinessGroup group3 = group3List.get(0);
		Assert.assertEquals("Export group 3", group3.getName());
		Assert.assertEquals("<p>Export group 2</p>", group3.getDescription());
		Assert.assertFalse(group3.getAutoCloseRanksEnabled().booleanValue());
		Assert.assertTrue(group3.getWaitingListEnabled().booleanValue());
		Assert.assertEquals(Integer.valueOf(25), group3.getMaxParticipants());
		//check display members settings
		Assert.assertTrue(group3.isOwnersVisibleIntern());
		Assert.assertTrue(group3.isParticipantsVisibleIntern());
		Assert.assertTrue(group3.isWaitingListVisibleIntern());
		//check collaboration tools
		CollaborationTools toolGroup3 = CollaborationToolsFactory.getInstance().getCollaborationToolsIfExists(group3);
		Assert.assertNotNull(toolGroup3);
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_CALENDAR));
		//Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_CHAT)); chat is not enabled during unit tests
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_CONTACT));
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_FOLDER));
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_FORUM));
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_NEWS));
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO));
		Assert.assertTrue(toolGroup3.isToolEnabled(CollaborationTools.TOOL_WIKI));
		Assert.assertEquals("<p>Hello Mitglied</p>", toolGroup3.lookupNews());
	}
	
	@Test
	public void importLearningGroupsAndAreasWithResource() throws URISyntaxException {
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		
		URL input = BusinessGroupImportExportTest.class.getResource("learninggroupexport_3.xml");
		File importXml = new File(input.toURI());
		businessGroupService.importGroups(resource, importXml);
		dbInstance.commitAndCloseSession();
		
		//check if all three groups are imported
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size());
		
		//check if all three areas are imported
		List<BGArea> areas = areaManager.findBGAreasInContext(resource.getOlatResource());
		Assert.assertNotNull(areas);
		Assert.assertEquals(3, areas.size());
		
		//check first area
		BGArea area1 = areaManager.findBGArea("Area 1", resource.getOlatResource());
		Assert.assertNotNull(area1);
		Assert.assertEquals("Area 1", area1.getName());
		Assert.assertEquals("<p>Area 1 description</p>", area1.getDescription());
		//check relation to groups
		List<BusinessGroup> groupArea1 = areaManager.findBusinessGroupsOfArea(area1);
		Assert.assertNotNull(groupArea1);
		Assert.assertEquals(2, groupArea1.size());
		Assert.assertTrue(groupArea1.get(0).getName().equals("Export group 1") || groupArea1.get(1).getName().equals("Export group 1"));
		Assert.assertTrue(groupArea1.get(0).getName().equals("Export group 2") || groupArea1.get(1).getName().equals("Export group 2"));

		//check empty area
		BGArea area3 = areaManager.findBGArea("Area 3", resource.getOlatResource());
		Assert.assertNotNull(area1);
		Assert.assertEquals("Area 3", area3.getName());
		//check relation to groups
		List<BusinessGroup> groupArea3 = areaManager.findBusinessGroupsOfArea(area3);
		Assert.assertNotNull(groupArea3);
		Assert.assertEquals(0, groupArea3.size());
	}


}
