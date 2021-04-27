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
package org.olat.modules.coach;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.UserListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is mainly a test with a large number of courses and groups.
 * It check if the rights courses are seen, but not details of the
 * statistics.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachingLargeTest extends OlatTestCase {
	
	private static boolean isInitialized = false;
	private static final Random rnd = new Random();
	
	private static int NUM_OF_COURSES = 5;
	private static int NUM_OF_STUDENTS = 15;
	
	private static Identity author;
	private static Identity coach10, coach11, coach12, coach13;
	private static Identity coach20, coach21, coach22, coach23, coach24, coach25, coach26;
	
	private static Identity aStudent;
	
	private static RepositoryEntry course10;

	private static Map<Long,List<Long>> coachToCourseMap = new ConcurrentHashMap<>();
	private static Map<Identity,List<RepositoryEntry>> studentToCourseMap = new ConcurrentHashMap<>();
	private static Map<Long,List<Long>> coachToGroupCourseMap = new ConcurrentHashMap<>();
	
	private static List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Before
	public void setUp() throws Exception {
		if(isInitialized) return;
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, false);
		
		//author
		author = JunitTestHelper.createAndPersistIdentityAsAuthor("author_" + UUID.randomUUID());
		//r1 set of coach
		coach10 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-10");
		coach11 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-11");
		coach12 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-12");
		coach13 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-13");
		//r2 set of coach
		coach20 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-20");
		coach21 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-21");
		coach22 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-22");
		coach23 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-23");
		coach24 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-24");
		coach25 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-25");
		coach26 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-26");
		
		List<Identity> students = new ArrayList<>();
		//r1 set of student
		for(int i=0; i<NUM_OF_STUDENTS; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("student-" + i);
			students.add(student);
		}
		
		int qCount = 0;

		//create courses with members
		for(int i=0; i<NUM_OF_COURSES; i++) {
			URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
			RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl); // 3
			if(i == 0) {
				course10 = re;
			}
			
			int r1 = (i % 4);
			int r2 = (i % 7);
			
			switch(r1) {
				case 0: addCoachToCourse(coach10, re); break;
				case 1: addCoachToCourse(coach11, re); break;
				case 2: addCoachToCourse(coach12, re); break;
				case 3: addCoachToCourse(coach13, re); break;
			}
			
			switch(r2) {
				case 0: addCoachToCourse(coach20, re); break;
				case 1: addCoachToCourse(coach21, re); break;
				case 2: addCoachToCourse(coach22, re); break;
				case 3: addCoachToCourse(coach23, re); break;
				case 4: addCoachToCourse(coach24, re); break;
				case 5: addCoachToCourse(coach25, re); break;
				case 6: addCoachToCourse(coach26, re); break;
			}
			assertNotNull(re);
			
			List<Identity> newStudents = reservoirSample(students, NUM_OF_STUDENTS / 2);
			for(Identity newStudent:newStudents) {
				if(aStudent == null) {
					aStudent = newStudent;
				}
				addStudentToCourse(newStudent, re);
				if(qCount++ % 20 == 0) {
					dbInstance.intermediateCommit();
				}
			}
			dbInstance.intermediateCommit();
		}
		
		//create courses with members
		for(int i=0; i<NUM_OF_COURSES; i++) {
			URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
			RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);// 3 
			// create groups without waiting list
			BusinessGroup g1 = businessGroupService.createBusinessGroup(author, "coach-g1", null, BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(10), false, false, re);
			BusinessGroup g2 = businessGroupService.createBusinessGroup(author, "coach-g2", null, BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(10), false, false, re);
	    
			//permission to see owners and participants
			businessGroupService.updateDisplayMembers(g1, false, false, false, false, false, false, false);
			businessGroupService.updateDisplayMembers(g2, true, true, false, false, false, false, false);
	    
			// coach
			addCoachToGroupCourse(coach10, g1, re);
			addCoachToGroupCourse(coach11, g1, re);
			addCoachToGroupCourse(coach12, g2, re);
	    
			//students
			List<Identity> newStudents = reservoirSample(students, NUM_OF_STUDENTS / 2);
			for(Iterator<Identity> it=newStudents.iterator(); it.hasNext(); ) {
				Identity newStudent = it.next();
				addStudentToGroupCourse(newStudent, g1, re);
				it.remove();
				if(qCount++ % 20 == 0) {
					dbInstance.intermediateCommit();
				}
				if(rnd.nextDouble() > 0.8d) {
					break;
				}
			}

			// members g2
			for(Iterator<Identity> it=newStudents.iterator(); it.hasNext(); ) {
				addStudentToGroupCourse(it.next(), g2, re);
				if(qCount++ % 20 == 0) {
					dbInstance.intermediateCommit();
				}
			}

			dbInstance.intermediateCommit();
		}
		
		isInitialized = true;
	}
	
	private void addCoachToCourse(Identity coach, RepositoryEntry re) {
		Long coachKey = coach.getKey();
		
		repositoryService.addRole(coach, re, GroupRoles.coach.name());
		if(!coachToCourseMap.containsKey(coachKey)) {
			coachToCourseMap.put(coachKey, new ArrayList<Long>());
		}
		coachToCourseMap.get(coachKey).add(re.getKey());
	}
	
	private void addCoachToGroupCourse(Identity coach, BusinessGroup group, RepositoryEntry re) {
		Long coachKey = coach.getKey();
		
		businessGroupRelationDao.addRole(coach, group, GroupRoles.coach.name());
		if(!coachToCourseMap.containsKey(coachKey)) {
			coachToCourseMap.put(coachKey, new ArrayList<Long>());
		}
		coachToCourseMap.get(coachKey).add(re.getKey());
		
		//GROUPS
		if(!coachToGroupCourseMap.containsKey(coachKey)) {
			coachToGroupCourseMap.put(coachKey, new ArrayList<Long>());
		}
		coachToGroupCourseMap.get(coachKey).add(group.getKey());
	}
	
	private void addStudentToCourse(Identity student, RepositoryEntry re) {
		repositoryService.addRole(student, re, GroupRoles.participant.name());
		if(!studentToCourseMap.containsKey(student)) {
			studentToCourseMap.put(student, new ArrayList<RepositoryEntry>());
		}
		studentToCourseMap.get(student).add(re);
	}
	
	private void addStudentToGroupCourse(Identity student, BusinessGroup group, RepositoryEntry re) {
		businessGroupRelationDao.addRole(student, group, GroupRoles.participant.name());
		if(!studentToCourseMap.containsKey(student)) {
			studentToCourseMap.put(student, new ArrayList<RepositoryEntry>());
		}
		studentToCourseMap.get(student).add(re);
	}
	
	public List<Identity> reservoirSample(Iterable<Identity> items, int m) {   
		List<Identity> res = new ArrayList<>(m);   
		int count = 0;   
		for(Identity item : items){       
			count++;
			if (count <= m)           
				res.add(item);       
			else{  
				int r = rnd.nextInt(count);
				if (r < m)               
					res.set(r, item);       
			}   
		}   
		return res;
	}
	
	@Test
	public void getStudentsStatistics() {
		List<StudentStatEntry> statEntries = coachingService.getStudentsStatistics(coach10, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(statEntries);
	}
	
	@Test
	public void getCoursesStatistics() {
		List<CourseStatEntry> courseStatEntries = coachingService.getCoursesStatistics(coach10);
		Assert.assertNotNull(courseStatEntries);
		List<Long> coachedCourses = coachToCourseMap.get(coach10.getKey());
		Assert.assertNotNull(coachedCourses);
		Assert.assertEquals(coachedCourses.size(), courseStatEntries.size());
		
		List<Long> courseStatsKeys = new ArrayList<>();
		for(CourseStatEntry statEntry:courseStatEntries) {
			courseStatsKeys.add(statEntry.getRepoKey());
		}
		Assert.assertTrue(courseStatsKeys.containsAll(coachedCourses));
		Assert.assertTrue(coachedCourses.containsAll(courseStatsKeys));
	}
	
	@Test
	public void getGroupsStatistics() {
		List<GroupStatEntry> groupStatEntries = coachingService.getGroupsStatistics(coach10);
		Assert.assertNotNull(groupStatEntries);
		List<Long> coachedGroups = coachToGroupCourseMap.get(coach10.getKey());
		Assert.assertNotNull(coachedGroups);
		Assert.assertEquals(coachedGroups.size(), groupStatEntries.size());
		
		List<Long> groupStatsKeys = new ArrayList<>();
		for(GroupStatEntry statEntry:groupStatEntries) {
			groupStatsKeys.add(statEntry.getGroupKey());
		}
		
		Assert.assertTrue(groupStatsKeys.containsAll(coachedGroups));
		Assert.assertTrue(coachedGroups.containsAll(groupStatsKeys));
	}
	
	@Test
	public void getCourse() {
		List<Long> coachedCourses = coachToCourseMap.get(coach10.getKey());
		Assert.assertNotNull(coachedCourses);

		List<EfficiencyStatementEntry> statEntries = coachingService.getCourse(coach10, course10, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(statEntries);
		Assert.assertFalse(statEntries.isEmpty());
		Assert.assertTrue(coachedCourses.contains(course10.getKey()));

		for(EfficiencyStatementEntry statEntry:statEntries) {
			Assert.assertNotNull(statEntry.getCourse());
			Assert.assertEquals(course10.getKey(), statEntry.getCourse().getKey());
		}
	}
	
	@Test
	public void getStudentsCourses() {
		List<RepositoryEntry> courses = coachingService.getStudentsCourses(coach10, aStudent);
		Assert.assertNotNull(courses);
		
		List<Long> myCourses = coachToCourseMap.get(coach10.getKey());
		Assert.assertNotNull(myCourses);
	}
	
	@Test
	public void getUserCourses() {
		List<RepositoryEntry> courses = coachingService.getUserCourses(aStudent);
		Assert.assertNotNull(courses);
		Assert.assertEquals(studentToCourseMap.get(aStudent).size(), courses.size());
	}
	
	@Test
	public void getUsersStatistics() {
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setLogin(aStudent.getName());
		
		List<StudentStatEntry> statEntries = coachingService.getUsersStatistics(params, userPropertyHandlers, Locale.ENGLISH);
		Assert.assertNotNull(statEntries);
		Assert.assertEquals(1, statEntries.size());
		
		StudentStatEntry statEntry = statEntries.get(0);
		Assert.assertEquals(aStudent.getKey(), statEntry.getIdentityKey());
		Assert.assertEquals(studentToCourseMap.get(aStudent).size(), statEntry.getCountRepo());
	}
}