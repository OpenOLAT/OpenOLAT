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
package org.olat.selenium.page.core;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 24 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SitesPage {
	
	private final WebDriver browser;
	
	public SitesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Assert on the list of sites to enable or disable them
	 * 
	 * @return Itself
	 */
	public SitesPage assertOnSites() {
		By sitesBy = By.cssSelector(".o_sel_sites table");
		OOGraphene.waitElement(sitesBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @return Itself
	 */
	public SitesPage enableCatalogAdmin() {
		By enableBy = By.xpath("//div[contains(@class,'o_sel_sites')]//table//td//input[@type='checkbox'][@name='site.enable.olatsites_catalogadmin']");
		OOGraphene.scrollBottom(enableBy, browser);
		WebElement enableEl = OOGraphene.waitElement(enableBy, browser);
		OOGraphene.check(enableEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		By enabledBy = By.xpath("//div[contains(@class,'o_sel_sites')]//table//td//input[@type='checkbox'][@name='site.enable.olatsites_catalogadmin'][@checked='checked']");
		OOGraphene.waitElement(enabledBy, browser);
		return this;
	}
	
	public SitesPage enableCatalogV1() {
		By enableBy = By.xpath("//div[contains(@class,'o_sel_sites')]//table//td//input[@type='checkbox'][@name='site.enable.olatsites_catalog']");
		WebElement enableEl = OOGraphene.waitElement(enableBy, browser);
		OOGraphene.check(enableEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		By enabledBy = By.xpath("//div[contains(@class,'o_sel_sites')]//table//td//input[@type='checkbox'][@name='site.enable.olatsites_catalog'][@checked='checked']");
		OOGraphene.waitElement(enabledBy, browser);
		return this;
	}
}
