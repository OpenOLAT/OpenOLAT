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
 * Test element configuration page in course editor.
 * @author Lavinia Dumitrescu
 *
 */
public class TestElementEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public TestElementEditor(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=courseEditor::content_bbTest_tabTestConfiguration()")) {
			throw new IllegalStateException("This is not the - Test configuration - page");
		}
	}

	/**
	 * Choose the test file for this TestElement.
	 * 
	 * @param testTitle
	 */
	public void chooseMyFile(String testTitle) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbTest_tabTestConfiguration()")) {
			selenium.click("ui=courseEditor::content_bbTest_tabTestConfiguration()");
			selenium.waitForPageToLoad("30000");
		}	
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// nothing to do		
		}
		selenium.click("ui=courseEditor::content_bbTest_chooseFile()");
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
	 * @param allowMenuNavigation should be null if default, true or false otherwise
	 * @param showMenuNavigation should be null if default, true or false otherwise
	 * @param allowCancel should be null if default, true or false otherwise
	 * @param allowSuspend should be null if default, true or false otherwise
	 * @param maxNumApproaches greater the 0 if set to some value 
	 * @param displayResultsOnTestHomepage should be null if default, true or false otherwise
	 */
	public void configureTestLayout(Boolean allowMenuNavigation, Boolean showMenuNavigation, Boolean allowCancel, Boolean allowSuspend,
			int maxNumApproaches, Boolean displayResultsOnTestHomepage) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbTest_tabTestConfiguration()")) {
			selenium.click("ui=courseEditor::content_bbTest_tabTestConfiguration()");
			selenium.waitForPageToLoad("30000");
		}
		
		if(maxNumApproaches>0) {		
			if(!selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_limitNumOfAttempts()")) {
			  selenium.click("ui=courseEditor::content_bbTest_layoutParameters_limitNumOfAttempts()");
			}
			try {
				Thread.sleep(5000); //sleep
			} catch (InterruptedException e) {				
			}
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Maximum number of attempts)", String.valueOf(maxNumApproaches));
		}
		
		//boolean isShowMenuNavigationPresent = selenium.isElementPresent("ui=courseEditor::content_bbTest_layoutParameters_showMenuNavigation()");
		//System.out.println("isShowMenuNavigationPresent: " + isShowMenuNavigationPresent);
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
		
		if(allowCancel!=null && allowCancel && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowCancel()")) {			
			selenium.click("ui=courseEditor::content_bbTest_layoutParameters_allowCancel()");
		} else if(allowCancel!=null && !allowCancel && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowCancel()")){
			selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_allowCancel()");
		}
		
		if(allowSuspend!=null && allowSuspend && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()")) {
			selenium.click("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()");
		} else if(allowSuspend!=null && !allowSuspend && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()")){
			selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_allowSuspend()");
		}		
		
		if(displayResultsOnTestHomepage!=null && displayResultsOnTestHomepage && !selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_displayResultsOnTestHomepage()")) {
			selenium.click("ui=courseEditor::content_bbTest_layoutParameters_displayResultsOnTestHomepage()");
		} else if(displayResultsOnTestHomepage!=null && !displayResultsOnTestHomepage && selenium.isChecked("ui=courseEditor::content_bbTest_layoutParameters_displayResultsOnTestHomepage()")) {			
			selenium.uncheck("ui=courseEditor::content_bbTest_layoutParameters_displayResultsOnTestHomepage()");
		}
		//TODO: LD: there are more parameters to configure, config only if neccessary!
		selenium.click("ui=commons::flexiForm_saveButton()");
		//selenium.waitForPageToLoad("30000");
		
	}
}
