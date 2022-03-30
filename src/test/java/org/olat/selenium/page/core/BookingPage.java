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
package org.olat.selenium.page.core;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * This page drive the booking configuration.
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BookingPage {
	
	
	private WebDriver browser;
	
	public BookingPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Open the dropdown to add an access control method.
	 * @return This page
	 */
	public BookingPage openAddDropMenu() {
		By addDropMenuBy = By.className("o_sel_accesscontrol_create");
		browser.findElement(addDropMenuBy).click();
		return this;
	}
	
	/**
	 * In the dropdown to add an access control method, choose
	 * the method by secret token.
	 * 
	 * @return This page
	 */
	public BookingPage addTokenMethod() {
		addMethod("o_ac_token_icon");
		By popupBy = By.cssSelector("div.modal-dialog");
		OOGraphene.waitElement(popupBy, 5, browser);
		return this;
	}
	
	private BookingPage addMethod(String iconClassname) {
		//wait menu
		By addMenuBy = By.cssSelector("fieldset.o_ac_configuration ul.dropdown-menu");
		OOGraphene.waitElement(addMenuBy, browser);
		By addMethodBy = By.xpath("//fieldset[contains(@class,'o_ac_configuration')]//ul[contains(@class,'dropdown-menu')]//a[i[contains(@class,'" + iconClassname + "')]]");
		browser.findElement(addMethodBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public BookingPage configureTokenMethod(String token, String description) {
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_token_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);		
		By tokenBy = By.cssSelector(".o_sel_accesscontrol_token_form .o_sel_accesscontrol_token input[type='text']");
		browser.findElement(tokenBy).sendKeys(token);

		By submitBy = By.cssSelector(".o_sel_accesscontrol_token_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public BookingPage assertOnToken(String token) {
		boolean found = false;
		By infosBy = By.className("o_ac_infos");
		List<WebElement> infos = browser.findElements(infosBy);
		for(WebElement info:infos) {
			if(info.getText().contains(token)) {
				found = true;
			}
		}
		Assert.assertTrue(found);
		return this;
	}
	
	public void bookToken(String token) {
		By tokenEntryBy = By.cssSelector(".o_sel_accesscontrol_token_entry input[type='text']");
		browser.findElement(tokenEntryBy).sendKeys(token);
		
		By submitBy = By.cssSelector(".o_sel_accesscontrol_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	/**
	 * Select the free booking option
	 * 
	 * @return Itself
	 */
	public BookingPage addFreeBooking() {
		addMethod("o_ac_free_icon");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Save the free booking.
	 * 
	 * @param description The description of the booking.
	 * @return Itself
	 */
	public BookingPage configureFreeBooking(String description) {
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_free_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);
		
		By submitBy = By.cssSelector(".o_sel_accesscontrol_free_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public void save() {
		By saveButtonBy = By.cssSelector("form button.btn-primary");
		OOGraphene.moveAndClick(saveButtonBy, browser);
		OOGraphene.waitBusy(browser);
	}
	
	/**
	 * Check if a the booking of a user is in the list
	 * of orders. The assert check by first name and
	 * if the order is ok.
	 * 
	 * @param user
	 * @return
	 */
	public BookingPage assertFirstNameInListIsOk(UserVO user) {
		By rowsBy = By.xpath("//div[contains(@class,'o_sel_order_list')]//table//tr[td/span[i[contains(@class,'o_ac_order_status_payed_icon')]]]/td[contains(text(),'" + user.getLastName() + "')]");
		List<WebElement> rows = browser.findElements(rowsBy);
		Assert.assertEquals(1, rows.size());
		return this;
	}
}
