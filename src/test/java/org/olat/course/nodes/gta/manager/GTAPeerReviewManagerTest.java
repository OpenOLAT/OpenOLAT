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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	@Autowired
	private RepositoryService repositoryService;
	
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
		AssignmentResponse response = gtaManager.selectTask(participant, tasks, null, node, new File("bg1.txt"));
		dbInstance.commitAndCloseSession();
		
		Task task = response.getTask();
		TaskReviewAssignment assignment = peerReviewManager.createAssignment(task, assignee);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(assignment);
		Assert.assertNotNull(assignment.getKey());
		Assert.assertEquals(task, assignment.getTask());
		Assert.assertEquals(assignee, assignment.getAssignee());
	}
	
	@Test
	public void assignmentMutualSameTask() {
		RepositoryEntry course = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(course);
		TaskList taskList = gtaManager.createIfNotExists(COURSE_FOR_ALL_TESTS, node);
		
		List<Identity> participants = new ArrayList<>();
		Map<Identity,Task> participantsMap = new HashMap<>();
		for(int i=1; i<=9; i++) {
			Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-review-" +i);
			repositoryService.addRole(participant, course, GroupRoles.participant.name());
			AssignmentResponse response = gtaManager.selectTask(participant, taskList, null, node, new File("bg" + (i % 3) + ".txt"));
			participants.add(participant);
			if(response != null && response.getTask() != null) {
				participantsMap.put(participant, response.getTask());
				gtaManager.submitTask(response.getTask(), node, 1, participant, Role.user);
			}
		}
		
		dbInstance.commitAndCloseSession();
		waitMessageAreConsumed();

		node.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, true);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS, "2");
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT, GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_SAME_TASK);

		peerReviewManager.assign(course, taskList, node);
		
		for(Identity participant:participants) {
			List<TaskReviewAssignment> reviews = peerReviewManager.getAssignmentsOfReviewer(taskList, participant);
			Assert.assertEquals(2, reviews.size());
		}
	}
	
	@Test
	public void assignmentOtherTask() {
		RepositoryEntry course = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(course);
		TaskList taskList = gtaManager.createIfNotExists(course, node);
		
		List<Identity> participants = new ArrayList<>();
		Map<Identity,Task> participantsMap = new HashMap<>();
		for(int i=1; i<=10; i++) {
			Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-review-" +i);
			repositoryService.addRole(participant, course, GroupRoles.participant.name());
			AssignmentResponse response = gtaManager.selectTask(participant, taskList, null, node, new File("bg" + (i % 3) + ".txt"));
			participants.add(participant);
			if(response != null && response.getTask() != null) {
				participantsMap.put(participant, response.getTask());
				gtaManager.submitTask(response.getTask(), node, 1, participant, Role.user);
			}
		}
		
		dbInstance.commitAndCloseSession();
		waitMessageAreConsumed();

		node.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW, false);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS, "2");
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT, GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_OTHER_TASK);

		peerReviewManager.assign(course, taskList, node);
		
		for(Identity participant:participants) {
			List<TaskReviewAssignment> reviews = peerReviewManager.getAssignmentsOfReviewer(taskList, participant);
			
			if(reviews.size() == 2) {
				Task task1 = participantsMap.get(reviews.get(0).getAssignee());
				Task task2 = participantsMap.get(reviews.get(1).getAssignee());
				Assert.assertNotNull(task1);
				Assert.assertNotNull(task2);
				Assert.assertEquals(task1.getTaskName(), task2.getTaskName());
			}
		}
	}
}
