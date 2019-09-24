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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.AssignmentResponse.Status;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevisionDate;
import org.olat.course.nodes.gta.model.TaskListImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
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
	
	private static final Logger log = Tracing.createLoggerFor(GTAManagerTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManagerImpl gtaManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void createIfNotExists() {
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
	public void getTaskList_byTask() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-3");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-4");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
		
		//reload and check
		TaskList reloadedTasks = gtaManager.getTaskList(assignedTasks.get(0));
		dbInstance.commitAndCloseSession();// entry nneed to be fetched
		Assert.assertNotNull(reloadedTasks);
		Assert.assertEquals(tasks, reloadedTasks);
		Assert.assertTrue(reloadedTasks instanceof TaskListImpl);
		TaskListImpl tasksImpl = (TaskListImpl)reloadedTasks;
		Assert.assertNotNull(tasksImpl.getCreationDate());
		Assert.assertNotNull(tasksImpl.getLastModified());
		Assert.assertEquals(re, tasksImpl.getEntry());
		Assert.assertEquals(node.getIdent(), tasksImpl.getCourseNodeIdent());
		dbInstance.commit();
	}
	
	@Test
	public void isTaskAssigned() {
		//create an individual task
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-6");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
	public void isTaskInProcess() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-11");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());
		
		//check
		boolean inProcess = gtaManager.isTaskInProcess(re, node, taskFile.getName());
		Assert.assertTrue(inProcess);
		
		//check dummy file name which cannot be in process
		boolean notInProcess = gtaManager.isTaskInProcess(re, node, "qwertz");
		Assert.assertFalse(notInProcess);
	}
	
	@Test
	public void isTasksInProcess_yes() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-12");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getTask());

		//check
		boolean inProcess = gtaManager.isTasksInProcess(re, node);
		Assert.assertTrue(inProcess);
	}
	
	@Test
	public void isTasksInProcess_no() {
		//prepare
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//check
		boolean inProcess = gtaManager.isTasksInProcess(re, node);
		Assert.assertFalse(inProcess);
	}
	
	@Test
	public void getAssignedTasks() {
		//create an individual task
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-8");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
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
	public void updateTaskName() {
		//create an individual task
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-8");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList taskList = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		Assert.assertNotNull(taskList);
		
		//select
		gtaManager.selectTask(id1, taskList, node, new File("work_1.txt"));
		gtaManager.selectTask(id2, taskList, node, new File("work_2.txt"));
		dbInstance.commit();
		
		//change a name
		int rowUpdated = gtaManager.updateTaskName(taskList, "work_1.txt", "changed_work.txt");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, rowUpdated);
		
		//check
		Task assignedTaskToId1 = gtaManager.getTask(id1, taskList);
		Assert.assertNotNull(assignedTaskToId1);
		Assert.assertEquals("changed_work.txt", assignedTaskToId1.getTaskName());

		List<Task> assignedTaskToId2 = gtaManager.getTasks(id2, re, node);
		Assert.assertNotNull(assignedTaskToId2);
		Assert.assertEquals(1, assignedTaskToId2.size());
		Assert.assertEquals("work_2.txt", assignedTaskToId2.get(0).getTaskName());	
	}
	
	@Test
	public void updateTaskName_paranoia() {
		//create an individual task
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-8");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList taskList = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		Assert.assertNotNull(taskList);
		
		//create a reference individual task
		GTACourseNode nodeRef = new GTACourseNode();
		nodeRef.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList taskListRef = gtaManager.createIfNotExists(re, nodeRef);
		dbInstance.commit();
		Assert.assertNotNull(taskListRef);
		
		//select
		gtaManager.selectTask(id1, taskList, node, new File("work_1.txt"));
		gtaManager.selectTask(id1, taskListRef, nodeRef, new File("work_1.txt"));
		gtaManager.selectTask(id2, taskList, node, new File("work_2.txt"));
		dbInstance.commit();
		
		//change a name
		int rowUpdated = gtaManager.updateTaskName(taskList, "work_1.txt", "changed_work.txt");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, rowUpdated);
		
		//check
		Task assignedTaskToId1 = gtaManager.getTask(id1, taskList);
		Assert.assertNotNull(assignedTaskToId1);
		Assert.assertEquals("changed_work.txt", assignedTaskToId1.getTaskName());

		List<Task> assignedTaskToId2 = gtaManager.getTasks(id2, re, node);
		Assert.assertNotNull(assignedTaskToId2);
		Assert.assertEquals(1, assignedTaskToId2.size());
		Assert.assertEquals("work_2.txt", assignedTaskToId2.get(0).getTaskName());	
		
		Task assignedTaskRefToId1 = gtaManager.getTask(id1, taskListRef);
		Assert.assertNotNull(assignedTaskRefToId1);
		Assert.assertEquals("work_1.txt", assignedTaskRefToId1.getTaskName());
	}
	
	@Test
	public void getEnrollmentDate() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-20");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		dbInstance.commit();
		
		//no participant enrolled
		Date noEnrollmentDate = gtaManager.getEnrollmentDate(businessGroup);
		Assert.assertNull(noEnrollmentDate);
		
		//add participant
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-21");
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		
		// there is a participant enrolled
		Date enrollmentDate = gtaManager.getEnrollmentDate(businessGroup);
		Assert.assertNotNull(enrollmentDate);
	}
	
	@Test
	public void deleteTaskList() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-9");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-10");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		
		//check that there is tasks
		List<Task> assignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(assignedTasks);
		Assert.assertEquals(1, assignedTasks.size());
		
		//delete
		int numOfDeletedObjects = gtaManager.deleteTaskList(re, node);
		Assert.assertEquals(2, numOfDeletedObjects);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any tasks
		List<Task> deletedAssignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(deletedAssignedTasks);
		Assert.assertEquals(0, deletedAssignedTasks.size());
	}
	
	@Test
	public void deleteTaskList_withRevisionDates() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-9");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-10");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(businessGroup, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		
		//check that there is tasks
		List<Task> assignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(assignedTasks);
		Assert.assertEquals(1, assignedTasks.size());
		
		// create a revision date
		gtaManager.createAndPersistTaskRevisionDate(assignedTasks.get(0), 2, TaskProcess.correction);
		dbInstance.commitAndCloseSession();
		
		//delete
		int numOfDeletedObjects = gtaManager.deleteTaskList(re, node);
		Assert.assertEquals(3, numOfDeletedObjects);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any tasks
		List<Task> deletedAssignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(deletedAssignedTasks);
		Assert.assertEquals(0, deletedAssignedTasks.size());
	}
	
	/**
	 * Create 2 pseudo nodes in a course, and delete the task of the first node
	 * and check that the task of second are always there.
	 * 
	 */
	@Test
	public void deleteTaskList_parano() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-9");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-10");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRole.participant.name());
		dbInstance.commit();
		RepositoryEntry re = deployGTACourse();
		
		//node 1
		GTACourseNode node1 = getGTACourseNode(re);
		node1.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks1 = gtaManager.createIfNotExists(re, node1);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks1);
		dbInstance.commit();
		
		//node 2
		GTACourseNode node2 = new GTACourseNode();
		node2.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		TaskList tasks2 = gtaManager.createIfNotExists(re, node2);
		Assert.assertNotNull(tasks2);
		dbInstance.commit();
		
		//select node 1
		AssignmentResponse response1 = gtaManager.selectTask(businessGroup, tasks1, node1, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response1);
		
		//select node 2
		AssignmentResponse response2 = gtaManager.selectTask(businessGroup, tasks2, node2, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response2);
		
		//check that there is tasks
		List<Task> assignedTasks1 = gtaManager.getTasks(participant, re, node1);
		Assert.assertNotNull(assignedTasks1);
		Assert.assertEquals(1, assignedTasks1.size());
		
		// create a revision date
		gtaManager.createAndPersistTaskRevisionDate(assignedTasks1.get(0), 2, TaskProcess.correction);
		
		List<Task> assignedTasks2 = gtaManager.getTasks(participant, re, node2);
		Assert.assertNotNull(assignedTasks2);
		Assert.assertEquals(1, assignedTasks2.size());
		
		// create a revision date
		gtaManager.createAndPersistTaskRevisionDate(assignedTasks2.get(0), 2, TaskProcess.correction);
		dbInstance.commitAndCloseSession();
		
		//delete
		int numOfDeletedObjects = gtaManager.deleteTaskList(re, node1);
		Assert.assertEquals(3, numOfDeletedObjects);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any tasks in node 1
		List<Task> deletedAssignedTasks = gtaManager.getTasks(participant, re, node1);
		Assert.assertNotNull(deletedAssignedTasks);
		Assert.assertEquals(0, deletedAssignedTasks.size());
		
		List<TaskRevisionDate> revisionsTask1 = gtaManager.getTaskRevisions(assignedTasks1.get(0));
		Assert.assertNotNull(revisionsTask1);
		Assert.assertEquals(0, revisionsTask1.size());
		
		//but always in node 2
		List<Task> notDeletedAssignedTasks2 = gtaManager.getTasks(participant, re, node2);
		Assert.assertNotNull(notDeletedAssignedTasks2);
		Assert.assertEquals(1, notDeletedAssignedTasks2.size());
		
		List<TaskRevisionDate> revisionsTask2 = gtaManager.getTaskRevisions(notDeletedAssignedTasks2.get(0));
		Assert.assertNotNull(revisionsTask2);
		Assert.assertEquals(1, revisionsTask2.size());
	}
	

	
	@Test
	public void deleteAllTaskLists() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-9");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-10");
		dbInstance.commit();
		RepositoryEntry re = deployGTACourse();
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, re, GroupRoles.participant.name());

		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response);
		
		//check that there is tasks
		List<Task> assignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(assignedTasks);
		Assert.assertEquals(1, assignedTasks.size());
		
		//delete
		int numOfDeletedObjects = gtaManager.deleteAllTaskLists(re);
		Assert.assertEquals(2, numOfDeletedObjects);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any tasks
		List<Task> deletedAssignedTasks = gtaManager.getTasks(participant, re, node);
		Assert.assertNotNull(deletedAssignedTasks);
		Assert.assertEquals(0, deletedAssignedTasks.size());
	}
	
	/**
	 * Create 2 pseudo courses in a course, and delete the task of the first course
	 * and check that the task of second are always there.
	 * 
	 */
	@Test
	public void deleteAllTaskLists_parano() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-20");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-21");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-22");
		
		RepositoryEntry re1 = deployGTACourse();
		repositoryEntryRelationDao.addRole(coach, re1, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, re1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re1, GroupRoles.participant.name());
		
		//course 1
		GTACourseNode node1 = getGTACourseNode(re1);
		node1.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks1 = gtaManager.createIfNotExists(re1, node1);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks1);
		dbInstance.commit();

		RepositoryEntry re2 = deployGTACourse();
		repositoryEntryRelationDao.addRole(coach, re2, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, re2, GroupRoles.participant.name());

		//participant 2 course 2
		GTACourseNode node2 = getGTACourseNode(re2);
		node2.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks2 = gtaManager.createIfNotExists(re2, node2);
		Assert.assertNotNull(tasks2);
		dbInstance.commit();
		
		//participant 1 and 2 select course 1
		AssignmentResponse response1_1 = gtaManager.selectTask(participant1, tasks1, node1, taskFile);
		AssignmentResponse response1_2 = gtaManager.selectTask(participant2, tasks1, node1, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response1_1);
		Assert.assertNotNull(response1_2);

		//participant 2 select node 2
		AssignmentResponse response2_2 = gtaManager.selectTask(participant2, tasks2, node2, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(response2_2);
		
		//check that there is tasks
		List<Task> assignedTasks1_1 = gtaManager.getTasks(participant1, re1, node1);
		Assert.assertNotNull(assignedTasks1_1);
		Assert.assertEquals(1, assignedTasks1_1.size());
		
		List<Task> assignedTasks1_2 = gtaManager.getTasks(participant2, re1, node1);
		Assert.assertNotNull(assignedTasks1_2);
		Assert.assertEquals(1, assignedTasks1_2.size());
		
		List<Task> assignedTasks2_2 = gtaManager.getTasks(participant2, re2, node2);
		Assert.assertNotNull(assignedTasks2_2);
		Assert.assertEquals(1, assignedTasks2_2.size());
		
		//delete
		int numOfDeletedObjects = gtaManager.deleteAllTaskLists(re1);
		Assert.assertEquals(3, numOfDeletedObjects);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any tasks in node 1
		List<Task> deletedAssignedTasks1_1 = gtaManager.getTasks(participant1, re1, node1);
		Assert.assertNotNull(deletedAssignedTasks1_1);
		Assert.assertEquals(0, deletedAssignedTasks1_1.size());
		
		List<Task> deletedAssignedTasks1_2 = gtaManager.getTasks(participant2, re1, node1);
		Assert.assertNotNull(deletedAssignedTasks1_2);
		Assert.assertEquals(0, deletedAssignedTasks1_2.size());
		
		//but always in node 2
		List<Task> notDeletedAssignedTasks2_2 = gtaManager.getTasks(participant2, re2, node2);
		Assert.assertNotNull(notDeletedAssignedTasks2_2);
		Assert.assertEquals(1, notDeletedAssignedTasks2_2.size());
	}
	
	@Test
	public void createTaskRevisionDate() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-20");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		//create task
		Task task = gtaManager.createAndPersistTask(null, tasks, TaskProcess.assignment, null, participant, node);
		dbInstance.commitAndCloseSession();
		
		//create the revision log
		TaskRevisionDate taskRevision = gtaManager.createAndPersistTaskRevisionDate(task, 2, TaskProcess.correction);
		Assert.assertNotNull(taskRevision);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taskRevision.getKey());
		Assert.assertNotNull(taskRevision.getDate());
		Assert.assertEquals(task, taskRevision.getTask());
		Assert.assertEquals(2, taskRevision.getRevisionLoop());
		Assert.assertEquals(TaskProcess.correction, taskRevision.getTaskStatus());
	}
	
	@Test
	public void getTaskRevisions() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-21");
		RepositoryEntry re = deployGTACourse();
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		//create a task
		Task task = gtaManager.createAndPersistTask(null, tasks, TaskProcess.assignment, null, participant, node);
		dbInstance.commitAndCloseSession();
		
		//add the revision log
		TaskRevisionDate taskRevision = gtaManager.createAndPersistTaskRevisionDate(task, 2, TaskProcess.correction);
		Assert.assertNotNull(taskRevision);
		dbInstance.commitAndCloseSession();
		
		//load the revisions
		List<TaskRevisionDate> taskRevisions = gtaManager.getTaskRevisions(task);
		Assert.assertNotNull(taskRevisions);
		Assert.assertEquals(1, taskRevisions.size());
		TaskRevisionDate loadedTaskRevision = taskRevisions.get(0);
		Assert.assertNotNull(loadedTaskRevision.getKey());
		Assert.assertNotNull(loadedTaskRevision.getDate());
		Assert.assertEquals(task, loadedTaskRevision.getTask());
		Assert.assertEquals(2, loadedTaskRevision.getRevisionLoop());
		Assert.assertEquals(TaskProcess.correction, loadedTaskRevision.getTaskStatus());
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
	public void roundRobin_oneRound() {
		String[] slots = new String[]{ "A", "B", "C" };
		List<String> usedSlots = new ArrayList<>();
		usedSlots.add("A");
		usedSlots.add("B");
		usedSlots.add("C");
		usedSlots.add("A");

		String nextSlot = gtaManager.nextSlotRoundRobin(slots, usedSlots);
		Assert.assertEquals("B", nextSlot);
	}
	
	@Test
	public void roundRobin_randomRound() {
		String[] slots = new String[]{ "A", "B", "C" };
		List<String> usedSlots = new ArrayList<>();
		usedSlots.add("A");
		usedSlots.add("B");
		usedSlots.add("B");
		usedSlots.add("B");
		usedSlots.add("C");
		usedSlots.add("C");
		usedSlots.add("A");

		String nextSlot = gtaManager.nextSlotRoundRobin(slots, usedSlots);
		Assert.assertEquals("A", nextSlot);
	}
	
	@Test
	public void roundRobin_randomRoundAlt() {
		String[] slots = new String[]{ "A", "B", "C" };
		List<String> usedSlots = new ArrayList<>();
		usedSlots.add("A");
		usedSlots.add("B");
		usedSlots.add("B");
		usedSlots.add("B");
		usedSlots.add("C");
		usedSlots.add("C");
		usedSlots.add("A");
		usedSlots.add("A");

		String nextSlot = gtaManager.nextSlotRoundRobin(slots, usedSlots);
		Assert.assertEquals("C", nextSlot);
	}
	
	private RepositoryEntry deployGTACourse() {
		try {
			Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-author");
			String displayname = "GTA-" + UUID.randomUUID();
			
			URL courseUrl = JunitTestHelper.class.getResource("file_resources/GTA_course.zip");
			File courseFile = new File(courseUrl.toURI());
			return JunitTestHelper.deployCourse(initialAuthor, displayname, courseFile);
		} catch (URISyntaxException e) {
			log.error("", e);
			return null;
		}
	}
	
	private GTACourseNode getGTACourseNode(RepositoryEntry courseEntry) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		for(int i=rootNode.getChildCount(); i-->0; ) {
			INode child = rootNode.getChildAt(i);
			if(child instanceof GTACourseNode) {
				return ((GTACourseNode)child);
			}
		}
		
		return null;
	}
}
