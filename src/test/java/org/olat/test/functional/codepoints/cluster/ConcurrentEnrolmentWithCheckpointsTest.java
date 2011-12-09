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

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

/**
 * Tests concurrent Enrollment with codepoints.
 * 
 * Test case: <br/>
 * 5 users try to subscribe to group with just one place, only one should get into group.
 * <br/>
 * Make sure that the rest get appropriate error messages.
 * 
 * 
 *
 */
public class ConcurrentEnrolmentWithCheckpointsTest extends BaseSeleneseTestCase {
	
	
	private CodepointClient codepointClient_1;
    private CodepointClient codepointClient_2;
	
    protected Selenium seleniums[] = new Selenium[5];
  
    private final static String COURSE_NAME = "Course_for_Enrolment_Concurrency" + System.currentTimeMillis();


    public void testConcurrentEnrolmentWithCodepointsTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(2, "enrolstudi02", standardPassword, true, false, false, false, false);
		OlatLoginInfos student03= context.createuserIfNotExists(1, "enrolstudi03", standardPassword, true, false, false, false, false);
		OlatLoginInfos student04= context.createuserIfNotExists(2, "enrolstudi04", standardPassword, true, false, false, false, false);
		OlatLoginInfos student05= context.createuserIfNotExists(1, "enrolstudi05", standardPassword, true, false, false, false, false);
		
