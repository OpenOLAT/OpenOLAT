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

package org.olat.test.functional.codepoints.cluster;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

	/**	 
	 * Tests the Assessment (AssessmentTool, TestRun, EfficiencyStatement) - in cluster mode.
	 * <br/>
	 * 
	 * Test setup: <br/>
	 * Expects a special "AssessmentTool" course. 
	 * It is also supposed that there is a learning group associated with this course which contains at least 
	 * one student and one tutor.
	 * <br/>
	 * Test case: <br/>
	 * - Student runs a test (Test 1) in a special "AssessmentTool" course. <br/>
	 * - Tutor opens the assessment tool of the same course for the specified student and checks 
	 * whether he see the correct score. Tutor changes the score. <br/>
	 * - Student should see the correct score in course run as in "Evidence of achievement". <br/> 
	 * Details: <br/>
	 * Test the doInSync update of the EfficiencyStatement for the STUDENT for the "AssessmentTool" course.	 
	 * <br/>
	 * Setup: STUDENT runs a test "Test 1", score (2) is stored. 
	 * <br/>
	 * If useCodepoints is false: STUDENT stores score (1) and tutor score (7), in this order;
	 * else if useCodepoints is true: TUTOR stores score (7) and STUDENT stores score (1), in this order.
	 * 
	 * 
	 * @author Lavinia Dumitrescu
	 *
	 */
public class AssessmentWithCodepointsTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
	protected Selenium selenium_A; //student on node 1
    protected Selenium selenium_B; //tutor on node 2  
    protected Selenium selenium_D; //student on node 1
  
    private CodepointClient codepointClient_A;
    private CodepointClient codepointClient_B;
	
    private final String COURSE_NAME = "AssessmentTool_codepoints"+System.currentTimeMillis(); 
    private final String GROUP_NAME = "Gruppe 1";
  
    //test actors
    private String STUDENT; //student username
    private String TUTOR; // tutor username
  
	public void setUp() throws Exception { 
		System.out.println("AssessmentWithCodepointsTest - setUp - STARTED");
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		TUTOR = context.getStandardAuthorOlatLoginInfos(2).getUsername();
		STUDENT = context.getStandardStudentOlatLoginInfos(1).getUsername();
		
		//deleteEvidencesOfAchievement for this student
		WorkflowHelper.deleteEvidencesOfAchievement("AssessmentTool_codepoints", context.getStandardStudentOlatLoginInfos(1));
									
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
		selenium_D = context.createSeleniumAndLogin(context.getStandardStudentOlatLoginInfos(1));
		WorkflowHelper.openCourseAfterLogin(selenium_D, COURSE_NAME);
		enrollInGroupAfterLogin(selenium_D, COURSE_NAME, GROUP_NAME);		
			
		System.out.println("AssessmentWithCodepointsTest - setUp - ENDED");
	}
	
	/**
	 * STUDENT enrolls - login and enroll if not already member in groupName
	 * @param selenium_
	 * @param courseName
	 * @param groupName
	 */
	private void enrollInGroupAfterLogin(Selenium selenium_, String courseName, String groupName) {
		WorkflowHelper.openCourseAfterLogin(selenium_, courseName);
		selenium_.click("ui=course::menu_courseNode(titleOfNode=Enrolment)");
		selenium_.waitForPageToLoad("30000");
		boolean alreadyEnrolled = seleniumEquals("enrolled", selenium_.getText("ui=course::content_enrollment_enrolledOrNot(nameOfGroup=" + groupName + ")"));		
		if(!alreadyEnrolled) {
			selenium_.click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=" + groupName + ")");
			selenium_.waitForPageToLoad("30000");
		  assertTrue(selenium_.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
		selenium_.click("ui=tabs::logOut()");
		selenium_.waitForPageToLoad("30000");
	}
	
	
	/**
	 * Tests assessment tool. 
	 * Student runs a test (Test 1) in a special "AssessmentTool" course.
	 * Tutor opens the assessment tool of the same course for the specified student and checks
	 * whether he see the correct score. Tutor changes the score. 
	 * Student should see the correct score in course run as in "Evidence of achievement"	
	 * 
	 * @throws Exception
	 */
	public void testEfficiencyStatementUpdate() throws Exception {	
		System.out.println("AssessmentWithCodepointsTest - testEfficiencyStatementUpdate - STARTED");
						
		//uses codepoints to test the efficiencyStatement update
		boolean useCodepoints = true;
		doTestEfficiencyStatementUpdateWithCodepoints(useCodepoints);
				
		System.out.println("AssessmentWithCodepointsTest - testEfficiencyStatementUpdate - ENDED");
	}
		
		
	/**
	 * Test the doInSync update of the EfficiencyStatement for the STUDENT for the "AssessmentTool" course.
	 * Uses Codepoints. 
	 * <p>
	 * Setup: STUDENT runs a test "Test 1", score (2) is stored. 
	 * <p>
	 * If useCodepoints is false: STUDENT stores score (1) and tutor score (7), in this order.
	 * else if useCodepoints is true: TUTOR stores score (7) and STUDENT stores score (1), in this order
	 *  
	 * @param useCodepoints
	 * @throws Exception
	 */
	private void doTestEfficiencyStatementUpdateWithCodepoints(boolean useCodepoints) throws Exception {
		System.out.println("AssessmentWithCodepointsTest - doTestEfficiencyStatementUpdateWithCodepoints - STARTED - useCodepoints: " + useCodepoints);
		Context context = Context.getContext();
		selenium_A = context.createSeleniumAndLogin(context.getStandardStudentOlatLoginInfos(1));
				
		WorkflowHelper.openCourseAfterLogin(selenium_A, COURSE_NAME);
		//setup: student runs the test first time ("Test 1" and gets a score equals with 2) 
		selenium_A.click("ui=course::menu_courseNode(titleOfNode=Struktur 1)");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=course::menu_courseNode(titleOfNode=Test 1)");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=commons::start()");
		selenium_A.waitForPageToLoad("30000");
		//select item, choose answer and submit answer
		selenium_A.click("ui=qti::menuItem(titleOfItem=Single Choice)");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=qti::testItemFormElement(text=Antwort 3)");
		selenium_A.click("ui=qti::saveAnswer()"); 
		selenium_A.waitForPageToLoad("30000");
        //select item, choose answer and submit answer
		selenium_A.click("ui=qti::menuItem(titleOfItem=Multiple Choice)");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=qti::testItemFormElement(text=Antwort 2)");
		selenium_A.click("ui=qti::testItemFormElement(text=Antwort 3)");
		selenium_A.click("ui=qti::saveAnswer()");
		selenium_A.waitForPageToLoad("30000");
		//submit test
		selenium_A.click("ui=qti::finishTest()");
		selenium_A.waitForPageToLoad("30000");
		boolean confirmedSubmit = selenium_A.getConfirmation().matches("^Do you really want to submit[\\s\\S]$");
		
		assertTrue(confirmedSubmit);
  	
		
		assertEquals("2", selenium_A.getText("ui=qti::achievedScore()"));
		selenium_A.click("ui=qti::closeTest()"); //updateEfficiencyStatement(3)
		selenium_A.waitForPageToLoad("30000");
		assertEquals("2.000", selenium_A.getText("ui=qti::yourScore()"));
		//end setup - student achieved a score of 2.000
		
		codepointClient_A = Context.getContext().createCodepointClient(1);
		CodepointRef beforeSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.assessment.NewCachePersistingAssessmentManager.beforeSyncUpdateUserEfficiencyStatement");
		beforeSyncCp_A.setHitCount(0);
		if(useCodepoints)
		  beforeSyncCp_A.enableBreakpoint();
		
		CodepointRef doInSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.assessment.NewCachePersistingAssessmentManager.doInSyncUpdateUserEfficiencyStatement");
		doInSyncCp_A.setHitCount(0);
		if(useCodepoints)
		  doInSyncCp_A.enableBreakpoint();
		
		codepointClient_B = Context.getContext().createCodepointClient(2);
		CodepointRef doInSyncCp_B = codepointClient_B.getCodepoint("org.olat.course.assessment.NewCachePersistingAssessmentManager.doInSyncUpdateUserEfficiencyStatement");
		doInSyncCp_B.setHitCount(0);
		if(useCodepoints)
		  doInSyncCp_B.enableBreakpoint();
		
		CodepointRef afterSyncCp_B = codepointClient_B.getCodepoint("org.olat.course.assessment.NewCachePersistingAssessmentManager.afterSyncUpdateUserEfficiencyStatement");
		afterSyncCp_B.setHitCount(0);
		if(useCodepoints)
		  afterSyncCp_B.enableBreakpoint();
						
		selenium_B = context.createSeleniumAndLogin(context.getStandardAuthorOlatLoginInfos(2));
		WorkflowHelper.openCourseAfterLogin(selenium_B, COURSE_NAME);
		
		//STUDENT runs the "Test 1" and gets a score equals with 1.		
		selenium_A.click("ui=commons::start()");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=qti::menuItem(titleOfItem=Single Choice)");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=qti::testItemFormElement(text=Antwort 3)");
		selenium_A.click("ui=qti::saveAnswer()");
		selenium_A.waitForPageToLoad("30000");
		selenium_A.click("ui=qti::finishTest()");		
		assertTrue(selenium_A.getConfirmation().matches("^Do you really want to submit[\\s\\S]$"));
		
    //A: check if codepoint reached: beforeSyncCp_A
		if(useCodepoints) {
		  beforeSyncCp_A.assertBreakpointReached(1, 20000);		
		  System.out.println("beforeSyncCp_A.assertBreakpointReached");
		}
						
		//tutor opens the assessment tool of the same course and overwrites the score of "Test 1"	with the value 7	
		selenium_B.click("ui=course::toolbox_courseTools_assessmentTool()");
		selenium_B.waitForPageToLoad("30000");
		selenium_B.click("ui=course::assessment_selectType(text=As per user)");
		selenium_B.waitForPageToLoad("30000");
		selenium_B.click("ui=course::assessment_selectUser(username=" + STUDENT + ")");
		selenium_B.waitForPageToLoad("10000");
		if(useCodepoints) {
			//student last score was not stored yet
			assertEquals("2.000", selenium_B.getText("ui=course::assessment_scoreInTable(title=Test 1)"));
		} else {
		  assertEquals("1.000", selenium_B.getText("ui=course::assessment_scoreInTable(title=Test 1)"));
		}
		//tutor selects "Test 1" and change its score and passed info
		selenium_B.click("ui=course::assessment_selectAssessmentCourseNode(title=Test 1)");
		selenium_B.waitForPageToLoad("30000");		
		selenium_B.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Score)", "7");
		selenium_B.click("ui=course::assessment_setPassedYes()");
		selenium_B.click("ui=commons::save()"); //updateEfficiencyStatement(2)
		// B: check if codepoint reached: doInSyncCp_B, and continue A thread 
		//this means that tutor's score is first stored and will be overwritten by the students's score
		if(useCodepoints) {
		  doInSyncCp_B.assertBreakpointReached(1, 20000);
		  System.out.println("doInSyncCp_B.assertBreakpointReached");
		  TemporaryPausedThread[] threadsA = beforeSyncCp_A.getPausedThreads();
		  threadsA[0].continueThread();
		  System.out.println("beforeSyncCp_A continue threads");
		
		  //B: continue thread
		  doInSyncCp_A.assertBreakpointNotReached(20000);
		  System.out.println("doInSyncCp_A.assertBreakpointNotReached");
		  TemporaryPausedThread[] threadsB = doInSyncCp_B.getPausedThreads();
		  threadsB[0].continueThread();
		  System.out.println("doInSyncCp_B continue threads");
		
		  //B: leave the doInSync block
		  afterSyncCp_B.assertBreakpointReached(1, 40000);
		  doInSyncCp_A.assertBreakpointReached(1, 40000);
		  threadsA = doInSyncCp_A.getPausedThreads();
		  threadsA[0].continueThread();
		  System.out.println("doInSyncCp_A continue threads");
		  threadsB = afterSyncCp_B.getPausedThreads();
		  threadsB[0].continueThread();
		  System.out.println("afterSyncCp_B continue threads");
		}
		selenium_B.waitForPageToLoad("30000");
		//what tutor sees
		/*if(useCodepoints) {
			//assert score on "Struktur 1" to be the sum of the "Test 1" and "Bewertung 1" scores
		  assertEquals("1.000", selenium_B.getText("ui=course::assessment_scoreInTable(title=Struktur 1)"));
      //assert score on "Struktur 2" to be the half of sum of the "Test 1" and "Bewertung 1" scores
		  assertEquals("0.500", selenium_B.getText("ui=course::assessment_scoreInTable(title=Struktur 2)"));
		} else {*/
		  //assert score on "Struktur 1" to be the sum of the "Test 1" and "Bewertung 1" scores
		  assertEquals("7.000", selenium_B.getText("ui=course::assessment_scoreInTable(title=Struktur 1)"));
      //assert score on "Struktur 2" to be the half of sum of the "Test 1" and "Bewertung 1" scores
		  assertEquals("3.500", selenium_B.getText("ui=course::assessment_scoreInTable(title=Struktur 2)"));
		//}
				
		//what student sees
		selenium_A.waitForPageToLoad("30000");
		assertEquals("1", selenium_A.getText("ui=qti::achievedScore()"));
		
		selenium_A.click("ui=qti::closeTest()"); //updateEfficiencyStatement(1) 		
    selenium_A.waitForPageToLoad("30000");
    if(useCodepoints) {
		  assertEquals("1.000", selenium_A.getText("ui=qti::yourScore()"));
    } else {
    	assertEquals("7.000", selenium_A.getText("ui=qti::yourScore()"));
    }
    
    // student checks "Evidence of achievement" in HOME - show details		
    selenium_A.click("ui=tabs::home()");
    selenium_A.waitForPageToLoad("30000");
    selenium_A.click("ui=home::menu_evidencesOfAchievement()");
    selenium_A.waitForPageToLoad("30000");
    selenium_A.click("ui=home::content_evidencesOfAchievement_selectDetails(title=" + COURSE_NAME + ")");	
		Thread.sleep(10000);		
		selenium_A.selectWindow(selenium_A.getAllWindowTitles()[2]); 		
		assertTrue(selenium_A.isTextPresent("Evidence of achievement"));
	  //check score in table	
		assertEquals("1.000", selenium_A.getText("ui=course::assessment_scoreInTable(title=Test 1)"));
		
		System.out.println("AssessmentWithCodepointsTest - doTestEfficiencyStatementUpdateWithCodepoints - ENDED");
	}


	@Override
	public void cleanUpAfterRun() {		
		System.out.println("AssessmentWithCodepointsTest - cleanUpAfterRun - STARTED");
		//resetScoreCalculationRule();
		WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
		System.out.println("AssessmentWithCodepointsTest - cleanUpAfterRun - ENDED");
	}
	
}
