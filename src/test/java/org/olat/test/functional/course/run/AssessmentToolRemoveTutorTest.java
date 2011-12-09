/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.test.functional.course.run;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests granting and revoking tutoring rights for one or more groups and assessing students in such groups. 
 * <br/> 
 * <p>
 * Test setup: <br/> 
 * Expects a special "AssessmentTool" course must be imported.
  Same tutor is tutoring two groups, each group has one student. <br/>
 * <p> 
 * import course - make sure that this is the course you need! <br/> 
 * "administrator" adds tutor to Gruppe 1 and 2 <br/> 
 * STUDENT1 enrolls - login and enroll if not already member in GROUP_NAME_1 <br/> 
 * STUDENT2 enrolls - login and enroll if not already member in GROUP_NAME_2 <br/> 
 * <p>
 * Test case: <br/>
 * Tutor opens assessment tool, assesses first student (member of Gruppe 1) with 3 <br/>
 * second student (member of Gruppe 2) with 4 points. <br/>
 * Course owner removes tutor of Gruppe 2.  <br/>
 * Tutor opens assessment tool, assesses first student (member of Gruppe 1) with 5  <br/>
 * tutor cannot asses second student (member of Gruppe 2).  <br/>
 * 
 * 
 * @author Marion Weber
 *
 */
public class AssessmentToolRemoveTutorTest extends BaseSeleneseTestCase {
	      	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
  private final String COURSE_NAME = "AssessmentToolRemoveTutor-"+System.currentTimeMillis(); 
  private final String GROUP_NAME_1 = "Gruppe 1";
  private final String GROUP_NAME_2 = "Gruppe 2";
  
  //test actors
  private final String STUDENT1 = "amtststudent01";
  private final String STUDENT2 = "amtststudent02";
  private String TUTOR; 
  
	public void setUp() throws Exception { 
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		TUTOR = context.getStandardAuthorOlatLoginInfos(1).getUsername();
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos olatLoginInfos1 = context.createuserIfNotExists(1, STUDENT1, standardPassword, true, false, false, false, false);
		OlatLoginInfos olatLoginInfos2 = context.createuserIfNotExists(1, STUDENT2, standardPassword, true, false, false, false, false);
										
    //import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
				
    //"administrator" adds tutor to Gruppe 1 and 2
		WorkflowHelper.addTutorToGroup(TUTOR, COURSE_NAME, GROUP_NAME_1);
		WorkflowHelper.addTutorToGroup(TUTOR, COURSE_NAME, GROUP_NAME_2);
		
		// STUDENT1 enrolls - login and enroll if not already member in GROUP_NAME_1				
		enrollInGroupAfterLogin(olatLoginInfos1, COURSE_NAME, GROUP_NAME_1);
		
		// STUDENT2 enrolls - login and enroll if not already member in GROUP_NAME_2		
		enrollInGroupAfterLogin(olatLoginInfos2, COURSE_NAME, GROUP_NAME_2);		
	}
	
	/**
	 * STUDENT enrolls - login and enroll if not already member in groupName
	 * @param selenium_
	 * @param courseName
	 * @param groupName
	 */
	private void enrollInGroupAfterLogin(OlatLoginInfos olatLoginInfos, String courseName, String groupName) {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(olatLoginInfos);
		EnrolmentRun enrolmentRun = workflow.getLearningResources().searchAndShowCourseContent(courseName).selectEnrolment("Enrolment");
		boolean alreadyEnrolled = enrolmentRun.alreadyEnrolled(groupName);
		if(!alreadyEnrolled) {
		  enrolmentRun.enrol(groupName);
		  assertTrue(enrolmentRun.isTextPresent("You have already enroled for the learning group mentioned below"));
		  assertTrue(enrolmentRun.alreadyEnrolled(groupName));
		}
		workflow.logout();		
	}
	
	
	/**
	 * 
	 * 
	 * @throws Exception
	 */
	public void testRunTest() throws Exception {	
		step1();

		step2();
		
		step3();		
	}

	
	private void step1() {
		//Tutor opens assessment tool, assesses first student (member of Gruppe 1) with 3 
		//second student (member of Gruppe 2) with 4 points.		
		OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		AssessmentForm assessmentForm1 = courseRun1.getAssessmentTool().getAssessmentFormAsPerUser(STUDENT1, "Test 0");
		assessmentForm1.setScore(3);
		AssessmentTool assessmentTool = assessmentForm1.save();
		
		AssessmentForm assessmentForm2 = assessmentTool.getAssessmentFormAsPerUser(STUDENT2, "Test 1");
		assessmentForm2.setScore(4);
		courseRun1 = assessmentForm2.save().close();
		courseRun1.close(COURSE_NAME);				
	}
	
	private void step2() {
		//Course owner removes tutor of Gruppe 2.		
		OLATWorkflowHelper workflow2 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		GroupManagement groupManagement = courseRun2.getGroupManagement();
		groupManagement.removeMemberFromGroup(TUTOR, "Gruppe 2");
		courseRun2 = groupManagement.close();
		courseRun2.close(COURSE_NAME);
	}
	
	private void step3() {
		////Tutor opens assessment tool, assesses first student (member of Gruppe 1) with 5 
		// tutor cannot asses second student (member of Gruppe 2).
		OLATWorkflowHelper workflow3 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CourseRun courseRun = workflow3.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		AssessmentForm assessmentForm = courseRun.getAssessmentTool().getAssessmentFormAsPerUser(STUDENT1, "Test 0");
		assessmentForm.setScore(5);
		AssessmentTool assessmentTool = assessmentForm.save();
		assessmentTool.getSelenium().click("ui=course::assessment_selectType(text=As per user)");
		assessmentTool.getSelenium().waitForPageToLoad("30000");
		assertFalse(assessmentTool.getSelenium().isTextPresent("student2"));
		assessmentTool.close().close(COURSE_NAME);		
	}

}
