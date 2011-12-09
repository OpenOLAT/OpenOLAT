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
