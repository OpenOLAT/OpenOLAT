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
import org.olat.test.util.selenium.olatapi.course.editor.CoursePreview;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.InsertPosition;
import org.olat.test.util.selenium.olatapi.course.editor.CoursePreview.Role;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**
 * 
 * Tests changes in course tree structure by moving and deleting nodes
 * <br/>
 * <p>
 * Test case: <br/>
 * login as author  <br/>
 * create course <br/>
 * insert elements with subelements, publish  <br/>
 * insert more elements with subelements, publish  <br/>
 * navigate in coursemenu, delete only a subelement, publish  <br/>
 * delete element with subelements, move first element to last position, publish  <br/>
 * leave editor, enter editor  <br/>
 * delete first and last elements, restore first, delete first  <br/>
 * preview, another preview with modified roles, publish  <br/>
 * delete course  <br/>
 *
 * @author alberto
 */

public class MoveDeleteElementsTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseMoveDelete"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	private OLATWorkflowHelper workflow;
	
	public void testCourseEditor_moveDeleteElementsTest() throws Exception {
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		workflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, true, null);
		courseEditor.insertCourseElement(CourseElemTypes.FORUM, false, null);
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, false, "Folder1");
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, false, "Folder2");
		courseEditor.publishCourse();
	  //close course, start run, start editor
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		courseEditor = lRDetailedView.showCourseContent().getCourseEditor();
		
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, InsertPosition.FOLLOWING_SIBLING_OF_ELEMENT, CourseEditor.STRUCTURE_TITLE, "Structure1");
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, InsertPosition.FIRST_CHILD_OF_ELEMENT, "Structure1", "Folder3");
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, InsertPosition.FIRST_CHILD_OF_ELEMENT, "Structure1", "Folder4");
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "Structure2");
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "Structure3");
				
		courseEditor.publishFirstChangedElement();
		courseEditor.publishFirstChangedElement();
				
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "Structure4");
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, InsertPosition.FIRST_CHILD_OF_ELEMENT, "Structure4", "Folder5");
		courseEditor.insertCourseElement(CourseElemTypes.FOLDER, InsertPosition.FIRST_CHILD_OF_ELEMENT, "Structure4", "Folder6");
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "Structure5");
		courseEditor.insertCourseElement(CourseElemTypes.STRUCTURE, false, "Structure6");
		/*while(courseEditor.publishFirstChangedElement()) {
			System.out.println("one more node to be published found");
		}*/
		courseEditor.publishCourse();
				
		courseEditor.selectCourseElement("Structure4");
		courseEditor.selectCourseElement("Folder6");
		courseEditor.selectCourseElement("Folder5");		
		courseEditor.deleteCourseElement();
		courseEditor.publishFirstChangedElement();
		//close course, start run, start editor
		CourseRun courseRun = courseEditor.closeToCourseRun();
		assertTrue(courseRun.isTextPresent("Structure6"));
		assertFalse(courseRun.isTextPresent("Folder5"));
		courseEditor = courseRun.getCourseEditor();
		
		courseEditor.selectCourseElement("Structure5");
		courseEditor.selectCourseElement("Structure4");
		courseEditor.deleteCourseElement();
		courseEditor.publishFirstChangedElement();
		courseEditor.selectCourseElement("Structure1");
		courseEditor.moveCourseElement(InsertPosition.LAST_CHILD_OF_ROOT, null);
		courseEditor.publishCourse();
						
		courseEditor.closeToCourseRun();
		assertFalse(courseRun.isTextPresent("Structure4"));
		assertTrue(courseRun.isTextPresent("Structure1"));
		
		courseEditor = workflow.getLearningResources().searchMyResource(COURSE_NAME).editCourseContent();
		courseEditor.selectCourseElement("Folder2");
		courseEditor.deleteCourseElement();
		courseEditor.selectCourseElement("Structure3");
		courseEditor.deleteCourseElement();
		courseEditor.selectCourseElement("Folder2");
		courseEditor.undeleteCourseElement();
				
		
		CoursePreview coursePreview = courseEditor.openPreview();
		coursePreview.changeRole(Role.AUTHOR);
		coursePreview.changeRole(Role.REGISTERED_USER);		
		coursePreview.showPreview();		
		
		//assertFalse(coursePreview.isTextPresent("Structure3"));
		assertTrue(coursePreview.isTextPresent("Folder2"));
		
		//check this out
		coursePreview.selectCourseElement("Structure1");		
		
		courseEditor = coursePreview.closePreview();
		courseEditor.publishCourse();		
		courseEditor.closeToCourseRun().close(COURSE_NAME);			
		
		workflow.getLearningResources().searchMyResource(COURSE_NAME).deleteLR();
	}
	
	
	@Override
	protected void cleanUpAfterRun() {
		System.out.println("***************** cleanUpAfterRun STARTED *********************");
		workflow.logout();
		System.out.println("***************** cleanUpAfterRun ENDED *********************");
	}
	
}
