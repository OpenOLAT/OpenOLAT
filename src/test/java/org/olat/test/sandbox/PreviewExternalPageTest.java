package org.olat.test.sandbox;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Test issue OLAT-4597.
 * Tries to reproduce a RS.
 * <br/>
 * <p>
 * Test case: <br/>
 * open course editor <br/>
 * add course building block 'external page' <br/>
 * Add an URL <br/>
 * press preview button <br/>
 * assert: there is NO RedScreen <br/>
 * </p>
 * 
 * @author alberto
 *
 */
public class PreviewExternalPageTest extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	public void testPreviewExternalPageTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		//open course editor
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
	
		//add course building block 'external page'
		
		
		//Add an URL
		
		//press preview button
		
		//=> assert: there is NO RedScreen NO RedScreen
		
	}
}
