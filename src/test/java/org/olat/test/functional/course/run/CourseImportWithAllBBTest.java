package org.olat.test.functional.course.run;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.CannotExecuteException;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.SCORM;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


/**
 * 
 * Tests course import with all building blocks, and asserts that all bb are visible.
 * <br/>
 * <p>
 * Test case: <br/>
 * Import course with all references <br/>
 * Open course <br/>
 * Check all course elements:
  podcast extern, 
  podcast intern, 
  blog extern, 
  blog intern, 
  topic assignment, 
  email, 
  calender,
  show preview <br/>
 * Delete course and all attached resources	<br/>
 * 
 * 
 * @author Hans-Jrg
 */

public class CourseImportWithAllBBTest extends BaseSeleneseTestCase {
	
	private final String courseTitle = "CourseImportTestCourse-"+System.currentTimeMillis();
	
	
	public void testCourseImport() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		
//Import course with all references
		File f = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip");
		assertNotNull("Could not locate the course zip!", f);
		assertTrue("file "+f.getAbsolutePath()+" not found!", f.exists());
		
		WorkflowHelper.importCourse(f, courseTitle, "Whatever right?");
//Open course	
				
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(courseTitle);
//Check all course elements
		//no more inline single page since 25.05.2010
		/*courseRun.selectCourseElement("Single page - inline");
		Thread.sleep(5000);
		courseRun.getSelenium().selectFrame("//iframe[contains(@src,'first.html')]");
		assertTrue(courseRun.isTextPresent("This is the first page"));	
		courseRun.getSelenium().selectFrame("relative=top");	
		*/
		
		courseRun.selectCourseElement("Single page - iframe");
		Thread.sleep(1000);
		courseRun.getSelenium().selectFrame("//iframe[contains(@src,'second.html')]");
		assertTrue(courseRun.getSelenium().isTextPresent("This is the second page"));
		courseRun.getSelenium().selectFrame("relative=top");	
		
		//courseRun = new CourseRun(courseRun.getSelenium());
		courseRun.selectCourseElement("External page");		
		Thread.sleep(1000);
		courseRun.getSelenium().selectFrame("//iframe[contains(@src,'http://www.google.com/')]");
		assertTrue(courseRun.getSelenium().isTextPresent("iGoogle"));
		courseRun.getSelenium().selectFrame("relative=top");		
		
		courseRun.selectCourseElement("CP learning content");		
		Thread.sleep(1000);
		courseRun.getSelenium().selectFrame("//iframe[contains(@src,'/MESOSWORLD/EXDE/EINF/EINF.html')]");
		assertTrue(courseRun.getSelenium().isTextPresent("empirischen"));
		courseRun.getSelenium().selectFrame("relative=top");
		
		SCORM scorm = courseRun.selectSCORM("SCORM learning content");
		scorm.showSCORMLearningContent();
		Thread.sleep(1000);
		scorm.getSelenium().selectFrame("//iframe[@id='scormContentFrame']");
		assertTrue(scorm.getSelenium().isTextPresent("Inland Rules"));	
		scorm.getSelenium().selectFrame("relative=top");
		courseRun = scorm.back();
    //	Thread.sleep(3000);
		Forum forum = courseRun.selectForum("Forum");
		forum.openNewTopic("Test Thread", "Yes, this is really a test");
		WikiRun wiki = courseRun.selectWiki("Wiki");
		wiki.editPage("Here is a wiki test page");
		Thread.sleep(1000);
		assertTrue(wiki.getSelenium().isTextPresent("Here is a wiki test page"));
		courseRun.selectCourseElement("File dialog");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Upload file"));
		courseRun.selectCourseElement("Folder");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("No files or folders"));
		courseRun.selectCourseElement("Assessment");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Result"));	
		courseRun.selectEnrolment("Enrolment").enrol("1stgroup");
		courseRun.selectCourseElement("Task");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Sample solution"));			
		courseRun.selectCourseElement("Test");
		Thread.sleep(1000);
		//translation key: qti.form.attempts
		assertTrue(courseRun.getSelenium().isTextPresent("Maximum number of attempts"));
		courseRun.selectCourseElement("Self-test");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Press the start button to begin with your self-test."));		
		courseRun.selectCourseElement("Questionnaire");		
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Press the start button to begin with your questionnaire."));
		
		//podcast extern
		courseRun.selectCourseElement("Podcast extern");
		Thread.sleep(1000);
		//podcast intern
		courseRun.selectCourseElement("Podcast intern");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Swedish Music"));
		//blog extern
		courseRun.selectCourseElement("Blog extern");
		Thread.sleep(1000);
		//blog intern
		courseRun.selectCourseElement("Blog intern");
		Thread.sleep(1000);
		assertTrue(courseRun.getSelenium().isTextPresent("Lorem Ipsum"));
		//topic assignment		
		courseRun.selectCourseElement("Topic assignment");
		Thread.sleep(1000);
		//email
		courseRun.selectCourseElement("E-mail");
		Thread.sleep(1000);
		//calender
		courseRun.selectCourseElement("Calendar");
		Thread.sleep(1000);
			
		//show preview
		CourseEditor courseEditor = courseRun.getCourseEditor();
		courseEditor.preview();
		
		Folder storageFolder = courseEditor.storageFolder();
		storageFolder.selectLink("_sharedfolder", true);
		storageFolder.selectLink("second.html", false);
		courseEditor = storageFolder.closeStorageFolder();
		//release locks
		workflow.logout();
	
	}
	
	
	
	@Override
	protected void cleanUpAfterRun() {
		//Delete course and all attached resources	
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		LearningResources learningResources = workflow.getLearningResources();
		learningResources.searchMyResource(courseTitle).deleteLR();
		
		String author = Context.getContext().getStandardAdminOlatLoginInfos(1).getUsername();
				
		try {
			deleteAllResources(learningResources, "fois_CSCW_de_scorm", author);		
			deleteAllResources(learningResources, "MESOS_EXDE_EINF", author);
			deleteAllResources(learningResources, "repo_1", author);
			deleteAllResources(learningResources, "repo_2", author);
			deleteAllResources(learningResources, "repo_3", author);
			deleteAllResources(learningResources, "test Wiki", author);
			deleteAllResources(learningResources, "Glossary", author);
			deleteAllResources(learningResources, "Resource folder", author);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}



	/**
	 * Deletes all resource with input resourceTitle and author. <p>
	 * Fill in the search form with resourceTitle and author, search, 
	 * select entry with resourceTitle title if any found, and delete resource
	 * and go back to the seach form. 
	 * 
	 * @param learningResources
	 * @param resourceTitle
	 * @param author
	 */
	private void deleteAllResources(LearningResources learningResources, String resourceTitle, String author) throws Exception {		
		LRDetailedView lRDetailedView = learningResources.searchResource(resourceTitle, author);
		while(lRDetailedView!=null) {
			try {
				learningResources = lRDetailedView.deleteLR();
			} catch (CannotExecuteException e) {
				break;
			}	
			lRDetailedView = learningResources.searchResource(resourceTitle, author);
		}		
		Thread.sleep(1000);
	}
	
}