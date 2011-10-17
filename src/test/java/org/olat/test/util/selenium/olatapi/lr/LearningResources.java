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
package org.olat.test.util.selenium.olatapi.lr;

import java.io.File;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.selenium.olatapi.qti.QuestionnaireEditor;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * OLAT abstraction for the Learning Resources tab.
 * Provides methods for the most common workflows.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class LearningResources extends OLATSeleniumWrapper {
	

	public enum LR_Types {COURSE, TEST, WIKI, GLOSSARY, QUESTIONNAIRE, RESOURCE_FOLDER, BLOG, PODCAST, CP} 

	/**
	 * @param selenium
	 */
	public LearningResources(Selenium selenium) {
		super(selenium);		
		
    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=learningResources::menu_searchForm()")) {
			throw new IllegalStateException("This is not the - Learning resources - page");
		}
	}

	/**
	 * Create resource with the given type, but do not start editing.
	 * @param title
	 * @param description
	 * @param type
	 * @return Returns a LRDetailedView object.
	 */
	public LRDetailedView createResource(String title, String description, LR_Types type) {
		createResourceWithoutStartEdit(title, description, type);
		if(LR_Types.COURSE.equals(type)) {
		  //new course wizard
		  //choose: create course with wizard, start course editor, or show detail view
		  selenium.click("ui=learningResources::courseImport_wizardShowDetailsView()");
		  selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		  selenium.waitForPageToLoad("30000");
		} else if(!LR_Types.WIKI.equals(type)) {
			//if the resource type is not wiki, the start dialog should show up
		  selenium.click("ui=learningResources::dialog_startNo()");
		  selenium.waitForPageToLoad("60000");
		  try { //TODO: LD: is this really needed?
				Thread.sleep(1000);
			} catch (InterruptedException e) {							
			}
		}
		return new LRDetailedView(selenium);
	}
	
	/**
	 * Clicks on the create XXX_resource button, fills the title and description, and clicks next.
	 * @param title
	 * @param description
	 * @param type
	 */
	private void createResourceWithoutStartEdit(String title, String description, LR_Types type) {
		if(LR_Types.COURSE.equals(type)) {
		  selenium.click("ui=learningResources::toolbox_create_course()");
		} else if(LR_Types.GLOSSARY.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_glossary()");
		} else if(LR_Types.WIKI.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_wiki()");
		} else if(LR_Types.QUESTIONNAIRE.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_questionnaire()");
		} else if(LR_Types.RESOURCE_FOLDER.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_resourceFolder()");
		} else if(LR_Types.TEST.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_test()");
		} else if(LR_Types.BLOG.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_blog()");
		} else if(LR_Types.PODCAST.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_podcast()");
		} else if (LR_Types.CP.equals(type)) {
			selenium.click("ui=learningResources::toolbox_create_createCP()");
		}
		
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::dialog_title()");
		selenium.type("ui=learningResources::dialog_title()", title);
	  		
		//SR:
		selenium.setSpeed("1000");
		
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()", description);
		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("60000");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {			
		}
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 */
	public void createGlossaryAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.GLOSSARY);
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: return a Glossary object if needed
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public CourseEditor createCourseAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.COURSE);
		
		selenium.click("ui=learningResources::courseImport_wizardStartCourseEditor()");
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
	
		return new CourseEditor(selenium);
	}
	
	/**
	 * Create a new Course wizard by creating new course
	 * @param title The title of the course
	 * @param description The description of the course
	 * @return A CourseWizard object that represents the wizard component
	 * 
	 * @author Thomas Linowsky
	 */

	public CourseWizard createCourseWizard(String title, String description){
		createResourceWithoutStartEdit(title, description, LR_Types.COURSE);
		
		selenium.click("ui=learningResources::courseWizard_selectWizardRadio()");
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
		return new CourseWizard(selenium);
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public TestEditor createTestAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.TEST);		
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		
		return new TestEditor(selenium);
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public QuestionnaireEditor createQuestionnaireAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.QUESTIONNAIRE);		
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		
		return new QuestionnaireEditor(selenium);
	}
	
	/**
	 * 
	 * @param title
	 * @param description
	 */
	public ResourceEditor createResourceFolderAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.RESOURCE_FOLDER);
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: return a ResourceFolderEditor if neccessary
		return new ResourceEditor(selenium, title);
	}
	
	/**
	 * Creates blog resource and opens it in a new tab.
	 * @param title
	 * @param description
	 * @return
	 */
	public BlogResource createBlogAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.BLOG);
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		return new BlogResource(selenium, title);
	}
	
	/**
	 * Creates podcast resource and opens it in a new tab.
	 * @param title
	 * @param description
	 * @return
	 */
	public PodcastResource createPodcastAndStartEditing(String title, String description) {
		createResourceWithoutStartEdit(title, description, LR_Types.PODCAST);
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		return new PodcastResource(selenium, title);
	}
  
	/**
	 * Select "Search form" and find the resource with the given title if provided,
	 * and with the given author if any provided.
	 * The title and the author could be null if not used for filtering.
	 * TODO : LD: filter after the resource type
	 * <br/>
	 * Could return null, if no resource found.
	 * 
	 * @param selenium
	 * @param title
	 * @param author could be null
	 * @return Returns a LRDetailedView instance if a resource was found, null otherwise
	 */
  public LRDetailedView searchResource(String title, String author) {
  	selenium.click("ui=learningResources::menu_searchForm()");
  	selenium.waitForPageToLoad("30000");
    
  	if(title!=null) {  	  
  	  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", title);
  	}
  	if(author!=null) {
  		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Author)", author);
  	}
  	//uncheck course type, if already checked
  	/*if(selenium.isChecked("ui=learningResources::content_searchForm_courseType()")) {
  		selenium.uncheck("ui=learningResources::content_searchForm_courseType()");
  	}*/
  	selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
  	selenium.waitForPageToLoad("30000");
    // if too many entries found - show all
  	if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}  	
  	
  	if(selenium.isTextPresent(title) && selenium.isElementPresent("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + title + ")")) {  	
  		selenium.click("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + title + ")");
    	selenium.waitForPageToLoad("30000");
  		return new LRDetailedView(selenium);
  	}
  	//no resource found
  	return null;
  }
  
  /**
   * Search resource and show the course content with the input title.
   * @param title
   * @return Returns a CourseRun instance if a course with the given name is found, else returns null.
   */
  public CourseRun searchAndShowCourseContent (String title) {
  	selenium.click("ui=learningResources::menu_searchForm()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", title);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
    //if too many entries found - show all
  	if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}  
		
  	selenium.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + title + ")");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {			
		}
		selenium.waitForPageToLoad("60000");
    
		return new CourseRun(selenium);		
  }
  
  /**
   * Select "My Entries", if too many [Show all] link present, click [Show all], 
   * and select the entry with the input name.
   * 
   * @param selenium
   * @param entryName
   * @return
   */
  public LRDetailedView searchMyResource(String title) {
  	selenium.click("ui=learningResources::menu_myEntries()");
		selenium.waitForPageToLoad("30000");
    //if too many entries in myEntries - show all 
		if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + title + ")");
		selenium.waitForPageToLoad("30000");
		
		return new LRDetailedView(selenium);
	}
  
  /**
   * Search and open a course from "My Entries".
   * @param title
   * @return
   */
  public CourseRun searchAndShowMyCourseContent(String title) {
  	selenium.click("ui=learningResources::menu_myEntries()");
		selenium.waitForPageToLoad("30000");
    //if too many entries in myEntries - show all 
		if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + title + ")");
		selenium.waitForPageToLoad("30000");
		
		return new CourseRun(selenium);
	}
     
  /**
   * Selects the "Courses" link, and next clicks on "Show content" of the course with the 
   * given title. It assumes that the course is visible on the first page of the courses list.
   * @return
   */
  public CourseRun showCourseContent(String title) {
  	selenium.click("ui=learningResources::menu_courses()");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}  	
		selenium.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + title + ")");
		selenium.waitForPageToLoad("30000");
				
		return new CourseRun(selenium);
  }
  
  /**
   * Show the wiki content with this title.
   * @param title
   * @return
   */
  public WikiRun showWikiContent(String title) {
  	selenium.click("ui=learningResources::menu_wikis()");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}  
		selenium.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + title + ")");
		selenium.waitForPageToLoad("30000");
		return new WikiRun(selenium);
  }
  
  /**
     * Shows the OLAT catalog view
     * 
     * Click the Catalog entry on the left hand menu
     * click it twice so the catalog is refreshed
     * @return The Catalog object representing the OLAT catalog
     */
    
    public Catalog showCatalog(){
  	  selenium.click("ui=learningResources::menu_catalog()");
  	  selenium.waitForPageToLoad("30000");
  	  selenium.click("ui=learningResources::menu_catalog()");
  	  selenium.waitForPageToLoad("30000");
  	  return new Catalog(selenium);
    }
  
  /**
   * Imports a course with the given title only if there is no course with this title.
   * Opens course editor and publish course.
   * 
   * @param zippedCourse
   * @param newTitleOfCourse
   * @param newDescriptionOfCourse
   * @return
   * @throws InterruptedException
   */
  public CourseEditor importCourse(File zippedCourse, String newTitleOfCourse, String newDescriptionOfCourse) throws InterruptedException {
		// check if course with newTitleOfCourse exists
		boolean checkIfExists = false;		
		LRDetailedView lRDetailedView = searchResource(newTitleOfCourse, null);
		checkIfExists = lRDetailedView!=null;
		
		if (checkIfExists) {
			System.out.println("Course with title " + newTitleOfCourse + " already exists, no need to import it!");
			return null;
		}

		String remoteFile = Context.getContext().provideFileRemotely(zippedCourse);
		
		System.out.println("===================");
		System.out.println("Course Import Start");
		System.out.println("       Course: " + newTitleOfCourse);
		System.out.println("       File:   " + zippedCourse.getAbsolutePath());
		System.out.println("       Remote: " + remoteFile);
		System.out.println("===================");
				
		selenium.click("ui=learningResources::toolbox_import_course()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::courseImport_uploadFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=learningResources::courseImport_fileChooser()",remoteFile);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("60000");
		
		while (!selenium.isElementPresent("ui=learningResources::dialog_title()")) {
			for (int second = 0;; second++) {
				if (second >= 120)
					break;
				try {
					if (selenium.isTextPresent("How do you wish to proceed?"))
						break;
				} catch (Exception e) {
				}
				Thread.sleep(500);
			}
			Thread.sleep(2000);
			selenium.click("ui=learningResources::courseImport_importReferencesImport()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::courseImport_importReferencesContinue()");
			selenium.waitForPageToLoad("30000");
		}
		// until the import is done
		selenium.type("ui=learningResources::dialog_title()", newTitleOfCourse);
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()",	newDescriptionOfCourse);		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("60000");	
		
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
				
		System.out.println("=================");
		System.out.println("Course Import End");
		System.out.println("=================");
		
		CourseEditor courseEditor = new CourseEditor(selenium);
		courseEditor.publishCourse();
		return courseEditor;		
	}
  
  /**
   * Import CP and open CP editor.
   * Attention: It could return null, if a resource with the same name already exists! Use unique names for resources!!!
   * 
   * @param zippedResource
   * @param newTitleOfCourse
   * @param newDescriptionOfCourse
   * @throws InterruptedException
   */
  public CPResourceEditor importCP(File zippedResource, String newTitleOfCourse, String newDescriptionOfCourse) throws InterruptedException {
  	if(zippedResource==null) {
  		throw new IllegalArgumentException("zippedResource is null!!");
  	}
	  boolean alreadyExists = checkIfExists(newTitleOfCourse);
	  if (alreadyExists) {
			System.out.println("CP with title " + newTitleOfCourse + " already exists, no need to import it!");
			return null;
		}
	  String remoteFile = Context.getContext().provideFileRemotely(zippedResource);
	 
	  selenium.click("ui=learningResources::toolbox_import_cpLearningContent()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::courseImport_uploadFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=learningResources::courseImport_fileChooser()",remoteFile);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("60000");		
				
		selenium.type("ui=learningResources::dialog_title()", newTitleOfCourse);
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()",	newDescriptionOfCourse);		
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");	
		selenium.click("ui=learningResources::dialog_startYes()");
		selenium.waitForPageToLoad("30000");
		
		return new CPResourceEditor(selenium,newTitleOfCourse);
  }
  
  /**
   * Check if resource exists. Returns true if found.
   * @param title
   * @return
   */
  private boolean checkIfExists(String title) {
	// check if resource with title exists
	boolean checkIfExists = false;		
	LRDetailedView lRDetailedView = searchResource(title, null);
	checkIfExists = lRDetailedView!=null;
		
	if (checkIfExists) {			
	  return true;
	}
	return false;
  }
 
  
  /**
   * Selects the courseIndex course in the next page, if desired, 
   * and returns a CourseRun if any found or null otherwise.
   * 
   * @param selectNextPage 
   * @param courseIndex starts at 1.
   * @return
   */
  public CourseRun showCourseContent (boolean selectNextPage, int courseIndex) {
    selectCoursesMenuItemOnce();
    
	  //select next page 
	  if(selectNextPage && selenium.isElementPresent("ui=commons::table_selectNextPage()")) {	  
	    selenium.click("ui=commons::table_selectNextPage()");	  
	    selenium.waitForPageToLoad("30000");
	  }
	
	  // select course in page
	  if(selenium.isElementPresent("ui=learningResources::content_courseTable_selectCourse(index=" + courseIndex + ")")) {
      selenium.click("ui=learningResources::content_courseTable_selectCourse(index=" + courseIndex + ")");
      selenium.waitForPageToLoad("30000");
      return new CourseRun(selenium);
	  }
	  //no course found for the input values
	  return null;
  }
  
  /**
   * Select courses menu item ONLY if not already selected.
   * Motivation: the implementation changed on 14.12.2010: 
   * Was: if the courses was on second page,
   * it remained on the second page upon new selection.
   * Now: each selection of courses leads to the first page.
   * 
   */
  private void selectCoursesMenuItemOnce() {
    if(!selenium.isElementPresent("ui=learningResources::menu_coursesMenuItemSelected()")) {
      selenium.click("ui=learningResources::menu_courses()");
      selenium.waitForPageToLoad("30000");
    }
  }
  
  /**
   * 
   * @return Returns true if the Forward button available.
   */
  public boolean hasMorePages() {
    return selenium.isElementPresent("ui=commons::table_selectNextPage()");
  }
  
  /**
   * Creates CP resource.
   * 
   * @param title
   * @param description
   * @return
   */
  public CPResourceEditor createCPAndStartEditing(String title, String description) {
	  createResourceWithoutStartEdit(title, description, LR_Types.CP);
	  selenium.click("ui=learningResources::dialog_startYes()");
	  selenium.waitForPageToLoad("30000");
	  return new CPResourceEditor(selenium, title);
  }

}
