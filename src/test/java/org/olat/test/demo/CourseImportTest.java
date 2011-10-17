package org.olat.test.demo;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


/**
 * 
 * @author eglis
 *
 */
public class CourseImportTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "/CourseImportCourse.zip";
	
	public void testCourseImport() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);

		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());
		String courseTitle = "CourseImportTestCourse-"+System.currentTimeMillis();
		WorkflowHelper.importCourse(f, courseTitle, "Whatever right?");
		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseTitle);
		courseRun.close(courseTitle);
				
	}

}
