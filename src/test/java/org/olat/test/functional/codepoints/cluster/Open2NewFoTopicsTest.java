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
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

/**
 * Tests that the org.olat.course.nodes.FOCourseNode.doInSync codepoint works correctly: that is no 2 threads could get
 * into the doInSync block at any time.
 * <br/>
 * 
 * Test setup:
 * Needs a clean "Demo Course" with a "Forum" node.
 * Creates 2 message topics suppose that a "Forum" node doesn't have any topics yet,
 * and even more the node was not selected yet.
 * <br/>
 * 
 * Test case: <br/>
 * 2 users try to open a new forum topic simultaneously. <br/>
 * Asserts that only one thread reaches the FOCourseNode.doInSync codepoint at any time.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class Open2NewFoTopicsTest extends BaseSeleneseTestCase {
	
  protected Selenium selenium_A;
  protected Selenium selenium_B;
  private CodepointClient codepointClient_A;
  private CodepointClient codepointClient_B;
  
  private String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
  private final String COURSE_NAME = "Demo Course 2NCp" + System.currentTimeMillis();
  
  public void setUp() throws Exception {  
	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
	//clone "Demo Course" and work with the clone
	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
  }
	
	
	public void testOpen2NewFoTopics() throws Exception {
		    Context context = Context.getContext();			
			
			OLATWorkflowHelper workflow_A = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
			CourseRun courseRun_A = workflow_A.getLearningResources().searchAndShowCourseContent(COURSE_NAME);	
			selenium_A = courseRun_A.getSelenium();
			
			OLATWorkflowHelper workflow_s = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
			CourseRun courseRun_B = workflow_s.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			selenium_B = courseRun_B.getSelenium();

			//codepoints node A
			codepointClient_A = context.createCodepointClient(1);
			CodepointRef beforeSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.nodes.FOCourseNode.beforeDoInSync");
			beforeSyncCp_A.setHitCount(0);
			beforeSyncCp_A.enableBreakpoint();
			
			CodepointRef doInSyncCp_A = codepointClient_A.getCodepoint("org.olat.course.nodes.FOCourseNode.doInSync");
			doInSyncCp_A.setHitCount(0);
			doInSyncCp_A.enableBreakpoint();
			
			//codepoints node B
			codepointClient_B = context.createCodepointClient(2);
			CodepointRef beforeSyncCp_B = codepointClient_B.getCodepoint("org.olat.course.nodes.FOCourseNode.beforeDoInSync");
			beforeSyncCp_B.setHitCount(0);
			beforeSyncCp_B.enableBreakpoint();
			
			CodepointRef doInSyncCp_B = codepointClient_B.getCodepoint("org.olat.course.nodes.FOCourseNode.doInSync");
			doInSyncCp_B.setHitCount(0);
			doInSyncCp_B.enableBreakpoint();
			
			//select forum in node A
			
			courseRun_A.selectAnyButGetToRoot("Activation Interaction", PageLoadWait.NO_WAIT);
            //check if codepoint reached, if yes continue
			beforeSyncCp_A.assertBreakpointReached(1, 20000);
			System.out.println("beforeSyncCp_A.assertBreakpointReached");
			TemporaryPausedThread[] threadsA = beforeSyncCp_A.getPausedThreads();
			threadsA[0].continueThread();
			
			doInSyncCp_A.assertBreakpointReached(1, 10000);
			System.out.println("doInSyncCp_A.assertBreakpointReached");
										
			//B stops at beforeSyncCp_B		
			courseRun_B.selectAnyButGetToRoot("Activation Interaction", PageLoadWait.NO_WAIT);			
			beforeSyncCp_B.assertBreakpointReached(1, 10000);			
			System.out.println("beforeSyncCp_B reached");
						
			//continue B and check that the doInSyncCp_B was not reached
			TemporaryPausedThread[] threadsB = beforeSyncCp_B.getPausedThreads();
			threadsB[0].continueThread();
			doInSyncCp_B.assertBreakpointNotReached(20000);					
			System.out.println("beforeSyncCp_B continues ... but doInSyncCp_B still not reached");
			
			//continue A 
			threadsA = doInSyncCp_A.getPausedThreads();
			threadsA[0].continueThread();
			System.out.println("doInSyncCp_A continues...");
			
			//if doInSyncCp_B is reached, continue
			doInSyncCp_B.assertBreakpointReached(1, 20000);			
			System.out.println("doInSyncCp_B reached");			
			threadsB = doInSyncCp_B.getPausedThreads();
			threadsB[0].continueThread();
			System.out.println("doInSyncCp_B continues");
			
			selenium_A.waitForPageToLoad("10000");
			courseRun_A.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			selenium_A.click("ui=course::content_forum_newTopic()");
			selenium_A.waitForPageToLoad("30000");
			System.out.println("A opens new topic");
			
			selenium_B.waitForPageToLoad("10000");
			courseRun_B.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			selenium_B.click("ui=course::content_forum_newTopic()");
			selenium_B.waitForPageToLoad("30000");
			System.out.println("B opens new topic");
					
			selenium_A.type("ui=course::content_forum_typeMsgTitle()", "CLUSTER <A> TOPIC");
			System.out.println("A types in a title");
						
			selenium_B.type("ui=course::content_forum_typeMsgTitle()", "CLUSTER <B> TOPIC");
			System.out.println("B types in a title");
			
			selenium_A.type("ui=course::content_forum_clickMsgBody()",	"message body: CLUSTER A TOPIC");
			selenium_A.click("ui=course::content_forum_save()");
			selenium_A.waitForPageToLoad("30000");
			
			selenium_B.type("ui=course::content_forum_clickMsgBody()",	"message body: BBB BBBBBB BBBB");
			selenium_B.click("ui=course::content_forum_save()");
			selenium_B.waitForPageToLoad("30000");
			
			selenium_A.click("ui=tabs::logOut()");
			selenium_B.click("ui=tabs::logOut()");
			
	}
	
	@Override
	public void cleanUpAfterRun() {
		System.out.println("cleanUpAfterRun - START");
		WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
		System.out.println("cleanUpAfterRun - END");
	}
	
}
