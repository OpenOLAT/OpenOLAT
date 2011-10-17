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
package org.olat.test.util.selenium.olatapi.course.editor;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Structure Course Element page.
 * @author Lavinia Dumitrescu
 *
 */
public class StructureEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public StructureEditor(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=courseEditor::content_bbStructure_scoreTab()")) {
			throw new IllegalStateException("This is not the - Structure element - page");
		}
	}
	
	public void setMinimumScore(int score) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbStructure_scoreTab()")) {
		  selenium.click("ui=courseEditor::content_bbStructure_scoreTab()");
		  selenium.waitForPageToLoad("30000");	
		}
		selenium.type("ui=courseEditor::content_bbStructure_minimumScore()", String.valueOf(score));
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
	}

}
