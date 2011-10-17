package org.olat.test.demo;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

public class OpenForumThreadTest extends BaseSeleneseTestCase {

	public void setUp() throws Exception {		
		Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);		
	}
	
	public void testShowCourseContent() throws Exception {		
		selenium = Context.getContext().createSeleniumAndLogin(); //login as the default admin user
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_1);
		Forum forum = courseRun.selectForum("Forum");
		forum.openNewTopic("abc", "MESSAGE BODY");
		courseRun.selectForum("Forum").viewTopic("abcd");
		
		workflow.logout();
	}
}
