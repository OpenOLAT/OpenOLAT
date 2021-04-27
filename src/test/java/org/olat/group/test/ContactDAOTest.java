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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.manager.ContactDAO;
import org.olat.group.model.ContactViewExtended;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ContactDAOTest extends OlatTestCase {

	@Autowired
	private ContactDAO contactDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private DB dbInstance;

	@Test
	public void testContacts() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());
		//id2 -> group 1 and 2
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.coach.name());
		//id3 -> group 1 and 3
		businessGroupRelationDao.addRole(id3, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name());
		//id4 -> group 2
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.coach.name());
		//id5 -> group 3
		businessGroupRelationDao.addRole(id5, group3, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : contact to id2 and id3
		int numOfContact1 = contactDao.countContacts(id1);
		Assert.assertEquals(2, numOfContact1);
		List<Identity> contactList1 = contactDao.findContacts(id1, 0, -1);
		Assert.assertEquals(2, contactList1.size());
		Assert.assertTrue(contactList1.contains(id2));
		Assert.assertTrue(contactList1.contains(id3));
		
		//check identity2 : contact to id1 and id3
		int numOfContact2 = contactDao.countContacts(id2);
		Assert.assertEquals(2, numOfContact2);
		List<Identity> contactList2 = contactDao.findContacts(id2, 0, -1);
		Assert.assertEquals(2, contactList2.size());
		Assert.assertTrue(contactList2.contains(id1));
		Assert.assertTrue(contactList2.contains(id3));
		
		//check identity3 : contact to id1 and id2
		int numOfContact3 = contactDao.countContacts(id3);
		Assert.assertEquals(2, numOfContact3);
		List<Identity> contactList3 = contactDao.findContacts(id3, 0, -1);
		Assert.assertEquals(2, contactList3.size());
		Assert.assertTrue(contactList3.contains(id1));
		Assert.assertTrue(contactList3.contains(id2));
		
		//check identity4 : contact to id1
		int numOfContact4 = contactDao.countContacts(id4);
		Assert.assertEquals(1, numOfContact4);
		List<Identity> contactList4 = contactDao.findContacts(id4, 0, -1);
		Assert.assertEquals(1, contactList4.size());
		Assert.assertTrue(contactList4.contains(id1));
		
		//check identity5 : contact to id2
		int numOfContact5 = contactDao.countContacts(id5);
		Assert.assertEquals(0, numOfContact5);
		List<Identity> contactList5 = contactDao.findContacts(id5, 0, -1);
		Assert.assertEquals(0, contactList5.size());
	}
	
	@Test
	public void testContactsWithMoreExclusions() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdoo", "gdoo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdpo", "gdpo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdqo", "gdqo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.coach.name());			//visible
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());	//visible
		//id2 -> group 1 and 2
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.participant.name()); //not
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.coach.name());        //not
		//id3 -> group 1 and 3
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.coach.name());        //not
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name()); //not
		//id4 -> group 2
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.coach.name());        //not

		businessGroupRelationDao.addRole(id4, group3, GroupRoles.coach.name());        //not
		//id5 -> group 3
		businessGroupRelationDao.addRole(id5, group3, GroupRoles.coach.name());        //not
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : contact to id2 and id3
		int numOfContact1 = contactDao.countContacts(id1);
		Assert.assertEquals(0, numOfContact1);
		List<Identity> contactList1 = contactDao.findContacts(id1, 0, -1);
		Assert.assertEquals(0, contactList1.size());
		
		//check identity2 : contact to id1 and id3
		int numOfContact2 = contactDao.countContacts(id2);
		Assert.assertEquals(1, numOfContact2);
		List<Identity> contactList2 = contactDao.findContacts(id2, 0, -1);
		Assert.assertEquals(1, contactList2.size());
		Assert.assertTrue(contactList2.contains(id1));
		
		//check identity3 : contact to id1 and id2
		int numOfContact3 = contactDao.countContacts(id3);
		Assert.assertEquals(1, numOfContact3);
		List<Identity> contactList3 = contactDao.findContacts(id3, 0, -1);
		Assert.assertEquals(1, contactList3.size());
		Assert.assertTrue(contactList3.contains(id1));
		
		//check identity4 : contact to id1
		int numOfContact4 = contactDao.countContacts(id4);
		Assert.assertEquals(1, numOfContact4);//!!! ne marche pas
		List<Identity> contactList4 = contactDao.findContacts(id4, 0, -1);
		Assert.assertEquals(1, contactList4.size());
		Assert.assertTrue(contactList4.contains(id1));
		
		//check identity5 : contact to id2
		int numOfContact5 = contactDao.countContacts(id5);
		Assert.assertEquals(0, numOfContact5);
		List<Identity> contactList5 = contactDao.findContacts(id5, 0, -1);
		Assert.assertEquals(0, contactList5.size());
	}
	
	@Test
	public void testDistinctGroupOwnersParticipants() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("edao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("edao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("edao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("edao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("edao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "geoo", "gdoo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gepo", "gdpo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "geqo", "gdqo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.coach.name());		//visible
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());	//visible
		//id2 -> group 1 and 2
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.participant.name()); //not
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.coach.name());        //not
		//id3 -> group 1 and 3
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.coach.name());       //not
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name()); //not
		//id4 -> group 2
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.coach.name());        //not
		businessGroupRelationDao.addRole(id4, group3, GroupRoles.coach.name());        //not
		//id5 -> group 3
		businessGroupRelationDao.addRole(id5, group3, GroupRoles.coach.name());        //not
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : no contact
		Collection<Long> contacts1 = contactDao.getDistinctGroupOwnersParticipants(id1);
		Assert.assertNotNull(contacts1);
		Assert.assertEquals(1, contacts1.size());
		Assert.assertTrue(contacts1.contains(id1.getKey()));
		
		//check identity2 : contact to id1 and id3
		Collection<Long> contacts2 = contactDao.getDistinctGroupOwnersParticipants(id2);
		Assert.assertNotNull(contacts2);
		Assert.assertEquals(1, contacts2.size());
		Assert.assertTrue(contacts2.contains(id1.getKey()));
		
		//check identity3 : contact to id1 and id2
		Collection<Long> contacts3 = contactDao.getDistinctGroupOwnersParticipants(id3);
		Assert.assertNotNull(contacts3);
		Assert.assertEquals(1, contacts3.size());
		Assert.assertTrue(contacts3.contains(id1.getKey()));
		
		//check identity4 : contact to id1
		Collection<Long> contacts4 = contactDao.getDistinctGroupOwnersParticipants(id4);
		Assert.assertNotNull(contacts4);
		Assert.assertEquals(1, contacts4.size());
		Assert.assertTrue(contacts4.contains(id1.getKey()));
		
		//check identity5 : contact to id2
		Collection<Long> contacts5 = contactDao.getDistinctGroupOwnersParticipants(id5);
		Assert.assertNotNull(contacts5);
		Assert.assertEquals(0, contacts5.size());
	}
	
	@Test
	public void testGroupOwners() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gfoo", "gfoo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gfpo", "gfpo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gfqo", "gfqo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.coach.name());		//visible
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());	//visible
		//id2 -> group 1 and 2
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.participant.name()); //not
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.coach.name());       //not
		//id3 -> group 1 and 3
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.coach.name());        //not
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name()); //not
		//id4 -> group 2
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.coach.name());        //not
		businessGroupRelationDao.addRole(id4, group3, GroupRoles.coach.name());       //not
		//id5 -> group 3
		businessGroupRelationDao.addRole(id5, group3, GroupRoles.coach.name());        //not
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : no contact
		Collection<ContactViewExtended> contacts1 = contactDao.getGroupOwners(id1);
		Assert.assertNotNull(contacts1);
		Assert.assertEquals(1, contacts1.size());
		Assert.assertTrue(contacts1.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity2 : contact to id1 and id3
		Collection<ContactViewExtended> contacts2 = contactDao.getGroupOwners(id2);
		Assert.assertNotNull(contacts2);
		Assert.assertEquals(1, contacts2.size());
		Assert.assertTrue(contacts2.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity3 : contact to id1 and id2
		Collection<ContactViewExtended> contacts3 = contactDao.getGroupOwners(id3);
		Assert.assertNotNull(contacts3);
		Assert.assertEquals(0, contacts3.size());
		
		//check identity4 : contact to id1
		Collection<ContactViewExtended> contacts4 = contactDao.getGroupOwners(id4);
		Assert.assertNotNull(contacts4);
		Assert.assertEquals(0, contacts4.size());
		
		//check identity5 : contact to id2
		Collection<ContactViewExtended> contacts5 = contactDao.getGroupOwners(id5);
		Assert.assertNotNull(contacts5);
		Assert.assertEquals(0, contacts5.size());
	}
	
	@Test
	public void testGroupParticipants() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("fdao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gfoo", "gfoo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gfpo", "gfpo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gfqo", "gfqo-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		businessGroupRelationDao.addRole(id1, group1, GroupRoles.coach.name());		//visible
		businessGroupRelationDao.addRole(id1, group2, GroupRoles.participant.name());	//visible
		//id2 -> group 1 and 2
		businessGroupRelationDao.addRole(id2, group1, GroupRoles.participant.name()); //not
		businessGroupRelationDao.addRole(id2, group2, GroupRoles.coach.name());       //not
		//id3 -> group 1 and 3
		businessGroupRelationDao.addRole(id3, group2, GroupRoles.coach.name());       //not
		businessGroupRelationDao.addRole(id3, group3, GroupRoles.participant.name()); //not
		//id4 -> group 2
		businessGroupRelationDao.addRole(id4, group2, GroupRoles.coach.name());        //not
		businessGroupRelationDao.addRole(id4, group3, GroupRoles.coach.name());        //not
		//id5 -> group 3
		businessGroupRelationDao.addRole(id5, group3, GroupRoles.coach.name());        //not
		dbInstance.commitAndCloseSession();	
		
		//check identity1 : no contact
		Collection<ContactViewExtended> contacts1 = contactDao.getParticipants(id1);
		Assert.assertNotNull(contacts1);
		Assert.assertEquals(1, contacts1.size());
		Assert.assertTrue(contacts1.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity2 : contact to id1 and id3
		Collection<ContactViewExtended> contacts2 = contactDao.getParticipants(id2);
		Assert.assertNotNull(contacts2);
		Assert.assertEquals(1, contacts2.size());
		Assert.assertTrue(contacts2.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity3 : contact to id1 and id2
		Collection<ContactViewExtended> contacts3 = contactDao.getParticipants(id3);
		Assert.assertNotNull(contacts3);
		Assert.assertEquals(1, contacts3.size());
		Assert.assertTrue(contacts3.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity4 : contact to id1
		Collection<ContactViewExtended> contacts4 = contactDao.getParticipants(id4);
		Assert.assertNotNull(contacts4);
		Assert.assertEquals(1, contacts4.size());
		Assert.assertTrue(contacts4.iterator().next().getIdentityKey().equals(id1.getKey()));
		
		//check identity5 : contact to id2
		Collection<ContactViewExtended> contacts5 = contactDao.getParticipants(id5);
		Assert.assertNotNull(contacts5);
		Assert.assertEquals(0, contacts5.size());
	}
}
