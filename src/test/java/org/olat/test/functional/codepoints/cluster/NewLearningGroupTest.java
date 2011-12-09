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
package org.olat.test.functional.codepoints.cluster;

import com.thoughtworks.selenium.Selenium;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;

/**
 * 
 * Tests that one cannot use the same name for creating two groups. 
 * This test uses codepoints.
 * 
 * @author eglis
 *
 */
public class NewLearningGroupTest extends BaseSeleneseTestCase {
	
    protected Selenium selenium1;
    protected Selenium selenium2;
    
    private String COURSE_NAME = Context.DEMO_COURSE_NAME_2;   
    private final String GROUP_NAME = "multibrowserclusterlearninggroup";

    /**
     * admin1 and admin2 open COURSE_NAME/Group management/Create group by use same group name.
     * Only one user should succeed to create a group with the given title, the other one should get a warning: "This group name is already being used in this context..."
     * 
     * @throws Exception
     */
    public void testMultiBrowserClusterNewLearningArea() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos user1 = context.createuserIfNotExists(1, "mbcnla1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(2, "mbcnla2", standardPassword, true, true, true, true, true);

		{
			System.out.println("logging in browser 1...");
			OLATWorkflowHelper workflow1 = context.getOLATWorkflowHelper(user1);
			CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			GroupManagement groupManagement1 = courseRun1.getGroupManagement();			
						
			// make sure the learning group does not exist yet - delete otherwise
			groupManagement1.deleteGroup(GROUP_NAME);
			
			selenium1 = groupManagement1.getSelenium();
			selenium1.click("ui=groupManagement::menu_allLearningGroups()");
			selenium1.waitForPageToLoad("30000");
			
			selenium1.click("ui=groupManagement::toolbox_groupManagement_newLearningGroup()");
			selenium1.waitForPageToLoad("30000");

			assertTrue(selenium1.isTextPresent("Create a new learning group"));

			selenium1.type("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_name()", GROUP_NAME);			
			selenium1.type("ui=commons::tinyMce_styledTextArea()", "egal oder?");
		}
		
		{
			System.out.println("logging in browser 2...");
			OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(user2);
			CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			selenium2 = courseRun2.getGroupManagement().getSelenium();
			
			selenium2.click("ui=groupManagement::toolbox_groupManagement_newLearningGroup()");
			selenium2.waitForPageToLoad("30000");
			assertTrue(selenium2.isTextPresent("Create a new learning group"));
			selenium2.type("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_name()", GROUP_NAME);			
			selenium2.type("ui=commons::tinyMce_styledTextArea()", "egal oder?");
		}

		CodepointClient codepointClientA = context.createCodepointClient(1);
		CodepointRef createAreaCpA = codepointClientA.getCodepoint("org.olat.group.BusinessGroupFactory.createAndPersistLearningGroup");
		createAreaCpA.setHitCount(0);
		createAreaCpA.enableBreakpoint();
		
		CodepointClient codepointClientB = context.createCodepointClient(2);
		CodepointRef createAreaCpB = codepointClientB.getCodepoint("org.olat.group.BusinessGroupFactory.createAndPersistLearningGroup");
		createAreaCpB.setHitCount(0);
		createAreaCpB.enableBreakpoint();

		selenium1.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");
		Thread.sleep(500);
		selenium2.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");		
		
		createAreaCpA.waitForBreakpointReached(10000);
		createAreaCpB.waitForBreakpointReached(10000);
		
		int pausedA = createAreaCpA.getPausedThreads()==null ? 0 : createAreaCpA.getPausedThreads().length;
		int pausedB = createAreaCpB.getPausedThreads()==null ? 0 : createAreaCpB.getPausedThreads().length;
		assertEquals("Asserts that there is only one thread that reached the codepoint.", 1, pausedA + pausedB);
		
		createAreaCpA.disableBreakpoint(true);
		createAreaCpB.disableBreakpoint(true);
		
		selenium1.waitForPageToLoad("30000");
		selenium2.waitForPageToLoad("30000");
		
		assertTrue(selenium1.isTextPresent("Edit group multibrowserclusterlearninggroup"));
		assertTrue(selenium2.isTextPresent("This group name is already being used in this context, please select another one."));
	}
}
