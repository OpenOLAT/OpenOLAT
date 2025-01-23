/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.manager;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupMembershipHistoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;
	
	@Test
	public void createGroupMembershipHistory() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-1-");
		Group group = groupDao.createGroup();
		dbInstance.commit();
		GroupMembershipHistory point = groupMembershipHistoryDao.createMembershipHistory(group, id,
				"owner", GroupMembershipStatus.declined, null, null, id, null);
		Assert.assertNotNull(point);
	}
	
	@Test
	public void loadMembershipHistory() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-2-");
		Group group = groupDao.createGroup();
		GroupMembershipHistory point = groupMembershipHistoryDao.createMembershipHistory(group, id,
				"owner", GroupMembershipStatus.declined, null, null, id, null);
		dbInstance.commit();
		
		List<GroupMembershipHistory> history = groupMembershipHistoryDao.loadMembershipHistory(group, id);
		Assertions.assertThat(history)
			.hasSize(1)
			.containsExactly(point);
	}
	
	@Test
	public void deleteMembershipHistory() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bgrp-3-");
		Group groupToDelete = groupDao.createGroup();
		GroupMembershipHistory point1 = groupMembershipHistoryDao.createMembershipHistory(groupToDelete, id,
				"owner", GroupMembershipStatus.active, null, null, id, null);
		Group refGroup = groupDao.createGroup();
		GroupMembershipHistory point2 = groupMembershipHistoryDao.createMembershipHistory(refGroup, id,
				"owner", GroupMembershipStatus.active, null, null, id, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(point1);
		Assert.assertNotNull(point2);
		Assert.assertNotNull(groupToDelete);
		Assert.assertNotNull(refGroup);
		
		groupMembershipHistoryDao.deleteMembershipHistory(groupToDelete);
		dbInstance.commitAndCloseSession();
		
		List<GroupMembershipHistory> history = groupMembershipHistoryDao.loadMembershipHistory(groupToDelete, id);
		Assertions.assertThat(history)
			.isEmpty();
		
		List<GroupMembershipHistory> refHistory = groupMembershipHistoryDao.loadMembershipHistory(refGroup, id);
		Assertions.assertThat(refHistory)
			.hasSize(1)
			.containsExactly(point2);
	}
}
