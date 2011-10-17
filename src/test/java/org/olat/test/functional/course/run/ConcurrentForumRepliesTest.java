package org.olat.test.functional.course.run;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Five students post replies and new topics in a forum shortly one after the other.
 * <br/>
 * Test setup: <br/>
 * 1. Create five student users STUDENT_USER_NAME_1 - STUDENT_USER_NAME_5. 
 * <p>
 * Test case: <br/>
 * 1. Author creates course COURSE_NAME and adds building block forum. <br/>
 * 2. Authors opens first forum topic. <br/>
 * 3. Students log in and navigate to forum. <br/>
 * 4. All students reply to the same forum message.  <br/>
 * 5. Author checks if all replies are displayed. <br/>
 * 6. All students write new topic.<br/>
 * 7. Author checks if all topics are displayed. <br/>
 * 8. Author deletes course. <br/>
 * 
 * @author sandra
 * 
 */

public class ConcurrentForumRepliesTest extends BaseSeleneseTestCase {
	
	 private final String COURSE_NAME = "Concurrent_Forum_Replies_" + System.currentTimeMillis();
	 
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	 /*private final String STUDENT_USER_NAME_1 = "forumstudi_01";
	 private final String STUDENT_USER_NAME_2 = "forumstudi_02";
	 private final String STUDENT_USER_NAME_3 = "forumstudi_03";
	 private final String STUDENT_USER_NAME_4 = "forumstudi_04";
	 private final String STUDENT_USER_NAME_5 = "forumstudi_05";*/
	 private final String STUDENT_USER_NAME_1 = "forumstudi01";
	 private final String STUDENT_USER_NAME_2 = "forumstudi02";
	 private final String STUDENT_USER_NAME_3 = "forumstudi03";
	 private final String STUDENT_USER_NAME_4 = "forumstudi04";
	 private final String STUDENT_USER_NAME_5 = "forumstudi05";
	 
	
	public void testConcurrentForumReplies() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student01= context.createuserIfNotExists(1, STUDENT_USER_NAME_1, standardPassword, true, false, false, false, false);
		OlatLoginInfos student02= context.createuserIfNotExists(1, STUDENT_USER_NAME_2, standardPassword, true, false, false, false, false);
		OlatLoginInfos student03= context.createuserIfNotExists(1, STUDENT_USER_NAME_3, standardPassword, true, false, false, false, false);
		OlatLoginInfos student04= context.createuserIfNotExists(1, STUDENT_USER_NAME_4, standardPassword, true, false, false, false, false);
		OlatLoginInfos student05= context.createuserIfNotExists(1, STUDENT_USER_NAME_5, standardPassword, true, false, false, false, false);
		
		
			// author creates course with forum, opens welcome message topic			
			OLATWorkflowHelper olatWorkflow_1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			CourseEditor courseEditor1 = olatWorkflow_1.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "selenium");
			courseEditor1.insertCourseElement(CourseElemTypes.FORUM, true, null);
			courseEditor1.publishCourse();
			LRDetailedView lRDetailedView1 = courseEditor1.closeToLRDetailedView();
			CourseRun courseRun1 = lRDetailedView1.showCourseContent();
			Forum forum1 = courseRun1.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum1.openNewTopic("welcome", "werdet euren senf los");
					
		
			// students log in and navigate to forum			
			CourseRun courseRun2 = openCourse(student01);
			Forum forum2 = courseRun2.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum2.viewTopic("welcome");
			 
			CourseRun courseRun3 = openCourse(student02);
			Forum forum3 = courseRun3.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum3.viewTopic("welcome");
			
			CourseRun courseRun4 = openCourse(student03);
			Forum forum4 = courseRun4.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum4.viewTopic("welcome");
			
			CourseRun courseRun5 = openCourse(student04);
			Forum forum5 = courseRun5.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum5.viewTopic("welcome");
			
			CourseRun courseRun6 = openCourse(student05);
			Forum forum6 = courseRun6.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum6.viewTopic("welcome");
			
					
			// students write forum message simultaneously
			forum2.replyToCurrentMessage("moutarde\n> test concurrent message reply", true);
			forum3.replyToCurrentMessage("mustard\n> test concurrent message reply", true);
			forum4.replyToCurrentMessage("senape\n> test concurrent message reply", true);
			forum5.replyToCurrentMessage("mostaza\n> test concurrent message reply", true);
			forum6.replyToCurrentMessage("sinappi\n> test concurrent message reply", true);
					
		
			// author checks if messages are present
			forum1 = courseRun1.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum1.viewTopic("welcome");
			assertTrue(forum1.isTextPresent(STUDENT_USER_NAME_1));
			assertTrue(forum1.isTextPresent("moutarde"));
			assertTrue(forum1.isTextPresent(STUDENT_USER_NAME_2));
			assertTrue(forum1.isTextPresent("mustard"));
			assertTrue(forum1.isTextPresent(STUDENT_USER_NAME_3));
			assertTrue(forum1.isTextPresent("senape"));
			assertTrue(forum1.isTextPresent(STUDENT_USER_NAME_4));
			assertTrue(forum1.isTextPresent("mostaza"));
			assertTrue(forum1.isTextPresent(STUDENT_USER_NAME_5));
			assertTrue(forum1.isTextPresent("sinappi"));
					
				
		// all students write new forum message
			forum2 = courseRun2.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum3 = courseRun3.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum4 = courseRun4.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum5 = courseRun5.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum6 = courseRun6.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
			forum2.clickNewTopic();
			forum3.clickNewTopic();
			forum4.clickNewTopic();
			forum5.clickNewTopic();
			forum6.clickNewTopic();			
			forum2.typeInNewMessage("moutarde topic", "some more senf");
			forum3.typeInNewMessage("mustard topic", "some more senf");
			forum4.typeInNewMessage("senape topic", "some more senf");
			forum5.typeInNewMessage("mostaza topic", "some more senf");
			forum6.typeInNewMessage("sinappi topic", "some more senf");
					
		
			//author checks if all messages are there
			forum1 = courseRun1.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);			
			assertTrue(forum1.isTextPresent("moutarde"));
			assertTrue(forum1.isTextPresent("mustard"));
			assertTrue(forum1.isTextPresent("senape"));
			assertTrue(forum1.isTextPresent("mostaza"));
			assertTrue(forum1.isTextPresent("sinappi"));
		
		
		  //author deletes course
			olatWorkflow_1.getLearningResources().searchMyResource(COURSE_NAME).deleteLR();
							
	}
  
    /**
     * Login, search course and open, select forum.
     * @param student
     * @return the selected forum.s
     */
    private CourseRun openCourse(OlatLoginInfos student) {
    	OLATWorkflowHelper olatWorkflow = Context.getContext().getOLATWorkflowHelper(student);
			CourseRun courseRun = olatWorkflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);			
			return courseRun;
    }
    
}



