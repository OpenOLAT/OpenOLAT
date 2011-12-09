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
package org.olat.test.functional.courseeditor;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.StructureEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.i18n.LocalStringProvider;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests that after course publish, any other user that has the course open gets a 
 * "This course has been modified" message, and must close the course tab.
 * 
 * @author eglis
 *
 */
public class CoursePublishInvalidateClusterTest extends BaseSeleneseTestCase {
    
    private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_2;
	  private final String COURSE_NAME = "CoursePublishInvalidate" + System.currentTimeMillis();
	
    private OlatLoginInfos user1;
    

    public void testMultiBrowserClusterCourseViewPublish() throws Exception {
    	
    	com.thoughtworks.selenium.Selenium selenium1;
    	
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		user1 = context.createuserIfNotExists(1, "mbcnla1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(2, "mbcnla2", standardPassword, true, true, true, true, true);
		
		//clone DEMO_COURSE_NAME and work with the clone
	  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
		
		{
			// open the course 'Demo course wiki', modify it and get ready to hit the publish button
			System.out.println("logging in browser 1...");
			OLATWorkflowHelper workflow1 = context.getOLATWorkflowHelper(user1);
			CourseEditor courseEditor = workflow1.getLearningResources().searchAndShowCourseContent(COURSE_NAME).getCourseEditor();
			StructureEditor structureEditor = courseEditor.getRoot(COURSE_NAME);
			structureEditor.setTitle("mod");
			//start publishing
			selenium1 = courseEditor.getSelenium();					
			selenium1.click("ui=courseEditor::toolbox_editorTools_publish()");
			for (int second = 0;; second++) {
				if (second >= 60) fail("timeout");
				try { if (selenium1.isTextPresent("Publishing")) break; } catch (Exception e) {}
				Thread.sleep(1000);
			}
	
			selenium1.click("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()");
			selenium1.click("ui=courseEditor::publishDialog_next()");
			Thread.sleep(1000);
			selenium1.click("ui=courseEditor::publishDialog_next()");
			Thread.sleep(1000);
			assertTrue(selenium1.isTextPresent("No problems found"));
			selenium1.click("ui=courseEditor::publishDialog_next()");
			Thread.sleep(1000);
			//assertTrue(selenium1.isTextPresent("Do you really want to publish this course?"));
			assertTrue(selenium1.isTextPresent(LocalStringProvider.COURSE_PUBLISH_CONFIRM));
		}
		
		CourseRun courseRun2;
		{
			// open 'Demo course wiki' with browser 2
			System.out.println("logging in browser 2...");
			OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(user2);
			courseRun2 = workflow2.getLearningResources().searchAndShowCourseContent(COURSE_NAME);				
		}
		
		{
			// now trigger the publish in browser 1
			selenium1.click("ui=courseEditor::publishDialog_finish()");
			for (int second = 0;; second++) {
				if (second >= 20) fail("timeout");
				try { if (selenium1.isTextPresent("Selected modifications published successfully")) break; } catch (Exception e) {}
				Thread.sleep(1000);
			}
		}
		
		{
			// after that, any click course run 2 (browser 2)
			//the root node gets modified, but the old browser session still shows
			// "OLAT: Demo course" as root entry. click on it will trigger the "this course has been modified
			if (courseRun2.getSelenium().isTextPresent("OLAT: Demo course")) {
				courseRun2.selectRoot("OLAT: Demo course");
			}
			// and waits until 'This course has been modified.' appears
			SeleniumHelper.waitUntilTextPresent(courseRun2.getSelenium(),"This course has been modified.", 20);
	  		// excellent, close course in browser 2		
	  		courseRun2.close(COURSE_NAME);
		}
		
	}
 
    
    @Override
  	protected void cleanUpAfterRun() {
  		System.out.println("***************** cleanUpAfterRun STARTED *********************");
  		
  		try {
  			//make sure you use the same user for cleanup, since the course could be locked
  			OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(user1);
  			workflow.getLearningResources().searchResource(COURSE_NAME, null).deleteLR();
  			workflow.logout();
  		} catch (Exception e) {
  			System.out.println("Exception while tried to delete the test course!!!");	
  		}	
  		
  		System.out.println("***************** cleanUpAfterRun ENDED *********************");
  	}
}
