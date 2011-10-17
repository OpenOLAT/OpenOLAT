package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates course, insert title and description, add owner, change access, delete course <br/>
 * <p>
 * Test setup: <br/>
 * 1. Author creates course <br/>
 * 2. course is deleted at the end<br/>
 * Test case: <br/>
 * 1. Author creates course CourseName <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. Author clicks assign owners, adds owner <br/>
 * 5. Author clicks modify properties <br/>
 * 6. Author changes to all registered OlAT users, save <br/>
 * 7. course is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createCourseAMTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createCourseAMTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos().getPassword();
		OlatLoginInfos secondUser = context.createuserIfNotExists(1, "coursenameauthor", standardPassword, true, true, true, true, false);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing("CourseName", "CourseDescription");
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		lRDetailedView.assignOwner(secondUser.getUsername());
		lRDetailedView.modifyProperties("All registered OLAT users");
				
		//delete course
		lRDetailedView.deleteLR();		
	}
}
