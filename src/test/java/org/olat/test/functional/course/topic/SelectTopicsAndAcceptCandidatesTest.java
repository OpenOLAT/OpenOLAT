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
package org.olat.test.functional.course.topic;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TopicAssignmentRun;
import org.olat.test.util.selenium.olatapi.course.run.TopicEditor;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Tests the new bb topic assignment. Step 3: Students choose topics and tutors accept students. 
 * <br/>
 * <p>
 * Test setup: <br/>
 * 1. course created and modified in CreateAndConfigureTopicAssignmentTest and CreateTopicInTopicAssignmentTest (TOPIC_ASSIGNMENT_COURSE) is available. <br/> 
 * 2. TUTOR1, TUTOR2, TUTOR3 from CreateAndConfigureTopicAssignmentTest are available <br/>
 * 3. prepare following users: STUDENT1, STUDENT2, STUDENT3  <br/>
 * 
 * Testcase:<br/>
 * 1. login as STUDENT1 <br/>
 * 2. go to learning resources, search form <br/>
 * 3. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 4. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 5. check if the button "create topic" is NOT available <br/>
 * 6. check if in column "topic status" all three topics have vacancies <br/>
 * 7. check if all three select links are available <br/>
 * 8. select "Topic_Tutor1" <br/>
 * 9. check if in column "topic status" value is "Temporary registration". <br/> 
 * 10. check if "deselect" link is available <br/>
 * 11. select "Topic_Tutor2", do same two checks as above <br/>
 * 12. check that student cannot select third topic any more (no select link available). <br/>
 * 13. click on title "Topic_Tutor1" <br/>
 * 14. click on tab "Folder" <br/>
 * 15. check if drop box and return box paragraphes are not available (as only temporarily registered) <br/>
 * 16. log out STUDENT1 <br/>
 * 17. login as STUDENT2 <br/>
 * 18. go to learning resources, search form <br/>
 * 19. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 20. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 21. check if in column "topic status" that first topic is filled, other two have vacancies <br/>
 * 22. check if two out of three select links are available <br/>
 * 23. select "Topic_Tutor2" <br/>
 * 24. check if in column "topic status" value is "temporary registration". <br/> 
 * 25. check if "deselect" link is available <br/>
 * 26. select "Topic_Tutor3", do same two checks as above <br/>
 * 27. log out STUDENT2 <br/>
 * 28. login as STUDENT3 <br/>
 * 29. go to learning resources, search form <br/>
 * 30. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 31. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 32. check if in column "topic status" that first two topics are filled, only third has vacancies <br/>
 * 33. check if one out of three select links are available <br/>
 * 34. select "Topic_Tutor3" <br/>
 * 35. check if in column "topic status" value is "temporary registration". <br/>
 * 36. check if "deselect" link is available <br/>
 * 37. log out STUDENT 3 <br/>
 *  * ----- now tutor view again ------ <br/>
 * 38. log in TUTOR1 <br/>
 * 39. go to learning resources, search form <br/>
 * 40. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 41. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 42. check if following two toolboxes are present "* tutored groups" with link "Topic Topic_Tutor1" and "My learning groups" with link "Administrator Topic assignment" <br/>
 * 43. check if in column "topic status" first line is "check participants". second: "filled", third: "vacancies" <br/>
 * 44. click on Topic Name "Topic_Tutor2": check if tab "Administration of participants" is not available, back <br/>
 * 45. click on Topic Name "Topic_Tutor1" <br/>
 * 46. go to tab "Administration of participants" <br/>
 * 47. check if in paragraph "Candidates" STUDENT1 is listed <br/>
 * 48. check user checkbox, click on "Transfer as participant", no e-mail notification, next <br/>
 * 49. check if in paragraph "participants accepted" user STUDENT1 is listed. <br/>
 * 50. check if in paragraph "Candidates" STUDENT1 isn't listed anymore <br/>
 * 51. click on TOPIC_ASSIGNMENT_1 <br/>
 * 52. check if in column "topic status" value is "participants accepted" <br/>
 * 53. as STUDENT1 is automatically unsubscribed from "Topic_Tutor2", the second row should have listed "vacancies" "1 of 2" and link "select" is available again. <br/> 
 * 54. log out TUTOR1 <br/>
 * 55. log in TUTOR 3  <br/>
 * 56. go to learning resources, search form <br/>
 * 57. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 58. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 59. check if in column "topic status" first line is "filled". second: "vacancies", third: "check participants" <br/>
 * 60. click on Topic Name "Topic_Tutor3" <br/>
 * 61. go to tab "Administration of participants" <br/>
 * 62. check if in paragraph "candidates" both STUDENT2 and STUDENT3 are listed.  <br/>
 * 63. check user "STUDENT3" and click "transfer as participant", no e-mail notification, next <br/>
 * 64. click on TOPIC_ASSIGNMENT_1 <br/>
 * 65. check if in column "topic status" value is still "check participants" as there are more candidates <br/>
 * 66. log out TUTOR3 <br/>
 * 67. log in TUTOR2 <br/>
 * 68. go to learning resources, search form <br/>
 * 69. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 70. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 71. check if in column "topic status" first line is "filled" second: "check participants", third: "vacancies" <br/>
 * 72. click on Topic Name "Topic_Tutor2" <br/>
 * 73. go to tab "Administration of participants" <br/>
 * 74. check STUDENT2 checkbox, click "transfer as participant", no e-mail notification, next <br/>
 * 75. log out TUTOR2 <br/>
 * 
 * </p>
 * 
 * @author sandra
 *
 */
