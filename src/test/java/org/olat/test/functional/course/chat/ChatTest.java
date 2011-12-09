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
package org.olat.test.functional.course.chat;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.components.ChatComponent;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.group.Group;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Chattest.<br/>
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. Import a course with a test (Course_with_all_bb.zip should have a test)<br/>
 * 2. Add participants seleniumauthor and seleniumstudent to the learning_group in course <br/>
 * 3. Create a project group with the same members, and select chat as group tool<br/>
 * 4. Login with seleniumauthor in browser 1 <br/>
 * 5. Login with seleniumstudent in browser 2  <br/>
 * 6. cleanup: delete course and project group<br/>
 * <br/>
 * Test case: <br/>
 * 1. Login with seleniumstudent in browser 2  <br/>
 * 2. Login with seleniumauthor in browser 1 <br/>
 * 3. Seleniumauthor clicks link "(xy People are online)" <br/>
 * 4. Seleniumauthor searches for user seleniumstudent <br/>
 * 5. Seleniumauthor clicks username: seleniumstudent to open chat <br/> 
 * 6. Seleniumauthor writes "hello" and clicks send <br/>
 * 7. Check whether "[TIME] seleniumauthor: hello" is displayed in browser 1 <br/>
 * 8. Check whether "[TIME] seleniumauthor: hello" is displayed in browser 2 <br/>
 * 9. Seleniumstudent writes "hello back" <br/>
 * 10. Check whether "[TIME] seleniumstudent: hello back" is displayed in browser 2 <br/>
 * 11. Check whether "[TIME] seleniumstudent: hello back" is displayed in browser 1 <br/>
 * 12. Seleniumauthor logs out <br/>
 * (13. Check whether "[TIME] seleniumstudent: [hat sich ausgeloggt]" is displayed in browser 1) -> removed
 * 14. Seleniumauthor logs back in.
 * 15. Seleniumstudent goes to course and opens course chat <br/>
 * 16. Seleniumauthor goes to the same course and opens cours chat <br/>
 * 17. Both go to tab group, click on group "Chatgroup", click on "Chat" in the menu, and on "enter group chat"<br/>
 * 18. Both write a message "hello".
 * 19. Assert that memberlist is updated so that both seleniumauthor and seleniumstudent are listed<br/>
 * 20. Close group tab in browser 1 by seleniumauthor, assert that groupchat window is closed<br/>
 * 21. Assert that seleniumauthor is removed in the list of the groupchatwindow in browser 2<br/>
 * 22. Seleniumstudent closes all Chatwindows <br/>
 * 23. Seleniumstudent enters a test <br/>
 * 24. Assert that chatstatus is set to "do not disturb" during test<br/>
 * 25. Assert that chat cannot be opened during test<br/>
 * 26. finish test and reopen all chatwindows. Assert that conversationhistory with buddy remains.<br/>
 * 27. quit course <br/>
 * 28. seleniumauthor deletes course<br/>
 * 29. logout seleniumauthor<br/>
 * 30. logout seleniumstudent <br/>
 * </p>
 * 
 * @author Kristina 
 *
 */

public class ChatTest extends BaseSeleneseTestCase {
	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip"; 
  private final String COURSE_NAME_PREFIX = "ChatTest-";
  private final String COURSE_NAME = COURSE_NAME_PREFIX+System.currentTimeMillis();
  private final String CHAT_GROUP = "Chatgroup";
  private final String SELENIUM_STUDENT_FULLNAME = "Selenium Test-Student";//"selenium student";
  private final String SELENIUM_AUTHOR_FULLNAME = "Selenium Test-Author";//"selenium author";
  private final String MESSAGE1 = "hello";
  private final String MESSAGE2 = "hi there";
  private final String MESSAGE3 = "how are you";
  private final String MESSAGE4 = "terrific";
  //private final String LOGOUT_MESSAGE = "[hat sich ausgeloggt]"; //SHOULD BE ENGLISH
  
  private String studentUsername;
  private String authorUsername;
  
  @Override
  public void setUp() throws Exception {
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    studentUsername = context.getStandardStudentOlatLoginInfos(1).getUsername();
    authorUsername = context.getStandardAuthorOlatLoginInfos(1).getUsername();
    
    //cleanup first
    WorkflowHelper.deleteLearningResources(context.getStandardAdminOlatLoginInfos(1).getUsername(), COURSE_NAME_PREFIX);

    //import course
    File file = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);      
    WorkflowHelper.importCourse(file, COURSE_NAME, COURSE_NAME_PREFIX);
        
    OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    CourseRun courseRun = workflow.getLearningResources().searchAndShowMyCourseContent(COURSE_NAME);
    
    //admin adds author and student as participants to a leaning group in the imported course
    GroupManagement groupManagement = courseRun.getGroupManagement();    
    GroupAdmin groupAdmin = groupManagement.createLearningGroup("blabla_gr", "bla", -1, false, false);
    
