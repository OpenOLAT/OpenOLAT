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
package org.olat.modules.coach.manager;

import static org.olat.test.JunitTestHelper.random;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.coach.CoachingLargeTest;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.UserListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CoachingDAO coachingDAO;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	@Autowired
	private EfficiencyStatementManager effManager;
	@Autowired
	private AssessmentService assessmnetService;
	
	/**
	 * 
	 * 1 course with 2 groups.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getStatistics_duplicateInGroups()
	throws URISyntaxException {
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);

		Assert.assertNotNull(re);

		dbInstance.commitAndCloseSession();
		
		ICourse course = CourseFactory.loadCourse(re);			
		boolean enabled = course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled();
		Assert.assertTrue(enabled);
		
		//re -> owner,coach, p1, p2
		//  -> group 1 p1
		//  -> group 2 p2
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re, GroupRoles.owner.name());
		repositoryService.addRole(coach, re, GroupRoles.coach.name());
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of 2 groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(coach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
	    businessGroupRelationDao.addRole(participant1, group1, GroupRoles.participant.name());
	    BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Coaching-grp-2", "tg", BusinessGroup.BUSINESS_TYPE,
	    		null, null, false, false, re);
	    businessGroupRelationDao.addRole(participant1, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements
	    effManager.createUserEfficiencyStatement(new Date(), 6.0f, "g1", "pc1", true, participant1, re.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 2.0f, "g2", "pc2", false, participant2, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		//make user infos
		userCourseInformationsManager.updateUserCourseInformations(course.getCourseEnvironment().getCourseGroupManager().getCourseResource(), participant1);
		dbInstance.commitAndCloseSession();
		
		
		//native
		List<CourseStatEntry> nativeStats = coachingDAO.getCoursesStatisticsNative(coach);
		Assert.assertNotNull(nativeStats);
		Assert.assertEquals(1, nativeStats.size());
		CourseStatEntry nativeStat = nativeStats.get(0);
		Assert.assertEquals(2, nativeStat.getCountStudents());
		Assert.assertEquals(1, nativeStat.getCountPassed());
		Assert.assertEquals(1, nativeStat.getCountFailed());
		Assert.assertEquals(1, nativeStat.getInitialLaunch());
		Assert.assertEquals(4.0f, nativeStat.getAverageScore(), 0.0001);

		
		//user native
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(nativeUserStats);
		Assert.assertEquals(2, nativeUserStats.size());
		//participant1
		StudentStatEntry entryParticipant1 = getStudentStatEntry(participant1, nativeUserStats);
		Assert.assertNotNull(entryParticipant1);
		Assert.assertEquals(1, entryParticipant1.getCountPassed());
		Assert.assertEquals(0, entryParticipant1.getCountFailed());
		Assert.assertEquals(0, entryParticipant1.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant1.getInitialLaunch());
		//participant2
		StudentStatEntry entryParticipant2 = getStudentStatEntry(participant2, nativeUserStats);
		Assert.assertNotNull(entryParticipant2);
		Assert.assertEquals(0, entryParticipant2.getCountPassed());
		Assert.assertEquals(1, entryParticipant2.getCountFailed());
		Assert.assertEquals(0, entryParticipant2.getCountNotAttempted());
		Assert.assertEquals(0, entryParticipant2.getInitialLaunch());

		
		//group native
		List<GroupStatEntry> nativeGroupStats = coachingDAO.getGroupsStatisticsNative(coach);
		Assert.assertNotNull(nativeGroupStats);
		Assert.assertEquals(2, nativeGroupStats.size());
		//group 1
		GroupStatEntry entryGroup1 = getGroupStatEntry(group1, nativeGroupStats);
		Assert.assertNotNull(entryGroup1);
		Assert.assertEquals(1, entryGroup1.getCountCourses());
		Assert.assertEquals(1, entryGroup1.getCountPassed());
		Assert.assertEquals(0, entryGroup1.getCountFailed());
		Assert.assertEquals(0, entryGroup1.getCountNotAttempted());
		Assert.assertEquals(1, entryGroup1.getInitialLaunch());
		Assert.assertEquals(6.0f, entryGroup1.getAverageScore(), 0.0001f);
		//group 2
		GroupStatEntry entryGroup2 = getGroupStatEntry(group1, nativeGroupStats);
		Assert.assertNotNull(entryGroup2);
		Assert.assertEquals(1, entryGroup2.getCountCourses());
		Assert.assertEquals(1, entryGroup2.getCountPassed());
		Assert.assertEquals(0, entryGroup2.getCountFailed());
		Assert.assertEquals(0, entryGroup2.getCountNotAttempted());
		Assert.assertEquals(1, entryGroup2.getInitialLaunch());
		Assert.assertEquals(6.0f, entryGroup2.getAverageScore(), 0.0001f);
	}
	
	/**
	 * 3 courses in the same business group
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getStatistics_notAttempted()
	throws URISyntaxException {
		
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl);
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl);
		RepositoryEntry re3 = JunitTestHelper.deployCourse(null, "Coaching course 3", courseUrl);
		dbInstance.commitAndCloseSession();
		
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re1, GroupRoles.owner.name());
		repositoryService.addRole(coach, re1, GroupRoles.coach.name());
		repositoryService.addRole(coach, re2, GroupRoles.coach.name());
		repositoryService.addRole(coach, re3, GroupRoles.coach.name());
		
		
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re2, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of 2 groups
		BusinessGroup group= businessGroupService.createBusinessGroup(coach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
		businessGroupService.addResourceTo(group, re2);
		businessGroupService.addResourceTo(group, re3);
		businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements participant 1
	    effManager.createUserEfficiencyStatement(new Date(), 6.0f, "g1", "pc1", true, participant1, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.0f, "g2", "pc2", false, participant1, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 2.0f, "g3", "pc3", false, participant1, re3.getOlatResource());

		//make statements participant 2
	    effManager.createUserEfficiencyStatement(new Date(), 6.0f, "g1", "pc1", true, participant2, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), null, "g2", "pc2", null, participant2, re2.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//make user infos
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant2);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant2);
		dbInstance.commitAndCloseSession();
		
		//check course
		List<CourseStatEntry> nativeStats = coachingDAO.getCoursesStatisticsNative(coach);
		Assert.assertNotNull(nativeStats);
		Assert.assertEquals(3, nativeStats.size());
		CourseStatEntry entryRe1 = getCourseStatEntry(re1, nativeStats);
		Assert.assertEquals(2, entryRe1.getCountStudents());
		Assert.assertEquals(2, entryRe1.getCountPassed());
		Assert.assertEquals(0, entryRe1.getCountFailed());
		Assert.assertEquals(0, entryRe1.getCountNotAttempted());
		Assert.assertEquals(2, entryRe1.getInitialLaunch());
		Assert.assertEquals(6.0f, entryRe1.getAverageScore(), 0.0001);
		
		CourseStatEntry entryRe2 = getCourseStatEntry(re2, nativeStats);
		Assert.assertEquals(2, entryRe2.getCountStudents());
		Assert.assertEquals(0, entryRe2.getCountPassed());
		Assert.assertEquals(1, entryRe2.getCountFailed());
		Assert.assertEquals(1, entryRe2.getCountNotAttempted());
		Assert.assertEquals(2, entryRe2.getInitialLaunch());
		Assert.assertEquals(4.0f, entryRe2.getAverageScore(), 0.0001);
		
		CourseStatEntry entryRe3 = getCourseStatEntry(re3, nativeStats);
		Assert.assertEquals(2, entryRe3.getCountStudents());
		Assert.assertEquals(0, entryRe3.getCountPassed());
		Assert.assertEquals(1, entryRe3.getCountFailed());
		Assert.assertEquals(1, entryRe3.getCountNotAttempted());
		Assert.assertEquals(1, entryRe3.getInitialLaunch());
		Assert.assertEquals(2.0f, entryRe3.getAverageScore(), 0.0001);
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		
		//user native
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(nativeUserStats);
		Assert.assertEquals(2, nativeUserStats.size());
		//participant1
		StudentStatEntry entryParticipant1 = getStudentStatEntry(participant1, nativeUserStats);
		Assert.assertNotNull(entryParticipant1);
		Assert.assertEquals(1, entryParticipant1.getCountPassed());
		Assert.assertEquals(2, entryParticipant1.getCountFailed());
		Assert.assertEquals(0, entryParticipant1.getCountNotAttempted());
		Assert.assertEquals(3, entryParticipant1.getInitialLaunch());
		Assert.assertEquals(3, entryParticipant1.getCountRepo());
		//participant2
		StudentStatEntry entryParticipant2 = getStudentStatEntry(participant2, nativeUserStats);
		Assert.assertNotNull(entryParticipant2);
		Assert.assertEquals(1, entryParticipant2.getCountPassed());
		Assert.assertEquals(0, entryParticipant2.getCountFailed());
		Assert.assertEquals(2, entryParticipant2.getCountNotAttempted());
		Assert.assertEquals(2, entryParticipant2.getInitialLaunch());
		Assert.assertEquals(3, entryParticipant1.getCountRepo());

		
		//group native
		List<GroupStatEntry> nativeGroupStats = coachingDAO.getGroupsStatisticsNative(coach);
		Assert.assertNotNull(nativeGroupStats);
		Assert.assertEquals(1, nativeGroupStats.size());
		//group 1
		GroupStatEntry entryGroup1 = getGroupStatEntry(group, nativeGroupStats);
		Assert.assertNotNull(entryGroup1);
		Assert.assertEquals(6, entryGroup1.getCountStudents());
		Assert.assertEquals(2, entryGroup1.getCountDistinctStudents());
		Assert.assertEquals(3, entryGroup1.getCountCourses());
		Assert.assertEquals(2, entryGroup1.getCountPassed());
		Assert.assertEquals(2, entryGroup1.getCountFailed());
		Assert.assertEquals(2, entryGroup1.getCountNotAttempted());
		Assert.assertEquals(5, entryGroup1.getInitialLaunch());
		Assert.assertEquals(4.5f, entryGroup1.getAverageScore(), 0.0001f);
	}
	
	@Test
	public void getStatistics_owner()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl); 
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl); 
		RepositoryEntry re3 = JunitTestHelper.deployCourse(null, "Coaching course 3", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re1, GroupRoles.owner.name());
		repositoryService.addRole(coach, re2, GroupRoles.owner.name());
		repositoryService.addRole(coach, re3, GroupRoles.coach.name());
		
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re1, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 2
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re2);
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-3");
		businessGroupRelationDao.addRole(participant3, group2, GroupRoles.participant.name());
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-4");
		businessGroupRelationDao.addRole(participant4, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 3
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re3);
		Identity participant5 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-5");
		businessGroupRelationDao.addRole(participant5, group3, GroupRoles.participant.name());
		Identity participant6 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-6");
		businessGroupRelationDao.addRole(participant6, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements participant 1
	    effManager.createUserEfficiencyStatement(new Date(), 6.0f, "g1", "pc1", true, participant1, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.0f, "g1", "pc1", false, participant2, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 5.5f, "g1", "pc1", true, participant3, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), null, null, null, null, participant4, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.0f, "g1", "pc1", true, participant5, re3.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 3.0f, "g1", "pc1", false, participant6, re3.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//make user infos
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant2);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant2);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant3);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant4);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant5);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant6);
		dbInstance.commitAndCloseSession();
		
		//owner can see participant 1,2,3 and 4
		// p1 has 1 assessment in re1
		// p2 has 1 assessment in re1
		// p3 has 2 assessments in re1 and re2
		// p4 has 1 assessment in re2
		// 5 and p6 has 1 assessment in re3
		
		List<GroupStatEntry> nativeGroupStats = coachingDAO.getGroupsStatisticsNative(coach);
		Assert.assertNotNull(nativeGroupStats);
		Assert.assertEquals(1, nativeGroupStats.size());
		GroupStatEntry entryGroup2 = getGroupStatEntry(group2, nativeGroupStats);
		Assert.assertNotNull(entryGroup2);
		Assert.assertEquals(2, entryGroup2.getCountDistinctStudents());
		Assert.assertEquals(2, entryGroup2.getInitialLaunch());
		Assert.assertEquals(1, entryGroup2.getCountPassed());
		Assert.assertEquals(0, entryGroup2.getCountFailed());
		Assert.assertEquals(1, entryGroup2.getCountNotAttempted());
		Assert.assertEquals(5.5f, entryGroup2.getAverageScore(), 0.0001f);
		

		//re 3 is removed because coach has no visible participants within
		List<CourseStatEntry> nativeCourseStats = coachingDAO.getCoursesStatisticsNative(coach);
		Assert.assertNotNull(nativeCourseStats);
		Assert.assertEquals(2, nativeCourseStats.size());
		//re 1
		CourseStatEntry entryCourse1 = getCourseStatEntry(re1, nativeCourseStats);
		Assert.assertNotNull(entryCourse1);
		Assert.assertEquals(2, entryCourse1.getCountStudents());
		Assert.assertEquals(2, entryCourse1.getInitialLaunch());
		Assert.assertEquals(1, entryCourse1.getCountPassed());
		Assert.assertEquals(1, entryCourse1.getCountFailed());
		Assert.assertEquals(0, entryCourse1.getCountNotAttempted());
		Assert.assertEquals(5.0f, entryCourse1.getAverageScore(), 0.0001f);
		//re 2
		CourseStatEntry entryCourse2 = getCourseStatEntry(re2, nativeCourseStats);
		Assert.assertNotNull(entryCourse2);
		Assert.assertEquals(2, entryCourse2.getCountStudents());
		Assert.assertEquals(2, entryCourse2.getInitialLaunch());
		Assert.assertEquals(1, entryCourse2.getCountPassed());
		Assert.assertEquals(0, entryCourse2.getCountFailed());
		Assert.assertEquals(1, entryCourse2.getCountNotAttempted());
		Assert.assertEquals(5.5f, entryCourse2.getAverageScore(), 0.0001f);
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		//user native
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(nativeUserStats);
	
		Assert.assertEquals(4, nativeUserStats.size());
		//participant1 is only in re 1
		StudentStatEntry entryParticipant1 = getStudentStatEntry(participant1, nativeUserStats);
		Assert.assertNotNull(entryParticipant1);
		Assert.assertEquals(1, entryParticipant1.getCountPassed());
		Assert.assertEquals(0, entryParticipant1.getCountFailed());
		Assert.assertEquals(0, entryParticipant1.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant1.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant1.getCountRepo());
		//participant2 is only in re 1
		StudentStatEntry entryParticipant2 = getStudentStatEntry(participant2, nativeUserStats);
		Assert.assertNotNull(entryParticipant2);
		Assert.assertEquals(0, entryParticipant2.getCountPassed());
		Assert.assertEquals(1, entryParticipant2.getCountFailed());
		Assert.assertEquals(0, entryParticipant2.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant2.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant2.getCountRepo());
		//participant3 is in re 2 ( via group 2)
		StudentStatEntry entryParticipant3 = getStudentStatEntry(participant3, nativeUserStats);
		Assert.assertNotNull(entryParticipant3);
		Assert.assertEquals(1, entryParticipant3.getCountPassed());
		Assert.assertEquals(0, entryParticipant3.getCountFailed());
		Assert.assertEquals(0, entryParticipant3.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant3.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant3.getCountRepo());
		//participant4 is in re 2 ( via group 2)
		StudentStatEntry entryParticipant4 = getStudentStatEntry(participant4, nativeUserStats);
		Assert.assertNotNull(entryParticipant4);
		Assert.assertEquals(0, entryParticipant4.getCountPassed());
		Assert.assertEquals(0, entryParticipant4.getCountFailed());
		Assert.assertEquals(1, entryParticipant4.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant4.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant4.getCountRepo());
	}
	
	/**
	 * Check the access permissions on course (coach can only see courses with memebrsOnly, or access >= 3)
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getStatistics_permissionOnCourses()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1",
				RepositoryEntryStatusEnum.preparation, false, false, courseUrl);
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2",
				RepositoryEntryStatusEnum.review, false, false, courseUrl);
		RepositoryEntry re3 = JunitTestHelper.deployCourse(null, "Coaching course 3",
				RepositoryEntryStatusEnum.published, true, false, courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity courseCoach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		Identity groupCoach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(courseCoach, re1, GroupRoles.coach.name());
		repositoryService.addRole(courseCoach, re2, GroupRoles.coach.name());
		repositoryService.addRole(courseCoach, re3, GroupRoles.coach.name());
		
		//add participants to courses
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re1, GroupRoles.participant.name());
		Identity participant11 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-11");
		repositoryService.addRole(participant11, re1, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re2, GroupRoles.participant.name());
		Identity participant21 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-21");
		repositoryService.addRole(participant21, re2, GroupRoles.participant.name());
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-3");
		repositoryService.addRole(participant3, re3, GroupRoles.participant.name());
		Identity participant31 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-31");
		repositoryService.addRole(participant31, re3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 1
		BusinessGroup group1 = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
		Identity participantG1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g1");
		businessGroupRelationDao.addRole(participantG1, group1, GroupRoles.participant.name());
		Identity participantG11 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g11");
		businessGroupRelationDao.addRole(participantG11, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//members of group of re 2
		BusinessGroup group2 = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re2);
		Identity participantG2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g2");
		businessGroupRelationDao.addRole(participantG2, group2, GroupRoles.participant.name());
		Identity participantG21 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g22");
		businessGroupRelationDao.addRole(participantG21, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 3
		BusinessGroup group3 = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-3", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re3);
		Identity participantG3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g3");
		businessGroupRelationDao.addRole(participantG3, group3, GroupRoles.participant.name());
		Identity participantG31 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-g33");
		businessGroupRelationDao.addRole(participantG31, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements participants
	    effManager.createUserEfficiencyStatement(new Date(), 6.230429f, "g1", "pc1", true, participant1, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.182317f, "g1", "pc1", false, participant11, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.095833f, "g1", "pc1", false, participantG1, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.578924f, "g1", "pc1", false, participantG11, re1.getOlatResource());
	    
	    effManager.createUserEfficiencyStatement(new Date(), 2.2894727f, "g1", "pc1", true, participant2, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), null, null, null, null, participant21, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 5.2347774f, "g1", "pc1", true, participantG2, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), null, null, null, null, participantG21, re2.getOlatResource());
	    
	    effManager.createUserEfficiencyStatement(new Date(), 4.0f, "g1", "pc1", true, participant3, re3.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 3.0f, "g1", "pc1", false, participant31, re3.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 5.5f, "g1", "pc1", true, participantG3, re3.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 1.0f, "g1", "pc1", false, participantG31, re3.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//make user infos
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant1);
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant11);
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participantG1);
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participantG11);
		
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant2);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant21);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participantG2);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participantG21);
		
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant3);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant31);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participantG3);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participantG31);
		dbInstance.commitAndCloseSession();
		

		//course coach cannot see groups
		List<GroupStatEntry> courseCoachGroupStats = coachingDAO.getGroupsStatisticsNative(courseCoach);
		Assert.assertNotNull(courseCoachGroupStats);
		Assert.assertEquals(0, courseCoachGroupStats.size());

		List<GroupStatEntry> groupCoachGroupStats = coachingDAO.getGroupsStatisticsNative(groupCoach);
		Assert.assertNotNull(groupCoachGroupStats);
		Assert.assertEquals(1, groupCoachGroupStats.size());
		GroupStatEntry entryGroup3 = getGroupStatEntry(group3, groupCoachGroupStats);
		Assert.assertNotNull(entryGroup3);
		Assert.assertEquals(2, entryGroup3.getCountDistinctStudents());
		Assert.assertEquals(2, entryGroup3.getInitialLaunch());
		Assert.assertEquals(1, entryGroup3.getCountPassed());
		Assert.assertEquals(1, entryGroup3.getCountFailed());
		Assert.assertEquals(0, entryGroup3.getCountNotAttempted());
		Assert.assertEquals(3.25f, entryGroup3.getAverageScore(), 0.0001f);
		

		//course statistics
		List<CourseStatEntry> courseCoachCourseStats = coachingDAO.getCoursesStatisticsNative(courseCoach);
		Assert.assertNotNull(courseCoachCourseStats);
		Assert.assertEquals(1, courseCoachCourseStats.size());
		CourseStatEntry entryCourse3 = getCourseStatEntry(re3, courseCoachCourseStats);
		Assert.assertNotNull(entryCourse3);
		Assert.assertEquals(2, entryCourse3.getCountStudents());
		Assert.assertEquals(2, entryCourse3.getInitialLaunch());
		Assert.assertEquals(1, entryCourse3.getCountPassed());
		Assert.assertEquals(1, entryCourse3.getCountFailed());
		Assert.assertEquals(0, entryCourse3.getCountNotAttempted());
		Assert.assertEquals(3.5f, entryCourse3.getAverageScore(), 0.0001f);
		//group coach can see course 3
		List<CourseStatEntry> groupCoachCourseStats = coachingDAO.getCoursesStatisticsNative(groupCoach);
		Assert.assertNotNull(groupCoachCourseStats);
		Assert.assertEquals(1, groupCoachCourseStats.size());
		CourseStatEntry entryCourse3g = getCourseStatEntry(re3, groupCoachCourseStats);
		Assert.assertEquals(2, entryCourse3g.getCountStudents());
		Assert.assertEquals(2, entryCourse3g.getInitialLaunch());
		Assert.assertEquals(1, entryCourse3g.getCountPassed());
		Assert.assertEquals(1, entryCourse3g.getCountFailed());
		Assert.assertEquals(0, entryCourse3g.getCountNotAttempted());
		Assert.assertEquals(3.25f, entryCourse3g.getAverageScore(), 0.0001f);
	
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		//user native
		List<StudentStatEntry> courseCoachUserStats = coachingDAO.getStudentsStatisticsNative(courseCoach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(courseCoachUserStats);
		Assert.assertEquals(2, courseCoachUserStats.size());
		//participant3 is only in re 1
		StudentStatEntry entryParticipant3 = getStudentStatEntry(participant3, courseCoachUserStats);
		Assert.assertNotNull(entryParticipant3);
		Assert.assertEquals(1, entryParticipant3.getCountPassed());
		Assert.assertEquals(0, entryParticipant3.getCountFailed());
		Assert.assertEquals(0, entryParticipant3.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant3.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant3.getCountRepo());
		//participant31 is only in re 1
		StudentStatEntry entryParticipant31 = getStudentStatEntry(participant31, courseCoachUserStats);
		Assert.assertNotNull(entryParticipant31);
		Assert.assertEquals(0, entryParticipant31.getCountPassed());
		Assert.assertEquals(1, entryParticipant31.getCountFailed());
		Assert.assertEquals(0, entryParticipant31.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipant31.getInitialLaunch());
		Assert.assertEquals(1, entryParticipant31.getCountRepo());
		
		//group coach
		List<StudentStatEntry> groupCoachUserStats = coachingDAO.getStudentsStatisticsNative(groupCoach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(groupCoachUserStats);
		Assert.assertEquals(2, groupCoachUserStats.size());

		//participantG3 is in re 3 ( via group 3)
		StudentStatEntry entryParticipantG3 = getStudentStatEntry(participantG3, groupCoachUserStats);
		Assert.assertNotNull(entryParticipantG3);
		Assert.assertEquals(1, entryParticipantG3.getCountPassed());
		Assert.assertEquals(0, entryParticipantG3.getCountFailed());
		Assert.assertEquals(0, entryParticipantG3.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipantG3.getInitialLaunch());
		Assert.assertEquals(1, entryParticipantG3.getCountRepo());
		//participantG3 is in re 3 ( via group 3)
		StudentStatEntry entryParticipantG31 = getStudentStatEntry(participantG31, groupCoachUserStats);
		Assert.assertNotNull(entryParticipantG31);
		Assert.assertEquals(0, entryParticipantG31.getCountPassed());
		Assert.assertEquals(1, entryParticipantG31.getCountFailed());
		Assert.assertEquals(0, entryParticipantG31.getCountNotAttempted());
		Assert.assertEquals(1, entryParticipantG31.getInitialLaunch());
		Assert.assertEquals(1, entryParticipantG31.getCountRepo());
	}
	
	/**
	 * This is an important test to check if the return values of the statistics
	 * are correctly handled because some of them can be null or 0.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getStatistics_emptyStatements_emptyCourseInfos()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl);
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re1, GroupRoles.owner.name());
		repositoryService.addRole(coach, re2, GroupRoles.coach.name());
		
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re1, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//groups
		BusinessGroup group1 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-3");
		businessGroupRelationDao.addRole(participant3, group1, GroupRoles.participant.name());
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-4");
		businessGroupRelationDao.addRole(participant4, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//check groups statistics
		List<GroupStatEntry> nativeGroupStats = coachingDAO.getGroupsStatisticsNative(coach);
		Assert.assertNotNull(nativeGroupStats);
		Assert.assertEquals(1, nativeGroupStats.size());
		GroupStatEntry entryGroup1 = getGroupStatEntry(group1, nativeGroupStats);
		Assert.assertNotNull(entryGroup1);
		Assert.assertEquals(2, entryGroup1.getCountDistinctStudents());
		Assert.assertEquals(0, entryGroup1.getInitialLaunch());
		Assert.assertEquals(0, entryGroup1.getCountPassed());
		Assert.assertEquals(0, entryGroup1.getCountFailed());
		Assert.assertEquals(2, entryGroup1.getCountNotAttempted());
		Assert.assertNull(entryGroup1.getAverageScore());
		
		//courses
		List<CourseStatEntry> nativeCourseStats = coachingDAO.getCoursesStatisticsNative(coach);
		Assert.assertNotNull(nativeCourseStats);
		Assert.assertEquals(1, nativeCourseStats.size());
		//re 1
		CourseStatEntry entryCourse1 = getCourseStatEntry(re1, nativeCourseStats);
		Assert.assertNotNull(entryCourse1);
		Assert.assertEquals(4, entryCourse1.getCountStudents());
		Assert.assertEquals(0, entryCourse1.getInitialLaunch());
		Assert.assertEquals(0, entryCourse1.getCountPassed());
		Assert.assertEquals(0, entryCourse1.getCountFailed());
		Assert.assertEquals(4, entryCourse1.getCountNotAttempted());
		Assert.assertNull(entryCourse1.getAverageScore());

		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		//user native
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(nativeUserStats);
		Assert.assertEquals(4, nativeUserStats.size());
		//participants have all the same statistics
		Identity[] participants = new Identity[]{ participant1, participant2, participant3, participant4};
		for(Identity participant:participants) {
			StudentStatEntry entryParticipant = getStudentStatEntry(participant, nativeUserStats);
			Assert.assertNotNull(entryParticipant);
			Assert.assertEquals(0, entryParticipant.getCountPassed());
			Assert.assertEquals(0, entryParticipant.getCountFailed());
			Assert.assertEquals(1, entryParticipant.getCountNotAttempted());
			Assert.assertEquals(0, entryParticipant.getInitialLaunch());
			Assert.assertEquals(1, entryParticipant.getCountRepo());
		}
	}
	
	/**
	 * This is an important test to check if the return values of the statistics
	 * are correctly handled because some of them can be null or 0.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getStatistics_empty()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl);
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re1, GroupRoles.owner.name());
		repositoryService.addRole(coach, re2, GroupRoles.coach.name());
		//groups
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re1);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(group);

		//groups (method doesn't return courses without participant)
		List<GroupStatEntry> nativeGroupStats = coachingDAO.getGroupsStatisticsNative(coach);
		Assert.assertNotNull(nativeGroupStats);
		Assert.assertEquals(0, nativeGroupStats.size());
		
		//courses (method doesn't return courses without participant)
		List<CourseStatEntry> nativeCourseStats = coachingDAO.getCoursesStatisticsNative(coach);
		Assert.assertNotNull(nativeCourseStats);
		Assert.assertEquals(0, nativeCourseStats.size());
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		//user native
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(nativeUserStats);
		Assert.assertEquals(0, nativeUserStats.size());
	}
	
	@Test
	public void getStatistics_completion()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl); 
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl); 
		RepositoryEntry re3 = JunitTestHelper.deployCourse(null, "Coaching course 3", courseUrl);
		RepositoryEntry re4 = JunitTestHelper.deployCourse(null, "Coaching course 4", courseUrl);
		dbInstance.commitAndCloseSession();
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("Coach-1-");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-3");

		//members of courses
		repositoryService.addRole(coach, re1, GroupRoles.coach.name());
		repositoryService.addRole(participant1, re1, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re1, GroupRoles.participant.name());
		repositoryService.addRole(participant3, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 2
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re2);
		businessGroupRelationDao.addRole(coach, group2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant1, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of group of re 3
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "Coaching-grp-2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re3);
		businessGroupRelationDao.addRole(coach, group3, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(participant1, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//members of course 4
		repositoryService.addRole(coach, re4, GroupRoles.coach.name());
		repositoryService.addRole(participant1, re4, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re4, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make assessments participant 1
		AssessmentEntry aeParticipant1Course1 = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re1, random(), Boolean.TRUE, null);
		aeParticipant1Course1.setCompletion(Double.valueOf(1));
		assessmnetService.updateAssessmentEntry(aeParticipant1Course1);
		AssessmentEntry aeParticipant1Course1a = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re1, random(), Boolean.FALSE, null);
		aeParticipant1Course1a.setCompletion(Double.valueOf(1));
		assessmnetService.updateAssessmentEntry(aeParticipant1Course1a);
		AssessmentEntry aeParticipant1Course2 = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re2, random(), Boolean.TRUE, null);
		aeParticipant1Course2.setCompletion(Double.valueOf(0.2));
		assessmnetService.updateAssessmentEntry(aeParticipant1Course2);
		AssessmentEntry aeParticipant1Course3 = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re3, random(), Boolean.TRUE, null);
		aeParticipant1Course3.setCompletion(null);
		assessmnetService.updateAssessmentEntry(aeParticipant1Course3);
		AssessmentEntry aeParticipant1Course4 = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re4, random(), Boolean.TRUE, null);
		aeParticipant1Course4.setCompletion(Double.valueOf(0.6));
		assessmnetService.updateAssessmentEntry(aeParticipant1Course4);
		dbInstance.commitAndCloseSession();
		
		//make assessments participant 2
		AssessmentEntry aeParticipant2Course3 = assessmnetService.getOrCreateAssessmentEntry(participant1, null, re3, random(), Boolean.TRUE, null);
		aeParticipant2Course3.setCompletion(null);
		assessmnetService.updateAssessmentEntry(aeParticipant2Course3);
		dbInstance.commitAndCloseSession();
		
		//make assessments participant 3
		AssessmentEntry aeParticipant3Course1 = assessmnetService.getOrCreateAssessmentEntry(participant3, null, re1, random(), Boolean.TRUE, null);
		aeParticipant3Course1.setCompletion(Double.valueOf(0.4));
		assessmnetService.updateAssessmentEntry(aeParticipant3Course1);
		dbInstance.commitAndCloseSession();
		
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		List<StudentStatEntry> nativeUserStats = coachingDAO.getStudentsStatisticsNative(coach, userPropertyHandlers, Locale.ENGLISH);
		
		// Assert average completions of members
		StudentStatEntry statsParticipant1 = getStudentStatEntry(participant1, nativeUserStats);
		Assert.assertNotNull(statsParticipant1);
		Assert.assertEquals(0.6, statsParticipant1.getAverageCompletion(), 0.0001f);
		
		StudentStatEntry statsParticipant2 = getStudentStatEntry(participant2, nativeUserStats);
		Assert.assertNotNull(statsParticipant2);
		Assert.assertNull(statsParticipant2.getAverageCompletion());
		
		// Assert average completions of courses
		List<CourseStatEntry> courseCoachCourseStats = coachingDAO.getCoursesStatisticsNative(coach);
		CourseStatEntry statsCourse1 = getCourseStatEntry(re1, courseCoachCourseStats);
		Assert.assertNotNull(statsCourse1);
		Assert.assertEquals(0.7, statsCourse1.getAverageCompletion(), 0.0001f);

		CourseStatEntry statsCourse2 = getCourseStatEntry(re2, courseCoachCourseStats);
		Assert.assertNotNull(statsCourse2);
		Assert.assertEquals(0.2, statsCourse2.getAverageCompletion(), 0.0001f);
		
		CourseStatEntry statsCourse3 = getCourseStatEntry(re3, courseCoachCourseStats);
		Assert.assertNotNull(statsCourse3);
		Assert.assertNull(statsCourse3.getAverageCompletion());
		
		CourseStatEntry statsCourse4 = getCourseStatEntry(re4, courseCoachCourseStats);
		Assert.assertNotNull(statsCourse4);
		Assert.assertEquals(0.6, statsCourse4.getAverageCompletion(), 0.0001f);
	}
	
	@Test
	public void getUsers()
	throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re1 = JunitTestHelper.deployCourse(null, "Coaching course 1", courseUrl);
		RepositoryEntry re2 = JunitTestHelper.deployCourse(null, "Coaching course 2", courseUrl);
		RepositoryEntry re3 = JunitTestHelper.deployCourse(null, "Coaching course 3", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("User-Part-1");
		repositoryService.addRole(participant, re1, GroupRoles.participant.name());
		repositoryService.addRole(participant, re2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		// groups
		BusinessGroup group2 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re2);
		businessGroupRelationDao.addRole(participant, group2, GroupRoles.participant.name());
		BusinessGroup group3 = businessGroupService.createBusinessGroup(null, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re3);
		businessGroupRelationDao.addRole(participant, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements participant 1
	    effManager.createUserEfficiencyStatement(new Date(), 6.0f, "g1", "pc1", true, participant, re1.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 4.0f, "g1", "pc1", false, participant, re2.getOlatResource());
	    effManager.createUserEfficiencyStatement(new Date(), 2.0f, "g1", "pc1", false, participant, re3.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		//make user infos
		userCourseInformationsManager.updateUserCourseInformations(re1.getOlatResource(), participant);
		userCourseInformationsManager.updateUserCourseInformations(re2.getOlatResource(), participant);
		userCourseInformationsManager.updateUserCourseInformations(re3.getOlatResource(), participant);
		dbInstance.commitAndCloseSession();

		//update props
		User partUser = participant.getUser();
		partUser.setProperty(UserConstants.FIRSTNAME, "Rei");
		partUser.setProperty(UserConstants.LASTNAME, "Ayanami");
		partUser.setProperty(UserConstants.EMAIL, "rei.ayanami@openolat.com");
		partUser = userManager.updateUser(participant, partUser);
		dbInstance.commitAndCloseSession();
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		
		//search by first name
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		Map<String,String> props = new HashMap<>();
		props.put(UserConstants.FIRSTNAME, "re");
		params.setUserProperties(props);
		List<StudentStatEntry> stats = coachingDAO.getUsersStatisticsNative(params, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(stats);
		Assert.assertFalse(stats.isEmpty());
		
		//check participant
		StudentStatEntry entryStat = getStudentStatEntry(participant, stats);
		Assert.assertNotNull(entryStat);
		Assert.assertEquals(3, entryStat.getCountRepo());
		Assert.assertEquals(3, entryStat.getInitialLaunch());
		Assert.assertEquals(1, entryStat.getCountPassed());
		Assert.assertEquals(2, entryStat.getCountFailed());
		Assert.assertEquals(0, entryStat.getCountNotAttempted());


		//search by user name
		SearchCoachedIdentityParams loginParams = new SearchCoachedIdentityParams();
		loginParams.setLogin(participant.getName());
		List<StudentStatEntry> loginStats = coachingDAO.getUsersStatisticsNative(loginParams, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(loginStats);
		Assert.assertEquals(1, loginStats.size());
		
		//check participant
		StudentStatEntry loginStat = loginStats.get(0);
		Assert.assertNotNull(loginStat);
		Assert.assertEquals(3, loginStat.getCountRepo());
		Assert.assertEquals(3, loginStat.getInitialLaunch());
		Assert.assertEquals(1, loginStat.getCountPassed());
		Assert.assertEquals(2, loginStat.getCountFailed());
		Assert.assertEquals(0, loginStat.getCountNotAttempted());
	}
	
	@Test
	public void getStudents_coach_course() throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("User-Part-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("User-Part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("User-Part-2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("User-Part-3");
		
		repositoryService.addRole(coach, re, GroupRoles.coach.name());
		repositoryService.addRole(participant1, re, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		// groups
		BusinessGroup group2 = businessGroupService.createBusinessGroup(coach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		businessGroupRelationDao.addRole(participant2, group2, GroupRoles.participant.name());
		BusinessGroup group3 = businessGroupService.createBusinessGroup(coach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		businessGroupRelationDao.addRole(participant3, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		List<Identity> students = coachingDAO.getStudents(coach, re);
		Assert.assertNotNull(students);
		Assert.assertEquals(3, students.size());
		Assert.assertTrue(students.contains(participant1));
		Assert.assertTrue(students.contains(participant2));
		Assert.assertTrue(students.contains(participant3));
	}
	
	@Test
	public void getStudents_owner_course() throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		//members of courses
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Owner-1");
		Identity groupCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("Group coach-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Part-2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Part-3");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("Part-4");
		
		repositoryService.addRole(owner, re, GroupRoles.owner.name());
		repositoryService.addRole(participant1, re, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		// groups
		BusinessGroup group2 = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		businessGroupRelationDao.addRole(participant2, group2, GroupRoles.participant.name());
		BusinessGroup group3 = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		businessGroupRelationDao.addRole(participant3, group3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant4, group3, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//owner
		List<Identity> ownerStudents = coachingDAO.getStudents(owner, re);
		Assert.assertNotNull(ownerStudents);
		Assert.assertEquals(4, ownerStudents.size());
		Assert.assertTrue(ownerStudents.contains(participant1));
		Assert.assertTrue(ownerStudents.contains(participant2));
		Assert.assertTrue(ownerStudents.contains(participant3));
		Assert.assertTrue(ownerStudents.contains(participant4));
		
		//groups coach
		List<Identity> coachedStudents = coachingDAO.getStudents(groupCoach, re);
		Assert.assertEquals(3, coachedStudents.size());
		Assert.assertTrue(coachedStudents.contains(participant2));
		Assert.assertTrue(coachedStudents.contains(participant3));
		Assert.assertTrue(coachedStudents.contains(participant4));
	}

	@Test
	public void isCoach_owner() throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);
		dbInstance.commitAndCloseSession();
	
		//members of courses
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("Owner-1-");
		repositoryService.addRole(coach, re, GroupRoles.owner.name());
		dbInstance.commitAndCloseSession();
		
		boolean canCoach = coachingDAO.isCoach(coach);
		Assert.assertTrue(canCoach);
	}
	
	@Test
	public void isCoach_coach() throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);
		dbInstance.commitAndCloseSession();
	
		//coach of course
		Identity courseCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-1");
		repositoryService.addRole(courseCoach, re, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();

		//coach in a group of the course
		Identity groupCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-2");
		BusinessGroup group = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check if coaching tool is enabled
		boolean canCourseCoach = coachingDAO.isCoach(courseCoach);
		Assert.assertTrue(canCourseCoach);
		boolean canGroupCoach = coachingDAO.isCoach(groupCoach);
		Assert.assertTrue(canGroupCoach);
	}
	
	@Test
	public void isCoach_notPermitted() throws URISyntaxException {
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course",
				RepositoryEntryStatusEnum.published, false, false, courseUrl);
		dbInstance.commitAndCloseSession();
	
		//owner of course
		Identity courseOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-1");
		Identity courseParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("Participant-1");
		repositoryService.addRole(courseOwner, re, GroupRoles.owner.name());
		repositoryService.addRole(courseParticipant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//coach in a group of the course
		Identity groupCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-2");
		BusinessGroup group = businessGroupService.createBusinessGroup(groupCoach, "Coaching-grp-1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, re);
		Assert.assertNotNull(group);
		dbInstance.commitAndCloseSession();
		
		//check if coaching tool is enabled
		boolean canCourseCoach = coachingDAO.isCoach(courseOwner);
		Assert.assertTrue(canCourseCoach);
		boolean canGroupCoach = coachingDAO.isCoach(groupCoach);
		Assert.assertTrue(canGroupCoach);
		boolean canCourseParticipant= coachingDAO.isCoach(courseParticipant);
		Assert.assertFalse(canCourseParticipant);
	}
	
	private StudentStatEntry getStudentStatEntry(IdentityRef identity, List<StudentStatEntry> entries) {
		StudentStatEntry entry = null;
		for(StudentStatEntry e:entries) {
			if(e.getIdentityKey().equals(identity.getKey())) {
				entry = e;
			}
		}
		return entry;
	}
	
	private GroupStatEntry getGroupStatEntry(BusinessGroupRef group, List<GroupStatEntry> entries) {
		GroupStatEntry entry = null;
		for(GroupStatEntry e:entries) {
			if(e.getGroupKey().equals(group.getKey())) {
				entry = e;
			}
		}
		return entry;
	}
	
	private CourseStatEntry getCourseStatEntry(RepositoryEntryRef re, List<CourseStatEntry> entries) {
		CourseStatEntry entry = null;
		for(CourseStatEntry e:entries) {
			if(e.getRepoKey().equals(re.getKey())) {
				entry = e;
			}
		}
		return entry;
	}
}