package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.ResourceEditor;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates resource folder, starts editor, closes editor, resource folder is deleted <br/> 
 * <p>
 * Test setup: <br/>
 * 1. Author creates resource folder <br/>
 * 
 * 
 * Test case: <br/>
 * 1. Author creates resource folder <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. resource folder is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createResourcefolderTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createResourcefolderTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		ResourceEditor resourceEditor = olatWorkflow.getLearningResources().createResourceFolderAndStartEditing("ResourcefolderName", "ResourcefolderDescription");
		LRDetailedView lRDetailedView = resourceEditor.close();
		
		// resource folder is deleted
		lRDetailedView.deleteLR();
		
	}
}
