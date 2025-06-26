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
package org.olat.selenium.page.core;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 17 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationPage {
	
	private WebDriver browser;
	
	public OrganisationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public OrganisationPage assertOnMetadata() {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_organisation_metadata_form"), browser);
		return this;
	}
	
	public OrganisationPage openAddressList() {
		By billingAddressBy = By.xpath("//div[contains(@class,'o_segments')]/a[contains(@onclick,'organisation.billing.addresses')]");
		OOGraphene.waitElement(billingAddressBy, browser).click();
		OOGraphene.waitElement(By.cssSelector(".o_sel_billing_address_list"), browser);
		
		return this;
	}
	
	public OrganisationPage addAddress(String identifier, String name1, String addressLine1, String city, String country) {
		By addBy = By.cssSelector("a.o_sel_billing_address_add");
		OOGraphene.waitElement(addBy, browser).click();
		OOGraphene.waitModalDialog(browser, ".o_sel_billing_address_form");
		
		By identifierBy = By.cssSelector("fieldset.o_sel_billing_address_form .o_sel_billing_address_identifier input[type='text']");
		browser.findElement(identifierBy).sendKeys(identifier);
		
		By nameBy = By.cssSelector("fieldset.o_sel_billing_address_form .o_sel_billing_address_name_line1 input[type='text']");
		browser.findElement(nameBy).sendKeys(name1);
		
		By addressBy = By.cssSelector("fieldset.o_sel_billing_address_form .o_sel_billing_address_line1 input[type='text']");
		browser.findElement(addressBy).sendKeys(addressLine1);
		
		By cityBy = By.cssSelector("fieldset.o_sel_billing_address_form .o_sel_billing_address_city input[type='text']");
		browser.findElement(cityBy).sendKeys(city);
		
		By countryBy = By.cssSelector("fieldset.o_sel_billing_address_form .o_sel_billing_address_country input[type='text']");
		browser.findElement(countryBy).sendKeys(country);
		return this;
	}
	
	public OrganisationPage saveAddress() {
		By saveBy = By.cssSelector("dialog .o_sel_billing_address_buttons button.btn.o_button_dirty");
		OOGraphene.waitElement(saveBy, browser).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
