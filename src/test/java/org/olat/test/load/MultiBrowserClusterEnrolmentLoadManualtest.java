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
package org.olat.test.load;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * This is a loadtest and is therefore excluded from the nightly selenium test set.
 * The name being lowercase it is excluded from the rule 'include *Test*'
 * @author Stefan
 *
 */
public class MultiBrowserClusterEnrolmentLoadManualtest extends BaseSeleneseTestCase {
	
    protected com.thoughtworks.selenium.Selenium selenium1;
    protected com.thoughtworks.selenium.Selenium selenium2;
    protected com.thoughtworks.selenium.Selenium selenium3;

    public void testMultiBrowserClusterNewLearningArea() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos().getPassword();
		OlatLoginInfos user1 = context.createuserIfNotExists(1, "enrtstusr1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(2, "enrtstusr2", standardPassword, true, true, true, true, true);
		OlatLoginInfos user3 = context.createuserIfNotExists(1, "enrtstusr3", standardPassword, true, true, true, true, true);

		// step1: make sure the limit on participants on group A is 1
		OLATWorkflowHelper workflow1;
		OLATWorkflowHelper workflow2;
		OLATWorkflowHelper workflow3;
		{
			System.out.println("logging in browser 1...");
			
			workflow1 = context.getOLATWorkflowHelper(user1);
			CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_3);

			// go to group management and modify the limit on gruppe A to 1
			GroupManagement groupManagement1 = courseRun1.getGroupManagement();
			GroupAdmin editLearningGruppeA = groupManagement1.editLearningGroup("Gruppe A");
			editLearningGruppeA.removeAllWaiting();
			editLearningGruppeA.removeAllParticipants();
			editLearningGruppeA.configureParticipantsAndWaitingList(1, false, false);
			editLearningGruppeA.close("Gruppe A");		
			
			courseRun1.close(Context.DEMO_COURSE_NAME_3);
		}
		
		// relogin user1 step 2: log in user 2 and 3
		CourseRun courseRun1;
		CourseRun courseRun2;
		CourseRun courseRun3;
		
		{
			workflow1 = context.getOLATWorkflowHelper(user1);
			courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_3);
			
			System.out.println("logging in browser 2...");
			workflow2 = context.getOLATWorkflowHelper(user2);
			courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_3);
			
			System.out.println("logging in browser 3...");
			workflow3 = context.getOLATWorkflowHelper(user3);
			courseRun3 = workflow3.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_3);
		}

		
		// now spawn 3 threads all racing to get the enrolment and releasing after a random amount of time
		final CourseRun[] courseRuns = new CourseRun[] {courseRun1, courseRun2, courseRun3};
		final List<String> errorList = new LinkedList<String>();
		List<Thread> l = new LinkedList<Thread>();
		for (int i=0; i<courseRuns.length; i++) {
			final CourseRun courseRun = courseRuns[i];
			final int id = i;
			
			Thread th = new Thread(new Runnable() {
				CourseRun myCourseRun = courseRun;
				public void run() {				
					Random r = new Random();
					
					while(true) {
						System.out.println(id+" Nanosleep...");
						try {
							Thread.sleep(r.nextInt(20));
						} catch (InterruptedException e1) {
							break;
						}
						System.out.println(id+" Clicking on Einschreibung...");
						EnrolmentRun selectEnrolment = myCourseRun.selectEnrolment("Einschreibung");
						System.out.println(id+" Nanosleep2...");
						try {
							Thread.sleep(r.nextInt(50));
						} catch (InterruptedException e1) {
							break;
						}
						System.out.println(id + "Let's look for 'enrol'...");
						if ( ! selectEnrolment.alreadyEnrolled("Gruppe A")) {
							Selenium s = null;
							try{
								System.out.println(id+" Let's click 'enrol'...");
								selectEnrolment.enrol("Gruppe A");
								System.out.println(id+" We clicked enrol.");
								
								s = selectEnrolment.getSelenium();
								// let's see if we won or not
								if (s.getBodyText().contains("In the meantime this group is complete.")) {
//								if (s.isTextPresent("In the meantime this group is complete.")) {
									// oh no, we lost!
									System.out.println(id+" but we lost... click 'OK'");
									s.click("ui=dialog::OK()");
									try {
										Thread.sleep(250);
									} catch (InterruptedException e) {
										break;
									}
									System.out.println(id+" and ocntinue...");
									continue;
								}
								System.out.println(id+" We got it!!!!");
								// otherwise:
								//     we're the winner!!!
								if (!s.isTextPresent("You have already enroled for the learning group mentioned below")) {
									errorList.add("enrol somehow didn't work (1)");
									return;
								}
								System.out.println(id+" Nanosleep 3...");
								try {
									Thread.sleep(r.nextInt(200));
								} catch (InterruptedException e) {
									break;
								}
								if (!s.isTextPresent("You have already enroled for the learning group mentioned below")) {
									errorList.add("enrol somehow didn't work (2)");
									return;
								}
								
								System.out.println(id+" cancel enrolment...");
								selectEnrolment.cancelEnrolment("Gruppe A");
								System.out.println(id+" cancel enrolment done. sleep a bit...");
								try {
									Thread.sleep(r.nextInt(100));
								} catch (InterruptedException e) {
									break;
								}
							} catch(SeleniumException e) {
								// ok, someone else was quicker
								
								if (s != null && !s.isTextPresent("Learning group is complete")) {
									errorList.add("expected 'learning group is complete' but didn't get it");
									return;
								}
								continue;
							}
						}
						
					}
					
				}
				
			});
			th.setDaemon(true);
			th.start();
			l.add(th);
		}
		
		
		Thread.sleep(180000);
		System.out.println("Stopping all threads...");
		for (Iterator<Thread> it = l.iterator(); it.hasNext();) {
			Thread th = it.next();
			th.interrupt();
			System.out.println("Waiting for a thread...");
			th.join();
			System.out.println("Thread stopped.");
		}
		System.out.println("All threads stopped.");
		assertEquals(0, errorList.size());
	}
}
