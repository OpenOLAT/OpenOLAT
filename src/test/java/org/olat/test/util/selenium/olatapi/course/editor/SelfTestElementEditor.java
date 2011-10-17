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
package org.olat.test.util.selenium.olatapi.course.editor;


import com.thoughtworks.selenium.Selenium;

/**
 * Self-Test element configuration page in course editor.
 * @author Lavinia Dumitrescu
 *
 */
public class SelfTestElementEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public SelfTestElementEditor(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()")) {
			throw new IllegalStateException("This is not the - Self-test configuration - page");
		}
	}

	/**
	 * Choose the test file for this TestElement.
	 * 
	 * @param testTitle
	 */
	public void chooseMyFile(String testTitle) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()")) {
			selenium.click("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbSelfTest_chooseFile()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		//if too many entries found - show all
	  	if(selenium.isElementPresent("ui=commons::table_showAll()")) {
		  selenium.click("ui=commons::table_showAll()");
		  selenium.waitForPageToLoad("30000");
		}  
		selenium.click("ui=courseEditor::commons_chooseLr_chooseTest(nameOfTest=" + testTitle + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Configure Layout. 
	 * There is only a partial coverage of the possible configuration.
	 * 
	 * @param allowMenuNavigation
	 * @param showMenuNavigation
	 * @param allowCancel
	 * @param allowSuspend
	 */
	public void configureSelfTestLayout(Boolean allowMenuNavigation, Boolean showMenuNavigation, Boolean allowCancel, Boolean allowSuspend) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()")) {
			selenium.click("ui=courseEditor::content_bbSelfTest_tabSelfTestConfiguration()");
			selenium.waitForPageToLoad("30000");
		}		
		if(showMenuNavigation!=null && showMenuNavigation && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_showMenuNavigation()")) {
			selenium.click("ui=courseEditor::content_bbTest_layoutParameters_showMenuNavigation()");
		} else if(showMenuNavigation!=null && !showMenuNavigation && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_showMenuNavigation()")) {
			selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_showMenuNavigation()");
		}
		
		if(allowMenuNavigation!=null && allowMenuNavigation && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowMenuNavigation()")) {
		  //check 
		  selenium.click("ui=courseEditor::content_bbTest_layoutParameters_allowMenuNavigation()");
		} else if(allowMenuNavigation!=null && !allowMenuNavigation && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowMenuNavigation()")) {
		  //uncheck
		  selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_allowMenuNavigation()");
		}
		//self-tests always have a cancel button, so no need to configure this
		
		if(allowSuspend!=null && allowSuspend && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()")) {
			selenium.click("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()");
		} else if(allowSuspend!=null && !allowSuspend && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()")){
			selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()");
		}	
		selenium.click("ui=commons::flexiForm_saveButton()");
	}
}
