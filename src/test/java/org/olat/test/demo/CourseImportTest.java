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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.test.demo;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


/**
 * 
 * @author eglis
 *
 */
public class CourseImportTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "/CourseImportCourse.zip";
	
	public void testCourseImport() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);

		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());
		String courseTitle = "CourseImportTestCourse-"+System.currentTimeMillis();
		WorkflowHelper.importCourse(f, courseTitle, "Whatever right?");
		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseTitle);
		courseRun.close(courseTitle);
				
	}

}
