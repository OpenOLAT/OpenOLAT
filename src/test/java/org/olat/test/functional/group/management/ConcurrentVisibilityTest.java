package org.olat.test.functional.group.management;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.StructureElement;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;

/**
 * 
 * Tests if group dependent visibility restrictions on course elements work depending on group membership.
 * <br/>
 * Test setup: <br/>
 * 1. Delete all learning resources from author. <br/>
 * <p>
 * Test case: <br/>
 * 1. Author goes to learning resources and creates course COURSE_TITLE. <br/>
 * 2. Author goes to group management and creates learning group GROUP_NAME. <br/>
 * 3. Author adds student 1 as participant and student 2 as tutor to group. <br/>
 * 4. Author opens course editor, adds folder course element and restricts visibility to above created group. <br/>
 * 5. Author publishes course. <br/>
 * 6. Student 1 opens course COURSE_TITLE and navigates to folder. <br/>
 * 7. Student 2 opens course COURSE_TITLE, opens group management and removes student 1 from group. <br/>
 * 8. Student 1 tries to access folder but as access rights have altered gets back to course root element.   <br/>
 * 9. Author deletes course COURSE_TITLE. <br/>
 * 
 * @author sandra
 * 
 */

public class ConcurrentVisibilityTest extends BaseSeleneseTestCase {
	    
	private final String COURSE_TITLE = "Course_for_Enrolment_Concurrency2-"+System.currentTimeMillis();
	private final String GROUP_NAME = "learning group selenium 6";
	private final String STUDENT_USER_NAME = "srenrolstudi03";
	
	
    public void testConcurrentVisibility() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		String standardPassword = context.getStandardStudentOlatLoginInfos(2).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(1, STUDENT_USER_NAME, standardPassword, true, false, false, false, false);

		{
			// Author01 add learning group to course COURSE_TITLE, adds student01 
			// as tutor and enrolmentstudent02 as participant and adds folder with visibility restriction to this group
			OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));							
			System.out.println("logging in browser 1...");
			LearningResources learningResources = olatWorkflow.getLearningResources();
			learningResources.createResource(COURSE_TITLE, "Enrolment Test", LR_Types.COURSE);
			LRDetailedView lRDetailedView = learningResources.searchMyResource(COURSE_TITLE);
			CourseRun courseRun = lRDetailedView.showCourseContent();
			courseRun.getGroupManagement().createGroupAndAddMembers(GROUP_NAME, context.getStandardStudentOlatLoginInfos(2).getUsername(), STUDENT_USER_NAME);
			CourseEditor courseEditor = courseRun.getCourseEditor();
			CourseElementEditor courseElementEditor = courseEditor.insertCourseElement(CourseElemTypes.FOLDER, true, null);
			courseElementEditor.changeVisibilityDependingOnGroup(GROUP_NAME);
			courseEditor.publishCourse();
			courseEditor.closeToCourseRun();
			olatWorkflow.logout();						
		}
		
			//enrolmentstudent02 opens course and navigates to folder
			OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(student02);
			CourseRun courseRun1 = olatWorkflow1.getLearningResources().searchAndShowCourseContent(COURSE_TITLE);
			courseRun1.selectCourseElement("Folder");		
			
			// student01 opens course, then group administration and removes 
			// enrolmentstudent02 from group
			OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
			CourseRun courseRun2 = olatWorkflow2.getLearningResources().searchAndShowCourseContent(COURSE_TITLE);
			Thread.sleep(3000);
			courseRun2.removeFromTutoredGroup(GROUP_NAME, STUDENT_USER_NAME);							
			
		
			// enrolmentstudent02 should not be able to select the folder node anymore: 
			//when the user attempts to select the node the root gets selected and the user gets a accessRightAltered message 
			/*courseRun1.selectCourseElement("Folder");		
			selenium1 = courseRun1.getSelenium();*/
			StructureElement root = courseRun1.selectAnyButGetToRoot("Folder");
						
			boolean accessRightAltered = false;
			for (int second = 0;; second++) {
				if (second >= 60) fail("timeout");
				try { 
					if (root.getSelenium().isTextPresent("Your access rights have been altered in the meantime. Therefore you cannot select the course element required anymore.")) {
					accessRightAltered = true;
					break;  }					
				} catch (Exception e) {}
				Thread.sleep(1000);				
			  	
		  }
		  assertTrue("Asserts that the access rights message shows up",accessRightAltered);
				
	}

	@Override
	protected void cleanUpAfterRun() {
		//author01 deletes course
		OLATWorkflowHelper olatWorkflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));		
		LearningResources learningResources = olatWorkflow.getLearningResources();
		learningResources.searchMyResource(COURSE_TITLE).deleteLR();			
	}
    
    
}
