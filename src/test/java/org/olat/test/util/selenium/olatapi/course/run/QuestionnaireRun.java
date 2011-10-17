package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the questionnaire run page.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class QuestionnaireRun extends OLATSeleniumWrapper {

	public QuestionnaireRun(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Press finishQuestionnaire, you cannot start the questionnaire a second time.
	 */
	public CourseRun finish() {
		selenium.click("ui=qti::finishQuestionnaire()");
		selenium.waitForPageToLoad("30000");
		assertTrue(selenium.getConfirmation().matches("^Do you really want to submit[\\s\\S]$"));
		return new CourseRun(selenium);
	}
	
	/**
	 * Press cancel and close, and get to the QuestionnaireElement.
	 */
	public QuestionnaireElement cancel() {
		selenium.click("ui=qti::cancelQuestionnaire()");
		selenium.waitForPageToLoad("30000");
		
		selenium.click("ui=qti::closeTest()");
		selenium.waitForPageToLoad("30000");
		return new QuestionnaireElement(selenium);
	}
	
	/**
	 * Press suspend and get to the QuestionnaireElement.
	 * @return
	 */
	public QuestionnaireElement suspend() {
		selenium.click("ui=qti::suspendQuestionnaire()");
		selenium.waitForPageToLoad("30000");	
		
		return new QuestionnaireElement(selenium);
	}
	
	/**
	 * Selects the menu item with the given name.
	 * @param title
	 */
	public void selectMenuItem(String title) {
		selenium.click("ui=qti::menuItem(titleOfItem=" + title + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	public boolean isSuspendPresent() {
		return selenium.isElementPresent("ui=qti::suspendQuestionnaire()");
	}
	
	public boolean isCancelPresent() {
		return selenium.isElementPresent("ui=qti::cancelQuestionnaire()");
	}
	
	/**
	 * Select the answer for the selected SingleChoice question type.
	 * @param answer
	 */
	public void setSingleChoiceSolution (String answer) {		
		QTIHelper.setSingleChoiceSolution(selenium, answer);
	}
	
	/**
	 * Select the MultipleChoice answers.
	 * @param answers
	 */
	public void setMultipleChoiceSolution(String[] answers) {
		QTIHelper.setMultipleChoiceSolution(selenium, answers);
	}
	
	/**
	 * Fill in the answer for the given text fragment.
	 * TODO: LD: check if it works with more than 1 text fragments.
	 * @param textFragment
	 * @param answer
	 */
	public void fillInGap(String textFragment, String answer) {
		QTIHelper.fillInGap(selenium, textFragment, answer);
	}
	
	/**
	 * Fill in the essay text area.
	 * @param text
	 */
	public void fillInEssay (String text) {
		selenium.type("ui=qti::questionnaireEssayTextArea()", text);
		selenium.click("ui=qti::saveAnswer()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void next() {
		selenium.click("ui=qti::next()");
		selenium.waitForPageToLoad("30000");
	}
}
