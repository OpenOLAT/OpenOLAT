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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
		List<WebElement> titleLinks = browser.findElements(titleBy);
		Assert.assertFalse(titleLinks.isEmpty());
		titleLinks.get(0).click();
		OOGraphene.waitBusy(browser);
		
		By pageTitleBy = By.xpath("//h2[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(pageTitleBy, browser);
		return this;
	}
	
	public CatalogPage select(String title) {
		By titleLinkBy = By.xpath("//h4[contains(@class,'o_title')]//a[span[text()[contains(.,'" + title + "')]]]");
		browser.findElement(titleLinkBy).click();
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
