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
package org.olat.test.sandbox;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Test issue OLAT-4597.
 * Tries to reproduce a RS.
 * <br/>
 * <p>
 * Test case: <br/>
 * open course editor <br/>
 * add course building block 'external page' <br/>
 * Add an URL <br/>
 * press preview button <br/>
 * assert: there is NO RedScreen <br/>
 * </p>
 * 
 * @author alberto
 *
 */
public class PreviewExternalPageTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	public void testPreviewExternalPageTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		//open course editor
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
	
		//add course building block 'external page'
		
		
		//Add an URL
		
		//press preview button
		
		//=> assert: there is NO RedScreen NO RedScreen
		
	}
}
