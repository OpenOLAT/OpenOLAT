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
package org.olat.selenium.page.user;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the visiting card of users.
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VisitingCardPage {
	
	private WebDriver browser;
	
	public VisitingCardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public VisitingCardPage assertOnVisitingCard() {
		By visitingCardBy = By.cssSelector("div.o_visitingcard");
		OOGraphene.waitElement(visitingCardBy, 5, browser);
		WebElement visitingCardEl = browser.findElement(visitingCardBy);
		Assert.assertTrue(visitingCardEl.isDisplayed());
		return this;
	}
	
	public VisitingCardPage assertOnLastName(String lastName) {
		By visitingCardBy = By.xpath("//div[contains(@class,'o_visitingcard')]//h2[text()[contains(.,'" + lastName + "')]]");
		OOGraphene.waitElement(visitingCardBy, 5, browser);
		WebElement visitingCardEl = browser.findElement(visitingCardBy);
		Assert.assertTrue(visitingCardEl.isDisplayed());
		return this;
	}

}
