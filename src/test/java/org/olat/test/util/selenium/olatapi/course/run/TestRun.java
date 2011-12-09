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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.course.run;

import java.util.Iterator;
import java.util.Map;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the TestRun page.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class TestRun extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public TestRun(Selenium selenium) {
		super(selenium);
		
		 //Check that we're on the right place
		if(!selenium.isElementPresent("ui=qti::finishTest()")) {
			throw new IllegalStateException("This is not the - Test run - page");
		}
	}

	/**
	 * Finish and close this test.
	 * @return Returns a CourseRun instance.
	 * @throws Exception
	 */
	public TestElement finishTest(boolean assertAchievedScore, int referenceScore) throws Exception {
		selenium.click("ui=qti::finishTest()");
		selenium.waitForPageToLoad("30000");
		assertTrue(selenium.getConfirmation().matches("^Do you really want to submit[\\s\\S]$"));
		//check if results were saved
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");			
			if (selenium.isTextPresent("Your results were saved")) break;							
			Thread.sleep(1000);
		}
		//check the score
		if(assertAchievedScore) {
		  assertEquals(String.valueOf(referenceScore),selenium.getText("ui=qti::achievedScore()"));
		}
    //close test
		selenium.click("ui=qti::closeTest()");
		selenium.waitForPageToLoad("30000");
		return new TestElement(selenium);
	}
	
	/**
	 * Waits for confirmation: your results were saved, and closes test.
	 * The self test finishes silently, no explicit finish test command needed.
	 * @throws Exception
	 */
	public TestElement selfTestFinishedConfirm() throws Exception {
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (selenium.isTextPresent("Your results were saved.")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("ui=qti::closeTest()");
		selenium.waitForPageToLoad("30000");
		return new TestElement(selenium);
	}
	
	/*public TestElement closeTest() {
		selenium.click("ui=qti::closeTest()"); 
		selenium.waitForPageToLoad("30000");
		return new TestElement(selenium);
	}*/
	
	/**
	 * 
	 * @return
	 */
	public CourseRun suspendTest() {
		if(!isSuspendPresent()) {
			throw new IllegalStateException("No suspend test button available!");
		}
		selenium.click("ui=qti::suspendTest()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: confirm suspend
		return new CourseRun(selenium);
	}
	
	/**
	 * 
	 * @return
	 */
	public CourseRun cancelTest() {
		if(!isCancelPresent()) {
			throw new IllegalStateException("No cancel test button available!");
		}
		selenium.click("ui=qti::cancelTest()");
		selenium.waitForPageToLoad("30000");
		//TODO: LD: confirm cancel
		selenium.click("ui=qti::closeTest()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	
	public boolean isSuspendPresent() {
		return selenium.isElementPresent("ui=qti::suspendTest()");
	}
	
	public boolean isCancelPresent() {
		return selenium.isElementPresent("ui=qti::cancelTest()");
	}
	
	public void next() {
		selenium.click("ui=qti::next()");
		selenium.waitForPageToLoad("30000");
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
	 * Select in the KPrim answers. 
	 * The input map contains as keys the answer text and as value a Boolean for correct or not.
	 * 
	 * @param answerToCorrectMap
	 */
	public void setKprimSolution(Map<String, Boolean> answerToCorrectMap) {
		Iterator<String> keyIterator = answerToCorrectMap.keySet().iterator();
		while(keyIterator.hasNext()) {
			String currentAnswer = keyIterator.next();
			Boolean isCorrect = answerToCorrectMap.get(currentAnswer);
			if(isCorrect) {
				selenium.click("ui=qti::testKprimItemFormElementPlus(text=" + currentAnswer + ")");
			} else {
				selenium.click("ui=qti::testKprimItemFormElementMinus(text=" + currentAnswer + ")");
			}
		}				
		selenium.click("ui=qti::saveAnswer()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Select the MultipleChoice answers.
	 * @param answers
	 */
	public void setMultipleChoiceSolution(String[] answers) {
		QTIHelper.setMultipleChoiceSolution(selenium, answers);
	}
	
	/**
	 * Select the answer for the selected SingleChoice question type.
	 * @param answer
	 */
	public void setSingleChoiceSolution (String answer) {
		QTIHelper.setSingleChoiceSolution(selenium, answer);
	}
	
	/**
	 * Selects the menu item with the given name.
	 * @param title
	 */
	public void selectMenuItem(String title) {
		selenium.click("ui=qti::menuItem(titleOfItem=" + title + ")");
		selenium.waitForPageToLoad("30000");
	}
	
}
