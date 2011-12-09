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
package org.olat.test.util.selenium.olatapi.qti;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * @author Lavinia Dumitrescu
 *
 */
public class SectionEditor extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public SectionEditor(Selenium selenium) {
		super(selenium);		
		
    //	Check that we're on the right place
		if(!selenium.isElementPresent("ui=testEditor::content_section_orderOfQuestions()")) {			
			throw new IllegalStateException("This is not the - Test Section - page");
		}
	}

	/**
	 * Selects section with the currentTitle and change title to newTitle.
	 * @param currentTitle
	 * @param newTitle
	 */
	public void setSectionTitle(String currentTitle, String newTitle) {		
		selenium.click("ui=testEditor::content_section_title()");
		selenium.type("ui=testEditor::content_section_title()", newTitle);
		selenium.click("ui=testEditor::content_section_save()");
		selenium.waitForPageToLoad("30000");
	}
	
}
