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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Drives the catalog administration
 * 
 * Initial date: 08.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogAdminPage {
	
	private final WebDriver browser;
	
	public CatalogAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Add a category to the catalog.
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public CatalogAdminPage addCatalogNode(String title, String shortTitle, String description) {
		//click in toolbox
		By addNodeBy = By.className("o_sel_catalog_add_category");
		OOGraphene.waitElement(addNodeBy, browser);
		browser.findElement(addNodeBy).click();
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitTinymce(browser);
		
		//fill the form
		By titleBy = By.cssSelector(".o_sel_catalog_add_category_popup .o_sel_cat_name input[type='text']");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElement(titleBy).sendKeys(title);
		By shortTitleBy = By.cssSelector(".o_sel_catalog_add_category_popup .o_sel_cat_short_title input[type='text']");
		browser.findElement(shortTitleBy).sendKeys(shortTitle);
		
		OOGraphene.tinymce(description, browser);
		
		//save
		By saveBy = By.cssSelector(".o_sel_catalog_add_category_popup .o_sel_catalog_entry_form_buttons button.btn-primary");
		OOGraphene.moveAndClick(saveBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		By nodeTitleBy = By.xpath("//div[contains(@class,'o_meta')]//h4[contains(@class,'o_title')]/a/span[contains(text(),'" + shortTitle + "')]");
		OOGraphene.waitElement(nodeTitleBy, browser);
		return this;
	}
	
	/**
	 * Select a node to navigate
	 * 
	 * @param title
	 * @return
	 */
	public CatalogAdminPage selectNode(String shortTitle) {
		By titleBy = By.xpath("//div[contains(@class,'o_sublevel')]/div[contains(@class,'o_meta')]/h4[contains(@class,'o_title')]/a[span[text()[contains(.,'" + shortTitle + "')]]]");
		OOGraphene.waitElement(titleBy, browser);
		browser.findElements(titleBy).get(0).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}