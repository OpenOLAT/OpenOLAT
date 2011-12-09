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
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.qti.FIBQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Change test in test editor, part of test suite TestEditorCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. author edits test TEST_NAME<br/>
 * 2. author checks if only section and only item are not deletable. <br/>
 * 3. Author copies question, deletes question.  <br/>
 * 4. Author saves test.  <br/>
 * 
 * @author sandra
 * 
 */

public class ChangeTstInEditor extends BaseSeleneseTestCase {

	@Test(dependsOnGroups = {TestEditorCombiTest.FIRST}, groups = {TestEditorCombiTest.SECOND})
	public void testChangeTstInEditor() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
			//author starts test from before
			OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
			LearningResources learningResources = olatWorkflow.getLearningResources();
	  		LRDetailedView lRDetailedView = learningResources.searchMyResource(TestEditorCombiTest.TEST_NAME);
			//edit Test
			TestEditor testEditor = lRDetailedView.editTestContent();
			// gets info of not having saved
	  		SeleniumHelper.waitUntilTextPresent(lRDetailedView.getSelenium(), "The test/questionnaire was not saved to learning resources during the last session.", 20);
			// author sets passing score, tries to delete an only section, adds section and tries to delete only item
			testEditor.setNecessaryPassingScore(4.0);
			testEditor.selectSection("Test section");			
			testEditor.deleteUndeleteable(true);
						
			testEditor.addSection("Second section");
			testEditor.selectQuestion("New question");
			testEditor.deleteUndeleteable(false);			
			
			//author copies and deletes items
			testEditor.selectSection("Test section");
			testEditor.selectQuestion("Gap Text Question");
			QuestionEditor questionEditor = testEditor.copyCurrentQuestion("Gap Text Question");
			questionEditor.setQuestionTitle("Second Gap");
			FIBQuestionEditor fIBQuestionEditor =(FIBQuestionEditor)testEditor.selectQuestion("Second Gap");
			questionEditor.selectQuestionAndAnswersTab();
			fIBQuestionEditor.changeCapitalization(2);
			testEditor.selectSection("Second section");
			testEditor.selectQuestion("New question");
			testEditor.deleteCurrentNode(true);
			testEditor.isTextPresent("New question");
			
			testEditor.close(); 
			
		}
	}

