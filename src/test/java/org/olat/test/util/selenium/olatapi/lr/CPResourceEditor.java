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
package org.olat.test.util.selenium.olatapi.lr;


import com.thoughtworks.selenium.Selenium;

public class CPResourceEditor extends ResourceEditor {

	public CPResourceEditor(Selenium selenium, String title) {
		super(selenium, title);
		 //Check that we're on the right place		
		if(!selenium.isElementPresent("ui=cpEditor::addPage()")) {
			throw new IllegalStateException("This is not the - CPResourceEditor - page");
		}
	}

	/**
	 * Changes title of the page assuming that the page is visible to be selected before.
	 * @param currentTitle
	 * @param newTitle
	 */
	public void changeTitle(String currentTitle, String newTitle) {
		selectPage(currentTitle);		
		changeCurrentTitle(newTitle);
	}
	
	/**
	 * Uses only saveAndClose.
	 * 
	 * @param newTitle
	 */
	private void changeCurrentTitle(String newTitle) {
		selenium.click("ui=cpEditor::editPageProperties()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=cpEditor::metadataTitleInput()", newTitle);
		selenium.click("ui=cpEditor::saveAndClose()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Selects the page with this title, if visible.
	 * As with selecting the node the subtree opens.
	 * @param title
	 * @return Returns true if page gets selected, else false.
	 */
	private boolean selectPage(String title) {
		//if link exists but it is not selected
		if(selenium.isElementPresent("ui=cpEditor::menuTreeLink(link=" + title + ")")
				&& !selenium.isElementPresent("ui=cpEditor::selectedTreeNodeExpanded(link=" + title + ")")
				&& !selenium.isElementPresent("ui=cpEditor::selectedTreeNodeLeaf(link=" + title + ")")) {			
		  selenium.click("ui=cpEditor::menuTreeLink(link=" + title + ")");
		  selenium.waitForPageToLoad("30000");
		  return true;
	  }
		//no such page found
		return false; 
	}
	
	/**
	 * Add page, after selecting the parent page, assuming that this is visible.
	 * Renames page.
	 * 
	 * @param parentTitle
	 * @param title
	 * @param content
	 */
	public void addPageAndRename(String parentTitle, String title, String content) {
		selectPage(parentTitle);
		//add page
		selenium.click("ui=cpEditor::addPage()");
		selenium.waitForPageToLoad("30000");
		//changeTitle
		changeCurrentTitle(title);
		//add some content
		selenium.type("ui=commons::tinyMce_styledTextArea()", content);
		selenium.click("ui=commons::saveLink()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Sets content to the page named title.
	 * 
	 * @param title
	 * @param content
	 */
	public void editPage(String title, String content) {
		selectPage(title);
	  //replace content
		selenium.type("ui=commons::tinyMce_styledTextArea()", content);
		selenium.click("ui=commons::saveLink()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Copies and renames the copy page.
	 * 
	 * @param sourceTitle
	 * @param targetTitle
	 */
	public void copyPageAndRename(String sourceTitle, String targetTitle) {
		copyPage(sourceTitle);
		//changeTitle
		changeCurrentTitle(targetTitle);
	}
	
	/**
	 * Select source page and press copy, creates a copy with the default title.
	 * 
	 * @param sourceTitle
	 * @param targetTitle
	 */
	public void copyPage(String sourceTitle) {
		selectPage(sourceTitle);
		//copy page
		selenium.click("ui=cpEditor::copyPage()");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Select parent and import file.
	 * @param parentTitle
	 * @param archiveFileName
	 */
	public void importPage(String parentTitle, String fileName) {
		selectPage(parentTitle);
		//import page
		selenium.click("ui=cpEditor::importPage()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=upload::fileChooser()", fileName);
		selenium.waitForPageToLoad("30000");		
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Import)");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Select page and delete.
	 * @param currentTitle
	 * @param deleteMenuElementOnly
	 */
	public void deletePage(String currentTitle, boolean deleteMenuElementOnly) {
		selectPage(currentTitle);
		//delete page
		selenium.click("ui=cpEditor::deletePage()");
		selenium.waitForPageToLoad("30000");
		//choose delete type
		if(deleteMenuElementOnly) {
			selenium.click("ui=commons::anyLink(linkText=Delete menu element only)");			
		} else {
			selenium.click("ui=commons::anyLink(linkText=Delete menu element and files)");			
		}
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Drags and drops from the sourceTitle to the targetTitle.
	 * 
	 * @param sourceTitle
	 * @param targetTitle
	 */
	public void movePage(String sourceTitle, String targetTitle) {
		//TODO:LD: fix this!!!
		
		//selenium.dragAndDropToObject("ui=cpEditor::dragAndDropSrcObject(link=" + sourceTitle + ")", "ui=cpEditor::dragAndDropSrcObject(link=" + targetTitle + ")");
		selenium.mouseDownAt("ui=cpEditor::dragAndDropSrcObject(link=" + sourceTitle + ")","10,10");
		selenium.mouseMoveAt("ui=cpEditor::dragAndDropSrcObject(link=" + targetTitle + ")","10,10");
		selenium.mouseOver("ui=cpEditor::dragAndDropSrcObject(link=" + targetTitle + ")");
		selenium.mouseUpAt("ui=cpEditor::dragAndDropSrcObject(link=" + targetTitle + ")","10,10");

		//selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * TODO LD: check this out!!! Open, close preview, is this OK?
	 * @param title
	 * @return
	 */
	public void preview(String title) {		
		selectPage(title);
		//press preview 
		selenium.click("ui=cpEditor::preview()");
		selenium.waitForPageToLoad("30000");
		//close preview
		selenium.click("ui=commons::anyLink(linkText=Close preview)");	
		selenium.waitForPageToLoad("30000");
	}
	
	
	/**
	 * Inserts image, steps: 
	 * click symbol "insert/edit image" <br/> 
	 * add Image description <br/> 
	 * click "Browse" <br/> 
	 * click "Upload file" <br/> 
	 * click Select file and select filename <br/> 
	 * click "Upload" <br/>
	 * click "Insert" <br/>
	 * click "save" <br/>
	 * 
	 * @param filename
	 */
	public void insertImage(String filename, String description) {		
		selenium.click("ui=commons::tinyMce_insertEditImage()");
		this.sleepThread(3000);		
		selenium.selectFrame("//iframe[contains(@src,'/image.htm')]");
		
		//add description
		selenium.type("ui=cpEditor::imageDescription()", description);
		
		selenium.click("ui=commons::tinyMce_browse()");
				
		//upload file
		this.sleepThread(10000);		
		selenium.selectWindow(selenium.getAllWindowTitles()[2]); 
		selenium.click("ui=cpEditor::uploadFile()");
		this.sleepThread(10000);
		//select file
		selenium.type("ui=upload::fileChooser()",filename);
		selenium.click("ui=upload::submit()");
		selenium.selectWindow(selenium.getAllWindowTitles()[1]);
							
		//insert image
		selenium.click("ui=cpEditor::insertImage()");
		
		//save
		selenium.click("ui=commons::saveLink()");
		selenium.waitForPageToLoad("30000");
	}
}
