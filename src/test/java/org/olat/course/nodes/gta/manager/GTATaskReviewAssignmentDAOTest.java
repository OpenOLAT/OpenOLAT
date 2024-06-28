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
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTATaskReviewAssignmentDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTATaskReviewAssignmentDAO reviewAssignmentDao;
	
	private static RepositoryEntry COURSE_FOR_ALL_TESTS;
	
	@Before
	public void initCourse() {
		if(COURSE_FOR_ALL_TESTS == null) {
			COURSE_FOR_ALL_TESTS = GTAManagerTest.deployGTACourse();
		}
	}
	
	@Test
	public void createAssignment() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-1");
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-1");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		dbInstance.commit();

		//select
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, new File("bg1.txt"));
		dbInstance.commitAndCloseSession();
		
		Task task = response.getTask();
		TaskReviewAssignment assignment = reviewAssignmentDao.createAssignment(task, assignee);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(assignment);
		Assert.assertNotNull(assignment.getKey());
		Assert.assertNotNull(assignment.getCreationDate());
		Assert.assertNotNull(assignment.getLastModified());
		Assert.assertEquals(task, assignment.getTask());
		Assert.assertEquals(assignee, assignment.getAssignee());
	}
	
	@Test
	public void loadByKey() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-2");
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-2");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		// select a task
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, new File("bg2.txt"));
		TaskReviewAssignment assignment = reviewAssignmentDao.createAssignment(response.getTask(), assignee);
		dbInstance.commitAndCloseSession();
		
		TaskReviewAssignment reloadAssignment = reviewAssignmentDao.loadByKey(assignment.getKey());
		Assert.assertEquals(assignment, reloadAssignment);
	}
	
	@Test
	public void getAssignmentsByTask() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-3");
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-assignee-3");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, new File("bg3.txt"));
		Task task = response.getTask();
		TaskReviewAssignment assignment = reviewAssignmentDao.createAssignment(task, assignee);
		dbInstance.commitAndCloseSession();
		
		List<TaskReviewAssignment> loadedAssignments = reviewAssignmentDao.getAssignments(task);
		Assertions.assertThat(loadedAssignments)
			.hasSize(1)
			.containsExactly(assignment);
	}
	
	@Test
	public void getAssignmentsByTaskList() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-4");
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-4");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, new File("bg4.txt"));
		TaskReviewAssignment assignment = reviewAssignmentDao.createAssignment(response.getTask(), assignee);
		dbInstance.commitAndCloseSession();
		
		List<TaskReviewAssignment> loadedAssignments = reviewAssignmentDao.getAssignments(tasks);
		Assertions.assertThat(loadedAssignments)
			.contains(assignment);
	}
	
	@Test
	public void getAssignmentsByTaskListAndReviewer() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-5");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-6");
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-5");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		// select tasks
		AssignmentResponse response1 = gtaManager.selectTask(participant1, tasks, null, node, new File("bg5.txt"));
		AssignmentResponse response2 = gtaManager.selectTask(participant2, tasks, null, node, new File("bg6.txt"));
		TaskReviewAssignment assignment1 = reviewAssignmentDao.createAssignment(response1.getTask(), assignee);
		TaskReviewAssignment assignment2 = reviewAssignmentDao.createAssignment(response2.getTask(), assignee);
		dbInstance.commitAndCloseSession();
		
		List<TaskReviewAssignment> loadedAssignments = reviewAssignmentDao.getAssignments(tasks, assignee);
		Assertions.assertThat(loadedAssignments)
			.hasSize(2)
			.containsExactlyInAnyOrder(assignment1, assignment2);
	}
	
	@Test
	public void findAssignees() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-5");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-to-review-6");
		Identity assignee1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-7");
		Identity assignee2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-assignee-8");
		
		GTACourseNode node = GTAManagerTest.getGTACourseNode(COURSE_FOR_ALL_TESTS);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		TaskList tasks = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		// select tasks
		AssignmentResponse response1 = gtaManager.selectTask(participant1, tasks, null, node, new File("bg5.txt"));
		AssignmentResponse response2 = gtaManager.selectTask(participant2, tasks, null, node, new File("bg6.txt"));
		TaskReviewAssignment assignment1 = reviewAssignmentDao.createAssignment(response1.getTask(), assignee1);
		assignment1.setStatus(TaskReviewAssignmentStatus.inProgress);
		reviewAssignmentDao.updateAssignment(assignment1);
		TaskReviewAssignment assignment2 = reviewAssignmentDao.createAssignment(response2.getTask(), assignee2);
		assignment2.setStatus(TaskReviewAssignmentStatus.done);
		reviewAssignmentDao.updateAssignment(assignment2);
		dbInstance.commitAndCloseSession();
		
		// Find all assignees
		List<TaskReviewAssignmentStatus> status = List.of(TaskReviewAssignmentStatus.inProgress, TaskReviewAssignmentStatus.done);
		List<Identity> allAssignees = reviewAssignmentDao.findAssignees(tasks, status);
		Assertions.assertThat(allAssignees)
			.hasSize(2)
			.containsExactlyInAnyOrder(assignee1, assignee2);
		
		// Find in progress only
		List<TaskReviewAssignmentStatus> inProgressStatus = List.of(TaskReviewAssignmentStatus.inProgress);
		List<Identity> inProgressAssignees = reviewAssignmentDao.findAssignees(tasks, inProgressStatus);
		Assertions.assertThat(inProgressAssignees)
			.hasSize(1)
			.containsExactlyInAnyOrder(assignee1);
		
		// Find invalid, empty
		List<TaskReviewAssignmentStatus> invalidStatus = List.of(TaskReviewAssignmentStatus.invalidate);
		List<Identity> invalidAssignees = reviewAssignmentDao.findAssignees(tasks, invalidStatus);
		Assertions.assertThat(invalidAssignees)
			.isEmpty();
	}
}
