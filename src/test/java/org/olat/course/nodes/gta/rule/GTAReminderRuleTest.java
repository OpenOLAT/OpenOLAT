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
package org.olat.course.nodes.gta.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.manager.GTAManagerImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAReminderRuleTest extends OlatTestCase {
	
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
	

	@Autowired
	private AssignTaskRuleSPI assignTaskRuleSPI;
	@Autowired
	private SubmissionTaskRuleSPI submissionTaskRuleSPI;
	
	@Test
	public void assignTask_individual() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-2");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		repositoryEntryRelationDao.addRole(participant1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		Calendar cal = Calendar.getInstance();
		cal.add(2, Calendar.MONTH);
		node.getModuleConfiguration().setDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE, cal.getTime());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select a task
		AssignmentResponse response = gtaManager.selectTask(participant1, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		//only remind participant 2
		List<Identity> toRemind = assignTaskRuleSPI.getPeopleToRemind(re, node);
		Assert.assertEquals(1, toRemind.size());
		Assert.assertTrue(toRemind.contains(participant2));
		

		{ // check before 30 days 
			ReminderRuleImpl rule = getAssignedTaskRules(30, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 5 weeks 
			ReminderRuleImpl rule = getAssignedTaskRules(5, LaunchUnit.week);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 1 month 
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.month);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before  90 days 
			ReminderRuleImpl rule = getAssignedTaskRules(90, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before  12 weeks 
			ReminderRuleImpl rule = getAssignedTaskRules(12, LaunchUnit.week);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before  3 month 
			ReminderRuleImpl rule = getAssignedTaskRules(3, LaunchUnit.month);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before 1 year 
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.year);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
	}
	
	private ReminderRuleImpl getAssignedTaskRules(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(AssignTaskRuleSPI.class.getSimpleName());
		rule.setOperator("<");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());
		return rule;
	}
	
	@Test
	public void assignTask_businessGroup() {
		//prepare
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-2");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-3");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-4");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-5");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-6");
		
		BusinessGroup businessGroup1 = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		BusinessGroup businessGroup2 = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup1, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant3, businessGroup2, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant4, businessGroup2, GroupRole.participant.name());
		dbInstance.commit();
		
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		businessGroupRelationDao.addRelationToResource(businessGroup1, re);
		businessGroupRelationDao.addRelationToResource(businessGroup2, re);
		
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
		List<Long> groupKeys = new ArrayList<>(2);
		groupKeys.add(businessGroup1.getKey());
		groupKeys.add(businessGroup2.getKey());
		node.getModuleConfiguration().setList(GTACourseNode.GTASK_GROUPS, groupKeys);
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("bg.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		// group 1 select a task
		AssignmentResponse response = gtaManager.selectTask(businessGroup1, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		// only remind group 2
		List<Identity> toRemind = assignTaskRuleSPI.getPeopleToRemind(re, node);
		Assert.assertEquals(2, toRemind.size());
		Assert.assertTrue(toRemind.contains(participant3));
		Assert.assertTrue(toRemind.contains(participant4));
	}
	
	@Test
	public void submitTask_individual() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-2");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		repositoryEntryRelationDao.addRole(participant1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		Calendar cal = Calendar.getInstance();
		cal.add(2, Calendar.MONTH);
		node.getModuleConfiguration().setDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE, cal.getTime());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select a task
		AssignmentResponse response = gtaManager.selectTask(participant1, tasks, node, taskFile);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		//only remind participant 2
		List<Identity> toRemind = submissionTaskRuleSPI.getPeopleToRemind(re, node);
		Assert.assertEquals(1, toRemind.size());
		Assert.assertTrue(toRemind.contains(participant2));
		

		{ // check before 30 days 
			ReminderRuleImpl rule = getSubmitTaskRules(30, LaunchUnit.day);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 5 weeks 
			ReminderRuleImpl rule = getSubmitTaskRules(5, LaunchUnit.week);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 1 month 
			ReminderRuleImpl rule = getSubmitTaskRules(1, LaunchUnit.month);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before  90 days 
			ReminderRuleImpl rule = getSubmitTaskRules(90, LaunchUnit.day);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before  12 weeks 
			ReminderRuleImpl rule = getSubmitTaskRules(12, LaunchUnit.week);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before  3 month 
			ReminderRuleImpl rule = getSubmitTaskRules(3, LaunchUnit.month);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);

			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
		
		{ // check before 1 year 
			ReminderRuleImpl rule = getSubmitTaskRules(1, LaunchUnit.year);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(toRemind.contains(participant2));
		}
	}
	
	private ReminderRuleImpl getSubmitTaskRules(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(SubmissionTaskRuleSPI.class.getSimpleName());
		rule.setOperator("<");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());
		return rule;
	}
}
