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
		By toolbarBy = By.cssSelector("div.o_toolbar.o_portfolio");
		OOGraphene.waitElement(toolbarBy, 5, browser);
		WebElement toolbarEl = browser.findElement(toolbarBy);
		Assert.assertTrue(toolbarEl.isDisplayed());
		return this;
	}
	
	public BindersPage openMyBinders() {
		return new BindersPage(browser);
	}

}
