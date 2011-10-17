package org.olat.test.functional.test;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.qti.FIBQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.KPrimQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.MCQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.SCQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.SectionEditor;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor.QUESTION_TYPES;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Create test in test editor, part of test suite TestEditorCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. author creates test TEST_NAME. <br/>
 * 2. author adds questions of the types MC, Kprim and gap text. <br/>
 * 3. author edits question titles and answers and sets correct answers. <br/>
 * 
 * @author sandra
 * 
 */

public class CreateTstInEditor extends BaseSeleneseTestCase {
	private final String TEST_DESCRIPTION = "TestDescription";
	
	@Test(groups = {TestEditorCombiTest.FIRST})
	public void testCreateTestInEditor() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
				
		// author creates test
			OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
			TestEditor testEditor = olatWorkflow.getLearningResources().createTestAndStartEditing(TestEditorCombiTest.TEST_NAME, TEST_DESCRIPTION);
		// author adds question types
			testEditor.addQuestion(QUESTION_TYPES.MULTIPLE_CHOICE,"Multiple Choice Question");
			testEditor.addQuestion(QUESTION_TYPES.KPRIM,"Kprim Question");
			testEditor.addQuestion(QUESTION_TYPES.GAP_TEXT,"Gap Text Question");
			
		// author edits answers and sets correct
			SectionEditor sectionEditor = testEditor.selectSection("New section");
			sectionEditor.setSectionTitle("New section", "Test section"); 
			SCQuestionEditor scQuestionEditor = (SCQuestionEditor)testEditor.selectQuestion("New question");
			scQuestionEditor.setQuestionTitle("Single Choice Question");			
			scQuestionEditor.selectQuestionAndAnswersTab();
			scQuestionEditor.editQuestion("Is Kristina expecting a boy or a girl?");
			scQuestionEditor.editAnswer("boy", 1);
			scQuestionEditor.addNewAnswer();
			scQuestionEditor.editAnswer("girl", 2);
			scQuestionEditor.setSingleChoiceSolution(2);
		// multiple choice 
			MCQuestionEditor mcQuestionEditor = (MCQuestionEditor)testEditor.selectQuestion("Multiple Choice Question");
			mcQuestionEditor.selectQuestionAndAnswersTab();
			mcQuestionEditor.editQuestion("What does Kristina like for breakfast?");
			mcQuestionEditor.editAnswer("Nussbrötli", 1);
			mcQuestionEditor.addNewAnswer();
			mcQuestionEditor.editAnswer("Gipfeli", 2);
			mcQuestionEditor.addNewAnswer();
			mcQuestionEditor.editAnswer("doppelter Espresso", 3);
			mcQuestionEditor.addNewAnswer();
			mcQuestionEditor.editAnswer("Latte Macchiato", 4);			
			mcQuestionEditor.setMultipleChoiceSolution(4);
			
		// Kprim question				
			KPrimQuestionEditor kprimQuestionEditor = (KPrimQuestionEditor)testEditor.selectQuestion("Kprim Question");
			kprimQuestionEditor.selectQuestionAndAnswersTab();
			kprimQuestionEditor.editQuestion("Which specialities are from Sweden?");
			kprimQuestionEditor.editAnswer("Princess Tarta", 1);
			kprimQuestionEditor.editAnswer("Spekemat", 2);
			kprimQuestionEditor.editAnswer("Klipfisk", 3);
			kprimQuestionEditor.editAnswer("Koetbullar", 4);
			kprimQuestionEditor.setCorrectKprimSolution(true, false, false, true);
			
		// Gap text	
			FIBQuestionEditor fIBQuestionEditor = (FIBQuestionEditor)testEditor.selectQuestion("Gap Text Question");
			fIBQuestionEditor.selectQuestionAndAnswersTab();
			fIBQuestionEditor.editTextFragment(1,"Name of Kristinas boy:");
			fIBQuestionEditor.addNewBlank();
			fIBQuestionEditor.setBlankSolution("Nils", 2);
			
  }
}
