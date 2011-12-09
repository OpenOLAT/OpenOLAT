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
package org.olat.test.functional.lr;

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
 * 
 * test new xpaths
 * <br/>
 * <p>
 * Test setup: <br/>
 * create lr blog and podcast from tab lr and delete afterward<br/>
 * insert blog and podcast bb<br/>
 * <p>
 * Test case: <br/>
 * edit internal blog <br/>
 * edit blog in new tab <br/>
 * blogResource.createEntry(title, description, content) <br/>
 * publish and preview <br/>
 *
 *
 *
 *
 * @author sandra, finishing: alberto
 */
public class TestBlogAndPodcastLinks extends BaseSeleneseTestCase {
	private final String COURSE_NAME = "CourseName"+System.currentTimeMillis();
	private final String BLOG_TITLE = "My blog title";
	private final String PODCAST_TITLE = "My podcast title";
	private final String DESC = "My first lr";
	private final String BLOG_URI = "";
	private final String BLOG_DESC = "BLOG_DESC";
	
	
	
	public void testCreateLRBlogPodcast() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		// create lr blog and podcast from tab lr and delete afterward --> ok
		LearningResources lr1 = olatWorkflow.getLearningResources();
		LRDetailedView lrdv = lr1.createResource(BLOG_TITLE, DESC, LearningResources.LR_Types.BLOG);
		LearningResources lr2 = lrdv.deleteLR();
		LRDetailedView lrdv2 = lr2.createResource(PODCAST_TITLE, DESC, LearningResources.LR_Types.PODCAST);
		LearningResources lr3 = lrdv2.deleteLR();
		
		// insert blog and podcast bb --> ok
		CourseEditor courseEditor = lr3.createCourseAndStartEditing(COURSE_NAME, DESC);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.BLOG, true, BLOG_TITLE);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.PODCAST, true, PODCAST_TITLE);
		
		
		// edit internal blog 
		BlogEditor blogEditorInt = (BlogEditor)courseEditor.selectCourseElement(BLOG_TITLE);
		blogEditorInt.create(BLOG_TITLE, BLOG_DESC);
		
		// edit blog in new tab
		BlogResource blogResource = blogEditorInt.edit();
		
		//blogResource.createEntry(title, description, content)
		blogResource.createEntry("HongKong", "HongKong entry description", "dubai entry content", true);
		
		// publish and preview
		LRDetailedView lRDetailedView = blogResource.close();
		courseEditor = lRDetailedView.editCourseContent();
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
	}
}
