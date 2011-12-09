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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
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
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.home.EvidencesOfAchievement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the assessment course element, assessment tool and the evidence of achievement.
 * <br/>
 * Test setup:<br/>
 * 1. clean-up: deleteEvidencesOfAchievement for this student, starting with COURSE_NAME_PREFIX. Delete courses starting with COURSE_NAME_PREFIX.<br/>
 * 2. administrator imports test course <br/>
 * 3. adds author as owner of the COURSE_NAME <br/>
 * 4. adds author as tutor of the group GROUP_NAME <br/>
 * 5. student enrolls into group <br/>
 * <p>
 * Test case: <br/>
 * 1. author adds new Assessment course element in course and configures it. (min, max score, passed cut value, etc.) <br/>
 * 2. publish course <br/>
 * 3. author checks in the assessment tool the stored values for the assessment form: min/max score, passed cut value <br/>
 * 4. author grants score to student, and checks if the passed is correctly computed <br/>
 * 5. Student checks the score/passed info in the "Evidence of achievement".	<br/>
 * 
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentElementTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
	private final String COURSE_NAME_PREFIX = "AssessmentTool_selenium";
	private final String COURSE_NAME = COURSE_NAME_PREFIX + System.currentTimeMillis();
	private final String GROUP_NAME = "Gruppe 1";
	private final String ASSESSMENT_ELEMENT_TITLE = "Assessment_NEW";
	
	private String STUDENT1 = "test";	
	
	public void setUp() throws Exception { 
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
	  //author adds participants to group or student enrolls in group
		// STUDENT enrolls - login and enroll if not already member in GROUP_NAME
		OlatLoginInfos olatLoginInfos1 = context.getStandardStudentOlatLoginInfos(1);		
		STUDENT1 = olatLoginInfos1.getUsername();		
		
	  //cleanup first - for the selenium load
		WorkflowHelper.deleteEvidencesOfAchievement(COURSE_NAME_PREFIX, olatLoginInfos1);
		WorkflowHelper.deleteLearningResources(context.getStandardAdminOlatLoginInfos(1).getUsername(), COURSE_NAME_PREFIX);
				
		//import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
					
		//"administrator" adds author as owner of the COURSE_NAME
		WorkflowHelper.addOwnerToLearningResource(context.getStandardAuthorOlatLoginInfos(1).getUsername(),COURSE_NAME);
		
		//add author as tutor of the group
		WorkflowHelper.addTutorToGroup(context.getStandardAuthorOlatLoginInfos(1).getUsername(), COURSE_NAME, GROUP_NAME);
						
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(olatLoginInfos1);
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		EnrolmentRun enrolmentElement = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
		if(!enrolmentElement.alreadyEnrolled(GROUP_NAME)) {
		  enrolmentElement.enrol(GROUP_NAME);
		  assertTrue(enrolmentElement.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
	}
		
	
	
	public void testAssessmentElement() throws Exception {
		Context context = Context.getContext();
		
    //author - owner of the COURSE_NAME
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		CourseEditor courseEditor = workflow.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
		AssessmentEditor assessmentEditor = (AssessmentEditor)courseEditor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, ASSESSMENT_ELEMENT_TITLE);
    //	author fills in the assessment configuration form
		assessmentEditor.configure(true, 1, 10, true, 5);
		Thread.sleep(1000);
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToCourseRun();
    //	author opens the AssessmentTool and checks the configured values for the newly introduced Assessment course element
		AssessmentTool assessmentTool = courseRun.getAssessmentTool();
		AssessmentForm assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE);
    //	check values for min/max score, passed cut value
		assertEquals("1.0", assessmentForm.getMinScore());
		assertEquals("10.0", assessmentForm.getMaxScore());
		assertEquals("5.0", assessmentForm.getPassedCutScore());
    //	fills the form
		assessmentForm.setScore(7);
		assessmentTool = assessmentForm.save();		
     //	check values in the assessment table (score, passed)
		assertEquals("7.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));	
		assertEquals("Passed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));
				
		//tutor changes score to 4
		assessmentForm = assessmentTool.getAssessmentFormAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE);
		assessmentForm.setScore(4);
		assessmentForm.save();
		 //	check values in the assessment table (score, passed)
		assertEquals("4.000", assessmentTool.getScoreInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));	
		assertEquals("Failed", assessmentTool.getPassedStatusInTableAsPerUser(STUDENT1, ASSESSMENT_ELEMENT_TITLE, false));
					
		//student check the "Evidence of achievement"
		OLATWorkflowHelper workflow_S = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
		CourseRun courseRun_S = workflow_S.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		EvidencesOfAchievement evidences = workflow_S.getHome().getEvidencesOfAchievement();
		evidences.selectDetails(COURSE_NAME);
		 //check score in table	
		assertEquals("4.000", evidences.getCourseElementScore(ASSESSMENT_ELEMENT_TITLE));		
		assertEquals("Failed", evidences.getCourseElementPassedStatus(ASSESSMENT_ELEMENT_TITLE));		
	}
	

}
