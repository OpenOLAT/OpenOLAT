package org.olat.test.functional.home;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.SinglePageEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Folder test class (See: OLAT-4394); tests the basic functionality of a folder.
 * TODO: LD: see TODOs below.
 * 
 * Test case:  <br/>
 * 1. go to personal folder/public  <br/>
 * 2. select all, delete selection  <br/>
 * 3. create file with name SAMPLE_FILE1  <br/>
 * 4. rename file  <br/>
 * 5. edit file <br/>
 * 6. assert that cannot create fie with invalid name <br/>
 * 7. upload doc file <br/>
 * 8. upload doc file again, do overwrite <br/>
 * 9. create folder <br/>
 * 10. assert that cannot create folder with invalid name <br/>
 * 11. create zip file <br/>
 * 12. assert that cannot create zip with invalid name <br/>
 * 13. delete doc file, assert that really deleted.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class FolderTest extends BaseSeleneseTestCase {
	
	private final String COURSE_NAME = "CourseFolderTest"+System.currentTimeMillis();
	private final String COURSE_DESCRIPTION = "CourseDescription"+System.currentTimeMillis();
	
	private final String SAMPLE_FILE1 = "selenium.txt";
	private final String SAMPLE_FILE2 = "selenium_mod.txt";
	private final String INVALID_FILE_NAME = "a:b";
	
	private final String SAMPLE_FOLDER = "sample_folder";
	private final String INVALID_FOLDER_NAME_1 = "folder:1";
	
	private final String ZIP_NAME = "archive";
	private final String INVALID_ZIP_NAME = "archive:zip";
	
	public void setUp() throws Exception {		
		Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);		
	}

	public void testPersonalFolder() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		
		Folder folder = workflow.getHome().getPersonalFolder();
		folder.selectFileOrFolder("public");
		
		//cleanup first
		folder.selectAll();
		folder.deleteSelection();
				
		//create file
		boolean fileCreated = folder.createFile(null, SAMPLE_FILE1, "bla");
		assertTrue("Asserts file created.",fileCreated);
		
		//rename file
		folder.editMetadata(SAMPLE_FILE1, SAMPLE_FILE2, "meta title", "meta description");
		assertTrue("Asserts file renamed.",folder.isFileOrFolderPresent(SAMPLE_FILE2));
		
		//edit file
		folder.editTxtFile(SAMPLE_FILE2, "any_text");
		assertEquals("Asserts text content.",folder.getText(SAMPLE_FILE2),"any_text");
		
	    //check that you cannot create file with an invalid name, empty, or already existing name
		boolean notCreated = folder.createFile(null, INVALID_FILE_NAME, "bla");
		assertFalse("Asserts that cannot create file with invalid name", notCreated);
		
		//upload file
		File doc = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Word.doc");
		String remoteDoc = Context.getContext().provideFileRemotely(doc);
		folder.uploadFile(null, remoteDoc);		
		
		//confirm overwrite file
		boolean overwritten = folder.uploadWithOverwrite(null, remoteDoc, true);
		assertTrue("Asserts that doc file was overwritten", overwritten);
		
	    //TODO: check that you cannot upload file with an invalid name, cannot overwrite file without confirmation
				
		//create folder: selenium_folder
		boolean folderCreated = folder.createFolder(SAMPLE_FOLDER);
		assertTrue("Asserts folder created.", folderCreated);
		
		//check that one cannot create folder with an invalid name
		folderCreated = folder.createFolder(INVALID_FOLDER_NAME_1);
		assertFalse("Asserts that cannot create folder with invalid name.", folderCreated);
					
		//zip one or more files		
		List selection = new ArrayList();
		selection.add(doc.getName());
		boolean zipped = folder.zipSelection(selection, ZIP_NAME);
		assertTrue("Asserts zip file created.", zipped);
		assertTrue("Asserts that a zip with the given name is present.", folder.isFileOrFolderPresent(ZIP_NAME+".zip"));
		
	    //check that you cannot use an invalid name for the zip file
		zipped = folder.zipSelection(selection, INVALID_ZIP_NAME);
		assertFalse("Asserts that cannot zip file with an invalid name.", zipped);
		folder.cancelZipSelection();
		
		//TODO: copy file to selenium_folder
		//TODO:check that you cannot copy twice
		//TODO:check that you cannot copy parent to child, but vice-versa should work
		
		//TODO:move file to folder, check that was moved
		//TODO:check that you get notified if you move, copy, unzip file with an invalid name: see how to smuggle a data with an invalid name
		
		
	    //delete file
		folder.deleteItem(doc.getName());
		assertFalse("Asserts that doc file was deleted.", folder.isTextPresent(doc.getName()));
	}
	
	/**
	 * TODO: LD
	 */
	/*private void testStorageFolder() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
	    //create course
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, COURSE_DESCRIPTION);
		
	    //insert single page, create html page and assign to single page		
		SinglePageEditor singlePageEditor = (SinglePageEditor)courseEditor.insertCourseElement(CourseElemTypes.SINGLE_PAGE, true, "single page2");
		singlePageEditor.setDescription("This is the second course TS090533 Description");
		singlePageEditor.createHTMLPage("second_html_descr", "a not very long content that serves as an example too");
		
		//delete html-page in the storage folder
		Folder storageFolder = courseEditor.storageFolder();
		storageFolder.deleteItem("second_html_descr.html");
	}*/
	
	
}
