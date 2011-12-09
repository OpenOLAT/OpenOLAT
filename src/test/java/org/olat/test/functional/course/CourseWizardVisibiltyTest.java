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
*/
package org.olat.test.functional.course;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.EnrolmentRun;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.lr.Catalog;
import org.olat.test.util.selenium.olatapi.lr.CourseWizard;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


/**
 * Tests the course wizard
 * 1. author creates course using the course wizard
 * 2. select all items (singlepage, enrollment, downloadfolder, forum and contact form) to create
 * 3. edit the enrollment configuration
 * 4. select all available CBBs (singlepage, downloadfolder, forum and contact form) for access restriction
 * 5. publish course and run it
 * 6. enrol in created group and show forum
 * 7. log out
 * @author Thomas Linowsky
 * 
 */

public class CourseWizardVisibiltyTest extends BaseSeleneseTestCase {

	private final String COURSE_NAME = "CourseWizard"	+ System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription" + System.currentTimeMillis();
	private final String ASSESSMENT_NAME = "Assessment"+System.currentTimeMillis();
	private final String ENROLMENT_NAME = "Enrollment";
	private final String GROUP_NAME = COURSE_NAME + " Group 1";
	private final String SINGLE_PAGE_NAME ="Information page"; 

	public void testVisibility() {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);

		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());

		// get the wizard object
		CourseWizard wizard = olatWorkflow.getLearningResources().createCourseWizard(COURSE_NAME, COURSE_DESCRIPTION);

		// select all elements that are possible
		wizard.selectCourseElements(true, true, true, true, true);
		// define that all possible CBBs are only visible when enrolled
		wizard.editEnrollment(true, true, true, true, true);

		// finish course wizard and define visibility status
		CourseRun run = wizard.createCourseAndRun(LRDetailedView.ACCESS_ALL_REGISTERED);
		
		// enrol author
		EnrolmentRun enrol = run.selectEnrolment(ENROLMENT_NAME);
		enrol.enrol(GROUP_NAME);
		
		// check the access rule
		LRDetailedView detail = run.getDetailedView();
		assertTrue("Acess rule does not match "+LRDetailedView.ACCESS_ALL_REGISTERED+", it is "+detail.getAccessString(), detail.getAccessString().equals(LRDetailedView.ACCESS_ALL_REGISTERED));

		Catalog catalog = olatWorkflow.getLearningResources().showCatalog();
		assertTrue("could not find "+COURSE_NAME+" in the catalog", catalog.isEntryAvailable(COURSE_NAME));
		
		// the the editor again
		run = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME).showCourseContent();
		CourseEditor editor = run.getCourseEditor();
		
		// make sure the storage folder is available
		Folder storage = editor.storageFolder();
		editor = storage.closeStorageFolder();
		
		// delete the information page and add an assessment to make sure the course is correct
		editor.selectCourseElement(SINGLE_PAGE_NAME);
		editor.deleteCourseElement();
		editor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, ASSESSMENT_NAME);
		editor.publishCourse();
		run = editor.closeToCourseRun();
		
		
		
		// make sure the information page is deleted and the assessment is available
		assertFalse("Information page is still present but should not be!", run.isElementPresent("ui=course::menu_courseNode(titleOfNode=Information page)"));
		assertTrue("assessment "+ASSESSMENT_NAME+" is not present but should be!", run.isElementPresent("ui=course::menu_courseNode(titleOfNode="+ASSESSMENT_NAME+")"));
		
		// delete the course
		detail = run.getDetailedView();
		detail.deleteLR();
		olatWorkflow.logout();
	}
	
}
