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
package org.olat.test.functional.lr;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * StandardAdmin creates and deletes courses<br/>
 * <br/>
 * Testsetup: <br/>
 * 1. Admin creates 10 courses<br/>  
 * Testcase: <br/>
 * 1. Admin creates 10 courses with prefix deletetest <br/>
 * 2. Admin deletes all courses with prefix deletetest <br/>
 * 
 * @author eglis
 *
 */
public class DeleteAllCoursesTest extends BaseSeleneseTestCase {

	public void testDeleteAllCourses() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
				
		String myRandomName="deletetest-"+System.currentTimeMillis();

		for(int i=0; i<10; i++) {
			workflow.getLearningResources().createResource(myRandomName, myRandomName, LR_Types.COURSE);		
		}
		
		WorkflowHelper.deleteAllCoursesNamed(myRandomName);
	}
}
