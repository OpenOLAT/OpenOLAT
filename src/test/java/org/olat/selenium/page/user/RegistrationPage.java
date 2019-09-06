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
package org.olat.selenium.page.user;

import java.util.List;

import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 5 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RegistrationPage {
	
	private final WebDriver browser;
	
	private RegistrationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static RegistrationPage getPage(WebDriver browser) {
		return new RegistrationPage(browser);
	}
	
	public RegistrationPage signIn() {
		By signInBy = By.id("o_co_olat_login_register");
		OOGraphene.waitElement(signInBy, browser);
		browser.findElement(signInBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public RegistrationPage nextToDisclaimer() {
		By languageBy = By.id("o_fioselect_language_SELBOX");
		OOGraphene.waitElement(languageBy, browser);	
		new Select(browser.findElement(languageBy)).selectByValue("en");
		OOGraphene.waitBusy(browser);
		By nextBy = By.cssSelector(".modal-content .modal-body .form-group button.btn.btn-primary");
		browser.findElement(nextBy).click();
		OOGraphene.waitBusy(browser);
		
		// wait disclaimer
		By disclaimerBy = By.cssSelector("fieldset.o_disclaimer");
		OOGraphene.waitElement(disclaimerBy, browser);
		return this;
	}
	
	public RegistrationPage acknowledgeDisclaimer() {
		List<WebElement> disclaimer = browser.findElements(LoginPage.disclaimerXPath);
		disclaimer.get(0).click();
		browser.findElement(LoginPage.disclaimerButtonXPath).click();
		OOGraphene.waitBusy(browser);
		
		By mailBy = By.className("o_sel_registration_email");
		OOGraphene.waitElement(mailBy, browser);
		return this;
	}
	
	public RegistrationPage register(String email) {
		By emailBy = By.cssSelector(".o_sel_registration_email input[type='text']");
		OOGraphene.waitElement(emailBy, browser);
		browser.findElement(emailBy).sendKeys(email);
		
		By sendBy = By.cssSelector("fieldset.o_sel_registration_email_form button.btn.btn-primary");
		browser.findElement(sendBy).click();
		OOGraphene.waitBusy(browser);
		
		By confirmationBy = By.xpath("//p[text()[contains(.,'" + email + "')]]");
		OOGraphene.waitElement(confirmationBy, browser);
		return this;
	}
	
	public String extractRegistrationLink(SmtpMessage message) {
		String body = message.getBody();
		int index = body.indexOf("http");
		if(index >= 0) {
			int lastIndex = body.indexOf(' ', index + 1);
			return body.substring(index, lastIndex);
		}
		return null;
	}
	
	public RegistrationPage loadRegistrationLink(String link) {
		browser.navigate().to(link);
		return this;
	}
	
	public void finalizeRegistration(String firstName, String lastName, String login, String password) {
		By firstNameBy = By.cssSelector(".o_sel_registration_firstName input[type='text']");
		OOGraphene.waitElement(firstNameBy, browser);
		browser.findElement(firstNameBy).sendKeys(firstName);
		
		By lastNameBy = By.cssSelector(".o_sel_registration_lastName input[type='text']");
		browser.findElement(lastNameBy).sendKeys(lastName);
		
		By loginBy = By.cssSelector(".o_sel_registration_login input[type='text']");
		browser.findElement(loginBy).sendKeys(login);
		
		By cred1By = By.cssSelector(".o_sel_registration_cred1 input[type='password']");
		browser.findElement(cred1By).sendKeys(password);
		By cred2By = By.cssSelector(".o_sel_registration_cred2 input[type='password']");
		browser.findElement(cred2By).sendKeys(password);
		
		By finalizeBy = By.cssSelector(".o_sel_registration_2_form button.btn.btn-primary");
		browser.findElement(finalizeBy).click();
		OOGraphene.waitBusy(browser);
		
		By previewBy = By.id("o_preview_details");
		OOGraphene.waitElement(previewBy, browser);
		By toLoginBy = By.cssSelector("#o_main_center_content_inner a.btn.btn-primary");
		browser.findElement(toLoginBy).click();
	}

}
