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
 * Initial date: 26 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallWizardPage {
	
	private final WebDriver browser;
	
	public TeacherRollCallWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TeacherRollCallWizardPage assertOnRollCallWizard() {
		By portraitBy = By.cssSelector("div.o_rollcall_wizard");
		OOGraphene.waitElement(portraitBy, browser);
		return this;
	}
	
	public TeacherRollCallWizardPage setAbsence(String lecture) {
		By checkBy = By.xpath("//div[contains(@class,'o_rollcall_wizard')]//table//tr[1]/td[count(//div[contains(@class,'o_rollcall_wizard')]//table//tr/th[a[text()='" + lecture + "']]/preceding-sibling::th)+1]/div/label/input");
		OOGraphene.waitElement(checkBy, browser);
		WebElement checkEl = browser.findElement(checkBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public TeacherRollCallWizardPage next() {
		By nextBy = By.cssSelector("a.o_sel_next");
		browser.findElement(nextBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public TeacherRollCallWizardPage saveAndNext() {
		By saveBy = By.cssSelector("div.o_rollcall_wizard button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public TeacherRollCallWizardPage closeRollCall() {
		By closeBy = By.cssSelector("fieldset.o_sel_lecture_confirm_close_form button.btn-primary");
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
