package org.olat.test.functional.course;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.selenium.olatapi.group.RightsAdmin;
import org.olat.test.util.selenium.olatapi.group.RightsManagement;
import org.olat.test.util.selenium.olatapi.lr.Catalog;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


/**
 * Tests the close course functionality
 * <br />
 * Test setup:
 * <br />
 * 1. login with role "author"
 * 2. create an empty course
 * 3. publish empty course
 * 4. create learning group and rights group for that course
 * 5. add course to catalog
 * 6. close course
 * 7. check that course is no longer in catalog and groups are empty
 * 
 * 
 * @author Thomas Linowsky, BPS GmbH
 *
 */

public class CourseCloseTest extends BaseSeleneseTestCase{
	
	private static String author;
	
	private static final String COURSE_NAME_PREFIX = "CloseCourse";
	
	private final String COURSE_NAME = COURSE_NAME_PREFIX + System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription" + System.currentTimeMillis();
	
	private final String ASSIGNMENT_NAME= "Enrollment"+System.currentTimeMillis();
	
	private final String LEARNING_GROUP_NAME = COURSE_NAME_PREFIX+"LearningGroup"+System.currentTimeMillis();
	private final String LEARNING_GROUP_DESC = "LearningGroupDesc"+System.currentTimeMillis();
	
	private final String RIGHTS_GROUP_NAME= COURSE_NAME_PREFIX+"RightsGroupName"+System.currentTimeMillis();
	private final String RIGHTS_GROUP_DESC= "RightsGroupDesc"+System.currentTimeMillis();
	
	@Override
	public void setUp() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		author = context.getStandardAuthorOlatLoginInfos().getUsername();
		
		WorkflowHelper.deleteLearningResources(author, COURSE_NAME_PREFIX);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		CourseEditor editor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		// make sure course is not empty
		editor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, ASSIGNMENT_NAME);
		editor.publishCourse();
		
		CourseRun run = editor.closeToLRDetailedView().showCourseContent();

		// create a learning group and add author to it
		GroupManagement learn = run.getGroupManagement();
		GroupAdmin lg = learn.createLearningGroup(LEARNING_GROUP_NAME, LEARNING_GROUP_DESC, 25, true, false);
		String[] names = new String[]{author};
		lg.addMembers(names, new String[0]);
		run = learn.close();
		
		// create a rights group and add author to it
		RightsManagement rights = run.getRightsManagement();
		RightsAdmin rga = rights.createRightsGroup(RIGHTS_GROUP_NAME, RIGHTS_GROUP_DESC);
		rga.addMembers(names);
		LRDetailedView detail = rights.closeRightsManagement().getDetailedView();
		// add the course to the catalog
		detail.addToCatalog();
		olatWorkflow.logout();
	}
	
	public void testCloseCourse() throws Exception{
		
		Context context = Context.getContext();
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		LRDetailedView detail = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME);

		// make sure the groups are there
		Groups g = olatWorkflow.getGroups();
		assertTrue("Group "+LEARNING_GROUP_NAME+" is not present but should be", g.hasGroup(LEARNING_GROUP_NAME));
		assertTrue("Group "+RIGHTS_GROUP_NAME+" is not present but should be", g.hasGroup(RIGHTS_GROUP_NAME));
		
		// make sure the course is in the catalog
		Catalog catalog = olatWorkflow.getLearningResources().showCatalog();
		assertTrue(catalog.isEntryAvailable(COURSE_NAME));

		detail = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME);
		
		// close course
		detail.closeCourse(true, true);
		
		assertTrue("could not find \"[Closed]\" in Detail Page", detail.isTextPresent("[closed]"));
		
		CourseRun run = detail.showCourseContent();
		assertTrue("could not find \"is closed\"-message", run.isTextPresent("This course is closed and can therefore no longer be edited or updated"));
		
		g = olatWorkflow.getGroups();
		// refresh group view, otherwise groups will still be displayed although not available anymore
		g = olatWorkflow.getGroups();
		
		// check that the groups are empty now
		assertFalse("Group "+LEARNING_GROUP_NAME+" is still present but should not be", g.hasGroup(LEARNING_GROUP_NAME));
		assertFalse("Group "+RIGHTS_GROUP_NAME+" is still present but should not be", g.hasGroup(RIGHTS_GROUP_NAME));
		
		// make sure the course is no longer in the catalog
		catalog = olatWorkflow.getLearningResources().showCatalog();
		assertFalse("Course "+COURSE_NAME+" is still present in Catalog but should not be!", catalog.isEntryAvailable(COURSE_NAME));
		
		// delete the course in the end
		detail = olatWorkflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getDetailedView();
		detail.deleteLR();
	}
	
	@Override
	protected void cleanUpAfterRun() {
		super.cleanUpAfterRun();
		WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
	}
	
}
