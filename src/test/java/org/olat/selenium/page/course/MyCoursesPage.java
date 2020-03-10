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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the "My courses" site
 * 
 * Initial date: 07.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyCoursesPage {

	private final WebDriver browser;
	
	public MyCoursesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MyCoursesPage assertOnMyCourses() {
		By myCoursesSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_mycourses_my");
		OOGraphene.waitElement(myCoursesSegmentBy, browser);
		WebElement myCoursesSegmentEl = browser.findElement(myCoursesSegmentBy);
		Assert.assertTrue(myCoursesSegmentEl.isDisplayed());
		return this;
	}
	
	public MyCoursesPage openSearch() {
		By searchBy = By.className("o_sel_mycourses_search");
		WebElement searchLink = browser.findElement(searchBy);
		searchLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MyCoursesPage openCatalog() {
		By catalogBy = By.className("o_sel_mycourses_catlog");
		WebElement catalogLink = browser.findElement(catalogBy);
		catalogLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MyCoursesPage extendedSearch(String title) {
		By extendedSearchButtonBy = By.className("o_sel_flexi_extendedsearch");
		List<WebElement> extendedSearchButtons = browser.findElements(extendedSearchButtonBy);
		if(extendedSearchButtons.size() > 0 && extendedSearchButtons.get(0).isDisplayed()) {
			WebElement extendedSearchButton = extendedSearchButtons.get(0);
			extendedSearchButton.click();
			OOGraphene.waitBusy(browser);
		}

		By titleBy = By.cssSelector(".o_sel_repo_search_displayname input[type='text']");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		By searchButton = By.className("o_sel_repo_search_button");
		WebElement searchEl = browser.findElement(searchButton);
		searchEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select a course (or a respository entry) in a
	 * "My course" list.
	 * 
	 * @param title
	 * @return
	 */
	public MyCoursesPage select(String title) {
		By titleBy = By.xpath("//h4[contains(@class,'o_title')]/a[span[text()[contains(.,'" + title + "')]]]");
		List<WebElement> titleLinks = browser.findElements(titleBy);
		Assert.assertEquals(1, titleLinks.size());
		titleLinks.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Click on the book button of the course specified
	 * by the title in the course list.
	 * 
	 * @param title
	 */
	public void book(String title) {
		By bookingBy = By.cssSelector("a.o_book");
		By rowBy = By.cssSelector("div.o_table_row");
		By titleLinkBy = By.cssSelector("h4.o_title a");
		WebElement linkToBook = null;
		List<WebElement> rows = browser.findElements(rowBy);
		for(WebElement row:rows) {
			WebElement titleLink = row.findElement(titleLinkBy);
			if(titleLink.getText().contains(title)) {
				linkToBook = row.findElement(bookingBy);
			}
		}
		Assert.assertNotNull(linkToBook);
		linkToBook.click();
		OOGraphene.waitBusy(browser);
	}
	
	public MyCoursesPage selectCatalogEntry(String shortTitle) {
		By titleBy = By.xpath("//div[contains(@class,'o_sublevel')][div[contains(@class,'o_meta')]/h4[@class='o_title'][contains(.,'" + shortTitle + "')]]/div/a");
		OOGraphene.waitElement(titleBy, browser);
		List<WebElement> titleLinks = browser.findElements(titleBy);
		Assert.assertEquals(1, titleLinks.size());
		titleLinks.get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public void start() {
		By startBy = By.className("o_start");
		WebElement startLink = browser.findElement(startBy);
		startLink.click();
		OOGraphene.waitBusy(browser);
	}

}
