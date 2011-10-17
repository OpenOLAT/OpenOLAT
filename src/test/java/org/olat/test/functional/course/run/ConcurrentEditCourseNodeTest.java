package org.olat.test.functional.course.run;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;

import com.thoughtworks.selenium.SeleniumException;

/**
 * 
 * Wiki and forum are edited concurrently. 
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Author creates wiki WIKI_NAME. <br/>
 * 2. Authors creates course COURSE_NAME and adds building blocks forum and wiki. <br/>
 * 3. Author adds WIKI_NAME to building block wiki. <br/>
 * 4. Author publishes course.  <br/>
 * 5. Author goes to course run, selects wiki and edits index page. <br/>
 * 6. Student opens course, opens wiki, creates new page. <br/>
 * 7. Author and students edit same wiki page simultaneously, check if only one of them can edit.  <br/>
 * 8. Student goes to version tab of wiki page. <br/>
 * 9. Author edits wiki page and deletes it. <br/>
 * 10. Student gets message that page was deleted. <br/>
 * 11. Student navigates to forum and opens new topic. <br/>
 * 12. Author opens forum and deletes topic. <br/>
 * 13. Student tries to edit topic but cannot. <br/>
 * 14. Author removes forum and publishes course. <br/>
 * 15. Student gets message to restart course.  <br/>
 * 
 * @author sandra
 * 
 */

public class ConcurrentEditCourseNodeTest extends BaseSeleneseTestCase {

	 protected com.thoughtworks.selenium.Selenium selenium[] = new com.thoughtworks.selenium.Selenium[2];
   
   //dynamic course name, just in case it won't be deleted at the end of the test
   private final String COURSE_NAME = "concurrent edit course-" + System.currentTimeMillis();
   private final String WIKI_NAME = "concurrent edit-" + System.currentTimeMillis();
   
