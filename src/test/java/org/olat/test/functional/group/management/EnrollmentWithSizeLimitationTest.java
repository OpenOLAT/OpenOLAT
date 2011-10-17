package org.olat.test.functional.group.management;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Test Jira Issue OLAT-5022: <br/>
 * define a learning group with a place limitation (assert that the place
  limitation is saved), waiting list and automatic adding. Add users until the group is full, remove
  one user, assert that the first on the waiting list is included in the group
 * <br/>
 * </p>
 * Test case: <br/>
 * create groupWithLimitation <br/>
 * define a learning group with a place limitation <br/>
 * open course editor <br/>
 * insert enrolment element <br/>
 * publish and close <br/>
 * author enrolls in groupWithLimitation <br/>
 * assert -> author is enrolled! <br/>
 * student tries to enroll in groupWithLimitation that is already full! <br/>
 * student opens course and navigates to enrollment course element <br/>
 * assert -> student is on waiting list! <br/>
 * author cancels enrollment <br/>
 * assert author enrollment was canceled <br/>
 * assert -> student is now enrolled! <br/>
 * Delete groups <br/>
 * Test the same adding several groups separated with a comma at one time <br/>
 * Delete course <br/>
 *
 * @author alberto
 */
public class EnrollmentWithSizeLimitationTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	public void testEnrolmentWaitingList() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		
		OLATWorkflowHelper olatWorkflow_0 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		
		// author creates course with learning group (max 1 participant) and enrollment course element	
		
		LearningResources learningResources_0 = olatWorkflow_0.getLearningResources();
		LRDetailedView lRDetailedView_0 = learningResources_0.createResource(COURSE_NAME, "Enrolment Test", LR_Types.COURSE);
		CourseRun courseRun_0 = learningResources_0.searchAndShowMyCourseContent(COURSE_NAME);
		
		//create groupWithLimitation	
		GroupManagement groupManagement_0 = courseRun_0.getGroupManagement();
		GroupAdmin groupAdmin = groupManagement_0.createLearningGroup("groupWithLimitation", "group description", 1, true, true);
		//assert that the place limitation is saved
		assertTrue(groupAdmin.getMaxNumParticipants().equals("1"));;		
		courseRun_0 = groupManagement_0.close();
		
		//open course editor
		CourseEditor courseEditor_0 = courseRun_0.getCourseEditor();
		
		//insert enrollment element
		EnrolmentEditor enrollmentElement_0 = (EnrolmentEditor)courseEditor_0.insertCourseElement(CourseEditor.CourseElemTypes.ENROLMENT, true, null);			
		enrollmentElement_0.selectLearningGroups("groupWithLimitation");
		
		//publish and close
		courseEditor_0.publishCourse();
		courseEditor_0.closeToCourseRun();
		olatWorkflow_0.logout();
		
		// author enrolls in groupWithLimitation
		courseRun_0 = this.selectEnrolment(context.getStandardAuthorOlatLoginInfos(1), COURSE_NAME);
		EnrolmentRun enrolmentRun_0 =  courseRun_0.selectEnrolment("Enrolment");
		enrolmentRun_0.enrol("groupWithLimitation");
		
		// assert -> author is enrolled!
		//assertTrue(courseRun_0.isTextPresent("enrolled"));
		
		assertTrue(enrolmentRun_0.alreadyEnrolled("groupWithLimitation"));
		
		
		// student opens course and navigates to enrollment course element			
		CourseRun courseRun_1 = this.selectEnrolment(context.getStandardStudentOlatLoginInfos(1), COURSE_NAME);
		EnrolmentRun enrolmentRun_1 = courseRun_1.selectEnrolment("Enrolment");
		
		
		
		// student tries to enroll in groupWithLimitation that is already full!
		enrolmentRun_1.enrol("groupWithLimitation");
		
		// assert -> student is on waiting list!
		assertTrue(enrolmentRun_1.isTextPresent("On waiting list (1)"));
		
		// author cancels enrollment
		enrolmentRun_0.cancelEnrolment("groupWithLimitation");
		
		// assert author enrollment was canceled
		assertTrue(courseRun_0.isTextPresent("Choose one of the learning groups below to enrol"));
		
		// assert -> student is now enrolled!
		enrolmentRun_1 = courseRun_1.selectEnrolment("Enrolment");
		assertTrue(enrolmentRun_1.alreadyEnrolled("groupWithLimitation"));
		
		
	}
	private CourseRun selectEnrolment(OlatLoginInfos student, String courseName) {
		Context context = Context.getContext();
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(student);
		CourseRun courseRun = olatWorkflow.getLearningResources().searchAndShowCourseContent(courseName);
		courseRun.selectCourseElement(CourseEditor.ENROLMENT_TITLE);
		return courseRun;
	}
	
	
	
	@Override
	protected void cleanUpAfterRun() {
		System.out.println("***************** cleanUpAfterRun STARTED *********************");
	  //author01 deletes course
		Context context = Context.getContext();
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		LRDetailedView lRDetailedView = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME);
		try {
			lRDetailedView.deleteLR();
		} catch (Exception e) {}				
		System.out.println("***************** cleanUpAfterRun ENDED *********************");
	}
	
}
