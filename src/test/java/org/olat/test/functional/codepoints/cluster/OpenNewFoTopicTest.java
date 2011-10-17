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
* <p>
*/ 
package org.olat.test.functional.codepoints.cluster;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.PageLoadWait;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.StructureElement;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

/**
 * Tests that the org.olat.course.nodes.FOCourseNode.doInSync codepoint is reached.
 * <br/>
 * 
 * Test case: <br/>
 * Opens a new Forum topic in a clean "Demo Course" with no forum threads into the "Forum" node. 
 * Asserts codepoint reached.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OpenNewFoTopicTest extends BaseSeleneseTestCase {
	
  private CodepointClient codepointClient_A;
	
  private String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
  private final String COURSE_NAME = "OpenNewFoTopic" + System.currentTimeMillis();
  
  public void setUp() throws Exception {  
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
	//clone "Demo Course" and work with the clone
	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
  }
	
	/**
	 * Opens a new Forum topic in a clean "Demo Course" with no forum threads into the "Forum" node.
	 * @throws Exception
	 */
	public void testOpenNewFoTopic() throws Exception {
		Context context = Context.getContext();
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));

		//open demo course copy
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);		
		
		//initialize codepoints
		codepointClient_A = context.createCodepointClient(1);
		CodepointRef beforeSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.nodes.FOCourseNode.beforeDoInSync");
		beforeSyncCp_A.setHitCount(0);
		beforeSyncCp_A.enableBreakpoint();
		
		CodepointRef doInSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.nodes.FOCourseNode.doInSync");
		doInSyncCp_A.setHitCount(0);
		doInSyncCp_A.enableBreakpoint();
		
		//trigger "Activation Content" which in turn loads each of its children for displaying the Previews -> Changed behavior of test!
		StructureElement selectActivation = courseRun.selectAnyButGetToRoot("Activation Interaction", PageLoadWait.NO_WAIT);
		selenium = selectActivation.getSelenium();
		
		// ASSERTION check if codepoint reached, if yes continue
		beforeSyncCp_A.assertBreakpointReached(1, 10000);
		System.out.println("beforeSyncCp_A.assertBreakpointReached");
		TemporaryPausedThread[] threadsA = beforeSyncCp_A.getPausedThreads();
		threadsA[0].continueThread();
		
		doInSyncCp_A.assertBreakpointReached(1, 10000);
		System.out.println("doInSyncCp_A.assertBreakpointReached");
		threadsA = doInSyncCp_A.getPausedThreads(); //overwrite threadsA
		threadsA[0].continueThread();
		
		// activate actual "Forum" content for proceeding
		selenium.waitForPageToLoad("30000");//wait for previous NO_WAITED Action
		courseRun.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
		
		if(selenium.isElementPresent("ui=course::content_forum_displayForum()")) {
		  selenium.click("ui=course::content_forum_displayForum()");
		  selenium.waitForPageToLoad("30000");
		}
				
		selenium.click("ui=course::content_forum_newTopic()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=course::content_forum_typeMsgTitle()", "TODAY'S TOPIC");
		selenium.type("ui=course::content_forum_clickMsgBody()", "message body");
		selenium.click("ui=course::content_forum_save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=tabs::logOut()");
	}

	@Override
	public void cleanUpAfterRun() {
		System.out.println("cleanUpAfterRun - START");
		LearningResources learningResources = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1)).getLearningResources();
		learningResources.searchResource(COURSE_NAME, null).deleteLR();
		System.out.println("cleanUpAfterRun - END");
	}
	
	
	
}
