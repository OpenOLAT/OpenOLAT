package org.olat.test.util.selenium.olatapi.qti;

import org.olat.test.util.selenium.olatapi.qti.QuestionEditor.QUESTION_TYPES;

import com.thoughtworks.selenium.Selenium;

/**
 * Subclass of the TestEditor. 
 * It allows to add section nodes or question nodes of the type: SINGLE_CHOICE, MULTIPLE_CHOICE, GAP_TEXT, and ESSAY.
 * It offers an EssayQuestionEditor more than the TestEditor.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class QuestionnaireEditor extends TestEditor {

	public QuestionnaireEditor(Selenium selenium) {
		super(selenium);

	  //Check that we're on the right place
		if(!selenium.isElementPresent("ui=testEditor::toolbox_add_addText()")) {
			//it must have an add Text link
			throw new IllegalStateException("This is not the - QuestionnaireEditor - page");
		}
	}

	/**
	 * QUESTION_TYPES: SINGLE_CHOICE, MULTIPLE_CHOICE, GAP_TEXT, and ESSAY.
	 */
	protected void clickAddQuestion(QUESTION_TYPES type) {
		if(QUESTION_TYPES.SINGLE_CHOICE.equals(type)) {
		  selenium.click("ui=testEditor::toolbox_add_addSingleChoice()");
		} else if(QUESTION_TYPES.MULTIPLE_CHOICE.equals(type)) {
			selenium.click("ui=testEditor::toolbox_add_addMultipleChoice()");
		} else if(QUESTION_TYPES.GAP_TEXT.equals(type)) {
			selenium.click("ui=testEditor::toolbox_add_addGapText()");
		} else if(QUESTION_TYPES.ESSAY.equals(type)) {
		  selenium.click("ui=testEditor::toolbox_add_addText()");
		} 
	}
	
	/**
	 * QUESTION_TYPES: SINGLE_CHOICE, MULTIPLE_CHOICE, GAP_TEXT, and ESSAY.
	 */
	protected QuestionEditor returnQuestionEditor(QUESTION_TYPES type) {
		if(QUESTION_TYPES.SINGLE_CHOICE.equals(type)) {
			return new SCQuestionEditor(selenium);
		} else if(QUESTION_TYPES.MULTIPLE_CHOICE.equals(type)) {
			return new MCQuestionEditor(selenium);
		} else if(QUESTION_TYPES.GAP_TEXT.equals(type)) {
			return new FIBQuestionEditor(selenium);
		} else if(QUESTION_TYPES.ESSAY.equals(type)) {
			return new EssayQuestionEditor(selenium);
		} 
		return new QuestionEditor(selenium);
	}
	
	/**
	 * Checks the question type.
	 * QUESTION_TYPES: SINGLE_CHOICE, MULTIPLE_CHOICE, GAP_TEXT, and ESSAY.
	 * @return Returns null if no question type identified.
	 */
	protected QuestionEditor getCurrentQuestion() {
		if(selenium.isElementPresent("ui=testEditor::content_metadata_scType()")) {
			return new SCQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_mcType()")) {
			return new MCQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_gapType()")) {
			return new FIBQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_essayType()")) {
      return new EssayQuestionEditor(selenium); 
		}  
		throw new IllegalStateException("This is not a - QuestionEditor - page!");
	}
}
