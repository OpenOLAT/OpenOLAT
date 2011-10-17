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
import java.util.HashMap;
import java.util.Map;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentElement;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentTool;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.home.EvidencesOfAchievement;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests the bulk assessment in cluster mode.  <br/>
 * Test setup: <br/>
 * 1. creates students (STUDENT1, STUDENT2, STUDENT3) <br/>
 * 2. import course (AssessmentTool) and name it COURSE_NAME <br/>
 * 3. adds author as owner of the course <br/>
 * 4. adds author as tutor of the group GROUP_NAME <br/>
 * 5. students enroll in group <br/> 
 * <p>
 * Test case: <br/>
 * Tutor (node 1) opens the AssessmentTool of the COURSE_NAME, starts the bulk assessment wizard and 
 * adds score values for a ASSESSMENT_ELEM_NAME course element for 3 users enrolled into the group. 
 * The scores are: 2, 3, and 4 for STUDENT1, STUDENT2, STUDENT3.
 * <br/>
 * STUDENT2 asserts his score in course run (COURSE_NAME) for the ASSESSMENT_ELEM_NAME element,
 * and in the efficiency statement (Home/Evidences of achievement) after he logins on the cluster node 2
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class BulkAssessmentTest extends BaseSeleneseTestCase {
	
	private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "CourseImportCourse.zip";
	
	private final String COURSE_NAME_PREFIX = "AssessmentTool_selenium";
	private final String COURSE_NAME = COURSE_NAME_PREFIX + System.currentTimeMillis();
	private final String GROUP_NAME = "Gruppe 1";
	private final String ASSESSMENT_ELEM_NAME = "Bewertung 1";
	
	private String STUDENT1 = "test";	
	
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	//private final String STUDENT2 = "selenium_student2"; 
	//private final String STUDENT3 = "selenium_student3";
	private final String STUDENT2 = "seleniumstudent2"; 
	private final String STUDENT3 = "seleniumstudent3";

	
	/**
	 * Steps: <br/>
	 * 1. import course (AssessmentTool) and much more see <code>OlatServerSetupHelper.importCourse</code> <br/>
	 * 2. adds author as owner of the course <br/>
	 * 3. adds author as tutor of the group <br/>
	 * 4. creates students and let the enroll in group <br/>
	 * 
	 */
	public void setUp() throws Exception { 
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
	  //author adds participants to group or student enrolls in group
		// STUDENT enrolls - login and enroll if not already member in GROUP_NAME
		OlatLoginInfos olatLoginInfos1 = context.getStandardStudentOlatLoginInfos(1);
		STUDENT1 = olatLoginInfos1.getUsername();		
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos olatLoginInfos2 = context.createuserIfNotExists(1, STUDENT2, standardPassword, true, false, false, false, false);
		
		OlatLoginInfos olatLoginInfos3 = context.createuserIfNotExists(1, STUDENT3, standardPassword, true, false, false, false, false);
		
		 //cleanup first - for the selenium load
		/*OlatServerSetupHelper.deleteEvidencesOfAchievement(COURSE_NAME_PREFIX, olatLoginInfos1);
		OlatServerSetupHelper.deleteEvidencesOfAchievement(COURSE_NAME_PREFIX, olatLoginInfos2);
		OlatServerSetupHelper.deleteEvidencesOfAchievement(COURSE_NAME_PREFIX, olatLoginInfos3);
		OlatServerSetupHelper.deleteLearningResources(context.getStandardAdminOlatLoginInfos(1).getUsername(), COURSE_NAME_PREFIX);*/
				
		//import course - make sure that this is the course you need!
		File f = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());		
		WorkflowHelper.importCourse(f, COURSE_NAME, "assessment test course description");
					
		//"administrator" adds author as owner of the COURSE_NAME
		WorkflowHelper.addOwnerToLearningResource(context.getStandardAuthorOlatLoginInfos(1).getUsername(),COURSE_NAME);
		
		//add author as tutor of the group
		WorkflowHelper.addTutorToGroup(context.getStandardAuthorOlatLoginInfos(1).getUsername(), COURSE_NAME, GROUP_NAME);
				
		enrollInGroupAfterLogin(olatLoginInfos1, COURSE_NAME, GROUP_NAME);		
		
		enrollInGroupAfterLogin(olatLoginInfos2, COURSE_NAME, GROUP_NAME);
				
		enrollInGroupAfterLogin(olatLoginInfos3, COURSE_NAME, GROUP_NAME);
	}
		
	private void enrollInGroupAfterLogin(OlatLoginInfos olatLoginInfos, String courseName, String groupName) {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(olatLoginInfos);
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseName);
		EnrolmentRun enrolmentElement = courseRun.selectEnrolment(CourseEditor.ENROLMENT_TITLE);
		if(!enrolmentElement.alreadyEnrolled(groupName)) {
		  enrolmentElement.enrol(groupName);
		  assertTrue(enrolmentElement.isTextPresent("You have already enroled for the learning group mentioned below"));
		}
		workflow.logout();		
	}
	
	
	public void testScoreBulkAssessment() throws Exception {
		Context context = Context.getContext();
    //author - owner of the COURSE_NAME
		OLATWorkflowHelper workflow_A = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		CourseRun courseRun_A = workflow_A.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		AssessmentTool assessmentTool = courseRun_A.getAssessmentTool();
		Map<String,Integer> userScoreMap = new HashMap<String,Integer>();
		userScoreMap.put(STUDENT1, 2);
		userScoreMap.put(STUDENT2, 3);
		userScoreMap.put(STUDENT3, 4);
		assessmentTool.bulkAssessment(ASSESSMENT_ELEM_NAME, userScoreMap);
		workflow_A.logout();
				
		//STUDENT2 check his score and efficiency statement after he logins on the cluster node 2
		String standardPassword = context.getStandardStudentOlatLoginInfos(2).getPassword();
		OlatLoginInfos olatLoginInfos2 = context.createuserIfNotExists(2, STUDENT2, standardPassword, true, false, false, false, false);
		OLATWorkflowHelper workflow_2 = context.getOLATWorkflowHelper(olatLoginInfos2);
		CourseRun courseRun_2 = workflow_2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		courseRun_2.selectCourseElement("Struktur 1");
		AssessmentElement assessmentElement = courseRun_2.selectAssessmentElement(ASSESSMENT_ELEM_NAME);		;
    //	check your score - it should be 3.000 after select element in course
		assertEquals("3.000", assessmentElement.getScore());
		EvidencesOfAchievement evidencesOfAchievement = workflow_2.getHome().getEvidencesOfAchievement();
		evidencesOfAchievement.selectDetails(COURSE_NAME);
		String score = evidencesOfAchievement.getCourseElementScore(ASSESSMENT_ELEM_NAME);
    //	check score in table	
		assertEquals("3.000", score);
			
	}
	
	
	protected void cleanUpAfterRun() {
		System.out.println("***************** cleanUpAfterRun STARTED *********************");
		WorkflowHelper.deleteAllCoursesNamed(COURSE_NAME);
		System.out.println("***************** cleanUpAfterRun ENDED *********************");
  }
}
