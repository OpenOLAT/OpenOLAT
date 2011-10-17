package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates questionnaire, starts editor, closes editor, questionnaire is deleted <br/>
 * <p>
 * Test setup: <br/>
 * 1. Author creates questionnaire <br/>
 * 2. questionnaire is deleted <br/> 
 * 
 * Test case: <br/>
 * 1. Author creates questionnaire <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. questionnaire is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createQuestionnaireTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createQuestionnaireTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());		
		TestEditor testEditor = olatWorkflowHelper.getLearningResources().createQuestionnaireAndStartEditing("QuestionnaireName", "QuestionnaireDescription");
		LRDetailedView lRDetailedView = testEditor.close();
		
		//questionnaire is deleted
		lRDetailedView.deleteLR();		
	}
}
