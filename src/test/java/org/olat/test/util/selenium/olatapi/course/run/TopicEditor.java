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
package org.olat.test.util.selenium.olatapi.course.run;

import java.util.List;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Topic editor, this is role independent. See the javadoc for role specific info.
 * 
 * @author lavinia
 *
 */
public class TopicEditor extends OLATSeleniumWrapper {

	public TopicEditor(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	public TopicAssignmentRun back() {
		selenium.click("ui=commons::backLink()");
		selenium.waitForPageToLoad("30000");
		return new TopicAssignmentRun(selenium);
	}
	
	private void selectFolderTab() {
		if(selenium.isElementPresent("ui=projectBroker::folder_folderTab()")) {
			selenium.click("ui=projectBroker::folder_folderTab()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public boolean hasDropbox() {
		selectFolderTab();
		if (selenium.isElementPresent("ui=projectBroker::folder_dropboxDiv()")) {
			return true;
		}
		return false;
	}
	
	public boolean hasFileInDropBoxFolder(String folderName, String fileName) {
	  selectFolderTab();
	  //if it has folder, select it
	  if (selenium.isElementPresent("ui=projectBroker::folder_dropBoxFolderLink(folderName=" + folderName + ")")) {
	    selenium.click("ui=projectBroker::folder_dropBoxFolderLink(folderName=" + folderName + ")");
	    selenium.waitForPageToLoad("30000");	    
    }
	  //it has file
    if(selenium.isElementPresent("ui=projectBroker::folder_dropBoxFolderLink(folderName=" + fileName + ")")) {
      return true;
    }
    return false;
	}
	
	/**
	 * For students.
	 * @param fileName
	 */
	public void uploadFileInDropBox(String remoteFileName) {
	  selectFolderTab();
    selenium.click("ui=projectBroker::folder_uploadFile()");
    selenium.waitForPageToLoad("90000");
    selenium.click("ui=projectBroker::folder_uploadFileInOverlay()");
    selenium.waitForPageToLoad("90000");
    selenium.type("ui=upload::fileChooser()", remoteFileName);
    selenium.click("ui=upload::submit()");
    selenium.waitForPageToLoad("30000");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {      
    }
  }
	
	public boolean hasReturnbox() {
		selectFolderTab();
		if (selenium.isElementPresent("ui=projectBroker::folder_returnboxDiv()")) {
			return true;
		}
		return false;
	}
	
	public boolean hasReturnboxFolder(String folderName) {
	  selectFolderTab();
	  //if (selenium.isElementPresent("ui=projectBroker::folder_returnboxFolderCheckbox(folderName=" + folderName + ")")) {
	  if (selenium.isElementPresent("ui=projectBroker::folder_returnBoxFolderLink(folderName=" + folderName + ")")) {
      return true;
    }
    return false;
	}
	
	public boolean hasFileInReturnBoxFolder(String fileName) {
	  selectFolderTab(); 
	  //it has file
	  if(selenium.isElementPresent("ui=projectBroker::folder_returnBoxFolderLink(folderName=" + fileName + ")")) {
	    return true;
	  }
	  return false;
	}

		
	/**
	 * Accessible only for tutors.
	 */
	private void edit() {
		
	}
	
	
	/**
	 * Accessible only for tutors.
	 * @return
	 */
	public TopicAssignmentRun delete() {
	  selenium.click("ui=projectBroker::createTopic()");
	  selenium.waitForPageToLoad("30000");
	  return new TopicAssignmentRun(selenium);
	}
	
	
	/**
	 * Accessible only for tutors.
	 */
	private void createDocument(String name) {
		
	}	
		
	
	/**
	 * Accessible only for tutors.
	 * TODO: check if could use Folder.java
	 * @param filename
	 * @param title
	 * @param description
	 */
  public void uploadFileInReturnBoxFolder(String folderName, String remoteFileName) {
    selectFolderTab();
    //select folder
    selenium.click("ui=projectBroker::folder_returnBoxFolderLink(folderName=" + folderName + ")");
    selenium.waitForPageToLoad("30000");
    //upload file in the current folder
    selenium.click("ui=briefCase::uploadFile()");
    selenium.waitForPageToLoad("90000");
    selenium.type("ui=upload::fileChooser()", remoteFileName);
    selenium.click("ui=upload::submit()");
    selenium.waitForPageToLoad("30000");
	}
    
    /**
     * Accessible only for tutors.
     * @param name
     */
    private void createFolder(String name) {
		
	}
    
    public boolean hasAdminTab() {
    	if(selenium.isElementPresent("ui=projectBroker::administrationOfParticipants_adminTab()")) {
			return true;
		}
    	return false;
    }
    
    private void selectAminTab() {
    	if(selenium.isElementPresent("ui=projectBroker::administrationOfParticipants_adminTab()")) {
			selenium.click("ui=projectBroker::administrationOfParticipants_adminTab()");
			selenium.waitForPageToLoad("30000");
		}
    }
	
	/**
	 * Accessible only for tutors.
	 * @param userList
	 */
	private void addParticipants(List<String> userList) {
		
	}
	
	public boolean hasParticipant(String username) {
		selectAminTab();
		if(selenium.isElementPresent("ui=projectBroker::administrationOfParticipants_participants_checkUser(userName=" + username + ")")) {
			return true;
		}
	   	return false;	
	}
	
	
	public boolean hasCandidate(String username) {
		selectAminTab();
		if(selenium.isElementPresent("ui=projectBroker::administrationOfParticipants_candidates_checkUser(userName=" + username + ")")) {
			return true;
		}
	   	return false;	
	}
	
	public void moveAsParticipant(String username) {
		selectAminTab();
		if(selenium.isElementPresent("ui=projectBroker::administrationOfParticipants_candidates_checkUser(userName=" + username + ")")) {
			selenium.click("ui=projectBroker::administrationOfParticipants_candidates_checkUser(userName=" + username + ")");
			selenium.click("ui=projectBroker::administrationOfParticipants_candidates_moveToParticipants()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=commons::usertable_adduser_finish()");
			selenium.waitForPageToLoad("30000");
		} else {
		  throw new IllegalStateException("No participant with this username found!");	
		}
	}
	
	/**
	 * Accessible only for tutors.
	 * @param userList
	 */
	private void addAuthors(List<String> userList) {
		
	}
}
