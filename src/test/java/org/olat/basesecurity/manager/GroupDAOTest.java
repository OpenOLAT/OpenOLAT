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

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	@Test
	public void createGroup() {
		Group group = groupDao.createGroup();
		dbInstance.commit();
		
		Assert.assertNotNull(group);
	}
	
	@Test
	public void createGroupMembership() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembership(group, id, "author");

		dbInstance.commit();
		
		Assert.assertNotNull(membership);
	}
	
	@Test
	public void createGroupMembership_v2() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembership(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.getCurrentEntityManager().detach(group);
		dbInstance.commitAndCloseSession();
		
		GroupImpl loadedGroup = (GroupImpl)groupDao.loadGroup(group.getKey());
		Assert.assertNotNull(loadedGroup);
		Set<GroupMembership> members = loadedGroup.getMembers();
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}
	
	@Test
	public void getMemberships() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		GroupMembership membership = groupDao.addMembership(group, id, "author");
		dbInstance.commit();
		
		Assert.assertNotNull(membership);
		dbInstance.getCurrentEntityManager().detach(group);
		dbInstance.commitAndCloseSession();
		
		List<GroupMembership> members = groupDao.getMemberships(group, "author");
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
	}

}
