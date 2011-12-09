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
package org.olat.test.util.selenium.olatapi.course.editor;

import org.olat.test.util.selenium.olatapi.CannotExecuteException;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Single page - Page content editor.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class SinglePageEditor extends CourseElementEditor {
	
	public enum SELECT_TYPE {CREATE, CHOOSE_FROM_STORAGE_FOLDER, UPLOAD_TO_STORAGE_FOLDER};

	/**
	 * @param selenium
	 */
	public SinglePageEditor(Selenium selenium) {
		super(selenium);
		 //Check that we're on the right place
		if(!selenium.isElementPresent("ui=courseEditor::content_bbSinglePage_tabPageContent()")) {
			throw new IllegalStateException("This is not the - Single page - page");
		}
	}

	/**
	 * Replace page, if possible. 
	 * Throws CannotExecuteException if button not available.
	 * @param type
	 * @param title
	 * @throws CannotExecuteException
	 */
	public void replacePage(SELECT_TYPE type, String title) {
		if (selenium.isElementPresent("ui=courseEditor::content_bbSinglePage_tabPageContent()")) {
			selenium.click("ui=courseEditor::content_bbSinglePage_tabPageContent()");
			selenium.waitForPageToLoad("30000");
		}		
		if(selenium.isElementPresent("ui=courseEditor::content_bbSinglePage_replacePage()")) {
		  selenium.click("ui=courseEditor::content_bbSinglePage_replacePage()");
		  selenium.waitForPageToLoad("30000");
		  if(SELECT_TYPE.UPLOAD_TO_STORAGE_FOLDER.equals(type)) {
		    selenium.type("ui=upload::fileChooser()", title);
		    selenium.click("ui=upload::submit()");
			  selenium.waitForPageToLoad("30000");
		  } else if(SELECT_TYPE.CHOOSE_FROM_STORAGE_FOLDER.equals(type)) {
		  	//TODO: LD: add code
		  } else if(SELECT_TYPE.CREATE.equals(type)) {
		  	//TODO: LD: Add code
		  }
		  
		} else {
			throw new IllegalStateException("Replace page - button not available!");
		}
	}
	
	public void createHTMLPage(String fileName, String content) {
		if (selenium.isElementPresent("ui=courseEditor::content_bbSinglePage_tabPageContent()")) {
			selenium.click("ui=courseEditor::content_bbSinglePage_tabPageContent()");
			selenium.waitForPageToLoad("30000");
		}	
		selenium.click("ui=courseEditor::content_bbSinglePage_selectOrCreatePage()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=New HTML page)", fileName);
		selenium.click("ui=commons::flexiForm_createButton()");				
		selenium.waitForPageToLoad("30000");
		this.typeInRichText(content);
		selenium.click("ui=courseEditor::content_bbSinglePage_saveAndClose()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Show the Preview page.
	 *
	 */
	public void preview() {
		selenium.click("ui=courseEditor::content_bbSinglePage_previewSinglePage()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Close the preview
	 *
	 */
	public void closePreview() {
		if(selenium.isElementPresent("ui=courseEditor::preview_closePreview()")) {
		    selenium.click("ui=courseEditor::preview_closePreview()");
		    selenium.waitForPageToLoad("30000");
		} else {
			throw new IllegalStateException("Close preview - link not available");
		}
	}
}