    String[] participants = {studentUsername, authorUsername};
    groupAdmin.addMembers(participants, new String[0]);
    
    // admin creates a project group, configures chat as tool, add same members
    Groups groups = workflow.getGroups();
    if(groups.hasGroup(CHAT_GROUP)) {
      groups.deleteGroup(CHAT_GROUP);
    }
    GroupAdmin groupAdmin2 = groups.createProjectGroup(CHAT_GROUP, "group for chat testing");
    groupAdmin2.setTools(false, false, false, false, false, false, true);
    groupAdmin2.addMembers(participants, new String[0]);
    
    workflow.logout();
  }
	
	
	public void testChatTest() throws Exception {	
	  Context context = Context.getContext();
	  
	  OLATWorkflowHelper author = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
	  OLATWorkflowHelper student = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
	  
	  ChatComponent authorChatComponent = author.getChatComponent();
	  authorChatComponent.openBuddyChat(SELENIUM_STUDENT_FULLNAME);
	  
	  ChatComponent studentChatComponent = student.getChatComponent();
	  studentChatComponent.openBuddyChat(SELENIUM_AUTHOR_FULLNAME);
	  
	  authorChatComponent.sendBuddyMessage(MESSAGE1);
	  
	  Thread.sleep(3000);
	  assertTrue(studentChatComponent.isTextPresent(authorUsername+": "+MESSAGE1));
	  assertTrue(authorChatComponent.isTextPresent(authorUsername+": "+MESSAGE1));
	  
	  studentChatComponent.sendBuddyMessage(MESSAGE2);
	  Thread.sleep(3000);
	  assertTrue(studentChatComponent.isTextPresent(studentUsername+": "+MESSAGE2));
    assertTrue(authorChatComponent.isTextPresent(studentUsername+": "+MESSAGE2));
		
    student.logout();
    //Thread.sleep(3000);
    //assertTrue(authorChatComponent.isTextPresent(studentUsername+": "+LOGOUT_MESSAGE));
    
    //student logins back
    student = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
    CourseRun courseRunStudent = student.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
    ChatComponent courseChatWindowStudent = courseRunStudent.getChatComponent();
    courseChatWindowStudent.openChat(false);
    
    CourseRun courseRunAuthor = author.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
    Thread.sleep(9000);
    courseRunAuthor.getChatComponent().openChat(false);
    
    
    //project group chat
    Groups groups = student.getGroups();
    Group chatGroupStudent = groups.selectGroup(CHAT_GROUP);
    ChatComponent groupChatWindowStudent = chatGroupStudent.selectChat();
    Thread.sleep(9000);
    groupChatWindowStudent.openProjectGoupChat();
    groupChatWindowStudent.sendMessage(MESSAGE3,"Chatroom: "+CHAT_GROUP);
        
    Group chatGroupAuthor = author.getGroups().selectGroup(CHAT_GROUP);
    ChatComponent groupChatWindowAuthor = chatGroupAuthor.selectChat();
    Thread.sleep(9000);
    groupChatWindowAuthor.openProjectGoupChat();
    groupChatWindowAuthor.sendMessage(MESSAGE4,"Chatroom: "+CHAT_GROUP);
    Thread.sleep(5000);
    
    assertTrue(groupChatWindowStudent.isTextPresent(studentUsername+": "+MESSAGE3));
    assertTrue(groupChatWindowStudent.isTextPresent(authorUsername+": "+MESSAGE4));
    
    //close project group tab
    chatGroupAuthor.close(CHAT_GROUP); //20.
    assertFalse(groupChatWindowAuthor.isOpen("Chatroom: "+CHAT_GROUP));
    
    courseChatWindowStudent.closeExtWindow();
    
     //Assert that seleniumauthor is removed in the list of the groupchatwindow in browser 2<br/>      
    assertFalse(groupChatWindowStudent.hasParticipant(authorUsername, "Chatroom: "+CHAT_GROUP));
        
    //student closes all chat windows
    groupChatWindowStudent.closeExtWindow();
    
    chatGroupStudent.close(CHAT_GROUP);
    
    courseRunStudent.selectCourseTab(COURSE_NAME);
    TestElement testElement = courseRunStudent.selectTest(CourseEditor.TEST_TITLE);
    TestRun testrun = testElement.startTest();
    
    ChatComponent studentChatComponent3 = courseRunStudent.getChatComponent();
    assertFalse(studentChatComponent3.isOpenStatusChangerAvailable());
    assertFalse(studentChatComponent3.isCourseChatAvailable());
    
    testrun.finishTest(false, 0);
    
    ChatComponent finalChatComponent = student.getChatComponent();
    finalChatComponent.openBuddyChat(SELENIUM_AUTHOR_FULLNAME);
    finalChatComponent.isTextPresent(MESSAGE1);
    
    author.logout();
    student.logout();
	}
}