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
		OOGraphene.waitModalDialog(browser);
		
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_binder_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_binder_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		OOGraphene.tinymce(summary, ".o_sel_pf_edit_binder_summary", browser);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_binder_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		
		By contentBy = By.cssSelector(".o_portfolio_content");
		OOGraphene.waitElement(contentBy, 5, browser);
		return new BinderPage(browser);
	}
	
	public BinderPage selectBinder(String title) {
		By binderBy = By.xpath("//div[contains(@class,'o_binder')][div/h4[contains(text(),'" + title + "')]]//div[contains(@class,'pull-right')]/a");
		browser.findElement(binderBy).click();
		OOGraphene.waitBusy(browser);
		return new BinderPage(browser);
	}
	
	public BindersPage switchDeletedBindersTableView() {
		By tableViewBy = By.xpath("//div[contains(@class,'o_table_tools')]/div/a[contains(@class,'o_sel_table')]");
		browser.findElement(tableViewBy).click();
		OOGraphene.waitBusy(browser);
		
		By classicViewBy = By.cssSelector(".o_portfolio_deleted_listing.o_rendertype_classic");
		OOGraphene.waitElement(classicViewBy, browser);
		return this;
	}
	
	/**
	 * Only check the presence of the message "table empty" in the binders list
	 * 
	 * @return
	 */
	public BindersPage assertEmptyTableView() {
		By emptyMessageBy = By.cssSelector("div.o_segments_content div.o_empty_state");
		OOGraphene.waitElement(emptyMessageBy, browser);
		return this;
	}
	
	public BinderPage selectBinderInTableView(String title) {
		By binderBy = By.xpath("//table//tr/td/a[contains(text(),'" + title + "')]");
		browser.findElement(binderBy).click();
		OOGraphene.waitBusy(browser);
		return new BinderPage(browser);
	}
	
	public BindersPage restoreBinder(String title) {
		By restoreBy = By.xpath("//table//tr[td/a[contains(text(),'" + title + "')]]/td/a[contains(@onclick,'restore')]");
		browser.findElement(restoreBy).click();
		OOGraphene.waitBusy(browser);
		
		By confirmButtonBy = By.xpath("//div[contains(@class,'modal-dialo')]//div[contains(@class,'modal-footer')]/a[contains(@onclick,'link_0')]");
		OOGraphene.waitElement(confirmButtonBy, 5, browser);
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
}
