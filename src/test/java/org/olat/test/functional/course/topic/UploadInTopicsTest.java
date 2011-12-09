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

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TopicAssignmentRun;
import org.olat.test.util.selenium.olatapi.course.run.TopicEditor;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Tests the new bb topic assignment. Step 4: Students hand in documents, tutors hands back documents 
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. course created and modified in CreateAndConfigureTopicAssignmentTest, CreateTopicInTopicAssignmentTest and SelectTopicsAndAcceptCandidates (TOPIC_ASSIGNMENT_COURSE) is available. <br/>
 * 2. TUTOR1, TUTOR2, TUTOR3 from CreateAndConfigureTopicAssignmentTest and STUDENT1, STUDENT2, STUDENT3 from SelectTopicsAndAcceptCandidatesTest are available <br/>
 * 3. prepare files HAND_IN_TOPIC1,HAND_IN_TOPIC2, HAND_IN_TOPIC3 and HAND_IN_TOPIC4 (e.g. pdf file) for upload in drop box <br/>
 * 4. prepare files HAND_BACK_TOPIC1,HAND_BACK_TOPIC3 (e.g. pdf file) for upload in return box <br/>
 * 
 * Testcase:<br/>
 * 1. log in as STUDENT1 <br/>
 * 2. go to learning resources, search form <br/>
 * 3. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 4. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 5. check if toolbox "my learning groups" shows "Topic Topic_Tutor1" <br/>
 * 6. check if column "topic status" shows "positive registration", "vacancies", "vacancies" <br/>
 * 7. check if neither select nor deselect links are available any more <br/>
 * 8. click on "Topic_Tutor1", go to tab "Folder" <br/>
 * 9. check if Drop box and Return box paragraphs are there. <br/>
 * 10. check if in paragraph Drop box String "you have not uploaded any files yet" is present <br/>
 * 10. upload file HAND_IN_TOPIC1 (sorry there are two no 10.) <br/>
 * 11. log out STUDENT1 <br/>
 * 12. log in as STUDENT2 <br/>
 * 13. go to learning resources, search form <br/>
 * 14. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 15. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 16. check if toolbox "my learning groups" shows "Topic Topic_Tutor2" <br/>
 * 17. check if column "topic status" shows "filled", "positive registration", "vacancies" <br/>
 * 18. check if neither select nor deselect links are available any more <br/>
 * 19. click on "Topic_Tutor1", go to tab "Folder" <br/>
 * 20. check if Drop box and Return box paragraphs are not available because STUDENT2 is registered for Topic_Tutor2 <br/>
 * 21. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 22. click on "Topic_Tutor2", go to tab "Folder" <br/>
 * 23 check if Drop box and Return box paragraphs are there <br/>
 * 24. upload file HAND_IN_TOPIC2 <br/>
 * 25. log out STUDENT2 <br/>
 * 26. log in as STUDENT3 <br/>
 * 27. go to learning resources, search form <br/>
 * 28. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 29. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 30. check if toolbox "my learning groups" shows "Topic Topic_Tutor3" <br/>
 * 31. check if column "topic status" shows "vacancies", "vacancies", "positive registration" <br/>
 * 32. check if neither select nor deselect links are available any more <br/> 
 * 33. click on "Topic_Tutor3", go to tab "Folder" <br/>
 * 34. check if Drop box and Return box paragraphs are there. <br/>
 * 35. upload files HAND_IN_TOPIC3 and HAND_IN_TOPIC4 <br/>
 * 36. log out STUDENT3 <br/>
 * -- tutor view now--- <br/>
 * 37. log in as TUTOR1 <br/>
 * 38. go to learning resources, search form <br/>
 * 28. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 29. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 30. click on "Topic_Tutor2", go to tab "Folder" <br/>
 * 31. check if drop box and return box are not available because TUTOR1 doesn't tutor this topic <br/>
 * 32. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 33. click on "Topic_Tutor1", go to tab "Folder" <br/>
 * 34. check if in both drop box and return box folder "STUDENT1" appears  <br/>
 * 35. open drop box folder "STUDENT1" and check if HAND_IN_TOPIC1 is there <br/>
 * 36. open return box folder "STUDENT1" and upload HAND_BACK_TOPIC1 <br/>
 * 37. log out TUTOR1 <br/>
 * 38. log in as TUTOR3 <br/>
 * 39. go to learning resources, search form <br/>
 * 40. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 41. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 42. click on "Topic_Tutor3", go to tab "Folder" <br/>
 * 43. check if in both drop box and return box folder "STUDENT3" appears  <br/>
 * 44. open drop box folder "STUDENT3" and check if HAND_IN_TOPIC3 and HAND_IN_TOPIC4 are there <br/>
 * 45. open return box folder "STUDENT3" and upload HAND_BACK_TOPIC3 <br/>
 * 46. log out TUTOR3 <br/>
 * --- student view now--- <br/>
 * 47. log in as STUDENT1 <br/>
 * 48. go to learning resources, search form <br/>
 * 49. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 50. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 51. click on "Topic_Tutor1", go to tab "Folder" <br/>
 * 52. check if in return box HAND_BACK_TOPIC1 is there <br/>
 * 53. log out STUDENT1 <br/>
 * 54. log in as STUDENT3 <br/>
 * 55. go to learning resources, search form <br/>
 * 56. search for TOPIC_ASSIGNMENT_COURSE and open course run <br/>
 * 57. go to bb TOPIC_ASSIGNMENT_1 <br/>
 * 58. click on "Topic_Tutor3", go to tab "Folder" <br/>
 * 59. check if in return box HAND_BACK_TOPIC3 is there <br/>
 * 60. log out STUDENT3 <br/>
 * 
 * </p>
 * 
 * @author sandra
 *
 */
