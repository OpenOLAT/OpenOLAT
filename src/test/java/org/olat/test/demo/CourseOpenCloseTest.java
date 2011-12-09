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

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

public class CourseOpenCloseTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = Context.DEMO_COURSE_NAME_1;

	public void testCourseOpenClose() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
		//context.checkSeleniumServerStarted();
		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().showCourseContent(COURSE_NAME);
		courseRun.close(COURSE_NAME);		
	}
}
