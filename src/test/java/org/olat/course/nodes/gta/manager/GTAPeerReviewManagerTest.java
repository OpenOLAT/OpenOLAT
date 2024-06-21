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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.olat.repository.RepositoryEntry;
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

}
