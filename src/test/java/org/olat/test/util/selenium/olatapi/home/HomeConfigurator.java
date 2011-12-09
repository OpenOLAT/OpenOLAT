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
package org.olat.test.util.selenium.olatapi.home;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

public class HomeConfigurator extends OLATSeleniumWrapper {
	
	public enum SORT_TYPE {TYPE, ALPHABET, DATE}
	

	public HomeConfigurator(Selenium selenium) {
		super(selenium);
		// TODO: LD: Check that we're on the right place
	}

	/**
	 * Configures only the numOfEntries. 
	 * TODO: LD: add configuration after sortType and ascending.
	 * @param numOfEntries
	 * @param sortType
	 * @param ascending
	 * @return
	 */
	public Home configMyGroupPortlet(int numOfEntries, SORT_TYPE sortType, boolean ascending) {
		//start auto config wizard
		selenium.click("ui=home::config_autoConfigMyGroups()");
		selenium.waitForPageToLoad("30000");
		//do configure
		configureCurrentSelectedPortlet(numOfEntries, sortType, ascending);
		
		return new Home(selenium);
	}
	
	public Home configMyBookmarkPortlet(int numOfEntries, SORT_TYPE sortType, boolean ascending) {
		selenium.click("ui=home::config_autoConfigMyBookmarks()");
		selenium.waitForPageToLoad("30000");
		//do configure
		configureCurrentSelectedPortlet(numOfEntries, sortType, ascending);
		
		return new Home(selenium);
	}
	
	/**
	 * Change num of entries if different of the current one.
	 * @param numOfEntries
	 * @param sortType
	 * @param ascending
	 */
	private void configureCurrentSelectedPortlet(int numOfEntries, SORT_TYPE sortType, boolean ascending) {		
	  String currentNumOfEntries = selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=Number of entries in portlet)");
	  if(!currentNumOfEntries.equals(String.valueOf(numOfEntries))) {
	    selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Number of entries in portlet)", String.valueOf(numOfEntries));
		selenium.click("ui=commons::save()");				
		selenium.waitForPageToLoad("30000");
	  } else {
	    selenium.click("ui=commons::cancelButton()");				
		selenium.waitForPageToLoad("30000");
	  }
	  //close editor
	  selenium.click("ui=home::config_endConfig()");
	  selenium.waitForPageToLoad("30000");
	}
	
}
