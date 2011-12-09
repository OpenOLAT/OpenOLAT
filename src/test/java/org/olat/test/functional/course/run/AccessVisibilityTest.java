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

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.AssessmentForm;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests course elements with access and visibility restrictions.
 * <br/>
 * Test setup: <br/>
 * 1. Delete all learning resources of author. <br/>
 * 2. Create student acvistudi03. 
 * <p>
 * Test case: <br/>
 * 1. Author creates wiki WIKI_NAME. <br/>
 * 2. Authors creates course COURSE_NAME and adds building blocks folder, forum, wiki and assessment. <br/>
 * 3. Author adds WIKI_NAME to building block wiki. <br/>
 * 4. Author opens group management, creates group lg av 1 and adds standard student to group. <br/>
 * 5. Author goes back to course editor and restricts visibility of forum to above created group. <br/>
 * 6. Author restricts access of assessment to above created group. <br/>
 * 7. Author configures visibility of wiki depending on the above assessment. <br/>
 * 8. Author publishes course and switches to course run. <br/>
 * 9. Student opens course, sets bookmark, checks if 3 out of 4 course elements are visible. <br/>
 * 10. Student acvistudi03 opens course, only sees assessment, but cannot access it (check message). <br/>
 * 11. Author opens assessment tool and sets student's assessment to "passed" . <br/>
 * 12. Student can now see wiki. <br/>
 * 13. Author deletes course and wiki. <br/> 
 * 
 * @author sandra
 * 
 */


public class AccessVisibilityTest extends BaseSeleneseTestCase {
	  
  
  private final String COURSE_NAME = "Access_Visibility-"+System.currentTimeMillis();
  private final String WIKI_NAME = "av_wiki";
  private boolean resourceCreated;
  
	
    public void testAccessVisibility() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos student02= context.createuserIfNotExists(1, "acvistudi03", standardPassword, true, false, false, false, false);
				
			// author01 creates wiki and course			
			OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			LearningResources learningResources1 = olatWorkflow1.getLearningResources();
			LRDetailedView lRDetailedView1 = learningResources1.createResource(WIKI_NAME, "course run test", LR_Types.WIKI);
			//select again the learningResources1
			learningResources1 = olatWorkflow1.getLearningResources();
			CourseEditor courseEditor1 = learningResources1.createCourseAndStartEditing(COURSE_NAME, "course run test");
			resourceCreated = true;			
			courseEditor1.insertCourseElement(CourseElemTypes.FOLDER, true, null);
			courseEditor1.insertCourseElement(CourseElemTypes.FORUM, true, null);
			courseEditor1.insertCourseElement(CourseElemTypes.WIKI, true, null);
			courseEditor1.insertCourseElement(CourseElemTypes.ASSESSMENT, true, null);
			courseEditor1.selectCourseElement("Wiki");
			courseEditor1.chooseMyWikiForElement("Wiki",WIKI_NAME);
			lRDetailedView1 = courseEditor1.closeToLRDetailedView();
			CourseRun courseRun1 = lRDetailedView1.showCourseContent();
			courseRun1.getGroupManagement().createGroupAndAddMembers("lg av 1", null, context.getStandardStudentOlatLoginInfos(1).getUsername());
			CourseEditor courseEditor = courseRun1.getCourseEditor();
			CourseElementEditor courseElementEditor = courseEditor.selectCourseElement("Forum");
			courseElementEditor.changeVisibilityDependingOnGroup("lg av 1");
      
			courseElementEditor = courseEditor.selectCourseElement("Folder");
			courseElementEditor.changeVisibilityDependingOnGroup("lg av 1");
						
			courseElementEditor = courseEditor.selectCourseElement("Assessment");
			courseElementEditor.changeAccessyDependingOnGroup("lg av 1");
			courseElementEditor.editVisibilityInfo("this assessment is only accessible to learning group members");
						
			courseElementEditor = courseEditor.selectCourseElement("Wiki");
			courseElementEditor.changeVisibilityDependingOnAssessment("Assessment*");
			courseEditor.publishCourse();
			courseRun1 = courseEditor.closeToCourseRun();						
				
		
			// student01 opens course, sets bookmark, checks if 3 out of 4 elements are visible
			OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
			LearningResources learningResources2 = olatWorkflow2.getLearningResources();
			CourseRun courseRun2 = learningResources2.searchAndShowCourseContent(COURSE_NAME);
			courseRun2.setBookmark();
									
			assertTrue(courseRun2.isTextPresent("Folder"));
			assertTrue(courseRun2.isTextPresent("Forum"));
			assertTrue(courseRun2.isTextPresent("Assessment"));
			assertFalse(courseRun2.isTextPresent("Wiki"));
		
			// student02 opens course and only sees assessment, but cannot access it (check message)
			OLATWorkflowHelper olatWorkflow3 = context.getOLATWorkflowHelper(student02);
			LearningResources learningResources3 = olatWorkflow3.getLearningResources();
			CourseRun courseRun3 = learningResources3.searchAndShowCourseContent(COURSE_NAME);
												
			assertFalse(courseRun3.isTextPresent("Folder"));
			assertFalse(courseRun3.isTextPresent("Forum"));		
			assertFalse(courseRun3.isTextPresent("Wiki"));
			courseRun3.selectCourseElement("Assessment");
			assertTrue(courseRun3.isTextPresent("this assessment is only accessible to learning group members"));			
		
			
			// author01 opens assessment tool and sets student01's assessment to "passed"			
			AssessmentForm assessmentForm = courseRun1.getAssessmentTool().getAssessmentFormAsPerUser(context.getStandardStudentOlatLoginInfos(1).getUsername(), "Assessment");
			assessmentForm.setPassed(true);
			assessmentForm.save();
			olatWorkflow1.logout();
							
			//student01 can now see wiki
			courseRun2.selectRoot(COURSE_NAME); 			
			assertTrue(courseRun2.isTextPresent("Wiki"));				
	}

		

		@Override
		protected void cleanUpAfterRun() {
			System.out.println("***************** cleanUpAfterRun STARTED *********************");
			if(resourceCreated) {
				//author01 deletes course and wiki
				OLATWorkflowHelper olatWorkflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos(1));
				LearningResources learningResources = olatWorkflow1.getLearningResources();
				LRDetailedView lRDetailedView = learningResources.searchMyResource(COURSE_NAME);
				try {
					learningResources = lRDetailedView.deleteLR();
				} catch (Exception e) {}				
				try {
					learningResources.searchMyResource(WIKI_NAME).deleteLR();
				} catch (Exception e) {}				
			}	
			System.out.println("***************** cleanUpAfterRun ENDED *********************");
		}
     
    
}



