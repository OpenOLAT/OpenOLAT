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

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Folder Course Element page.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class FolderEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public FolderEditor(Selenium selenium) {
		super(selenium);
		if(!selenium.isElementPresent("ui=courseEditor::content_bbFolder_tabFolderConfiguration()")) {
			throw new IllegalStateException("This is not the - Folder editor - page");
		}
	}
	
	/**
   * Changes access to the current selected course element, blocked for learners read and write.
   *
   */
  public void changeAccessBlockForLearnersReadAndWrite() {
  	if(selenium.isElementPresent("ui=courseEditor::content_access_tabAccess()")) {
  		selenium.click("ui=courseEditor::content_access_tabAccess()");
  		selenium.waitForPageToLoad("30000");
  	}
  	selenium.click("ui=courseEditor::content_access_blockedForLearnersReadAndWrite()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_access_saveReadAndWrite()");
		selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Changes access to the current selected course element, blocked for learners read only.
   *
   */
  public void changeAccessBlockForLearnersReadOnly() {
  	if(selenium.isElementPresent("ui=courseEditor::content_access_tabAccess()")) {
  		selenium.click("ui=courseEditor::content_access_tabAccess()");
  		selenium.waitForPageToLoad("30000");
  	}
  	selenium.click("ui=courseEditor::content_access_blockedForLearnersReadOnly()");
		selenium.waitForPageToLoad("30000");
		//Save button no more visible in olat7
		/*selenium.click("ui=courseEditor::content_access_saveReadOnly()");
		selenium.waitForPageToLoad("30000");*/
  }
	
}
