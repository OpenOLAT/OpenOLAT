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
package org.olat.test.util.selenium.olatapi;

import junit.framework.Assert;

import com.thoughtworks.selenium.Selenium;

/**
 * Generic superclass for all OLAT abstraction layer classes.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OLATSeleniumWrapper extends Assert {
  
	protected Selenium selenium;
	
	/**
	 * It is recommended to check in the subclass's constructor that we're on the right page.
	 * @param selenium
	 */
	public OLATSeleniumWrapper(Selenium selenium) {		
		this.selenium = selenium;
	}

  /**
   * Provide access to the selenium instance, for the case a finer grained stepping is needed.
   * @return
   */
	public Selenium getSelenium() {
		return selenium;
	}
	
	/**
	 * Helper method. 
	 * Use this instead of the selenium object.
	 * @param text
	 * @return
	 */
	public boolean isTextPresent(String text) {
		return selenium.isTextPresent(text);
	}
	
	/**
	 * Helper method
	 * Use this instead of the selenium method
	 * 
	 * Check for the Element with given locator
	 * @param locator The locator for the element to check for
	 * @return true if the element with given locator exists, false otherwise
	 */
	public boolean isElementPresent(String locator){
		return selenium.isElementPresent(locator);
	}
	
	/**
	 * Helper method.
	 * Only selects the iframe and types the text, back to the top frame. 
	 * No save, finish is called.
	 * @param text
	 */
	public void typeInRichText(String text) {
	  // the description shows up in an iframe
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.type("ui=commons::tinyMce_styledTextArea()", text);
		//selenium.selectFrame("relative=top");	
	}
	
	public void sleepThread(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
