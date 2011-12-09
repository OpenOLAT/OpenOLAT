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
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;

/**
 * 
 * Tests if subscription to a learning group with just one place is locked if many students enrol simultaneously.
 * <br/>
 * Test setup: <br/>
 * 1. Delete all learning groups from authors and students. <br/>
 * 2. Create five student test users. <br/>
 * <p>
 * Test case: <br/>
 * 1. Author goes to learning resources and creates course COURSE_NAME. <br/>
 * 2. Author goes to group management and creates learning group "learning group selenium 5". <br/>
 * 3. Author configures group to allow just one member and no waiting list. <br/>
 * 4. Author opens course editor, adds course element enrolment and selects the above created group. <br/>
 * 5. Author publishes course. <br/>
 * 6. All five students log in and start course COURSE_NAME, navigate to enrolment course element. <br/>
 * 7. Check that only one student gets into group, all other four get appropriate error message that 
 * group is already full. <br/>
 * 8. Author deletes course.  <br/>
 * 
 * @author sandra
 * 
 */

public class ConcurrentEnrolmentTest extends BaseSeleneseTestCase {
	

  protected com.thoughtworks.selenium.Selenium seleniums[] = new com.thoughtworks.selenium.Selenium[5];
  private final static String COURSE_NAME = "Course_for_Enrolment_Concurrency" + System.currentTimeMillis();
  
    public void testConcurrentEnrolmentTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(2, "enrolstudi02", standardPassword, true, false, false, false, false);
		OlatLoginInfos student03= context.createuserIfNotExists(1, "enrolstudi03", standardPassword, true, false, false, false, false);
		OlatLoginInfos student04= context.createuserIfNotExists(2, "enrolstudi04", standardPassword, true, false, false, false, false);
		OlatLoginInfos student05= context.createuserIfNotExists(1, "enrolstudi05", standardPassword, true, false, false, false, false);

	
			// Author01 creates course with learning group (max 1 participant) and enrolment course element		
			System.out.println("logging in browser 1...");
			OLATWorkflowHelper olatWorkflow_0 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			LearningResources learningResources_0 = olatWorkflow_0.getLearningResources();
			LRDetailedView lRDetailedView_0 = learningResources_0.createResource(COURSE_NAME, "Enrolment Test", LR_Types.COURSE);
			CourseRun courseRun_0 = learningResources_0.searchAndShowMyCourseContent(COURSE_NAME);
			GroupManagement groupManagement_0 = courseRun_0.getGroupManagement();
			groupManagement_0.createLearningGroup("learning group selenium 5", "fifth lg", 1, false, false);
			courseRun_0 = groupManagement_0.close();
			CourseEditor courseEditor_0 = courseRun_0.getCourseEditor();
			EnrolmentEditor enrollmentElement_0 = (EnrolmentEditor)courseEditor_0.insertCourseElement(CourseEditor.CourseElemTypes.ENROLMENT, true, null);			
			enrollmentElement_0.selectLearningGroups("learning group selenium 5");
			courseEditor_0.publishCourse();
			courseEditor_0.closeToCourseRun();
			olatWorkflow_0.logout();
					
			// student01 opens course and navigates to enrolment course element			
			courseRun_0 = this.selectEnrolment(context.getStandardStudentOlatLoginInfos(1), COURSE_NAME);
			
			// student02 opens course and navigates to enrolment course element
			System.out.println("logging in browser 2...");
			CourseRun courseRun_1 = this.selectEnrolment(student02, COURSE_NAME);
					
			// student03 opens course and navigates to enrolment course element
			System.out.println("logging in browser 3...");
			CourseRun courseRun_2 = this.selectEnrolment(student03, COURSE_NAME);
											
			// student04 opens course and navigates to enrolment course element
			System.out.println("logging in browser 4...");
			CourseRun courseRun_3 = this.selectEnrolment(student04, COURSE_NAME);
					
			// student05 opens course and navigates to enrolment course element
			System.out.println("logging in browser 5...");
			CourseRun courseRun_4 = this.selectEnrolment(student05, COURSE_NAME);
							
		// All students enrol, only student01 should get into group 		
			seleniums[0] = courseRun_0.getSelenium();
			seleniums[1] = courseRun_1.getSelenium();
			seleniums[2] = courseRun_2.getSelenium();
			seleniums[3] = courseRun_3.getSelenium();
			seleniums[4] = courseRun_4.getSelenium();
			
			seleniums[0].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
			seleniums[1].click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=learning group selenium 5)");
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
	}
    
	private CourseRun selectEnrolment(OlatLoginInfos student, String courseName) {
		Context context = Context.getContext();
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(student);
		CourseRun courseRun = olatWorkflow.getLearningResources().searchAndShowCourseContent(courseName);
		courseRun.selectCourseElement(CourseEditor.ENROLMENT_TITLE);
		return courseRun;
	}

	
		@Override
		protected void cleanUpAfterRun() {
			System.out.println("***************** cleanUpAfterRun STARTED *********************");
		//author01 deletes course
			Context context = Context.getContext();
			OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			LRDetailedView lRDetailedView = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME);
			try {
				lRDetailedView.deleteLR();
			} catch (Exception e) {}	
			System.out.println("***************** cleanUpAfterRun ENDED *********************");
		}
	
}
