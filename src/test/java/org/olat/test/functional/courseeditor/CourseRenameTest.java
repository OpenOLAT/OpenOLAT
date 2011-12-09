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
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.StructureElement;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests the course title change and publish.
 * Test setup & test case:
 * Admin creates a clone of "Demo course wiki" course, publishes it, changes the title,
 * publishes again, asserts if the course title was changed. Cleanup.
 * 
 * 
 * @author lavinia
 *
 */
public class CourseRenameTest extends BaseSeleneseTestCase {
	
	private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_2;
	private final String COURSE_NAME = "CourseRename" + System.currentTimeMillis();

	public void testCoursePublish() throws Exception {
		System.out.println("CoursePublishTest - before setupContext");
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		System.out.println("CoursePublishTest - after setupContext");
		
		//clone DEMO_COURSE_NAME and work with the clone
	  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		System.out.println("CoursePublishTest - logged in");
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		Thread.sleep(9000);
		CourseEditor courseEditor = courseRun.getCourseEditor();
		CourseElementEditor courseElementEditor = courseEditor.selectCourseElement(COURSE_NAME);
		courseElementEditor.setTitle("mod");
		courseEditor.publishCourseAfterCourseTitleChanged();
		courseRun = courseEditor.closeToCourseRun();
		//asserts that root changed the title
		StructureElement root = courseRun.selectRoot("mod");
		courseRun.close(COURSE_NAME);	
	}


	@Override
	public void cleanUpAfterRun() {				
		try {
			OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());		
			workflow.getLearningResources().searchMyResource(COURSE_NAME).deleteLR();			
		} catch (Exception e) {			
			System.out.println("Exception while tried to delete test course!!!");
			e.printStackTrace();
		}
	}
	
	
}
