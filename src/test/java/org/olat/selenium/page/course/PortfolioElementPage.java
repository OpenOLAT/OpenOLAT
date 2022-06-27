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

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.BinderPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 31.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioElementPage {
	
	private WebDriver browser;
	
	public PortfolioElementPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PortfolioElementPage pickPortfolio() {
		By pickBy = By.cssSelector("a.btn.o_sel_ep_new_map_template");
		OOGraphene.waitElement(pickBy, 5, browser);
		browser.findElement(pickBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public BinderPage goToPortfolioV2() {
		By openBy = By.cssSelector("a.o_sel_ep_select_map");
		OOGraphene.waitElement(openBy, 5, browser);
		browser.findElement(openBy).click();
		BinderPage binderPage = new BinderPage(browser);
		binderPage.assertOnBinder();
		return binderPage;
	}

}
