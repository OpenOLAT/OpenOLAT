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
