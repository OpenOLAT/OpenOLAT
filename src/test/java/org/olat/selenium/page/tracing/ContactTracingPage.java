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
package org.olat.selenium.page.tracing;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 17 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContactTracingPage {

	private final WebDriver browser;
	
	public ContactTracingPage(WebDriver browser) {
		this.browser = browser; 
	}
	
	/**
	 * Load and assert a contact tracing URL
	 * 
	 * @param url The contact tracing URL
	 * @return Itself
	 */
	public ContactTracingPage load(String url) {
		browser.navigate().to(url);
		return assertOnContactTracing();
	}
	
	/**
	 * Assert on the selection page of the contract tracing.
	 * 
	 * @return Itself
	 */
	public ContactTracingPage assertOnContactTracing() {
		By loginBy = By.cssSelector("form>fieldset.o_sel_contacttracing_login");
		OOGraphene.waitElement(loginBy, browser);
		return this;
	}
	
	/**
	 * Choose the "guest" option.
	 * 
	 * @return Itself
	 */
	public ContactTracingPage asGuest() {
		By guestBy = By.cssSelector("fieldset.o_sel_contacttracing_login a.o_sel_contacttracing_guest");
		browser.findElement(guestBy).click();
		OOGraphene.waitBusy(browser);
		By generalBy = By.cssSelector("fieldset.o_sel_contacttracing_general");
		OOGraphene.waitElement(generalBy, browser);
		return this;
	}
	
	/**
	 * Choose the "login" option.
	 * 
	 * @return Itself
	 */
	public LoginPage asAuthenticatedUser() {
		By loginBy = By.cssSelector("fieldset.o_sel_contacttracing_login button.btn.btn-primary");
		browser.findElement(loginBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(LoginPage.loginFormBy, browser);
		return new LoginPage(browser);
	}
	
	/**
	 * Fill the form with personal data.
	 * 
	 * @param firstName The first name
	 * @param lastName The last name
	 * @return Itself
	 */
	public ContactTracingPage fillIdentification(String firstName, String lastName) {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contacttracing_identification"), browser);
		
		if(StringHelper.containsNonWhitespace(firstName)) {
			By nameBy = By.cssSelector("fieldset.o_sel_contacttracing_identification div.o_sel_contacttracing_firstname input[type='text']");
			browser.findElement(nameBy).sendKeys(firstName);
		}
		if(StringHelper.containsNonWhitespace(lastName)) {
			By nameBy = By.cssSelector("fieldset.o_sel_contacttracing_identification div.o_sel_contacttracing_lastname input[type='text']");
			browser.findElement(nameBy).sendKeys(lastName);
		}
		return this;
	}
	
	/**
	 * Fill the contact information.
	 * 
	 * @param email The e-mail
	 * @param mobilePhone The mobile phone number
	 * @return Itself
	 */
	public ContactTracingPage fillContact(String email, String mobilePhone) {
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contacttracing_contact"), browser);
		
		if(StringHelper.containsNonWhitespace(email)) {
			By mailBy = By.cssSelector("fieldset.o_sel_contacttracing_contact div.o_sel_contacttracing_email input[type='text']");
			browser.findElement(mailBy).sendKeys(email);
		}
		if(StringHelper.containsNonWhitespace(mobilePhone)) {
			By phoneBy = By.cssSelector("fieldset.o_sel_contacttracing_contact div.o_sel_contacttracing_mobile input[type='text']");
			browser.findElement(phoneBy).sendKeys(mobilePhone);
		}
		return this;
	}
	
	/**
	 * Send the contact tracing's form.
	 * 
	 * @return Itself
	 */
	public ContactTracingPage send() {
		By saveBy = By.cssSelector("fieldset.o_sel_contacttracing_contact button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return assertSent();
	}
	
	/**
	 * Assert that the form was successfully sent.
	 * 
	 * @return Itself
	 */
	public ContactTracingPage assertSent() {
		By detailsBy = By.cssSelector("div.o_ct_confirmation_wrapper div.o_ct_confirmation_location_details");
		OOGraphene.waitElement(detailsBy, browser);
		return this;
	}
}
