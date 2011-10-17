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
package org.olat.test.util.selenium.olatapi.qti;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is a QuestionEditor. The class might be splitted later in specific types.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class QuestionEditor extends OLATSeleniumWrapper {

  //supported question types
	public enum QUESTION_TYPES {SINGLE_CHOICE, MULTIPLE_CHOICE, KPRIM, GAP_TEXT, ESSAY}
	
	/**
	 * @param selenium
	 */
	public QuestionEditor(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isTextPresent("Meta data")) {			
			throw new IllegalStateException("This is not the - Question Editor- page");
		}
	}
	
	/**
	 * Change question title to newTitle, in Meta data tab.
	 * @param currentTitle
	 * @param newTitle
	 */
	public void setQuestionTitle(String newTitle) {				
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", newTitle);
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
	}
	
	
	/**
	 * Select question with the given title and then the Question/answers tab.
	 * @param title
	 */
	public void selectQuestionAndAnswersTab() {
		if(selenium.isElementPresent("ui=testEditor::content_questionAnswers_tabQuestionAnswers()")) {
			selenium.click("ui=testEditor::content_questionAnswers_tabQuestionAnswers()");
			selenium.waitForPageToLoad("30000");
		}		
	}
	
	/**
	 * Edit question, while in Question/answers tab of the current selected question of the types:
	 * SINGLE_CHOICE, MULTIPLE_CHOICE or KPRIM.
	 * 
	 * @param newText
	 */
	public void editQuestion(String newText) {
		selenium.click("ui=testEditor::content_questionAnswers_editQuestion()");
		selenium.waitForPageToLoad("30000");
		
		editRichText(newText);
	}
	
	protected void editRichText(String text) {
	  // the description shows up in an iframe
		typeInRichText(text);
		selenium.click("ui=commons::flexiForm_saveButtonVertical()");
		selenium.waitForPageToLoad("30000");
	}
	
}
