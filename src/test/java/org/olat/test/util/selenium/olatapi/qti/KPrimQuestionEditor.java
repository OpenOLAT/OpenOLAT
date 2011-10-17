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
 * This is a KPrim QuestionEditor used only for tests editing.
 * (The class might be splitted later in specific types.)
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class KPrimQuestionEditor extends QuestionEditor {

  
	/**
	 * @param selenium
	 */
	public KPrimQuestionEditor(Selenium selenium) {
		super(selenium);
	}
	

	/**
	 * Edits the answer with the answerIndex for the selected KPRIM, 
	 * while in Question/answers tab of the current selected question.
	 * The answerIndex must be greater that 0.
	 * @param newText
	 * @param answerIndex
	 */
	public void editAnswer(String newText, int answerIndex) {
		selectQuestionAndAnswersTab();
		selenium.click("ui=testEditor::content_questionAnswers_editAnswerKprim(indexOfAnswer=" + String.valueOf(answerIndex) + ")");
		selenium.waitForPageToLoad("30000");
		editRichText(newText);
	}
		
	/**
	 * Chooses the correct solution for the current selected KPRIM question.
	 * 
	 * @param firstCorrect
	 * @param secondCorrect
	 * @param thirdCorrect
	 * @param forthCorrect
	 */
	public void setCorrectKprimSolution(boolean firstCorrect, boolean secondCorrect, boolean thirdCorrect, boolean forthCorrect) {
		selectQuestionAndAnswersTab();
		if(firstCorrect) {
		  selenium.click("ui=testEditor::content_questionAnswers_setCorrectKprim(indexOfAnswer=1)");
		}else {
			selenium.click("ui=testEditor::content_questionAnswers_setIncorrectKprim(indexOfAnswer=1)");
		}
		if(secondCorrect) {
		  selenium.click("ui=testEditor::content_questionAnswers_setCorrectKprim(indexOfAnswer=2)");
		}else {
			selenium.click("ui=testEditor::content_questionAnswers_setIncorrectKprim(indexOfAnswer=2)");
		}
		if(thirdCorrect) {
		  selenium.click("ui=testEditor::content_questionAnswers_setCorrectKprim(indexOfAnswer=3)");
		}else {
			selenium.click("ui=testEditor::content_questionAnswers_setIncorrectKprim(indexOfAnswer=3)");
		}
		if(forthCorrect) {
		  selenium.click("ui=testEditor::content_questionAnswers_setCorrectKprim(indexOfAnswer=4)");
		}else {
			selenium.click("ui=testEditor::content_questionAnswers_setIncorrectKprim(indexOfAnswer=4)");
		}		
		selenium.click("ui=testEditor::content_questionAnswers_save()");
		selenium.waitForPageToLoad("30000");
	}	
	
}
