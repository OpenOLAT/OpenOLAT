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
package org.olat.test.util.selenium.olatapi.admin;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

public class Administration extends OLATSeleniumWrapper {

	public Administration(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}
	
	public void editInfoMessage(String messageText) {
		if(selenium.isElementPresent("ui=systemInformation::infoMsgTab()")) {
    	  selenium.click("ui=systemInformation::infoMsgTab()");
    	  selenium.waitForPageToLoad("30000");
		}
    	selenium.click("ui=systemInformation::infoMsgEditButton()");
    	selenium.waitForPageToLoad("30000");
    	
    	selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Message)", messageText);
    	selenium.click("ui=commons::flexiForm_saveButton()");	
    	selenium.waitForPageToLoad("30000");
	}
	
	public boolean hasInfoMessage(String message) {
		if(selenium.isElementPresent("ui=systemInformation::infoMsgTab()")) {
	    	  selenium.click("ui=systemInformation::infoMsgTab()");
	    	  selenium.waitForPageToLoad("30000");
		}
		return selenium.isTextPresent(message);
	}
	
}
