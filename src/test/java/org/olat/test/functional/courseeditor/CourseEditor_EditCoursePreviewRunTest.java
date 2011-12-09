/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.SinglePageEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.InsertPosition;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**        
 *         
 * Tests changes in previews of course and single page 
 * <br/>  
 * <p>        
 * Test setup:<br/>        
 * 1. Standardauthor greates course COURSE_NAME<br/>
 * 2. cleanup in the end: delete learning resource<br/>
 * <br/>
 * Test case: <br/>     
 * 1. Test case:  <br/>
 * login as author  <br/>
 * create course  <br/>
 * insert elements  <br/>
 * insert information at structure and single pages, edit html pages of single pages, publish  <br/>
 * edit, move nodes, delete nodes, preview, publish  <br/>
 * delete course  <br/>        
 *  </p>  
 *      
 * @author alberto   
 *        
 */        


public class CourseEditor_EditCoursePreviewRunTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	public void testEditCoursePreviewRunTest() throws Exception {
		
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		SinglePageEditor singlePageEditor = (SinglePageEditor)courseEditor.insertCourseElement(CourseElemTypes.SINGLE_PAGE, true, null);
		singlePageEditor.setDescription("This is the course TS090533 Description");
		singlePageEditor.createHTMLPage("Firstnode_HTML_Descr", "This is the");
		singlePageEditor.preview();
		singlePageEditor.closePreview();
				
		CourseElementEditor courseElementEditor = courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, null);
		courseElementEditor.setDescription("This is the description of first structure node");
		courseEditor.publishCourse();
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		courseEditor = lRDetailedView.editCourseContent();
		courseEditor.selectCourseElement(CourseEditor.STRUCTURE_TITLE);
		courseEditor.moveCourseElement(InsertPosition.FIRST_CHILD_OF_ROOT, null);
						
		courseEditor.selectCourseElement(CourseEditor.SINGLE_PAGE_TITLE);
		courseEditor.deleteCourseElement();
		courseEditor.selectCourseElement(CourseEditor.STRUCTURE_TITLE);
		courseEditor.deleteCourseElement();
	  //TODO: LD: selective publishing 
		courseEditor.publishCourse();
		courseEditor.preview();
		courseEditor.closeToLRDetailedView().deleteLR();
		olatWorkflow.logout();
				
	}
}
