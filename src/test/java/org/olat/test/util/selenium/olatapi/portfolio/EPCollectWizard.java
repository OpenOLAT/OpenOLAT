/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.test.util.selenium.olatapi.portfolio;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * abstraction for the ePortfolio artefact collect wizard
 * possible steps in wizard are: 
 *  1. Inhalt erfassen
 *  2. Metadaten erfassen
 *  3. Tags angeben
 *  4. Urheberrecht
 *  5. Reflexion erfassen
 *  6. Mappe auswählen
 * not all of them are available depending on artefact-type
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectWizard extends OLATSeleniumWrapper {

	public EPCollectWizard(Selenium selenium) {
		super(selenium);
	}
	
	// wizzard to collect olat internal sources has 3 steps only as long as no map exists
	public void filloutWizardForOLATSource(String title, String description) {
		filloutStep2(title, description);
		filloutStep3();
		filloutStep5(description);		
	}

	public void filloutWizardForTextArtefact(String content, String title, String description) {
		// step 1: text content
		selenium.type("ui=epCollectDialog::description()", content);
		selenium.click("ui=commons::flexiForm_wizzard_next()");
		selenium.waitForPageToLoad("30000");
		filloutStep2(title, description);
		filloutStep3();
		filloutStep4();
		filloutStep5(description);
	}

	/**
	 * @param title
	 * @param description
	 */
	private void filloutStep2(String title, String description) {
		// step 2: metadata
		selenium.type("ui=epCollectDialog::title()", title);
		selenium.type("ui=epCollectDialog::description()", description);
		selenium.click("ui=commons::flexiForm_wizzard_next()");
		selenium.waitForPageToLoad("30000");
	}

	private void filloutStep3() {
		// step 3: tagging
		selenium.click("ui=epCollectDialog::taginput()");
		for (int i = 0; i < 3; i++) {
			selenium.typeKeys("ui=epCollectDialog::taginput()", "test" + i);
			sleepThread(3000);
			if (selenium.isElementPresent("ui=epCollectDialog::taginputFeed()")) {
				selenium.click("ui=epCollectDialog::taginputFeed()");
			}
		}
		selenium.click("ui=commons::flexiForm_wizzard_next()");
		selenium.waitForPageToLoad("30000");
	}

	private void filloutStep4() {
		// step 4: check the copyright checkbox
		selenium.check("ui=epCollectDialog::copyrightCheckbox()");
		selenium.click("ui=commons::flexiForm_wizzard_next()");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * @param description represents the reflexion
	 */
	private void filloutStep5(String description) {
		// step 5: reflexion and proceed with finishing wizard
		selenium.type("ui=epCollectDialog::description()", "reflexion: " + description);
		selenium.click("ui=commons::flexiForm_wizzard_finish()");
		selenium.waitForPageToLoad("30000");
	}
	

	
}
