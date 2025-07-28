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

import org.junit.Assert;
import org.olat.core.util.Formatter;
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
		By myCoursesSegmentBy = By.cssSelector("div.o_sel_my_repository_entries .o_sel_my_courses");
		WebElement myCoursesSegmentEl = OOGraphene.waitElement(myCoursesSegmentBy, browser);
		Assert.assertTrue(myCoursesSegmentEl.isDisplayed());
		return this;
	}
	
	/**
	 * Check the presence of the element in the card view
	 * 
	 * @param title The title of the curriculum element
	 * @return Itself
	 */
	public MyCoursesPage assertOnCurriculumElementInList(String title) {
		title = Formatter.truncateOnly(title, 55);
		By titleBy = By.xpath("//h3[contains(@class,'o_title')]/a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public MyCoursesPage assertOnCurriculumElementDetails(String title) {
		title = Formatter.truncateOnly(title, 55);
		By titleBy = By.xpath("//div[@class='o_curriculum_element_infos']//div[contains(@class,'o_curriculum_element_infos_header')]//h2[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public MyCoursesPage openSearch() {
		By searchBy = By.className("o_sel_mycourses_search");
		browser.findElement(searchBy).click();
		OOGraphene.waitBusy(browser);
		By largeSearchBy = By.className("o_table_large_search");
		OOGraphene.waitElement(largeSearchBy, browser);
		return this;
	}
	
	public MyCoursesPage openInPreparation() {
		By inPreparationScopeBy = By.xpath("//ul/li/button[contains(@class,'o_scope_toggle')][div/span/div/div/i[contains(@class,'o_ac_offer_pending_icon')]]");
		browser.findElement(inPreparationScopeBy).click();
		By inPreparationActiveBy = By.xpath("//ul/li/button[contains(@class,'o_scope_toggle') and contains(@class,'o_toggle_on')][div/span/div/div/i[contains(@class,'o_ac_offer_pending_icon')]]");
		OOGraphene.waitElement(inPreparationActiveBy, browser);
		By courseTableBy = By.className("o_coursetable");
		OOGraphene.waitElement(courseTableBy, browser);
		return this;
	}
	
	public MyCoursesPage openCatalog() {
		By catalogBy = By.className("o_sel_mycourses_catalog");
		browser.findElement(catalogBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public MyCoursesPage extendedSearch(String title) {
		By titleBy = By.cssSelector(".o_table_large_search input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By searchButton = By.cssSelector(".o_table_large_search a.o_table_search_button");
		browser.findElement(searchButton).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select a course (or a repository entry) in a
	 * "My course" list.
	 * 
	 * @param title
	 * @return Itself
	 */
	public MyCoursesPage select(String title) {
		title = Formatter.truncateOnly(title, 55);
		By titleBy = By.xpath("//h3[contains(@class,'o_title')]/a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(titleBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Show more informations
	 * 
	 * @param title Title of the course or curriculum element
	 * @return Itself
	 */
	public MyCoursesPage more(String title) {
		title = Formatter.truncateOnly(title, 55);
		By titleBy = By.xpath("//div[contains(@class,'o_repo_entry_list_item')][div/h3/a/span[text()[contains(.,'" + title + "')]]]/div/div/a[contains(@class,'o_details')][i[contains(@class,'o_icon_details')]]");
		OOGraphene.waitElement(titleBy, browser).click();
		return this;
	}
	
	/**
	 * Click on the book button of the course specified
	 * by the title in the course list.
	 * 
	 * @param title
	 */
	public void book(String title) {
		By bookBy = By.xpath("//div[contains(@class,'o_repo_entry_list_item')][div/h3/a/span[text()[contains(.,'" + title + "')]]]/div/div/a[contains(@class,'o_book')]");
		OOGraphene.waitElement(bookBy, browser).click();
		OOGraphene.waitBusy(browser);
	}
	
	public MyCoursesPage selectCatalogEntry(String shortTitle) {
		By titleBy = By.xpath("//div[contains(@class,'o_sublevel')]/div[contains(@class,'o_meta')]/h4/a[span[contains(.,'" + shortTitle + "')]]");
		OOGraphene.waitElement(titleBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public void start() {
		By startBy = By.className("o_start");
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
	}

}
