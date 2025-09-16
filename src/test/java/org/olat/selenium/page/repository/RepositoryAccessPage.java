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

import org.olat.repository.RepositoryEntryRuntimeType;
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
	
	/**
	 * Select the option for the runtime type.
	 * @param type
	 * @return
	 */
	public RepositoryAccessPage setRuntimeType(RepositoryEntryRuntimeType type) {
		By runtimeTypeBy = By.xpath("//fieldset[contains(@class,'o_ac_runtime_type_form')]//a[contains(@class,'o_sel_repo_change_runtime_type')]");
		OOGraphene.waitElement(runtimeTypeBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		
		By typeBy = By.xpath("//div[@id='o_cocif_runtime_type']/fieldset//label/input[@name='cif.runtime.type' and @value='" + type + "']");
		browser.findElement(typeBy).click();
		
		By saveBy = By.cssSelector("fieldset.o_sel_edit_runtime_type_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		String icon = type == RepositoryEntryRuntimeType.standalone ? "o_icon_people" : "o_icon_link";
		By accessBy = By.cssSelector("fieldset.o_ac_runtime_type_form i." + icon);
		OOGraphene.waitElement(accessBy, browser);
		return this;
	}
	
	/**
	 * Select the option public only.
	 * 
	 * @param access Type of access
	 * @return Itself
	 */
	public RepositoryAccessPage setAccessToPublic() {
		By allUsersBy = By.xpath("//fieldset[@id='o_coentry_access_type']//label/input[@name='entry.access.type' and @value='public']");
		browser.findElement(allUsersBy).click();
		return this;
	}
	
	public RepositoryAccessPage setAccessToMembersOnly() {
		By allUsersBy = By.xpath("//fieldset[@id='o_coentry_access_type']//label/input[@name='entry.access.type' and @value='private']");
		browser.findElement(allUsersBy).click();
		return this;
	}
	
	public RepositoryAccessPage setMetadataIndex() {
		By metadataBy = By.xpath("//fieldset[contains(@class,'o_sel_repo_access_metadata_index')]//label/input[@name='cif.metadata.enabled' and @value='indexing']");
		WebElement metadataEl = browser.findElement(metadataBy);
		OOGraphene.check(metadataEl, Boolean.TRUE);
		return this;
	}
	
	public RepositoryAccessPage assertOnOaiWarning() {
		By oaiWarningBy = By.cssSelector(".o_sel_repo_access_general .o_sel_repo_oai_warning");
		OOGraphene.waitElement(oaiWarningBy, browser);
		return this;
	}
	
	public RepositoryAccessPage setStandaloneAccessToRegisteredUser() {
		setRuntimeType(RepositoryEntryRuntimeType.standalone)
			.setAccessToPublic()
			.save()
			.selectModalOpenBooking("Hello");
		return this;
	}
	
	/**
	 * Add a public access for registered user to a learn
	 * resource with the open booking method.
	 * 
	 * @return Itself
	 */
	public RepositoryAccessPage setAccessToRegisteredUser() {
		setAccessToPublic()
			.save()
			.selectModalOpenBooking("Hello");
		return this;
	}
	
	public RepositoryAccessPage setAccessWithFreeBooking(String message) {
		setAccessToPublic()
			.save()
			.selectModalFreeBooking(message);
		return this;
	}
	
	public RepositoryAccessPage setAccessWithTokenBooking(String token, String message) {
		setAccessToPublic()
			.save()
			.selectModalTokenBooking(token, message);
		return this;
	}
	
	public RepositoryAccessPage editAccessForGuest(String message, boolean externalCatalog) {
		By editBy = By.xpath("//div[contains(@class,'o_sel_ac_offer')]//a[contains(@class,'btn-default')][i[contains(@class,'o_icon_edit')]]");
		OOGraphene.waitElement(editBy, browser).click();

		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_accesscontrol_guest_form");
		
		new BookingPage(browser)
				.configureGuestBooking(message, externalCatalog);
		return this;
	}
	
	public RepositoryAccessPage save() {
		By saveSwitch = By.cssSelector("fieldset.o_sel_repo_access_configuration button.btn.btn-primary");
		OOGraphene.click(saveSwitch, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public RepositoryAccessPage selectModalOpenBooking(String description) {
		OOGraphene.waitModalDialog(browser);
		
		// select open
		By openBy = By.xpath("//div[@class='modal-content']//input[@value='open.access']");
		OOGraphene.waitElement(openBy, browser).click();
		
		// save
		By saveBy = By.cssSelector("div.o_ac_billing_address_selection div.buttons button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElementRefreshed(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		
		// wait second popup
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_accesscontrol_open_form");
		// configure method
		new BookingPage(browser)
			.configureOpenMethod(description);

		return this;
	}
	
	public RepositoryAccessPage selectModalFreeBooking(String message) {
		OOGraphene.waitModalDialog(browser);
		
		// select open
		By openBy = By.xpath("//div[@class='modal-content']//input[@value='free.method']");
		OOGraphene.waitElement(openBy, browser).click();
		
		// save
		By saveBy = By.cssSelector("div.o_ac_billing_address_selection div.buttons button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElementRefreshed(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		
		// wait second popup
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_accesscontrol_free_form");
		// configure method
		new BookingPage(browser)
			.configureFreeBooking(message, false, false);

		return this;
	}
	
	public RepositoryAccessPage selectModalTokenBooking(String token, String message) {
		OOGraphene.waitModalDialog(browser);
		
		// select open
		By openBy = By.xpath("//div[@class='modal-content']//input[@value='token.method']");
		OOGraphene.waitElement(openBy, browser).click();
		
		// save
		By saveBy = By.cssSelector("div.o_ac_billing_address_selection div.buttons button.btn.btn-primary.o_button_dirty");
		OOGraphene.waitElementRefreshed(saveBy, browser);
		OOGraphene.click(saveBy, browser);
		
		// wait second popup
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_accesscontrol_token_form");
		// configure method
		new BookingPage(browser)
			.configureTokenMethod(token, message);

		return this;
		
	}

	public RepositoryAccessPage cleanBlueBox() {
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}

	public BookingPage booking() {
		By bookingFieldsetBy = By.cssSelector("fieldset.o_ac_configuration");
		OOGraphene.waitElement(bookingFieldsetBy, browser);
		OOGraphene.scrollBottom(bookingFieldsetBy, browser);
		return new BookingPage(browser);
	}
	
	/**
	 * Click toolbar
	 */
	public void clickToolbarBack() {
		By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
	}
}
