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
package org.olat.test.functional.course.run;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.LTIPageEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.user.UserSettings;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the LTI BB: insert, configure, launch.
 * <br/>
 * <p>
 * Test setup:<br/>
 * import Course "Course_with_all_bb.zip"
 * enter title "LTIUseTestCourse"
 * enter description "Edit And Configure LTI Test Course" 
 * <p>
 * Test case: <br/>
 * 
 * ------------- as author ------------------- <br/>
 * login as author and go to tab learning resources <br/>
 * start course editor <br/>
 * go to BB "LTI Example" <br/>
 * click on tab "Page content" <br/>
 * enter "URL": http://www.imsglobal.org/developers/BLTI/tool.php <br/>
 * enter "Key": lmsng.school.edu <br/>
 * enter "Password": secret <br/>
 * uncheck "Send name to supplier" <br/>
 * uncheck "Send e-mail address to supplier" <br/>
 * check "Show information sent" <br/>
 * click "Save" <br/>
 * click "Show Preview" <br/>
 * assure authors name and lastname are NOT visible <br/>
 * assure authors email is NOT visible <br/>
 * click on "Close Preview" <br/>
 * check "Send name to supplier" <br/>
 * check "Send e-mail address to supplier" <br/>
 * click "Save" <br/>
 * click "Show Preview" <br/>
 * assure authors name and lastname are visible <br/>
 * assure authors email is visible <br/>
 * click on "Close Preview" <br/>
 * click on "LTI page" from "Insert course elements" navigation <br/>
 * insert the new LTI BB to the course tree structure <br/>
 * go to new BB "LTI Page" <br/>
 * click on tab "Page content" <br/>
 * enter "URL": URL": http://www.imsglobal.org/developers/BLTI/tool.php  <br/>
 * enter "Key": lmsng.school.edu <br/>
 * enter "Password": secret <br/>
 * check "Show information sent" <br/>
 * click "Save" <br/>
 * click "Publish" from "Editor tools" <br/>
 * select "LTI Example"  <br/>
 * select "LTI Page"  <br/>
 * click "Next"  <br/>
 * set permission "All registered OLAT users"  <br/>
 * click "Finish" <br/>
 * close course editor <br/>
 * 
 * --------- as student ------------- <br/>
 * login as student and go to tab learning resources <br/>
 * search for course with title "LTIUseTestCourse"  <br/>
 * run course  <br/>
 * click on "LTI Example" from the Course Navigation <br/>
 * click "Launch Endpoint with BasicLTI Data" <br/>
 * assure "Launch Endpoint with BasicLTI Data" is visible <br/>
 * click on "LTI Page" from the Course Navigation <br/>
 * assure "Launch Endpoint with BasicLTI Data" is visible <br/>
 * close course <br/>
 * </p>
 * 
 * @author Alberto Sanz
 *
 */

public class UseLTITest extends BaseSeleneseTestCase {
	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip"; 
  private final String COURSE_NAME_PREFIX = "LTIUseTestCourse";
  private final String COURSE_NAME = COURSE_NAME_PREFIX+System.currentTimeMillis(); 
  //private final static String COURSE_DESCRIPTION = "Edit And Configure LTI Test Course";
  
  private final static String LTI_ELEM_NAME = "LTI Example";
  private final static String URL = "http://www.imsglobal.org/developers/BLTI/tool.php";
  private final static String KEY = "lmsng.school.edu";
  private final static String PASSWORD = "secret";
	
  
  public void setUp() throws Exception {
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
    
    //cleanup first
    WorkflowHelper.deleteLearningResources(context.getStandardAdminOlatLoginInfos(1).getUsername(), COURSE_NAME_PREFIX);
    
    //import course
    File file = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);      
    WorkflowHelper.importCourse(file, COURSE_NAME, COURSE_NAME_PREFIX);
        
    //assign owner
    OLATWorkflowHelper workflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    workflowAdmin.getLearningResources().searchMyResource(COURSE_NAME).assignOwner(context.getStandardAuthorOlatLoginInfos(1).getUsername());
  }
  
  public void testEditAndConfigureLTI() throws Exception {
    Context context = Context.getContext();		

    OlatLoginInfos authorOlatLoginInfos = context.getStandardAuthorOlatLoginInfos(1);

    OLATWorkflowHelper workflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    UserSettings userSettings = workflowAdmin.getUserManagement().selectUser(authorOlatLoginInfos.getUsername());
    String authorEmail = userSettings.getEmail();
    //author		

    OLATWorkflowHelper workflowAuthor = context.getOLATWorkflowHelper(authorOlatLoginInfos);
    //CourseEditor courseEditor = workflowAuthor.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
    //LTIPageEditor lTIPageEditor = (LTIPageEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.LTI_PAGE, true, LTI_ELEM_NAME);
    CourseEditor courseEditor = workflowAuthor.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
    LTIPageEditor lTIPageEditor = (LTIPageEditor)courseEditor.selectCourseElement(LTI_ELEM_NAME);
    lTIPageEditor.configurePage(URL, KEY, PASSWORD, false, false, true);
    LTIPageEditor.LTIPreview preview = lTIPageEditor.showPreview();

    assertFalse(preview.hasInfo(authorEmail));
    lTIPageEditor = preview.closePreview();
    lTIPageEditor.configurePage(null, null, null, true, true, false);

    LTIPageEditor.LTIPreview preview2 = lTIPageEditor.showPreview();
    assertTrue(preview2.hasInfo(authorEmail));
    preview2.closePreview();

    LTIPageEditor lTIPageEditor2 = (LTIPageEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.LTI_PAGE, true, null);
    lTIPageEditor2.configurePage(URL, KEY, PASSWORD, false, false, true);

    courseEditor.publishCourse();    
    courseEditor.closeToCourseRun();

    //student
    OLATWorkflowHelper olatWorkflowStudent = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
    CourseRun courseRun = olatWorkflowStudent.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
    courseRun.selectLTI(LTI_ELEM_NAME).launch();

    courseRun.selectLTI(CourseEditor.LTI_TITLE).launch();
    olatWorkflowStudent.logout();

  }
}
