package org.olat.test.functional.course.topic;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.TopicAssignmentRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Tests the new bb topic assignment. Step 2: Create topics
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. test created in CreateAndConfigureTopicAssignmentTest (TOPIC_ASSIGNMENT_COURSE) is available. <br/>
 * 2. TUTOR1, TUTOR2, TUTOR3 from CreateAndConfigureTopicAssignmentTest are available
 * 
 * Testcase:<br/>
 * 1. log in as TUTOR1
 * 2. go to learning resources, search form
 * 3. search for TOPIC_ASSIGNMENT_COURSE and open course run
 * 4. go to bb TOPIC_ASSIGNMENT_1
 * 5. check if the button "create topic" is available
 * 6. check if text "No Topics for this Topic assignment available." is present
 * 7. click "create topic"
 * 8. title: "Topic_Tutor1", description "Description Topic of Tutor 1"
 * 9. check "do you want to limit the vacancies" and edit 1. 
 * 10. Save
 * 11. Log out TUTOR1
 * 12. log in as TUTOR2
 * 13. go to learning resources, search form
 * 14. search for TOPIC_ASSIGNMENT_COURSE and open course run
 * 15. go to bb TOPIC_ASSIGNMENT_1
 * 16. check if the button "create topic" is available
 * 17. check if "Topic_Tutor1" is available
 * 18. click "create topic"
 * 19. title: "Topic_Tutor2", description "Description Topic of Tutor 2"
 * 20. check "do you want to limit the vacancies" and edit 2. 
 * 21. Save
 * 22. Log out TUTOR2
 * 23. log in as TUTOR3
 * 24. go to learning resources, search form
 * 25. search for TOPIC_ASSIGNMENT_COURSE and open course run
 * 26. go to bb TOPIC_ASSIGNMENT_1
 * 27. check if the button "create topic" is available
 * 28. check if "Topic_Tutor1" and "Topic_Tutor2" are available
 * 29. click "create topic"
 * 30. title: "Topic_Tutor3", description "Description Topic of Tutor 3"
 * 31. check "do you want to limit the vacancies" and edit 3. 
 * 32. Save
 * 33. go again to TOPIC_ASSIGNMENT_1
 * 34. check if all topics are available "Topic_Tutor1", "Topic_Tutor2", "Topic_Tutor3"
 * 35. check if in column "in charge" TUTOR1, 2 and 3, respectively, are listed
 * 36. check if in the colum "topic status" the first two topics have value "vacancies", the 
 * third "no participants to check"
 * 37. check if in column "number of filled vacancies" is 0 out of 1, 2 and 3 respectively
 * 38. check if "select" link is available for all 3 topics 
 * 39. Log out 
 * 
 * </p>
 * 
 * @author sandra
 *
 */
public class CreateTopicInTopicAssignmentTest extends BaseSeleneseTestCase {
	
	private final String NO_TOPIC_AVAILABLE = "No Topics for this Topic assignment available";	
	private final String TOPIC_DESCRIPTION_1 = "Description Topic of Tutor 1";	
	private final String TOPIC_DESCRIPTION_2 = "Description Topic of Tutor 2";	
	private final String TOPIC_DESCRIPTION_3 = "Description Topic of Tutor 3";
	private final String FILLED_VACANCIES_1 = "0 of 1";
	private final String FILLED_VACANCIES_2 = "0 of 2";
	private final String FILLED_VACANCIES_3 = "0 of 3";
	
	@Test(dependsOnGroups={TopicAssignmentSuite.FIRST}, groups={TopicAssignmentSuite.SECOND})
	public void testCreateTopicInTopicAssignment() throws Exception {
		System.out.println("********* CreateTopicInTopicAssignmentTest **************");
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		//TUTOR1
		OLATWorkflowHelper tutor1Workflow = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, TopicAssignmentSuite.TUTOR1));
		CourseRun courseRun1 = tutor1Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun projectBrokerRun = courseRun1.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(projectBrokerRun.isTextPresent(NO_TOPIC_AVAILABLE));
		projectBrokerRun.createTopic(TopicAssignmentSuite.TOPIC_TITLE_1, TOPIC_DESCRIPTION_1, true, 1);
		tutor1Workflow.logout();
		
		//TUTOR2
		OLATWorkflowHelper tutor2Workflow = context.getOLATWorkflowHelper(context.getOlatLoginInfo(2, TopicAssignmentSuite.TUTOR2));
		CourseRun courseRun2 = tutor2Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRun = courseRun2.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRun.isTextPresent(TopicAssignmentSuite.TOPIC_TITLE_1));		
		topicAssignmentRun.createTopic(TopicAssignmentSuite.TOPIC_TITLE_2, TOPIC_DESCRIPTION_2, true, 2);
		tutor2Workflow.logout();
		
		//TUTOR3
		OLATWorkflowHelper tutor3Workflow = context.getOLATWorkflowHelper(context.getOlatLoginInfo(2, TopicAssignmentSuite.TUTOR3));
		CourseRun courseRun3 = tutor3Workflow.getLearningResources().searchAndShowCourseContent(TopicAssignmentSuite.COURSE_NAME);
		TopicAssignmentRun topicAssignmentRun3 = courseRun3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRun3.isTextPresent(TopicAssignmentSuite.TOPIC_TITLE_1));	
		assertTrue(topicAssignmentRun3.isTextPresent(TopicAssignmentSuite.TOPIC_TITLE_2));	
		topicAssignmentRun3.createTopic(TopicAssignmentSuite.TOPIC_TITLE_3, TOPIC_DESCRIPTION_3, true, 3);
		
		topicAssignmentRun3 = courseRun3.selectTopicAssignment(TopicAssignmentSuite.TOPIC_ASSIGNMENT_1);
		assertTrue(topicAssignmentRun3.hasTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun3.hasTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun3.hasTopic(TopicAssignmentSuite.TOPIC_TITLE_3));	
		assertTrue(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_1));
		assertTrue(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_2));
		assertTrue(topicAssignmentRun3.canSelectTopic(TopicAssignmentSuite.TOPIC_TITLE_3));
		
		assertTrue(topicAssignmentRun3.isTextPresent(TopicAssignmentSuite.TUTOR1));	
		assertTrue(topicAssignmentRun3.isTextPresent(TopicAssignmentSuite.TUTOR2));	
		assertTrue(topicAssignmentRun3.isTextPresent(TopicAssignmentSuite.TUTOR3));	
		assertTrue(topicAssignmentRun3.isTextPresent(FILLED_VACANCIES_1));
		assertTrue(topicAssignmentRun3.isTextPresent(FILLED_VACANCIES_2));	
		assertTrue(topicAssignmentRun3.isTextPresent(FILLED_VACANCIES_3));	
		
	}
}
