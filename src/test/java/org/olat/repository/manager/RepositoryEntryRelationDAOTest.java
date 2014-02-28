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
package org.olat.repository.manager;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRelationDAOTest extends OlatTestCase {
	
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void getDefaultGroup() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "rel", "rel", null, null);
		dbInstance.commitAndCloseSession();
		
		Group group = repositoryEntryRelationDao.getDefaultGroup(re);
		Assert.assertNotNull(group);
	}
	
	@Test
	public void addRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-2-");
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "rel", "rel", null, null);
		dbInstance.commit();

		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
	}
	
	@Test
	public void hasRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-3-");
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "rel", "rel", null, null);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
		
		boolean owner = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name());
		Assert.assertTrue(owner);
		boolean participant = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.participant.name());
		Assert.assertFalse(participant);
	}
	
	@Test
	public void removeRole() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("add-role-4-");
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "rel", "rel", null, null);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id, re, GroupRoles.owner.name());
		dbInstance.commit();
		
		//check
		Assert.assertTrue(repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name()));
		
		//remove role
		int removeRoles = repositoryEntryRelationDao.removeRole(id, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, removeRoles);
		
		//check
		boolean owner = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.owner.name());
		Assert.assertFalse(owner);
		boolean participant = repositoryEntryRelationDao.hasRole(id, re, GroupRoles.participant.name());
		Assert.assertFalse(participant);
	}
	
	@Test
	public void getMembersAndCountMembers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("member-2-");
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "rel", "rel", null, null);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		dbInstance.commit();

		//all members
		List<Identity> members = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup);
		int numOfMembers = repositoryEntryRelationDao.countMembers(re);
		Assert.assertNotNull(members);
		Assert.assertEquals(2, members.size());
		Assert.assertEquals(2, numOfMembers);
		Assert.assertTrue(members.contains(id1));
		Assert.assertTrue(members.contains(id2));
		
		//participant
		List<Identity> participants = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		int numOfParticipants = repositoryEntryRelationDao.countMembers(re, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(1, participants.size());
		Assert.assertEquals(1, numOfParticipants);
		Assert.assertTrue(members.contains(id2));
	}
}
