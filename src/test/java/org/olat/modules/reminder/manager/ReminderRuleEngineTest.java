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
package org.olat.modules.reminder.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.model.GroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.rule.AttemptsRuleSPI;
import org.olat.course.reminder.rule.InitialAttemptsRuleSPI;
import org.olat.course.reminder.rule.PassedRuleSPI;
import org.olat.course.reminder.rule.ScoreRuleSPI;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.Role;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.BeforeDateRuleSPI;
import org.olat.modules.reminder.rule.CourseEnrollmentDateRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.modules.reminder.rule.InitialCourseLaunchRuleSPI;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.modules.reminder.rule.RecentCourseLaunchRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryLifecycleAfterValidFromRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryLifecycleAfterValidToRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryRoleRuleSPI;
import org.olat.modules.reminder.rule.UserPropertyRuleSPI;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRuleEngineTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ReminderRuleEngine ruleEngine;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@Test
	public void dateRule() {
		Calendar cal = Calendar.getInstance();
		
		//check rule with date in the past
		List<ReminderRule> rulePastList = new ArrayList<>();
		ReminderRuleImpl pastRule = new ReminderRuleImpl();
		pastRule.setType(DateRuleSPI.class.getSimpleName());
		pastRule.setOperator(DateRuleSPI.AFTER);
		cal.add(Calendar.HOUR_OF_DAY, -4);
		pastRule.setRightOperand(Formatter.formatDatetime(cal.getTime()));
		rulePastList.add(pastRule);

		boolean pastEval = ruleEngine.evaluateDateRule(rulePastList);
		Assert.assertTrue(pastEval);
		
		//check rule with date in the future
		List<ReminderRule> ruleFutureList = new ArrayList<>();
		ReminderRuleImpl futureRule = new ReminderRuleImpl();
		futureRule.setType(DateRuleSPI.class.getSimpleName());
		futureRule.setOperator(DateRuleSPI.AFTER);
		cal.add(Calendar.DATE, 4);
		futureRule.setRightOperand(Formatter.formatDatetime(cal.getTime()));
		ruleFutureList.add(futureRule);
		
		boolean futureEval = ruleEngine.evaluateDateRule(ruleFutureList);
		Assert.assertFalse(futureEval);
	}
	
	@Test
	public void beforeDateRule() {
		Calendar cal = Calendar.getInstance();
		
		//check rule with date in the future
		List<ReminderRule> rulePastList = new ArrayList<>();
		ReminderRuleImpl pastRule = new ReminderRuleImpl();
		pastRule.setType(BeforeDateRuleSPI.class.getSimpleName());
		pastRule.setOperator(BeforeDateRuleSPI.BEFORE);
		cal.add(Calendar.HOUR_OF_DAY, 2);
		pastRule.setRightOperand(Formatter.formatDatetime(cal.getTime()));
		rulePastList.add(pastRule);

		boolean pastEval = ruleEngine.evaluateDateRule(rulePastList);
		Assert.assertTrue(pastEval);
		
		//check rule with date in the pase
		List<ReminderRule> ruleFutureList = new ArrayList<>();
		ReminderRuleImpl futureRule = new ReminderRuleImpl();
		futureRule.setType(BeforeDateRuleSPI.class.getSimpleName());
		futureRule.setOperator(BeforeDateRuleSPI.BEFORE);
		cal.add(Calendar.DATE, -4);
		futureRule.setRightOperand(Formatter.formatDatetime(cal.getTime()));
		ruleFutureList.add(futureRule);
		
		boolean futureEval = ruleEngine.evaluateDateRule(ruleFutureList);
		Assert.assertFalse(futureEval);
	}
	
	@Test
	public void repositoryRules() {
		//create a repository entry with a relation to a group and members
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-1");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-2");
		Identity groupCoach3 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-3");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-4");
		Identity groupParticipant5 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-5");
		
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(owner1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant4, re, GroupRoles.participant.name());
		
		BusinessGroup group = businessGroupDao.createAndPersist(groupCoach3, "grp-rule-1", "grp-rule-1-desc", BusinessGroup.BUSINESS_TYPE,
				0, 5, true, false, true, false, false);
		businessGroupRelationDao.addRole(groupParticipant5, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRelationToResource(group, re);
		dbInstance.commitAndCloseSession();
		
		//check the rules
		// 1. all
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.all);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(5, all.size());
			Assert.assertTrue(all.contains(owner1));
			Assert.assertTrue(all.contains(coach2));
			Assert.assertTrue(all.contains(groupCoach3));
			Assert.assertTrue(all.contains(participant4));
			Assert.assertTrue(all.contains(groupParticipant5));
		}

		// 2. owner
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.owner);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(owner1));
		}
		
		// 3. coach
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.coach);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(coach2));
			Assert.assertTrue(all.contains(groupCoach3));
		}
		
		// 4. participant
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.participant);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(participant4));
			Assert.assertTrue(all.contains(groupParticipant5));
		}
		
		// 5. participant and coach
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.participantAndCoach);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(4, all.size());
			Assert.assertTrue(all.contains(coach2));
			Assert.assertTrue(all.contains(groupCoach3));
			Assert.assertTrue(all.contains(participant4));
			Assert.assertTrue(all.contains(groupParticipant5));
		}
		
		// 6. owner and coach
		{
			List<ReminderRule> rules = getRules(RepositoryEntryRoleRuleSPI.Roles.ownerAndCoach);
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(3, all.size());
			Assert.assertTrue(all.contains(owner1));
			Assert.assertTrue(all.contains(coach2));
			Assert.assertTrue(all.contains(groupCoach3));
		}	
	}
	
	private List<ReminderRule> getRules(RepositoryEntryRoleRuleSPI.Roles option) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(RepositoryEntryRoleRuleSPI.class.getSimpleName());
		rule.setOperator("=");
		rule.setRightOperand(option.name());
		
		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void userPropertyRules() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("rule-3");
		
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();

		id1.getUser().setProperty(UserConstants.FIRSTNAME, "Aoi");
		id1.getUser().setProperty(UserConstants.LASTNAME, "Volks");
		userManager.updateUserFromIdentity(id1);
	
		id2.getUser().setProperty(UserConstants.FIRSTNAME, "Yukino");
		id2.getUser().setProperty(UserConstants.LASTNAME, "Volks");
		userManager.updateUserFromIdentity(id2);
		
		id3.getUser().setProperty(UserConstants.FIRSTNAME, "Ryomou");
		id3.getUser().setProperty(UserConstants.LASTNAME, "Shimei");
		userManager.updateUserFromIdentity(id3);
		dbInstance.commit();
		
		//check user properties rules
		{
			List<ReminderRule> rules = getRules(UserConstants.LASTNAME, "Volks");
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id1));
			Assert.assertTrue(all.contains(id2));
		}

		//check user properties rules with role
		{
			List<ReminderRule> rules = getRules(UserConstants.LASTNAME, "Volks");

			ReminderRuleImpl rule = new ReminderRuleImpl();
			rule.setType(RepositoryEntryRoleRuleSPI.class.getSimpleName());
			rule.setOperator("=");
			rule.setRightOperand(RepositoryEntryRoleRuleSPI.Roles.owner.name());
			rules.add(rule);
			
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id1));
		}
		
		//check with 2 rules
		{
			List<ReminderRule> rules = getRules(UserConstants.LASTNAME, "Volks");

			ReminderRuleImpl rule = new ReminderRuleImpl();
			rule.setType(UserPropertyRuleSPI.class.getSimpleName());
			rule.setLeftOperand(UserConstants.FIRSTNAME);
			rule.setOperator("=");
			rule.setRightOperand("Yukino");
			rules.add(rule);
			
			List<Identity> all = ruleEngine.getIdentities(re, null, rules, true);
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id2));
		}
		
	}
	
	private List<ReminderRule> getRules(String propertyName, String value) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(UserPropertyRuleSPI.class.getSimpleName());
		rule.setLeftOperand(propertyName);
		rule.setOperator("=");
		rule.setRightOperand(value);
		
		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}

	@Test
	public void initialLaunchDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-3");
		
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);

		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
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
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);

		{ // check after 3 days
			List<ReminderRule> rules = getInitialLaunchRules(3, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(3, all.size());
		}

		{ // check after 7 days
			List<ReminderRule> rules = getInitialLaunchRules(7, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 week
			List<ReminderRule> rules = getInitialLaunchRules(1, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 1 month
			List<ReminderRule> rules = getInitialLaunchRules(1, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 6 weeks
			List<ReminderRule> rules = getInitialLaunchRules(6, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getInitialLaunchRules(2, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getInitialLaunchRules(1, LaunchUnit.year);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(0, all.size());
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
	
	private List<ReminderRule> getInitialLaunchRules(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(InitialCourseLaunchRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	
	@Test
	public void recentLaunchDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-3");
		
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);

		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		//create user course infos
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id1);
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id2);
		userCourseInformationsManager.updateUserCourseInformations(re.getOlatResource(), id3);
		dbInstance.commit();
		
		//fake the date
		updateRecentLaunchDate(re.getOlatResource(), id1, -5, Calendar.DATE);
		updateRecentLaunchDate(re.getOlatResource(), id2, -35, Calendar.DATE);
		updateRecentLaunchDate(re.getOlatResource(), id3, -75, Calendar.DATE);
		dbInstance.commitAndCloseSession();
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);

		{ // check after 3 days
			List<ReminderRule> rules = getRecentLaunchRules(3, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(3, all.size());
		}

		{ // check after 7 days
			List<ReminderRule> rules = getRecentLaunchRules(7, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 week
			List<ReminderRule> rules = getRecentLaunchRules(1, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 1 month
			List<ReminderRule> rules = getRecentLaunchRules(1, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 6 weeks
			List<ReminderRule> rules = getRecentLaunchRules(6, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getRecentLaunchRules(2, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getRecentLaunchRules(1, LaunchUnit.year);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(0, all.size());
		}
	}
	
	private void updateRecentLaunchDate(OLATResource courseRes, Identity id, int amount, int field) {
		UserCourseInfosImpl userCourseInfos = (UserCourseInfosImpl)userCourseInformationsManager.getUserCourseInformations(courseRes, id);
		Date recentLaunch = userCourseInfos.getRecentLaunch();
		Calendar cal = Calendar.getInstance();
		cal.setTime(recentLaunch);
		cal.add(field, amount);
		userCourseInfos.setRecentLaunch(cal.getTime());
		dbInstance.getCurrentEntityManager().merge(userCourseInfos);
		dbInstance.commit();
	}
	
	private List<ReminderRule> getRecentLaunchRules(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(RecentCourseLaunchRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	

	@Test
	public void courseEnrollmentDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("initial-launch-3");
		
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);
		dbInstance.commit();
		
		addEnrollmentDate(re, id1, GroupRoles.owner,  -5, Calendar.DATE);
		addEnrollmentDate(re, id2, GroupRoles.coach, -35, Calendar.DATE);
		addEnrollmentDate(re, id3, GroupRoles.participant, -75, Calendar.DATE);
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);

		{ // check after 3 days
			List<ReminderRule> rules = getCourseEnrollmentDateRules(3, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(3, all.size());
		}

		{ // check after 7 days
			List<ReminderRule> rules = getCourseEnrollmentDateRules(7, LaunchUnit.day);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 week
			List<ReminderRule> rules = getCourseEnrollmentDateRules(1, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 1 month
			List<ReminderRule> rules = getCourseEnrollmentDateRules(1, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}

		{ // check after 6 weeks
			List<ReminderRule> rules = getCourseEnrollmentDateRules(6, LaunchUnit.week);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getCourseEnrollmentDateRules(2, LaunchUnit.month);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check after 1 year 
			List<ReminderRule> rules = getCourseEnrollmentDateRules(1, LaunchUnit.year);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(0, all.size());
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
	
	private List<ReminderRule> getCourseEnrollmentDateRules(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(CourseEnrollmentDateRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void afterBeginDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("before-begin-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("before-begin-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("before-begin-3");

		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());//now
		cal.add(Calendar.DATE, -21);
		Date validFrom = cal.getTime();
		cal.add(Calendar.DATE, 90);
		Date validTo = cal.getTime();
		
		RepositoryEntryLifecycle cycle = lifecycleDao.create("Cycle 1", "Cycle soft 1", false, validFrom, validTo);
		re = repositoryManager.setDescriptionAndName(re, null, null, null, null, null, null, null, null, cycle);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		{ // check after 2 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(2, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 7 days (between begin and and date)
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(7, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 2 week s
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(2, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 21 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(21, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 3 weeks
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(3, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 22 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(22, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
		
		{ // check after 4 weeks
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(4, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
		
		{ // check after 1 month
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidFromRule(1, LaunchUnit.month);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
	}
	
	private List<ReminderRule> getRepositoryEntryLifecycleRuleValidFromRule(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(RepositoryEntryLifecycleAfterValidFromRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());
		
		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}

	@Test
	public void afterEndDate() {
		//create a course with 3 members
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("after-end-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("after-end-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("after-end-3");
		
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(null);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());//now
		cal.add(Calendar.DATE, -25);
		Date validFrom = cal.getTime();
		cal.add(Calendar.DATE, 4);//- 3weeks
		Date validTo = cal.getTime();
		
		RepositoryEntryLifecycle cycle = lifecycleDao.create("Cycle 2", "Cycle soft 2", false, validFrom, validTo);
		re = repositoryManager.setDescriptionAndName(re, null, null, null, null, null, null, null, null, cycle);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		{ // check after 2 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(2, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 7 days (between begin and and date)
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(7, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 2 week s
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(1, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 21 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(21, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 3 weeks
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(3, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertTrue(match);
		}
		
		{ // check after 22 days
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(22, LaunchUnit.day);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
		
		{ // check after 4 weeks
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(4, LaunchUnit.week);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
		
		{ // check after 1 month
			List<ReminderRule> rules = getRepositoryEntryLifecycleRuleValidToRule(1, LaunchUnit.month);
			boolean match = ruleEngine.evaluateRepositoryEntryRule(re, rules);
			Assert.assertFalse(match);
		}
	}
	
	private List<ReminderRule> getRepositoryEntryLifecycleRuleValidToRule(int amount, LaunchUnit unit) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(RepositoryEntryLifecycleAfterValidToRuleSPI.class.getSimpleName());
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(amount));
		rule.setRightUnit(unit.name());
		
		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void score() {
		//create a course with 3 members and generate some datas
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsRndUser("score-tutor-1");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("score-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("score-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("score-3");
		
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(id1);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		String nodeIdent = assessmentData(tutor, id1, createScoreEvaluation(1.0f, false), re);
		assessmentData(tutor, id2, createScoreEvaluation(5.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(10.0f, true), re);
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);
		
		{ // check score > 0.5
			List<ReminderRule> rules = getScoreRules(">", 0.5f, nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(3, all.size());
		}
		
		{ // check score > 20.0
			List<ReminderRule> rules = getScoreRules(">", 20.0f, nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(0, all.size());
		}
	}
	
	private List<ReminderRule> getScoreRules(String operator, float value, String nodeIdent) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(ScoreRuleSPI.class.getSimpleName());
		rule.setLeftOperand(nodeIdent);
		rule.setOperator(operator);
		rule.setRightOperand(Float.toString(value));

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void attempts() {
		//create a course with 3 members and generate some datas
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-tutor-");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-3");
		
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(id1);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		String nodeIdent = assessmentData(tutor, id1, createScoreEvaluation(1.0f, false), re);
		assessmentData(tutor, id2, createScoreEvaluation(5.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(10.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(11.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(12.0f, true), re);
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);
		
		{ // check attempts > 1
			List<ReminderRule> rules = getAttemptsRules(">", 1, nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check attempts = 1
			List<ReminderRule> rules = getAttemptsRules("=", 1, nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id1));
			Assert.assertTrue(all.contains(id2));
		}
	}
	
	private List<ReminderRule> getAttemptsRules(String operator, int value, String nodeIdent) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(AttemptsRuleSPI.class.getSimpleName());
		rule.setLeftOperand(nodeIdent);
		rule.setOperator(operator);
		rule.setRightOperand(Integer.toString(value));

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void initialAttempts() {
		//create a course with 3 members and generate some datas
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-tutor-");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-3");
		
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(id1);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		String nodeIdent = assessmentData(tutor, id1, createScoreEvaluation(1.0f, false), re);
		assessmentData(tutor, id2, createScoreEvaluation(5.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(10.0f, true), re);
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);
		
		{ // check attempts > 1
			List<ReminderRule> rules = getInitialAttemptsRules(1, LaunchUnit.day.name(), nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(0, all.size());
		}
	}
	
	private List<ReminderRule> getInitialAttemptsRules(int value, String unit, String nodeIdent) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(InitialAttemptsRuleSPI.class.getSimpleName());
		rule.setLeftOperand(nodeIdent);
		rule.setOperator(">");
		rule.setRightOperand(Integer.toString(value));
		rule.setRightUnit(unit);

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	@Test
	public void passed() {
		//create a course with 3 members and generate some datas
		Identity tutor = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-tutor-");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("attempts-3");
		
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(id1);
		repositoryEntryRelationDao.addRole(id1, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(id2, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(id3, re, GroupRoles.participant.name());
		dbInstance.commit();
		
		String nodeIdent = assessmentData(tutor, id1, createScoreEvaluation(1.0f, false), re);
		assessmentData(tutor, id2, createScoreEvaluation(5.0f, true), re);
		assessmentData(tutor, id3, createScoreEvaluation(10.0f, true), re);
		
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);
		
		{ // check passed
			List<ReminderRule> rules = getPassedRules("passed", nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(2, all.size());
			Assert.assertTrue(all.contains(id2));
			Assert.assertTrue(all.contains(id3));
		}
		
		{ // check failed
			List<ReminderRule> rules = getPassedRules("failed", nodeIdent);
			List<Identity> all = new ArrayList<>(identities);
			ruleEngine.filterByRules(re, all, rules);
			
			Assert.assertEquals(1, all.size());
			Assert.assertTrue(all.contains(id1));
		}
	}
	
	private List<ReminderRule> getPassedRules(String status, String nodeIdent) {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(PassedRuleSPI.class.getSimpleName());
		rule.setLeftOperand(nodeIdent);
		rule.setOperator("=");
		rule.setRightOperand(status);

		List<ReminderRule> rules = new ArrayList<>(1);
		rules.add(rule);
		return rules;
	}
	
	private String assessmentData(Identity tutor, Identity student, ScoreEvaluation scoreEval, RepositoryEntry re) {
		//create user course infos
		ICourse course = CourseFactory.loadCourse(re);
		List<CourseNode> assessableNodeList = AssessmentHelper.getAssessableNodes(re, course.getEditorTreeModel(), null);
		CourseNode testNode = null; 
		for(CourseNode currentNode: assessableNodeList) {	
			if (currentNode.getType().equalsIgnoreCase("iqtest")) {
				testNode = currentNode;
				break;
			}
		}
		Assert.assertNotNull(testNode);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(re, testNode);
		Assert.assertTrue(Mode.none != assessmentConfig.getScoreMode());
		
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(student);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

		course.getCourseEnvironment().getAssessmentManager().saveScoreEvaluation(testNode, tutor, student, scoreEval, userCourseEnv, true, Role.coach);
		dbInstance.commit();
		
		return testNode.getIdent();
	}
	
	private ScoreEvaluation createScoreEvaluation(float score, boolean passed) {
		return new ScoreEvaluation(Float.valueOf(score), null, null, null, Boolean.valueOf(passed), null, null, null, null, null, null);
	}
	
	@Test
	public void evaluateRuleListThrowsException() {
		List<ReminderRule> ruleList = Collections.<ReminderRule>emptyList();
		
		boolean ollOk = ruleEngine.evaluate(null, ruleList);
		
		Assert.assertFalse(ollOk);
	}
	
	@Test
	public void getMembersThrowsException() {
		
		List<Identity> members = ruleEngine.getMembers(null, null);
		
		assertThat(members).isNotNull().isEmpty();
	}
	
	@Test
	public void getFilterThrowsException() {
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType(ScoreRuleSPI.class.getSimpleName());
		rule.setRightOperand("no integer");
		
		ruleEngine.filterByRule(null, null, rule);
	}
}
