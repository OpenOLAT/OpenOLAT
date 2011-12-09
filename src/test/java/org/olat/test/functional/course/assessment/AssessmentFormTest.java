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
package org.olat.test.functional.course.assessment;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.home.EvidencesOfAchievement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests that all inputs in AssessmentForm (AssessmentTool) are correctly stored and seen by the student 
 * in different cluster nodes.<br/>
 * 
 * Test setup: <br/>
 * 1. import course <br/>
 * 2. add standard author as owner to imported course <br/>
 * 3. add standard author as tutor to group <br/>
 * 4. standard student enrolls in group <br/>
 * 
 * Test case: <br/>
 * Tests whether all inputs in AssessmentForm are correctly stored and correctly seen by the tutor in AssessmentTool and
 * the student in course run and in the "Evidence of achievement".
 * The AssessmentForm is used for assessing: Test, Task and Assessment course elements.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentFormTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
	private final String COURSE_NAME = "AssessmentTool_selenium" +System.currentTimeMillis();	
	private final String GROUP_NAME = "Gruppe 1";
	
	private String STUDENT1 = "test";	
	private final String COMMENT_TXT = "comment_for_user";
	
	
	public void setUp() throws Exception { 
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
	  //author adds participants to group or student enrolls in group
		// STUDENT enrolls - login and enroll if not already member in GROUP_NAME
		OlatLoginInfos olatLoginInfos1 = context.getStandardStudentOlatLoginInfos(1);		
		STUDENT1 = olatLoginInfos1.getUsername();		
		
		//cleanup first - only if the course name is reused
		/*OlatServerSetupHelper.deleteEvidenceOfAchievement(COURSE_NAME, olatLoginInfos1);
		OlatServerSetupHelper.deleteAllCoursesNamed(COURSE_NAME);*/
				
		//import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
					
		//"administrator" adds author as owner of the COURSE_NAME
		WorkflowHelper.addOwnerToLearningResource(context.getStandardAuthorOlatLoginInfos(1).getUsername(),COURSE_NAME);
		
		//add author as tutor of the group
		WorkflowHelper.addTutorToGroup(context.getStandardAuthorOlatLoginInfos(1).getUsername(), COURSE_NAME, GROUP_NAME);
				
		enrollInGroupAfterLogin(olatLoginInfos1, COURSE_NAME, GROUP_NAME);		
	}
	
	private void enrollInGroupAfterLogin(OlatLoginInfos olatLoginInfos, String courseName, String groupName) {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(olatLoginInfos);
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseName);
		EnrolmentRun enrolmentElement = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
		if(!enrolmentElement.alreadyEnrolled(groupName)) {
		  enrolmentElement.enrol(groupName);
		  assertTrue(enrolmentElement.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
		workflow.logout();		
	}
	
	
	public void testFormAssessmentTest() throws Exception {		
		Context context = Context.getContext();
		OLATWorkflowHelper workflow_S = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
		CourseRun courseRun_S = workflow_S.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
    //	setup: student runs the test first time ("Test 1" and gets a score equals with 2)
		courseRun_S.selectCourseElement("Struktur 1");
		TestRun testRun = courseRun_S.selectTest("Test 1").startTest();
		testRun.selectMenuItem("Single Choice");
		testRun.setSingleChoiceSolution("Antwort 3");
		testRun.selectMenuItem("Multiple Choice");
		String[] answers = {"Antwort 2", "Antwort 3"};
		testRun.setMultipleChoiceSolution(answers);
    //	student achieved score: 2
		TestElement testElement_S = testRun.finishTest(true, 2);
		assertEquals("2.000", testElement_S.getAchievedScore());
    //	end setup - student achieved a score of 2.000
				
    //author - tutor of the default group - opens the AssessmentTool and assess student
		OLATWorkflowHelper workflow_A = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		AssessmentTool assessmentTool = workflow_A.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getAssessmentTool();
		String score = assessmentTool.getScoreInTableAsPerUser(STUDENT1, "Test 1", false);
		String passed = assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, "Test 1", false);
		assertEquals("2.000", score);
		assertEquals("Passed", passed);
		
    //	author selects "Test 1" and fills in the form - attempts:7, score: 7, passed: true
		AssessmentForm assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, "Test 1");
		assertEquals("2.000", assessmentForm.getScore());
		assessmentForm.setAttempts(7);
		assessmentForm.setScore(7);
		assessmentForm.setPassed(true);
		assessmentForm.setUserComments(COMMENT_TXT);
		assessmentTool = assessmentForm.save();
    //	check in table the update of the values		
		assertEquals("7.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, "Test 1", false));
		assertEquals("7", assessmentTool.getAttemptsInTableAsPerUser(STUDENT1, "Test 1", false));
		assertEquals("Passed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, "Test 1", false));
		
    //	author selects "Test 1" and fills in the form
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, "Test 1");		
		
		//author checks the previous stored values in AssessmentForm				
		assertEquals("7.000", assessmentForm.getScore());
		assertEquals("7", assessmentForm.getAttempts());		
		assertEquals(AssessmentForm.PASSED_YES, assessmentForm.getPassed());
		assertEquals(COMMENT_TXT, assessmentForm.getUserComment());
					
		//author changes inputs - attempts:6, score: 6, passed: false
		assessmentForm.setAttempts(6);
		assessmentForm.setScore(6);
		assessmentForm.setPassed(false);
		assessmentTool = assessmentForm.save();
    //	check in table the update of the values		
		assertEquals("6.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, "Test 1", false));
		assertEquals("6", assessmentTool.getAttemptsInTableAsPerUser(STUDENT1, "Test 1", false));
		assertEquals("Failed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, "Test 1", false));
					
		//author changes the score evaluation for the "Bewertung 1" element, that reset it to the original values
		//tests that the PASSED info could be reset
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, "Bewertung 1");
		assessmentForm.setScore(3);
		assessmentForm.setPassed(true);
		assessmentTool = assessmentForm.save();
		assertEquals("3.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, "Bewertung 1", false));		
		assertEquals("Passed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, "Bewertung 1", false));
						
		//reset form 
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, "Bewertung 1");
		assessmentForm.setScore(0);
		assessmentForm.setPassed(null);
		assessmentTool = assessmentForm.save();		
		assertEquals("0.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, "Bewertung 1", false));		
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, "Bewertung 1");
		assertEquals(AssessmentForm.PASSED_NO_INFO, assessmentForm.getPassed());
				
		//student checks the results in course run
		TestElement testElement = courseRun_S.selectTest("Test 1");		
		assertEquals("6.000", testElement.getAchievedScore());
		assertEquals("Failed", testElement.getStatus());
		assertEquals(COMMENT_TXT, testElement.getCommentFromTutor());
			
    //student checks "Evidence of achievement" in HOME - show details		
		EvidencesOfAchievement evidencesOfAchievement = workflow_S.getHome().getEvidencesOfAchievement();
		evidencesOfAchievement.selectDetails(COURSE_NAME);	
    //	check score in table	
		assertEquals("6.000", evidencesOfAchievement.getCourseElementScore("Test 1"));
		assertEquals("6", evidencesOfAchievement.getCourseElementAttempts("Test 1"));
		assertEquals("Failed", evidencesOfAchievement.getCoursePassedStatus("Test 1"));
		assertEquals("0.000", evidencesOfAchievement.getCourseElementScore("Bewertung 1"));
						
		workflow_A.logout();
	}	
	
}
