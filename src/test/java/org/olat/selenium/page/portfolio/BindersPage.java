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
package org.olat.selenium.page.portfolio;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 01.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BindersPage {
	
	private final WebDriver browser;

	public BindersPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BinderPage createBinder(String title, String summary) {
		By newBinderBy = By.cssSelector("li.o_tool a.o_sel_pf_new_binder");
		browser.findElement(newBinderBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_binder_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_binder_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		By summaryBy = By.cssSelector(".o_sel_pf_edit_binder_summary textarea");
		WebElement summaryEl = browser.findElement(summaryBy);
		summaryEl.sendKeys(summary);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_binder_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		
		By contentBy = By.cssSelector(".o_portfolio_content");
		OOGraphene.waitElement(contentBy, 5, browser);
		return new BinderPage(browser);
	}
}
