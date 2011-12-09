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
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Author creates course, insert title and description, add owner, change access, delete course <br/>
 * <p>
 * Test setup: <br/>
 * 1. Author creates course <br/>
 * 2. course is deleted at the end<br/>
 * Test case: <br/>
 * 1. Author creates course CourseName <br/>
 * 2. Author starts editor <br/>
 * 3. Author closes editor <br/>
 * 4. Author clicks assign owners, adds owner <br/>
 * 5. Author clicks modify properties <br/>
 * 6. Author changes to all registered OlAT users, save <br/>
 * 7. course is deleted <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_createCourseAMTest extends BaseSeleneseTestCase {
	
	
	
	public void testlr_createCourseAMTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos().getPassword();
		OlatLoginInfos secondUser = context.createuserIfNotExists(1, "coursenameauthor", standardPassword, true, true, true, true, false);
				
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing("CourseName", "CourseDescription");
		LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
		lRDetailedView.assignOwner(secondUser.getUsername());
		lRDetailedView.modifyProperties("All registered OLAT users");
				
		//delete course
		lRDetailedView.deleteLR();		
	}
}
