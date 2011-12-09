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
 * Test checks, if it is ensured that each learning resource has at least one owner. Course is deleted at the end<br/>
 * <p>
 * <br/>
 * Test setup: <br/>
 * 1. Authors logs in in Browser 1 and creates course and adds Student as owner <br/>
 * 2. Student logs in in Browser 2 <br/>
 * <br/>
 * Test case: <br/>
 * 1. user1 creates course COURSE_NAME with two owners (user1 and user2)<br/>
 * 2. user2 enters the course <br/>
 * 3. user1 removes user2 as owner <br/>
 * 4. user1 tries to remove himself as owner <br/>
 * 5. check, if the message "At least one user is required in a group" appears <br/>
 * 6. delete the course COURSE_NAME <br/>
 * </p>
 * 
 * @author kristina
 */

public class lr_RemoveOwnerTest extends BaseSeleneseTestCase {	
	
    private final String COURSE_NAME = "CourseName" + System.currentTimeMillis();
    

    public void testlr_RemoveOwnerTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OlatLoginInfos user1 = context.getStandardAuthorOlatLoginInfos();
		OlatLoginInfos user2 = context.getStandardStudentOlatLoginInfos();
		
		System.out.println("logging in browser 1...");
		OLATWorkflowHelper olatWorkflowHelper1 = context.getOLATWorkflowHelper(user1);			

		LRDetailedView lRDetailedView1 = null;
		try {
		// user1 creates course CourseName with two owners				
		CourseEditor courseEditor1 = olatWorkflowHelper1.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "CourseDescription");
		lRDetailedView1 = courseEditor1.closeToLRDetailedView();
		//assign only one new owner
		lRDetailedView1.assignOwner(user2.getUsername());

		// user2 opens detail view of COURSE_NAME with browser 2			
		System.out.println("logging in browser 2...");
		OLATWorkflowHelper olatWorkflowHelper2 = context.getOLATWorkflowHelper(user2);	
		LRDetailedView lRDetailedView2 = olatWorkflowHelper2.getLearningResources().searchMyResource(COURSE_NAME);

		// now remove user2 in browser 1
		lRDetailedView1.removeOwner(user2.getUsername());

		// now user1 tries to remove himself in browser 1
		Boolean successfullyRemoved = lRDetailedView1.removeOwner(user1.getUsername());
		assertNotNull(successfullyRemoved);
		assertFalse(successfullyRemoved);
		} finally {
			if(lRDetailedView1!=null) {
		      lRDetailedView1.deleteLR();
			}
		}

		// excellent, close course in browser 2
		// selenium2.click("ui=tabs::closeCourse(nameOfCourse=CourseName)");
				
	}
}
