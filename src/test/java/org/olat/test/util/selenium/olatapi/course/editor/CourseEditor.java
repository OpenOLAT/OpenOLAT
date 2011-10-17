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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.test.util.selenium.olatapi.course.editor;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.i18n.LocalStringProvider;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;

import com.thoughtworks.selenium.Selenium;

/**
 * OLAT abstraction for the CourseEditor.
 * One can get a CourseEditor instance either via the CourseRun, via the LRDetailedView,
 * or via LearningResources createCourseAndStartEditing.
 * <p>
 * @author Lavinia Dumitrescu
 *
 */
public class CourseEditor extends OLATSeleniumWrapper {
	
	//default titles for the course elements
	public static final String STRUCTURE_TITLE = "Structure"; 
	public static final String SINGLE_PAGE_TITLE = "Single page";
	public static final String EXTERNAL_PAGE_TITLE = "External page"; 
	public static final String CP_LEARNING_CONTENT_TITLE = "CP learning content"; 
	public static final String SCORM_LEARNING_CONTENT_TITLE = "SCORM learning content"; 
	public static final String FORUM_COURSE_ELEM_TITLE = "Forum"; 
	public static final String WIKI_TITLE = "Wiki"; 
	public static final String FILE_DIALOG_TITLE = "File dialog"; 
	public static final String FOLDER_TITLE = "Folder"; 
	public static final String ASSESSMENT_TITLE = "Assessment";
	public static final String TASK_TITLE = "Task"; 
	public static final String TEST_TITLE = "Test"; 
	public static final String SELF_TEST_TITLE = "Self-test"; 
	public static final String QUESTIONNAIRE_TITLE = "Questionnaire"; 		
	public static final String ENROLMENT_TITLE = "Enrolment";	
	public static final String CONTACT_FORM_TITLE = "E-mail";	
	public static final String BLOG_TITLE = "Blog";
	public static final String PODCAST_TITLE = "Podcast";	
	public static final String TOPIC_ASSIGNMENT_TITLE = "Topic assignment";
	public static final String CALENDAR_TITLE = "Calendar";
	public static final String LTI_TITLE = "LTI page";
	
	
	//ALL VALID COURSE ELEMENT TYPES
  public enum CourseElemTypes {STRUCTURE, SINGLE_PAGE, EXTERNAL_PAGE, CP_LEARNING_CONTENT, SCORM_LEARNING_CONTENT, 
	FORUM, WIKI, FILE_DIALOG, FOLDER, ASSESSMENT, TASK, TEST, SELF_TEST, QUESTIONNAIRE, ENROLMENT, CONTACT_FORM, BLOG, PODCAST, TOPIC_ASSIGNMENT, CALENDAR, 
	LTI_PAGE, INFO_MESSAGE} 
	
  //ALL SUPPORTED INSERT TYPES
  public enum InsertPosition {FIRST_CHILD_OF_ROOT, LAST_CHILD_OF_ROOT, FIRST_CHILD_OF_ELEMENT, FOLLOWING_SIBLING_OF_ELEMENT}
  
  private String currentElementTitle;
	
	/**
	 * 
	 * @param selenium
	 */
	public CourseEditor(Selenium selenium) {
		super(selenium);
		
    //Check that we're on the right place
		//if(!selenium.isTextPresent("Close editor")) {
		if(!selenium.isElementPresent("ui=courseEditor::toolbox_editorTools_closeEditor()")) {
			//sleep and check again later
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {				
			}
			if(!selenium.isElementPresent("ui=courseEditor::toolbox_editorTools_closeEditor()")) {
			  throw new IllegalStateException("This is not the - Course editor - page");
			}
		}
	}
	

  /**
   * Insert a course element of the given type, with the newTitle if any provided (newTitle!=null).
   * If newTitle==null the element gets the default title.
   * @param elementType
   * @param asFirstChildOfRoot, if true as first child, else as last child
   * @param newTitle
   * @return Returns an instance of the CourseElementEditor by default,
	 * or an EnrolmentEditor if an enrolment element was selected, 
   * or an TestElementEditor, 
   * SelfTestElementEditor, 
   * FolderEditor, 
   * SinglePageEditor, 
   * AssessmentEditor.
   */
	public CourseElementEditor insertCourseElement(CourseElemTypes elementType, boolean asFirstChildOfRoot, String newTitle) {
		clickInsertElement(elementType);
		
		if(asFirstChildOfRoot) {
		  selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		} else {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsLastChild()");
		}
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_clickInsertCourseElement()");
		selenium.waitForPageToLoad("30000");
		if(newTitle!=null) {
			selenium.type("ui=courseEditor::content_TitleDescription_shortTitle()", newTitle);
			selenium.click("ui=courseEditor::content_TitleDescription_save()");
			selenium.waitForPageToLoad("30000");
		}
		
		return returnCourseElement(elementType);
	}
	