		{
			// Author01 creates course with learning group (max 1 participant) and enrolment course element			
			OLATWorkflowHelper olatWorkflow_0 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));			
			System.out.println("logging in browser 1...");
			LearningResources learningResources = olatWorkflow_0.getLearningResources();
			learningResources.createResource(COURSE_NAME, "Enrolment Test", LearningResources.LR_Types.COURSE);
			CourseRun courseRun = learningResources.searchAndShowMyCourseContent(COURSE_NAME);
			GroupManagement groupManagement = courseRun.getGroupManagement();
			groupManagement.createLearningGroup("learning group selenium 5", "fifth lg", 1, false, false);
			courseRun = groupManagement.close();
			CourseEditor courseEditor = courseRun.getCourseEditor();
			EnrolmentEditor enrolmentEditor = (EnrolmentEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.ENROLMENT, true, null);
			enrolmentEditor.selectLearningGroups("learning group selenium 5");
			courseEditor.publishCourse();
			courseEditor.closeToCourseRun();
			olatWorkflow_0.logout();						
		}
		
		//node 1
		codepointClient_1 = Context.getContext().createCodepointClient(1);		
		CodepointRef doInSync1Cp_1 = codepointClient_1.getCodepoint("org.olat.course.nodes.en.EnrollmentManager.doInSync1");
		doInSync1Cp_1.setHitCount(0);		
		doInSync1Cp_1.enableBreakpoint();
		
		CodepointRef doInSync2Cp_1 = codepointClient_1.getCodepoint("org.olat.course.nodes.en.EnrollmentManager.doInSync2");
		doInSync2Cp_1.setHitCount(0);		
		doInSync2Cp_1.enableBreakpoint();
		
		CodepointRef afterDoInSyncCp_1 = codepointClient_1.getCodepoint("org.olat.course.nodes.en.EnrollmentManager.afterDoInSync");
		afterDoInSyncCp_1.setHitCount(0);		
		afterDoInSyncCp_1.enableBreakpoint();
		
		//node 2
		codepointClient_2 = Context.getContext().createCodepointClient(2);			
		CodepointRef doInSyncCp_2 = codepointClient_2.getCodepoint("org.olat.course.nodes.en.EnrollmentManager.doInSync1");
		doInSyncCp_2.setHitCount(0);		
		doInSyncCp_2.enableBreakpoint();
					
		{
			// student01 opens course and navigates to enrolment course element			
			OLATWorkflowHelper olatWorkflow_0 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));	
			CourseRun courseRun = olatWorkflow_0.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
			seleniums[0] = enrolmentRun.getSelenium();			
		}
				
		{
			// student02 opens course and navigates to enrolment course element
			System.out.println("logging in browser 2...");			
			OLATWorkflowHelper olatWorkflow_1 = context.getOLATWorkflowHelper(student02);	
			CourseRun courseRun = olatWorkflow_1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
			seleniums[1] = enrolmentRun.getSelenium();								
		}
			  
	  {
			// student03 opens course and navigates to enrolment course element
			System.out.println("logging in browser 3...");			
			OLATWorkflowHelper olatWorkflow_2 = context.getOLATWorkflowHelper(student03);	
			CourseRun courseRun = olatWorkflow_2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
			seleniums[2] = enrolmentRun.getSelenium();				
		}
		
		{
			// student04 opens course and navigates to enrolment course element
			System.out.println("logging in browser 4...");			
			OLATWorkflowHelper olatWorkflow_3 = context.getOLATWorkflowHelper(student04);	
			CourseRun courseRun = olatWorkflow_3.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
			seleniums[3] = enrolmentRun.getSelenium();								
		}
		
		{
			// student05 opens course and navigates to enrolment course element
			System.out.println("logging in browser 5...");
			OLATWorkflowHelper olatWorkflow_4 = context.getOLATWorkflowHelper(student05);
			CourseRun courseRun = olatWorkflow_4.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
			seleniums[4] = enrolmentRun.getSelenium();				
		}
		
		// All students enrol, only student01 should get into group 
		{
			seleniums[0].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
			seleniums[1].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
						
			doInSync1Cp_1.assertBreakpointReached(1, 20000);
			System.out.println("doInSync1Cp_1.assertBreakpointReached");
			
			doInSyncCp_2.assertBreakpointNotReached(20000);
			System.out.println("doInSyncCp_2.assertBreakpointNotReached");
			
		  TemporaryPausedThread[] threads1 = doInSync1Cp_1.getPausedThreads();
		  threads1[0].continueThread();
		  System.out.println("doInSync1Cp_1 continue threads");
		  doInSync1Cp_1.disableBreakpoint(true);
		  
		  doInSync2Cp_1.assertBreakpointReached(1, 20000);
		  System.out.println("doInSync2Cp_1.assertBreakpointReached");
		  
		  doInSyncCp_2.assertBreakpointNotReached(20000);
		  System.out.println("doInSyncCp_2.assertBreakpointNotReached");
		  
		  threads1 = doInSync2Cp_1.getPausedThreads();
		  threads1[0].continueThread();
		  System.out.println("doInSync2Cp_1 continue threads");
		  doInSync2Cp_1.disableBreakpoint(true);
		  
		  
		  afterDoInSyncCp_1.assertBreakpointReached(1, 20000);
		  System.out.println("afterDoInSyncCp_1.assertBreakpointReached");
		  
		  doInSyncCp_2.assertBreakpointReached(1, 20000);
		  System.out.println("doInSyncCp_2.assertBreakpointReached");
		  TemporaryPausedThread[] threads2 = doInSyncCp_2.getPausedThreads();
		  threads2[0].continueThread();
		  System.out.println("doInSyncCp_2 continue threads");
		  doInSyncCp_2.disableBreakpoint(true);
		  
		  threads1 = afterDoInSyncCp_1.getPausedThreads();
		  threads1[0].continueThread();
		  System.out.println("afterDoInSyncCp_1 continue threads");
		  afterDoInSyncCp_1.disableBreakpoint(true);
		  
		  seleniums[2].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
			seleniums[3].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
			seleniums[4].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
			
			
			int numWinners = 0;
			int numLoosers = 0;
			for(int i=0; i<5; i++) {
				seleniums[i].waitForPageToLoad("30000");
				if (seleniums[i].isTextPresent("enrolled")) {
					numWinners++;
				} else if (seleniums[i].isTextPresent("In the meantime this group is complete. Please select another one.")) {
					numLoosers++;
				} else {
					fail("oups...");
				}
			}
			assertEquals("expected only 1 winner", 1, numWinners);
			assertEquals("expected only 4 loosers", 4, numLoosers);
			//assertEquals("expected only 1 loosers", 1, numLoosers);			
			
		}
		{
		  //author01 deletes course
			/*OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			LRDetailedView lRDetailedView = workflow.getLearningResources().searchMyResource(COURSE_NAME);
			lRDetailedView.deleteLR();*/			
		}		
	}
}
