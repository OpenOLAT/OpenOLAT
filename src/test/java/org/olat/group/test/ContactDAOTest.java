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

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupPropertyDAO;
import org.olat.group.manager.ContactDAO;
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
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupPropertyDAO businessGroupPropertyManager;

	@Test
	public void testContacts() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("cdao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdlo", "gdlo-desc", 0, 5, true, false, true, true, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdmo", "gdmo-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdno", "gdno-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getOwnerGroup());
		securityManager.addIdentityToSecurityGroup(id1, group2.getPartipiciantGroup());
		//id2 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id2, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id2, group2.getOwnerGroup());
		//id3 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id3, group1.getPartipiciantGroup());
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup());
		//id4 -> group 2
		securityManager.addIdentityToSecurityGroup(id4, group2.getOwnerGroup());
		//id5 -> group 3
		securityManager.addIdentityToSecurityGroup(id5, group3.getOwnerGroup());
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
	public void testContactsWithMoreExclusions() {
		//5 identities
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-3-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-4-" + UUID.randomUUID().toString());
		Identity id5 = JunitTestHelper.createAndPersistIdentityAsUser("ddao-5-" + UUID.randomUUID().toString());
		//create 3 groups
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "gdoo", "gdoo-desc", 0, 5, true, false, true, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "gdpo", "gdpo-desc", 0, 5, true, false, false, true, false);
		BusinessGroup group3 = businessGroupDao.createAndPersist(null, "gdqo", "gdqo-desc", 0, 5, true, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//id1 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id1, group1.getOwnerGroup());				//visible
		securityManager.addIdentityToSecurityGroup(id1, group2.getPartipiciantGroup());	//visible
		//id2 -> group 1 and 2
		securityManager.addIdentityToSecurityGroup(id2, group1.getPartipiciantGroup()); //not
		securityManager.addIdentityToSecurityGroup(id2, group2.getOwnerGroup());        //not
		//id3 -> group 1 and 3
		securityManager.addIdentityToSecurityGroup(id3, group2.getOwnerGroup());        //not
		securityManager.addIdentityToSecurityGroup(id3, group3.getPartipiciantGroup()); //not
		//id4 -> group 2
		securityManager.addIdentityToSecurityGroup(id4, group2.getOwnerGroup());        //not
		securityManager.addIdentityToSecurityGroup(id4, group3.getOwnerGroup());        //not
		//id5 -> group 3
		securityManager.addIdentityToSecurityGroup(id5, group3.getOwnerGroup());        //not
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
}
