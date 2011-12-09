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
package org.olat.test.functional.group.management;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.group.Group;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.home.Home;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Adds all collaborative tools to learning group and checks their configuration, part of test suite GroupManagementCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Authors goes to learning resources and opens course COURSE_NAME. <br/>
 * 2. Author opens group management and edits group GROUP_NAME_1. <br/>
 * 3. Author configures group tools to info on, contact form on, calendar on, folder on, forum on, wiki on, chat off.  <br/>
 * 4. Author edits group info message. <br/>
 * 5. Author starts group and writes forum message. <br/>
 * 6. Student selects group GROUP_NAME_1 from Home - My groups. <br/>
 * 7. Student checks if info message is present. <br/>
 * 8. Student replies to forum message. <br/>
 * 9. Student checks if he can see members.<br/>
 * 10. Student checks if calendar is present. <br/>
 * 11. Student starts course and checks if enrolment course element is present. <br/>
 * 
 * @author sandra
 * 
 */


public class ConfigureToolsLearningGroup extends BaseSeleneseTestCase {

	@Test(dependsOnGroups={GroupManagementCombiTest.SECOND})
	public void testConfigureToolsLearningGroupTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(1, GroupManagementCombiTest.STUDENT_USER_NAME, standardPassword, true, false, false, false, false);
		
		// refactored with abstraction layer
		OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		LearningResources learningResources1 = olatWorkflow1.getLearningResources();
		LRDetailedView lRDetailedView1 = learningResources1.searchMyResource(GroupManagementCombiTest.COURSE_NAME);
		CourseRun courseRun1 = lRDetailedView1.showCourseContent();
		GroupAdmin groupAdmin1 = courseRun1.getGroupManagement().editLearningGroup(GroupManagementCombiTest.GROUP_NAME_1);
		groupAdmin1.setTools(true, true, true, true, true, true, false);
		groupAdmin1.setInfo("hello everybody");
		Thread.sleep(5000);
		groupAdmin1.setMemberDisplayOptions(null, true, null);
		Thread.sleep(5000);
		Group group1 = groupAdmin1.start(GroupManagementCombiTest.GROUP_NAME_1);
		Thread.sleep(5000);
		Forum forum1 = group1.selectForum();
		forum1.openNewTopic("welcome", "welcome everybody");
		olatWorkflow1.logout();
		
		// log in student02
		OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(student02);
		Home home1 = olatWorkflow2.getHome();
		Group group2 = home1.selectMyGroup(GroupManagementCombiTest.GROUP_NAME_1);
		group2.selectInfo();
		assertTrue(group2.isTextPresent("hello everybody"));	
		Forum forum2 = group2.selectForum();
		Thread.sleep(10000);
		forum2.replyToTopic("welcome", "my reply", false);		
		group2.selectMembers();
		assertTrue(group2.isTextPresent(GroupManagementCombiTest.STUDENT_USER_NAME));
		assertTrue(group2.isTextPresent("srlgauthor02"));
		group2.selectCalendar();
		group2.startCourse();
		assertTrue(group2.isTextPresent("Course element of the type Enrolment"));
		olatWorkflow2.logout();
			
	}
	
	
}
	
