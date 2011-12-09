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
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests Enrolment and Waitinglist with 3 participants
 * Original name: MultiBrowserClusterEnrolmentWaitinglistTest 
 * <br/>
 * <p>
 * Test case: <br/>
 * make sure the limit on participants on group A is 1 <br/>
 * go to group management and modify the limit on gruppe A to 1 <br/>
 * log in user 2 and 3 <br/>
 * user 1 does an enrolment to gruppe A <br/>
 * user 2 does a enrol too but lands on the waitinglist <br/>
 * user 3 and the admin in parallel (which is played by selenium1) <br/>
 * user 3 does an enrol <br/>
 * admin deletes user 1 from the course <br/>
 * now in 'parallel' <br/>
 * make the asserts <br/>
 * 
 * </p>
 * 
 * @author eglis
 *
 */
public class EnrolmentWaitinglistTest extends BaseSeleneseTestCase {
	
    
    
    private OLATWorkflowHelper workflow1;
    private CourseRun courseRun1;
    
    private OLATWorkflowHelper workflow2;
    private CourseRun courseRun2;
    
    private OLATWorkflowHelper workflow3;
    
    private final String COURSE_NAME = Context.DEMO_COURSE_NAME_3;
    private final String GROUP_NAME = "Gruppe A"; 
    private final String ENROLMENT_ELEMENT = "Einschreibung";

    public void testMultiBrowserClusterNewLearningArea() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos user1 = context.createuserIfNotExists(1, "enrtstusr1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(1, "enrtstusr2", standardPassword, true, true, true, true, true);
		OlatLoginInfos user3 = context.createuserIfNotExists(2, "enrtstusr3", standardPassword, true, true, true, true, true);

		// step1: make sure the limit on participants on group A is 1
		{
			System.out.println("logging in browser 1...");
			workflow1 = context.getOLATWorkflowHelper(user1);		
			courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		  // go to group management and modify the limit on gruppe A to 1
			GroupAdmin groupAdmin = courseRun1.getGroupManagement().editLearningGroup(GROUP_NAME);
			groupAdmin.removeAllWaiting();
			groupAdmin.removeAllParticipants();
			groupAdmin.configureParticipantsAndWaitingList(1, Boolean.TRUE, Boolean.TRUE);
			groupAdmin.close(GROUP_NAME);
			courseRun1.close(COURSE_NAME);			
		}
		
		// step 2: log in user 2 and 3
		{
			System.out.println("logging in browser 2...");			
			workflow2 = context.getOLATWorkflowHelper(user2);		
		}
		{
			System.out.println("logging in browser 3...");			
			workflow3 = context.getOLATWorkflowHelper(user3);		
		}

		// step 3: user 1 does an enrolment to gruppe A
		{
			CourseRun courseRun = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun = courseRun.selectEnrolment(ENROLMENT_ELEMENT);
			enrolmentRun.enrol(GROUP_NAME);
			assertTrue(enrolmentRun.isTextPresent("You have already enroled for the learning group mentioned below"));			
		}
		
		// step 4: user 2 does a enrol too but lands on the waitinglist
		{
			courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			EnrolmentRun enrolmentRun2 = courseRun2.selectEnrolment(ENROLMENT_ELEMENT);
			enrolmentRun2.enrol(GROUP_NAME);
			assertTrue(enrolmentRun2.isTextPresent("You are on the waiting list of the learning group mentioned below"));			
		}
		
		// step 5: user 3 and the admin in parallel (which is played by selenium1):
		//    user 3 does an enrol
		//    admin deletes user 1 from the course
		{
			// admin:
			GroupAdmin groupAdmin1 = courseRun1.getGroupManagement().editLearningGroup(GROUP_NAME);
			groupAdmin1.removeParticipant("enrtstusr1", false);						
			// selenium1.click("ui=dialog::Yes()"); .. but we wait with that
		
			EnrolmentRun enrolmentRun3 = workflow3.getLearningResources().searchAndShowCourseContent(COURSE_NAME).selectEnrolment(ENROLMENT_ELEMENT);			
			// selenium3.click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=Gruppe A)"); .. but we wait with that		
		
		  // now in 'parallel':
			groupAdmin1.confirmRemove();
			enrolmentRun3.enrol(GROUP_NAME);
						
		  // make the asserts:
			assertTrue(enrolmentRun3.isTextPresent("You are on the waiting list of the learning group mentioned below"));
			courseRun2.selectEnrolment(ENROLMENT_ELEMENT);
			assertTrue(courseRun2.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
				
	}
}
