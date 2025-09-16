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
	
	private final WebDriver browser;
	
	public BookingPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Open the dropdown to add an access control method.
	 * @return This page
	 */
	public BookingPage openAddDropMenu() {
		By addDropMenuBy = By.className("o_sel_accesscontrol_create");
		OOGraphene.click(addDropMenuBy, browser);
		return this;
	}
	
	
	/**
	 * Add a guest booking method via the callout menu.
	 * Callout menu need to be open.
	 * 
	 * @return Itself
	 */
	public BookingPage addGuestMethod() {
		addMethodByLink("o_sel_ac_add_guest");
		OOGraphene.waitModalDialog(browser, ".o_sel_accesscontrol_guest_form");
		return this;
	}
	
	/**
	 * Add a public booking method via the callout menu.
	 * Callout menu need to be open.
	 * 
	 * @return Itself
	 */
	public BookingPage addOpenMethod() {
		addMethodByLink("o_sel_ac_add_open");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public BookingPage addOpenAsFirstMethod() {
		addFirstMethodByButton("o_sel_ac_add_open");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * In the dropdown to add an access control method, choose
	 * the method by secret token.
	 * 
	 * @return This page
	 */
	public BookingPage addTokenMethod() {
		addMethodByIcon("o_ac_token_icon");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * In the dropdown to add an access control method, choose
	 * the method by invoice.
	 * 
	 * @return This page
	 */
	public BookingPage addInvoiceMethod() {
		addMethodByIcon("o_ac_invoice_icon");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * Select the free booking option
	 * 
	 * @return Itself
	 */
	public BookingPage addFreeBooking() {
		addMethodByIcon("o_ac_free_icon");
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	private BookingPage addFirstMethodByButton(String linkClassname) {
		By addMethodBy = By.xpath("//fieldset[contains(@class,'o_ac_configuration')]//div[contains(@class,'o_sel_ac_add_first_methods')]//a[contains(@class,'" + linkClassname + "')]");
		OOGraphene.click(addMethodBy, browser);
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	private BookingPage addMethodByLink(String linkClassname) {
		//wait menu
		By addMenuBy = By.cssSelector("fieldset.o_ac_configuration ul.dropdown-menu");
		OOGraphene.waitElement(addMenuBy, browser);
		By addMethodBy = By.xpath("//fieldset[contains(@class,'o_ac_configuration')]//ul[contains(@class,'dropdown-menu')]//a[contains(@class,'" + linkClassname + "')]");
		browser.findElement(addMethodBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	private BookingPage addMethodByIcon(String iconClassname) {
		//wait menu
		By addMenuBy = By.cssSelector("fieldset.o_ac_configuration ul.dropdown-menu");
		OOGraphene.waitElement(addMenuBy, browser);
		By addMethodBy = By.xpath("//fieldset[contains(@class,'o_ac_configuration')]//ul[contains(@class,'dropdown-menu')]//a[i[contains(@class,'" + iconClassname + "')]]");
		browser.findElement(addMethodBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public BookingPage configureGuestMethod(String description) {
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_guest_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);	

		By submitBy = By.cssSelector(".o_sel_accesscontrol_guest_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_accesscontrol_guest_form");
		
		By publicRowBy = By.cssSelector("fieldset.o_ac_configuration div.o_sel_ac_offer>div.o_icon_panel_icon_col>h4>i.o_ac_guest_icon");
		OOGraphene.waitElement(publicRowBy, browser);
		return this;
	}
	
	public BookingPage configureOpenMethod(String description) {
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_open_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);		

		By submitBy = By.cssSelector(".o_sel_accesscontrol_open_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_accesscontrol_open_form");
		
		By publicRowBy = By.cssSelector("fieldset.o_ac_configuration div.o_sel_ac_offer>div.o_icon_panel_content_col>div.o_icon_panel_header>h4");
		OOGraphene.waitElement(publicRowBy, browser);
		return this;
	}
	
	/**
	 * Configure the token, save it and check that the token appears
	 * in the list.
	 * 
	 * @param token The secret token
	 * @param description The description
	 * @return Itself
	 */
	public BookingPage configureTokenMethod(String token, String description) {
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_token_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);		
		By tokenBy = By.cssSelector(".o_sel_accesscontrol_token_form .o_sel_accesscontrol_token input[type='text']");
		browser.findElement(tokenBy).sendKeys(token);

		By submitBy = By.cssSelector(".o_sel_accesscontrol_token_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		By tokenRowBy = By.cssSelector("fieldset.o_ac_configuration div.o_sel_ac_offer>div.o_icon_panel_icon_col>h4>i.o_ac_token_icon");
		OOGraphene.waitElement(tokenRowBy, browser);
		return this;
	}
	
	public void bookToken(String token) {
		By bookBy = By.cssSelector("button.o_button_call_to_action");
		OOGraphene.waitElement(bookBy, browser);
		browser.findElement(bookBy).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By tokenEntryBy = By.cssSelector(".o_sel_accesscontrol_token_entry input[type='text']");
		browser.findElement(tokenEntryBy).sendKeys(token);
		
		By submitBy = By.cssSelector(".modal-dialog .o_method_token button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
	}
	
	/**
	 * Configure the invoice, save it and check that it appears in the list.
	 * 
	 * @param description the description
	 * @return Itself
	 */
	public BookingPage configureInvoiceMethod(String label, String description, String price, boolean adminConfirmation) {
		// 
		By labelBy = By.cssSelector(".o_sel_accesscontrol_label input[type='text']");
		browser.findElement(labelBy).sendKeys(label);
		
		By priceBy = By.cssSelector(".o_sel_accesscontrol_invoice_price input[type='text']");
		browser.findElement(priceBy).sendKeys(price);
		
		// Default is no
		if(adminConfirmation) {
			By confirmationBy =  By.cssSelector(".o_sel_accesscontrol_confirmation_manager input[type='radio'][name='membership.confirmation'][value='confirmation.by.manager.yes']");
			browser.findElement(confirmationBy).click();
			By confirmationCheckBy =  By.cssSelector(".o_sel_accesscontrol_confirmation_manager input[type='radio'][checked='checked'][name='membership.confirmation'][value='confirmation.by.manager.yes']");
			OOGraphene.waitElement(confirmationCheckBy, browser);
		}
		
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_invoice_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);

		By submitBy = By.cssSelector(".o_sel_accesscontrol_invoice_form button.btn-primary");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		
		By invoiceRowBy = By.cssSelector("fieldset.o_ac_configuration div.o_sel_ac_offer>div.o_icon_panel_icon_col>h4>i.o_ac_invoice_icon");
		OOGraphene.waitElement(invoiceRowBy, browser);
		return this;
	}
	
	/**
	 * Save the free booking.
	 * 
	 * @param description The description of the booking.
	 * @return Itself
	 */
	public BookingPage configureFreeBooking(String description, boolean externalCatalog, boolean adminConfirmation) {
		if(externalCatalog) {
			By catalogBy = By.cssSelector(".o_sel_accesscontrol_free_form .o_sel_accesscontrol_publish input[name='offer.publish.in'][value='web']");
			WebElement catalogEl = OOGraphene.waitElement(catalogBy, browser);
			OOGraphene.check(catalogEl, Boolean.TRUE);
			By catalogCheckedBy = By.xpath("//fieldset[contains(@class,'o_sel_accesscontrol_free_form')]//fieldset[contains(@class,'o_sel_accesscontrol_publish')]//input[@checked='checked'][@name='offer.publish.in'][@value='web']");
			OOGraphene.waitElement(catalogCheckedBy, browser);
		}
		
		// Default is no
		if(adminConfirmation) {
			By confirmationBy =  By.cssSelector(".o_sel_accesscontrol_confirmation_manager input[type='radio'][name='membership.confirmation'][value='confirmation.by.manager.yes']");
			browser.findElement(confirmationBy).click();
			By descriptionBy = By.cssSelector(".o_sel_accesscontrol_free_form .o_sel_accesscontrol_description textarea");
			OOGraphene.waitElement(descriptionBy, browser);
			browser.findElement(descriptionBy).sendKeys(description);
		}
		
		By submitBy = By.cssSelector(".o_sel_accesscontrol_free_form button.btn-primary");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public BookingPage configureGuestBooking(String description, boolean externalCatalog) {
		if(externalCatalog) {
			By catalogBy = By.cssSelector(".o_sel_accesscontrol_guest_form .o_sel_accesscontrol_catalog input[name='offer.publish.in'][value='web']");
			WebElement catalogEl = OOGraphene.waitElement(catalogBy, browser);
			OOGraphene.check(catalogEl, Boolean.TRUE);
		}
		
		By descriptionBy = By.cssSelector(".o_sel_accesscontrol_guest_form .o_sel_accesscontrol_description textarea");
		browser.findElement(descriptionBy).sendKeys(description);
		
		By submitBy = By.cssSelector(".o_sel_accesscontrol_guest_form button.btn-primary");
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
		By rowsBy = By.xpath("//fieldset[@class='o_sel_order_list']//table//tr[td/span/i[contains(@class,'o_ac_order_status_payed_icon')]]/td[contains(text(),'" + user.getLastName() + "')]");
		List<WebElement> rows = browser.findElements(rowsBy);
		Assert.assertEquals(1, rows.size());
		return this;
	}
}
