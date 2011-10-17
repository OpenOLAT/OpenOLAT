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
