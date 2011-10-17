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
* <p>
*/ 

package org.olat.test.functional.course.assessment;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.StructureEditor;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.course.run.StructureElement;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.home.EvidencesOfAchievement;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests the Assessment (AssessmentTool, TestRun, EfficiencyStatement) - cluster mode.
 * 
 * Test setup: <br/>
 * Expects a special "AssessmentTool" course. This must be imported. <br/>
 * It is also supposed that there is a learning group associated with this course which contains at least 
 * one student and one tutor. <br/>
 * 
 * Test case: <br/>
 * - Student runs a test (Test 1) in a special "AssessmentTool" course. <br/>
 * - Tutor opens the assessment tool of the same course for the specified student and checks whether he see the correct score. <br/>
 * - Tutor changes the score (3) and passed value (Yes) of the Assessment course element: "Bewertung 1".  <br/>
 * - Tutor checks the parent Structure node score (Struktur 1). <br/>
 * - Student runs the test (Test 1) second time and achieves 2 points.
 * - Tutor increases the score to 4. The student should see the latest score.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentTest extends BaseSeleneseTestCase {
	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
  private OLATWorkflowHelper workflow_A; //student on node 1
  private OLATWorkflowHelper workflow_B; //tutor on node 2
  	
  private final String COURSE_NAME = "AssessmentTool_selenium" +System.currentTimeMillis(); 
  private final String GROUP_NAME = "Gruppe 1";
  
  //test actors
  private String STUDENT; //student username
  private String TUTOR; // tutor username
  
	public void setUp() throws Exception { 
		System.out.println("AssessmentTest - setUp - STARTED");
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		//cleanup first - only if the course name is reused
		/*OlatServerSetupHelper.deleteEvidenceOfAchievement(COURSE_NAME, context.getStandardStudentOlatLoginInfos(1));
		OlatServerSetupHelper.deleteAllCoursesNamed(COURSE_NAME);*/
		
		TUTOR = context.getStandardAuthorOlatLoginInfos(2).getUsername();
		STUDENT = context.getStandardStudentOlatLoginInfos(1).getUsername();
												
    //import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
				
		//"administrator" adds tutor as owner of the "AssessmentTool"
		WorkflowHelper.addOwnerToLearningResource(TUTOR, COURSE_NAME);
		
    //"administrator" adds tutor to GROUP_NAME
		WorkflowHelper.addTutorToGroup(TUTOR, COURSE_NAME, GROUP_NAME);
		
		// STUDENT enrolls - login and enroll if not already member in GROUP_NAME		
		OLATWorkflowHelper workflow_D = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		CourseRun courseRun = workflow_D.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
		enrolmentRun.enrol(GROUP_NAME);
		assertTrue(enrolmentRun.isTextPresent("You have already enroled for the learning group mentioned below"));
		
		OLATWorkflowHelper authorWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));
		resetScoreCalculationRule(authorWorkflow, 10);
		
		System.out.println("AssessmentTest - setUp - ENDED");
	}
	
			
	public void testRunTest() throws Exception {	
		System.out.println("AssessmentTest - testRunTest - STARTED");
		Context context = Context.getContext();
				
		workflow_A = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		workflow_B = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));
		CourseRun courseRun_A = workflow_A.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		CourseRun courseRun_B = workflow_B.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		
    //	STUDENT runs the "Test 1" and gets a score equals with 1.
		courseRun_A.selectCourseElement("Struktur 1");
		TestRun testRun_A = courseRun_A.selectTest("Test 1").startTest();		
		testRun_A.selectMenuItem("Single Choice");
		testRun_A.setSingleChoiceSolution("Antwort 3");
		TestElement testElement_A = testRun_A.finishTest(true, 1); //updateEfficiencyStatement(1) 		
		assertEquals("1.000", testElement_A.getSelenium().getText("ui=qti::yourScore()"));
		
    //	tutor opens the assessment tool of the same course		
		AssessmentTool assessmetTool = courseRun_B.getAssessmentTool();	
		assertEquals("1.000",assessmetTool.getScoreInTableAsPerUser(STUDENT,"Test 1", false));
		AssessmentForm assessmentForm = assessmetTool.getAssessmentFormAsPerUser(STUDENT, "Bewertung 1");
		assessmentForm.setScore(3);
		assessmentForm.setPassed(Boolean.TRUE);
		assessmetTool = assessmentForm.save(); //updateEfficiencyStatement(2)
    //	assert score on "Struktur 1" to be the sum of the "Test 1" and "Bewertung 1" scores
		assertEquals("4.000", assessmetTool.getSelenium().getText("ui=course::assessment_scoreInTable(title=Struktur 1)"));
    //assert score on "Struktur 2" to be the half of sum of the "Test 1" and "Bewertung 1" scores
		assertEquals("2.000", assessmetTool.getSelenium().getText("ui=course::assessment_scoreInTable(title=Struktur 2)"));
								
		//student runs the test second time ("Test 1" and gets a score equals with 2)
		testRun_A = testElement_A.startTest();
		testRun_A.selectMenuItem("Single Choice");
		testRun_A.setSingleChoiceSolution("Antwort 3");
		testRun_A.selectMenuItem("Multiple Choice");
		String[] answers = {"Antwort 2", "Antwort 3"};
		testRun_A.setMultipleChoiceSolution(answers);
		testElement_A = testRun_A.finishTest(true, 2); //updateEfficiencyStatement(3)			
		assertEquals("2.000", testElement_A.getSelenium().getText("ui=qti::yourScore()"));
				
		//tutor opens the Assessment form for STUDENT, "Test 1" and verify the score		
		assertEquals("2.000", assessmetTool.getScoreInTableAsPerUser(STUDENT,"Test 1", true));
    //	verify score in AssessmentForm - select "Test 1"
		assessmentForm = assessmetTool.getAssessmentFormAsPerUser(STUDENT, "Test 1");
		assessmentForm.setScore(4);
		assessmentForm.setPassed(Boolean.TRUE);
		assessmetTool = assessmentForm.save();
    //assert score on "Struktur 1" to be the sum of the "Test 1" and "Bewertung 1" scores
		assertEquals("7.000", assessmetTool.getScoreInTableAsPerUser(STUDENT,"Struktur 1", false));
    //assert score on "Struktur 2" to be the half of sum of the "Test 1" and "Bewertung 1" scores
		assertEquals("3.500", assessmetTool.getScoreInTableAsPerUser(STUDENT,"Struktur 2", false));
				
    //A: close course
		courseRun_A.close(COURSE_NAME);
				
    //B: close course
		assessmetTool.close().close(COURSE_NAME);
						
		doTestRuleChange();						
								
		System.out.println("AssessmentTest - testRunTest - ENDED");
	}
	
	/**
	 * Tests the update of the scoreEvaluation (score/passed) in course for a student,
	 * and for a tutor in AssessmentTool after a change in the score calculation rule (CourseEditor/Score).
	 * This assumes that the passed cut value for this course is 10.
	 * The value will be next increased to 15 so the student should see the correct passed/failed information
	 * in her "Evidence of achievement".
	 */
	private void doTestRuleChange() throws Exception {
		System.out.println("AssessmentTest - doTestRuleChange - STARTED");				
		//student enters the "Evidence of achievement" - (score 10.5 and passed)
		EvidencesOfAchievement evidencesOfAchievement = workflow_A.getHome().getEvidencesOfAchievement();
		String passedStatus = evidencesOfAchievement.getCoursePassedStatus(COURSE_NAME);
		assertEquals("Passed",passedStatus);
    //	student enters course and checks her passed status
		CourseRun courseRun_A = evidencesOfAchievement.startCourse(COURSE_NAME);
		StructureElement root = courseRun_A.selectRoot(COURSE_NAME);		
		assertEquals("Passed", root.getPassedStatus());
		courseRun_A.close(COURSE_NAME);
		
    //	tutor changes the score calculation rule and publishes the course	
		resetScoreCalculationRule(workflow_B, 15);
		
    //	the student should get the updated (passed/failed) info 		
		evidencesOfAchievement = workflow_A.getHome().getEvidencesOfAchievement();
		assertEquals("Failed", evidencesOfAchievement.getCoursePassedStatus(COURSE_NAME));
		passedStatus = evidencesOfAchievement.startCourse(COURSE_NAME).selectRoot(COURSE_NAME).getPassedStatus();		
		assertEquals("Failed", passedStatus);
		
		workflow_A.logout();
		workflow_B.logout();
		
		System.out.println("AssessmentTest - doTestRuleChange - ENDED");
	}
		
	

	/**
	 * tutor resets the score calculation rule and publishes the course
	 *
	 */
	private void resetScoreCalculationRule(OLATWorkflowHelper authorWorkflow , int score) {		
		CourseEditor courseEditor = authorWorkflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
		StructureEditor structureEditor = (StructureEditor)courseEditor.selectCourseElement(COURSE_NAME);
		structureEditor.setMinimumScore(score);
		courseEditor.publishCourse();
		courseEditor.closeToCourseRun().close(COURSE_NAME);			
	}	
	
	
}
