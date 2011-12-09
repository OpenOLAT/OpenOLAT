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
*/
package org.olat.test.functional.test;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.TestElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests attempts settings of a test integrated in a course. 
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Author creates test TEST_NAME and integrates it in course COURSE_NAME. <br/>
 * 2. First configuration: only one attempt. <br/>
 * 3. Check if student can solve test only once. <br/>
 * 4. Second configuration: Authors changes assessment form of student and sets attempts to zero. <br/>
 * 5. Student can solve test once again. <br/>
 * 6. Third configuration: Change attempts config in course editor to 2 attempts. <br/>
 * 7. Check if user gets message to relaunch course and that he can solve test again.<br/>
 * 
 * 
 * @author sandra
 * 
 */

public class CheckTestAttempts extends BaseSeleneseTestCase {
	
	private final String TEST_NAME = "CheckTestAttemptsTest_" + System.currentTimeMillis();
	private final String TEST_DESCRIPTION = "CheckTestAttemptsTestDesc_" + System.currentTimeMillis();
	private final String COURSE_NAME = "CheckTestAttemptsCourseDesc_" + System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CheckTestAttemptsCourse_"+System.currentTimeMillis();
	private final String GROUP_NAME = "AttemptsGroup";
	private final String TEST_NODE_NAME = "Test1";

	public void testIntegrateTestAndCheckAttempts() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		// author creates simple test
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		TestEditor testEditor = olatWorkflow.getLearningResources().createTestAndStartEditing(TEST_NAME, TEST_DESCRIPTION);
		testEditor.setNecessaryPassingScore(1.0);
		testEditor.close(); 
 
		// author creates course, adds test, configures test with one attempt, publish
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		TestElementEditor testElementEditor = (TestElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TEST, true,TEST_NODE_NAME);			
		testElementEditor.chooseMyFile(TEST_NAME);
		testElementEditor.configureTestLayout(null, null, false, false, 1, true);
		courseEditor.publishCourse();
		LRDetailedView lRDetailedView1 = courseEditor.closeToLRDetailedView();
		CourseRun courseRunAuthor = lRDetailedView1.showCourseContent();
		
		//author creates learning group and adds student
		courseRunAuthor.getGroupManagement().createGroupAndAddMembers(GROUP_NAME, context.getStandardAuthorOlatLoginInfos().getUsername(), context.getStandardStudentOlatLoginInfos().getUsername());
	
		// student starts course
		OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos());
		
		LearningResources learningResources = olatWorkflow1.getLearningResources();
  		CourseRun courseRunStudent = learningResources.searchAndShowCourseContent(COURSE_NAME);
	
  	    // student starts and finishes test 
		TestElement testElement = courseRunStudent.selectTest(TEST_NODE_NAME);
		TestRun testRun = testElement.startTest();		
		testElement = testRun.finishTest(true, 0);
        
		//select course element again, check if he cannot start test anymore
		testElement = courseRunStudent.selectTest(TEST_NODE_NAME);
		assertEquals("0.000", testElement.getAchievedScore());
		assertEquals("Failed", testElement.getStatus());
		assertTrue(testElement.isShowResultsPresent());
		assertTrue(testElement.cannotStartTestAnymore());
		
        //author opens assessment tool and sets attempt for student to zero 
		AssessmentTool assessmentTool = courseRunAuthor.getAssessmentTool();
		AssessmentForm assessmentForm = assessmentTool.getAssessmentFormAsPerUser(context.getStandardStudentOlatLoginInfos().getUsername(),TEST_NODE_NAME);
		assessmentForm.setAttempts(0);
		assessmentForm.save();
		assessmentForm.close();
		
	    //check if student can solve test for the second time now 
		testElement = courseRunStudent.selectTest(TEST_NODE_NAME);
		TestRun testRun2 = testElement.startTest();
		testElement = testRun2.finishTest(true, 0);
		testElement = courseRunStudent.selectTest(TEST_NODE_NAME);
		assertEquals("0.000", testElement.getAchievedScore());
		assertEquals("Failed", testElement.getStatus());
		
		//check that student cannot solve test any more
		assertTrue(testElement.cannotStartTestAnymore());
		
	    //author opens course editor, sets max. number of attempts to 2 and publishes
		CourseEditor courseEditor2 = courseRunAuthor.getCourseEditor();
		TestElementEditor testElementEditor2 = (TestElementEditor)courseEditor2.selectCourseElement(TEST_NODE_NAME);
		testElementEditor2.configureTestLayout(null, null, false, false, 2, true);
		courseEditor2.publishCourse();
		
		Thread.sleep(10000);
	    //student should restart course after publish and can solve test once more.
		courseRunStudent.getDisposedCourseRun().closeCourseAndRestart();
		testElement = courseRunStudent.selectTest(TEST_NODE_NAME);
		TestRun testRun3 = testElement.startTest();
		testElement = testRun3.finishTest(true, 0);
		assertEquals("0.000", testElement.getAchievedScore());
		assertEquals("Failed", testElement.getStatus());
		assertTrue(testElement.cannotStartTestAnymore());
		
		}
	}

