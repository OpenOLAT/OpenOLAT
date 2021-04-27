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
package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.ContactVOes;
import org.springframework.beans.factory.annotation.Autowired;

public class ContactsTest extends OlatRestTestCase {
	
	private static boolean initialized = false;

	private static Identity owner1, owner2, owner3, part1, part2, part3;
	private static BusinessGroup g1, g2;
	private static BusinessGroup g3, g4;
	private static OLATResource course;

	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private OrganisationService organisationService;
	
	@Before
	public void setUp() throws Exception {
		if(initialized) return;
			//create a course with learn group
			
		owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-one");
		owner2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-two");
		owner3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-three");
		part1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-four");
		part2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-five");
		part3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-contacts-six");
		
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		course = OLATResourceManager.getInstance().findOrPersistResourceable(resourceable);
		
		RepositoryService rs = CoreSpringFactory.getImpl(RepositoryService.class);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = rs.create(null, "administrator", "-", "rest-re", null, course,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		rs.update(re);
		DBFactory.getInstance().commit();
			
		//create learn group
		// 1) context one: learning groups
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		// create groups without waiting list
		g1 = businessGroupService.createBusinessGroup(null, "rest-g1", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c1);
		g2 = businessGroupService.createBusinessGroup(null, "rest-g2", null, BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, c1);
		DBFactory.getInstance().commit();
    
		//permission to see owners and participants
		businessGroupService.updateDisplayMembers(g1, false, false, false, false, false, false, false);
		businessGroupService.updateDisplayMembers(g2, true, true, false, false, false, false, false);
    
		// members g1
		businessGroupRelationDao.addRole(owner1, g1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(owner2, g1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(part1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part2, g1, GroupRoles.participant.name());
    
		// members g2
		businessGroupRelationDao.addRole(owner1, g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(part1, g2, GroupRoles.participant.name());
    
    
		// 2) context two: right groups
		RepositoryEntry c2 =  JunitTestHelper.createAndPersistRepositoryEntry();
		// groups
		g3 = businessGroupService.createBusinessGroup(null, "rest-g3", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c2);
		g4 = businessGroupService.createBusinessGroup(null, "rest-g4", null, BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, c2);
		DBFactory.getInstance().commit();
		
		businessGroupService.updateDisplayMembers(g3, false, true, false, false, false, false, false);
		businessGroupService.updateDisplayMembers(g4, false, true, false, false, false, false, false);
		// members -> default participants are visible
		businessGroupRelationDao.addRole(owner1, g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part3, g3, GroupRoles.participant.name());
    
		businessGroupRelationDao.addRole(owner2, g4, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part3, g4, GroupRoles.participant.name());
    
		DBFactory.getInstance().commitAndCloseSession(); // simulate user clicks
		initialized = true;
	}
	
	@Test
	public void testGetContactsDirectOwner1() throws IOException {
		List<Identity> identities = businessGroupService.findContacts(owner1, 0, -1);
		
		assertEquals(2, identities.size());
		assertFalse(identities.contains(owner1));//not a contact of myself
		assertFalse(identities.contains(owner2));//no
		assertFalse(identities.contains(owner3));//no
		assertTrue(identities.contains(part1));//yes -> g1
		assertFalse(identities.contains(part2));//no
		assertTrue(identities.contains(part3));//yes -> g3
	}
	
	@Test
	public void testCountContactsDirectOwner1() throws IOException {
		int numOfContacts = businessGroupService.countContacts(owner1);
		assertEquals(2, numOfContacts);
	}
	
	@Test
	public void testGetContactsDirectOwner2() throws IOException {
		List<Identity> identities = businessGroupService.findContacts(owner2, 0, -1);
		
		assertEquals(1, identities.size());
		assertFalse(identities.contains(owner1));//no
		assertFalse(identities.contains(owner2));//not contact of myself
		assertFalse(identities.contains(owner3));//no
		assertFalse(identities.contains(part1));//no
		assertFalse(identities.contains(part2));//no
		assertTrue(identities.contains(part3));//yes -> g4
	}
	
	@Test
	public void testCountContactsDirectOwner2() throws IOException {
		int numOfContacts = businessGroupService.countContacts(owner2);
		assertEquals(1, numOfContacts);
	}
	
	@Test
	public void testGetContactsRest() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("rest-contacts-two", "A6B7C8"));

		UriBuilder uri = UriBuilder.fromUri(getContextURI()).path("contacts").queryParam("start", "0").queryParam("limit", "10");
		HttpGet method = conn.createGet(uri.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ContactVOes contacts = conn.parse(response, ContactVOes.class);
		assertNotNull(contacts);
		assertNotNull(contacts.getUsers());
		assertEquals(1, contacts.getUsers().length);
		assertEquals(1, contacts.getTotalCount());
		//owner3 -> g4
		assertEquals(part3.getKey(), contacts.getUsers()[0].getKey());
		
		conn.shutdown();
	}
}
