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
 * API wrapper for ePortfolio Extensions
 * also providing some helper methods.
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPExtensions extends OLATSeleniumWrapper {

	public EPExtensions(Selenium selenium) {
		super(selenium);
	}

	public int getArtefactAmount(){
		// check the string containing amount of artefacts, if not present, or no artefact, return 0
		int amount = 0;
		if (selenium.isElementPresent("ui=epArtefacts::artefactCount()")) {
			String artCount = selenium.getText("ui=epArtefacts::artefactCount()");
			try {
				amount = Integer.parseInt(artCount.split(":")[1].trim());
			} catch (Exception e) {
				return amount;
			}
		}
		return amount;
	}
	
	public int getTotalArtefactAmount(){
		selenium.click("ui=epMenu::ePortfolioArtefacts()");
		selenium.waitForPageToLoad("30000");
		return getArtefactAmount();
	}

	
	public EPCollectWizard createTextArtefactAndOpenWizard(){
		selenium.click("ui=epMenu::ePortfolioArtefacts()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=epArtefacts::addArtefactLink()");
		sleepThread(3000);
		selenium.click("ui=epArtefacts::createTextArtefactLink()");
		selenium.waitForPageToLoad("30000");
		return new EPCollectWizard(selenium);		
	}
	
	public EPArtefactSearch getArtefactSearch(){
		selenium.click("ui=epArtefacts::artefactSearch()");
		selenium.waitForPageToLoad("30000");
		return new EPArtefactSearch(selenium);
	}
	
	public EPMapEditor getMapEditor(){
		selenium.click("ui=epMenu::ePortfolioMyMaps()");
		selenium.waitForPageToLoad("30000");
		return new EPMapEditor(selenium);
	}
	
	public void deleteFirstArtefact(){
		if (selenium.isElementPresent("ui=epArtefactSearch::deleteFirstArtefactLink()")){
			selenium.click("ui=epArtefactSearch::deleteFirstArtefactLink()");
			selenium.waitForPageToLoad("30000");
			sleepThread(6000);
			selenium.click("ui=dialog::Yes()");
			sleepThread(1000);
		}		
	}
	
	public boolean hasArtefact() {
	  return selenium.isElementPresent("ui=epArtefactSearch::deleteFirstArtefactLink()");
	}
	
}
