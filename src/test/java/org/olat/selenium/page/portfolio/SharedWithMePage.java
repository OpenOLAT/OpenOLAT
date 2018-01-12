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
 * Initial date: 30 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedWithMePage {
	
	private final WebDriver browser;

	public SharedWithMePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public SharedWithMePage openSharedBindersWithMe() {
		By sharedBindersBy = By.cssSelector("a.o_sel_shared_binders_seg");
		browser.findElement(sharedBindersBy).click();
		OOGraphene.waitBusy(browser);
		
		By sharedBindersWithMeBy = By.cssSelector("div.o_table_flexi.o_binder_shared_items_listing");
		OOGraphene.waitElement(sharedBindersWithMeBy, 5, browser);
		return this;
	}
	
	public SharedWithMePage assertOnBinder(String title) {
		By binderBy = By.xpath("//div[contains(@class,'o_binder_shared_items_listing')]//td/a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(binderBy, 5, browser);
		WebElement binderEl = browser.findElement(binderBy);
		Assert.assertTrue(binderEl.isDisplayed());
		return this;
	}
	
	public BinderPage selectBinder(String title) {
		By binderBy = By.xpath("//div[contains(@class,'o_binder_shared_items_listing')]//td/a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(binderBy, 5, browser);
		browser.findElement(binderBy).click();
		return new BinderPage(browser);
	}
}