package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.FolderEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.InsertPosition;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**        
 *         
 * create course with all elements and configure 
 * <br/>  
 * <p>        
 * Test setup:<br/>        
 * 1. Standardauthor creates course COURSE_NAME<br/>
 * 2. cleanup in the end: delete learning resource<br/>
 * <br/>
 * Test case: <br/>     
 * 1. create course "CourseName" and start course editor <br/>
 * 2. insert structure<br/>
 * 3. insert single page<br/>
 * 4. insert external page<br/>
 * 5. insert cp<br/>
 * 6. insert scorm<br/>
 * 7. insert forum<br/>
 * 8. insert wiki<br/>
 * 9. insert file dialog<br/>
 * 10. insert folder<br/>
 * 11. insert assessment<br/>
 * 12. insert task<br/>
 * 13. insert test<br/>
 * 14. insert selftest<br/>
 * 15. insert questionnaire<br/>
 * 16. insert enrolment<br/>
 * 17. insert contact form<br/>
 * 18. move contact form<br/>
 * 19. delete contact form<br/>
 * 20. click on folder, goto tab visibility<br/>
 * 21. choose blocked for learners, save<br/>
 * 22. goto tab access, deselect blocked for learner (Read and Write), choose blocked for learners (read only)<br/>
 * 23. click course preview<br/>
 * 24. close course editor<br/>
 * 25. delete course  <br/>      
 *  </p>  
 *      
 * @author kristina   
 *        
 */        

public class courseEditor_insertElementsAMTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	private final String FOLDER_NAME = "Folder";
	
	
	public void testcourseEditor_insertElementsAMTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		// create course "CourseName" and start course editor
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		// insert structure, single page, external page etc.
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.STRUCTURE, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.SINGLE_PAGE, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.EXTERNAL_PAGE, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.CP_LEARNING_CONTENT, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.SCORM_LEARNING_CONTENT, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.WIKI, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FILE_DIALOG, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FOLDER, true, FOLDER_NAME);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.ASSESSMENT, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TASK, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TEST, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.SELF_TEST, true, null);		
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.QUESTIONNAIRE, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.ENROLMENT, true, null);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.CONTACT_FORM, true, null);
		courseEditor.selectCourseElement(CourseEditor.CONTACT_FORM_TITLE);
		courseEditor.moveCourseElement(InsertPosition.FIRST_CHILD_OF_ROOT, null);
		courseEditor.selectCourseElement(CourseEditor.CONTACT_FORM_TITLE);
		courseEditor.deleteCourseElement();
		
		// click on folder, goto tab visibility
		FolderEditor folderEditor = (FolderEditor)courseEditor.selectCourseElement(FOLDER_NAME);
    // choose blocked for learners, save
		folderEditor.changeVisibilityBlockForLearners();
    //goto tab access, deselect blocked for learner (Read and Write), choose blocked for learners (read only)
		folderEditor.changeAccessBlockForLearnersReadAndWrite();
		folderEditor.changeAccessBlockForLearnersReadOnly();
			
		// click course preview, close preview
		courseEditor.preview();		
		// close course editor
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		try {
			// delete course
			lRDetailedView.deleteLR();
		} catch (Exception e) {}		
	}
}
