package org.olat.test.util.selenium.olatapi.course.run;

import com.thoughtworks.selenium.Selenium;

public class QTIHelper {

	/**
	 * Select the answer for the selected SingleChoice question type.
	 * @param answer
	 */
	public static void setSingleChoiceSolution (Selenium selenium, String answer) {
		selenium.click("ui=qti::testItemFormElement(text=" + answer + ")");
		selenium.click("ui=qti::saveAnswer()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Select the MultipleChoice answers.
	 * @param answers
	 */
	public static void setMultipleChoiceSolution(Selenium selenium, String[] answers) {
		for(String answer:answers) {
			selenium.click("ui=qti::testItemFormElement(text=" + answer + ")");
		}
		selenium.click("ui=qti::saveAnswer()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Fill in the answer for the given text fragment.
	 * TODO: LD: check if it works with more than 1 text fragments.
	 * @param textFragment
	 * @param answer
	 */
	public static void fillInGap(Selenium selenium, String textFragment, String answer) {
		selenium.type("ui=qti::testGapItemFormElement(text=" + textFragment + ")", answer);
		selenium.click("ui=qti::saveAnswer()");
		selenium.waitForPageToLoad("30000");
	}
}
