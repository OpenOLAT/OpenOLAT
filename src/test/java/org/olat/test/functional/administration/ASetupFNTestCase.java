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
package org.olat.test.functional.administration;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * This is the very first test that should run in the selenium test suite.
 * It imports the necessary assets, and executes some cleanup tasks.
 * 
 * @author lavinia
 *
 */
public class ASetupFNTestCase extends BaseSeleneseTestCase {
  
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "DemoCourse.zip";
  
  
  
  @Override
  public void setUp() throws Exception {
    Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
  }

  /**
   * Import demo course.
   * @throws Exception
   */
  public void testImportDemoCourses() throws Exception {
            
    //check if "Demo Course" already available, if so return
    OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
    if(workflow.getLearningResources().searchResource(Context.DEMO_COURSE_NAME_1, "")!=null) {
      return;
    }
    
    //import test course 
    File file = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);
    assertNotNull("Could not locate the course zip!", file);
    assertTrue("file "+file.getAbsolutePath()+" not found!", file.exists());
    
    WorkflowHelper.importCourse(file, Context.DEMO_COURSE_NAME_1, "demo course description");
    WorkflowHelper.addOwnerToLearningResource("author", Context.DEMO_COURSE_NAME_1);
    
    workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
    LRDetailedView lRDetailedView = workflow.getLearningResources().searchMyResource(Context.DEMO_COURSE_NAME_1);
    lRDetailedView.removeOwner(Context.getContext().getStandardAdminOlatLoginInfos(1).getUsername());     
  }
  
  /**
   * Deletes archives from personal folder, if any.
   */
  public void testCleanupArchiveFolder() {
    testCleanupArchiveFolder(Context.getContext().getStandardAdminOlatLoginInfos(1));
    testCleanupArchiveFolder(Context.getContext().getStandardAuthorOlatLoginInfos(1));
  }
  
  /**
   * deletes Personal folder/private/archive.
   * @param olatLoginInfos
   */
  private void testCleanupArchiveFolder(OlatLoginInfos olatLoginInfos) {    
    OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(olatLoginInfos);
    Folder folder = workflow.getHome().getPersonalFolder();
    folder.selectFileOrFolder("private");
    folder.deleteItem("archive");
  }
}
