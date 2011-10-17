package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**        
 *         
 * Cancel publish workflow in different moments of the workflow 
 * <br/>  
 * <p>        
 * Test setup:<br/>        
 * 1. standardauthor creates course CoursName  <br/>
 * 2. Cleanup in the end: delete learning resource<br/>
 * <br/>
 * Test case: <br/>        
 * 1. create course "CourseName" <br/>  
 * 2. insert forum, click publish, change access to all registered OLAT users<br/>
 * 3. insert structure, click publish, cancel<br/>
 * 4. click publish, select all, cancel<br/>
 * 5. click publish, click next, cancel<br/>
 * 6. click publish, click select all, click next, choose Only owners of this learning resource, cancel<br/>
 * 7. click publish, select firstTreeCheckbox, click next, click finish<br/>
 * 8. close editor<br/>
 * 9. click show content<br/>
 * 10. click forum<br/>
 * 11. click detail view<br/>
 * 12. delete course CourseName  <br/>    
 *  </p>   
 *         
 * @author kristina       
 *        
 */        

public class courseEditor_consistencyPublishTest extends BaseSeleneseTestCase {
	
	
	public void testcourseEditor_consistencyPublishTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		//selenium = context.createSeleniumAndLogin(context.getStandardAuthorOlatLoginInfos());
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing("CourseName", "CourseDescription");
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		courseEditor.publishCourse();
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.STRUCTURE, true, null);
		selenium = courseEditor.getSelenium();		
		
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_cancel()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()");
		// cancel publish
		selenium.click("ui=courseEditor::publishDialog_cancel()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_selectall()");
		selenium.waitForPageToLoad("30000");
		//cancel publish
		selenium.click("ui=courseEditor::publishDialog_cancel()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");
		//cancel publish
		selenium.click("ui=courseEditor::publishDialog_cancel()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_selectall()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");
		selenium.select("ui=courseEditor::publishDialog_courseAccessDropDown()", "label=Only owners of this learning resource");
		//cancel publish
		selenium.click("ui=courseEditor::publishDialog_cancel()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_finish()");
		selenium.waitForPageToLoad("30000");
		
		//we are still in the courseEditor
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		CourseRun courseRun = lRDetailedView.showCourseContent();
		courseRun.selectCourseElement(CourseEditor.STRUCTURE_TITLE);
		courseRun.selectCourseElement(CourseEditor.FORUM_COURSE_ELEM_TITLE);
		lRDetailedView = courseRun.getDetailedView();
		try {
			lRDetailedView.deleteLR();
		} catch (Exception e) {}		
		
	}
}
