package org.olat.test.functional.group.management;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.group.LearningArea;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

/**
 * 
 * Checks if enrolment course element with learning groups and learning areas works, part of test suite GroupManagementCombiTest.java.
 * <br/>
 * Test setup: <br/>
 * 1. Delete all learning resources and groups from author. <br/>
 * 2. Create user "srlgauthor02". <br/>
 * <p>
 * Test case: <br/>
 * 1. Author creates course COURSE_NAME. <br/>
 * 2. Author switches to course run, opens group management and creates group GROUP_NAME_1. <br/>
 * 3. Author adds "srlgauthor02" to group. <br/>
 * 5. Author closes group management and opens course editor. <br/>
 * 6. Author adds enrolment course element and adds group GROUP_NAME_1. <br/>
 * 7. Author publishes course.  <br/>
 * 8. Author closes editor, opens group management.  <br/>
 * 9. Author creates learning area "learning area selenium 1" and two groups, adds these two groups to learning area.  <br/>
 * 10. Author closes group management and opens course editor. <br/>
 * 11. Author adds another course element enrolment "enrolment learning areas" and adds "learning area selenium 1".  <br/>
 * 12. Author publishes course and logs out.  <br/>
 * 13. Standard student opens course COURSE_NAME and navigates to "enrolment learning areas". <br/>
 * 14. Student checks if the two groups are available.   <br/>
 * 
 * @author sandra
 * 
 */

public class CreateLearningGroupLearningArea extends BaseSeleneseTestCase {
	
	@Test(groups = {GroupManagementCombiTest.FIRST})
	public void testCreateLearningGroupLearningAreaTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		// delete all author's groups first !!!
		WorkflowHelper.deleteAllGroupsFromAuthor(context.getStandardAuthorOlatLoginInfos(1));
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		// TODO this user is never used, line could be deleted. 
		OlatLoginInfos author02= context.createuserIfNotExists(1, "srlgauthor02", standardPassword, true, false, false, false, false);
		
		// refactored with abstraction layer
		OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		LearningResources learningResources1 = olatWorkflow1.getLearningResources();
		LRDetailedView lRDetailedView1 = learningResources1.createResource(GroupManagementCombiTest.COURSE_NAME, "GroupAdmin Management Test", LR_Types.COURSE);
		CourseRun courseRun1 = lRDetailedView1.showCourseContent();		
		GroupAdmin group1 = courseRun1.getGroupManagement().createLearningGroup(GroupManagementCombiTest.GROUP_NAME_1, "first lg", 2, true, true);
		assertFalse("Could not create group. It was already there: learning group selenium 1", group1.isTextPresent("This group name is already being used in this context, please select another one."));
		String[] userNames = {"srlgauthor02", context.getStandardStudentOlatLoginInfos(1).getUsername()};
		group1.addMembers(userNames, new String[0]);
		courseRun1 = courseRun1.getGroupManagement().close();		
		CourseEditor courseEditor1 = courseRun1.getCourseEditor();				
		EnrolmentEditor enrolmentElement1 = (EnrolmentEditor)courseEditor1.insertCourseElement(CourseElemTypes.ENROLMENT, true, null);				
		enrolmentElement1.selectLearningGroups(GroupManagementCombiTest.GROUP_NAME_1);
		courseEditor1.publishCourse();		
		courseRun1 = courseEditor1.closeToCourseRun();
		GroupManagement groupManagement1 = courseRun1.getGroupManagement();
		groupManagement1.createLearningArea("learning area selenium 1", "area description");
		groupManagement1.createLearningGroup("learning group selenium 2", "description 2", 0, false, false);
		groupManagement1.createLearningGroup("learning group selenium 3", "description 3", 0, false, false);
		LearningArea learningArea1 = groupManagement1.editLearningArea("learning area selenium 1");
		String[] groupNames = {"learning group selenium 2", "learning group selenium 3"};
		learningArea1.assignGroup(groupNames);
		courseRun1 = groupManagement1.close();
		courseEditor1 = courseRun1.getCourseEditor();		
		enrolmentElement1 = (EnrolmentEditor)courseEditor1.insertCourseElement(CourseElemTypes.ENROLMENT, true, "Enrolment learning areas");		
		enrolmentElement1.selectLearningAreas("learning area selenium 1");
		courseEditor1.publishCourse();
		courseEditor1.closeToCourseRun();
		olatWorkflow1.logout();
		
		
		// student logs in and checks if enrolment works as expected: should be fine like that
		OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		LearningResources learningResources2 = olatWorkflow2.getLearningResources();
		CourseRun courseRun2 = learningResources2.searchAndShowCourseContent(GroupManagementCombiTest.COURSE_NAME);
		courseRun2.selectCourseElement("Enrolment learning areas");
		assertTrue(courseRun2.isTextPresent("learning group selenium 2"));	
		assertTrue(courseRun2.isTextPresent("learning group selenium 3"));	
		olatWorkflow2.logout();				
	}
}
