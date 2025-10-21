/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.repository;

import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 25 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogV2Page {
	
	private WebDriver browser;
	
	public CatalogV2Page(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * The header is there.
	 * 
	 * @return Itself
	 */
	public CatalogV2Page assertOnCatalog() {
		By catalogHeaderBy = By.cssSelector(".o_catalog2 .o_catalog_search_header");
		OOGraphene.waitElement(catalogHeaderBy, browser);
		return this;
	}
	
	/**
	 * List the offers available with the link under the search field.
	 * 
	 * @return Itself
	 */
	public CatalogV2Page exploreOffers() {
		By listBy = By.cssSelector(".o_catalog2 .o_sel_catalog_explore > a");
		OOGraphene.waitElement(listBy, browser).click();
		return this;
	}
	
	/**
	 * Select a course
	 * 
	 * @param title The course title
	 * @return Itself
	 */
	public CatalogV2Page visitCourse(String title) {
		By courseBy = By.xpath("//div[contains(@class,'o_repo_entry_list_item')][div/a/h3[text()[contains(.,'" + title + "')]]]//a[contains(@class,'o_catalog_start')]");
		OOGraphene.waitElement(courseBy, browser).click();
		return this;
	}
	
	public CatalogV2Page openCourse(String title) {
		By courseBy = By.xpath("//div[contains(@class,'o_repo_entry_list_item')][div/a/h3[text()[contains(.,'" + title + "')]]]//a[contains(@class,'o_catalog_start')]");
		OOGraphene.waitElement(courseBy, browser).click();
		return this;
	}
	
	public CatalogV2Page login(UserVO user) {
		new LoginPage(browser).loginAs(user.getLogin(), user.getPassword());

		try {
			By landingPointBy = By.cssSelector(".o_navbar_button a>i.o_icon_login");
			OOGraphene.waitElementDisappears(landingPointBy, 10, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Login catalog", browser);
			throw e;
		}
		return this;
	}
	
	public CatalogV2Page startCourse(String title) {
		By courseBy = By.xpath("//div[contains(@class,'o_repo_details')]//div[@class='o_meta_wrapper'][div/h2[text()[contains(.,'" + title + "')]]]//a[contains(@class,'o_start') and contains(@class,'o_button_call_to_action')]");		
		OOGraphene.waitElement(courseBy, browser).click();
		return this;
	}

}
