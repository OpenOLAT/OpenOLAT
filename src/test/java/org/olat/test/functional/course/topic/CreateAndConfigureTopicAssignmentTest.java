package org.olat.test.functional.course.topic;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.course.editor.TopicAssignmentEditor;
import org.testng.annotations.Test;

/**
 * 
 * Tests the new bb topic assignment. Step 1: Configuration. 
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. import demo course or any other course. Copy course and rename to TOPIC_ASSIGNMENT_COURSE. <br/>
 * 2. prepare following test users: 1 author (AUTHOR), 3 users without authoring rights TUTOR1, TUTOR2, TUTOR3
 * 
 * Testcase:<br/>
 * 1. log in as AUTHOR
 * 2. go to learning resources
 * 3. search for TOPIC_ASSIGNMENT_COURSE from test setup
 * 4. edit content of TOPIC_ASSIGNMENT_COURSE  
 * 5. add bb topic assignment, name it TOPIC_ASSIGNMENT_1
 * 6. go to tab configuration
 * 7. limit number or project per student to 2
 * 8. check the two other dependent checkboxes, save
 * 9. go to tab persons in charge
 * 10. add TUTOR1, TUTOR2, TUTOR3 as topic authors
 *  (leave all other config options as default)
 * 11. publish TOPIC_ASSIGNMENT_COURSE, set access to all registered users
 * 12. log out
 * </p>
 * 
 * @author sandra
 *
 */
public class CreateAndConfigureTopicAssignmentTest extends BaseSeleneseTestCase {
	
	private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
	
		
	
	@Override
	public void setUp() throws Exception {
		System.out.println("********* CreateAndConfigureTopicAssignmentTest **************");
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		//clone DEMO_COURSE_NAME and work with the clone
		//assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, TopicAssignmentSuite.COURSE_NAME));
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		
		//workaround: could not copy course at the moment
		workflow.getLearningResources().createCourseAndStartEditing(TopicAssignmentSuite.COURSE_NAME, TopicAssignmentSuite.COURSE_NAME).publishCourse();
		workflow.logout();
		
		workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		workflow.getLearningResources().searchMyResource(TopicAssignmentSuite.COURSE_NAME).assignOwner(context.getStandardAuthorOlatLoginInfos(1).getUsername());
		
		//create test users		
		OlatLoginInfos tutor1= context.createuserIfNotExists(2, TopicAssignmentSuite.TUTOR1, true, false, false, false, false);
		OlatLoginInfos tutor2= context.createuserIfNotExists(1, TopicAssignmentSuite.TUTOR2, true, false, false, false, false);
		OlatLoginInfos tutor3= context.createuserIfNotExists(2, TopicAssignmentSuite.TUTOR3, true, false, false, false, false);
		
	}

	@Test(groups={TopicAssignmentSuite.FIRST})
	public void testCreateAndConfigureTopicAssignment() throws Exception {		
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
		CourseEditor courseEditor = workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME).getCourseEditor();
		TopicAssignmentEditor topicAssignmentEditor = (TopicAssignmentEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TOPIC_ASSIGNMENT, true, TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		topicAssignmentEditor.configure(true, 2, true, true);
		topicAssignmentEditor.addUser(TopicAssignmentSuite.TUTOR1);
		topicAssignmentEditor.addUser(TopicAssignmentSuite.TUTOR2);
		topicAssignmentEditor.addUser(TopicAssignmentSuite.TUTOR3);
		
		//TODO: LD: workaround for selectall problem in publishCourse, doesn't always work with only one course element, so we insert a second one.
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		courseEditor.publishCourse();
		workflow.logout();
		
	}

	@Override
	protected void cleanUpAfterRun() {
		//nothing to cleanup - there is a follow-up test using the created resource
		
	}
	
	
}
