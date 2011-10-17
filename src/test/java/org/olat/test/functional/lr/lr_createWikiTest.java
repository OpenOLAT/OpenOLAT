package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;

/**
 * Author creates wiki, wiki is deleted <br/> 
 * <p>
 * Test setup: <br/>
 * 1. Author creates wiki <br/>
 * <br/>
 * Test case: <br/>
 * 1. Author creates wiki <br/>
 * 2. wiki is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createWikiTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createWikiTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		LRDetailedView lRDetailedView = olatWorkflow.getLearningResources().createResource("WikiName", "WikiDescription", LR_Types.WIKI);

		// wiki is deleted
		lRDetailedView.deleteLR();
		
	}
}
