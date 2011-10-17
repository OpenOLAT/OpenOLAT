package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests Jira Issue OLAT-4515
 *  <br/>
 *  <p>
 * Test setup: <br/>
 * create course and open course editor  <br/>
 * insert enrolment element <br/>
 * select configuration tab for creating new groups <br/>
 * create group gr1 and group gr2 with CSV input <br/>
 * assure that group gets added to the group enumeration in the learning group text element <br/>
 * Delete groups  <br/>
 * create groups again, but one more. example: 
  gr1comma, gr2comma, gr3comma <br/>
 * close course  <br/>
 * </p>
 *
 * @author alberto
 */
public class EnrolmentCreateGroupsWithCSVTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "EnrollWithCSV"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	private final String GR1 = "gr1comma";
	private final String GR2 = "gr2comma";
	private final String GR3 = "gr3comma";
	
	public void testEnrolmentCreateGroupsWithCSV() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		//create course and open course editor
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
	
		//insert enrolment element
		EnrolmentEditor enrolmentEditor = (EnrolmentEditor)courseEditor.insertCourseElement(CourseElemTypes.ENROLMENT, true, null);
		
		//select configuration tab for creating new groups
		//create group gr1 and group gr2 with CSV input		
		String groupEnumerationString = GR1+","+GR2;
		enrolmentEditor.createAndSelectGroups(groupEnumerationString);
		assertTrue(enrolmentEditor.isGroupSelected(GR1));
		assertTrue(enrolmentEditor.isGroupSelected(GR2));
		
		//delete groups
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		GroupManagement groupManagement = courseRun.getGroupManagement();
		groupManagement.deleteGroup(GR1);
		groupManagement.deleteGroup(GR2);
		courseRun = groupManagement.close();
		
		//create groups again, but one more. in example: gr1comma, gr2comma, gr3comma
		courseEditor = courseRun.getCourseEditor();
		enrolmentEditor = (EnrolmentEditor)courseEditor.selectCourseElement(CourseEditor.ENROLMENT_TITLE);
		groupEnumerationString += ","+GR3;
		enrolmentEditor.createAndSelectGroups(groupEnumerationString);
		assertTrue(enrolmentEditor.isGroupSelected(GR1));
		assertTrue(enrolmentEditor.isGroupSelected(GR2));
		assertTrue(enrolmentEditor.isGroupSelected(GR3));
		
		//close course
		courseEditor.closeToCourseRun().closeAny();
	}
}
