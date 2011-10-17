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
* <p>
*/ 
package org.olat.test.functional.codepoints.cluster;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

import com.thoughtworks.selenium.Selenium;

	/**
	 * Tests course editing in clustered mode, and that the GUI locking works, 
	 * that is a course could be edited by only a single user at a time.
	 * <br/>
	 * Test setup: <br/>
	 * Needs a clean "Demo Course".</br>
	 * 
	 * Test case: <br/>
	 * The cluster_B node should be able the see the latest inserted node by cluster_A node after the course lock release. </br>
	 * - cluster_A: inserts a Structure course element, assertBreakpointReached </br>
	 * - cluster_B: try to open the course editor of the same course but gets a "course locked" warning </br>
	 * - cluster_A: continues the thread </br>
	 * - cluster_A: closes course editor </br>
	 * - cluster_B: opens editor, selects the first Structure node, changes the title, closes the editor </br>
	 * 
	 * @author Lavinia Dumitrescu
	 *
	 */
public class CourseEditingTest extends BaseSeleneseTestCase {
	
  protected Selenium selenium_A;
  protected Selenium selenium_B;
  private CodepointClient codepointClient_A;
  
  private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
  private final String COURSE_NAME = "CourseEditing" + System.currentTimeMillis();
 
	
  public void setUp() throws Exception {
  	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);    
  	
    //clone DEMO_COURSE_NAME and work with the clone
  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
  	  	
  	//author
  	OlatLoginInfos authorOlatLoginInfos = context.getStandardAuthorOlatLoginInfos(2);
    //"administrator" adds author as owner of the "Demo Course"
  	WorkflowHelper.addOwnerToLearningResource(authorOlatLoginInfos.getUsername(), COURSE_NAME);   
		selenium_B = context.createSeleniumAndLogin(context.getStandardAuthorOlatLoginInfos(2));
  	 
    //administrator
  	selenium_A = context.createSeleniumAndLogin(context.getStandardAdminOlatLoginInfos(1));
  }
  
	/**
	 * Needs a clean "Demo Course".</br>
	 * Tests course editing in clustered mode. 
	 * The cluster_B node should be able the see the latest inserted node by cluster_A node after the course lock release. </br>
	 * - cluster_A: inserts a Structure course element, assertBreakpointReached </br>
	 * - cluster_B: try to open the course editor of the same course but gets a "course locked" warning </br>
	 * - cluster_A: continues the thread </br>
	 * - cluster_A: closes course editor </br>
	 * - cluster_B: opens editor, selects the first Structure node, changes the title, closes the editor </br>
	 * @throws Exception
	 */
	public void testCourseEditing() throws Exception {
					
		  WorkflowHelper.openCourseAfterLogin(selenium_A, COURSE_NAME); 
		  WorkflowHelper.openCourseAfterLogin(selenium_B, COURSE_NAME);
									
			Context context = Context.getContext();
	    //codepoints node A     
	  	codepointClient_A = context.createCodepointClient(1);
			CodepointRef startInsertCp_A = codepointClient_A.getCodepoint("org.olat.course.editor.EditorMainController.startInsertNode");
			startInsertCp_A.setHitCount(0);
			startInsertCp_A.enableBreakpoint();
			
			selenium_A.click("ui=course::toolbox_courseTools_courseEditor()");
			selenium_A.waitForPageToLoad("30000");
			
      //A: inserts Structure course node
			selenium_A.click("ui=courseEditor::toolbox_insertCourseElements_insertStructure()");			
			
			//A: check if codepoint reached: startInsertCp_A
			startInsertCp_A.assertBreakpointReached(1, 20000);
			System.out.println("startInsertCp_A.assertBreakpointReached");
			
			
			//B: start course editor in cluster_B - this should lead to a courseLocked warning 
			selenium_B.click("ui=course::toolbox_courseTools_courseEditor()");
			selenium_B.waitForPageToLoad("30000");
						
			//B: check if "course locked" warning shows up - 
			//selenium_B.click("ui=dialog::OK()"); //we don't really have the chance to click OK on the warning dialog
			//selenium_B.waitForPageToLoad("30000");
			/*for (int second = 0;; second++) { //TODO: ld: for some reason this check doesn't work
				if (second >= 120) fail("timeout");
				try { 
					if (selenium_B.isTextPresent("This course is currently edited by")) break; 
				} catch (Exception e) {}
				Thread.sleep(1000);
			}*/			
			//WORKAROUND for the "course locked" check: try again to open course editor 
			selenium_B.click("ui=course::toolbox_courseTools_courseEditor()");
			selenium_B.waitForPageToLoad("30000");
			
			//A: continue threads hold by startInsertCp_A
			TemporaryPausedThread[] threadsA = startInsertCp_A.getPausedThreads();
			threadsA[0].continueThread();
						
      selenium_A.waitForPageToLoad("10000");
			//no need for clickAndWait here
			selenium_A.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
						
			selenium_A.click("ui=courseEditor::toolbox_insertCourseElements_clickInsertCourseElement()");
			selenium_A.waitForPageToLoad("30000");
			
			selenium_A.click("ui=courseEditor::toolbox_editorTools_closeEditor()");
			selenium_A.waitForPageToLoad("30000");
			
			//A: add CodepointRef here in cluster_A: endCourseEditCp_A
			
			//B: start course editor in cluster_B
			selenium_B.click("ui=course::toolbox_courseTools_courseEditor()");
			selenium_B.waitForPageToLoad("30000");
			
			//B: select the (Structure) node inserted by A
			selenium_B.click("ui=course::menu_structureNode()");
			selenium_B.waitForPageToLoad("30000");
			//B: change node title
			selenium_B.type("ui=courseEditor::content_TitleDescription_shortTitle()", "Structure B");
			selenium_B.click("ui=commons::save()");
			selenium_B.waitForPageToLoad("30000");
						
			
			//B: close course editor (releases lock)
			selenium_B.click("ui=courseEditor::toolbox_editorTools_closeEditor()");
			selenium_B.waitForPageToLoad("30000");
			
			//A: continue threads hold by endCourseEditCp_A
						
			selenium_A.click("ui=tabs::logOut()");
			selenium_B.click("ui=tabs::logOut()");
			
	}
	
	
}
