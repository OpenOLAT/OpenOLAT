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

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CoursePreview;
import org.olat.test.util.selenium.olatapi.course.editor.PodcastEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.PodcastResource;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the Podcast BB and Podcast Editor with both configurations internal and external
 * <br/>
 * <p>
 * Test case: <br/>
 * create course with podcast element <br/>
 * create 1 podcast whose entries are created within olat <br/>
 * create 1 external podcast <br/>
 * publish <br/>
 * preview course <br/>
 * check content of external podcast <br/>
 * check content of internal podcast <br/>
 * check content as student <br/>
 * delete course and delete podcast resources <br/>
 * 
 * 
 * @author alberto
 */
public class CreatePodcastTest extends BaseSeleneseTestCase {	
	private final String COURSE_NAME = "CreatePodcast"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	private final String INT_PODCAST = "Internal";
	private final String EXT_PODCAST = "External";
	private final String EXT_PODCAST_DESC = "First external Podcast";
	private final String INT_PODCAST_DESC = "First OLAT Podcast";
	
	
	public void testCreatePodcast() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos());
		
		// create course and add podcast elements for internal and external podcast
		CourseEditor courseEditor = olatWorkflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.PODCAST, true, INT_PODCAST);
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.PODCAST, true, EXT_PODCAST);
		
		//create 1 podcast whose entries are created within olat
		courseEditor.selectCourseElement(INT_PODCAST);
		PodcastEditor podcastEditorInt = (PodcastEditor)courseEditor.selectCourseElement(INT_PODCAST);
		podcastEditorInt.create(INT_PODCAST, INT_PODCAST_DESC);
				
		//create 1 external podcast
		courseEditor.selectCourseElement(EXT_PODCAST);
		PodcastEditor podcastEditorExt = (PodcastEditor)courseEditor.selectCourseElement(EXT_PODCAST);
		podcastEditorExt.create(EXT_PODCAST, EXT_PODCAST_DESC);
		PodcastResource podcastResource = podcastEditorExt.edit();
		podcastResource.includeExternal(EXT_PODCAST, EXT_PODCAST_DESC,  "feed://pod.drs.ch/focus_-_die_talkshow_mpx.xml");
		LRDetailedView lrDetailedView = podcastResource.close();
				
		//create Episode	
		courseEditor = lrDetailedView.editCourseContent();
		podcastEditorInt = (PodcastEditor)courseEditor.selectCourseElement(INT_PODCAST);
		podcastResource = podcastEditorInt.edit();
		File mediaFile = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "firstmedia.flv");
		String remoteFile = Context.getContext().provideFileRemotely(mediaFile);
		podcastResource.createEpisode("Episode_title", "Episode_description", remoteFile);
		LRDetailedView lRDetailedView = podcastResource.close();		
		
		// publish
		courseEditor = lrDetailedView.editCourseContent();
		courseEditor.publishCourse();
		
		//preview course
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		courseRun.selectCourseElement(EXT_PODCAST);
		
		//check content of external podcast
		assertTrue(courseRun.isTextPresent(EXT_PODCAST_DESC));
		
		//check content of internal podcast
		courseRun.selectCourseElement(INT_PODCAST);
		assertTrue(courseRun.isTextPresent(INT_PODCAST_DESC));
		
		//preview as student
		courseEditor = courseRun.getCourseEditor();
		CoursePreview coursePreview = courseEditor.openPreview();
		coursePreview.showPreview();
		
		//check content as student
		assertTrue(courseRun.isTextPresent("Course element of the type Podcast"));
		coursePreview.selectCourseElement(EXT_PODCAST);
		//TODO: When Podcast element has a preview, replace next line with accordingly
		assertTrue(courseRun.isTextPresent("No preview available for this course element"));
		coursePreview.selectCourseElement(EXT_PODCAST);
		
		//delete course and delete resource
		courseEditor = coursePreview.closePreview();
		courseRun = courseEditor.closeToCourseRun();
		lrDetailedView = courseRun.getDetailedView();
		courseRun.close(COURSE_NAME);
		LearningResources lrs = lrDetailedView.deleteLR();
		lrDetailedView = lrs.searchMyResource(EXT_PODCAST);
		lrs = lrDetailedView.deleteLR();
		lrDetailedView = lrs.searchMyResource(INT_PODCAST);
		lrs = lrDetailedView.deleteLR();
		
		
	}
}