   private CourseRun courseRun_0;
   private CourseRun courseRun_1;
   
	
	public void testConcurrentEditCourseNode() throws Exception {
		Context context = Context.setupContext(getFullName(),
				SetupType.TWO_NODE_CLUSTER);

		{
			// author creates wiki and course with wiki and forum 
			OLATWorkflowHelper olatWorkflow_0 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
			LearningResources learningResources_0 = olatWorkflow_0.getLearningResources();
			LRDetailedView lRDetailedView = learningResources_0.createResource(WIKI_NAME, "selenium", LR_Types.WIKI);
			learningResources_0 = olatWorkflow_0.getLearningResources();
			CourseEditor courseEditor_0 = learningResources_0.createCourseAndStartEditing(COURSE_NAME, "selenium");
			courseEditor_0.insertCourseElement(CourseElemTypes.FORUM, true, null);
			courseEditor_0.insertCourseElement(CourseElemTypes.WIKI, true, null);
			courseEditor_0.chooseWikiForElement(WIKI_NAME, context.getStandardAuthorOlatLoginInfos(1).getUsername());
			courseEditor_0.publishCourse();
			LRDetailedView lRDetailedView_0 = courseEditor_0.closeToLRDetailedView();
			courseRun_0 = lRDetailedView_0.showCourseContent();
			WikiRun wikiRun_0 = (WikiRun)courseRun_0.selectWiki("Wiki");			
			wikiRun_0.editPage("Welcome");
			selenium[0] = wikiRun_0.getSelenium();
		}	
		
		{
			// student creates wiki page
			OLATWorkflowHelper olatWorkflow_1 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
			courseRun_1 = olatWorkflow_1.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
			WikiRun wikiRun_1 = courseRun_1.selectWiki("Wiki");
			wikiRun_1.createOrSearchArticle("Deleteconcurrency", "Deleteconcurrency test entry");
			wikiRun_1.selectIndex();
			selenium[1] = wikiRun_1.getSelenium();
		}
		
		{
			//both try to edit same wiki page
			selenium[0].click("ui=wiki::topNavigation_editPage()");
			selenium[1].click("ui=wiki::topNavigation_editPage()");
			//selenium[0].waitForPageToLoad("30000");
			//selenium[1].waitForPageToLoad("30000");			
			
			int numWinners = 0;
			int numLoosers = 0;
			for(int i=0; i<2; i++) {				
				if (selenium[i].isTextPresent("Edit:Index")) {
					numWinners++;
				} else if (selenium[i].isTextPresent("The page (Index) is being modified by:")) {
					numLoosers++;
				} else {
					fail("oups...");
				}
			}
			assertEquals("expected only 1 editor", 1, numWinners);
			assertEquals("expected only 1 who cannot edit", 1, numLoosers);
		}
		
		{
			// student goes to page deleteconcurrency version tab
			selenium[1].click("ui=wiki::sideNavigation_from-a-z()");
			selenium[1].waitForPageToLoad("30000");
			selenium[1].click("ui=wiki::sideNavigation_clickAWikiPage(nameOfWikiPage=Deleteconcurrency)");
			selenium[1].waitForPageToLoad("30000");
			selenium[1].click("ui=wiki::topNavigation_versions()");
			selenium[1].waitForPageToLoad("30000");
		}
		
		{
			// author deletes this page
			selenium[0].click("ui=wiki::sideNavigation_from-a-z()");
			selenium[0].waitForPageToLoad("30000");
			selenium[0].click("ui=wiki::sideNavigation_clickAWikiPage(nameOfWikiPage=Deleteconcurrency)");
			selenium[0].waitForPageToLoad("30000");
			selenium[0].click("ui=wiki::topNavigation_editPage()");
			//selenium[0].waitForPageToLoad("30000");
			selenium[0].click("ui=wiki::edit_deletePage()");
			//selenium[0].waitForPageToLoad("30000");
			selenium[0].click("ui=dialog::Okay()");
			selenium[0].waitForPageToLoad("30000");
		}
		
		{
			// student gets message that article has been deleted, opens forum message
			selenium[1].click("ui=wiki::topNavigation_article()");
			selenium[1].waitForPageToLoad("30000");
			assertTrue(selenium[1].isTextPresent("This article has been deleted and cannot be displayed anymore."));
			
			//we are still in CourseRun context so the object courseRun_1 is still valid
			Forum forum1 = courseRun_1.selectForum("Forum");
			forum1.openNewTopic("test entry concurrent edit", "forum message editing");					
		}
		
		{
			// author opens forum and deletes message
			Forum forum_0 = courseRun_0.selectForum("Forum");
			forum_0.deleteForumTopic("test entry concurrent edit");			
		}
		
		{
			// student tries to edit forum message
			selenium[1].click("ui=course::content_forum_edit()");
			selenium[1].waitForPageToLoad("30000");
			for (int second = 0;; second++) {
				if (second >= 60) fail("timeout");
				//translation key: header.cannoteditmessage
				try { if (selenium[1].isTextPresent("Post cannot be edited")) break; } catch (Exception e) {}
				Thread.sleep(1000);
			}

			selenium[1].click("ui=course::menu_link(link=Forum)");
			selenium[1].waitForPageToLoad("30000");
			
		}
		{
		// author removes forum
		CourseEditor courseEditor_0 = courseRun_0.getCourseEditor();
		courseEditor_0.selectCourseElement("Forum");
		courseEditor_0.deleteCourseElement();
		courseEditor_0.publishCourse();
		courseRun_0 = courseEditor_0.closeToCourseRun();
		//courseRun_0.getDetailedView();		
	}
	
	{
		// student should get message to restart course
		
		// code here is required because Ajax can come at any time and do a poll resulting 
		// in this 'Please close this course and restart' text to show up without the user clicking anywhere
		if (!selenium[1].isTextPresent("Please close this course and restart.")) {
			try{
				selenium[1].click("ui=course::menu_link(link=Forum)");
				selenium[1].waitForPageToLoad("30000");
			} catch(SeleniumException se) {
				if (!selenium[1].isTextPresent("Please close this course and restart.")) {
					fail("Could not click link=Forum but also didn't see text saying 'Please close this course and restart.'");
				}
			}
		}
		assertTrue(selenium[1].isTextPresent("Please close this course and restart."));
		selenium[1].click("ui=tabs::closeCourse(nameOfCourse=" + COURSE_NAME + ")");
		selenium[1].waitForPageToLoad("30000");
	}	
	}

	
	@Override
	protected void cleanUpAfterRun() {
		System.out.println("***************** cleanUpAfterRun STARTED *********************");
		 //	author deletes course and wiki		
		LRDetailedView lRDetailedView1 = courseRun_0.getDetailedView();
		try {
			LearningResources learningResources = lRDetailedView1.deleteLR();
			LRDetailedView lRDetailedView2 = learningResources.searchMyResource(WIKI_NAME);
			lRDetailedView2.deleteLR();
		} catch (Exception e) {}		
		System.out.println("***************** cleanUpAfterRun ENDED *********************");
	}
	
}
