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
 * Initial date: 16 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgesAdminPage {
	
	private final WebDriver browser;
	
	public BadgesAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BadgesAdminPage assertOnConfiguration() {
		By configBy = By.cssSelector("fieldset.o_sel_openbadges_admin_form");
		OOGraphene.waitElement(configBy, browser);
		return this;
	}
	
	public BadgesAdminPage openGlobalBadges() {
		By linkBy = By.cssSelector(".o_segments a.o_sel_openbadges_global");
		OOGraphene.waitElement(linkBy, browser);
		browser.findElement(linkBy).click();
		By badgesClassesBy = By.cssSelector("fieldset.o_badge_classes");
		OOGraphene.waitElement(badgesClassesBy, browser);
		return this;
	}
	

	public BadgesAdminPage openIssuedBadges() {
		By linkBy = By.cssSelector(".o_segments a.o_sel_openbadges_issued");
		OOGraphene.waitElement(linkBy, browser);
		browser.findElement(linkBy).click();
		By issuedBadgesBy = By.cssSelector("fieldset.o_badge_classes");
		OOGraphene.waitElement(issuedBadgesBy, browser);
		return this;
	}
	
	public BadgeClassesPage createBadgeClass() {
		return new BadgeClassesPage(browser).createBadgeClass();
	}
	
	public BadgeIssuePage awardNewBadge() {
		By awardBadgeBy = By.cssSelector("a.o_sel_openbadges_issue");
		OOGraphene.waitElement(awardBadgeBy, browser);
		browser.findElement(awardBadgeBy).click();
		OOGraphene.waitModalDialog(browser);
		return new BadgeIssuePage(browser);
	}


}
