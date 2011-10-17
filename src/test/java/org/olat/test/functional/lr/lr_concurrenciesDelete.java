package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Check if Student receives the message, that the Author has deleted the course. <br/>
 * <p>
 * Test setup: <br/>
 * 1. Author creates course <br/>
 * 2. Author deletes course which Student is viewing <br/>
 * Test case: <br/>
 * 1. Author creates course with forum, publishes course and sets access to all registered OLAT users. <br/>
 * 2. Student opens course in course-run in browser 2 <br/>
 * 3. Author deletes the course in browser 1 <br/>
 * 4. Student clicks the link forum <br/>
 * 5. Check if Student receives the message, that the course was deleted. <br/>
 * 
 * </p>
 * 
 * @author kristina
 */

public class lr_concurrenciesDelete extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "CourseName";
	
	
	    
    public void testlr_concurrenciesDelete() throws Exception {
    	Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
  		
  		OlatLoginInfos user1 = context.getStandardAuthorOlatLoginInfos();
  		OlatLoginInfos user2 = context.getStandardStudentOlatLoginInfos();
  		  		
  		// Author creates course CourseName with forum and open the detail view of course 'CourseName'
  		System.out.println("logging in browser 1...");			
  		OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(user1);
  		LearningResources learningResources1 = olatWorkflow1.getLearningResources();
  		CourseEditor courseEditor = learningResources1.createCourseAndStartEditing(COURSE_NAME, "CourseDescription");
  		courseEditor.insertCourseElement(CourseElemTypes.FORUM, true, null);
  		courseEditor.publishCourse();
  		courseEditor.closeToLRDetailedView();								
  		LRDetailedView lRDetailedView1 = learningResources1.searchResource(COURSE_NAME, null);
  		  		
  		// Student opens 'CourseName' with browser 2
  		System.out.println("logging in browser 2...");			
  		OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(user2);
  		LearningResources learningResources2 = olatWorkflow2.getLearningResources();
  		LRDetailedView lRDetailedView2 = learningResources2.searchResource(COURSE_NAME, null);
  		CourseRun courseRun2 = lRDetailedView2.showCourseContent();			
  		
  		// Author deletes 'CourseName' in browser 1
  		lRDetailedView1.deleteLR();
  			  		
  		// after that Student clicks 'Forum' in browser 2
  		// beware of polling which already shows the message "this course..."
  		// to stabilize the test: check first if Forum is available.
  		// test may still fail because of polling before clicking, but the probability is lowered.
  		if(courseRun2.isElementPresent("Forum")){
  			courseRun2.selectCourseElement("Forum");
  		}
      //and waits until 'This course has been modified.' appears
  		SeleniumHelper.waitUntilTextPresent(courseRun2.getSelenium(), "This course has been modified.", 20);			
  		// excellent, close course in browser 2		
  		courseRun2.close(COURSE_NAME);
	}
}
