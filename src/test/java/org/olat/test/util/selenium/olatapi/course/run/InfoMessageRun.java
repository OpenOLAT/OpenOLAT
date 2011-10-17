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


package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  4. jan 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessageRun extends OLATSeleniumWrapper {
	  
	public InfoMessageRun(Selenium selenium) {
		super(selenium);
	}
	
	/*
	 * Create a message
	 */
	public void createMessage(String title, String message) {
		selenium.click("ui=infoMessage::createMessage()");
		selenium.waitForPageToLoad("30000");
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Subject)", title);	  			
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Message)", message);
		
		selenium.click("ui=infoMessage::finishMessage()");
		selenium.waitForPageToLoad("30000");
		
		try{//the order by is made up to the second, if the selenium test is too quick
			//the order is not predictable.
			Thread.sleep(1100);
		} catch(InterruptedException ie) {
			ie.printStackTrace(System.out);
		}
	}
	
	public void showOlderMessage() {
		selenium.click("ui=infoMessage::olderMessage()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void showCurrentMessage() {
		selenium.click("ui=infoMessage::currentMessage()");
		selenium.waitForPageToLoad("30000");
	}
	
	public boolean hasMessage(String title) {
		if(selenium.isElementPresent("ui=infoMessage::messageTitle(titleOfMessage=" + title + ")")) {
			return true;
		}
		return false;
	}
	
	public boolean canCreateMessage() {
		if(selenium.isElementPresent("ui=infoMessage::createMessage()")) {
			return true;
		}
		return false;
	}
	
	public boolean canEditMessage() {
		if(selenium.isElementPresent("ui=infoMessage::editFirstMessage()")) {
			return true;
		}
		return false;
	}
	
	public boolean canDeleteMessage() {
		if(selenium.isElementPresent("ui=infoMessage::deleteFirstMessage()")) {
			return true;
		}
		return false;
	}
	
	public void editFirstMessage() {
		selenium.click("ui=infoMessage::editFirstMessage()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void deleteFirstMessage() {
		selenium.click("ui=infoMessage::deleteFirstMessage()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void dialogOk() {
		selenium.click("ui=dialog::OK()");
	}
	
	public void save() {
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void yes() {
		selenium.click("ui=dialog::Yes()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void cancel() {
		selenium.click("ui=dialog::Cancel()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void close() {
		selenium.click("ui=overlay::overlayClose()");
		selenium.waitForPageToLoad("30000");
	}
	
	public boolean isMessageEdited() {
		if(selenium.isElementPresent("ui=infoMessage::messageInEdition()")) {
			return true;
		}
		return false;
	}
	
	public boolean isMessageAlreadyEdited() {
		if(selenium.isElementPresent("ui=infoMessage::messageAlreadyEdited()")) {
			return true;
		}
		return false;
	}
}
