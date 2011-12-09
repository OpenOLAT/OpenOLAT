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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.lr;

import org.olat.test.util.selenium.olatapi.CannotExecuteException;
import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.DisposedCourseRun;
import org.olat.test.util.selenium.olatapi.course.run.WikiRun;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;

import com.thoughtworks.selenium.Selenium;

/**
 * OLAT abstraction for the Detailed view of learning resource.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class LRDetailedView extends OLATSeleniumWrapper {
	
	public static final String ACCESS_ALL_REGISTERED = "All registered OLAT users";
	public static final String ACCESS_REGISTERED_AND_GUESTS = "Registered OLAT users and guests";
	public static final String ACCESS_OWNERS_AND_AUTHORS = "Owners and other OLAT authors";
	public static final String ACCESS_ONLY_OWNERS = "Only owners of this learning resource";
  
	/**
	 * @param selenium
	 */
	public LRDetailedView(Selenium selenium) {
		super(selenium);	
		
    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=learningResources::content_showContent()")) {
			throw new IllegalStateException("This is not the - Detailed view - page");
		}
	}

	/**
	 * Deletes this learning resource, if possible.
	 * If not possible to delete this LR, go back to the Learning resources,
	 * and throw new CannotExecuteException.
	 * 
	 * @return
	 * @throws CannotExecuteException
	 */
	public LearningResources deleteLR() {
		selenium.click("ui=learningResources::toolbox_learningResource_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYesLs()");
		//delete course takes very long since 12.01.2010 (the course logs are also deleted)
		selenium.waitForPageToLoad("150000");		
		//This learning resource cannot be deleted
		if(selenium.isTextPresent("This learning resource cannot be deleted.")) {
	    	//click OK
	    	selenium.click("ui=dialog::OK()");		    	
	    	//back to resource list
	    	selenium.click("ui=courseEditor::publishDialog_back()");
	  		selenium.waitForPageToLoad("30000");
	  		throw new CannotExecuteException("Cannot delete resource!");
		}
		return new LearningResources(selenium);
	}
	
	/**
	 * Modify properties.
	 * Modify access if necessary.
	 * @param accessLabel
	 */
	public void modifyProperties(String accessLabel) {
		//open the LR Settings dialog
		selenium.click("ui=learningResources::toolbox_learningResource_modifyProperties()");
		selenium.waitForPageToLoad("30000");
		//change access
		boolean accessChanged = false;
		if(ACCESS_ALL_REGISTERED.equals(accessLabel) && !selenium.isChecked("ui=learningResourcesModifieProperties::accessAllRegistered()")) {			
			selenium.click("ui=learningResourcesModifieProperties::accessAllRegistered()");
			accessChanged = true;
		} else if(ACCESS_REGISTERED_AND_GUESTS.equals(accessLabel) && !selenium.isChecked("ui=learningResourcesModifieProperties::accessRegisteredAndGuests()")) {
			selenium.click("ui=learningResourcesModifieProperties::accessRegisteredAndGuests()");
			accessChanged = true;
		} else if(ACCESS_OWNERS_AND_AUTHORS.equals(accessLabel) && !selenium.isChecked("ui=learningResourcesModifieProperties::accessOwnersAndAuthors()")) {
			selenium.click("ui=learningResourcesModifieProperties::accessOwnersAndAuthors()");
			accessChanged = true;
		} else if (ACCESS_ONLY_OWNERS.equals(accessLabel) && !selenium.isChecked("ui=learningResourcesModifieProperties::accessOnlyOwners()")) {
			selenium.click("ui=learningResourcesModifieProperties::accessOnlyOwners()");
			accessChanged = true;
		}
		if(accessChanged) {
		  selenium.click("ui=commons::flexiForm_saveButton()");
	      selenium.waitForPageToLoad("30000");
		  selenium.click("ui=overlay::overlayClose()");
		  selenium.waitForPageToLoad("30000");
		  //confirm modification of settings (new step in modify properties)
		  if(selenium.isElementPresent("ui=learningResources::dialog_yes()")) {
		    selenium.click("ui=learningResources::dialog_yes()");
			selenium.waitForPageToLoad("30000");
		  }
		} else {
			selenium.click("ui=overlay::overlayClose()");
			selenium.waitForPageToLoad("30000");
		}
		
	}
	
	/**
	 * Show course content.
	 * @return
	 */
	public CourseRun showCourseContent() {
		selenium.click("ui=learningResources::content_showContent()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	
	/**
	 * Show wiki content.
	 * @return
	 */
	public WikiRun showWikiContent() {
		selenium.click("ui=learningResources::content_showContent()");
		//selenium.waitForPageToLoad("30000");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return new WikiRun(selenium);
	}
	
	/**
	 * Click Edit content for a course detailed view.
	 * @return a new CourseEditor instance.
	 */
	public CourseEditor editCourseContent() {
		//TODO: LD: add a string independent for the resource type
		if(selenium.isTextPresent("Type") && selenium.isTextPresent("Course")) {
		  selenium.click("ui=learningResources::toolbox_learningResource_editContent()");
		  selenium.waitForPageToLoad("30000");

		  return new CourseEditor(selenium);
		}
		throw new IllegalStateException("This is not a course detailed view");
	}
	
	/**
	 * Try to edit the course who was already been open, and got disposed.
	 * The user sees the "Close and restart course" button.
	 * @return
	 */
	public DisposedCourseRun selectDisposedCourse() {
		selenium.click("ui=learningResources::toolbox_learningResource_editContent()");
	  selenium.waitForPageToLoad("30000");
	  return new DisposedCourseRun(selenium);
	  
	}
	
	/**
	 * Start editing test.
	 * @return a new TestEditor instance.
	 */
	public TestEditor editTestContent() {
		//TODO: LD: add a string independent for the resource type
		if(selenium.isTextPresent("Type") && selenium.isTextPresent("Test")) {
		  selenium.click("ui=learningResources::toolbox_learningResource_editContent()");
		  selenium.waitForPageToLoad("30000");

		  return new TestEditor(selenium);
		}
		throw new IllegalStateException("This is not a test detailed view");
	}
	
	/**
	 * The user tries to open the courseEditor but it is locked.
	 * The caller of this method expects to find the course locked, so it should not open the CourseEditor.
	 * 
	 * @return Returns true if the "alreadyLocked" message shows up, false otherwise.
	 */
	public boolean checkCourseLocked(String lockOwnerUsername) throws Exception {
    //	TODO: LD: add a string independent for the resource type
		if(selenium.isTextPresent("Type") && selenium.isTextPresent("Course")) {
			selenium.click("ui=learningResources::toolbox_learningResource_editContent()");

			// and wait until 'This course is currently edited by lockOwnerUsername and therefore locked.' appears
			for (int second = 0;; second++) {
				if (second >= 20) return false;
				try { //WARNING: HERE POTENTIAL FAILURE EACH TIME THE TRANSLATION CHANGES!
					if (selenium.isTextPresent("This course is being edited by "+lockOwnerUsername+" and therefore locked.")) 
						return true; 
				} catch (Exception e) {}

				Thread.sleep(1000);
			}
		}
		throw new IllegalStateException("This is not a course detailed view");
	}
	
		
	public void setBookmark() {
		selenium.click("ui=course::toolbox_generalTools_setBookmark()");
		selenium.waitForPageToLoad("30000");		
		selenium.click("ui=commons::saveInput()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
   * Assign new owner.
   * @param userName
   */
  public void assignOwner(String userName) {
  	selenium.click("ui=learningResources::toolbox_learningResource_assignOwners()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::toolbox_learningResource_assignOwners_addOwner()");
		selenium.waitForPageToLoad("30000");				
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userName);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser="+ userName +")");
		selenium.click("ui=commons::usertable_adduser_choose()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=overlay::overlayClose()");
		selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Assign new owners. 
   * It selects all users received by filtering with userName.
   * 
   * @param userName
   */
  public void assignOwners(String userName) {  	
		selenium.click("ui=learningResources::toolbox_learningResource_assignOwners()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::toolbox_learningResource_assignOwners_addOwner()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userName);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_userlist_selectAll()");
		selenium.click("ui=commons::usertable_adduser_choose()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=overlay::overlayClose()");
		selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Remove userName as owner of this learning resource.
   * @param userName
   * @return Returns Boolean.TRUE if the owner was successfully removed, 
   * 					Boolean.FALSE if the owner is the last one in group, 
   * 					and null if none of the above is true.
   */
  public Boolean removeOwner(String userName) {
  	Boolean successfullyRemoved = null;
  	
  	selenium.click("ui=learningResources::toolbox_learningResource_assignOwners()");
  	selenium.waitForPageToLoad("30000");
  	selenium.click("ui=commons::usertable_checkUsernameToRemove(nameOfUser=" + userName + ")");
  	selenium.click("ui=commons::usertable_removeOwner()");
  	selenium.waitForPageToLoad("30000");
  	if(selenium.isTextPresent("Do you really want to remove")) {
  	  selenium.click("ui=dialog::Yes()");
  	  selenium.waitForPageToLoad("30000");
  	  successfullyRemoved = true;
  	} else {
  		for (int second = 0; second < 20; second++) {				
				try { 
					if (selenium.isTextPresent("At least one user is required in a group.")) {
						successfullyRemoved = false;
						break; 
					}
				} 
				catch (Exception e) {}
  		}
  	}
  	selenium.click("ui=overlay::overlayClose()");
  	selenium.waitForPageToLoad("30000");
  	
  	return successfullyRemoved;
  }
  
  /**
   * Get the String of the Access value
   * 
   * Returns the value of course access, e.g. only owners
   * 
   * @return value of course access
   */
  public String getAccessString(){
	  return selenium.getText("ui=learningResources::content_selectedAccessLabel()");
  }
  
  /**
   * Make a copy of the current learning resource, withj the given title and description.
   * 
   * @param newTitle
   * @param newDescription could be null, keeps the old description
   */
  public LearningResources copyLR(String newTitle, String newDescription) {
  	if(selenium.isElementPresent("ui=learningResources::toolbox_learningResource_copy()")) {
  		selenium.click("ui=learningResources::toolbox_learningResource_copy()");
  		selenium.waitForPageToLoad("30000");	
  		try { //TODO: LD: is this really needed?
				Thread.sleep(5000);
			} catch (InterruptedException e) {							
			}
  		selenium.type("ui=learningResources::dialog_title()", newTitle);	
  		if(newDescription!=null) {
  		  selenium.type("ui=learningResources::dialog_description()", newDescription);
  		}
  		selenium.click("ui=commons::save()");				
  		selenium.waitForPageToLoad("30000");
  		selenium.click("ui=courseEditor::publishDialog_next()");
  		//selenium.waitForPageToLoad("30000");
  	} else {
  		throw new IllegalStateException("Cannot copy learning resource!");
  	}
  	return new LearningResources(selenium);
  }
  
  public void exportLR() {
  	selenium.click("ui=learningResources::toolbox_learningResource_exportContent()");
  	selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Close the course with dialog
   * 
   * @param cleanCatalog clean catalog entries?
   * @param cleanGroup clean group entries?
   * 
   * @author Thomas Linowsky
   */
  
  public void closeCourse(boolean cleanCatalog, boolean cleanGroup){
	  selenium.click("ui=learningResources::toolbox_learningResource_closeCourse()");
	  selenium.waitForPageToLoad("30000");
	  selenium.click("ui=courseEditor::publishDialog_next()");
	  selenium.waitForPageToLoad("30000");
	  if(cleanCatalog){
		  selenium.click("ui=learningResources::closeCourseWizard_cleanCatalog()");
	  }
	  if(cleanGroup){
		  selenium.click("ui=learningResources::closeCourseWizard_cleanGroup()");
	  }
	  selenium.click("ui=learningResources::dialog_buttonNext()");
	  selenium.waitForPageToLoad("30000");
	  selenium.click("ui=learningResources::dialog_buttonNext()");
	  selenium.waitForPageToLoad("30000");
  }
  
    /**
     * Add the course to Catalog root
     * 
     * @author Thomas Linowsky
     */
    
    public void addToCatalog(){
  	  selenium.click("ui=learningResources::toolbox_learningResource_addToCatalog()");
  	  selenium.waitForPageToLoad("30000");
  	  selenium.click("ui=learningResources::dialog_catalogRoot()");
  	  selenium.click("ui=dialog::select()");
    }
    
}
