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
package org.olat.test.util.selenium.olatapi.folder;

import java.util.Iterator;
import java.util.List;

import org.olat.test.util.selenium.SeleniumHelper;
import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;

import com.mchange.util.AssertException;
import com.thoughtworks.selenium.Selenium;

/**
 * Generic Folder abstraction for the FolderRunController.
 * Supposed to be used from: Personal folder, Storage folder in course editor, Folder node in course run, etc.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class Folder extends OLATSeleniumWrapper {

	public Folder(Selenium selenium) {
		super(selenium);
	  //Check that we're on the right place
		if(!selenium.isElementPresent("ui=briefCase::folderComponent()")) {
			throw new IllegalStateException("This is not the - Folder - page");
		}
	}

	/**
	 * Select item.
	 * @param name
	 */
	public void selectFileOrFolder(String name) {
		if(name!=null && selenium.isElementPresent("ui=briefCase::clickBriefcaseEntry(linkText=" + name + ")")) {
			selenium.click("ui=briefCase::clickBriefcaseEntry(linkText=" + name + ")");
			selenium.waitForPageToLoad("30000");			
		}		
	}
	
	public void selectLink(String link, boolean waitForPageToLoad) {
		selenium.click("ui=briefCase::clickBriefcaseEntry(linkText=" + link + ")");
		if(waitForPageToLoad) {
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Click on checkbox of an item.
	 * @param name
	 */
	public void checkFileOrFolder(String name) {
		SeleniumHelper.safeWait(100);
		if(name!=null && selenium.isElementPresent("ui=briefCase::selectBriefcaseCheckBox(linkText=" + name + ")")) {
			selenium.click("ui=briefCase::selectBriefcaseCheckBox(linkText=" + name + ")");
		}		
	}

	/**
	 * Closes storage folder.
	 * @return
	 */
	public CourseEditor closeStorageFolder() {
		selenium.click("ui=overlay::overlayClose()");
		selenium.waitForPageToLoad("30000");
		return new CourseEditor(selenium);
	}
	
	
	/**
	 * Creates a file with a given name in the current container. 
	 * It checks if the filename is valid and returns true if valid, false otherwise.
	 * Closes the overlay.
	 * @param fileName
	 */
	public boolean createFile(String folder, String fileName, String text) {
		selectFileOrFolder(folder);
		
		selenium.click("ui=briefCase::createFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=briefCase::createFileName()", fileName);
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Create document)");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=commons::flexiForm_formErrorMsg()")) {			
			selenium.click("ui=commons::flexiForm_cancelButton()");
			selenium.waitForPageToLoad("30000");
			return false;
		}
		//selenium.type(locator, text)
		//TODO: LD: add text
		if(fileName.endsWith(".txt")) {
			selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Content of document)", text);
	  	selenium.click("ui=commons::flexiForm_saveButton()");	
	  	selenium.waitForPageToLoad("30000");
		} else if (fileName.endsWith(".html")) {
		  selenium.click("ui=briefCase::htmlEditorSaveAndClose()");
		  selenium.waitForPageToLoad("30000"); 
		}
		return true;
	}
	
	/**
	 * Creates a folder if does not encounter a error message: either because the folder already exists,
	 * or because the folder name is invalid.
	 * @param folderName
	 * @return Returns true if folder successful created, false otherwise.
	 */
	public boolean createFolder(String folderName) {
		selenium.click("ui=briefCase::createFolder()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Name)", folderName);	
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Create folder)");
		selenium.waitForPageToLoad("30000");
		if(selenium.isElementPresent("ui=commons::flexiForm_formErrorMsg()")) {			
			selenium.click("ui=commons::flexiForm_cancelButton()");
			return false;
		}
		return true;
	}
		
	/**
	 * Uploads a file in the specified folder, or in the current folder if none specified.
	 * @param folder could be null.
	 * @param fileName
	 */
	public void uploadFile(String folder,String fileName) {		
		selectFileOrFolder(folder);
	
		boolean uploadIsThere = false;
		int retries = 5;
		while(!uploadIsThere && retries > 1){	
			selenium.click("ui=briefCase::uploadFile()");
			selenium.waitForPageToLoad("90000");
			uploadIsThere = selenium.isElementPresent("ui=upload::fileChooser()");
			if(!uploadIsThere){
				retries--;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		selenium.type("ui=upload::fileChooser()", fileName);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("30000");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {			
		}
	}
	
	/**
	 * Upload, overwrite or rename.
	 * @param folder
	 * @param fileName
	 * @param overwrite
	 */
	public boolean uploadWithOverwrite(String folder,String fileName, boolean overwrite) {
		selectFileOrFolder(folder);
		
		boolean overwritten = false;
		
		selenium.click("ui=briefCase::uploadFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=upload::fileChooser()", fileName);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("30000");
		if(overwrite && selenium.isTextPresent("Overwrite")) {		
			selenium.click("ui=commons::anyLink(linkText=Overwrite)");	
			overwritten = true;
		} else if(!overwrite && selenium.isTextPresent("Overwrite")) {
			//TODO: LD: implement this!
		}
		selenium.waitForPageToLoad("30000");
		return overwritten;
	}
	
	/**
	 * Delete the item with the input title.
	 * @param title
	 */
	public void deleteItem(String title) {
		if(selenium.isElementPresent("ui=briefCase::selectBriefcaseCheckBox(linkText=" + title + ")")) {
		  selenium.click("ui=briefCase::selectBriefcaseCheckBox(linkText=" + title + ")");
		  selenium.click("ui=briefCase::buttonDelete()");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=groups::content_deleteYes()");
		  selenium.waitForPageToLoad("30000");
		  System.out.println("Item deleted: " + title);
		} else {
		  System.out.println("No item found for deletion: " + title);
		}
	}
	
	public void deleteSelection() {
		if(selenium.isElementPresent("ui=briefCase::buttonDelete()")) {
			selenium.click("ui=briefCase::buttonDelete()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=groups::content_deleteYes()");
			selenium.waitForPageToLoad("30000");
			System.out.println("Selection deleted");
		} else {
		  System.out.println("No item deleted.");
		}
	}
	
	public void moveSelection(List selection) {
		
	}
	
  public void copySelection(List selection) {
		
	}
	
  /**
   * Return true if successfully zipped.
   * @param selection
   * @param zipName
   * @return
   */
  public boolean zipSelection(List<String> selection, String zipName) {
	  Iterator<String> selIterator = selection.iterator();
	  while(selIterator.hasNext()) {
		  String item = selIterator.next();
		  checkFileOrFolder(item);
	  }
	  //buttonZip
	  selenium.click("ui=briefCase::buttonZip()");
	  selenium.waitForPageToLoad("30000");
	  //enter zip name
	  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Name of new Zip file)", zipName);

	  selenium.click("ui=briefCase::zipFiles()");
	  selenium.waitForPageToLoad("30000");
	  if(selenium.isElementPresent("ui=briefCase::zipFiles()")) {
		  //todo: cancel
		  return false;
	  }
	  return true;
  }
  
  /**
   * Cancels zip workflow if any started.
   */
  public void cancelZipSelection() {
    if(selenium.isElementPresent("ui=briefCase::zipFiles()")) {
      selenium.click("ui=commons::flexiForm_cancelButton()");
      selenium.waitForPageToLoad("30000");
	}
  }
  
  public void unzipSelection(List selection) {
		
	}
  
  /**
   * 
   * @param fileFolderName
   * @param newName
   * @param title
   * @param description
   */
  public void editMetadata(String fileFolderName, String newName, String title, String description) {
  	selenium.click("ui=briefCase::editMetadata(linkText=" + fileFolderName + ")");
  	selenium.waitForPageToLoad("30000");
  	selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=File name)", newName);
  	selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);
  	//TODO: add description
  	selenium.click("ui=commons::flexiForm_saveButton()");	
  	selenium.waitForPageToLoad("30000");
  }
  
  public void editTxtFile(String fileName, String content) {
  	selenium.click("ui=briefCase::editFile(linkText=" + fileName + ")");
  	selenium.waitForPageToLoad("30000");
  	selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Content of document)", content);
  	selenium.click("ui=commons::flexiForm_saveButton()");	
  	selenium.waitForPageToLoad("30000");
  }
  
  public String getText(String fileName) {
  	selenium.click("ui=briefCase::editFile(linkText=" + fileName + ")");
  	selenium.waitForPageToLoad("30000");
  	String content = selenium.getValue("ui=commons::flexiForm_labeledTextArea(formElementLabel=Content of document)");
  	selenium.click("ui=commons::flexiForm_saveButton()");	
  	selenium.waitForPageToLoad("30000");
  	return content;
  }
  
  public void editHtmlFile(String fileName, String content) {
  	
  }
  
  public void selectAll() {
  	if(selenium.isElementPresent("ui=briefCase::selectAll()")) {
  	  selenium.click("ui=briefCase::selectAll()");
  	}
  }
  
  public void deselectAll() {
  	
  }
  
  public boolean isFileOrFolderPresent(String fileName) {
  	return selenium.isElementPresent("ui=briefCase::clickBriefcaseEntry(linkText=" + fileName + ")");
  }
	
  
}
