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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 7 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseInfoPage {
	
	private final WebDriver browser;
	
	public CourseInfoPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CourseInfoPage assertOnTitle(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_info_page')]//div[@class='o_meta']/h2[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public String guestUrl() {
		By shareLinksBy = By.cssSelector("#o_share a.o_button_ghost");
		OOGraphene.waitElement(shareLinksBy, browser).click();
		OOGraphene.waitCallout(browser, ".o_share_links");
		
		By guestUrlBy = By.cssSelector(".o_share_links #copyLink");
		WebElement urlEl = OOGraphene.waitElement(guestUrlBy, browser);
		String url = urlEl.getDomAttribute("href");
		urlEl.click();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		// Remove details + add guest link
		return url.replace("/Infos/0", "") + "?guest=true&lang=de";
	}

}
