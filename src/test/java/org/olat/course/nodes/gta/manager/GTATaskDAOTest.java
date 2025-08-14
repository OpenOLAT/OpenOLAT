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
package org.olat.course.nodes.gta.manager;

import java.io.File;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTATaskDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTATaskDAO gtaTaskDao;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void getEntry() {
		// Prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-dao-user-1");
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		// Select a task
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);

		//reload and check
		RepositoryEntry reloadedCourseEntry = gtaTaskDao.getEntry(response.getTask());
		dbInstance.commitAndCloseSession();// entry need to be fetched
		Assert.assertNotNull(reloadedCourseEntry);
		Assert.assertEquals(re, reloadedCourseEntry);
		Assert.assertNotNull(reloadedCourseEntry.getOlatResource());
		dbInstance.commit();
	}
	
	@Test
	public void getTasksByBusinessGroup() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gtask-dao-user-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gtask-dao-user-3");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gtaskdao", "gtaskdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		// Select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, null, node, taskFile, participant);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());
		
		//reload and check
		List<Task> groupTasks = gtaTaskDao.getTasks(businessGroup);
		Assertions.assertThat(groupTasks)
			.hasSize(1)
			.containsExactly(response.getTask());
	}
	
	@Test
	public void getTasksKeysByBusinessGroup() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gtask-dao-user-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gtask-dao-user-5");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gtaskdao-2", "gtaskdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		// Select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, null, node, taskFile, participant);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());
		
		//reload and check
		List<Long> groupTasksKeys = gtaTaskDao.getTasksKeys(businessGroup);
		Assertions.assertThat(groupTasksKeys)
			.hasSize(1)
			.containsExactly(response.getTask().getKey());
	}
}
