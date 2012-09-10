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
package org.olat.basesecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the basic functions of the base security manager.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BaseSecurityManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;

	@Test
	public void testFindIdentityByUser() {
		//create a user it
		String username = "find-me-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(username);
		Assert.assertNotNull(id);
		Assert.assertNotNull(id.getUser());
		dbInstance.commitAndCloseSession();
		
		//find it
		Identity foundId = securityManager.findIdentityByUser(id.getUser());
		Assert.assertNotNull(foundId);
		Assert.assertEquals(username, foundId.getName());
		Assert.assertEquals(id, foundId);
		Assert.assertEquals(id.getUser(), foundId.getUser());
	}
	
	@Test
	public void testFindIdentityByName() {
		//create a user it
		String name = "find-me-" + UUID.randomUUID().toString();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser(name);
		Assert.assertNotNull(id);
		Assert.assertEquals(name, id.getName());
		dbInstance.commitAndCloseSession();
		
		//find it
		Identity foundId = securityManager.findIdentityByName(name);
		Assert.assertNotNull(foundId);
		Assert.assertEquals(name, foundId.getName());
		Assert.assertEquals(id, foundId);
	}
	
	@Test
	public void testGetSecurityGroupsForIdentity() {
		// create
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser( "find-sec-" + UUID.randomUUID().toString());
		SecurityGroup secGroup1 = securityManager.createAndPersistSecurityGroup();
		SecurityGroup secGroup2 = securityManager.createAndPersistSecurityGroup();
		SecurityGroup secGroup3 = securityManager.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id, secGroup1);
		securityManager.addIdentityToSecurityGroup(id, secGroup2);
		dbInstance.commitAndCloseSession();
		
		//check
		List<SecurityGroup> secGroups = securityManager.getSecurityGroupsForIdentity(id);
		Assert.assertNotNull(secGroups);
		Assert.assertTrue(secGroups.contains(secGroup1));
		Assert.assertTrue(secGroups.contains(secGroup2));
		Assert.assertFalse(secGroups.contains(secGroup3));
	}
	
	@Test
	public void testCreateNamedGroup() {
		String name = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		SecurityGroup ng = securityManager.createAndPersistNamedSecurityGroup(name);
		dbInstance.commitAndCloseSession();
		
		SecurityGroup sgFound = securityManager.findSecurityGroupByName(name);
		Assert.assertNotNull(sgFound);
		Assert.assertEquals(sgFound.getKey(), ng.getKey());
	}
	
	@Test
	public void testRemoveFromSecurityGroup() {
		//create a security group with 2 identites
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser( "rm-1-sec-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser( "rm-2-sec-" + UUID.randomUUID().toString());
		SecurityGroup secGroup = securityManager.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id1, secGroup);
		securityManager.addIdentityToSecurityGroup(id2, secGroup);
		dbInstance.commitAndCloseSession();
		
		//remove the first one
		securityManager.removeIdentityFromSecurityGroup(id1, secGroup);
		dbInstance.commitAndCloseSession();
		
		int countMembers = securityManager.countIdentitiesOfSecurityGroup(secGroup);
		Assert.assertEquals(1, countMembers);
		List<Identity> members = securityManager.getIdentitiesOfSecurityGroup(secGroup);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(id2, members.get(0));
	}
	
	@Test
	public void testLoadIdentityByKeys() {
		//create a security group with 2 identites
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser( "load-1-sec-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser( "load-2-sec-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		List<Long> keys = new ArrayList<Long>(2);
		keys.add(id1.getKey());
		keys.add(id2.getKey());
		List<Identity> identities = securityManager.loadIdentityByKeys(keys);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}
	
}
