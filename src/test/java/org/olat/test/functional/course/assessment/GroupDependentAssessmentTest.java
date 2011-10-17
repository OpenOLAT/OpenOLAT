/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.test.functional.course.assessment;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.AssessmentEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.home.EvidencesOfAchievement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * Assessment course element test.
 * <br/>
 * 
 * Test case:
 * The test proves that the tutor of group1 has access to grading the newly inserted element, which is only accessible for group2.
 * <br/>
 * Administrator adds new Assessment course element in course and configures it. (min, max score, passed cut value, etc.) 
 * and allows visibility/access only to the GROUP_NAME_2.
 * Course gets published.
 * <br/>
 * Tutor of group1 assesses student - participant of group 1 - for the latest introduced course element.
 * <br/>	
 * Student checks the score/passed info in "Evidence of achievement" even if he does't have access to that element
 * in course run. 
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class GroupDependentAssessmentTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
	private final String COURSE_NAME = "AssessmentTool_selenium" +System.currentTimeMillis();
	private final String GROUP_NAME_1 = "Gruppe 1";
	private final String GROUP_NAME_2 = "Gruppe 2";
	private final String ASSESSMENT_ELEMENT_TITLE = "Assessment_NEW";
	
	private String STUDENT1 = "test";
	
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	/*private final String STUDENT2 = "selenium_student2";
	private final String TUTOR = "selenium_tutor";*/
	private final String STUDENT2 = "seleniumstudent2";
	private final String TUTOR = "seleniumtutor";
	
	
	public void setUp() throws Exception { 
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
	  //author adds participants to group or student enrolls in group
		// STUDENT enrolls - login and enroll if not already member in GROUP_NAME
		OlatLoginInfos olatLoginInfos1 = context.getStandardStudentOlatLoginInfos(1);		
		STUDENT1 = olatLoginInfos1.getUsername();	
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos olatLoginInfos2 = context.createuserIfNotExists(1, STUDENT2, standardPassword, true, false, false, false, false);
		
	  //cleanup first - only if the course name is reused
		/*OlatServerSetupHelper.deleteEvidenceOfAchievement(COURSE_NAME, olatLoginInfos1);
		OlatServerSetupHelper.deleteEvidenceOfAchievement(COURSE_NAME, olatLoginInfos2);
		OlatServerSetupHelper.deleteAllCoursesNamed(COURSE_NAME);*/
		
		//import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
				
		//add tutor to the group: GROUP_NAME_1
		WorkflowHelper.addTutorToGroup(context.getStandardAuthorOlatLoginInfos(1).getUsername(), COURSE_NAME, GROUP_NAME_1);
		//add tutor to the group: GROUP_NAME_2		
		OlatLoginInfos olatLoginInfos0 = context.createuserIfNotExists(1, TUTOR, standardPassword, true, false, false, true, false);
		WorkflowHelper.addTutorToGroup(olatLoginInfos0.getUsername(), COURSE_NAME, GROUP_NAME_2);
				
		enrollInGroupAfterLogin(olatLoginInfos1, COURSE_NAME, GROUP_NAME_1);				
		
		enrollInGroupAfterLogin(olatLoginInfos2, COURSE_NAME, GROUP_NAME_2);
	}
	
	private void enrollInGroupAfterLogin(OlatLoginInfos olatLoginInfos, String courseName, String groupName) {
		Selenium selenium_ = Context.getContext().createSeleniumAndLogin(olatLoginInfos);
		WorkflowHelper.openCourseAfterLogin(selenium_, courseName);
		selenium_.click("ui=course::menu_courseNode(titleOfNode=Enrolment)");
		selenium_.waitForPageToLoad("30000");
		boolean alreadyEnrolled = seleniumEquals("enrolled", selenium_.getText("ui=course::content_enrollment_enrolledOrNot(nameOfGroup=" + groupName + ")"));		
		if(!alreadyEnrolled) {
			selenium_.click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=" + groupName + ")");
			selenium_.waitForPageToLoad("30000");
		  assertTrue(selenium_.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
		selenium_.click("ui=tabs::logOut()");
		selenium_.waitForPageToLoad("30000");
	}
	
	
	public void testGroupDependentAssessment() throws Exception {
		Context context = Context.getContext();
		
    //administrator - owner of the COURSE_NAME		
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		CourseEditor courseEditor = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
	  //administrator configures the assessment element
		AssessmentEditor assessmentEditor = (AssessmentEditor)courseEditor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, ASSESSMENT_ELEMENT_TITLE);
		assessmentEditor.configure(Boolean.TRUE, 1, 10, Boolean.TRUE, 5);
	  //change visibility - only visible for GROUP_NAME_2
		assessmentEditor.changeVisibilityDependingOnGroup(GROUP_NAME_2);
		assessmentEditor.changeAccessyDependingOnGroup(GROUP_NAME_2);
		courseEditor.publishCourse();
		courseEditor.closeToCourseRun();
		
		
	  //author - tutor of GROUP_NAME_1
		OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		AssessmentTool assessmentTool = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getAssessmentTool();
		AssessmentForm assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE);
	  //check values for min/max score, passed cut value
		assertEquals("1.0", assessmentForm.getMinScore());
		assertEquals("10.0", assessmentForm.getMaxScore());
		assertEquals("5.0", assessmentForm.getPassedCutScore());
		assessmentForm.setScore(7);
		assessmentTool = assessmentForm.save();
	  //check values in the assessment table (score, passed)		
		assertEquals("7.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));		
		assertEquals("Passed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));
	  //tutor changes score to 4
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE);
		assessmentForm.setScore(4);
		assessmentForm.save();
	   //check values in the assessment table (score, passed)
		assertEquals("4.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));	
		assertEquals("Failed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));
		
				
		//student check the "Evidence of achievement"
		OLATWorkflowHelper workflow3 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
		CourseRun courseRun = workflow3.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
	  //ASSESSMENT_ELEMENT_TITLE not visible/accessible
		assertFalse(courseRun.isTextPresent(ASSESSMENT_ELEMENT_TITLE));
		//but ASSESSMENT_ELEMENT_TITLE present in "Evidence of achievement"
		EvidencesOfAchievement evidencesOfAchievement = workflow3.getHome().getEvidencesOfAchievement();
		evidencesOfAchievement.selectDetails(COURSE_NAME);
	  //check score in table			
		assertEquals("4.000", evidencesOfAchievement.getCourseElementScore(ASSESSMENT_ELEMENT_TITLE));		
		assertEquals("Failed", evidencesOfAchievement.getCoursePassedStatus(ASSESSMENT_ELEMENT_TITLE));
				
	}
	
	
}
