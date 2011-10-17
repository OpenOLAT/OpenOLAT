package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * StandardAdmin creates and deletes courses<br/>
 * <br/>
 * Testsetup: <br/>
 * 1. Admin creates 10 courses<br/>  
 * Testcase: <br/>
 * 1. Admin creates 10 courses with prefix deletetest <br/>
 * 2. Admin deletes all courses with prefix deletetest <br/>
 * 
 * @author eglis
 *
 */
public class DeleteAllCoursesTest extends BaseSeleneseTestCase {

	public void testDeleteAllCourses() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
				
		String myRandomName="deletetest-"+System.currentTimeMillis();

		for(int i=0; i<10; i++) {
			workflow.getLearningResources().createResource(myRandomName, myRandomName, LR_Types.COURSE);		
		}
		
		WorkflowHelper.deleteAllCoursesNamed(myRandomName);
	}
}
