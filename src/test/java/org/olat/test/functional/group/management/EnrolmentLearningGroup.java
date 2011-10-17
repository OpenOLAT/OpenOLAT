package org.olat.test.functional.group.management;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.home.Home;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Student subscribes to waiting list and moves up to regular group member when other student cancels his enrolment, part of test suite GroupManagementCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * 1. Student 2 is already member of learning group (from test case CreateLearningGroupLearningArea.java)
 * <p>
 * Test case: <br/>
 * 1. Student 1 opens course COURSE_NAME, sets bookmark and navigates to enrolment course element. <br/>
 * 2. Student 1 enrols and checks if he is on waiting list. <br/>
 * 3. Student 2 opens course COURSE_NAME and navigates to same enrolment course element. <br/>
 * 5. Student 2 cancels enrolment. <br/>
 * 6. Student 1 goes to enrolment again and checks if he has moved from waiting list to enrolled member. <br/>
 * 
 * @author sandra
 * 
 */

public class EnrolmentLearningGroup extends BaseSeleneseTestCase {
	
	@Test(dependsOnGroups = {GroupManagementCombiTest.FIRST}, groups = {GroupManagementCombiTest.SECOND})
	public void testEnrolmentLearningGroupTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(1, GroupManagementCombiTest.STUDENT_USER_NAME, standardPassword, true, false, false, false, false);
		
		// refactored with abstraction layer
		OLATWorkflowHelper workflow1 = context.getOLATWorkflowHelper(student02);
		LearningResources learningResources1 = workflow1.getLearningResources();
		CourseRun courseRun1 = learningResources1.searchAndShowCourseContent(GroupManagementCombiTest.COURSE_NAME);
		courseRun1.setBookmark();
		EnrolmentRun enrolmentRun1 = courseRun1.selectEnrolment("Enrolment");
		enrolmentRun1.enrol(GroupManagementCombiTest.GROUP_NAME_1);
		assertTrue(enrolmentRun1.isTextPresent("On waiting list (1)"));
		workflow1.logout();
		
		OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		LearningResources learningResources2 = workflow2.getLearningResources();
		CourseRun courseRun2 = learningResources2.searchAndShowCourseContent(GroupManagementCombiTest.COURSE_NAME);
		EnrolmentRun enrolmentRun2 = courseRun2.selectEnrolment("Enrolment");
		enrolmentRun2.cancelEnrolment(GroupManagementCombiTest.GROUP_NAME_1);
		workflow2.logout();
		
		OLATWorkflowHelper workflow3 = context.getOLATWorkflowHelper(student02);
		Home home1 = workflow3.getHome();
		CourseRun courseRun3 = home1.selectMyBookmarkedCourse(GroupManagementCombiTest.COURSE_NAME);
		courseRun3.selectCourseElement("Enrolment");
		assertTrue(courseRun3.isTextPresent("enrolled"));
		workflow3.logout();
		
	}
}
