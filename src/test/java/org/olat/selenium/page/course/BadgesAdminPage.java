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
import org.olat.user.restapi.UserVO;
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
		OOGraphene.waitElement(linkBy, browser).click();
		By badgesClassesBy = By.cssSelector("fieldset.o_badge_classes");
		OOGraphene.waitElement(badgesClassesBy, browser);
		return this;
	}
	

	public BadgesAdminPage openIssuedBadges() {
		By linkBy = By.cssSelector(".o_segments a.o_sel_openbadges_issued");
		OOGraphene.waitElement(linkBy, browser).click();
		By issuedBadgesBy = By.cssSelector("fieldset.o_badge_classes");
		OOGraphene.waitElement(issuedBadgesBy, browser);
		return this;
	}
	
	public BadgeClassesPage createBadgeClass() {
		return new BadgeClassesPage(browser).createBadgeClass();
	}
	
	public BadgeIssueWizardPage awardNewBadge(String badgeName) {
		By badgeClassBy = By.xpath("//div[contains(@class,'o_sel_badge_classes_list')]//table//tr[td/a[text()[contains(.,'" + badgeName + "')]]]/td[contains(@class,'o_col_action')]/div/a[i[contains(@class,'o_icon_actions')]]"); 
		OOGraphene.waitElement(badgeClassBy, browser).click();
		OOGraphene.waitCallout(browser, " a.o_sel_badges_issue");
		
		By awardBadgeBy = By.cssSelector("ul.o_dropdown > li > a.o_sel_badges_issue");
		OOGraphene.waitElement(awardBadgeBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		return new BadgeIssueWizardPage(browser);
	}
	
	public BadgesAdminPage assertIssuedBadge(String className, UserVO user) {
		By badgeBy = By.xpath("//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/a[text()[contains(.,'" + className + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}
}