public class UploadInTopicsTest extends BaseSeleneseTestCase {
	
  public void setUp() throws Exception {
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);   
  }
	
	@Test(dependsOnGroups={TopicAssignmentSuite.THIRD})
	public void testCreateAndConfigureTopicAssignment() throws Exception {
		System.out.println("********* UploadInTopicsTest **************");
				
	  //STUDENT1
    OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.STUDENT1));
    CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    TopicAssignmentRun topicAssignmentRun = courseRun1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    assertTrue(topicAssignmentRun.getCourseRun().hasMyGroup(TopicAssignmentSuite.TOPIC_TITLE_1)); //"my learning groups" shows "Topic Topic_Tutor1"
    
    assertTrue(topicAssignmentRun.isRegistered(TopicAssignmentSuite.TOPIC_TITLE_1));
    assertTrue(topicAssignmentRun.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
    assertTrue(topicAssignmentRun.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
    assertFalse(topicAssignmentRun.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
    assertFalse(topicAssignmentRun.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_1));
    
    TopicEditor topicEditor = topicAssignmentRun.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
    assertTrue(topicEditor.hasDropbox());
    assertTrue(topicEditor.hasReturnbox());
    assertTrue(topicEditor.isTextPresent("You have not uploaded any files yet"));
    
    File pdf1 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_IN_TOPIC1);
    String remotePdf1 = Context.getContext().provideFileRemotely(pdf1);
    topicEditor.uploadFileInDropBox(remotePdf1);
    workflow1.logout();
            
    //STUDENT2
    OLATWorkflowHelper workflow2 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(2, TopicAssignmentSuite.STUDENT2));
    CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    TopicAssignmentRun topicAssignmentRun1 = courseRun2.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    assertTrue(topicAssignmentRun1.getCourseRun().hasMyGroup(TopicAssignmentSuite.TOPIC_TITLE_2));
    
    assertTrue(topicAssignmentRun1.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
    assertTrue(topicAssignmentRun1.isRegistered(TopicAssignmentSuite.TOPIC_TITLE_2));
    assertTrue(topicAssignmentRun1.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_3));
    
    assertFalse(topicAssignmentRun1.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
    assertFalse(topicAssignmentRun1.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_2));
    
    TopicEditor topicEditor21 = topicAssignmentRun1.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
    assertFalse(topicEditor21.hasDropbox());
    assertFalse(topicEditor21.hasReturnbox());
    
    TopicAssignmentRun topicAssignmentRun2 = courseRun2.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    TopicEditor topicEditor22 = topicAssignmentRun2.openTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
    
    assertTrue(topicEditor22.hasDropbox());
    assertTrue(topicEditor22.hasReturnbox());
        
    File pdf2 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_IN_TOPIC2);
    String remotePdf2 = Context.getContext().provideFileRemotely(pdf2);
    topicEditor22.uploadFileInDropBox(remotePdf2);
    workflow2.logout();
    
    //STUDENT3
    OLATWorkflowHelper workflow3 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.STUDENT3));
    CourseRun courseRun3 = workflow3.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    TopicAssignmentRun topicAssignmentRun3 = courseRun3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    assertTrue(topicAssignmentRun3.getCourseRun().hasMyGroup(TopicAssignmentSuite.TOPIC_TITLE_3)); //"my learning groups" shows "Topic Topic_Tutor3"
    
    assertTrue(topicAssignmentRun3.isFilled(TopicAssignmentSuite.TOPIC_TITLE_1));
    assertTrue(topicAssignmentRun3.hasVacancies(TopicAssignmentSuite.TOPIC_TITLE_2));
    assertTrue(topicAssignmentRun3.isRegistered(TopicAssignmentSuite.TOPIC_TITLE_3));
    assertFalse(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
    assertFalse(topicAssignmentRun3.canDeselect(TopicAssignmentSuite.TOPIC_TITLE_3));
    
    TopicEditor topicEditor3 = topicAssignmentRun3.openTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
    assertTrue(topicEditor3.hasDropbox());
    assertTrue(topicEditor3.hasReturnbox());
        
    File pdf3 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_IN_TOPIC3);
    String remotePdf3 = Context.getContext().provideFileRemotely(pdf3);
    topicEditor3.uploadFileInDropBox(remotePdf3);
    File pdf4 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_IN_TOPIC4);
    String remotePdf4 = Context.getContext().provideFileRemotely(pdf4);
    topicEditor3.uploadFileInDropBox(remotePdf4);    
    workflow3.logout();
    
    //tutor view now
    //TUTOR1
    OLATWorkflowHelper workflowTutor1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(2, TopicAssignmentSuite.TUTOR1));
    CourseRun courseRunTutor1 = workflowTutor1.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    TopicAssignmentRun topicAssignmentTutor11 = courseRunTutor1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    
    TopicEditor topicEditorTutor12 = topicAssignmentTutor11.openTopic(TopicAssignmentSuite.TOPIC_TITLE_2);
    assertFalse(topicEditorTutor12.hasDropbox());
    assertFalse(topicEditorTutor12.hasReturnbox());
    
    topicAssignmentTutor11 = courseRunTutor1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    TopicEditor topicEditorTutor11 = topicAssignmentTutor11.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
    assertTrue(topicEditorTutor11.hasDropbox());
    assertTrue(topicEditorTutor11.hasReturnboxFolder(TopicAssignmentSuite.STUDENT1));
    assertTrue(topicEditorTutor11.hasFileInDropBoxFolder(TopicAssignmentSuite.STUDENT1, TopicAssignmentSuite.HAND_IN_TOPIC1));
    
    File file1 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_BACK_TOPIC1);
    String remoteFilePath1 = Context.getContext().provideFileRemotely(file1);
    topicEditorTutor11.uploadFileInReturnBoxFolder(TopicAssignmentSuite.STUDENT1, remoteFilePath1);
    workflowTutor1.logout();
    
    //TUTOR3
    OLATWorkflowHelper workflowTutor3 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.TUTOR3));
    CourseRun courseRunTutor3 = workflowTutor3.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    TopicAssignmentRun topicAssignmentTutor31 = courseRunTutor3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    TopicEditor topiceEditorTutor33 = topicAssignmentTutor31.openTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
    assertTrue(topiceEditorTutor33.hasReturnbox());
    assertTrue(topiceEditorTutor33.hasDropbox());
    assertTrue(topiceEditorTutor33.hasFileInDropBoxFolder(TopicAssignmentSuite.STUDENT3, TopicAssignmentSuite.HAND_IN_TOPIC3));
    assertTrue(topiceEditorTutor33.hasFileInDropBoxFolder(TopicAssignmentSuite.STUDENT3, TopicAssignmentSuite.HAND_IN_TOPIC4));
    
    File file2 = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + TopicAssignmentSuite.HAND_BACK_TOPIC3);
    String remoteFilePath2 = Context.getContext().provideFileRemotely(file2);
    topiceEditorTutor33.uploadFileInReturnBoxFolder(TopicAssignmentSuite.STUDENT3, remoteFilePath2);
    workflowTutor3.logout();
    
    //student view now  
   
    //STUDENT1
    workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(2, TopicAssignmentSuite.STUDENT1));
    courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    topicAssignmentRun = courseRun1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    TopicEditor topicEditor1 = topicAssignmentRun.openTopic(TopicAssignmentSuite.TOPIC_TITLE_1);
    assertTrue(topicEditor1.hasFileInReturnBoxFolder(TopicAssignmentSuite.HAND_BACK_TOPIC1));
    workflow1.logout();
    
    //STUDENT3    
    workflow3 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, TopicAssignmentSuite.STUDENT3));
    courseRun3 = workflow3.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
    topicAssignmentRun3 = courseRun3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
    topicEditor3 = topicAssignmentRun3.openTopic(TopicAssignmentSuite.TOPIC_TITLE_3);
    assertTrue(topicEditor3.hasFileInReturnBoxFolder(TopicAssignmentSuite.HAND_BACK_TOPIC3));
	}
}
