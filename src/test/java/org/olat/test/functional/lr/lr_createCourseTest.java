package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates course, starts editor, closes editor, course is deleted <br/> 
 * <p>
 * Test setup: <br/>
 * 1. Author creates course <br/>
 * 2. course is deleted <br/>
 * 
 * Test case: <br/>
 * 1. Author creates course <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. course is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createCourseTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createCourseTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		CourseEditor courseEditor = learningResources.createCourseAndStartEditing("CourseName", "CourseDescription");
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
    //cleanup 
		lRDetailedView.deleteLR();			
	}
}
