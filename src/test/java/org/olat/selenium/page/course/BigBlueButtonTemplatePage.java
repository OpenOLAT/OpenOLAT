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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonTemplatePage {
	
	private final WebDriver browser;
	
	public BigBlueButtonTemplatePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BigBlueButtonTemplatePage enableTemplate() {
		By enableBy = By.cssSelector("fieldset.o_sel_bbb_edit_template div.o_sel_bbb_template_enable input[type='checkbox']");
		OOGraphene.waitElement(enableBy, browser);
		WebElement checkEl = browser.findElement(enableBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		return this;
	}
	
	public BigBlueButtonTemplatePage enableGuestLink() {
		By enableGuestBy = By.cssSelector("fieldset.o_sel_bbb_edit_template div.o_sel_bbb_template_external input[type='checkbox']");
		OOGraphene.waitElement(enableGuestBy, browser);
		WebElement checkEl = browser.findElement(enableGuestBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		return this;
	}
	
	public void save() {
		By saveBy = By.cssSelector("div.modal-body fieldset.o_sel_bbb_edit_template button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
	}
}
