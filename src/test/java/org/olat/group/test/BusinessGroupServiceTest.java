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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupServiceTest extends OlatTestCase {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	
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
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(); 
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, false, false, null, 0, 5);
		Assert.assertNotNull(groups);
	}
	
	@Test
	public void testGroupsOfBGContext() {
		OLATResource c1 = JunitTestHelper.createRandomResource();
		OLATResource c2 = JunitTestHelper.createRandomResource();

		DBFactory.getInstance().closeSession(); // simulate user clicks
		assertTrue(businessGroupService.findBusinessGroups(null, null, false, false, c1, 0, -1).isEmpty());
		assertTrue(businessGroupService.countBusinessGroups(null, null, false, false, c1) == 0);

		DBFactory.getInstance().closeSession(); // simulate user clicks
		BusinessGroup g1 = businessGroupService.createBusinessGroup(null, "g1", null, 0, 10, false, false, c1);
		assertNotNull(g1);
		BusinessGroup g2 = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c1);
		assertNotNull(g2);
		BusinessGroup g3 = businessGroupService.createBusinessGroup(null, "g3", null, 0, 10, false, false, c2);
		assertNotNull(g3);

		BusinessGroup g2douplicate = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c1);
		assertNull(g2douplicate); // name duplicate names allowed per group context

		BusinessGroup g4 = businessGroupService.createBusinessGroup(null, "g2", null, 0, 10, false, false, c2);
		assertNotNull(g4); // name duplicate in other context allowed

		DBFactory.getInstance().closeSession(); // simulate user clicks
		Assert.assertEquals(2, businessGroupService.findBusinessGroups(null, null, false, false, c1, 0, -1).size());
		Assert.assertEquals(2, businessGroupService.countBusinessGroups(null, null, false, false, c1));
	}
}