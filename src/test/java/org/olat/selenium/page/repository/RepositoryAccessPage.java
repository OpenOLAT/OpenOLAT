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

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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
		By publishStatusBy = By.id("o_fiopublishedStatus_SELBOX");
		OOGraphene.waitElement(publishStatusBy, browser);
		WebElement publishStatusEl = browser.findElement(publishStatusBy);
		Select publishStatusSelect = new Select(publishStatusEl);
		publishStatusSelect.selectByValue("published");

		if(access == UserAccess.registred || access == UserAccess.guest) {
			By allUsersBy = By.xpath("//label/input[@name='cif.allusers' and @value='y']");
			browser.findElement(allUsersBy).click();
			OOGraphene.waitBusy(browser);
		}
		if(access == UserAccess.guest) {
			By guestsBy = By.xpath("//label[input[@name='cif.guests' and @value='y']]");
			By labelGuestsBy = By.xpath("//label/input[@name='cif.guests' and @value='y']");
			OOGraphene.scrollTo(guestsBy, browser);
			
			WebElement guestsEl = browser.findElement(guestsBy);
			WebElement labelGuestsEl = browser.findElement(labelGuestsBy);
			OOGraphene.check(labelGuestsEl, guestsEl, Boolean.TRUE);
		}

		By saveSwitch = By.cssSelector("fieldset.o_sel_repositoryentry_access button.btn.btn-primary");
		browser.findElement(saveSwitch).click();
		OOGraphene.waitBusy(browser);

		if(access == UserAccess.registred || access == UserAccess.guest) {
			By accessConfigurationBy = By.cssSelector("fieldset.o_ac_configuration");
			OOGraphene.waitElement(accessConfigurationBy, browser);
		}
		
		return this;
	}
	
	public BookingPage boooking() {
		By bookingFieldsetBy = By.cssSelector("fieldset.o_ac_configuration");
		List<WebElement> bookingFieldsetEls = browser.findElements(bookingFieldsetBy);
		Assert.assertEquals(1, bookingFieldsetEls.size());
		return new BookingPage(browser);
	}
	
	/**
	 * Click toolbar
	 */
	public void clickToolbarBack() {
		OOGraphene.closeBlueMessageWindow(browser);
		By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
	}
}
