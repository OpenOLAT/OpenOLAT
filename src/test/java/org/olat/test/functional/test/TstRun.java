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

import java.util.HashMap;
import java.util.Map;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Test how student solves test in test run, part of test suite TestEditorCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Student starts course COURSE_NAME.<br/>
 * 2. Student starts course element self test and check if configuration of IntegrateTstInCourse.java is effective.<br/>
 * 3. Student solves self test and checks if actual score is updated according to correct solutions. <br/>
 * 4. Student checks if he passed test and if setting of results layout is according to configuration. <br/>
 * 5. Student starts course element test and check if configuration of IntegrateTstInCourse.java is effective.<br/>
 * 6. Student solves test for the first time and checks if actual score is updated according to correct solutions. <br/>
 * 7. Student checks if he failed the test and if setting of results layout is according to configuration. <br/>
 * 8. Student starts test for the second time and finishes without saving any results.<br/>
 * 9. Student tries to start test for the third time but cannot as max. nr of attempts is 2.  <br/>
 * 10. Author deletes course and test. <br/>
 * 
 * @author sandra
 * 
 */

public class TstRun extends BaseSeleneseTestCase {

	/**
	 * <p>
	 * This test is part of test suite TestEditorCombiTest.java. Student solves
	 * self-test and test. Check the configurations made in TestCase before.
	 * Author deletes course and test.
	 * </p>
	 * 
	 * @throws Exception
	 */
	@Test(dependsOnGroups = {TestEditorCombiTest.THIRD})
	public void testTestRun() throws Exception {
			Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
			// student logs in and starts course
			OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos());
			LearningResources learningResources = olatWorkflow.getLearningResources();
	  		CourseRun courseRun = learningResources.searchAndShowCourseContent(TestEditorCombiTest.COURSE_NAME);
		
			// student starts self-test and checks configuration
			TestElement testElement = courseRun.selectTest("SelfTest");
			TestRun testRun = testElement.startTest();
			// student solves self-test
			testRun.isCancelPresent();
			testRun.isSuspendPresent();
			testRun.next();
			assertTrue(testRun.isTextPresent("Second section"));
			testRun.next();
			assertTrue(testRun.isTextPresent("Second Gap"));
			testRun.fillInGap("Name of Kristinas boy:", "nils");			
			assertTrue(testRun.isTextPresent("Actual score: 1 / 5"));
			testRun.next();
						
			assertTrue(testRun.isTextPresent("Gap Text Question"));
			testRun.fillInGap("Name of Kristinas boy:", "nils");
			assertTrue(testRun.isTextPresent("Actual score: 1 / 5"));
			
			assertTrue(testRun.isTextPresent("Kprim Question"));			
			Map<String, Boolean> answerToCorrectMap = new HashMap<String, Boolean>();
			answerToCorrectMap.put("Princess Tarta",true);
			answerToCorrectMap.put("Spekemat", false);
			answerToCorrectMap.put("Klipfisk", false);
			answerToCorrectMap.put("Koetbullar", true);
			testRun.setKprimSolution(answerToCorrectMap);			
			assertTrue(testRun.isTextPresent("Actual score: 2 / 5"));
			
			assertTrue(testRun.isTextPresent("Multiple Choice Question"));				
			String[] answers = {"Nussbrötli","Latte Macchiato"};
			testRun.setMultipleChoiceSolution(answers);
			assertTrue(testRun.isTextPresent("Actual score: 3 / 5"));
			
			assertTrue(testRun.isTextPresent("Single Choice Question"));			
			testRun.setSingleChoiceSolution("girl");
			assertTrue(testRun.isTextPresent("Actual score: 4 / 5"));
			
			TestElement testElement2 = testRun.selfTestFinishedConfirm();
			//select course element again, else could not see the changes
			testElement2 = courseRun.selectTest("SelfTest");
			assertEquals("4.000", testElement2.getAchievedScore());
			assertEquals("Passed",testElement2.getStatus());
			assertFalse(testElement2.isShowResultsPresent());
			
						
			// student starts test and checks configuration
			testElement = courseRun.selectTest("Test");
			TestRun testRun2 = testElement.startTest();
			testRun2.isCancelPresent();
			testRun2.isSuspendPresent();
			
			// student solves test for the first time
			testRun2.selectMenuItem("Second Gap");
			testRun2.fillInGap("Name of Kristinas boy:", "nils");
			testRun2.selectMenuItem("Gap Text Question");
			testRun2.fillInGap("Name of Kristinas boy:", "nils");
			testRun2.selectMenuItem("Kprim Question");
			Map<String, Boolean> answerToCorrectMap2 = new HashMap<String, Boolean>();
			answerToCorrectMap.put("Princess Tarta",true);
			answerToCorrectMap.put("Spekemat", false);
			answerToCorrectMap.put("Klipfisk", false);
			answerToCorrectMap.put("Koetbullar", true);
			testRun2.setKprimSolution(answerToCorrectMap2);
			testRun2.selectMenuItem("Multiple Choice Question");
			String[] answers2 = {"Nussbrötli","Latte Macchiato"};
			testRun2.setMultipleChoiceSolution(answers2);
			testRun2.selectMenuItem("Single Choice Question");
			testRun2.setSingleChoiceSolution("girl");			
			TestElement testElement3 = testRun2.finishTest(true, 3);
      //select course element again, else could not see the changes
			testElement3 = courseRun.selectTest("Test");
						
			assertEquals("3.000", testElement3.getAchievedScore());
			assertEquals("Failed", testElement3.getStatus());
			assertTrue(testElement3.isShowResultsPresent());
			
			
			// student solves test for the second time
			testElement = courseRun.selectTest("Test");
			TestRun testRun3 = testElement.startTest();
			TestElement testElement4 = testRun3.finishTest(true, 0);
			testElement4 = courseRun.selectTest("Test");
			assertEquals("0.000", testElement4.getAchievedScore());
			assertEquals("Failed", testElement4.getStatus());
					
		
			// student tries to solve test for the third time
			TestElement testElement5 = courseRun.selectTest("Test");
			testElement5 = courseRun.selectTest("Test");
			assertTrue(testElement5.cannotStartTestAnymore());
			assertTrue(testElement5.isTextPresent("There are no more attempts at your disposal."));
			
		
			// author deletes course and test
			OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
			LRDetailedView lRDetailedView = olatWorkflow2.getLearningResources().searchMyResource(TestEditorCombiTest.COURSE_NAME);
			try {
				lRDetailedView.deleteLR();
			} catch (Exception e) {} 
			lRDetailedView = olatWorkflow2.getLearningResources().searchMyResource(TestEditorCombiTest.TEST_NAME);
			try {
				lRDetailedView.deleteLR();
			} catch (Exception e) {}
				
	}
}
