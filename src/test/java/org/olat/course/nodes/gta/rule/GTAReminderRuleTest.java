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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.model.GroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTARelativeToDates;
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
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
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
	
	private static final Logger log = Tracing.createLoggerFor(GTAReminderRuleTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManagerImpl gtaManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifeCycleDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	

	@Autowired
	private AssignTaskRuleSPI assignTaskRuleSPI;
	@Autowired
	private SubmissionTaskRuleSPI submissionTaskRuleSPI;
	
	@Test
	public void assignTask_individual() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-2");
		RepositoryEntry re = deployGTACourse();
		repositoryEntryRelationDao.addRole(participant1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		Calendar cal = Calendar.getInstance();
		cal.add(2, Calendar.MONTH);
		node.getModuleConfiguration().setDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE, cal.getTime());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select a task
		AssignmentResponse response = gtaManager.selectTask(participant1, tasks, null, node, taskFile);
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
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-3");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-4");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-5");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-6");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-7");
		
		RepositoryEntry re = deployGTACourse();
		
		BusinessGroup businessGroup1 = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		BusinessGroup businessGroup2 = businessGroupDao.createAndPersist(coach, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup1, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant3, businessGroup2, GroupRole.participant.name());
		businessGroupRelationDao.addRole(participant4, businessGroup2, GroupRole.participant.name());
		dbInstance.commit();
		
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
		AssignmentResponse response = gtaManager.selectTask(businessGroup1, tasks, null, node, taskFile, participant2);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(AssignmentResponse.Status.ok, response.getStatus());
		
		// only remind group 2
		List<Identity> toRemind = assignTaskRuleSPI.getPeopleToRemind(re, node);
		Assert.assertEquals(2, toRemind.size());
		Assert.assertTrue(toRemind.contains(participant3));
		Assert.assertTrue(toRemind.contains(participant4));
	}
	
	@Test
	public void assignTask_relativeToDateEnrollment() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-8");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-9");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		addEnrollmentDate(re, participant1, GroupRoles.participant, -12, Calendar.DATE);
		addEnrollmentDate(re, participant2, GroupRoles.participant, -5, Calendar.DATE);
		dbInstance.commit();
		
		// create a fake node
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		node.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, true);
		node.getModuleConfiguration().setIntValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, 15);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO, GTARelativeToDates.enrollment.name());
		
		// need the task list
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		// participant 1 has still 3 days to choose a task
		// participant 2 has still 10 days to choose a task
		
		{ // check before 1 day
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}

		{ // check before 5 days
			ReminderRuleImpl rule = getAssignedTaskRules(5, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(participant1));
		}
		
		{ // check before 1 week
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.week);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(participant1));
		}
		
		{ // check before 1 month
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.month);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
	}
	
	private void addEnrollmentDate(RepositoryEntry entry, Identity id, GroupRoles role, int amount, int field) {
		Group group = repositoryEntryRelationDao.getDefaultGroup(entry);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(field, amount);
		
		GroupMembershipImpl membership = new GroupMembershipImpl();
		membership.setCreationDate(cal.getTime());
		membership.setLastModified(cal.getTime());
		membership.setGroup(group);
		membership.setIdentity(id);
		membership.setRole(role.name());
		membership.setInheritanceMode(GroupMembershipInheritance.none);
		dbInstance.getCurrentEntityManager().persist(membership);
		dbInstance.commit();
	}
	
	@Test
	public void assignTask_relativeToInitialLaunchDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-3");

		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(null);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//create user course infos
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id1);
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id2);
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id3);
		dbInstance.commit();
		
		//fake the date
		updateInitialLaunchDate(re.getOlatResource(), id1, -5, Calendar.DATE);
		updateInitialLaunchDate(re.getOlatResource(), id2, -35, Calendar.DATE);
		updateInitialLaunchDate(re.getOlatResource(), id3, -75, Calendar.DATE);
		dbInstance.commitAndCloseSession();
		
		// create a fake node
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		node.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, true);
		node.getModuleConfiguration().setIntValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, 40);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO, GTARelativeToDates.courseLaunch.name());
		
		// need the task list
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(tasks);
		dbInstance.commit();		

		{ // check 3 days
			ReminderRuleImpl rule = getAssignedTaskRules(3, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check 5 days
			ReminderRuleImpl rule = getAssignedTaskRules(5, LaunchUnit.day);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check 1 week
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.week);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check 1 month
			ReminderRuleImpl rule = getAssignedTaskRules(1, LaunchUnit.month);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check 2 month
			ReminderRuleImpl rule = getAssignedTaskRules(2, LaunchUnit.month);
			List<Identity> all = assignTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(3, all.size());
			Assert.assertTrue(all.contains(id1));
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

	}
	
	private void updateInitialLaunchDate(OLATResource courseRes, Identity id, int amount, int field) {
		UserCourseInfosImpl userCourseInfos = (UserCourseInfosImpl)userCourseInformationsManager.getUserCourseInformations(courseRes, id);
		Date initialLaunch = userCourseInfos.getInitialLaunch();
		Calendar cal = Calendar.getInstance();
		cal.setTime(initialLaunch);
		cal.add(field, amount);
		userCourseInfos.setInitialLaunch(cal.getTime());
		dbInstance.getCurrentEntityManager().merge(userCourseInfos);
		dbInstance.commit();
	}
	
	@Test
	public void submitTask_individual() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-10");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-11");
		RepositoryEntry re = deployGTACourse();
		repositoryEntryRelationDao.addRole(participant1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		GTACourseNode node = getGTACourseNode(re);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		
		Calendar cal = Calendar.getInstance();
		cal.add(2, Calendar.MONTH);
		node.getModuleConfiguration().setDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE, cal.getTime());
		TaskList tasks = gtaManager.createIfNotExists(re, node);
		File taskFile = new File("solo.txt");
		Assert.assertNotNull(tasks);
		dbInstance.commit();
		
		//select a task
		AssignmentResponse response = gtaManager.selectTask(participant1, tasks, null, node, taskFile);
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
	
	@Test
	public void submitTask_relativeLifecycle() {
		//prepare a course with a volatile task
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-12");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-user-13");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		repositoryEntryRelationDao.addRole(participant1, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		String label = "Life cycle for relative date";
		String softKey = UUID.randomUUID().toString();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -5);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, 20);
		Date to = cal.getTime();
		RepositoryEntryLifecycle lifecycle = reLifeCycleDao.create(label, softKey, true, from, to);
		re.setLifecycle(lifecycle);
		re = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commit();
		
		//create a fake node with a relative submit deadline 15 days after the start of the course
		GTACourseNode node = new GTACourseNode();
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		node.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, true);
		node.getModuleConfiguration().setIntValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, 15);
		node.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO, GTARelativeToDates.courseStart.name());

		TaskList tasks = gtaManager.createIfNotExists(re, node);
		Assert.assertNotNull(tasks);
		dbInstance.commitAndCloseSession();
		
		//the course has start 5 days before, deadline is 15 days after it
		//conclusion the deadline is 10 days from now
		
		{ // check before 5 days 
			ReminderRuleImpl rule = getSubmitTaskRules(5, LaunchUnit.day);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 1 week 
			ReminderRuleImpl rule = getSubmitTaskRules(1, LaunchUnit.week);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(0, all.size());
		}
		
		{ // check before 10 days 
			ReminderRuleImpl rule = getSubmitTaskRules(10, LaunchUnit.day);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
		
		{ // check before 2 days 
			ReminderRuleImpl rule = getSubmitTaskRules(10, LaunchUnit.week);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
		
		{ // check before 30 days 
			ReminderRuleImpl rule = getSubmitTaskRules(30, LaunchUnit.day);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
		
		{ // check before 1 months 
			ReminderRuleImpl rule = getSubmitTaskRules(1, LaunchUnit.month);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
		
		{ // check before 5 months 
			ReminderRuleImpl rule = getSubmitTaskRules(5, LaunchUnit.month);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
		}
		
		{ // check before 1 year 
			ReminderRuleImpl rule = getSubmitTaskRules(1, LaunchUnit.year);
			List<Identity> all = submissionTaskRuleSPI.evaluateRule(re, node, rule);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant1));
			Assert.assertTrue(all.contains(participant2));
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
	
	private RepositoryEntry deployGTACourse() {
		try {
			Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("gta-reminder");
			String displayname = "GTARemind-" + UUID.randomUUID();
			
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
