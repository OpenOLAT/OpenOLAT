package org.olat.test.demo;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

public class CourseOpenCloseTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = Context.DEMO_COURSE_NAME_1;

	public void testCourseOpenClose() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
		//context.checkSeleniumServerStarted();
		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().showCourseContent(COURSE_NAME);
		courseRun.close(COURSE_NAME);		
	}
}
