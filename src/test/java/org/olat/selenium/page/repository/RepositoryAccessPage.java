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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryAccessPage {
	
	private WebDriver browser;
	
	public RepositoryAccessPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public RepositoryAccessPage setUserAccess(UserAccess access) {
		if(access == UserAccess.registred || access == UserAccess.guest) {
			By allUsersBy = By.xpath("//div[@id='o_coentry_access_type']/div/label/input[@name='entry.access.type' and @value='shared']");
			browser.findElement(allUsersBy).click();
			OOGraphene.waitBusy(browser);
			
			By guestsBy = By.xpath("//div[contains(@class,'o_sel_repositoryentry_access_guest')]//label/input[@name='entry.access.guest' and @value='on']");
			OOGraphene.waitElement(guestsBy, browser);
			
			if(access == UserAccess.guest) {
				WebElement guestsEl = browser.findElement(guestsBy);
				OOGraphene.check(guestsEl, Boolean.TRUE);
			}
		} else if(access == UserAccess.membersOnly) {
			By allUsersBy = By.xpath("//div[@id='o_coentry_access_type']/div/label/input[@name='entry.access.type' and @value='private']");
			browser.findElement(allUsersBy).click();
			OOGraphene.waitBusy(browser);
		} else if(access == UserAccess.booking) {
			By allUsersBy = By.xpath("//div[@id='o_coentry_access_type']/div/label/input[@name='entry.access.type' and @value='booking']");
			browser.findElement(allUsersBy).click();
			OOGraphene.waitBusy(browser);
			
			By bookingFieldsetBy = By.cssSelector("fieldset.o_ac_configuration");
			OOGraphene.waitElement(bookingFieldsetBy, browser);
		}
		return this;
	}

	public BookingPage boooking() {
		By bookingFieldsetBy = By.cssSelector("fieldset.o_ac_configuration");
		OOGraphene.waitElement(bookingFieldsetBy, browser);
		return new BookingPage(browser);
	}
	
	public RepositoryAccessPage save() {
		By saveSwitch = By.cssSelector("fieldset.o_sel_repo_access_configuration button.btn.btn-primary");
		browser.findElement(saveSwitch).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

	public RepositoryAccessPage cleanBlueBox() {
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Click toolbar
	 */
	public void clickToolbarBack() {
		try {
			By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
			browser.findElement(toolbarBackBy).click();
			OOGraphene.waitBusy(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Toolbar Back", browser);
			throw e;
		}
	}
}
