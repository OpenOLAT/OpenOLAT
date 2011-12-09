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
import org.olat.test.util.selenium.olatapi.course.editor.BlogEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.run.BlogRun;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the Blog BB and Blog Editor.
 * <br/>
 * <p>
 * Test setup:<br/>
 * 1. import Course "Course_with_all_bb.zip" <br/>
 * 2. enter title "EditBlogCourse" <br/>
 * 3. enter description "Edit And Configure Blog Test Course" <br/> 
 * <p>
 * Test case: <br/>
 * 1. login as author and go to tab learning resources <br/>
 * 2. start course editor <br/>
 * 3. click on publish course <br/>
 * 4. select "Blog intern"  <br/>
 * 5. click "Next" <br/>
 * 6. set permission "All registered OLAT users" <br/>
 * 7. click "Finish" <br/>
 * 8. close editor <br/>
 * 9. run course <br/>
 * 10. select BB "Blog intern" <br/>
 * 11. click on "Create new entry" <br/>
 * 12. enter title "ABlogEntryTitle" <br/>
 * 13. enter description "A Blog Entry Description" <br/>
 * 14. enter content "This is my short blog posting draft" <br/>
 * 15. click on save draft <br/>
 * 16. assure is visible "This is only a draft." <br/>
 * 17. click on "Edit entry" <br/>
 * 18. enter "This is my short blog posting and i like to share it with you." <br/>
 * 19. click on "Publish" <br/>
 * 20. assure is not visible "This is only a draft." <br/>
 * 21. click on "Comments (0)" for comment <br/>
 * 22. enter comment "I really enjoyed writing this." <br/>
 * 23. click save <br/>
 * 24. assure visible "Comments (1)" <br/>
 * 25. start course editor <br/>
 * 26. select BB "Blog intern" <br/>
 * 27. uncheck "Blocked for learners" from section "Present" <br/>
 * 28. click save <br/>
 * 29. uncheck "Blocked for learners" from section "Read and Write" <br/>
 * 30. click save <br/>
 * 31. click publish <br/>
 * 32. select "Blog intern" <br/>
 * 33. click "Next" <br/>
 * 34. click "Finish"  <br/>
 * 35. logout as author <br/>
 * 
 * 36. login as student and go to tab learning resources <br/>
 * 38. click "Search form" <br/>
 * 39. enter "EditBlogCourse" <br/>
 * 40. run course <br/>
 * 41. select BB "Blog intern" <br/>
 * 42. click Create new entry <br/>
 * 43. click on "Create new entry" <br/>
 * 44. enter title "StudiBlogTitle" <br/>
 * 45. enter description "A Student Blog Entry Description" <br/>
 * 46. enter content "This is a student's short blog posting" <br/>
 * 47. click on "Publish" <br/>
 * 48. assure is visible "A Student Blog Entry Description" <br/>
 * 49. close course <br/>
 * 50. logout as student <br/>
 * 
 * 51. login as author and go to tab learning resources <br/>
 * 52. go to tab learning resources <br/>
 * 53. click "Search form" <br/>
 * 54. enter "EditBlogCourse" <br/>
 * 55. click on "Detailed view" for "EditBlogCourse" <br/>
 * 56. click on "Delete" <br/>
 * 57. logout <br/>
 * 
 *  
 * </p>
 * 
 * @author Alberto Sanz
 *
 */

public class EditAndConfigureBlogTest extends BaseSeleneseTestCase {
	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip"; 
  private final String COURSE_NAME_PREFIX = "EditAndConfigureBlog-";
  private final String COURSE_NAME = COURSE_NAME_PREFIX+System.currentTimeMillis();

  private final String BLOG_INTERN = "Blog intern";
  private final String BLOG_ENTRY_TITLE = "ABlogEntryTitle";
  private final String BLOG_ENTRY_DESCRIPTION = "A Blog Entry Description";
  private final String BLOG_ENTRY_CONTENT1 = "This is my short blog posting draft";
  private final String BLOG_ENTRY_CONTENT2 = "This is my short blog posting and i like to share it with you.";
  private final String BLOG_ENTRY_COMMENT = "I really enjoyed writing this.";
  
  private final String BLOG_ENTRY_STUDENT = "StudiBlogTitle";
  private final String BLOG_ENTRY_STUDENT_DESCRIPTION = "A Student Blog Entry Description";
  private final String BLOG_ENTRY_STUDENT_CONTENT = "This is a student's short blog posting";
  
  
  
	@Override
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



  public void testEditAndConfigureBlog() throws Exception {
    Context context = Context.getContext();
    
		OLATWorkflowHelper workflowAuthor = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		CourseEditor courseEditor = workflowAuthor.getLearningResources().showCourseContent(COURSE_NAME).getCourseEditor();
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToCourseRun();
		BlogRun blogRun = courseRun.selectBlog(BLOG_INTERN);
		assertNotNull(blogRun);
		blogRun.createEntry(BLOG_ENTRY_TITLE, BLOG_ENTRY_DESCRIPTION, BLOG_ENTRY_CONTENT1, false);
		assertTrue(blogRun.hasDraft(BLOG_ENTRY_TITLE));
		blogRun.editEntry(BLOG_ENTRY_TITLE, null, BLOG_ENTRY_CONTENT2, true);
		assertFalse(blogRun.hasDraft(BLOG_ENTRY_TITLE));
		assertTrue(blogRun.hasComments(BLOG_ENTRY_TITLE,0));
		blogRun.commentEntry(BLOG_ENTRY_TITLE, BLOG_ENTRY_COMMENT);
		assertTrue(blogRun.hasComments(BLOG_ENTRY_TITLE,1));
		CourseEditor courseEditor2 = courseRun.getCourseEditor();
		BlogEditor blogEditor = (BlogEditor)courseEditor2.selectCourseElement(BLOG_INTERN);
		blogEditor.changeAccessBlockedForLearners(CourseElementEditor.ACCESS_TYPE.PRESENT);
		blogEditor.changeAccessBlockedForLearners(CourseElementEditor.ACCESS_TYPE.READ_AND_WRITE);
		courseEditor2.publishCourse();
		workflowAuthor.logout();
		
		//student
		OLATWorkflowHelper workflowStudent = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		CourseRun courseRun2 = workflowStudent.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
		BlogRun blogRun2 = courseRun2.selectBlog(BLOG_INTERN);
		blogRun2.createEntry(BLOG_ENTRY_STUDENT, BLOG_ENTRY_STUDENT_DESCRIPTION, BLOG_ENTRY_STUDENT_CONTENT, true);
		assertTrue(blogRun2.isTextPresent(BLOG_ENTRY_STUDENT_DESCRIPTION));
		workflowStudent.logout();
		
		//cleanup: none, it is done in setup. We might want to have a look at this course if the test failed!!!		
		
	}
}
