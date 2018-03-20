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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityGroupDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	
	/**
	 * Dummy test to make sure all works as wanted
	 */
	@Test
	public void createSecurityGroupMembership() {
		//create a user with the default provider
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("update-membership-" + UUID.randomUUID().toString());
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(identity, secGroup);
		dbInstance.commitAndCloseSession();

		boolean member = securityManager.isIdentityInSecurityGroup(identity, secGroup);
		Assert.assertTrue(member);
	}
	
	/**
	 * We remove the optimistic locking from SecurityGroupMembershipImpl mapping
	 */
	@Test
	public void createAndUpdateSecurityGroupMembership_lastCommitWin() {
		//create a user with the default provider
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("update-membership-" + UUID.randomUUID().toString());
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		
		SecurityGroupMembershipImpl sgmsi = new SecurityGroupMembershipImpl();
		sgmsi.setIdentity(identity);
		sgmsi.setSecurityGroup(secGroup);
		sgmsi.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().persist(sgmsi);
		dbInstance.commitAndCloseSession();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		sgmsi.setLastModified(cal.getTime());
		dbInstance.getCurrentEntityManager().merge(sgmsi);
		dbInstance.commitAndCloseSession();
	
		cal.add(Calendar.DATE, -1);
		sgmsi.setLastModified(cal.getTime());
		dbInstance.getCurrentEntityManager().merge(sgmsi);
		dbInstance.commitAndCloseSession();	
	}
	
	@Test
	public void testGetIdentitiesOfSecurityGroup() {
		//create 3 identities and 2 security groups
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-3-" + UUID.randomUUID().toString());
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id1, secGroup);
		securityManager.addIdentityToSecurityGroup(id2, secGroup);
		securityManager.addIdentityToSecurityGroup(id3, secGroup);
		dbInstance.commitAndCloseSession();
		
		//retrieve them
		List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(secGroup, 0, -1);
		Assert.assertNotNull(identities);
		Assert.assertEquals(3, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
		Assert.assertTrue(identities.contains(id3));
	}
	
	@Test
	public void testGetIdentitiesOfSecurityGroups() {
		//create 3 identities and 2 security groups
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("user-sec-3-" + UUID.randomUUID().toString());
		SecurityGroup secGroup1 = securityGroupDao.createAndPersistSecurityGroup();
		SecurityGroup secGroup2 = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id1, secGroup1);
		securityManager.addIdentityToSecurityGroup(id2, secGroup1);
		securityManager.addIdentityToSecurityGroup(id2, secGroup2);
		securityManager.addIdentityToSecurityGroup(id3, secGroup2);
		dbInstance.commitAndCloseSession();
		
		//retrieve them
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		secGroups.add(secGroup1);
		secGroups.add(secGroup2);
		List<Identity> identities = securityManager.getIdentitiesOfSecurityGroups(secGroups);
		Assert.assertNotNull(identities);
		Assert.assertEquals(3, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
		Assert.assertTrue(identities.contains(id3));
	}
	
	@Test
	public void testGetSecurityGroupsForIdentity() {
		// create
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser( "find-sec");
		SecurityGroup secGroup1 = securityGroupDao.createAndPersistSecurityGroup();
		SecurityGroup secGroup2 = securityGroupDao.createAndPersistSecurityGroup();
		SecurityGroup secGroup3 = securityGroupDao.createAndPersistSecurityGroup();
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
	public void testRemoveIdentityFromSecurityGroup() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("sec-1");
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id, secGroup);
		dbInstance.commitAndCloseSession();
	
		Assert.assertTrue(securityManager.isIdentityInSecurityGroup(id, secGroup));
		securityManager.removeIdentityFromSecurityGroup(id, secGroup);
		Assert.assertFalse(securityManager.isIdentityInSecurityGroup(id, secGroup));
		securityManager.addIdentityToSecurityGroup(id, secGroup);
		Assert.assertTrue(securityManager.isIdentityInSecurityGroup(id, secGroup));
	}
	
	@Test
	public void testGetIdentitiesAndDateOfSecurityGroup() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("sec-2");
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id, secGroup);
		dbInstance.commitAndCloseSession();

		List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(secGroup);// not sortedByAddDate
		Assert.assertFalse("Found no users", identities.isEmpty());
		Object[] firstIdentity = identities.get(0);
		Assert.assertTrue("Wrong type, Identity[0] must be an Identity", firstIdentity[0] instanceof Identity);
		Assert.assertTrue("Wrong type, Identity[1] must be a Date", firstIdentity[1] instanceof Date);
	}
	
	@Test
	public void testRemoveFromSecurityGroup() {
		//create a security group with 2 identites
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser( "rm-1-sec");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser( "rm-2-sec");
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
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
	public void testRemoveFromSecurityGroup_list() {
		//create a security group with 2 identites
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser( "rm-3-sec-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser( "rm-4-sec-" + UUID.randomUUID().toString());
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id1, secGroup);
		securityManager.addIdentityToSecurityGroup(id2, secGroup);
		dbInstance.commitAndCloseSession();
		
		//remove the first one
		List<Identity> ids = new ArrayList<Identity>();
		ids.add(id1);
		ids.add(id2);
		securityManager.removeIdentityFromSecurityGroups(ids, Collections.singletonList(secGroup));
		dbInstance.commitAndCloseSession();
		
		int countMembers = securityManager.countIdentitiesOfSecurityGroup(secGroup);
		Assert.assertEquals(0, countMembers);
		List<Identity> members = securityManager.getIdentitiesOfSecurityGroup(secGroup);
		Assert.assertNotNull(members);
		Assert.assertTrue(members.isEmpty());
		
		//check if robust against null and empty
		securityManager.removeIdentityFromSecurityGroups(ids, Collections.<SecurityGroup>emptyList());
		securityManager.removeIdentityFromSecurityGroups(Collections.<Identity>emptyList(), Collections.singletonList(secGroup));
		securityManager.removeIdentityFromSecurityGroups(ids, null);
		securityManager.removeIdentityFromSecurityGroups(null, Collections.singletonList(secGroup));
	}
	
	@Test
	public void deleteSecurityGroup() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("test-del-2");
		SecurityGroup secGroup = securityGroupDao.createAndPersistSecurityGroup();
		securityManager.addIdentityToSecurityGroup(id, secGroup);
		dbInstance.commitAndCloseSession();
		
		//delete the security group (and membership, and policies)
		securityManager.deleteSecurityGroup(secGroup);
		dbInstance.commit();

		boolean membership = securityManager.isIdentityInSecurityGroup(id, secGroup);
		Assert.assertFalse(membership);
	}

}
