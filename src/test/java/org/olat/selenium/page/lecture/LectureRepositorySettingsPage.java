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
package org.olat.selenium.page.lecture;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 15 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositorySettingsPage {
	
	private WebDriver browser;
	
	public LectureRepositorySettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LectureRepositorySettingsPage enableLectures() {
		By enableBy = By.xpath("//label/input[@name='lecture.admin.enabled' and @value='on']");
		OOGraphene.waitElement(enableBy, browser);
		
		WebElement checkboxEl = browser.findElement(enableBy);
		OOGraphene.check(checkboxEl, Boolean.TRUE);
		
		By overrideBy = By.cssSelector("div.o_sel_repo_lecture_override input[type=radio]");
		OOGraphene.waitElement(overrideBy, browser);
		return this;
	}
	
	public LectureRepositorySettingsPage overrideDefaultSettings() {
		By overrideBy = By.xpath("//label[input[@name='config.override' and @value='yes']]");
		OOGraphene.waitElement(overrideBy, browser);
		browser.findElement(overrideBy).click();
		By rollcallEnabledBy = By.xpath("//label/input[@name='config.rollcall.enabled' and not(@disabled='disabled')]");
		OOGraphene.waitElement(rollcallEnabledBy, browser);
		return this;
	}
	
	public LectureRepositorySettingsPage saveSettings() {
		By saveBy = By.cssSelector("fieldset.o_sel_repo_lecture_settings_form button.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}

}
