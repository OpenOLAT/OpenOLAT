package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Questionnaire course element.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class QuestionnaireElement extends OLATSeleniumWrapper {

	public QuestionnaireElement(Selenium selenium) {
		super(selenium);
	  // TODO : LD: add check: where am I?
	}

	/**
	 * A questionnaire could be started only once!
	 * @return
	 */
	public QuestionnaireRun start() {
		if(selenium.isElementPresent("ui=commons::start()")) {
			selenium.click("ui=commons::start()");
			selenium.waitForPageToLoad("30000");
			return new QuestionnaireRun(selenium);
		} else {
			throw new IllegalStateException("Cannot start questionnaire!");
		}
	}
	
	public boolean cannotStartAnymore() {
    return !selenium.isElementPresent("ui=commons::start()");
  }
}