	/**
	 * Inserts a course element of the given type, in the given position (one of the possible InsertType) relative to the element with elementTitle.
	 * If newTitle==null the element gets the default title. <p>
	 * elementTitle must not be null if the insertType is InsertType.FIRST_CHILD_OF_ELEMENT or InsertType.FOLLOWING_SIBLING_OF_ELEMENT.
	 * <p>
	 * @param elementType
	 * @param insertType
	 * @param elementTitle
	 * @param newTitle
	 * @return
	 */
  public CourseElementEditor insertCourseElement(CourseElemTypes elementType, InsertPosition insertType, String elementTitle, String newTitle) {
  	clickInsertElement(elementType);
				
  	if(InsertPosition.FIRST_CHILD_OF_ROOT.equals(insertType)) {
		  selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		} else if(InsertPosition.LAST_CHILD_OF_ROOT.equals(insertType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsLastChild()");
		} else if(InsertPosition.FIRST_CHILD_OF_ELEMENT.equals(insertType) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFirstChild(title=" + elementTitle + ")");
		} else if(InsertPosition.FOLLOWING_SIBLING_OF_ELEMENT.equals(insertType) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFollowingSibling(title=" + elementTitle + ")");
		} else {
			throw new IllegalStateException("InsertType or elementTitle are not acceptable!");
		}
		
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_clickInsertCourseElement()");
		selenium.waitForPageToLoad("30000");
		if(newTitle!=null) {
			selenium.type("ui=courseEditor::content_TitleDescription_shortTitle()", newTitle);
			selenium.click("ui=courseEditor::content_TitleDescription_save()");
			selenium.waitForPageToLoad("30000");
		}
		
		return returnCourseElement(elementType);
  }
  
  private void clickInsertElement(CourseElemTypes elementType) {
  	if(CourseElemTypes.STRUCTURE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertStructure()");
		} else if(CourseElemTypes.SINGLE_PAGE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertSinglePage()");
		} else if(CourseElemTypes.EXTERNAL_PAGE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertExternalPage()");
		} else if(CourseElemTypes.CP_LEARNING_CONTENT.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertCP()");
		} else if(CourseElemTypes.SCORM_LEARNING_CONTENT.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertSCORM()");
		} else if(CourseElemTypes.FORUM.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertForum()");
		} else if(CourseElemTypes.WIKI.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertWiki()");
		} else if(CourseElemTypes.FILE_DIALOG.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertFileDialog()");
		} else if(CourseElemTypes.FOLDER.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertFolder()");
		} else if(CourseElemTypes.ASSESSMENT.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAssessment()");
		} else if(CourseElemTypes.TASK.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertTask()");
		} else if(CourseElemTypes.TEST.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertTest()");
		} else if (CourseElemTypes.SELF_TEST.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertSelfTest()");
		} else if(CourseElemTypes.QUESTIONNAIRE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertQuestionnaire()");
		}	else if(CourseElemTypes.ENROLMENT.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertEnrolment()");
		} else if(CourseElemTypes.CONTACT_FORM.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertContactForm()");
		} else if(CourseElemTypes.BLOG.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertBlog()");
		} else if(CourseElemTypes.PODCAST.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertPodcast()");
		} else if(CourseElemTypes.TOPIC_ASSIGNMENT.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertTopicAssignment()");
		} else if(CourseElemTypes.CALENDAR.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertCalendar()");
		} else if(CourseElemTypes.LTI_PAGE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertLTIPage()");
		} else if(CourseElemTypes.INFO_MESSAGE.equals(elementType)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertInfoMessagePage()");
		}
		selenium.waitForPageToLoad("60000");
  }
  
  private CourseElementEditor returnCourseElement(CourseElemTypes elementType) {
  //check the inserted element type to decide the return type
		if(CourseElemTypes.ENROLMENT.equals(elementType)) {
			return new EnrolmentEditor(selenium);
		} else if(CourseElemTypes.TEST.equals(elementType)) {
			return new TestElementEditor(selenium);
		} else if (CourseElemTypes.SELF_TEST.equals(elementType)) {
			return new SelfTestElementEditor(selenium);
		} else if(CourseElemTypes.FOLDER.equals(elementType)) {
			return new FolderEditor(selenium);
		} else if(CourseElemTypes.SINGLE_PAGE.equals(elementType)) {
			return new SinglePageEditor(selenium);
		} else if(CourseElemTypes.ASSESSMENT.equals(elementType)) {
			return new AssessmentEditor(selenium);
		} else if(CourseElemTypes.QUESTIONNAIRE.equals(elementType)) {
			return new QuestionnaireElementEditor(selenium);//			
	    } else if(CourseElemTypes.BLOG.equals(elementType)) {
			return new BlogEditor(selenium, currentElementTitle);
	    } else if(CourseElemTypes.PODCAST.equals(elementType)) {
 			return new PodcastEditor(selenium, currentElementTitle);
		} else if (CourseElemTypes.TOPIC_ASSIGNMENT.equals(elementType)) {
			return new TopicAssignmentEditor(selenium);
		} else if (CourseElemTypes.LTI_PAGE.equals(elementType)) {
      return new LTIPageEditor(selenium);
    }
		return new CourseElementEditor(selenium);
  }
	
	/**
	 * Deteles the current selected course element.
	 * @param elementTitle
	 */
	public void deleteCourseElement() {
		selenium.click("ui=courseEditor::toolbox_modifyTools_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=dialog::Yes()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Restore the deleted course element.
	 */
	public void undeleteCourseElement() {
		selenium.click("ui=courseEditor::content_undeleteCourseElement()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Moves the current selected course element and inserts it accordingly with the insertPosition. <p>
	 * elementTitle could be null if the insert position is relative to the root.
	 * @param insertPosition
	 * @param elementTitle
	 */
	public void moveCourseElement(InsertPosition insertPosition, String elementTitle) {
		selenium.click("ui=courseEditor::toolbox_modifyTools_move()");
		selenium.waitForPageToLoad("30000");
		
		if(InsertPosition.FIRST_CHILD_OF_ROOT.equals(insertPosition)) {
		  selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		} else if(InsertPosition.LAST_CHILD_OF_ROOT.equals(insertPosition)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsLastChild()");
		} else if(InsertPosition.FIRST_CHILD_OF_ELEMENT.equals(insertPosition) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFirstChild(title=" + elementTitle + ")");
		} else if(InsertPosition.FOLLOWING_SIBLING_OF_ELEMENT.equals(insertPosition) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFollowingSibling(title=" + elementTitle + ")");
		} else {
			throw new IllegalStateException("InsertType or elementTitle are not acceptable!");
		}
				
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_clickInsertCourseElement()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void copyCourseElement(InsertPosition insertPosition, String elementTitle) {
		selenium.click("ui=courseEditor::toolbox_modifyTools_copy()");
		selenium.waitForPageToLoad("30000");
		
		if(InsertPosition.FIRST_CHILD_OF_ROOT.equals(insertPosition)) {
		  selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		} else if(InsertPosition.LAST_CHILD_OF_ROOT.equals(insertPosition)) {
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsLastChild()");
		} else if(InsertPosition.FIRST_CHILD_OF_ELEMENT.equals(insertPosition) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFirstChild(title=" + elementTitle + ")");
		} else if(InsertPosition.FOLLOWING_SIBLING_OF_ELEMENT.equals(insertPosition) && elementTitle!=null && !elementTitle.equals("")) { 
			selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsElementsFollowingSibling(title=" + elementTitle + ")");
		} else {
			throw new IllegalStateException("InsertType or elementTitle are not acceptable!");
		}
				
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_clickInsertCourseElement()");
		selenium.waitForPageToLoad("30000");
	}
	
	
	
		/**
	 * Call this if the CourseEditor was created via the LRDetailedView,
	 * or if the course was just imported/created.
	 * @return
	 */
	public LRDetailedView closeToLRDetailedView() {
		selenium.click("ui=courseEditor::toolbox_editorTools_closeEditor()");
		selenium.waitForPageToLoad("30000");
		
		return new LRDetailedView(selenium);
	}
	
	
	/**
	 * Call this if the CourseEditor was created via the CourseRun.
	 * @return
	 */
	public CourseRun closeToCourseRun() {
		selenium.click("ui=courseEditor::toolbox_editorTools_closeEditor()");
		selenium.waitForPageToLoad("30000");
		
		return new CourseRun(selenium);
	}
	
	/**
	 * Straightforward course publish.
	 * Click "Publish", "Select all", "Next", select "All registered users", "Finish".	 
	 *
	 */
	public void publishCourse() {
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=courseEditor::publishDialog_selectall()")) {
		  selenium.click("ui=courseEditor::publishDialog_selectall()");
		  // select-all is implemented locally in the client - without any request to the server
		  // selenium seems to not interpret this as a page-load consistently
		  // hence disabling the waitForPageToLoad and replacing it with a sleep of 1 sec
		  //selenium.waitForPageToLoad("30000");
		  try{ 
			  Thread.currentThread().sleep(1000);
		  } catch(InterruptedException ie) {
			  ie.printStackTrace(System.out);
		  }
		}
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");
		selenium.select("ui=courseEditor::publishDialog_courseAccessDropDown()", "label=All registered OLAT users");
		//TODO: LD: add here check if next selectable, else finish
		if(selenium.isElementPresent("ui=courseEditor::publishDialog_next()")) {
		  selenium.click("ui=courseEditor::publishDialog_next()");
		  selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=courseEditor::publishDialog_finish()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @return Returns true if anything found to be published, false otherwise.
	 */
	public boolean publishFirstChangedElement() {		
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()")) {
		  System.out.println("There is something to publish ...");
		//if(!selenium.isTextPresent("The course is up to date.")) {
		  selenium.click("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()");
		  //TODO: LD: select only certain elements
		  if(selenium.isElementPresent("ui=courseEditor::publishDialog_next()")) {
		    selenium.click("ui=courseEditor::publishDialog_next()");
		    selenium.waitForPageToLoad("30000");
		  }
		  if(selenium.isElementPresent("ui=courseEditor::publishDialog_next()")) {
		    selenium.click("ui=courseEditor::publishDialog_next()");
		    selenium.waitForPageToLoad("30000");
		  }
		  if(selenium.isElementPresent("ui=courseEditor::publishDialog_finish()")) {
		    selenium.click("ui=courseEditor::publishDialog_finish()");
		    selenium.waitForPageToLoad("30000");
		    System.out.println("Published finished!");
		  }
		  return true;
		} else {
			System.out.println("No modifications to be published. The course is up to date. ");
			selenium.click("ui=dialog::Cancel()");
			selenium.waitForPageToLoad("30000");
		}
		return false;
	}
	
		
	/**
	 * Publishes only the root element, plus lots of asserts.
	 * @throws Exception
	 */
	public void publishCourseAfterCourseTitleChanged() throws Exception {
		selenium.click("ui=courseEditor::toolbox_editorTools_publish()");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (selenium.isTextPresent("Publishing")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		selenium.click("ui=courseEditor::publishDialog_howToPublish_firstTreeCheckbox()");
		selenium.click("ui=courseEditor::publishDialog_next()");
		Thread.sleep(1000);
		selenium.click("ui=courseEditor::publishDialog_next()");
		Thread.sleep(1000);
		assertTrue(selenium.isTextPresent("No problems found"));
		selenium.click("ui=courseEditor::publishDialog_next()");
		Thread.sleep(1000);
		//pbl.confirm.users
		//assertTrue(selenium.isTextPresent("Do you really want to publish this course?"));
		assertTrue(selenium.isTextPresent(LocalStringProvider.COURSE_PUBLISH_CONFIRM));
		selenium.click("ui=courseEditor::publishDialog_finish()");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (selenium.isTextPresent("Selected modifications published successfully")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
	}
	
	/**
	 * Selects the course element with the given title. 
	 * @param title
	 * @return Returns an instance of the CourseElementEditor by default,
	 * or an EnrolmentEditor if an enrolment element was inserted, 
   * or an TestElementEditor, 
   * SelfTestElementEditor, 
   * FolderEditor, 
   * SinglePageEditor, 
   * AssessmentEditor.
	 */
	public CourseElementEditor selectCourseElement(String title) {
	  selectTruncatedTitle(title);
		return getCurrentElement();
	}
	
	private void selectTruncatedTitle(String title) {
	  String truncatedTitle = title;
    if(title.length()>22) {
      truncatedTitle = title.substring(0, 22);
    }
    currentElementTitle = truncatedTitle;
    selenium.click("ui=courseEditor::menu_link(link=" + truncatedTitle + ")");
    selenium.waitForPageToLoad("60000");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block      
    }
	}
		
	
	public CourseElementEditor selectNextCourseElement(String title) {
		//TODO: LD: implement this!!!
		return null;
	}
	
	/**
	 * Returns true if an element with this title is found.
	 * @param title
	 * @return
	 */
	public boolean containsElement(String title) {
		String truncatedTitle = title;
		if(title.length()>22) {
		  truncatedTitle = title.substring(0, 22);
		}
		currentElementTitle = truncatedTitle;
		return selenium.isElementPresent("ui=courseEditor::menu_link(link=" + truncatedTitle + ")");		
	}
	
	/**
	 * Selects the root if title provided, else assumes the root is selected.
	 * Per default the root is selected at open course editor.
	 * @return
	 */
	public StructureEditor getRoot (String title) {
		if(title!=null) {
		  return (StructureEditor)selectCourseElement(title);
		} else {
			//hopefully the ROOT is selected!!!, if not bummer!
			return (StructureEditor)getCurrentElement();
		}
	}
	
	 /**
   * Automatically checks the type of the currently selected course element.
   * 
   * @return the current selected course element.
   */
	private CourseElementEditor getCurrentElement() {
	  if(selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
	    return new EnrolmentEditor(selenium);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbFolder_tabFolderConfiguration()")) {
	    return new FolderEditor(selenium);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbStructure_scoreTab()")) {
	    return new StructureEditor(selenium);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()")) {
	    return new SelfTestElementEditor(selenium);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbTest_tabTestConfiguration()")) {
	    return new TestElementEditor(selenium);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbSinglePage_tabPageContent()") && selenium.isTextPresent("Single page")) {
	    //warning: it should have a long title containing "Single page" string in it, any better xpath?
	    return new SinglePageEditor(selenium); 		
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbBlog_tabBlogLearningContent()")) {
	    return new BlogEditor(selenium, currentElementTitle);
	  } else if(selenium.isElementPresent("ui=courseEditor::content_bbPodcast_tabPodcastLearningContent()")) {
	    return new PodcastEditor(selenium, currentElementTitle);
	  } else if (selenium.isElementPresent("ui=courseEditor::content_lti_tabPageContent()")  && selenium.isTextPresent("LTI page")) {
	    //warning: it should have a long title containing "Single page" string in it, any better xpath?
	    return new LTIPageEditor(selenium);
	  }
	  //TODO: LD: Add more course elements if necessary
	  return new CourseElementEditor(selenium);
	}
	
	/**
	 * Select a wiki course element an choose wiki resource (from My entries) for it.
	 * @param elementTitle
	 * @param wikiTitle
	 */
  public void chooseMyWikiForElement(String elementTitle, String wikiTitle) {
  	selenium.click("ui=courseEditor::menu_link(link=" + elementTitle + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbWiki_tabWikiLearningContent()");
		selenium.waitForPageToLoad("30000");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// nothing to do			
		}
		selenium.click("ui=courseEditor::content_bbWiki_chooseWiki()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_chooseWiki(nameOfWiki=" + wikiTitle + ")");
		selenium.waitForPageToLoad("30000");
	}
  
  /** 
   * Choose wiki for the current selected course element by searching after wiki with wikiTitle and authorName. <p>
   * This is an alternative to chooseMyWikiForElement (if myEntries list is too long).
   * @param wikiTitle
   * @param authorName
   */
  public void chooseWikiForElement(String wikiTitle, String authorName) {
  	selenium.click("ui=courseEditor::content_bbWiki_tabWikiLearningContent()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbWiki_chooseWiki()");
		selenium.waitForPageToLoad("30000");	
		selenium.click("ui=courseEditor::commons_chooseLr_search()");
		selenium.waitForPageToLoad("30000");			
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Author)", authorName);		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", wikiTitle);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_chooseWiki(nameOfWiki=" + wikiTitle + ")");
		selenium.waitForPageToLoad("30000");
  }
	  
  /**
   * Shows course preview, and closes preview.
   *
   */
  public void preview() {
  	selenium.click("ui=courseEditor::toolbox_editorTools_coursePreview()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::preview_showCoursePreview()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::preview_closePreview()");
		selenium.waitForPageToLoad("30000");
  }
  
  public CoursePreview openPreview () {
  	selenium.click("ui=courseEditor::toolbox_editorTools_coursePreview()");
		selenium.waitForPageToLoad("30000");	
		return new CoursePreview(selenium);
  }
  
  
  /**
   * Selects the Storage folder.
   * @return
   */
  public Folder storageFolder() {
  	selenium.click("ui=courseEditor::toolbox_editorTools_storageFolder()");
		selenium.waitForPageToLoad("30000");
		return new Folder(selenium);
  }
  
}