public class SelectTopicsAndAcceptCandidatesTest extends BaseSeleneseTestCase {
	
	private final String FILLED_VACANCIES_1_of_2 = "1 of 2";
	
	@Override
	public void setUp() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		//create test users		
		OlatLoginInfos student1= context.createuserIfNotExists(2, TopicAssignmentSuite.STUDENT1, true, false, false, false, false);
		OlatLoginInfos student2= context.createuserIfNotExists(1, TopicAssignmentSuite.STUDENT2, true, false, false, false, false);
		OlatLoginInfos student3= context.createuserIfNotExists(2, TopicAssignmentSuite.STUDENT3, true, false, false, false, false);
	}

	@Test(dependsOnGroups={TopicAssignmentSuite.SECOND}, groups={TopicAssignmentSuite.THIRD})
	public void testSelectTopicsAndAcceptCandidates() throws Exception {
		System.out.println("********* SelectTopicsAndAcceptCandidatesTest **************");		
		//STUDENT1
		OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.STUDENT1));
		CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRun = courseRun1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		
		assertFalse(topicAssignmentRun.canCreateTopic());
		assertTrue(topicAssignmentRun.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
		assertTrue(topicAssignmentRun.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
		
		topicAssignmentRun.selectTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
		assertTrue(topicAssignmentRun.isTemporaryRegisterd(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_1));
		
		topicAssignmentRun.selectTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
		assertTrue(topicAssignmentRun.isTemporaryRegisterd(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_2));
		
		assertFalse(topicAssignmentRun.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
		
		TopicEditor topicEditor = topicAssignmentRun.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
		assertFalse(topicEditor.hasDropbox());
		assertFalse(topicEditor.hasReturnbox());
		workflow1.logout();
		workflow1 = null;
		
		//STUDENT2
		OLATWorkflowHelper workflow2 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(2, TopicAssignmentSuite.STUDENT2));
		CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRun2 = courseRun2.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		
		assertTrue(topicAssignmentRun2.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun2.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun2.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
		
		assertFalse(topicAssignmentRun2.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun2.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun2.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
		
		topicAssignmentRun2.selectTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
		assertTrue(topicAssignmentRun2.isTemporaryRegisterd(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun2.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_2));
		
		topicAssignmentRun2.selectTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
		assertTrue(topicAssignmentRun2.isTemporaryRegisterd(TopicAssignmentSuite.TOPIC_TITLE_3));
		assertTrue(topicAssignmentRun2.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_3));
		workflow2.logout();
		workflow2 = null;
				
		//STUDENT3		
		OLATWorkflowHelper workflow3 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.STUDENT3));
		CourseRun courseRun3 = workflow3.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRun3 = courseRun3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRun3.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
		assertTrue(topicAssignmentRun3.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun3.isFilled(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertFalse(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertFalse(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
		topicAssignmentRun3.selectTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
		assertTrue(topicAssignmentRun3.isTemporaryRegisterd(TopicAssignmentSuite.TOPIC_TITLE_3));
		assertTrue(topicAssignmentRun3.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_3));
		workflow3.logout();
		workflow3 = null;
		
		//TUTOR1		
		OLATWorkflowHelper tutor1Workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.TUTOR1));
		CourseRun courseRun1Tutor = tutor1Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRunTutor1 = courseRun1Tutor.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		topicAssignmentRunTutor1.hasTutoredGroup(TopicAssignmentSuite.TOPIC_TITLE_1);//"Topic Topic_Tutor1"
		
		//ask sandra: ??? "My learning groups" with link "Administrator Topic assignment"
		assertTrue(topicAssignmentRunTutor1.getCourseRun().hasMyGroup(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1));
		assertTrue(topicAssignmentRunTutor1.hasCheckParticipants(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRunTutor1.isFilled(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRunTutor1.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
		TopicEditor topicEditor1 = topicAssignmentRunTutor1.openTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
		assertFalse(topicEditor1.hasAdminTab());
		topicAssignmentRunTutor1 = topicEditor1.back();
		
		topicEditor1 = topicAssignmentRunTutor1.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
		assertTrue(topicEditor1.hasCandidate(TopicAssignmentSuite.STUDENT1));
		topicEditor1.moveAsParticipant(TopicAssignmentSuite.STUDENT1);
		assertTrue(topicEditor1.hasParticipant(TopicAssignmentSuite.STUDENT1));
		assertFalse(topicEditor1.hasCandidate(TopicAssignmentSuite.STUDENT1));
		
		topicAssignmentRunTutor1 = courseRun1Tutor.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRunTutor1.hasParticipantsAccepted(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRunTutor1.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRunTutor1.isTextPresent(FILLED_VACANCIES_1_of_2));
		assertTrue(topicAssignmentRunTutor1.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		tutor1Workflow.logout();
		
		//TUTOR3				
		OLATWorkflowHelper tutor3Workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(2, TopicAssignmentSuite.TUTOR3));
		CourseRun courseRun3Tutor = tutor3Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRunTutor3 = courseRun3Tutor.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRunTutor3.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRunTutor3.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRunTutor3.hasCheckParticipants(TopicAssignmentSuite.TOPIC_TITLE_3));
		TopicEditor topicEditor3 = topicAssignmentRunTutor3.openTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
		assertTrue(topicEditor3.hasCandidate(TopicAssignmentSuite.STUDENT2));
		assertTrue(topicEditor3.hasCandidate(TopicAssignmentSuite.STUDENT3));
		topicEditor3.moveAsParticipant(TopicAssignmentSuite.STUDENT3);
		
		topicAssignmentRunTutor3 = courseRun3Tutor.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRunTutor3.hasCheckParticipants(TopicAssignmentSuite.TOPIC_TITLE_3));
		tutor3Workflow.logout();
		
		//TUTOR2		
		OLATWorkflowHelper tutor2Workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.TUTOR2));
		CourseRun courseRun2Tutor = tutor2Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRunTutor2 = courseRun2Tutor.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRunTutor2.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRunTutor2.hasCheckParticipants(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRunTutor2.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
		TopicEditor topicEditor2 = topicAssignmentRunTutor2.openTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
		topicEditor2.moveAsParticipant(TopicAssignmentSuite.STUDENT2);
		tutor2Workflow.logout();
	}
}
