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
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.BlogEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.BlogResource;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests the Blog BB and Blog Editor with both configurations internal and external
 * <br/>
 * <p>
 * Test case: <br/>
 * create course and add blog elements for internal and external blog <br/>
 * edit internal blog <br/>
 * edit blog in new tab <br/>
 * close blog tab <br/>
 * edit external blog <br/>
 * close blog tab <br/>
 * go back to course editor <br/>
 * publish and check course view <br/>
 * check content - assert <br/>
 * delete course <br/>
 * delete blog resource <br/>
 * 
 * 
 * @author sandra, alberto
 */
public class CreateBlogTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "BlogCourse"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	private final String EXT_BLOG = "An External Blog";
	private final String INT_BLOG = "My Internal Blog";
	private final String INT_BLOG_MOD = "Mod Internal";
	private final String BLOG_DESC = "My First Blog";
	private final String BLOG_TITEL_OF_CONTENT = "Dubai";
	private final String BLOG_DESC_OF_CONTENT = "My Dubai trip";
	private final String BLOG_CONTENT = "trip to Dubai entry description";

	
	
	
	public void testCreateBlog() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		// create course and add blog elements for internal and external blog
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.BLOG, true, INT_BLOG);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.BLOG, true, EXT_BLOG);
		
		// edit internal blog
		courseEditor.selectCourseElement(INT_BLOG);
		BlogEditor blogEditorInt = (BlogEditor)courseEditor.selectCourseElement(INT_BLOG);
		blogEditorInt.create(INT_BLOG, BLOG_DESC);
		
		// edit blog in new tab
		BlogResource blogResource_1 = blogEditorInt.edit();
		blogResource_1.editBlog(INT_BLOG_MOD, "Trips around the world", null);
		blogResource_1.createEntry(BLOG_TITEL_OF_CONTENT, BLOG_DESC_OF_CONTENT, BLOG_CONTENT, true);
		
		//close blog tab
		LRDetailedView lRDetailedView = blogResource_1.close();		
		courseEditor = lRDetailedView.editCourseContent();		 
				
		// edit external blog 
		BlogEditor blogEditorExt = (BlogEditor)courseEditor.selectCourseElement(EXT_BLOG);
		blogEditorExt.select(INT_BLOG_MOD);		
		BlogResource blogResource_2 = blogEditorExt.edit();
		
		// close blog tab
		LRDetailedView lRDetailedView2 = blogResource_2.close();
		// go back to course editor
		courseEditor = lRDetailedView2.editCourseContent();
		
		// publish and check course view
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		
		//check content
		courseRun.selectCourseElement(EXT_BLOG);
		assertTrue(courseRun.isTextPresent(BLOG_TITEL_OF_CONTENT));
		assertTrue(courseRun.isTextPresent(BLOG_DESC_OF_CONTENT));
				
	}


	@Override
	protected void cleanUpAfterRun() {
		OLATWorkflowHelper olatWorkflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos());
		//delete course
		LRDetailedView lRDetailedView3 = olatWorkflow.getLearningResources().searchMyResource(COURSE_NAME);
		LearningResources lrs = lRDetailedView3.deleteLR();
		//delete blog ressource
		LRDetailedView lRDetailedView4 = lrs.searchMyResource(INT_BLOG_MOD);
		lRDetailedView4.deleteLR();
	}
	
	
}
