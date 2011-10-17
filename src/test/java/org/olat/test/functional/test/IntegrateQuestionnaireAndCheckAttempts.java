package org.olat.test.functional.test;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.QuestionnaireElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.QuestionnaireElement;
import org.olat.test.util.selenium.olatapi.course.run.QuestionnaireRun;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**
 * 
 * Author adds the questionnaire element to a course and tests the different configurations in the questionnaire run, part of test suite CombiQuestionnaireTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Author creates course COURSE_NAME and starts editing. <br/>
 * 2. Author adds questionnaire course element and adds questionnaire QUESTIONNAIRE_NAME. <br/>
 * 3. Author sets configuration options: allow menu navigation, show menu navigation, allow cancel, 
 * do not allow suspend.  <br/>
 * 5. Author publishes course. <br/>
 * 6. Student opens course COURSE_NAME and starts questionnaire QUESTIONNAIRE_NAME and checks if the above 
 * configuration options are effective. <br/>
 * 7. Student edits answers and finishes questionnaire.<br/>
 * 8. Student tries to start questionnaire for the second time, check if message that questionnaire was 
 * already solved is shown. <br/>
 * 
 * @author sandra
 * 
 */
public class IntegrateQuestionnaireAndCheckAttempts extends BaseSeleneseTestCase {
	


	
	public void testCreateQuestionnaireCheckAttempts() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

		// author creates course
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(CombiQuestionnaireTest.COURSE_NAME, CombiQuestionnaireTest.COURSE_DESCRIPTION);;

		// author adds questionnaire 
		QuestionnaireElementEditor questionnaireElementEditor = (QuestionnaireElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.QUESTIONNAIRE, true, "Questionnaire");
		String questionnaireName = CombiQuestionnaireTest.QUESTIONNAIRE_NAME;
		questionnaireElementEditor.chooseMyFile(questionnaireName);
		
		// author configures questionnaire: cancel: ok, suspend: nok and publishes
		questionnaireElementEditor.configureQuestionnaireLayout(true, true, true, false);
		courseEditor.publishCourse();
			
		//student starts course
		OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		LearningResources learningResources = olatWorkflow2.getLearningResources();
		String courseName = CombiQuestionnaireTest.COURSE_NAME;
	  	CourseRun courseRun = learningResources.searchAndShowCourseContent(courseName);
	  		
	  	// student starts questionnaire  and checks configuration
		QuestionnaireElement questionnaireElement = courseRun.selectQuestionnaire("Questionnaire");
		QuestionnaireRun questionnaireRun = questionnaireElement.start();
		assertTrue("Assert that a cancel questionnaire button is present", questionnaireRun.isCancelPresent());
		assertFalse("Assert that a suspend questionnaire button is present", questionnaireRun.isSuspendPresent());
			
		// student solves questionnaire
		assertTrue(questionnaireRun.isTextPresent("Questionnaire section"));
		questionnaireRun.selectMenuItem("Single Choice Question");
		questionnaireRun.setSingleChoiceSolution("Victoria Peak");
			
		questionnaireRun.selectMenuItem("Multiple Choice Question");
		String[] answers = {"Shrimp dumplings","Rice pudding"};
		questionnaireRun.setMultipleChoiceSolution(answers);
			
		questionnaireRun.selectMenuItem("Gap Text Question");
		questionnaireRun.fillInGap("Which skyscraper do you like most?", "Bank of China Tower");
		
		questionnaireRun.selectMenuItem("Essay Question");
		questionnaireRun.fillInEssay("Hmmm... difficult question...");
			
		//student finishes and tries to do it once again
		CourseRun courseRun2 = questionnaireRun.finish();
		
		QuestionnaireElement questionnaireElement2 = courseRun2.selectQuestionnaire("Questionnaire");
		questionnaireElement2.cannotStartAnymore();
		questionnaireElement2.isTextPresent("You have already filled in this questionnaire, thank you! Questionnaires can only be filled in once.");
		
		
			
  }
}
