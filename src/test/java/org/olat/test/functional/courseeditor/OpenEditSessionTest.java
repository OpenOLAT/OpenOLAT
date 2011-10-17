package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Test the open/close course edit session. 
 * See OLAT-4546.
 * 
 * Test case: <br/>
 * Author: <br/>
 * - creates course, insert node, publish, close course to detail view <br/>
 * - open course run from detail view <br/>
 * - go to detail view again (select detail view from course run), and modify property (e.g. access) <br/>
 * - click edit course (from detail view) and land to the disposed course <br/>
 * - click "Close and restart course" -> reopens course run <br/>
 * - edit course again (e.g. insert node) and publish. Close course. <br/>
 * - delete course.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OpenEditSessionTest extends BaseSeleneseTestCase {
	
	private String COURSE_NAME = "AAA"+ System.currentTimeMillis();
	private boolean cleanedUp = false;

	
	public void testOpenEditSession() {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla");
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		CourseElementEditor courseElementEditor = courseEditor.selectCourseElement("Forum");
		courseElementEditor.setDescription("NEW DESCRIPTION");
		
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		
		LRDetailedView lRDetailedView = courseRun.getDetailedView();
		lRDetailedView.modifyProperties(LRDetailedView.ACCESS_REGISTERED_AND_GUESTS);
		
		//the course run was disposed due to property change
		courseRun = lRDetailedView.selectDisposedCourse().closeCourseAndRestart();
		courseEditor = courseRun.getCourseEditor();
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.STRUCTURE, true, null);
		courseEditor.publishCourse();
		//cleanup
		courseEditor.closeToCourseRun().getDetailedView().deleteLR();
		cleanedUp = true;
	}

	
	@Override
	protected void cleanUpAfterRun() {
		System.out.println("***************** cleanUpAfterRun STARTED *********************");
	  //cleanup
		if(!cleanedUp) {
		  OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos());		
			workflow.getLearningResources().searchMyResource(COURSE_NAME).deleteLR();
		}
		System.out.println("***************** cleanUpAfterRun ENDED *********************");
	}
}
