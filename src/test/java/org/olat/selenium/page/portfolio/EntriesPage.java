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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 3 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EntriesPage {
	
	private final WebDriver browser;

	public EntriesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EntryPage newPage(String title) {
		//click create button
		By createBy = By.className("o_sel_pf_new_entry");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_entry_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_entry_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_entry_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return new EntryPage(browser);
	}
	
	public EntryPage selectPageInTableFlatView(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_binder_page_listing')]/table//td/a[contains(text(),'" + title + "')]");
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		return new EntryPage(browser);
	}
	
	public EntriesPage assertOnPage(String title) {
		By pageTitleBy = By.xpath("//div[contains(@class,'o_portfolio_page')]/div/h4[contains(text(),'" + title + "')]");
		List<WebElement> pageTitleEls = browser.findElements(pageTitleBy);
		Assert.assertEquals(1, pageTitleEls.size());
		return this;
	}
	
	/**
	 * Select an entry if the table is in flat mode (without the
	 * tree structure for sections).
	 * 
	 * @param title The title
	 * @return Itself
	 */
	public EntriesPage assertOnPageTableFlatView(String title) {
		By pageTitleBy = By.xpath("//div[contains(@class,'o_binder_page_listing')]/table//a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(pageTitleBy, browser);
		List<WebElement> pageTitleEls = browser.findElements(pageTitleBy);
		Assert.assertEquals(1, pageTitleEls.size());
		return this;
	}
	
	public EntriesPage switchTableView() {
		By tableViewBy = By.xpath("//a[i[contains(@class,'o_icon o_icon_table o_icon-lg')]]");
		browser.findElement(tableViewBy).click();
		OOGraphene.waitBusy(browser);
		
		By classicViewBy = By.cssSelector(".o_rendertype_classic");
		OOGraphene.waitElement(classicViewBy, browser);
		return this;
	}
	
	public EntriesPage assertEmptyTableView() {
		By emptyMessageBy = By.cssSelector("div.o_portfolio_entries div.o_empty_state");
		OOGraphene.waitElement(emptyMessageBy, browser);
		return this;
	}
	
	public EntriesPage restore(String title, String binder, String section) {
		By restoreBy = By.xpath("//table//tr[td/a[contains(text(),'" + title + "')]]/td/a[contains(@onclick,'restore')]");
		browser.findElement(restoreBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By selectBinderBy = By.cssSelector("#o_cobinders_SELBOX select");
		WebElement selectBinderEl = browser.findElement(selectBinderBy);
		new Select(selectBinderEl).selectByVisibleText(binder);
		OOGraphene.waitBusy(browser);

		By selectSectionBy = By.cssSelector("#o_cosections_SELBOX select");
		OOGraphene.waitElement(selectSectionBy, browser);
		WebElement selectSectionEl = browser.findElement(selectSectionBy);
		new Select(selectSectionEl).selectByVisibleText(section);
		
		By restoreButtonBy = By.cssSelector("div.modal-dialog button");
		browser.findElement(restoreButtonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
}
