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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the portfolio homepage, mostly to jump to the list
 * of binders, entries...
 * 
 * Initial date: 01.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioV2HomePage {
	
	private final WebDriver browser;

	public PortfolioV2HomePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PortfolioV2HomePage assertHome() {
		By toolbarBy = By.cssSelector("div.o_portfolio div.o_toolbar");
		OOGraphene.waitElement(toolbarBy, 5, browser);
		WebElement toolbarEl = browser.findElement(toolbarBy);
		Assert.assertTrue(toolbarEl.isDisplayed());
		return this;
	}
	
	public BindersPage openMyBinders() {
		By myBindersBy = By.cssSelector("a.o_sel_pf_my_binders");
		OOGraphene.waitElement(myBindersBy, 5, browser);
		browser.findElement(myBindersBy).click();
		
		By binderListBy = By.cssSelector("div.o_table_flexi.o_portfolio_listing");
		OOGraphene.waitElement(binderListBy, 5, browser);
		return new BindersPage(browser);
	}
	
	public MediaCenterPage openMediaCenter() {
		By mediaCenterLinkBy = By.cssSelector("a.o_sel_pf_media_center");
		OOGraphene.waitElement(mediaCenterLinkBy, 5, browser);
		browser.findElement(mediaCenterLinkBy).click();
		
		By mediaListBy = By.cssSelector("div.o_portfolio_media_browser div.o_table_flexi");
		OOGraphene.waitElement(mediaListBy, 5, browser);
		return new MediaCenterPage(browser);
	}
	
	public SharedWithMePage openSharedWithMe() {
		By sharedWithMeLinkBy = By.cssSelector("a.o_sel_pf_shared_with_me");
		OOGraphene.waitElement(sharedWithMeLinkBy, 5, browser);
		browser.findElement(sharedWithMeLinkBy).click();
		
		By sharedWithMeBy = By.cssSelector("div.o_binder_shared_overview");
		OOGraphene.waitElement(sharedWithMeBy, 5, browser);
		return new SharedWithMePage(browser);
	}
	
	public EntriesPage openMyEntries() {
		By myEntriesLinkBy = By.cssSelector("a.o_sel_pf_my_entries");
		OOGraphene.waitElement(myEntriesLinkBy, 5, browser);
		browser.findElement(myEntriesLinkBy).click();
		
		By myEntriesBy = By.cssSelector("div.o_portfolio_entries");
		OOGraphene.waitElement(myEntriesBy, browser);
		return new EntriesPage(browser);
	}
	
	public EntriesPage openDeletedEntries() {
		By goToTrashBy = By.cssSelector("a.o_sel_pf_trash");
		OOGraphene.waitElement(goToTrashBy, browser);
		browser.findElement(goToTrashBy).click();
		OOGraphene.waitBusy(browser);
		By entriesBy = By.cssSelector("div.o_portfolio_content div.o_portfolio_entries");
		OOGraphene.waitElement(entriesBy, browser);
		return new EntriesPage(browser);
	}
	
	public BindersPage openDeletedBinders() {
		openDeletedEntries();
		
		By deletedBindersBy = By.xpath("//div[contains(@class,'o_segments')]//a[contains(@onclick,'binders')]");
		OOGraphene.waitElement(deletedBindersBy, 5, browser);
		browser.findElement(deletedBindersBy).click();
		OOGraphene.waitBusy(browser);
		By deletedListingBy = By.cssSelector("div.o_portfolio_listing.o_portfolio_deleted_listing");
		OOGraphene.waitElement(deletedListingBy, browser);
		return new BindersPage(browser);
	}
	
	public PortfolioV2HomePage clickToolbarBack() {
		By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return assertHome();
	}
	
	/**
	 * Click the second crumbs in the bread crumb of a portfolio binder.
	 * 
	 * @return The list of binders
	 */
	public BindersPage clickToolbarCrumbBinders() {
		By crumbBindersBy = By.xpath("//ol[@class='breadcrumb']/li[3]/a");
		browser.findElement(crumbBindersBy).click();
		OOGraphene.waitBusy(browser);
		By listBindersBy = By.id("o_my_binders_list");
		OOGraphene.waitElement(listBindersBy, browser);
		return new BindersPage(browser);
	}
}
