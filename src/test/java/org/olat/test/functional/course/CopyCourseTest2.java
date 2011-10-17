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
package org.olat.test.functional.course;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests CourseFactory.copyCourse() in single VM mode - no codepoints.
 * 
 * Test case: <br/>
 * Creates 2 copies ("COPY A - DEMO COURSE" and "COPY B - DEMO COURSE") of the same "Demo Course", serial not parallel. <p>
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class CopyCourseTest2 extends BaseSeleneseTestCase {
	
	
	private final String DEMO_COURSE_NAME = Context.DEMO_COURSE_NAME_1;
	private final String COURSE_NAME = "CopyCourse" + System.currentTimeMillis();
	private final String CLONE_A = "COPY A - DEMO COURSE";
	private final String CLONE_B = "COPY B - DEMO COURSE";
		

  	
  /**
   * administrator adds author as owner of the "Demo Course"
   */
  public void setUp() throws Exception {  
  	System.out.println("CopyCourseTest - setUp - START");
  	Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
  	
  	//cleanup first
  	WorkflowHelper.deleteAllCoursesNamed(CLONE_A);
  	WorkflowHelper.deleteAllCoursesNamed(CLONE_B);
  	
    //clone "Demo Course" and work with the clone
  	assertTrue(WorkflowHelper.cloneCourse(context, DEMO_COURSE_NAME, COURSE_NAME));
  	
  	//author
  	OlatLoginInfos authorOlatLoginInfos = context.getStandardAuthorOlatLoginInfos(1);
    //"administrator" adds author as owner of the "Demo Course"
  	WorkflowHelper.addOwnerToLearningResource(authorOlatLoginInfos.getUsername(), COURSE_NAME);
  	System.out.println("CopyCourseTest - setUp - END");
  }
  
	/**
	 * Tests course copy in singleVM mode. 
	 * Creates 2 copies ("COPY A - DEMO COURSE" and "COPY B - DEMO COURSE") of the same "Demo Course". <p>
	 * 
	 * 
	 * @throws Exception
	 */
	public void testCopyCourse2() throws Exception {		
		Context context = Context.getContext();
				
		//administrator		
		OLATWorkflowHelper workflow_A = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		LRDetailedView lRDetailedView_A = workflow_A.getLearningResources().searchResource(COURSE_NAME, null);
				
		//author - owner of the "Demo Course"		
		OLATWorkflowHelper workflow_B = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		LRDetailedView lRDetailedView_B = workflow_B.getLearningResources().searchResource(COURSE_NAME, null);
		
		lRDetailedView_A.copyLR(CLONE_A, null);
		try { 
			Thread.sleep(5000);
		} catch (InterruptedException e) {							
		}
		lRDetailedView_B.copyLR(CLONE_B, null);
		
		workflow_A.logout();
		workflow_B.logout();			
	}
		
			
	
	/**
	 * Deletes the created copies.
	 */
	@Override
	public void cleanUpAfterRun() {		
		System.out.println("CopyCourseTest - cleanUpAfterRun - START");
				
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		LearningResources learningResources = workflow.getLearningResources().searchResource(CLONE_A, null).deleteLR();
		learningResources.searchResource(CLONE_B, null).deleteLR();
				
		System.out.println("CopyCourseTest - cleanUpAfterRun - END");
	}
}
