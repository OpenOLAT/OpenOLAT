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
package org.olat.selenium.page.user;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 26 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserViewPage {
	
	private static final Logger log = Tracing.createLoggerFor(UserViewPage.class);
	
	private final WebDriver browser;
	
	public UserViewPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public UserViewPage assertOnUserEditView(String username) {
		By userInfoBy = By.xpath("//div[contains(@class,'o_user_infos')]//table//tr/td[contains(text(),'" + username + "')]");
		OOGraphene.waitElement(userInfoBy, browser);
		return this;
	}
	
	/**
	 * Click the tool to delete the user.
	 * 
	 * @return Itself
	 */
	public UserViewPage deleteUser() {
		By createBy = By.cssSelector("ul.o_tools a.o_sel_user_delete");
		OOGraphene.waitElement(createBy, browser);
		browser.findElement(createBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Acknowledge and confirm to delete a user.
	 */
	public void confirmDeleteUsers() {
		By confirmCheckBy = By.cssSelector("fieldset.o_sel_confirm_delete_user input[type='checkbox']");
		OOGraphene.waitElement(confirmCheckBy, browser);
		WebElement confirmCheckEl = browser.findElement(confirmCheckBy);
		OOGraphene.check(confirmCheckEl, Boolean.TRUE);
		
		By buttonsBy = By.cssSelector("div.modal-dialog div.modal-body a.btn.o_sel_delete_user");
		browser.findElement(buttonsBy).click();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
	}
	
	public UserViewPage openPasswordsTab() {
		By passwordsTabBy = By.cssSelector("ul.nav > li.o_sel_passwords > a");
		OOGraphene.waitElement(passwordsTabBy, browser);
		browser.findElement(passwordsTabBy).click();
		By overviewBy = By.cssSelector("div.o_tabbed_pane_content div.o_authentication_overview");
		OOGraphene.waitElement(overviewBy, browser);
		return this;
	}
	
	public String sendPasswordLink() {
		By sendPasswordLinkBy = By.xpath("//fieldset[contains(@class,'o_authentication_olat')]//div[@class='o_empty_action']/a[contains(@onclick,'secondary.empty.state')]");
		OOGraphene.waitElement(sendPasswordLinkBy, browser);
		browser.findElement(sendPasswordLinkBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By bodyContentBy = By.cssSelector("fieldset.o_sel_send_token_form .o_sel_send_body textarea");
		OOGraphene.waitElement(bodyContentBy, browser);
		String text = browser.findElement(bodyContentBy).getDomProperty("value");
		
		By sendBy = By.cssSelector("fieldset.o_sel_send_token_form button.btn.btn-primary");
		browser.findElement(sendBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		// The change password link is the first one.
		int index = text.indexOf("a href='");
		if(index > 0) {
			int lastIndex = text.indexOf('\'', index + 10);
			String link = text.substring(index + 8, lastIndex);
			log.debug("Password link: {}", link);
			return link;
		}
		return null;
	}

}
