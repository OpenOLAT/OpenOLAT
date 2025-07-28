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
 * Drive the catalog tab.
 * 
 * Initial date: 01.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogPage {
	
	private WebDriver browser;
	
	public CatalogPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CatalogPage selectCatalogEntry(String title, String shortTitle) {
		By titleBy = By.xpath("//div[contains(@class,'o_sublevel')]/div/h4[contains(@class,'o_title')]/a[span[text()[contains(.,'" + shortTitle + "')]]]");
		OOGraphene.waitElement(titleBy, browser).click();
		
		By pageTitleBy = By.xpath("//h2[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(pageTitleBy, browser);
		return this;
	}
	
	public CatalogPage selectCatalogEntry(String shortTitle) {
		By titleBy = By.xpath("//div[contains(@class,'o_sublevel')]/div[contains(@class,'o_meta')]/h4/a[span[contains(.,'" + shortTitle + "')]]");
		OOGraphene.waitElement(titleBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CatalogPage select(String title) {
		By titleLinkBy = By.xpath("//h3[contains(@class,'o_title')]//a[span[text()[contains(.,'" + title + "')]]]");
		browser.findElement(titleLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public void start() {
		By startBy = By.className("o_start");
		browser.findElement(startBy).click();
		OOGraphene.waitBusy(browser);
	}

}
