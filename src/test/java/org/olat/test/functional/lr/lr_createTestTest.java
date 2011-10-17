package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates test, starts editor, closes editor, tes is deleted <br/> 
 * <p>
 * Test setup: <br/>
 * 1. Author creates test <br/>
 * <br/>
 * Test case: <br/>
 * 1. Author creates test <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. test is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createTestTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createTestTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		OLATWorkflowHelper olatWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());	
		TestEditor testEditor = olatWorkflowHelper.getLearningResources().createTestAndStartEditing("TestName", "TestDescription");
		LRDetailedView lRDetailedView = testEditor.close();
		
		// test is deleted
		lRDetailedView.deleteLR();
		
	}
}
