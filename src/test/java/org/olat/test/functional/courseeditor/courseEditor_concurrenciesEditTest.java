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
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
/**
 * 
 * Assure that owner A and B cannot start editing the same course at the same time.
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. user1 (standardauthor) creates course Coursename<br/>
 * 2. user2 (standardstudent) is also owner of course Coursename<br/>
 * 3. cleanup in the end: course is deleted<br/>
 * 
 * Test case: <br/>
 * 1. user1 creates course "CourseName" with the element "forum" and adds user2 as owner <br/>
 * 2. user2 opens detailed view of course "CourseName" in browser 2<br/>
 * 3. user1 clicks edit "CourseName" in browser 1<br/>
 * 4. user2 clicks "Edit content" in browser 2 <br/>
 * 5. Check if the message "This course is currently edited by 'user1' and therefore locked." appears<br/>
 * 6. user1 closes editor in browser 1<br/>
 * 7. user2 clicks "Edit content" in browser 2<br/>
 * 8. Check if "This course has never been published." appears<br/>
 * </p>
 * 
 * @author kristina
 *
 */
public class courseEditor_concurrenciesEditTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	
    protected com.thoughtworks.selenium.Selenium selenium1;
    protected com.thoughtworks.selenium.Selenium selenium2;

    public void testcourseEditor_concurrenciesEditTest() throws Exception {
    	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    	OlatLoginInfos user1 = context.getStandardAuthorOlatLoginInfos(1);
    	OlatLoginInfos user2 = context.getStandardStudentOlatLoginInfos(2);

    	//user1 creates course COURSE_NAME with the element "forum" and adds user 2 as owner
    	System.out.println("logging in browser 1...");
    	OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(user1);	
    	CourseEditor courseEditor = olatWorkflow1.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
    	courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
    	LRDetailedView lRDetailedView = courseEditor.closeToLRDetailedView();
    	lRDetailedView.assignOwner(user2.getUsername());

    	//user 2 opens detailed view of course COURSE_NAME in browser 2
    	System.out.println("logging in browser 2...");
    	OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(user2);
    	LearningResources learningResources2 = olatWorkflow2.getLearningResources();
    	LRDetailedView lRDetailedView2 = learningResources2.searchMyResource(COURSE_NAME);
    	
    	//user 1  clicks edit COURSE_NAME in browser 1 
    	CourseEditor courseEditor3 = lRDetailedView.editCourseContent();

    	//user2 clicks "Edit content" in browser 2  		    	
    	boolean isCourseLocked = lRDetailedView2.checkCourseLocked(user1.getUsername());
    	assertTrue(isCourseLocked);
    	
    	//user1 closes editor in browser 1
    	courseEditor3.closeToLRDetailedView(); //course run was open via the LRDetailedView

    	//user2 clicks "Edit content" in browser 2
    	CourseEditor courseEditor4 = lRDetailedView2.editCourseContent();    	

    	//Check if "This course has never been published." appears
    	SeleniumHelper.waitUntilTextPresent(lRDetailedView2.getSelenium(), "This course has never been published.", 20);

    	//delete course
    	LRDetailedView lRDetailedView3 = courseEditor4.closeToLRDetailedView();
			try {
				lRDetailedView3.deleteLR();
			} catch (Exception e) {}    	
    }
}


