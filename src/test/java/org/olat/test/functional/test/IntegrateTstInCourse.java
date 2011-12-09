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
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.SelfTestElementEditor;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.TestElementEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Integrate test in course editor, part of test suite TestEditorCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. author creates course COURSE_NAME<br/>
 * 2. author adds self test course element, selects test from CreateTstInEditor.java<br/>
 * 3. author configures self test layout options: do not allow menu navigation, do not show menu navigation, 
 * allow cancel, allow suspend <br/>
 * 4. author adds test course element, selects test from CreateTstInEditor.java <br/>
 * 5. author configures test layout options: allow menu navigation default (=true?), show menu navigation default 
 * (=true?), do not allow cancel, do not allow suspend, maximum nr of approaches is 2, do display results on 
 * test starting page <br/>
 * 6. author publishes course <br/>
 * 
 * @author sandra
 * 
 */

public class IntegrateTstInCourse extends BaseSeleneseTestCase {

	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	@Test(dependsOnGroups = {TestEditorCombiTest.SECOND}, groups = {TestEditorCombiTest.THIRD})
	public void testIntegrateTestInCourse() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);

		// author creates course
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(TestEditorCombiTest.COURSE_NAME, COURSE_DESCRIPTION);

		// author adds self-test and configures 
		SelfTestElementEditor selfTestElementEditor = (SelfTestElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.SELF_TEST, true, "SelfTest");
		selfTestElementEditor.chooseMyFile(TestEditorCombiTest.TEST_NAME);
		selfTestElementEditor.configureSelfTestLayout(false, false, true, true);

		//author adds test and configures 
		TestElementEditor testElementEditor = (TestElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TEST, true, "Test");
		testElementEditor.chooseMyFile(TestEditorCombiTest.TEST_NAME);
		testElementEditor.configureTestLayout(null, null, false, false, 2, true);

		courseEditor.publishCourse();

	}
}
