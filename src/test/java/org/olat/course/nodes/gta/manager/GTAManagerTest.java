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
package org.olat.course.nodes.gta.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.AssignmentResponse.Status;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskListImpl;
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
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManagerImpl gtaManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void createIfNotExists() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		Assert.assertNotNull(node.getIdent());
		
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//reload and check
		TaskList reloadedTasks = gtaManager.getTaskList(re, node);
		Assert.assertNotNull(reloadedTasks);
		Assert.assertEquals(tasks, reloadedTasks);
		Assert.assertTrue(reloadedTasks instanceof TaskListImpl);
		TaskListImpl tasksImpl = (TaskListImpl)reloadedTasks;
		Assert.assertNotNull(tasksImpl.getCreationDate());
		Assert.assertNotNull(tasksImpl.getLastModified());
		Assert.assertEquals(re, tasksImpl.getEntry());
		Assert.assertEquals(node.getIdent(), tasksImpl.getCourseNodeIdent());
		dbInstance.commit();
		
		//check that a second call doesn't create a new task list
		TaskList secondTasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(secondTasks);
		dbInstance.commit();
		Assert.assertEquals(tasks, secondTasks);
	}
	
	@Test
	public void selectTask_identity() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-1");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		Task task = response.getTask();
		Assert.assertNotNull(task.getKey());
		Assert.assertNull(task.getBusinessGroup());
		Assert.assertNotNull(task.getCreationDate());
		Assert.assertNotNull(task.getLastModified());
		Assert.assertEquals(tasks, task.getTaskList());
		Assert.assertEquals("solo.txt", task.getTaskName());
		Assert.assertEquals(participant, task.getIdentity());
	}
	
	@Test
	public void selectTask_businessGroup() {
		//prepare
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-2");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		dbInstance.commit();
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		Task task = response.getTask();
		Assert.assertNotNull(task.getKey());
		Assert.assertNull(task.getIdentity());
		Assert.assertNotNull(task.getCreationDate());
		Assert.assertNotNull(task.getLastModified());
		Assert.assertEquals(tasks, task.getTaskList());
		Assert.assertEquals("bg.txt", task.getTaskName());
		Assert.assertEquals(businessGroup, task.getBusinessGroup());
	}
	
	@Test
	public void getTasks() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-3");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-4");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		
		List<Task> assignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(assignedTasks);
		Assert.assertEquals(1, assignedTasks.size());
	}
	
	@Test
	public void isTaskAssigned() {
		//create an individual task
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-6");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		Assert.assertNotNull(tasks);
		
		//select
		File taskFile = new File("bg.txt");
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, node, taskFile);
		Assert.assertNotNull(response);
		Assert.assertEquals(Status.ok, response.getStatus());
		
		//check is assigned
		boolean assigned = gtaManager.isTaskAssigned(tasks, taskFile.getName());
		Assert.assertTrue(assigned);
		boolean notAssigned = gtaManager.isTaskAssigned(tasks, "noise.txt");
		Assert.assertFalse(notAssigned);
	}
	
	@Test
	public void getAssignedTasks() {
		//create an individual task
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-8");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList taskList = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		Assert.assertNotNull(taskList);
		
		//select
		gtaManager.selectTask(id1, taskList, node, new File("work_1.txt"));
		gtaManager.selectTask(id2, taskList, node, new File("work_2.txt"));
		
		//get assigned tasks
		List<String> assigned = gtaManager.getAssignedTasks(taskList);
		Assert.assertNotNull(assigned);
		Assert.assertEquals(2, assigned.size());
		Assert.assertTrue(assigned.contains("work_1.txt"));
		Assert.assertTrue(assigned.contains("work_2.txt"));
	}
	
	@Test
	public void roundRobin() {
		String[] slots = new String[]{ "A", "B", "C" };
		List<String> usedSlots = new ArrayList<>();
		usedSlots.add("A");
		usedSlots.add("B");

		String nextSlot = gtaManager.nextSlotRoundRobin(slots, usedSlots);
		Assert.assertEquals("C", nextSlot);
	}
	
	@Test
	public void roundsRobin() {
		String[] slots = new String[]{ "A", "B", "C" };
		List<String> usedSlots = new ArrayList<>();
		usedSlots.add("A");
		usedSlots.add("B");
		usedSlots.add("C");
		usedSlots.add("A");

		String nextSlot = gtaManager.nextSlotRoundRobin(slots, usedSlots);
		Assert.assertEquals("A", nextSlot);
	}
}
