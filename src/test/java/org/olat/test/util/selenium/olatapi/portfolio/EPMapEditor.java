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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.test.util.selenium.olatapi.portfolio;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 *
 */
public class EPMapEditor extends OLATSeleniumWrapper {

	public EPMapEditor(Selenium selenium) {
		super(selenium);
	}

	public void createDefaultMap(String title, String description){
		selenium.click("ui=epMap::addMapLink()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=epMap::createDefaultMap()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=epMap::createMapTitle()", title);
		selenium.type("ui=epMap::createMapDescription()", description);
		selenium.click("ui=epMap::createMapSaveButton()");		
	}
	
	public void openMapByTitle(String title){
		selenium.click("ui=epMap::openMap(titleOfMap=" + title + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	public void openFirstPage(){
		if (selenium.isElementPresent("ui=epMap::firstPageLink()")){
			selenium.click("ui=epMap::firstPageLink()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public void addPages(int amount){
		for (int i = 0; i < amount; i++) {
			selenium.click("ui=epMap::addPageLink()");
			selenium.waitForPageToLoad("30000");
			toggleEditMode(false);
		}
	}
	
	public void addStructures(int amount){
		for (int i = 0; i < amount; i++) {
			selenium.click("ui=epMap::addStructLink()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public void toggleEditMode(boolean onOff){
		if (onOff){
			if (selenium.isElementPresent("ui=epMap::openEditor()"))
				selenium.click("ui=epMap::openEditor()");
		} else {
			if (selenium.isElementPresent("ui=epMap::closeEditor()"))
				selenium.click("ui=epMap::closeEditor()");
		}
		selenium.waitForPageToLoad("30000");
	}
	
	public void openShareDialogForMapByTitle(String title){
		selenium.click("ui=epMap::shareMap(titleOfMap=" + title + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	public void shareToOtherUserWithName(String username){
		// add a rule
		selenium.click("ui=epShare::createRule()");
		selenium.waitForPageToLoad("30000");
		// choose user
		selenium.click("ui=epShare::nameInput()");
		selenium.typeKeys("ui=epShare::nameInput()", username);
		sleepThread(3000);
		if (selenium.isElementPresent("ui=epShare::nameInputSelect()")) {
			selenium.click("ui=epShare::nameInputSelect()");
		}
		// persist
		selenium.click("ui=epShare::saveShare()");		
	}
	
	public void changeElementTitleDescription(String title, String description){
		selenium.type("ui=epMapEditor::elTitle()", title);
		selenium.type("ui=epMapEditor::elDescription()", description);
		selenium.click("ui=epMapEditor::saveEditor()");
		selenium.waitForPageToLoad("30000");
	}
	
	public boolean deleteActiveTOCElement(){		
		if (selenium.isElementPresent("ui=epMapEditor::deleteButton()")) {
			selenium.click("ui=epMapEditor::deleteButton()");
			selenium.waitForPageToLoad("30000");
			return true;
		}
		return false;
	}
	
}
