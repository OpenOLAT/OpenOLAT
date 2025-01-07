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
		By titleBy = By.xpath("//div[contains(@class,'o_repo_details')]//div[contains(@class,'o_repo_header')]//div[@class='o_meta']/h2[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public String guestUrl() {
		By guestUrlBy = By.xpath("//input[@aria-labelledby='o_extlink2']");
		OOGraphene.waitElement(guestUrlBy, browser);
		return browser.findElement(guestUrlBy).getDomAttribute("value");
	}

}
