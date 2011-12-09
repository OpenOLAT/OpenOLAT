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
package org.olat.test.util.selenium.olatapi.qti;

import com.thoughtworks.selenium.Selenium;

/**
 * This is an Text/Essay QuestionEditor, used only for Questionnaire. 
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class EssayQuestionEditor extends QuestionEditor {

	public EssayQuestionEditor(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Set essay's answer lettersPerLine and numberOfLines, save.
	 * 
	 * @param lettersPerLine
	 * @param numberOfLines
	 */
	public void setAnswerSize(int lettersPerLine, int numberOfLines) {
		selectQuestionAndAnswersTab();
		selenium.type("ui=testEditor::content_questionAnswers_essayLettersPerLine()", String.valueOf(lettersPerLine));		
		selenium.type("ui=testEditor::content_questionAnswers_essayNumberOfLines()", String.valueOf(numberOfLines));	
		selenium.click("ui=commons::saveInput()");				
		selenium.waitForPageToLoad("30000");
	}
	
}
