package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CoursePreview;
import org.olat.test.util.selenium.olatapi.course.editor.SinglePageEditor;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.InsertPosition;
import org.olat.test.util.selenium.olatapi.course.editor.CoursePreview.Role;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**
 * Tests incomplete publish workflows
 * <br/>
 * <p>
 * Test case: <br/> 
 * create course <br/>
 * create and insert single page, preview within single page <br/>
 * insert structure element <br/>
 * leave editor, enter editor <br/>
 * copy single page, delete copy <br/>
 * preview <br/>
 * publish <br/>
 * insert single page, create html page and assign to single page <br/>
 * delete html-page in the storage folder <br/>
 * publish, delete single page, publish <br/>
 * delete course and logout <br/>
 *
 * @author alberto
 */
public class DeletePublishIncompleteElementsTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	
	public void testDeletePublishIncompleteElementsTest() throws Exception {
		
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		//create course
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		
		//create and insert single page, preview within single page
		SinglePageEditor singlePageEditor = (SinglePageEditor)courseEditor.insertCourseElement(CourseElemTypes.SINGLE_PAGE, true, "single page1");
		singlePageEditor.setDescription("This is the course TS090533 Description");
		singlePageEditor.createHTMLPage("first_html_descr", "a not very long content that serves as an example");
	    singlePageEditor.preview();
	    singlePageEditor.closePreview();
	    
	    //insert structure element
	    CourseElementEditor courseElementEditor = courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "structure1");
		courseElementEditor.setDescription("This is the description of first structure node");
	    
		//leave editor, enter editor
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		courseEditor = lRDetailedView.editCourseContent();
		
		//copy single page, delete copy
		courseEditor.selectCourseElement("single page1");
		courseEditor.copyCourseElement(InsertPosition.FIRST_CHILD_OF_ROOT, "single page2");
		courseEditor.deleteCourseElement();
		
		//preview
		CoursePreview coursePreview = courseEditor.openPreview();
		coursePreview.changeRole(Role.AUTHOR);	
		coursePreview.showPreview();
		courseEditor = coursePreview.closePreview();
		
		//publish
	    courseEditor.publishCourse();
	    
	    //insert single page, create html page and assign to single page
		singlePageEditor = (SinglePageEditor)courseEditor.insertCourseElement(CourseElemTypes.SINGLE_PAGE, true, "single page2");
		singlePageEditor.setDescription("This is the second course TS090533 Description");
		singlePageEditor.createHTMLPage("second_html_descr", "a not very long content that serves as an example too");
		
		//delete html-page in the storage folder
		Folder storageFolder = courseEditor.storageFolder();
		storageFolder.deleteItem("second_html_descr.html");
		
		//publish, delete single page, publish
		courseEditor.publishCourse();
		courseEditor.selectCourseElement("single page2");
		courseEditor.deleteCourseElement();
		courseEditor.publishCourse();
		
		//delete course and logout
		courseEditor.closeToLRDetailedView().deleteLR();
		olatWorkflow.logout();	
	}
}
