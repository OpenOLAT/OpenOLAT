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
