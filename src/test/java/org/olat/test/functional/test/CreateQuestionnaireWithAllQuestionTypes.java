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
import org.olat.test.util.selenium.olatapi.qti.EssayQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.FIBQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.MCQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionnaireEditor;
import org.olat.test.util.selenium.olatapi.qti.SCQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.SectionEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor.QUESTION_TYPES;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**
 * 
 * Author creates questionnaire with all question types, part of test suite CombiQuestionnaireTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Author creates questionnaire QUESTIONNAIRE_NAME and starts editing. <br/>
 * 2. Author adds all four possible question types. <br/>
 * 3. Author edits all question titles and adds answers options for SC and MC. <br/>
 * 5. Author closes questionnaire. <br/>
 * 
 * @author sandra
 * 
 */
public class CreateQuestionnaireWithAllQuestionTypes extends BaseSeleneseTestCase {
	
	
	
	public void testCreateQuestionnaireCheckAttempts() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		//author creates questionnaire
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));		
		QuestionnaireEditor questionnaireEditor = olatWorkflowHelper.getLearningResources().createQuestionnaireAndStartEditing(CombiQuestionnaireTest.QUESTIONNAIRE_NAME, CombiQuestionnaireTest.QUESTIONNAIRE_DESCRIPTION);
				
		// adding all question types
		questionnaireEditor.addQuestion(QUESTION_TYPES.MULTIPLE_CHOICE,"Multiple Choice Question");
		questionnaireEditor.addQuestion(QUESTION_TYPES.ESSAY,"Essay Question");
		questionnaireEditor.addQuestion(QUESTION_TYPES.GAP_TEXT,"Gap Text Question");
			
		// editing question and answers 
		SectionEditor sectionEditor = questionnaireEditor.selectSection("New section");
		sectionEditor.setSectionTitle("New section", "Questionnaire section"); 

		// edit single choice question
		SCQuestionEditor scQuestionEditor = (SCQuestionEditor)questionnaireEditor.selectQuestion("New question");
		scQuestionEditor.setQuestionTitle("Single Choice Question");			
		scQuestionEditor.selectQuestionAndAnswersTab();
		scQuestionEditor.editQuestion("Which do you think is the most famous tourist attraction in Hong Kong?");
		scQuestionEditor.editAnswer("Harbour Junk Trip", 1);
		scQuestionEditor.addNewAnswer();
		scQuestionEditor.editAnswer("Victoria Peak", 2);
		scQuestionEditor.addNewAnswer();
		scQuestionEditor.editAnswer("Disney Land", 3);
			
		// edit multiple choice question
		MCQuestionEditor mcQuestionEditor = (MCQuestionEditor)questionnaireEditor.selectQuestion("Multiple Choice Question");
		mcQuestionEditor.selectQuestionAndAnswersTab();
		mcQuestionEditor.editQuestion("Which Dim Sum specialities do you like?");
		mcQuestionEditor.editAnswer("Shrimp dumplings", 1);
		mcQuestionEditor.addNewAnswer();
		mcQuestionEditor.editAnswer("Chicken feet", 2);
		mcQuestionEditor.addNewAnswer();
		mcQuestionEditor.editAnswer("Rice rolls", 3);
		mcQuestionEditor.addNewAnswer();
		mcQuestionEditor.editAnswer("Rice pudding", 4);	
			
		// edit gap text	
		FIBQuestionEditor fIBQuestionEditor = (FIBQuestionEditor)questionnaireEditor.selectQuestion("Gap Text Question");
		fIBQuestionEditor.selectQuestionAndAnswersTab();
		fIBQuestionEditor.editTextFragment(1,"Which skyscraper do you like most?");
		fIBQuestionEditor.addNewBlank();
			
		// edit essay question			
		EssayQuestionEditor essayQuestionEditor = (EssayQuestionEditor)questionnaireEditor.selectQuestion("Essay Question");
		essayQuestionEditor.selectQuestionAndAnswersTab();
		essayQuestionEditor.editQuestion("Write about your impression of the 'Symphony of Lights'" );
			
		questionnaireEditor.close();
		
  }
}
