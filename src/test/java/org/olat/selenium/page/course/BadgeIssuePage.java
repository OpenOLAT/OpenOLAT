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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 16 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgeIssuePage {
	
	private WebDriver browser;
	
	public BadgeIssuePage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BadgeIssuePage selectClass(String className) {
		By classBy = By.cssSelector(".o_sel_openbadges_class select#o_fioform_badge_class_SELBOX");
		WebElement classEl = browser.findElement(classBy);
		new Select(classEl).selectByContainsVisibleText(className);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BadgeIssuePage addRecipient(UserVO user) {
		By addBy = By.cssSelector("a.o_sel_openbadges_add_recipient");
		browser.findElement(addBy).click();
		OOGraphene.waitCallout(browser, "fieldset.o_sel_user_search_form");
		
		// Name
		By nameBy = By.cssSelector(".o_form_auto_completer span.twitter-typeahead>input[type='text']");
		OOGraphene.waitElement(nameBy, browser).sendKeys(user.getLogin());
		
		By selectBy = By.xpath("//div[contains(@class,'o_form_auto_completer')]//div[contains(@class,'tt-menu')]//div[contains(@class,'tt-suggestion') and contains(@class,'tt-selectable')][text()[contains(.,'" + user.getLogin() + "')]]");
		OOGraphene.waitElement(selectBy, browser).click();
		
		OOGraphene.waitCalloutDisappears(browser, "fieldset.o_sel_user_search_form");
		return this;
	}
	
	public BadgeIssuePage award() {
		By awardBy = By.cssSelector("dialog .modal-body button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElement(awardBy, browser).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public BadgeIssuePage assertIssuedBadge(String className, UserVO user) {
		By badgeBy = By.xpath("//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/a[text()[contains(.,'" + className + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}
}
