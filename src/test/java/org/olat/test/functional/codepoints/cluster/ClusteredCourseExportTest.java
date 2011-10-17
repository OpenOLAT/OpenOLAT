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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.test.functional.codepoints.cluster;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

  /**
   * Tests that no course export, copy and edit could occur concurrently - clusterwide - 
   * but protected by a GUI lock.
   * <br/>
   * Test setup: <br/>
   * - admin clones DEMO_COURSE_NAME and adds author as owner to this. <br/>
   * - admin and author login on separate nodes 
   * (Theoretically, since this depends of the multiVmOlatUrl1 and multiVmOlatUrl2 in customcontext.xml)
   * <br/>
   * Test case: <br/>
   * - author opens "Demo Course" and start editing course <br/>
	 * - administrator wants to export course - but it is locked <br/>
	 * - author closes course editor <br/>
	 * - administrator exports course - threadA stops at codepoint - <br/>
	 * - author want to open course editor but course locked for export - threadA stopped at codepoint - <br/>
	 * - continue threadA - exports finishes - lock released <br/>
	 * - author opens successfully course editor <br/>
	 * - logout
	 * 
   * @author Lavinia Dumitrescu
   *
   */
public class ClusteredCourseExportTest extends BaseSeleneseTestCase {
		
  private OLATWorkflowHelper workflow_A; //administrator on node 1
  private OLATWorkflowHelper workflow_B; //author on node 2
  private CodepointClient codepointClient_A;
    
  private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
  private final String COURSE_NAME = "ClusteredCourseExportTest" + System.currentTimeMillis();
  
	/**
	 * "administrator" adds author as owner of the "Demo Course"
	 */
	public void setUp() throws Exception { 
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		//clone DEMO_COURSE_NAME and work with the clone
		assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
				
		//"administrator" adds author as owner of the "Demo Course"
		String authorUsername = Context.getContext().getStandardAuthorOlatLoginInfos(1).getUsername();
		WorkflowHelper.addOwnerToLearningResource(authorUsername, COURSE_NAME);
				
		workflow_A = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		workflow_B = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));
	}
	
	/**
	 * Tests that no course export, copy and edit could occur concurently - clusterwide - 
	 * but protected by a GUI lock.
	 * Steps:
	 * - author opens "Demo Course" and start editing course
	 * - administrator wants to export course - but it is locked
	 * - author closes course editor
	 * - administrator exports course - threadA stops at codepoint - 
	 * - author want to open course editor but course locked for export - threadA stopped at codepoint - 
	 * - continue threadA - exports finishes - lock released
	 * - author opens successfully course editor
	 * - logout
	 * 
	 * @throws Exception
	 */
	public void testExportCourse() throws Exception {	
				
		Context context = Context.getContext();
		codepointClient_A = context.createCodepointClient(1);
		CodepointRef longExportStartedCp_A = codepointClient_A.getCodepoint("org.olat.course.CourseFactory.longExportCourseToZIP");
		longExportStartedCp_A.setHitCount(0);
		longExportStartedCp_A.enableBreakpoint();
		
		//author opens "Demo Course"
		CourseEditor courseEditor_B = workflow_B.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
				
		//administrator wants to export course - but it is locked
		LRDetailedView lRDetailedView_A = workflow_A.getLearningResources().searchResource(COURSE_NAME, null);
		Selenium selenium_A = lRDetailedView_A.getSelenium();			
		selenium_A.click("ui=learningResources::toolbox_learningResource_exportContent()");
		//no need for waitForPageToLoad since the message shows up immediately for a very short time
		
		SeleniumHelper.waitUntilTextPresent(selenium_A, "This course is currently locked", 20);
		
		//author closes course editor and releases lock
		CourseRun courseRun_B  = courseEditor_B.closeToCourseRun();
				
		//administrator exports course
		selenium_A.click("ui=learningResources::toolbox_learningResource_exportContent()");		
		//pause at breakpoint
		longExportStartedCp_A.assertBreakpointReached(1, 20000);
				
		//author tries to reopen course editor and gets "already locked" warning
		Selenium selenium_B = courseRun_B.getSelenium();
		selenium_B.click("ui=course::toolbox_courseTools_courseEditor()");
		//no need for waitForPageToLoad since the message shows up immediately for a very short time
		for (int second = 0;; second++) { 
			if (second >= 120) fail("timeout"); //WARNING: HERE POTENTIAL FAILURE EACH TIME THE TRANSLATION CHANGES!
			try { 
        //Make sure that the string really exists in the _en property file 
				if (selenium_B.isTextPresent("This course is being locked by") || 
						selenium_B.isTextPresent("This course is being edited by")) break; 
			} catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		//continue threads
		TemporaryPausedThread[] threadsA = longExportStartedCp_A.getPausedThreads();		
		threadsA[0].continueThread();
		//Thread.sleep(1000);
		Thread.sleep(3000);
		
		//author opens successfully the course editor 
		selenium_B.click("ui=course::toolbox_courseTools_courseEditor()");		
		boolean isLockedByEncountered = false;
		boolean isEditedByEncountered = false;
		for (int second = 0;; second++) { 
			if (selenium_B.isTextPresent("This course was last published on")) break; 
			if (second >= 120) {
				if (isLockedByEncountered) {
					fail("Timeout. But encountered String 'This course is currently locked by'. Maybe that's the actual problem?");
				}
				if (isEditedByEncountered) {
					fail("Timeout. But encountered String 'This course is currently edited by'. Maybe that's the actual problem?");
				}
				fail("Timeout");
			}
			try { 
        //Make sure that the string really exists in the _en property file 
				if (selenium_B.isTextPresent("This course was last published on")) break; 
				if (selenium_B.isTextPresent("This course is currently locked by")) {
					System.out.println("Encountered String 'This course is currently locked by'!!!!!");
					isLockedByEncountered = true;
				}
				if (selenium_B.isTextPresent("This course is currently edited  by")) {
					System.out.println("Encountered String 'This course is currently edited  by'!!!!!");
					isEditedByEncountered = true;
				}
			} catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		selenium_A.click("ui=tabs::logOut()");					
		selenium_B.click("ui=tabs::logOut()");
	}
		

}
