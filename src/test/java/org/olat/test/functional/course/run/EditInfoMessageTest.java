/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.test.functional.course.run;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.InfoMessageRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  4. jan 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditInfoMessageTest extends BaseSeleneseTestCase {
	
	
	private static final String COURSE_NAME = "Messages Course";
	private static final String INFO_MESSAGE_NAME = "Special messages";
	
	public void setUp() throws Exception {		
		//each test has to setup a context
		Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
	}
	
	/**
	 * Login, create an empty, logout, login, go to Learning resources, select Courses, select "Messages Course",
	 * Show content, open Course Editor, insert a Info message course element, publish course, add messages,
	 * count if the messages are there, show older messages, count the messages, show current messages, count
	 * the messages.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNewInfoMessages() throws Exception {
		//delete old courses
		WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
		
		//Get a workflow object for the default administrator user. 
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));

		//create to learning resources and the course named COURSE_NAME, get a CourseRun object
		workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_NAME).publishCourse();
		workflow.logout();
		
		//add standard author as owner
		workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		workflow.getLearningResources().searchMyResource(COURSE_NAME).assignOwner(Context.getContext().getStandardAuthorOlatLoginInfos(1).getUsername());
		workflow.logout();
		
		workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		
		//go to the course editor
		CourseEditor courseEditor = courseRun.getCourseEditor();
		
		//insert a course element of type info message, with a specified title
		CourseElementEditor courseElementEditor = courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.INFO_MESSAGE, true, INFO_MESSAGE_NAME);
		assertTrue(courseElementEditor.isTextPresent(INFO_MESSAGE_NAME));		
		
		//publish course
		courseEditor.publishCourse();
		
		//create a message
	    courseRun = courseEditor.closeToCourseRun();
	    InfoMessageRun infoRun = courseRun.selectInfoMessage(INFO_MESSAGE_NAME);
	    infoRun.createMessage("Hello 0", "Hello world 0");
	    
	    //create 11 messages more
	    for(int i=1; i<12; i++) {
	    	infoRun.createMessage("Hello " + i, "Hello world " + i);
	    }
	    
	    int found1 = countMessages(infoRun);
	    assertEquals(10, found1);
	    
	    //show and count all messages
	    infoRun.showOlderMessage();
	    int found2 = countMessages(infoRun);
	    assertEquals(12, found2);
	    
	    //show and count current messages
		infoRun.showCurrentMessage();
	    int found3 = countMessages(infoRun);
	    assertEquals(10, found3);
	    
	    //edit first message
	    infoRun.editFirstMessage();
	    assertTrue(infoRun.isMessageEdited());
	    infoRun.save();
	    
	    //delete first message
	    infoRun.deleteFirstMessage();
	    infoRun.yes();
	    infoRun.showOlderMessage();
	    int found4 = countMessages(infoRun);
	    assertEquals(11, found4);

		//logout
		workflow.logout();		
	}
	
	/**
	 * Login with the standard admin and the standard author, open "Messages Course",
	 * open course building block "Messages", standard admin edit a message, author try to edit the same message,
	 * and try to delete it. The main goal of this test is to check if the lock are correctly set and released.
	 * @throws Exception
	 */
	@Test(dependsOnMethods={"testNewInfoMessages"})
	public void testConcurrentEditMessage() throws Exception {
		//Get a workflow object for the default administrator. 
		OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		InfoMessageRun infoRun1 = courseRun1.selectInfoMessage(INFO_MESSAGE_NAME);
		
		int found1 = countMessages(infoRun1);
	    assertEquals(10, found1);
	    
		//Get a workflow object for the default author.
		OLATWorkflowHelper workflow2 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		InfoMessageRun infoRun2 = courseRun2.selectInfoMessage(INFO_MESSAGE_NAME);
		
		int found2 = countMessages(infoRun2);
	    assertEquals(10, found2);
	    
	    //check the locks on the messages
	    
	    //admin edit a message
	    infoRun1.editFirstMessage();
	    assertTrue(infoRun1.isMessageEdited());
	    //author try to edit the same message
	    infoRun2.editFirstMessage();
	    assertTrue(infoRun2.isMessageAlreadyEdited());
	    infoRun2.dialogOk();
	    //author try to delete the same message
	    infoRun2.deleteFirstMessage();
	    assertTrue(infoRun2.isMessageAlreadyEdited());
	    infoRun2.dialogOk();
	    //admin save the edit box
	    infoRun1.save();
	    
	    
	    //author edit a message
	    infoRun2.editFirstMessage();
	    assertTrue(infoRun2.isMessageEdited());
	    //admin try to edit
	    infoRun1.editFirstMessage();
	    assertTrue(infoRun1.isMessageAlreadyEdited());
	    infoRun1.dialogOk();
	    //author close the overlay
	    infoRun2.close();
	    
	    
	    //admin edit a message
	    infoRun1.editFirstMessage();
	    assertTrue(infoRun1.isMessageEdited());
	    //author try to edit
	    infoRun2.editFirstMessage();
	    assertTrue(infoRun2.isMessageAlreadyEdited());
	    infoRun2.dialogOk();
	    //admin close the overlay
	    infoRun1.cancel();
	    
	    
	    //author edit a message
	    infoRun2.editFirstMessage();
	    assertTrue(infoRun2.isMessageEdited());
	    infoRun2.cancel();
	    
	    //logout
		workflow1.logout();
		workflow2.logout();
	}
	
	/**
	 * Test the possibility to create, edit and delete a message with a standard student (cannot) 
	 * and the author (can).
	 */
	@Test(dependsOnMethods={"testConcurrentEditMessage"})
	public void testSecurityBasics() {
		//Get a workflow object for the default student. 
		OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardStudentOlatLoginInfos(1));
		CourseRun courseRun1 = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		InfoMessageRun infoRun1 = courseRun1.selectInfoMessage(INFO_MESSAGE_NAME);
		
		//it cannot edit or delete a message
		assertFalse(infoRun1.canCreateMessage());
		assertFalse(infoRun1.canEditMessage());
		assertFalse(infoRun1.canDeleteMessage());
		
		
		//Get a workflow object for the default author. 
		OLATWorkflowHelper workflow2 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CourseRun courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		InfoMessageRun infoRun2 = courseRun2.selectInfoMessage(INFO_MESSAGE_NAME);
		
		//it cannot edit or delete a message
		assertTrue(infoRun2.canCreateMessage());
		assertTrue(infoRun2.canEditMessage());
		assertTrue(infoRun2.canDeleteMessage());
	}
	
	private int countMessages(InfoMessageRun infoRun) {
		int found = 0;
	    for(int i=0; i<12; i++) {
	    	if(infoRun.hasMessage("Hello " + i)) {
	    		found++;
	    	}
	    }
	    return found;
	}
}
