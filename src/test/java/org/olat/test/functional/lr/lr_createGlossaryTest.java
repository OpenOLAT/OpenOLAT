package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates glossary, starts editor, closes editor, glossary is deleted <br/>
 * <p>
 * Test setup: <br/>
 * 1. cleanup <br/>
 * 2. Author creates glossary <br/>
 * 3. glossary is deleted <br/>
 * 
 * Test case: <br/> 
 * 1. Author creates glossary <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. glossary is deleted<br/>
 * </p>
 * 
 * @author kristina
 */
public class lr_createGlossaryTest extends BaseSeleneseTestCase {
	
	private final String GLOSSARY_NAME = "GlossaryName" + System.currentTimeMillis();;
	
	
	public void testlr_createGlossaryTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		learningResources.createGlossaryAndStartEditing(GLOSSARY_NAME, "GlossaryDescription");
		
		//cleanup
		learningResources = olatWorkflow.getLearningResources();
		LRDetailedView lRDetailedView = learningResources.searchMyResource(GLOSSARY_NAME);
		lRDetailedView.deleteLR();						
	}
}
