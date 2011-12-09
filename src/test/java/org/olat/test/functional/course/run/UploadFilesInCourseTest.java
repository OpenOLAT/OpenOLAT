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
import org.olat.test.util.selenium.olatapi.course.editor.SinglePageEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.FileDialog;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests file upload for Folder, SinglePage, Forum, Wiki and FileDialog. 
 * <br/>
 * <p>
 * Test setup:<br/>
 * clean up before start <br/>
 * Import files <br/>
 * <p>
 * Test case:<br/>
 * Import files <br/>
 * Open course <br/>
 * Open editor, storage folder and delete existing html-file <br/>
 * Attach deleted single page again and check in preview <br/>
 * Go to forum and create a new message with two file attachments
 - check if uploaded properly and delete again <br/>
 * Go to wiki and upload a pdf file to be linked in the wiki
 - check if there <br/>
 * Delete course and all attached resources <br/>
 *
 *
 *
 *
 *
 * @author hjzuber
 *
 */
public class UploadFilesInCourseTest extends BaseSeleneseTestCase {
	
	public void testUploadFiles() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		

//Import files
		File course = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip");
		assertNotNull("Could not locate the course zip!", course);
		assertTrue("file "+course.getAbsolutePath()+" not found!", course.exists());
		String courseTitle = "CourseImportTestCourse-"+System.currentTimeMillis();
		WorkflowHelper.importCourse(course, courseTitle, "Whatever right?");
		
		File html = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "first.html");
		File doc = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Word.doc");
		File docx = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Word_new.docx");	
		File pdf = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Pdf.pdf");
		String remoteHtml = Context.getContext().provideFileRemotely(html);
		String remoteDoc = Context.getContext().provideFileRemotely(doc);
		String remoteDocx = Context.getContext().provideFileRemotely(docx);
		String remotePdf = Context.getContext().provideFileRemotely(pdf);

		
//Open course			
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseTitle);
		Thread.sleep(3000);
	  //Open editor, storage folder and delete existing html-file
		CourseEditor courseEditor = courseRun.getCourseEditor();
		Folder storageFolder = courseEditor.storageFolder();
		storageFolder.deleteItem("first.html");
		courseEditor = storageFolder.closeStorageFolder();
	  //Attach deleted single page again and check in preview
		//single page is always inframe since 25.05.2010
		SinglePageEditor singlePageEditor = (SinglePageEditor)courseEditor.selectCourseElement("Single page - inline");
		singlePageEditor.replacePage(SinglePageEditor.SELECT_TYPE.UPLOAD_TO_STORAGE_FOLDER, remoteHtml);
		singlePageEditor.preview();
		Thread.sleep(5000);
		courseRun.getSelenium().selectFrame("//iframe[contains(@src,'first.html')]");
		assertTrue(singlePageEditor.isTextPresent("This is the first page"));		
		courseRun.getSelenium().selectFrame("relative=top");
		singlePageEditor.closePreview();
		
	//Go to forum and create a new message with two file attachments - check if uploaded properly and delete again
		courseRun = courseEditor.closeToCourseRun();
		Forum forum = courseRun.selectForum("Forum");
		forum.openNewTopic("Yes we want to upload", "two Word files");
		forum.attachFileToMsg(remoteDoc);
		Thread.sleep(1000);
		assertTrue(forum.isTextPresent("Word.doc"));
		forum.attachFileToMsg(remoteDocx);
		Thread.sleep(1000);
		assertTrue(forum.isTextPresent("Word_new.docx"));
		forum.deleteAttachedFile("Word_new.docx");	
		//Go to wiki and upload a pdf file to be linked in the wiki - check if there
		WikiRun wikiRun = courseRun.selectWiki("Wiki");
		wikiRun.insertMedia(remotePdf, "Pdf.pdf");
		Thread.sleep(1000);
		assertTrue(wikiRun.isTextPresent("Media:Pdf.pdf"));	
		
		FileDialog fileDialog = courseRun.selectFileDialog("File dialog");
		fileDialog.uploadFile(remotePdf);		
		Thread.sleep(1000);
		assertTrue(courseRun.isTextPresent("Pdf.pdf"));		
		fileDialog.deleteSingleFile();
		
		Folder folder = courseRun.selectFolder("Folder");
		folder.uploadFile(null, remoteDoc);
		assertTrue(folder.isTextPresent("Word.doc"));
		
	  //Delete course and all attached resources		
		courseRun.getDetailedView().deleteLR();

		deleteResource(workflow, "fois_CSCW_de_scorm");
		deleteResource(workflow, "MESOS_EXDE_EINF");
		
		deleteResource(workflow, "repo_1");
		deleteResource(workflow, "repo_2");
		deleteResource(workflow, "repo_3");
		deleteResource(workflow, "test Wiki");
		deleteResource(workflow, "Glossary");
		deleteResource(workflow, "Resource folder");
						
	}	
		
	/**
	 * Deletes resource with the given name, having the administrator as author.
	 * Filter for the author as well, else won't work for selenium load.
	 * @param workflow
	 * @param title
	 */
	private void deleteResource(OLATWorkflowHelper workflow, String title) {
		LRDetailedView lRDetailedView = workflow.getLearningResources().searchResource(title, Context.getContext().getStandardAdminOlatLoginInfos(1).getUsername());
		if(lRDetailedView!=null) {
		  lRDetailedView.deleteLR();
		}
	}
}
