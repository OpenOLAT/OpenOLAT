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
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPage {
	
	public static final By portfolioBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation");

	private final WebDriver browser;

	public BinderPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BinderPage assertOnBinder() {
		By navigationBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation");
		OOGraphene.waitElement(navigationBy, browser);
		WebElement navigationEl = browser.findElement(navigationBy);
		Assert.assertTrue(navigationEl.isDisplayed());
		return this;
	}
	
	public BinderPage assertOnSectionTitleInEntries(String title) {
		By sectionTitleBy = By.xpath("//div[contains(@class,'o_portfolio_section')]//h3[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	public BinderPage assertOnPageInEntries(String title) {
		By sectionTitleBy = By.xpath("//div[contains(@class,'o_portfolio_page')]//h4[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	public BinderPage assertOnPageInToc(String title) {
		By sectionTitleBy = By.xpath("//a[contains(@class,'o_pf_open_entry')]/span[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	/**
	 * Assert that the page is opened in the page view.
	 * 
	 * @param title
	 * @return
	 */
	public BinderPage assertOnPage(String title) {
		By metaTitleBy = By.xpath("//div[contains(@class,'o_page_lead')]//h2[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(metaTitleBy, 5, browser);
		return this;
	}
	
	/**
	 * Assert that no pages in the entries is listed
	 * @return
	 */
	public BinderPage assertNoPagesInEntries() {
		By metaTitleBy = By.xpath("//div[contains(@class,'o_page_lead')]//h2");
		List<WebElement> titleEls = browser.findElements(metaTitleBy);
		Assert.assertTrue(titleEls.isEmpty());
		return this;
	}
	
	public BinderPage assertOnAssignmentInEntries(String title) {
		By assignmentTitleBy = By.xpath("//h4[i[contains(@class,'o_icon_assignment')]][contains(text(),'" + title + "')]");
		OOGraphene.waitElement(assignmentTitleBy, 5, browser);
		return this;
	}
	
	/**
	 * Select the table of content segment of the navigation
	 * @return Itself
	 */
	public BinderPage selectTableOfContent() {
		By tocLinkBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation .o_sel_pf_toc");
		browser.findElement(tocLinkBy).click();
		OOGraphene.waitBusy(browser);
		By tocBy = By.cssSelector("div.o_portfolio_toc");
		OOGraphene.waitElement(tocBy, 5, browser);
		return this;
	}
	
	/**
	 * Select the entries segment of the navigation
	 * 
	 * @return Itself
	 */
	public BinderPage selectEntries() {
		By tocBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation .o_sel_pf_entries");
		browser.findElement(tocBy).click();
		OOGraphene.waitBusy(browser);
		By binderPageListBy = By.cssSelector("div.o_portfolio_entries");
		OOGraphene.waitElement(binderPageListBy, 5, browser);
		return this;
	}
	
	public BinderPublicationPage selectPublish() {
		By publishBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation .o_sel_pf_publication");
		browser.findElement(publishBy).click();
		OOGraphene.waitBusy(browser);
		By binderPageListBy = By.cssSelector("div.o_portfolio_publication");
		OOGraphene.waitElement(binderPageListBy, 5, browser);
		return new BinderPublicationPage(browser);
	}
	
	/**
	 * Create a section in the tab entries (this is one must
	 * be selected).
	 * 
	 * @param title
	 * @return
	 */
	public BinderPage createSectionInEntries(String title) {
		createSection(title);
		assertOnSectionTitleInEntries(title);
		return this;
	}
	
	public BinderPage createSection(String title) {
		//click create button
		By createBy = By.className("o_sel_pf_new_section");
		browser.findElement(createBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_section_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_section_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_section_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderPage createEntry(String title) {
		return createEntry(title, -1);
	}
	
	public BinderPage createEntry(String title, int sectionIndex) {
		//click create button
		By createBy = By.className("o_sel_pf_new_entry");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_entry_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_entry_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		
		if(sectionIndex > 0) {
			By sectionBy = By.cssSelector(".o_sel_pf_edit_entry_section select");
			WebElement sectionEl = browser.findElement(sectionBy);
			new Select(sectionEl).selectByIndex(sectionIndex);
		}
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_entry_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderPage createAssignmentForSection(String sectionTitle, String title, String summary, String content) {
		By newAssignmentBy = By.xpath("//div[contains(@class,'o_portfolio_section')][h3[contains(text(),'" + sectionTitle + "')]]//a[contains(@class,'o_sel_pf_new_assignment')]");
		List<WebElement> newAssignmentButtons = browser.findElements(newAssignmentBy);
		Assert.assertEquals(1, newAssignmentButtons.size());
		newAssignmentButtons.get(0).click();
		
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_assignment_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_assignment_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		OOGraphene.tinymce(summary, ".o_sel_pf_edit_assignment_summary", browser);
		OOGraphene.tinymce(content, ".o_sel_pf_edit_assignment_content", browser);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_assignment_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BinderPage pickAssignment(String assignmentTitle) {
		By assignmentButton = By.xpath("//div[contains(@class,'o_portfolio_assignments')][div/h4[contains(text(),'" + assignmentTitle + "')]]//a[contains(@class,'btn')]");
		browser.findElement(assignmentButton).click();
		OOGraphene.waitBusy(browser);
		assertOnPage(assignmentTitle);
		return this;
	}
	
	/**
	 * Select the entry in the table of content.
	 * 
	 * @param title The title of the entry to select
	 * @return Itself
	 */
	public BinderPage selectEntryInToc(String title) {
		By entryLinkBy = By.xpath("//a[contains(@class,' o_pf_open_entry')][span[contains(text(),'" + title + "')]]");
		browser.findElement(entryLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the entry by its title in the entry list
	 * 
	 * @param title The title of the entry
	 * @return Itself
	 */
	public BinderPage selectEntryInEntries(String title) {
		By entryLinkBy = By.xpath("//div[contains(@class,'o_portfolio_page')][div/h4[contains(text(),'" + title + "')]]/div[contains(@class,'o_portfolio_page_links')]/a[contains(@class,'btn')]");
		OOGraphene.waitElement(entryLinkBy, 5, browser);
		browser.findElement(entryLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Publish the entry in the page view.
	 * 
	 * @return Itself
	 */
	public BinderPage publishEntry() {
		By publishBy = By.cssSelector("a.o_sel_pf_publish_entry");
		browser.findElement(publishBy).click();
		OOGraphene.waitBusy(browser);
		confirm();
		return this;
	}
	
	/**
	 * Yes in a dialog box controller.
	 */
	private void confirm() {
		// confirm
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-footer a");
		OOGraphene.waitElement(confirmButtonBy, 5, browser);
		List<WebElement> buttonsEl = browser.findElements(confirmButtonBy);
		buttonsEl.get(0).click();
		OOGraphene.waitBusy(browser);
	}
}
