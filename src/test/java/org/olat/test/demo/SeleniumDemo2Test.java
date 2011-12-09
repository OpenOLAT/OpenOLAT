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
package org.olat.test.demo;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Demo test class.
 * Uses the OLAT testing framework, including the OLAT abstraction layer.
 * 
 * <br/>
 * Test setup: none, since any OLAT instance has a "Demo Course" per default.
 *  <br/>
 *  Test case:  <br/>
 *  - login, <br/>
 *  - go to Learning resources, <br/> 
 *  - search for "Demo Course", <br/>
 *  - Show content, <br/>
 *  - open Course Editor, <br/>
 *  - insert a Forum course element, <br/>
 *  - publish course, <br/>
 *  - logout.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class SeleniumDemo2Test extends BaseSeleneseTestCase {
	
	private static final String COURSE_NAME = Context.DEMO_COURSE_NAME_1;
	private static final String FORUM_NAME = "Special Forum Name";
	
	public void setUp() throws Exception {		
		//each test has to setup a context
		Context.setupContext(getFullName(), SetupType.SINGLE_VM);		
	}
	
	/**
	 * Login, go to Learning resources, select Courses, select "Demo Course", Show content, 
	 * open Course Editor, insert a Forum course element, publish course, logout.
	 * 
	 * @throws Exception
	 */
	public void testCourseEditing() throws Exception {
		//Get a workflow object for the default administrator user. 
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		//go to learning resources, search and show the course named COURSE_NAME, get a CourseRun object
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		
		//go to the course editor
		CourseEditor courseEditor = courseRun.getCourseEditor();
		
		//insert a course element of type FORUM, with a specified title
		CourseElementEditor courseElementEditor = courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, FORUM_NAME);
		assertTrue(courseElementEditor.isTextPresent(FORUM_NAME));		
		
		//publish course
		courseEditor.publishCourse();
		
		//logout
		workflow.logout();		
	}
}
