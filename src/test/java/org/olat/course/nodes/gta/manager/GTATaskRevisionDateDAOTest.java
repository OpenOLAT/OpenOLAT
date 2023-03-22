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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevisionDate;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTATaskRevisionDateDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTATaskRevisionDateDAO gtaTaskRevisionDateDao;
	
	@Test
	public void createTaskRevisionDate() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-20");
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		//create task
		Task task = gtaManager.createAndPersistTask(null, tasks, TaskProcess.assignment, null, participant, node);
		dbInstance.commitAndCloseSession();
		
		//create the revision log
		TaskRevisionDate taskRevision = gtaTaskRevisionDateDao.createAndPersistTaskRevisionDate(task, 2, TaskProcess.correction);
		Assert.assertNotNull(taskRevision);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taskRevision.getKey());
		Assert.assertNotNull(taskRevision.getDate());
		Assert.assertEquals(task, taskRevision.getTask());
		Assert.assertEquals(2, taskRevision.getRevisionLoop());
		Assert.assertEquals(TaskProcess.correction, taskRevision.getTaskStatus());
	}
	
	@Test
	public void deleteTaskRevisionDatebyTaskList() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-21");
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		//create task
		Task task = gtaManager.createAndPersistTask(null, tasks, TaskProcess.assignment, null, participant, node);
		dbInstance.commitAndCloseSession();
		
		//create the revision log
		TaskRevisionDate taskRevision = gtaTaskRevisionDateDao.createAndPersistTaskRevisionDate(task, 2, TaskProcess.correction);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taskRevision);
		
		int rowDelete = gtaTaskRevisionDateDao.deleteTaskRevisionDate(tasks);
		dbInstance.commit();
		Assert.assertEquals(1, rowDelete);
	}
	
	@Test
	public void deleteTaskRevisionDatebyTask() {
		//prepare
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-22");
		RepositoryEntry re = GTAManagerTest.deployGTACourse();
		GTACourseNode node = GTAManagerTest.getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		dbInstance.commit();
		
		//create task
		Task task = gtaManager.createAndPersistTask(null, tasks, TaskProcess.assignment, null, participant, node);
		dbInstance.commitAndCloseSession();
		
		//create the revision log
		TaskRevisionDate taskRevision = gtaTaskRevisionDateDao.createAndPersistTaskRevisionDate(task, 2, TaskProcess.correction);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taskRevision);
		
		int rowDelete = gtaTaskRevisionDateDao.deleteTaskRevisionDate(task);
		dbInstance.commit();
		Assert.assertEquals(1, rowDelete);
	}
}
