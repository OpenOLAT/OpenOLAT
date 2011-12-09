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
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * admin starts course editor of a course, author tries to export/download the same course at the same time
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. admin user creates course<br/>
 * 2. author user is also owner of the course<br/>
 * 3. cleanup in the end: admin deletes course<br/>
 * 
 * Test case: <br/>
 * 1. admin creates course<br/>
 * 2. admin assigns author as owner<br/>
 * 3. admin opens courseeditor<br/>
 * 4. author views the detail view of the same course<br/>
 * 5. author tries to download the same course at the same time as admin is in the courseeditor<br/>
 * 6. assert that the correct message is displayed "This course is currently edited by admin and therefore locked."<br/>
 * 7. admin exits courseeditor<br/>
 * 8. admin deletes course <br/>
 * </p>
 * 
 * @author kristina
 *
 */


public class CourseEditor_concurrencyEditAndExportTest extends BaseSeleneseTestCase {

	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	

	
  	private CourseEditor courseEditor1;
    

    public void testCourseEditor_concurrencyEditAndExportTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OlatLoginInfos user1 = context.getStandardAdminOlatLoginInfos();
		OlatLoginInfos user2 = context.getStandardAuthorOlatLoginInfos();
		
		{
				//user1 creates course COURSE_NAME with the element "forum" and adds user 2 as owner
	    	System.out.println("logging in browser 1...");
	    	OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(user1);	
	    	courseEditor1 = olatWorkflow1.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
	    	LRDetailedView lRDetailedView = courseEditor1.closeToLRDetailedView();
	    	lRDetailedView.assignOwner(user2.getUsername());
	    	courseEditor1 = lRDetailedView.editCourseContent();	 				
		}
		
		{
			// open detailed view of 'CourseName' with browser 2
			System.out.println("logging in browser 2...");
			OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(user2);
			LRDetailedView lRDetailedView2 = workflow2.getLearningResources().searchMyResource(COURSE_NAME);
			lRDetailedView2.exportLR();			
		
			// and wait until 'This course is currently edited by user1 and therefore locked.' appears
			SeleniumHelper.waitUntilTextPresent(lRDetailedView2.getSelenium(), "This course is currently locked by "+user1.getUsername()+" due to editing purposes.", 20);		
		}
		
		{
			// now click 'Close editor'  in browser 1
			LRDetailedView lRDetailedView1 = courseEditor1.closeToLRDetailedView();						
			
			// delete course
			lRDetailedView1.deleteLR();			
		}	
	}
}

