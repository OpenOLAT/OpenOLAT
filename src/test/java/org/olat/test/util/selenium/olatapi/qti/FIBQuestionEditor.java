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

import com.thoughtworks.selenium.Selenium;

/**
 * This is a Gap-text or FILL-in-BLANK QuestionEditor with 2 modes: Test and Questionnaire. 
 * (The class might be splitted later in specific types.)
 * <p>
 * Used for tests/questionnaires editing. 
 * The Questionnaire functionality is a subset of the Test mode.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class FIBQuestionEditor extends QuestionEditor {
	
	/**
	 * @param selenium
	 */
	public FIBQuestionEditor(Selenium selenium) {
		super(selenium);
	}

	/**
	 * Adds new blank in the Question/answer tab of the selected GAP_TEXT question.
	 *
	 */
	public void addNewBlank() {
		selectQuestionAndAnswersTab();
		selenium.click("ui=testEditor::content_questionAnswers_addNewBlank()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Only for tests!
	 * Inserts the solution text in the blank field of the selected GAP_TEXT question.
	 * @param newText
	 * @param indexOfElement
	 */
	public void setBlankSolution(String newText, int indexOfElement) {
		//TODO: implement parameter indexOfElement
		selenium.type("ui=testEditor::content_questionAnswers_blankField(indexOfElement=" + String.valueOf(indexOfElement) + ")", newText);
		selenium.click("ui=testEditor::content_questionAnswers_save()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Add new text fragment in the Question/answer tab of the selected GAP_TEXT question.
	 *
	 */	
	public void addNewTextFragment() {
		selectQuestionAndAnswersTab();
		selenium.click("ui=testEditor::content_questionAnswers_addNewTextFragment()");
		selenium.waitForPageToLoad("30000");
	}
		
	/**
	 * Edit text element with the given index for the GAP_TEXT question type, while in Question/answers tab. 
	 * @param index
	 * @param text
	 */
	public void editTextFragment(int index, String text) {
		selenium.click("ui=testEditor::content_questionAnswers_editAnswerRest(indexOfAnswer=" + index + ")");
		selenium.waitForPageToLoad("30000");
		editRichText(text);
	}
	
	/**
	 * Only for tests!
	 * Question/answer tab of the GAP_TEXT question type.
	 * 
	 * @param elemIndex
	 */
	public void changeCapitalization(int elemIndex) {
		selectQuestionAndAnswersTab();
		selenium.click("ui=testEditor::content_questionAnswers_capitalization(indexOfElement=" + String.valueOf(elemIndex) + ")");
		selenium.click("ui=testEditor::content_questionAnswers_save()");
		selenium.waitForPageToLoad("30000");
	}
}
