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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.test.functional.codepoints.cluster;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;

import com.thoughtworks.selenium.Selenium;

/**
 * Tests CourseFactory.copyCourse() in cluster mode - with codepoints. <br/>
 * 
 * Test setup: <br/>
 * administrator adds author as owner of the "Demo Course". <br/>
 * 
 * Test case: <br/>
 * Creates 2 copies ("COPY A - DEMO COURSE" and "COPY B - DEMO COURSE") of the same "Demo Course". <br/>	 
 * Administrator and author create 2 copy of the same course simultaneously, so if the administrator starts
 * to copy and the thread_A pauses at codepoint, the author finds the course locked when tries to copy.
 * Author succeds to copy after the thread_A is continued.
 * <br/>
 * 
 * 13.09.2010: 
 * fixed to conform with the new code behaviour (a course copy is already created after click copyLR, 
 * before insert new title/description and OK).
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class CopyCourseTest extends BaseSeleneseTestCase {
		
  protected Selenium selenium_B;
  
  private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
  private final String COURSE_NAME = "CopyCourse" + System.currentTimeMillis();
    
  private CodepointClient codepointClient_A;
  	
  
  public void setUp() throws Exception {  
  	System.out.println("CopyCourseTest - setUp - START");
  	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);  	
  	
  	//clone "Demo Course" and work with the clone
  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
  	
    //author
  	OlatLoginInfos authorOlatLoginInfos = context.getStandardAuthorOlatLoginInfos(1);
    //"administrator" adds author as owner of the "Demo Course"
  	WorkflowHelper.addOwnerToLearningResource(authorOlatLoginInfos.getUsername(), COURSE_NAME);
  	System.out.println("CopyCourseTest - setUp - END");
  }
  
	
	public void testCopyCourse() throws Exception {		
		Context context = Context.getContext();
						
		//administrator		
		OLATWorkflowHelper workflow1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));		
		LRDetailedView lRDetailedView1 = workflow1.getLearningResources().searchResource(COURSE_NAME, null);
		
		//author - owner of the "Demo Course"		
		OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		LRDetailedView lRDetailedView2 = workflow2.getLearningResources().searchResource(COURSE_NAME,null);
		
		codepointClient_A = context.createCodepointClient(1);
		CodepointRef longCopyStartedCp_A = codepointClient_A.getCodepoint("org.olat.course.CourseFactory.copyCourseAfterSaveTreeModel");
		longCopyStartedCp_A.setHitCount(0);
		longCopyStartedCp_A.enableBreakpoint();
		
		//lRDetailedView1.copyLR("COPY A - DEMO COURSE", "bla");
		this.startCopyLR(lRDetailedView1.getSelenium());
				
		 //A: check if codepoint reached: longCopyStartedCp_A
		longCopyStartedCp_A.assertBreakpointReached(1, 20000);		
		System.out.println("longCopyStartedCp_A.assertBreakpointReached");
				    				
		selenium_B = lRDetailedView2.getSelenium();
		selenium_B.click("ui=learningResources::toolbox_learningResource_copy()");
		selenium_B.waitForPageToLoad("30000");	
		//course still locked
		for (int second = 0;; second++) { 
			if (second >= 120) fail("timeout");
			try { 
        //Make sure that the string really exists in the _en property file 
				if (selenium_B.isTextPresent("This course is currently locked by") || 
						selenium_B.isTextPresent("This course is currently edited by")) break; 
			} catch (Exception e) {}
			Thread.sleep(1000);
		}
		//A: continue threads hold by longCopyStartedCp_A
		TemporaryPausedThread[] threadsA = longCopyStartedCp_A.getPausedThreads();
		threadsA[0].continueThread();
		longCopyStartedCp_A.disableBreakpoint(true);
		Thread.sleep(3000);
		this.finishCopyLR(lRDetailedView1.getSelenium(), "COPY A - DEMO COURSE", "bla");
		
		Thread.sleep(5000);
		lRDetailedView2.copyLR("COPY B - DEMO COURSE", null);		
				
		workflow1.logout();
		workflow2.logout();				
	}
	
	/**
	 * 
	 * @param selenium
	 */
	private void startCopyLR(Selenium selenium) {
		if(selenium.isElementPresent("ui=learningResources::toolbox_learningResource_copy()")) {
			selenium.click("ui=learningResources::toolbox_learningResource_copy()");
						
		} else {
			throw new IllegalStateException("Cannot copy learning resource!");
		}		
	}

	/**
	 * 
	 * @param selenium
	 * @param newTitle
	 * @param newDescription
	 */
	private void finishCopyLR(Selenium selenium, String newTitle, String newDescription) {
		selenium.type("ui=learningResources::dialog_title()", newTitle);	
		if(newDescription!=null) {
			selenium.type("ui=learningResources::dialog_description()", newDescription);
		}
		selenium.click("ui=commons::save()");				
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()"); 
	}
	
	
	/**
	 * Deletes the created copies.
	 */
	@Override
	public void cleanUpAfterRun() {		
		System.out.println("CopyCourseTest - cleanUpAfterRun - START");
		Context context = Context.getContext();		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));		
		workflow.getLearningResources().searchResource("COPY A - DEMO COURSE", null).deleteLR();
		workflow.getLearningResources().searchResource("COPY B - DEMO COURSE", null).deleteLR();				
		System.out.println("CopyCourseTest - cleanUpAfterRun - END");
	}
}
