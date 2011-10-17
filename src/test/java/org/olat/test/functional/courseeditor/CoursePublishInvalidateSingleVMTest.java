package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests that after course publish, any other user that has the course open gets a 
 * "This course has been modified" message, and must close the course tab.
 * 
 * @author eglis
 *
 */
public class CoursePublishInvalidateSingleVMTest extends BaseSeleneseTestCase {
	
    //TODO: LD: import test course instead of cloning the DEMO_COURSE_NAME
	private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_2;
	private final String COURSE_NAME = "CoursePublishInvalidate" + System.currentTimeMillis();

    public void testMultiBrowserCourseViewPublish() throws Exception {
    	Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
    	
    	//clone DEMO_COURSE_NAME and work with the clone
	  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
	  	
    	OLATWorkflowHelper workflow_1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    	OLATWorkflowHelper workflow_2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));    	
    	
    	// open the course 'Demo course wiki', modify it and get ready to hit the publish button
    	System.out.println("logging in browser 1...");			
    	CourseRun courseRun_1 = workflow_1.getLearningResources().searchAndShowMyCourseContent(COURSE_NAME);
    	Thread.sleep(9000);
    	CourseEditor courseEditor_1 = courseRun_1.getCourseEditor();
    	CourseElementEditor courseElementEditor = courseEditor_1.selectCourseElement(COURSE_NAME);
    	courseElementEditor.setTitle("mod");

    	// open 'Demo course wiki' with browser 2
    	System.out.println("logging in browser 2...");			
    	CourseRun courseRun_2 = workflow_2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);

    	// now trigger the publish in browser 1
    	courseEditor_1.publishCourse();

    	// after that, click 'Wiki sandbox' in browser 2
    	if(courseRun_2.isTextPresent("Wiki sandbox")) {
    	  courseRun_2.selectCourseElement("Wiki sandbox");
    	}
    	//and wait until 'This course has been deleted.' appears
    	SeleniumHelper.waitUntilTextPresent(courseRun_2.getSelenium(), "This course has been modified.", 60);	//increased timeout		

    	// excellent, close course in browser 2			
    	courseRun_2.close(COURSE_NAME);		
    }
    
    @Override
	protected void cleanUpAfterRun() {			
		try {
			OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());		
		    workflow.getLearningResources().searchMyResource(COURSE_NAME).deleteLR();			
		} catch (Exception e) {			
			System.out.println("Exception while tried to delete test course!!!");
			e.printStackTrace();
		}
	}
}
