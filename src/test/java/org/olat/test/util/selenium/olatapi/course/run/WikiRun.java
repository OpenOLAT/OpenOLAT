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
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Wiki course Run element.
 * @author Lavinia Dumitrescu
 *
 */
public class WikiRun extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public WikiRun(Selenium selenium) {
		super(selenium);
    //	Check that we're on the right place
		//if(!selenium.isTextPresent("Article")) {
		if(!selenium.isElementPresent("ui=wiki::sideNavigation_index()")) {
			throw new IllegalStateException("This is not the - Wiki - page");
		}
	}

	/**
	 * Edit page, save, and go to Article link.
	 * @param text
	 */
	public void editPage(String text) {
		openEditor();
		selenium.click("ui=wiki::edit_editFormTextarea()");
		selenium.type("ui=wiki::edit_editFormTextarea()", text);
		selenium.click("ui=commons::flexiForm_saveButton()"); //this should clean the cache
		//selenium.waitForPageToLoad("30000");
		selenium.click("ui=wiki::topNavigation_article()"); //release lock 
		//selenium.waitForPageToLoad("30000");
	}
	
	public void openEditor() {
		if(selenium.isElementPresent("ui=wiki::topNavigation_editPage()")) {
			selenium.click("ui=wiki::topNavigation_editPage()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * 
	 * @param filepath
	 * @param mediaLabel
	 * @throws Exception
	 */
	public void insertMedia(String filepath, String mediaLabel) throws Exception {
		openEditor();
		if(selenium.isElementPresent("ui=wiki::edit_uploadFileButton()")) {
			selenium.click("ui=wiki::edit_uploadFileButton()");
			Thread.sleep(1000);
			selenium.type("ui=upload::fileChooser()", filepath);
			selenium.click("ui=upload::submit()");
			//selenium.waitForPageToLoad("30000");
			try {
				Thread.sleep(3000);
			} catch (Exception e) {			
			}
			selenium.select("mediaFileChooser", "label=" + mediaLabel);
			selenium.click("ui=wiki::edit_insertFileButton()");
			selenium.click("ui=commons::save()");
			//selenium.waitForPageToLoad("30000");
			try {
				Thread.sleep(3000);
			} catch (Exception e) {			
			}
			selenium.click("ui=wiki::topNavigation_article()");
			selenium.waitForPageToLoad("30000");
		} else {
			throw new IllegalStateException("Upload file - link not available");
		}
	}
	
	/**
	 * Creates a wiki page via the Create article, or searches a wiki page, if already selected.
	 * @param wikiPage
	 * @param articleText, could be null if search is wanted.
	 */
	public void createOrSearchArticle(String wikiPage, String articleText) {
		//searches wikiPage and creates it if not already created
		selenium.type("ui=wiki::sideNavigation_createInput()", wikiPage);
		selenium.click("ui=wiki::sideNavigation_createButton()");
		selenium.waitForPageToLoad("30000");
		//new page created, edit it
		if(selenium.isElementPresent("ui=wiki::sideNavigation_clickAWikiPage(nameOfWikiPage=" + wikiPage + ")")) {
		  selenium.click("ui=wiki::sideNavigation_clickAWikiPage(nameOfWikiPage=" + wikiPage + ")");
		  //selenium.waitForPageToLoad("30000");
		  try {
				Thread.sleep(3000);
			} catch (Exception e) {			
			}
		  selenium.type("ui=wiki::edit_editFormTextarea()", articleText);
		  selenium.click("ui=commons::save()");
		  //selenium.waitForPageToLoad("30000");
		  try {
				Thread.sleep(3000);
			} catch (Exception e) {			
			}
		} else {
			System.out.println("The wiki page is supposed to already exist!");
		}
	}
	
	/**
	 * Searches article with this title.
	 * @param wikiPage
	 */
	public void searchArticle(String wikiPage) {
		selenium.type("ui=wiki::sideNavigation_searchInput()", wikiPage);
		selenium.click("ui=wiki::sideNavigation_searchButton()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Selects Article link.
	 *
	 */
	public void showArticle() {
		selenium.click("ui=wiki::topNavigation_article()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Selects index.
	 *
	 */
	public void selectIndex() {
		selenium.click("ui=wiki::sideNavigation_index()");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * Click the From a-z link.
	 *
	 */
	public void selectFromAToZ() {
		selenium.click("ui=wiki::sideNavigation_from-a-z()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Edit menu and go to the Article link.
	 * @param wikiString
	 */
	public void editMenu(String wikiString) {
		selenium.click("ui=wiki::sideNavigation_editWikiMenu()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=wiki::edit_editFormTextarea()", wikiString);
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=wiki::topNavigation_article()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Show versions and click on version compare link.
	 *
	 */
	public void showVersionsAndCompare() {
		selenium.click("ui=wiki::topNavigation_versions()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=wiki::versions_compare()");
		selenium.waitForPageToLoad("30000");
	}
	
}
